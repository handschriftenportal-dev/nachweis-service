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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation.PublikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.LizenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationStatus;
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
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBatchService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsSynchronizer;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport.ImportVorgangBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport.ImportVorgangService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentNotFoundException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.konfiguration.KonfigurationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.messaging.dto.ActivityStreamObjectDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.BeschreibungApprovalSperrenService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.PublikationApprovalService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.PublikationException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.PublikationPURLService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.PublikationsService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SolrUebernahmeException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.DataEntity;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportFile;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportServicePort;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportVorgang;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaImportProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenUpdateService;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.PublikationTeiSnapshotRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.tei_c.ns._1.TEI;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 06.06.2019.
 */
@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
@Execution(ExecutionMode.SAME_THREAD)
class BeschreibungsImportServiceTest {

  private static final Identifikation IDENTIFIKATION = new IdentifikationBuilder()
      .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
      .withIdent("1-1-1")
      .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
      .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
      .build();
  static String TEI_FILENAME = "tei-msDesc_Westphal.xml";
  static String TEI_BIG_FILENAME = "02_Wolfenbuettel_HAB_helmst-5_EXT_TEI.xml";
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
  KonfigurationBoundary konfigurationBoundary;
  @Inject
  KafkaIndexingProducer kafkaIndexingProducer;
  @Inject
  NormdatenReferenzRepository normdatenReferenzRepository;

  SuchDokumentService suchDokumentService = mock(SuchDokumentService.class);
  KafkaIndexingProducer kafkaIndexingProducerMock = mock(
      KafkaIndexingProducer.class);
  KafkaImportProducer kafkaImportProducer = mock(KafkaImportProducer.class);
  BeschreibungsImportService beschreibungsImportService;
  ImportVorgangBoundary importVorgangBoundary;
  ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
  NormdatenReferenzBoundary normdatenReferenzBoundaryMock;
  ImportServicePort importServicePort;
  Event<ImportVorgang> importVorgangEvent;
  PURLBoundary purlBoundary;
  NormdatenUpdateService normdatenUpdateService;
  PublikationsService publikationsService;
  BeschreibungsSynchronizer beschreibungsSynchronizer;

  @Inject
  BearbeiterBoundary bearbeiterBoundary;

  @Inject
  KulturObjektDokumentBoundary kulturObjektDokumentBoundary;

  @BeforeEach
  @TestTransaction
  public void init() throws IOException, JAXBException {

    Path teiFilePath = Paths
        .get("../domainmodel-tei-mapper/src", "test", "resources", "tei",
            TEI_FILENAME);

    try (InputStream is = newInputStream(teiFilePath)) {
      List<TEI> allTEI = TEIObjectFactory.unmarshal(is);
      tei = allTEI.getFirst();
    }

    suchDokumentService = mock(SuchDokumentService.class);

    kafkaIndexingProducerMock = mock(KafkaIndexingProducer.class);

    normdatenReferenzBoundaryMock = mock(NormdatenReferenzBoundary.class);

    importServicePort = mock(ImportServicePort.class);

    importVorgangEvent = mock(Event.class);

    purlBoundary = mock(PURLBoundary.class);

    importVorgangBoundary = new ImportVorgangService(kafkaImportProducer,
        importServicePort, mapper,
        importVorgangEvent);

    normdatenUpdateService = mock(NormdatenUpdateService.class);

    publikationsService = mock(PublikationsService.class);

    beschreibungsImportService = new BeschreibungsImportService();
    beschreibungsImportService.setImportVorgangBoundary(importVorgangBoundary);
    beschreibungsImportService.setPublikationsService(publikationsService);
    beschreibungsImportService.setImportRepository(repository);
    beschreibungsImportService.setBeschreibungsBatchService(
        new BeschreibungsBatchService(
            kulturObjektDokumentRepository,
            beschreibungsRepository,
            suchDokumentService,
            kafkaIndexingProducerMock));
    beschreibungsImportService.setKulturObjektDokumentBoundary(kulturObjektDokumentBoundary);
    beschreibungsImportService.setBeschreibungsRepository(
        beschreibungsRepository);
    beschreibungsImportService.setNormdatenReferenzBoundary(
        normdatenReferenzBoundaryMock);
    beschreibungsImportService.setTeiXmlValidationBoundary(
        teiXmlValidationBoundary);
    beschreibungsImportService.setKonfigurationBoundary(konfigurationBoundary);
    beschreibungsImportService.setNormdatenUpdateService(normdatenUpdateService);
    beschreibungsSynchronizer = new BeschreibungsSynchronizer(
        beschreibungsRepository,
        kulturObjektDokumentBoundary,
        bearbeiterBoundary,
        normdatenUpdateService);
    beschreibungsImportService.setBeschreibungsSynchronizer(beschreibungsSynchronizer);
    beschreibungsImportService.setAutomatischenUebernahmeAktiv(false);
    beschreibungsImportService.setImportInternPublishedEnabled(false);
  }

  @Test
  @TestTransaction
  void test_GivenRepository_AlleImporteAnzeigen()
      throws ActivityStreamsException {
    BeschreibungImport beschreibungImportJob = new BeschreibungImport(
        ImportStatus.OFFEN, null);
    repository.save(beschreibungImportJob);

    List<BeschreibungImport> result = beschreibungsImportService.alleImporteAnzeigen();

    assertTrue(result.contains(beschreibungImportJob));
  }

  @Test
  @TestTransaction
  void test_GivenRepository_FindByID() throws ActivityStreamsException {
    BeschreibungImport beschreibungImportJob = new BeschreibungImport(
        ImportStatus.OFFEN, null);
    repository.save(beschreibungImportJob);

    Optional<BeschreibungImport> result = beschreibungsImportService
        .findById(beschreibungImportJob.getId());

    assertTrue(result.isPresent());
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
    List<Beschreibung> beschreibungen = beschreibungMapper.map(
        teiList.getFirst());

    Beschreibung beschreibung = beschreibungen.getFirst();

    LocalDateTime now = LocalDateTime.now();
    PURL purlInternal = new PURL(URI.create("https://resolver.de/HSP-123"),
        URI.create("https://target.de/HSP-123"),
        PURLTyp.INTERNAL);
    Publikation publikation = new PublikationBuilder()
        .withErstpublikationsDatum(now)
        .withPublikationsDatumHSP(now)
        .addPURL(purlInternal)
        .build();
    publikation.onApproved();
    beschreibung.setPublikation(publikation);
    beschreibung.setAenderungsDatum(LocalDateTime.now());
    beschreibung.setVerwaltungsTyp(VerwaltungsTyp.EXTERN);

    KulturObjektDokument kod = createDummyKulturObjektDokument(beschreibung);

    GNDEntityFact kiel = new GNDEntityFact.GNDEntityFactBuilder().withId("123")
        .withGndIdentifier("4030481-4").withPreferredName("Kiel").build();
    GNDEntityFact bordesholm = new GNDEntityFact.GNDEntityFactBuilder().withId(
            "456")
        .withGndIdentifier("4007721-4").withPreferredName("Bordesholm").build();

    final NormdatenReferenz latein = new NormdatenReferenz("NORM-la", "la",
        "GND_ID_la");
    normdatenReferenzRepository.save(
        latein);
    final NormdatenReferenz deutsch = new NormdatenReferenz("NORM-de", "de",
        "GND_ID_de");
    normdatenReferenzRepository.save(
        deutsch);

    when(normdatenReferenzBoundaryMock.findSpracheById("GND_ID_la")).thenReturn(
        Optional.of(latein));
    when(normdatenReferenzBoundaryMock.findSpracheById("GND_ID_de")).thenReturn(
        Optional.of(deutsch));

    beschreibungsImportService.enhanceGrundsprachenAsNormdatenReferenz(
        beschreibung);

    mockAutorenNormdatenReferenzen();

    beschreibungsImportService.enhanceAutorenNormdatenReferenz(
        beschreibung);

    beschreibungsSynchronizer.updateTEIDocuments(kod, beschreibung,
        List.of(kiel, bordesholm));

    assertTrue(beschreibung.getTeiXML()
        .contains(DokumentObjektTyp.HSP_DESCRIPTION.toString()));

    assertTrue(beschreibung.getTeiXML().contains(beschreibung.getId()));

    assertTrue(beschreibung.getTeiXML().contains(
            "xml:id=\"HSP-cd421bcb-cf34-3640-9dbf-b3b3cb4ddb67\""),
        "Error find Pointer ID");

    assertTrue(beschreibung.getTeiXML().contains(
            "<term ref=\"http://d-nb.info/gnd/4007721-4\" key=\"456\" type=\"origPlace_norm\">Bordesholm</term><term ref=\"http://dnb.indo/4030481-4\" key=\"123\" type=\"origPlace_norm\">Kiel</term>"),
        beschreibung.getTeiXML());

    assertFalse(kod.getAttributsReferenzen().isEmpty());

    assertEquals(12, kod.getAttributsReferenzen().size());
    assertEquals("Agrimensoren-Codex (Codex Arcerianus)",
        kod.getAttributsReferenzen().get(AttributsTyp.TITEL).getText());
    assertEquals("Mischung aus Papier und Leinen",
        kod.getAttributsReferenzen().get(AttributsTyp.BESCHREIBSTOFF)
            .getText());
    assertEquals(12, kod.getAttributsReferenzen().values().stream()
        .filter(r -> r.getChangeID() != null).collect(Collectors.toSet())
        .size());
    assertTrue(kod.getTeiXML()
        .contains("Change Attributsreferenzen nach Publikation"));

    assertTrue(beschreibung.getTeiXML().contains(deutsch.getId()));
    assertTrue(beschreibung.getTeiXML().contains(latein.getId()));

    beschreibung.setBeschreibungsSprache(deutsch);

    beschreibungsRepository.saveAndFlush(beschreibung);
  }

  @Test
  @TestTransaction
  void testBeschreibungUebernehmenWithFailedSaveException() throws Exception {

    Event<BeschreibungImport> sendIndexMessageEvent = mock(Event.class);

    beschreibungsImportService.setBeschreibungsRepository(
        beschreibungsRepository);
    beschreibungsImportService.setSendIndexMessageEvent(sendIndexMessageEvent);

    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType("GND-ID1234",
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME)).thenReturn(
        Optional.of(new NormdatenReferenzBuilder().withId(
                "NORM-" + UUID.nameUUIDFromBytes(
                    "Herzog August Bibliothek".getBytes(
                        StandardCharsets.UTF_8)))
            .withGndID("GND-ID1234").withName("Herzog August Bibliothek")
            .build()));
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType("GND-ID3456",
        NormdatenReferenz.ORT_TYPE_NAME)).thenReturn(
        Optional.of(new NormdatenReferenzBuilder().withId(
                "NORM-" + UUID.nameUUIDFromBytes("Wolfenbüttel".getBytes(
                    StandardCharsets.UTF_8)))
            .withGndID("GND-ID3456").withName("Wolfenbüttel").build()));

    String suffix = String.valueOf(new Random().nextInt());
    ActivityStreamObject activityStreamObject = createActivityStreamObject(
        suffix);
    ActivityStream activityStream = createActivityStream(activityStreamObject,
        suffix);

    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<TEI> teiList = TEIObjectFactory
        .unmarshal(new ByteArrayInputStream(activityStreamObject.getContent()));
    List<Beschreibung> beschreibungen = beschreibungMapper.map(teiList.getFirst());

    Beschreibung beschreibung = beschreibungen.getFirst();

    KulturObjektDokument kod = createDummyKulturObjektDokument(beschreibung);
    kulturObjektDokumentRepository.save(kod);

    assertNotNull(beschreibungen);
    assertFalse(beschreibungen.isEmpty());

    beschreibung.setKodID(kod.getId());

    BeschreibungImport beschreibungImportJob = new BeschreibungImport(
        ImportStatus.OFFEN,
        activityStream);
    int initialSize = beschreibungsRepository.listAll().size();

    repository.save(beschreibungImportJob);

    doThrow(new SolrUebernahmeException("")).when(suchDokumentService)
        .kodUebernehmen(any());

    try {
      beschreibungsImportService.importeUebernehmen(
          beschreibungImportJob.getId(), "intern");
    } catch (BeschreibungsImportException e) {
      e.printStackTrace();
    }

    assertTrue(kulturObjektDokumentRepository.listAll().contains(kod));

    assertEquals(1, kod.getBeschreibungenIDs().size());

    assertFalse(
        kod.getTeiXML().contains(DokumentObjektTyp.HSP_DESCRIPTION.toString()));

    assertFalse(kod.getTeiXML().contains(beschreibung.getId()));

    assertEquals(initialSize, beschreibungsRepository.listAll().size());
  }

  @Test
  @TestTransaction
  void testsaveKODAndBeschreibungAsBatch() throws Exception {

    beschreibungsImportService.setBeschreibungsBatchService(
        new BeschreibungsBatchService(
            kulturObjektDokumentRepository,
            beschreibungsRepository,
            suchDokumentService,
            kafkaIndexingProducer));
    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = beschreibungMapper.map(tei);
    assertNotNull(beschreibungen);
    assertFalse(beschreibungen.isEmpty());

    Beschreibung beschreibung = beschreibungen.getFirst();
    beschreibung.getAutoren().clear();

    KulturObjektDokument kod = createDummyKulturObjektDokument(beschreibung);
    saveKOD(kod);

    ActivityStreamObject importObject = ActivityStreamObject.builder()
        .withCompressed(true)
        .withType(ActivityStreamsDokumentTyp.IMPORT)
        .withName("test.xml")
        .withId("1")
        .withContent("""
            {
              "id": "03c45a36-a364-4541-ad72-9e640643836b",
              "creationDate": "2020-09-24T13:24:22.657368",
              "benutzerName": "Redakteur",
              "importFiles": [
                {
                  "id": "7ee84c46-78b9-433a-94bc-4ffb3a4a291e",
                  "path": "file:///tmp/hsp-import-/2020.09.24.13.24.22.656/ms_1_neu-wthout-BOM-translated.xml",
                  "dateiTyp": "ms_1_neu-wthout-BOM.xml",
                  "dateiName": "test.xml",
                  "dateiFormat": "MXML",
                  "error": false,
                  "message": null,
                  "importEntityData": []
                }
              ],
              "name": "ms_1_neu.xml",
              "importDir": "/tmp/hsp-import-/2020.09.24.13.24.22.656",
              "result": "FAILED",
              "errorMessage": null,
              "datatype": "BESCHREIBUNG"
            }
            """)
        .build();

    ActivityStream message = ActivityStream.builder()
        .withId("Berlin_test_4_190405.fmt.xml")
        .withPublished(LocalDateTime.now())
        .withType(ActivityStreamAction.ADD)
        .addObject(importObject)
        .build();

    BeschreibungImport beschreibungImportJob = new BeschreibungImport(
        ImportStatus.OFFEN, message);
    beschreibung.getGueltigeIdentifikation().ifPresent(i -> {
      i.setBesitzer(kod.getGueltigeIdentifikation().getBesitzer());
      i.setAufbewahrungsOrt(
          kod.getGueltigeIdentifikation().getAufbewahrungsOrt());
    });

    final NormdatenReferenz latein = new NormdatenReferenz("NORM-la", "la",
        "GND_ID_la");
    normdatenReferenzRepository.saveAndFlush(
        latein);

    final NormdatenReferenz deutsch = new NormdatenReferenz("Norm-de", "de",
        "GND_ID_de");
    deutsch.getIdentifikator()
        .add(new Identifikator("de", "http://localhost/de", "ISO-639-1"));
    normdatenReferenzRepository.saveAndFlush(
        deutsch);

    when(normdatenReferenzBoundaryMock.findSpracheById("de")).thenReturn(
        Optional.of(deutsch));
    when(normdatenReferenzBoundaryMock.findSpracheById("GND_ID_de")).thenReturn(
        Optional.of(deutsch));
    when(normdatenReferenzBoundaryMock.findSpracheById("GND_ID_la")).thenReturn(
        Optional.of(latein));

    beschreibungsImportService.enhanceNormdatenReferenzSprache(beschreibung);
    beschreibungsImportService.enhanceGrundsprachenAsNormdatenReferenz(
        beschreibung);

    BeschreibungsImportService.BatchRecord batchRecords =
        new BeschreibungsImportService.BatchRecord(500);
    batchRecords.addRecord(kod, beschreibung);
    beschreibungsImportService.saveKODAndBeschreibungAsBatch(batchRecords, null);

    assertEquals(ImportStatus.OFFEN, beschreibungImportJob.getStatus());

    verify(suchDokumentService, times(1)).allBeschreibungenUebernehmen(any(List.class));
    verify(suchDokumentService, times(1)).allKODsUebernehmen(any(List.class));
  }

  @TestTransaction
  @Test
  public void testHandleReimport_SameKodID()
      throws KulturObjektDokumentMismatchException {
    String originalBeschreibungId = "beschreibung-id";
    String kodId = "kod-123";
    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId(kodId)
        .withGueltigerIdentifikation(IDENTIFIKATION)
        .build();
    Beschreibung oldBeschreibung = new BeschreibungsBuilder(originalBeschreibungId, "kod-123",
        IDENTIFIKATION).build();
    kulturObjektDokumentRepository.save(kod);
    beschreibungsRepository.save(oldBeschreibung);

    Beschreibung newBeschreibung = new BeschreibungsBuilder(originalBeschreibungId, "kod-123",
        IDENTIFIKATION).build();

    beschreibungsImportService.handleReImport(newBeschreibung, oldBeschreibung);

    Beschreibung updatedBeschreibung = beschreibungsRepository.findById(
        originalBeschreibungId);
    assertNull(updatedBeschreibung);
  }

  @TestTransaction
  @Test
  public void testHandleReimport_DifferentKodID()
      throws KulturObjektDokumentMismatchException {
    String originalBeschreibungId = "beschreibung-id";
    String kodId = "kod-123";
    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId(kodId)
        .withGueltigerIdentifikation(IDENTIFIKATION)
        .build();
    Beschreibung oldBeschreibung = new BeschreibungsBuilder(originalBeschreibungId, "kod-123",
        IDENTIFIKATION).build();
    kulturObjektDokumentRepository.save(kod);
    beschreibungsRepository.save(oldBeschreibung);

    Beschreibung newBeschreibung = new BeschreibungsBuilder(originalBeschreibungId, "kod-124",
        IDENTIFIKATION).build();

    assertThrows(KulturObjektDokumentMismatchException.class,
        () -> beschreibungsImportService.handleReImport(newBeschreibung, oldBeschreibung));
  }

  @Test
  @TestTransaction
  void testGivenIDsimporteAblehnen()
      throws ActivityStreamsException, BeschreibungsImportException {
    BeschreibungImport beschreibungImportJob = new BeschreibungImport(
        ImportStatus.OFFEN,
        mock(ActivityStreamMessage.class));

    repository.save(beschreibungImportJob);

    beschreibungsImportService.importeAblehnen(
        List.of(beschreibungImportJob.getId()));

    beschreibungImportJob = repository.findById(beschreibungImportJob.getId());

    assertEquals(ImportStatus.ABGELEHNT, beschreibungImportJob.getStatus());
  }

  @Test
  @TestTransaction
  void testimportErfolgreich() throws ActivityStreamsException {
    BeschreibungImport beschreibungImportJob = new BeschreibungImport(
        ImportStatus.OFFEN,
        mock(ActivityStreamMessage.class));
    Event<BeschreibungImport> importSendEvent = mock(Event.class);

    beschreibungsImportService.setSendIndexMessageEvent(importSendEvent);

    beschreibungsImportService.importErfolgreich(beschreibungImportJob);

    beschreibungImportJob = repository.findById(beschreibungImportJob.getId());

    assertEquals(ImportStatus.UEBERNOMMEN, beschreibungImportJob.getStatus());
    assertFalse(beschreibungImportJob.isSelectedForImport());

    verify(importSendEvent, times(1)).fire(beschreibungImportJob);
  }

  @Test
  @TestTransaction
  void testimportFehlgeschlagen() throws ActivityStreamsException {

    BeschreibungImport beschreibungImportJob = new BeschreibungImport(
        ImportStatus.OFFEN,
        mock(ActivityStreamMessage.class));
    Event<BeschreibungImport> importSendEvent = mock(Event.class);

    beschreibungsImportService.setSendIndexMessageEvent(importSendEvent);

    beschreibungsImportService.importFehlgeschlagen(beschreibungImportJob,
        new Exception(""));

    assertEquals(ImportStatus.FEHLGESCHLAGEN,
        beschreibungImportJob.getStatus());
    assertFalse(beschreibungImportJob.isSelectedForImport());

    verify(importSendEvent, times(1)).fire(beschreibungImportJob);
  }

  @Test
  @TestTransaction
  void testProcessBeschreibungen_throwsKODNotFoundWithFormattedMessageWhenFindKodEmpty()
      throws Exception {
    NormdatenReferenz german = createLanguageNormdatenReferenz();
    when(normdatenReferenzBoundaryMock.findSpracheById(anyString())).thenReturn(
        Optional.of(german));
    beschreibungsImportService.setNormdatenReferenzBoundary(normdatenReferenzBoundaryMock);

    NormdatenReferenz dresden = createDresden();
    NormdatenReferenz koerperschaft = createSLB();
    Identifikation identifikation = createIdnoMscrDresdA111(koerperschaft, dresden);

    Beschreibung beschreibung = new BeschreibungsBuilder("40170459", null, identifikation)
        .addBeschreibungsKomponente(
            new BeschreibungsKomponenteKopfBuilder().withId("kopfID").build())
        .build();

    KulturObjektDokumentNotFoundException exception = assertThrows(
        KulturObjektDokumentNotFoundException.class,
        () -> beschreibungsImportService.processBeschreibungen(
            VerwaltungsTyp.EXTERN, List.of(beschreibung), new ImportFile()));

    assertEquals(
        "Kulturobjekt konnte mit Minimaldaten von Beschreibung "
            + "Mscr.Dresd.A.111;Dresden;"
            + "Sächsische Landesbibliothek - Staats- und Universitätsbibliothek Dresden"
            + " nicht gefunden werden!",
        exception.getMessage());
  }

  @Test
  @TestTransaction
  void testProcessBeschreibungen_throwsKODNotFound_withOhneSignaturFallback()
      throws Exception {
    NormdatenReferenz german = createLanguageNormdatenReferenz();
    when(normdatenReferenzBoundaryMock.findSpracheById(anyString())).thenReturn(
        Optional.of(german));
    beschreibungsImportService.setNormdatenReferenzBoundary(normdatenReferenzBoundaryMock);

    Beschreibung beschreibung = new BeschreibungsBuilder("40170459", null, null)
        .addBeschreibungsKomponente(
            new BeschreibungsKomponenteKopfBuilder().withId("kopfID").build())
        .build();

    KulturObjektDokumentNotFoundException exception = assertThrows(
        KulturObjektDokumentNotFoundException.class,
        () -> beschreibungsImportService.processBeschreibungen(
            VerwaltungsTyp.EXTERN, List.of(beschreibung), new ImportFile()));

    assertEquals(
        "Kulturobjekt konnte mit Minimaldaten von Beschreibung ohne Signatur nicht gefunden werden!",
        exception.getMessage());
  }

  private NormdatenReferenz createSLB() {
    return new NormdatenReferenz("SLUB",
        "Sächsische Landesbibliothek - Staats- und Universitätsbibliothek Dresden",
        "");
  }

  @Test
  @TestTransaction
  void testConvertTEI2Beschreibung() throws Exception {

    beschreibungsImportService.setBeschreibungsRepository(
        beschreibungsRepository);

    NormdatenReferenz german = createLanguageNormdatenReferenz();
    when(normdatenReferenzBoundaryMock.findSpracheById(anyString())).thenReturn(
        Optional.of(german));
    beschreibungsImportService.setNormdatenReferenzBoundary(
        normdatenReferenzBoundaryMock);

    ActivityStreamObject activityStreamObject = ActivityStreamObject.builder()
        .withContent(tei)
        .withName(TEI_FILENAME)
        .withId(TEI_FILENAME)
        .withType(ActivityStreamsDokumentTyp.BESCHREIBUNG)
        .build();
    ActivityStreamObjectDTO activityStreamObjectDTO = new ActivityStreamObjectDTO(
        activityStreamObject);

    List<Beschreibung> beschreibungs = beschreibungsImportService
        .convertTEI2Beschreibung(activityStreamObjectDTO);

    assertEquals(1, beschreibungs.size());

    assertEquals("mss_36-23-aug-2f_tei-msDesc_Westphal",
        beschreibungs.getFirst().getId());

    assertFalse(beschreibungs.getFirst().getTeiXML().isEmpty());
  }

  @Test
  @TestTransaction
  void testCheckIfBeschreibungAlreadyExists() throws Exception {

    Beschreibung beschreibung = new BeschreibungsBuilder(("40170459"), "1234",
        IDENTIFIKATION)
        .addAltIdentifier("mss_36-23-aug-2f_tei-msDesc_Westphal")
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .build();

    beschreibungsRepository.save(beschreibung);

    beschreibungsImportService.setBeschreibungsRepository(
        beschreibungsRepository);

    NormdatenReferenz german = createLanguageNormdatenReferenz();
    when(normdatenReferenzBoundaryMock.findSpracheById(anyString())).thenReturn(
        Optional.of(german));
    beschreibungsImportService.setNormdatenReferenzBoundary(
        normdatenReferenzBoundaryMock);

    ActivityStreamObject activityStreamObject = ActivityStreamObject.builder()
        .withContent(tei)
        .withName(TEI_FILENAME)
        .withId(TEI_FILENAME)
        .withType(ActivityStreamsDokumentTyp.BESCHREIBUNG)
        .build();

    Optional<Beschreibung> already = beschreibungsImportService.findExistingBeschreibung(
        beschreibungsImportService
            .convertTEI2Beschreibung(
                new ActivityStreamObjectDTO(activityStreamObject))
            .getFirst());

    assertTrue(already.isPresent());
  }

  @TestTransaction
  void saveKOD(KulturObjektDokument dummyKulturObjektDokument) {
    kulturObjektDokumentRepository.persistAndFlush(dummyKulturObjektDokument);
  }

  @Test
  @TestTransaction
  void testsendKafkaMessageToImport()
      throws ActivityStreamsException, JsonProcessingException {

    String jsonJob = """
        {
          "id": "03c45a36-a364-4541-ad72-9e640643836b",
          "creationDate": "2020-09-24T13:24:22.657368",
          "benutzerName": "Redakteur",
          "importFiles": [
            {
              "id": "7ee84c46-78b9-433a-94bc-4ffb3a4a291e",
              "path": "file:///tmp/hsp-import-/2020.09.24.13.24.22.656/ms_1_neu-wthout-BOM-translated.xml",
              "dateiTyp": "ms_1_neu-wthout-BOM.xml",
              "dateiName": "ms_1_neu.xml",
              "dateiFormat": "MXML",
              "error": false,
              "message": null,
              "importEntityData": []
            }
          ],
          "name": "ms_1_neu.xml",
          "importDir": "/tmp/hsp-import-/2020.09.24.13.24.22.656",
          "result": "FAILED",
          "errorMessage": null,
          "datatype": "BESCHREIBUNG"
        }
        """;

    ImportVorgang importVorgang = ObjectMapperFactory.getObjectMapper()
        .readValue(jsonJob, ImportVorgang.class);

    importVorgangBoundary.sendImportJobToKafka(importVorgang);

    verify(kafkaImportProducer, times(1)).sendImportJobAsActivityStreamMessage(
        importVorgang);
  }

  @Test
  @TestTransaction
  void testupdateImportJsonNodeForFailure() throws Exception {

    String jsonJob = """
        {
          "id": "1c8050b1-616c-4454-b441-9281a57bc99d",
          "creationDate": "2020-09-24T15:22:15.926333",
          "benutzerName": "Redakteur",
          "importFiles": [
            {
              "id": "bec5a8c1-4e01-4d82-b19f-8189bb6b8a12",
              "path": "file:///tmp/hsp-import-/2020.09.24.15.22.15.925/ms_1-wthout-BOM-translated.xml",
              "dateiTyp": "ms_1-wthout-BOM.xml",
              "dateiName": "ms_1.xml",
              "dateiFormat": "MXML",
              "error": false,
              "message": null,
              "importEntityData": [
                {
                  "id": "HSP-c137570e-95b8-4af8-a92e-eae03d0363b2",
                  "label": "Ms 1",
                  "url": null
                }
              ]
            }
          ],
          "name": "ms_1.xml",
          "importDir": "/tmp/hsp-import-/2020.09.24.15.22.15.925",
          "result": "SUCCESS",
          "errorMessage": "",
          "datatype": "BESCHREIBUNG"
        }
        """;

    ImportVorgang importVorgang = ObjectMapperFactory.getObjectMapper()
        .readValue(jsonJob, ImportVorgang.class);

    beschreibungsImportService.updateImportJsonNodeForFailure(importVorgang,
        "Fehler");

    assertEquals("Fehler", importVorgang.getFehler());
    assertEquals(IMPORTJOB_RESULT_VALUES.FAILED, importVorgang.getStatus());

  }

  @Test
  @TestTransaction
  void testupdateImportJsonNodeForSuccess() throws Exception {

    NormdatenReferenz dresden = createDresden();
    NormdatenReferenz koerperschaft = createSLB();
    Identifikation identifikation = createIdnoMscrDresdA111(koerperschaft,
        dresden);
    Beschreibung beschreibung = new BeschreibungsBuilder("40170459", "1234",
        identifikation)
        .build();

    String jsonJob = """
        {
          "id": "1c8050b1-616c-4454-b441-9281a57bc99d",
          "creationDate": "2020-09-24T15:22:15.926333",
          "benutzerName": "Redakteur",
          "importFiles": [
            {
              "id": "bec5a8c1-4e01-4d82-b19f-8189bb6b8a12",
              "path": "file:///tmp/hsp-import-/2020.09.24.15.22.15.925/ms_1-wthout-BOM-translated.xml",
              "dateiTyp": "ms_1-wthout-BOM.xml",
              "dateiName": "ms_1.xml",
              "dateiFormat": "MXML",
              "error": false,
              "message": null,
              "importEntityData": [
                {
                  "id": "HSP-c137570e-95b8-4af8-a92e-eae03d0363b2",
                  "label": "Ms 1",
                  "url": null
                }
              ]
            }
          ],
          "name": "ms_1.xml",
          "importDir": "/tmp/hsp-import-/2020.09.24.15.22.15.925",
          "result": "SUCCESS",
          "errorMessage": "",
          "datatype": "BESCHREIBUNG"
        }
        """;

    ImportVorgang importVorgang = ObjectMapperFactory.getObjectMapper()
        .readValue(jsonJob, ImportVorgang.class);

    beschreibungsImportService
        .updateImportJsonNodeForSuccess(beschreibung,
            importVorgang.getImportFiles().getFirst());

    assertEquals(IMPORTJOB_RESULT_VALUES.SUCCESS, importVorgang.getStatus());
    assertEquals(1, importVorgang.getImportFiles().size());
    ImportFile importFile = importVorgang.getImportFiles().getFirst();
    assertEquals(2, importFile.getDataEntityList().size());
    DataEntity dataEntity1 = importFile.getDataEntityList().get(0);
    DataEntity dataEntity2 = importFile.getDataEntityList().get(1);
    assertEquals("HSP-c137570e-95b8-4af8-a92e-eae03d0363b2",
        dataEntity1.getId());
    assertEquals("Ms 1", dataEntity1.getLabel());
    assertEquals("40170459", dataEntity2.getId());
    assertEquals("Mscr.Dresd.A.111", dataEntity2.getLabel());
  }

  private Identifikation createIdnoMscrDresdA111(
      NormdatenReferenz koerperschaft,
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

  protected KulturObjektDokument createDummyKulturObjektDokument(
      Beschreibung beschreibung)
      throws IOException {

    Path xmlPath = Paths.get("src", "test", "resources", "kodinitial.xml");
    Identifikation identifikation = new Identifikation.IdentifikationBuilder().withId(
            beschreibung.getGueltigeIdentifikation().get().getId())
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withIdent(beschreibung.getGueltigeIdentifikation().get().getIdent())
        .withBesitzer(new NormdatenReferenz(
            beschreibung.getGueltigeIdentifikation().get().getBesitzer().getId()
                != null
                ? beschreibung.getGueltigeIdentifikation().get().getBesitzer()
                .getId()
                : UUID.randomUUID().toString(),
            beschreibung.getGueltigeIdentifikation().get().getBesitzer()
                .getName(),
            beschreibung.getGueltigeIdentifikation().get().getBesitzer()
                .getGndID()))
        .withAufbewahrungsOrt(new NormdatenReferenz(
            beschreibung.getGueltigeIdentifikation().get().getAufbewahrungsOrt()
                .getId() != null
                ? beschreibung.getGueltigeIdentifikation().get()
                .getAufbewahrungsOrt().getId()
                : UUID.randomUUID().toString(),
            beschreibung.getGueltigeIdentifikation().get().getAufbewahrungsOrt()
                .getName(),
            beschreibung.getGueltigeIdentifikation().get().getAufbewahrungsOrt()
                .getGndID()))
        .build();

    return new KulturObjektDokumentBuilder()
        .withId(TEIValues.UUID_PREFIX + UUID.randomUUID())
        .withRegistrierungsDatum(LocalDateTime.now())
        .withTEIXml(Files.readString(xmlPath))
        .withGueltigerIdentifikation(identifikation)
        .addBeschreibungsdokumentID(beschreibung.getId())
        .build();
  }

  @Test
  @TestTransaction
  void testEnhanceNormdatenReferenzSprache() throws Exception {

    NormdatenReferenz german = createLanguageNormdatenReferenz();
    when(normdatenReferenzBoundaryMock.findSpracheById(anyString())).thenReturn(
        Optional.of(german));
    beschreibungsImportService.setNormdatenReferenzBoundary(
        normdatenReferenzBoundaryMock);

    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = beschreibungMapper.map(tei);
    assertNotNull(beschreibungen);
    assertFalse(beschreibungen.isEmpty());
    Beschreibung beschreibung = beschreibungen.getFirst();

    beschreibungsImportService.enhanceNormdatenReferenzSprache(beschreibung);

    assertNotNull(beschreibung.getBeschreibungsSprache());
    assertEquals(german.getId(),
        beschreibung.getBeschreibungsSprache().getId());
    assertEquals(german.getName(),
        beschreibung.getBeschreibungsSprache().getName());
    assertEquals(german.getTypeName(),
        beschreibung.getBeschreibungsSprache().getTypeName());
    assertEquals(german.getGndID(),
        beschreibung.getBeschreibungsSprache().getGndID());
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
    beschreibungsImportService.setNormdatenReferenzBoundary(
        normdatenReferenzBoundaryMock);

    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = beschreibungMapper.map(tei);
    assertNotNull(beschreibungen);
    assertFalse(beschreibungen.isEmpty());
    Beschreibung beschreibung = beschreibungen.getFirst();

    beschreibung.setBeschreibungsSprache(null);

    beschreibungsImportService.enhanceNormdatenReferenzSprache(beschreibung);

    assertNull(beschreibung.getBeschreibungsSprache());
  }

  @Test
  @TestTransaction
  void testEnhanceNormdatenReferenzSprache_no_Normdatensprache()
      throws Exception {
    when(normdatenReferenzBoundaryMock.findSpracheById(anyString())).thenReturn(
        Optional.empty());
    beschreibungsImportService.setNormdatenReferenzBoundary(
        normdatenReferenzBoundaryMock);

    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = beschreibungMapper.map(tei);
    assertNotNull(beschreibungen);
    assertFalse(beschreibungen.isEmpty());
    Beschreibung beschreibung = beschreibungen.getFirst();

    Exception exception = assertThrows(BeschreibungsImportException.class,
        () -> beschreibungsImportService.enhanceNormdatenReferenzSprache(
            beschreibung));

    assertEquals(
        "Found no Language for Beschreibung mss_36-23-aug-2f_tei-msDesc_Westphal  and Sprache de",
        exception.getMessage());
  }

  @Test
  @TestTransaction
  void testAutomatischenUebernahmeAktiv() {
    assertFalse(beschreibungsImportService.isAutomatischenUebernahmeAktiv());
    beschreibungsImportService.setAutomatischenUebernahmeAktiv(true);
    assertTrue(beschreibungsImportService.isAutomatischenUebernahmeAktiv());
  }

  @Test
  @TestTransaction
  void testprocessBeschreibungen() throws Exception {
    Path teiFilePath = Paths.get("src", "test", "resources", TEI_BIG_FILENAME);
    NormdatenReferenz german = createLanguageNormdatenReferenz();
    when(normdatenReferenzBoundaryMock.findSpracheById(anyString())).thenReturn(
        Optional.of(german));
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType("8989-8",
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME)).thenReturn(
        Optional.of(new NormdatenReferenzBuilder().withId(
                "NORM-" + UUID.nameUUIDFromBytes(
                    "Herzog August Bibliothek".getBytes(
                        StandardCharsets.UTF_8)))
            .withGndID("8989-8").withName("Herzog August Bibliothek")
            .addVarianterName(
                new VarianterName("Herzog August Bibliothek Wolfenbüttel",
                    "de"))
            .build()));
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType("4066832-0",
        NormdatenReferenz.ORT_TYPE_NAME)).thenReturn(
        Optional.of(new NormdatenReferenzBuilder().withId(
                "NORM-" + UUID.nameUUIDFromBytes("Wolfenbüttel".getBytes(
                    StandardCharsets.UTF_8)))
            .withGndID("4066832-0").withName("Wolfenbüttel").build()));

    try (InputStream is = newInputStream(teiFilePath)) {
      List<TEI> allTEI = TEIObjectFactory.unmarshal(is);

      TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();

      List<Beschreibung> beschreibungen = new ArrayList<>();

      for (TEI tei : allTEI) {
        beschreibungen.addAll(beschreibungMapper.map(tei));
      }

      for (Beschreibung b : beschreibungen) {
        KulturObjektDokument kod = createDummyKulturObjektDokument(b);
        kulturObjektDokumentRepository.save(kod);
      }

      ImportFile importFile = new ImportFile();

      assertEquals(0, beschreibungsRepository.listAll().size());

      beschreibungsImportService.setBatchSize(60);
      beschreibungsImportService.processBeschreibungen(VerwaltungsTyp.EXTERN,
          beschreibungen,
          importFile);

      assertEquals(108, beschreibungsRepository.listAll().size());
      List<KulturObjektDokument> kods = kulturObjektDokumentRepository.listAll();
      assertEquals(108, kods.size());
    }
  }

  @Test
  @TestTransaction
  void testProcessBeschreibungen_reImportExtern_publizierenOhneApprovalSucceeds() throws Exception {
    // Parse beschreibung from test XML (tei-msDesc_Westphal.xml, parsed in @BeforeEach)
    TEI2BeschreibungMapper tei2BeschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = tei2BeschreibungMapper.map(tei);
    Beschreibung oldBeschreibung = beschreibungen.getFirst();

    // Create and persist KOD
    KulturObjektDokument kod = createDummyKulturObjektDokument(oldBeschreibung);
    kulturObjektDokumentRepository.save(kod);

    // Persist NormdatenReferenz entities needed by the XML-parsed beschreibung
    NormdatenReferenz latein = new NormdatenReferenz("NORM-la", "la", "GND_ID_la");
    normdatenReferenzRepository.save(latein);
    NormdatenReferenz deutsch = new NormdatenReferenz("NORM-de", "de", "GND_ID_de");
    normdatenReferenzRepository.save(deutsch);

    // Set up normdaten mocks for the enhance methods
    when(normdatenReferenzBoundaryMock.findSpracheById(anyString()))
        .thenReturn(Optional.of(latein));
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(anyString(), anyString()))
        .thenReturn(Optional.empty());

    // Set up old beschreibung as EXTERN + PUBLISHED (simulates a previous import)
    oldBeschreibung.setVerwaltungsTyp(VerwaltungsTyp.EXTERN);
    oldBeschreibung.setKodID(kod.getId());
    oldBeschreibung.getAutoren().clear();
    // Run enhance to replace XML normdaten references with persisted ones
    beschreibungsImportService.enhanceNormdatenReferenzSprache(oldBeschreibung);
    beschreibungsImportService.enhanceGrundsprachenAsNormdatenReferenz(oldBeschreibung);
    Publikation publishedPub = new PublikationBuilder(oldBeschreibung.getPublikation())
        .withLizenz(new Lizenz(LizenzTyp.CC_BY))
        .build();
    publishedPub.onApproved();
    oldBeschreibung.setPublikation(publishedPub);
    beschreibungsRepository.save(oldBeschreibung);

    assertTrue(oldBeschreibung.isPublished(), "Old beschreibung must be published for re-import");

    // Parse again to get "new" beschreibung (simulates re-import of same XML)
    List<Beschreibung> reimportBeschreibungen = tei2BeschreibungMapper.map(tei);
    Beschreibung newBeschreibung = reimportBeschreibungen.getFirst();
    newBeschreibung.getAutoren().clear();
    // Set license on new beschreibung (required by publizierenOhneApproval)
    newBeschreibung.setPublikation(new PublikationBuilder(newBeschreibung.getPublikation())
        .withLizenz(new Lizenz(LizenzTyp.CC_BY))
        .build());

    assertFalse(newBeschreibung.isPublished(),
        "New beschreibung should be DRAFT before publication");

    // Create a real PublikationsService where findById hits the real DB.
    // This is critical to reproduce the bug: after handleReImport deletes the old
    // beschreibung, reloadBeschreibung calls findById which returns empty because
    // the new beschreibung hasn't been persisted yet.
    BeschreibungsBoundary beschreibungsBoundaryForPub = mock(BeschreibungsBoundary.class);
    when(beschreibungsBoundaryForPub.findById(anyString()))
        .thenAnswer(inv -> Optional.ofNullable(
            beschreibungsRepository.findById(inv.<String>getArgument(0))));

    PublikationsService realPubService = new PublikationsService(
        beschreibungsBoundaryForPub,
        mock(PublikationPURLService.class),
        mock(BeschreibungApprovalSperrenService.class),
        new PublikationApprovalService(),
        mock(PublikationTeiSnapshotRepository.class),
        mock(Event.class)
    );
    beschreibungsImportService.setPublikationsService(realPubService);

    ImportFile importFile = new ImportFile();

    // BUG: processBeschreibungen currently throws PublikationException because
    // publizierenOhneApproval -> reloadBeschreibung -> findById returns empty
    // (the old entity was deleted by handleReImport, but the new one isn't persisted yet).
    // After the fix, this should complete successfully with the beschreibung published.
    beschreibungsImportService.processBeschreibungen(
        VerwaltungsTyp.EXTERN, List.of(newBeschreibung), importFile);

    assertTrue(newBeschreibung.isPublished(),
        "Re-imported EXTERN Beschreibung should be published after automatic publication");
  }

  @Test
  @TestTransaction
  void testProcessBeschreibungen_newImportExtern_publizierenOhneApprovalSucceeds()
      throws Exception {
    // Parse beschreibung from test XML (tei-msDesc_Westphal.xml, parsed in @BeforeEach)
    TEI2BeschreibungMapper tei2BeschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = tei2BeschreibungMapper.map(tei);
    Beschreibung newBeschreibung = beschreibungen.getFirst();
    newBeschreibung.getAutoren().clear();

    // Create and persist KOD (but NO existing beschreibung — this is a new import)
    KulturObjektDokument kod = createDummyKulturObjektDokument(newBeschreibung);
    kulturObjektDokumentRepository.save(kod);

    // Persist NormdatenReferenz entities needed by the XML-parsed beschreibung
    NormdatenReferenz latein = new NormdatenReferenz("NORM-la", "la", "GND_ID_la");
    normdatenReferenzRepository.save(latein);

    // Set up normdaten mocks for the enhance methods
    when(normdatenReferenzBoundaryMock.findSpracheById(anyString()))
        .thenReturn(Optional.of(latein));
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(anyString(), anyString()))
        .thenReturn(Optional.empty());

    // Set license on beschreibung (required by publizierenOhneApproval)
    newBeschreibung.setPublikation(new PublikationBuilder(newBeschreibung.getPublikation())
        .withLizenz(new Lizenz(LizenzTyp.CC_BY))
        .build());

    assertFalse(newBeschreibung.isPublished(),
        "New beschreibung should be DRAFT before publication");

    // Create a real PublikationsService where findById hits the real DB.
    // This reproduces the bug: handleNewImport generates an ID but never persists,
    // so reloadBeschreibung -> findById returns empty.
    BeschreibungsBoundary beschreibungsBoundaryForPub = mock(BeschreibungsBoundary.class);
    when(beschreibungsBoundaryForPub.findById(anyString()))
        .thenAnswer(inv -> Optional.ofNullable(
            beschreibungsRepository.findById(inv.<String>getArgument(0))));

    PublikationsService realPubService = new PublikationsService(
        beschreibungsBoundaryForPub,
        mock(PublikationPURLService.class),
        mock(BeschreibungApprovalSperrenService.class),
        new PublikationApprovalService(),
        mock(PublikationTeiSnapshotRepository.class),
        mock(Event.class)
    );
    beschreibungsImportService.setPublikationsService(realPubService);

    ImportFile importFile = new ImportFile();

    // BUG: processBeschreibungen currently throws PublikationException because
    // publizierenOhneApproval -> reloadBeschreibung -> findById returns empty
    // (handleNewImport generates an ID but the entity is never persisted before
    // publizierenOhneApproval is called).
    // After the fix, this should complete successfully with the beschreibung published.
    beschreibungsImportService.processBeschreibungen(
        VerwaltungsTyp.EXTERN, List.of(newBeschreibung), importFile);

    assertTrue(newBeschreibung.isPublished(),
        "New EXTERN Beschreibung should be published after automatic publication");
  }

  @Test
  @TestTransaction
  void testProcessBeschreibungen_newImportExtern_teiWithoutLicense_publizierenOhneApprovalSucceeds()
      throws Exception {
    Path noLicenseTeiPath = Path.of("src", "test", "resources",
        "tei-Wolfenbuettel-NO-LICENSE.xml");
    TEI noLicenseTei;
    try (InputStream is = newInputStream(noLicenseTeiPath)) {
      noLicenseTei = TEIObjectFactory.unmarshal(is).getFirst();
    }

    TEI2BeschreibungMapper tei2BeschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = tei2BeschreibungMapper.map(noLicenseTei);
    Beschreibung newBeschreibung = beschreibungen.getFirst();
    newBeschreibung.getAutoren().clear();

    assertNull(newBeschreibung.getPublikation().getLizenz(),
        "Regression anchor: tei-Wolfenbuettel-NO-LICENSE.xml must parse to a Publikation "
            + "without Lizenz — otherwise this test no longer covers the no-license path.");

    KulturObjektDokument kod = createDummyKulturObjektDokument(newBeschreibung);
    kulturObjektDokumentRepository.save(kod);

    NormdatenReferenz latein = new NormdatenReferenz("NORM-la", "la", "GND_ID_la");
    normdatenReferenzRepository.save(latein);

    when(normdatenReferenzBoundaryMock.findSpracheById(anyString()))
        .thenReturn(Optional.of(latein));
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(anyString(), anyString()))
        .thenReturn(Optional.empty());

    assertFalse(newBeschreibung.isPublished(),
        "New beschreibung should be DRAFT before publication");

    BeschreibungsBoundary beschreibungsBoundaryForPub = mock(BeschreibungsBoundary.class);
    when(beschreibungsBoundaryForPub.findById(anyString()))
        .thenAnswer(inv -> Optional.ofNullable(
            beschreibungsRepository.findById(inv.<String>getArgument(0))));

    PublikationsService realPubService = new PublikationsService(
        beschreibungsBoundaryForPub,
        mock(PublikationPURLService.class),
        mock(BeschreibungApprovalSperrenService.class),
        new PublikationApprovalService(),
        mock(PublikationTeiSnapshotRepository.class),
        mock(Event.class)
    );
    beschreibungsImportService.setPublikationsService(realPubService);

    ImportFile importFile = new ImportFile();

    beschreibungsImportService.processBeschreibungen(
        VerwaltungsTyp.EXTERN, List.of(newBeschreibung), importFile);

    assertTrue(newBeschreibung.isPublished(),
        "EXTERN Beschreibung without Lizenz should still be published after automatic publication");
    assertNull(newBeschreibung.getPublikation().getLizenz(),
        "Lizenz should remain null — publizierenOhneApproval must not invent a default");
  }

  @Test
  void testFormatErrorMessage_publikationException_resolvesI18nKeyAndSubstitutesArgs() {
    PublikationException ex = new PublikationException(
        "publikation_automatic_approval", "HSP-42", new RuntimeException("boom"),
        "Cod. Guelf. 113 Noviss. 4°", "License must not be null");

    String formatted = beschreibungsImportService.formatErrorMessage(ex);

    assertEquals(
        "Automatische Veröffentlichung der Beschreibung Cod. Guelf. 113 Noviss. 4° "
            + "fehlgeschlagen: License must not be null",
        formatted);
  }

  @Test
  void testFormatErrorMessage_unknownKey_returnsRawMessage() {
    RuntimeException ex = new RuntimeException("not a resource-bundle key");

    String formatted = beschreibungsImportService.formatErrorMessage(ex);

    assertEquals("not a resource-bundle key", formatted);
  }

  @Test
  void testFormatErrorMessage_nonPublikationExceptionWithKnownKey_returnsTemplateUnsubstituted() {
    RuntimeException ex = new RuntimeException("publikation_automatic_approval");

    String formatted = beschreibungsImportService.formatErrorMessage(ex);

    assertEquals("Automatische Veröffentlichung der Beschreibung {0} fehlgeschlagen: {1}",
        formatted);
  }

  @Test
  @TestTransaction
  void testOnImportMessageAutomatischUebernehmenInaktiv() throws Exception {

    Event<BeschreibungImport> sendIndexMessageEvent = mock(Event.class);

    beschreibungsImportService.setBeschreibungsRepository(
        beschreibungsRepository);
    beschreibungsImportService.setSendIndexMessageEvent(sendIndexMessageEvent);

    String suffix = String.valueOf(new Random().nextInt());
    ActivityStreamObject activityStreamObject = createActivityStreamObject(
        suffix);
    ActivityStream activityStream = createActivityStream(activityStreamObject,
        suffix);

    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<TEI> teiList = TEIObjectFactory
        .unmarshal(new ByteArrayInputStream(activityStreamObject.getContent()));
    List<Beschreibung> beschreibungen = beschreibungMapper.map(teiList.getFirst());

    Beschreibung beschreibung = beschreibungen.getFirst();

    KulturObjektDokument kod = createDummyKulturObjektDokument(beschreibung);
    kulturObjektDokumentRepository.save(kod);

    assertNotNull(beschreibungen);
    assertFalse(beschreibungen.isEmpty());

    beschreibung.setKodID(kod.getId());

    BeschreibungImport beschreibungImportJob = new BeschreibungImport(
        ImportStatus.OFFEN,
        activityStream);
    int initialSize = beschreibungsRepository.listAll().size();

    beschreibungsImportService.onImportMessage(beschreibungImportJob);

    assertTrue(kulturObjektDokumentRepository.listAll().contains(kod));

    assertEquals(1, kod.getBeschreibungenIDs().size());

    assertFalse(kod.getTeiXML().contains(DokumentObjektTyp.HSP_DESCRIPTION.toString()));

    assertFalse(kod.getTeiXML().contains(beschreibung.getId()));

    assertEquals(initialSize, beschreibungsRepository.listAll().size());

    assertTrue(
        repository.findByIdOptional(beschreibungImportJob.getId()).isPresent());
    assertEquals(ImportStatus.OFFEN, beschreibungImportJob.getStatus());
  }

  @Test
  @TestTransaction
  void testOnImportMessageAutomatischUebernehmenAktiv() throws Exception {

    Event<BeschreibungImport> sendIndexMessageEvent = mock(Event.class);
    NormdatenReferenz german = createLanguageNormdatenReferenz();
    when(normdatenReferenzBoundaryMock.findSpracheById(anyString())).thenReturn(
        Optional.of(german));
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType("GND-ID1234",
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME)).thenReturn(
        Optional.of(new NormdatenReferenzBuilder().withId(
                "NORM-" + UUID.nameUUIDFromBytes(
                    "Herzog August Bibliothek".getBytes(
                        StandardCharsets.UTF_8)))
            .withGndID("GND-ID1234").withName("Herzog August Bibliothek")
            .build()));
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType("GND-ID3456",
        NormdatenReferenz.ORT_TYPE_NAME)).thenReturn(
        Optional.of(new NormdatenReferenzBuilder().withId(
                "NORM-" + UUID.nameUUIDFromBytes("Wolfenbüttel".getBytes(
                    StandardCharsets.UTF_8)))
            .withGndID("GND-ID3456").withName("Wolfenbüttel").build()));

    beschreibungsImportService.setBeschreibungsRepository(
        beschreibungsRepository);
    beschreibungsImportService.setSendIndexMessageEvent(sendIndexMessageEvent);
    beschreibungsImportService.setAutomatischenUebernahmeAktiv(true);
    beschreibungsImportService.setNormdatenReferenzBoundary(
        normdatenReferenzBoundaryMock);
    beschreibungsImportService.setImportRepository(mock(ImportRepository.class));
    GNDEntityFact kiel = new GNDEntityFact.GNDEntityFactBuilder().withId("12")
        .withPreferredName("Kiel").withGndIdentifier("4007721-4").build();
    GNDEntityFact bordesholm = new GNDEntityFact.GNDEntityFactBuilder().withId(
            "12")
        .withPreferredName("Bordesholm").withGndIdentifier("4030481-4").build();
    beschreibungsImportService.getEntstehungsortCache().put("4007721-4", kiel);
    beschreibungsImportService.getEntstehungsortCache()
        .put("4030481-4", bordesholm);
    String suffix = String.valueOf(new Random().nextInt());
    ActivityStreamObjectTag internExternTag = new HSPActivityStreamObjectTag(
        "String",
        ActivityStreamObjectTagId.INTERN_EXTERN, "EXTERN");
    ActivityStreamObject activityStreamObject = createActivityStreamObject(
        suffix);
    ActivityStream activityStream = createActivityStream(activityStreamObject,
        suffix,
        internExternTag);
    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<TEI> teiList = TEIObjectFactory
        .unmarshal(new ByteArrayInputStream(activityStreamObject.getContent()));
    List<Beschreibung> beschreibungen = beschreibungMapper.map(teiList.get(0));
    Beschreibung beschreibung = beschreibungen.getFirst();
    KulturObjektDokument kod = createDummyKulturObjektDokument(beschreibung);
    kulturObjektDokumentRepository.deleteAll();
    kulturObjektDokumentRepository.save(kod);
    assertNotNull(beschreibungen);
    assertFalse(beschreibungen.isEmpty());
    beschreibung.setKodID(kod.getId());
    beschreibungsRepository.deleteAll();
    BeschreibungImport beschreibungImportJob = new BeschreibungImport(
        ImportStatus.OFFEN,
        activityStream);
    assertEquals(0, beschreibungsRepository.listAll().size());

    beschreibungsImportService.onImportMessage(beschreibungImportJob);

    kod = kulturObjektDokumentRepository.findById(kod.getId());
    assertEquals(2, kod.getBeschreibungenIDs().size());
    assertEquals(1, beschreibungsRepository.listAll().size());

    assertEquals(ImportStatus.UEBERNOMMEN, beschreibungImportJob.getStatus());

    beschreibungsRepository.listAll()
        .stream()
        .findFirst()
        .ifPresentOrElse(
            b -> {
              assertNotNull(b.getPublikation());
              assertEquals(PublikationStatus.DRAFT,
                  beschreibung.getPublikation().getStatus());
            },
            () -> Assertions.fail("Beschreibung not found")
        );
  }

  @Test
  @TestTransaction
  void testOnImportMessageAutomatischUebernehmenInaktivRedakteur() throws Exception {
    beschreibungsImportService.setAutomatischenUebernahmeAktiv(false);

    Event<BeschreibungImport> sendIndexMessageEvent = mock(Event.class);
    NormdatenReferenz german = createLanguageNormdatenReferenz();
    when(normdatenReferenzBoundaryMock.findSpracheById(anyString())).thenReturn(
        Optional.of(german));
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType("GND-ID1234",
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME)).thenReturn(
        Optional.of(new NormdatenReferenzBuilder().withId(
                "NORM-" + UUID.nameUUIDFromBytes(
                    "Herzog August Bibliothek".getBytes(
                        StandardCharsets.UTF_8)))
            .withGndID("GND-ID1234").withName("Herzog August Bibliothek")
            .build()));
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType("GND-ID3456",
        NormdatenReferenz.ORT_TYPE_NAME)).thenReturn(
        Optional.of(new NormdatenReferenzBuilder().withId(
                "NORM-" + UUID.nameUUIDFromBytes("Wolfenbüttel".getBytes(
                    StandardCharsets.UTF_8)))
            .withGndID("GND-ID3456").withName("Wolfenbüttel").build()));

    beschreibungsImportService.setBeschreibungsRepository(
        beschreibungsRepository);
    beschreibungsImportService.setSendIndexMessageEvent(sendIndexMessageEvent);
    beschreibungsImportService.setNormdatenReferenzBoundary(
        normdatenReferenzBoundaryMock);
    beschreibungsImportService.setImportRepository(mock(ImportRepository.class));
    GNDEntityFact kiel = new GNDEntityFact.GNDEntityFactBuilder().withId("12")
        .withPreferredName("Kiel").withGndIdentifier("4007721-4").build();
    GNDEntityFact bordesholm = new GNDEntityFact.GNDEntityFactBuilder().withId(
            "12")
        .withPreferredName("Bordesholm").withGndIdentifier("4030481-4").build();
    beschreibungsImportService.getEntstehungsortCache().put("4007721-4", kiel);
    beschreibungsImportService.getEntstehungsortCache()
        .put("4030481-4", bordesholm);
    String suffix = String.valueOf(new Random().nextInt());
    ActivityStreamObjectTag internExternTag = new HSPActivityStreamObjectTag(
        "String",
        ActivityStreamObjectTagId.INTERN_EXTERN, "EXTERN");
    ActivityStreamObject activityStreamObject = createActivityStreamObject(
        suffix);
    ActivityStream activityStream = createActivityStream(activityStreamObject,
        suffix,
        internExternTag);
    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<TEI> teiList = TEIObjectFactory
        .unmarshal(new ByteArrayInputStream(activityStreamObject.getContent()));
    List<Beschreibung> beschreibungen = beschreibungMapper.map(teiList.getFirst());
    Beschreibung beschreibung = beschreibungen.getFirst();
    KulturObjektDokument kod = createDummyKulturObjektDokument(beschreibung);
    kulturObjektDokumentRepository.deleteAll();
    kulturObjektDokumentRepository.save(kod);
    assertNotNull(beschreibungen);
    assertFalse(beschreibungen.isEmpty());
    beschreibung.setKodID(kod.getId());
    beschreibungsRepository.deleteAll();
    BeschreibungImport beschreibungImportJob = new BeschreibungImport(
        ImportStatus.OFFEN,
        activityStream);
    assertEquals(0, beschreibungsRepository.listAll().size());

    beschreibungsImportService.onImportMessage(beschreibungImportJob);

    beschreibungsImportService.findById(beschreibungImportJob.getId())
        .ifPresent(bi -> assertEquals(ImportStatus.OFFEN, bi.getStatus()));
  }

  @Test
  @TestTransaction
  void testOnImportMessageAutomatischUebernehmenInaktivUnbekannterBenutzer() throws Exception {
    beschreibungsImportService.setAutomatischenUebernahmeAktiv(false);

    Event<BeschreibungImport> sendIndexMessageEvent = mock(Event.class);
    NormdatenReferenz german = createLanguageNormdatenReferenz();
    when(normdatenReferenzBoundaryMock.findSpracheById(anyString())).thenReturn(
        Optional.of(german));
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType("GND-ID1234",
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME)).thenReturn(
        Optional.of(new NormdatenReferenzBuilder().withId(
                "NORM-" + UUID.nameUUIDFromBytes(
                    "Herzog August Bibliothek".getBytes(
                        StandardCharsets.UTF_8)))
            .withGndID("GND-ID1234").withName("Herzog August Bibliothek")
            .build()));
    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType("GND-ID3456",
        NormdatenReferenz.ORT_TYPE_NAME)).thenReturn(
        Optional.of(new NormdatenReferenzBuilder().withId(
                "NORM-" + UUID.nameUUIDFromBytes("Wolfenbüttel".getBytes(
                    StandardCharsets.UTF_8)))
            .withGndID("GND-ID3456").withName("Wolfenbüttel").build()));

    beschreibungsImportService.setBeschreibungsRepository(
        beschreibungsRepository);
    beschreibungsImportService.setSendIndexMessageEvent(sendIndexMessageEvent);
    beschreibungsImportService.setNormdatenReferenzBoundary(
        normdatenReferenzBoundaryMock);
    beschreibungsImportService.setImportRepository(mock(ImportRepository.class));
    GNDEntityFact kiel = new GNDEntityFact.GNDEntityFactBuilder().withId("12")
        .withPreferredName("Kiel").withGndIdentifier("4007721-4").build();
    GNDEntityFact bordesholm = new GNDEntityFact.GNDEntityFactBuilder().withId(
            "12")
        .withPreferredName("Bordesholm").withGndIdentifier("4030481-4").build();
    beschreibungsImportService.getEntstehungsortCache().put("4007721-4", kiel);
    beschreibungsImportService.getEntstehungsortCache()
        .put("4030481-4", bordesholm);
    String suffix = String.valueOf(new Random().nextInt());
    ActivityStreamObjectTag internExternTag = new HSPActivityStreamObjectTag(
        "String",
        ActivityStreamObjectTagId.INTERN_EXTERN, "EXTERN");
    ActivityStreamObject activityStreamObject = createActivityStreamObject(
        suffix);
    ActivityStream activityStream = createActivityStream(activityStreamObject,
        suffix,
        "unbekannterBenutzer",
        internExternTag);
    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<TEI> teiList = TEIObjectFactory
        .unmarshal(new ByteArrayInputStream(activityStreamObject.getContent()));
    List<Beschreibung> beschreibungen = beschreibungMapper.map(teiList.getFirst());
    Beschreibung beschreibung = beschreibungen.getFirst();
    KulturObjektDokument kod = createDummyKulturObjektDokument(beschreibung);
    kulturObjektDokumentRepository.deleteAll();
    kulturObjektDokumentRepository.save(kod);
    assertNotNull(beschreibungen);
    assertFalse(beschreibungen.isEmpty());
    beschreibung.setKodID(kod.getId());
    beschreibungsRepository.deleteAll();
    BeschreibungImport beschreibungImportJob = new BeschreibungImport(
        ImportStatus.OFFEN,
        activityStream);
    assertEquals(0, beschreibungsRepository.listAll().size());

    beschreibungsImportService.onImportMessage(beschreibungImportJob);

    beschreibungsImportService.findById(beschreibungImportJob.getId())
        .ifPresent(bi -> assertEquals(ImportStatus.UEBERNOMMEN, bi.getStatus()));
  }

  @Test
  @TestTransaction
  void testOnImportMessageNoVerwaltungsTyp() throws Exception {

    Event<BeschreibungImport> sendIndexMessageEvent = mock(Event.class);

    beschreibungsImportService.setBeschreibungsRepository(
        beschreibungsRepository);
    beschreibungsImportService.setSendIndexMessageEvent(sendIndexMessageEvent);
    beschreibungsImportService.setAutomatischenUebernahmeAktiv(true);
    beschreibungsImportService.setImportRepository(mock(ImportRepository.class));

    String suffix = String.valueOf(new Random().nextInt());
    ActivityStreamObject activityStreamObject = createActivityStreamObject(
        suffix);
    ActivityStream activityStream = createActivityStream(activityStreamObject,
        suffix);

    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<TEI> teiList = TEIObjectFactory
        .unmarshal(new ByteArrayInputStream(activityStreamObject.getContent()));
    List<Beschreibung> beschreibungen = beschreibungMapper.map(teiList.getFirst());

    Beschreibung beschreibung = beschreibungen.getFirst();

    KulturObjektDokument kod = createDummyKulturObjektDokument(beschreibung);
    kulturObjektDokumentRepository.save(kod);

    assertNotNull(beschreibungen);
    assertFalse(beschreibungen.isEmpty());

    beschreibung.setKodID(kod.getId());

    BeschreibungImport beschreibungImportJob = new BeschreibungImport(
        ImportStatus.OFFEN,
        activityStream);
    int initialSize = beschreibungsRepository.listAll().size();

    beschreibungsImportService.onImportMessage(beschreibungImportJob);

    assertTrue(kulturObjektDokumentRepository.listAll().contains(kod));

    assertEquals(1, kod.getBeschreibungenIDs().size());

    assertFalse(
        kod.getTeiXML().contains(DokumentObjektTyp.HSP_DESCRIPTION.toString()));

    assertFalse(kod.getTeiXML().contains(beschreibung.getId()));

    assertEquals(initialSize, beschreibungsRepository.listAll().size());

    assertEquals(ImportStatus.FEHLGESCHLAGEN,
        beschreibungImportJob.getStatus());
    assertEquals("Automatische Übernahme ohne VerwaltungsTyp nicht möglich!",
        beschreibungImportJob.getFehlerBeschreibung());
  }

  @Test
  void testenhanceAutorenNormdatenReferenz() throws Exception {
    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = beschreibungMapper.map(tei);

    assertNotNull(beschreibungen);
    assertFalse(beschreibungen.isEmpty());

    Beschreibung beschreibung = beschreibungen.getFirst();

    mockAutorenNormdatenReferenzen();

    beschreibungsImportService.enhanceAutorenNormdatenReferenz(beschreibung);

    assertFalse(beschreibung.getAutoren().isEmpty());

    verify(normdatenReferenzBoundaryMock, times(3))
        .findOneByIdOrNameAndType(
            anyString(),
            eq(NormdatenReferenz.PERSON_TYPE_NAME));

    assertEquals(3, beschreibung.getAutoren().size());

    assertTrue(
        beschreibung.getAutoren().stream()
            .anyMatch(autor -> "Werner Hoffmann".equals(autor.getName())));

    assertTrue(
        beschreibung.getAutoren().stream()
            .anyMatch(autor -> "Konrad Eichstädt".equals(autor.getName())));

    assertTrue(
        beschreibung.getAutoren().stream()
            .anyMatch(autor -> "Anton Müller".equals(autor.getName())));
  }

  private void mockAutorenNormdatenReferenzen() {

    final NormdatenReferenz autorOne =
        new NormdatenReferenzBuilder()
            .withName("Werner Hoffmann")
            .withId("NORM-345346")
            .withGndID("GNDID")
            .build();

    final NormdatenReferenz autorTwo =
        new NormdatenReferenzBuilder()
            .withName("Konrad Eichstädt")
            .withId("NORM-345347")
            .build();

    final NormdatenReferenz autorThree =
        new NormdatenReferenzBuilder()
            .withName("Anton Müller")
            .withId("NORM-345349")
            .withGndID("GNDID11")
            .build();

    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(
        "GNDID",
        NormdatenReferenz.PERSON_TYPE_NAME))
        .thenReturn(Optional.of(autorOne));

    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(
        "NORM-345347",
        NormdatenReferenz.PERSON_TYPE_NAME))
        .thenReturn(Optional.of(autorTwo));

    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(
        "GNDID11",
        NormdatenReferenz.PERSON_TYPE_NAME))
        .thenReturn(Optional.of(autorThree));
  }

  @Test
  void testenhanceAutorenNormdatenReferenz_removesAutorWhenPersNameHasNoKeyAndRef()
      throws Exception {

    Path teiFilePath = Paths.get(
        "src",
        "test",
        "resources",
        "tei-msDesc_Westphal_invalid_persName.xml");

    List<TEI> teiList = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));

    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = beschreibungMapper.map(teiList.getFirst());

    assertNotNull(beschreibungen);
    assertFalse(beschreibungen.isEmpty());

    Beschreibung beschreibung = beschreibungen.getFirst();

    assertFalse(beschreibung.getAutoren().isEmpty());

    beschreibungsImportService.enhanceAutorenNormdatenReferenz(beschreibung);

    assertTrue(beschreibung.getAutoren().isEmpty());

    verify(normdatenReferenzBoundaryMock, never())
        .findOneByIdOrNameAndType(anyString(), anyString());
  }

  @Test
  void testisKODAndBeschreibungBesitzerTheSame() {

    Identifikation gueltigeIdentifikation = new IdentifikationBuilder().withIdentTyp(
            IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(
            new NormdatenReferenz.NormdatenReferenzBuilder().withId("2323")
                .withName("Staatsbibliothek zu Berlin").withGndID("GNDwewe")
                .build()).build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("BS1",
        "KOD1",
        gueltigeIdentifikation).build();

    KulturObjektDokument kulturObjektDokument = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("KOD1").withGueltigerIdentifikation(gueltigeIdentifikation)
        .build();

    assertTrue(
        beschreibungsImportService.hasSameBesitzer(beschreibung,
            kulturObjektDokument));

    Identifikation gueltigeIdentifikationKOD = new IdentifikationBuilder().withIdentTyp(
            IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(
            new NormdatenReferenz.NormdatenReferenzBuilder().withId("2323")
                .withName(
                    "Staatsbibliothek zu Berlin -Preußischer Kulturbesitz")
                .withGndID("GNDwewe")
                .build()).build();
    kulturObjektDokument.setGueltigeIdentifikation(gueltigeIdentifikationKOD);

    assertTrue(beschreibungsImportService.hasSameBesitzer(beschreibung, kulturObjektDokument));
  }

  @Test
  void testisKODAndBeschreibungBesitzerTheSameNPE() {

    Identifikation gueltigeIdentifikation = new IdentifikationBuilder().withIdentTyp(
            IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(null).build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("BS1",
        "KOD1",
        gueltigeIdentifikation).build();

    KulturObjektDokument kulturObjektDokument = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("KOD1").withGueltigerIdentifikation(gueltigeIdentifikation)
        .build();

    assertFalse(
        beschreibungsImportService.hasSameBesitzer(beschreibung,
            kulturObjektDokument));

    Identifikation gueltigeIdentifikationKOD = new IdentifikationBuilder().withIdentTyp(
            IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(null).build();
    kulturObjektDokument.setGueltigeIdentifikation(gueltigeIdentifikationKOD);

    assertFalse(
        beschreibungsImportService.hasSameBesitzer(beschreibung,
            kulturObjektDokument));

    Beschreibung beschreibungNull = new Beschreibung.BeschreibungsBuilder("BS1",
        "KOD1",
        null).build();

    assertFalse(beschreibungsImportService.hasSameBesitzer(beschreibungNull, kulturObjektDokument));
  }

  @Test
  void testenhanceNormdatenReferenzenForGueltigeIdentifikationAndSameBesitzer() {
    Identifikation gueltigeIdentifikationKOD = new IdentifikationBuilder().withIdentTyp(
            IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(
            new NormdatenReferenz.NormdatenReferenzBuilder().withId("2323")
                .withName("Staatsbibliothek zu Berlin").withGndID("GNDwewe")
                .build())
        .withAufbewahrungsOrt(
            new NormdatenReferenz.NormdatenReferenzBuilder().withId("456")
                .withName("Berlin").withGndID("GNDBerlin").build())
        .build();

    Identifikation gueltigeIdentifikationBeschreibung = new IdentifikationBuilder().withIdentTyp(
            IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(
            new NormdatenReferenz.NormdatenReferenzBuilder().withId("2323")
                .withName("Staatsbibliothek zu Berlin").withGndID("GNDwewe")
                .build()).build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("BS1",
        "KOD1",
        gueltigeIdentifikationBeschreibung).build();

    KulturObjektDokument kulturObjektDokument = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("KOD1").withGueltigerIdentifikation(gueltigeIdentifikationKOD)
        .build();

    beschreibungsImportService.enhanceNormdatenReferenzenForGueltigeIdentifikation(
        beschreibung,
        kulturObjektDokument);

    assertEquals(
        beschreibung.getGueltigeIdentifikation().get().getAufbewahrungsOrt(),
        gueltigeIdentifikationKOD.getAufbewahrungsOrt());
  }

  @Test
  void testenhanceNormdatenReferenzenForGueltigeIdentifikationAndNotSameBesitzer() {
    Identifikation gueltigeIdentifikationKOD = new IdentifikationBuilder().withIdentTyp(
            IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(
            new NormdatenReferenz.NormdatenReferenzBuilder().withId("2323")
                .withName("Staatsbibliothek zu Berlin").withGndID("GNDwewe")
                .build())
        .withAufbewahrungsOrt(
            new NormdatenReferenz.NormdatenReferenzBuilder().withId("456")
                .withName("Berlin").withGndID("GNDBerlin").build())
        .build();

    Identifikation gueltigeIdentifikationBeschreibung = new IdentifikationBuilder().withIdentTyp(
            IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(
            new NormdatenReferenz.NormdatenReferenzBuilder().withId("2323")
                .withName(
                    "Staatsbibliothek zu Berlin - Preußischer Kulturbesitz")
                .withGndID("GNDAndersAlsKOD").build()).build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("BS1",
        "KOD1",
        gueltigeIdentifikationBeschreibung).build();

    KulturObjektDokument kulturObjektDokument = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("KOD1").withGueltigerIdentifikation(gueltigeIdentifikationKOD)
        .build();

    when(normdatenReferenzBoundaryMock.findOneByIdOrNameAndType(
        "GNDAndersAlsKOD",
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME)).thenReturn(
        Optional.of(gueltigeIdentifikationKOD.getBesitzer()));

    beschreibungsImportService.enhanceNormdatenReferenzenForGueltigeIdentifikation(
        beschreibung,
        kulturObjektDokument);

    assertEquals(
        beschreibung.getGueltigeIdentifikation().get().getAufbewahrungsOrt(),
        gueltigeIdentifikationKOD.getAufbewahrungsOrt());
  }

  @Test
  void testfindNormdatenReferenzID() {
    final String id = "ID";

    NormdatenReferenz referenzWithGNDID = new NormdatenReferenz.NormdatenReferenzBuilder().withGndID(
            id)
        .build();

    assertEquals(id,
        beschreibungsImportService.findNormdatenReferenzID(referenzWithGNDID));

    NormdatenReferenz referenzWithName = new NormdatenReferenz.NormdatenReferenzBuilder().withGndID(
            "").withName(id)
        .build();

    assertEquals(id,
        beschreibungsImportService.findNormdatenReferenzID(referenzWithName));
  }

  @Test
  @TestTransaction
  void testEnhanceGrundsprachenAsNormdatenReferenz() throws Exception {
    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = beschreibungMapper.map(tei);
    assertNotNull(beschreibungen);

    Beschreibung beschreibung = beschreibungen.getFirst();

    assertNotNull(beschreibung);
    assertTrue(beschreibung.findKopf().isPresent());

    beschreibung.findKopf()
        .ifPresent(kopf -> assertEquals(2, kopf.getGrundsprachen().size()));

    beschreibungen = beschreibungMapper.map(tei);
    beschreibung = beschreibungen.getFirst();
    beschreibung.findKopf()
        .ifPresent(kopf -> {
          assertEquals(2, kopf.getGrundsprachen().size());
          kopf.getGrundsprachen().forEach(grundsprache -> {
            if (grundsprache.getGndID() == null || grundsprache.getGndID()
                .isEmpty()) {
              grundsprache.setGndID("GND_ID_" + grundsprache.getName());
            }
          });
        });

    when(normdatenReferenzBoundaryMock.findSpracheById("GND_ID_de")).thenReturn(
        Optional.of(new NormdatenReferenz("NORM-1", "deutsch",
            "GND_ID_de")));
    when(normdatenReferenzBoundaryMock.findSpracheById("GND_ID_la")).thenReturn(
        Optional.of(new NormdatenReferenz("NORM-2", "deutsch",
            "GND_ID_la")));

    beschreibungsImportService.enhanceGrundsprachenAsNormdatenReferenz(
        beschreibung);
    beschreibung.findKopf()
        .ifPresent(kopf -> assertEquals(2, kopf.getGrundsprachen().size()));

  }

  private NormdatenReferenz createLanguageNormdatenReferenz() {
    return new NormdatenReferenzBuilder()
        .withId(UUID.randomUUID().toString())
        .withTypeName("Language")
        .withName("Deutsch")
        .withGndID("4113292-0")
        .addIdentifikator(
            new Identifikator("deu", "http://id.loc.gov/vocabulary/iso639-1/de",
                "ISO_639-1"))
        .addIdentifikator(
            new Identifikator("deu",
                "http://id.loc.gov/vocabulary/iso639-2/deu", "ISO_639-2"))
        .addIdentifikator(
            new Identifikator("ger",
                "http://id.loc.gov/vocabulary/iso639-2/ger", "ISO_639-2"))
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

  private ActivityStream createActivityStream(
      ActivityStreamObject activityStreamObject,
      String suffix,
      ActivityStreamObjectTag... tags) throws ActivityStreamsException {
    return createActivityStream(
        activityStreamObject,
        suffix,
        "Redakteur",
        tags);
  }

  private ActivityStream createActivityStream(
      ActivityStreamObject activityStreamObject,
      String suffix,
      String benutzerName,
      ActivityStreamObjectTag... tags) throws ActivityStreamsException {
    ActivityStreamObject importObject = ActivityStreamObject.builder()
        .withCompressed(true)
        .withType(ActivityStreamsDokumentTyp.IMPORT)
        .withName("test.xml")
        .withTag(Arrays.asList(tags))
        .withId("1c8050b1-616c-4454-b441-9281a57bc99d")
        .withContent("""
            {
              "id": "1c8050b1-616c-4454-b441-9281a57bc99d",
              "creationDate": "2020-09-24T15:22:15.926333",
              "benutzerName": "%s",
              "importFiles": [
                {
                  "id": "bec5a8c1-4e01-4d82-b19f-8189bb6b8a12",
                  "path": "file:///tmp/hsp-import-/2020.09.24.15.22.15.925/ms_1-wthout-BOM-translated.xml",
                  "dateiTyp": "ms_1-wthout-BOM.xml",
                  "dateiName": "%s",
                  "dateiFormat": "MXML",
                  "error": false,
                  "message": null,
                  "importEntityData": [
                    {
                      "id": "HSP-c137570e-95b8-4af8-a92e-eae03d0363b2",
                      "label": "Ms 1",
                      "url": null
                    }
                  ]
                }
              ],
              "name": "ms_1.xml",
              "importDir": "/tmp/hsp-import-/2020.09.24.15.22.15.925",
              "result": "SUCCESS",
              "errorMessage": "",
              "datatype": "BESCHREIBUNG"
            }
            """.formatted(benutzerName, TEI_FILENAME))
        .build();

    return ActivityStream.builder()
        .withId("Berlin_test_4_190405.fmt.xml" + suffix)
        .withPublished(LocalDateTime.now())
        .withType(ActivityStreamAction.ADD)
        .addObject(activityStreamObject)
        .addObject(importObject)
        .build();
  }

  @Test
  @TestTransaction
  void testAssignId_withoutKulturObjektDokument() {
    String originalId = "original-id";
    Beschreibung beschreibung = new BeschreibungsBuilder(originalId, "", IDENTIFIKATION).build();
    String originalIdCopy = beschreibung.getId();

    beschreibungsImportService.handleNewImport(beschreibung, null);

    assertNotNull(beschreibung.getId());
    assertTrue(beschreibung.getId().startsWith(TEIValues.UUID_PREFIX));
    assertNotEquals(originalIdCopy, beschreibung.getId());
    assertTrue(beschreibung.getAltIdentifier().contains(originalIdCopy));
    assertEquals(1, beschreibung.getAltIdentifier().size());
  }

  @Test
  @TestTransaction
  void testAssignId_withKulturObjektDokument() {
    String originalBeschreibungId = "beschreibung-id";
    String kodId = "kod-123";
    Beschreibung beschreibung = new BeschreibungsBuilder(originalBeschreibungId, "",
        IDENTIFIKATION).build();
    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId(kodId)
        .withGueltigerIdentifikation(IDENTIFIKATION)
        .build();

    beschreibungsImportService.handleNewImport(beschreibung, kod);

    assertNotNull(beschreibung.getId());
    assertTrue(beschreibung.getId().startsWith(TEIValues.UUID_PREFIX));
    assertNotEquals(originalBeschreibungId, beschreibung.getId());
    assertTrue(beschreibung.getAltIdentifier().contains(originalBeschreibungId));
    assertTrue(beschreibung.getAltIdentifier().contains(kodId));
    assertEquals(2, beschreibung.getAltIdentifier().size());
  }
}
