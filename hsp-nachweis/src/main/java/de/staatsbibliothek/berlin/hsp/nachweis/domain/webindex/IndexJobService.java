package de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary.SYSTEM_USERNAME;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.TEIDokument;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.common.TEIUtility;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.KatalogRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingNoneTransactionalProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.CRUDRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrAdminClient;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.producer.SolrClientQualifiers.FollowerPraesentation;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.producer.SolrClientQualifiers.LeaderPraesentation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.Serial;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@ToString
public class IndexJobService implements IndexJobBoundary, Serializable {

  @Serial
  private static final long serialVersionUID = 125789L;
  private final KulturObjektDokumentRepository kulturObjektDokumentRepository;
  private final KatalogRepository katalogRepository;
  private final KafkaIndexingNoneTransactionalProducer kafkaIndexingNoneTransactionalProducer;
  private final SolrAdminClient solrLeaderClient;
  private final SolrAdminClient solrFollowerClient;
  private final Map<IndexJobDocumentType, CRUDRepository<? extends TEIDokument, String>> indexRepositories = new EnumMap<>(
      IndexJobDocumentType.class);

  @Inject
  public IndexJobService(
      KulturObjektDokumentRepository kulturObjektDokumentRepository,
      KatalogRepository katalogRepository,
      KafkaIndexingNoneTransactionalProducer kafkaIndexingNoneTransactionalProducer,
      @LeaderPraesentation SolrAdminClient solrLeaderClient,
      @FollowerPraesentation SolrAdminClient solrFollowerClient) {
    this.kulturObjektDokumentRepository = kulturObjektDokumentRepository;
    this.katalogRepository = katalogRepository;
    this.kafkaIndexingNoneTransactionalProducer = kafkaIndexingNoneTransactionalProducer;
    this.solrLeaderClient = solrLeaderClient;
    this.solrFollowerClient = solrFollowerClient;
    indexRepositories.put(IndexJobDocumentType.KOD, kulturObjektDokumentRepository);
    indexRepositories.put(IndexJobDocumentType.KATALOG, katalogRepository);
  }

  @Override
  public void startIndexingDocuments(IndexJob indexJob) {
    log.info("Received onSendFrontEndIndexEvent {} ", indexJob);
    assertReplicationDisabled();
    removeIndex(indexJob.getActorName());
    if (IndexJobDocumentType.ALL.equals(indexJob.getIndexJobDocumentType())) {
      startIndexingAll(indexJob);
    } else {
      startIndexing(indexJob, indexRepositories.get(indexJob.getIndexJobDocumentType()));
    }
  }

  /**
   * Replication is switched manually (Werkzeuge page or Solr REST API), indexing only verifies the
   * switch really happened before it clears and rebuilds the index.
   *
   * @throws IllegalStateException If leader replication or follower polling is still enabled or the
   *                               status cannot be determined.
   */
  void assertReplicationDisabled() {
    boolean replicationEnabled;
    boolean pollingEnabled;
    try {
      replicationEnabled = solrLeaderClient.isReplicationEnabled();
      pollingEnabled = solrFollowerClient.isPollingEnabled();
    } catch (Exception error) {
      throw new IllegalStateException(
          "Unable to verify that solr replication is disabled - indexing aborted", error);
    }
    if (replicationEnabled || pollingEnabled) {
      throw new IllegalStateException(
          "Solr replication is still enabled (leader replication: " + replicationEnabled
              + ", follower polling: " + pollingEnabled
              + ") - disable both manually before indexing");
    }
  }

  void removeIndex(String actor) {
    try {
      log.info("Remove all index by actor {}", actor);
      kafkaIndexingNoneTransactionalProducer.sendRemoveAllAsActivityStreamMessage(actor);
    } catch (ActivityStreamsException e) {
      log.error("Error during remove all index message creation", e);
    }
  }

  void startIndexing(IndexJob indexJob, CRUDRepository<? extends TEIDokument, String> repository) {
    log.info("Start indexing {} repository {}", indexJob.getIndexJobDocumentType(),
        repository.getClass().getSimpleName());
    var documents = findDocumentsToIndex(indexJob, repository);
    log.info("Found {} documents to index", documents.size());
    indexDocuments(documents, ActivityStreamAction.ADD);
    log.info("Finishing Indexing of {}", indexJob.getIndexJobDocumentType());
  }

  void startIndexingAll(IndexJob indexJob) {
    indexRepositories.forEach((k, repository) -> startIndexing(indexJob, repository));
  }

  List<? extends TEIDokument> findDocumentsToIndex(IndexJob indexJob,
      CRUDRepository<? extends TEIDokument, String> repository) {
    log.info("findDocumentsToIndex for indexJob: {}", repository.getClass().getSimpleName());
    return indexJob.isSingleDocumentIndexJob()
        ? repository.findByIdOptional(indexJob.getDocumentId()).stream().toList()
        : repository.listAll().stream().toList();
  }


  public void indexDocuments(List<? extends TEIDokument> documents, ActivityStreamAction action) {
    documents.forEach(document -> indexDocument(document, action));
  }

  public void indexDocument(TEIDokument document, ActivityStreamAction action) {
    log.info("indexDocument: typ{}, id={}, activityStreamAction={}", document.getClass().getName(),
        document.getId(),
        action);
    if (TEIUtility.isTEINullOrEmpty(document)) {
      log.error("No TEI XML found for document. Skipping indexing.");
      return;
    }

    try {
      kafkaIndexingNoneTransactionalProducer.sendTEIDocumentAsActivityStreamMessage(document,
          action, true, SYSTEM_USERNAME);
    } catch (Exception exception) {
      log.error("Error during index producer message creation", exception);
    }
  }
}
