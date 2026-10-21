package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.WerkzeugeRechte.PERMISSION_KANN_PRAESENTATION_INDEX_AKTUALISIEREN;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.sendIndexAllForPraesentation;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexReplicationService;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrServiceException;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class WerkzeugBatchIndexPraesentation {

  private final Event<WerkzeugBatchJob> cdiEvent;
  private final BearbeiterBoundary bearbeiterBoundary;
  private final IndexReplicationService indexReplicationService;

  @Getter
  private final WerkzeugBatchJob batch = new WerkzeugBatchJob(
      "Indexing all documents in the presentation");
  // null = status unknown (solr not reachable or no details in the response)
  @Getter
  private Boolean replicationEnabled;

  @Inject
  public WerkzeugBatchIndexPraesentation(
      @sendIndexAllForPraesentation Event<WerkzeugBatchJob> cdiEvent,
      BearbeiterBoundary bearbeiterBoundary,
      IndexReplicationService indexReplicationService
  ) {
    this.cdiEvent = cdiEvent;
    this.bearbeiterBoundary = bearbeiterBoundary;
    this.indexReplicationService = indexReplicationService;
  }

  @CheckPermission(permission = PERMISSION_KANN_PRAESENTATION_INDEX_AKTUALISIEREN)
  public void execute() {
    log.info("Reindex praesentation..");
    refreshReplicationStatus();
    if (isReplicationEnabled()) {
      throw new IllegalStateException(
          "Solr replication is still enabled - disable leader replication and follower polling"
              + " before reindexing the presentation");
    }
    batch.start(bearbeiterBoundary.getLoggedBearbeiter());
    cdiEvent.fireAsync(batch)
        .exceptionally(t -> {
          batch.fail("Asynchroner Observer fehlgeschlagen: " + (t.getMessage() != null
              ? t.getMessage()
              : t.getClass().getSimpleName()));
          log.error("Asynchroner Observer beim Reindex der Praesentation fehlgeschlagen : ", t);
          return null;
        });
  }

  /**
   * Switches leader replication and follower polling together, so the follower neither receives
   * pushed commits nor pulls the incomplete index while the presentation is reindexed.
   *
   * @param enable True to enable, false to disable replication and polling.
   * @throws SolrServiceException If one of the replication commands fails.
   */
  @CheckPermission(permission = PERMISSION_KANN_PRAESENTATION_INDEX_AKTUALISIEREN)
  public void toggleReplication(boolean enable) throws SolrServiceException {
    log.info("Turning solr leader replication and follower polling {}...", enable ? "on" : "off");
    try {
      replicationEnabled = indexReplicationService.setReplicationEnabled(enable);
    } finally {
      refreshReplicationStatus();
    }
  }

  public void refreshReplicationStatus() {
    try {
      replicationEnabled = indexReplicationService.isReplicationEnabled();
    } catch (SolrServiceException e) {
      log.warn("Unable to determine solr replication status: {}", e.getMessage(), e);
      replicationEnabled = null;
    }
  }

  /**
   * @return True if leader replication or follower polling is enabled or the status is unknown,
   * false only if both are confirmed disabled. An unknown status counts as enabled, so the reindex
   * stays blocked until solr answers.
   */
  public boolean isReplicationEnabled() {
    return !Boolean.FALSE.equals(replicationEnabled);
  }

  public String getBearbeiterName() {
    return batch.getBearbeiter() != null ? batch.getBearbeiter().getName() : null;
  }

  public LocalDateTime getStartTime() {
    return batch.getStartTime();
  }

  public LocalDateTime getEndTime() {
    return batch.getEndTime();
  }

  public WerkzeugBatchJobStatus getStatus() {
    return batch.getStatus();
  }

  public String getSuccessMessage() {
    return batch.getSuccessMessage();
  }
}
