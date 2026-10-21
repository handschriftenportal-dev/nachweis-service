package de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary.SYSTEM_USERNAME;
import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEI2BeschreibungMapper;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.KODTemplateMapper;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.KatalogRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingNoneTransactionalProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.CRUDRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrAdminClient;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrServiceException;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.tei_c.ns._1.TEI;

class IndexJobServiceTest {

  static String TEI_FILENAME = "tei-msDesc_Westphal.xml";
  static TEI tei = null;

  IndexJobService indexJobBoundary;
  IndexJobBoundary indexJobStatusBoundary;
  BeschreibungsRepository beschreibungsRepository;
  KafkaIndexingProducer kafkaIndexingProducer;
  KafkaIndexingNoneTransactionalProducer kafkaIndexingNoneTransactionalProducer;
  KulturObjektDokumentRepository kulturObjektDokumentRepository;
  KatalogRepository katalogRepository;
  SolrAdminClient solrLeaderClient;
  SolrAdminClient solrFollowerClient;

  @BeforeEach
  void startUp() throws IOException, JAXBException {
    this.indexJobStatusBoundary = new IndexJobService(kulturObjektDokumentRepository,
        katalogRepository,
        kafkaIndexingNoneTransactionalProducer,
        solrLeaderClient,
        solrFollowerClient);
    this.beschreibungsRepository = mock(BeschreibungsRepository.class);
    this.kafkaIndexingProducer = mock(KafkaIndexingProducer.class);
    this.kafkaIndexingNoneTransactionalProducer = mock(
        KafkaIndexingNoneTransactionalProducer.class);
    this.kulturObjektDokumentRepository = mock(KulturObjektDokumentRepository.class);
    this.solrLeaderClient = mock(SolrAdminClient.class);
    this.solrFollowerClient = mock(SolrAdminClient.class);

    this.indexJobBoundary = new IndexJobService(kulturObjektDokumentRepository, katalogRepository,
        kafkaIndexingNoneTransactionalProducer, solrLeaderClient, solrFollowerClient);

    if (Objects.isNull(tei)) {
      Path teiFilePath = Paths
          .get("../domainmodel-tei-mapper/src", "test", "resources", "tei", TEI_FILENAME);

      try (InputStream is = newInputStream(teiFilePath)) {
        List<TEI> allTEI = TEIObjectFactory.unmarshal(is);
        tei = allTEI.get(0);
      }
    }
  }

  @Test
  void sendKafkaMessageToIndexWithId() throws Exception {
    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = beschreibungMapper.map(tei);

    Beschreibung beschreibung = beschreibungen.getFirst();

    when(beschreibungsRepository.findByIdOptional(any())).thenReturn(Optional.of(beschreibung));

    KulturObjektDokument kod = createDummyKulturObjektDokument(beschreibung);

    when(kulturObjektDokumentRepository.findByIdOptional(any())).thenReturn(Optional.of(kod));
    when(kulturObjektDokumentRepository.findById(any())).thenReturn(kod);

    IndexJob indexJob = new IndexJob("1", "System", IndexJobDocumentType.KOD);

    indexJobBoundary.startIndexingDocuments(indexJob);

    verify(kafkaIndexingNoneTransactionalProducer,
        times(1)).sendTEIDocumentAsActivityStreamMessage(kod, ActivityStreamAction.ADD,
        true, SYSTEM_USERNAME);
    verify(kafkaIndexingNoneTransactionalProducer, times(1)).sendRemoveAllAsActivityStreamMessage(
        "System");
  }

  @Test
  void sendKafkaMessageToIndexWithoutId() throws Exception {
    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = beschreibungMapper.map(tei);
    KatalogRepository katalogRepo = mock(KatalogRepository.class);

    Beschreibung beschreibung = beschreibungen.getFirst();

    when(beschreibungsRepository.findByIdOptional(any())).thenReturn(Optional.of(beschreibung));

    KulturObjektDokument kod = createDummyKulturObjektDokument(beschreibung);

    KulturObjektDokument kod2 = createDummyKulturObjektDokument(beschreibung);

    when(kulturObjektDokumentRepository.listAll()).thenReturn(List.of(kod, kod2));
    when(kulturObjektDokumentRepository.findById(kod.getId())).thenReturn(kod);
    when(kulturObjektDokumentRepository.findById(kod2.getId())).thenReturn(kod2);
    IndexJobService indexJobService = new IndexJobService(kulturObjektDokumentRepository,
        katalogRepo, kafkaIndexingNoneTransactionalProducer, solrLeaderClient, solrFollowerClient);

    IndexJob indexJob = new IndexJob("Konrad", IndexJobDocumentType.ALL);

    indexJobService.startIndexingDocuments(indexJob);

    verify(kafkaIndexingNoneTransactionalProducer,
        times(2)).sendTEIDocumentAsActivityStreamMessage(any(), any(),
        eq(true), eq(SYSTEM_USERNAME));
    verify(kafkaIndexingNoneTransactionalProducer, times(1)).sendRemoveAllAsActivityStreamMessage(
        "Konrad");
  }

  @Test
  void testIndexKulturObjektDokument() throws Exception {
    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = beschreibungMapper.map(tei);

    Beschreibung beschreibung = beschreibungen.getFirst();

    when(beschreibungsRepository.findByIdOptional(any())).thenReturn(Optional.of(beschreibung));

    KulturObjektDokument kod = createDummyKulturObjektDokument(beschreibung);

    indexJobBoundary.indexDocument(kod, ActivityStreamAction.UPDATE);

    verify(kafkaIndexingNoneTransactionalProducer,
        times(1)).sendTEIDocumentAsActivityStreamMessage(kod, ActivityStreamAction.UPDATE,
        true, SYSTEM_USERNAME);
  }

  @Test
  void testIndexKatalog() throws Exception {
    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = beschreibungMapper.map(tei);

    Beschreibung beschreibung = beschreibungen.getFirst();

    when(beschreibungsRepository.findByIdOptional(any())).thenReturn(Optional.of(beschreibung));

    Katalog katalog = new Katalog.KatalogBuilder().withId(TEIValues.UUID_PREFIX + UUID.randomUUID())
        .withErstelldatum(LocalDateTime.now())
        .withTEIXML("<xml></xml>")
        .build();

    indexJobBoundary.indexDocument(katalog, ActivityStreamAction.UPDATE);

    verify(kafkaIndexingNoneTransactionalProducer,
        times(1)).sendTEIDocumentAsActivityStreamMessage(katalog, ActivityStreamAction.UPDATE,
        true, SYSTEM_USERNAME);
  }

  @Test
  void testFindSingleDocumentToIndex() {
    KatalogRepository katalogRepo = mock(KatalogRepository.class);
    KulturObjektDokumentRepository kodRepo = mock(
        KulturObjektDokumentRepository.class);
    IndexJobService indexJobService = new IndexJobService(kodRepo,
        katalogRepo, kafkaIndexingNoneTransactionalProducer, solrLeaderClient, solrFollowerClient);

    IndexJob indexJob = new IndexJob("1", "System", IndexJobDocumentType.KOD);
    indexJobService.findDocumentsToIndex(indexJob, kodRepo);
    verify(kodRepo, times(1)).findByIdOptional(indexJob.getDocumentId());

    IndexJob indexJobKatalog = new IndexJob("2", "System", IndexJobDocumentType.KATALOG);
    indexJobService.findDocumentsToIndex(indexJobKatalog, katalogRepo);
    verify(katalogRepo, times(1)).findByIdOptional(indexJobKatalog.getDocumentId());
  }

  @Test
  void testFindAllDocumentsToIndex() {
    KulturObjektDokumentRepository kodRepo = mock(
        KulturObjektDokumentRepository.class);
    KatalogRepository katalogRepo = mock(KatalogRepository.class);
    IndexJobService indexJobService = new IndexJobService(kodRepo,
        katalogRepo, kafkaIndexingNoneTransactionalProducer, solrLeaderClient, solrFollowerClient);

    IndexJob indexJobKatalog = new IndexJob("System", IndexJobDocumentType.KATALOG);
    indexJobService.findDocumentsToIndex(indexJobKatalog, katalogRepo);
    verify(katalogRepo, times(1)).listAll();

    IndexJob indexJobKod = new IndexJob("System", IndexJobDocumentType.KOD);
    indexJobService.findDocumentsToIndex(indexJobKod, kodRepo);
    verify(kodRepo, times(1)).listAll();
  }

  @Test
  void testStartIndexingDocuments_checksReplicationBeforeIndexing() throws Exception {
    // mocks default to false = replication and polling disabled
    when(kulturObjektDokumentRepository.listAll()).thenReturn(List.of());
    IndexJob indexJob = new IndexJob("Konrad", IndexJobDocumentType.KOD);

    indexJobBoundary.startIndexingDocuments(indexJob);

    InOrder inOrder = inOrder(solrLeaderClient, solrFollowerClient,
        kafkaIndexingNoneTransactionalProducer);
    inOrder.verify(solrLeaderClient, times(1)).isReplicationEnabled();
    inOrder.verify(solrFollowerClient, times(1)).isPollingEnabled();
    inOrder.verify(kafkaIndexingNoneTransactionalProducer, times(1))
        .sendRemoveAllAsActivityStreamMessage("Konrad");
    // replication is switched manually, indexing must not touch it
    verify(solrLeaderClient, never()).disableReplication();
    verify(solrLeaderClient, never()).enableReplication();
    verify(solrFollowerClient, never()).disablePoll();
    verify(solrFollowerClient, never()).enablePoll();
  }

  @Test
  void testStartIndexingDocuments_replicationStillEnabled_abortsIndexing() throws Exception {
    when(solrLeaderClient.isReplicationEnabled()).thenReturn(true);
    IndexJob indexJob = new IndexJob("Konrad", IndexJobDocumentType.KOD);

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> indexJobBoundary.startIndexingDocuments(indexJob));
    assertTrue(exception.getMessage().contains("still enabled"));

    verify(kafkaIndexingNoneTransactionalProducer, never())
        .sendRemoveAllAsActivityStreamMessage(any());
  }

  @Test
  void testStartIndexingDocuments_followerStillPolling_abortsIndexing() throws Exception {
    when(solrLeaderClient.isReplicationEnabled()).thenReturn(false);
    when(solrFollowerClient.isPollingEnabled()).thenReturn(true);
    IndexJob indexJob = new IndexJob("Konrad", IndexJobDocumentType.KOD);

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> indexJobBoundary.startIndexingDocuments(indexJob));
    assertTrue(exception.getMessage().contains("follower polling: true"));

    verify(kafkaIndexingNoneTransactionalProducer, never())
        .sendRemoveAllAsActivityStreamMessage(any());
  }

  @Test
  void testStartIndexingDocuments_statusCheckFails_abortsIndexing() throws Exception {
    doThrow(new SolrServiceException("Test")).when(solrLeaderClient).isReplicationEnabled();
    IndexJob indexJob = new IndexJob("Konrad", IndexJobDocumentType.KOD);

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> indexJobBoundary.startIndexingDocuments(indexJob));
    assertTrue(exception.getMessage().contains("Unable to verify"));

    verify(kafkaIndexingNoneTransactionalProducer, never())
        .sendRemoveAllAsActivityStreamMessage(any());
  }

  @Test
  void testStartIndexingDocuments_indexingExceptionPropagates() {
    doThrow(new RuntimeException("indexing failed"))
        .when(kulturObjektDokumentRepository).listAll();
    IndexJob indexJob = new IndexJob("Konrad", IndexJobDocumentType.KOD);

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> indexJobBoundary.startIndexingDocuments(indexJob));
    assertEquals("indexing failed", exception.getMessage());
  }

  @Test
  void testStartIndexingAll() {
    KulturObjektDokumentRepository kulturObjektDokumentRepo = mock(
        KulturObjektDokumentRepository.class);
    KatalogRepository katalogRepo = mock(KatalogRepository.class);
    IndexJobService indexJobService = spy(new IndexJobService(kulturObjektDokumentRepo,
        katalogRepo, kafkaIndexingNoneTransactionalProducer, solrLeaderClient, solrFollowerClient));

    indexJobService.startIndexingAll(new IndexJob("System", IndexJobDocumentType.ALL));
    verify(indexJobService, times(2)).startIndexing(isA(IndexJob.class), isA(CRUDRepository.class));
  }


  private KulturObjektDokument createDummyKulturObjektDokument(Beschreibung beschreibung)
      throws Exception {

    KulturObjektDokument kulturObjektDokument = new KulturObjektDokumentBuilder()
        .withId(TEIValues.UUID_PREFIX + UUID.randomUUID())
        .withRegistrierungsDatum(LocalDateTime.now())
        .withTEIXml("<xml></xml>")
        .build();

    if (beschreibung.getBeschreibungsStruktur() != null
        && !beschreibung.getBeschreibungsStruktur().isEmpty()) {
      Optional<BeschreibungsKomponenteKopf> kopf = beschreibung.getBeschreibungsStruktur()
          .stream()
          .filter(k -> k instanceof BeschreibungsKomponenteKopf)
          .map(k -> (BeschreibungsKomponenteKopf) k).findFirst();

      kopf.flatMap(
          beschreibungsKomponenteKopf -> beschreibungsKomponenteKopf.getIdentifikationen().stream()
              .filter(i -> i.getIdentTyp().equals(IdentTyp.GUELTIGE_SIGNATUR))
              .findAny()).ifPresent(kulturObjektDokument::setGueltigeIdentifikation);
    }

    String xml = TEIObjectFactory.marshal(
        KODTemplateMapper.createTEIFromInitialTemplate(kulturObjektDokument));
    kulturObjektDokument.setTeiXML(xml);

    kulturObjektDokument.getBeschreibungenIDs().add("1");

    return kulturObjektDokument;
  }

}
