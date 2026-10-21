package de.staatsbibliothek.berlin.hsp.nachweis.domain.purl;

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp.HSP_DESCRIPTION;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp.HSP_DESCRIPTION_RETRO;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp.HSP_OBJECT;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.konfiguration.KonfigurationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.purl.SBBPURLGenerator;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 08.08.22
 */
@ApplicationScoped
@Slf4j
public class PURLService implements PURLBoundary, Serializable {

  static final String CONFIG_PURL_TARGET_KOD = "purl.target.kod";
  static final String CONFIG_PURL_TARGET_BESCHREIBUNG = "purl.target.beschreibung";
  static final String CONFIG_PURL_TARGET_BESCHREIBUNG_RETRO = "purl.target.beschreibung_retro";

  @Serial
  private static final long serialVersionUID = -8173889354028723708L;

  private static final Pattern TEMPLATE_PATTERN = Pattern.compile("^https?://.*\\{0}");
  private static final String DOKUMENT_OBJEKT_TYP_IS_REQUIRED = "dokumentObjektTyp is required.";

  private final SBBPURLGenerator sbbPURLGenerator;
  private final PURLRepository purlRepository;
  private final PURLResolverRepository purlResolverRepository;
  private final KonfigurationBoundary konfigurationBoundary;
  private final Map<DokumentObjektTyp, String> targetTemplates;
  private final boolean isResolverEnabled;

  @Inject
  public PURLService(
      SBBPURLGenerator sbbPURLGenerator,
      PURLRepository purlRepository,
      PURLResolverRepository purlResolverRepository,
      KonfigurationBoundary konfigurationBoundary,
      PURLConfiguration purlConfiguration) {
    this.sbbPURLGenerator = sbbPURLGenerator;
    this.purlRepository = purlRepository;
    this.purlResolverRepository = purlResolverRepository;
    this.konfigurationBoundary = konfigurationBoundary;

    this.isResolverEnabled = purlConfiguration.resolver().enabled();
    this.targetTemplates = Collections.synchronizedMap(new HashMap<>());

    initTargetTemplate(HSP_OBJECT, purlConfiguration.target().kodTemplate());
    initTargetTemplate(HSP_DESCRIPTION, purlConfiguration.target().beschreibungTemplate());
    initTargetTemplate(HSP_DESCRIPTION_RETRO,
        purlConfiguration.target().beschreibungRetroTemplate());
  }

  static String getConfigKeyForDokumentObjektTyp(DokumentObjektTyp dokumentObjektTyp) {
    return switch (dokumentObjektTyp) {
      case HSP_OBJECT -> CONFIG_PURL_TARGET_KOD;
      case HSP_DESCRIPTION -> CONFIG_PURL_TARGET_BESCHREIBUNG;
      case HSP_DESCRIPTION_RETRO -> CONFIG_PURL_TARGET_BESCHREIBUNG_RETRO;
    };
  }

  @Override
  public List<PURLViewModel> findAllAsViewModels() {
    return purlRepository.findAllAsViewModels();
  }

  @Override
  @Transactional
  public PURL createNewPURL(String dokumentId, DokumentObjektTyp dokumentObjektTyp)
      throws PURLException {
    Objects.requireNonNull(dokumentId, "dokumentId is required.");
    Objects.requireNonNull(dokumentObjektTyp, DOKUMENT_OBJEKT_TYP_IS_REQUIRED);

    log.info("Create new PURL: dokumentId={}, dokumentObjektTyp={}", dokumentId, dokumentObjektTyp);

    Set<PURL> existingPURLS =
        purlRepository.findByDokumentIdAndPURLTyp(dokumentId, PURLTyp.INTERNAL);
    if (!existingPURLS.isEmpty()) {
      throw new PURLException(
          getMessage("purlservice_document_has_purltyp", PURLTyp.INTERNAL, dokumentId));
    }

    URI target = createTargetURI(dokumentObjektTyp, dokumentId);
    try {
      return sbbPURLGenerator.createNewPURL(target);
    } catch (Exception e) {
      throw new PURLException(
          getMessage("purlservice_error_create_purl", target, e.getLocalizedMessage()), e);
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public int updateInternalPURLs(Collection<PURLViewModel> purlViewModels) throws PURLException {
    Objects.requireNonNull(purlViewModels, "purlViewModels is required.");

    log.info("updateInternalPURLs started ...");
    int updated = 0;

    for (PURLViewModel viewModel : purlViewModels) {
      for (PURL purl : viewModel.getInternalPURLs()) {
        purl.setTarget(
            createTargetURI(viewModel.getDokumentObjektTyp(), viewModel.getDokumentId()));
        purlRepository.save(purl);
        updated++;

        if (updated % 1000 == 0) {
          log.info("updateInternalPURLs: {} purls updated...", updated);
        }
      }
    }
    log.info("updateInternalPURLs finished, updated {} PURLs.", updated);
    return updated;
  }

  Set<PURL> createDBMFile(Set<PURL> purls) throws PURLException {
    Objects.requireNonNull(purls, "purls is required.");

    log.debug("createDBMFile: purls={}", purls.size());

    return purlResolverRepository.createDBMFile(purls);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void updateTargetTemplate(DokumentObjektTyp dokumentObjektTyp, String targetTemplate)
      throws PURLException {
    Objects.requireNonNull(dokumentObjektTyp, DOKUMENT_OBJEKT_TYP_IS_REQUIRED);
    Objects.requireNonNull(targetTemplate, "targetTemplate is required.");

    if (!isTemplateValid(targetTemplate)) {
      throw new PURLException(
          getMessage("purlservice_error_targettemplate_invalid", targetTemplate));
    }

    final String configKey = getConfigKeyForDokumentObjektTyp(dokumentObjektTyp);
    konfigurationBoundary.setWert(configKey, targetTemplate);
    targetTemplates.put(dokumentObjektTyp, targetTemplate);
  }

  @Override
  public String getTargetTemplate(DokumentObjektTyp dokumentObjektTyp) {
    Objects.requireNonNull(dokumentObjektTyp, DOKUMENT_OBJEKT_TYP_IS_REQUIRED);
    return targetTemplates.get(dokumentObjektTyp);
  }

  @Override
  public boolean isTemplateValid(String template) {
    return (Objects.nonNull(template) && TEMPLATE_PATTERN.matcher(template).matches());
  }

  void initTargetTemplate(DokumentObjektTyp dokumentObjektTyp, String defaultValue) {
    String configKey = getConfigKeyForDokumentObjektTyp(dokumentObjektTyp);

    Optional<String> template = konfigurationBoundary.getWert(configKey);
    if (template.isPresent()) {
      targetTemplates.put(dokumentObjektTyp, template.get());
    } else {
      konfigurationBoundary.setWert(configKey, defaultValue);
      targetTemplates.put(dokumentObjektTyp, defaultValue);
    }
  }

  URI createTargetURI(DokumentObjektTyp dokumentObjektTyp, String dokumentId) throws PURLException {
    String targetTemplate = targetTemplates.get(dokumentObjektTyp);
    String target = MessageFormat.format(targetTemplate, dokumentId);
    try {
      return URI.create(target);
    } catch (Exception e) {
      throw new PURLException(
          getMessage("purlservice_error_create_target", dokumentObjektTyp, dokumentId, target), e);
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  @TransactionConfiguration(timeout = 3600)
  public void resolverDateiGenerieren() throws PURLException {
    if (!isResolverEnabled) {
      log.info("resolverDateiGenerieren: PURL resolver file generation is disabled, skipping.");
      return;
    }
    Set<PURL> internalPURLs =
        findAllAsViewModels().stream()
            .flatMap(model -> model.getInternalPURLs().stream())
            .collect(Collectors.toSet());

    Set<PURL> purlsDBM = createDBMFile(internalPURLs);
    log.info(
        "Created Resolver-File for {} purls.", Objects.isNull(purlsDBM) ? 0 : purlsDBM.size());
  }

}
