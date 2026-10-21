package de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.DIV_TYPE_BESCHREIBUNG;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary.SYSTEM_USERNAME;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import de.digitalcollections.iiif.model.MetadataEntry;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import de.digitalcollections.iiif.model.sharedcanvas.Range;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KatalogBeschreibungReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIBeschreibungCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIKatalogCommand;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KatalogViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.minimaldaten.IdentifikationRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.http.HttpBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.XMLDokument;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.io.ByteArrayInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.tei_c.ns._1.Body;
import org.tei_c.ns._1.Div;
import org.tei_c.ns._1.P;
import org.tei_c.ns._1.Pb;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 28.02.2022.
 * @version 1.0
 */
@Slf4j
@Dependent
public class KatalogService implements KatalogBoundary, Serializable {

  @Serial
  private static final long serialVersionUID = -6231634553640531082L;

  private static final String LABEL_SIGNATUR = "Signatur";
  private static final String PREFIX_PAGE = "Page";
  private static final String PREFIX_MXML = "MXML-";
  private static final String CRLF = "\r\n";
  public static final String VALIDIERUNG_FEHLER_NULL = "validierung_fehler_null";

  private final KatalogRepository katalogRepository;
  private final BeschreibungsRepository beschreibungsRepository;
  private final IdentifikationRepository identifikationRepository;
  private final KafkaIndexingProducer kafkaIndexingProducer;
  private final KulturObjektDokumentService kulturObjektDokumentService;
  private final TeiXmlValidationBoundary teiXmlValidationBoundary;
  private final HttpBoundary httpBoundary;
  private final NormdatenReferenzBoundary normdatenReferenzBoundary;

  @Inject
  public KatalogService(
      KatalogRepository katalogRepository,
      BeschreibungsRepository beschreibungsRepository,
      IdentifikationRepository identifikationRepository,
      KafkaIndexingProducer kafkaIndexingProducer,
      KulturObjektDokumentService kulturObjektDokumentService,
      TeiXmlValidationBoundary teiXmlValidationBoundary,
      HttpBoundary httpBoundary,
      NormdatenReferenzBoundary normdatenReferenzBoundary) {
    this.katalogRepository = katalogRepository;
    this.beschreibungsRepository = beschreibungsRepository;
    this.identifikationRepository = identifikationRepository;
    this.kafkaIndexingProducer = kafkaIndexingProducer;
    this.kulturObjektDokumentService = kulturObjektDokumentService;
    this.teiXmlValidationBoundary = teiXmlValidationBoundary;
    this.httpBoundary = httpBoundary;
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
  }

  @Override
  public Collection<Katalog> findAll() {

    return katalogRepository.findAll().stream().collect(Collectors.toSet());
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public Katalog importieren(Bearbeiter bearbeiter, Katalog katalog)
      throws KatalogException {

    Katalog katalogWork = null;

    try {
      Optional<Katalog> katalogOptional = katalogRepository.findByIdOptional(
          katalog.getId());
      if (katalogOptional.isPresent()) {
        katalogWork = katalogOptional.get();

        katalogWork.setTitle(katalog.getTitle());
        katalogWork.getAutoren().clear();
        katalogWork.getAutoren().addAll(katalog.getAutoren());
        katalogWork.setVerlag(katalog.getVerlag());
        katalogWork.setDigitalisat(katalog.getDigitalisat());
        katalogWork.setPublikationsJahr(katalog.getPublikationsJahr());
        katalogWork.setLizenzUri(katalog.getLizenzUri());
        katalogWork.setTeiXML(katalog.getTeiXML());
        katalogWork.setAenderungsDatum(katalog.getAenderungsDatum());

        updateKatalogAndBeschreibungen(katalogWork,
            ActivityStreamAction.UPDATE);
      } else {
        katalogWork = katalog;

        updateKatalogAndBeschreibungen(katalogWork, ActivityStreamAction.ADD);
      }
    } catch (KatalogException katalogException) {
      throw katalogException;
    } catch (Exception e) {
      log.error("Error in Katalog XML {}",
          katalogWork != null ? katalogWork.getTeiXML() : "");
      throw new KatalogException(
          getMessage("katalog_service_fehler_import", e.getMessage()), e);
    }

    return katalogWork;
  }

  public Optional<KatalogViewModel> buildKatalogViewModel(String katalogId) {
    log.debug("buildKatalogViewModel: katalogId={}", katalogId);

    return katalogRepository
        .findByIdOptional(katalogId)
        .map(
            katalog ->
                KatalogViewModel.map(katalog,
                    beschreibungsRepository.findByKatalogId(katalogId)));
  }

  @Transactional(rollbackOn = {Exception.class})
  void updateKatalogAndBeschreibungen(Katalog katalog,
      ActivityStreamAction addOrUpdate)
      throws Exception {

    Objects.requireNonNull(katalog,
        getMessage(VALIDIERUNG_FEHLER_NULL, "Katalog"));
    Objects.requireNonNull(
        addOrUpdate,
        getMessage(VALIDIERUNG_FEHLER_NULL, "ActivityStreamAction"));

    String kID = "Katalog " + katalog.getId() + ": ";
    Objects.requireNonNull(
        katalog.getTeiXML(),
        getMessage(VALIDIERUNG_FEHLER_NULL, kID + "TEI-XML"));

    updateVerlagsNormdatenReferenz(katalog);
    updateAutorenNormdatenReferenzen(katalog);

    TEIKatalogCommand.updateId(katalog);
    TEIKatalogCommand.updateVerlag(katalog);

    if (katalog.getVerlag() != null && katalog.getVerlag().getId() == null) {
      katalog.setVerlag(null);
    }

    katalog = katalogRepository.saveAndFlush(katalog);

    Set<Beschreibung> updatedBeschreibungen = volltexteUmhaengen(katalog);

    speichern(katalog, updatedBeschreibungen);

    sendMessages(katalog, addOrUpdate, updatedBeschreibungen);
  }

  Optional<URI> findRangeURIForBeschreibung(Beschreibung beschreibung,
      Map<String, URI> allRanges) {
    Optional<URI> rangeURI =
        beschreibung
            .getGueltigeIdentifikation()
            .map(gueltigeIdentifikation -> allRanges.get(
                gueltigeIdentifikation.getIdent()));

    if (rangeURI.isEmpty()) {
      log.info(
          "Found no rangeURI for signature of beschreibung {}, trying kod-signatures.",
          beschreibung.getId());

      rangeURI =
          identifikationRepository
              .getGueltigeSignatureByKodID(beschreibung.getKodID())
              .map(signature -> allRanges.get(signature.getSignature()));

      if (rangeURI.isEmpty()) {
        rangeURI =
            identifikationRepository
                .getAlternativeSignaturesByKodID(beschreibung.getKodID())
                .stream()
                .filter(altSignatur -> allRanges.containsKey(
                    altSignatur.getSignature()))
                .findFirst()
                .map(altSignatur -> allRanges.get(altSignatur.getSignature()));
      }
    }
    return rangeURI;
  }

  Map<String, URI> getAllRangeURIsWithSignatureAsKey(URI manifestURL) throws KatalogException {
    log.info("getAllRangeURIsWithSignatureAsKey for manifestURL {}", manifestURL);

    Optional<Manifest> manifestOptional = loadManifest(manifestURL);
    if (manifestOptional.isEmpty()) {
      log.info("Manifest empty for manifestURL {}", manifestURL);
      return Collections.emptyMap();
    }
    Manifest manifest = manifestOptional.get();
    List<Range> ranges = manifest.getRanges();
    if (ranges == null || ranges.isEmpty()) {
      log.info("No ranges found in manifestID = '{}'",
          manifest.getIdentifier().toASCIIString());
      return Collections.emptyMap();
    }
    Map<String, URI> allRanges = new HashMap<>(ranges.size());
    for (Range range : ranges) {
      List<MetadataEntry> metadata = range.getMetadata();
      if (metadata == null || metadata.isEmpty()) {
        continue;
      }
      for (MetadataEntry metadataEntry : metadata) {
        if (LABEL_SIGNATUR.equals(metadataEntry.getLabelString())) {
          String signature = metadataEntry.getValueString();
          String normalizedSignature = StringUtils.normalizeSpace(signature);

          allRanges.put(normalizedSignature, range.getIdentifier());

          log.debug(
              "Found range {} for Signatur '{}', normalized '{}'",
              range.getIdentifier().toASCIIString(),
              signature,
              normalizedSignature);
          break;
        }
      }
    }

    return allRanges;
  }

  Optional<Manifest> loadManifest(URI manifestURL) throws KatalogException {
    if (Objects.isNull(manifestURL)) {
      log.warn(getMessage("validierung_fehler_null", "manifestURL"));
      return Optional.empty();
    }

    try {
      Optional<String> manifestBody = httpBoundary.loadBodyFromURL(manifestURL);
      if (manifestBody.isEmpty() || manifestBody.get().isEmpty()) {
        log.warn(getMessage("katalog_service_fehler_manifestURL", manifestURL));
        return Optional.empty();
      }

      ObjectMapper iiifMapper = getIIIFObjectMapper();
      return Optional.of(iiifMapper.readValue(manifestBody.get(), Manifest.class));
    } catch (Exception e) {
      String message = getMessage("katalog_service_fehler_manifest",
          manifestURL, e.getMessage());
      log.warn(message, e);
      throw new KatalogException(message, e);
    }
  }

  ObjectMapper getIIIFObjectMapper() {
    final ObjectMapper iiifMapper = new IiifObjectMapper();
    iiifMapper
        .coercionConfigFor(LogicalType.Array)
        .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsEmpty);
    iiifMapper
        .coercionConfigFor(LogicalType.Collection)
        .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsEmpty);
    iiifMapper
        .coercionConfigFor(LogicalType.Map)
        .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsEmpty);
    return iiifMapper;
  }

  Set<Beschreibung> volltexteUmhaengen(Katalog katalog) throws Exception {

    Set<Beschreibung> updatedBeschreibungen = new LinkedHashSet<>();

    Set<Beschreibung> katalogBeschreibungen = new LinkedHashSet<>();
    katalogBeschreibungen.addAll(beschreibungsRepository.findByKatalogId(katalog.getHskID()));
    katalogBeschreibungen.addAll(beschreibungsRepository.findByKatalogId(katalog.getId()));

    Map<String, URI> allRanges =
        getAllRangeURIsWithSignatureAsKey(katalog.getDigitalisat().getManifestURL());
    log.info("HSK: {} KatalogID: {}, allRanges: {}, katalogBeschreibungen: {}", katalog.getHskID(), katalog.getId(),
        allRanges.size(), katalogBeschreibungen.size());

    LocalDateTime aenderungsDatum = LocalDateTime.now();
    katalog.setAenderungsDatum(aenderungsDatum);

    katalog.getBeschreibungen().clear();

    Map<String, Div> volltexte = getVolltextBeschreibungen(katalog);
    Map<String, KatalogBeschreibungReferenz> referenzen = new LinkedHashMap<>();

    for (Entry<String, Div> entry : volltexte.entrySet()) {
      KatalogBeschreibungReferenz referenz =
          createKatalogBeschreibungReferenz(katalog.getId(), entry.getValue());
      referenzen.put(entry.getKey(), referenz);
    }

    for (Beschreibung beschreibung : katalogBeschreibungen) {
      Optional<String> mxmlId =
          beschreibung.getAltIdentifier().stream()
              .filter(
                  altIdentifier ->
                      volltexte.containsKey(altIdentifier)
                          && referenzen.containsKey(altIdentifier))
              .findFirst();

      if (mxmlId.isEmpty()) {
        log.warn("No Volltext found for Beschreibung {}", beschreibung.getId());
        continue;
      }

      Div volltext = volltexte.get(mxmlId.get());
      KatalogBeschreibungReferenz referenz = referenzen.get(mxmlId.get());
      referenz.setBeschreibungsID(beschreibung.getId());
      beschreibung.setAenderungsDatum(aenderungsDatum);

      TEIBeschreibungCommand.openTEIDokument(beschreibung);
      TEIBeschreibungCommand.updateAenderungsdatum(beschreibung);
      TEIBeschreibungCommand.writeXML(beschreibung);
      TEIBeschreibungCommand.updateVolltext(beschreibung, volltext);
      TEIBeschreibungCommand.updateKatalogId(beschreibung, katalog.getId());

      addKatalogDigitalisateToBeschreibung(katalog, beschreibung, allRanges, referenz);

      updatedBeschreibungen.add(beschreibung);
    }

    katalog.getBeschreibungen().addAll(referenzen.values());

    return updatedBeschreibungen;
  }

  void addKatalogDigitalisateToBeschreibung(Katalog katalog, Beschreibung beschreibung, Map<String, URI> allRanges,
      KatalogBeschreibungReferenz referenz) throws Exception {
    Optional<URI> rangeURI = findRangeURIForBeschreibung(beschreibung, allRanges);
    if (rangeURI.isPresent()) {
      referenz.setManifestRangeURL(rangeURI.get());
      TEIBeschreibungCommand.updateKatalogReferences(
          referenz.getManifestRangeURL(), beschreibung, katalog.getDigitalisat().getManifestURL());
    } else {
      log.warn("No ManifestRangeUrl found for Beschreibung {}", beschreibung.getId());
    }
  }

  Map<String, Div> getVolltextBeschreibungen(Katalog katalog) throws Exception {
    if (Objects.isNull(katalog)) {
      throw new KatalogException(getMessage("validierung_fehler_null", "katalog"));
    }
    if (Objects.isNull(katalog.getTeiXML()) || katalog.getTeiXML().isEmpty()) {
      throw new KatalogException(
          getMessage("katalog_service_fehler_kein_teixml", katalog.getId()));
    }

    TEI tei =
        TEIObjectFactory.unmarshalOne(
                new ByteArrayInputStream(
                    katalog.getTeiXML().getBytes(StandardCharsets.UTF_8)))
            .orElseThrow(
                () ->
                    new KatalogException(
                        getMessage("katalog_service_fehler_invalides_teixml",
                            katalog.getId())));

    Body body =
        TEICommon.findFirst(Body.class, tei)
            .orElseThrow(
                () ->
                    new KatalogException(
                        getMessage("katalog_service_fehler_invalides_teixml",
                            katalog.getId())));

    Map<String, Div> volltexte = new LinkedHashMap<>();
    body.getIndicesAndSpenAndSpanGrps().stream()
        .filter(elem -> Div.class.isAssignableFrom(elem.getClass()))
        .map(elem -> (Div) elem)
        .filter(div -> DIV_TYPE_BESCHREIBUNG.equals(div.getType())
            && Objects.nonNull(div.getN()))
        .forEachOrdered(div -> volltexte.put(createMxmlId(div.getN()), div));
    return volltexte;
  }

  String createMxmlId(String value) {
    return Optional.ofNullable(value)
        .map(v -> v.replace(",", "-"))
        .map(v -> PREFIX_MXML + v)
        .orElse(null);
  }

  KatalogBeschreibungReferenz createKatalogBeschreibungReferenz(
      String katalogId, Div div)
      throws Exception {
    String beschreibungsID =
        Optional.ofNullable(div)
            .map(Div::getN)
            .map(n -> n.split(",")[0])
            .orElseThrow(
                () ->
                    new KatalogException(
                        getMessage("katalog_service_fehler_beschreibungId",
                            katalogId)));

    List<Pb> pbs = new ArrayList<>();
    TEICommon.findAll(Pb.class, div, pbs);

    String seiteVon = "";
    String seiteBis = "";
    if (pbs.isEmpty()) {
      List<P> ps = new ArrayList<>();
      TEICommon.findAll(P.class, div, ps);
      if (ps.isEmpty()) {
        log.warn("No PB found for katalog {} page lookup", katalogId);
      } else {
        seiteVon = getSeite(ps.getFirst());
        seiteBis = getSeite(ps.getLast());
      }
    } else {
      seiteVon = getSeite(pbs.getFirst());
      seiteBis = getSeite(pbs.getLast());
    }

    String ocrVolltext = getOcrVolltext(div);

    return new KatalogBeschreibungReferenz(beschreibungsID, seiteVon, seiteBis,
        ocrVolltext, null);
  }

  String getSeite(Pb pb) {
    return Optional.ofNullable(pb).map(Pb::getN)
        .map(n -> n.replace(PREFIX_PAGE, "")).orElse("");
  }

  String getSeite(P p) {
    return Optional.ofNullable(p).map(P::getN)
        .map(n -> n.substring(PREFIX_PAGE.length(), n.indexOf("_"))).orElse("");
  }

  String getOcrVolltext(Div div) throws Exception {
    List<P> blocks = new ArrayList<>();
    TEICommon.findAll(P.class, div, blocks);

    return blocks.stream()
        .map(p -> TEICommon.getContentAsString(p.getContent()))
        .map(StringUtils::normalizeSpace)
        .filter(text -> !text.trim().isEmpty())
        .collect(Collectors.joining(CRLF));
  }

  @Transactional
  void sendMessages(
      Katalog katalog, ActivityStreamAction katalogAction,
      Set<Beschreibung> beschreibungen)
      throws ActivityStreamsException {

    kafkaIndexingProducer.sendKatalogAsActivityStreamMessage(
        katalog, katalogAction, true, SYSTEM_USERNAME);

    for (Beschreibung beschreibung : beschreibungen) {
      var kod = kulturObjektDokumentService.findById(beschreibung.getKodID());
      if (kod.isPresent()) {
        kafkaIndexingProducer.sendKulturobjektDokumentAsActivityStreamMessage(
            kod.get(), ActivityStreamAction.UPDATE, true, SYSTEM_USERNAME);
      }
    }
  }

  @Transactional(rollbackOn = Exception.class)
  void speichern(Katalog katalog, Set<Beschreibung> beschreibungen)
      throws TeiXmlValidationException {

    teiXmlValidationBoundary.validateTeiXml(katalog);
    katalogRepository.saveAndFlush(katalog);

    for (Beschreibung beschreibung : beschreibungen) {
      teiXmlValidationBoundary.validateTeiXml(beschreibung, true);
      beschreibungsRepository.save(beschreibung);
    }
  }

  void updateVerlagsNormdatenReferenz(Katalog katalog) {

    if (katalog.getVerlag() != null && katalog.getVerlag().getGndID() != null) {
      Optional<NormdatenReferenz> updated =
          normdatenReferenzBoundary.findOneByIdOrNameAndType(
              katalog.getVerlag().getGndID(),
              GNDEntityFact.CORPORATE_BODY_TYPE_NAME);

      updated.ifPresent(katalog::setVerlag);
    }
  }

  void updateAutorenNormdatenReferenzen(Katalog katalog) {
    if (katalog.getAutoren() == null) {
      return;
    }

    List<NormdatenReferenz> updatedAutoren = new ArrayList<>();

    for (NormdatenReferenz autor : katalog.getAutoren()) {

      Optional<NormdatenReferenz> updated =
          normdatenReferenzBoundary.findOneByIdOrNameAndType(
              autor.getGndID() != null && !autor.getGndID().isEmpty()
                  ? autor.getGndID()
                  : autor.getName(),
              GNDEntityFact.PERSON_TYPE_NAME);
      updated.ifPresent(updatedAutoren::add);
    }

    katalog.getAutoren().clear();
    katalog.getAutoren().addAll(updatedAutoren);
  }

  @Override
  @Transactional(TxType.REQUIRES_NEW)
  @TransactionConfiguration(timeout = 60000)
  public Set<KatalogValidationReportItem> findAllInvalid() {
    Set<KatalogValidationReportItem> report = new HashSet<>();
    AtomicInteger started = new AtomicInteger();
    AtomicInteger succeeded = new AtomicInteger();

    katalogRepository
        .selectAllTeiXml()
        .forEach(xmlDokument ->
            validateAndCollect(xmlDokument, report, started, succeeded)
        );

    log.info("Validation finished (KATALOG). totals: started={}, succeeded={}, failed={}",
        started.get(), succeeded.get(), started.get() - succeeded.get());

    return report;
  }

  private void validateAndCollect(XMLDokument xmlDokument,
      Set<KatalogValidationReportItem> report,
      AtomicInteger started,
      AtomicInteger succeeded) {
    int startedNow = started.incrementAndGet();
    log.info("Start validation of Katalog {} (started={}, succeeded={})",
        xmlDokument.getId(), startedNow, succeeded.get());

    try {
      ValidationResult validationResult = teiXmlValidationBoundary.validateTeiXml(
          xmlDokument.getTeiXml(),
          String.valueOf(Locale.GERMAN),
          false
      );

      int succeededNow = succeeded.incrementAndGet();
      log.debug("Finished validation of {} (started={}, succeeded={})",
          xmlDokument.getId(), started.get(), succeededNow);

      if (!validationResult.isValid()) {
        Katalog katalog = katalogRepository.findById(xmlDokument.getId());
        if (katalog != null) {
          report.add(buildReportItem(katalog, validationResult));
        } else {
          log.warn("Katalog entity not found by id {}", xmlDokument.getId());
        }
      }

    } catch (Exception exception) {
      log.error("Validation call failed for Katalog {} (started={}, succeeded={}): {}",
          xmlDokument.getId(), started.get(), succeeded.get(), exception.getMessage(), exception);
      throw new IllegalStateException("Validation service unavailable (IMPORTSERVICE/Eureka).", exception);
    }
  }

  private KatalogValidationReportItem buildReportItem(Katalog katalog,
      ValidationResult validationResult) {

    String titel = katalog.getTitle() != null ? katalog.getTitle() : "";
    String publikationsJahr = katalog.getPublikationsJahr() != null
        ? katalog.getPublikationsJahr()
        : "";

    return KatalogValidationReportItem.builder()
        .hspId(katalog.getId())
        .titel(titel)
        .publikationsjahr(publikationsJahr)
        .validationResult(validationResult)
        .build();
  }
}
