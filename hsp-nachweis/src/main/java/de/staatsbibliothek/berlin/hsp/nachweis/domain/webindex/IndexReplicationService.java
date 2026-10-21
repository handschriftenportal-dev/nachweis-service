package de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrAdminClient;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrServiceException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.producer.SolrClientQualifiers.FollowerPraesentation;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.producer.SolrClientQualifiers.LeaderPraesentation;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class IndexReplicationService implements IndexReplicationBoundary {

  private final SolrAdminClient solrLeaderClient;
  private final SolrAdminClient solrFollowerClient;

  public IndexReplicationService(
      @LeaderPraesentation SolrAdminClient solrLeaderClient,
      @FollowerPraesentation SolrAdminClient solrFollowerClient) {
    this.solrLeaderClient = solrLeaderClient;
    this.solrFollowerClient = solrFollowerClient;
  }

  @Override
  public boolean disableReplication() {
    try {
      return setReplicationEnabled(false);
    } catch (SolrServiceException e) {
      return true;
    }
  }

  @Override
  public boolean enableReplication() {
    try {
      return setReplicationEnabled(true);
    } catch (SolrServiceException e) {
      return true;
    }
  }

  @Override
  public boolean setReplicationEnabled(boolean enable) throws SolrServiceException {
    log.info("Turning solr leader replication and follower polling {}...", enable ? "on" : "off");

    boolean pollingConfigured = isPollingConfigured();
    if (enable) {
      solrLeaderClient.enableReplication();
      if (pollingConfigured) {
        solrFollowerClient.enablePoll();
      }
    } else {
      if (pollingConfigured) {
        solrFollowerClient.disablePoll();
      }
      solrLeaderClient.disableReplication();
    }

    return isReplicationEnabled();
  }

  @Override
  public boolean isReplicationEnabled() throws SolrServiceException {
    try {
      return
          solrLeaderClient.isReplicationEnabled() || solrFollowerClient.isPollingEnabled();
    } catch (Exception error) {
      log.warn("Unable to determine solr replication status: {}", error.getMessage(), error);
      throw new SolrServiceException(error);
    }
  }

  /**
   * Guards the poll commands: on a standalone Solr without follower configuration (e.g., the
   * testsystem) they would fail.
   *
   * @return true if follower polling is configured
   */
  private boolean isPollingConfigured() throws SolrServiceException {
    return solrFollowerClient.isPollingConfigured();
  }
}
