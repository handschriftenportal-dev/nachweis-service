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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport;

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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEI2BeschreibungMapper;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObject;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObjectTag;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamObjectTagId;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.mapper.ObjectMapperFactory;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.model.ActivityStreamMessage;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.model.HSPActivityStreamObjectTag;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.konfiguration.KonfigurationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamObjectDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SolrUebernahmeException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.transformation.TeiXmlTransformationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.DataEntity;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportFile;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgang;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgangBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgangService;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportServicePort;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaImportProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenUpdateService;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.tei_c.ns._1.TEI;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 06.06.2019.
 */
@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
@Execution(ExecutionMode.SAME_THREAD)
class BeschreibungImportServiceTest {

  static String TEI_FILENAME = "tei-msDesc_Westphal.xml";

  private static TEI tei = null;

  @Inject
  ImportRepository repository;
  @Inject
  BeschreibungsRepository beschreibungsRepository;
  @Inject
  KulturObjektDokumentRepository kulturObjektDokumentRepository;
  @Inject
  TeiXmlValidationBoundary teiXmlValidationBoundary;
  @Inject
  TeiXmlTransformationBoundary teiXmlTransformationBoundary;

  @Inject
  KonfigurationBoundary konfigurationBoundary;

  SuchDokumentService suchDokumentService = mock(SuchDokumentService.class);

  KafkaIndexingProducer kafkaIndexingProducer = mock(KafkaIndexingProducer.class);

  KafkaImportProducer kafkaImportProducer = mock(KafkaImportProducer.class);

  BeschreibungImportService beschreibungImportService;

  ImportVorgangBoundary importVorgangBoundary;

  ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();

  NormdatenReferenzBoundary normdatenReferenzBoundary;

  ImportServicePort importServicePort;

  Event<ImportVorgang> importVorgangEvent;

  PURLBoundary purlBoundary;
  NormdatenUpdateService normdatenUpdateService;

  @BeforeEach
  @TestTransaction
  public void init() throws IOException, JAXBException {
    if (Objects.isNull(tei)) {
      Path teiFilePath = Paths
          .get("../domainmodel-tei-mapper/src", "test", "resources", "tei", TEI_FILENAME);

      try (InputStream is = newInputStream(teiFilePath)) {
        List<TEI> allTEI = TEIObjectFactory.unmarshal(is);
        tei = allTEI.get(0);
      }
    }

    suchDokumentService = mock(SuchDokumentService.class);

    kafkaIndexingProducer = mock(KafkaIndexingProducer.class);

    normdatenReferenzBoundary = mock(NormdatenReferenzBoundary.class);

    importServicePort = mock(ImportServicePort.class);

    importVorgangEvent = mock(Event.class);

    purlBoundary = mock(PURLBoundary.class);

    importVorgangBoundary = new ImportVorgangService(kafkaImportProducer, importServicePort, mapper,
        importVorgangEvent);

    normdatenUpdateService = mock(NormdatenUpdateService.class);

    beschreibungImportService = new BeschreibungImportService();
    beschreibungImportService.setImportVorgangBoundary(importVorgangBoundary);
    beschreibungImportService.setSuchDokumentService(suchDokumentService);
    beschreibungImportService.setImportRepository(repository);
    beschreibungImportService.setKafkaIndexingProducer(kafkaIndexingProducer);
    beschreibungImportService.setKulturObjektDokumentRepository(kulturObjektDokumentRepository);
    beschreibungImportService.setBeschreibungsRepository(beschreibungsRepository);
    beschreibungImportService.setAddBeschreibungMsDescInKodSourceDesc("true");
    beschreibungImportService.setNormdatenReferenzBoundary(normdatenReferenzBoundary);
    beschreibungImportService.setTeiXmlValidationBoundary(teiXmlValidationBoundary);
    beschreibungImportService.setTeiXmlTransformationBoundary(teiXmlTransformationBoundary);
    beschreibungImportService.setPURLBoundary(purlBoundary);
    beschreibungImportService.setKonfigurationBoundary(konfigurationBoundary);
    beschreibungImportService.setNormdatenUpdateService(normdatenUpdateService);
  }

  @Test
  @TestTransaction
  void test_GivenRepository_AlleImporteAnzeigen() throws ActivityStreamsException {
    BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.OFFEN, null);
    repository.save(beschreibungImportJob);

    List<BeschreibungImport> result = beschreibungImportService.alleImporteAnzeigen();

    assertTrue(result.contains(beschreibungImportJob));
  }

  @Test
  @TestTransaction
  void test_GivenRepository_FindByID() throws ActivityStreamsException {
    BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.OFFEN, null);
    repository.save(beschreibungImportJob);

    Optional<BeschreibungImport> result = beschreibungImportService
        .findById(beschreibungImportJob.getId());

    assertTrue(result.isPresent());
  }

  @Test
  @TestTransaction
  void testsaveKodAndBeschreibungenIntoSystemFailedMissingArgumnents()
      throws ActivityStreamsException {

    Event<BeschreibungImport> sendIndexMessageEvent = mock(Event.class);
    beschreibungImportService.setSendIndexMessageEvent(sendIndexMessageEvent);

    ActivityStream message = ActivityStream.builder()
        .withId("Berlin_test_4_190405.fmt.xml")
        .withPublished(LocalDateTime.now())
        .withType(ActivityStreamAction.ADD)
        .build();

    BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.OFFEN, message);

    assertThrows(IllegalArgumentException.class,
        () -> beschreibungImportService
            .saveKodAndBeschreibungenIntoSystem(beschreibungImportJob, null, null));
  }

  @Test
  @TestTransaction
  void testUpdateTEI() throws Exception {
    ActivityStreamObject activityStreamObject = ActivityStreamObject.builder()
        .withContent(tei)
        .withName(TEI_FILENAME)
        .withId(TEI_FILENAME)
        .withType(ActivityStreamsDokumentTyp.BESCHREIBUNG)
        .build();

    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<TEI> teiList = TEIObjectFactory
        .unmarshal(new ByteArrayInputStream(activityStreamObject.getContent()));
    List<Beschreibung> beschreibungen = beschreibungMapper.map(teiList.get(0));

    Beschreibung beschreibung = beschreibungen.get(0);
    PURL purlInternal = new PURL(URI.create("https://resolver.de/HSP-123"),
        URI.create("https://target.de/HSP-123"),
        PURLTyp.INTERNAL);
    beschreibung.getPURLs().add(purlInternal);

    KulturObjektDokument kod = createDummyKulturObjektDokument(beschreibung);

    beschreibungImportService.setAddBeschreibungMsDescInKodSourceDesc("true");

    beschreibungImportService.updateTEIDocuments(kod, beschreibung);

    assertTrue(beschreibung.getTeiXML().contains(DokumentObjektTyp.HSP_DESCRIPTION.toString()));

    assertTrue(beschreibung.getTeiXML().contains(beschreibung.getId()));

    assertTrue(kod.getTeiXML().contains(beschreibung.getId()));

    assertTrue(beschreibung.getTeiXML().contains(DokumentObjektTyp.HSP_DESCRIPTION.toString()));

    assertTrue(beschreibung.getTeiXML().contains(
        "<ptr xml:id=\"HSP-cd421bcb-cf34-3640-9dbf-b3b3cb4ddb67\" target=\"https://resolver.de/HSP-123\" subtype=\"hsp\" type=\"purl\"/>"));
  }

  @Test
  @TestTransaction
  void testBeschreibungUebernehmenWithFailedSaveException() throws Exception {

    Event<BeschreibungImport> sendIndexMessageEvent = mock(Event.class);

    beschreibungImportService.setKulturObjektDokumentRepository(kulturObjektDokumentRepository);
    beschreibungImportService.setBeschreibungsRepository(beschreibungsRepository);
    beschreibungImportService.setSendIndexMessageEvent(sendIndexMessageEvent);

    String suffix = String.valueOf(new Random().nextInt());
    ActivityStreamObject activityStreamObject = createActivityStreamObject(suffix);
    ActivityStream activityStream = createActivityStream(activityStreamObject, suffix);

    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<TEI> teiList = TEIObjectFactory
        .unmarshal(new ByteArrayInputStream(activityStreamObject.getContent()));
    List<Beschreibung> beschreibungen = beschreibungMapper.map(teiList.get(0));

    Beschreibung beschreibung = beschreibungen.get(0);

    KulturObjektDokument kod = createDummyKulturObjektDokument(beschreibung);
    kulturObjektDokumentRepository.save(kod);

    assertNotNull(beschreibungen);
    assertFalse(beschreibungen.isEmpty());

    beschreibung.setKodID(kod.getId());

    BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.OFFEN,
        activityStream);
    int initialSize = beschreibungsRepository.listAll().size();

    repository.save(beschreibungImportJob);

    doThrow(new SolrUebernahmeException("")).when(suchDokumentService)
        .kodUebernehmen(any());

    try {
      beschreibungImportService.importeUebernehmen(beschreibungImportJob.getId(), "intern");
    } catch (BeschreibungImportException e) {
      e.printStackTrace();
    }

    assertTrue(kulturObjektDokumentRepository.listAll().contains(kod));

    assertEquals(0, kod.getBeschreibungenIDs().size());

    assertFalse(kod.getTeiXML().contains(DokumentObjektTyp.HSP_DESCRIPTION.toString()));

    assertFalse(kod.getTeiXML().contains(beschreibung.getId()));

    assertEquals(initialSize, beschreibungsRepository.listAll().size());

  }

  @Test
  @TestTransaction
  void testsaveKodAndBeschreibungenIntoSystemSuccess() throws Exception {

    beschreibungImportService.setKulturObjektDokumentRepository(kulturObjektDokumentRepository);
    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = beschreibungMapper.map(tei);
    assertNotNull(beschreibungen);
    assertFalse(beschreibungen.isEmpty());

    Beschreibung beschreibung = beschreibungen.get(0);

    KulturObjektDokument kod = createDummyKulturObjektDokument(beschreibung);
    saveKOD(kod);

    ActivityStreamObject importObject = ActivityStreamObject.builder()
        .withCompressed(true)
        .withType(ActivityStreamsDokumentTyp.IMPORT)
        .withName("test.xml")
        .withId("1")
        .withContent(
            "{\"id\":\"03c45a36-a364-4541-ad72-9e640643836b\",\"creationDate\":\"2020-09-24T13:24:22.657368\",\"benutzerName\":\"Redakteur\",\"importFiles\":[{\"id\":\"7ee84c46-78b9-433a-94bc-4ffb3a4a291e\",\"path\":\"file:///tmp/hsp-import-/2020.09.24.13.24.22.656/ms_1_neu-wthout-BOM-translated.xml\",\"dateiTyp\":\"ms_1_neu-wthout-BOM.xml\",\"dateiName\":\"test.xml\",\"dateiFormat\":\"MXML\",\"error\":false,\"message\":null,\"importEntityData\":[]}],\"name\":\"ms_1_neu.xml\",\"importDir\":\"/tmp/hsp-import-/2020.09.24.13.24.22.656\",\"result\":\"FAILED\",\"errorMessage\":null,\"datatype\":\"BESCHREIBUNG\"}")
        .build();

    ActivityStream message = ActivityStream.builder()
        .withId("Berlin_test_4_190405.fmt.xml")
        .withPublished(LocalDateTime.now())
        .withType(ActivityStreamAction.ADD)
        .addObject(importObject)
        .build();

    BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.OFFEN, message);

    beschreibungImportService
        .saveKodAndBeschreibungenIntoSystem(beschreibungImportJob, beschreibung, kod);

    assertEquals(ImportStatus.OFFEN, beschreibungImportJob.getStatus());

    verify(suchDokumentService, times(1)).beschreibungUebernehmen(beschreibung);
    verify(suchDokumentService, times(1)).kodUebernehmen(kod);
  }

  @ParameterizedTest
  @TestTransaction
  @ValueSource(strings = {"extern", "intern"})
  void testHandleBeschreibungAlreadyExist(String type) throws BeschreibungAlreadyExistException {

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("", "").build();
    PURL purlInternal = new PURL(URI.create("https://resolver.de/HSP-123"),
        URI.create("https://target.de/HSP-123"),
        PURLTyp.INTERNAL);
    Optional<Beschreibung> beschreibungAlreadyExist = Optional.of(
        new Beschreibung.BeschreibungsBuilder("1", "1")
            .addPURL(purlInternal)
            .build());

    BeschreibungsRepository beschreibungsRepositoryMock = mock(BeschreibungsRepository.class);

    if ("intern".equals(type)) {
      assertThrows(BeschreibungAlreadyExistException.class, () -> beschreibungImportService
          .handleBeschreibungAlreadyExist(VerwaltungsTyp.valueOf(type.toUpperCase()),
              beschreibung, beschreibungAlreadyExist));
    }

    if ("extern".equals(type)) {
      beschreibungImportService.setBeschreibungsRepository(beschreibungsRepositoryMock);
      beschreibungImportService
          .handleBeschreibungAlreadyExist(VerwaltungsTyp.valueOf(type.toUpperCase()), beschreibung,
              beschreibungAlreadyExist);
      assertEquals(beschreibungAlreadyExist.get().getId(), beschreibung.getId());
      assertNotNull(beschreibung.getPURLs());
      assertEquals(1, beschreibung.getPURLs().size());
      assertTrue(beschreibung.getPURLs().stream()
          .anyMatch(purl -> "https://resolver.de/HSP-123".equals(purl.getPurl().toASCIIString())));
      verify(beschreibungsRepositoryMock, times(1))
          .deleteById(beschreibungAlreadyExist.get().getId());
    }
  }

  @Test
  @TestTransaction
  void testHandleBeschreibungAlreadyExistWithNewBeschreibung()
      throws BeschreibungAlreadyExistException {
    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("altID", "").build();
    beschreibungImportService.handleBeschreibungAlreadyExist(VerwaltungsTyp.EXTERN, beschreibung,
        Optional.empty());

    assertTrue(beschreibung.getAltIdentifier().contains("altID"));
    assertNotEquals("altID", beschreibung.getId());
  }

  @Test
  @TestTransaction
  void testGivenIDsimporteAblehnen()
      throws ActivityStreamsException, BeschreibungImportException {
    BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.OFFEN,
        mock(ActivityStreamMessage.class));

    repository.save(beschreibungImportJob);

    beschreibungImportService.importeAblehnen(List.of(beschreibungImportJob.getId()));

    beschreibungImportJob = repository.findById(beschreibungImportJob.getId());

    assertEquals(ImportStatus.ABGELEHNT, beschreibungImportJob.getStatus());
  }

  @Test
  @TestTransaction
  void testimportErfolgreich() throws ActivityStreamsException {
    BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.OFFEN,
        mock(ActivityStreamMessage.class));
    Event<BeschreibungImport> importSendEvent = mock(Event.class);

    beschreibungImportService.setSendIndexMessageEvent(importSendEvent);

    beschreibungImportService.importErfolgreich(beschreibungImportJob);

    beschreibungImportJob = repository.findById(beschreibungImportJob.getId());

    assertEquals(ImportStatus.UEBERNOMMEN, beschreibungImportJob.getStatus());
    assertFalse(beschreibungImportJob.isSelectedForImport());

    verify(importSendEvent, times(1)).fire(beschreibungImportJob);
  }

  @Test
  @TestTransaction
  void testimportFehlgeschlagen() throws ActivityStreamsException {

    BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.OFFEN,
        mock(ActivityStreamMessage.class));
    Event<BeschreibungImport> importSendEvent = mock(Event.class);

    beschreibungImportService.setSendIndexMessageEvent(importSendEvent);

    beschreibungImportService.importFehlgeschlagen(beschreibungImportJob, new Exception(""));

    assertEquals(ImportStatus.FEHLGESCHLAGEN, beschreibungImportJob.getStatus());
    assertFalse(beschreibungImportJob.isSelectedForImport());

    verify(importSendEvent, times(1)).fire(beschreibungImportJob);
  }

  @Test
  @TestTransaction
  void testErmittleKODWithObligatorischeID() throws KulturObjektDokumentNotFoundException {
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

    kulturObjektDokumentRepository.save(kod);

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopfBuilder()
        .withTitel("Flavius Josephus, de bello Judaico")
        .withIndentifikationen(Set.of(identifikation))
        .build();
    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withId("40170459")
        .withKodID(kod.getId())
        .build();
    beschreibung.getBeschreibungsStruktur().add(kopf);

    assertTrue(beschreibungImportService.ermittleKOD(beschreibung).isPresent());
  }

  @Test
  @TestTransaction
  void testermittleKODWithIdentifikation() throws KulturObjektDokumentNotFoundException {
    NormdatenReferenz dresden = createDresden();
    NormdatenReferenz koerperschaft = createSLB();
    Identifikation identifikation = createIdnoMscrDresdA111(koerperschaft, dresden);

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("1234")
        .withGueltigerIdentifikation(identifikation)
        .build();

    saveKOD(kod);

    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withId("40170459")
        .addBeschreibungsKomponente(new BeschreibungsKomponenteKopfBuilder()
            .withIndentifikationen(Set.of(identifikation))
            .build())
        .build();
    Beschreibung beschreibung1 = new BeschreibungsBuilder()
        .withId("345356476")
        .addBeschreibungsKomponente(new BeschreibungsKomponenteKopfBuilder()
            .withIndentifikationen(Set.of(identifikation))
            .build())
        .build();

    assertTrue(beschreibungImportService.ermittleKOD(beschreibung).isPresent());

    assertTrue(beschreibungImportService.ermittleKOD(beschreibung1).isPresent());

    assertEquals(beschreibungImportService.ermittleKOD(beschreibung).get(),
        beschreibungImportService.ermittleKOD(beschreibung1).get());
  }

  @Test
  @TestTransaction
  void testermittleKODWithAlternativeIdentifikation() throws KulturObjektDokumentNotFoundException {
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

    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withId("40170459")
        .addBeschreibungsKomponente(new BeschreibungsKomponenteKopfBuilder()
            .withIndentifikationen(Set.of(identifikationKOD))
            .build())
        .build();
    Beschreibung beschreibung1 = new BeschreibungsBuilder()
        .withId("345356476")
        .addBeschreibungsKomponente(new BeschreibungsKomponenteKopfBuilder()
            .withIndentifikationen(Set.of(identifikationBeschreibung1))
            .build())
        .build();

    assertTrue(beschreibungImportService.ermittleKOD(beschreibung).isPresent());

    assertTrue(beschreibungImportService.ermittleKOD(beschreibung1).isPresent());

    assertEquals(beschreibungImportService.ermittleKOD(beschreibung).get(),
        beschreibungImportService.ermittleKOD(beschreibung1).get());
  }

  private NormdatenReferenz createSLB() {
    return new NormdatenReferenz("SLUB",
        "Sächsische Landesbibliothek - Staats- und Universitätsbibliothek Dresden",
        "");
  }

  @Test
  @TestTransaction
  void testConvertTEI2Beschreibung() throws Exception {

    beschreibungImportService.setBeschreibungsRepository(beschreibungsRepository);

    NormdatenReferenz german = createLanguageNormdatenReferenz();
    when(normdatenReferenzBoundary.findSpracheById(anyString())).thenReturn(Optional.of(german));
    beschreibungImportService.setNormdatenReferenzBoundary(normdatenReferenzBoundary);

    ActivityStreamObject activityStreamObject = ActivityStreamObject.builder()
        .withContent(tei)
        .withName(TEI_FILENAME)
        .withId(TEI_FILENAME)
        .withType(ActivityStreamsDokumentTyp.BESCHREIBUNG)
        .build();
    ActivityStreamObjectDTO activityStreamObjectDTO = new ActivityStreamObjectDTO(
        activityStreamObject);

    List<Beschreibung> beschreibungs = beschreibungImportService
        .convertTEI2Beschreibung(activityStreamObjectDTO);

    assertEquals(1, beschreibungs.size());

    assertEquals("mss_36-23-aug-2f_tei-msDesc_Westphal", beschreibungs.get(0).getId());

    assertFalse(beschreibungs.get(0).getTeiXML().isEmpty());
  }

  @Test
  @TestTransaction
  void testCheckIfBeschreibungAlreadyExists() throws Exception {

    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withId("40170459")
        .addAltIdentifier("mss_36-23-aug-2f_tei-msDesc_Westphal")
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .build();

    beschreibungsRepository.save(beschreibung);

    beschreibungImportService.setBeschreibungsRepository(beschreibungsRepository);

    NormdatenReferenz german = createLanguageNormdatenReferenz();
    when(normdatenReferenzBoundary.findSpracheById(anyString())).thenReturn(Optional.of(german));
    beschreibungImportService.setNormdatenReferenzBoundary(normdatenReferenzBoundary);

    ActivityStreamObject activityStreamObject = ActivityStreamObject.builder()
        .withContent(tei)
        .withName(TEI_FILENAME)
        .withId(TEI_FILENAME)
        .withType(ActivityStreamsDokumentTyp.BESCHREIBUNG)
        .build();

    Optional<Beschreibung> already = beschreibungImportService.checkIfBeschreibungAlreadyExists(
        beschreibungImportService
            .convertTEI2Beschreibung(new ActivityStreamObjectDTO(activityStreamObject))
            .get(0));

    assertTrue(already.isPresent());
  }

  @Test
  @TestTransaction
  void testErmittleKulturObjektDokumentAndUpdateBeschreibung() throws Exception {

    NormdatenReferenz dresden = createDresden();
    NormdatenReferenz koerperschaft = createSLB();
    Identifikation identifikation = createIdnoMscrDresdA111(koerperschaft, dresden);

    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withId("40170459")
        .addBeschreibungsKomponente(new BeschreibungsKomponenteKopfBuilder().withId("kopfID")
            .withIndentifikationen(Set.of(identifikation))
            .build())
        .build();

    BeschreibungImportService beschreibungImportService = new BeschreibungImportService();
    beschreibungImportService.setBeschreibungsRepository(beschreibungsRepository);
    beschreibungImportService.setKulturObjektDokumentRepository(kulturObjektDokumentRepository);
    KulturObjektDokument dummyKulturObjektDokument = createDummyKulturObjektDokument(beschreibung);

    kulturObjektDokumentRepository.save(dummyKulturObjektDokument);

    Optional<KulturObjektDokument> kod = beschreibungImportService.ermittleKOD(beschreibung);

    assertTrue(kod.isPresent());

    assertEquals(identifikation, kod.get().getGueltigeIdentifikation());
  }

  @TestTransaction
  void saveKOD(KulturObjektDokument dummyKulturObjektDokument) {
    kulturObjektDokumentRepository.persistAndFlush(dummyKulturObjektDokument);
  }

  @Test
  @TestTransaction
  void testsendKafkaMessageToImport() throws ActivityStreamsException, JsonProcessingException {
    beschreibungImportService.setKulturObjektDokumentRepository(kulturObjektDokumentRepository);

    String jsonJob = "{\"id\":\"03c45a36-a364-4541-ad72-9e640643836b\",\"creationDate\":\"2020-09-24T13:24:22.657368\",\"benutzerName\":\"Redakteur\",\"importFiles\":[{\"id\":\"7ee84c46-78b9-433a-94bc-4ffb3a4a291e\",\"path\":\"file:///tmp/hsp-import-/2020.09.24.13.24.22.656/ms_1_neu-wthout-BOM-translated.xml\",\"dateiTyp\":\"ms_1_neu-wthout-BOM.xml\",\"dateiName\":\"ms_1_neu.xml\",\"dateiFormat\":\"MXML\",\"error\":false,\"message\":null,\"importEntityData\":[]}],\"name\":\"ms_1_neu.xml\",\"importDir\":\"/tmp/hsp-import-/2020.09.24.13.24.22.656\",\"result\":\"FAILED\",\"errorMessage\":null,\"datatype\":\"BESCHREIBUNG\"}";

    ImportVorgang importVorgang = ObjectMapperFactory.getObjectMapper()
        .readValue(jsonJob, ImportVorgang.class);

    importVorgangBoundary.sendImportJobToKafka(importVorgang);

    verify(kafkaImportProducer, times(1)).sendImportJobAsActivityStreamMessage(importVorgang);
  }

  @Test
  @TestTransaction
  void testupdateImportJsonNodeForFailure() throws Exception {

    String jsonJob = "{\"id\":\"1c8050b1-616c-4454-b441-9281a57bc99d\",\"creationDate\":\"2020-09-24T15:22:15.926333\",\"benutzerName\":\"Redakteur\",\"importFiles\":[{\"id\":\"bec5a8c1-4e01-4d82-b19f-8189bb6b8a12\",\"path\":\"file:///tmp/hsp-import-/2020.09.24.15.22.15.925/ms_1-wthout-BOM-translated.xml\",\"dateiTyp\":\"ms_1-wthout-BOM.xml\",\"dateiName\":\"ms_1.xml\",\"dateiFormat\":\"MXML\",\"error\":false,\"message\":null,\"importEntityData\":[{\"id\":\"HSP-c137570e-95b8-4af8-a92e-eae03d0363b2\",\"label\":\"Ms 1\",\"url\":null}]}],\"name\":\"ms_1.xml\",\"importDir\":\"/tmp/hsp-import-/2020.09.24.15.22.15.925\",\"result\":\"SUCCESS\",\"errorMessage\":\"\",\"datatype\":\"BESCHREIBUNG\"}";

    ImportVorgang importVorgang = ObjectMapperFactory.getObjectMapper()
        .readValue(jsonJob, ImportVorgang.class);

    beschreibungImportService.updateImportJsonNodeForFailure(importVorgang, "Fehler");

    assertEquals("Fehler", importVorgang.getFehler());
    assertEquals(IMPORTJOB_RESULT_VALUES.FAILED, importVorgang.getStatus());

  }

  @Test
  @TestTransaction
  void testupdateImportJsonNodeForSuccess() throws Exception {

    NormdatenReferenz dresden = createDresden();
    NormdatenReferenz koerperschaft = createSLB();
    Identifikation identifikation = createIdnoMscrDresdA111(koerperschaft, dresden);
    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withId("40170459")
        .addBeschreibungsKomponente(new BeschreibungsKomponenteKopfBuilder()
            .withIndentifikationen(Set.of(identifikation))
            .build())
        .build();

    String jsonJob = "{\"id\":\"1c8050b1-616c-4454-b441-9281a57bc99d\",\"creationDate\":\"2020-09-24T15:22:15.926333\",\"benutzerName\":\"Redakteur\",\"importFiles\":[{\"id\":\"bec5a8c1-4e01-4d82-b19f-8189bb6b8a12\",\"path\":\"file:///tmp/hsp-import-/2020.09.24.15.22.15.925/ms_1-wthout-BOM-translated.xml\",\"dateiTyp\":\"ms_1-wthout-BOM.xml\",\"dateiName\":\"ms_1.xml\",\"dateiFormat\":\"MXML\",\"error\":false,\"message\":null,\"importEntityData\":[{\"id\":\"HSP-c137570e-95b8-4af8-a92e-eae03d0363b2\",\"label\":\"Ms 1\",\"url\":null}]}],\"name\":\"ms_1.xml\",\"importDir\":\"/tmp/hsp-import-/2020.09.24.15.22.15.925\",\"result\":\"SUCCESS\",\"errorMessage\":\"\",\"datatype\":\"BESCHREIBUNG\"}";

    ImportVorgang importVorgang = ObjectMapperFactory.getObjectMapper()
        .readValue(jsonJob, ImportVorgang.class);

    beschreibungImportService
        .updateImportJsonNodeForSuccess(beschreibung, importVorgang.getImportFiles().get(0));

    assertEquals(IMPORTJOB_RESULT_VALUES.SUCCESS, importVorgang.getStatus());
    assertEquals(1, importVorgang.getImportFiles().size());
    ImportFile importFile = importVorgang.getImportFiles().get(0);
    assertEquals(2, importFile.getDataEntityList().size());
    DataEntity dataEntity1 = importFile.getDataEntityList().get(0);
    DataEntity dataEntity2 = importFile.getDataEntityList().get(1);
    assertEquals("HSP-c137570e-95b8-4af8-a92e-eae03d0363b2", dataEntity1.getId());
    assertEquals("Ms 1", dataEntity1.getLabel());
    assertEquals("40170459", dataEntity2.getId());
    assertEquals("Mscr.Dresd.A.111", dataEntity2.getLabel());
  }

  private Identifikation createIdnoMscrDresdA111(NormdatenReferenz koerperschaft,
      NormdatenReferenz aufbewahrungsOrt) {
    return new IdentifikationBuilder()
        .withId("ident1")
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(koerperschaft)
        .withAufbewahrungsOrt(aufbewahrungsOrt)
        .build();
  }

  private NormdatenReferenz createDresden() {
    return new NormdatenReferenz("D", "Dresden", "");
  }

  protected KulturObjektDokument createDummyKulturObjektDokument(Beschreibung beschreibung)
      throws IOException {

    Path xmlPath = Paths.get("src", "test", "resources", "kodinitial.xml");

    return new KulturObjektDokumentBuilder()
        .withId(TEIValues.UUID_PREFIX + UUID.randomUUID())
        .withRegistrierungsDatum(LocalDateTime.now())
        .withTEIXml(Files.readString(xmlPath))
        .withGueltigerIdentifikation(beschreibung.getGueltigeIdentifikation().get())
        .build();
  }

  @Test
  @TestTransaction
  void testselectForImport() throws ActivityStreamsException {
    BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.OFFEN, null);

    repository.save(beschreibungImportJob);

    assertFalse(beschreibungImportJob.isSelectedForImport());

    beschreibungImportService.selectForImport(List.of(beschreibungImportJob.getId()));

    beschreibungImportJob = repository.findById(beschreibungImportJob.getId());

    assertTrue(beschreibungImportJob.isSelectedForImport());
  }

  @Test
  @TestTransaction
  void testEnhanceNormdatenReferenzSprache() throws Exception {

    NormdatenReferenz german = createLanguageNormdatenReferenz();
    when(normdatenReferenzBoundary.findSpracheById(anyString())).thenReturn(Optional.of(german));
    beschreibungImportService.setNormdatenReferenzBoundary(normdatenReferenzBoundary);

    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = beschreibungMapper.map(tei);
    assertNotNull(beschreibungen);
    assertFalse(beschreibungen.isEmpty());
    Beschreibung beschreibung = beschreibungen.get(0);

    beschreibungImportService.enhanceNormdatenReferenzSprache(beschreibung);

    assertNotNull(beschreibung.getBeschreibungsSprache());
    assertEquals(german.getId(), beschreibung.getBeschreibungsSprache().getId());
    assertEquals(german.getName(), beschreibung.getBeschreibungsSprache().getName());
    assertEquals(german.getTypeName(), beschreibung.getBeschreibungsSprache().getTypeName());
    assertEquals(german.getGndID(), beschreibung.getBeschreibungsSprache().getGndID());
    assertNotNull(beschreibung.getBeschreibungsSprache().getVarianterName());
    assertEquals(german.getVarianterName().size(),
        beschreibung.getBeschreibungsSprache().getVarianterName().size());
    assertNotNull(beschreibung.getBeschreibungsSprache().getIdentifikator());
    assertEquals(german.getIdentifikator().size(),
        beschreibung.getBeschreibungsSprache().getIdentifikator().size());
  }

  @Test
  @TestTransaction
  void testEnhanceNormdatenReferenzSprache_noSprache() throws Exception {
    beschreibungImportService.setNormdatenReferenzBoundary(normdatenReferenzBoundary);

    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = beschreibungMapper.map(tei);
    assertNotNull(beschreibungen);
    assertFalse(beschreibungen.isEmpty());
    Beschreibung beschreibung = beschreibungen.get(0);

    beschreibung.setBeschreibungsSprache(null);

    beschreibungImportService.enhanceNormdatenReferenzSprache(beschreibung);

    assertNull(beschreibung.getBeschreibungsSprache());
  }

  @Test
  @TestTransaction
  void testEnhanceNormdatenReferenzSprache_no_Normdatensprache() throws Exception {
    when(normdatenReferenzBoundary.findSpracheById(anyString())).thenReturn(Optional.empty());
    beschreibungImportService.setNormdatenReferenzBoundary(normdatenReferenzBoundary);

    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = beschreibungMapper.map(tei);
    assertNotNull(beschreibungen);
    assertFalse(beschreibungen.isEmpty());
    Beschreibung beschreibung = beschreibungen.get(0);

    Exception exception = assertThrows(BeschreibungImportException.class,
        () -> beschreibungImportService.enhanceNormdatenReferenzSprache(beschreibung));

    assertEquals("Found no Language for Beschreibung mss_36-23-aug-2f_tei-msDesc_Westphal",
        exception.getMessage());
  }

  @Test
  @TestTransaction
  void testAutomatischenUebernahmeAktiv() {
    assertFalse(beschreibungImportService.isAutomatischenUebernahmeAktiv());
    beschreibungImportService.setAutomatischenUebernahmeAktiv(true);
    assertTrue(beschreibungImportService.isAutomatischenUebernahmeAktiv());
  }

  @Test
  @TestTransaction
  void testOnImportMessageAutomatischUebernehmenInaktiv() throws Exception {

    Event<BeschreibungImport> sendIndexMessageEvent = mock(Event.class);

    beschreibungImportService.setKulturObjektDokumentRepository(kulturObjektDokumentRepository);
    beschreibungImportService.setBeschreibungsRepository(beschreibungsRepository);
    beschreibungImportService.setSendIndexMessageEvent(sendIndexMessageEvent);

    String suffix = String.valueOf(new Random().nextInt());
    ActivityStreamObject activityStreamObject = createActivityStreamObject(suffix);
    ActivityStream activityStream = createActivityStream(activityStreamObject, suffix);

    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<TEI> teiList = TEIObjectFactory
        .unmarshal(new ByteArrayInputStream(activityStreamObject.getContent()));
    List<Beschreibung> beschreibungen = beschreibungMapper.map(teiList.get(0));

    Beschreibung beschreibung = beschreibungen.get(0);

    KulturObjektDokument kod = createDummyKulturObjektDokument(beschreibung);
    kulturObjektDokumentRepository.save(kod);

    assertNotNull(beschreibungen);
    assertFalse(beschreibungen.isEmpty());

    beschreibung.setKodID(kod.getId());

    BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.OFFEN,
        activityStream);
    int initialSize = beschreibungsRepository.listAll().size();

    beschreibungImportService.onImportMessage(beschreibungImportJob);

    assertTrue(kulturObjektDokumentRepository.listAll().contains(kod));

    assertEquals(0, kod.getBeschreibungenIDs().size());

    assertFalse(kod.getTeiXML().contains(DokumentObjektTyp.HSP_DESCRIPTION.toString()));

    assertFalse(kod.getTeiXML().contains(beschreibung.getId()));

    assertEquals(initialSize, beschreibungsRepository.listAll().size());

    assertTrue(repository.findByIdOptional(beschreibungImportJob.getId()).isPresent());
    assertEquals(ImportStatus.OFFEN, beschreibungImportJob.getStatus());
  }

  @Test
  @TestTransaction
  void testOnImportMessageAutomatischUebernehmenAktiv() throws Exception {

    Event<BeschreibungImport> sendIndexMessageEvent = mock(Event.class);
    NormdatenReferenz german = createLanguageNormdatenReferenz();
    when(normdatenReferenzBoundary.findSpracheById(anyString())).thenReturn(Optional.of(german));
    beschreibungImportService.setBeschreibungsRepository(beschreibungsRepository);
    beschreibungImportService.setSendIndexMessageEvent(sendIndexMessageEvent);
    beschreibungImportService.setAutomatischenUebernahmeAktiv(true);
    beschreibungImportService.setNormdatenReferenzBoundary(normdatenReferenzBoundary);
    beschreibungImportService.setImportRepository(mock(ImportRepository.class));
    String suffix = String.valueOf(new Random().nextInt());
    ActivityStreamObjectTag internExternTag = new HSPActivityStreamObjectTag("String",
        ActivityStreamObjectTagId.INTERN_EXTERN, "EXTERN");
    ActivityStreamObject activityStreamObject = createActivityStreamObject(suffix);
    ActivityStream activityStream = createActivityStream(activityStreamObject, suffix,
        internExternTag);
    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<TEI> teiList = TEIObjectFactory
        .unmarshal(new ByteArrayInputStream(activityStreamObject.getContent()));
    List<Beschreibung> beschreibungen = beschreibungMapper.map(teiList.get(0));
    Beschreibung beschreibung = beschreibungen.get(0);
    KulturObjektDokument kod = createDummyKulturObjektDokument(beschreibung);
    kulturObjektDokumentRepository.deleteAll();
    kulturObjektDokumentRepository.save(kod);
    assertNotNull(beschreibungen);
    assertFalse(beschreibungen.isEmpty());
    beschreibung.setKodID(kod.getId());
    beschreibungsRepository.deleteAll();
    BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.OFFEN,
        activityStream);
    assertEquals(0, beschreibungsRepository.listAll().size());

    beschreibungImportService.onImportMessage(beschreibungImportJob);

    assertEquals(kod.getGueltigeIdentifikation(), beschreibung.getGueltigeIdentifikation().get());
    kod = kulturObjektDokumentRepository.findById(kod.getId());
    assertEquals(1, kod.getBeschreibungenIDs().size());
    assertTrue(kod.getTeiXML().contains(DokumentObjektTyp.HSP_DESCRIPTION.toString()));
    assertTrue(kod.getTeiXML().contains(beschreibung.getId()));
    assertEquals(1, beschreibungsRepository.listAll().size());


    assertEquals(ImportStatus.UEBERNOMMEN, beschreibungImportJob.getStatus());

    Optional<Beschreibung> optBeschreibung = beschreibungsRepository.listAll().stream().findFirst();
    assertTrue(optBeschreibung.isPresent());
    assertNotNull(optBeschreibung.get().getPublikationen());
    assertEquals(2, optBeschreibung.get().getPublikationen().size());

    assertTrue(optBeschreibung.get().getPublikationen().stream()
        .anyMatch(p -> PublikationsTyp.PUBLIKATION_HSP == p.getPublikationsTyp()
            && Objects.nonNull(p.getDatumDerVeroeffentlichung())
            && p.getDatumDerVeroeffentlichung().isAfter(LocalDateTime.of(2020, 02, 3, 0, 0))));
  }

  @Test
  @TestTransaction
  void testOnImportMessageNoVerwaltungsTyp() throws Exception {

    Event<BeschreibungImport> sendIndexMessageEvent = mock(Event.class);

    beschreibungImportService.setKulturObjektDokumentRepository(kulturObjektDokumentRepository);
    beschreibungImportService.setBeschreibungsRepository(beschreibungsRepository);
    beschreibungImportService.setSendIndexMessageEvent(sendIndexMessageEvent);
    beschreibungImportService.setAutomatischenUebernahmeAktiv(true);
    beschreibungImportService.setImportRepository(mock(ImportRepository.class));

    String suffix = String.valueOf(new Random().nextInt());
    ActivityStreamObject activityStreamObject = createActivityStreamObject(suffix);
    ActivityStream activityStream = createActivityStream(activityStreamObject, suffix);

    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<TEI> teiList = TEIObjectFactory
        .unmarshal(new ByteArrayInputStream(activityStreamObject.getContent()));
    List<Beschreibung> beschreibungen = beschreibungMapper.map(teiList.get(0));

    Beschreibung beschreibung = beschreibungen.get(0);

    KulturObjektDokument kod = createDummyKulturObjektDokument(beschreibung);
    kulturObjektDokumentRepository.save(kod);

    assertNotNull(beschreibungen);
    assertFalse(beschreibungen.isEmpty());

    beschreibung.setKodID(kod.getId());

    BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.OFFEN,
        activityStream);
    int initialSize = beschreibungsRepository.listAll().size();

    beschreibungImportService.onImportMessage(beschreibungImportJob);

    assertTrue(kulturObjektDokumentRepository.listAll().contains(kod));

    assertEquals(0, kod.getBeschreibungenIDs().size());

    assertFalse(kod.getTeiXML().contains(DokumentObjektTyp.HSP_DESCRIPTION.toString()));

    assertFalse(kod.getTeiXML().contains(beschreibung.getId()));

    assertEquals(initialSize, beschreibungsRepository.listAll().size());

    assertEquals(ImportStatus.FEHLGESCHLAGEN, beschreibungImportJob.getStatus());
    assertEquals("Automatische Übernahme ohne VerwaltungsTyp nicht möglich!",
        beschreibungImportJob.getFehlerBeschreibung());
  }

  @Test
  @TestTransaction
  void testFindByIdentifikationNonUniqueResult() {
    NormdatenReferenz institution = new NormdatenReferenz("1", "SBB", "GND_1", GNDEntityFact.CORPORATE_BODY_TYPE_NAME);
    NormdatenReferenz ort = new NormdatenReferenz("2", "Berlin", "GND_2", GNDEntityFact.PLACE_TYPE_NAME);

    KulturObjektDokument kod1 = new KulturObjektDokumentBuilder()
        .withId("KOD_1")
        .withGueltigerIdentifikation(new IdentifikationBuilder()
            .withId("GI-1")
            .withIdent("HS. 1")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(institution)
            .withAufbewahrungsOrt(ort)
            .build())
        .build();
    kulturObjektDokumentRepository.save(kod1);

    KulturObjektDokument kod2 = new KulturObjektDokumentBuilder()
        .withId("KOD_2")
        .withGueltigerIdentifikation(new IdentifikationBuilder()
            .withId("GI-1")
            .withIdent("HS. 1")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(institution)
            .withAufbewahrungsOrt(ort)
            .build())
        .build();
    kulturObjektDokumentRepository.save(kod2);

    beschreibungImportService.setKulturObjektDokumentRepository(kulturObjektDokumentRepository);

    Identifikation beschreibungsIdentifikation = new IdentifikationBuilder()
        .withId("BI-1")
        .withIdent("HS. 1")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(institution)
        .withAufbewahrungsOrt(ort)
        .build();

    KulturObjektDokumentNotFoundException exception = assertThrows(KulturObjektDokumentNotFoundException.class,
        () -> beschreibungImportService.findByIdentifikation(beschreibungsIdentifikation));

    assertNotNull(exception);
    assertEquals("Es wurde mehr als eine übereinstimmende gültige KOD-Signatur zur BeschreibungsIdentifikation"
        + " gefunden: HS. 1", exception.getMessage());
  }

  @Test
  @TestTransaction
  void testFindByAlternativeIdentifikationNonUniqueResult() {
    NormdatenReferenz institution = new NormdatenReferenz("1", "SBB", "GND_1", GNDEntityFact.CORPORATE_BODY_TYPE_NAME);
    NormdatenReferenz ort = new NormdatenReferenz("2", "Berlin", "GND_2", GNDEntityFact.PLACE_TYPE_NAME);

    KulturObjektDokument kod1 = new KulturObjektDokumentBuilder()
        .withId("KOD_1")
        .withGueltigerIdentifikation(new IdentifikationBuilder()
            .withId("GI-1")
            .withIdent("HS. 1")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(institution)
            .withAufbewahrungsOrt(ort)
            .build())
        .addAlternativeIdentifikation(new IdentifikationBuilder()
            .withId("AI-1")
            .withIdent("HS-1")
            .withIdentTyp(IdentTyp.ALTSIGNATUR)
            .withBesitzer(institution)
            .withAufbewahrungsOrt(ort)
            .build())
        .build();
    kulturObjektDokumentRepository.save(kod1);

    KulturObjektDokument kod2 = new KulturObjektDokumentBuilder()
        .withId("KOD_2")
        .withGueltigerIdentifikation(new IdentifikationBuilder()
            .withId("GI-2")
            .withIdent("HS. 1")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(institution)
            .withAufbewahrungsOrt(ort)
            .build())
        .addAlternativeIdentifikation(new IdentifikationBuilder()
            .withId("AI-2")
            .withIdent("HS-1")
            .withIdentTyp(IdentTyp.ALTSIGNATUR)
            .withBesitzer(institution)
            .withAufbewahrungsOrt(ort)
            .build())
        .build();
    kulturObjektDokumentRepository.save(kod2);

    beschreibungImportService.setKulturObjektDokumentRepository(kulturObjektDokumentRepository);

    Identifikation beschreibungsIdentifikation = new IdentifikationBuilder()
        .withId("BS-1")
        .withIdent("HS-1")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(institution)
        .withAufbewahrungsOrt(ort)
        .build();

    KulturObjektDokumentNotFoundException exception = assertThrows(KulturObjektDokumentNotFoundException.class,
        () -> beschreibungImportService.findByAlternativeIdentifikation(beschreibungsIdentifikation));

    assertNotNull(exception);
    assertEquals("Es wurde mehr als eine übereinstimmende alternative KOD-Signatur zur "
        + "BeschreibungsIdentifikation gefunden: HS-1", exception.getMessage());
  }

  @Test
  @TestTransaction
  void testCreateAndAddInternalPURL() throws PURLException {
    beschreibungImportService.setPurlAutogenerateEnabled(true);

    URI purlUrl = URI.create("https://resolver.url/HSP-1234");
    URI targetUrl = URI.create("https://target.url/HSP-1234");
    URI external = URI.create("https://external.url/HSP-5678");

    PURL purlInternal = new PURL("P-1", purlUrl, targetUrl, PURLTyp.INTERNAL);
    PURL purlExternal = new PURL("P-2", external, external, PURLTyp.EXTERNAL);

    when(purlBoundary.createNewPURL("B-1", DokumentObjektTyp.HSP_DESCRIPTION))
        .thenReturn(purlInternal);

    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withId("B-1")
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION)
        .addPURL(purlExternal)
        .build();

    beschreibungImportService.createAndAddInternalPURL(beschreibung);
    assertEquals(2, beschreibung.getPURLs().size());
    assertTrue(beschreibung.getPURLs().stream().anyMatch(purl -> "P-2".equals(purl.getId())));
    assertTrue(beschreibung.getPURLs().stream().anyMatch(purl -> "P-1".equals(purl.getId())));

    verify(purlBoundary, times(1)).createNewPURL(anyString(), any(DokumentObjektTyp.class));
  }

  @Test
  @TestTransaction
  void testCreateAndAddInternalPURL_purlExists() throws PURLException {
    beschreibungImportService.setPurlAutogenerateEnabled(true);

    URI purlUrl = URI.create("https://resolver.url/HSP-1234");
    URI targetUrl = URI.create("https://target.url/HSP-1234");

    PURL purlInternal = new PURL("P-1", purlUrl, targetUrl, PURLTyp.INTERNAL);

    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withId("B-2")
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION)
        .addPURL(purlInternal)
        .build();

    beschreibungImportService.createAndAddInternalPURL(beschreibung);
    assertEquals(1, beschreibung.getPURLs().size());
    assertTrue(beschreibung.getPURLs().stream().anyMatch(purl -> "P-1".equals(purl.getId())));

    verify(purlBoundary, times(0)).createNewPURL(anyString(), any(DokumentObjektTyp.class));
  }

  @Test
  @TestTransaction
  void testCreateAndAddInternalPURL_purlError() throws PURLException {
    beschreibungImportService.setPurlAutogenerateEnabled(true);

    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withId("B-1")
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION_RETRO)
        .build();

    when(purlBoundary.createNewPURL("B-1", DokumentObjektTyp.HSP_DESCRIPTION_RETRO))
        .thenThrow(new PURLException("TestError"));

    beschreibungImportService.createAndAddInternalPURL(beschreibung);

    verify(purlBoundary, times(1)).createNewPURL(anyString(), any(DokumentObjektTyp.class));
    assertEquals(0, beschreibung.getPURLs().size());
  }

  @Test
  @TestTransaction
  void testCreateAndAddInternalPURL_disabled() throws PURLException {
    beschreibungImportService.setPurlAutogenerateEnabled(false);

    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withId("B-1")
        .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION)
        .build();

    beschreibungImportService.createAndAddInternalPURL(beschreibung);
    verify(purlBoundary, times(0)).createNewPURL(anyString(), any(DokumentObjektTyp.class));
    assertEquals(0, beschreibung.getPURLs().size());
  }

  @Test
  @TestTransaction
  void testCreateAndAddInternalPURL_VerwaltungsTypIntern() throws PURLException {
    beschreibungImportService.setPurlAutogenerateEnabled(true);

    Beschreibung beschreibung = new BeschreibungsBuilder()
        .withId("B-1")
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .build();

    beschreibungImportService.createAndAddInternalPURL(beschreibung);
    verify(purlBoundary, times(0)).createNewPURL(anyString(), any(DokumentObjektTyp.class));
    assertEquals(0, beschreibung.getPURLs().size());
  }

  private NormdatenReferenz createLanguageNormdatenReferenz() {
    return new NormdatenReferenzBuilder()
        .withId(UUID.randomUUID().toString())
        .withTypeName("Language")
        .withName("Deutsch")
        .withGndID("4113292-0")
        .addIdentifikator(
            new Identifikator("deu", "http://id.loc.gov/vocabulary/iso639-1/de", "ISO_639-1"))
        .addIdentifikator(
            new Identifikator("deu", "http://id.loc.gov/vocabulary/iso639-2/deu", "ISO_639-2"))
        .addIdentifikator(
            new Identifikator("ger", "http://id.loc.gov/vocabulary/iso639-2/ger", "ISO_639-2"))
        .addIdentifikator(new Identifikator("de", "", ""))
        .addVarianterName(new VarianterName("Neuhochdeutsch", "de"))
        .addVarianterName(new VarianterName("Deutsche Sprache", "de"))
        .addVarianterName(new VarianterName("Hochdeutsch", "de"))
        .addVarianterName(new VarianterName("Deutsch", "de"))
        .build();
  }

  private ActivityStreamObject createActivityStreamObject(String suffix)
      throws ActivityStreamsException {
    return ActivityStreamObject.builder()
        .withId(UUID.randomUUID().toString())
        .withContent(tei)
        .withName(TEI_FILENAME)
        .withId(UUID.randomUUID().toString())
        .withType(ActivityStreamsDokumentTyp.BESCHREIBUNG)
        .build();
  }

  private ActivityStream createActivityStream(ActivityStreamObject activityStreamObject,
      String suffix,
      ActivityStreamObjectTag... tags) throws ActivityStreamsException {
    ActivityStreamObject importObject = ActivityStreamObject.builder()
        .withCompressed(true)
        .withType(ActivityStreamsDokumentTyp.IMPORT)
        .withName("test.xml")
        .withTag(Arrays.asList(tags))
        .withId("1c8050b1-616c-4454-b441-9281a57bc99d")
        .withContent(
            "{\"id\":\"1c8050b1-616c-4454-b441-9281a57bc99d\",\"creationDate\":\"2020-09-24T15:22:15.926333\",\"benutzerName\":\"Redakteur\",\"importFiles\":[{\"id\":\"bec5a8c1-4e01-4d82-b19f-8189bb6b8a12\",\"path\":\"file:///tmp/hsp-import-/2020.09.24.15.22.15.925/ms_1-wthout-BOM-translated.xml\",\"dateiTyp\":\"ms_1-wthout-BOM.xml\",\"dateiName\":\""
                + TEI_FILENAME
                + "\",\"dateiFormat\":\"MXML\",\"error\":false,\"message\":null,\"importEntityData\":[{\"id\":\"HSP-c137570e-95b8-4af8-a92e-eae03d0363b2\",\"label\":\"Ms 1\",\"url\":null}]}],\"name\":\"ms_1.xml\",\"importDir\":\"/tmp/hsp-import-/2020.09.24.15.22.15.925\",\"result\":\"SUCCESS\",\"errorMessage\":\"\",\"datatype\":\"BESCHREIBUNG\"}")
        .build();

    return ActivityStream.builder()
        .withId("Berlin_test_4_190405.fmt.xml" + suffix)
        .withPublished(LocalDateTime.now())
        .withType(ActivityStreamAction.ADD)
        .addObject(activityStreamObject)
        .addObject(importObject)
        .build();
  }
}
