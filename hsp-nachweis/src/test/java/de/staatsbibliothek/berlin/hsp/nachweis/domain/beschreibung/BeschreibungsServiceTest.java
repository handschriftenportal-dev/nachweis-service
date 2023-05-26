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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung;

import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KulturObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz.LizenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIKODAttributsReferenzCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIKulturObjektDokumentCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.IdentifikationRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.papierkorb.PapierkorbBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.SperreRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SolrUebernahmeException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.transformation.TeiXmlTransformationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexService;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.BeschreibungListDTO;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import de.staatsbibliothek.berlin.javaee.authentication.service.AuthenticationService;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.TEI;

@Slf4j
@QuarkusTest
@TestInstance(Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
class BeschreibungsServiceTest {

  private static final Logger logger = LoggerFactory.getLogger(BeschreibungsServiceTest.class);
  public static final String ENTITY_ID = "entityId";
  public static final String GUELTIGE_SIGNATURE = "a-007-777";
  static Bearbeiter BEARBEITER = new Bearbeiter("1", "Unbekannter Bearbeiter");

  private static final String HTTP_RESOLVER_SPK_DE_PURL = "http://resolver.sbb.spk-berlin.de/HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001";
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
  TeiXmlTransformationBoundary teiXmlTransformationBoundaryMock;
  @Inject
  TeiXmlValidationBoundary teiXmlValidationBoundaryMock;

  BeschreibungsRepository beschreibungsRepositoryMOCK;
  IdentifikationRepository identifikationRepositoryMOCK;
  KulturObjektDokumentRepository kulturObjektDokumentRepositoryMOCK;
  SuchDokumentService suchDokumentServiceMOCK;
  DokumentSperreBoundary dokumentSperreBoundaryMOCK;
  PapierkorbBoundary papierkorbBoundaryMOCK;
  IndexService indexServiceMock;

  @BeforeAll
  public static void beforeAll() {
    BearbeiterService bearbeiterBoundary = mock(BearbeiterService.class);

    AuthenticationService authenticationService = mock(AuthenticationService.class);
    bearbeiterBoundary.setAuthenticationService(authenticationService);

    AuthenticationRepository authenticationRepository = mock(AuthenticationRepository.class);
    bearbeiterBoundary.setAuthenticationRepository(authenticationRepository);

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(BEARBEITER);
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
    indexServiceMock = mock(IndexService.class);
    papierkorbBoundaryMOCK = mock(PapierkorbBoundary.class);

    bearbeiterRepository.saveAndFlush(BEARBEITER);
  }

  @AfterEach
  @Transactional(TxType.REQUIRES_NEW)
  void afterEach() {
    sperreRepository.listAll(false).forEach(s -> sperreRepository.deleteByIdAndFlush(s.getId()));
    bearbeiterRepository.deleteByIdAndFlush(BEARBEITER.getId());
  }

  @Test
  @TestTransaction
  void testGetSignatureByBeschreibungID() {
    BeschreibungsService beschreibungsService = new BeschreibungsService(
        beschreibungsRepositoryMOCK,
        identifikationRepository,
        kulturObjektDokumentRepositoryMOCK,
        suchDokumentServiceMOCK,
        indexServiceMock,
        dokumentSperreBoundary,
        papierkorbBoundaryMOCK,
        teiXmlValidationBoundaryMock,
        teiXmlTransformationBoundaryMock);

    Identifikation identifikation = new IdentifikationBuilder()
        .withId("007")
        .withIdent(GUELTIGE_SIGNATURE)
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .build();

    BeschreibungsKomponenteKopf beschreibungsKomponenteKopf = new BeschreibungsKomponenteKopfBuilder()
        .withId("123")
        .withIndentifikationen(Set.of(identifikation))
        .build();

    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withId(ENTITY_ID)
        .addBeschreibungsKomponente(beschreibungsKomponenteKopf)
        .build();

    assertNull(beschreibungsService.getSignatureForBeschreibungId(ENTITY_ID));

    beschreibungsRepository.save(beschreibung);

    SignatureValue actual = beschreibungsService.getSignatureForBeschreibungId(ENTITY_ID);
    assertNotNull(actual);

    assertEquals(GUELTIGE_SIGNATURE, actual.getSignature());

  }

  @Test
  @TestTransaction
  void testGetAllSignaturesWithBeschreibungIDs() {
    BeschreibungsService beschreibungsService = new BeschreibungsService(
        beschreibungsRepositoryMOCK,
        identifikationRepository, kulturObjektDokumentRepositoryMOCK,
        suchDokumentServiceMOCK,
        indexServiceMock,
        dokumentSperreBoundary,
        papierkorbBoundaryMOCK,
        teiXmlValidationBoundaryMock,
        teiXmlTransformationBoundaryMock);
    Identifikation identifikation = new IdentifikationBuilder()
        .withId("007")
        .withIdent(GUELTIGE_SIGNATURE)
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .build();

    BeschreibungsKomponenteKopf beschreibungsKomponenteKopf = new BeschreibungsKomponenteKopfBuilder()
        .withId("123")
        .withIndentifikationen(Set.of(identifikation))
        .build();

    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withId(ENTITY_ID)
        .addBeschreibungsKomponente(beschreibungsKomponenteKopf)
        .build();

    assertTrue(beschreibungsService.getAllSignaturesWithBeschreibungIDs().isEmpty());

    beschreibungsRepository.save(beschreibung);

    Map<String, SignatureValue> actual = beschreibungsService.getAllSignaturesWithBeschreibungIDs();
    assertNotNull(actual);

    assertFalse(actual.isEmpty());

    assertEquals(GUELTIGE_SIGNATURE, actual.get(ENTITY_ID).getSignature());

  }

  @Test
  void testUpdateBeschreibungMitXML() throws Exception {
    final String id = "mss_36-23-aug-2f_tei-msDesc_Westphal";
    final String kodID = "1";
    Beschreibung code166 = new Beschreibung.BeschreibungsBuilder(id, kodID)
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .build();

    BeschreibungsService beschreibungsService = new BeschreibungsService(
        beschreibungsRepositoryMOCK,
        identifikationRepositoryMOCK, kulturObjektDokumentRepositoryMOCK,
        suchDokumentServiceMOCK,
        indexServiceMock,
        dokumentSperreBoundaryMOCK,
        papierkorbBoundaryMOCK,
        teiXmlValidationBoundaryMock,
        teiXmlTransformationBoundaryMock);

    Path teiFilePath = Paths
        .get("../domainmodel-tei-mapper/src", "test", "resources", "tei",
            "tei-msDesc_Westphal.xml");

    when(beschreibungsService.findById(id)).thenReturn(Optional.of(code166));
    when(beschreibungsService.findById("wrongId")).thenReturn(Optional.of(code166));
    when(beschreibungsService.findById("notFound")).thenReturn(Optional.empty());

    Locale locale = Locale.ENGLISH;
    String xml = Files.readString(teiFilePath);

    Throwable exceptionThatWasThrown;

    exceptionThatWasThrown = assertThrows(BeschreibungsException.class,
        () -> beschreibungsService.updateBeschreibungMitXML("wrongId", xml, locale));
    assertEquals("The id of the description wrongId does not match the XML.", exceptionThatWasThrown.getMessage());

    exceptionThatWasThrown = assertThrows(BeschreibungsException.class,
        () -> beschreibungsService.updateBeschreibungMitXML("notFound", xml, locale));
    assertEquals("No description was found for id notFound.", exceptionThatWasThrown.getMessage());

    beschreibungsService.updateBeschreibungMitXML(id, xml, Locale.GERMAN);

    doThrow(new SolrUebernahmeException("Test"))
        .when(suchDokumentServiceMOCK).beschreibungUebernehmen(any(Beschreibung.class));

    exceptionThatWasThrown = assertThrows(BeschreibungsException.class,
        () -> beschreibungsService.updateBeschreibungMitXML(id, xml, locale));
    assertEquals("An error occurred while saving the description: Test", exceptionThatWasThrown.getMessage());

    verify(beschreibungsRepositoryMOCK, times(2)).save(any());
  }

  @Test
  @TestTransaction
  void buildBeschreibungsViewModel() {

    KulturObjektDokument kod = createKulturObjektDokument();
    Beschreibung beschreibung = createBeschreibung(kod);

    kulturObjektDokumentRepository.save(kod);
    beschreibungsRepository.save(beschreibung);

    BeschreibungsService beschreibungsService = new BeschreibungsService(beschreibungsRepository,
        identifikationRepository, kulturObjektDokumentRepository,
        suchDokumentServiceMOCK,
        indexServiceMock,
        dokumentSperreBoundary,
        papierkorbBoundaryMOCK,
        teiXmlValidationBoundaryMock,
        teiXmlTransformationBoundaryMock);

    Optional<BeschreibungsViewModel> beschreibungsViewModelOptional = beschreibungsService
        .buildBeschreibungsViewModel(beschreibung.getId());

    assertTrue(beschreibungsViewModelOptional.isPresent());
    BeschreibungsViewModel beschreibungsViewModel = beschreibungsViewModelOptional.get();

    assertEquals("Mscr.Dresd.A.111", beschreibungsViewModel.getSignatur());
    assertEquals("Mscr.Dresd.A.111", beschreibungsViewModel.getKodSignatur());
    assertEquals("Flavius Josephus, de bello Judaico", beschreibungsViewModel.getTitel());
    assertEquals("Publikation{id='1', datumDerVeroeffentlichung=1982-01-02T00:00, publikationsTyp=ERSTPUBLIKATION}"
            + "Publikation{id='2', datumDerVeroeffentlichung=1983-03-04T00:00, publikationsTyp=PUBLIKATION_HSP}",
        beschreibungsViewModel.getPublikationen().stream().map(Publikation::toString)
            .collect(Collectors.joining()));

    assertEquals(1, beschreibungsViewModel.getAutoren().size());

    beschreibungsViewModel.getAutoren().stream()
        .findFirst()
        .ifPresent(autor -> assertEquals("Tester", autor.getPreferredName()));

    assertTrue(beschreibungsViewModel.getAusGedrucktemKatalog());

    assertEquals("https://resolver.url/HSP-123", beschreibungsViewModel.getHspPurl());

    assertNotNull(beschreibungsViewModel.getExterneLinks());
    assertEquals(1, beschreibungsViewModel.getExterneLinks().size());
    assertTrue(beschreibungsViewModel.getExterneLinks().stream()
        .anyMatch("https://doi.org/HSP-123"::equals));
  }

  @Test
  void testValidateXML() throws IOException {
    Path teiFilePath = Paths
        .get("src", "test", "resources", "tei-msDesc_Lesser-wthout-BOM44.xml");

    String beschreibungTEI = Files.readString(teiFilePath);

    BeschreibungsService beschreibungsService = new BeschreibungsService(
        beschreibungsRepositoryMOCK,
        identifikationRepositoryMOCK, kulturObjektDokumentRepositoryMOCK,
        suchDokumentServiceMOCK,
        indexServiceMock,
        dokumentSperreBoundary,
        papierkorbBoundaryMOCK,
        teiXmlValidationBoundaryMock,
        teiXmlTransformationBoundaryMock);

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder().withId("1")
        .withTEIXml(beschreibungTEI).build();

    ValidationResult validationResult = beschreibungsService
        .validateXML(beschreibung.getTeiXML(), null, "de");

    assertTrue(validationResult.isValid);
  }

  @Test
  void testMinimalBeschreibungErstellen() throws BeschreibungsException {

    BeschreibungsService beschreibungsService = new BeschreibungsService(
        beschreibungsRepositoryMOCK,
        identifikationRepositoryMOCK, kulturObjektDokumentRepositoryMOCK,
        suchDokumentServiceMOCK,
        indexServiceMock,
        dokumentSperreBoundary,
        papierkorbBoundaryMOCK,
        teiXmlValidationBoundaryMock,
        teiXmlTransformationBoundaryMock);

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withId(UUID.randomUUID().toString())
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(new NormdatenReferenz("12", "SBB", ""))
        .withAufbewahrungsOrt(new NormdatenReferenz("13", "Berlin", ""))
        .withIdent("Ms 1108")
        .build();

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withGueltigerIdentifikation(identifikation)
        .build();

    Set<NormdatenReferenz> autoren = new HashSet<>();
    autoren.add(new NormdatenReferenz("1", "Konrad", "Konrad3X-221", "Person"));

    NormdatenReferenz sprache = createNormdatenReferenzGerman();

    Lizenz lizenz = new LizenzBuilder().addUri("https://creativecommons.org/publicdomain/mark/1.0/")
        .build();

    Beschreibung minimal = beschreibungsService.minimalBeschreibungErstellen("KODID", sprache, kod,
        autoren, lizenz,
        TEIValues.HSP_BESCHREIBUNG_MITTELALTERLICHE_HS);

    assertNotNull(minimal);
    assertNotNull(minimal.getId());
    assertEquals(VerwaltungsTyp.INTERN, minimal.getVerwaltungsTyp());
    assertNotNull(minimal.getErstellungsDatum());
    assertNotNull(minimal.getAenderungsDatum());
    assertNotNull(minimal.getBeschreibungsSprache());
    assertEquals(sprache.getId(), minimal.getBeschreibungsSprache().getId());
    assertEquals(sprache.getGndID(), minimal.getBeschreibungsSprache().getGndID());
    assertEquals(sprache.getName(), minimal.getBeschreibungsSprache().getName());
    assertEquals(sprache.getTypeName(), minimal.getBeschreibungsSprache().getTypeName());
    assertNotNull(minimal.getBeschreibungsSprache().getVarianterName());
    assertEquals(sprache.getVarianterName().size(),
        minimal.getBeschreibungsSprache().getVarianterName().size());
    assertNotNull(minimal.getBeschreibungsSprache().getIdentifikator());
    assertEquals(sprache.getIdentifikator().size(),
        minimal.getBeschreibungsSprache().getIdentifikator().size());
    assertTrue(minimal.getGueltigeIdentifikation().isPresent());
    assertEquals(1, minimal.getBeschreibungsStruktur().size());
    assertEquals(DokumentObjektTyp.HSP_DESCRIPTION, minimal.getDokumentObjektTyp());
    assertEquals(BeschreibungsTyp.MEDIEVAL, minimal.getBeschreibungsTyp());
    assertEquals(lizenz, minimal.getLizenz());

    minimal.getBeschreibungsStruktur().forEach(kopf -> {
      assertEquals(2, kopf.getIdentifikationen().size());
      assertEquals("", ((BeschreibungsKomponenteKopf) kopf).getTitel());
    });

    assertEquals(1, minimal.getAutoren().size());
  }

  @Test
  void testBeschreibungErstellen() throws Exception {

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withId(UUID.randomUUID().toString())
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(new NormdatenReferenz("12", "SBB", ""))
        .withAufbewahrungsOrt(new NormdatenReferenz("13", "Berlin", ""))
        .withIdent("Ms 1108")
        .build();

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId(TEIValues.UUID_PREFIX + UUID.randomUUID())
        .withGueltigerIdentifikation(identifikation)
        .withRegistrierungsDatum(LocalDateTime.now())
        .withTEIXml(Files.readString(Paths.get("src", "test", "resources", "kodinitial.xml")))
        .build();

    NormdatenReferenz sprache = createSprache();

    Set<NormdatenReferenz> autoren = new HashSet<>();
    autoren.add(new NormdatenReferenz("1", "Konrad", "Konrad3X-221", "Person"));

    BeschreibungsRepository beschreibungsRepositoryMock = mock(BeschreibungsRepository.class);
    KulturObjektDokumentBoundary kulturObjektDokumentBoundaryMock = mock(
        KulturObjektDokumentBoundary.class);
    KulturObjektDokumentRepository kulturObjektDokumentRepositoryMock = mock(
        KulturObjektDokumentRepository.class);
    SuchDokumentService suchDokumentServiceMock = mock(SuchDokumentService.class);

    BeschreibungsService beschreibungsService = new BeschreibungsService(
        beschreibungsRepositoryMock,
        identifikationRepositoryMOCK, kulturObjektDokumentRepositoryMock, suchDokumentServiceMock,
        indexServiceMock,
        dokumentSperreBoundary,
        papierkorbBoundaryMOCK,
        teiXmlValidationBoundaryMock,
        teiXmlTransformationBoundaryMock);

    when(kulturObjektDokumentRepositoryMock.findById(anyString())).thenReturn(kod);

    Lizenz lizenz = new LizenzBuilder().addUri("https://creativecommons.org/publicdomain/mark/1.0/")
        .build();

    Beschreibung beschreibung = beschreibungsService.beschreibungErstellen(
        new Bearbeiter("1", "Unbekannter Tester"), kod.getId(), sprache,
        autoren, lizenz, TEIValues.HSP_BESCHREIBUNG_MITTELALTERLICHE_HS,
        kulturObjektDokumentBoundaryMock);

    assertNotNull(beschreibung);
    assertNotNull(beschreibung.getId());
    assertEquals(VerwaltungsTyp.INTERN, beschreibung.getVerwaltungsTyp());
    assertNotNull(beschreibung.getErstellungsDatum());
    assertNotNull(beschreibung.getAenderungsDatum());
    assertNotNull(beschreibung.getBeschreibungsSprache());
    assertEquals(sprache.getId(), beschreibung.getBeschreibungsSprache().getId());
    assertEquals(sprache.getGndID(), beschreibung.getBeschreibungsSprache().getGndID());
    assertEquals(sprache.getName(), beschreibung.getBeschreibungsSprache().getName());
    assertEquals(sprache.getTypeName(), beschreibung.getBeschreibungsSprache().getTypeName());
    assertNotNull(beschreibung.getBeschreibungsSprache().getVarianterName());
    assertEquals(sprache.getVarianterName().size(),
        beschreibung.getBeschreibungsSprache().getVarianterName().size());
    assertNotNull(beschreibung.getBeschreibungsSprache().getIdentifikator());
    assertEquals(sprache.getIdentifikator().size(),
        beschreibung.getBeschreibungsSprache().getIdentifikator().size());
    assertTrue(beschreibung.getGueltigeIdentifikation().isPresent());
    assertEquals(1, beschreibung.getBeschreibungsStruktur().size());
    assertEquals(BeschreibungsTyp.MEDIEVAL, beschreibung.getBeschreibungsTyp());
    assertEquals(beschreibung.getLizenz(), lizenz);
    beschreibung.getBeschreibungsStruktur().forEach(kopf -> {
      assertEquals(2, kopf.getIdentifikationen().size());
      assertEquals("", ((BeschreibungsKomponenteKopf) kopf).getTitel());
    });

    assertEquals(1, beschreibung.getAutoren().size());

    verify(beschreibungsRepositoryMock, times(1)).save(beschreibung);
    verify(suchDokumentServiceMock, times(1)).beschreibungUebernehmen(beschreibung);
    verify(kulturObjektDokumentBoundaryMock, times(1)).saveKulturObjektDokument(kod,
        ActivityStreamAction.ADD);
  }

  @Test
  @TestTransaction
  void testSperren() throws Exception {
    KulturObjektDokument kod = createKulturObjektDokument();
    Beschreibung beschreibung = createBeschreibung(kod);

    kulturObjektDokumentRepository.save(kod);
    beschreibungsRepository.save(beschreibung);
    BeschreibungsService beschreibungsService = new BeschreibungsService(beschreibungsRepository,
        identifikationRepositoryMOCK, kulturObjektDokumentRepository, suchDokumentServiceMOCK,
        indexServiceMock,
        dokumentSperreBoundary,
        papierkorbBoundaryMOCK,
        teiXmlValidationBoundaryMock,
        teiXmlTransformationBoundaryMock);

    Sperre sperre_1 = beschreibungsService.beschreibungSperren(
        new Bearbeiter("1", "Unbekannter Tester"), beschreibung.getId());
    assertNotNull(sperre_1);
    assertNotNull(sperre_1.getId());
    assertEquals(BEARBEITER, sperre_1.getBearbeiter());
    assertEquals(SperreTyp.MANUAL, sperre_1.getSperreTyp());
    assertEquals("BeschreibungBearbeiten", sperre_1.getSperreGrund());
    assertNotNull(sperre_1.getSperreEintraege());
    assertTrue(sperre_1.getSperreEintraege().stream()
        .anyMatch(se -> beschreibung.getId().equals(se.getTargetId())
            && SperreDokumentTyp.BESCHREIBUNG == se.getTargetType()));

    Optional<Sperre> sperre_2 = beschreibungsService.findSperreForBeschreibung(
        beschreibung.getId());
    assertTrue(sperre_2.isPresent());
    assertEquals(sperre_1, sperre_2.get());

    beschreibungsService.beschreibungEntsperren(beschreibung.getId());
    Optional<Sperre> sperre_3 = beschreibungsService.findSperreForBeschreibung(
        beschreibung.getId());
    assertFalse(sperre_3.isPresent());
  }

  @Test
  @TestTransaction
  void testDeleteBeschreibung() throws Exception {
    Path kodFilePath = Paths.get("src", "test", "resources", "kod-attributsreferenzen_leer.xml");
    List<TEI> kodTeiList = TEIObjectFactory.unmarshal(newInputStream(kodFilePath));
    TEI kodTEI = kodTeiList.get(0);

    Path teiFilePath1 = Paths.get("src", "test", "resources", "tei-beschreibung-attributsreferenzen.xml");
    List<TEI> teiList1 = TEIObjectFactory.unmarshal(newInputStream(teiFilePath1));
    TEI teiBeschreibung1 = teiList1.get(0);

    Path teiFilePath2 = Paths.get("src", "test", "resources", "tei-beschreibung-attributsreferenzen_2.xml");
    List<TEI> teiList2 = TEIObjectFactory.unmarshal(newInputStream(teiFilePath2));
    TEI teiBeschreibung2 = teiList2.get(0);

    final PURL beschreibungsPURL1 = new PURL(
        URI.create("http://resolver.sbb.spk-berlin.de/HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001"),
        URI.create("http://target.sbb.spk-berlin.de/HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001"),
        PURLTyp.INTERNAL);
    Beschreibung beschreibung1 = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001")
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withTEIXml(TEIObjectFactory.marshal(teiBeschreibung1))
        .addPURL(beschreibungsPURL1)
        .build();

    Beschreibung beschreibung2 = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP-f9f6eb32-068c-3089-a7d4-bbc3e55f3235")
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withTEIXml(TEIObjectFactory.marshal(teiBeschreibung2))
        .build();

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-0037fea7-58e0-3df9-a45c-488a7ee7e752")
        .withTEIXml(TEIObjectFactory.marshal(kodTEI))
        .addBeschreibungsdokumentID(beschreibung1.getId())
        .build();

    try {

      TEIKulturObjektDokumentCommand.updateKODSourceDescWithAddBeschreibungMsDesc(kod,
          beschreibung1);
      TEIKulturObjektDokumentCommand.updateKODBeschreibungenReferenzes(kod);
      TEIKODAttributsReferenzCommand.updateAttributsReferenzen(kod, beschreibung1);

    }catch(Exception error) {
      logger.error("Error: ",error);
    }

    kod.getBeschreibungenIDs().add(beschreibung2.getId());
    TEIKulturObjektDokumentCommand.updateKODBeschreibungenReferenzes(kod);

    String updatedTEIXML = kod.getTeiXML();

    assertTrue(updatedTEIXML.contains("<msDesc xml:id=\"HSP-0037fea7-58e0-3df9-a45c-488a7ee7e752\""));
    assertTrue(updatedTEIXML.contains("<msDesc status=\"extern\" xml:id=\"HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001\""));
    assertTrue(updatedTEIXML.contains("<ref target=\"HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001\"/>"), updatedTEIXML);
    assertTrue(updatedTEIXML.contains("<ref target=\"HSP-f9f6eb32-068c-3089-a7d4-bbc3e55f3235\"/>"), updatedTEIXML);

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {
      assertTrue(
          updatedTEIXML.contains("<index indexName=\"" + attributsTyp.getIndexName() +"\" copyOf=\"#" +beschreibung1.getId() +"\" source=\""+HTTP_RESOLVER_SPK_DE_PURL+"\">"),
          updatedTEIXML);
    }

    kulturObjektDokumentRepository.save(kod);
    beschreibungsRepository.save(beschreibung1);
    beschreibungsRepository.save(beschreibung2);

    BeschreibungsService beschreibungsService = new BeschreibungsService(beschreibungsRepository,
        identifikationRepositoryMOCK, kulturObjektDokumentRepository, suchDokumentServiceMOCK,
        indexServiceMock,
        dokumentSperreBoundary,
        papierkorbBoundaryMOCK,
        teiXmlValidationBoundaryMock,
        teiXmlTransformationBoundaryMock);

    beschreibungsService.deleteBeschreibung(BEARBEITER, beschreibung1.getId());

    assertFalse(beschreibungsRepository.findByIdOptional(beschreibung1.getId()).isPresent());

    KulturObjektDokument updatedKOD = kulturObjektDokumentRepository.findById(kod.getId());
    assertNotNull(updatedKOD);
    assertNotNull(updatedKOD.getTeiXML());

    updatedTEIXML = updatedKOD.getTeiXML();

    assertTrue(updatedTEIXML.contains("<msDesc xml:id=\"HSP-0037fea7-58e0-3df9-a45c-488a7ee7e752\""));
    assertFalse(
        updatedTEIXML.contains("<msDesc status=\"extern\" xml:id=\"HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001\""));
    assertFalse(updatedTEIXML.contains("<ref target=\"HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001\"/>"), updatedTEIXML);
    assertTrue(updatedTEIXML.contains("<ref target=\"HSP-f9f6eb32-068c-3089-a7d4-bbc3e55f3235\"/>"), updatedTEIXML);

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {
      assertTrue(updatedTEIXML.contains("<index indexName=\"" + attributsTyp.getIndexName() + "\">"),
          updatedTEIXML);
    }

    verify(indexServiceMock, times(1))
        .indexKulturObjektDokumentWithKafkaTransaction(any(KulturObjektDokument.class),
            any(ActivityStreamAction.class));
    verify(papierkorbBoundaryMOCK, times(1))
        .erzeugeGeloeschtesDokument(beschreibung1, BEARBEITER);
  }

  @Test
  @TestTransaction
  void testFindLatestModified() {
    BeschreibungsService beschreibungsService = new BeschreibungsService(beschreibungsRepository,
        identifikationRepositoryMOCK, kulturObjektDokumentRepository, suchDokumentServiceMOCK,
        indexServiceMock,
        dokumentSperreBoundary,
        papierkorbBoundaryMOCK,
        teiXmlValidationBoundaryMock,
        teiXmlTransformationBoundaryMock);

    Identifikation identifikation = new IdentifikationBuilder()
        .withId("007")
        .withIdent(GUELTIGE_SIGNATURE)
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(new NormdatenReferenz("774909e2", "Staatsbibliothek zu Berlin", "5036103-X", "CorporateBody"))
        .withAufbewahrungsOrt(new NormdatenReferenz("ee1611b6", "Berlin", "4005728-8", "Place"))
        .build();

    BeschreibungsKomponenteKopf bkKopf = new BeschreibungsKomponenteKopfBuilder()
        .withId("123")
        .withKulturObjektTyp(KulturObjektTyp.CODEX)
        .withTitel("Titel")
        .withIndentifikationen(Set.of(identifikation))
        .build();

    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withId(ENTITY_ID)
        .withKodID("k-1")
        .addBeschreibungsKomponente(bkKopf)
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .withBeschreibungsTyp(BeschreibungsTyp.MEDIEVAL)
        .withErstellungsDatum(LocalDateTime.of(2021, 1, 1, 12, 0))
        .withAenderungsDatum(LocalDateTime.of(2022, 2, 2, 12, 0))
        .build();

    beschreibungsRepository.save(beschreibung);

    List<BeschreibungListDTO> beschreibungen = beschreibungsService.findLatestModified();
    assertNotNull(beschreibungen);
    assertEquals(1, beschreibungen.size());
  }

  @Test
  @TestTransaction
  void testAddPURL() throws Exception {

    PURL purlInternal = new PURL(URI.create("https://resolver.de/40170459"), URI.create("https://target.de/40170459"),
        PURLTyp.INTERNAL);

    PURL purlExternal = new PURL(URI.create("https://doi.org/40170459"), URI.create("https://doi.org/40170459"),
        PURLTyp.EXTERNAL);

    BeschreibungsService beschreibungsService = new BeschreibungsService(beschreibungsRepository,
        identifikationRepositoryMOCK, kulturObjektDokumentRepository, suchDokumentServiceMOCK,
        indexServiceMock,
        dokumentSperreBoundary,
        papierkorbBoundaryMOCK,
        teiXmlValidationBoundaryMock,
        teiXmlTransformationBoundaryMock);

    KulturObjektDokument kod = createKulturObjektDokument();
    Beschreibung beschreibung = createBeschreibung(kod);
    beschreibung.getPURLs().clear();
    beschreibung.getPURLs().add(purlExternal);
    kod.getBeschreibungenIDs().add(beschreibung.getId());
    kulturObjektDokumentRepository.save(kod);

    Path teiFilePath = Paths.get("../domainmodel-tei-mapper/src", "test", "resources", "tei",
        "tei-msDesc_Westphal.xml");
    beschreibung.setTeiXML(Files.readString(teiFilePath));

    beschreibungsRepository.save(beschreibung);

    NullPointerException npe = assertThrows(NullPointerException.class,
        () -> beschreibungsService.addPURL(null, null));
    assertEquals("beschreibungId is required", npe.getMessage());

    String beschreibungId = beschreibung.getId();
    npe = assertThrows(NullPointerException.class,
        () -> beschreibungsService.addPURL(beschreibungId, null));
    assertEquals("purl is required", npe.getMessage());

    BeschreibungsException beNoKOD = assertThrows(BeschreibungsException.class,
        () -> beschreibungsService.addPURL("INVALID", purlInternal));
    assertEquals("Zur Id INVALID wurde keine Beschreibung gefunden!", beNoKOD.getMessage());

    beschreibungsService.addPURL(beschreibung.getId(), purlInternal);

    Optional<Beschreibung> updated = beschreibungsRepository.findByIdOptional(beschreibung.getId());
    assertTrue(updated.isPresent());

    assertEquals(2, updated.get().getPURLs().size());
    assertTrue(updated.get().getPURLs().stream().anyMatch(
        purl -> PURLTyp.INTERNAL == purl.getTyp()
            && "https://resolver.de/40170459".equals(purl.getPurl().toASCIIString())
            && "https://target.de/40170459".equals(purl.getTarget().toASCIIString())
    ));

    verify(suchDokumentServiceMOCK, times(1)).beschreibungUebernehmen(any(Beschreibung.class));
    verify(indexServiceMock, times(1))
        .indexKulturObjektDokumentWithKafkaTransaction(any(KulturObjektDokument.class),
            any(ActivityStreamAction.class));
  }

  @Test
  @TestTransaction
  void testSendIndexJob() {
    BeschreibungsService beschreibungsService = new BeschreibungsService(beschreibungsRepository,
        identifikationRepositoryMOCK, kulturObjektDokumentRepository, suchDokumentServiceMOCK,
        indexServiceMock,
        dokumentSperreBoundary,
        papierkorbBoundaryMOCK,
        teiXmlValidationBoundaryMock,
        teiXmlTransformationBoundaryMock);

    beschreibungsService.indexKulturObjektDokument(new KulturObjektDokumentBuilder().withId("test").build());
    verify(indexServiceMock, times(1))
        .indexKulturObjektDokumentWithKafkaTransaction(any(KulturObjektDokument.class),
            any(ActivityStreamAction.class));
  }

  @Test
  void testMapBeschreibungFromXML() throws IOException, BeschreibungsException {
    BeschreibungsService beschreibungsService = new BeschreibungsService(beschreibungsRepository,
        identifikationRepositoryMOCK, kulturObjektDokumentRepository, suchDokumentServiceMOCK,
        indexServiceMock,
        dokumentSperreBoundary,
        papierkorbBoundaryMOCK,
        teiXmlValidationBoundaryMock,
        teiXmlTransformationBoundaryMock);

    Locale locale = Locale.ENGLISH;

    Throwable exceptionThatWasThrown;

    exceptionThatWasThrown = assertThrows(BeschreibungsException.class,
        () -> beschreibungsService.mapBeschreibungFromXML(null, locale));
    assertEquals("An error occurred while parsing the XML: null", exceptionThatWasThrown.getMessage());

    exceptionThatWasThrown = assertThrows(BeschreibungsException.class,
        () -> beschreibungsService.mapBeschreibungFromXML("<teiCorpus xmlns=\"http://www.tei-c.org/ns/1.0\" />",
            locale));
    assertEquals("The XML does not contain a TEI element.", exceptionThatWasThrown.getMessage());

    exceptionThatWasThrown = assertThrows(BeschreibungsException.class,
        () -> beschreibungsService.mapBeschreibungFromXML(
            "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\"/>", locale));
    assertEquals("The MsDesc element is missing or ambiguous.", exceptionThatWasThrown.getMessage());

    exceptionThatWasThrown = assertThrows(BeschreibungsException.class,
        () -> beschreibungsService.mapBeschreibungFromXML(
            "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\"><msDesc/><msDesc/></TEI>", locale));
    assertEquals("The MsDesc element is missing or ambiguous.", exceptionThatWasThrown.getMessage());

    Path teiFilePath = Paths.get("../domainmodel-tei-mapper/src", "test", "resources", "tei",
        "tei-msDesc_Westphal.xml");

    Beschreibung beschreibung = beschreibungsService.mapBeschreibungFromXML(Files.readString(teiFilePath), locale);
    assertNotNull(beschreibung);
  }

  @Test
  void testAddBeschreibungToActivityStreamMessage() throws Exception {
    ActivityStream message = ActivityStream.builder().build();
    Beschreibung beschreibung = createBeschreibung(createKulturObjektDokument());
    beschreibung.setTeiXML("<TEI xmlns=\"http://www.tei-c.org/ns/1.0\"/>");

    Throwable exceptionThatWasThrown = assertThrows(NullPointerException.class,
        () -> BeschreibungsService.addBeschreibungToActivityStreamMessage(null, null));
    assertEquals("message is required!", exceptionThatWasThrown.getMessage());

    exceptionThatWasThrown = assertThrows(NullPointerException.class,
        () -> BeschreibungsService.addBeschreibungToActivityStreamMessage(message, null));
    assertEquals("beschreibung is required!", exceptionThatWasThrown.getMessage());

    BeschreibungsService.addBeschreibungToActivityStreamMessage(message, beschreibung);
    assertNotNull(message.getObjects());
    assertEquals(1, message.getObjects().size());

    ActivityStreamObject activityStreamObject = message.getObjects().iterator().next();
    assertEquals(beschreibung.getId(), activityStreamObject.getId());
    assertArrayEquals(beschreibung.getTeiXML().getBytes(StandardCharsets.UTF_8), activityStreamObject.getContent(),
        "Content not equal");
    assertTrue(activityStreamObject.isCompressed());
    assertSame(ActivityStreamsDokumentTyp.BESCHREIBUNG, activityStreamObject.getType());
  }

  private NormdatenReferenz createSprache() {
    return new NormdatenReferenzBuilder()
        .withId(UUID.randomUUID().toString())
        .withTypeName("Language")
        .withName("Deutsch")
        .withGndID("4113292-0")
        .addIdentifikator(
            new Identifikator("deu", "https://id.loc.gov/vocabulary/iso639-1/de", "ISO_639-1"))
        .addIdentifikator(
            new Identifikator("deu", "https://id.loc.gov/vocabulary/iso639-2/deu", "ISO_639-2"))
        .addIdentifikator(
            new Identifikator("ger", "https://id.loc.gov/vocabulary/iso639-2/ger", "ISO_639-2"))
        .addIdentifikator(new Identifikator("de", "", ""))
        .addVarianterName(new VarianterName("Neuhochdeutsch", "de"))
        .addVarianterName(new VarianterName("Deutsche Sprache", "de"))
        .addVarianterName(new VarianterName("Hochdeutsch", "de"))
        .addVarianterName(new VarianterName("Deutsch", "de"))
        .build();
  }

  private KulturObjektDokument createKulturObjektDokument() {
    NormdatenReferenz besitzer = new NormdatenReferenz("", "Staatsbibliothek zu Berlin", "");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
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

  private Beschreibung createBeschreibung(KulturObjektDokument kod) {
    Publikation publikation2 = new Publikation("1", LocalDateTime.of(1982, 1, 2, 0, 0),
        PublikationsTyp.ERSTPUBLIKATION);
    Publikation publikation3 = new Publikation("2", LocalDateTime.of(1983, 3, 4, 0, 0),
        PublikationsTyp.PUBLIKATION_HSP);

    PURL internal = new PURL(URI.create("https://resolver.url/HSP-123"),
        URI.create("https://target.url/HSP-123"), PURLTyp.INTERNAL);
    PURL external = new PURL(URI.create("https://doi.org/HSP-123"),
        URI.create("https://doi.org/HSP-123"), PURLTyp.EXTERNAL);

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopfBuilder()
        .withId("777")
        .withTitel("Flavius Josephus, de bello Judaico")
        .withIndentifikationen(new LinkedHashSet<>(List.of(kod.getGueltigeIdentifikation())))
        .build();
    String beschreibungId = "40170459";
    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withId(beschreibungId)
        .withKodID(kod.getId())
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .addPublikation(publikation2)
        .addPublikation(publikation3)
        .addAutor(new NormdatenReferenz("Tester", "GNDID"))
        .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION_RETRO)
        .addPURL(internal)
        .addPURL(external)
        .build();
    beschreibung.getBeschreibungsStruktur().add(kopf);
    return beschreibung;
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
            new Identifikator("ger", "https://id.loc.gov/vocabulary/iso639-2/ger", "ISO_639-2"))
        .addIdentifikator(
            new Identifikator("deu", "https://id.loc.gov/vocabulary/iso639-2/deu", "ISO_639-2"))
        .addIdentifikator(
            new Identifikator("de", "https://id.loc.gov/vocabulary/iso639-1/de", "ISO_639-1"))
        .withGndID("4113292-0")
        .build();
  }

}
