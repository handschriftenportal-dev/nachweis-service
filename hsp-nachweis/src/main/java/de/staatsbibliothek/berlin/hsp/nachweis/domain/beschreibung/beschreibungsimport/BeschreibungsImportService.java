package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.ActivityStreamMessageFactory.createMessageForImportJob;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsKomponentenTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEI2BeschreibungMapper;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIQuery;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamObjectTagId;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBatchService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsSynchronizer;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport.ImportVorgangBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.neueImporte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.sendIndexMessage;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentNotFoundException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.konfiguration.KonfigurationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamObjectDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamObjectTagDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.PublikationException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.PublikationsService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.DataEntity;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportFile;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportVorgang;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenUpdateService;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.io.ByteArrayInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.tei_c.ns._1.TEI;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 06.06.2019.
 */
@Slf4j
@ApplicationScoped
public class BeschreibungsImportService implements ImportBoundary, Serializable {

  public static final String DATE_TIME_PATTERN = "dd.MM.yyyy HH:mm:ss";
  public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
      DATE_TIME_PATTERN);
  @Serial
  private static final long serialVersionUID = 43621061554989457L;
  private static final String CONFIG_AUTO_UERBERNAHME = "automatische.uebernahme.aktiv";
  private static final String UNBEKANNTER_BENUTZER = "unbekannterBearbeiter";
  private final transient ResourceBundle resourceBundle =
      ResourceBundle.getBundle(I18NController.APPLICATION_MESSAGES,
          I18NController.getLocale());
  private final Map<String, GNDEntityFact> entstehungsortCache = new HashMap<>();

  private final Map<String, NormdatenReferenz> normdatenIdentifikationCache =
      new HashMap<>();
  ImportRepository importRepository;
  @Setter
  private int batchSize = 500;
  private boolean importInternPublishedEnabled;
  private transient BeschreibungsRepository beschreibungsRepository;
  private transient KulturObjektDokumentBoundary kulturObjektDokumentBoundary;
  private transient BeschreibungsBatchService beschreibungsBatchService;
  private transient Event<BeschreibungImport> sendIndexMessageEvent;
  private transient NormdatenReferenzBoundary normdatenReferenzBoundary;
  private transient ImportVorgangBoundary importVorgangBoundary;
  private transient TeiXmlValidationBoundary teiXmlValidationBoundary;
  private String praesentationBaseUrl;
  private transient NormdatenUpdateService normdatenUpdateService;
  private transient KonfigurationBoundary konfigurationBoundary;
  private transient PublikationsService publikationsService;
  private transient BeschreibungsSynchronizer beschreibungsSynchronizer;

  protected BeschreibungsImportService() {
  }

  protected void handleNewImport(Beschreibung beschreibung,
      KulturObjektDokument kulturObjektDokument) {
    if (kulturObjektDokument != null) {
      beschreibung.getAltIdentifier().add(kulturObjektDokument.getId());
    }
    beschreibung.generateHspID();
  }

  @Override
  public List<Beschreibung> convertTEI2Beschreibung(
      ActivityStreamObjectDTO streamObject)
      throws Exception {
    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = new ArrayList<>();

    if (streamObject.getContent() != null) {
      List<TEI> content = TEIObjectFactory.unmarshal(
          new ByteArrayInputStream(streamObject.getContent())
      );
      beschreibungen.addAll(beschreibungMapper.map(content));
    }
    return beschreibungen;
  }

  @Override
  public List<BeschreibungImport> alleImporteAnzeigen() {
    return importRepository.listAll();
  }

  @Override
  public Optional<BeschreibungImport> findById(String id) {
    return importRepository.findByIdOptional(id);
  }

  @Override
  public boolean isAutomatischenUebernahmeAktiv() {
    return konfigurationBoundary
        .getWert(CONFIG_AUTO_UERBERNAHME)
        .map("true"::equals)
        .orElse(Boolean.FALSE);
  }

  @Override
  public void setAutomatischenUebernahmeAktiv(
      boolean automatischenUebernahmeAktiv) {
    log.info(
        "setAutomatischenUebernahmeAktiv: automatischenUebernahmeAktiv={}",
        automatischenUebernahmeAktiv);
    konfigurationBoundary.setWert(
        CONFIG_AUTO_UERBERNAHME, String.valueOf(automatischenUebernahmeAktiv));
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void onImportMessage(@Observes @neueImporte BeschreibungImport beschreibungImportJob) {
    log.info("onImportMessage: received new beschreibungImportJob {}",
        beschreibungImportJob.getId());

    ActivityStreamMessageDTO activityStream = beschreibungImportJob.getMessageDTO();
    ImportVorgang importVorgang = null;

    try {
      importVorgang = importVorgangBoundary.getImportVorgang(activityStream);
      String benutzerName = importVorgang.getBenutzerName();

      if (isAutomatischenUebernahmeAktiv() || UNBEKANNTER_BENUTZER.equals(benutzerName)) {
        log.info("onImportMessage: start automatische Uebernahme for beschreibungImport {}",
            beschreibungImportJob.getId());
        entstehungsortCache.clear();
        normdatenIdentifikationCache.clear();

        VerwaltungsTyp verwaltungstyp = findInternExternTag(activityStream.getObjects())
            .flatMap(t -> VerwaltungsTyp.fromString(t.getName()))
            .orElseThrow(
                () -> new Exception("Automatische Übernahme ohne VerwaltungsTyp nicht möglich!"));

        for (ActivityStreamObjectDTO activityStreamObject : activityStream.getObjects()) {
          beschreibungUebernehmen(importVorgang, activityStreamObject, verwaltungstyp);
        }

        importVorgang.setStatus(IMPORTJOB_RESULT_VALUES.SUCCESS);
        setImportVorgang(activityStream, importVorgang);
        importErfolgreich(beschreibungImportJob);
        importRepository.save(beschreibungImportJob);
        importVorgangBoundary.sendImportJobToKafka(importVorgang);
      } else {
        log.info(
            "onImportMessage: Automatic import not active and user is unknown. Saving job {} for manual processing.",
            beschreibungImportJob.getId());
        importRepository.save(beschreibungImportJob);
      }

    } catch (Exception ex) {
      log.error("Unable to process import {}: {}", beschreibungImportJob.getId(), ex.getMessage(),
          ex);
      handleImportJobError(beschreibungImportJob, importVorgang, ex);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  @TransactionConfiguration(timeout = 7200)
  @Override
  public void importeUebernehmen(String beschreibungImportId,
      String verwaltungsTyp)
      throws BeschreibungsImportException {

    VerwaltungsTyp verwaltungstyp = checkParameter(beschreibungImportId,
        verwaltungsTyp);

    log.info("Importe übernehmen for job {}", beschreibungImportId);

    Optional<BeschreibungImport> optBeschreibungImportJob =
        importRepository.findByIdOptional(beschreibungImportId);

    if (optBeschreibungImportJob.isPresent()) {
      BeschreibungImport beschreibungImportJob = optBeschreibungImportJob.get();

      ImportVorgang importVorgang = null;

      try {
        ActivityStreamMessageDTO activityStream = beschreibungImportJob.getMessageDTO();
        importVorgang = importVorgangBoundary.getImportVorgang(activityStream);

        for (ActivityStreamObjectDTO activityStreamObject : activityStream.getObjects()) {
          beschreibungUebernehmen(importVorgang, activityStreamObject,
              verwaltungstyp);
        }

        handleImportJobSuccess(importVorgang, beschreibungImportJob);
      } catch (Exception ex) {

        log.error("Unable to process import {}", ex.getMessage(), ex);
        handleImportJobError(beschreibungImportJob, importVorgang, ex);

        throw new BeschreibungsImportException(
            "Beschreibungsimport fehlgeschlagen!", ex);
      }
    }
  }

  private void handleImportJobSuccess(
      ImportVorgang importVorgang, BeschreibungImport beschreibungImportJob)
      throws ActivityStreamsException, JsonProcessingException {
    importVorgang.setStatus(IMPORTJOB_RESULT_VALUES.SUCCESS);
    setImportVorgang(beschreibungImportJob.getMessageDTO(), importVorgang);
    importErfolgreich(beschreibungImportJob);
    importVorgangBoundary.sendImportJobToKafka(importVorgang);
  }

  private VerwaltungsTyp checkParameter(String beschreibungImportId,
      String verwaltungsTyp)
      throws BeschreibungsImportException {
    VerwaltungsTyp verwaltungstyp;
    try {
      if (Objects.isNull(beschreibungImportId)
          || beschreibungImportId.isEmpty()) {
        throw new IllegalArgumentException("Wrong beschreibungImportId");
      }

      if (Objects.isNull(verwaltungsTyp) || verwaltungsTyp.isEmpty()) {
        throw new IllegalArgumentException("Wrong Verwaltungstyp");
      }

      verwaltungstyp = VerwaltungsTyp.valueOf(verwaltungsTyp.toUpperCase());
    } catch (Exception error) {
      throw new BeschreibungsImportException(
          resourceBundle.getString("datenuebernahme_couldnotstart"), error);
    }
    return verwaltungstyp;
  }

  @Transactional(rollbackOn = {Exception.class})
  void beschreibungUebernehmen(
      ImportVorgang importVorgang,
      ActivityStreamObjectDTO activityStreamObject,
      VerwaltungsTyp verwaltungstyp)
      throws Exception {

    if (ActivityStreamsDokumentTyp.BESCHREIBUNG.equals(
        activityStreamObject.getType())) {
      LocalDateTime startTime = LocalDateTime.now();
      log.info(
          "Starting Beschreibungsimport at {} for {}",
          DateTimeFormatter.ofPattern(DATE_TIME_PATTERN).format(startTime),
          importVorgang.getName());

      List<Beschreibung> mappedBeschreibungen = convertTEI2Beschreibung(
          activityStreamObject);

      ImportFile importFile =
          findImportFileForActivityStreamObject(importVorgang,
              activityStreamObject);
      if (importFile != null) {
        processBeschreibungen(verwaltungstyp, mappedBeschreibungen, importFile);
      }

      LocalDateTime endTime = LocalDateTime.now();
      log.info(
          "End Beschreibungsimport at {} for {}",
          DateTimeFormatter.ofPattern(DATE_TIME_PATTERN).format(endTime),
          importVorgang.getName());
      log.info(
          "Time (Seconds) needed for Beschreibungsimport {} for {}",
          Duration.between(startTime, endTime).toSeconds(),
          importVorgang.getName());
    }
  }

  protected void processBeschreibungen(
      VerwaltungsTyp verwaltungstyp,
      List<Beschreibung> mappedBeschreibungen,
      ImportFile importFile) throws Exception {

    BatchRecord batchRecords = new BatchRecord(batchSize);

    for (int i = 0; i < mappedBeschreibungen.size(); i++) {
      var newBeschreibung = mappedBeschreibungen.get(i);
      boolean isLast = (i == mappedBeschreibungen.size() - 1);

      newBeschreibung.setVerwaltungsTyp(verwaltungstyp);
      String beschreibungTitel = newBeschreibung.getTitel().orElse("");

      LocalDateTime startTimeBeschreibung = LocalDateTime.now(ZoneId.systemDefault());
      log.debug("Process Beschreibung at {} for {}",
          DATE_TIME_FORMATTER.format(startTimeBeschreibung),
          beschreibungTitel);

      enhanceNormdatenReferenzSprache(newBeschreibung);
      enhanceGrundsprachenAsNormdatenReferenz(newBeschreibung);
      enhanceAutorenNormdatenReferenz(newBeschreibung);

      Optional<Beschreibung> oldBeschreibungState = findExistingBeschreibung(
          newBeschreibung);

      Beschreibung beschreibungForKodLookup = oldBeschreibungState.orElse(newBeschreibung);
      KulturObjektDokument kulturObjektDokument = kulturObjektDokumentBoundary
          .findKodByBeschreibung(beschreibungForKodLookup)
          .orElseThrow(
              () -> new KulturObjektDokumentNotFoundException(
                  "Kulturobjekt konnte mit Minimaldaten von Beschreibung "
                      + beschreibungForKodLookup.formatSignatureLabel()
                      + " nicht gefunden werden!")
          );

      newBeschreibung.setKodID(kulturObjektDokument.getId());

      if (oldBeschreibungState.isPresent()) {
        var oldBeschreibung = oldBeschreibungState.get();
        validateExistingBeschreibungForImport(newBeschreibung, oldBeschreibung);
        handleReImport(newBeschreibung, oldBeschreibung);
      } else {
        handleNewImport(newBeschreibung, kulturObjektDokument);
      }

      enhanceNormdatenReferenzenForGueltigeIdentifikation(newBeschreibung,
          kulturObjektDokument);

      if (newBeschreibung.isPublished()) {
        publikationsService.updateExistingBeschreibung(newBeschreibung);
      } else {
        publikationsService.publizierenOhneApproval(newBeschreibung);
      }

      if (Objects.isNull(newBeschreibung.getAenderungsDatum())) {
        newBeschreibung.setAenderungsDatum(newBeschreibung.getErstellungsDatum());
      }

      kulturObjektDokument.addBeschreibungsdokument(newBeschreibung.getId());

      final List<GNDEntityFact> entstehungsorte = findEntstehungsorteAsEntityFacts(
          newBeschreibung);

      beschreibungsSynchronizer.updateTEIDocuments(kulturObjektDokument, newBeschreibung,
          entstehungsorte);

      log.info("Validate KOD and Beschreibung with ImportService");

      teiXmlValidationBoundary.validateTeiXml(kulturObjektDokument);
      teiXmlValidationBoundary.validateTeiXml(newBeschreibung, true);

      log.info("Save as Batch Beschreibung into System");
      batchRecords.addRecord(kulturObjektDokument, newBeschreibung);

      if (batchRecords.isBatchProcessingNeeded(isLast)) {
        saveKODAndBeschreibungAsBatch(batchRecords, importFile);
      }

      LocalDateTime endTimeBeschreibung = LocalDateTime.now();
      log.debug(
          "Time  needed: {}ms for Beschreibung {}",
          Duration.between(startTimeBeschreibung, endTimeBeschreibung).toMillis(),
          beschreibungTitel);
    }
  }

  private Optional<NormdatenReferenz> findNormdatenReferenz(String id,
      String typ) {

    if (normdatenIdentifikationCache.containsKey(id)) {
      return Optional.of(normdatenIdentifikationCache.get(id));
    }

    var normdatenReferenz = normdatenReferenzBoundary.findOneByIdOrNameAndType(
        id, typ);

    log.info("Found Normdatenreferenz {} with id {} and typ {}",
        normdatenReferenz, id, typ);

    normdatenReferenz.ifPresent(it -> normdatenIdentifikationCache.putIfAbsent(id, it));
    return normdatenReferenz;
  }

  /**
   * Enhances authors with Normdaten references.
   *
   * <p>Authors are resolved by GND-ID or, if unavailable, by the identifier
   * stored in {@link NormdatenReferenz#getId()}, which corresponds to the TEI attribute
   * {@code persName/@key}.
   */
  void enhanceAutorenNormdatenReferenz(Beschreibung beschreibung) {

    Set<NormdatenReferenz> autorenAsNormdaten = new HashSet<>(beschreibung.getAutoren());

    beschreibung.getAutoren().clear();

    autorenAsNormdaten.stream()
        .map(this::lookupAuthor)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(beschreibung.getAutoren()::add);

    log.info(
        "Authors added to beschreibung ({}): {} ",
        beschreibung.getId(),
        beschreibung.getAutoren().stream()
            .map(NormdatenReferenz::getName)
            .collect(Collectors.joining(";")));
  }

  private Optional<NormdatenReferenz> lookupAuthor(NormdatenReferenz author) {
    if (StringUtils.isNotBlank(author.getGndID())) {
      return findNormdatenReferenz(
          author.getGndID(),
          NormdatenReferenz.PERSON_TYPE_NAME);
    } else if (StringUtils.isNotBlank(author.getId())) {
      return findNormdatenReferenz(
          author.getId(),
          NormdatenReferenz.PERSON_TYPE_NAME);
    }
    return Optional.empty();
  }

  void enhanceNormdatenReferenzenForGueltigeIdentifikation(
      Beschreibung beschreibung, KulturObjektDokument kod) {

    if (beschreibung.getGueltigeIdentifikation().isPresent()
        && hasSameBesitzer(beschreibung, kod)) {

      beschreibung
          .getGueltigeIdentifikation()
          .ifPresent(
              identifikation ->
                  identifikation.setBesitzer(
                      kod.getGueltigeIdentifikation().getBesitzer()));

      beschreibung
          .getGueltigeIdentifikation()
          .ifPresent(
              identifikation ->
                  identifikation.setAufbewahrungsOrt(
                      kod.getGueltigeIdentifikation().getAufbewahrungsOrt()));

    } else {

      beschreibung
          .getGueltigeIdentifikation()
          .ifPresent(
              identifikation ->
                  identifikation.setBesitzer(
                      findNormdatenReferenz(
                          findNormdatenReferenzID(identifikation.getBesitzer()),
                          NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME)
                          .orElseThrow(
                              () ->
                                  new IllegalArgumentException(
                                      String.format(
                                          "Can't find Besitzer for Beschreibung %s Besitzer: %s with GND-ID: %s",
                                          identifikation.getIdent(),
                                          identifikation.getBesitzer()
                                              .getName(),
                                          identifikation.getBesitzer()
                                              .getGndID())))));

      log.info(
          "Get Besitzer for Beschreibung {} ",
          beschreibung.getGueltigeIdentifikation().get().getBesitzer());

      beschreibung
          .getGueltigeIdentifikation()
          .ifPresent(
              identifikation ->
                  identifikation.setAufbewahrungsOrt(
                      Stream.concat(
                              kod.getAlternativeIdentifikationen().stream(),
                              Stream.of(kod.getGueltigeIdentifikation()))
                          .filter(
                              kodidentifikation ->
                                  kodidentifikation
                                      .getBesitzer()
                                      .getId()
                                      .equals(
                                          beschreibung
                                              .getGueltigeIdentifikation()
                                              .get()
                                              .getBesitzer()
                                              .getId()))
                          .map(Identifikation::getAufbewahrungsOrt)
                          .findFirst()
                          .orElseThrow(
                              () ->
                                  new IllegalArgumentException(
                                      String.format(
                                          "Can't find Aufbewahrungsort for Beschreibung in KOD %s Aufbewahrungsort: %s with GND-ID: %s",
                                          identifikation.getIdent(),
                                          identifikation.getAufbewahrungsOrt()
                                              .getName(),
                                          identifikation.getAufbewahrungsOrt()
                                              .getGndID())))));
    }
  }

  String findNormdatenReferenzID(NormdatenReferenz normdatenReferenz) {
    if (StringUtils.isNotEmpty(normdatenReferenz.getGndID())) {
      return normdatenReferenz.getGndID();
    }
    if (StringUtils.isNotEmpty(normdatenReferenz.getId())) {
      return normdatenReferenz.getId();
    }
    return normdatenReferenz.getName();
  }

  boolean hasSameBesitzer(Beschreibung beschreibung, KulturObjektDokument kod) {
    String besitzerGndIdBeschreibung = null;
    String besitzerNameBeschreibung = null;

    String besitzerGndIdKod = null;
    String besitzerNameKod = null;

    Optional<Identifikation> beschreibungIdentifikation = beschreibung.getGueltigeIdentifikation();
    if (beschreibungIdentifikation.isPresent()) {
      NormdatenReferenz besitzer = beschreibungIdentifikation.get()
          .getBesitzer();
      if (besitzer != null) {
        besitzerGndIdBeschreibung = besitzer.getGndID();
        besitzerNameBeschreibung = besitzer.getName();
      }
    }

    Identifikation kodIdentifikation = kod.getGueltigeIdentifikation();
    if (kodIdentifikation != null) {
      NormdatenReferenz besitzer = kodIdentifikation.getBesitzer();
      if (besitzer != null) {
        besitzerGndIdKod = besitzer.getGndID();
        besitzerNameKod = besitzer.getName();
      }
    }

    return (besitzerGndIdBeschreibung != null
        && besitzerGndIdBeschreibung.equals(besitzerGndIdKod))
        || (besitzerNameBeschreibung != null
        && besitzerNameBeschreibung.equals(besitzerNameKod));
  }

  private List<GNDEntityFact> findEntstehungsorteAsEntityFacts(
      Beschreibung beschreibung) {
    List<String> gndids = TEIQuery.queryForTermOriginGNDID(beschreibung);

    log.info("Found GND ID for Entstehungsorte: {} ",
        String.join(", ", gndids));

    return gndids.stream().map(this::createOrLoadEntstehungsort)
        .filter(Objects::nonNull).toList();
  }

  @Transactional
  void saveKODAndBeschreibungAsBatch(BatchRecord batchRecords, ImportFile importFile)
      throws Exception {
    log.info("Save Batch Records of {} ", batchRecords);
    try {
      beschreibungsBatchService.publizierenBatch(
          batchRecords.getKulturobjekte(),
          batchRecords.getBeschreibungen());
      // TEI Snapshot needs to be captured after publizierenBatch,
      // because the parent row (Publikation) does not exist yet.
      batchRecords.getBeschreibungen().stream()
          .filter(Beschreibung::isPublished)
          .forEach(publikationsService::captureTeiSnapshot);
      batchRecords
          .getBeschreibungen()
          .forEach(b -> updateImportJsonNodeForSuccess(b, importFile));
    } finally {
      batchRecords.clear();
    }
  }

  private GNDEntityFact createOrLoadEntstehungsort(String gndid) {
    if (gndid == null) {
      return null;
    }
    return entstehungsortCache.computeIfAbsent(gndid, key ->
        normdatenUpdateService.createPlaceIfNotExist(key).orElse(null));
  }

  @Transactional
  void handleReImport(Beschreibung newBeschreibung, Beschreibung oldBeschreibung)
      throws KulturObjektDokumentMismatchException {
    moveDataFromOldToNewBeschreibung(newBeschreibung, oldBeschreibung);
    deleteOldBeschreibung(newBeschreibung, oldBeschreibung);
  }

  protected void moveDataFromOldToNewBeschreibung(Beschreibung newBeschreibung,
      Beschreibung oldBeschreibung) {
    newBeschreibung.getAltIdentifier().add(newBeschreibung.getId());
    newBeschreibung.setId(oldBeschreibung.getId());
    movePurlsFromOldToNew(oldBeschreibung, newBeschreibung);
  }

  protected void validateExistingBeschreibungForImport(Beschreibung newBeschreibung,
      Beschreibung oldBeschreibung)
      throws ExterneBeschreibungNotPublishedException, BeschreibungAlreadyPublishedException {

    VerwaltungsTyp verwaltungstyp = newBeschreibung.getVerwaltungsTyp();
    if (verwaltungstyp == VerwaltungsTyp.INTERN
        && oldBeschreibung.isPublished()
        && !importInternPublishedEnabled
    ) {
      throw new BeschreibungAlreadyPublishedException(
          resourceBundle.getString("datenuebernahme_description_already_published")
              + oldBeschreibung.getId());
    }

    if (verwaltungstyp == VerwaltungsTyp.EXTERN && !oldBeschreibung.isPublished()) {
      throw new ExterneBeschreibungNotPublishedException(
          resourceBundle.getString("datenuebernahme_description_extern_not_published")
              + oldBeschreibung.getId());
    }
  }

  protected void deleteOldBeschreibung(
      Beschreibung newBeschreibung,
      Beschreibung oldBeschreibung
  ) throws KulturObjektDokumentMismatchException {

    if (!oldBeschreibung.getKodID().equals(newBeschreibung.getKodID())) {
      throw new KulturObjektDokumentMismatchException(
          String.format(
              "Old Beschreibung KOD ID %s doesn't match new Beschreibung KOD ID %s !",
              oldBeschreibung.getKodID(),
              newBeschreibung.getKodID())
      );
    }

    beschreibungsRepository.deleteByIdAndFlush(
        oldBeschreibung.getId());
  }

  protected void movePurlsFromOldToNew(Beschreibung oldBeschreibung, Beschreibung newBeschreibung) {
    if (oldBeschreibung.getPublikation() != null && newBeschreibung.getPublikation() != null) {
      oldBeschreibung.getPublikation().getPURLs().stream()
          .filter(purl -> PURLTyp.INTERNAL == purl.getTyp())
          .forEach(purl ->
              newBeschreibung.getPublikation().getPURLs().add(purl)
          );
    }
  }

  Optional<ActivityStreamObjectTagDTO> findInternExternTag(
      List<ActivityStreamObjectDTO> activityStreamObjects) {
    Optional<ActivityStreamObjectDTO> importStreamObjectDTO =
        activityStreamObjects.stream()
            .filter(o -> ActivityStreamsDokumentTyp.IMPORT.equals(o.getType()))
            .findFirst();

    return importStreamObjectDTO.flatMap(
        activityStreamObjectDTO ->
            activityStreamObjectDTO.getTag().stream()
                .filter(tag -> ActivityStreamObjectTagId.INTERN_EXTERN.name()
                    .equals(tag.getId()))
                .findFirst());
  }

  @Transactional(
      rollbackOn = {Exception.class},
      value = TxType.REQUIRES_NEW)
  void handleImportJobError(
      BeschreibungImport beschreibungImportJob, ImportVorgang importVorgang,
      Exception ex) {
    try {
      updateImportJsonNodeForFailure(importVorgang, formatErrorMessage(ex));
      setImportVorgang(beschreibungImportJob.getMessageDTO(), importVorgang);
      importFehlgeschlagen(beschreibungImportJob, ex);
      importVorgangBoundary.sendImportJobToKafka(importVorgang);
      importRepository.save(beschreibungImportJob);
    } catch (Exception e) {
      log.error("Unable to send error to importjob {}", e.getMessage(), e);
    }
  }

  String formatErrorMessage(Exception ex) {
    try {
      String template = resourceBundle.getString(ex.getMessage());
      Object[] args = (ex instanceof PublikationException pex)
          ? pex.getMessageArgs()
          : new Object[0];
      return MessageFormat.format(template, args);
    } catch (MissingResourceException ignored) {
      return ex.getMessage();
    }
  }

  private ImportFile findImportFileForActivityStreamObject(
      ImportVorgang importVorgang,
      ActivityStreamObjectDTO activityStreamObject) {

    ImportFile importFile;
    Objects.requireNonNull(activityStreamObject.getName(),
        "ActivityStreamObject without name");
    importFile =
        importVorgang.getImportFiles().stream()
            .filter(
                searchForImportFile ->
                    activityStreamObject.getName()
                        .equals(searchForImportFile.getDateiName()))
            .findFirst()
            .orElse(null);
    return importFile;
  }

  private void setImportVorgang(
      ActivityStreamMessageDTO activityStream, ImportVorgang importVorgang)
      throws ActivityStreamsException, JsonProcessingException {
    ActivityStream importJobMessage = createMessageForImportJob(importVorgang);
    ActivityStreamObject newActivityStreamObject = importJobMessage.getObjects()
        .getFirst();
    List<ActivityStreamObjectDTO> objects = activityStream.getObjects();
    for (int i = 0, size = objects.size(); i < size; i++) {
      ActivityStreamObjectDTO activityStreamObject = objects.get(i);
      if (ActivityStreamsDokumentTyp.IMPORT.equals(
          activityStreamObject.getType())) {
        objects.remove(i);
        objects.add(new ActivityStreamObjectDTO(newActivityStreamObject));
        log.info(
            "replaced old Object {} with new one {}",
            activityStreamObject,
            newActivityStreamObject);
        return;
      }
    }
    log.warn("The Activity Object with type IMPORT not found in the {}",
        activityStream);
  }

  void updateImportJsonNodeForFailure(ImportVorgang importVorgang,
      String errorMessage) {
    importVorgang.setStatus(IMPORTJOB_RESULT_VALUES.FAILED);
    importVorgang.setFehler(errorMessage);
  }

  void updateImportJsonNodeForSuccess(Beschreibung beschreibung,
      ImportFile importFile) {
    if (importFile != null) {
      AtomicReference<String> signatur = new AtomicReference<>("Signatur");
      beschreibung.getGueltigeIdentifikation()
          .ifPresent(i -> signatur.set(i.getIdent()));

      List<DataEntity> dataEntityList = importFile.getDataEntityList();
      DataEntity dataEntity =
          new DataEntity(
              beschreibung.getId(),
              signatur.get(),
              praesentationBaseUrl + "search?hspobjectid="
                  + beschreibung.getKodID() + "?q=%2A");
      if (dataEntityList.contains(dataEntity)) {
        int pos = dataEntityList.indexOf(dataEntity);
        DataEntity dataEntityInList = dataEntityList.get(pos);
        dataEntity.setUrl(dataEntityInList.getUrl());
        dataEntityList.remove(pos);
      }
      dataEntityList.add(dataEntity);
    }
  }

  @Override
  @Transactional
  public Optional<Beschreibung> findExistingBeschreibung(
      Beschreibung beschreibung) {

    return beschreibungsRepository.findByIdOptional(beschreibung.getId())
        .or(
            () -> beschreibungsRepository.findByAltIdentifierOptional(beschreibung.getId())
        );
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void importErfolgreich(
      final BeschreibungImport beschreibungImportJob) {
    if (Objects.nonNull(beschreibungImportJob)) {
      beschreibungImportJob.setStatus(ImportStatus.UEBERNOMMEN);
      beschreibungImportJob.setSelectedForImport(false);
      importRepository.save(beschreibungImportJob);
      sendIndexMessageEvent.fire(beschreibungImportJob);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void importFehlgeschlagen(
      final BeschreibungImport beschreibungImportJob, Exception exception) {
    if (Objects.nonNull(beschreibungImportJob)) {
      beschreibungImportJob.setStatus(ImportStatus.FEHLGESCHLAGEN);
      beschreibungImportJob.setFehlerBeschreibung(exception.getMessage());
      beschreibungImportJob.setSelectedForImport(false);
      importRepository.save(beschreibungImportJob);
      sendIndexMessageEvent.fire(beschreibungImportJob);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void importeAblehnen(List<String> importe)
      throws BeschreibungsImportException {

    if (Objects.nonNull(importe) && !importe.isEmpty()) {

      Collection<BeschreibungImport> beschreibungImportJobs =
          importRepository.listAll().stream()
              .filter(j -> importe.contains(j.getId())).toList();

      for (BeschreibungImport j : beschreibungImportJobs) {
        try {
          j.setStatus(ImportStatus.ABGELEHNT);
          j.setSelectedForImport(false);
          importRepository.save(j);

          ImportVorgang importVorgang = importVorgangBoundary.getImportVorgang(
              j.getMessageDTO());
          if (importVorgang != null) {
            importVorgang.setStatus(IMPORTJOB_RESULT_VALUES.FAILED);
            importVorgang.setFehler("Import abgelehnt");
            importVorgangBoundary.sendImportJobToKafka(importVorgang);
          }
        } catch (Exception e) {
          log.error("Problem updating importJob {}", e.getMessage(), e);
          throw new BeschreibungsImportException(
              "Beschreibungsimport fehlgeschlagen!" + e.getMessage(), e);
        }
      }
    }
  }

  public void enhanceGrundsprachenAsNormdatenReferenz(
      Beschreibung beschreibung) {
    beschreibung.getBeschreibungsStruktur().stream()
        .filter(k -> k.getTyp().equals(BeschreibungsKomponentenTyp.KOPF))
        .map(k -> (BeschreibungsKomponenteKopf) k)
        .findFirst()
        .ifPresent(kopf -> {
          Set<NormdatenReferenz> toRemove = new HashSet<>();
          Set<NormdatenReferenz> toAdd = new HashSet<>();

          for (NormdatenReferenz grundsprache : kopf.getGrundsprachen()) {
            if (grundsprache.getGndID() != null && !grundsprache.getGndID()
                .isEmpty()) {
              normdatenReferenzBoundary.findSpracheById(grundsprache.getGndID())
                  .ifPresentOrElse(nr -> {
                    toRemove.add(grundsprache);
                    toAdd.add(nr);
                  }, () -> {
                    throw new IllegalArgumentException(
                        "No metadata for base language (Grundsprache) found: "
                            + grundsprache.getName());
                  });
            }
          }

          // Änderungen nach der Iteration durchführen
          kopf.getGrundsprachen().removeAll(toRemove);
          kopf.getGrundsprachen().addAll(toAdd);
          kopf.removeGrundspracheWithoutNORMUUID();
        });
  }


  protected void enhanceNormdatenReferenzSprache(Beschreibung beschreibung)
      throws BeschreibungsImportException {
    if (Objects.isNull(beschreibung.getBeschreibungsSprache())
        || Objects.isNull(beschreibung.getBeschreibungsSprache().getName())
        || beschreibung.getBeschreibungsSprache().getName().isBlank()) {
      log.info("No Beschreibungssprache available in Beschreibung {}.",
          beschreibung.getId());
      return;
    }

    NormdatenReferenz normdatenSprache;

    normdatenSprache =
        normdatenReferenzBoundary
            .findSpracheById(beschreibung.getBeschreibungsSprache().getName())
            .orElseThrow(
                () ->
                    new BeschreibungsImportException(
                        String.format(
                            "Found no Language for Beschreibung %s  and Sprache %s",
                            beschreibung.getId(),
                            beschreibung.getBeschreibungsSprache()
                                .getName())));

    beschreibung.setBeschreibungsSprache(normdatenSprache);
  }

  @Inject
  public void setImportRepository(ImportRepository importRepository) {
    this.importRepository = importRepository;
  }

  @Inject
  protected void setBeschreibungsRepository(
      BeschreibungsRepository beschreibungsRepository) {
    this.beschreibungsRepository = beschreibungsRepository;
  }

  @Inject
  protected void setKulturObjektDokumentBoundary(
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary) {
    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
  }

  @Inject
  public void setBeschreibungsBatchService(BeschreibungsBatchService beschreibungsBatchService) {
    this.beschreibungsBatchService = beschreibungsBatchService;
  }

  @Inject
  public void setSendIndexMessageEvent(
      @sendIndexMessage Event<BeschreibungImport> sendIndexMessageEvent) {
    this.sendIndexMessageEvent = sendIndexMessageEvent;
  }

  @Inject
  public void setImportVorgangBoundary(
      ImportVorgangBoundary importVorgangBoundary) {
    this.importVorgangBoundary = importVorgangBoundary;
  }

  @Inject
  public void setPraesentationBaseUrl(
      @ConfigProperty(name = "praesentationbaseurl") String praesentationBaseUrl) {
    this.praesentationBaseUrl = praesentationBaseUrl;
  }

  @Inject
  public void setNormdatenReferenzBoundary(
      NormdatenReferenzBoundary normdatenReferenzBoundary) {
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
  }

  @Inject
  public void setTeiXmlValidationBoundary(
      TeiXmlValidationBoundary teiXmlValidationBoundary) {
    this.teiXmlValidationBoundary = teiXmlValidationBoundary;
  }

  @Inject
  public void setNormdatenUpdateService(
      NormdatenUpdateService normdatenUpdateService) {
    this.normdatenUpdateService = normdatenUpdateService;
  }

  @Inject
  public void setImportInternPublishedEnabled(
      @ConfigProperty(name = "nachweis.import.intern.published.enabled", defaultValue = "false") boolean importInternPublishedEnabled) {
    this.importInternPublishedEnabled = importInternPublishedEnabled;
  }

  @Inject
  public void setKonfigurationBoundary(
      KonfigurationBoundary konfigurationBoundary) {
    this.konfigurationBoundary = konfigurationBoundary;
  }

  @Inject
  public void setPublikationsService(
      PublikationsService publikationsService) {
    this.publikationsService = publikationsService;
  }

  @Inject
  public void setBeschreibungsSynchronizer(
      BeschreibungsSynchronizer beschreibungsSynchronizer) {
    this.beschreibungsSynchronizer = beschreibungsSynchronizer;
  }

  Map<String, GNDEntityFact> getEntstehungsortCache() {
    return entstehungsortCache;
  }

  protected static class BatchRecord {

    @Getter
    private final Set<Beschreibung> beschreibungen = new HashSet<>();
    @Getter
    private final Set<KulturObjektDokument> kulturobjekte = new HashSet<>();
    private final int batchSize;

    public BatchRecord(int batchSize) {
      this.batchSize = batchSize;
    }

    public void addRecord(KulturObjektDokument kulturObjektDokument,
        Beschreibung beschreibung) {
      beschreibungen.add(beschreibung);
      kulturobjekte.add(kulturObjektDokument);
    }

    public void clear() {
      beschreibungen.clear();
      kulturobjekte.clear();
    }

    public boolean isBatchProcessingNeeded(boolean lastBeschreibung) {
      return beschreibungen.size() >= batchSize || lastBeschreibung;
    }

    public Optional<KulturObjektDokument> findKODInBatchRecords(String id) {
      return kulturobjekte.stream()
          .filter(kod -> id.equals(kod.getId()))
          .findFirst();
    }

    @Override
    public String toString() {
      return "BatchRecord{" + "beschreibungen=" + beschreibungen.size()
          + ", kulturobjekte=" + kulturobjekte.size()
          + '}';
    }
  }
}
