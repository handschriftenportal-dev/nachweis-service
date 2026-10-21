package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung;

import static de.staatsbibliothek.berlin.hsp.nachweis.TestUtils.BeschreibungTestUtils.assertBeschreibungListDTOEqualsBeschreibung;
import static de.staatsbibliothek.berlin.hsp.nachweis.TestUtils.BeschreibungTestUtils.createBeschreibung;
import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.HSP_UUI_Factory;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KulturObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.mapper.tei.kod.TEIKODAttributsReferenzCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.kod.TEIKulturObjektDokumentCommand;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.minimaldaten.IdentifikationRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.papierkorb.PapierkorbBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.PublikationException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.PublikationsService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege.SchemaPflegeDatei;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.SperreRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SolrUebernahmeException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJobBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJobService;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.SchemaPflegeDateiServicePort;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.NormdatenReferenzRepositoryAdapter;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungListDbView;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import de.staatsbibliothek.berlin.javaee.authentication.service.AuthenticationService;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.Change;
import org.tei_c.ns._1.TEI;

@Slf4j
@QuarkusTest
@TestInstance(Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
class BeschreibungsServiceTest {

  public static final String ENTITY_ID = "entityId";
  public static final String GUELTIGE_SIGNATURE = "a-007-777";
  private static final Identifikation IDENTIFIKATION =
      new IdentifikationBuilder()
          .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
          .withIdent("1-1-1")
          .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
          .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
          .withId(UUID.randomUUID().toString())
          .build();
  private static final Logger logger = LoggerFactory.getLogger(
      BeschreibungsServiceTest.class);
  private static final String HTTP_RESOLVER_SPK_DE_PURL =
      "http://resolver.sbb.spk-berlin.de/HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001";
  static Bearbeiter BEARBEITER = new Bearbeiter("1", "Unbekannter Bearbeiter");
  static Bearbeiter BEARBEITER2 = new Bearbeiter("2", "auch unbekannt");
  @Inject
  BeschreibungsRepository beschreibungsRepository;
  @Inject
  KulturObjektDokumentRepository kulturObjektDokumentRepository;
  @Inject
  IdentifikationRepository identifikationRepository;
  @Inject
  DokumentSperreBoundary dokumentSperreBoundary;
  @Inject
  BearbeiterRepository bearbeiterRepository;
  @Inject
  SperreRepository sperreRepository;
  @Inject
  TeiXmlValidationBoundary teiXmlValidationBoundaryMock;
  @Inject
  BearbeiterBoundary bearbeiterBoundary;
  @Inject
  KulturObjektDokumentBoundary kulturObjektDokumentBoundary;


  BeschreibungsRepository beschreibungsRepositoryMOCK;
  IdentifikationRepository identifikationRepositoryMOCK;
  KulturObjektDokumentRepository kulturObjektDokumentRepositoryMOCK;
  SuchDokumentService suchDokumentServiceMOCK;
  DokumentSperreBoundary dokumentSperreBoundaryMOCK;
  PapierkorbBoundary papierkorbBoundaryMOCK;
  IndexJobBoundary indexServiceMock;
  BeschreibungsRechte beschreibungsRechteMOCK;
  SchemaPflegeDateiServicePort schemaPflegeDateiServicePortMock;
  BeschreibungsSynchronizer beschreibungsSynchronizerMOCK;
  KafkaIndexingProducer kafkaIndexingProducerMOCK;
  @Inject
  NormdatenReferenzRepositoryAdapter normdatenReferenzRepositoryAdapter;

  @BeforeAll
  public static void beforeAll() {
    BearbeiterService bearbeiterBoundary = mock(BearbeiterService.class);

    AuthenticationService authenticationService = mock(
        AuthenticationService.class);
    bearbeiterBoundary.setAuthenticationService(authenticationService);

    AuthenticationRepository authenticationRepository = mock(
        AuthenticationRepository.class);
    bearbeiterBoundary.setAuthenticationRepository(authenticationRepository);

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(BEARBEITER);
    when(bearbeiterBoundary.getUnbekannterBearbeiter()).thenReturn(BEARBEITER);
    when(bearbeiterBoundary.findBearbeiterWithName(BEARBEITER.getId())).thenReturn(BEARBEITER);
    when(bearbeiterBoundary.findBearbeiterWithName(BEARBEITER2.getId())).thenReturn(BEARBEITER2);
    QuarkusMock.installMockForType(bearbeiterBoundary, BearbeiterBoundary.class);
  }

  @BeforeEach
  @Transactional(TxType.REQUIRES_NEW)
  void beforeEach() {
    beschreibungsRepositoryMOCK = mock(BeschreibungsRepository.class);
    identifikationRepositoryMOCK = mock(IdentifikationRepository.class);
    kulturObjektDokumentRepositoryMOCK = mock(
        KulturObjektDokumentRepository.class);
    suchDokumentServiceMOCK = mock(SuchDokumentService.class);
    dokumentSperreBoundaryMOCK = mock(DokumentSperreBoundary.class);
    indexServiceMock = mock(IndexJobService.class);
    papierkorbBoundaryMOCK = mock(PapierkorbBoundary.class);
    beschreibungsRechteMOCK = mock(BeschreibungsRechte.class);
    schemaPflegeDateiServicePortMock = mock(SchemaPflegeDateiServicePort.class);
    beschreibungsSynchronizerMOCK = mock(BeschreibungsSynchronizer.class);
    kafkaIndexingProducerMOCK = mock(KafkaIndexingProducer.class);
    bearbeiterRepository.saveAndFlush(BEARBEITER);
    bearbeiterRepository.saveAndFlush(BEARBEITER2);
  }

  @AfterEach
  @Transactional(TxType.REQUIRES_NEW)
  void afterEach() {
    sperreRepository.listAll(false)
        .forEach(s -> sperreRepository.deleteByIdAndFlush(s.getId()));
    bearbeiterRepository.deleteByIdAndFlush(BEARBEITER.getId());
  }

  @Test
  @TestTransaction
  void testGetSignatureByBeschreibungID() {
    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepositoryMOCK,
            identifikationRepository,
            kulturObjektDokumentRepositoryMOCK,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    Identifikation identifikation =
        new IdentifikationBuilder()
            .withId("007")
            .withIdent(GUELTIGE_SIGNATURE)
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();

    BeschreibungsKomponenteKopf beschreibungsKomponenteKopf =
        new BeschreibungsKomponenteKopfBuilder().withId("123").build();

    Beschreibung beschreibung =
        new BeschreibungsBuilder(ENTITY_ID, "KODID", identifikation)
            .addBeschreibungsKomponente(beschreibungsKomponenteKopf)
            .build();

    assertNull(beschreibungsService.getSignatureForBeschreibungId(ENTITY_ID));

    beschreibungsRepository.save(beschreibung);

    SignatureValue actual = beschreibungsService.getSignatureForBeschreibungId(
        ENTITY_ID);
    assertNotNull(actual);

    assertEquals(GUELTIGE_SIGNATURE, actual.getSignature());
  }

  @Test
  void findTeiSnapshotZipDelegatesToPublikationsService() {
    BeschreibungsService beschreibungsService = new BeschreibungsService();
    PublikationsService publikationsServiceMock = mock(PublikationsService.class);
    beschreibungsService.setPublikationsService(publikationsServiceMock);

    byte[] zip = {1, 2, 3};
    when(publikationsServiceMock.findTeiSnapshotZip("id-1")).thenReturn(Optional.of(zip));

    assertEquals(Optional.of(zip), beschreibungsService.findTeiSnapshotZip("id-1"));
  }

  @Test
  void findTeiSnapshotZipReturnsEmptyWhenPublikationsServiceMissing() {
    assertEquals(Optional.empty(), new BeschreibungsService().findTeiSnapshotZip("id-1"));
  }

  @Test
  @TestTransaction
  void testGetAllSignaturesWithBeschreibungIDs() {
    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepositoryMOCK,
            identifikationRepository,
            kulturObjektDokumentRepositoryMOCK,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    Identifikation identifikation =
        new IdentifikationBuilder()
            .withId("007")
            .withIdent(GUELTIGE_SIGNATURE)
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();

    BeschreibungsKomponenteKopf beschreibungsKomponenteKopf =
        new BeschreibungsKomponenteKopfBuilder().withId("123").build();

    Beschreibung beschreibung =
        new BeschreibungsBuilder(ENTITY_ID, "KODID", identifikation)
            .addBeschreibungsKomponente(beschreibungsKomponenteKopf)
            .build();

    assertTrue(
        beschreibungsService.getAllSignaturesWithBeschreibungIDs().isEmpty());

    beschreibungsRepository.save(beschreibung);

    Map<String, SignatureValue> actual = beschreibungsService.getAllSignaturesWithBeschreibungIDs();
    assertNotNull(actual);

    assertFalse(actual.isEmpty());

    assertEquals(GUELTIGE_SIGNATURE, actual.get(ENTITY_ID).getSignature());
  }

  @Test
  @TestTransaction
  void testUpdateBeschreibungMitXMLWithDatabase()
      throws IOException, BeschreibungsException, DokumentSperreException {
    final String id = "mss_36-23-aug-2f_tei-msDesc_Westphal";
    final KulturObjektDokument kod = createKulturObjektDokument();
    Path teiFilePath =
        Paths.get(
            "../domainmodel-tei-mapper/src", "test", "resources", "tei",
            "tei-msDesc_Westphal.xml");

    String xml = Files.readString(teiFilePath);
    Beschreibung code166 =
        new Beschreibung.BeschreibungsBuilder(id, kod.getId(),
            kod.getGueltigeIdentifikation())
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .withBearbeiterIds(BEARBEITER)
            .build();

    assertNull(beschreibungsRepository.findById(id));

    kulturObjektDokumentRepository.save(kod);
    var b = beschreibungsRepository.save(code166);
    var initialDate = b.getAenderungsDatum();
    normdatenReferenzRepositoryAdapter.saveAndFlush(
        new NormdatenReferenz("la", "la", "GND_ID_la"));
    normdatenReferenzRepositoryAdapter.saveAndFlush(
        new NormdatenReferenz("de", "de", "GND_ID_de"));

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepository,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepositoryMOCK,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundaryMOCK,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    beschreibungsService.updateBeschreibungMitXML(id, xml, BEARBEITER.getId(), true, Locale.GERMAN);

    Beschreibung beschreibung = beschreibungsRepository.findById(id);

    assertNotNull(beschreibung);
    assertNotNull(beschreibung.getTeiXML());
    assertNotEquals(initialDate, beschreibung.getAenderungsDatum());
  }

  @Test
  void testUpdateBeschreibungMitXML_throwsWhenSperreNotAcquired() throws Exception {
    final String id = "mss_36-23-aug-2f_tei-msDesc_Westphal";
    Path teiFilePath = Paths.get(
        "../domainmodel-tei-mapper/src", "test", "resources", "tei",
        "tei-msDesc_Westphal.xml");
    String xml = Files.readString(teiFilePath);

    Beschreibung code166 =
        new Beschreibung.BeschreibungsBuilder(id, "1", IDENTIFIKATION)
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .withBearbeiterIds(BEARBEITER)
            .build();

    when(beschreibungsRepositoryMOCK.findById(id)).thenReturn(code166);
    doThrow(new DokumentSperreException("locked"))
        .when(dokumentSperreBoundaryMOCK)
        .acquireSperre(any(), any(), any(), any(Beschreibung.class));

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepositoryMOCK,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepositoryMOCK,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundaryMOCK,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    assertThrows(
        DokumentSperreException.class,
        () -> beschreibungsService.updateBeschreibungMitXML(id, xml, BEARBEITER.getId(),
            true, Locale.ENGLISH));
  }

  @Test
  void testUpdateBeschreibungMitXML_throwsWhenNotFound() throws Exception {
    Path teiFilePath = Paths.get(
        "../domainmodel-tei-mapper/src", "test", "resources", "tei",
        "tei-msDesc_Westphal.xml");
    String xml = Files.readString(teiFilePath);

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepositoryMOCK,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepositoryMOCK,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundaryMOCK,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    assertThrows(
        NullPointerException.class,
        () -> beschreibungsService.updateBeschreibungMitXML("notFound", xml, BEARBEITER.getId(),
            true, Locale.ENGLISH));
  }

  @Test
  void testUpdateBeschreibungMitXML_throwsWhenNoBearbeiterRights() throws Exception {
    final String id = "mss_36-23-aug-2f_tei-msDesc_Westphal";
    Path teiFilePath = Paths.get(
        "../domainmodel-tei-mapper/src", "test", "resources", "tei",
        "tei-msDesc_Westphal.xml");
    String xml = Files.readString(teiFilePath);

    Beschreibung code166 =
        new Beschreibung.BeschreibungsBuilder(id, "1", IDENTIFIKATION)
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .build();

    when(beschreibungsRepositoryMOCK.findById(id)).thenReturn(code166);

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepositoryMOCK,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepositoryMOCK,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundaryMOCK,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    Throwable ex = assertThrows(
        BeschreibungsException.class,
        () -> beschreibungsService.updateBeschreibungMitXML(id, xml, BEARBEITER.getId(),
            true, Locale.ENGLISH));
    assertEquals(
        "User 1 does not have permission for description mss_36-23-aug-2f_tei-msDesc_Westphal.",
        ex.getMessage());
  }

  @Test
  void testUpdateBeschreibungMitXML_throwsWhenIdMismatch() throws Exception {
    final String id = "mss_36-23-aug-2f_tei-msDesc_Westphal";
    Path teiFilePath = Paths.get(
        "../domainmodel-tei-mapper/src", "test", "resources", "tei",
        "tei-msDesc_Westphal.xml");
    String xml = Files.readString(teiFilePath);

    Beschreibung code166 =
        new Beschreibung.BeschreibungsBuilder(id, "1", IDENTIFIKATION)
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .withBearbeiterIds(BEARBEITER)
            .build();

    when(beschreibungsRepositoryMOCK.findById("wrongId")).thenReturn(code166);
    when(beschreibungsRepositoryMOCK.findByIdOptional("wrongId")).thenReturn(Optional.of(code166));

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepositoryMOCK,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepositoryMOCK,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundaryMOCK,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    Throwable ex = assertThrows(
        BeschreibungsException.class,
        () -> beschreibungsService.updateBeschreibungMitXML("wrongId", xml, BEARBEITER.getId(),
            true, Locale.ENGLISH));
    assertEquals(
        "The id of the description wrongId does not match the XML.",
        ex.getMessage());
  }

  @Test
  void testUpdateBeschreibungMitXML_success() throws Exception {
    final String id = "mss_36-23-aug-2f_tei-msDesc_Westphal";
    Path teiFilePath = Paths.get(
        "../domainmodel-tei-mapper/src", "test", "resources", "tei",
        "tei-msDesc_Westphal.xml");
    String xml = Files.readString(teiFilePath);

    Beschreibung code166 =
        new Beschreibung.BeschreibungsBuilder(id, "1", IDENTIFIKATION)
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .withBearbeiterIds(BEARBEITER)
            .build();

    when(beschreibungsRepositoryMOCK.findById(id)).thenReturn(code166);
    when(beschreibungsRepositoryMOCK.findByIdOptional(id)).thenReturn(Optional.of(code166));

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepositoryMOCK,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepositoryMOCK,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundaryMOCK,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    beschreibungsService.updateBeschreibungMitXML(id, xml, BEARBEITER.getId(), true, Locale.GERMAN);

    verify(beschreibungsRepositoryMOCK).save(any());
  }

  @Test
  void testUpdateBeschreibungMitXML_throwsOnSolrError() throws Exception {
    final String id = "mss_36-23-aug-2f_tei-msDesc_Westphal";
    Path teiFilePath = Paths.get(
        "../domainmodel-tei-mapper/src", "test", "resources", "tei",
        "tei-msDesc_Westphal.xml");
    String xml = Files.readString(teiFilePath);

    Beschreibung code166 =
        new Beschreibung.BeschreibungsBuilder(id, "1", IDENTIFIKATION)
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .withBearbeiterIds(BEARBEITER)
            .build();

    when(beschreibungsRepositoryMOCK.findById(id)).thenReturn(code166);
    when(beschreibungsRepositoryMOCK.findByIdOptional(id)).thenReturn(Optional.of(code166));
    doThrow(new SolrUebernahmeException("Test"))
        .when(suchDokumentServiceMOCK)
        .beschreibungUebernehmen(any(Beschreibung.class));

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepositoryMOCK,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepositoryMOCK,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundaryMOCK,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    Throwable ex = assertThrows(
        BeschreibungsException.class,
        () -> beschreibungsService.updateBeschreibungMitXML(id, xml, BEARBEITER.getId(),
            true, Locale.ENGLISH));
    assertEquals(
        "An error occurred while saving the description: Test",
        ex.getMessage());

    verify(beschreibungsRepositoryMOCK).save(any());
  }

  @Test
  @TestTransaction
  void testUpdateBeschreibungMitXML_succeedsSperreSameUser()
      throws Exception {
    final String id = "mss_36-23-aug-2f_tei-msDesc_Westphal";
    final KulturObjektDokument kod = createKulturObjektDokument();
    Path teiFilePath =
        Paths.get(
            "../domainmodel-tei-mapper/src", "test", "resources", "tei",
            "tei-msDesc_Westphal.xml");

    String xml = Files.readString(teiFilePath);
    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder(id, kod.getId(),
            kod.getGueltigeIdentifikation())
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .withBearbeiterIds(BEARBEITER)
            .build();

    kulturObjektDokumentRepository.save(kod);
    beschreibungsRepository.save(beschreibung);
    normdatenReferenzRepositoryAdapter.saveAndFlush(
        new NormdatenReferenz("la", "la", "GND_ID_la"));
    normdatenReferenzRepositoryAdapter.saveAndFlush(
        new NormdatenReferenz("de", "de", "GND_ID_de"));

    // Use real dokumentSperreBoundary (not mock) so the Sperre is persisted in the DB
    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepository,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepositoryMOCK,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    Sperre sperre = beschreibungsService.beschreibungSperren(BEARBEITER, beschreibung.getId(),
        SperreTyp.MANUAL, "BeschreibungBearbeiten");
    assertNotNull(sperre);
    assertEquals(SperreTyp.MANUAL, sperre.getSperreTyp());

    Optional<Sperre> foundSperre =
        beschreibungsService.findSperreForBeschreibung(beschreibung.getId());
    assertTrue(foundSperre.isPresent());

    beschreibungsService.updateBeschreibungMitXML(id, xml, BEARBEITER.getId(), true, Locale.GERMAN);

    Beschreibung updated = beschreibungsRepository.findById(id);

    assertNotNull(updated);
    assertNotNull(updated.getTeiXML());
    assertTrue(updated.getTeiXML().contains("Cod. Guelf. 36.23 Aug."));

    Optional<Sperre> sperreAfterUpdate =
        beschreibungsService.findSperreForBeschreibung(beschreibung.getId());
    assertTrue(sperreAfterUpdate.isPresent());
  }

  @Test
  @TestTransaction
  void testUpdateBeschreibungMitXML_failsSperreDifferentBearbeiter()
      throws Exception {
    final String id = "mss_36-23-aug-2f_tei-msDesc_Westphal";
    final KulturObjektDokument kod = createKulturObjektDokument();
    Path teiFilePath =
        Paths.get(
            "../domainmodel-tei-mapper/src", "test", "resources", "tei",
            "tei-msDesc_Westphal.xml");

    String xml = Files.readString(teiFilePath);
    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder(id, kod.getId(),
            kod.getGueltigeIdentifikation())
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .withBearbeiterIds(BEARBEITER)
            .withBearbeiterIds(BEARBEITER2)
            .build();

    kulturObjektDokumentRepository.save(kod);
    beschreibungsRepository.save(beschreibung);
    normdatenReferenzRepositoryAdapter.saveAndFlush(
        new NormdatenReferenz("la", "la", "GND_ID_la"));
    normdatenReferenzRepositoryAdapter.saveAndFlush(
        new NormdatenReferenz("de", "de", "GND_ID_de"));

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepository,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepositoryMOCK,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    Sperre sperre = beschreibungsService.beschreibungSperren(BEARBEITER2,
        beschreibung.getId(), SperreTyp.MANUAL, "BeschreibungBearbeiten");
    assertNotNull(sperre);
    assertEquals(SperreTyp.MANUAL, sperre.getSperreTyp());

    Optional<Sperre> foundSperre =
        beschreibungsService.findSperreForBeschreibung(beschreibung.getId());
    assertTrue(foundSperre.isPresent());

    assertThrows(
        SperreAlreadyExistException.class,
        () -> beschreibungsService.updateBeschreibungMitXML(id, xml, BEARBEITER.getId(), true,
            Locale.GERMAN));
  }

  @Test
  @TestTransaction
  void buildBeschreibungsViewModel() {

    KulturObjektDokument kod = createKulturObjektDokument();
    Beschreibung beschreibung = createBeschreibung(kod);
    Bearbeiter bearbeiter = createBearbeiter();

    kulturObjektDokumentRepository.save(kod);
    beschreibungsRepository.save(beschreibung);

    Optional<BeschreibungsViewModel> beschreibungsViewModelOptional = getBeschreibungsViewModel(
        beschreibung);

    assertTrue(beschreibungsViewModelOptional.isPresent());
    beschreibungsViewModelOptional.get().getBearbeiterNames()
        .add(bearbeiter.getBearbeitername());

    BeschreibungsViewModel beschreibungsViewModel = beschreibungsViewModelOptional.get();

    assertEquals("Mscr.Dresd.A.111", beschreibungsViewModel.getSignatur());
    assertEquals("Mscr.Dresd.A.111", beschreibungsViewModel.getKodSignatur());
    assertEquals("Flavius Josephus, de bello Judaico",
        beschreibungsViewModel.getTitel());
    assertNotNull(beschreibungsViewModel.getPublikation());

    assertEquals(1, beschreibungsViewModel.getAutoren().size());

    beschreibungsViewModel.getAutoren().stream()
        .findFirst()
        .ifPresent(autor -> assertEquals("Tester", autor.preferredName()));

    assertEquals(1, beschreibungsViewModel.getBearbeiterNames().size());

    assertEquals("nachname, vorname", bearbeiter.getFullName());

    assertTrue(beschreibungsViewModel.getAusGedrucktemKatalog());

    assertEquals("https://resolver.url/HSP-123",
        beschreibungsViewModel.getHspPurl());

    assertNotNull(beschreibungsViewModel.getExterneLinks());
    assertEquals(1, beschreibungsViewModel.getExterneLinks().size());
    assertTrue(
        beschreibungsViewModel.getExterneLinks().stream()
            .anyMatch("https://doi.org/HSP-123"::equals));
  }

  private Optional<BeschreibungsViewModel> getBeschreibungsViewModel(Beschreibung beschreibung) {
    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepository,
            identifikationRepository,
            kulturObjektDokumentRepository,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    return beschreibungsService.buildBeschreibungsViewModel(beschreibung.getId());
  }

  @Test
  void testValidateXML() throws IOException {
    Path teiFilePath = Paths.get("src", "test", "resources",
        "tei-msDesc_Lesser-wthout-BOM44.xml");

    String beschreibungTEI = Files.readString(teiFilePath);

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepositoryMOCK,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepositoryMOCK,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder("1", "KODID", IDENTIFIKATION)
            .withTEIXml(beschreibungTEI)
            .build();

    ValidationResult validationResult =
        beschreibungsService.validateXML(beschreibung.getTeiXML(), null, "de");

    assertTrue(validationResult.isValid());
  }

  @Test
  void testMinimalBeschreibungErstellen() throws BeschreibungsException {

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepositoryMOCK,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepositoryMOCK,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    Identifikation identifikation =
        new Identifikation.IdentifikationBuilder()
            .withId(UUID.randomUUID().toString())
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(new NormdatenReferenz("12", "SBB", ""))
            .withAufbewahrungsOrt(new NormdatenReferenz("13", "Berlin", ""))
            .withIdent("Ms 1108")
            .build();

    KulturObjektDokument kod =
        new KulturObjektDokumentBuilder().withGueltigerIdentifikation(
            identifikation).build();

    Set<NormdatenReferenz> autoren = new HashSet<>();
    autoren.add(new NormdatenReferenz("1", "Konrad", "Konrad3X-221", "Person"));

    NormdatenReferenz sprache = createNormdatenReferenzGerman();

    Bearbeiter bearbeiter =
        Bearbeiter.newBuilder()
            .withBearbeitername("b-ml123")
            .withVorname("test")
            .withNachname("test")
            .withEmail("test@test.de")
            .withRolle("Redakteur")
            .build();
    Bearbeiter bearbeiter2 =
        Bearbeiter.newBuilder()
            .withBearbeitername("b-ml123456")
            .withVorname("test1")
            .withNachname("test1")
            .withEmail("test1@test.de")
            .withRolle("Redakteur")
            .build();

    Beschreibung minimal =
        beschreibungsService.minimalBeschreibungErstellen(
            bearbeiter,
            "KODID",
            sprache,
            kod,
            autoren);

    assertNotNull(minimal);
    assertNotNull(minimal.getId());
    assertEquals(VerwaltungsTyp.INTERN, minimal.getVerwaltungsTyp());
    assertNotNull(minimal.getErstellungsDatum());
    assertNotNull(minimal.getAenderungsDatum());
    assertNotNull(minimal.getBeschreibungsSprache());
    assertNotNull(minimal.getBearbeiterIDs());
    assertEquals(sprache.getId(), minimal.getBeschreibungsSprache().getId());
    assertEquals(sprache.getGndID(),
        minimal.getBeschreibungsSprache().getGndID());
    assertEquals(sprache.getName(),
        minimal.getBeschreibungsSprache().getName());
    assertEquals(sprache.getTypeName(),
        minimal.getBeschreibungsSprache().getTypeName());
    assertNotNull(minimal.getBeschreibungsSprache().getVarianterName());
    assertEquals(
        sprache.getVarianterName().size(),
        minimal.getBeschreibungsSprache().getVarianterName().size());
    assertNotNull(minimal.getBeschreibungsSprache().getIdentifikator());
    assertEquals(
        sprache.getIdentifikator().size(),
        minimal.getBeschreibungsSprache().getIdentifikator().size());
    assertTrue(minimal.getGueltigeIdentifikation().isPresent());
    assertEquals(1, minimal.getBeschreibungsStruktur().size());
    assertEquals(DokumentObjektTyp.HSP_DESCRIPTION,
        minimal.getDokumentObjektTyp());

    minimal
        .getBeschreibungsStruktur()
        .forEach(
            kopf -> {
              assertEquals(1, kopf.getIdentifikationen().size());
              assertEquals("", ((BeschreibungsKomponenteKopf) kopf).getTitel());

              assertTrue(
                  kopf.getIdentifikationen().stream()
                      .anyMatch(
                          i -> i.getIdentTyp().equals(IdentTyp.HSP_ALT_ID)));
            });

    assertEquals(1, minimal.getAutoren().size());

    Set<String> list = new HashSet<>();
    list.add(bearbeiter.getId());

    minimal.setBearbeiterIDs(list);
    Set<String> strings = minimal.getBearbeiterIDs();
    assertEquals(1, strings.size());

    list.add(bearbeiter2.getId());
    assertEquals(2, strings.size());

    assertEquals(Set.of("b-ml123", "b-ml123456"), strings);
    assertTrue(minimal.getBearbeiterIDs().contains("b-ml123"));
    assertTrue(minimal.getBearbeiterIDs().contains("b-ml123456"));
  }

  @Test
  void testBeschreibungErstellen() throws Exception {

    Identifikation identifikation =
        new Identifikation.IdentifikationBuilder()
            .withId(UUID.randomUUID().toString())
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(new NormdatenReferenz("12", "SBB", ""))
            .withAufbewahrungsOrt(new NormdatenReferenz("13", "Berlin", ""))
            .withIdent("Ms 1108")
            .build();

    KulturObjektDokument kod =
        new KulturObjektDokumentBuilder()
            .withId(TEIValues.UUID_PREFIX + UUID.randomUUID())
            .withGueltigerIdentifikation(identifikation)
            .withRegistrierungsDatum(LocalDateTime.now())
            .withTEIXml(Files.readString(
                Paths.get("src", "test", "resources", "kodinitial.xml")))
            .build();

    NormdatenReferenz sprache = createSprache();

    Set<NormdatenReferenz> autoren = new HashSet<>();
    autoren.add(new NormdatenReferenz("1", "Konrad", "Konrad3X-221", "Person"));

    BeschreibungsRepository beschreibungsRepositoryMock = mock(
        BeschreibungsRepository.class);
    KulturObjektDokumentBoundary kulturObjektDokumentBoundaryMock =
        mock(KulturObjektDokumentBoundary.class);
    KulturObjektDokumentRepository kulturObjektDokumentRepositoryMock =
        mock(KulturObjektDokumentRepository.class);
    SuchDokumentService suchDokumentServiceMock = mock(
        SuchDokumentService.class);

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepositoryMock,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepositoryMock,
            suchDokumentServiceMock,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    when(kulturObjektDokumentRepositoryMock.findById(anyString())).thenReturn(kod);

    String templateXml;

    try (InputStream resourceAsStream =
        getClass().getResourceAsStream("/aktuelle_beschreibungstemplate.xml")) {

      assertNotNull(resourceAsStream,
          "Test template 'aktuelle_beschreibungstemplate.xml' not found");

      templateXml = new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    SchemaPflegeDatei templateMock = mock(SchemaPflegeDatei.class);

    when(templateMock.getDatei()).thenReturn(templateXml);
    when(templateMock.getVersion()).thenReturn("4.7.5");

    when(schemaPflegeDateiServicePortMock.findById(anyString()))
        .thenReturn(templateMock);

    Beschreibung beschreibung =
        beschreibungsService.beschreibungErstellen(
            new Bearbeiter("1", "Unbekannter Tester"),
            kod.getId(),
            sprache,
            autoren,
            kulturObjektDokumentBoundaryMock);

    assertNotNull(beschreibung);
    assertNotNull(beschreibung.getId());
    assertEquals(VerwaltungsTyp.INTERN, beschreibung.getVerwaltungsTyp());
    assertNotNull(beschreibung.getErstellungsDatum());
    assertNotNull(beschreibung.getAenderungsDatum());
    assertNotNull(beschreibung.getBeschreibungsSprache());
    assertEquals(sprache.getId(),
        beschreibung.getBeschreibungsSprache().getId());
    assertEquals(sprache.getGndID(),
        beschreibung.getBeschreibungsSprache().getGndID());
    assertEquals(sprache.getName(),
        beschreibung.getBeschreibungsSprache().getName());
    assertEquals(sprache.getTypeName(),
        beschreibung.getBeschreibungsSprache().getTypeName());
    assertNotNull(beschreibung.getBeschreibungsSprache().getVarianterName());
    assertEquals(
        sprache.getVarianterName().size(),
        beschreibung.getBeschreibungsSprache().getVarianterName().size());
    assertNotNull(beschreibung.getBeschreibungsSprache().getIdentifikator());
    assertEquals(
        sprache.getIdentifikator().size(),
        beschreibung.getBeschreibungsSprache().getIdentifikator().size());
    assertTrue(beschreibung.getGueltigeIdentifikation().isPresent());
    assertEquals(1, beschreibung.getBeschreibungsStruktur().size());
    beschreibung
        .getBeschreibungsStruktur()
        .forEach(
            kopf -> {
              assertEquals(1, kopf.getIdentifikationen().size());
              assertEquals("", ((BeschreibungsKomponenteKopf) kopf).getTitel());
            });

    assertEquals(1, beschreibung.getAutoren().size());
    assertEquals(1, beschreibung.getBearbeiterIDs().size());

    verify(beschreibungsRepositoryMock, times(1)).save(beschreibung);
    verify(suchDokumentServiceMock, times(1)).beschreibungUebernehmen(
        beschreibung);
    verify(kulturObjektDokumentBoundaryMock, times(1))
        .saveKulturObjektDokument(kod, ActivityStreamAction.ADD);
  }

  @Test
  @TestTransaction
  void testSperren() throws Exception {
    KulturObjektDokument kod = createKulturObjektDokument();
    Beschreibung beschreibung = createBeschreibung(kod);

    kulturObjektDokumentRepository.save(kod);
    beschreibungsRepository.save(beschreibung);
    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepository,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepository,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    Sperre sperre_1 =
        beschreibungsService.beschreibungSperren(
            new Bearbeiter("1", "Unbekannter Tester"), beschreibung.getId(),
            SperreTyp.MANUAL, "BeschreibungBearbeiten");
    assertNotNull(sperre_1);
    assertNotNull(sperre_1.getId());
    assertEquals(BEARBEITER, sperre_1.getBearbeiter());
    assertEquals(SperreTyp.MANUAL, sperre_1.getSperreTyp());
    assertEquals("BeschreibungBearbeiten", sperre_1.getSperreGrund());
    assertNotNull(sperre_1.getSperreEintraege());
    assertTrue(
        sperre_1.getSperreEintraege().stream()
            .anyMatch(
                se ->
                    beschreibung.getId().equals(se.getTargetId())
                        && SperreDokumentTyp.BESCHREIBUNG
                        == se.getTargetType()));

    Optional<Sperre> sperre_2 =
        beschreibungsService.findSperreForBeschreibung(beschreibung.getId());
    assertTrue(sperre_2.isPresent());
    assertEquals(sperre_1, sperre_2.get());

    beschreibungsService.beschreibungEntsperren(beschreibung.getId());
    Optional<Sperre> sperre_3 =
        beschreibungsService.findSperreForBeschreibung(beschreibung.getId());
    assertFalse(sperre_3.isPresent());
  }

  @Test
  @TestTransaction
  void testDeleteBeschreibung() throws Exception {

    Identifikation IDENTIFIKATION1 =
        new IdentifikationBuilder()
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withIdent("1-1-1")
            .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
            .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
            .withId(UUID.randomUUID().toString())
            .build();

    Identifikation IDENTIFIKATION2 =
        new IdentifikationBuilder()
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withIdent("1-1-1")
            .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
            .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
            .withId(UUID.randomUUID().toString())
            .build();

    Path kodFilePath = Paths.get("src", "test", "resources",
        "kod-attributsreferenzen_leer.xml");
    List<TEI> kodTeiList = TEIObjectFactory.unmarshal(
        newInputStream(kodFilePath));
    TEI kodTEI = kodTeiList.getFirst();

    Path teiFilePath1 =
        Paths.get("src", "test", "resources",
            "tei-beschreibung-attributsreferenzen.xml");
    List<TEI> teiList1 = TEIObjectFactory.unmarshal(
        newInputStream(teiFilePath1));
    TEI teiBeschreibung1 = teiList1.getFirst();

    Path teiFilePath2 =
        Paths.get("src", "test", "resources",
            "tei-beschreibung-attributsreferenzen_2.xml");
    List<TEI> teiList2 = TEIObjectFactory.unmarshal(
        newInputStream(teiFilePath2));
    TEI teiBeschreibung2 = teiList2.getFirst();

    final PURL beschreibungsPURL1 =
        new PURL(
            URI.create(
                "http://resolver.sbb.spk-berlin.de/HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001"),
            URI.create(
                "http://target.sbb.spk-berlin.de/HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001"),
            PURLTyp.INTERNAL);
    Beschreibung beschreibung1 =
        new Beschreibung.BeschreibungsBuilder(
            "HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001", "KODID",
            IDENTIFIKATION1)
            .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
            .withTEIXml(TEIObjectFactory.marshal(teiBeschreibung1))
            .withPublikation(createPublishedPublikation(
                LocalDateTime.of(2021, 1, 1, 0, 0), beschreibungsPURL1))
            .build();

    Beschreibung beschreibung2 =
        new Beschreibung.BeschreibungsBuilder(
            "HSP-f9f6eb32-068c-3089-a7d4-bbc3e55f3235", "KODID",
            IDENTIFIKATION2)
            .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
            .withPublikation(new Publikation.PublikationBuilder()
                .withErstpublikationsDatum(LocalDateTime.of(2020, 1, 1, 0, 0))
                .build())
            .withTEIXml(TEIObjectFactory.marshal(teiBeschreibung2))
            .build();

    KulturObjektDokument kod =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("HSP-0037fea7-58e0-3df9-a45c-488a7ee7e752")
            .withTEIXml(TEIObjectFactory.marshal(kodTEI))
            .addBeschreibungsdokumentID(beschreibung1.getId())
            .build();

    try {

      TEIKulturObjektDokumentCommand.updateKODBeschreibungenReferenzes(kod);

    } catch (Exception error) {
      logger.error("Error: ", error);
    }

    kod.getBeschreibungenIDs().add(beschreibung2.getId());
    TEIKulturObjektDokumentCommand.updateKODBeschreibungenReferenzes(kod);

    kulturObjektDokumentRepository.saveAndFlush(kod);
    beschreibungsRepository.saveAndFlush(beschreibung1);
    beschreibungsRepository.saveAndFlush(beschreibung2);
    Change change = TEIKODAttributsReferenzCommand.createChangeElement(
        HSP_UUI_Factory.generate(), LocalDateTime.now(),
        AttributsReferenzStatusTyp.AUTOMATISCH,
        "Delete Beschreibung", bearbeiterBoundary.getUnbekannterBearbeiter());

    kulturObjektDokumentBoundary.attributsReferenzenAusBeschreibungenAktualisieren(
        kod,
        AttributsReferenzStatusTyp.AUTOMATISCH, change.getId());
    TEIKODAttributsReferenzCommand.updateAttributsReferenzen(kod, change);

    String updatedTEIXML = kod.getTeiXML();

    assertTrue(
        updatedTEIXML.contains(
            "xml:id=\"HSP-0037fea7-58e0-3df9-a45c-488a7ee7e752\""),
        updatedTEIXML);
    assertTrue(
        updatedTEIXML.contains(
            "<ref target=\"HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001\"/>"),
        updatedTEIXML);
    assertTrue(
        updatedTEIXML.contains(
            "<ref target=\"HSP-f9f6eb32-068c-3089-a7d4-bbc3e55f3235\"/>"),
        updatedTEIXML);

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {
      var index = "<index indexName=\""
          + attributsTyp.getIndexName()
          + "\" copyOf=\"#"
          + beschreibung1.getId()
          + "\" source=\""
          + HTTP_RESOLVER_SPK_DE_PURL
          + "\" change=\"" + TEIValues.TEI_ANKER_TAG + change.getId()
          + "\">";
      assertTrue(updatedTEIXML.contains(index), index);
    }

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepository,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepository,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    kulturObjektDokumentRepository.saveAndFlush(kod);

    beschreibungsService.deleteBeschreibung(BEARBEITER, beschreibung1.getId());

    assertFalse(beschreibungsRepository.findByIdOptional(beschreibung1.getId())
        .isPresent());

    KulturObjektDokument updatedKOD = kulturObjektDokumentRepository.findById(
        kod.getId());
    assertNotNull(updatedKOD);
    assertNotNull(updatedKOD.getTeiXML());

    updatedTEIXML = updatedKOD.getTeiXML();

    assertTrue(
        updatedTEIXML.contains(
            "xml:id=\"HSP-0037fea7-58e0-3df9-a45c-488a7ee7e752\""));

    assertFalse(
        updatedTEIXML.contains(
            "<msDesc status=\"extern\" xml:id=\"HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001\""));
    assertFalse(
        updatedTEIXML.contains(
            "<ref target=\"HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001\"/>"),
        updatedTEIXML);
    assertTrue(
        updatedTEIXML.contains(
            "<ref target=\"HSP-f9f6eb32-068c-3089-a7d4-bbc3e55f3235\"/>"),
        updatedTEIXML);

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {
      assertTrue(
          updatedTEIXML.contains(
              "<index indexName=\"" + attributsTyp.getIndexName()
                  + "\" change"),
          updatedTEIXML);
    }

    assertTrue(updatedTEIXML.contains("Delete Beschreibung"));
    assertEquals(AttributsReferenzStatusTyp.MANUELL,
        updatedKOD.getAttributsReferenzStatusTyp());

    verify(indexServiceMock, times(1))
        .indexDocument(any(KulturObjektDokument.class),
            any(ActivityStreamAction.class));
    verify(papierkorbBoundaryMOCK, times(1)).erzeugeGeloeschtesDokument(
        beschreibung1, BEARBEITER);
  }

  @Test
  @TestTransaction
  void testIsBeschreibungActionableForLoggedInBearbeiter_falseWhenForeignSperreExists()
      throws Exception {
    final KulturObjektDokument kod = createKulturObjektDokument();
    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder("HSP-actionable-foreign-sperre", kod.getId(),
            kod.getGueltigeIdentifikation())
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .withBearbeiterIds(BEARBEITER)
            .withBearbeiterIds(BEARBEITER2)
            .build();

    kulturObjektDokumentRepository.save(kod);
    beschreibungsRepository.save(beschreibung);

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepository,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepositoryMOCK,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    assertTrue(
        beschreibungsService.isBeschreibungAssignedToLoggedInBearbeiter(beschreibung.getId()));
    assertTrue(
        beschreibungsService.isBeschreibungActionableForLoggedInBearbeiter(beschreibung.getId()));

    Sperre foreignSperre = beschreibungsService.beschreibungSperren(BEARBEITER2,
        beschreibung.getId(), SperreTyp.MANUAL, "EditorOpenedByOtherUser");
    assertNotNull(foreignSperre);

    assertTrue(
        beschreibungsService.isBeschreibungAssignedToLoggedInBearbeiter(beschreibung.getId()),
        "Assignment-only check ignores Sperre — must remain true for read-only views");
    assertFalse(
        beschreibungsService.isBeschreibungActionableForLoggedInBearbeiter(beschreibung.getId()),
        "Foreign Sperre must block write actions for logged-in Bearbeiter");
  }

  @Test
  @TestTransaction
  void testDeleteBeschreibung_blockedByForeignEditorSperre() throws Exception {
    final KulturObjektDokument kod = createKulturObjektDokument();
    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder("HSP-delete-foreign-sperre", kod.getId(),
            kod.getGueltigeIdentifikation())
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .withBearbeiterIds(BEARBEITER)
            .build();

    kulturObjektDokumentRepository.save(kod);
    beschreibungsRepository.save(beschreibung);

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepository,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepository,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    Sperre foreignEditorSperre = beschreibungsService.beschreibungSperren(BEARBEITER2,
        beschreibung.getId(), SperreTyp.MANUAL, "BeschreibungBearbeiten");
    assertNotNull(foreignEditorSperre);

    assertThrows(
        SperreAlreadyExistException.class,
        () -> beschreibungsService.deleteBeschreibung(BEARBEITER, beschreibung.getId()));

    assertTrue(beschreibungsRepository.findByIdOptional(beschreibung.getId()).isPresent(),
        "Beschreibung must remain after blocked delete");
  }

  @Test
  @TestTransaction
  void testFindLatestModified() {
    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepository,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepository,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    Identifikation identifikation =
        new IdentifikationBuilder()
            .withId("007")
            .withIdent(GUELTIGE_SIGNATURE)
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(
                new NormdatenReferenz(
                    "774909e2", "Staatsbibliothek zu Berlin", "5036103-X",
                    "CorporateBody"))
            .withAufbewahrungsOrt(
                new NormdatenReferenz("ee1611b6", "Berlin", "4005728-8",
                    "Place"))
            .build();

    BeschreibungsKomponenteKopf bkKopf =
        new BeschreibungsKomponenteKopfBuilder()
            .withId("123")
            .withKulturObjektTyp(KulturObjektTyp.CODEX)
            .withTitel("Titel")
            .build();

    Beschreibung beschreibung =
        new BeschreibungsBuilder(ENTITY_ID, "k-1", identifikation)
            .addBeschreibungsKomponente(bkKopf)
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .withErstellungsDatum(LocalDateTime.of(2021, 1, 1, 12, 0))
            .withAenderungsDatum(LocalDateTime.of(2022, 2, 2, 12, 0))
            .build();

    beschreibungsRepository.saveAndFlush(beschreibung);

    List<BeschreibungListDbView> beschreibungen = beschreibungsService.findLatestModifiedTopTen();
    assertNotNull(beschreibungen);
    assertEquals(1, beschreibungen.size());
  }

  @Test
  @TestTransaction
  void testFindLatestModifiedTopTenByBearbeiter() {
    Bearbeiter bearbeiter =
        new Bearbeiter.BearbeiterBuilder()
            .withBearbeitername("bearbeiter1")
            .withNachname("Nachname")
            .withVorname("Vorname")
            .build();
    Bearbeiter bearbeiter2 =
        new Bearbeiter.BearbeiterBuilder()
            .withBearbeitername("bearbeiter2")
            .withNachname("Mustermann")
            .withVorname("Max")
            .build();
    bearbeiterRepository.save(bearbeiter);
    bearbeiterRepository.save(bearbeiter2);
    Beschreibung beschreibung_1 =
        createBeschreibung(
            "b-1",
            "kod-1",
            "Titel-1",
            "SIG. I",
            "SIG. I alt",
            "alt_1",
            LocalDateTime.of(2021, 3, 3, 12, 0),
            Set.of(bearbeiter, bearbeiter2),
            true);
    beschreibung_1.setAenderungsDatum(LocalDateTime.of(2022, 4, 4, 12, 0));

    Beschreibung beschreibung_2 =
        createBeschreibung(
            "b-2",
            "kod-2",
            "Titel-2",
            "SIG. II",
            "SIG. II alt",
            "alt_2",
            LocalDateTime.of(2021, 1, 1, 12, 0),
            Set.of(bearbeiter),
            false);
    beschreibung_2.setAenderungsDatum(LocalDateTime.of(2022, 2, 2, 12, 0));

    beschreibungsRepository.saveAndFlush(beschreibung_1);
    beschreibungsRepository.saveAndFlush(beschreibung_2);

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepository,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepository,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    List<BeschreibungListDbView> beschreibungen =
        beschreibungsService.findLatestModifiedTopTenByBearbeiter(
            bearbeiter.getFullName());
    assertNotNull(beschreibungen);
    assertEquals(2, beschreibungen.size());

    List<BeschreibungListDbView> beschreibungen2 =
        beschreibungsService.findLatestModifiedTopTenByBearbeiter(
            bearbeiter2.getFullName());
    assertNotNull(beschreibungen2);
    assertEquals(1, beschreibungen2.size());

    assertBeschreibungListDTOEqualsBeschreibung(beschreibung_1,
        beschreibungen.get(0));
    assertBeschreibungListDTOEqualsBeschreibung(beschreibung_2,
        beschreibungen.get(1));
    assertBeschreibungListDTOEqualsBeschreibung(beschreibung_1,
        beschreibungen2.get(0));
  }

  @Test
  @TestTransaction
  void testAddPURL() throws Exception {

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

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepository,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepository,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    KulturObjektDokument kod = createKulturObjektDokument();
    Beschreibung beschreibung = createBeschreibung(kod);
    Publikation publikation = createPublishedPublikation(
        LocalDateTime.of(2020, 1, 1, 0, 0), purlExternal);
    beschreibung.setPublikation(publikation);
    kod.getBeschreibungenIDs().add(beschreibung.getId());
    kulturObjektDokumentRepository.save(kod);

    Path teiFilePath =
        Paths.get(
            "../domainmodel-tei-mapper/src", "test", "resources", "tei",
            "tei-msDesc_Westphal.xml");
    beschreibung.setTeiXML(Files.readString(teiFilePath));

    beschreibungsRepository.save(beschreibung);

    NullPointerException npe =
        assertThrows(NullPointerException.class,
            () -> beschreibungsService.addPURL(null, null));
    assertEquals("beschreibungId is required", npe.getMessage());

    String beschreibungId = beschreibung.getId();
    npe =
        assertThrows(
            NullPointerException.class,
            () -> beschreibungsService.addPURL(beschreibungId, null));
    assertEquals("purl is required", npe.getMessage());

    BeschreibungsException beNoKOD =
        assertThrows(
            BeschreibungsException.class,
            () -> beschreibungsService.addPURL("INVALID", purlInternal));
    assertEquals("Zur Id INVALID wurde keine Beschreibung gefunden!",
        beNoKOD.getMessage());

    beschreibungsService.addPURL(beschreibung.getId(), purlInternal);

    Optional<Beschreibung> updated = beschreibungsRepository.findByIdOptional(
        beschreibung.getId());
    assertTrue(updated.isPresent());

    assertEquals(2, updated.get().getPURLs().size());
    assertTrue(
        updated.get().getPURLs().stream()
            .anyMatch(
                purl ->
                    PURLTyp.INTERNAL == purl.getTyp()
                        && "https://resolver.de/40170459".equals(
                        purl.getPurl().toASCIIString())
                        && "https://target.de/40170459".equals(
                        purl.getTarget().toASCIIString())));

    verify(suchDokumentServiceMOCK, times(1)).beschreibungUebernehmen(
        any(Beschreibung.class));
    verify(indexServiceMock, times(1))
        .indexDocument(any(KulturObjektDokument.class),
            any(ActivityStreamAction.class));
  }

  @Test
  @TestTransaction
  void testSendIndexJob() {
    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepository,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepository,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    beschreibungsService.indexKulturObjektDokument(
        new KulturObjektDokumentBuilder().withId("test").build());
    verify(indexServiceMock, times(1))
        .indexDocument(any(KulturObjektDokument.class),
            any(ActivityStreamAction.class));
  }

  @Test
  void testMapBeschreibungFromXML() throws IOException, BeschreibungsException {
    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepository,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepository,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    Locale locale = Locale.ENGLISH;

    Throwable exceptionThatWasThrown;

    exceptionThatWasThrown =
        assertThrows(
            BeschreibungsException.class,
            () -> beschreibungsService.mapBeschreibungFromXML(null, locale));
    String exceptionThatWasThrownMessage = exceptionThatWasThrown.getMessage();
    assertTrue(
        exceptionThatWasThrownMessage.contains(
            "An error occurred while parsing the XML"),
        "Wrong message: " + exceptionThatWasThrownMessage);

    exceptionThatWasThrown =
        assertThrows(
            BeschreibungsException.class,
            () ->
                beschreibungsService.mapBeschreibungFromXML(
                    "<teiCorpus xmlns=\"http://www.tei-c.org/ns/1.0\" />",
                    locale));
    assertEquals("The XML does not contain a TEI element.",
        exceptionThatWasThrown.getMessage());

    exceptionThatWasThrown =
        assertThrows(
            BeschreibungsException.class,
            () ->
                beschreibungsService.mapBeschreibungFromXML(
                    "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\"/>", locale));
    assertEquals(
        "The MsDesc element is missing or ambiguous.",
        exceptionThatWasThrown.getMessage());

    exceptionThatWasThrown =
        assertThrows(
            BeschreibungsException.class,
            () ->
                beschreibungsService.mapBeschreibungFromXML(
                    "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\"><msDesc/><msDesc/></TEI>",
                    locale));
    assertEquals(
        "The MsDesc element is missing or ambiguous.",
        exceptionThatWasThrown.getMessage());

    Path teiFilePath =
        Paths.get(
            "../domainmodel-tei-mapper/src", "test", "resources", "tei",
            "tei-msDesc_Westphal.xml");

    Beschreibung beschreibung =
        beschreibungsService.mapBeschreibungFromXML(
            Files.readString(teiFilePath), locale);
    assertNotNull(beschreibung);
  }

  @Test
  void testAddBeschreibungToActivityStreamMessage() throws Exception {
    ActivityStream message = ActivityStream.builder().build();
    Beschreibung beschreibung = createBeschreibung(
        createKulturObjektDokument());
    beschreibung.setTeiXML("<TEI xmlns=\"http://www.tei-c.org/ns/1.0\"/>");

    Throwable exceptionThatWasThrown =
        assertThrows(
            NullPointerException.class,
            () -> BeschreibungsService.addBeschreibungToActivityStreamMessage(
                null, null));
    assertEquals("message is required!", exceptionThatWasThrown.getMessage());

    exceptionThatWasThrown =
        assertThrows(
            NullPointerException.class,
            () -> BeschreibungsService.addBeschreibungToActivityStreamMessage(
                message, null));
    assertEquals("beschreibung is required!",
        exceptionThatWasThrown.getMessage());

    BeschreibungsService.addBeschreibungToActivityStreamMessage(message,
        beschreibung);
    assertNotNull(message.getObjects());
    assertEquals(1, message.getObjects().size());

    ActivityStreamObject activityStreamObject = message.getObjects().iterator()
        .next();
    assertEquals(beschreibung.getId(), activityStreamObject.getId());
    assertArrayEquals(
        beschreibung.getTeiXML().getBytes(StandardCharsets.UTF_8),
        activityStreamObject.getContent(),
        "Content not equal");
    assertTrue(activityStreamObject.isCompressed());
    assertSame(ActivityStreamsDokumentTyp.BESCHREIBUNG,
        activityStreamObject.getType());
  }

  private NormdatenReferenz createSprache() {
    return new NormdatenReferenzBuilder()
        .withId(UUID.randomUUID().toString())
        .withTypeName("Language")
        .withName("Deutsch")
        .withGndID("4113292-0")
        .addIdentifikator(
            new Identifikator("deu",
                "https://id.loc.gov/vocabulary/iso639-1/de", "ISO_639-1"))
        .addIdentifikator(
            new Identifikator("deu",
                "https://id.loc.gov/vocabulary/iso639-2/deu", "ISO_639-2"))
        .addIdentifikator(
            new Identifikator("ger",
                "https://id.loc.gov/vocabulary/iso639-2/ger", "ISO_639-2"))
        .addIdentifikator(new Identifikator("de", "", ""))
        .addVarianterName(new VarianterName("Neuhochdeutsch", "de"))
        .addVarianterName(new VarianterName("Deutsche Sprache", "de"))
        .addVarianterName(new VarianterName("Hochdeutsch", "de"))
        .addVarianterName(new VarianterName("Deutsch", "de"))
        .build();
  }

  private KulturObjektDokument createKulturObjektDokument() {

    NormdatenReferenz besitzer =
        new NormdatenReferenz(UUID.randomUUID().toString(),
            "Staatsbibliothek zu Berlin", "");

    Identifikation identifikation =
        new Identifikation.IdentifikationBuilder()
            .withId("Mscr.Dresd.A.111")
            .withIdent("Mscr.Dresd.A.111")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(besitzer)
            .build();

    return new KulturObjektDokumentBuilder()
        .withId("1234")
        .withGndIdentifier("gndid1231234x")
        .withGueltigerIdentifikation(identifikation)
        .build();
  }

  private Bearbeiter createBearbeiter() {
    return new Bearbeiter.BearbeiterBuilder()
        .withBearbeitername("112233")
        .withVorname("vorname")
        .withNachname("nachname")
        .build();
  }

  private NormdatenReferenz createNormdatenReferenzGerman() {
    return new NormdatenReferenz.NormdatenReferenzBuilder()
        .withId(UUID.nameUUIDFromBytes("de".getBytes()).toString())
        .withTypeName("Language")
        .withName("Deutsch")
        .addVarianterName(new VarianterName("Neuhochdeutsch", "de"))
        .addVarianterName(new VarianterName("Deutsche Sprache", "de"))
        .addVarianterName(new VarianterName("Hochdeutsch", "de"))
        .addIdentifikator(new Identifikator("de", "", ""))
        .addIdentifikator(
            new Identifikator("ger",
                "https://id.loc.gov/vocabulary/iso639-2/ger", "ISO_639-2"))
        .addIdentifikator(
            new Identifikator("deu",
                "https://id.loc.gov/vocabulary/iso639-2/deu", "ISO_639-2"))
        .addIdentifikator(
            new Identifikator("de", "https://id.loc.gov/vocabulary/iso639-1/de",
                "ISO_639-1"))
        .withGndID("4113292-0")
        .build();
  }

  @Test
  @TestTransaction
  void testaddBearbeiterToBeschreiben() throws Exception {

    KulturObjektDokument kod = createKulturObjektDokument();
    Beschreibung beschreibung = createBeschreibung(kod);
    Bearbeiter bearbeiter = createBearbeiter();

    kulturObjektDokumentRepository.save(kod);
    beschreibungsRepository.save(beschreibung);

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepository,
            identifikationRepository,
            kulturObjektDokumentRepository,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    Optional<BeschreibungsViewModel> beschreibungsViewModelOptional =
        beschreibungsService.buildBeschreibungsViewModel(beschreibung.getId());

    assertTrue(beschreibungsViewModelOptional.isPresent());

    beschreibungsService.addBearbeiterToBeschreibung(
        beschreibung.getId(), Set.of(bearbeiter.getBearbeitername()));

    assertEquals(0, beschreibung.getBearbeiterIDs().size());

    Optional<Beschreibung> updated = beschreibungsRepository.findByIdOptional(
        beschreibung.getId());

    assertTrue(updated.isPresent());

    assertEquals(1, updated.get().getBearbeiterIDs().size());

    verify(suchDokumentServiceMOCK, times(1)).beschreibungUebernehmen(
        any(Beschreibung.class));
  }

  @Test
  void testisBeschreibungActionableForLoggedInBearbeiter() {
    KulturObjektDokument kod = createKulturObjektDokument();
    Beschreibung beschreibung = createBeschreibung(kod);

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepository,
            identifikationRepository,
            kulturObjektDokumentRepository,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);
    assertEquals(
        "Unbekannter Bearbeiter",
        bearbeiterBoundary.getLoggedBearbeiter().getBearbeitername());
    assertEquals(0, beschreibung.getBearbeiterIDs().size());

    assertFalse(
        beschreibungsService.isBeschreibungActionableForLoggedInBearbeiter(
            beschreibung.getId()));
  }

  @Test
  @TestTransaction
  void testremoveBearbeiterFromBeschreibung()
      throws BeschreibungsException, DokumentSperreException {

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepository,
            identifikationRepositoryMOCK,
            kulturObjektDokumentRepository,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    Identifikation identifikation =
        new IdentifikationBuilder()
            .withId("007")
            .withIdent(GUELTIGE_SIGNATURE)
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(
                new NormdatenReferenz(
                    "774909e2", "Staatsbibliothek zu Berlin", "5036103-X",
                    "CorporateBody"))
            .withAufbewahrungsOrt(
                new NormdatenReferenz("ee1611b6", "Berlin", "4005728-8",
                    "Place"))
            .build();

    BeschreibungsKomponenteKopf bkKopf =
        new BeschreibungsKomponenteKopfBuilder()
            .withId("111")
            .withKulturObjektTyp(KulturObjektTyp.CODEX)
            .withTitel("Titel")
            .build();
    Bearbeiter bearbeiter = createBearbeiter();

    Beschreibung beschreibung =
        new BeschreibungsBuilder(ENTITY_ID, "k-11", identifikation)
            .addBeschreibungsKomponente(bkKopf)
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .withBearbeiterIds(bearbeiter)
            .build();

    beschreibungsRepository.save(beschreibung);
    assertEquals(1, beschreibung.getBearbeiterIDs().size());

    beschreibungsService.removeBearbeiterFromBeschreibung(
        beschreibung.getId(), bearbeiter.getBearbeitername());

    Optional<Beschreibung> updated = beschreibungsRepository.findByIdOptional(
        beschreibung.getId());
    assertTrue(updated.isPresent());

    assertEquals(0, updated.get().getBearbeiterIDs().size());
  }

  @Test
  @TestTransaction
  void testfindBearbeiterEineBeschreibung() throws BeschreibungsException {

    PURL internal =
        new PURL(
            URI.create("https://resolver.url/HSP-123"),
            URI.create("https://target.url/HSP-123"),
            PURLTyp.INTERNAL);
    PURL external =
        new PURL(
            URI.create("https://doi.org/HSP-123"),
            URI.create("https://doi.org/HSP-123"),
            PURLTyp.EXTERNAL);
    Publikation publikation = new Publikation.PublikationBuilder()
        .withErstpublikationsDatum(LocalDateTime.of(1982, 1, 2, 0, 0))
        .addPURL(internal)
        .addPURL(external)
        .build();

    Beschreibung beschreibung =
        new BeschreibungsBuilder("40170459", "KODID", IDENTIFIKATION)
            .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
            .withPublikation(publikation)
            .addAutor(
                new NormdatenReferenz(UUID.randomUUID().toString(), "Tester",
                    "GNDID"))
            .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION_RETRO)

            .withBearbeiterIds(new Bearbeiter("111", "222"))
            .build();

    beschreibungsRepository.save(beschreibung);

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepository,
            identifikationRepository,
            kulturObjektDokumentRepository,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    assertEquals(
        1, beschreibungsService.findBearbeiterEineBeschreibung(
            beschreibung.getId()).size());
  }

  @Test
  @TestTransaction
  void testFindAllInvalidBeschreibungen_ODD() throws TeiXmlValidationException {

    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder(
            "HSP1",
            "KOD1",
            new Identifikation.IdentifikationBuilder()
                .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
                .withId("ID1")
                .withIdent("Ms.1")
                .withBesitzer(new NormdatenReferenz("1", "Staabi", "GND1"))
                .withAufbewahrungsOrt(
                    new NormdatenReferenz("2", "Berlin", "GND2"))
                .build())
            .withTEIXml("<xml></xml>")
            .addAltIdentifier("MXML-123")
            .build();

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepository,
            identifikationRepository,
            kulturObjektDokumentRepository,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    beschreibungsRepository.save(beschreibung);
    when(teiXmlValidationBoundaryMock.validateTeiXml(
        beschreibung.getTeiXML(), String.valueOf(Locale.GERMAN), true))
        .thenReturn(new ValidationResult(false, "1", "1", "Fehler"));

    Set<BeschreibungValidationReportItem> report = beschreibungsService.findAllInvalidODD();

    assertNotNull(report);

    assertEquals(1, report.size());

    report.stream()
        .findFirst()
        .ifPresentOrElse(
            r -> {
              assertEquals(beschreibung.getId(), r.getBeschreibungsID());
              assertEquals(
                  beschreibung.getGueltigeIdentifikation().get().getBesitzer()
                      .getName(),
                  r.getNameBesitzer());
              assertEquals(
                  beschreibung.getGueltigeIdentifikation().get()
                      .getAufbewahrungsOrt().getName(),
                  r.getNameAufbewahrungsort());
              assertEquals(
                  beschreibung.getGueltigeIdentifikation().get().getIdent(),
                  r.getSignatur());
              assertEquals(
                  beschreibung.getAltIdentifier().stream().findFirst().get(),
                  r.getMxmlID());
              assertEquals("Fehler", r.getValidationResult().getMessage());
            },
            Assertions::fail);
  }

  @Test
  @TestTransaction
  void testFindAllInvalidBeschreibungen_ALL() throws TeiXmlValidationException {

    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder(
            "HSP2",
            "KOD2",
            new Identifikation.IdentifikationBuilder()
                .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
                .withId("ID2")
                .withIdent("Ms.2")
                .withBesitzer(new NormdatenReferenz("1", "Staabi", "GND2"))
                .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND3"))
                .build())
            .withTEIXml("<xml></xml>")
            .addAltIdentifier("MXML-123")
            .build();

    BeschreibungsService beschreibungsService =
        new BeschreibungsService(
            beschreibungsRepository,
            identifikationRepository,
            kulturObjektDokumentRepository,
            suchDokumentServiceMOCK,
            indexServiceMock,
            dokumentSperreBoundary,
            papierkorbBoundaryMOCK,
            teiXmlValidationBoundaryMock,
            bearbeiterBoundary,
            beschreibungsRechteMOCK,
            schemaPflegeDateiServicePortMock,
            beschreibungsSynchronizerMOCK,
            kafkaIndexingProducerMOCK);

    beschreibungsRepository.save(beschreibung);

    when(teiXmlValidationBoundaryMock.validateTeiXml(
        beschreibung.getTeiXML(), String.valueOf(Locale.GERMAN), false))
        .thenReturn(new ValidationResult(false, "1", "1", "Fehler-ALL"));

    Set<BeschreibungValidationReportItem> report = beschreibungsService.findAllInvalidALL();

    assertNotNull(report);
    assertEquals(1, report.size());

    report.stream().findFirst().ifPresentOrElse(r -> {
          assertEquals(beschreibung.getId(), r.getBeschreibungsID());
          assertEquals("Staabi", r.getNameBesitzer());
          assertEquals("Berlin", r.getNameAufbewahrungsort());
          assertEquals("Ms.2", r.getSignatur());
          assertEquals("MXML-123", r.getMxmlID());
          assertEquals("Fehler-ALL", r.getValidationResult().getMessage());
        },
        Assertions::fail);
  }


  @Test
  @TestTransaction
  void publizieren_happyPath_runsAllSideEffects() throws Exception {
    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("KOD-PUB-1")
        .withTEIXml("<xml></xml>")
        .build();
    kod.getBeschreibungenIDs().add("BPUB-1");
    Beschreibung beschreibung = new BeschreibungsBuilder("BPUB-1", kod.getId(), IDENTIFIKATION)
        .withTEIXml("<xml></xml>")
        .build();

    BeschreibungsService service = newServiceWithAllMocks();

    when(kulturObjektDokumentRepositoryMOCK.findByIdOptional(kod.getId()))
        .thenReturn(Optional.of(kod));
    when(beschreibungsSynchronizerMOCK.findEntstehungsorteAsEntityFacts(beschreibung))
        .thenReturn(List.of());

    service.publizieren(beschreibung);

    verify(beschreibungsSynchronizerMOCK).updateTEIDocuments(kod, beschreibung, List.of());
    verify(kulturObjektDokumentRepositoryMOCK).saveAndFlush(kod);
    verify(suchDokumentServiceMOCK).beschreibungUebernehmen(beschreibung);
    verify(suchDokumentServiceMOCK).kodUebernehmen(kod);
    verify(kafkaIndexingProducerMOCK).sendKulturobjektDokumentAsActivityStreamMessage(
        kod, ActivityStreamAction.UPDATE, true, "System");
  }

  /**
   * Ticket 23862: after the synchronizer rewrites the TEI, publizieren must re-validate. If the
   * post-synchronizer TEI is invalid, the approval transaction must abort — saveAndFlush, Solr, and
   * Kafka side effects must NOT run. The Beschreibung's TEI is set to the literal string "invalid",
   * which
   * {@link de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationServiceMock}
   * treats as a validation failure.
   */
  @Test
  @TestTransaction
  void publizieren_postSyncTeiInvalid_throwsAndSkipsSideEffects() throws Exception {
    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("KOD-PUB-INVALID-TEI")
        .withTEIXml("<xml></xml>")
        .build();
    kod.getBeschreibungenIDs().add("BPUB-INVALID-TEI");
    Beschreibung beschreibung = new BeschreibungsBuilder("BPUB-INVALID-TEI", kod.getId(),
        IDENTIFIKATION)
        .withTEIXml("invalid")
        .build();

    BeschreibungsService service = newServiceWithAllMocks();

    when(kulturObjektDokumentRepositoryMOCK.findByIdOptional(kod.getId()))
        .thenReturn(Optional.of(kod));
    when(beschreibungsSynchronizerMOCK.findEntstehungsorteAsEntityFacts(beschreibung))
        .thenReturn(List.of());

    PublikationException ex = assertThrows(PublikationException.class,
        () -> service.publizieren(beschreibung));
    assertEquals("publikation_error_tei_invalid", ex.getMessage());

    verify(beschreibungsSynchronizerMOCK).updateTEIDocuments(kod, beschreibung, List.of());
    verify(kulturObjektDokumentRepositoryMOCK, never()).saveAndFlush(any());
    verify(suchDokumentServiceMOCK, never()).beschreibungUebernehmen(any());
    verify(suchDokumentServiceMOCK, never()).kodUebernehmen(any());
    verify(kafkaIndexingProducerMOCK, never()).sendKulturobjektDokumentAsActivityStreamMessage(
        any(), any(), anyBoolean(), anyString());
  }

  /**
   * P0 regression: old {@code PublikationSucheIndexerService.updateSolrAndIndexer} threw
   * {@code PublikationException("publikation_error_kod_not_found")} when the KOD was missing,
   * causing the approval tx to roll back. The new {@code publizieren} silently logs and returns,
   * which leaves the Beschreibung marked as published but the index stale. This test pins the
   * intended behavior (throw) — currently RED.
   */
  @Test
  @TestTransaction
  void publizieren_kodNotFound_throwsPublikationException() {
    Beschreibung beschreibung = new BeschreibungsBuilder("BPUB-2", "KOD-MISSING", IDENTIFIKATION)
        .withTEIXml("<xml></xml>")
        .build();

    BeschreibungsService service = newServiceWithAllMocks();

    when(kulturObjektDokumentRepositoryMOCK.findByIdOptional("KOD-MISSING"))
        .thenReturn(Optional.empty());

    PublikationException ex = assertThrows(PublikationException.class,
        () -> service.publizieren(beschreibung));
    assertEquals("publikation_error_kod_not_found", ex.getMessage());
  }

  /**
   * P0 regression: in the approve flow the KOD is guaranteed to be indexable (has TEI + at least
   * this Beschreibung). A {@code !isIndexable} KOD here means invariant violation — should fail
   * loudly, not silently skip the Kafka send. This test pins the intended behavior — currently RED
   * (impl does {@code log.warn + skip}).
   */
  @Test
  @TestTransaction
  void publizieren_kodNotIndexable_throwsIllegalStateException() throws Exception {
    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("KOD-NOT-INDEXABLE")
        .withTEIXml("<xml></xml>")
        .build();
    // no beschreibungenIDs added → !isIndexable
    Beschreibung beschreibung = new BeschreibungsBuilder("BPUB-4", kod.getId(), IDENTIFIKATION)
        .withTEIXml("<xml></xml>")
        .build();

    BeschreibungsService service = newServiceWithAllMocks();

    when(kulturObjektDokumentRepositoryMOCK.findByIdOptional(kod.getId()))
        .thenReturn(Optional.of(kod));
    when(beschreibungsSynchronizerMOCK.findEntstehungsorteAsEntityFacts(beschreibung))
        .thenReturn(List.of());

    assertThrows(IllegalStateException.class, () -> service.publizieren(beschreibung));
  }

  @Test
  @TestTransaction
  void publizieren_nullBeschreibung_throwsNpe() {
    BeschreibungsService service = newServiceWithAllMocks();

    assertThrows(NullPointerException.class, () -> service.publizieren(null));
  }

  @Test
  @TestTransaction
  void publizieren_kafkaFailure_wrapsToKafkaKey() throws Exception {
    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("KOD-PUB-3")
        .withTEIXml("<xml></xml>")
        .build();
    kod.getBeschreibungenIDs().add("BPUB-3");
    Beschreibung beschreibung = new BeschreibungsBuilder("BPUB-3", kod.getId(), IDENTIFIKATION)
        .withTEIXml("<xml></xml>")
        .build();

    BeschreibungsService service = newServiceWithAllMocks();

    when(kulturObjektDokumentRepositoryMOCK.findByIdOptional(kod.getId()))
        .thenReturn(Optional.of(kod));
    when(beschreibungsSynchronizerMOCK.findEntstehungsorteAsEntityFacts(beschreibung))
        .thenReturn(List.of());
    doThrow(new ActivityStreamsException("kafka broker unreachable"))
        .when(kafkaIndexingProducerMOCK).sendKulturobjektDokumentAsActivityStreamMessage(
            any(), any(), anyBoolean(), anyString());

    PublikationException ex = assertThrows(PublikationException.class,
        () -> service.publizieren(beschreibung));
    assertEquals("publikation_error_kafka", ex.getMessage());

    verify(suchDokumentServiceMOCK).beschreibungUebernehmen(beschreibung);
    verify(suchDokumentServiceMOCK).kodUebernehmen(kod);
  }

  @Test
  @TestTransaction
  void publizieren_unexpectedRuntimeFailure_wrapsToUnexpectedKey() throws Exception {
    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("KOD-PUB-UX")
        .withTEIXml("<xml></xml>")
        .build();
    kod.getBeschreibungenIDs().add("BPUB-UX");
    Beschreibung beschreibung = new BeschreibungsBuilder("BPUB-UX", kod.getId(), IDENTIFIKATION)
        .withTEIXml("<xml></xml>")
        .build();

    BeschreibungsService service = newServiceWithAllMocks();

    when(kulturObjektDokumentRepositoryMOCK.findByIdOptional(kod.getId()))
        .thenReturn(Optional.of(kod));
    when(beschreibungsSynchronizerMOCK.findEntstehungsorteAsEntityFacts(beschreibung))
        .thenReturn(List.of());
    doThrow(new RuntimeException("db connection lost"))
        .when(kulturObjektDokumentRepositoryMOCK).saveAndFlush(any());

    PublikationException ex = assertThrows(PublikationException.class,
        () -> service.publizieren(beschreibung));
    assertEquals("publikation_error_unexpected", ex.getMessage());
    assertEquals("RuntimeException", ex.getMessageArgs()[0]);
  }

  @Test
  @TestTransaction
  void publizieren_solrBeschreibungUebernehmenFailure_wrapsToSucheIndexerKey() throws Exception {
    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("KOD-SOLR-1")
        .withTEIXml("<xml/>")
        .build();
    kod.getBeschreibungenIDs().add("B-SOLR-1");
    Beschreibung beschreibung = new BeschreibungsBuilder("B-SOLR-1", kod.getId(), IDENTIFIKATION)
        .withTEIXml("<xml/>")
        .build();

    BeschreibungsService service = newServiceWithAllMocks();

    when(kulturObjektDokumentRepositoryMOCK.findByIdOptional(kod.getId()))
        .thenReturn(Optional.of(kod));
    when(beschreibungsSynchronizerMOCK.findEntstehungsorteAsEntityFacts(beschreibung))
        .thenReturn(List.of());
    doThrow(new SolrUebernahmeException("solr down"))
        .when(suchDokumentServiceMOCK).beschreibungUebernehmen(beschreibung);

    PublikationException ex = assertThrows(PublikationException.class,
        () -> service.publizieren(beschreibung));
    assertEquals("publikation_error_suche_indexer", ex.getMessage());

    verify(kafkaIndexingProducerMOCK, never()).sendKulturobjektDokumentAsActivityStreamMessage(
        any(), any(), anyBoolean(), anyString());
  }

  @Test
  @TestTransaction
  void publizieren_solrKodUebernehmenFailure_wrapsToSucheIndexerKey() throws Exception {
    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("KOD-SOLR-2")
        .withTEIXml("<xml/>")
        .build();
    kod.getBeschreibungenIDs().add("B-SOLR-2");
    Beschreibung beschreibung = new BeschreibungsBuilder("B-SOLR-2", kod.getId(), IDENTIFIKATION)
        .withTEIXml("<xml/>")
        .build();

    BeschreibungsService service = newServiceWithAllMocks();

    when(kulturObjektDokumentRepositoryMOCK.findByIdOptional(kod.getId()))
        .thenReturn(Optional.of(kod));
    when(beschreibungsSynchronizerMOCK.findEntstehungsorteAsEntityFacts(beschreibung))
        .thenReturn(List.of());
    doThrow(new SolrUebernahmeException("solr kod write failed"))
        .when(suchDokumentServiceMOCK).kodUebernehmen(kod);

    PublikationException ex = assertThrows(PublikationException.class,
        () -> service.publizieren(beschreibung));
    assertEquals("publikation_error_suche_indexer", ex.getMessage());

    verify(suchDokumentServiceMOCK).beschreibungUebernehmen(beschreibung);
    verify(kafkaIndexingProducerMOCK, never()).sendKulturobjektDokumentAsActivityStreamMessage(
        any(), any(), anyBoolean(), anyString());
  }

  private BeschreibungsService newServiceWithAllMocks() {
    return new BeschreibungsService(
        beschreibungsRepositoryMOCK,
        identifikationRepositoryMOCK,
        kulturObjektDokumentRepositoryMOCK,
        suchDokumentServiceMOCK,
        indexServiceMock,
        dokumentSperreBoundaryMOCK,
        papierkorbBoundaryMOCK,
        teiXmlValidationBoundaryMock,
        bearbeiterBoundary,
        beschreibungsRechteMOCK,
        schemaPflegeDateiServicePortMock,
        beschreibungsSynchronizerMOCK,
        kafkaIndexingProducerMOCK);
  }

  // ==========================================================================
  // T5: isBeschreibungActionableForLoggedInBearbeiter — fail-closed on Sperre-Service down
  // ==========================================================================

  @Test
  void isBeschreibungActionableForLoggedInBearbeiter_returnsFalse_whenSperrenCheckFails()
      throws Exception {
    Beschreibung beschreibung =
        new BeschreibungsBuilder("B-FAIL-CLOSED", "KOD", IDENTIFIKATION)
            .withBearbeiterIds(BEARBEITER)
            .build();

    when(beschreibungsRepositoryMOCK.findById("B-FAIL-CLOSED")).thenReturn(beschreibung);
    when(dokumentSperreBoundaryMOCK.findBySperreEintraege(any(SperreEintrag.class)))
        .thenThrow(new DokumentSperreException("sperre service down"));

    BeschreibungsService service = newServiceWithAllMocks();

    assertFalse(service.isBeschreibungActionableForLoggedInBearbeiter("B-FAIL-CLOSED"),
        "Sperren-Service unreachable must deny write (fail-closed)");
  }

  // ==========================================================================
  // T6: isBeschreibungAssignedToLoggedInBearbeiter — null/empty cases
  // ==========================================================================

  @Test
  void isBeschreibungAssignedToLoggedInBearbeiter_returnsFalse_whenBeschreibungMissing() {
    when(beschreibungsRepositoryMOCK.findById("B-MISSING")).thenReturn(null);
    BeschreibungsService service = newServiceWithAllMocks();
    assertFalse(service.isBeschreibungAssignedToLoggedInBearbeiter("B-MISSING"));
  }

  @Test
  void isBeschreibungAssignedToLoggedInBearbeiter_returnsFalse_whenBearbeiterIdsEmpty() {
    Beschreibung beschreibung =
        new BeschreibungsBuilder("B-NOBODY", "KOD", IDENTIFIKATION).build();
    when(beschreibungsRepositoryMOCK.findById("B-NOBODY")).thenReturn(beschreibung);
    BeschreibungsService service = newServiceWithAllMocks();
    assertFalse(service.isBeschreibungAssignedToLoggedInBearbeiter("B-NOBODY"));
  }


  private Publikation createPublishedPublikation(LocalDateTime erstpublikationsDatum, PURL purl) {
    Publikation publikation = new Publikation.PublikationBuilder()
        .withErstpublikationsDatum(erstpublikationsDatum)
        .addPURL(purl)
        .build();
    publikation.onApproved();
    return publikation;
  }
}
