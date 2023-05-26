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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary.SYSTEM_USERNAME;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Status;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Titel;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Umfang;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AbmessungWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DigitalisatTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.EntstehungszeitWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.FormatWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel.BeschreibungsViewBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DigitalisatViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel.KulturObjektDokumentViewModelBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.SignatureValue;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodregistrieren.KulturObjektDokumentRegistrierenException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodregistrieren.KulturObjektDokumentRegistrierenException.ERROR_TYPE;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.IdentifikationRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.papierkorb.PapierkorbBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.SperreRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SolrUebernahmeException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.transformation.TeiXmlTransformationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.IdentifikationRepositoryAdapter;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import org.apache.commons.io.FileUtils;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatformException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

@QuarkusTest
@Execution(ExecutionMode.SAME_THREAD)
class KulturObjektDokumentServiceTest {

  public static final String ENTITY_ID = "entityId";
  public static final String GUELTIGE_SIGNATURE = "a-007-777";
  public static final Bearbeiter BEARBEITER = new Bearbeiter("1", "Unbekannter Bearbeiter");
  private static final String TEI_FILENAME = "kulturobjekteimport.xml";
  private static final Path TEI_FILE_PATH = Paths.get("src", "test", "resources", TEI_FILENAME);
  private static final String TEI_KOD_MSDESC_BESCHREIBUNG = "tei-kod-beschreibung.xml";
  private static final Path TEI_KOD_MSDESC_BESCHREIBUNG_PATH = Paths.get("src", "test", "resources",
      TEI_KOD_MSDESC_BESCHREIBUNG);

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
  TeiXmlTransformationBoundary teiXmlTransformationServiceMock;
  @Inject
  TeiXmlValidationBoundary teiXmlValidationServiceMock;
  @Inject
  NormdatenReferenzRepository normdatenReferenzRepository;

  KulturObjektDokumentRepository kulturObjektDokumentRepositoryMOCK;
  IdentifikationRepository identifikationRepositoryMOCK;
  BeschreibungsRepository beschreibungsRepositoryMOCK;
  BeschreibungsService beschreibungServiceMOCK;
  SuchDokumentService suchDokumentServiceMOCK;
  DokumentSperreService dokumentSperreServiceMOCK;
  KafkaIndexingProducer kafkaIndexingProducerMOCK;
  PURLBoundary purlServiceMOCK;
  PapierkorbBoundary papierkorbServiceMOCK;

  @BeforeEach
  @Transactional(TxType.REQUIRES_NEW)
  void beforeEach() {
    kulturObjektDokumentRepositoryMOCK = mock(KulturObjektDokumentRepository.class);
    identifikationRepositoryMOCK = mock(IdentifikationRepositoryAdapter.class);
    beschreibungsRepositoryMOCK = mock(BeschreibungsRepository.class);
    beschreibungServiceMOCK = mock(BeschreibungsService.class);
    suchDokumentServiceMOCK = mock(SuchDokumentService.class);
    dokumentSperreServiceMOCK = mock(DokumentSperreService.class);
    kafkaIndexingProducerMOCK = mock(KafkaIndexingProducer.class);
    purlServiceMOCK = mock(PURLBoundary.class);
    papierkorbServiceMOCK = mock(PapierkorbBoundary.class);

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
  void testGetAllSignaturesWithKodIDs() {
    KulturObjektDokumentService service = new KulturObjektDokumentService(
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
        teiXmlTransformationServiceMock, teiXmlValidationServiceMock);
    Identifikation identifikation = new IdentifikationBuilder()
        .withId("007")
        .withIdent(GUELTIGE_SIGNATURE)
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .build();

    KulturObjektDokument kulturObjektDokument = new KulturObjektDokumentBuilder()
        .withId(ENTITY_ID)
        .withGueltigerIdentifikation(identifikation)
        .build();

    int initialSize = service.getAllSignaturesWithKodIDs().size();

    kulturObjektDokumentRepository.save(kulturObjektDokument);

    Map<String, SignatureValue> actual = service.getAllSignaturesWithKodIDs();
    assertNotNull(actual);

    assertFalse(actual.isEmpty());

    assertEquals(initialSize + 1L, actual.size());

    assertEquals(GUELTIGE_SIGNATURE, actual.get(ENTITY_ID).getSignature());
    kulturObjektDokumentRepository.deleteByIdAndFlush(kulturObjektDokument.getId());
  }

  @Test
  void testFindById() {
    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepositoryMOCK,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK, dokumentSperreServiceMOCK, kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock, teiXmlValidationServiceMock);

    service.findById("12");

    verify(kulturObjektDokumentRepositoryMOCK, times(1)).findByIdOptional("12");
  }

  @Test
  void testFindAll() {
    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepositoryMOCK,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK, dokumentSperreServiceMOCK, kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock, teiXmlValidationServiceMock);

    service.findAll();

    verify(kulturObjektDokumentRepositoryMOCK, times(1)).listAll(true);
  }

  @Test
  void testregistrieren()
      throws IOException, KulturObjektDokumentRegistrierenException, SperreAlreadyExistException {
    Path csvFile = Paths.get("src", "test", "resources", "BSB_SignList.csv");

    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepositoryMOCK,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK, dokumentSperreServiceMOCK, kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    NormdatenReferenz modelKoerperschaft = new NormdatenReferenz(
        TEIValues.UUID_PREFIX + UUID.randomUUID(),
        "Staatsbibliothek zu Berlin", "234234-x");

    NormdatenReferenz modelOrt = new NormdatenReferenz(
        TEIValues.UUID_PREFIX + UUID.randomUUID(),
        "Berlin", "4005728-8");

    Collection<KulturObjektDokument> kods = service
        .registrieren(new Bearbeiter("1", "Unbekannter Tester"), modelOrt, modelKoerperschaft,
            Files.readAllBytes(csvFile));

    assertEquals(53, kods.size());
  }

  @Test
  void testRegistrieren_list()
      throws KulturObjektDokumentRegistrierenException, SperreAlreadyExistException, PURLException {
    when(purlServiceMOCK.createNewPURL(anyString(), any(DokumentObjektTyp.class)))
        .thenAnswer(i -> new PURL(URI.create("https://resolver.de/" + i.getArguments()[0]),
            URI.create("https://target.de/" + i.getArguments()[0]), PURLTyp.INTERNAL));

    NormdatenReferenz koerperschaft = new NormdatenReferenz(
        TEIValues.UUID_PREFIX + UUID.randomUUID(),
        "Staatsbibliothek zu Berlin", "234234-x");

    NormdatenReferenz ort = new NormdatenReferenz(
        TEIValues.UUID_PREFIX + UUID.randomUUID(),
        "Berlin", "4005728-8");

    KulturObjektDokument kod1 = new KulturObjektDokument(
        new KulturObjektDokumentBuilder()
            .withId("HSP-1")
            .withGueltigerIdentifikation(new Identifikation(new IdentifikationBuilder()
                .withId(UUID.randomUUID().toString())
                .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
                .withIdent("Ms. germ. oct. 6")
                .withAufbewahrungsOrt(ort)
                .withBesitzer(koerperschaft)))
            .addAlternativeIdentifikation(new Identifikation(new IdentifikationBuilder()
                .withId(UUID.randomUUID().toString())
                .withIdentTyp(IdentTyp.ALTSIGNATUR)
                .withIdent("Ms.germ.oct. 6")
                .withAufbewahrungsOrt(ort)
                .withBesitzer(koerperschaft)))
            .addAlternativeIdentifikation(new Identifikation(new IdentifikationBuilder()
                .withId(UUID.randomUUID().toString())
                .withIdentTyp(IdentTyp.ALTSIGNATUR)
                .withIdent("Ms.germ.oct. 6")
                .withAufbewahrungsOrt(ort)
                .withBesitzer(koerperschaft)))
    );

    KulturObjektDokument kod2 = new KulturObjektDokument(
        new KulturObjektDokumentBuilder()
            .withId("HSP-1")
            .withGueltigerIdentifikation(new Identifikation(new IdentifikationBuilder()
                .withId(UUID.randomUUID().toString())
                .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
                .withIdent("Hdschr. 25")
                .withAufbewahrungsOrt(ort)
                .withBesitzer(koerperschaft)))
            .addAlternativeIdentifikation(new Identifikation(new IdentifikationBuilder()
                .withId(UUID.randomUUID().toString())
                .withIdentTyp(IdentTyp.ALTSIGNATUR)
                .withIdent("Hdschr 25")
                .withAufbewahrungsOrt(ort)
                .withBesitzer(koerperschaft)))
    );

    List<KulturObjektDokument> kodsZuRegistrieren = Arrays.asList(kod1, kod2);

    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepositoryMOCK,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK, dokumentSperreServiceMOCK, kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        true,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    Collection<KulturObjektDokument> kodsRegistriert = service.registrieren(
        new Bearbeiter("1", "Unbekannter Tester"), kodsZuRegistrieren);

    assertNotNull(kodsRegistriert);
    assertEquals(2, kodsRegistriert.size());
    verify(kulturObjektDokumentRepositoryMOCK, times(2)).save(any(KulturObjektDokument.class));
    verify(purlServiceMOCK, times(2)).createNewPURL(any(String.class), any(DokumentObjektTyp.class));

    kodsRegistriert.forEach(kod ->
        assertTrue(kod.getPURLs().stream()
            .anyMatch(purl -> PURLTyp.INTERNAL == purl.getTyp())));
  }

  @Test
  void testCheckBadEncoding() {
    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepositoryMOCK,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK, dokumentSperreServiceMOCK, kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    Throwable exceptionThatWasThrown = assertThrows(KulturObjektDokumentRegistrierenException.class,
        () ->
            service.checkEncoding(StandardCharsets.ISO_8859_1.encode("Cod. I.2.2º 1").array()));

    Assertions.assertEquals("Signaturen CSV Datei hat keine UTF-8- oder US-ASCII-Codierung! (WINDOWS-1252)",
        exceptionThatWasThrown.getMessage());

  }

  @Test
  void testregistrierenWithKODAlreadyExists() {
    Path csvFile = Paths.get("src", "test", "resources", "BSB_SignList.csv");

    NormdatenReferenz koerperschaft = new NormdatenReferenz(
        TEIValues.UUID_PREFIX + UUID.randomUUID(),
        "Staatsbibliothek zu Berlin", "234234-x");

    NormdatenReferenz ort = new NormdatenReferenz(
        TEIValues.UUID_PREFIX + UUID.randomUUID(),
        "Berlin", "4005728-8");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Cbm Cat. 1").withIdentTyp(
            IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(koerperschaft)
        .withAufbewahrungsOrt(ort)
        .build();
    KulturObjektDokument kulturObjektDokumentAlreadyExist = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withGueltigerIdentifikation(identifikation)
        .build();

    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepositoryMOCK,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK, dokumentSperreServiceMOCK, kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    when(kulturObjektDokumentRepositoryMOCK.findByIdentifikationIdentAndBesitzerIDAndAufbewahrungsortID("Cbm Cat. 1", koerperschaft.getId(), ort.getId()))
        .thenReturn(List.of(kulturObjektDokumentAlreadyExist));

    Throwable exceptionThatWasThrown = assertThrows(KulturObjektDokumentRegistrierenException.class,
        () ->
            service.registrieren(new Bearbeiter("1", "Unbekannter Tester"), ort, koerperschaft,
                Files.readAllBytes(csvFile)));

    assertEquals("Kulturobjekt mit gültiger Signatur bereits im System: Cbm Cat. 1",
        exceptionThatWasThrown.getMessage());
  }

  @Test
  void testRegistrierenWithKODExistsInDatei() throws Exception {
    Path csvFile = Paths.get("src", "test", "resources", "BSB_SignList.csv");

    NormdatenReferenz modelKoerperschaft = new NormdatenReferenz(
        TEIValues.UUID_PREFIX + UUID.randomUUID(),
        "Staatsbibliothek zu Berlin", "234234-x");

    NormdatenReferenz modelOrt = new NormdatenReferenz(
        TEIValues.UUID_PREFIX + UUID.randomUUID(),
        "Berlin", "4005728-8");

    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepositoryMOCK,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK, dokumentSperreServiceMOCK, kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    final String csvData = Files.readString(csvFile, StandardCharsets.UTF_8)
        + "\"Ms. lat. oct. 118\"\n";

    KulturObjektDokumentRegistrierenException exceptionThatWasThrown = assertThrows(
        KulturObjektDokumentRegistrierenException.class,
        () ->
            service.registrieren(new Bearbeiter("1", "Unbekannter Tester"),
                modelOrt, modelKoerperschaft, csvData.getBytes(StandardCharsets.UTF_8)));

    assertEquals(
        "Gültige Signatur mehrfach in Datei vorhanden: Ms. lat. oct. 118",
        exceptionThatWasThrown.getMessage());
    assertEquals(
        ERROR_TYPE.GUELTIGE_SIGNATUR_IN_DATEI,
        exceptionThatWasThrown.getErrorType());
  }

  @Test
  void testregistrierenWithTechnicalError() {
    Path csvFile = Paths.get("src", "test", "resources", "BSB_SignList.csv");

    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepositoryMOCK,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK, dokumentSperreServiceMOCK, kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    NormdatenReferenz modelKoerperschaft = new NormdatenReferenz(
        TEIValues.UUID_PREFIX + UUID.randomUUID(),
        "Staatsbibliothek zu Berlin", "234234-x");

    NormdatenReferenz modelOrt = new NormdatenReferenz(
        TEIValues.UUID_PREFIX + UUID.randomUUID(),
        "Berlin", "4005728-8");

    when(kulturObjektDokumentRepositoryMOCK.save(any()))
        .thenThrow(new JtaPlatformException("Datenbank Fehler!"));

    Throwable error = Assertions.assertThrows(KulturObjektDokumentRegistrierenException.class, () ->
        service.registrieren(new Bearbeiter("1", "Unbekannter Tester"), modelOrt,
            modelKoerperschaft, Files.readAllBytes(csvFile)));

    assertEquals(
        "Kulturobjektdokument Cbm Cat. 1 konnte nicht gespeichert werden! Datenbank Fehler!",
        error.getMessage());
  }

  @Test
  void testcheckIfKulturObjektAlreadyExists() {
    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepositoryMOCK,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK, dokumentSperreServiceMOCK, kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Signatur")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(new NormdatenReferenz("1", "Staatsbibliothek zu Berlin", ""))
        .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", ""))
        .build();

    Identifikation unkownIdentifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Unkown")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(new NormdatenReferenz("1", "Staatsbibliothek zu Berlin", ""))
        .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", ""))
        .build();

    Identifikation alternativeIdentifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Alternative Signatur")
        .withIdentTyp(IdentTyp.ALTERNATIVE_SCHREIBUNG)
        .withBesitzer(new NormdatenReferenz("1", "Staatsbibliothek zu Berlin", ""))
        .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", ""))
        .build();

    KulturObjektDokument kulturObjektDokumentAlreadyExist = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withGueltigerIdentifikation(identifikation)
        .build();

    KulturObjektDokument kulturObjektDokumentAlternative = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withGueltigerIdentifikation(unkownIdentifikation)
        .addAllAlternativeIdentifikationen(Set.of(alternativeIdentifikation, identifikation))
        .build();

    KulturObjektDokument kulturObjektDokumentSuccess = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withGueltigerIdentifikation(unkownIdentifikation)
        .build();

    when(kulturObjektDokumentRepositoryMOCK.findByIdentifikationIdentAndBesitzerIDAndAufbewahrungsortID("Signatur", "1",
        "2"))
        .thenReturn(List.of(kulturObjektDokumentAlreadyExist));

    List<KulturObjektDokument> result;

    result = service.checkIfKulturObjektAlreadyExists(List.of(kulturObjektDokumentAlreadyExist));
    assertNotNull(result);
    assertTrue(result.contains(kulturObjektDokumentAlreadyExist));

    result = service.checkIfKulturObjektAlreadyExists(List.of(kulturObjektDokumentAlternative));
    assertNotNull(result);
    assertTrue(result.isEmpty());

    result = service.checkIfKulturObjektAlreadyExists(List.of(kulturObjektDokumentSuccess));
    assertNotNull(result);
    assertTrue(result.isEmpty());

  }

  @Test
  void testCheckIfKulturObjektAlreadyInDatei() {
    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepositoryMOCK,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK, dokumentSperreServiceMOCK, kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    Identifikation identifikation_1 = new Identifikation.IdentifikationBuilder()
        .withIdent("Signatur 1")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(new NormdatenReferenz("1", "Staatsbibliothek zu Berlin", ""))
        .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", ""))
        .build();

    Identifikation identifikation_2 = new Identifikation.IdentifikationBuilder()
        .withIdent("Signatur 2")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(new NormdatenReferenz("1", "Staatsbibliothek zu Berlin", ""))
        .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", ""))
        .build();

    KulturObjektDokument kod_1 = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withGueltigerIdentifikation(identifikation_1)
        .build();

    KulturObjektDokument kod_2 = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withGueltigerIdentifikation(identifikation_2)
        .build();

    List<KulturObjektDokument> result;

    result = service.checkIfKulturObjektAlreadyInDatei(List.of(kod_1, kod_2));
    assertNotNull(result);
    assertTrue(result.isEmpty());

    result = service.checkIfKulturObjektAlreadyInDatei(List.of(kod_1, kod_2, kod_1));
    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.contains(kod_1));

    result = service.checkIfKulturObjektAlreadyInDatei(List.of(kod_1, kod_2, kod_1, kod_1));
    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.contains(kod_1));
  }

  @TestTransaction
  @Test
  void testsaveKulturObjektDokumentInSystem()
      throws KulturObjektDokumentRegistrierenException, SolrUebernahmeException, IOException, ActivityStreamsException {
    KulturObjektDokument kulturObjektDokument = createKulturObjektDokument();

    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepositoryMOCK,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK, dokumentSperreServiceMOCK, kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    service.saveKulturObjektDokumentInSystem(List.of(kulturObjektDokument));

    assertNotNull(kulturObjektDokument.getTeiXML());

    verify(kulturObjektDokumentRepositoryMOCK, times(1)).save(kulturObjektDokument);

    verify(suchDokumentServiceMOCK, times(1)).kodUebernehmen(kulturObjektDokument);

    verify(kafkaIndexingProducerMOCK, times(1)).sendKulturobjektDokumentAsActivityStreamMessage(
        kulturObjektDokument, ActivityStreamAction.ADD,true,SYSTEM_USERNAME);
  }

  @Test
  void testAddKulturObjektDokumentToActivityStreamMessage() throws Exception {
    ActivityStream message = ActivityStream.builder().build();
    KulturObjektDokument kod = createKulturObjektDokument();

    Throwable exceptionThatWasThrown = assertThrows(NullPointerException.class,
        () -> KulturObjektDokumentService.addKulturObjektDokumentToActivityStreamMessage(null, null));
    assertEquals("message is required!", exceptionThatWasThrown.getMessage());

    exceptionThatWasThrown = assertThrows(NullPointerException.class,
        () -> KulturObjektDokumentService.addKulturObjektDokumentToActivityStreamMessage(message, null));
    assertEquals("kod is required!", exceptionThatWasThrown.getMessage());

    KulturObjektDokumentService.addKulturObjektDokumentToActivityStreamMessage(message, kod);
    assertNotNull(message.getObjects());
    assertEquals(1, message.getObjects().size());

    ActivityStreamObject activityStreamObjectKOD = message.getObjects().iterator().next();
    assertEquals(kod.getId(), activityStreamObjectKOD.getId());
    assertArrayEquals(kod.getTeiXML().getBytes(StandardCharsets.UTF_8), activityStreamObjectKOD.getContent(),
        "Content not equal");
    assertTrue(activityStreamObjectKOD.isCompressed());
    assertSame(ActivityStreamsDokumentTyp.KOD, activityStreamObjectKOD.getType());
  }

  private KulturObjektDokument createKulturObjektDokument() throws IOException {

    Path teiFilePath = Paths
        .get("../domainmodel-tei-mapper/src", "test", "resources", "tei",
            "tei-msDesc_Westphal.xml");

    Identifikation identifikation = new IdentifikationBuilder().withIdent("Signatur")
        .withBesitzer(new NormdatenReferenz("NR-1", "Berlin", ""))
        .withAufbewahrungsOrt(new NormdatenReferenz("NR-2", "Staatsbibliothek", ""))
        .withIdentTyp(
            IdentTyp.GUELTIGE_SIGNATUR)
        .build();

    return new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withGueltigerIdentifikation(identifikation)
        .withTEIXml(Files.readString(teiFilePath))
        .withBeschreibungsdokumentIDs(Set.of("1234"))
        .build();
  }

  private Beschreibung createBeschreibung(KulturObjektDokument kod) {
    PURL internal = new PURL(URI.create("https://resolver.url/HSP-123"),
        URI.create("https://target.url/HSP-123"), PURLTyp.INTERNAL);

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopfBuilder()
        .withId("777")
        .withTitel("Beschreibung Titel")
        .withIndentifikationen(Set.of(kod.getGueltigeIdentifikation()))
        .build();
    String beschreibungId = "1234";
    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withId(beschreibungId)
        .withKodID(kod.getId())
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .addPURL(internal)
        .withTEIXml("<TEI/>")
        .build();
    beschreibung.getBeschreibungsStruktur().add(kopf);
    return beschreibung;
  }

  @Test
  void testDigitalisatHinzufuegen() throws Exception {
    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepositoryMOCK,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK, dokumentSperreServiceMOCK, kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    KulturObjektDokument result = new KulturObjektDokumentBuilder().withId("1")
        .withTEIXml(FileUtils.readFileToString(TEI_FILE_PATH.toFile(), StandardCharsets.UTF_8))
        .build();

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel()
        .withId("1")
        .withTeiXML(FileUtils.readFileToString(TEI_FILE_PATH.toFile(), StandardCharsets.UTF_8))
        .build();

    NormdatenReferenz modelKoerperschaft = new NormdatenReferenz(
        "6790851b-9519-3874-a9fd-0839d494a3c9",
        "Staatsbibliothek zu Berlin",
        "5036103-X",
        GNDEntityFact.CORPORATE_BODY_TYPE_NAME);

    NormdatenReferenz modelOrt = new NormdatenReferenz(
        "ee1611b6-1f56-38e7-8c12-b40684dbb395",
        "Berlin",
        "4005728-8",
        GNDEntityFact.PLACE_TYPE_NAME);

    DigitalisatViewModel digitalisatViewModel = DigitalisatViewModel.builder()
        .manifestURL("https://iiif.ub.uni-leipzig.de/0000009089/manifest.json")
        .digitalisatTyp(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL)
        .digitalisierungsdatum(LocalDate.now())
        .thumbnail(
            "https://iiif.ub.uni-leipzig.de/fcgi-bin/iipsrv.fcgi?iiif=/j2k/0000/0090/0000009089/00000057.jpx/full/500,/0/default.jpg")
        .ort(modelOrt)
        .einrichtung(modelKoerperschaft)
        .build();

    Mockito.when(kulturObjektDokumentRepositoryMOCK.findByIdOptional("1"))
        .thenReturn(Optional.of(result));
    when(kulturObjektDokumentRepositoryMOCK.save(result)).thenReturn(result);

    Optional<KulturObjektDokument> kod = service
        .digitalisatHinzufuegen(kulturObjektDokumentViewModel, digitalisatViewModel);

    assertTrue(kod.isPresent());

    assertEquals(1, result.getDigitalisate().size());

    verify(kulturObjektDokumentRepositoryMOCK, times(1)).save(result);

    verify(suchDokumentServiceMOCK, times(1)).kodUebernehmen(result);

    verify(kafkaIndexingProducerMOCK, times(1)).sendKulturobjektDokumentAsActivityStreamMessage(
        result,ActivityStreamAction.UPDATE,true,SYSTEM_USERNAME);
  }

  @Test
  @TestTransaction
  void testDigitalisatHinzufuegenReal() throws Exception {
    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepository,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK, beschreibungService, dokumentSperreServiceMOCK,
        kafkaIndexingProducerMOCK,
        beschreibungsRepository,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    KulturObjektDokument result = new KulturObjektDokumentBuilder().withId("1")
        .withTEIXml(FileUtils.readFileToString(TEI_FILE_PATH.toFile(), StandardCharsets.UTF_8))
        .build();

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel()
        .withId("1")
        .withTeiXML(FileUtils.readFileToString(TEI_FILE_PATH.toFile(), StandardCharsets.UTF_8))
        .build();

    kulturObjektDokumentRepository.save(result);

    NormdatenReferenz modelKoerperschaft = new NormdatenReferenz(
        "6790851b-9519-3874-a9fd-0839d494a3c9",
        "Staatsbibliothek zu Berlin",
        "5036103-X",
        GNDEntityFact.CORPORATE_BODY_TYPE_NAME);
    normdatenReferenzRepository.saveAndFlush(modelKoerperschaft);

    NormdatenReferenz modelOrt = new NormdatenReferenz(
        "ee1611b6-1f56-38e7-8c12-b40684dbb395",
        "Berlin",
        "4005728-8",
        GNDEntityFact.PLACE_TYPE_NAME);
    normdatenReferenzRepository.saveAndFlush(modelOrt);

    DigitalisatViewModel digitalisatViewModel = DigitalisatViewModel.builder()
        .id("digiID")
        .manifestURL("https://iiif.ub.uni-leipzig.de/0000009089/manifest.json")
        .digitalisatTyp(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL)
        .digitalisierungsdatum(LocalDate.now())
        .thumbnail(
            "https://iiif.ub.uni-leipzig.de/fcgi-bin/iipsrv.fcgi?iiif=/j2k/0000/0090/0000009089/00000057.jpx/full/500,/0/default.jpg")
        .ort(modelOrt)
        .einrichtung(modelKoerperschaft)
        .build();

    Optional<KulturObjektDokument> kod = service
        .digitalisatHinzufuegen(kulturObjektDokumentViewModel, digitalisatViewModel);

    assertTrue(kod.isPresent());

    assertEquals(1, kod.get().getDigitalisate().size());
  }

  @Test
  void testDigitalisatLoeschen() throws Exception {
    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepositoryMOCK,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK, beschreibungServiceMOCK, dokumentSperreServiceMOCK,
        kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    KulturObjektDokument result = new KulturObjektDokumentBuilder().withId("1")
        .addDigitalisat(Digitalisat.DigitalisatBuilder().withID("1").build())
        .withTEIXml(FileUtils.readFileToString(TEI_FILE_PATH.toFile(), StandardCharsets.UTF_8))
        .build();

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel()
        .withId("1")
        .withTeiXML(FileUtils.readFileToString(TEI_FILE_PATH.toFile(), StandardCharsets.UTF_8))
        .build();

    DigitalisatViewModel digitalisatViewModel = DigitalisatViewModel.builder()
        .id("1")
        .manifestURL("https://iiif.ub.uni-leipzig.de/0000009089/manifest.json")
        .digitalisatTyp(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL)
        .build();

    Mockito.when(kulturObjektDokumentRepositoryMOCK.findByIdOptional("1"))
        .thenReturn(Optional.of(result));
    when(kulturObjektDokumentRepositoryMOCK.save(result)).thenReturn(result);

    assertEquals(1, result.getDigitalisate().size());

    Optional<KulturObjektDokument> kod = service
        .digitalisatLoeschen(kulturObjektDokumentViewModel, digitalisatViewModel);

    assertTrue(kod.isPresent());

    assertEquals(0, result.getDigitalisate().size());
  }

  @Test
  void testBuildKulturObjektDokumentViewModel() throws IOException {
    NormdatenReferenz besitzer = new NormdatenReferenz("", "Staatsbibliothek zu Berlin", "");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .build();

    Identifikation altIdentifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr. Dresd. A. 111")
        .withIdentTyp(IdentTyp.ALTERNATIVE_SCHREIBUNG)
        .withBesitzer(besitzer)
        .build();

    PURL purlInternal = new PURL(URI.create("https://resolver.de/40170459"), URI.create("https://target.de/40170459"),
        PURLTyp.INTERNAL);
    PURL purlExternal = new PURL(URI.create("https://doi.org/40170459"), URI.create("https://doi.org/40170459"),
        PURLTyp.EXTERNAL);

    Path teixml = Paths.get("src", "test", "resources", "kod-attributsreferenzen.xml");

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("1234")
        .withGndIdentifier("gndid1231234x")
        .withGueltigerIdentifikation(identifikation)
        .addAlternativeIdentifikation(altIdentifikation)
        .addPURL(purlInternal)
        .addPURL(purlExternal)
        .withTEIXml(Files.readString(teixml))
        .build();

    Publikation publikation2 = new Publikation("1", LocalDateTime.of(1982, 1, 2, 0, 0),
        PublikationsTyp.ERSTPUBLIKATION);
    Publikation publikation3 = new Publikation("2", LocalDateTime.of(1983, 3, 4, 0, 0),
        PublikationsTyp.PUBLIKATION_HSP);

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopfBuilder()
        .withTitel("Flavius Josephus, de bello Judaico")
        .withIndentifikationen(new LinkedHashSet<>(List.of(identifikation)))
        .build();
    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withId("1")
        .withKodID(kod.getId())
        .addPublikation(publikation2)
        .addPublikation(publikation3)
        .build();
    beschreibung.getBeschreibungsStruktur().add(kopf);

    kod.addBeschreibungsdokument("1");

    BeschreibungsViewModel beschreibungsViewModel = new BeschreibungsViewBuilder()
        .withKODView("ewr")
        .withTitel("Flavius Josephus, de bello Judaico").withSignatur("Mscr.Dresd.A.111")
        .withPublikationen(List.copyOf(beschreibung.getPublikationen())).build();

    when(kulturObjektDokumentRepositoryMOCK.findByIdOptional("1")).thenReturn(Optional.of(kod));
    when(beschreibungsRepositoryMOCK.findByIdOptional("1")).thenReturn(Optional.of(beschreibung));
    when(beschreibungServiceMOCK.buildBeschreibungsViewModel("1"))
        .thenReturn(Optional.of(beschreibungsViewModel));

    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepositoryMOCK,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK, dokumentSperreServiceMOCK, kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    Optional<KulturObjektDokumentViewModel> kulturObjektDokumentViewModel = service
        .buildKulturObjektDokumentViewModel("1");

    assertTrue(kulturObjektDokumentViewModel.isPresent());
    KulturObjektDokumentViewModel kulturObjektDokumentViewModel1 = kulturObjektDokumentViewModel
        .get();
    assertEquals("Flavius Josephus, de bello Judaico",
        kulturObjektDokumentViewModel1.getBeschreibungen().get(0).getTitel());
    assertEquals("Mscr.Dresd.A.111", kulturObjektDokumentViewModel1.getSignatur());
    assertEquals("Publikation{id='1', datumDerVeroeffentlichung=1982-01-02T00:00, publikationsTyp=ERSTPUBLIKATION}"
            + "Publikation{id='2', datumDerVeroeffentlichung=1983-03-04T00:00, publikationsTyp=PUBLIKATION_HSP}",
        kulturObjektDokumentViewModel1.getBeschreibungen().get(0).getPublikationen().stream()
            .map(Publikation::toString)
            .collect(Collectors.joining()));

    assertNotNull(kulturObjektDokumentViewModel1.getAlternativeSignaturen());
    assertEquals(1, kulturObjektDokumentViewModel1.getAlternativeSignaturen().size());
    assertEquals("Mscr. Dresd. A. 111",
        kulturObjektDokumentViewModel1.getAlternativeSignaturen().get(0));

    assertEquals("https://resolver.de/40170459", kulturObjektDokumentViewModel1.getHspPurl());
    assertNotNull(kulturObjektDokumentViewModel1.getExterneLinks());
    assertEquals(1, kulturObjektDokumentViewModel1.getExterneLinks().size());
    assertEquals("https://doi.org/40170459",
        kulturObjektDokumentViewModel1.getExterneLinks().get(0));

    Set<AttributsReferenz> attributsReferenzen =
        service.getAttributsReferenzenFromKODViewModel(kulturObjektDokumentViewModel1);

    assertEquals(AttributsTyp.values().length, attributsReferenzen.size());

    attributsReferenzen.forEach(attributsReferenz ->
        assertEquals("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001", attributsReferenz.getReferenzId()));
  }

  @Test
  void testBuildKulturObjektDokumentViewModelWithEmptyBeschreibungsModel() {
    NormdatenReferenz besitzer = new NormdatenReferenz("", "Staatsbibliothek zu Berlin", "");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .build();

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("1234")
        .withGndIdentifier("gndid1231234x")
        .withGueltigerIdentifikation(identifikation)
        .build();

    Publikation publikation2 = new Publikation("1", LocalDateTime.of(1982, 1, 2, 0, 0),
        PublikationsTyp.ERSTPUBLIKATION);
    Publikation publikation3 = new Publikation("2", LocalDateTime.of(1983, 3, 4, 0, 0),
        PublikationsTyp.PUBLIKATION_HSP);

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopfBuilder()
        .withTitel("Flavius Josephus, de bello Judaico")
        .withIndentifikationen(new LinkedHashSet<>(List.of(identifikation)))
        .build();
    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withId("1")
        .withKodID(kod.getId())
        .addPublikation(publikation2)
        .addPublikation(publikation3)
        .build();
    beschreibung.getBeschreibungsStruktur().add(kopf);

    kod.addBeschreibungsdokument("1");

    when(kulturObjektDokumentRepositoryMOCK.findByIdOptional("1")).thenReturn(Optional.of(kod));
    when(beschreibungsRepositoryMOCK.findByIdOptional("1")).thenReturn(Optional.of(beschreibung));
    when(beschreibungServiceMOCK.buildBeschreibungsViewModel("1")).thenReturn(Optional.empty());

    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepositoryMOCK,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK, dokumentSperreServiceMOCK, kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    Optional<KulturObjektDokumentViewModel> kulturObjektDokumentViewModel = service
        .buildKulturObjektDokumentViewModel("1");

    assertTrue(kulturObjektDokumentViewModel.isPresent());
    KulturObjektDokumentViewModel kulturObjektDokumentViewModel1 = kulturObjektDokumentViewModel
        .get();
    assertTrue(kulturObjektDokumentViewModel1.getBeschreibungen().isEmpty());
    assertEquals("Mscr.Dresd.A.111", kulturObjektDokumentViewModel1.getSignatur());
  }


  @Test
  @TestTransaction
  void testCount() {
    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepository,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK, beschreibungServiceMOCK, dokumentSperreServiceMOCK,
        kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    long initialCount = service.count();
    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("1234")
        .withGndIdentifier("gndid1231234x")
        .build();
    kulturObjektDokumentRepository.save(kod);
    assertEquals(initialCount + 1L, service.count());
    kulturObjektDokumentRepository.deleteByIdAndFlush(kod.getId());
  }

  @Test
  @TestTransaction
  void testDelete() throws Exception {
    KulturObjektDokumentService service = new KulturObjektDokumentService(
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
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

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
    verify(papierkorbServiceMOCK, times(1)).erzeugeGeloeschtesDokument(kod, bearbeiter);
    verify(papierkorbServiceMOCK, times(1)).erzeugeGeloeschtesDokument(beschreibung, bearbeiter);
  }

  @Test
  @TestTransaction
  void testSperrenForKulturObjektDokument() throws Exception {
    KulturObjektDokument kod = createKulturObjektDokument();
    kod.setId(TEIValues.UUID_PREFIX + UUID.randomUUID());

    kulturObjektDokumentRepository.save(kod);

    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepository,
        identifikationRepository,
        suchDokumentServiceMOCK, beschreibungServiceMOCK, dokumentSperreService,
        kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    Sperre sperre = service.kulturObjektDokumentSperren(BEARBEITER, "KulturObjektDokumentBearbeiten", kod.getId());

    assertNotNull(sperre);
    assertNotNull(sperre.getId());
    assertNotNull(sperre.getId());
    assertEquals(BEARBEITER, sperre.getBearbeiter());
    assertEquals(SperreTyp.MANUAL, sperre.getSperreTyp());
    assertEquals("KulturObjektDokumentBearbeiten", sperre.getSperreGrund());
    assertNotNull(sperre.getSperreEintraege());
    assertTrue(sperre.getSperreEintraege().stream()
        .anyMatch(se -> kod.getId().equals(se.getTargetId()) && SperreDokumentTyp.KOD == se.getTargetType()));

    SperreAlreadyExistException sperreAlreadyExistException = assertThrows(SperreAlreadyExistException.class,
        () -> service.kulturObjektDokumentSperren(new Bearbeiter("2", "Unbekannter Bearbeiter 2"),
            "DigitalisatHinzufügen", kod.getId()));

    assertNotNull(sperreAlreadyExistException.getSperren());
    assertTrue(sperreAlreadyExistException.getSperren().stream()
        .anyMatch(sp -> sperre.getId().equals(sp.getId())));

    Optional<Sperre> optionalSperre = service.findSperreForKulturObjektDokument(kod.getId());
    assertNotNull(optionalSperre);
    assertTrue(optionalSperre.isPresent());
    assertEquals(sperre, optionalSperre.get());

    service.kulturObjektDokumentEntsperren(kod.getId());
    Optional<Sperre> noSperre = service.findSperreForKulturObjektDokument(kod.getId());
    assertNotNull(noSperre);
    assertFalse(noSperre.isPresent());
  }

  @Test
  @TestTransaction
  void testCreateNewPURL() throws Exception {
    PURL purlInternal = new PURL(URI.create("https://resolver.de/HSP-123"), URI.create("https://target.de/HSP-123"),
        PURLTyp.INTERNAL);

    PURL purlExternal = new PURL(URI.create("https://doi.org/HSP-123"), URI.create("https://doi.org/HSP-123"),
        PURLTyp.EXTERNAL);

    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepository,
        identifikationRepository,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK, dokumentSperreServiceMOCK, kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock, teiXmlValidationServiceMock);

    KulturObjektDokument kod = createKulturObjektDokument();
    kod.getPURLs().add(purlExternal);
    kod.setId("HSP-123");

    kulturObjektDokumentRepository.save(kod);

    NullPointerException npe = assertThrows(NullPointerException.class,
        () -> service.addPURL(null, null));
    assertEquals("kodId is required", npe.getMessage());

    String kodId = kod.getId();
    npe = assertThrows(NullPointerException.class,
        () -> service.addPURL(kodId, null));
    assertEquals("purl is required", npe.getMessage());

    KulturObjektDokumentException kodeNoKOD = assertThrows(KulturObjektDokumentException.class,
        () -> service.addPURL("INVALID", purlInternal));
    assertEquals("Zur Id INVALID wurde kein KulturObjektDokument gefunden!", kodeNoKOD.getMessage());

    service.addPURL(kod.getId(), purlInternal);

    Optional<KulturObjektDokument> updated = kulturObjektDokumentRepository.findByIdOptional(kod.getId());
    assertTrue(updated.isPresent());

    assertEquals(2, updated.get().getPURLs().size());
    assertTrue(updated.get().getPURLs().stream().anyMatch(
        purl -> PURLTyp.INTERNAL == purl.getTyp()
            && "https://resolver.de/HSP-123".equals(purl.getPurl().toASCIIString())
            && "https://target.de/HSP-123".equals(purl.getTarget().toASCIIString())));
  }

  @Test
  @TestTransaction
  void testCreateAndAddInternalPURLs() throws IOException, PURLException {
    Mockito.doAnswer((Answer<PURL>) invocation -> {
      final Object[] args = invocation.getArguments();
      String docId = String.valueOf(args[0]);
      return new PURL("PURL-" + docId,
          URI.create("https://resolver.url/" + docId),
          URI.create("https://target.url/" + docId),
          PURLTyp.INTERNAL);
    }).when(purlServiceMOCK).createNewPURL("HSP-1234", DokumentObjektTyp.HSP_OBJECT);

    Mockito.doThrow(new PURLException("TestError"))
        .when(purlServiceMOCK).createNewPURL("HSP-5678", DokumentObjektTyp.HSP_OBJECT);

    KulturObjektDokument kod_1 = createKulturObjektDokument();
    kod_1.setId("HSP-1234");

    KulturObjektDokument kod_2 = createKulturObjektDokument();
    kod_2.setId("HSP-5678");

    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepository,
        identifikationRepository,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK, dokumentSperreServiceMOCK, kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        true,
        teiXmlTransformationServiceMock, teiXmlValidationServiceMock);

    service.createAndAddInternalPURLs(List.of(kod_1, kod_2));

    verify(purlServiceMOCK, times(2)).createNewPURL(anyString(), any(DokumentObjektTyp.class));

    assertNotNull(kod_1.getPURLs());
    assertEquals(1, kod_1.getPURLs().size());
    assertTrue(kod_1.getPURLs().stream().anyMatch(purl -> PURLTyp.INTERNAL == purl.getTyp()),
        "PURL not found in KOD");

    assertNotNull(kod_2.getPURLs());
    assertEquals(0, kod_2.getPURLs().size());
  }

  @Test
  @TestTransaction
  void testCreateAndAddInternalPURLs_disabled() throws IOException, PURLException {
    KulturObjektDokument kod_1 = createKulturObjektDokument();
    kod_1.setId("HSP-1234");

    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepository,
        identifikationRepository,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK, dokumentSperreServiceMOCK, kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock, teiXmlValidationServiceMock);

    service.createAndAddInternalPURLs(List.of(kod_1));
    verify(purlServiceMOCK, times(0)).createNewPURL(anyString(), any(DokumentObjektTyp.class));
  }

  @Test
  void testFindFilteredKulturObjektDokumentListDTOs() {
    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepositoryMOCK,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK,
        dokumentSperreServiceMOCK,
        kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    service.findFilteredKulturObjektDokumentListDTOs(0, 10, new HashMap<>(), new HashMap<>());
    verify(kulturObjektDokumentRepositoryMOCK, times(1))
        .findFilteredKulturObjektDokumentListDTOs(anyInt(), anyInt(), anyMap(), anyMap());
  }

  @Test
  void testCountFilteredKulturObjektDokumentListDTOs() {
    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepositoryMOCK,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK,
        dokumentSperreServiceMOCK,
        kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    service.countFilteredKulturObjektDokumentListDTOs(new HashMap<>());
    verify(kulturObjektDokumentRepositoryMOCK, times(1))
        .countFilteredKulturObjektDokumentListDTOs(anyMap());
  }

  @Test
  void testFindKulturObjektDokumentListDTOById() {
    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepositoryMOCK,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK,
        dokumentSperreServiceMOCK,
        kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    service.findKulturObjektDokumentListDTOById("id");
    verify(kulturObjektDokumentRepositoryMOCK, times(1))
        .findKulturObjektDokumentListDTOById("id");
  }

  @Test
  void testKulturObjektDokumentAktualisieren() throws Exception {
    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepositoryMOCK,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK, dokumentSperreServiceMOCK, kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    KulturObjektDokument result = new KulturObjektDokumentBuilder().withId("1")
        .withTEIXml(FileUtils.readFileToString(TEI_FILE_PATH.toFile(), StandardCharsets.UTF_8))
        .build();

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel()
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
        .withTeiXML(FileUtils.readFileToString(TEI_FILE_PATH.toFile(), StandardCharsets.UTF_8))
        .build();

    Mockito.when(kulturObjektDokumentRepositoryMOCK.findByIdOptional("1"))
        .thenReturn(Optional.of(result));
    when(kulturObjektDokumentRepositoryMOCK.save(result)).thenReturn(result);

    KulturObjektDokument kod = service.kulturObjektDokumentAktualisieren(kulturObjektDokumentViewModel);

    assertNotNull(kod);

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {
      assertFalse(kod.getTeiXML().contains(
          "<index indexName=\"" + attributsTyp.getIndexName() + "\">"), attributsTyp.name());
      assertTrue(kod.getTeiXML().contains(
              "<index indexName=\"norm_material\""),
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

    kod = service.kulturObjektDokumentAktualisieren(kulturObjektDokumentViewModel);

    assertNotNull(kod);

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {
      assertTrue(kod.getTeiXML().contains(
          "<index indexName=\"" + attributsTyp.getIndexName() + "\">"), kod.getTeiXML());
    }

    verify(kulturObjektDokumentRepositoryMOCK, times(2)).save(result);

    verify(suchDokumentServiceMOCK, times(2)).kodUebernehmen(result);

    verify(kafkaIndexingProducerMOCK, times(2)).sendKulturobjektDokumentAsActivityStreamMessage(
        kod,ActivityStreamAction.UPDATE,true,SYSTEM_USERNAME);
  }

  @Test
  void testGetAttributsReferenzenFromKODViewModel() {
    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepositoryMOCK,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK, dokumentSperreServiceMOCK, kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    Set<AttributsReferenz> result = service.getAttributsReferenzenFromKODViewModel(null);
    assertNotNull(result);
    assertTrue(result.isEmpty());

    KulturObjektDokumentViewModel kodViewModel = KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
        .build();
    result = service.getAttributsReferenzenFromKODViewModel(kodViewModel);
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

    kodViewModel = KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
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

    result = service.getAttributsReferenzenFromKODViewModel(kodViewModel);
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
  void testMigrateAttributsReferenzen() throws IOException {
    KulturObjektDokumentService service = new KulturObjektDokumentService(
        kulturObjektDokumentRepositoryMOCK,
        identifikationRepositoryMOCK,
        suchDokumentServiceMOCK,
        beschreibungServiceMOCK, dokumentSperreServiceMOCK, kafkaIndexingProducerMOCK,
        beschreibungsRepositoryMOCK,
        purlServiceMOCK,
        papierkorbServiceMOCK,
        false,
        teiXmlTransformationServiceMock,
        teiXmlValidationServiceMock);

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("HSP-123")
        .withGndIdentifier("gndid1231234x")
        .withTEIXml(Files.readString(TEI_KOD_MSDESC_BESCHREIBUNG_PATH))
        .build();

    PURL beschreibungsPURL = new PURL(URI.create("https://resolver.url/HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001"),URI.create("https://target.url/HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001"),
        PURLTyp.INTERNAL);

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001")
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withKodID(kod.getId())
        .addPURL(beschreibungsPURL)
        .build();

    when(kulturObjektDokumentRepositoryMOCK.listAll()).thenReturn(List.of(kod));
    when(kulturObjektDokumentRepositoryMOCK.findByIdOptional(anyString())).thenReturn(Optional.of(kod));
    when(beschreibungsRepositoryMOCK.findById(beschreibung.getId())).thenReturn(beschreibung);
    when(purlServiceMOCK.createPURLMapWithBeschreibungsID(any())).thenReturn(Map.of("HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001",
        beschreibungsPURL));

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {
      assertFalse(kod.getTeiXML().contains(
              "<index indexName=\"" + attributsTyp.getIndexName() + "\" copyOf=\"HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001\">"),
          attributsTyp.name());
    }

    service.migrateAttributsReferenzen();

    assertNotNull(kod.getTeiXML());

    for (AttributsTyp attributsTyp : AttributsTyp.values()) {
      assertTrue(kod.getTeiXML().contains(
              "<index indexName=\"" + attributsTyp.getIndexName() + "\" copyOf=\"#HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001\" source=\"https://resolver.url/HSP-b4dec8f6-88d3-329f-8bc9-cecd6d13c001\">"),
          kod.getTeiXML());
    }

  }

  private Titel createTitel() {
    return Titel.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("HSP-123")
        .withText("Titel der Beschreibung")
        .build();
  }

  private Beschreibstoff createBeschreibstoff() {
    return Beschreibstoff.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("HSP-123")
        .withText("Pergament, Papyrus")
        .withTypen(List.of("parchment", "papyrus"))
        .build();
  }

  private Umfang createUmfang() {
    return Umfang.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("HSP-123")
        .withText("I + 394 Bl.")
        .withBlattzahl("394")
        .build();
  }

  private Abmessung createAbmessung() {
    return Abmessung.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .addAbmessungWert(AbmessungWert.builder()
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
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .addFormatWert(FormatWert.builder()
            .withText("long and narrow")
            .withTyp("deduced")
            .build())
        .build();
  }

  private Entstehungsort createEntstehungsort() {
    return Entstehungsort.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("HSP-123")
        .withText("Weißenburg")
        .build();
  }

  private Entstehungszeit createEntstehungszeit() {
    return Entstehungszeit.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("HSP-123")
        .addEntstehungszeitWert(EntstehungszeitWert.builder()
            .withText("1446 oder vorher (Fasz. I)")
            .withNichtVor("1426")
            .withNichtNach("1446")
            .withTyp("datable")
            .build())
        .build();
  }

  private Grundsprache createGrundSprache() {
    return Grundsprache.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withText("Mittelhochdeutsch")
        .addNormdatenId("4039687-3")
        .build();
  }

  private Buchschmuck createBuchschmuck() {
    return Buchschmuck.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("HSP-123")
        .withText("yes")
        .build();
  }

  private Handschriftentyp createHandschriftentyp() {
    return Handschriftentyp.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("HSP-123")
        .withText("composite")
        .build();
  }

  private Musiknotation createMusiknotation() {
    return Musiknotation.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("HSP-123")
        .withText("yes")
        .build();
  }

  private Status createStatus() {
    return Status.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("HSP-123")
        .withText("displaced")
        .build();
  }
}
