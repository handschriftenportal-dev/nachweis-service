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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.DIV_TYPE_BESCHREIBUNG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KatalogViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.IdentifikationRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.transformation.TeiXmlTransformationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.http.HttpBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaKatalogProducer;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.tei_c.ns._1.Div;
import org.tei_c.ns._1.Lb;
import org.tei_c.ns._1.P;
import org.tei_c.ns._1.Pb;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 28.02.2022.
 * @version 1.0
 */

@QuarkusTest
@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class KatalogServiceTest {

  static final Path KATALOG_TEI_FILE_PATH = Paths.get("src", "test", "resources",
      "tei-katalog_Biberach.xml");

  static final Path BESCHREIBUNG_TEI_FILE_PATH = Paths.get("src", "test", "resources",
      "tei-msDesc_Biberach.xml");

  static final String MANIFEST_URL = "https://iiif.ub.uni-leipzig.de/0000034258/manifest.json";

  static final String RANGE_URL = "https://iiif.ub.uni-leipzig.de/0000034258/range/LOG_0009";

  static final String MANIFEST_FILENAME = "manifest-katalog_Biberach.json";

  static final Path MANIFEST_PATH = Paths.get("src", "test", "resources", MANIFEST_FILENAME);

  static final String MANIFEST_SIGNATUR = "A 3519";

  static final LocalDateTime creationDate = LocalDateTime.of(2022, Month.JANUARY, 1, 12, 0, 0);

  Bearbeiter bearbeiter = new Bearbeiter("1", "Unbekannter Bearbeiter");

  KatalogBoundary katalogBoundary;

  @Inject
  KatalogRepository katalogRepository;

  @Inject
  KafkaKatalogProducer kafkaKatalogProducer;

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

  @Inject
  TeiXmlValidationBoundary teiXmlValidationBoundaryMock;

  @Inject
  TeiXmlTransformationBoundary teiXmlTransformationBoundaryMock;

  HttpBoundary httpBoundaryMock;

  @BeforeEach
  void beforeEach() throws IOException, NoSuchAlgorithmException, InterruptedException, KeyManagementException {
    httpBoundaryMock = mock(HttpBoundary.class);
    when(httpBoundaryMock.loadBodyFromURL(any(URI.class)))
        .thenReturn(Optional.ofNullable(Files.readString(MANIFEST_PATH)));

    katalogBoundary = new KatalogService(
        katalogRepository,
        beschreibungsRepository,
        identifikationRepository,
        kafkaKatalogProducer,
        kafkaIndexingProducer,
        kulturObjektDokumentService,
        teiXmlValidationBoundaryMock,
        teiXmlTransformationBoundaryMock,
        httpBoundaryMock);
  }

  @Test
  @TestTransaction
  void testFindAllKatalogs() {
    assertEquals(0, katalogBoundary.findAll().size());
    KatalogDirector katalogDirector = new KatalogDirector(new KatalogBuilder());
    Katalog katalog = katalogDirector.createSimpleHSKKatalog("Test Katalog", "<TEI></TEI>", "1998",
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

    Katalog importierterKatalog = katalogBoundary.importieren(bearbeiter, katalogNew);

    assertNotNull(importierterKatalog);

    assertEquals(katalogNew.getId(), importierterKatalog.getId());
    assertEquals(katalogNew.getHskID(), importierterKatalog.getHskID());
    assertEquals(katalogNew.getLizenzUri(), importierterKatalog.getLizenzUri());
    assertEquals(katalogNew.getPublikationsJahr(), importierterKatalog.getPublikationsJahr());
    assertEquals(katalogNew.getTitle(), importierterKatalog.getTitle());
    assertNotNull(importierterKatalog.getErstellDatum());
    assertNotNull(importierterKatalog.getAenderungsDatum());
    assertTrue(
        importierterKatalog.getAenderungsDatum().isAfter(importierterKatalog.getErstellDatum()));
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

    List<Beschreibung> katalogBeschreibungen = beschreibungsRepository.findByKatalogId(katalogNew.getId());
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

    Katalog importierterKatalog = katalogBoundary.importieren(bearbeiter, katalogNew);

    assertNotNull(importierterKatalog);

    assertEquals(1, katalogRepository.listAll(false).size());

    assertEquals(katalogNew.getId(), importierterKatalog.getId());
    assertEquals(katalogNew.getHskID(), importierterKatalog.getHskID());
    assertEquals(katalogNew.getLizenzUri(), importierterKatalog.getLizenzUri());
    assertEquals(katalogNew.getPublikationsJahr(), importierterKatalog.getPublikationsJahr());
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

    List<Beschreibung> katalogBeschreibungen = beschreibungsRepository.findByKatalogId(katalogNew.getId());
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

    assertTrue(beschreibungsRepository.findByIdOptional(beschreibung.getId()).isPresent());
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

    Beschreibung volltextBeschreibung = beschreibungen.stream().findFirst().orElseThrow();
    assertNotNull(volltextBeschreibung.getTeiXML());
    assertTrue(volltextBeschreibung.getTeiXML()
        .contains("<lb n=\"Page15_Block5_Line2\"/>Aus 2 Teilen zusammengesetzt: I. (Bl. 12—106)"));
    assertTrue(volltextBeschreibung.getTeiXML()
        .contains("<ptr target=\"" + katalog.getId() + "\" type=\"hsp\"/>"));
    assertNotEquals(volltextBeschreibung.getErstellungsDatum(),
        volltextBeschreibung.getAenderungsDatum());
    assertTrue(volltextBeschreibung.getAenderungsDatum().isAfter(creationDate));

    assertTrue(volltextBeschreibung.getTeiXML().contains(MANIFEST_URL));
    assertTrue(volltextBeschreibung.getTeiXML().contains(RANGE_URL));

    assertNotNull(katalog.getBeschreibungen());
    assertEquals(29, katalog.getBeschreibungen().size());

    Optional<KatalogBeschreibungReferenz> referenz = katalog.getBeschreibungen().stream()
        .filter(ref -> volltextBeschreibung.getId().equals(ref.getBeschreibungsID()))
        .findFirst();

    assertTrue(referenz.isPresent());
    assertNotNull(referenz.get().getManifestRangeURL());
    assertEquals(RANGE_URL, referenz.get().getManifestRangeURL().toASCIIString());
    assertEquals("15", referenz.get().getSeiteVon());
    assertEquals("15", referenz.get().getSeiteBis());
    assertNotNull(referenz.get().getOcrVolltext());
    assertTrue(referenz.get().getOcrVolltext().startsWith("1. B 3519 15. Jh. (II)"));

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

    Set<Beschreibung> beschreibungen = ((KatalogService) katalogBoundary).volltexteUmhaengen(katalog);

    assertNotNull(katalog.getBeschreibungen());
    assertEquals(29, katalog.getBeschreibungen().size());
    assertTrue(katalog.getAenderungsDatum().isAfter(creationDate));

    assertNotNull(beschreibungen);
    assertTrue(beschreibungen.isEmpty());
  }

  @Test
  void testCreateKatalogBeschreibungReferenz() throws Exception {
    KatalogService katalogService = ((KatalogService) katalogBoundary);

    Assertions.assertThrows(KatalogException.class,
        () -> katalogService.createKatalogBeschreibungReferenz("K-1", null));

    Assertions.assertThrows(KatalogException.class,
        () -> katalogService.createKatalogBeschreibungReferenz("K-1", new Div()));

    Div div = createTestDiv();

    KatalogBeschreibungReferenz referenz = katalogService.createKatalogBeschreibungReferenz("K-1", div);
    assertNotNull(referenz);

    assertNotNull(referenz.getBeschreibungsID());
    assertEquals("90412639", referenz.getBeschreibungsID());

    assertNotNull(referenz.getSeiteVon());
    assertEquals("32", referenz.getSeiteVon());

    assertNotNull(referenz.getSeiteBis());
    assertEquals("33", referenz.getSeiteBis());

    assertNotNull(referenz.getOcrVolltext());
    assertEquals("Initialen, nicht oder kaum verziert. Einband 15. Jh.", referenz.getOcrVolltext());

    assertNull(referenz.getManifestRangeURL());
  }

  @Test
  void testGetVolltextBeschreibungen() throws Exception {
    KatalogService katalogService = ((KatalogService) katalogBoundary);

    Assertions.assertThrows(KatalogException.class, () -> katalogService.getVolltextBeschreibungen(null));

    Assertions.assertThrows(KatalogException.class,
        () -> katalogService.getVolltextBeschreibungen(new KatalogBuilder().build()));

    Katalog katalog = createNewKatalog();

    Map<String, Div> volltextBeschreibungen = katalogService.getVolltextBeschreibungen(katalog);
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

    Assertions.assertThrows(KatalogException.class, () -> katalogService.getAllRangeURIsWithSignatureAsKey(null));

    rangeUrls = katalogService.getAllRangeURIsWithSignatureAsKey(URI.create(MANIFEST_URL));

    assertNotNull(rangeUrls);
    assertEquals(29, rangeUrls.size());

    assertTrue(rangeUrls.containsKey(MANIFEST_SIGNATUR));
    assertEquals(URI.create(RANGE_URL), rangeUrls.get(MANIFEST_SIGNATUR));
  }

  @Test
  void testLoadManifest() {
    KatalogService katalogService = ((KatalogService) katalogBoundary);
    Manifest manifest = katalogService.loadManifest(URI.create(MANIFEST_URL));
    assertNotNull(manifest);
    assertNotNull(manifest.getRanges());
    assertEquals(40, manifest.getRanges().size());
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
    assertEquals("Initialen, nicht oder kaum verziert. Einband 15. Jh.", ocrVolltext);
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

    result = katalogService.findRangeURIForBeschreibung(beschreibung, allRanges);
    assertTrue(result.isEmpty());

    allRanges.put("B 3519", URI.create("http://localhost/bgs"));
    result = katalogService.findRangeURIForBeschreibung(beschreibung, allRanges);
    assertTrue(result.isPresent());
    assertEquals("http://localhost/bgs", result.get().toASCIIString());

    allRanges.clear();
    allRanges.put("K 3519", URI.create("http://localhost/kgs"));
    result = katalogService.findRangeURIForBeschreibung(beschreibung, allRanges);
    assertTrue(result.isPresent());
    assertEquals("http://localhost/kgs", result.get().toASCIIString());

    allRanges.clear();
    allRanges.put("A 3519", URI.create("http://localhost/kas"));
    result = katalogService.findRangeURIForBeschreibung(beschreibung, allRanges);
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

    katalog.getBeschreibungen().iterator().next().setBeschreibungsID(beschreibung.getId());
    katalogRepository.save(katalog);

    KatalogViewModel katalogViewModel = katalogBoundary.buildKatalogViewModel(katalog.getId())
        .orElse(null);
    assertNotNull(katalogViewModel);
    assertNotNull(katalogViewModel.getBeschreibungen());
    assertEquals(1, katalogViewModel.getBeschreibungen().size());
    assertEquals(1, katalogViewModel.getAnzahlBeschreibungen());
    assertEquals(1, katalogViewModel.getAnzahlReferenzen());
    assertNotNull(katalogViewModel.getDigitalisat());
  }

  Katalog createExistingKatalog() {
    KatalogBeschreibungReferenz beschreibungReferenz = new KatalogBeschreibungReferenz("90034276",
        "15", "15", "bestehender volltext",
        URI.create("https://iiif.ub.uni-leipzig.de/0000034258/range/LOG_0009"));

    return new Katalog.KatalogBuilder()
        .withId("HSP-a8abb4bb-284b-3b27-aa7c-b790dc20f80b")
        .withHSKID("011")
        .withTitle("Test")
        .withDigitalisat(Digitalisat.DigitalisatBuilder().build())
        .withPublikationsJahr("2000")
        .withVerlag(new NormdatenReferenz(null, "Verlag", null, "CorporateBody"))
        .withLizenzURI("https://rightsstatements.org/page/InC/1.0/")
        .addAutor(new NormdatenReferenz(null, "Tom Tester", null, "Person"))
        .withTEIXML("<TEI></TEI>")
        .withErstelldatum(creationDate)
        .withAenderungsdatum(creationDate)
        .addKatalogBeschreibungReferenz(beschreibungReferenz)
        .build();
  }

  Katalog createNewKatalog() throws Exception {
    String teiXML = Files.readString(KATALOG_TEI_FILE_PATH, StandardCharsets.UTF_8);
    LocalDateTime date = LocalDateTime.now();

    return new Katalog.KatalogBuilder()
        .withId("HSP-a8abb4bb-284b-3b27-aa7c-b790dc20f80b")
        .withHSKID("011")
        .withTitle("Die Handschriften und Inkunabeln des Spitalarchivs zu Biberach")
        .withDigitalisat(Digitalisat.DigitalisatBuilder()
            .withManifest(MANIFEST_URL)
            .build())
        .withPublikationsJahr("1979")
        .withVerlag(new NormdatenReferenz("v_1", "Harrassowitz", null, "CorporateBody"))
        .withLizenzURI("https://rightsstatements.org/page/InC/1.0/")
        .addAutor(new NormdatenReferenz("a_1", "Helmut Boese", null, "Person"))
        .withTEIXML(teiXML)
        .withErstelldatum(date)
        .withAenderungsdatum(date)
        .build();
  }

  Beschreibung createBeschreibung() throws Exception {
    String teiXML = Files.readString(BESCHREIBUNG_TEI_FILE_PATH, StandardCharsets.UTF_8);

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP-66def941-2651-310d-ad6c-fa7e25fc8e1b")
        .withKodID("HSP-k1")
        .addAltIdentifier("MXML-90034276-T")
        .withKatalog("011")
        .withTEIXml(teiXML)
        .withErstellungsDatum(creationDate)
        .withAenderungsDatum(creationDate)
        .build();

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withId(UUID.randomUUID().toString())
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withIdent("B 3519")
        .build();

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withId(UUID.randomUUID().toString())
        .withIndentifikationen(Set.of(identifikation))
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
}
