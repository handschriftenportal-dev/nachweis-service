package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.DUPLICATE;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.IDENTIFICATION_INCOMPLETE;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.KOD_AMBIGUOUS;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.KOD_CONFLICTING;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.KOD_LOCKED;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.KOD_NOT_FOUND;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.UUID_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.AttributsReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Beschreibstoff;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Buchschmuck;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Handschriftentyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Musiknotation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation.PublikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre.SperreBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Status;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Titel;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Umfang;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AbmessungWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.EntstehungszeitWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessource;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessourceTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Forschungsdokumentation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.FormatWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPMapperException;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEI2AttributsReferenzMapper;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.mapper.tei.kod.TEIExterneRessourcenCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.kod.TEIForschungsdokumentationCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.kod.TEIKulturObjektDokumentCommand;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.ExterneRessourcenImportController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.ExterneRessourcenImportModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel.BeschreibungsViewBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DigitalisatViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel.KulturObjektDokumentViewModelBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.PublikationViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.SignatureValue;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.events.KulturObjektDokumentRegistriertEvent;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportCSVParser;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportEntryService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportResultCSVWriter;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportStateService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportStatus;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodregistrieren.KulturObjektDokumentRegistrierenException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodregistrieren.KulturObjektDokumentRegistrierenException.ERROR_TYPE;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.KulturObjektSortkeyUpdateException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.KulturObjektSortkeyUpdateReport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortKeyBatchResult;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortKeyEntries;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortKeyEntry;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortkeyBatchService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortkeyImportCSVParser;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortkeyPartialImportException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.minimaldaten.IdentifikationRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.papierkorb.PapierkorbBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.SperreRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugOverrideAttributsReferenzen;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaNormdatenProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.network.DistributedSaveCommand;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.network.DistributedSaveKODCommand;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.IdentifikationRepositoryAdapter;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.KulturObjektDokumentListDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.XMLDokument;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.primefaces.model.StreamedContent;
import wiremock.org.xmlunit.builder.DiffBuilder;
import wiremock.org.xmlunit.diff.DefaultNodeMatcher;
import wiremock.org.xmlunit.diff.Diff;
import wiremock.org.xmlunit.diff.ElementSelectors;

@QuarkusTest
@Execution(ExecutionMode.SAME_THREAD)
class KulturObjektDokumentServiceTest {

  public static final String ENTITY_ID = "entityId";
  public static final String GUELTIGE_SIGNATURE = "a-007-777";
  public static final Bearbeiter BEARBEITER = new Bearbeiter("1",
      "Unbekannter Bearbeiter");
  public static final String NEUE_SIGNATUR = "neueGueltigeSignatur";
  private static final Bearbeiter SPERRENDER_BEARBEITER =
      new Bearbeiter.BearbeiterBuilder()
          .withId("sperrenderBearbeiter")
          .withBearbeitername("Hans")
          .withNachname("dwfe")
          .withVorname("1234123")
          .build();
  private static final String TEI_FILENAME = "kulturobjekteimport.xml";
  private static final Path TEI_FILE_PATH = Paths.get("src", "test",
      "resources", TEI_FILENAME);
  private static final String TEI_KOD_MSDESC_BESCHREIBUNG = "tei-kod-beschreibung.xml";
  private static final Path TEI_KOD_MSDESC_BESCHREIBUNG_PATH =
      Paths.get("src", "test", "resources", TEI_KOD_MSDESC_BESCHREIBUNG);
  private static final String KOD_ID = "HSP-123";
  private static final String HSC_URL = "https://handschriftencensus.de/123";
  private static final String ECODICES_URL = "https://e-codices.ch/abc";
  private static final String INVALID_TEI_XML = "<TEI><invalid>";

  @Inject
  KulturObjektDokumentService kulturObjektDokumentService;
  @Inject
  KulturObjektDokumentRepository kulturObjektDokumentRepository;
  @Inject
  BeschreibungsRepository beschreibungsRepository;
  @Inject
  BeschreibungsService beschreibungService;
  @Inject
  IdentifikationRepository identifikationRepository;
  @Inject
  DokumentSperreBoundary dokumentSperreService;
  @Inject
  BearbeiterRepository bearbeiterRepository;
  @Inject
  SperreRepository sperreRepository;
  @Inject
  NormdatenReferenzRepository normdatenReferenzRepository;
  @Inject
  EntityManager entityManager;
  @Inject
  BearbeiterBoundary bearbeiterBoundary;
  @Inject
  WerkzeugOverrideAttributsReferenzen werkzeugOverrideAttributsReferenzen;

  KulturObjektDokumentRepository kulturObjektDokumentRepositoryMOCK;
  IdentifikationRepository identifikationRepositoryMOCK;
  BeschreibungsRepository beschreibungsRepositoryMOCK;
  BeschreibungsService beschreibungServiceMOCK;
  SuchDokumentService suchDokumentServiceMOCK;
  DokumentSperreService dokumentSperreServiceMOCK;
  KafkaIndexingProducer kafkaIndexingProducerMOCK;
  PURLBoundary purlServiceMOCK;
  PapierkorbBoundary papierkorbServiceMOCK;
  KafkaNormdatenProducer kafkaNormdatenProducer;
  DistributedSaveCommand<KulturObjektDokument> distributedSaveCommandMOCK;
  KulturObjektDokumentService kulturObjektDokumentServiceMOCK;
  TeiXmlValidationBoundary teiXmlValidationBoundaryMOCK;
  SortkeyImportCSVParser sortkeyUpdateImportParserMock;
  SortkeyBatchService sortkeyBatchServiceMOCK;
  Event<KulturObjektDokumentRegistriertEvent> kodRegistriertEventPublisherMOCK;

  private static NormdatenReferenz createDresden() {
    return new NormdatenReferenz("D", "Dresden", "");
  }

  private static NormdatenReferenz createSLB() {
    return new NormdatenReferenz("SLUB",
        "Sächsische Landesbibliothek - Staats- und Universitätsbibliothek Dresden", "");
  }

  private static Identifikation createIdnoMscrDresdA111(NormdatenReferenz koerperschaft,
      NormdatenReferenz aufbewahrungsOrt) {
    return new IdentifikationBuilder()
        .withId("ident1")
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(koerperschaft)
        .withAufbewahrungsOrt(aufbewahrungsOrt)
        .build();
  }

  private static Sperre testSperre() {
    return new SperreBuilder()
        .withBearbeiter(BEARBEITER)
        .withSperreTyp(SperreTyp.MANUAL)
        .build();
  }

  @BeforeEach
  @Transactional(TxType.REQUIRES_NEW)
  void beforeEach() {
    kulturObjektDokumentRepositoryMOCK = mock(
        KulturObjektDokumentRepository.class);
    identifikationRepositoryMOCK = mock(IdentifikationRepositoryAdapter.class);
    beschreibungsRepositoryMOCK = mock(BeschreibungsRepository.class);
    beschreibungServiceMOCK = mock(BeschreibungsService.class);
    suchDokumentServiceMOCK = mock(SuchDokumentService.class);
    dokumentSperreServiceMOCK = mock(DokumentSperreService.class);
    kafkaIndexingProducerMOCK = mock(KafkaIndexingProducer.class);
    purlServiceMOCK = mock(PURLBoundary.class);
    papierkorbServiceMOCK = mock(PapierkorbBoundary.class);
    kafkaNormdatenProducer = mock(KafkaNormdatenProducer.class);
    distributedSaveCommandMOCK = mock(DistributedSaveKODCommand.class);
    teiXmlValidationBoundaryMOCK = mock(TeiXmlValidationBoundary.class);
    sortkeyUpdateImportParserMock = mock(SortkeyImportCSVParser.class);
    sortkeyBatchServiceMOCK = mock(SortkeyBatchService.class);
    kodRegistriertEventPublisherMOCK = mock();

    bearbeiterRepository.saveAndFlush(BEARBEITER);
    bearbeiterRepository.save(SPERRENDER_BEARBEITER);

    kulturObjektDokumentServiceMOCK =
        new KulturObjektDokumentService(
            kulturObjektDokumentRepositoryMOCK,
            identifikationRepository,
            suchDokumentServiceMOCK,
            beschreibungServiceMOCK,
            dokumentSperreServiceMOCK,
            kafkaIndexingProducerMOCK,
            beschreibungsRepositoryMOCK,
            purlServiceMOCK,
            papierkorbServiceMOCK,
            false,
            distributedSaveCommandMOCK, bearbeiterBoundary,
            werkzeugOverrideAttributsReferenzen,
            teiXmlValidationBoundaryMOCK,
            sortkeyUpdateImportParserMock,
            sortkeyBatchServiceMOCK,
            kodRegistriertEventPublisherMOCK);
  }

  @AfterEach
  @Transactional(TxType.REQUIRES_NEW)
  void afterEach() {
    sperreRepository.listAll(false)
        .forEach(s -> sperreRepository.deleteByIdAndFlush(s.getId()));
    bearbeiterRepository.deleteByIdAndFlush(BEARBEITER.getId());
    bearbeiterRepository.deleteByIdAndFlush(SPERRENDER_BEARBEITER.getId());
  }

  @Test
  @TestTransaction
  void testGetAllSignaturesWithKodIDs() {

    Identifikation identifikation =
        new IdentifikationBuilder()
            .withId("007")
            .withIdent(GUELTIGE_SIGNATURE)
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();

    KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokumentBuilder()
            .withId(ENTITY_ID)
            .withGueltigerIdentifikation(identifikation)
            .build();

    int initialSize = kulturObjektDokumentServiceMOCK.getAllSignaturesWithKodIDs()
        .size();

    kulturObjektDokumentRepository.save(kulturObjektDokument);

    Map<String, SignatureValue> actual =
        kulturObjektDokumentServiceMOCK.getAllSignaturesWithKodIDs();
    assertNotNull(actual);

    assertFalse(actual.isEmpty());

    assertEquals(initialSize + 1L, actual.size());

    assertEquals(GUELTIGE_SIGNATURE, actual.get(ENTITY_ID).getSignature());
    kulturObjektDokumentRepository.deleteByIdAndFlush(
        kulturObjektDokument.getId());
  }

  @Test
  void testFindById() {

    kulturObjektDokumentServiceMOCK.findById("12");

    verify(kulturObjektDokumentRepositoryMOCK, times(1)).findByIdOptional("12");
  }

  @Test
  void testFindAll() {

    kulturObjektDokumentServiceMOCK.findAll();

    verify(kulturObjektDokumentRepositoryMOCK, times(1)).listAll(true);
  }

  @Test
  void testregistrieren()
      throws IOException, KulturObjektDokumentRegistrierenException, SperreAlreadyExistException {
    Path csvFile = Paths.get("src", "test", "resources", "BSB_SignList.csv");

    NormdatenReferenz modelKoerperschaft =
        new NormdatenReferenz(
            TEIValues.UUID_PREFIX + UUID.randomUUID(),
            "Staatsbibliothek zu Berlin", "234234-x");

    NormdatenReferenz modelOrt =
        new NormdatenReferenz(TEIValues.UUID_PREFIX + UUID.randomUUID(),
            "Berlin", "4005728-8");

    Collection<KulturObjektDokument> kods =
        kulturObjektDokumentServiceMOCK.registrieren(
            new Bearbeiter("1", "Unbekannter Tester"),
            modelOrt,
            modelKoerperschaft,
            Files.readAllBytes(csvFile));

    assertEquals(53, kods.size());
    verify(kodRegistriertEventPublisherMOCK, times(1))
        .fire(any(KulturObjektDokumentRegistriertEvent.class));
  }

  @Test
  void testRegistrierenFiresNoEventForEmptyKodList()
      throws KulturObjektDokumentRegistrierenException {
    kulturObjektDokumentServiceMOCK.registrieren(
        new Bearbeiter("1", "Unbekannter Tester"), List.of());

    verifyNoInteractions(kodRegistriertEventPublisherMOCK);
  }

  @Test
  void testRegistrieren_list() throws Exception {
    when(purlServiceMOCK.createNewPURL(anyString(),
        any(DokumentObjektTyp.class)))
        .thenAnswer(
            i ->
                new PURL(
                    URI.create("https://resolver.de/" + i.getArguments()[0]),
                    URI.create("https://target.de/" + i.getArguments()[0]),
                    PURLTyp.INTERNAL));

    NormdatenReferenz koerperschaft =
        new NormdatenReferenz(
            TEIValues.UUID_PREFIX + UUID.randomUUID(),
            "Staatsbibliothek zu Berlin", "234234-x");

    NormdatenReferenz ort =
        new NormdatenReferenz(TEIValues.UUID_PREFIX + UUID.randomUUID(),
            "Berlin", "4005728-8");

    KulturObjektDokument kod1 =
        new KulturObjektDokument(
            new KulturObjektDokumentBuilder()
                .withId("HSP-1")
                .withGueltigerIdentifikation(
                    new Identifikation(
                        new IdentifikationBuilder()
                            .withId(UUID.randomUUID().toString())
                            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
                            .withIdent("Ms. germ. oct. 6")
                            .withAufbewahrungsOrt(ort)
                            .withBesitzer(koerperschaft)))
                .addAlternativeIdentifikation(
                    new Identifikation(
                        new IdentifikationBuilder()
                            .withId(UUID.randomUUID().toString())
                            .withIdentTyp(IdentTyp.ALTSIGNATUR)
                            .withIdent("Ms.germ.oct. 6")
                            .withAufbewahrungsOrt(ort)
                            .withBesitzer(koerperschaft)))
                .addAlternativeIdentifikation(
                    new Identifikation(
                        new IdentifikationBuilder()
                            .withId(UUID.randomUUID().toString())
                            .withIdentTyp(IdentTyp.ALTSIGNATUR)
                            .withIdent("Ms.germ.oct. 6")
                            .withAufbewahrungsOrt(ort)
                            .withBesitzer(koerperschaft))));

    KulturObjektDokument kod2 =
        new KulturObjektDokument(
            new KulturObjektDokumentBuilder()
                .withId("HSP-1")
                .withGueltigerIdentifikation(
                    new Identifikation(
                        new IdentifikationBuilder()
                            .withId(UUID.randomUUID().toString())
                            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
                            .withIdent("Hdschr. 25")
                            .withAufbewahrungsOrt(ort)
                            .withBesitzer(koerperschaft)))
                .addAlternativeIdentifikation(
                    new Identifikation(
                        new IdentifikationBuilder()
                            .withId(UUID.randomUUID().toString())
                            .withIdentTyp(IdentTyp.ALTSIGNATUR)
                            .withIdent("Hdschr 25")
                            .withAufbewahrungsOrt(ort)
                            .withBesitzer(koerperschaft))));

    List<KulturObjektDokument> kodsZuRegistrieren = Arrays.asList(kod1, kod2);

    KulturObjektDokumentService service =
        new KulturObjektDokumentService(
            kulturObjektDokumentRepositoryMOCK,
            identifikationRepositoryMOCK,
            suchDokumentServiceMOCK,
            beschreibungServiceMOCK,
            dokumentSperreServiceMOCK,
            kafkaIndexingProducerMOCK,
            beschreibungsRepositoryMOCK,
            purlServiceMOCK,
            papierkorbServiceMOCK,
            true,
            distributedSaveCommandMOCK, bearbeiterBoundary,
            werkzeugOverrideAttributsReferenzen,
            teiXmlValidationBoundaryMOCK,
            sortkeyUpdateImportParserMock,
            sortkeyBatchServiceMOCK,
            kodRegistriertEventPublisherMOCK);

    Collection<KulturObjektDokument> kodsRegistriert =
        service.registrieren(new Bearbeiter("1", "Unbekannter Tester"),
            kodsZuRegistrieren);

    assertNotNull(kodsRegistriert);
    assertEquals(2, kodsRegistriert.size());
    verify(distributedSaveCommandMOCK, times(2)).execute(any(), any());
    verify(purlServiceMOCK, times(2))
        .createNewPURL(any(String.class), any(DokumentObjektTyp.class));

    kodsRegistriert.forEach(
        kod ->
            assertTrue(
                kod.getPURLs().stream()
                    .anyMatch(purl -> PURLTyp.INTERNAL == purl.getTyp())));
  }

  @Test
  void testCheckBadEncoding() {

    Throwable exceptionThatWasThrown =
        assertThrows(
            KulturObjektDokumentRegistrierenException.class,
            () ->
                kulturObjektDokumentServiceMOCK.checkEncoding(
                    StandardCharsets.ISO_8859_1.encode("Cod. I.2.2º 1")
                        .array()));

    assertEquals(
        "Signaturen CSV Datei hat keine UTF-8- oder US-ASCII-Codierung! (WINDOWS-1252)",
        exceptionThatWasThrown.getMessage());
  }

  @Test
  void testregistrierenWithKODAlreadyExists() {
    Path csvFile = Paths.get("src", "test", "resources", "BSB_SignList.csv");

    NormdatenReferenz koerperschaft =
        new NormdatenReferenz(
            TEIValues.UUID_PREFIX + UUID.randomUUID(),
            "Staatsbibliothek zu Berlin", "234234-x");

    NormdatenReferenz ort =
        new NormdatenReferenz(TEIValues.UUID_PREFIX + UUID.randomUUID(),
            "Berlin", "4005728-8");

    Identifikation identifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdent("Cbm Cat. 1")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(koerperschaft)
            .withAufbewahrungsOrt(ort)
            .build();
    KulturObjektDokument kulturObjektDokumentAlreadyExist =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withGueltigerIdentifikation(identifikation)
            .build();

    when(kulturObjektDokumentRepositoryMOCK
        .findByIdentifikationIdentAndBesitzerIDAndAufbewahrungsortID(
            "Cbm Cat. 1", koerperschaft.getId(), ort.getId()))
        .thenReturn(List.of(kulturObjektDokumentAlreadyExist));

    Throwable exceptionThatWasThrown =
        assertThrows(
            KulturObjektDokumentRegistrierenException.class,
            () ->
                kulturObjektDokumentServiceMOCK.registrieren(
                    new Bearbeiter("1", "Unbekannter Tester"),
                    ort,
                    koerperschaft,
                    Files.readAllBytes(csvFile)));

    assertEquals(
        "Kulturobjekt mit gültiger Signatur bereits im System: Cbm Cat. 1",
        exceptionThatWasThrown.getMessage());
  }

  @Test
  void testRegistrierenWithKODExistsInDatei() throws Exception {
    Path csvFile = Paths.get("src", "test", "resources", "BSB_SignList.csv");

    NormdatenReferenz modelKoerperschaft =
        new NormdatenReferenz(
            TEIValues.UUID_PREFIX + UUID.randomUUID(),
            "Staatsbibliothek zu Berlin", "234234-x");

    NormdatenReferenz modelOrt =
        new NormdatenReferenz(TEIValues.UUID_PREFIX + UUID.randomUUID(),
            "Berlin", "4005728-8");

    final String csvData =
        Files.readString(csvFile, StandardCharsets.UTF_8)
            + "\"Ms. lat. oct. 118\"\n";

    KulturObjektDokumentRegistrierenException exceptionThatWasThrown =
        assertThrows(
            KulturObjektDokumentRegistrierenException.class,
            () ->
                kulturObjektDokumentServiceMOCK.registrieren(
                    new Bearbeiter("1", "Unbekannter Tester"),
                    modelOrt,
                    modelKoerperschaft,
                    csvData.getBytes(StandardCharsets.UTF_8)));

    assertEquals(
        "Gültige Signatur mehrfach in Datei vorhanden: Ms. lat. oct. 118",
        exceptionThatWasThrown.getMessage());
    assertEquals(ERROR_TYPE.GUELTIGE_SIGNATUR_IN_DATEI,
        exceptionThatWasThrown.getErrorType());
  }

  @Test
  void testregistrierenWithTechnicalError() throws Exception {
    Path csvFile = Paths.get("src", "test", "resources", "BSB_SignList.csv");

    NormdatenReferenz modelKoerperschaft =
        new NormdatenReferenz(
            TEIValues.UUID_PREFIX + UUID.randomUUID(),
            "Staatsbibliothek zu Berlin", "234234-x");

    NormdatenReferenz modelOrt =
        new NormdatenReferenz(TEIValues.UUID_PREFIX + UUID.randomUUID(),
            "Berlin", "4005728-8");

    doThrow(new Exception("Datenbank Fehler!"))
        .when(distributedSaveCommandMOCK)
        .execute(any(), any());

    Bearbeiter bearbeiter = new Bearbeiter("1", "Unbekannter Tester");
    byte[] csvInhalt = Files.readAllBytes(csvFile);

    Throwable error =
        assertThrows(
            Exception.class,
            () -> kulturObjektDokumentServiceMOCK.registrieren(
                bearbeiter, modelOrt, modelKoerperschaft, csvInhalt));

    assertEquals(
        "Kulturobjektdokument Cbm Cat. 1 konnte nicht gespeichert werden! Datenbank Fehler!",
        error.getMessage());
  }

  @Test
  void testcheckIfKulturObjektAlreadyExists() {

    Identifikation identifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdent("Signatur")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(
                new NormdatenReferenz("1", "Staatsbibliothek zu Berlin", ""))
            .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", ""))
            .build();

    Identifikation unkownIdentifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdent("Unkown")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(
                new NormdatenReferenz("1", "Staatsbibliothek zu Berlin", ""))
            .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", ""))
            .build();

    Identifikation alternativeIdentifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdent("Alternative Signatur")
            .withIdentTyp(IdentTyp.ALTERNATIVE_SCHREIBUNG)
            .withBesitzer(
                new NormdatenReferenz("1", "Staatsbibliothek zu Berlin", ""))
            .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", ""))
            .build();

    KulturObjektDokument kulturObjektDokumentAlreadyExist =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withGueltigerIdentifikation(identifikation)
            .build();

    KulturObjektDokument kulturObjektDokumentAlternative =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withGueltigerIdentifikation(unkownIdentifikation)
            .addAllAlternativeIdentifikationen(
                Set.of(alternativeIdentifikation, identifikation))
            .build();

    KulturObjektDokument kulturObjektDokumentSuccess =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withGueltigerIdentifikation(unkownIdentifikation)
            .build();

    when(kulturObjektDokumentRepositoryMOCK
        .findByIdentifikationIdentAndBesitzerIDAndAufbewahrungsortID("Signatur",
            "1", "2"))
        .thenReturn(List.of(kulturObjektDokumentAlreadyExist));

    List<KulturObjektDokument> result;

    result =
        kulturObjektDokumentServiceMOCK.checkIfKulturObjektAlreadyExists(
            List.of(kulturObjektDokumentAlreadyExist));
    assertNotNull(result);
    assertTrue(result.contains(kulturObjektDokumentAlreadyExist));

    result =
        kulturObjektDokumentServiceMOCK.checkIfKulturObjektAlreadyExists(
            List.of(kulturObjektDokumentAlternative));
    assertNotNull(result);
    assertTrue(result.isEmpty());

    result =
        kulturObjektDokumentServiceMOCK.checkIfKulturObjektAlreadyExists(
            List.of(kulturObjektDokumentSuccess));
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testCheckIfKulturObjektAlreadyInDatei() {

    Identifikation identifikation1 =
        new Identifikation.IdentifikationBuilder()
            .withIdent("Signatur 1")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(
                new NormdatenReferenz("1", "Staatsbibliothek zu Berlin", ""))
            .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", ""))
            .build();

    Identifikation identifikation2 =
        new Identifikation.IdentifikationBuilder()
            .withIdent("Signatur 2")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(
                new NormdatenReferenz("1", "Staatsbibliothek zu Berlin", ""))
            .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", ""))
            .build();

    KulturObjektDokument kod1 =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withGueltigerIdentifikation(identifikation1)
            .build();

    KulturObjektDokument kod2 =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withGueltigerIdentifikation(identifikation2)
            .build();

    List<KulturObjektDokument> result;

    result =
        kulturObjektDokumentServiceMOCK.checkIfKulturObjektAlreadyInDatei(
            List.of(kod1, kod2));
    assertNotNull(result);
    assertTrue(result.isEmpty());

    result =
        kulturObjektDokumentServiceMOCK.checkIfKulturObjektAlreadyInDatei(
            List.of(kod1, kod2, kod1));
    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.contains(kod1));

    result =
        kulturObjektDokumentServiceMOCK.checkIfKulturObjektAlreadyInDatei(
            List.of(kod1, kod2, kod1, kod1));
    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.contains(kod1));
  }

  @TestTransaction
  @Test
  void testSaveKulturObjektDokumentInSystem() throws Exception {
    KulturObjektDokument kulturObjektDokument = createKulturObjektDokument();

    kulturObjektDokumentServiceMOCK.saveKulturObjektDokumentInSystem(
        List.of(kulturObjektDokument));

    assertNotNull(kulturObjektDokument.getTeiXML());

    verify(distributedSaveCommandMOCK, times(1))
        .execute(kulturObjektDokument, ActivityStreamAction.ADD);
  }

  @Test
  void testAddKulturObjektDokumentToActivityStreamMessage() throws Exception {
    ActivityStream message = ActivityStream.builder().build();
    KulturObjektDokument kod = createKulturObjektDokument();

    Throwable exceptionThatWasThrown =
        assertThrows(
            NullPointerException.class,
            () ->
                KulturObjektDokumentService.addKulturObjektDokumentToActivityStreamMessage(
                    null, null));
    assertEquals("message is required!", exceptionThatWasThrown.getMessage());

    exceptionThatWasThrown =
        assertThrows(
            NullPointerException.class,
            () ->
                KulturObjektDokumentService.addKulturObjektDokumentToActivityStreamMessage(
                    message, null));
    assertEquals("kod is required!", exceptionThatWasThrown.getMessage());

    KulturObjektDokumentService.addKulturObjektDokumentToActivityStreamMessage(
        message, kod);
    assertNotNull(message.getObjects());
    assertEquals(1, message.getObjects().size());

    ActivityStreamObject activityStreamObjectKOD = message.getObjects()
        .getFirst();
    assertEquals(kod.getId(), activityStreamObjectKOD.getId());
    assertArrayEquals(
        kod.getTeiXML().getBytes(StandardCharsets.UTF_8),
        activityStreamObjectKOD.getContent(),
        "Content not equal");
    assertTrue(activityStreamObjectKOD.isCompressed());
    assertSame(ActivityStreamsDokumentTyp.KOD,
        activityStreamObjectKOD.getType());
  }

  private KulturObjektDokument createKulturObjektDokument()
      throws IOException {

    Path teiFilePath =
        Paths.get(
            "../domainmodel-tei-mapper/src", "test", "resources", "tei",
            "tei-msDesc_Westphal.xml");

    Identifikation identifikation =
        new IdentifikationBuilder()
            .withIdent("Signatur")
            .withBesitzer(new NormdatenReferenz("NR-1", "Berlin", ""))
            .withAufbewahrungsOrt(
                new NormdatenReferenz("NR-2", "Staatsbibliothek", ""))
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();

    return new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("KOD1")
        .withGueltigerIdentifikation(identifikation)
        .withTEIXml(Files.readString(teiFilePath))
        .withBeschreibungsdokumentIDs(Set.of("1234"))
        .build();
  }

  private Beschreibung createBeschreibung(KulturObjektDokument kod) {
    PURL internal =
        new PURL(
            URI.create("https://resolver.url/" + KOD_ID),
            URI.create("https://target.url/" + KOD_ID),
            PURLTyp.INTERNAL);

    Publikation publikation = new Publikation.PublikationBuilder()
        .addPURL(internal)
        .build();

    BeschreibungsKomponenteKopf kopf =
        new BeschreibungsKomponenteKopfBuilder()
            .withId("777")
            .withTitel("Beschreibung Titel")
            .build();
    String beschreibungId = "1234";
    Beschreibung beschreibung =
        new BeschreibungsBuilder(beschreibungId, kod.getId(),
            kod.getGueltigeIdentifikation())
            .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
            .withPublikation(publikation)
            .withTEIXml("<TEI/>")
            .build();
    beschreibung.getBeschreibungsStruktur().add(kopf);
    return beschreibung;
  }

  @Test
  void testDigitalisatHinzufuegen() throws Exception {

    KulturObjektDokument result =
        new KulturObjektDokumentBuilder()
            .withId("1")
            .withTEIXml(FileUtils.readFileToString(TEI_FILE_PATH.toFile(),
                StandardCharsets.UTF_8))
            .build();

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel =
        KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
            .withId("1")
            .withTeiXML(FileUtils.readFileToString(TEI_FILE_PATH.toFile(),
                StandardCharsets.UTF_8))
            .build();

    NormdatenReferenz modelKoerperschaft =
        new NormdatenReferenz(
            "6790851b-9519-3874-a9fd-0839d494a3c9",
            "Staatsbibliothek zu Berlin",
            "5036103-X",
            GNDEntityFact.CORPORATE_BODY_TYPE_NAME);

    NormdatenReferenz modelOrt =
        new NormdatenReferenz(
            "ee1611b6-1f56-38e7-8c12-b40684dbb395",
            "Berlin",
            "4005728-8",
            GNDEntityFact.PLACE_TYPE_NAME);

    DigitalisatViewModel digitalisatViewModel =
        DigitalisatViewModel.builder()
            .manifestURL(
                "https://iiif.ub.uni-leipzig.de/0000009089/manifest.json")
            .digitalisierungsdatum(LocalDate.now())
            .thumbnail(
                "https://iiif.ub.uni-leipzig.de/fcgi-bin/iipsrv.fcgi?iiif=/j2k/0000/0090/0000009089/00000057.jpx/full/500,/0/default.jpg")
            .ort(modelOrt)
            .einrichtung(modelKoerperschaft)
            .digitalisatTypKonzepte(new ArrayList<>())
            .build();

    when(kulturObjektDokumentRepositoryMOCK.findByIdOptional("1"))
        .thenReturn(Optional.of(result));
    when(kulturObjektDokumentRepositoryMOCK.save(result)).thenReturn(result);

    Optional<KulturObjektDokument> kod =
        kulturObjektDokumentServiceMOCK.digitalisatHinzufuegen(
            kulturObjektDokumentViewModel.getId(), digitalisatViewModel);

    assertTrue(kod.isPresent());

    assertEquals(1, result.getDigitalisate().size());

    verify(distributedSaveCommandMOCK, times(1)).execute(result,
        ActivityStreamAction.UPDATE);
  }

  @Test
  void testDigitalisateHinzufuegenBatchedSavesOnce() throws Exception {
    KulturObjektDokument result =
        new KulturObjektDokumentBuilder()
            .withId("1")
            .withTEIXml(FileUtils.readFileToString(TEI_FILE_PATH.toFile(),
                StandardCharsets.UTF_8))
            .build();

    DigitalisatViewModel erstes = DigitalisatViewModel.builder()
        .manifestURL("https://iiif.ub.uni-leipzig.de/0000009089/manifest.json")
        .digitalisatTypKonzepte(new ArrayList<>())
        .build();
    DigitalisatViewModel zweites = DigitalisatViewModel.builder()
        .manifestURL("https://iiif.ub.uni-leipzig.de/0000007344/manifest.json")
        .digitalisatTypKonzepte(new ArrayList<>())
        .build();

    when(kulturObjektDokumentRepositoryMOCK.findByIdOptional("1"))
        .thenReturn(Optional.of(result));
    when(kulturObjektDokumentRepositoryMOCK.save(result)).thenReturn(result);

    Optional<KulturObjektDokument> kod =
        kulturObjektDokumentServiceMOCK.digitalisateHinzufuegen("1", List.of(erstes, zweites));

    assertTrue(kod.isPresent());
    // both digitalisate added to the KOD
    assertEquals(2, result.getDigitalisate().size());
    // but only ONE distributed save (validate + Solr + Kafka) for the whole KOD
    verify(distributedSaveCommandMOCK, times(1)).execute(result, ActivityStreamAction.UPDATE);
  }

  @Test
  @TestTransaction
  void testDigitalisatHinzufuegenReal() throws Exception {
    KulturObjektDokumentService service =
        new KulturObjektDokumentService(
            kulturObjektDokumentRepository,
            identifikationRepositoryMOCK,
            suchDokumentServiceMOCK,
            beschreibungService,
            dokumentSperreServiceMOCK,
            kafkaIndexingProducerMOCK,
            beschreibungsRepository,
            purlServiceMOCK,
            papierkorbServiceMOCK,
            false,
            distributedSaveCommandMOCK, bearbeiterBoundary,
            werkzeugOverrideAttributsReferenzen,
            teiXmlValidationBoundaryMOCK,
            sortkeyUpdateImportParserMock,
            sortkeyBatchServiceMOCK,
            kodRegistriertEventPublisherMOCK);

    KulturObjektDokument result =
        new KulturObjektDokumentBuilder()
            .withId("1")
            .withTEIXml(FileUtils.readFileToString(TEI_FILE_PATH.toFile(),
                StandardCharsets.UTF_8))
            .build();

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel =
        KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
            .withId("1")
            .withTeiXML(FileUtils.readFileToString(TEI_FILE_PATH.toFile(),
                StandardCharsets.UTF_8))
            .build();

    kulturObjektDokumentRepository.save(result);

    NormdatenReferenz modelKoerperschaft =
        new NormdatenReferenz(
            "6790851b-9519-3874-a9fd-0839d494a3c9",
            "Staatsbibliothek zu Berlin",
            "5036103-X",
            GNDEntityFact.CORPORATE_BODY_TYPE_NAME);
    normdatenReferenzRepository.saveAndFlush(modelKoerperschaft);

    NormdatenReferenz modelOrt =
        new NormdatenReferenz(
            "ee1611b6-1f56-38e7-8c12-b40684dbb395",
            "Berlin",
            "4005728-8",
            GNDEntityFact.PLACE_TYPE_NAME);
    normdatenReferenzRepository.saveAndFlush(modelOrt);

    NormdatenReferenz completeKonzept =
        new NormdatenReferenz.NormdatenReferenzBuilder()
            .withUri(
                "http://normdaten.staatsbibliothek-berlin.de/vocabulary/digitalizationType/color")
            .withName("color")
            .withId("1123123123")
            .build();

    DigitalisatViewModel digitalisatViewModel =
        DigitalisatViewModel.builder()
            .id("digiID")
            .manifestURL(
                "https://iiif.ub.uni-leipzig.de/0000009089/manifest.json")
            .digitalisierungsdatum(LocalDate.now())
            .thumbnail(
                "https://iiif.ub.uni-leipzig.de/fcgi-bin/iipsrv.fcgi?iiif=/j2k/0000/0090/0000009089/00000057.jpx/full/500,/0/default.jpg")
            .ort(modelOrt)
            .einrichtung(modelKoerperschaft)
            .digitalisatTypKonzepte(List.of(completeKonzept))
            .build();

    Optional<KulturObjektDokument> kod =
        service.digitalisatHinzufuegen(kulturObjektDokumentViewModel.getId(),
            digitalisatViewModel);

    assertTrue(kod.isPresent());

    assertEquals(1, kod.get().getDigitalisate().size());
  }

  @Test
  void testDigitalisatLoeschen() throws Exception {

    KulturObjektDokument result =
        new KulturObjektDokumentBuilder()
            .withId("1")
            .addDigitalisat(
                Digitalisat.DigitalisatBuilder().withID("1").build())
            .withTEIXml(FileUtils.readFileToString(TEI_FILE_PATH.toFile(),
                StandardCharsets.UTF_8))
            .build();

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel =
        KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
            .withId("1")
            .withTeiXML(FileUtils.readFileToString(TEI_FILE_PATH.toFile(),
                StandardCharsets.UTF_8))
            .build();

    DigitalisatViewModel digitalisatViewModel =
        DigitalisatViewModel.builder()
            .id("1")
            .manifestURL(
                "https://iiif.ub.uni-leipzig.de/0000009089/manifest.json")
            .build();

    when(kulturObjektDokumentRepositoryMOCK.findByIdOptional("1"))
        .thenReturn(Optional.of(result));
    when(kulturObjektDokumentRepositoryMOCK.save(result)).thenReturn(result);

    assertEquals(1, result.getDigitalisate().size());

    Optional<KulturObjektDokument> kod =
        kulturObjektDokumentServiceMOCK.digitalisatLoeschen(
            kulturObjektDokumentViewModel.getId(), digitalisatViewModel);

    assertTrue(kod.isPresent());

    assertEquals(0, result.getDigitalisate().size());
  }

  @Test
  void testBuildKulturObjektDokumentViewModel() throws IOException {
    NormdatenReferenz besitzer = new NormdatenReferenz("",
        "Staatsbibliothek zu Berlin", "");
    NormdatenReferenz aufbewahrungsOrt = new NormdatenReferenz("", "Berlin",
        "");

    Identifikation identifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdent("Mscr.Dresd.A.111")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withAufbewahrungsOrt(aufbewahrungsOrt)
            .withBesitzer(besitzer)
            .build();

    Identifikation altIdentifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdent("Mscr. Dresd. A. 111")
            .withIdentTyp(IdentTyp.ALTERNATIVE_SCHREIBUNG)
            .withAufbewahrungsOrt(aufbewahrungsOrt)
            .withBesitzer(besitzer)
            .build();

    PURL purlInternal =
        new PURL(
            URI.create("https://resolver.de/40170459"),
            URI.create("https://target.de/40170459"),
            PURLTyp.INTERNAL);
    PURL purlExternal =
        new PURL(
            URI.create("https://doi.org/40170459"),
            URI.create("https://doi.org/40170459"),
            PURLTyp.EXTERNAL);

    Path teixml = Paths.get("src", "test", "resources",
        "kod-attributsreferenzen.xml");

    KulturObjektDokument kod =
        new KulturObjektDokumentBuilder()
            .withId("1234")
            .withGndIdentifier("gndid1231234x")
            .withGueltigerIdentifikation(identifikation)
            .addAlternativeIdentifikation(altIdentifikation)
            .addPURL(purlInternal)
            .addPURL(purlExternal)
            .withTEIXml(Files.readString(teixml))
            .withAttributsReferenzenTyp(AttributsReferenzStatusTyp.MANUELL)
            .build();

    Publikation publikation = new Publikation.PublikationBuilder()
        .withErstpublikationsDatum(LocalDateTime.of(1982, Month.JANUARY, 2, 0, 0))
        .withPublikationsDatumHSP(LocalDateTime.of(1983, Month.MARCH, 4, 0, 0))
        .build();

    BeschreibungsKomponenteKopf kopf =
        new BeschreibungsKomponenteKopfBuilder()
            .withTitel("Flavius Josephus, de bello Judaico")
            .build();
    Beschreibung beschreibung =
        new BeschreibungsBuilder("1", kod.getId(), identifikation)
            .withPublikation(publikation)
            .build();
    beschreibung.getBeschreibungsStruktur().add(kopf);

    kod.addBeschreibungsdokument("1");

    BeschreibungsViewModel beschreibungsViewModel =
        new BeschreibungsViewBuilder()
            .withKODView("ewr")
            .withTitel("Flavius Josephus, de bello Judaico")
            .withSignatur("Mscr.Dresd.A.111")
            .withPublikation(PublikationViewModel.fromEntity(beschreibung.getPublikation()))
            .build();

    when(kulturObjektDokumentRepositoryMOCK.findByIdOptional("1")).thenReturn(
        Optional.of(kod));
    when(beschreibungsRepositoryMOCK.findByIdOptional("1")).thenReturn(
        Optional.of(beschreibung));
    when(beschreibungServiceMOCK.buildBeschreibungsViewModel("1"))
        .thenReturn(Optional.of(beschreibungsViewModel));

    Optional<KulturObjektDokumentViewModel> kulturObjektDokumentViewModelOptional =
        kulturObjektDokumentServiceMOCK.buildKulturObjektDokumentViewModel("1");

    assertTrue(kulturObjektDokumentViewModelOptional.isPresent());
    KulturObjektDokumentViewModel kulturObjektDokumentViewModel =
        kulturObjektDokumentViewModelOptional.get();
    assertEquals(
        "Flavius Josephus, de bello Judaico",
        kulturObjektDokumentViewModel.getBeschreibungen().getFirst()
            .getTitel());
    assertEquals("Mscr.Dresd.A.111",
        kulturObjektDokumentViewModel.getSignatur());
    PublikationViewModel publikationFromView = kulturObjektDokumentViewModel.getBeschreibungen()
        .getFirst()
        .getPublikation();
    assertNotNull(publikationFromView);
    assertEquals(LocalDateTime.of(1982, Month.JANUARY, 2, 0, 0),
        publikationFromView.erstpublikationsDatum());
    assertEquals(LocalDateTime.of(1983, Month.MARCH, 4, 0, 0),
        publikationFromView.publikationsDatumHSP());

    assertNotNull(kulturObjektDokumentViewModel.getAlternativeSignaturen());
    assertEquals(1,
        kulturObjektDokumentViewModel.getAlternativeSignaturen().size());
    kulturObjektDokumentViewModel.getAlternativeSignaturen().stream()
        .findFirst()
        .ifPresent(i -> assertEquals("Mscr. Dresd. A. 111", i.signatur()));

    assertEquals("https://resolver.de/40170459",
        kulturObjektDokumentViewModel.getHspPurl());
    assertNotNull(kulturObjektDokumentViewModel.getExterneLinks());
    assertEquals(1, kulturObjektDokumentViewModel.getExterneLinks().size());
    assertEquals(
        "https://doi.org/40170459",
        kulturObjektDokumentViewModel.getExterneLinks().getFirst());

    Set<AttributsReferenz> attributsReferenzen =
        kulturObjektDokumentServiceMOCK.getAttributsReferenzenFromKODViewModel(
            kulturObjektDokumentViewModel);

    assertEquals(AttributsTyp.values().length, attributsReferenzen.size());

    attributsReferenzen.forEach(
        attributsReferenz ->
            assertEquals(
                "HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001",
                attributsReferenz.getReferenzId()));
    assertEquals(AttributsReferenzStatusTyp.MANUELL,
        kulturObjektDokumentViewModel.getAttributsReferenzStatusTyp());
  }

  @Test
  void testBuildKulturObjektDokumentViewModelWithEmptyBeschreibungsModel() {
    NormdatenReferenz besitzer = new NormdatenReferenz("",
        "Staatsbibliothek zu Berlin", "");

    Identifikation identifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdent("Mscr.Dresd.A.111")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(besitzer)
            .build();

    KulturObjektDokument kod =
        new KulturObjektDokumentBuilder()
            .withId("1234")
            .withGndIdentifier("gndid1231234x")
            .withGueltigerIdentifikation(identifikation)
            .build();

    Publikation publikation = new Publikation.PublikationBuilder()
        .withErstpublikationsDatum(LocalDateTime.of(1982, Month.JANUARY, 2, 0, 0))
        .withPublikationsDatumHSP(LocalDateTime.of(1983, Month.MARCH, 4, 0, 0))
        .build();

    BeschreibungsKomponenteKopf kopf =
        new BeschreibungsKomponenteKopfBuilder()
            .withTitel("Flavius Josephus, de bello Judaico")
            .build();
    Beschreibung beschreibung =
        new BeschreibungsBuilder("1", kod.getId(), identifikation)
            .withPublikation(publikation)
            .build();
    beschreibung.getBeschreibungsStruktur().add(kopf);

    kod.addBeschreibungsdokument("1");

    when(kulturObjektDokumentRepositoryMOCK.findByIdOptional("1")).thenReturn(
        Optional.of(kod));
    when(beschreibungsRepositoryMOCK.findByIdOptional("1")).thenReturn(
        Optional.of(beschreibung));
    when(beschreibungServiceMOCK.buildBeschreibungsViewModel("1")).thenReturn(
        Optional.empty());

    Optional<KulturObjektDokumentViewModel> kulturObjektDokumentViewModel =
        kulturObjektDokumentServiceMOCK.buildKulturObjektDokumentViewModel("1");

    assertTrue(kulturObjektDokumentViewModel.isPresent());
    KulturObjektDokumentViewModel kulturObjektDokumentViewModel1 =
        kulturObjektDokumentViewModel.get();
    assertTrue(kulturObjektDokumentViewModel1.getBeschreibungen().isEmpty());
    assertEquals("Mscr.Dresd.A.111",
        kulturObjektDokumentViewModel1.getSignatur());
  }

  @Test
  @TestTransaction
  void testCount() {
    KulturObjektDokumentService service =
        new KulturObjektDokumentService(
            kulturObjektDokumentRepository,
            identifikationRepositoryMOCK,
            suchDokumentServiceMOCK,
            beschreibungServiceMOCK,
            dokumentSperreServiceMOCK,
            kafkaIndexingProducerMOCK,
            beschreibungsRepositoryMOCK,
            purlServiceMOCK,
            papierkorbServiceMOCK,
            false,
            distributedSaveCommandMOCK, bearbeiterBoundary,
            werkzeugOverrideAttributsReferenzen,
            teiXmlValidationBoundaryMOCK,
            sortkeyUpdateImportParserMock,
            sortkeyBatchServiceMOCK,
            kodRegistriertEventPublisherMOCK);

    long initialCount = service.count();
    KulturObjektDokument kod =
        new KulturObjektDokumentBuilder().withId("1234")
            .withGndIdentifier("gndid1231234x").build();
    kulturObjektDokumentRepository.save(kod);
    assertEquals(initialCount + 1L, service.count());
    kulturObjektDokumentRepository.deleteByIdAndFlush(kod.getId());
  }

  @Test
  @TestTransaction
  void testDelete() throws Exception {
    KulturObjektDokumentService service =
        new KulturObjektDokumentService(
            kulturObjektDokumentRepository,
            identifikationRepositoryMOCK,
            suchDokumentServiceMOCK,
            beschreibungServiceMOCK,
            dokumentSperreServiceMOCK,
            kafkaIndexingProducerMOCK,
            beschreibungsRepository,
            purlServiceMOCK,
            papierkorbServiceMOCK,
            false,
            distributedSaveCommandMOCK, bearbeiterBoundary,
            werkzeugOverrideAttributsReferenzen,
            teiXmlValidationBoundaryMOCK,
            sortkeyUpdateImportParserMock,
            sortkeyBatchServiceMOCK,
            kodRegistriertEventPublisherMOCK);

    KulturObjektDokument kod = createKulturObjektDokument();
    kod.setId("K-1");
    Beschreibung beschreibung = createBeschreibung(kod);

    Bearbeiter bearbeiter = new Bearbeiter("1", "Unbekannter Tester");

    kulturObjektDokumentRepository.save(kod);

    assertEquals(1L, kulturObjektDokumentRepository.count());

    beschreibungsRepository.save(beschreibung);
    assertEquals(1L, beschreibungsRepository.count());

    service.delete(bearbeiter, kod.getId());

    assertEquals(0L, kulturObjektDokumentRepository.count());
    assertEquals(0L, beschreibungsRepository.count());
    verify(papierkorbServiceMOCK, times(1)).erzeugeGeloeschtesDokument(kod,
        bearbeiter);
    verify(papierkorbServiceMOCK, times(1)).erzeugeGeloeschtesDokument(
        beschreibung, bearbeiter);
  }

  @Test
  @TestTransaction
  void testSperrenForKulturObjektDokument() throws Exception {
    KulturObjektDokument kod = createKulturObjektDokument();
    kod.setId(TEIValues.UUID_PREFIX + UUID.randomUUID());

    kulturObjektDokumentRepository.save(kod);

    KulturObjektDokumentService service =
        new KulturObjektDokumentService(
            kulturObjektDokumentRepository,
            identifikationRepository,
            suchDokumentServiceMOCK,
            beschreibungServiceMOCK,
            dokumentSperreService,
            kafkaIndexingProducerMOCK,
            beschreibungsRepositoryMOCK,
            purlServiceMOCK,
            papierkorbServiceMOCK,
            false,
            distributedSaveCommandMOCK, bearbeiterBoundary,
            werkzeugOverrideAttributsReferenzen,
            teiXmlValidationBoundaryMOCK,
            sortkeyUpdateImportParserMock,
            sortkeyBatchServiceMOCK,
            kodRegistriertEventPublisherMOCK);

    Sperre sperre =
        service.kulturObjektDokumentSperren(
            BEARBEITER, "KulturObjektDokumentBearbeiten", kod.getId());

    assertNotNull(sperre);
    assertNotNull(sperre.getId());
    assertNotNull(sperre.getId());
    assertEquals(BEARBEITER, sperre.getBearbeiter());
    assertEquals(SperreTyp.MANUAL, sperre.getSperreTyp());
    assertEquals("KulturObjektDokumentBearbeiten", sperre.getSperreGrund());
    assertNotNull(sperre.getSperreEintraege());
    assertTrue(
        sperre.getSperreEintraege().stream()
            .anyMatch(
                se ->
                    kod.getId().equals(se.getTargetId())
                        && SperreDokumentTyp.KOD == se.getTargetType()));

    Bearbeiter andererBearbeiter = new Bearbeiter("2", "Unbekannter Bearbeiter 2");
    String kodId = kod.getId();

    SperreAlreadyExistException sperreAlreadyExistException =
        assertThrows(
            SperreAlreadyExistException.class,
            () -> service.kulturObjektDokumentSperren(
                andererBearbeiter, "DigitalisatHinzufügen", kodId));

    assertNotNull(sperreAlreadyExistException.getSperren());
    assertTrue(
        sperreAlreadyExistException.getSperren().stream()
            .anyMatch(sp -> sperre.getId().equals(sp.getId())));

    Optional<Sperre> optionalSperre = service.findSperreForKulturObjektDokument(
        kod.getId());
    assertNotNull(optionalSperre);
    assertTrue(optionalSperre.isPresent());
    assertEquals(sperre, optionalSperre.get());

    service.kulturObjektDokumentEntsperren(kod.getId());
    Optional<Sperre> noSperre = service.findSperreForKulturObjektDokument(
        kod.getId());
    assertNotNull(noSperre);
    assertFalse(noSperre.isPresent());
  }

  @Test
  @TestTransaction
  void testCreateNewPURL() throws Exception {
    PURL purlInternal =
        new PURL(
            URI.create("https://resolver.de/" + KOD_ID),
            URI.create("https://target.de/" + KOD_ID),
            PURLTyp.INTERNAL);

    PURL purlExternal =
        new PURL(
            URI.create("https://doi.org/" + KOD_ID),
            URI.create("https://doi.org/" + KOD_ID),
            PURLTyp.EXTERNAL);

    KulturObjektDokumentService service =
        new KulturObjektDokumentService(
            kulturObjektDokumentRepository,
            identifikationRepository,
            suchDokumentServiceMOCK,
            beschreibungServiceMOCK,
            dokumentSperreServiceMOCK,
            kafkaIndexingProducerMOCK,
            beschreibungsRepositoryMOCK,
            purlServiceMOCK,
            papierkorbServiceMOCK,
            false,
            distributedSaveCommandMOCK, bearbeiterBoundary,
            werkzeugOverrideAttributsReferenzen,
            teiXmlValidationBoundaryMOCK,
            sortkeyUpdateImportParserMock,
            sortkeyBatchServiceMOCK,
            kodRegistriertEventPublisherMOCK);

    KulturObjektDokument kod = createKulturObjektDokument();
    kod.getPURLs().add(purlExternal);
    kod.setId(KOD_ID);

    kulturObjektDokumentRepository.save(kod);

    NullPointerException npe =
        assertThrows(NullPointerException.class,
            () -> service.addPURL(null, null));
    assertEquals("kodId is required", npe.getMessage());

    String kodId = kod.getId();
    npe = assertThrows(NullPointerException.class,
        () -> service.addPURL(kodId, null));
    assertEquals("purl is required", npe.getMessage());

    KulturObjektDokumentException kodeNoKOD =
        assertThrows(
            KulturObjektDokumentException.class,
            () -> service.addPURL("INVALID", purlInternal));
    assertEquals(
        "Zur Id INVALID wurde kein KulturObjektDokument gefunden!",
        kodeNoKOD.getMessage());

    service.addPURL(kod.getId(), purlInternal);

    Optional<KulturObjektDokument> updated =
        kulturObjektDokumentRepository.findByIdOptional(kod.getId());
    assertTrue(updated.isPresent());

    assertEquals(2, updated.get().getPURLs().size());
    assertTrue(
        updated.get().getPURLs().stream()
            .anyMatch(
                purl ->
                    PURLTyp.INTERNAL == purl.getTyp()
                        && "https://resolver.de/HSP-123".equals(
                        purl.getPurl().toASCIIString())
                        && "https://target.de/HSP-123".equals(
                        purl.getTarget().toASCIIString())));
  }

  @Test
  @TestTransaction
  void testCreateAndAddInternalPURLs()
      throws IOException, PURLException {
    doAnswer(
        (Answer<PURL>)
            invocation -> {
              final Object[] args = invocation.getArguments();
              String docId = String.valueOf(args[0]);
              return new PURL(
                  "PURL-" + docId,
                  URI.create("https://resolver.url/" + docId),
                  URI.create("https://target.url/" + docId),
                  PURLTyp.INTERNAL);
            })
        .when(purlServiceMOCK)
        .createNewPURL("HSP-1234", DokumentObjektTyp.HSP_OBJECT);

    doThrow(new PURLException("TestError"))
        .when(purlServiceMOCK)
        .createNewPURL("HSP-5678", DokumentObjektTyp.HSP_OBJECT);

    KulturObjektDokument kod1 = createKulturObjektDokument();
    kod1.setId("HSP-1234");

    KulturObjektDokument kod2 = createKulturObjektDokument();
    kod2.setId("HSP-5678");

    KulturObjektDokumentService service =
        new KulturObjektDokumentService(
            kulturObjektDokumentRepository,
            identifikationRepository,
            suchDokumentServiceMOCK,
            beschreibungServiceMOCK,
            dokumentSperreServiceMOCK,
            kafkaIndexingProducerMOCK,
            beschreibungsRepositoryMOCK,
            purlServiceMOCK,
            papierkorbServiceMOCK,
            true,
            distributedSaveCommandMOCK, bearbeiterBoundary,
            werkzeugOverrideAttributsReferenzen,
            teiXmlValidationBoundaryMOCK,
            sortkeyUpdateImportParserMock,
            sortkeyBatchServiceMOCK,
            kodRegistriertEventPublisherMOCK);

    service.createAndAddInternalPURLs(List.of(kod1, kod2));

    verify(purlServiceMOCK, times(2)).createNewPURL(anyString(),
        any(DokumentObjektTyp.class));

    assertNotNull(kod1.getPURLs());
    assertEquals(1, kod1.getPURLs().size());
    assertTrue(
        kod1.getPURLs().stream()
            .anyMatch(purl -> PURLTyp.INTERNAL == purl.getTyp()),
        "PURL not found in KOD");

    assertNotNull(kod2.getPURLs());
    assertEquals(0, kod2.getPURLs().size());
  }

  @Test
  @TestTransaction
  void testCreateAndAddInternalPURLs_disabled()
      throws IOException, PURLException {
    KulturObjektDokument kod1 = createKulturObjektDokument();
    kod1.setId("HSP-1234");

    KulturObjektDokumentService service =
        new KulturObjektDokumentService(
            kulturObjektDokumentRepository,
            identifikationRepository,
            suchDokumentServiceMOCK,
            beschreibungServiceMOCK,
            dokumentSperreServiceMOCK,
            kafkaIndexingProducerMOCK,
            beschreibungsRepositoryMOCK,
            purlServiceMOCK,
            papierkorbServiceMOCK,
            false,
            distributedSaveCommandMOCK, bearbeiterBoundary,
            werkzeugOverrideAttributsReferenzen,
            teiXmlValidationBoundaryMOCK,
            sortkeyUpdateImportParserMock,
            sortkeyBatchServiceMOCK,
            kodRegistriertEventPublisherMOCK);

    service.createAndAddInternalPURLs(List.of(kod1));
    verify(purlServiceMOCK, times(0)).createNewPURL(anyString(),
        any(DokumentObjektTyp.class));
  }

  @Test
  void testFindFilteredKulturObjektDokumentListDTOs() {

    kulturObjektDokumentServiceMOCK.findFilteredKulturObjektDokumentListDTOs(
        0, 10, new HashMap<>(), new HashMap<>());
    verify(kulturObjektDokumentRepositoryMOCK, times(1))
        .findFilteredKulturObjektDokumentListDTOs(anyInt(), anyInt(), anyMap(),
            anyMap());
  }

  @Test
  @TestTransaction
  void testFindFilteredKulturObjektDokumentListDTOsWithDatabase()
      throws IOException, URISyntaxException {

    KulturObjektDokument kod = createKulturObjektDokument();
    kod.addPURL(
        new PURL(
            new URI(
                "https://resolver.staatsbibliothek-berlin.de/HSP0003D74500000000"),
            new URI("https://handschriftenportal.de"),
            PURLTyp.EXTERNAL));

    kulturObjektDokumentRepository.saveAndFlush(kod);

    KulturObjektDokumentService service =
        new KulturObjektDokumentService(
            kulturObjektDokumentRepository,
            identifikationRepository,
            suchDokumentServiceMOCK,
            beschreibungServiceMOCK,
            dokumentSperreServiceMOCK,
            kafkaIndexingProducerMOCK,
            beschreibungsRepositoryMOCK,
            purlServiceMOCK,
            papierkorbServiceMOCK,
            false,
            distributedSaveCommandMOCK, bearbeiterBoundary,
            werkzeugOverrideAttributsReferenzen,
            teiXmlValidationBoundaryMOCK,
            sortkeyUpdateImportParserMock,
            sortkeyBatchServiceMOCK,
            kodRegistriertEventPublisherMOCK);

    List<KulturObjektDokumentListDTO> result =
        service.findFilteredKulturObjektDokumentListDTOs(0, 10, new HashMap<>(),
            new HashMap<>());

    assertNotNull(result);
    assertEquals(1, result.size());

    result.stream()
        .findFirst()
        .ifPresentOrElse(
            dto ->
                assertEquals(
                    "https://resolver.staatsbibliothek-berlin.de/HSP0003D74500000000",
                    dto.getPurl()),
            Assertions::fail);
  }

  @Test
  void testCountFilteredKulturObjektDokumentListDTOs() {

    kulturObjektDokumentServiceMOCK.countFilteredKulturObjektDokumentListDTOs(
        new HashMap<>());
    verify(kulturObjektDokumentRepositoryMOCK, times(1))
        .countFilteredKulturObjektDokumentListDTOs(anyMap());
  }

  @Test
  void testFindKulturObjektDokumentListDTOById() {

    kulturObjektDokumentServiceMOCK.findKulturObjektDokumentListDTOById("id");
    verify(kulturObjektDokumentRepositoryMOCK,
        times(1)).findKulturObjektDokumentListDTOById("id");
  }

  @Test
  void testKulturObjektDokumentAktualisierenFromViewModel() throws IOException {
    KulturObjektDokument kod =
        new KulturObjektDokumentBuilder()
            .withId("1")
            .withTEIXml(FileUtils.readFileToString(
                Paths.get("src", "test", "resources", "kod_goerlitz.xml")
                    .toFile(), StandardCharsets.UTF_8))
            .build();

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel =
        KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
            .withId("1")
            .withTitel(null)
            .withBeschreibstoff(Beschreibstoff.builder()
                .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
                .withReferenzId("HSP-1bea9030-f875-3bab-9a8a-16acd2ad006e")
                .withText("Papier")
                .withReferenzen(
                    List.of(new NormdatenReferenz("NORM-1", "Papier", "4074502-8")))
                .build())
            .withUmfang(Umfang.builder()
                .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
                .withReferenzId("HSP-1bea9030-f875-3bab-9a8a-16acd2ad006e")
                .withText("I + 394 Bl.")
                .withBlattzahl("394")
                .build())
            .withAbmessung(Abmessung.builder()
                .withReferenzId("HSP-1bea9030-f875-3bab-9a8a-16acd2ad006e")
                .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
                .build())
            .withFormat(Format.builder()
                .withReferenzId("HSP-1bea9030-f875-3bab-9a8a-16acd2ad006e")
                .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
                .addFormatWert(FormatWert.builder()
                    .withReferenz(
                        new NormdatenReferenz("NORM-1", "long and narrow", "fdc091ab3ab2"))
                    .withTyp("deduced")
                    .build()
                )
                .build())
            .withEntstehungsort(Entstehungsort.builder()
                .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
                .withReferenzId("HSP-1bea9030-f875-3bab-9a8a-16acd2ad006e")
                .withText("Weißenburg")
                .build())
            .withEntstehungszeit(Entstehungszeit.builder()
                .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
                .withReferenzId("HSP-1bea9030-f875-3bab-9a8a-16acd2ad006e")
                .addEntstehungszeitWert(
                    EntstehungszeitWert.builder()
                        .withText("1446 oder vorher (Fasz. I)")
                        .withNichtVor("1426")
                        .withNichtNach("1446")
                        .withTyp("datable")
                        .build())
                .build())
            .withGrundSprache(Grundsprache.builder()
                .withReferenzId("HSP-1bea9030-f875-3bab-9a8a-16acd2ad006e")
                .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
                .withText("Mittelhochdeutsch")
                .addReferenz(
                    new NormdatenReferenz("NORM-1", "deutsch", "4039687-3"))
                .build())
            .withBuchschmuck(Buchschmuck.builder()
                .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
                .withReferenzId("HSP-1bea9030-f875-3bab-9a8a-16acd2ad006e")
                .withText("yes")
                .build())
            .withHandschriftentyp(Handschriftentyp.builder()
                .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
                .withReferenzId("HSP-1bea9030-f875-3bab-9a8a-16acd2ad006e")
                .withReferenzen(List.of(
                    new NormdatenReferenz.NormdatenReferenzBuilder()
                        .withId("NORM-8edc413b-0b70-3366-b78c-fdc091ab3ab2")
                        .withUri(
                            "https://normdaten.staatsbibliothek-berlin.de/hsp/vocabulary/FORM-X709")
                        .withName("composite")
                        .build(),
                    new NormdatenReferenz.NormdatenReferenzBuilder()
                        .withId("NORM-fcdc9965-677e-3c69-9a70-0c266893e376")
                        .withUri(
                            "https://normdaten.staatsbibliothek-berlin.de/hsp/vocabulary/FORM-A890")
                        .build(),
                    new NormdatenReferenz.NormdatenReferenzBuilder()
                        .withId("NORM-5dcac9ee-2a31-3245-bddf-6e1f4c22478e")
                        .withUri(
                            "https://normdaten.staatsbibliothek-berlin.de/hsp/vocabulary/FORM-E350")
                        .build()
                ))
                .build())
            .withMusiknotation(Musiknotation.builder()
                .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
                .withReferenzId("HSP-1bea9030-f875-3bab-9a8a-16acd2ad006e")
                .withText("yes")
                .build())
            .withStatus(Status.builder()
                .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
                .withReferenzId("HSP-1bea9030-f875-3bab-9a8a-16acd2ad006e")
                .withText("displaced")
                .build()).build();

    when(kulturObjektDokumentRepositoryMOCK.findByIdOptional("1"))
        .thenReturn(Optional.of(kod));
    when(kulturObjektDokumentRepositoryMOCK.save(kod)).thenReturn(kod);

    KulturObjektDokument kodWithUpdatedReferenzes =
        kulturObjektDokumentServiceMOCK.kulturObjektDokumentAttributsreferenzenAktualisieren(
            kulturObjektDokumentViewModel);

    assertNotNull(kodWithUpdatedReferenzes);

    assertTrue(kodWithUpdatedReferenzes.getTeiXML().contains("<index "
        + "indexName=\"norm_title\" change=\"#"
        + kodWithUpdatedReferenzes.getAttributsReferenzen()
        .get(AttributsTyp.TITEL).getChangeID()
        + "\"><term type=\"title\"/></index>"));

    assertEquals(AttributsReferenzStatusTyp.MANUELL,
        kodWithUpdatedReferenzes.getAttributsReferenzStatusTyp());
  }

  @Test
  void testKulturObjektDokumentAktualisieren() throws Exception {

    KulturObjektDokument result =
        new KulturObjektDokumentBuilder()
            .withId("1")
            .withTEIXml(FileUtils.readFileToString(TEI_FILE_PATH.toFile(),
                StandardCharsets.UTF_8))
            .build();

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel =
        KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
            .withId("1")
            .withTitel(createTitel())
            .withBeschreibstoff(createBeschreibstoff())
            .withUmfang(createUmfang())
            .withAbmessung(createAbmessung())
            .withFormat(createFormat())
            .withEntstehungsort(createEntstehungsort())
            .withEntstehungszeit(createEntstehungszeit())
            .withGrundSprache(createGrundSprache())
            .withBuchschmuck(createBuchschmuck())
            .withHandschriftentyp(createHandschriftentyp())
            .withMusiknotation(createMusiknotation())
            .withStatus(createStatus())
            .withTeiXML(FileUtils.readFileToString(TEI_FILE_PATH.toFile(),
                StandardCharsets.UTF_8))
            .build();

    when(kulturObjektDokumentRepositoryMOCK.findByIdOptional("1"))
        .thenReturn(Optional.of(result));
    when(kulturObjektDokumentRepositoryMOCK.save(result)).thenReturn(result);

    KulturObjektDokument kod =
        kulturObjektDokumentServiceMOCK.kulturObjektDokumentAttributsreferenzenAktualisieren(
            kulturObjektDokumentViewModel);

    assertNotNull(kod);

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {
      assertFalse(
          kod.getTeiXML().contains(
              "<index indexName=\"" + attributsTyp.getIndexName() + "\">"),
          attributsTyp.name());
      assertTrue(
          kod.getTeiXML().contains("<index indexName=\"norm_material\""),
          attributsTyp.name());
    }

    kulturObjektDokumentViewModel.setTitel(null);
    kulturObjektDokumentViewModel.setBeschreibstoff(null);
    kulturObjektDokumentViewModel.setUmfang(null);
    kulturObjektDokumentViewModel.setAbmessung(null);
    kulturObjektDokumentViewModel.setFormat(null);
    kulturObjektDokumentViewModel.setEntstehungszeit(null);
    kulturObjektDokumentViewModel.setEntstehungsort(null);
    kulturObjektDokumentViewModel.setGrundsprache(null);
    kulturObjektDokumentViewModel.setBuchschmuck(null);
    kulturObjektDokumentViewModel.setHandschriftentyp(null);
    kulturObjektDokumentViewModel.setMusiknotation(null);
    kulturObjektDokumentViewModel.setStatus(null);

    kod =
        kulturObjektDokumentServiceMOCK.kulturObjektDokumentAttributsreferenzenAktualisieren(
            kulturObjektDokumentViewModel);

    assertNotNull(kod);

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {
      assertTrue(
          kod.getTeiXML()
              .contains("<index indexName=\"" + attributsTyp.getIndexName()),
          kod.getTeiXML() + attributsTyp.getIndexName());
    }

    verify(distributedSaveCommandMOCK, times(2)).execute(result,
        ActivityStreamAction.UPDATE);
  }

  @Test
  void testsaveKulturObjektDokument() throws Exception {
    KulturObjektDokument kod =
        new KulturObjektDokumentBuilder()
            .withId("1")
            .withTEIXml(FileUtils.readFileToString(TEI_FILE_PATH.toFile(),
                StandardCharsets.UTF_8))
            .build();

    kulturObjektDokumentServiceMOCK.saveKulturObjektDokument(kod,
        ActivityStreamAction.ADD);

    verify(distributedSaveCommandMOCK, times(1)).execute(kod,
        ActivityStreamAction.ADD);
  }

  @Test
  void testGetAttributsReferenzenFromKODViewModel() {

    Set<AttributsReferenz> result =
        kulturObjektDokumentServiceMOCK.getAttributsReferenzenFromKODViewModel(
            null);
    assertNotNull(result);
    assertTrue(result.isEmpty());

    KulturObjektDokumentViewModel kodViewModel =
        KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
            .build();
    result = kulturObjektDokumentServiceMOCK.getAttributsReferenzenFromKODViewModel(
        kodViewModel);
    assertNotNull(result);
    assertTrue(result.isEmpty());

    Titel titel = createTitel();
    Beschreibstoff beschreibstoff = createBeschreibstoff();
    Umfang umfang = createUmfang();
    Abmessung abmessung = createAbmessung();
    Format format = createFormat();
    Entstehungsort entstehungsort = createEntstehungsort();
    Entstehungszeit entstehungszeit = createEntstehungszeit();
    Grundsprache grundsprache = createGrundSprache();
    Buchschmuck buchschmuck = createBuchschmuck();
    Handschriftentyp handschriftentyp = createHandschriftentyp();
    Musiknotation musiknotation = createMusiknotation();
    Status status = createStatus();

    kodViewModel =
        KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
            .withTitel(titel)
            .withBeschreibstoff(beschreibstoff)
            .withUmfang(umfang)
            .withAbmessung(abmessung)
            .withFormat(format)
            .withEntstehungsort(entstehungsort)
            .withEntstehungszeit(entstehungszeit)
            .withGrundSprache(grundsprache)
            .withBuchschmuck(buchschmuck)
            .withHandschriftentyp(handschriftentyp)
            .withMusiknotation(musiknotation)
            .withStatus(status)
            .build();

    result = kulturObjektDokumentServiceMOCK.getAttributsReferenzenFromKODViewModel(
        kodViewModel);
    assertNotNull(result);
    assertEquals(AttributsTyp.values().length, result.size());

    assertTrue(result.contains(titel));
    assertTrue(result.contains(beschreibstoff));
    assertTrue(result.contains(umfang));
    assertTrue(result.contains(abmessung));
    assertTrue(result.contains(format));
    assertTrue(result.contains(entstehungsort));
    assertTrue(result.contains(entstehungszeit));
    assertTrue(result.contains(grundsprache));
    assertTrue(result.contains(buchschmuck));
    assertTrue(result.contains(handschriftentyp));
    assertTrue(result.contains(musiknotation));
    assertTrue(result.contains(status));
  }

  @Test
  void testAlternativeSignaturHinzufuegen() throws Exception {

    assertThrows(
        IllegalArgumentException.class,
        () -> kulturObjektDokumentServiceMOCK.alternativeSignaturHinzufuegen(
            null, "123", null),
        "IllegalArgumentException erwartet");

    Identifikation gueltigeIdentifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdent("Clm 14464")
            .withBesitzer(new NormdatenReferenz("1", "SBB Berlin", "GND123"))
            .withAufbewahrungsOrt(
                new NormdatenReferenz("2", "Berlin", "GND456"))
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();

    KulturObjektDokument kod =
        new KulturObjektDokumentBuilder()
            .withId("123")
            .withTEIXml(Files.readString(TEI_KOD_MSDESC_BESCHREIBUNG_PATH))
            .withGueltigerIdentifikation(gueltigeIdentifikation)
            .build();

    when(kulturObjektDokumentRepositoryMOCK.findById("123")).thenReturn(kod);
    final Sperre sperre =
        new SperreBuilder().withBearbeiter(BEARBEITER)
            .withSperreTyp(SperreTyp.MANUAL).build();

    Optional<KulturObjektDokument> result =
        kulturObjektDokumentServiceMOCK.alternativeSignaturHinzufuegen(
            "NeueAlternativeSignature", "123", sperre);

    assertTrue(result.isPresent());

    assertEquals(1, result.get().getAlternativeIdentifikationen().size());

    verify(dokumentSperreServiceMOCK, times(1)).releaseSperre(
        any(Sperre.class));
  }

  @Test
  void testgueltigeSignaturVeraendernMock() throws Exception {

    Identifikation gueltigeIdentifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdent("Clm 14464")
            .withBesitzer(new NormdatenReferenz("1", "SBB Berlin", "GND123"))
            .withAufbewahrungsOrt(
                new NormdatenReferenz("2", "Berlin", "GND456"))
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();

    KulturObjektDokument kod =
        new KulturObjektDokumentBuilder()
            .withId("123")
            .withTEIXml(Files.readString(TEI_KOD_MSDESC_BESCHREIBUNG_PATH))
            .withGueltigerIdentifikation(gueltigeIdentifikation)
            .build();

    // Testfall 1: Gültige Signatur und ID
    String neueGueltigeSignatur = "neueSignatur";
    String kodID = "123";
    when(kulturObjektDokumentRepositoryMOCK.findById("123")).thenReturn(kod);
    final Sperre sperre =
        new SperreBuilder().withBearbeiter(BEARBEITER)
            .withSperreTyp(SperreTyp.MANUAL).build();
    when(dokumentSperreServiceMOCK.acquireSperre(
        BEARBEITER, SperreTyp.MANUAL, "Signature changed", kod))
        .thenReturn(sperre);
    Optional<KulturObjektDokument> result =
        kulturObjektDokumentServiceMOCK.gueltigeSignaturVeraendern(
            neueGueltigeSignatur, kodID, sperre);
    assertTrue(result.isPresent(), "Optional sollte nicht leer sein");

    // Testfall 2: Neue Gueltige Signatur ist null
    assertThrows(
        IllegalArgumentException.class,
        () -> kulturObjektDokumentServiceMOCK.gueltigeSignaturVeraendern(null,
            "123", sperre),
        "IllegalArgumentException erwartet");

    // Testfall 3: Neue Gültige Signatur ist leer
    assertThrows(
        IllegalArgumentException.class,
        () -> kulturObjektDokumentServiceMOCK.gueltigeSignaturVeraendern("", "",
            sperre),
        "IllegalArgumentException erwartet");

    // Testfall 4: Kod ID ist null
    assertThrows(
        IllegalArgumentException.class,
        () -> kulturObjektDokumentServiceMOCK.gueltigeSignaturVeraendern("Ms",
            null, sperre),
        "IllegalArgumentException erwartet");

    // Testfall 5: Kod ID ist leer
    assertThrows(
        IllegalArgumentException.class,
        () -> kulturObjektDokumentServiceMOCK.gueltigeSignaturVeraendern(
            NEUE_SIGNATUR, "", sperre),
        "IllegalArgumentException erwartet");

    assertEquals(neueGueltigeSignatur, kod.getGueltigeSignatur());

    assertEquals(1, kod.getAlternativeIdentifikationen().size());

    verify(dokumentSperreServiceMOCK, times(5)).releaseSperre(
        any(Sperre.class));
  }

  @Test
  @TestTransaction
  void testgueltigeSignaturVeraendern() throws Exception {

    Identifikation gueltigeIdentifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdent("Clm 14464")
            .withBesitzer(new NormdatenReferenz("1", "SBB Berlin", "GND123"))
            .withAufbewahrungsOrt(
                new NormdatenReferenz("2", "Berlin", "GND456"))
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();

    String kodID = "123";
    KulturObjektDokument kod =
        new KulturObjektDokumentBuilder()
            .withId(kodID)
            .withTEIXml(Files.readString(TEI_KOD_MSDESC_BESCHREIBUNG_PATH))
            .withGueltigerIdentifikation(gueltigeIdentifikation)
            .build();
    kulturObjektDokumentRepository.save(kod);

    String neueGueltigeSignatur = "neueSignatur";
    final Sperre sperre =
        new SperreBuilder().withBearbeiter(BEARBEITER)
            .withSperreTyp(SperreTyp.MANUAL).build();
    when(dokumentSperreServiceMOCK.acquireSperre(
        BEARBEITER, SperreTyp.MANUAL, "Signature changed", kod))
        .thenReturn(sperre);
    Optional<KulturObjektDokument> result =
        kulturObjektDokumentService.gueltigeSignaturVeraendern(
            neueGueltigeSignatur, kodID, sperre);
    assertTrue(result.isPresent(), "Optional sollte nicht leer sein");

    var kod3 = kulturObjektDokumentRepository.findById(kodID);
    assertEquals(neueGueltigeSignatur, kod3.getGueltigeSignatur());

    assertEquals(1, kod3.getAlternativeIdentifikationen().size());

    kulturObjektDokumentService.gueltigeSignaturVeraendern("sig.1", kodID,
        sperre);

    var kod4 = kulturObjektDokumentRepository.findById(kodID);

    assertEquals(2, kod4.getAlternativeIdentifikationen().size());
  }

  @Test
  @TestTransaction
  void testAdjustIdentifikationForKOD() {

    Identifikation gueltigeIdentifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdent("MS1")
            .withBesitzer(new NormdatenReferenz("1", "SBB Berlin", "GND123"))
            .withAufbewahrungsOrt(
                new NormdatenReferenz("2", "Berlin", "GND456"))
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();

    String kodId = "123";
    KulturObjektDokument kod =
        new KulturObjektDokumentBuilder()
            .withId(kodId)
            .withGueltigerIdentifikation(gueltigeIdentifikation)
            .build();
    kulturObjektDokumentRepository.save(kod);

    var kod1 = kulturObjektDokumentRepository.findById(kodId);
    assertEquals(0, kod1.getAlternativeIdentifikationen().size());

    kulturObjektDokumentService.adjustIdentifikationForKOD("MS2", kod1);

    assertEquals("MS2", kod1.getGueltigeSignatur());
    assertEquals(1, kod1.getAlternativeIdentifikationen().size());

    var kod2 = kulturObjektDokumentRepository.findById(kodId);
    kulturObjektDokumentService.adjustIdentifikationForKOD("MS1", kod2);

    assertEquals("MS1", kod2.getGueltigeSignatur());
    assertEquals(1, kod2.getAlternativeIdentifikationen().size());

    var kod3 = kulturObjektDokumentRepository.findById(kodId);
    kulturObjektDokumentService.adjustIdentifikationForKOD("MS3", kod3);

    assertEquals("MS3", kod3.getGueltigeSignatur());
    assertEquals(2, kod3.getAlternativeIdentifikationen().size());
  }

  @Test
  void testcheckIfSignaturAlreadyExists() {

    Identifikation gueltigeIdentifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdent(NEUE_SIGNATUR)
            .withBesitzer(new NormdatenReferenz("1", "SBB Berlin", "GND123"))
            .withAufbewahrungsOrt(
                new NormdatenReferenz("2", "Berlin", "GND456"))
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();

    KulturObjektDokument kod =
        new KulturObjektDokumentBuilder()
            .withId("123")
            .withGueltigerIdentifikation(gueltigeIdentifikation)
            .build();

    when(kulturObjektDokumentRepositoryMOCK.findByIdentifikationIdent(
        NEUE_SIGNATUR))
        .thenReturn(Collections.singletonList(kod));

    String besitzerId = gueltigeIdentifikation.getBesitzer().getId();

    assertThrows(
        KulturObjektDokumentException.class,
        () -> kulturObjektDokumentServiceMOCK.checkIfSignaturAlreadyExists(
            NEUE_SIGNATUR, besitzerId),
        "KOD already exist with signature %s");
  }

  @Test
  void testcheckIfAlternativeSignatureAlreadyExistsForBesitzer() {

    Identifikation gueltigeIdentifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdent(NEUE_SIGNATUR)
            .withBesitzer(new NormdatenReferenz("1", "SBB Berlin", "GND123"))
            .withAufbewahrungsOrt(
                new NormdatenReferenz("2", "Berlin", "GND456"))
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();

    KulturObjektDokument kod =
        new KulturObjektDokumentBuilder()
            .withId("123")
            .withGueltigerIdentifikation(gueltigeIdentifikation)
            .build();

    when(
        kulturObjektDokumentRepositoryMOCK.findByAlternativeIdentifikationIdent(
            NEUE_SIGNATUR))
        .thenReturn(Collections.singletonList(kod));

    String besitzerId = gueltigeIdentifikation.getBesitzer().getId();

    KulturObjektDokumentException exceptionForAddAlternative =
        assertThrows(
            KulturObjektDokumentException.class,
            () -> kulturObjektDokumentServiceMOCK
                .checkIfAlternativeSignatureAlreadyExistsForBesitzer(
                    "Alternative", "Alternative", besitzerId),
            "KOD already exist with signature %s");

    assertEquals(
        "Diese alternative Signatur ist bereits die gültige Signatur dieses Kulturobjekts!",
        exceptionForAddAlternative.getMessage());
  }

  @Test
  void testcheckPreconditionAndFindKOD() {

    final Sperre sperre =
        new SperreBuilder().withBearbeiter(BEARBEITER)
            .withSperreTyp(SperreTyp.MANUAL).build();

    assertThrows(
        IllegalArgumentException.class,
        () -> kulturObjektDokumentServiceMOCK.checkPreconditionAndFindKOD(" ",
            "123", sperre),
        "Angabe der Signature ungültig!");

    assertThrows(
        IllegalArgumentException.class,
        () -> kulturObjektDokumentServiceMOCK.checkPreconditionAndFindKOD("",
            "123", sperre),
        "Angabe der Signature ungültig!");

    assertThrows(
        IllegalArgumentException.class,
        () ->
            kulturObjektDokumentServiceMOCK.checkPreconditionAndFindKOD(
                "NeueSignature", "", sperre),
        "Angabe der Signature ungültig!");

    assertThrows(
        IllegalArgumentException.class,
        () ->
            kulturObjektDokumentServiceMOCK.checkPreconditionAndFindKOD(
                "NeueSignature", "", null),
        "Angabe der Signature ungültig!");
  }

  @Test
  void testPruefeKODUmziehen() throws IOException {

    NormdatenReferenz neuerBesitzer =
        new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId("1")
            .withName("BSB München")
            .withGndID("GND1212")
            .build();

    NormdatenReferenz neuerOrt =
        new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId("2")
            .withName("München")
            .withGndID("GND1212X")
            .build();

    KulturObjektDokument kod = createKulturObjektDokument();

    List<KulturobjektDokumentUmziehenErgebnis> ergebnis =
        kulturObjektDokumentServiceMOCK.pruefeKODUmziehen(
            Map.of(kod.getId(), kod.getGueltigeSignatur()), neuerBesitzer,
            neuerOrt);

    assertNotNull(ergebnis);

    assertEquals(1, ergebnis.size());

    assertEquals(kod.getGueltigeSignatur(), ergebnis.getFirst().signatur());
    assertEquals(0, ergebnis.getFirst().existingKODIDs().size());
    assertEquals(neuerOrt, ergebnis.getFirst().neuerOrt());
    assertEquals(neuerBesitzer, ergebnis.getFirst().neuerBesitzer());
    assertNotNull(ergebnis.getFirst().executionTime());
    assertTrue(ergebnis.getFirst().isSuccessful());
  }

  @Test
  void testPruefeKODUmziehenWithErrors()
      throws IOException {

    NormdatenReferenz neuerBesitzer =
        new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId("1")
            .withName("BSB München")
            .withGndID("GND1212")
            .build();

    NormdatenReferenz neuerOrt =
        new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId("2")
            .withName("München")
            .withGndID("GND1212X")
            .build();

    KulturObjektDokument kod = createKulturObjektDokument();

    when(kulturObjektDokumentRepositoryMOCK
        .findByIdentifikationIdentAndBesitzerIDAndAufbewahrungsortID("Signatur",
            "1", "2"))
        .thenThrow(
            new IllegalCallerException("Error during searching Database"));

    List<KulturobjektDokumentUmziehenErgebnis> ergebnis =
        kulturObjektDokumentServiceMOCK.pruefeKODUmziehen(
            Map.of(kod.getId(), kod.getGueltigeSignatur()), neuerBesitzer,
            neuerOrt);

    assertNotNull(ergebnis);
    assertFalse(ergebnis.getFirst().isSuccessful());
  }

  @Test
  void testPruefeKODUmziehenWithSperre()
      throws IOException, DokumentSperreException {

    NormdatenReferenz neuerBesitzer =
        new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId("1")
            .withName("BSB München")
            .withGndID("GND1212")
            .build();

    NormdatenReferenz neuerOrt =
        new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId("2")
            .withName("München")
            .withGndID("GND1212X")
            .build();

    KulturObjektDokument kod = createKulturObjektDokument();

    when(dokumentSperreServiceMOCK.isGesperrt(any())).thenReturn(true);

    List<KulturobjektDokumentUmziehenErgebnis> ergebnis =
        kulturObjektDokumentServiceMOCK.pruefeKODUmziehen(
            Map.of(kod.getId(), kod.getGueltigeSignatur()), neuerBesitzer,
            neuerOrt);

    assertNotNull(ergebnis);
    assertFalse(ergebnis.getFirst().isSuccessful());
    assertEquals(KulturObjektDokumentService.KOD_IST_LOCKED,
        ergebnis.getFirst().errorMessage());
  }

  @Test
  void testKODUmziehenWithBadErgebnis() {

    NormdatenReferenz neuerBesitzer =
        new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId("1")
            .withName("BSB München")
            .withGndID("GND1212")
            .build();

    NormdatenReferenz neuerOrt =
        new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId("2")
            .withName("München")
            .withGndID("GND1212X")
            .build();

    Bearbeiter bearbeiter =
        new Bearbeiter.BearbeiterBuilder().withId("b-ke101")
            .withBearbeitername("Konrad").build();

    List<KulturobjektDokumentUmziehenErgebnis> umzugsErgebnisse =
        List.of(
            new KulturobjektDokumentUmziehenErgebnis(
                "1",
                "Ms. 1",
                List.of("HSP-121212"),
                neuerOrt,
                neuerBesitzer,
                LocalDateTime.now(),
                ""));

    assertThrows(
        IllegalArgumentException.class,
        () -> kulturObjektDokumentServiceMOCK.umziehen(umzugsErgebnisse, bearbeiter));
  }

  @Test
  void testumziehenHappyPath() throws Exception {

    Path teiFilePath =
        Paths.get(
            "../domainmodel-tei-mapper/src", "test", "resources", "tei",
            "tei-HSP-kodms1.xml");

    Identifikation identifikation =
        new IdentifikationBuilder()
            .withIdent("Signatur")
            .withBesitzer(new NormdatenReferenz("NR-1", "Berlin", ""))
            .withAufbewahrungsOrt(
                new NormdatenReferenz("NR-2", "Staatsbibliothek", ""))
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();

    final KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("KOD1")
            .withGueltigerIdentifikation(identifikation)
            .withTEIXml(Files.readString(teiFilePath))
            .withBeschreibungsdokumentIDs(Set.of("1234"))
            .build();

    NormdatenReferenz neuerBesitzer =
        new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId("1")
            .withName("BSB München")
            .withGndID("GND1212")
            .build();

    NormdatenReferenz neuerOrt =
        new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId("2")
            .withName("München")
            .withGndID("GND1212X")
            .build();

    when(kulturObjektDokumentRepositoryMOCK.findByIdOptional("KOD1"))
        .thenReturn(Optional.of(kulturObjektDokument));

    Bearbeiter bearbeiter =
        new Bearbeiter.BearbeiterBuilder().withId("b-ke101")
            .withBearbeitername("Konrad").build();

    kulturObjektDokumentServiceMOCK.umziehen(
        List.of(
            new KulturobjektDokumentUmziehenErgebnis(
                "KOD1", "Ms. 1", List.of(), neuerOrt, neuerBesitzer,
                LocalDateTime.now(), "")),
        bearbeiter);

    String otherXml =
        """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?><TEI xmlns="http://www.tei-c.org/ns/1.0" xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:dcr="http://www.isocat.org/ns/dcr" xmlns:egXML="http://www.tei-c.org/ns/Examples" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><teiHeader xml:lang="de"><fileDesc><titleStmt><title>Kulturobjektdokument von Leipzig, Universitätsbibliothek Leipzig, Ms 1</title><respStmt><resp>Herausgegeben vom</resp><name type="org">Handschriftenportal</name></respStmt></titleStmt><editionStmt><edition>Elektronische Ausgabe nach TEI P5</edition><respStmt><resp>Automatisch generierte Ausgabe</resp><orgName>Handschriftenportal</orgName></respStmt></editionStmt><publicationStmt><publisher>
                                <name type="org">Handschriftenportal</name>
                                <ptr target="http://www.handschriftenportal.de"/>
                    </publisher><date when="2020-10-06" type="issued">2020-10-06</date><distributor>Handschriftenportal</distributor><availability status="free"><licence target="https://creativecommons.org/publicdomain/zero/1.0/deed.de">
                                    <p>Für das Kulturobjektdokument verzichtet das Handschriftenportal auf alle
                          Nutzungsrechte.
                        </p>
                                    <p>Für die Nutzung weiterer Daten wie Digitalisaten gelten gegebenenfalls andere
                          Lizenzen. Vgl. die
                                        <ref target="http://www.handschriftenportal.de">Copyright
                            Information
                          </ref> des Handschriftenportals.
                        </p>
                      </licence></availability></publicationStmt><sourceDesc><msDesc xml:id="HSP-36a88bd1-987b-4ac9-8c67-212a43681b02" xml:lang="de" type="hsp:object">
                      <msIdentifier>
                        <settlement ref="GND1212X" key="2">München</settlement>
                        <repository ref="GND1212" key="1">BSB München</repository>
                        <idno>Signatur</idno>
                      <altIdentifier notAfter="%s" type="former">
                        <settlement key="NR-2">Staatsbibliothek</settlement>
                        <repository key="NR-1">Berlin</repository>
                        <idno>Signatur</idno></altIdentifier>
                      </msIdentifier>
                      <additional><adminInfo><availability status="free"><licence target="https://creativecommons.org/publicdomain/zero/1.0/deed.de">
                                                <p>Für das Kulturobjektdokument verzichtet das Handschriftenportal auf alle
                                Nutzungsrechte.
                              </p>
                                                <p>Für die Nutzung weiterer Daten wie Digitalisaten gelten gegebenenfalls andere
                                Lizenzen. Vgl. die
                                                    <ref target="http://www.handschriftenportal.de">Copyright
                                  Information
                                </ref> des Handschriftenportals.
                              </p>
                            </licence></availability></adminInfo><listBibl><bibl>
                                            <ref target="HSP-d3d89a5b-9621-477e-b918-8190af7ee9d4"/>
                                        </bibl></listBibl></additional></msDesc></sourceDesc></fileDesc></teiHeader><text><body><p/></body></text></TEI>""";

    Diff diffs =
        DiffBuilder.compare(kulturObjektDokument.getTeiXML())
            .withTest(
                String.format(
                    otherXml, DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        .format(LocalDate.now())))
            .ignoreComments()
            .ignoreWhitespace()
            .checkForSimilar()
            .checkForIdentical()
            .withNodeMatcher(
                new DefaultNodeMatcher(ElementSelectors.byNameAndText))
            .build();

    assertEquals(0, IterableUtils.size(diffs.getDifferences()),
        kulturObjektDokument.getTeiXML());
  }

  @Test
  @TestTransaction
  void testDeleteAltSignatur() throws Exception {
    Path teiFilePath =
        Paths.get(
            "../domainmodel-tei-mapper/src", "test", "resources", "tei",
            "tei-HSP-kodms1.xml");

    final NormdatenReferenz staatsbibliothek = new NormdatenReferenz("NR-2",
        "Staatsbibliothek",
        "");
    final NormdatenReferenz berlin = new NormdatenReferenz("NR-1", "Berlin",
        "");

    Identifikation identifikation =
        new IdentifikationBuilder()
            .withIdent("Signatur")
            .withBesitzer(staatsbibliothek)
            .withAufbewahrungsOrt(berlin)
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();

    Identifikation altIdentifikation =
        new IdentifikationBuilder()
            .withIdent("AltSignatur")
            .withId("AltIdentifikation1")
            .withBesitzer(staatsbibliothek)
            .withAufbewahrungsOrt(berlin)
            .withIdentTyp(IdentTyp.ALTSIGNATUR)
            .build();

    final KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("KOD1")
            .withGueltigerIdentifikation(identifikation)
            .addAlternativeIdentifikation(altIdentifikation)
            .withTEIXml(Files.readString(teiFilePath))
            .withBeschreibungsdokumentIDs(Set.of("1234"))
            .build();

    // Sync Identifier in Model and TEI
    TEIKulturObjektDokumentCommand.updateMsIdentifierAndAllAltidentifier(
        kulturObjektDokument);

    kulturObjektDokumentRepository.save(kulturObjektDokument);

    KulturObjektDokument kod = kulturObjektDokumentRepository.findById("KOD1");
    assertEquals(1, kod.getAlternativeIdentifikationen().size());
    kulturObjektDokumentService.deleteAltIdentifikation(BEARBEITER, "KOD1",
        "AltIdentifikation1");
    KulturObjektDokument kodUpdated = kulturObjektDokumentRepository.findById(
        "KOD1");
    assertEquals(0, kodUpdated.getAlternativeIdentifikationen().size());
  }

  @Test
  @TestTransaction
  void testDeleteAltSignaturWhenReferenced() throws Exception {
    Path teiFilePath =
        Paths.get(
            "../domainmodel-tei-mapper/src", "test", "resources", "tei",
            "tei-HSP-kodms1.xml");

    final KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("KOD1")
            .withTEIXml(Files.readString(teiFilePath))
            .withGueltigerIdentifikation(
                new Identifikation.IdentifikationBuilder()
                    .withId("ID1")
                    .withIdent("Signatur1")
                    .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
                    .build())
            .addAlternativeIdentifikation(
                new Identifikation.IdentifikationBuilder()
                    .withId("ID2")
                    .withIdent("Signatur2")
                    .withIdentTyp(IdentTyp.ALTSIGNATUR)
                    .build())
            .addAlternativeIdentifikation(
                new Identifikation.IdentifikationBuilder()
                    .withId("ID3")
                    .withIdent("Signatur3")
                    .withIdentTyp(IdentTyp.ALTSIGNATUR)
                    .build())
            .withBeschreibungsdokumentIDs(Set.of("bd1", "bd2", "bd3"))
            .build();
    kulturObjektDokumentRepository.save(kulturObjektDokument);

    String query =
        """
            INSERT INTO beschreibungsdokument (ID, gueltigeidentifikation_id) VALUES ('bd1', 'ID1');
            INSERT INTO beschreibungsdokument (ID, gueltigeidentifikation_id) VALUES ('bd2', 'ID2');
            INSERT INTO beschreibungsdokument (ID, gueltigeidentifikation_id) VALUES ('bd3', 'ID3');
            """;
    entityManager.createNativeQuery(query).executeUpdate();

    KulturObjektDokument kod = kulturObjektDokumentRepository.findById("KOD1");
    assertEquals(2, kod.getAlternativeIdentifikationen().size());
    assertThrows(
        KulturObjektDokumentException.class,
        () -> kulturObjektDokumentService.deleteAltIdentifikation(BEARBEITER,
            "KOD1", "ID2"));
    kod = kulturObjektDokumentRepository.findById("KOD1");
    assertEquals(2, kod.getAlternativeIdentifikationen().size());
  }

  @Test
  @TestTransaction
  void testDeleteAltSignaturGesperrt() throws Exception {
    Path teiFilePath =
        Paths.get(
            "../domainmodel-tei-mapper/src", "test", "resources", "tei",
            "tei-HSP-kodms1.xml");

    final KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("KOD1")
            .withTEIXml(Files.readString(teiFilePath))
            .withGueltigerIdentifikation(
                new Identifikation.IdentifikationBuilder()
                    .withId("ID1")
                    .withIdent("Signatur1")
                    .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
                    .build())
            .addAlternativeIdentifikation(
                new Identifikation.IdentifikationBuilder()
                    .withId("ID2")
                    .withIdent("Signatur2")
                    .withIdentTyp(IdentTyp.ALTSIGNATUR)
                    .build())
            .addAlternativeIdentifikation(
                new Identifikation.IdentifikationBuilder()
                    .withId("ID3")
                    .withIdent("Signatur3")
                    .withIdentTyp(IdentTyp.ALTSIGNATUR)
                    .build())
            .withBeschreibungsdokumentIDs(Set.of("bd1", "bd2", "bd3"))
            .build();
    KulturObjektDokument kod = kulturObjektDokumentRepository.save(
        kulturObjektDokument);

    kulturObjektDokumentService.kulturObjektDokumentSperren(
        SPERRENDER_BEARBEITER, "TESTGRUND", kod.getId());
    assertEquals(2, kod.getAlternativeIdentifikationen().size());

    String kodId = kulturObjektDokument.getId();

    assertThrows(
        SperreAlreadyExistException.class,
        () -> kulturObjektDokumentService.deleteAltIdentifikation(BEARBEITER, kodId, "ID2"));

    kod = kulturObjektDokumentRepository.findById("KOD1");
    assertEquals(2, kod.getAlternativeIdentifikationen().size());
  }

  private Titel createTitel() {
    return Titel.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId(KOD_ID)
        .withText("Titel der Beschreibung")
        .build();
  }

  private Beschreibstoff createBeschreibstoff() {
    return Beschreibstoff.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId(KOD_ID)
        .withText("Pergament, Papyrus (Freitext)")
        .withReferenzen(List.of(
            new NormdatenReferenz("NORM-1", "Pergament", "4039687-3"),
            new NormdatenReferenz("NORM-2", "Papyrus", "4039687-4")))
        .build();
  }

  private Umfang createUmfang() {
    return Umfang.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId(KOD_ID)
        .withText("I + 394 Bl.")
        .withBlattzahl("394")
        .build();
  }

  private Abmessung createAbmessung() {
    return Abmessung.builder()
        .withReferenzId(KOD_ID)
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .addAbmessungWert(
            AbmessungWert.builder()
                .withText("23,5 × 16,5 x 2,5")
                .withHoehe("23,5")
                .withBreite("16,5")
                .withTiefe("2,5")
                .withTyp("factual")
                .build())
        .build();
  }

  private Format createFormat() {
    return Format.builder()
        .withReferenzId(KOD_ID)
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .addFormatWert(
            FormatWert.builder()
                .withReferenz(
                    new NormdatenReferenz("NORM-1", "long and narrow",
                        "4039687-3"))
                .withTyp("deduced")
                .build()
        )
        .build();
  }

  private Entstehungsort createEntstehungsort() {
    return Entstehungsort.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId(KOD_ID)
        .withText("Weißenburg")
        .build();
  }

  private Entstehungszeit createEntstehungszeit() {
    return Entstehungszeit.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId(KOD_ID)
        .addEntstehungszeitWert(
            EntstehungszeitWert.builder()
                .withText("1446 oder vorher (Fasz. I)")
                .withNichtVor("1426")
                .withNichtNach("1446")
                .withTyp("datable")
                .build())
        .build();
  }

  private Grundsprache createGrundSprache() {
    return Grundsprache.builder()
        .withReferenzId(KOD_ID)
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withText("Mittelhochdeutsch")
        .addReferenz(new NormdatenReferenz("NORM-1", "deutsch", "4039687-3"))
        .build();
  }

  private Buchschmuck createBuchschmuck() {
    return Buchschmuck.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId(KOD_ID)
        .withText("yes")
        .build();
  }

  private Handschriftentyp createHandschriftentyp() {
    return Handschriftentyp.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId(KOD_ID)
        .addReferenz(new NormdatenReferenz("NORM-789", "composite",
            "4006789-0"))
        .build();
  }

  private Musiknotation createMusiknotation() {
    return Musiknotation.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId(KOD_ID)
        .withText("yes")
        .build();
  }

  private Status createStatus() {
    return Status.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId(KOD_ID)
        .withText("displaced")
        .build();
  }

  @Test
  @TestTransaction
  void testAttributsReferenzenAktualisierenOhneBeschreibungenUndReferenzen()
      throws Exception {

    Path teiKodFilePath = Paths.get("src", "test", "resources",
        "kod-attributsreferenzen_leer.xml");

    String teiKODXmlContent;
    teiKODXmlContent = Files.readString(teiKodFilePath, StandardCharsets.UTF_8);

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("KOD1")
        .withTEIXml(teiKODXmlContent)
        .build();

    kod.setBeschreibungenIDs(Collections.emptySet());

    kulturObjektDokumentService.attributsReferenzenAusBeschreibungenAktualisieren(
        kod,
        AttributsReferenzStatusTyp.AUTOMATISCH, "changeID");

    assertFalse(kod.getAttributsReferenzen().isEmpty(),
        "AttributsReferenzen sollten nicht leer bleiben, auch wenn es keine Beschreibungen gibt!");

    assertTrue(kod.getBeschreibungenIDs().isEmpty(),
        "Es sollten keine Beschreibungen für KOD vorhanden sein!");
    assertEquals(AttributsReferenzStatusTyp.LEER,
        kod.getAttributsReferenzStatusTyp(),
        "Status muss Leer sein, da keine Beschreibungen "
            + "vorhanden");
  }

  @Test
  @TestTransaction
  void testAttributsReferenzenAktualisierenMitEinemBeschreibungManuallySet()
      throws IOException, HSPMapperException {

    Path teiKodFilePath = Paths.get("src", "test", "resources",
        "kod_attributsreferenzen_man.xml");

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("KOD1")
        .withTEIXml(Files.readString(teiKodFilePath, StandardCharsets.UTF_8))
        .build();

    kulturObjektDokumentService.attributsReferenzenAusBeschreibungenAktualisieren(
        kod,
        AttributsReferenzStatusTyp.AUTOMATISCH, "changeID");

    assertNotNull(kod.getAttributsReferenzen());
    assertEquals(AttributsReferenzStatusTyp.MANUELL,
        kod.getAttributsReferenzStatusTyp());
  }

  @Test
  @TestTransaction
  void testAttributsReferenzenAktualisierenMitEinemBeschreibungManuallySetAndOverride()
      throws IOException, HSPMapperException {

    Path teiKodFilePath = Paths.get("src", "test", "resources",
        "kod_attributsreferenzen_man.xml");

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("KOD1")
        .withTEIXml(Files.readString(teiKodFilePath, StandardCharsets.UTF_8))
        .build();

    Path teiBeschreibungFilePath = Paths.get("src", "test", "resources",
        "tei-beschreibung-attributsreferenzen.xml");
    String teiBeschreibungXmlContent;

    teiBeschreibungXmlContent = Files.readString(teiBeschreibungFilePath,
        StandardCharsets.UTF_8);

    PURL purlInternal =
        new PURL(
            URI.create("https://resolver.de/40170459"),
            URI.create("https://target.de/40170459"),
            PURLTyp.INTERNAL);
    Publikation publikation1 = new Publikation.PublikationBuilder()
        .withPublikationsDatumHSP(LocalDateTime.of(2022, Month.AUGUST, 15, 14, 0))
        .addPURL(purlInternal)
        .build();
    publikation1.onApproved();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("2",
        "KOD1",
        new Identifikation.IdentifikationBuilder()
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build())
        .withTEIXml(teiBeschreibungXmlContent)
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withPublikation(publikation1)
        .build();

    kod.getBeschreibungenIDs().add(beschreibung.getId());

    beschreibungsRepository.saveAndFlush(beschreibung);

    werkzeugOverrideAttributsReferenzen.setAttributsreferenzenOverride(false);

    kulturObjektDokumentService.attributsReferenzenAusBeschreibungenAktualisieren(
        kod,
        AttributsReferenzStatusTyp.AUTOMATISCH, "changeID");

    assertNotNull(kod.getAttributsReferenzen());
    assertEquals(AttributsReferenzStatusTyp.MANUELL,
        kod.getAttributsReferenzStatusTyp());

    werkzeugOverrideAttributsReferenzen.setAttributsreferenzenOverride(true);

    kulturObjektDokumentService.attributsReferenzenAusBeschreibungenAktualisieren(
        kod,
        AttributsReferenzStatusTyp.AUTOMATISCH, "changeID");

    assertEquals(AttributsReferenzStatusTyp.AUTOMATISCH,
        kod.getAttributsReferenzStatusTyp());
  }

  @Test
  @TestTransaction
  void testAttributsReferenzenAktualisierenMitEinemBeschreibung()
      throws Exception {

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .build();

    Path teiKodFilePath = Paths.get("src", "test", "resources",
        "kod-attributsreferenzen_leer.xml");
    String teiKODXmlContent;
    try {
      teiKODXmlContent = Files.readString(teiKodFilePath,
          StandardCharsets.UTF_8);
    } catch (IOException exception) {
      fail("Failed to read the TEI XML file: " + exception.getMessage());
      return;
    }

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("KOD1")
        .withTEIXml(teiKODXmlContent)
        .build();

    Path teiBeschreibungFilePath = Paths.get("src", "test", "resources",
        "tei-beschreibung-attributsreferenzen.xml");
    String teiBeschreibungXmlContent;
    try {
      teiBeschreibungXmlContent = Files.readString(teiBeschreibungFilePath,
          StandardCharsets.UTF_8);
    } catch (IOException exception) {
      fail("Failed to read the TEI XML file: " + exception.getMessage());
      return;
    }

    PURL purlInternal =
        new PURL(
            URI.create("https://resolver.de/40170459"),
            URI.create("https://target.de/40170459"),
            PURLTyp.INTERNAL);

    Publikation pub2 = new PublikationBuilder()
        .withPublikationsDatumHSP(LocalDateTime.of(2022, Month.AUGUST, 15, 14, 0))
        .addPURL(purlInternal)
        .build();
    pub2.onApproved();
    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("2",
        "KOD_001",
        identifikation)
        .withTEIXml(teiBeschreibungXmlContent)
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withPublikation(pub2)
        .build();

    beschreibungsRepository.saveAndFlush(beschreibung);

    kod.setBeschreibungenIDs(Set.of(beschreibung.getId()));

    kulturObjektDokumentService.attributsReferenzenAusBeschreibungenAktualisieren(
        kod,
        AttributsReferenzStatusTyp.AUTOMATISCH, "changeID");

    assertNotNull(kod.getAttributsReferenzen(),
        "AttributsReferenzen sollten nicht null sein!");
    assertFalse(kod.getAttributsReferenzen().isEmpty(),
        "AttributsReferenzen sollten nicht leer sein!");

    for (AttributsReferenz ref : kod.getAttributsReferenzen().values()) {
      assertEquals(pub2.getErstpublikationsDatum(),
          ref.getPublikationDatum(),
          "Das Publikationsdatum sollte aus der einzigen Beschreibung übernommen werden!");
      assertEquals(AttributsReferenzStatusTyp.AUTOMATISCH,
          ref.getAttributsReferenzStatusTyp());
      assertNotNull(ref.getModificationDatum());
      assertEquals("changeID", ref.getChangeID());
    }
    assertEquals(AttributsReferenzStatusTyp.AUTOMATISCH,
        kod.getAttributsReferenzStatusTyp());
  }

  @Test
  @TestTransaction
  void testAttributsReferenzenAktualisierenMitMehrererBeschreibungenUndExistierendenReferenzen()
      throws Exception {

    // Beschreibung 1
    Path teiBeschreibungFilePath1 = Paths.get("src", "test", "resources",
        "tei-beschreibung-attributsreferenzen.xml");
    String teiBeschreibungXmlContent1 = Files.readString(teiBeschreibungFilePath1,
        StandardCharsets.UTF_8);

    PURL purlInternal1 =
        new PURL(
            URI.create("https://resolver.de/40170459"),
            URI.create("https://target.de/40170459"),
            PURLTyp.INTERNAL);

    Publikation publikation1 = new Publikation.PublikationBuilder()
        .withErstpublikationsDatum(LocalDateTime.of(2020, Month.MAY, 10, 12, 0))
        .withPublikationsDatumHSP(LocalDateTime.of(2021, Month.JANUARY, 1, 14, 0))
        .addPURL(purlInternal1)
        .build();
    publikation1.onApproved();

    Identifikation identifikation1 = new Identifikation.IdentifikationBuilder()
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withId(UUID.randomUUID().toString())
        .build();

    Beschreibung beschreibung1 = new Beschreibung.BeschreibungsBuilder(
        "HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001", "KOD_001",
        identifikation1)
        .withTEIXml(teiBeschreibungXmlContent1)
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withPublikation(publikation1)
        .build();

    // Beschreibung 2
    Path teiBeschreibungFilePath2 = Paths.get("src", "test", "resources",
        "tei-beschreibung-attributsreferenzen_2.xml");
    String teiBeschreibungXmlContent2 = Files.readString(teiBeschreibungFilePath2,
        StandardCharsets.UTF_8);

    PURL purlInternal2 =
        new PURL(
            URI.create("https://resolver.de/40170459"),
            URI.create("https://target.de/40170459"),
            PURLTyp.INTERNAL);

    Publikation publikation2 = new Publikation.PublikationBuilder()
        .withErstpublikationsDatum(LocalDateTime.of(2022, Month.MARCH, 5, 9, 30))
        .withPublikationsDatumHSP(LocalDateTime.of(2023, Month.AUGUST, 15, 14, 0))
        .addPURL(purlInternal2)
        .build();
    publikation2.onApproved();

    Identifikation identifikation2 = new Identifikation.IdentifikationBuilder()
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withId(UUID.randomUUID().toString())
        .build();

    Beschreibung beschreibung2 = new Beschreibung.BeschreibungsBuilder(
        "HSP-f9f6eb32-068c-3089-a7d4-bbc3e55f3235", "KOD_001",
        identifikation2)
        .withTEIXml(teiBeschreibungXmlContent2)
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withPublikation(publikation2)
        .build();

    beschreibungsRepository.saveAndFlush(beschreibung1);
    beschreibungsRepository.saveAndFlush(beschreibung2);

    Map<AttributsTyp, AttributsReferenz> alteReferenzen = new EnumMap<>(AttributsTyp.class);
    alteReferenzen.put(AttributsTyp.UMFANG, new Umfang.UmfangBuilder()
        .withReferenzId("Test1")
        .withAttributsReferenzStatusTyp(AttributsReferenzStatusTyp.MANUELL)
        .withPublikationDatum(LocalDateTime.of(1900, Month.OCTOBER, 10, 12, 12))
        .build());

    alteReferenzen.put(AttributsTyp.TITEL, new Titel.TitelBuilder()
        .withReferenzId("Test2")
        .withAttributsReferenzStatusTyp(AttributsReferenzStatusTyp.AUTOMATISCH)
        .withPublikationDatum(LocalDateTime.of(1890, Month.OCTOBER, 10, 12, 12))
        .build());

    alteReferenzen.put(AttributsTyp.BUCHSCHMUCK,
        new Buchschmuck.BuchschmuckBuilder()
            .withReferenzId("Test3")
            .withAttributsReferenzStatusTyp(AttributsReferenzStatusTyp.MANUELL)
            .withPublikationDatum(LocalDateTime.of(1900, Month.OCTOBER, 10, 12, 12))
            .build());

    alteReferenzen.put(AttributsTyp.MUSIKNOTATION,
        new Musiknotation.MusiknotationBuilder()
            .withReferenzId("Test4")
            .withAttributsReferenzStatusTyp(
                AttributsReferenzStatusTyp.AUTOMATISCH)
            .build());

    alteReferenzen.put(AttributsTyp.ENTSTEHUNGSORT,
        new Musiknotation.MusiknotationBuilder()
            .withReferenzId("Test5")
            .withAttributsReferenzStatusTyp(AttributsReferenzStatusTyp.LEER)
            .build());

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("KOD1")
        .withBeschreibungsdokumentIDs(Set.of(beschreibung1.getId(), beschreibung2.getId()))
        .withTEIXml("<dummy/>")
        .withAttributsReferenzen(new EnumMap<>(alteReferenzen))
        .build();

    try (MockedStatic<TEI2AttributsReferenzMapper> mockedMapper =
        mockStatic(TEI2AttributsReferenzMapper.class, Mockito.CALLS_REAL_METHODS)) {

      // Only mock the KOD's dummy XML, let real method parse Beschreibungen' TEI
      mockedMapper.when(() -> TEI2AttributsReferenzMapper.map(eq("<dummy/>")))
          .thenReturn(new EnumMap<>(alteReferenzen));

      kulturObjektDokumentService.attributsReferenzenAusBeschreibungenAktualisieren(
          kod,
          AttributsReferenzStatusTyp.AUTOMATISCH, "changeID");
    }

    Map<AttributsTyp, AttributsReferenz> kodReferenzen = kod.getAttributsReferenzen();

    assertNotNull(kodReferenzen,
        "AttributsReferenzen sollten nicht null sein!");
    assertFalse(kodReferenzen.isEmpty(),
        "AttributsReferenzen sollten nicht leer sein!");

    assertEquals(alteReferenzen.get(AttributsTyp.UMFANG),
        kodReferenzen.get(AttributsTyp.UMFANG),
        "Manuelle Referenz für UMFANG sollte unverändert bleiben!");

    assertEquals(alteReferenzen.get(AttributsTyp.BUCHSCHMUCK),
        kodReferenzen.get(AttributsTyp.BUCHSCHMUCK),
        "Manuelle Referenz für BUCHSCHMUCK sollte unverändert bleiben!");

    assertNotEquals(alteReferenzen.get(AttributsTyp.TITEL),
        kodReferenzen.get(AttributsTyp.TITEL),
        "Automatische Referenz für TITEL sollte aktualisiert werden!");

    assertEquals(publikation2.getErstpublikationsDatum(),
        kodReferenzen.get(AttributsTyp.TITEL).getPublikationDatum(),
        "Publikationsdatum für TITEL sollte aus der neuesten Beschreibung übernommen werden!");

    assertNotNull(kodReferenzen.get(AttributsTyp.GRUNDSPRACHE),
        "Neue Referenz für GRUNDSPRACHE sollte existieren, wenn aktualisiert!");

    assertNotEquals(alteReferenzen.get(AttributsTyp.GRUNDSPRACHE),
        kodReferenzen.get(AttributsTyp.GRUNDSPRACHE));

    assertNotEquals(AttributsReferenzStatusTyp.AUTOMATISCH,
        kodReferenzen.get(AttributsTyp.BUCHSCHMUCK)
            .getAttributsReferenzStatusTyp());

    assertNotNull(kodReferenzen.get(AttributsTyp.TITEL).getModificationDatum());

    assertEquals(AttributsReferenzStatusTyp.MANUELL,
        kodReferenzen.get(AttributsTyp.UMFANG).getAttributsReferenzStatusTyp());

  }

  @Test
  void testFindeNeuesteAttributsReferenzenAusBeschreibungen()
      throws IOException, HSPMapperException {
    Path beschreibungPath1980 = Paths.get("src", "test", "resources",
        "beschreibung_freiburg_1980.xml");
    Path beschreibungPath1988 = Paths.get("src", "test", "resources",
        "beschreibung_freiburg_1988.xml");

    Beschreibung beschreibung1980 = new Beschreibung.BeschreibungsBuilder(
        "HSP-bed99ed5-4722-35c4-9aa2-785445a25888", "KOD_001",
        null)
        .withTEIXml(Files.readString(beschreibungPath1980))
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withPublikation(new Publikation.PublikationBuilder()
            .withErstpublikationsDatum(LocalDateTime.of(1980, Month.MAY, 10, 12, 0))
            .build())
        .build();

    Beschreibung beschreibung1988 = new Beschreibung.BeschreibungsBuilder(
        "HSP-33ea2722-3764-37b0-ba51-759ae00c811a", "KOD_001",
        null)
        .withTEIXml(Files.readString(beschreibungPath1988))
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withPublikation(new Publikation.PublikationBuilder()
            .withErstpublikationsDatum(LocalDateTime.of(1988, Month.MAY, 10, 12, 0))
            .build())
        .build();

    Map<AttributsTyp, AttributsReferenz> result = kulturObjektDokumentService.findeNeuesteAttributsReferenzenAusBeschreibungen(
        List.of(beschreibung1980, beschreibung1988));

    assertNotNull(result);

    assertEquals(LocalDateTime.of(1980, Month.MAY, 10, 12, 0, 0),
        result.get(AttributsTyp.TITEL).getPublikationDatum());
    assertEquals(LocalDateTime.of(1988, Month.MAY, 10, 12, 0, 0),
        result.get(AttributsTyp.HANDSCHRIFTENTYP).getPublikationDatum());
  }

  @Test
  void testLoadAttributsReferenzenFromTei() throws HSPMapperException {
    Path teiFilePath = Paths.get("src", "test", "resources",
        "kod-attributsreferenzen.xml");

    String teiXmlContent;
    try {
      teiXmlContent = Files.readString(teiFilePath, StandardCharsets.UTF_8);
    } catch (IOException exception) {
      fail("Failed to read the TEI XML file: " + exception.getMessage());
      return;
    }

    KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("KOD1")
            .withTEIXml(teiXmlContent)
            .build();

    Map<AttributsTyp, AttributsReferenz> attributsReferenzen =
        kulturObjektDokumentService.loadAttributsReferenzenFromTei(
            kulturObjektDokument);

    assertNotNull(attributsReferenzen,
        "AttributsReferenzen sollten nicht null sein!");
    assertFalse(attributsReferenzen.isEmpty(),
        "AttributsReferenzen sollten nicht leer sein!");

    assertTrue(attributsReferenzen.containsKey(AttributsTyp.TITEL),
        "TITEL Referenz fehlt!");
    assertTrue(attributsReferenzen.containsKey(AttributsTyp.UMFANG),
        "UMFANG Referenz fehlt!");
    assertTrue(attributsReferenzen.containsKey(AttributsTyp.BUCHSCHMUCK),
        "BUCHSCHMUCK Referenz fehlt!");

    AttributsReferenz titelReferenz = attributsReferenzen.get(
        AttributsTyp.TITEL);
    assertNotNull(titelReferenz, "TITEL Referenz sollte existieren!");
    assertNotNull(titelReferenz.getReferenzId(),
        "TITEL ReferenzId sollte nicht null sein!");
    assertNotNull(titelReferenz.getReferenzTyp(),
        "TITEL ReferenzTyp sollte nicht null sein!");

    AttributsReferenz umfangReferenz = attributsReferenzen.get(
        AttributsTyp.UMFANG);
    assertNotNull(umfangReferenz, "UMFANG Referenz sollte existieren!");
    assertNotNull(umfangReferenz.getReferenzId(),
        "UMFANG ReferenzId sollte nicht null sein!");
    assertNotNull(umfangReferenz.getReferenzTyp(),
        "UMFANG ReferenzTyp sollte nicht null sein!");

    AttributsReferenz buchschmuckReferenz = attributsReferenzen.get(
        AttributsTyp.BUCHSCHMUCK);
    assertNotNull(buchschmuckReferenz,
        "BUCHSCHMUCK Referenz sollte existieren!");
    assertNotNull(buchschmuckReferenz.getReferenzId(),
        "BUCHSCHMUCK ReferenzId sollte nicht null sein!");
    assertNotNull(buchschmuckReferenz.getReferenzTyp(),
        "BUCHSCHMUCK ReferenzTyp sollte nicht null sein!");
  }

  @Test
  void testLoadAttributsReferenzenFromTeiWithNullTeiXml() {
    KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("KOD3")
            .withTEIXml(null)
            .build();

    assertThrows(
        IllegalArgumentException.class,
        () -> kulturObjektDokumentService.loadAttributsReferenzenFromTei(
            kulturObjektDokument),
        "Expected IllegalArgumentException for null TEI XML");
  }

  @Test
  void testLoadAttributsReferenzenFromTeiWithEmptyTeiXml() {
    KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("KOD4")
            .withTEIXml("")
            .build();

    assertThrows(
        IllegalArgumentException.class,
        () -> kulturObjektDokumentService.loadAttributsReferenzenFromTei(
            kulturObjektDokument),
        "Expected IllegalArgumentException for empty TEI XML");
  }

  @Test
  void findAllInvalid_gibtNurUngueltigeZurueck_undMappedFelderKorrekt_new() throws Exception {
    XMLDokument xmlValid = mock(XMLDokument.class);
    when(xmlValid.getId()).thenReturn("KOD-VALID");
    when(xmlValid.getTeiXml()).thenReturn("<TEI id='valid'/>");

    XMLDokument xmlInvalid = mock(XMLDokument.class);
    when(xmlInvalid.getId()).thenReturn("KOD-INVALID");
    when(xmlInvalid.getTeiXml()).thenReturn("<TEI id='invalid'/>");

    when(kulturObjektDokumentRepositoryMOCK.selectAllTeiXml())
        .thenAnswer(_ -> Stream.of(xmlValid, xmlInvalid));

    when(teiXmlValidationBoundaryMOCK.validateTeiXml(anyString(), anyString(), eq(false)))
        .thenReturn(new ValidationResult(true, null, null, "ok"))
        .thenReturn(new ValidationResult(false, "12", "34",
            "Dokument ist nicht valide -> element \"textLang\" not allowed"));

    NormdatenReferenz besitzer = mock(NormdatenReferenz.class);
    when(besitzer.getName()).thenReturn("Badische Landesbibliothek");
    NormdatenReferenz ort = mock(NormdatenReferenz.class);
    when(ort.getName()).thenReturn("Karlsruhe");

    Identifikation gi = mock(Identifikation.class);
    when(gi.getIdent()).thenReturn("Cod. Donaueschingen 423");
    when(gi.getBesitzer()).thenReturn(besitzer);
    when(gi.getAufbewahrungsOrt()).thenReturn(ort);

    KulturObjektDokument kodEntity = mock(KulturObjektDokument.class);
    when(kodEntity.getId()).thenReturn("KOD-INVALID");
    when(kodEntity.getGueltigeIdentifikation()).thenReturn(gi);

    when(kulturObjektDokumentRepositoryMOCK.findById("KOD-INVALID")).thenReturn(kodEntity);

    Set<KulturObjekteValidationReportItem> report = kulturObjektDokumentServiceMOCK.findAllInvalid();

    assertNotNull(report);
    assertEquals(1, report.size());

    KulturObjekteValidationReportItem item = report.iterator().next();
    assertEquals("KOD-INVALID", item.getKulturobjektHspID());
    assertEquals("Cod. Donaueschingen 423", item.getSignatur());
    assertEquals("Badische Landesbibliothek", item.getBestandsHaltendeEinrichtung());
    assertEquals("Karlsruhe", item.getOrt());
    assertFalse(item.getValidationResult().isValid());
    assertTrue(item.getValidationResult().getMessageWithDetails(Locale.GERMAN)
        .contains("Dokument ist nicht valide"));

    verify(teiXmlValidationBoundaryMOCK, atLeastOnce())
        .validateTeiXml(anyString(), anyString(), eq(false));
    verify(kulturObjektDokumentRepositoryMOCK, times(1)).findById("KOD-INVALID");
    verify(kulturObjektDokumentRepositoryMOCK, never()).findById("KOD-VALID");
  }

  @Test
  @TestTransaction
  void findAllInvalid_leerWennAlleGueltig() throws Exception {
    KulturObjektDokument kod1 = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("K1")
        .withTEIXml("<TEI/>")
        .build();

    KulturObjektDokument kod2 = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("K2")
        .withTEIXml("<TEI/>")
        .build();

    kulturObjektDokumentRepository.save(kod1);
    kulturObjektDokumentRepository.save(kod2);

    when(teiXmlValidationBoundaryMOCK.validateTeiXml(anyString(), anyString(), eq(false)))
        .thenReturn(new ValidationResult(true, null, null, "ok"))
        .thenReturn(new ValidationResult(true, null, null, "ok"));

    Set<KulturObjekteValidationReportItem> report =
        kulturObjektDokumentService.findAllInvalid();

    assertNotNull(report, "Report darf nicht null sein");
    assertTrue(report.isEmpty(), "Report sollte leer sein, wenn alle KOD gültig sind");
  }

  @Test
  void findAllInvalid_wirftIllegalState_wennValidatorNichtErreichbar() throws Exception {
    XMLDokument xml = mock(XMLDokument.class);
    when(xml.getId()).thenReturn("K1");
    when(xml.getTeiXml()).thenReturn("<TEI/>");

    when(kulturObjektDokumentRepositoryMOCK.selectAllTeiXml())
        .thenAnswer(_ -> Stream.of(xml));

    when(teiXmlValidationBoundaryMOCK.validateTeiXml(anyString(), anyString(), eq(false)))
        .thenThrow(new TeiXmlValidationException("ImportService down", null));

    IllegalStateException ex = assertThrows(
        IllegalStateException.class,
        () -> kulturObjektDokumentServiceMOCK.findAllInvalid(),
        "Bei nicht erreichbarem Validator sollte IllegalStateException geworfen werden"
    );
    assertTrue(ex.getMessage().contains("Validation service unavailable"),
        "Fehlermeldung sollte 'Validation service unavailable' enthalten");

    verify(kulturObjektDokumentRepositoryMOCK, never()).findById(anyString());
  }

  @Test
  @TestTransaction
  void testUpdateKODXML() throws Exception {
    Path teixml = Paths.get("src", "test", "resources", "kod_test_teiodd_3_5_5.xml");
    final NormdatenReferenz entstehungsOrt = new NormdatenReferenz(
        "NORM-956e3022-336a-344d-b0fb-ce9bc76dc2b9",
        "Wolfenbüttel", "GNDID");
    final NormdatenReferenz bremen = new NormdatenReferenz(
        "NORM-92c1f80a-07ad-337d-9b7e-00137d6a25f9",
        "Bremen", "4008135-7");
    final NormdatenReferenz digitalisierendeEinrichtung = new NormdatenReferenz(
        "NORM-af67d002-a9a6-3017-b8f1-1b2861b7d14b", "HAB",
        "GNDID");
    final NormdatenReferenz besitzer = new NormdatenReferenz(
        "NORM-3ece3a19-5a39-3ae7-beaa-028f6a66f03d", "Staats- und Universitätsbibliothek Bremen",
        "130088-X");
    normdatenReferenzRepository.saveAll(
        Arrays.asList(entstehungsOrt, digitalisierendeEinrichtung, besitzer, bremen));

    Digitalisat digitalisat = Digitalisat.DigitalisatBuilder()
        .withID("HSP-e3ec6342-2721-36bd-8e13-b5484cae8cf5")
        .withManifest("https://brema.suub.uni-bremen.de/i3f/v20/2445778/manifest")
        .withAlternativeUrl("https://nbn-resolving.de/urn:nbn:de:gbv:46:1-115474")
        .withThumbnail("https://brema.suub.uni-bremen.de/ms/image/largethumb/2445781")
        .withDigitalisierungsdatum(LocalDate.of(2021, Month.OCTOBER, 21))
        .withErstellungsdatum(LocalDate.of(2025, Month.OCTOBER, 23))
        .withPublikationsdatum(LocalDate.of(2025, Month.OCTOBER, 23))
        .withEntstehungsort(entstehungsOrt)
        .withDigitalisierendeEinrichtung(digitalisierendeEinrichtung)
        .build();

    Identifikation identifikation1 = new Identifikation.IdentifikationBuilder()
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withId(UUID.randomUUID().toString())
        .withAufbewahrungsOrt(bremen)
        .withBesitzer(besitzer)
        .build();

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-KOD_ID1")
        .withBeschreibungsdokumentIDs(Set.of("HSP-e15841af-7045-3540-b185-0415f6cd2d4b"))
        .withDigitalisaten(Set.of(digitalisat))
        .withGueltigerIdentifikation(identifikation1)
        .withTEIXml(Files.readString(teixml)).build();

    kulturObjektDokumentRepository.saveAndFlush(kod);

    final KulturObjektDokument updated = kulturObjektDokumentService.updateKODXML(kod.getId());

    assertTrue(
        updated.getTeiXML()
            .contains("<bibl xml:id=\"HSP-e3ec6342-2721-36bd-8e13-b5484cae8cf5\" default=\"true\""),
        updated.getTeiXML());

    assertTrue(
        updated.getTeiXML()
            .contains("<bibl xml:id=\"HSP-e15841af-7045-3540-b185-0415f6cd2d4b\" default=\"true\""),
        updated.getTeiXML());
  }

  @Test
  @TestTransaction
  void testFindKodByBeschreibung_kodIDHit() throws KulturObjektDokumentNotFoundException {
    NormdatenReferenz besitzer = new NormdatenReferenz("", "Staatsbibliothek zu Berlin", "");
    Identifikation identifikation = new IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .build();

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("1234")
        .withGndIdentifier("gndid1231234x")
        .withGueltigerIdentifikation(identifikation)
        .build();
    kulturObjektDokumentRepository.save(kod);

    Beschreibung beschreibung = new BeschreibungsBuilder("40170459", kod.getId(), identifikation)
        .build();

    assertTrue(kulturObjektDokumentService.findKodByBeschreibung(beschreibung).isPresent());
  }

  @Test
  @TestTransaction
  void testFindKodByBeschreibung_viaGueltigeIdentifikation()
      throws KulturObjektDokumentNotFoundException {
    NormdatenReferenz dresden = createDresden();
    NormdatenReferenz koerperschaft = createSLB();
    Identifikation identifikation = createIdnoMscrDresdA111(koerperschaft, dresden);

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("1234")
        .withGueltigerIdentifikation(identifikation)
        .build();
    kulturObjektDokumentRepository.save(kod);

    Beschreibung beschreibung = new BeschreibungsBuilder("40170459", kod.getId(), identifikation)
        .build();
    Beschreibung beschreibung1 = new BeschreibungsBuilder("345356476", kod.getId(), identifikation)
        .build();

    assertTrue(kulturObjektDokumentService.findKodByBeschreibung(beschreibung).isPresent());
    assertTrue(kulturObjektDokumentService.findKodByBeschreibung(beschreibung1).isPresent());
    assertEquals(kulturObjektDokumentService.findKodByBeschreibung(beschreibung).get(),
        kulturObjektDokumentService.findKodByBeschreibung(beschreibung1).get());
  }

  @Test
  @TestTransaction
  void testFindKodByBeschreibung_viaAlternativeIdentifikation()
      throws KulturObjektDokumentNotFoundException {
    NormdatenReferenz aufbewahrungsOrt = createDresden();
    NormdatenReferenz koerperschaft = createSLB();
    Identifikation identifikationKOD = createIdnoMscrDresdA111(koerperschaft, aufbewahrungsOrt);

    Identifikation alternativeIdentifikation = new IdentifikationBuilder()
        .withId("ident2")
        .withIdent("Mscr.Dresd.A.111_ALT_ID")
        .withIdentTyp(IdentTyp.ALTSIGNATUR)
        .withBesitzer(koerperschaft)
        .withAufbewahrungsOrt(aufbewahrungsOrt)
        .build();

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("1234")
        .withGueltigerIdentifikation(identifikationKOD)
        .addAlternativeIdentifikation(alternativeIdentifikation)
        .build();
    kulturObjektDokumentRepository.save(kod);

    Identifikation identifikationBeschreibung1 = new IdentifikationBuilder()
        .withId("ident3")
        .withIdent("Mscr.Dresd.A.111_ALT_ID")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(koerperschaft)
        .withAufbewahrungsOrt(aufbewahrungsOrt)
        .build();

    Beschreibung beschreibung = new BeschreibungsBuilder("40170459", kod.getId(), identifikationKOD)
        .build();
    Beschreibung beschreibung1 = new BeschreibungsBuilder("40170459", kod.getId(),
        identifikationBeschreibung1).build();

    assertTrue(kulturObjektDokumentService.findKodByBeschreibung(beschreibung).isPresent());
    assertTrue(kulturObjektDokumentService.findKodByBeschreibung(beschreibung1).isPresent());
    assertEquals(kulturObjektDokumentService.findKodByBeschreibung(beschreibung).get(),
        kulturObjektDokumentService.findKodByBeschreibung(beschreibung1).get());
  }

  @Test
  @TestTransaction
  void testFindKodByBeschreibung_returnsEmptyWhenNoKodMatches()
      throws KulturObjektDokumentNotFoundException {
    NormdatenReferenz dresden = createDresden();
    NormdatenReferenz koerperschaft = createSLB();
    Identifikation identifikation = createIdnoMscrDresdA111(koerperschaft, dresden);

    Beschreibung beschreibung = new BeschreibungsBuilder("40170459", null, identifikation).build();

    assertTrue(kulturObjektDokumentService.findKodByBeschreibung(beschreibung).isEmpty());
  }

  @Test
  @TestTransaction
  void testFindKodByBeschreibung_kodIDNull_fallsThroughToIdentifikationLookup()
      throws KulturObjektDokumentNotFoundException {
    NormdatenReferenz dresden = createDresden();
    NormdatenReferenz koerperschaft = createSLB();
    Identifikation identifikation = createIdnoMscrDresdA111(koerperschaft, dresden);

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("kod-via-ident")
        .withGueltigerIdentifikation(identifikation)
        .build();
    kulturObjektDokumentRepository.save(kod);

    Beschreibung beschreibung = new BeschreibungsBuilder("40170459", null, identifikation).build();

    Optional<KulturObjektDokument> result = kulturObjektDokumentService
        .findKodByBeschreibung(beschreibung);

    assertTrue(result.isPresent());
    assertEquals("kod-via-ident", result.get().getId());
  }

  @Test
  @TestTransaction
  void testFindKodByBeschreibung_staleKodID_fallsThroughToIdentifikationLookup()
      throws KulturObjektDokumentNotFoundException {
    NormdatenReferenz dresden = createDresden();
    NormdatenReferenz koerperschaft = createSLB();
    Identifikation identifikation = createIdnoMscrDresdA111(koerperschaft, dresden);

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("kod-via-ident")
        .withGueltigerIdentifikation(identifikation)
        .build();
    kulturObjektDokumentRepository.save(kod);

    Beschreibung beschreibung = new BeschreibungsBuilder("40170459",
        "non-existent-kod-id", identifikation).build();

    Optional<KulturObjektDokument> result = kulturObjektDokumentService
        .findKodByBeschreibung(beschreibung);

    assertTrue(result.isPresent());
    assertEquals("kod-via-ident", result.get().getId());
  }

  @Test
  @TestTransaction
  void testFindByIdentifikationNonUniqueResult() {
    NormdatenReferenz institution = new NormdatenReferenz("1", "SBB", "GND_1",
        GNDEntityFact.CORPORATE_BODY_TYPE_NAME);
    NormdatenReferenz ort = new NormdatenReferenz("2", "Berlin", "GND_2",
        GNDEntityFact.PLACE_TYPE_NAME);

    KulturObjektDokument kod1 = new KulturObjektDokumentBuilder()
        .withId("KOD_1")
        .withGueltigerIdentifikation(new IdentifikationBuilder()
            .withId("GI-1").withIdent("HS. 1").withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(institution).withAufbewahrungsOrt(ort).build())
        .build();
    kulturObjektDokumentRepository.save(kod1);

    KulturObjektDokument kod2 = new KulturObjektDokumentBuilder()
        .withId("KOD_2")
        .withGueltigerIdentifikation(new IdentifikationBuilder()
            .withId("GI-2").withIdent("HS. 1").withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(institution).withAufbewahrungsOrt(ort).build())
        .build();
    kulturObjektDokumentRepository.save(kod2);

    Identifikation beschreibungsIdentifikation = new IdentifikationBuilder()
        .withId("BI-1").withIdent("HS. 1").withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(institution).withAufbewahrungsOrt(ort).build();

    KulturObjektDokumentNotFoundException exception = assertThrows(
        KulturObjektDokumentNotFoundException.class,
        () -> kulturObjektDokumentService.findByIdentifikation(beschreibungsIdentifikation));

    assertEquals(
        "Es wurde mehr als eine übereinstimmende gültige KOD-Signatur zur BeschreibungsIdentifikation"
            + " gefunden: HS. 1",
        exception.getMessage());
  }

  @Test
  @TestTransaction
  void testFindByAlternativeIdentifikationNonUniqueResult() {
    NormdatenReferenz institution = new NormdatenReferenz("1", "SBB", "GND_1",
        GNDEntityFact.CORPORATE_BODY_TYPE_NAME);
    NormdatenReferenz ort = new NormdatenReferenz("2", "Berlin", "GND_2",
        GNDEntityFact.PLACE_TYPE_NAME);

    KulturObjektDokument kod1 = new KulturObjektDokumentBuilder()
        .withId("KOD_1")
        .withGueltigerIdentifikation(new IdentifikationBuilder()
            .withId("GI-1").withIdent("HS. 1").withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(institution).withAufbewahrungsOrt(ort).build())
        .addAlternativeIdentifikation(new IdentifikationBuilder()
            .withId("AI-1").withIdent("HS-1").withIdentTyp(IdentTyp.ALTSIGNATUR)
            .withBesitzer(institution).withAufbewahrungsOrt(ort).build())
        .build();
    kulturObjektDokumentRepository.save(kod1);

    KulturObjektDokument kod2 = new KulturObjektDokumentBuilder()
        .withId("KOD_2")
        .withGueltigerIdentifikation(new IdentifikationBuilder()
            .withId("GI-2").withIdent("HS. 1").withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(institution).withAufbewahrungsOrt(ort).build())
        .addAlternativeIdentifikation(new IdentifikationBuilder()
            .withId("AI-2").withIdent("HS-1").withIdentTyp(IdentTyp.ALTSIGNATUR)
            .withBesitzer(institution).withAufbewahrungsOrt(ort).build())
        .build();
    kulturObjektDokumentRepository.save(kod2);

    Identifikation beschreibungsIdentifikation = new IdentifikationBuilder()
        .withId("BS-1").withIdent("HS-1").withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(institution).withAufbewahrungsOrt(ort).build();

    KulturObjektDokumentNotFoundException exception = assertThrows(
        KulturObjektDokumentNotFoundException.class,
        () -> kulturObjektDokumentService.findByAlternativeIdentifikation(
            beschreibungsIdentifikation));

    assertEquals(
        "Es wurde mehr als eine übereinstimmende alternative KOD-Signatur zur "
            + "BeschreibungsIdentifikation gefunden: HS-1",
        exception.getMessage());
  }

  @Test
  @TestTransaction
  void testUmziehen_preservesSortKeyInAlternativeIdentifikation() throws Exception {

    NormdatenReferenz ort = createDresden();
    NormdatenReferenz besitzer = createSLB();

    Identifikation identifikation = new IdentifikationBuilder()
        .withId("ident-sortkey")
        .withIdent(GUELTIGE_SIGNATURE)
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(ort)
        .withSortKey("test_sortkey_123")
        .build();

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("kod-sortkey-test")
        .withGueltigerIdentifikation(identifikation)
        .withTEIXml("""
              <TEI xmlns="http://www.tei-c.org/ns/1.0">
                  <msDesc>
                      <msIdentifier/>
                  </msDesc>
              </TEI>
            """)
        .build();

    kulturObjektDokumentRepository.saveAndFlush(kod);

    KulturobjektDokumentUmziehenErgebnis ergebnis =
        new KulturobjektDokumentUmziehenErgebnis(
            "kod-sortkey-test",
            GUELTIGE_SIGNATURE,
            List.of(),
            createDresden(),
            createSLB(),
            LocalDateTime.now(),
            null
        );

    kulturObjektDokumentService.umziehen(
        List.of(ergebnis),
        BEARBEITER
    );

    KulturObjektDokument reloaded =
        kulturObjektDokumentRepository.findById("kod-sortkey-test");

    assertNotNull(reloaded);

    assertNotNull(reloaded.getAlternativeIdentifikationen());
    assertEquals(1, reloaded.getAlternativeIdentifikationen().size());

    Identifikation alt =
        reloaded.getAlternativeIdentifikationen().iterator().next();

    assertEquals("test_sortkey_123", alt.getSortKey());
    assertEquals(GUELTIGE_SIGNATURE, alt.getIdent());
    assertEquals(IdentTyp.FRUEHERE_ALTSIGNATUR, alt.getIdentTyp());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      """
          "Cbm Cat. 1"$"Sortkey"$"Alt1"$"Alt2"
          """,
      """
          Cbm Cat. 1$Sortkey$Alt1$Alt2
          """,
      """
          "Cbm Cat. 1"$""$"Alt1"
          """
  })
  void shouldParseCsvVariants(String csv) throws Exception {

    NormdatenReferenz ort = new NormdatenReferenz("1", "Berlin", "123");
    NormdatenReferenz koerperschaft = new NormdatenReferenz("2", "SB", "456");

    List<KulturObjektDokument> result =
        kulturObjektDokumentServiceMOCK.registrieren(
            new Bearbeiter("1", "Tester"),
            ort,
            koerperschaft,
            csv.getBytes(StandardCharsets.UTF_8)
        );

    assertFalse(result.isEmpty());
  }

  @Test
  void shouldParseMultipleLines() throws Exception {

    String csv = """
        "Cbm Cat. 1"$"Sortkey1"$"Codices bavarici monacenses Catalogi 1"
        "Clm 1330"$"Sortkey2"$"Clm Alt 1330"
        """;

    NormdatenReferenz ort = new NormdatenReferenz("1", "Berlin", "123");
    NormdatenReferenz koerperschaft = new NormdatenReferenz("2", "SB", "456");

    List<KulturObjektDokument> result =
        kulturObjektDokumentServiceMOCK.registrieren(
            new Bearbeiter("1", "Tester"),
            ort,
            koerperschaft,
            csv.getBytes(StandardCharsets.UTF_8)
        );

    assertEquals(2, result.size());
  }

  @Test
  void shouldChunkSortkeyEntriesAndAggregateReport() throws Exception {

    List<SortKeyEntry> entries = IntStream.range(0, 2500)
        .mapToObj(i -> new SortKeyEntry("uuid-" + i, "sortkey-" + i, i + 1))
        .toList();

    when(sortkeyUpdateImportParserMock.parse(any(), any()))
        .thenReturn(new SortKeyEntries(entries, List.of("Zeile 7")));

    when(sortkeyBatchServiceMOCK.processChunk(any()))
        .thenAnswer(invocation -> {
          List<SortKeyEntry> chunk = invocation.getArgument(0);
          return new SortKeyBatchResult(chunk.size(), List.of(chunk.getFirst().uuid()));
        });

    KulturObjektSortkeyUpdateReport report =
        kulturObjektDokumentServiceMOCK.updateSortkeys(new byte[]{1}, "sortkeys.csv");

    ArgumentCaptor<List<SortKeyEntry>> chunkCaptor = ArgumentCaptor.captor();
    verify(sortkeyBatchServiceMOCK, times(3)).processChunk(chunkCaptor.capture());

    assertEquals(
        List.of(1000, 1000, 500),
        chunkCaptor.getAllValues().stream().map(List::size).toList());

    assertEquals(2500, report.updatedCount());
    assertEquals(List.of("uuid-0", "uuid-1000", "uuid-2000"), report.unknownUuids());
    assertEquals(List.of("Zeile 7"), report.missingOrEmptySortkeyLines());
  }

  @Test
  void shouldThrowPartialImportExceptionWithCommittedCount() throws Exception {

    List<SortKeyEntry> entries = IntStream.range(0, 1500)
        .mapToObj(i -> new SortKeyEntry("uuid-" + i, "sortkey-" + i, i + 1))
        .toList();

    when(sortkeyUpdateImportParserMock.parse(any(), any()))
        .thenReturn(new SortKeyEntries(entries, List.of()));

    when(sortkeyBatchServiceMOCK.processChunk(any()))
        .thenReturn(new SortKeyBatchResult(800, List.of()))
        .thenThrow(new KulturObjektSortkeyUpdateException("kod_sortkeys_error"));

    SortkeyPartialImportException exception = assertThrows(
        SortkeyPartialImportException.class,
        () -> kulturObjektDokumentServiceMOCK.updateSortkeys(new byte[]{1}, "sortkeys.csv"));

    assertEquals(800, exception.getUpdatedCountBeforeFailure());
  }

  private KulturObjektDokumentService createService() {

    return new KulturObjektDokumentService(
        kulturObjektDokumentRepository,
        identifikationRepository,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK,
        dokumentSperreServiceMOCK,
        kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        distributedSaveCommandMOCK,
        bearbeiterBoundary,
        werkzeugOverrideAttributsReferenzen,
        teiXmlValidationBoundaryMOCK,
        sortkeyUpdateImportParserMock,
        sortkeyBatchServiceMOCK,
        kodRegistriertEventPublisherMOCK);
  }

  @Test
  void testFindKulturObjektDokumentForImportByCompleteSignatur() throws Exception {
    KulturObjektDokument expected = importKod("KOD-1");
    when(kulturObjektDokumentRepositoryMOCK
        .findByIdentifikationIdent("Ms. 1"))
        .thenReturn(List.of(expected));

    KulturObjektDokument result = kulturObjektDokumentServiceMOCK
        .findKulturObjektDokumentBySignaturForExterneRessourcenImport(
            " Ms. 1 ", " Staatsbibliothek ", " Berlin ");

    assertSame(expected, result);
  }

  @Test
  void testFindKulturObjektDokumentForImportFiltersByBesitzendeEinrichtung()
      throws Exception {
    KulturObjektDokument berlin = importKod(
        "KOD-1", "Staatsbibliothek zu Berlin", "Berlin");
    KulturObjektDokument bamberg = importKod(
        "KOD-2", "Staatsbibliothek Bamberg", "Berlin");
    when(kulturObjektDokumentRepositoryMOCK.findByIdentifikationIdent("Ms. 1"))
        .thenReturn(List.of(berlin, bamberg));

    KulturObjektDokument result = kulturObjektDokumentServiceMOCK
        .findKulturObjektDokumentBySignaturForExterneRessourcenImport(
            "Ms. 1", "Staatsbibliothek Bamberg", "Berlin");

    assertSame(bamberg, result);
  }

  @Test
  void testFindKulturObjektDokumentForImportFiltersByAufbewahrungsort()
      throws Exception {
    KulturObjektDokument berlin = importKod(
        "KOD-1", "Staatsbibliothek", "Berlin");
    KulturObjektDokument bamberg = importKod(
        "KOD-2", "Staatsbibliothek", "Bamberg");
    when(kulturObjektDokumentRepositoryMOCK.findByIdentifikationIdent("Ms. 1"))
        .thenReturn(List.of(berlin, bamberg));

    KulturObjektDokument result = kulturObjektDokumentServiceMOCK
        .findKulturObjektDokumentBySignaturForExterneRessourcenImport(
            "Ms. 1", "Staatsbibliothek", "Bamberg");

    assertSame(bamberg, result);
  }

  @Test
  void testFindKulturObjektDokumentByPurl() {
    KulturObjektDokument expected = importKod("KOD-1");
    when(kulturObjektDokumentRepositoryMOCK.findByPurl("https://resolver.example/KOD-1"))
        .thenReturn(Optional.of(expected));

    Optional<KulturObjektDokument> result = kulturObjektDokumentServiceMOCK
        .findByPurl(" https://resolver.example/KOD-1 ");

    assertTrue(result.isPresent());
    assertSame(expected, result.get());
    assertTrue(kulturObjektDokumentServiceMOCK.findByPurl(" ").isEmpty());
  }

  @Test
  void testFindKulturObjektDokumentForImportRejectsConflictingSingleSignaturMatch() {
    KulturObjektDokument bySignatur = importKod(
        "KOD-1", "Staatsbibliothek zu Berlin", "Berlin");
    when(kulturObjektDokumentRepositoryMOCK.findByIdentifikationIdent("Ms. 1"))
        .thenReturn(List.of(bySignatur));

    ExterneRessourcenImportException exception = assertThrows(
        ExterneRessourcenImportException.class,
        () -> kulturObjektDokumentServiceMOCK
            .findKulturObjektDokumentBySignaturForExterneRessourcenImport(
                "Ms. 1", "Staatsbibliothek Bamberg", "Bamberg"));

    assertEquals(KOD_CONFLICTING, exception.getMessageKey());
  }

  @Test
  void testFindKulturObjektDokumentForImportRejectsConflictingContextForMultipleMatches() {
    KulturObjektDokument berlin = importKod(
        "KOD-1", "Staatsbibliothek zu Berlin", "Berlin");
    KulturObjektDokument bamberg = importKod(
        "KOD-2", "Staatsbibliothek Bamberg", "Bamberg");
    when(kulturObjektDokumentRepositoryMOCK.findByIdentifikationIdent("Ms. 1"))
        .thenReturn(List.of(berlin, bamberg));

    ExterneRessourcenImportException exception = assertThrows(
        ExterneRessourcenImportException.class,
        () -> kulturObjektDokumentServiceMOCK
            .findKulturObjektDokumentBySignaturForExterneRessourcenImport(
                "Ms. 1", "Other Einrichtung", "Berlin"));

    assertEquals(KOD_CONFLICTING, exception.getMessageKey());
  }

  @Test
  void testFindKulturObjektDokumentForImportDoesNotMatchVarianterName() {
    KulturObjektDokument bySignatur = importKod(
        "KOD-1", "Staatsbibliothek zu Berlin", "Berlin");
    bySignatur.getGueltigeIdentifikation().getBesitzer().getVarianterName()
        .add(new VarianterName("SBB", "de"));
    when(kulturObjektDokumentRepositoryMOCK.findByIdentifikationIdent("Ms. 1"))
        .thenReturn(List.of(bySignatur, importKod("KOD-2", "Other", "Berlin")));

    ExterneRessourcenImportException exception = assertThrows(
        ExterneRessourcenImportException.class,
        () -> kulturObjektDokumentServiceMOCK
            .findKulturObjektDokumentBySignaturForExterneRessourcenImport(
                "Ms. 1", "SBB", "Berlin"));

    assertEquals(KOD_CONFLICTING, exception.getMessageKey());
  }

  @Test
  void testFindKulturObjektDokumentForImportRejectsAmbiguousIdentificationData() {
    when(kulturObjektDokumentRepositoryMOCK.findByIdentifikationIdent("Ms. 1"))
        .thenReturn(List.of(importKod("KOD-1"), importKod("KOD-2")));

    ExterneRessourcenImportException exception = assertThrows(
        ExterneRessourcenImportException.class,
        () -> kulturObjektDokumentServiceMOCK
            .findKulturObjektDokumentBySignaturForExterneRessourcenImport(
                "Ms. 1", "Staatsbibliothek", "Berlin"));

    assertEquals(KOD_AMBIGUOUS, exception.getMessageKey());
  }

  @Test
  void testFindKulturObjektDokumentForImportRejectsUnknownIdentificationData() {
    when(kulturObjektDokumentRepositoryMOCK.findByIdentifikationIdent("Ms. unknown"))
        .thenReturn(Collections.emptyList());

    ExterneRessourcenImportException exception = assertThrows(
        ExterneRessourcenImportException.class,
        () -> kulturObjektDokumentServiceMOCK
            .findKulturObjektDokumentBySignaturForExterneRessourcenImport(
                "Ms. unknown", "Staatsbibliothek", "Berlin"));

    assertEquals(KOD_NOT_FOUND, exception.getMessageKey());
  }

  @Test
  void testFindKulturObjektDokumentForImportRejectsIncompleteIdentificationData() {
    ExterneRessourcenImportException missingSignatur = assertThrows(
        ExterneRessourcenImportException.class,
        () -> kulturObjektDokumentServiceMOCK
            .findKulturObjektDokumentBySignaturForExterneRessourcenImport(
                "", "Staatsbibliothek", "Berlin"));
    ExterneRessourcenImportException missingEinrichtung = assertThrows(
        ExterneRessourcenImportException.class,
        () -> kulturObjektDokumentServiceMOCK
            .findKulturObjektDokumentBySignaturForExterneRessourcenImport(
                "Ms. 1", "", "Berlin"));
    ExterneRessourcenImportException missingAufbewahrungsort = assertThrows(
        ExterneRessourcenImportException.class,
        () -> kulturObjektDokumentServiceMOCK
            .findKulturObjektDokumentBySignaturForExterneRessourcenImport(
                "Ms. 1", "Staatsbibliothek", ""));

    assertEquals(
        IDENTIFICATION_INCOMPLETE,
        missingSignatur.getMessageKey());
    assertEquals(
        IDENTIFICATION_INCOMPLETE,
        missingEinrichtung.getMessageKey());
    assertEquals(
        IDENTIFICATION_INCOMPLETE,
        missingAufbewahrungsort.getMessageKey());
    verifyNoInteractions(kulturObjektDokumentRepositoryMOCK);
  }

  private static KulturObjektDokument importKod(String id) {
    return importKod(id, "Staatsbibliothek", "Berlin");
  }

  private static KulturObjektDokument importKod(
      String id, String besitzendeEinrichtung, String aufbewahrungsort) {
    Identifikation identifikation = new IdentifikationBuilder()
        .withId("IDENT-" + id)
        .withIdent("Ms. 1")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(new NormdatenReferenz("B-" + id, besitzendeEinrichtung, ""))
        .withAufbewahrungsOrt(new NormdatenReferenz("O-" + id, aufbewahrungsort, ""))
        .build();
    return new KulturObjektDokumentBuilder()
        .withId(id)
        .withGueltigerIdentifikation(identifikation)
        .build();
  }

  @Test
  @TestTransaction
  void testExterneRessourceImportieren() throws Exception {
    KulturObjektDokumentService service = createService();
    KulturObjektDokument kod = createKulturObjektDokument();
    kod.setId(KOD_ID);
    kod.setTeiXML(FileUtils.readFileToString(
        TEI_FILE_PATH.toFile(), StandardCharsets.UTF_8));
    kulturObjektDokumentRepository.save(kod);

    ExterneRessource externeRessource =
        new ExterneRessource(ExterneRessourceTyp.HSC, HSC_URL);

    service.externeRessourceImportieren(KOD_ID, externeRessource);

    ArgumentCaptor<KulturObjektDokument> kodCaptor =
        ArgumentCaptor.forClass(KulturObjektDokument.class);
    verify(distributedSaveCommandMOCK).execute(
        kodCaptor.capture(), eq(ActivityStreamAction.UPDATE));
    assertEquals(List.of(externeRessource),
        TEIExterneRessourcenCommand.readExterneRessourcen(
            kodCaptor.getValue().getTeiXML()));
  }

  @Test
  @TestTransaction
  void testExterneRessourcenImportFromCsvToTeiReportAndDownload() throws Exception {
    KulturObjektDokumentService service = createService();
    KulturObjektDokument kod = createKulturObjektDokument();
    kod.setId(KOD_ID);
    kod.setTeiXML(FileUtils.readFileToString(
        TEI_FILE_PATH.toFile(), StandardCharsets.UTF_8));
    kulturObjektDokumentRepository.save(kod);

    doAnswer(invocation -> {
      KulturObjektDokument updatedKod = invocation.getArgument(0);
      kulturObjektDokumentRepository.save(updatedKod);
      return null;
    }).when(distributedSaveCommandMOCK)
        .execute(any(KulturObjektDokument.class), eq(ActivityStreamAction.UPDATE));

    BearbeiterBoundary importBearbeiterBoundary = mock(BearbeiterBoundary.class);
    when(importBearbeiterBoundary.getLoggedBearbeiter()).thenReturn(BEARBEITER);
    ExterneRessourcenImportStateService stateService =
        new ExterneRessourcenImportStateService();
    ExterneRessourcenImportService importService = new ExterneRessourcenImportService(
        new ExterneRessourcenImportCSVParser(),
        new ExterneRessourcenImportEntryService(service));
    ExterneRessourcenImportModel importModel = new ExterneRessourcenImportModel(
        importService,
        stateService,
        importBearbeiterBoundary,
        null);
    byte[] importCsv = ("""
        ""$""$""$"%s"$""$"HSC"$"%s"
        """.formatted(KOD_ID, HSC_URL)).getBytes(StandardCharsets.UTF_8);

    assertTrue(importModel.startImport(importCsv));
    entityManager.flush();
    entityManager.clear();

    ExterneRessource externeRessource =
        new ExterneRessource(ExterneRessourceTyp.HSC, HSC_URL);
    Optional<KulturObjektDokumentViewModel> viewModel =
        service.buildKulturObjektDokumentViewModel(KOD_ID);

    assertTrue(viewModel.isPresent());
    assertEquals(List.of(externeRessource), viewModel.get().getExterneRessourcen());
    assertEquals(
        ExterneRessourcenImportStatus.SUCCESS,
        stateService.getStatus(BEARBEITER.getId()));

    ExterneRessourcenImportResultCSVWriter resultCSVWriter =
        new ExterneRessourcenImportResultCSVWriter();
    ExterneRessourcenImportController controller = new ExterneRessourcenImportController(
        importModel, stateService, importBearbeiterBoundary, resultCSVWriter);
    StreamedContent download = controller.getResultCsv();

    assertNotNull(download);
    assertEquals("externe-ressourcen-import-ergebnis.csv", download.getName());
    String resultText = new String(
        download.getStream().get().readAllBytes(), StandardCharsets.UTF_8);
    assertTrue(resultText.contains(
        KOD_ID + ";hsc;" + HSC_URL + ";ERFOLGREICH;"), resultText);
  }

  @Test
  @TestTransaction
  void testExterneRessourceImportierenDuplicate() throws Exception {
    KulturObjektDokumentService service = createService();
    KulturObjektDokument kod = createKulturObjektDokument();
    kod.setId(KOD_ID);
    kod.setTeiXML(FileUtils.readFileToString(
        TEI_FILE_PATH.toFile(), StandardCharsets.UTF_8));
    ExterneRessource externeRessource =
        new ExterneRessource(ExterneRessourceTyp.HSC, HSC_URL);
    kod.setTeiXML(TEIExterneRessourcenCommand
        .addExterneRessource(kod.getTeiXML(), externeRessource)
        .orElseThrow());
    kulturObjektDokumentRepository.save(kod);

    ExterneRessourcenImportException exception = assertThrows(
        ExterneRessourcenImportException.class,
        () -> service.externeRessourceImportieren(KOD_ID, externeRessource));

    assertEquals(DUPLICATE, exception.getMessageKey());
    assertArrayEquals(new Object[]{HSC_URL}, exception.getArgs());
    verify(distributedSaveCommandMOCK, never()).execute(any(), any());
  }

  @Test
  @TestTransaction
  void testExterneRessourceImportierenRejectsLockedKOD() throws Exception {
    KulturObjektDokumentService service = createService();
    KulturObjektDokument kod = createKulturObjektDokument();
    kod.setId(KOD_ID);
    kod.setTeiXML(FileUtils.readFileToString(
        TEI_FILE_PATH.toFile(), StandardCharsets.UTF_8));
    kulturObjektDokumentRepository.save(kod);
    when(dokumentSperreServiceMOCK.findBySperreEintraege(
        any(SperreEintrag[].class))).thenReturn(List.of(testSperre()));

    ExterneRessourcenImportException exception = assertThrows(
        ExterneRessourcenImportException.class,
        () -> service.externeRessourceImportieren(
            KOD_ID, new ExterneRessource(ExterneRessourceTyp.HSC, HSC_URL)));

    assertEquals(KOD_LOCKED, exception.getMessageKey());
    verify(distributedSaveCommandMOCK, never()).execute(any(), any());
  }

  @Test
  @TestTransaction
  void testExterneRessourceImportierenNoKOD() throws Exception {
    KulturObjektDokumentService service = createService();

    ExterneRessourcenImportException exception = assertThrows(
        ExterneRessourcenImportException.class,
        () -> service.externeRessourceImportieren(
            "INVALID", new ExterneRessource(ExterneRessourceTyp.HSC, HSC_URL)));

    assertEquals(UUID_NOT_FOUND, exception.getMessageKey());
    assertArrayEquals(new Object[]{"INVALID"}, exception.getArgs());
    verify(distributedSaveCommandMOCK, never()).execute(any(), any());
  }

  @Test
  @TestTransaction
  void testExterneRessourceHinzufuegen() throws Exception {
    KulturObjektDokumentService service = createService();

    KulturObjektDokument kod = createKulturObjektDokument();
    kod.setTeiXML(FileUtils.readFileToString(TEI_FILE_PATH.toFile(), StandardCharsets.UTF_8));

    kod.setId(KOD_ID);

    kulturObjektDokumentRepository.save(kod);

    Optional<KulturObjektDokument> updated = service.externeRessourceHinzufuegen(
        kod.getId(),
        ExterneRessourceTyp.HSC,
        HSC_URL,
        testSperre());

    assertTrue(updated.isPresent());

    String teiXml = updated.get().getTeiXML();

    assertEquals(List.of(new ExterneRessource(ExterneRessourceTyp.HSC, HSC_URL)),
        TEIExterneRessourcenCommand.readExterneRessourcen(teiXml));

    assertTrue(teiXml.contains(
        "<listBibl type=\"" + TEIValues.LIST_BIBL_TYPE_RESSOURCES + "\">"), teiXml);
    assertTrue(teiXml.contains(
        "type=\"" + ExterneRessourceTyp.HSC.getTeiType() + "\""), teiXml);
    assertTrue(teiXml.contains(
        "<ref target=\"" + HSC_URL + "\">"
            + ExterneRessourceTyp.HSC.getTeiCaption() + "</ref>"), teiXml);
  }

  @Test
  @TestTransaction
  void testExterneRessourceHinzufuegenNullParameter() {
    KulturObjektDokumentService service = createService();

    NullPointerException npe = assertThrows(NullPointerException.class, () ->
        service.externeRessourceHinzufuegen(
            null,
            ExterneRessourceTyp.HSC,
            HSC_URL,
            testSperre()));

    assertEquals("kulturObjektDokumentId is required", npe.getMessage());

    npe = assertThrows(NullPointerException.class, () ->
        service.externeRessourceHinzufuegen(
            KOD_ID,
            null,
            HSC_URL,
            testSperre()));

    assertEquals("typ is required", npe.getMessage());

    npe = assertThrows(NullPointerException.class, () ->
        service.externeRessourceHinzufuegen(
            KOD_ID,
            ExterneRessourceTyp.HSC,
            null,
            testSperre()));

    assertEquals("url is required", npe.getMessage());
  }

  @Test
  @TestTransaction
  void testExterneRessourceHinzufuegenNoKOD() {
    KulturObjektDokumentService service = createService();

    KulturObjektDokumentException exception =
        assertThrows(KulturObjektDokumentException.class, () ->
            service.externeRessourceHinzufuegen(
                "INVALID",
                ExterneRessourceTyp.HSC,
                HSC_URL,
                testSperre()));

    assertEquals("Zur Id INVALID wurde kein KulturObjektDokument gefunden!",
        exception.getMessage());
  }

  @Test
  @TestTransaction
  void testExterneRessourceHinzufuegenInvalidUrl() throws Exception {
    KulturObjektDokumentService service = createService();

    KulturObjektDokument kod = createKulturObjektDokument();
    kod.setTeiXML(FileUtils.readFileToString(TEI_FILE_PATH.toFile(), StandardCharsets.UTF_8));
    kod.setId(KOD_ID);

    kulturObjektDokumentRepository.save(kod);
    var kodId = kod.getId();
    KulturObjektDokumentException exception = assertThrows(KulturObjektDokumentException.class,
        () ->
            service.externeRessourceHinzufuegen(
                kodId,
                ExterneRessourceTyp.HSC,
                "ftp://example.org/test",
                testSperre()));

    assertEquals("Die URL 'ftp://example.org/test' ist keine gültige HTTP- oder HTTPS-URL.",
        exception.getMessage());
  }

  @Test
  @TestTransaction
  void testExterneRessourceHinzufuegenDuplicate() throws Exception {

    KulturObjektDokumentService service = createService();

    KulturObjektDokument kod = createKulturObjektDokument();
    kod.setTeiXML(FileUtils.readFileToString(
        TEI_FILE_PATH.toFile(), StandardCharsets.UTF_8));
    kod.setId(KOD_ID);

    kod.setTeiXML(TEIExterneRessourcenCommand
        .addExterneRessource(kod.getTeiXML(),
            new ExterneRessource(ExterneRessourceTyp.HSC, HSC_URL))
        .orElseThrow());

    kulturObjektDokumentRepository.save(kod);
    var kodId = kod.getId();
    KulturObjektDokumentException exception =
        assertThrows(
            KulturObjektDokumentException.class,
            () -> service.externeRessourceHinzufuegen(kodId, ExterneRessourceTyp.HSC, HSC_URL,
                testSperre()));

    String expectedMessage =
        "Externe Ressource mit dem Link '%s' zu diesem Kulturobjekt existiert bereits."
            .formatted(HSC_URL);

    assertEquals(expectedMessage, exception.getMessage());
  }

  @Test
  @TestTransaction
  void testExterneRessourceHinzufuegenSecondResourcePreservesFirst() throws Exception {
    KulturObjektDokumentService service = createService();

    KulturObjektDokument kod = createKulturObjektDokument();
    kod.setTeiXML(FileUtils.readFileToString(TEI_FILE_PATH.toFile(), StandardCharsets.UTF_8));
    kod.setId(KOD_ID);

    kulturObjektDokumentRepository.save(kod);

    service.externeRessourceHinzufuegen(
        kod.getId(),
        ExterneRessourceTyp.HSC,
        HSC_URL,
        testSperre());

    Optional<KulturObjektDokument> updated = service.externeRessourceHinzufuegen(
        kod.getId(),
        ExterneRessourceTyp.ECODICES,
        ECODICES_URL,
        testSperre());

    assertTrue(updated.isPresent());

    String teiXML = updated.get().getTeiXML();

    assertTrue(teiXML.contains("<ref target=\"" + HSC_URL + "\">"
            + ExterneRessourceTyp.HSC.getTeiCaption() + "</ref>"),
        teiXML);

    assertTrue(teiXML.contains("<ref target=\"" + ECODICES_URL + "\">"
            + ExterneRessourceTyp.ECODICES.getTeiCaption() + "</ref>"),
        teiXML);
  }

  @Test
  @TestTransaction
  void testForschungsdokumentationHinzufuegenWithLink() throws Exception {
    KulturObjektDokumentService service = createService();
    KulturObjektDokument kod = persistKulturObjektDokumentForForschungsdokumentation();

    service.forschungsdokumentationHinzufuegen(
        kod.getId(), "  Handschriften\n  digital  ", " https://example.org/research ", testSperre());

    ArgumentCaptor<KulturObjektDokument> kodCaptor =
        ArgumentCaptor.forClass(KulturObjektDokument.class);
    verify(distributedSaveCommandMOCK).execute(
        kodCaptor.capture(), eq(ActivityStreamAction.UPDATE));
    assertTrue(kodCaptor.getValue().getTeiXML().contains(
        "<ref target=\"https://example.org/research\">Handschriften digital</ref>"));
  }

  @Test
  @TestTransaction
  void testForschungsdokumentationHinzufuegenWithoutLink() throws Exception {
    KulturObjektDokumentService service = createService();
    KulturObjektDokument kod = persistKulturObjektDokumentForForschungsdokumentation();

    service.forschungsdokumentationHinzufuegen(
        kod.getId(), "Handschriften digital", null, testSperre());

    ArgumentCaptor<KulturObjektDokument> kodCaptor =
        ArgumentCaptor.forClass(KulturObjektDokument.class);
    verify(distributedSaveCommandMOCK).execute(
        kodCaptor.capture(), eq(ActivityStreamAction.UPDATE));
    String updatedTeiXML = kodCaptor.getValue().getTeiXML();
    assertTrue(updatedTeiXML.contains(
        "<bibl default=\"true\">Handschriften digital</bibl>"));
    assertFalse(updatedTeiXML.contains("target=\"\""));
  }

  @Test
  @TestTransaction
  void testForschungsdokumentationHinzufuegenValidatesInput() throws Exception {
    KulturObjektDokumentService service = createService();
    KulturObjektDokument kod = persistKulturObjektDokumentForForschungsdokumentation();

    KulturObjektDokumentException emptyRessource = assertThrows(
        KulturObjektDokumentException.class,
        () -> service.forschungsdokumentationHinzufuegen(
            kod.getId(), " \n\t ", null, testSperre()));
    assertEquals("Die Ressource darf nicht leer sein.", emptyRessource.getMessage());

    KulturObjektDokumentException invalidLink = assertThrows(
        KulturObjektDokumentException.class,
        () -> service.forschungsdokumentationHinzufuegen(
            kod.getId(), "Forschung", "kein-link", testSperre()));
    assertEquals("Es ist kein Link.", invalidLink.getMessage());
    verify(distributedSaveCommandMOCK, never()).execute(any(), any());
  }

  @Test
  @TestTransaction
  void testForschungsdokumentationHinzufuegenRejectsDuplicateLink() throws Exception {
    KulturObjektDokumentService service = createService();
    KulturObjektDokument kod = persistKulturObjektDokumentForForschungsdokumentation();
    kod.setTeiXML(TEIForschungsdokumentationCommand.addForschungsdokumentation(
        kod.getTeiXML(),
        new Forschungsdokumentation("Bestehende Forschung", "https://example.org/research"))
        .orElseThrow());
    kulturObjektDokumentRepository.saveAndFlush(kod);

    KulturObjektDokumentException exception = assertThrows(
        KulturObjektDokumentException.class,
        () -> service.forschungsdokumentationHinzufuegen(
            kod.getId(), "Andere Forschung", "https://example.org/research", testSperre()));

    assertEquals("Forschungsdokumentation mit diesem Link existiert bereits.",
        exception.getMessage());
    verify(distributedSaveCommandMOCK, never()).execute(any(), any());
  }

  @Test
  @TestTransaction
  void testForschungsdokumentationHinzufuegenRejectsDuplicateRessourceWithoutLink()
      throws Exception {
    KulturObjektDokumentService service = createService();
    KulturObjektDokument kod = persistKulturObjektDokumentForForschungsdokumentation();
    kod.setTeiXML(TEIForschungsdokumentationCommand.addForschungsdokumentation(
        kod.getTeiXML(), new Forschungsdokumentation("Bestehende Forschung", null))
        .orElseThrow());
    kulturObjektDokumentRepository.saveAndFlush(kod);

    KulturObjektDokumentException exception = assertThrows(
        KulturObjektDokumentException.class,
        () -> service.forschungsdokumentationHinzufuegen(
            kod.getId(), "  Bestehende\nForschung ", null, testSperre()));

    assertEquals("Forschungsdokumentation mit dieser Ressource existiert bereits.",
        exception.getMessage());
    verify(distributedSaveCommandMOCK, never()).execute(any(), any());
  }

  @Test
  @TestTransaction
  void testForschungsdokumentationHinzufuegenWrapsTechnicalSaveError() throws Exception {
    KulturObjektDokumentService service = createService();
    KulturObjektDokument kod = persistKulturObjektDokumentForForschungsdokumentation();
    doThrow(new RuntimeException("technical details"))
        .when(distributedSaveCommandMOCK).execute(any(), eq(ActivityStreamAction.UPDATE));

    KulturObjektDokumentException exception = assertThrows(
        KulturObjektDokumentException.class,
        () -> service.forschungsdokumentationHinzufuegen(
            kod.getId(), "Forschung", null, testSperre()));

    assertEquals(
        "Die Forschungsdokumentation konnte nicht gespeichert werden. Bitte versuchen Sie es erneut.",
        exception.getMessage());
    assertEquals("technical details", exception.getCause().getMessage());
  }

  private KulturObjektDokument persistKulturObjektDokumentForForschungsdokumentation()
      throws IOException {
    KulturObjektDokument kod = createKulturObjektDokument();
    kod.setId(KOD_ID);
    kod.setTeiXML(FileUtils.readFileToString(TEI_FILE_PATH.toFile(), StandardCharsets.UTF_8));
    kulturObjektDokumentRepository.save(kod);
    return kod;
  }

  @Test
  @TestTransaction
  void testBuildKulturObjektDokumentViewModelMapsExterneRessourcen() throws Exception {
    KulturObjektDokumentService service = createService();

    KulturObjektDokument kod = createKulturObjektDokument();
    kod.setId(KOD_ID);
    kod.setTeiXML(FileUtils.readFileToString(TEI_FILE_PATH.toFile(), StandardCharsets.UTF_8));

    kulturObjektDokumentRepository.save(kod);

    Optional<KulturObjektDokument> updated = service.externeRessourceHinzufuegen(
        kod.getId(),
        ExterneRessourceTyp.HSC,
        HSC_URL,
        testSperre());

    assertTrue(updated.isPresent());

    Optional<KulturObjektDokumentViewModel> viewModel =
        service.buildKulturObjektDokumentViewModel(kod.getId());

    assertTrue(viewModel.isPresent());
    assertNotNull(viewModel.get().getExterneRessourcen());
    assertEquals(List.of(new ExterneRessource(ExterneRessourceTyp.HSC, HSC_URL)),
        viewModel.get().getExterneRessourcen());
  }

  @Test
  @TestTransaction
  void testBuildKulturObjektDokumentViewModelMapsForschungsdokumentationen() throws Exception {
    KulturObjektDokumentService service = createService();

    KulturObjektDokument kod = createKulturObjektDokument();
    kod.setId(KOD_ID);
    kod.setTeiXML(FileUtils.readFileToString(TEI_FILE_PATH.toFile(), StandardCharsets.UTF_8));
    kod.setTeiXML(TEIForschungsdokumentationCommand.addForschungsdokumentation(
        kod.getTeiXML(), new Forschungsdokumentation("Z Forschung", HSC_URL)).orElseThrow());
    kod.setTeiXML(TEIForschungsdokumentationCommand.addForschungsdokumentation(
        kod.getTeiXML(), new Forschungsdokumentation("A Forschung", null)).orElseThrow());
    kulturObjektDokumentRepository.save(kod);

    Optional<KulturObjektDokumentViewModel> viewModel =
        service.buildKulturObjektDokumentViewModel(kod.getId());

    assertTrue(viewModel.isPresent());
    assertEquals(List.of(
            new Forschungsdokumentation("Z Forschung", HSC_URL),
            new Forschungsdokumentation("A Forschung", null)),
        viewModel.get().getForschungsdokumentationen());
  }

  @Test
  @TestTransaction
  void testBuildKulturObjektDokumentViewModelWithInvalidTeiXml() throws Exception {
    KulturObjektDokumentService service = createService();

    KulturObjektDokument kod = createKulturObjektDokument();
    kod.setId(KOD_ID);

    String expectedSignatur = kod.getGueltigeIdentifikation().getIdent();

    kod.setTeiXML(INVALID_TEI_XML);

    kulturObjektDokumentRepository.save(kod);

    Optional<KulturObjektDokumentViewModel> viewModel =
        service.buildKulturObjektDokumentViewModel(kod.getId());

    assertTrue(viewModel.isPresent());
    assertEquals(expectedSignatur, viewModel.get().getSignatur());
    assertNotNull(viewModel.get().getExterneRessourcen());
    assertTrue(viewModel.get().getExterneRessourcen().isEmpty());
  }

  @Test
  @TestTransaction
  void testExterneRessourceLoeschen() throws Exception {

    KulturObjektDokument kod = createKulturObjektDokument();
    kod.setId("HSP-DELETE-1");
    kod.setTeiXML(FileUtils.readFileToString(
        TEI_FILE_PATH.toFile(), StandardCharsets.UTF_8));

    kod.setTeiXML(TEIExterneRessourcenCommand
        .addExterneRessource(kod.getTeiXML(),
            new ExterneRessource(ExterneRessourceTyp.HSC, HSC_URL))
        .orElseThrow());

    kulturObjektDokumentRepository.save(kod);

    kulturObjektDokumentService.externeRessourceLoeschen(
        kod.getId(), ExterneRessourceTyp.HSC, HSC_URL, testSperre());

    KulturObjektDokument updated =
        kulturObjektDokumentRepository.findById(kod.getId());

    assertTrue(TEIExterneRessourcenCommand
        .readExterneRessourcen(updated.getTeiXML()).isEmpty());
    assertFalse(updated.getTeiXML().contains(
        "<ref target=\"" + HSC_URL + "\">"
            + ExterneRessourceTyp.HSC.getTeiCaption() + "</ref>"));
  }

  @Test
  @TestTransaction
  void testExterneRessourceLoeschenAlreadyAbsent() throws Exception {

    String resourceUrl = "https://handschriftencensus.de/absent";

    KulturObjektDokument kod = createKulturObjektDokument();
    kod.setId("HSP-DELETE-ABSENT");
    kod.setTeiXML(FileUtils.readFileToString(
        TEI_FILE_PATH.toFile(), StandardCharsets.UTF_8));

    kulturObjektDokumentRepository.save(kod);

    String teiXmlBeforeDeletion = kod.getTeiXML();

    kulturObjektDokumentService.externeRessourceLoeschen(
        kod.getId(), ExterneRessourceTyp.HSC, resourceUrl, testSperre());

    KulturObjektDokument updated =
        kulturObjektDokumentRepository.findById(kod.getId());

    assertEquals(teiXmlBeforeDeletion, updated.getTeiXML());
  }

  @Test
  @TestTransaction
  void testExterneRessourceLoeschenNullParameter() {

    KulturObjektDokumentService service = createService();
    String resourceUrl = "https://handschriftencensus.de/123";

    NullPointerException exception = assertThrows(
        NullPointerException.class,
        () -> service.externeRessourceLoeschen(
            null, ExterneRessourceTyp.HSC, HSC_URL, testSperre()));

    assertEquals("kulturObjektDokumentId is required", exception.getMessage());

    exception = assertThrows(
        NullPointerException.class,
        () -> service.externeRessourceLoeschen(
            KOD_ID, null, HSC_URL, testSperre()));

    assertEquals("typ is required", exception.getMessage());

    exception = assertThrows(
        NullPointerException.class,
        () -> service.externeRessourceLoeschen(
            KOD_ID, ExterneRessourceTyp.HSC, null, testSperre()));

    assertEquals("url is required", exception.getMessage());

    exception = assertThrows(
        NullPointerException.class,
        () -> service.externeRessourceLoeschen(
            KOD_ID, ExterneRessourceTyp.HSC, HSC_URL, null));

    assertEquals("sperre is required", exception.getMessage());
  }

  @Test
  @TestTransaction
  void testExterneRessourceLoeschenNoKOD() {

    KulturObjektDokumentService service = createService();

    KulturObjektDokumentException exception = assertThrows(
        KulturObjektDokumentException.class,
        () -> service.externeRessourceLoeschen(
            "INVALID",
            ExterneRessourceTyp.HSC,
            HSC_URL,
            testSperre()));

    assertEquals(
        "Zur Id INVALID wurde kein KulturObjektDokument gefunden!",
        exception.getMessage());
  }

  @Test
  @TestTransaction
  void testExterneRessourceLoeschenWrapsErrorAndLeavesSperreToTheCaller() throws Exception {

    KulturObjektDokument kod = createKulturObjektDokument();
    kod.setId("HSP-DELETE-INVALID-TEI");
    kod.setTeiXML(INVALID_TEI_XML);

    kulturObjektDokumentRepository.save(kod);
    var kodId = kod.getId();

    KulturObjektDokumentException exception = assertThrows(
        KulturObjektDokumentException.class,
        () -> kulturObjektDokumentService.externeRessourceLoeschen(
            kodId,
            ExterneRessourceTyp.HSC,
            HSC_URL,
            testSperre()));

    assertTrue(exception.getMessage().contains(HSC_URL));
    assertTrue(exception.getMessage().contains(kod.getId()));
    assertNotNull(exception.getCause());
  }

  @Test
  @TestTransaction
  void testForschungsdokumentationLoeschenWithLink() throws Exception {
    KulturObjektDokumentService service = createService();
    KulturObjektDokument kod = persistKulturObjektDokumentForForschungsdokumentation();
    kod.setTeiXML(TEIForschungsdokumentationCommand
        .addForschungsdokumentation(kod.getTeiXML(),
            new Forschungsdokumentation("Forschung", "https://example.org/research"))
        .orElseThrow());
    kulturObjektDokumentRepository.saveAndFlush(kod);

    service.forschungsdokumentationLoeschen(
        kod.getId(), "Forschung", "https://example.org/research", testSperre());

    KulturObjektDokument updated = kulturObjektDokumentRepository.findById(kod.getId());
    assertTrue(TEIForschungsdokumentationCommand
        .readForschungsdokumentationen(updated.getTeiXML()).isEmpty());
  }

  @Test
  @TestTransaction
  void testForschungsdokumentationLoeschenWithoutLink() throws Exception {
    KulturObjektDokumentService service = createService();
    KulturObjektDokument kod = persistKulturObjektDokumentForForschungsdokumentation();
    kod.setTeiXML(TEIForschungsdokumentationCommand
        .addForschungsdokumentation(kod.getTeiXML(),
            new Forschungsdokumentation("Forschung", null))
        .orElseThrow());
    kulturObjektDokumentRepository.saveAndFlush(kod);

    service.forschungsdokumentationLoeschen(kod.getId(), "Forschung", null, testSperre());

    KulturObjektDokument updated = kulturObjektDokumentRepository.findById(kod.getId());
    assertTrue(TEIForschungsdokumentationCommand
        .readForschungsdokumentationen(updated.getTeiXML()).isEmpty());
  }

  @Test
  @TestTransaction
  void testForschungsdokumentationLoeschenAlreadyAbsent() throws Exception {
    KulturObjektDokumentService service = createService();
    KulturObjektDokument kod = persistKulturObjektDokumentForForschungsdokumentation();
    String teiXmlBeforeDeletion = kod.getTeiXML();

    service.forschungsdokumentationLoeschen(
        kod.getId(), "Nicht vorhanden", null, testSperre());

    KulturObjektDokument updated = kulturObjektDokumentRepository.findById(kod.getId());
    assertEquals(teiXmlBeforeDeletion, updated.getTeiXML());
    verify(distributedSaveCommandMOCK, never()).execute(any(), any());
  }

  @Test
  @TestTransaction
  void testForschungsdokumentationLoeschenNullParameter() {
    KulturObjektDokumentService service = createService();
    NullPointerException exception = assertThrows(
        NullPointerException.class,
        () -> service.forschungsdokumentationLoeschen(
            null, "Forschung", null, testSperre()));
    assertEquals("kulturObjektDokumentId is required", exception.getMessage());

    exception = assertThrows(
        NullPointerException.class,
        () -> service.forschungsdokumentationLoeschen(
            KOD_ID, null, null, testSperre()));
    assertEquals("ressource is required", exception.getMessage());

    exception = assertThrows(
        NullPointerException.class,
        () -> service.forschungsdokumentationLoeschen(
            KOD_ID, "Forschung", null, null));
    assertEquals("sperre is required", exception.getMessage());
  }
}
