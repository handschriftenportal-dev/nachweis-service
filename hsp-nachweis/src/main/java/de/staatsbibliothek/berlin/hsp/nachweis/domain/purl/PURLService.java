/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
import java.io.Serializable;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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

  private static final long serialVersionUID = -8173889354028723708L;

  private static final Pattern TEMPLATE_PATTERN = Pattern.compile("^http[s]{0,1}:\\/\\/.*\\{0\\}");

  private final SBBPURLGenerator sbbPURLGenerator;
  private final PURLRepository purlRepository;
  private final PURLResolverRepository purlResolverRepository;
  private final KonfigurationBoundary konfigurationBoundary;
  private final Map<DokumentObjektTyp, String> targetTemplates;

  @Inject
  public PURLService(SBBPURLGenerator sbbPURLGenerator,
      PURLRepository purlRepository,
      PURLResolverRepository purlResolverRepository,
      KonfigurationBoundary konfigurationBoundary,
      @ConfigProperty(name = "purl.target.kod") String kodTargetTemplate,
      @ConfigProperty(name = "purl.target.beschreibung") String beschreibungTargetTemplate,
      @ConfigProperty(name = "purl.target.beschreibung_retro") String beschreibungRetroTargetTemplate) {
    this.sbbPURLGenerator = sbbPURLGenerator;
    this.purlRepository = purlRepository;
    this.purlResolverRepository = purlResolverRepository;
    this.konfigurationBoundary = konfigurationBoundary;

    this.targetTemplates = Collections.synchronizedMap(new HashMap<>());

    initTargetTemplate(HSP_OBJECT, kodTargetTemplate);
    initTargetTemplate(HSP_DESCRIPTION, beschreibungTargetTemplate);
    initTargetTemplate(HSP_DESCRIPTION_RETRO, beschreibungRetroTargetTemplate);
  }

  static String getConfigKeyForDokumentObjektTyp(DokumentObjektTyp dokumentObjektTyp) {
    switch (dokumentObjektTyp) {
      case HSP_OBJECT:
        return CONFIG_PURL_TARGET_KOD;
      case HSP_DESCRIPTION:
        return CONFIG_PURL_TARGET_BESCHREIBUNG;
      case HSP_DESCRIPTION_RETRO:
        return CONFIG_PURL_TARGET_BESCHREIBUNG_RETRO;
      default:
        throw new RuntimeException("Invalid DokumentObjektTyp " + dokumentObjektTyp);
    }
  }

  @Override
  @Transactional
  public PURL createNewPURL(String dokumentId, DokumentObjektTyp dokumentObjektTyp) throws PURLException {
    Objects.requireNonNull(dokumentId, "dokumentId is required.");
    Objects.requireNonNull(dokumentObjektTyp, "dokumentObjektTyp is required.");

    log.info("Create new PURL: dokumentId={}, dokumentObjektTyp={}", dokumentId, dokumentObjektTyp);

    Set<PURL> existingPURLS = purlRepository.findByDokumentIdAndPURLTyp(dokumentId, PURLTyp.INTERNAL);
    if (!existingPURLS.isEmpty()) {
      throw new PURLException(getMessage("purlservice_document_has_purltyp", PURLTyp.INTERNAL, dokumentId));
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
        purl.setTarget(createTargetURI(viewModel.getDokumentObjektTyp(), viewModel.getDokumentId()));
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

  @Override
  @Transactional(rollbackOn = Exception.class)
  @TransactionConfiguration(timeout = 3600)
  public Set<PURL> createDBMFile(Set<PURL> purls) throws PURLException {
    Objects.requireNonNull(purls, "purls is required.");

    log.debug("createDBMFile: purls={}", purls.size());

    return purlResolverRepository.createDBMFile(purls);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void updateTargetTemplate(DokumentObjektTyp dokumentObjektTyp, String targetTemplate) throws PURLException {
    Objects.requireNonNull(dokumentObjektTyp, "dokumentObjektTyp is required.");
    Objects.requireNonNull(targetTemplate, "targetTemplate is required.");

    if (!isTemplateValid(targetTemplate)) {
      throw new PURLException(getMessage("purlservice_error_targettemplate_invalid", targetTemplate));
    }

    final String configKey = getConfigKeyForDokumentObjektTyp(dokumentObjektTyp);
    konfigurationBoundary.setWert(configKey, targetTemplate);
    targetTemplates.put(dokumentObjektTyp, targetTemplate);
  }

  @Override
  public String getTargetTemplate(DokumentObjektTyp dokumentObjektTyp) {
    Objects.requireNonNull(dokumentObjektTyp, "dokumentObjektTyp is required.");
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
      throw new PURLException(getMessage("purlservice_error_create_target", dokumentObjektTyp, dokumentId, target), e);
    }
  }

  @Override
  public Map<String, PURL> createPURLMapWithBeschreibungsID(Set<String> beschreibungsID) {

    Map<String, PURL> purlMap = new HashMap<>();

    beschreibungsID.stream().forEach(id -> purlRepository.findByDokumentIdAndPURLTyp(id,PURLTyp.INTERNAL)
        .stream().findFirst().ifPresent(purl -> purlMap.put(id,purl)));

    return purlMap;
  }
}
