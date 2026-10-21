package de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrServiceException;

public interface IndexReplicationBoundary {

  /**
   * Disables leader replication and follower polling.
   *
   * @return The replication status after the command: false if replication is confirmed disabled,
   * true if it is still enabled or the status could not be determined. An unknown status counts as
   * enabled, so callers do not index against a possibly replicating solr.
   */
  boolean disableReplication();

  /**
   * Enables leader replication and follower polling.
   *
   * @return The replication status after the command: true if replication is enabled or the status
   * could not be determined, false if it is still disabled.
   */
  boolean enableReplication();

  boolean setReplicationEnabled(boolean enable) throws SolrServiceException;

  boolean isReplicationEnabled() throws SolrServiceException;
}
