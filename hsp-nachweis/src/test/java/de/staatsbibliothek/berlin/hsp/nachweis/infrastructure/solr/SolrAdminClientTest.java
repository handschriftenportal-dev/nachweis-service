package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.util.NamedList;
import org.junit.jupiter.api.Test;

class SolrAdminClientTest {

  SolrClient solrClient = mock(SolrClient.class);
  SolrAdminClient testling = new SolrAdminClient(solrClient, "test-core");

  @Test
  void testEnableReplication() throws Exception {
    NamedList<Object> response = new NamedList<>();
    response.add(SolrAdminClient.REPLICATION_STATUS_KEY, SolrAdminClient.REPLICATION_STATUS_OK);
    when(solrClient.request(any(), any())).thenReturn(response);

    assertDoesNotThrow(testling::enableReplication);
    assertDoesNotThrow(testling::disableReplication);
  }

  @Test
  void testEnablePoll() throws Exception {
    NamedList<Object> response = new NamedList<>();
    response.add(SolrAdminClient.REPLICATION_STATUS_KEY, SolrAdminClient.REPLICATION_STATUS_OK);
    when(solrClient.request(any(), any())).thenReturn(response);

    assertDoesNotThrow(testling::enablePoll);
    assertDoesNotThrow(testling::disablePoll);
  }

  @Test
  void testEnablePoll_statusMissing() throws Exception {
    // a core without follower configuration answers with status ERR / without status OK
    when(solrClient.request(any(), any())).thenReturn(new NamedList<>());

    SolrServiceException exception =
        assertThrows(SolrServiceException.class, testling::disablePoll);
    assertTrue(exception.getMessage().contains("was not confirmed by solr"));
  }

  @Test
  void testExecuteReplicationCommand_statusMissing() throws Exception {
    // solr answers HTTP 200 without the status field for unknown commands
    when(solrClient.request(any(), any())).thenReturn(new NamedList<>());

    SolrServiceException exception =
        assertThrows(SolrServiceException.class, testling::enableReplication);
    assertTrue(exception.getMessage().contains("was not confirmed by solr"));
  }

  @Test
  void testExecuteReplicationCommand_requestFails() throws Exception {
    when(solrClient.request(any(), any())).thenThrow(new IOException("connection refused"));

    SolrServiceException exception =
        assertThrows(SolrServiceException.class, testling::disableReplication);
    assertTrue(exception.getMessage().contains("connection refused"));
  }

  @Test
  void testIsReplicationEnabled_leader() throws Exception {
    when(solrClient.request(any(), any()))
        .thenReturn(detailsResponse(SolrAdminClient.REPLICATION_LEADER_KEY,
            SolrAdminClient.REPLICATION_ENABLED_KEY, "true"));
    assertTrue(testling.isReplicationEnabled());

    when(solrClient.request(any(), any()))
        .thenReturn(detailsResponse(SolrAdminClient.REPLICATION_LEADER_KEY,
            SolrAdminClient.REPLICATION_ENABLED_KEY, "false"));
    assertFalse(testling.isReplicationEnabled());
  }

  @Test
  void testIsReplicationEnabled_masterFallback() throws Exception {
    when(solrClient.request(any(), any()))
        .thenReturn(detailsResponse(SolrAdminClient.REPLICATION_MASTER_KEY,
            SolrAdminClient.REPLICATION_ENABLED_KEY, "true"));
    assertTrue(testling.isReplicationEnabled());
  }

  @Test
  void testIsReplicationEnabled_detailsMissing() throws Exception {
    when(solrClient.request(any(), any())).thenReturn(new NamedList<>());

    SolrServiceException exception =
        assertThrows(SolrServiceException.class, testling::isReplicationEnabled);
    assertTrue(exception.getMessage().contains("No replication details"));
  }

  @Test
  void testIsPollingEnabled_follower() throws Exception {
    when(solrClient.request(any(), any()))
        .thenReturn(detailsResponse(SolrAdminClient.REPLICATION_FOLLOWER_KEY,
            SolrAdminClient.REPLICATION_POLLING_DISABLED_KEY, "false"));
    assertTrue(testling.isPollingEnabled());

    when(solrClient.request(any(), any()))
        .thenReturn(detailsResponse(SolrAdminClient.REPLICATION_FOLLOWER_KEY,
            SolrAdminClient.REPLICATION_POLLING_DISABLED_KEY, "true"));
    assertFalse(testling.isPollingEnabled());
  }

  @Test
  void testIsPollingEnabled_slaveFallback() throws Exception {
    when(solrClient.request(any(), any()))
        .thenReturn(detailsResponse(SolrAdminClient.REPLICATION_SLAVE_KEY,
            SolrAdminClient.REPLICATION_POLLING_DISABLED_KEY, "true"));
    assertFalse(testling.isPollingEnabled());
  }

  @Test
  void testIsPollingEnabled_noFollowerConfigured_returnsFalse() throws Exception {
    // details of a core without follower configuration (single solr, e.g. the testsystem)
    when(solrClient.request(any(), any()))
        .thenReturn(detailsResponse(SolrAdminClient.REPLICATION_LEADER_KEY,
            SolrAdminClient.REPLICATION_ENABLED_KEY, "true"));

    assertFalse(testling.isPollingEnabled());
  }

  @Test
  void testIsPollingEnabled_followerSectionWithoutPollingDetails_throws() throws Exception {
    when(solrClient.request(any(), any()))
        .thenReturn(detailsResponse(SolrAdminClient.REPLICATION_FOLLOWER_KEY,
            "somethingElse", "x"));

    SolrServiceException exception =
        assertThrows(SolrServiceException.class, testling::isPollingEnabled);
    assertTrue(exception.getMessage().contains("No polling details"));
  }

  @Test
  void testIsPollingConfigured() throws Exception {
    when(solrClient.request(any(), any()))
        .thenReturn(detailsResponse(SolrAdminClient.REPLICATION_FOLLOWER_KEY,
            SolrAdminClient.REPLICATION_POLLING_DISABLED_KEY, "false"));
    assertTrue(testling.isPollingConfigured());

    when(solrClient.request(any(), any()))
        .thenReturn(detailsResponse(SolrAdminClient.REPLICATION_SLAVE_KEY,
            SolrAdminClient.REPLICATION_POLLING_DISABLED_KEY, "false"));
    assertTrue(testling.isPollingConfigured());

    when(solrClient.request(any(), any()))
        .thenReturn(detailsResponse(SolrAdminClient.REPLICATION_LEADER_KEY,
            SolrAdminClient.REPLICATION_ENABLED_KEY, "true"));
    assertFalse(testling.isPollingConfigured());
  }

  private NamedList<Object> detailsResponse(String sectionKey, String fieldKey, String value) {
    NamedList<Object> sectionDetails = new NamedList<>();
    sectionDetails.add(fieldKey, value);
    NamedList<Object> details = new NamedList<>();
    details.add(sectionKey, sectionDetails);
    NamedList<Object> response = new NamedList<>();
    response.add(SolrAdminClient.REPLICATION_DETAILS_KEY, details);
    return response;
  }
}
