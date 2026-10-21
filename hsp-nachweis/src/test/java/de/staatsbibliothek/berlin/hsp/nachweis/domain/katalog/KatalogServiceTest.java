package de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.DIV_TYPE_BESCHREIBUNG;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary.SYSTEM_USERNAME;
import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog.KatalogBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KatalogDirector;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KatalogBeschreibungReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KatalogViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.katalogimport.KatalogImportService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamMessageDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.minimaldaten.IdentifikationRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.http.HttpBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.XMLDokument;
import io.quarkus.test.InjectMock;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mockito;
import org.tei_c.ns._1.Div;
import org.tei_c.ns._1.Lb;
import org.tei_c.ns._1.P;
import org.tei_c.ns._1.Pb;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 28.02.2022.
 * @version 1.0
 */

@QuarkusTest
@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class KatalogServiceTest {

  static final Path KATALOG_TEI_FILE_PATH = Paths.get("src", "test",
      "resources",
      "tei-katalog_Biberach.xml");

  static final Path BESCHREIBUNG_TEI_FILE_PATH = Paths.get("src", "test",
      "resources",
      "tei-msDesc_Biberach.xml");

  static final String MANIFEST_URL = "https://iiif.ub.uni-leipzig.de/0000034258/manifest.json";

  static final String RANGE_URL = "https://iiif.ub.uni-leipzig.de/0000034258/range/LOG_0009";

  static final String MANIFEST_FILENAME = "manifest-katalog_Biberach.json";

  static final Path MANIFEST_PATH = Paths.get("src", "test", "resources",
      MANIFEST_FILENAME);

  static final Path MANIFEST_404_PATH = Paths.get("src", "test", "resources", "kataloge",
      "04_FrankfurtMain_StuUB_Kat_5_4_tei.xml");
  static final String MANIFEST_SIGNATUR = "B 3519";
  static final LocalDateTime creationDate = LocalDateTime.of(2022,
      Month.JANUARY, 1, 12, 0, 0);
  private static final String TEI_FILENAME = "tei-katalog_Biberach.xml";
  private static final Path teiFilePath = Paths.get("src", "test", "resources",
      TEI_FILENAME);
  private static final Path EMPTY_KATALOG_PATH = Paths.get("src", "test", "resources", "kataloge",
      "04_Bsp_bibliogrAngaben-kuenftiges_manifest.xml");
  Bearbeiter bearbeiter = new Bearbeiter("1", "Unbekannter Bearbeiter");
  KatalogService katalogBoundary;
  @Inject
  KatalogRepository katalogRepository;
  @Inject
  KafkaIndexingProducer kafkaIndexingProducer;
  @Inject
  BeschreibungsRepository beschreibungsRepository;
  @Inject
  IdentifikationRepository identifikationRepository;
  @Inject
  KulturObjektDokumentService kulturObjektDokumentService;
  @Inject
  KulturObjektDokumentRepository kulturObjektDokumentRepository;
  @InjectMock
  TeiXmlValidationBoundary teiXmlValidationBoundaryMock;
  @Inject
  KatalogImportService katalogImportService;
  HttpBoundary httpBoundaryMock;
  NormdatenReferenzBoundary normdatenReferenzBoundary = Mockito.mock(NormdatenReferenzBoundary.class);
  TEI tei;
  ActivityStreamMessageDTO message;

  ActivityStreamObject importObject;

  @BeforeEach
  void beforeEach()
      throws IOException, NoSuchAlgorithmException, InterruptedException, KeyManagementException, JAXBException, ActivityStreamsException {
    httpBoundaryMock = mock(HttpBoundary.class);
    when(httpBoundaryMock.loadBodyFromURL(any(URI.class)))
        .thenReturn(Optional.ofNullable(Files.readString(MANIFEST_PATH)));

    try (InputStream is = newInputStream(teiFilePath)) {
      tei = TEIObjectFactory.unmarshalOne(is).orElseThrow();
    }

    ActivityStreamObject activityStreamObject = ActivityStreamObject.builder()
        .withContent(tei)
        .withName(TEI_FILENAME)
        .withId(TEI_FILENAME)
        .withType(ActivityStreamsDokumentTyp.KATALOG)
        .build();

    importObject = ActivityStreamObject.builder()
        .withCompressed(false)
        .withType(ActivityStreamsDokumentTyp.IMPORT)
        .withName(TEI_FILENAME)
        .withId("1")
        .withContent(
            "{\"id\":\"1c8050b1-616c-4454-b441-9281a57bc99d\",\"creationDate\":\"2020-09-24T15:22:15.926333\",\"benutzerName\":\"Redakteur\",\"importFiles\":[{\"id\":\"bec5a8c1-4e01-4d82-b19f-8189bb6b8a12\",\"path\":\"file:///tmp/hsp-import-/2020.09.24.15.22.15.925/tei-katalog_Aurich-wthout-BOM-translated.xml\",\"dateiTyp\":\"ms_1-wthout-BOM.xml\",\"dateiName\":\""
                + TEI_FILENAME
                + "\",\"dateiFormat\":\"TEI\",\"error\":false,\"message\":null,\"importEntityData\":[]}],\"name\":\"ms_1.xml\",\"importDir\":\"/tmp/hsp-import-/2020.09.24.15.22.15.925\",\"result\":\"NO_RESULT\",\"errorMessage\":\"\",\"datatype\":\"KATALOG\"}")
        .build();
    message = new ActivityStreamMessageDTO(ActivityStream.builder()
        .withId(TEI_FILENAME)
        .withPublished(LocalDateTime.now())
        .withType(ActivityStreamAction.ADD)
        .addObject(activityStreamObject)
        .addObject(importObject)
        .build());

    katalogBoundary = new KatalogService(
        katalogRepository,
        beschreibungsRepository,
        identifikationRepository,
        kafkaIndexingProducer,
        kulturObjektDokumentService,
        teiXmlValidationBoundaryMock,
        httpBoundaryMock,
        normdatenReferenzBoundary);
  }

  @Test
  @TestTransaction
  void testFindAllKatalogs() {
    assertEquals(0, katalogBoundary.findAll().size());
    KatalogDirector katalogDirector = new KatalogDirector(new KatalogBuilder());
    Katalog katalog = katalogDirector.createSimpleHSKKatalog("Test Katalog",
        "<TEI></TEI>", "1998",
        "440", null);
    katalogRepository.save(katalog);
    assertEquals(1, katalogBoundary.findAll().size());
  }

  @Test
  @TestTransaction
  void testImportierenNeu() throws Exception {
    kulturObjektDokumentRepository.save(createKOD());

    Beschreibung beschreibung = createBeschreibung();
    beschreibungsRepository.save(beschreibung);

    Katalog katalogNew = createNewKatalog();

    Katalog importierterKatalog = katalogBoundary.importieren(bearbeiter,
        katalogNew);

    assertNotNull(importierterKatalog);

    assertEquals(katalogNew.getId(), importierterKatalog.getId());
    assertEquals(katalogNew.getHskID(), importierterKatalog.getHskID());
    assertEquals(katalogNew.getLizenzUri(), importierterKatalog.getLizenzUri());
    assertEquals(katalogNew.getPublikationsJahr(),
        importierterKatalog.getPublikationsJahr());
    assertEquals(katalogNew.getTitle(), importierterKatalog.getTitle());
    assertNotNull(importierterKatalog.getErstellDatum());
    assertNotNull(importierterKatalog.getAenderungsDatum());
    assertTrue(
        importierterKatalog.getAenderungsDatum()
            .isAfter(importierterKatalog.getErstellDatum()));
    assertNotNull(importierterKatalog.getDigitalisat());
    assertEquals(katalogNew.getDigitalisat().getManifestURL(),
        importierterKatalog.getDigitalisat().getManifestURL());
    assertEquals(katalogNew.getTitle(), importierterKatalog.getTitle());

    assertNotNull(importierterKatalog.getTeiXML());
    assertTrue(importierterKatalog.getTeiXML()
        .contains("<idno type=\"hsp\">" + katalogNew.getId() + "</idno>"));

    assertNotNull(importierterKatalog.getBeschreibungen());
    assertEquals(29, importierterKatalog.getBeschreibungen().size());

    Optional<KatalogBeschreibungReferenz> beschreibungReferenzOptional = importierterKatalog.getBeschreibungen()
        .stream()
        .filter(br -> beschreibung.getId().equals(br.getBeschreibungsID()))
        .findFirst();

    assertTrue(beschreibungReferenzOptional.isPresent());

    List<Beschreibung> katalogBeschreibungen = beschreibungsRepository.findByKatalogId(
        katalogNew.getId());
    assertNotNull(katalogBeschreibungen);
    assertEquals(1, katalogBeschreibungen.size());
  }

  @Test
  @TestTransaction
  void testImportierenBestehend() throws Exception {
    kulturObjektDokumentRepository.save(createKOD());

    Beschreibung beschreibung = createBeschreibung();
    beschreibungsRepository.save(beschreibung);

    katalogRepository.save(createExistingKatalog());

    Katalog katalogNew = createNewKatalog();

    Katalog importierterKatalog = katalogBoundary.importieren(bearbeiter,
        katalogNew);

    assertNotNull(importierterKatalog);

    assertEquals(1, katalogRepository.listAll(false).size());

    assertEquals(katalogNew.getId(), importierterKatalog.getId());
    assertEquals(katalogNew.getHskID(), importierterKatalog.getHskID());
    assertEquals(katalogNew.getLizenzUri(), importierterKatalog.getLizenzUri());
    assertEquals(katalogNew.getPublikationsJahr(),
        importierterKatalog.getPublikationsJahr());
    assertEquals(katalogNew.getTitle(), importierterKatalog.getTitle());
    assertNotNull(importierterKatalog.getErstellDatum());
    assertNotNull(importierterKatalog.getAenderungsDatum());
    assertEquals(creationDate, importierterKatalog.getErstellDatum());
    assertTrue(importierterKatalog.getAenderungsDatum().isAfter(creationDate));
    assertNotNull(importierterKatalog.getDigitalisat());
    assertEquals(katalogNew.getDigitalisat().getManifestURL(),
        importierterKatalog.getDigitalisat().getManifestURL());

    assertEquals(katalogNew.getTitle(), importierterKatalog.getTitle());

    assertNotNull(importierterKatalog.getTeiXML());

    assertTrue(importierterKatalog.getTeiXML()
        .contains("<idno type=\"hsp\">" + katalogNew.getId() + "</idno>"));

    assertNotNull(importierterKatalog.getBeschreibungen());
    assertEquals(29, importierterKatalog.getBeschreibungen().size());

    Optional<KatalogBeschreibungReferenz> beschreibungReferenzOptional = importierterKatalog.getBeschreibungen()
        .stream()
        .filter(br -> beschreibung.getId().equals(br.getBeschreibungsID()))
        .findFirst();

    assertTrue(beschreibungReferenzOptional.isPresent());

    List<Beschreibung> katalogBeschreibungen = beschreibungsRepository.findByKatalogId(
        katalogNew.getId());
    assertNotNull(katalogBeschreibungen);
    assertEquals(1, katalogBeschreibungen.size());
  }

  @Test
  @TestTransaction
  void testSpeichern() throws Exception {
    Katalog katalog = createNewKatalog();
    Set<Beschreibung> beschreibungen = new HashSet<>();
    Beschreibung beschreibung = createBeschreibung();
    beschreibungen.add(beschreibung);

    ((KatalogService) katalogBoundary).speichern(katalog, beschreibungen);

    assertEquals(1, katalogBoundary.findAll().size());

    assertTrue(beschreibungsRepository.findByIdOptional(beschreibung.getId())
        .isPresent());
  }

  @Test
  @TestTransaction
  void testVolltexteUmhaengen() throws Exception {
    kulturObjektDokumentRepository.save(createKOD());

    Beschreibung beschreibung = createBeschreibung();
    beschreibungsRepository.save(beschreibung);

    Katalog katalog = createNewKatalog();

    Set<Beschreibung> beschreibungen = ((KatalogService) katalogBoundary).volltexteUmhaengen(
        katalog);
    assertNotNull(beschreibungen);
    assertEquals(1, beschreibungen.size());

    Beschreibung volltextBeschreibung = beschreibungen.stream().findFirst()
        .orElseThrow();
    assertNotNull(volltextBeschreibung.getTeiXML());
    assertTrue(volltextBeschreibung.getTeiXML()
        .contains(
            "<lb n=\"Page15_Block5_Line2\"/>Aus 2 Teilen zusammengesetzt: I. (Bl. 12—106)"));
    assertTrue(volltextBeschreibung.getTeiXML()
        .contains("<ptr type=\"hsp\" target=\"" + katalog.getId() + "\""));
    assertNotEquals(volltextBeschreibung.getErstellungsDatum(),
        volltextBeschreibung.getAenderungsDatum());
    assertTrue(volltextBeschreibung.getAenderungsDatum().isAfter(creationDate));

    assertNotNull(katalog.getBeschreibungen());
    assertEquals(29, katalog.getBeschreibungen().size());

    Optional<KatalogBeschreibungReferenz> referenz = katalog.getBeschreibungen()
        .stream()
        .filter(ref -> volltextBeschreibung.getId()
            .equals(ref.getBeschreibungsID()))
        .findFirst();

    assertTrue(referenz.isPresent());
    assertEquals("15", referenz.get().getSeiteVon());
    assertEquals("15", referenz.get().getSeiteBis());
    assertNotNull(referenz.get().getOcrVolltext());
    assertTrue(
        referenz.get().getOcrVolltext().startsWith("1. B 3519 15. Jh. (II)"));

    assertTrue(katalog.getAenderungsDatum().isAfter(creationDate));
  }

  @Test
  @TestTransaction
  void testVolltexteUmhaengenNoVolltext() throws Exception {
    Beschreibung beschreibung = createBeschreibung();
    beschreibung.getAltIdentifier().clear();
    beschreibung.getAltIdentifier().add("MXML-notInKatalog");
    beschreibungsRepository.save(beschreibung);

    Katalog katalog = createNewKatalog();

    Set<Beschreibung> beschreibungen = ((KatalogService) katalogBoundary).volltexteUmhaengen(
        katalog);

    assertNotNull(katalog.getBeschreibungen());
    assertEquals(29, katalog.getBeschreibungen().size());
    assertTrue(katalog.getAenderungsDatum().isAfter(creationDate));

    assertNotNull(beschreibungen);
    assertTrue(beschreibungen.isEmpty());
  }

  @Test
  @TestTransaction
  void testUpdateRangeUrisFromManifestinKatalogBeschreibungReferenzen()
      throws Exception {
    kulturObjektDokumentRepository.save(createKOD());

    Beschreibung beschreibung = createBeschreibung();
    beschreibungsRepository.save(beschreibung);

    Katalog katalog = createNewKatalog();
    katalogBoundary.volltexteUmhaengen(katalog);

    assertNotNull(katalog.getBeschreibungen());
    assertEquals(29, katalog.getBeschreibungen().size());
    katalog.getBeschreibungen().stream()
        .findFirst()
        .ifPresent(
            b ->
                assertNotNull(b.getManifestRangeURL(),
                    "Missing Range Url in KatalogBeschreibungsReferenzen"));
  }

  @Test
  void testCreateKatalogBeschreibungReferenz() throws Exception {
    KatalogService katalogService = ((KatalogService) katalogBoundary);

    assertThrows(KatalogException.class,
        () -> katalogService.createKatalogBeschreibungReferenz("K-1", null));

    assertThrows(KatalogException.class,
        () -> katalogService.createKatalogBeschreibungReferenz("K-1",
            new Div()));

    Div div = createTestDiv();

    KatalogBeschreibungReferenz referenz = katalogService.createKatalogBeschreibungReferenz(
        "K-1",
        div);
    assertNotNull(referenz);

    assertNotNull(referenz.getBeschreibungsID());
    assertEquals("90412639", referenz.getBeschreibungsID());

    assertNotNull(referenz.getSeiteVon());
    assertEquals("32", referenz.getSeiteVon());

    assertNotNull(referenz.getSeiteBis());
    assertEquals("33", referenz.getSeiteBis());

    assertNotNull(referenz.getOcrVolltext());
    assertEquals("Initialen, nicht oder kaum verziert. Einband 15. Jh.",
        referenz.getOcrVolltext());

    assertNull(referenz.getManifestRangeURL());
  }

  @Test
  void testGetVolltextBeschreibungen() throws Exception {
    KatalogService katalogService = ((KatalogService) katalogBoundary);

    assertThrows(KatalogException.class,
        () -> katalogService.getVolltextBeschreibungen(null));

    assertThrows(KatalogException.class,
        () -> katalogService.getVolltextBeschreibungen(
            new KatalogBuilder().build()));

    Katalog katalog = createNewKatalog();

    Map<String, Div> volltextBeschreibungen = katalogService.getVolltextBeschreibungen(
        katalog);
    assertNotNull(volltextBeschreibungen);
    assertEquals(29, volltextBeschreibungen.size());

    for (Entry<String, Div> entry : volltextBeschreibungen.entrySet()) {
      assertNotNull(entry.getKey());
      assertTrue(entry.getKey().startsWith("MXML-"));
      assertNotNull(entry.getValue().getType());
      assertEquals(DIV_TYPE_BESCHREIBUNG, entry.getValue().getType());
      assertNotNull(entry.getValue().getN());
    }
  }

  @Test
  void testGetAllRangeURIsWithSignatureAsKey() {
    KatalogService katalogService = ((KatalogService) katalogBoundary);
    Map<String, URI> rangeUrls;

    assertEquals(katalogService.getAllRangeURIsWithSignatureAsKey(null), Collections.emptyMap());
    ;

    rangeUrls = katalogService.getAllRangeURIsWithSignatureAsKey(URI.create(MANIFEST_URL));

    assertNotNull(rangeUrls);
    assertEquals(29, rangeUrls.size());

    assertTrue(rangeUrls.containsKey(MANIFEST_SIGNATUR));
    assertEquals(URI.create(RANGE_URL), rangeUrls.get(MANIFEST_SIGNATUR));
  }

  @Test
  void testLoadManifest() {
    KatalogService katalogService = ((KatalogService) katalogBoundary);
    Optional<Manifest> manifest = katalogService.loadManifest(URI.create(MANIFEST_URL));
    assertTrue(manifest.isPresent());
    assertNotNull(manifest.get().getRanges());
    assertEquals(40, manifest.get().getRanges().size());
  }

  @Test
  void testGetOcrVolltext() throws Exception {
    KatalogService katalogService = ((KatalogService) katalogBoundary);

    String ocrVolltext = katalogService.getOcrVolltext(null);
    assertNotNull(ocrVolltext);
    assertEquals("", ocrVolltext);

    ocrVolltext = katalogService.getOcrVolltext(new Div());
    assertNotNull(ocrVolltext);
    assertEquals("", ocrVolltext);

    Div div = createTestDiv();
    ocrVolltext = katalogService.getOcrVolltext(div);
    assertNotNull(ocrVolltext);
    assertEquals("Initialen, nicht oder kaum verziert. Einband 15. Jh.",
        ocrVolltext);
  }

  @Test
  void testCreateMxmlId() {
    KatalogService katalogService = ((KatalogService) katalogBoundary);
    assertNull(katalogService.createMxmlId(null));
    assertEquals("MXML-1234", katalogService.createMxmlId("1234"));
    assertEquals("MXML-1234-T", katalogService.createMxmlId("1234,T"));
    assertEquals("MXML-1234-Admin", katalogService.createMxmlId("1234,Admin"));
  }

  @Test
  @TestTransaction
  void testFindRangeURIForBeschreibung() throws Exception {
    KatalogService katalogService = ((KatalogService) katalogBoundary);

    Beschreibung beschreibung = createBeschreibung();
    KulturObjektDokument kod = createKOD();
    kulturObjektDokumentRepository.save(kod);

    Map<String, URI> allRanges = new HashMap<>();
    Optional<URI> result;

    result = katalogService.findRangeURIForBeschreibung(beschreibung,
        allRanges);
    assertTrue(result.isEmpty());

    allRanges.put("B 3519", URI.create("http://localhost/bgs"));
    result = katalogService.findRangeURIForBeschreibung(beschreibung,
        allRanges);
    assertTrue(result.isPresent());
    assertEquals("http://localhost/bgs", result.get().toASCIIString());

    allRanges.clear();
    allRanges.put("K 3519", URI.create("http://localhost/kgs"));
    result = katalogService.findRangeURIForBeschreibung(beschreibung,
        allRanges);
    assertTrue(result.isPresent());
    assertEquals("http://localhost/kgs", result.get().toASCIIString());

    allRanges.clear();
    allRanges.put(MANIFEST_SIGNATUR, URI.create("http://localhost/kas"));
    result = katalogService.findRangeURIForBeschreibung(beschreibung,
        allRanges);
    assertTrue(result.isPresent());
    assertEquals("http://localhost/kas", result.get().toASCIIString());
  }

  @Test
  @TestTransaction
  void testBuildKatalogViewModel() throws Exception {
    Beschreibung beschreibung = createBeschreibung();
    KulturObjektDokument kod = createKOD();
    Katalog katalog = createExistingKatalog();

    kod.getBeschreibungenIDs().add(beschreibung.getId());
    kulturObjektDokumentRepository.save(kod);

    beschreibung.setKatalogID(katalog.getId());
    beschreibungsRepository.save(beschreibung);

    katalog.getBeschreibungen().iterator().next()
        .setBeschreibungsID(beschreibung.getId());
    katalogRepository.save(katalog);

    KatalogViewModel katalogViewModel = katalogBoundary.buildKatalogViewModel(
            katalog.getId())
        .orElse(null);
    assertNotNull(katalogViewModel);
    assertNotNull(katalogViewModel.getBeschreibungen());
    assertEquals(1, katalogViewModel.getBeschreibungen().size());
    assertEquals(1, katalogViewModel.getAnzahlBeschreibungen());
    assertEquals(1, katalogViewModel.getAnzahlReferenzen());
    assertNotNull(katalogViewModel.getDigitalisat());
  }

  @Test
  void testUpdateAutoren() throws Exception {
    Optional<Katalog> katalog = katalogImportService.convertTEI2Katalog(
        message);
    assertNotNull(katalog.get().getAutoren());
    assertEquals(1, katalog.get().getAutoren().size());

    final Optional<NormdatenReferenz> normdatumFormService = Optional.of(
        new NormdatenReferenzBuilder().withId(UUID.randomUUID().toString())
            .withName("Helmut Boese").withGndID("1053059922").build());
    when(normdatenReferenzBoundary.findOneByIdOrNameAndType("1053059922",
        "Person")).thenReturn(
        normdatumFormService);

    katalogBoundary.updateAutorenNormdatenReferenzen(katalog.get());

    assertNotNull(katalog.get().getAutoren());
    assertEquals(1, katalog.get().getAutoren().size());
    assertEquals(normdatumFormService.get(),
        katalog.get().getAutoren().stream().findFirst().get());
  }

  @Test
  void testUpdateVerlagsNormdatenReferenz() throws Exception {
    Optional<Katalog> katalog = katalogImportService.convertTEI2Katalog(
        message);
    assertNotNull(katalog.get().getVerlag());
    assertNull(katalog.get().getVerlag().getId());

    final Optional<NormdatenReferenz> normdatumFormService = Optional.of(
        new NormdatenReferenzBuilder().withId(UUID.randomUUID().toString())
            .withName("Test Normdaten").withGndID("GND1").build());
    when(normdatenReferenzBoundary.findOneByIdOrNameAndType("1068107626",
        "CorporateBody")).thenReturn(
        normdatumFormService);

    katalogBoundary.updateVerlagsNormdatenReferenz(katalog.get());

    assertNotNull(katalog.get().getVerlag());
    assertEquals(normdatumFormService.get(), katalog.get().getVerlag());
  }

  @Test
  void testSendMessages() throws Exception {
    KafkaIndexingProducer kafkaIndexingProducerMock = Mockito.mock(
        KafkaIndexingProducer.class);
    KulturObjektDokumentService kulturObjektDokumentServiceMock = Mockito.mock(
        KulturObjektDokumentService.class);

    KatalogService katalogBoundary = new KatalogService(
        katalogRepository,
        beschreibungsRepository,
        identifikationRepository,
        kafkaIndexingProducerMock,
        kulturObjektDokumentServiceMock,
        teiXmlValidationBoundaryMock,
        httpBoundaryMock, normdatenReferenzBoundary);

    final Katalog newKatalog = createNewKatalog();
    final var kod = createKOD();
    final var beschreibung = createBeschreibung();

    when(kulturObjektDokumentServiceMock.findById(
        beschreibung.getKodID())).thenReturn(
        Optional.of(kod));

    katalogBoundary.sendMessages(
        newKatalog, ActivityStreamAction.ADD,
        Set.of(beschreibung));

    verify(kafkaIndexingProducerMock,
        times(1)).sendKatalogAsActivityStreamMessage(newKatalog,
        ActivityStreamAction.ADD, true, SYSTEM_USERNAME);

    verify(kafkaIndexingProducerMock,
        times(1)).sendKulturobjektDokumentAsActivityStreamMessage(
        any(),
        eq(ActivityStreamAction.UPDATE), eq(true), eq(SYSTEM_USERNAME));
  }

  @Test
  @TestTransaction
  void testKatalogManifest404()
      throws Exception {
    when(httpBoundaryMock.loadBodyFromURL(any(URI.class)))
        .thenReturn(Optional.empty());

    String teiXML = Files.readString(BESCHREIBUNG_TEI_FILE_PATH,
        StandardCharsets.UTF_8);

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withId(UUID.randomUUID().toString())
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withIdent("MS. GERM. FOL. 1")
        .build();

    var beschreibungsID = "HSP-66def941-2651-310d-ad6c-fa7e25fc8e1b";
    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder(
        beschreibungsID, "HSP-k1", identifikation)
        .addAltIdentifier("MXML-90062723-T")
        .withKatalog("011")
        .withTEIXml(teiXML)
        .withErstellungsDatum(creationDate)
        .withAenderungsDatum(creationDate)
        .build();

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withId(UUID.randomUUID().toString())
        .build();

    beschreibung.getBeschreibungsStruktur().add(kopf);

    beschreibungsRepository.save(beschreibung);

    assertFalse(beschreibung.getTeiXML().contains("Marmorierter Pappband"));

    var katalog = createNewKatalog(MANIFEST_404_PATH,
        "https://iiif.ub.uni-leipzig.de/0000035491/manifest.json");
    var k2 = katalogRepository.findById(katalog.getId());
    assertNull(k2);

    katalogBoundary.updateKatalogAndBeschreibungen(katalog, ActivityStreamAction.ADD);

    var k3 = katalogRepository.findById(katalog.getId());
    assertNotNull(k3);
    assertEquals(85, k3.getBeschreibungen().size());
    assertEquals("22", k3.getBeschreibungen().stream()
        .filter(b -> b.getBeschreibungsID().equals(beschreibungsID))
        .findFirst().get().getSeiteVon());
    var b2 = beschreibungsRepository.findById(beschreibung.getId());
    assertTrue(b2.getTeiXML().contains("Marmorierter Pappband"));

  }

  @Test
  @TestTransaction
  void testEmptyKatalog() throws Exception {

    ActivityStreamObject object = ActivityStreamObject.builder().withContent(Files.readString(
            EMPTY_KATALOG_PATH))
        .withType(ActivityStreamsDokumentTyp.KATALOG).build();
    ActivityStream activityStream = ActivityStream.builder().addObject(object).withType(ActivityStreamAction.ADD)
        .withId("1")

        .build();
    ActivityStreamMessageDTO messageDTO = new ActivityStreamMessageDTO(activityStream);

    final Optional<Katalog> katalog = katalogImportService.convertTEI2Katalog(
        messageDTO);

    when(normdatenReferenzBoundary.findOneByIdOrNameAndType("1068107626",
        GNDEntityFact.CORPORATE_BODY_TYPE_NAME)).thenReturn(
        Optional.of(new NormdatenReferenz("NORM-123", "Otto Harrasowitz", "1068107626")));

    when(normdatenReferenzBoundary.findOneByIdOrNameAndType("1177404419",
        GNDEntityFact.PERSON_TYPE_NAME)).thenReturn(
        Optional.of(new NormdatenReferenz("NORM-12334", "Kurt HEydeck", "1177404419")));

    assertEquals(0, katalogRepository.findAll().count());

    katalogBoundary.updateKatalogAndBeschreibungen(katalog.get(), ActivityStreamAction.ADD);

    assertEquals(1, katalogRepository.findAll().count());

    Katalog result = katalogRepository.findAll().firstResult();

    assertNotNull(result);
    assertNotNull(result.getId());
    assertEquals(
        "Die Handschriften der Signaturenreihe Hdschr. der Staatsbibliothek zu Berlin Preussischer Kulturbesitz, Teil 1: Hdschr. 1–150.",
        result.getTitle());
    assertNotNull(result.getVerlag());
    assertEquals(1, result.getAutoren().size());
    assertEquals("666", result.getHskID());
    assertNotNull(result.getTeiXML());
    assertNotNull(result.getErstellDatum());
    assertEquals(0, result.getBeschreibungen().size());
    assertNotNull(result.getDigitalisat().getAlternativeURL());

  }

  Katalog createExistingKatalog() {
    KatalogBeschreibungReferenz beschreibungReferenz = new KatalogBeschreibungReferenz(
        "90034276",
        "15", "15", "bestehender volltext",
        URI.create("https://iiif.ub.uni-leipzig.de/0000034258/range/LOG_0009"));

    return new Katalog.KatalogBuilder()
        .withId("HSP-a8abb4bb-284b-3b27-aa7c-b790dc20f80b")
        .withHSKID("011")
        .withTitle("Test")
        .withDigitalisat(Digitalisat.DigitalisatBuilder().build())
        .withPublikationsJahr("2000")
        .withLizenzURI("https://rightsstatements.org/page/InC/1.0/")
        .withTEIXML("<TEI></TEI>")
        .withErstelldatum(creationDate)
        .withAenderungsdatum(creationDate)
        .addKatalogBeschreibungReferenz(beschreibungReferenz)
        .build();
  }

  Katalog createNewKatalog() throws Exception {
    return createNewKatalog(KATALOG_TEI_FILE_PATH, MANIFEST_URL);
  }

  Katalog createNewKatalog(Path path, String manifestURL) throws Exception {
    String teiXML = Files.readString(path,
        StandardCharsets.UTF_8);
    LocalDateTime date = LocalDateTime.now();

    return new Katalog.KatalogBuilder()
        .withId("HSP-a8abb4bb-284b-3b27-aa7c-b790dc20f80b")
        .withHSKID("011")
        .withTitle(
            "Die Handschriften und Inkunabeln des Spitalarchivs zu Biberach")
        .withDigitalisat(Digitalisat.DigitalisatBuilder()
            .withManifest(manifestURL)
            .build())
        .withPublikationsJahr("1979")
        .withVerlag(
            new NormdatenReferenz("v_1", "Harrassowitz", null, "CorporateBody"))
        .withLizenzURI("https://rightsstatements.org/page/InC/1.0/")
        .addAutor(new NormdatenReferenz("a_1", "Helmut Boese", null, "Person"))
        .withTEIXML(teiXML)
        .withErstelldatum(date)
        .withAenderungsdatum(date)
        .build();
  }

  Beschreibung createBeschreibung() throws Exception {
    String teiXML = Files.readString(BESCHREIBUNG_TEI_FILE_PATH,
        StandardCharsets.UTF_8);

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withId(UUID.randomUUID().toString())
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withIdent("B 3519")
        .build();
    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder(
        "HSP-66def941-2651-310d-ad6c-fa7e25fc8e1b", "HSP-k1", identifikation)
        .addAltIdentifier("MXML-90034276-T")
        .withKatalog("011")
        .withTEIXml(teiXML)
        .withErstellungsDatum(creationDate)
        .withAenderungsDatum(creationDate)
        .build();

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withId(UUID.randomUUID().toString())
        .build();

    beschreibung.getBeschreibungsStruktur().add(kopf);

    return beschreibung;
  }

  private KulturObjektDokument createKOD() {
    return new KulturObjektDokumentBuilder()
        .withId("HSP-k1")
        .withGueltigerIdentifikation(new Identifikation.IdentifikationBuilder()
            .withId(UUID.randomUUID().toString())
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withIdent("K 3519")
            .build())
        .addAlternativeIdentifikation(new Identifikation.IdentifikationBuilder()
            .withId(UUID.randomUUID().toString())
            .withIdentTyp(IdentTyp.ALTSIGNATUR)
            .withIdent(MANIFEST_SIGNATUR)
            .build())
        .withTEIXml("<xml></xml>")
        .build();
  }

  private Div createTestDiv() {
    Div div = new Div();
    div.setType(DIV_TYPE_BESCHREIBUNG);
    div.setSubtype("Codex");
    div.setN("90412639,T");

    P p = new P();
    p.setN("Page32_Block1");
    div.getMeetingsAndBylinesAndDatelines().add(p);

    Pb pb = new Pb();
    pb.setN("Page32");
    p.getContent().add(pb);

    Lb lb = new Lb();
    lb.setN("Page32_Block1_Line1");
    p.getContent().add(lb);

    p.getContent().add("Initialen, nicht oder kaum verziert.");

    Pb pb2 = new Pb();
    pb2.setN("Page33");
    p.getContent().add(pb2);

    Lb lb2 = new Lb();
    lb2.setN("Page33_Block1_Line1");
    p.getContent().add(lb2);

    p.getContent().add("Einband 15. Jh.");

    return div;
  }

  @Test
  @TestTransaction
  void testfindKatalogBeschreibungen() {

    Katalog katalog = new Katalog.KatalogBuilder()
        .withId("HSP-a8abb4bb-284b-3b27-aa7c-b790dc20f80b")
        .withHSKID("011")
        .withTitle("Test").build();

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withId(UUID.randomUUID().toString())
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withIdent("B 3519")
        .build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder(
        "HSP-66def941-2651-310d-ad6c-fa7e25fc8e1b", "HSP-k1", identifikation)
        .addAltIdentifier("MXML-90034276-T")
        .withKatalog("011").build();

    beschreibungsRepository.saveAndFlush(beschreibung);

    assertEquals(0, katalog.getBeschreibungen().size());

  }

  @Test
  void findAllInvalid_gibtNurUngueltigeZurueck_undMappedFelderKorrekt()
      throws TeiXmlValidationException {
    KatalogRepository katalogRepositoryMock = Mockito.mock(KatalogRepository.class);

    KatalogService katalogServiceMock = new KatalogService(
        katalogRepositoryMock,
        beschreibungsRepository,
        identifikationRepository,
        kafkaIndexingProducer,
        kulturObjektDokumentService,
        teiXmlValidationBoundaryMock,
        httpBoundaryMock,
        normdatenReferenzBoundary
    );

    XMLDokument xmlValid = Mockito.mock(XMLDokument.class);
    when(xmlValid.getId()).thenReturn("KAT-VALID");
    when(xmlValid.getTeiXml()).thenReturn("<TEI id='valid'/>");

    XMLDokument xmlInvalid = Mockito.mock(XMLDokument.class);
    when(xmlInvalid.getId()).thenReturn("KAT-INVALID");
    when(xmlInvalid.getTeiXml()).thenReturn("<TEI id='invalid'/>");

    when(katalogRepositoryMock.selectAllTeiXml())
        .thenAnswer(inv -> Stream.of(xmlValid, xmlInvalid));

    ValidationResult validResult = new ValidationResult(true, null, null, "ok");
    ValidationResult invalidResult = new ValidationResult(false, "12", "34",
        "Dokument ist nicht valide -> element \"textLang\" not allowed");

    when(teiXmlValidationBoundaryMock.validateTeiXml(anyString(), anyString(), eq(false)))
        .thenReturn(validResult)
        .thenReturn(invalidResult);

    Katalog katalogInvalid = new Katalog.KatalogBuilder()
        .withId("KAT-INVALID")
        .withTitle("Ungültiger Katalog")
        .withPublikationsJahr("2002")
        .build();

    when(katalogRepositoryMock.findById("KAT-INVALID")).thenReturn(katalogInvalid);

    Set<KatalogValidationReportItem> report = katalogServiceMock.findAllInvalid();

    assertNotNull(report);
    assertEquals(1, report.size(), "Es sollte genau ein ungültiger Katalog im Report sein");

    KatalogValidationReportItem item = report.iterator().next();
    assertEquals("KAT-INVALID", item.getHspId());
    assertEquals("Ungültiger Katalog", item.getTitel());
    assertEquals("2002", item.getPublikationsjahr());
    assertFalse(item.getValidationResult().isValid());
    assertTrue(item.getValidationResult().getMessageWithDetails(Locale.GERMAN)
        .contains("Dokument ist nicht valide"));

    verify(katalogRepositoryMock, times(1)).selectAllTeiXml();
    verify(katalogRepositoryMock, times(1)).findById("KAT-INVALID");
    verify(katalogRepositoryMock, never()).findById("KAT-VALID");
  }

  @Test
  @TestTransaction
  void findAllInvalid_leerWennAlleGueltig() throws TeiXmlValidationException {
    Katalog katalogOne = new Katalog.KatalogBuilder()
        .withId("K1")
        .withTitle("Katalog 1")
        .withPublikationsJahr("2001")
        .withTEIXML("<TEI id='k1'/>")
        .build();

    Katalog katalogTwo = new Katalog.KatalogBuilder()
        .withId("K2")
        .withTitle("Katalog 2")
        .withPublikationsJahr("2002")
        .withTEIXML("<TEI id='k2'/>")
        .build();

    katalogRepository.save(katalogOne);
    katalogRepository.save(katalogTwo);

    when(teiXmlValidationBoundaryMock.validateTeiXml(anyString(), anyString(), eq(false)))
        .thenReturn(new ValidationResult(true, null, null, "ok"))
        .thenReturn(new ValidationResult(true, null, null, "ok"));

    Set<KatalogValidationReportItem> report = katalogBoundary.findAllInvalid();

    assertNotNull(report, "Report darf nicht null sein");
    assertTrue(report.isEmpty(), "Report sollte leer sein, wenn alle Kataloge gültig sind");
  }

  @Test
  @TestTransaction
  void findAllInvalid_wirftIllegalState_wennValidatorNichtErreichbar()
      throws TeiXmlValidationException {
    Katalog katalog = new Katalog.KatalogBuilder()
        .withId("K1")
        .withTitle("Katalog 1")
        .withPublikationsJahr("2001")
        .withTEIXML("<TEI/>")
        .build();

    katalogRepository.save(katalog);

    when(teiXmlValidationBoundaryMock.validateTeiXml(anyString(), anyString(), eq(false)))
        .thenThrow(new TeiXmlValidationException("ImportService down", null));

    IllegalStateException ex = assertThrows(
        IllegalStateException.class,
        () -> katalogBoundary.findAllInvalid(),
        "Bei nicht erreichbarem Validator sollte IllegalStateException geworfen werden"
    );

    assertTrue(ex.getMessage().contains("Validation service unavailable"),
        "Fehlermeldung sollte 'Validation service unavailable' enthalten");
  }
}
