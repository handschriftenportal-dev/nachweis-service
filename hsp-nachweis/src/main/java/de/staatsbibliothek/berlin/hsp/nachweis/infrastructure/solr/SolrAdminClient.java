package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BaseHttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.request.GenericSolrRequest;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.util.NamedList;

/**
 * Wraps a {@link SolrClient} pinned to one core and offers administrative operations on that core,
 * currently controlling the replication to the followers.
 */
@Slf4j
public class SolrAdminClient {

  static final String REPLICATION_PATH = "/replication";
  static final String REPLICATION_COMMAND_PARAM = "command";
  static final String REPLICATION_COMMAND_DISABLE = "disablereplication";
  static final String REPLICATION_COMMAND_ENABLE = "enablereplication";
  static final String REPLICATION_COMMAND_DISABLE_POLL = "disablepoll";
  static final String REPLICATION_COMMAND_ENABLE_POLL = "enablepoll";
  static final String REPLICATION_COMMAND_DETAILS = "details";
  static final String REPLICATION_STATUS_KEY = "status";
  static final String REPLICATION_STATUS_OK = "OK";
  static final String REPLICATION_DETAILS_KEY = "details";
  static final String REPLICATION_LEADER_KEY = "leader";
  static final String REPLICATION_MASTER_KEY = "master";
  static final String REPLICATION_FOLLOWER_KEY = "follower";
  static final String REPLICATION_SLAVE_KEY = "slave";
  static final String REPLICATION_ENABLED_KEY = "replicationEnabled";
  static final String REPLICATION_POLLING_DISABLED_KEY = "isPollingDisabled";

  private final SolrClient solrClient;
  private final String core;

  public SolrAdminClient(SolrClient solrClient, String core) {
    this.solrClient = solrClient;
    this.core = core;
  }

  public void disableReplication() throws SolrServiceException {
    executeReplicationCommand(REPLICATION_COMMAND_DISABLE);
  }

  public void enableReplication() throws SolrServiceException {
    executeReplicationCommand(REPLICATION_COMMAND_ENABLE);
  }

  /**
   * Disables the periodic polling for index updates. Only meaningful on a follower core.
   *
   * @throws SolrServiceException If the command fails or is not confirmed by solr.
   */
  public void disablePoll() throws SolrServiceException {
    executeReplicationCommand(REPLICATION_COMMAND_DISABLE_POLL);
  }

  /**
   * Enables the periodic polling for index updates. Only meaningful on a follower core.
   *
   * @throws SolrServiceException If the command fails or is not confirmed by solr.
   */
  public void enablePoll() throws SolrServiceException {
    executeReplicationCommand(REPLICATION_COMMAND_ENABLE_POLL);
  }

  /**
   * Checks whether replication is enabled on the Solr server.
   * <p>
   * Solr up to 8 names the section "master", newer versions "leader".
   * <p>
   * Testsystem runs 9.4.1, Stage and Prod run 8.11.2 @ 13.07.2026
   *
   * @return True if replication is enabled, false otherwise.
   * @throws SolrServiceException If an error occurs while checking replication status.
   */
  public boolean isReplicationEnabled() throws SolrServiceException {
    NamedList<Object> response = executeReplicationRequest(REPLICATION_COMMAND_DETAILS);

    Object replicationEnabled = Optional.ofNullable(response.findRecursive(
            REPLICATION_DETAILS_KEY, REPLICATION_LEADER_KEY, REPLICATION_ENABLED_KEY))
        .orElseGet(() -> response.findRecursive(
            REPLICATION_DETAILS_KEY, REPLICATION_MASTER_KEY, REPLICATION_ENABLED_KEY));
    if (replicationEnabled == null) {
      log.error("No replication details found in solr response: {}", response);
      throw new SolrServiceException("No replication details found in solr response");
    }
    return Boolean.parseBoolean(String.valueOf(replicationEnabled));
  }

  /**
   * Checks whether the follower polls the leader for index updates.
   * <p>
   * Solr up to 8 names the section "slave", newer versions "follower". A core without follower
   * configuration (single solr instance, e.g. the testsystem) never polls, so this returns false.
   *
   * @return True if polling is enabled, false if polling is disabled or no follower is configured.
   * @throws SolrServiceException If an error occurs while checking the polling status.
   */
  public boolean isPollingEnabled() throws SolrServiceException {
    NamedList<Object> response = executeReplicationRequest(REPLICATION_COMMAND_DETAILS);
    Object followerSection = findFollowerSection(response);
    if (followerSection == null) {
      return false;
    }
    Object pollingDisabled = ((NamedList<?>) followerSection)
        .get(REPLICATION_POLLING_DISABLED_KEY);
    if (pollingDisabled == null) {
      log.error("No polling details found in the follower section of the solr response: {}",
          response);
      throw new SolrServiceException(
          "No polling details found in the follower section of the solr response");
    }
    return !Boolean.parseBoolean(String.valueOf(pollingDisabled));
  }

  /**
   * Checks whether the core has a follower configuration at all. Poll commands fail on a core
   * without one (single solr instance, e.g. the testsystem), so callers should skip them.
   *
   * @return True if the replication details contain a follower section, false otherwise.
   * @throws SolrServiceException If an error occurs while fetching the replication details.
   */
  public boolean isPollingConfigured() throws SolrServiceException {
    NamedList<Object> response = executeReplicationRequest(REPLICATION_COMMAND_DETAILS);
    return findFollowerSection(response) != null;
  }

  private Object findFollowerSection(NamedList<Object> response) {
    return Optional.ofNullable(
            response.findRecursive(REPLICATION_DETAILS_KEY, REPLICATION_FOLLOWER_KEY))
        .orElseGet(() -> response.findRecursive(REPLICATION_DETAILS_KEY, REPLICATION_SLAVE_KEY));
  }

  /**
   * Executes a replication command on the Solr server.
   * <p>
   * Solr answers HTTP 200 even if the command is unknown or fails. Only the top-level field
   * status=OK confirms that the command was executed.
   *
   * @param command The replication command to execute.
   * @throws SolrServiceException If the replication command fails or the response does not confirm
   *                              the command execution.
   */
  private void executeReplicationCommand(String command) throws SolrServiceException {
    NamedList<Object> response = executeReplicationRequest(command);
    validateResponse(command, response);
    log.info("Executed replication command '{}'", command);
  }

  private void validateResponse(String command, NamedList<Object> response)
      throws SolrServiceException {
    Object status = response.get(REPLICATION_STATUS_KEY);
    if (!REPLICATION_STATUS_OK.equals(status)) {
      log.error("Replication command '{}' was not confirmed by solr, response: {}",
          command, response);
      throw new SolrServiceException(
          "Replication command '" + command + "' was not confirmed by solr, status: " + status);
    }
  }

  private NamedList<Object> executeReplicationRequest(String command) throws SolrServiceException {
    try {
      GenericSolrRequest request = new GenericSolrRequest(METHOD.GET, REPLICATION_PATH,
          new MapSolrParams(Map.of(REPLICATION_COMMAND_PARAM, command)));
      // requiresCollection=true -> /solr/{core}/replication
      // requiresCollection=false -> /solr/replication
      request.setRequiresCollection(true);
      return request.process(solrClient, core).getResponse();
    } catch (SolrServerException | RemoteSolrException | IOException e) {
      log.error("Unable to execute replication command '{}' {}", command, e.getMessage(), e);
      throw new SolrServiceException(
          "Unable to execute replication command '" + command + "' " + e.getMessage(), e);
    }
  }
}
