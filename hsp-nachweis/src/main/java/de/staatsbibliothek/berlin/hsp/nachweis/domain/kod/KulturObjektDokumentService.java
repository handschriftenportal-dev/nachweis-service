package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEI2AttributsReferenzMapper.mapAttributsReferenz;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.kod.TEIKODAttributsReferenzCommand.updateAttributsReferenzen;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.kod.TEIKulturObjektDokumentCommand.updateIndentInMsIdentifier;
import static de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction.ADD;
import static de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction.REMOVE;
import static de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction.UPDATE;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary.SYSTEM_USERNAME;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.DUPLICATE;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.IDENTIFICATION_INCOMPLETE;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.KOD_AMBIGUOUS;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.KOD_CONFLICTING;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.KOD_LOCKED;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.KOD_NOT_FOUND;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.SAVE_ERROR;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.UUID_NOT_FOUND;
import static java.util.Optional.of;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;

import de.staatsbibliothek.berlin.hsp.domainmodel.HSP_UUI_Factory;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.TEIDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.AttributsReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Beschreibstoff;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Buchschmuck;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Handschriftentyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Musiknotation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Status;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Titel;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Umfang;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessource;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessourceTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Forschungsdokumentation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPMapperException;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEI2AttributsReferenzMapper;
import de.staatsbibliothek.berlin.hsp.mapper.tei.kod.TEIExterneRessourcenCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.kod.TEIForschungsdokumentationCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.kod.TEIKODAttributsReferenzCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.kod.TEIKulturObjektDokumentCommand;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DigitalisatViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel.KulturObjektDokumentViewModelBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.SignatureValue;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.sendIndexAllKODForNachweis;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.events.KulturObjektDokumentRegistriertEvent;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodregistrieren.KulturObjektDokumentRegistrierenException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodregistrieren.KulturObjektDokumentRegistrierenException.ERROR_TYPE;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodregistrieren.KulturObjektDokumentRegistry;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.KulturObjektSortkeyUpdateException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.KulturObjektSortkeyUpdateReport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortKeyBatchResult;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortKeyEntries;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortKeyEntry;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortkeyBatchService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortkeyImportCSVParser;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortkeyPartialImportException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.minimaldaten.IdentifikationRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.papierkorb.PapierkorbBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchJob;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugOverrideAttributsReferenzen;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.network.DistributedSaveCommand;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.KulturObjektDokumentListDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.XMLDokument;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import jakarta.persistence.NonUniqueResultException;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.mozilla.universalchardet.UniversalDetector;
import org.tei_c.ns._1.Change;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.TEI;

@Slf4j
@ApplicationScoped
public class KulturObjektDokumentService implements
    KulturObjektDokumentBoundary, Serializable {

  public static final String KOD_IST_LOCKED = "KOD ist locked";
  private static final int BATCH_SIZE = 1000;

  @Serial
  private static final long serialVersionUID = 7896727068740214139L;

  private final transient KulturObjektDokumentRepository kulturObjektDokumentRepository;

  private final transient IdentifikationRepository identifikationRepository;

  private final transient BeschreibungsRepository beschreibungsRepository;
  private final transient KafkaIndexingProducer kafkaIndexingProducer;
  private final transient BeschreibungsBoundary beschreibungsService;
  private final transient DokumentSperreBoundary dokumentSperreService;
  private final transient PapierkorbBoundary papierkorbService;
  private final transient PURLBoundary purlService;
  private final boolean purlAutogenerateEnabled;
  private final transient BearbeiterBoundary bearbeiterBoundary;
  private final transient WerkzeugOverrideAttributsReferenzen werkzeugOverrideAttributsReferenzen;
  private final TeiXmlValidationBoundary teiXmlValidationBoundary;
  private final transient SortkeyImportCSVParser sortkeyImportCSVParser;
  private final transient SortkeyBatchService sortkeyBatchService;
  private final transient Event<KulturObjektDokumentRegistriertEvent> kodRegistriertEventPublisher;
  private transient SuchDokumentBoundary suchDokumentService;
  @Setter
  private transient DistributedSaveCommand<KulturObjektDokument> distributedSaveCommand;

  @Inject
  public KulturObjektDokumentService(
      KulturObjektDokumentRepository kulturObjektDokumentRepository,
      IdentifikationRepository identifikationRepository,
      SuchDokumentBoundary suchDokumentService,
      BeschreibungsBoundary beschreibungsService,
      DokumentSperreBoundary dokumentSperreService,
      KafkaIndexingProducer kafkaIndexingProducer,
      BeschreibungsRepository beschreibungsRepository,
      PURLBoundary purlService,
      PapierkorbBoundary papierkorbService,
      @ConfigProperty(name = "purl.autogenerate.enabled") boolean purlAutogenerateEnabled,
      DistributedSaveCommand<KulturObjektDokument> distributedSaveCommand,
      BearbeiterBoundary bearbeiterBoundary,
      WerkzeugOverrideAttributsReferenzen werkzeugOverrideAttributsReferenzen,
      TeiXmlValidationBoundary teiXmlValidationBoundary,
      SortkeyImportCSVParser sortkeyUpdateImportParser,
      SortkeyBatchService sortkeyBatchService,
      Event<KulturObjektDokumentRegistriertEvent> kodRegistriertEventPublisher) {

    this.kulturObjektDokumentRepository = kulturObjektDokumentRepository;
    this.identifikationRepository = identifikationRepository;
    this.suchDokumentService = suchDokumentService;
    this.kafkaIndexingProducer = kafkaIndexingProducer;
    this.beschreibungsService = beschreibungsService;
    this.beschreibungsRepository = beschreibungsRepository;
    this.dokumentSperreService = dokumentSperreService;
    this.purlService = purlService;
    this.papierkorbService = papierkorbService;
    this.purlAutogenerateEnabled = purlAutogenerateEnabled;
    this.distributedSaveCommand = distributedSaveCommand;
    this.bearbeiterBoundary = bearbeiterBoundary;
    this.werkzeugOverrideAttributsReferenzen = werkzeugOverrideAttributsReferenzen;
    this.teiXmlValidationBoundary = teiXmlValidationBoundary;
    this.sortkeyImportCSVParser = sortkeyUpdateImportParser;
    this.sortkeyBatchService = sortkeyBatchService;
    this.kodRegistriertEventPublisher = kodRegistriertEventPublisher;
  }

  public static void addKulturObjektDokumentToActivityStreamMessage(
      ActivityStream message, KulturObjektDokument kod)
      throws ActivityStreamsException {

    Objects.requireNonNull(message, "message is required!");
    Objects.requireNonNull(kod, "kod is required!");

    ActivityStreamObject activityStreamObjectKOD =
        ActivityStreamObject.builder()
            .withId(kod.getId())
            .withContent(kod.getTeiXML())
            .withCompressed(true)
            .withType(ActivityStreamsDokumentTyp.KOD)
            .build();
    message.addObject(activityStreamObjectKOD);
  }

  private static Identifikation createAlternativeIdentifikationFromGueltige(
      KulturObjektDokument k) {
    return new Identifikation.IdentifikationBuilder()
        .withId(HSP_UUI_Factory.generate())
        .withIdent(k.getGueltigeIdentifikation().getIdent())
        .withBesitzer(k.getGueltigeIdentifikation().getBesitzer())
        .withAufbewahrungsOrt(
            k.getGueltigeIdentifikation().getAufbewahrungsOrt())
        .withIdentTyp(IdentTyp.FRUEHERE_ALTSIGNATUR)
        .withGueltigBis(LocalDate.now(ZoneId.systemDefault()))
        .withSortKey(k.getGueltigeIdentifikation().getSortKey())
        .build();
  }

  private static boolean sollReferenzUebernommenWerden(
      AttributsReferenz aktuelleReferenz,
      AttributsReferenz neueReferenz) {
    return aktuelleReferenz == null ||
        aktuelleReferenz.getPublikationDatum() == null
        || aktuelleReferenz.getText() == null ||
        aktuelleReferenz.getText().isEmpty() ||
        (neueReferenz.getPublikationDatum()
            .isAfter(aktuelleReferenz.getPublikationDatum())
            && neueReferenz.getText() != null && !neueReferenz.getText()
            .isEmpty());
  }

  @Override
  public Optional<KulturObjektDokument> findById(String id) {
    if (id == null || id.isEmpty()) {
      return Optional.empty();
    }
    return kulturObjektDokumentRepository.findByIdOptional(id);
  }

  @Override
  public Optional<KulturObjektDokument> findKodByBeschreibung(Beschreibung beschreibung)
      throws KulturObjektDokumentNotFoundException {
    Optional<KulturObjektDokument> kodByID = findKodByID(beschreibung);
    if (kodByID.isPresent()) {
      return kodByID;
    }
    return findKodByIdentifikation(beschreibung);
  }

  private Optional<KulturObjektDokument> findKodByID(Beschreibung beschreibung) {
    if (beschreibung.getKodID() == null) {
      return Optional.empty();
    }
    log.debug("Find KOD {} for already existing Beschreibung {} ",
        beschreibung.getKodID(), beschreibung.getId());
    return kulturObjektDokumentRepository.findByIdOptional(beschreibung.getKodID());
  }

  private Optional<KulturObjektDokument> findKodByIdentifikation(Beschreibung beschreibung)
      throws KulturObjektDokumentNotFoundException {
    var gueltigeIdentifikation = beschreibung.getGueltigeIdentifikation();
    if (gueltigeIdentifikation.isEmpty()) {
      return Optional.empty();
    }
    return findKodByIdentifikation(gueltigeIdentifikation.get());
  }

  Optional<KulturObjektDokument> findKodByIdentifikation(Identifikation identifikation)
      throws KulturObjektDokumentNotFoundException {
    var kod = findByIdentifikation(identifikation);
    if (kod.isPresent()) {
      return kod;
    }
    return findByAlternativeIdentifikation(identifikation);
  }

  Optional<KulturObjektDokument> findByIdentifikation(
      Identifikation beschreibungsIdentifikation)
      throws KulturObjektDokumentNotFoundException {
    var ident = beschreibungsIdentifikation.getIdent();
    var besitzer = beschreibungsIdentifikation.getBesitzer().getName();
    var aufbewahrungsOrt = beschreibungsIdentifikation.getAufbewahrungsOrt().getName();

    try {
      var kod = kulturObjektDokumentRepository
          .findByIdentifikationIdentAndBesitzerNameAndAufbewahrungsortName(
              ident, besitzer, aufbewahrungsOrt);
      if (kod.isPresent()) {
        log.debug("Found KOD by Gültige Signatur of Beschreibung {}, {}, {}",
            ident, besitzer, aufbewahrungsOrt);
      }
      return kod;
    } catch (NonUniqueResultException nonUniqueResultException) {
      throw new KulturObjektDokumentNotFoundException(
          "Es wurde mehr als eine übereinstimmende gültige KOD-Signatur zur BeschreibungsIdentifikation gefunden: "
              + ident,
          nonUniqueResultException);
    }
  }

  Optional<KulturObjektDokument> findByAlternativeIdentifikation(
      Identifikation beschreibungsIdentifikation)
      throws KulturObjektDokumentNotFoundException {
    var ident = beschreibungsIdentifikation.getIdent();
    var besitzer = beschreibungsIdentifikation.getBesitzer().getName();
    var aufbewahrungsOrt = beschreibungsIdentifikation.getAufbewahrungsOrt().getName();

    try {
      var kod = kulturObjektDokumentRepository
          .findByAlternativeIdentifikationIdentAndBesitzerNameAndAufbewahrungsortName(
              ident, besitzer, aufbewahrungsOrt);
      if (kod.isPresent()) {
        log.debug("Found KOD by Alternative Signatur of Beschreibung {}, {}, {}",
            ident, besitzer, aufbewahrungsOrt);
      }
      return kod;
    } catch (NonUniqueResultException nonUniqueResultException) {
      throw new KulturObjektDokumentNotFoundException(
          "Es wurde mehr als eine übereinstimmende alternative KOD-Signatur zur BeschreibungsIdentifikation gefunden: "
              + ident,
          nonUniqueResultException);
    }
  }

  @Override
  public List<KulturObjektDokument> findAll() {
    return kulturObjektDokumentRepository.listAll(true);
  }

  @Override
  public long count() {
    return kulturObjektDokumentRepository.count();
  }

  @Override
  @TransactionConfiguration(timeout = 7200)
  @Transactional(rollbackOn = {Exception.class})
  public void delete(Bearbeiter bearbeiter, String... kulturObjektDokumentIds)
      throws Exception {

    Set<KulturObjektDokument> kulturObjektDokumente =
        Arrays.stream(kulturObjektDokumentIds)
            .map(kulturObjektDokumentRepository::findById)
            .collect(toSet());

    Set<Beschreibung> beschreibungen =
        kulturObjektDokumente.stream()
            .flatMap(
                kulturObjektDokument -> kulturObjektDokument.getBeschreibungenIDs()
                    .stream())
            .map(beschreibungsRepository::findById)
            .filter(Objects::nonNull)
            .collect(toSet());

    Set<SperreEintrag> sperreEintraege =
        dokumentSperreService.createSperreEintraege(kulturObjektDokumente,
            beschreibungen);

    dokumentSperreService.acquireSperre(
        bearbeiter,
        SperreTyp.IN_TRANSACTION,
        "Löschen via GUI",
        sperreEintraege.toArray(new SperreEintrag[0]));

    for (KulturObjektDokument kulturObjektDokument : kulturObjektDokumente) {
      deleteKulturObjektDokument(bearbeiter, kulturObjektDokument.getId());
    }
  }

  @Override
  public void reindexAllKulturObjektDokumente(
      @ObservesAsync @sendIndexAllKODForNachweis WerkzeugBatchJob event) {
    try {
      suchDokumentService.reindexAllKulturObjektDokumente();
      event.end("Indexing all KODs successfully completed!");
    } catch (Exception e) {
      event.fail("Indexing KODs failed: " + (e.getMessage() != null ? e.getMessage()
          : e.getClass().getSimpleName()));
      log.error("ReindexAllKOD failed", e);
    }
  }

  @Override
  public Map<String, SignatureValue> getAllSignaturesWithKodIDs() {
    return identifikationRepository.getAllSignaturesWithKodIDs();
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public List<KulturObjektDokument> registrieren(
      Bearbeiter bearbeiter, List<KulturObjektDokument> kods)
      throws KulturObjektDokumentRegistrierenException {

    if (Objects.isNull(kods)) {
      throw new KulturObjektDokumentRegistrierenException(
          "Fehler bei der Verarbeitung der Signaturdatei! KOD-Liste ist null.",
          ERROR_TYPE.TECHNICAL,
          null);
    }

    try {
      log.info(
          "Starting filter kods without gueltige signatur number of kod's {}",
          kods.size());

      List<KulturObjektDokument> kodsWithGueltigeSignatur =
          kods.stream()
              .filter(kod -> Objects.nonNull(kod.getGueltigeIdentifikation()))
              .toList();

      log.info(
          "Starting check if already in file for number of kod's {}",
          kodsWithGueltigeSignatur.size());

      List<KulturObjektDokument> gueltigeSignaturInDatei = checkIfKulturObjektAlreadyInDatei(
          kods);
      if (!gueltigeSignaturInDatei.isEmpty()) {
        throw new KulturObjektDokumentRegistrierenException(
            "Gültige Signatur mehrfach in Datei vorhanden: "
                + gueltigeSignaturInDatei.stream()
                .map(KulturObjektDokument::getGueltigeSignatur)
                .collect(joining(",")),
            gueltigeSignaturInDatei,
            ERROR_TYPE.GUELTIGE_SIGNATUR_IN_DATEI);
      }

      log.info(
          "Starting check if already exists for number of kod's {}",
          kodsWithGueltigeSignatur.size());

      List<KulturObjektDokument> gueltigeSignaturInSystem =
          checkIfKulturObjektAlreadyExists(kodsWithGueltigeSignatur);
      if (!gueltigeSignaturInSystem.isEmpty()) {
        throw new KulturObjektDokumentRegistrierenException(
            "Kulturobjekt mit gültiger Signatur bereits im System: "
                + gueltigeSignaturInSystem.stream()
                .map(KulturObjektDokument::getGueltigeSignatur)
                .collect(joining(",")),
            gueltigeSignaturInSystem,
            ERROR_TYPE.GUELTIGE_SIGNATUR_IN_SYSTEM);
      }

      createAndAddInternalPURLs(kodsWithGueltigeSignatur);

      log.info("Saving KOD's successfull {} ", kodsWithGueltigeSignatur.size());

      saveKulturObjektDokumentInSystem(kodsWithGueltigeSignatur);

      if (!kodsWithGueltigeSignatur.isEmpty()) {
        kodRegistriertEventPublisher.fire(new KulturObjektDokumentRegistriertEvent());
      }

      return kodsWithGueltigeSignatur;

    } catch (KulturObjektDokumentRegistrierenException kodrex) {
      throw kodrex;
    } catch (Exception error) {
      throw new KulturObjektDokumentRegistrierenException(
          "Fehler bei der Verarbeitung der Signaturdatei !,  "
              + error.getMessage(),
          ERROR_TYPE.TECHNICAL,
          error);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public List<KulturObjektDokument> registrieren(
      Bearbeiter bearbeiter,
      NormdatenReferenz ort,
      NormdatenReferenz koerperschaft,
      byte[] signaturen)
      throws KulturObjektDokumentRegistrierenException {

    checkEncoding(signaturen);

    List<KulturObjektDokument> kods = new ArrayList<>();

    try (Reader reader = new InputStreamReader(
        new ByteArrayInputStream(signaturen), StandardCharsets.UTF_8)) {

      CSVFormat format = CSVFormat.Builder.create()
          .setDelimiter('$')
          .setQuote('"')
          .setIgnoreEmptyLines(true)
          .get();

      Iterable<CSVRecord> records = format.parse(reader);

      for (CSVRecord line : records) {
        StringBuilder originalLine = new StringBuilder();

        for (int i = 0; i < line.size(); i++) {
          if (i > 0) {
            originalLine.append("$");
          }

          String value = line.get(i) != null ? line.get(i) : "";
          originalLine.append("\"").append(value).append("\"");
        }

        String signaturLine = originalLine.toString();

        if (findAnEmptyRow(signaturLine)) {
          break;
        }

        kods.add(
            KulturObjektDokumentRegistry.create(
                ort,
                koerperschaft,
                KulturObjektDokumentRegistry.splitSignaturen(signaturLine)
            )
        );
      }

      return registrieren(bearbeiter, kods);
    } catch (KulturObjektDokumentRegistrierenException exception) {
      throw exception;
    } catch (Exception error) {
      throw new KulturObjektDokumentRegistrierenException(
          "Fehler bei der Verarbeitung der Signaturdatei!,  "
              + error.getMessage(),
          ERROR_TYPE.TECHNICAL,
          error);
    }
  }

  @Override
  public KulturObjektSortkeyUpdateReport updateSortkeys(
      byte[] content,
      String filename)
      throws KulturObjektSortkeyUpdateException {

    SortKeyEntries entries = sortkeyImportCSVParser.parse(filename, content);

    SortKeyBatchResult result = processBatches(entries.entries());

    return new KulturObjektSortkeyUpdateReport(
        result.updatedCount(),
        result.unknownUuids(),
        entries.missingOrEmptySortkeyLines());
  }

  private SortKeyBatchResult processBatches(List<SortKeyEntry> entries)
      throws KulturObjektSortkeyUpdateException {

    List<List<SortKeyEntry>> chunks = ListUtils.partition(entries, BATCH_SIZE);

    log.info("Starting SortKey import. Total entries={}", entries.size());

    var chunkNumber = 1;
    List<SortKeyBatchResult> chunkResults = new ArrayList<>();
    for (List<SortKeyEntry> chunk : chunks) {

      log.info(
          "Processing SortKey batch {} of {}",
          chunkNumber,
          chunks.size()
      );

      try {
        SortKeyBatchResult chunkResult = sortkeyBatchService.processChunk(chunk);
        chunkResults.add(chunkResult);
      } catch (KulturObjektSortkeyUpdateException exception) {

        int updatedBeforeFailure = SortKeyBatchResult.combine(chunkResults).updatedCount();

        log.error(
            "SortKey import aborted in batch {} of {}. Already committed updates={}",
            chunkNumber,
            chunks.size(),
            updatedBeforeFailure);

        throw new SortkeyPartialImportException(updatedBeforeFailure, exception);
      }
      chunkNumber++;
    }

    SortKeyBatchResult total = SortKeyBatchResult.combine(chunkResults);
    log.info(
        "SortKey import finished. Updated={}, Unknown={}",
        total.updatedCount(),
        total.unknownUuids().size());

    return total;
  }

  protected void checkEncoding(byte[] data)
      throws KulturObjektDokumentRegistrierenException {

    String encoding;
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
      encoding = UniversalDetector.detectCharset(inputStream);

      log.info("Check CSV Encoding: {}", encoding);

      if (encoding != null && !"UTF-8|US-ASCII".contains(encoding)) {
        throw new KulturObjektDokumentRegistrierenException(
            I18NController.getMessage("kodservice_error_wrong_encoding",
                encoding));
      }
    } catch (IOException e) {
      log.error("Error during determine CSV Encoding", e);
    }
  }

  private boolean findAnEmptyRow(String signaturLine) {
    return signaturLine.trim().isEmpty();
  }

  @Override
  public Optional<KulturObjektDokumentViewModel> buildKulturObjektDokumentViewModel(
      String id) {
    KulturObjektDokumentViewModel model = null;
    Optional<KulturObjektDokument> byId = this.findById(id);

    if (Objects.nonNull(byId) && byId.isPresent()) {
      KulturObjektDokument kulturObjektDokument = byId.get();

      KulturObjektDokumentViewModelBuilder builder =
          KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
              .withId(kulturObjektDokument.getId())
              .withSignatur(
                  kulturObjektDokument.getGueltigeIdentifikation().getIdent())
              .withTeiXML(kulturObjektDokument.getTeiXML())
              .withBeschreibungen(
                  map(kulturObjektDokument.getBeschreibungenIDs()))
              .withAlternativeSignaturen(
                  kulturObjektDokument.getAlternativeIdentifikationen())
              .withDigitalisats(mapDigitalisate(kulturObjektDokument))
              .withHspPurl(mapHspUrl(kulturObjektDokument))
              .withExterneLinks(mapExterneLinks(kulturObjektDokument))
              .withExterneRessourcen(readExterneRessourcen(kulturObjektDokument))
              .withForschungsdokumentationen(
                  readForschungsdokumentationen(kulturObjektDokument))
              .addAttributsReferenzStatusTyp(
                  kulturObjektDokument.getAttributsReferenzStatusTyp());

      mapAttributsReferenzen(builder, kulturObjektDokument.getTeiXML());

      model = builder.build();
      mapIdentifikationWerte(
          Set.of(kulturObjektDokument.getGueltigeIdentifikation()), model);
    }

    return Optional.ofNullable(model);
  }

  private List<BeschreibungsViewModel> map(Set<String> beschreibungenIDs) {
    return beschreibungenIDs.stream()
        .map(
            beschreibungsID -> {
              log.debug("Map Beschreibung {}", beschreibungsID);
              return beschreibungsService.buildBeschreibungsViewModel(
                  beschreibungsID).orElse(null);
            })
        .filter(Objects::nonNull)
        .toList();
  }

  private void mapAttributsReferenzen(
      KulturObjektDokumentViewModelBuilder builder, String teiXml) {
    try {
      TEI tei = TEI2AttributsReferenzMapper.unmarshalTEI(teiXml);
      MsDesc msDesc = TEI2AttributsReferenzMapper.findFirstMsDesc(tei);

      builder.withTitel(mapAttributsReferenz(msDesc, tei, Titel.class));
      builder.withBeschreibstoff(
          mapAttributsReferenz(msDesc, tei, Beschreibstoff.class));
      builder.withUmfang(mapAttributsReferenz(msDesc, tei, Umfang.class));
      builder.withAbmessung(mapAttributsReferenz(msDesc, tei, Abmessung.class));
      builder.withFormat(mapAttributsReferenz(msDesc, tei, Format.class));
      builder.withEntstehungsort(
          mapAttributsReferenz(msDesc, tei, Entstehungsort.class));
      builder.withEntstehungszeit(
          mapAttributsReferenz(msDesc, tei, Entstehungszeit.class));
      builder.withGrundSprache(
          mapAttributsReferenz(msDesc, tei, Grundsprache.class));
      builder.withBuchschmuck(
          mapAttributsReferenz(msDesc, tei, Buchschmuck.class));
      builder.withHandschriftentyp(
          mapAttributsReferenz(msDesc, tei, Handschriftentyp.class));
      builder.withMusiknotation(
          mapAttributsReferenz(msDesc, tei, Musiknotation.class));
      builder.withStatus(mapAttributsReferenz(msDesc, tei, Status.class));
    } catch (Exception e) {
      log.error("Error mapping attributsReferenzen!", e);
    }
  }

  private String mapHspUrl(KulturObjektDokument kulturObjektDokument) {
    return kulturObjektDokument.getPURLs().stream()
        .filter(purl -> PURLTyp.INTERNAL == purl.getTyp())
        .findFirst()
        .map(purl -> purl.getPurl().toASCIIString())
        .orElse(null);
  }

  private List<String> mapExterneLinks(
      KulturObjektDokument kulturObjektDokument) {
    return kulturObjektDokument.getPURLs().stream()
        .filter(purl -> PURLTyp.INTERNAL != purl.getTyp())
        .map(purl -> purl.getPurl().toASCIIString())
        .toList();
  }

  private List<DigitalisatViewModel> mapDigitalisate(
      KulturObjektDokument kulturObjektDokument) {
    return kulturObjektDokument.getDigitalisate().stream()
        .map(DigitalisatViewModel::map).toList();
  }

  private void mapIdentifikationWerte(
      Set<Identifikation> identifikationen,
      KulturObjektDokumentViewModel kulturObjektDokumentViewModel) {
    for (Identifikation identifikation : identifikationen) {
      if (identifikation.getIdentTyp() == IdentTyp.GUELTIGE_SIGNATUR) {
        kulturObjektDokumentViewModel.setSignatur(identifikation.getIdent());

        of(identifikation)
            .map(Identifikation::getBesitzer)
            .map(NormdatenReferenz::getName)
            .ifPresent(
                kulturObjektDokumentViewModel::setBestandhaltendeInstitutionName);

        of(identifikation)
            .map(Identifikation::getAufbewahrungsOrt)
            .map(NormdatenReferenz::getName)
            .ifPresent(
                kulturObjektDokumentViewModel::setBestandhaltendeInstitutionOrt);
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void saveKulturObjektDokumentInSystem(
      List<KulturObjektDokument> kodsSucess)
      throws KulturObjektDokumentRegistrierenException {
    for (KulturObjektDokument kod : kodsSucess) {
      try {

        KulturObjektDokumentRegistry.addTEI(kod);

        saveKulturObjektDokument(kod, ADD);

      } catch (Exception e) {
        throw new KulturObjektDokumentRegistrierenException(
            "Kulturobjektdokument "
                + kod.getGueltigeSignatur()
                + " konnte nicht gespeichert werden! "
                + e.getMessage(),
            ERROR_TYPE.TECHNICAL,
            e);
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void deleteKulturObjektDokument(Bearbeiter bearbeiter, String kodId)
      throws Exception {
    log.debug("deleteKulturObjektDokument: bearbeiter={},kodId={}", bearbeiter,
        kodId);

    Optional<KulturObjektDokument> kodOptional =
        kulturObjektDokumentRepository.findByIdOptional(kodId);

    if (kodOptional.isPresent()) {
      KulturObjektDokument kod = kodOptional.get();
      for (String beschreibungId : kod.getBeschreibungenIDs()) {
        Optional<Beschreibung> optBeschreibung =
            beschreibungsRepository.findByIdOptional(beschreibungId);
        if (optBeschreibung.isPresent()) {
          String loeschId =
              papierkorbService.erzeugeGeloeschtesDokument(
                  optBeschreibung.get(), bearbeiter);
          log.debug("created GeloeschtesDokument {} for Beschreibung {}",
              loeschId, beschreibungId);
          beschreibungsRepository.deleteByIdAndFlush(beschreibungId);
        }

        suchDokumentService.dokumentLoeschen(beschreibungId);
      }

      String loeschId = papierkorbService.erzeugeGeloeschtesDokument(kod,
          bearbeiter);
      log.debug("created GeloeschtesDokument {} for KOD {}", loeschId, kodId);

      kulturObjektDokumentRepository.deleteByIdAndFlush(kodId);
      kafkaIndexingProducer.sendKulturobjektDokumentAsActivityStreamMessage(
          kod, REMOVE, true, SYSTEM_USERNAME);
      suchDokumentService.dokumentLoeschen(kodId);
    }
  }

  @Transactional(rollbackOn = Exception.class)
  public void saveKulturObjektDokument(KulturObjektDokument kod,
      ActivityStreamAction action)
      throws Exception {
    distributedSaveCommand.execute(kod, action);
  }

  protected List<KulturObjektDokument> checkIfKulturObjektAlreadyInDatei(
      List<KulturObjektDokument> kods) {
    Set<Identifikation> gueltigeSignaturenInDatei = new HashSet<>();
    List<KulturObjektDokument> gueltigeSignaturMehrfachInDatei = new ArrayList<>();

    for (KulturObjektDokument kod : kods) {
      if (Objects.nonNull(kod.getGueltigeIdentifikation())) {
        if (gueltigeSignaturenInDatei.contains(
            kod.getGueltigeIdentifikation())) {
          gueltigeSignaturMehrfachInDatei.add(kod);
        } else {
          gueltigeSignaturenInDatei.add(kod.getGueltigeIdentifikation());
        }
      }
    }

    return gueltigeSignaturMehrfachInDatei;
  }

  protected List<KulturObjektDokument> checkIfKulturObjektAlreadyExists(
      List<KulturObjektDokument> kods) {
    List<KulturObjektDokument> kodsFailureWithGueltigeSignature = new ArrayList<>();

    for (KulturObjektDokument kod : kods) {
      kulturObjektDokumentRepository
          .findByIdentifikationIdentAndBesitzerIDAndAufbewahrungsortID(
              kod.getGueltigeIdentifikation().getIdent(),
              kod.getGueltigeIdentifikation().getBesitzer().getId(),
              kod.getGueltigeIdentifikation().getAufbewahrungsOrt().getId())
          .stream()
          .findAny()
          .ifPresent(kodsFailureWithGueltigeSignature::add);
    }

    return kodsFailureWithGueltigeSignature;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Optional<KulturObjektDokument> digitalisatHinzufuegen(
      String kulturObjektDokumentId,
      DigitalisatViewModel digitalisatViewModel)
      throws KulturObjektDokumentException, SperreAlreadyExistException {

    final Optional<KulturObjektDokument> kulturObjektDokument =
        kulturObjektDokumentRepository.findByIdOptional(kulturObjektDokumentId);

    if (kulturObjektDokument.isPresent() && digitalisatViewModel != null) {

      KulturObjektDokument kod = kulturObjektDokument.get();

      kod.addDigitalisat(buildDigitalisat(digitalisatViewModel));

      try {

        TEIKulturObjektDokumentCommand.updateSurrogatesDigitalisate(kod);

        saveKulturObjektDokument(kod, UPDATE);

      } catch (Exception error) {
        log.error("Error during Digiatlisate hinzufuegen to kod!", error);
        throw new KulturObjektDokumentException(
            "Digitalisate konnten nicht gespeichert werden!", error);
      }
    }

    return kulturObjektDokument;
  }

  /**
   * Adds all given Digitalisate to a KOD (existing ones with the same URL are replaced) and
   * saves+indexes the KOD once. Used by the Digitalisat import to trigger only one distributed save
   * (validation, Solr, Kafka) per KOD instead of one per Digitalisat.
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Optional<KulturObjektDokument> digitalisateHinzufuegen(
      String kulturObjektDokumentId,
      Collection<DigitalisatViewModel> digitalisate)
      throws KulturObjektDokumentException {

    final Optional<KulturObjektDokument> kulturObjektDokument =
        kulturObjektDokumentRepository.findByIdOptional(kulturObjektDokumentId);

    if (kulturObjektDokument.isEmpty() || digitalisate == null || digitalisate.isEmpty()) {
      return kulturObjektDokument;
    }

    KulturObjektDokument kod = kulturObjektDokument.get();

    digitalisate.stream()
        .filter(Objects::nonNull)
        .forEach(digitalisatViewModel -> {
              List<Digitalisat> sameUrl = kod.getDigitalisate().stream()
                  .filter(digitalisatViewModel::hasSameUrl)
                  .toList();
              // Replace existing Digitalisate with the same manifest/alternative URL.
              sameUrl.forEach(kod::removeDigitalisat);
              // Replace existing Digitalisat based on id
              kod.addDigitalisat(buildDigitalisat(digitalisatViewModel));
            }
        );

    try {
      TEIKulturObjektDokumentCommand.updateSurrogatesDigitalisate(kod);
      saveKulturObjektDokument(kod, UPDATE);
    } catch (Exception error) {
      log.error("Error during Digitalisate hinzufuegen to kod!", error);
      throw new KulturObjektDokumentException(
          "Digitalisate konnten nicht gespeichert werden!", error);
    }

    return Optional.of(kod);
  }

  private Digitalisat buildDigitalisat(DigitalisatViewModel digitalisatViewModel)
      throws KulturObjektDokumentException {
    if (digitalisatViewModel.getManifestURL() == null
        && digitalisatViewModel.getAlternativeUrl() == null) {
      throw new KulturObjektDokumentException("Es sind nicht alle Angaben gemacht worden!");
    }
    return digitalisatViewModel.toDigitalisat();
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Optional<KulturObjektDokument> digitalisatLoeschen(
      String kulturObjektDokumentId,
      DigitalisatViewModel digitalisatViewModel)
      throws KulturObjektDokumentException {
    final Optional<KulturObjektDokument> kulturObjektDokument =
        kulturObjektDokumentRepository.findByIdOptional(
            kulturObjektDokumentId);

    if (kulturObjektDokument.isPresent()) {
      KulturObjektDokument kod = kulturObjektDokument.get();

      Optional<Digitalisat> digitalisatToRemove =
          kod.getDigitalisate().stream()
              .filter(d -> d.getId().equals(digitalisatViewModel.getId()))
              .findFirst();

      if (digitalisatToRemove.isPresent()) {

        kod.getDigitalisate().remove(digitalisatToRemove.get());

        try {

          TEIKulturObjektDokumentCommand.updateSurrogatesDigitalisate(kod);

          saveKulturObjektDokument(kod, UPDATE);

        } catch (Exception error) {
          log.error("Error during Digiatlisate loeschen to kod!", error);
          throw new KulturObjektDokumentException(
              "Digitalisate konnten nicht gelöscht werden!", error);
        }
      }
    }

    return kulturObjektDokument;
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public Sperre kulturObjektDokumentSperren(
      Bearbeiter bearbeiter, String sperreGrund, String kulturObjektDokumentID)
      throws DokumentSperreException, SperreAlreadyExistException {

    KulturObjektDokument kulturObjektDokument =
        kulturObjektDokumentRepository.findById(kulturObjektDokumentID);

    return dokumentSperreService.acquireSperre(
        bearbeiter, SperreTyp.MANUAL, sperreGrund, kulturObjektDokument);
  }

  @Override
  public Optional<Sperre> findSperreForKulturObjektDokument(
      String kulturObjektDokumentID)
      throws DokumentSperreException {
    return dokumentSperreService
        .findBySperreEintraege(
            new SperreEintrag(kulturObjektDokumentID, SperreDokumentTyp.KOD))
        .stream()
        .findFirst();
  }

  @Override
  public void kulturObjektDokumentEntsperren(String kulturObjektDokumentID)
      throws DokumentSperreException {
    Optional<Sperre> sperre = findSperreForKulturObjektDokument(
        kulturObjektDokumentID);
    if (sperre.isPresent()) {
      dokumentSperreService.releaseSperre(sperre.get());
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void addPURL(String kodId, PURL purl)
      throws KulturObjektDokumentException {
    log.debug("addPURL: kodId={}, purl={}", kodId, purl);

    Objects.requireNonNull(kodId, "kodId is required");
    Objects.requireNonNull(purl, "purl is required");

    KulturObjektDokument kod =
        findById(kodId)
            .orElseThrow(
                () ->
                    new KulturObjektDokumentException(
                        getMessage("kodservice_no_kod_for_id", kodId)));

    try {
      kod.getPURLs().add(purl);
      TEIKulturObjektDokumentCommand.updatePURLs(kod);
      saveKulturObjektDokument(kod, UPDATE);
    } catch (Exception e) {
      throw new KulturObjektDokumentException(
          getMessage("kodservice_error_create_purl", purl.getPurl(), kodId,
              e.getMessage()), e);
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public Optional<KulturObjektDokument> externeRessourceHinzufuegen(
      String kulturObjektDokumentId,
      ExterneRessourceTyp typ,
      String url,
      Sperre sperre) throws KulturObjektDokumentException {

    Objects.requireNonNull(kulturObjektDokumentId,
        "kulturObjektDokumentId is required");
    Objects.requireNonNull(typ, "typ is required");
    Objects.requireNonNull(url, "url is required");
    Objects.requireNonNull(sperre, "sperre is required");

    KulturObjektDokument kod = findById(kulturObjektDokumentId)
        .orElseThrow(() -> new KulturObjektDokumentException(
            getMessage("kodservice_no_kod_for_id", kulturObjektDokumentId)));

    try {
      ExterneRessource externeRessource = buildExterneRessource(typ, url);

      String updatedTeiXML = TEIExterneRessourcenCommand
          .addExterneRessource(kod.getTeiXML(), externeRessource)
          .orElseThrow(() -> new KulturObjektDokumentException(
              getMessage("kodservice_error_duplicate_externe_ressource", url)));

      kod.setTeiXML(updatedTeiXML);
      saveKulturObjektDokument(kod, UPDATE);
      return Optional.of(kod);

    } catch (KulturObjektDokumentException exception) {
      throw exception;

    } catch (Exception exception) {
      throw new KulturObjektDokumentException(
          getMessage("kodservice_error_add_externe_ressource",
              url, kulturObjektDokumentId, exception.getMessage()),
          exception);
    }
  }

  private List<ExterneRessource> readExterneRessourcen(
      KulturObjektDokument kulturObjektDokument) {
    try {
      return TEIExterneRessourcenCommand.readExterneRessourcen(
          kulturObjektDokument.getTeiXML());
    } catch (Exception exception) {
      log.error("Error reading externeRessourcen for KOD {}!",
          kulturObjektDokument.getId(), exception);
      return List.of();
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void externeRessourceImportieren(
      String kulturObjektDokumentId,
      ExterneRessource externeRessource) throws ExterneRessourcenImportException {

    Objects.requireNonNull(kulturObjektDokumentId, "kulturObjektDokumentId is required");
    Objects.requireNonNull(externeRessource, "externeRessource is required");

    KulturObjektDokument kod = findById(kulturObjektDokumentId)
        .orElseThrow(() -> new ExterneRessourcenImportException(
            UUID_NOT_FOUND, kulturObjektDokumentId));

    try {
      if (findSperreForKulturObjektDokument(kulturObjektDokumentId).isPresent()) {
        throw new ExterneRessourcenImportException(
            KOD_LOCKED);
      }

      String updatedTeiXML = TEIExterneRessourcenCommand
          .addExterneRessource(kod.getTeiXML(), externeRessource)
          .orElseThrow(() -> new ExterneRessourcenImportException(
              DUPLICATE, externeRessource.url()));

      kod.setTeiXML(updatedTeiXML);
      saveKulturObjektDokument(kod, UPDATE);

    } catch (ExterneRessourcenImportException exception) {
      throw exception;

    } catch (Exception exception) {
      throw new ExterneRessourcenImportException(
          SAVE_ERROR, exception, exception.getMessage());
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void externeRessourceLoeschen(
      String kulturObjektDokumentId,
      ExterneRessourceTyp typ,
      String url,
      Sperre sperre) throws KulturObjektDokumentException {

    Objects.requireNonNull(kulturObjektDokumentId, "kulturObjektDokumentId is required");
    Objects.requireNonNull(typ, "typ is required");
    Objects.requireNonNull(url, "url is required");
    Objects.requireNonNull(sperre, "sperre is required");

    KulturObjektDokument kod = findById(kulturObjektDokumentId)
        .orElseThrow(() -> new KulturObjektDokumentException(
            getMessage("kodservice_no_kod_for_id", kulturObjektDokumentId)));

    try {
      ExterneRessource externeRessource = buildExterneRessource(typ, url);

      Optional<String> updatedTeiXML = TEIExterneRessourcenCommand
          .removeExterneRessource(kod.getTeiXML(), externeRessource);

      if (updatedTeiXML.isEmpty()) {
        log.info(
            "External resource already absent: kodId={}, type={}, url={}",
            kulturObjektDokumentId, typ, url);
        return;
      }

      kod.setTeiXML(updatedTeiXML.get());
      saveKulturObjektDokument(kod, UPDATE);
    } catch (KulturObjektDokumentException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new KulturObjektDokumentException(
          getMessage(
              "kodservice_error_delete_externe_ressource",
              url,
              kulturObjektDokumentId,
              exception.getMessage()),
          exception);
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void forschungsdokumentationHinzufuegen(
      String kulturObjektDokumentId,
      String ressource,
      String link,
      Sperre sperre) throws KulturObjektDokumentException {

    Objects.requireNonNull(kulturObjektDokumentId,
        "kulturObjektDokumentId is required");
    Objects.requireNonNull(ressource, "ressource is required");
    Objects.requireNonNull(sperre, "sperre is required");

    KulturObjektDokument kod = findById(kulturObjektDokumentId)
        .orElseThrow(() -> new KulturObjektDokumentException(
            getMessage("kodservice_no_kod_for_id", kulturObjektDokumentId)));

    try {
      Forschungsdokumentation forschungsdokumentation =
          buildForschungsdokumentation(ressource, link);

      String updatedTeiXML = TEIForschungsdokumentationCommand
          .addForschungsdokumentation(kod.getTeiXML(), forschungsdokumentation)
          .orElseThrow(() -> new KulturObjektDokumentException(
              getMessage(forschungsdokumentation.hasLink()
                  ? "kodservice_error_duplicate_forschungsdokumentation_link"
                  : "kodservice_error_duplicate_forschungsdokumentation_ressource")));

      kod.setTeiXML(updatedTeiXML);
      saveKulturObjektDokument(kod, UPDATE);

    } catch (KulturObjektDokumentException exception) {
      throw exception;

    } catch (Exception exception) {
      throw new KulturObjektDokumentException(
          getMessage("kodservice_error_add_forschungsdokumentation"), exception);
    }
  }

  private List<Forschungsdokumentation> readForschungsdokumentationen(
      KulturObjektDokument kulturObjektDokument) {
    try {
      return TEIForschungsdokumentationCommand.readForschungsdokumentationen(
          kulturObjektDokument.getTeiXML());
    } catch (Exception exception) {
      log.error("Error reading forschungsdokumentationen for KOD {}!",
          kulturObjektDokument.getId(), exception);
      return List.of();
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void forschungsdokumentationLoeschen(
      String kulturObjektDokumentId,
      String ressource,
      String link,
      Sperre sperre) throws KulturObjektDokumentException {

    Objects.requireNonNull(kulturObjektDokumentId, "kulturObjektDokumentId is required");
    Objects.requireNonNull(ressource, "ressource is required");
    Objects.requireNonNull(sperre, "sperre is required");

    KulturObjektDokument kod = findById(kulturObjektDokumentId)
        .orElseThrow(() -> new KulturObjektDokumentException(
            getMessage("kodservice_no_kod_for_id", kulturObjektDokumentId)));

    try {
      Forschungsdokumentation forschungsdokumentation =
          buildForschungsdokumentation(ressource, link);

      Optional<String> updatedTeiXML = TEIForschungsdokumentationCommand
          .removeForschungsdokumentation(kod.getTeiXML(), forschungsdokumentation);

      if (updatedTeiXML.isEmpty()) {
        log.info(
            "Research documentation already absent: kodId={}, resource={}, link={}",
            kulturObjektDokumentId, ressource, link);
        return;
      }

      kod.setTeiXML(updatedTeiXML.get());
      saveKulturObjektDokument(kod, UPDATE);
    } catch (KulturObjektDokumentException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new KulturObjektDokumentException(
          getMessage("kodservice_error_delete_forschungsdokumentation"), exception);
    }
  }

  @Override
  public Optional<KulturObjektDokument> findByPurl(String purl) {
    if (StringUtils.isBlank(purl)) {
      return Optional.empty();
    }
    return kulturObjektDokumentRepository.findByPurl(purl.trim());
  }

  @Override
  public KulturObjektDokument findKulturObjektDokumentBySignaturForExterneRessourcenImport(
      String signatur,
      String besitzendeEinrichtung,
      String aufbewahrungsort)
      throws ExterneRessourcenImportException {

    String normalizedSignatur = StringUtils.trimToEmpty(signatur);
    String normalizedEinrichtung = StringUtils.trimToEmpty(besitzendeEinrichtung);
    String normalizedAufbewahrungsort = StringUtils.trimToEmpty(aufbewahrungsort);
    if (StringUtils.isAnyBlank(
        normalizedSignatur, normalizedEinrichtung, normalizedAufbewahrungsort)) {
      throw new ExterneRessourcenImportException(
          IDENTIFICATION_INCOMPLETE);
    }

    List<KulturObjektDokument> signaturTreffer =
        kulturObjektDokumentRepository.findByIdentifikationIdent(normalizedSignatur);
    if (signaturTreffer.isEmpty()) {
      throw new ExterneRessourcenImportException(
          KOD_NOT_FOUND);
    }
    List<KulturObjektDokument> signaturUndKontextTreffer = signaturTreffer.stream()
        .filter(kod -> matchesImportKontext(
            kod, normalizedEinrichtung, normalizedAufbewahrungsort))
        .toList();

    if (signaturUndKontextTreffer.isEmpty()) {
      throw new ExterneRessourcenImportException(
          KOD_CONFLICTING);
    }
    if (signaturUndKontextTreffer.size() == 1) {
      return signaturUndKontextTreffer.getFirst();
    }

    throw new ExterneRessourcenImportException(
        KOD_AMBIGUOUS);
  }

  private boolean matchesImportKontext(
      KulturObjektDokument kod, String besitzendeEinrichtung, String aufbewahrungsort) {
    Identifikation identifikation = kod.getGueltigeIdentifikation();
    return identifikation != null
        && matchesImportName(identifikation.getBesitzer(), besitzendeEinrichtung)
        && matchesImportName(identifikation.getAufbewahrungsOrt(), aufbewahrungsort);
  }

  private boolean matchesImportName(NormdatenReferenz referenz, String expectedName) {
    return referenz != null && Objects.equals(referenz.getName(), expectedName);
  }

  /**
   * Translates the technical validation failure into a localized message for the user.
   *
   * @throws KulturObjektDokumentException when typ or url do not form a valid ExterneRessource
   */
  ExterneRessource buildExterneRessource(ExterneRessourceTyp typ, String url)
      throws KulturObjektDokumentException {
    try {
      return new ExterneRessource(typ, url);

    } catch (IllegalArgumentException | NullPointerException exception) {
      throw new KulturObjektDokumentException(
          getMessage("kodservice_error_invalid_externe_ressource_url", url),
          exception);
    }
  }

  Forschungsdokumentation buildForschungsdokumentation(String ressource, String link)
      throws KulturObjektDokumentException {
    try {
      return new Forschungsdokumentation(ressource, link);

    } catch (Forschungsdokumentation.InvalidRessourceException exception) {
      throw new KulturObjektDokumentException(
          getMessage("kodservice_error_invalid_forschungsdokumentation_ressource"),
          exception);

    } catch (Forschungsdokumentation.InvalidLinkException exception) {
      throw new KulturObjektDokumentException(
          getMessage("kodservice_error_invalid_forschungsdokumentation_link"),
          exception);
    }
  }

  @Override
  public List<KulturObjektDokumentListDTO> findFilteredKulturObjektDokumentListDTOs(
      int first, int pageSize, Map<String, String> filterBy,
      Map<String, Boolean> sortByAsc) {
    return kulturObjektDokumentRepository.findFilteredKulturObjektDokumentListDTOs(
        first, pageSize, filterBy, sortByAsc);
  }

  @Override
  public int countFilteredKulturObjektDokumentListDTOs(
      Map<String, String> filterBy) {
    return kulturObjektDokumentRepository.countFilteredKulturObjektDokumentListDTOs(
        filterBy);
  }

  @Override
  public KulturObjektDokumentListDTO findKulturObjektDokumentListDTOById(
      String id) {
    return kulturObjektDokumentRepository.findKulturObjektDokumentListDTOById(
        id);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public KulturObjektDokument kulturObjektDokumentAttributsreferenzenAktualisieren(
      KulturObjektDokumentViewModel kodViewModel)
      throws KulturObjektDokumentException {
    Objects.requireNonNull(kodViewModel, "kodViewModel is required!");

    log.info("kulturObjektDokumentAktualisieren: kodId={}",
        kodViewModel.getId());

    KulturObjektDokument kod =
        findById(kodViewModel.getId())
            .orElseThrow(
                () ->
                    new KulturObjektDokumentException(
                        getMessage("kodservice_no_kod_for_id",
                            kodViewModel.getId())));

    try {

      kod.setAttributsReferenzen(loadAttributsReferenzenFromTei(kod));

      Change change = TEIKODAttributsReferenzCommand.createChangeElement(
          HSP_UUI_Factory.generate(),
          LocalDateTime.now(ZoneId.systemDefault()), AttributsReferenzStatusTyp.MANUELL,
          "Update Attributsreferenzen by Redakteur",
          bearbeiterBoundary.getLoggedBearbeiter());

      updateKODAttributsreferenzenWithViewModel(kodViewModel, kod, change);
      updateAttributsReferenzen(kod, change);
      kod.updateAttributsReferenzStatusTyp();
      saveKulturObjektDokument(kod, UPDATE);
      return kod;
    } catch (Exception e) {
      throw new KulturObjektDokumentException(
          getMessage("kodservice_error_update_kod", kodViewModel.getId(),
              e.getMessage()), e);
    }
  }

  private void updateKODAttributsreferenzenWithViewModel(
      KulturObjektDokumentViewModel kodViewModel,
      KulturObjektDokument kod, Change change) {

    updateTitle(kodViewModel, kod, change);

    updateBeschreibstoff(kodViewModel, kod, change);

    updateUmfang(kodViewModel, kod, change);

    updateAbmessungen(kodViewModel, kod, change);

    updateFormat(kodViewModel, kod, change);

    updateEntsehungsort(kodViewModel, kod, change);

    updateEntstehungszeit(kodViewModel, kod, change);

    updateGrundsprache(kodViewModel, kod, change);

    updateBuchschmuck(kodViewModel, kod, change);

    updateHandschriftentyp(kodViewModel, kod, change);

    updateMusiknotation(kodViewModel, kod, change);

    updateStatus(kodViewModel, kod, change);
  }

  private void updateStatus(KulturObjektDokumentViewModel kodViewModel,
      KulturObjektDokument kod, Change change) {
    if (kod.getAttributsReferenzen().get(AttributsTyp.STATUS) == null ||
        kod.getAttributsReferenzen().get(AttributsTyp.STATUS)
            .hasChanged(kodViewModel.getStatus())) {
      if (kodViewModel.getStatus() != null) {
        kod.getAttributsReferenzen()
            .put(AttributsTyp.STATUS, kodViewModel.getStatus());
        kod.getAttributsReferenzen().get(AttributsTyp.STATUS)
            .setChangeID(change.getId());
      } else {
        kod.getAttributsReferenzen().put(AttributsTyp.STATUS,
            Status.builder().withChangeId(change.getId()).build());
      }
      kod.getAttributsReferenzen().get(AttributsTyp.STATUS)
          .setAttributsReferenzStatusTyp(change.getN());
    }
  }

  private void updateMusiknotation(KulturObjektDokumentViewModel kodViewModel,
      KulturObjektDokument kod, Change change) {
    if (kod.getAttributsReferenzen().get(AttributsTyp.MUSIKNOTATION) == null ||
        kod.getAttributsReferenzen().get(AttributsTyp.MUSIKNOTATION)
            .hasChanged(kodViewModel.getMusiknotation())) {
      if (kodViewModel.getMusiknotation() != null) {
        kod.getAttributsReferenzen()
            .put(AttributsTyp.MUSIKNOTATION, kodViewModel.getMusiknotation());
        kod.getAttributsReferenzen().get(AttributsTyp.MUSIKNOTATION)
            .setChangeID(change.getId());
      } else {
        kod.getAttributsReferenzen().put(AttributsTyp.MUSIKNOTATION,
            Musiknotation.builder().withChangeId(
                change.getId()).build());
      }
      kod.getAttributsReferenzen().get(AttributsTyp.MUSIKNOTATION)
          .setAttributsReferenzStatusTyp(change.getN());
    }
  }

  private void updateHandschriftentyp(
      KulturObjektDokumentViewModel kodViewModel,
      KulturObjektDokument kod, Change change) {
    if (kod.getAttributsReferenzen().get(AttributsTyp.HANDSCHRIFTENTYP) == null
        ||
        kod.getAttributsReferenzen().get(AttributsTyp.HANDSCHRIFTENTYP)
            .hasChanged(kodViewModel.getHandschriftentyp())) {
      if (kodViewModel.getHandschriftentyp() != null) {
        kod.getAttributsReferenzen()
            .put(AttributsTyp.HANDSCHRIFTENTYP,
                kodViewModel.getHandschriftentyp());
        kod.getAttributsReferenzen().get(AttributsTyp.HANDSCHRIFTENTYP)
            .setChangeID(change.getId());
      } else {
        kod.getAttributsReferenzen().put(AttributsTyp.HANDSCHRIFTENTYP,
            Handschriftentyp.builder().withChangeId(
                change.getId()).build());
      }
      kod.getAttributsReferenzen().get(AttributsTyp.HANDSCHRIFTENTYP)
          .setAttributsReferenzStatusTyp(change.getN());
    }
  }

  private void updateBuchschmuck(KulturObjektDokumentViewModel kodViewModel,
      KulturObjektDokument kod, Change change) {
    if (kod.getAttributsReferenzen().get(AttributsTyp.BUCHSCHMUCK) == null ||
        kod.getAttributsReferenzen().get(AttributsTyp.BUCHSCHMUCK)
            .hasChanged(kodViewModel.getBuchschmuck())) {
      if (kodViewModel.getBuchschmuck() != null) {
        kod.getAttributsReferenzen()
            .put(AttributsTyp.BUCHSCHMUCK, kodViewModel.getBuchschmuck());
        kod.getAttributsReferenzen().get(AttributsTyp.BUCHSCHMUCK)
            .setChangeID(change.getId());
      } else {
        kod.getAttributsReferenzen().get(AttributsTyp.BUCHSCHMUCK)
            .setAttributsReferenzStatusTyp(change.getN());
      }
    }
  }

  private void updateGrundsprache(KulturObjektDokumentViewModel kodViewModel,
      KulturObjektDokument kod, Change change) {
    if (kod.getAttributsReferenzen().get(AttributsTyp.GRUNDSPRACHE) == null ||
        kod.getAttributsReferenzen().get(AttributsTyp.GRUNDSPRACHE)
            .hasChanged(kodViewModel.getGrundsprache())) {
      if (kodViewModel.getGrundsprache() != null) {
        kod.getAttributsReferenzen()
            .put(AttributsTyp.GRUNDSPRACHE, kodViewModel.getGrundsprache());
        kod.getAttributsReferenzen().get(AttributsTyp.GRUNDSPRACHE)
            .setChangeID(change.getId());
      } else {
        kod.getAttributsReferenzen()
            .put(AttributsTyp.GRUNDSPRACHE, Grundsprache.builder().withChangeId(
                change.getId()).build());
      }
      kod.getAttributsReferenzen().get(AttributsTyp.GRUNDSPRACHE)
          .setAttributsReferenzStatusTyp(change.getN());
    }
  }

  private void updateEntstehungszeit(KulturObjektDokumentViewModel kodViewModel,
      KulturObjektDokument kod, Change change) {
    if (kod.getAttributsReferenzen().get(AttributsTyp.ENTSTEHUNGSZEIT) == null
        ||
        kod.getAttributsReferenzen().get(AttributsTyp.ENTSTEHUNGSZEIT)
            .hasChanged(kodViewModel.getEntstehungszeit())) {
      if (kodViewModel.getEntstehungszeit() != null) {
        kod.getAttributsReferenzen()
            .put(AttributsTyp.ENTSTEHUNGSZEIT,
                kodViewModel.getEntstehungszeit());
        kod.getAttributsReferenzen().get(AttributsTyp.ENTSTEHUNGSZEIT)
            .setChangeID(change.getId());
      } else {
        kod.getAttributsReferenzen().put(AttributsTyp.ENTSTEHUNGSZEIT,
            Entstehungszeit.builder().withChangeId(
                change.getId()).build());
      }
      kod.getAttributsReferenzen().get(AttributsTyp.ENTSTEHUNGSZEIT)
          .setAttributsReferenzStatusTyp(change.getN());
    }
  }

  private void updateEntsehungsort(KulturObjektDokumentViewModel kodViewModel,
      KulturObjektDokument kod, Change change) {
    if (kod.getAttributsReferenzen().get(AttributsTyp.ENTSTEHUNGSORT) == null ||
        kod.getAttributsReferenzen().get(AttributsTyp.ENTSTEHUNGSORT)
            .hasChanged(kodViewModel.getEntstehungsort())) {
      if (kodViewModel.getEntstehungsort() != null) {
        kod.getAttributsReferenzen()
            .put(AttributsTyp.ENTSTEHUNGSORT, kodViewModel.getEntstehungsort());
        kod.getAttributsReferenzen().get(AttributsTyp.ENTSTEHUNGSORT)
            .setChangeID(change.getId());
      } else {
        kod.getAttributsReferenzen().put(AttributsTyp.ENTSTEHUNGSORT,
            Entstehungsort.builder().withChangeId(
                change.getId()).build());
      }
      kod.getAttributsReferenzen().get(AttributsTyp.ENTSTEHUNGSORT)
          .setAttributsReferenzStatusTyp(change.getN());
    }
  }

  private void updateFormat(KulturObjektDokumentViewModel kodViewModel,
      KulturObjektDokument kod, Change change) {
    if (kod.getAttributsReferenzen().get(AttributsTyp.FORMAT) == null ||
        kod.getAttributsReferenzen().get(AttributsTyp.FORMAT)
            .hasChanged(kodViewModel.getFormat())) {
      if (kodViewModel.getFormat() != null) {
        kod.getAttributsReferenzen()
            .put(AttributsTyp.FORMAT, kodViewModel.getFormat());
        kod.getAttributsReferenzen().get(AttributsTyp.FORMAT)
            .setChangeID(change.getId());
      } else {
        kod.getAttributsReferenzen().put(AttributsTyp.FORMAT,
            Format.builder().withChangeId(change.getId()).build());
      }
      kod.getAttributsReferenzen().get(AttributsTyp.FORMAT)
          .setAttributsReferenzStatusTyp(change.getN());
    }
  }

  private void updateAbmessungen(KulturObjektDokumentViewModel kodViewModel,
      KulturObjektDokument kod, Change change) {
    if (kod.getAttributsReferenzen().get(AttributsTyp.ABMESSUNG) == null ||
        kod.getAttributsReferenzen().get(AttributsTyp.ABMESSUNG)
            .hasChanged(kodViewModel.getAbmessung())) {
      if (kodViewModel.getAbmessung() != null) {
        kod.getAttributsReferenzen()
            .put(AttributsTyp.ABMESSUNG, kodViewModel.getAbmessung());
        kod.getAttributsReferenzen().get(AttributsTyp.ABMESSUNG)
            .setChangeID(change.getId());
      } else {
        kod.getAttributsReferenzen()
            .put(AttributsTyp.ABMESSUNG, Abmessung.builder().withChangeId(
                change.getId()).build());
      }
      kod.getAttributsReferenzen().get(AttributsTyp.ABMESSUNG)
          .setAttributsReferenzStatusTyp(change.getN());
    }
  }

  private void updateUmfang(KulturObjektDokumentViewModel kodViewModel,
      KulturObjektDokument kod, Change change) {
    if (kod.getAttributsReferenzen().get(AttributsTyp.UMFANG) == null ||
        kod.getAttributsReferenzen().get(AttributsTyp.UMFANG)
            .hasChanged(kodViewModel.getUmfang())) {
      if (kodViewModel.getUmfang() != null) {
        kod.getAttributsReferenzen()
            .put(AttributsTyp.UMFANG, kodViewModel.getUmfang());
        kod.getAttributsReferenzen().get(AttributsTyp.UMFANG)
            .setChangeID(change.getId());
      } else {
        kod.getAttributsReferenzen().put(AttributsTyp.UMFANG,
            Umfang.builder().withChangeId(change.getId()).build());
      }
      kod.getAttributsReferenzen().get(AttributsTyp.UMFANG)
          .setAttributsReferenzStatusTyp(change.getN());
    }
  }

  private void updateBeschreibstoff(KulturObjektDokumentViewModel kodViewModel,
      KulturObjektDokument kod, Change change) {
    if (kod.getAttributsReferenzen().get(AttributsTyp.BESCHREIBSTOFF) == null ||
        kod.getAttributsReferenzen().get(AttributsTyp.BESCHREIBSTOFF)
            .hasChanged(kodViewModel.getBeschreibstoff())) {
      if (kodViewModel.getBeschreibstoff() != null) {
        kod.getAttributsReferenzen()
            .put(AttributsTyp.BESCHREIBSTOFF, kodViewModel.getBeschreibstoff());
        kod.getAttributsReferenzen().get(AttributsTyp.BESCHREIBSTOFF)
            .setChangeID(change.getId());
      } else {
        kod.getAttributsReferenzen().put(AttributsTyp.BESCHREIBSTOFF,
            Beschreibstoff.builder().withChangeId(
                change.getId()).build());
      }
      kod.getAttributsReferenzen().get(AttributsTyp.BESCHREIBSTOFF)
          .setAttributsReferenzStatusTyp(change.getN());
    }
  }

  private void updateTitle(KulturObjektDokumentViewModel kodViewModel,
      KulturObjektDokument kod, Change change) {
    if (kod.getAttributsReferenzen().get(AttributsTyp.TITEL) == null ||
        kod.getAttributsReferenzen().get(AttributsTyp.TITEL)
            .hasChanged(kodViewModel.getTitel())) {
      if (kodViewModel.getTitel() != null) {
        kod.getAttributsReferenzen()
            .put(AttributsTyp.TITEL, kodViewModel.getTitel());
        kod.getAttributsReferenzen().get(AttributsTyp.TITEL)
            .setChangeID(change.getId());
      } else {
        kod.getAttributsReferenzen().put(AttributsTyp.TITEL,
            Titel.builder().withChangeId(change.getId()).build());
      }
      kod.getAttributsReferenzen().get(AttributsTyp.TITEL)
          .setAttributsReferenzStatusTyp(change.getN());
    }
  }

  public void setSuchDokumentService(SuchDokumentService suchDokumentService) {
    this.suchDokumentService = suchDokumentService;
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public Optional<KulturObjektDokument> gueltigeSignaturVeraendern(
      String neueGueltigeSignatur, String kodID, Sperre sperre)
      throws Exception {
    KulturObjektDokument kod;

    try {
      kod = checkPreconditionAndFindKOD(neueGueltigeSignatur, kodID, sperre);

      checkIfSignaturAlreadyExists(
          neueGueltigeSignatur,
          kod.getGueltigeIdentifikation().getBesitzer().getId());

      adjustIdentifikationForKOD(neueGueltigeSignatur, kod);

      updateIndentInMsIdentifier(kod);

      saveKulturObjektDokument(kod, UPDATE);

    } finally {
      if (sperre != null) {
        dokumentSperreService.releaseSperre(sperre);
      }
    }

    return of(kod);
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public Optional<KulturObjektDokument> alternativeSignaturHinzufuegen(
      String neueAlternativeSignatur, String kodID, Sperre sperre)
      throws Exception {

    log.info(
        "Neue alternative Signature hinzuguegen {} for kod {} ",
        neueAlternativeSignatur, kodID);

    KulturObjektDokument kod;

    try {
      kod = checkPreconditionAndFindKOD(neueAlternativeSignatur, kodID, sperre);

      checkIfAlternativeSignatureAlreadyExistsForBesitzer(
          neueAlternativeSignatur,
          kod.getGueltigeSignatur(),
          kod.getGueltigeIdentifikation().getBesitzer().getId());

      Identifikation neueAlternativeIdentifikation =
          new Identifikation.IdentifikationBuilder()
              .withId(
                  HSP_UUI_Factory.generate(
                      neueAlternativeSignatur.getBytes(StandardCharsets.UTF_8)))
              .withIdent(neueAlternativeSignatur)
              .withBesitzer(kod.getGueltigeIdentifikation().getBesitzer())
              .withAufbewahrungsOrt(
                  kod.getGueltigeIdentifikation().getAufbewahrungsOrt())
              .withIdentTyp(IdentTyp.ALTSIGNATUR)
              .build();

      kod.getAlternativeIdentifikationen().add(neueAlternativeIdentifikation);

      log.info("Generating new alternative Identifikation {} ", kod);

      updateIndentInMsIdentifier(kod);

      saveKulturObjektDokument(kod, UPDATE);
    } finally {
      if (sperre != null) {
        dokumentSperreService.releaseSperre(sperre);
      }
    }

    return Optional.of(kod);
  }

  protected void checkIfAlternativeSignatureAlreadyExistsForBesitzer(
      String neueAlternativeSignatur, String gueltigeSignature,
      String besitzerID) {

    List<KulturObjektDokument> alreadyExistingKOdsWithaltSignature =
        kulturObjektDokumentRepository
            .findByAlternativeIdentifikationIdent(neueAlternativeSignatur)
            .stream()
            .filter(kod -> kod.getGueltigeIdentifikation().getBesitzer().getId()
                .equals(besitzerID))
            .toList();

    if (alreadyExistingKOdsWithaltSignature.stream()
        .anyMatch(kod -> kod.getGueltigeSignatur().equals(gueltigeSignature))) {
      throw new KulturObjektDokumentException(
          getMessage("kod_alternativesignaturalreadyexistsforKOD"));
    }

    if (gueltigeSignature.equals(neueAlternativeSignatur)) {
      throw new KulturObjektDokumentException(
          getMessage("kod_alternativesignaturisgueltige"));
    }
  }

  protected KulturObjektDokument checkPreconditionAndFindKOD(
      String neueGueltigeSignatur, String kodID, Sperre sperre) {
    if (neueGueltigeSignatur == null
        || neueGueltigeSignatur.isBlank()
        || kodID == null
        || kodID.isEmpty()
        || sperre == null) {
      throw new IllegalArgumentException(
          getMessage("kod_preconditioncheck_error"));
    }

    return Optional.of(kulturObjektDokumentRepository.findById(kodID))
        .orElseThrow(
            () ->
                new KulturObjektDokumentException(
                    String.format("KulturObjektDokument not found %s", kodID)));
  }

  @Transactional
  protected void adjustIdentifikationForKOD(String neueGueltigeSignatur,
      KulturObjektDokument kod) {
    Identifikation alteGueltigeIdentifikation = kod.getGueltigeIdentifikation();

    Identifikation neueAlternativeIdentifikation =
        new Identifikation.IdentifikationBuilder()
            .withId(HSP_UUI_Factory.generate())
            .withIdent(alteGueltigeIdentifikation.getIdent())
            .withBesitzer(alteGueltigeIdentifikation.getBesitzer())
            .withAufbewahrungsOrt(
                alteGueltigeIdentifikation.getAufbewahrungsOrt())
            .withIdentTyp(IdentTyp.ALTSIGNATUR)
            .build();

    Identifikation neueGueltigeIdentifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdent(neueGueltigeSignatur)
            .withBesitzer(alteGueltigeIdentifikation.getBesitzer())
            .withAufbewahrungsOrt(
                alteGueltigeIdentifikation.getAufbewahrungsOrt())
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();

    kod.setGueltigeIdentifikation(neueGueltigeIdentifikation);
    kod.getAlternativeIdentifikationen().add(neueAlternativeIdentifikation);
    kod.getAlternativeIdentifikationen()
        .removeIf(
            identifikation ->
                identifikation.getIdent()
                    .equals(neueGueltigeIdentifikation.getIdent()));
  }

  public void checkIfSignaturAlreadyExists(String neueGueltigeSignatur,
      String besitzerID) {
    if (kulturObjektDokumentRepository.findByIdentifikationIdent(
            neueGueltigeSignatur).stream()
        .anyMatch(
            kod -> kod.getGueltigeIdentifikation().getBesitzer().getId()
                .equals(besitzerID))) {
      throw new KulturObjektDokumentException(
          getMessage("kod_signaturealreadyexists"));
    }
  }

  @Override
  @Transactional
  public List<KulturobjektDokumentUmziehenErgebnis> pruefeKODUmziehen(
      Map<String, String> signaturenMap,
      NormdatenReferenz neuerBesitzer,
      NormdatenReferenz neuerOrt) {

    Objects.requireNonNull(signaturenMap);
    Objects.requireNonNull(neuerBesitzer);
    Objects.requireNonNull(neuerOrt);

    return signaturenMap.entrySet().stream()
        .map(signaturEntry -> createKODUmziehenErgebnis(neuerBesitzer, neuerOrt,
            signaturEntry))
        .toList();
  }

  @Override
  @Transactional
  public void umziehen(
      List<KulturobjektDokumentUmziehenErgebnis> pruefungsErgebnis,
      Bearbeiter bearbeiter)
      throws Exception {

    if (pruefungsErgebnis == null || pruefungsErgebnis.stream()
        .anyMatch(e -> !e.isSuccessful())) {
      throw new IllegalArgumentException(
          "Bad KulturobjektDokumentUmziehenErgebnis " + pruefungsErgebnis);
    }

    for (KulturobjektDokumentUmziehenErgebnis ergebnis : pruefungsErgebnis) {
      Optional<KulturObjektDokument> kod = findById(ergebnis.id());
      if (kod.isPresent()) {
        KulturObjektDokument k = kod.get();

        Sperre sperre =
            dokumentSperreService.acquireSperre(bearbeiter, SperreTyp.MANUAL,
                "KOD moving", k);

        final Identifikation neueAlternativeIdentifikation =
            createAlternativeIdentifikationFromGueltige(k);

        k.getAlternativeIdentifikationen().add(neueAlternativeIdentifikation);

        k.getGueltigeIdentifikation().setBesitzer(ergebnis.neuerBesitzer());
        k.getGueltigeIdentifikation().setAufbewahrungsOrt(ergebnis.neuerOrt());

        k.getAlternativeIdentifikationen()
            .forEach(alt -> alt.setIdentTyp(IdentTyp.FRUEHERE_ALTSIGNATUR));

        TEIKulturObjektDokumentCommand.updateMsIdentifierAndAllAltidentifier(k);

        saveKulturObjektDokument(k, UPDATE);

        dokumentSperreService.releaseSperre(sperre);
      }
    }
  }

  @Override
  public boolean isAltIdentifikationDeletable(String altIdentifikationId) {
    return identifikationRepository.isAltIdentifikationDeletable(
        altIdentifikationId);
  }

  @Override
  @Transactional
  public void deleteAltIdentifikation(
      Bearbeiter bearbeiter, String kodId, String altIdentifikationId)
      throws Exception {
    if (!identifikationRepository.isAltIdentifikationDeletable(
        altIdentifikationId)) {
      throw new KulturObjektDokumentException(
          "AlIdentifikation cannot be deleted because it is referenced in a Description.");
    }

    KulturObjektDokument kod = kulturObjektDokumentRepository.findById(kodId);
    if (kod == null) {
      throw new KulturObjektDokumentException(
          "KulturObjektDokument not found for id " + kodId);
    }
    var sperre =
        dokumentSperreService.acquireSperre(
            bearbeiter, SperreTyp.MANUAL, "Delete AltIdentifikation", kod);

    kod.getAlternativeIdentifikationen()
        .removeIf(alt -> alt.getId().equals(altIdentifikationId));
    TEIKulturObjektDokumentCommand.updateMsIdentifierAndAllAltidentifier(kod);
    saveKulturObjektDokument(kod, UPDATE);
    dokumentSperreService.releaseSperre(sperre);
  }

  @Override
  @Transactional
  public void attributsReferenzenAusBeschreibungenAktualisieren(
      KulturObjektDokument kod,
      AttributsReferenzStatusTyp attributsReferenzStatusTyp, String changeID)
      throws HSPMapperException {

    if (kod == null) {
      throw new KulturObjektDokumentException("Das KulturObjektDokument darf nicht null sein!");
    }

    log.info("Beginne Aktualisierung der Attributs Referenzen für KOD: {}", kod.getId());

    Map<AttributsTyp, AttributsReferenz> kodReferenzen = loadAttributsReferenzenFromTei(kod);

    kod.setAttributsReferenzenAndAttributsReferenzStatusTyp(kodReferenzen);

    log.info("Load Number of KOD Attributsreferenzen {} ", kodReferenzen.size());

    List<Beschreibung> beschreibungen = kod.getBeschreibungenIDs().stream()
        .map(beschreibungsRepository::findByIdOptional)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(Beschreibung::isPublished)
        .toList();

    log.info("Load number of external Beschreibungen {}", beschreibungen.size());

    if (beschreibungen.isEmpty()) {
      log.info(
          "Keine Beschreibungen und keine AttributsReferenzen für KOD {} gefunden. Kein Update erforderlich.",
          kod.getId());
      return;
    }

    var aktuelleKodReferenzen = ermittelnAktuelleAttributsReferenzen(
        kodReferenzen, beschreibungen, changeID, attributsReferenzStatusTyp);
    kod.setAttributsReferenzenAndAttributsReferenzStatusTyp(aktuelleKodReferenzen);
    log.info("AttributsReferenzen für KOD {} erfolgreich aktualisiert.", kod.getId());
  }

  Map<AttributsTyp, AttributsReferenz> ermittelnAktuelleAttributsReferenzen(
      Map<AttributsTyp, AttributsReferenz> kodReferenzen,
      List<Beschreibung> beschreibungen, String changeID,
      AttributsReferenzStatusTyp attributsReferenzStatusTyp)
      throws HSPMapperException {

    if (beschreibungen.size() == 1) {
      return handleAttributsreferencesForOneBeschreibung(kodReferenzen,
          beschreibungen, changeID,
          attributsReferenzStatusTyp);
    }

    if (beschreibungen.size() > 1) {
      return handleAttributsreferencesForMultipleBeschreibungen(kodReferenzen,
          beschreibungen, changeID,
          attributsReferenzStatusTyp);
    }

    return kodReferenzen;
  }

  Map<AttributsTyp, AttributsReferenz> handleAttributsreferencesForMultipleBeschreibungen(
      Map<AttributsTyp, AttributsReferenz> kodReferenzen,
      List<Beschreibung> beschreibungen,
      String changeID, AttributsReferenzStatusTyp attributsReferenzStatusTyp)
      throws HSPMapperException {
    log.info(
        "Mehrere Beschreibungen gefunden - Vergleiche und übernehme die neuesten AttributsReferenzen.");
    Map<AttributsTyp, AttributsReferenz> beschreibungReferenzen =
        findeNeuesteAttributsReferenzenAusBeschreibungen(beschreibungen);

    beschreibungReferenzen.values()
        .forEach(ref ->
            beschreibungen.stream()
                .filter(b -> b.getId().equals(ref.getReferenzId()))
                .findFirst()
                .ifPresent(b -> {
                  ref.setPublikationDatum(b.getErstpublikationsDatum());
                  ref.setChangeID(changeID);
                  ref.setAttributsReferenzStatusTyp(attributsReferenzStatusTyp);
                  ref.setModificationDatum(LocalDateTime.now(ZoneId.systemDefault()));
                  b.getPURLs().stream()
                      .filter(purl -> purl.getTyp().equals(PURLTyp.INTERNAL))
                      .findFirst()
                      .ifPresent(internalPURL -> ref.setReferenzURL(
                          internalPURL.getPurl().toASCIIString()));
                })
        );

    if (kodReferenzen.isEmpty()) {
      return beschreibungReferenzen;
    }

    Arrays.stream(AttributsTyp.values())
        .forEach(typ -> {
          if (werkzeugOverrideAttributsReferenzen.isAttributsreferenzenOverride()
              || (kodReferenzen.get(typ) != null
              && !AttributsReferenzStatusTyp.MANUELL.equals(
              kodReferenzen.get(typ).getAttributsReferenzStatusTyp()))
              || kodReferenzen.get(typ) == null) {
            kodReferenzen.put(typ, beschreibungReferenzen.get(typ));
          }
        });

    return kodReferenzen;
  }

  Map<AttributsTyp, AttributsReferenz> handleAttributsreferencesForOneBeschreibung(
      Map<AttributsTyp, AttributsReferenz> kodReferenzen,
      List<Beschreibung> beschreibungen,
      String changeID, AttributsReferenzStatusTyp attributsReferenzStatusTyp)
      throws HSPMapperException {
    log.info("Nur eine Beschreibung gefunden - Übernehme AttributsReferenzen.");
    Map<AttributsTyp, AttributsReferenz> neueReferenzen = loadAttributsReferenzenFromTei(
        beschreibungen.getFirst());

    Beschreibung beschreibung = beschreibungen.getFirst();
    neueReferenzen.values().forEach(ref -> {
      ref.setPublikationDatum(beschreibung.getErstpublikationsDatum());
      ref.setChangeID(changeID);
      ref.setAttributsReferenzStatusTyp(attributsReferenzStatusTyp);
      ref.setModificationDatum(LocalDateTime.now(ZoneId.systemDefault()));
    });
    beschreibung.getPURLs().stream()
        .filter(purl -> purl.getTyp().equals(PURLTyp.INTERNAL))
        .findFirst().ifPresent(internalPURL ->
            neueReferenzen.values().forEach(
                ref -> ref.setReferenzURL(internalPURL.getPurl().toASCIIString())));

    if (kodReferenzen.isEmpty()) {
      return neueReferenzen;
    }

    kodReferenzen.entrySet().forEach(entry -> {
      if (werkzeugOverrideAttributsReferenzen.isAttributsreferenzenOverride()
          || !AttributsReferenzStatusTyp.MANUELL.equals(
          entry.getValue().getAttributsReferenzStatusTyp())) {
        entry.setValue(neueReferenzen.get(entry.getKey()));
      }
    });

    return kodReferenzen;
  }

  Map<AttributsTyp, AttributsReferenz> findeNeuesteAttributsReferenzenAusBeschreibungen(
      List<Beschreibung> beschreibungen) throws HSPMapperException {

    Map<AttributsTyp, AttributsReferenz> attributsReferenzenMap = new EnumMap<>(AttributsTyp.class);

    for (Beschreibung beschreibung : beschreibungen) {
      Map<AttributsTyp, AttributsReferenz> referenzenAusBeschreibung = loadAttributsReferenzenFromTei(
          beschreibung);

      for (Map.Entry<AttributsTyp, AttributsReferenz> entry : referenzenAusBeschreibung.entrySet()) {
        AttributsTyp typ = entry.getKey();
        AttributsReferenz neueReferenz = entry.getValue();

        if (neueReferenz == null) {
          continue;
        }

        neueReferenz.setPublikationDatum(
            beschreibung.getErstpublikationsDatum());
        AttributsReferenz aktuelleReferenz = attributsReferenzenMap.get(typ);

        // Falls keine aktuelle Referenz existiert oder das Datum neuer ist, ersetzen
        if (sollReferenzUebernommenWerden(aktuelleReferenz, neueReferenz)) {

          attributsReferenzenMap.put(typ, neueReferenz);
          log.debug(
              "Neue AttributsReferenz für {} übernommen mit Publikationsdatum {}",
              typ, neueReferenz.getPublikationDatum());
        }
      }
    }
    return attributsReferenzenMap;
  }

  @Override
  public Map<AttributsTyp, AttributsReferenz> loadAttributsReferenzenFromTei(
      TEIDokument teiDokument) throws HSPMapperException {
    if (teiDokument == null || teiDokument.getTeiXML() == null
        || teiDokument.getTeiXML()
        .isEmpty()) {
      throw new IllegalArgumentException(
          "Das TEI-Dokument darf nicht null oder leer sein!");
    }
    return TEI2AttributsReferenzMapper.map(teiDokument.getTeiXML());
  }

  @Override
  @Transactional(TxType.REQUIRES_NEW)
  @TransactionConfiguration(timeout = 60000)
  public Set<KulturObjekteValidationReportItem> findAllInvalid() {
    Set<KulturObjekteValidationReportItem> report = new HashSet<>();
    AtomicInteger started = new AtomicInteger();
    AtomicInteger succeeded = new AtomicInteger();

    kulturObjektDokumentRepository
        .selectAllTeiXml()
        .forEach(xmlDokument ->
            validateAndCollect(xmlDokument, report, started, succeeded)
        );

    log.info("Validation finished (KOD). totals: started={}, succeeded={}, failed={}",
        started.get(), succeeded.get(), started.get() - succeeded.get());

    return report;
  }

  @Override
  @Transactional
  public KulturObjektDokument updateKODXML(String id) throws Exception {

    if (id == null || id.isEmpty()) {
      throw new IllegalArgumentException("kod id must not be null or empty");
    }

    KulturObjektDokument kod = kulturObjektDokumentRepository.findById(id);

    if (kod != null) {
      TEIKulturObjektDokumentCommand.updateSurrogatesDigitalisate(kod);
      TEIKulturObjektDokumentCommand.updateKODBeschreibungenReferenzes(kod);

      teiXmlValidationBoundary.validateTeiXml(kod);
      kulturObjektDokumentRepository.save(kod);

      log.info("Updated KOD XML for id {}", id);

      return kod;
    }
    return null;
  }

  private void validateAndCollect(XMLDokument xmlDokument,
      Set<KulturObjekteValidationReportItem> report,
      AtomicInteger started,
      AtomicInteger succeeded) {
    int startedNow = started.incrementAndGet();
    log.info("Start validation of KulturObjektDokument {} (started={}, succeeded={})",
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
        KulturObjektDokument kod = kulturObjektDokumentRepository.findById(xmlDokument.getId());
        if (kod != null) {
          report.add(buildReportItem(kod, validationResult));
        } else {
          log.warn("KOD entity not found by id {}", xmlDokument.getId());
        }
      }

    } catch (Exception e) {
      log.error("Validation call failed for KOD {} (started={}, succeeded={}): {}",
          xmlDokument.getId(), started.get(), succeeded.get(), e.getMessage(), e);
      throw new IllegalStateException("Validation service unavailable (IMPORTSERVICE/Eureka).", e);
    }
  }

  private KulturObjekteValidationReportItem buildReportItem(KulturObjektDokument kod,
      ValidationResult validationResult) {
    Identifikation id = kod.getGueltigeIdentifikation();

    String signatur = (id != null && id.getIdent() != null)
        ? id.getIdent() : "";

    String einrichtung =
        (id != null && id.getBesitzer() != null && id.getBesitzer().getName() != null)
            ? id.getBesitzer().getName() : "";

    String ort = (id != null && id.getAufbewahrungsOrt() != null
        && id.getAufbewahrungsOrt().getName() != null)
        ? id.getAufbewahrungsOrt().getName() : "";

    return new KulturObjekteValidationReportItem.KulturObjekteValidationReportItemBuilder()
        .kulturobjektHspID(kod.getId())
        .signatur(signatur)
        .bestandsHaltendeEinrichtung(einrichtung)
        .ort(ort)
        .validationResult(validationResult)
        .build();
  }

  KulturobjektDokumentUmziehenErgebnis createKODUmziehenErgebnis(
      NormdatenReferenz neuerBesitzer,
      NormdatenReferenz neuerOrt,
      Entry<String, String> signaturEntry) {
    try {

      boolean isKODGesperrt =
          dokumentSperreService.isGesperrt(
              new SperreEintrag(signaturEntry.getKey(), SperreDokumentTyp.KOD));

      List<KulturObjektDokument> kodsExists =
          kulturObjektDokumentRepository
              .findByIdentifikationIdentAndBesitzerIDAndAufbewahrungsortID(
                  signaturEntry.getValue(), neuerBesitzer.getId(),
                  neuerOrt.getId());

      log.info("Found already existing kod {} ", kodsExists.size());

      return new KulturobjektDokumentUmziehenErgebnis(
          signaturEntry.getKey(),
          signaturEntry.getValue(),
          kodsExists.stream().map(KulturObjektDokument::getId).toList(),
          neuerOrt,
          neuerBesitzer,
          LocalDateTime.now(ZoneId.systemDefault()),
          isKODGesperrt ? KOD_IST_LOCKED : "");

    } catch (Throwable error) {
      log.error("Error during KOD umziehen", error);
      return new KulturobjektDokumentUmziehenErgebnis(
          signaturEntry.getKey(),
          signaturEntry.getValue(),
          List.of(),
          neuerOrt,
          neuerBesitzer,
          LocalDateTime.now(ZoneId.systemDefault()),
          error.getMessage());
    }
  }

  void createAndAddInternalPURLs(List<KulturObjektDokument> kods) {
    log.info(
        "createAndAddInternalPURLs: purlAutogenerateEnabled={}, kods={}",
        purlAutogenerateEnabled,
        kods.size());
    if (purlAutogenerateEnabled) {
      for (KulturObjektDokument kod : kods) {
        try {
          PURL purl = purlService.createNewPURL(kod.getId(),
              DokumentObjektTyp.HSP_OBJECT);
          kod.getPURLs().add(purl);
        } catch (Exception e) {
          // just catch exception and register KODs anyway:
          log.error(
              "createAndAddInternalPURLs: error registering PURL for kod {}",
              kod.getId(), e);
          return;
        }
      }
    }
  }

  Set<AttributsReferenz> getAttributsReferenzenFromKODViewModel(
      KulturObjektDokumentViewModel kodViewModel) {
    if (Objects.nonNull(kodViewModel)) {
      return Stream.of(
              kodViewModel.getTitel(),
              kodViewModel.getBeschreibstoff(),
              kodViewModel.getUmfang(),
              kodViewModel.getAbmessung(),
              kodViewModel.getFormat(),
              kodViewModel.getEntstehungsort(),
              kodViewModel.getEntstehungszeit(),
              kodViewModel.getGrundsprache(),
              kodViewModel.getBuchschmuck(),
              kodViewModel.getHandschriftentyp(),
              kodViewModel.getMusiknotation(),
              kodViewModel.getStatus())
          .filter(Objects::nonNull)
          .collect(toCollection(LinkedHashSet::new));
    } else {
      return Collections.emptySet();
    }
  }
}
