package de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrAdminClient;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrServiceException;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class IndexReplicationServiceTest {

  SolrAdminClient solrLeaderClient = mock(SolrAdminClient.class);
  SolrAdminClient solrFollowerClient = mock(SolrAdminClient.class);
  IndexReplicationService testling = new IndexReplicationService(
      solrLeaderClient, solrFollowerClient);

  void mockStatusDisabled() throws SolrServiceException {
    when(solrLeaderClient.isReplicationEnabled()).thenReturn(false);
    when(solrFollowerClient.isPollingEnabled()).thenReturn(false);
  }

  void mockStatusEnabled() throws SolrServiceException {
    when(solrLeaderClient.isReplicationEnabled()).thenReturn(true);
    when(solrFollowerClient.isPollingEnabled()).thenReturn(true);
  }

  @Test
  void testSetReplicationEnabled_disable_stopsPollingBeforeLeader() throws Exception {
    when(solrFollowerClient.isPollingConfigured()).thenReturn(true);
    mockStatusDisabled();

    boolean stillEnabled = testling.setReplicationEnabled(false);

    assertFalse(stillEnabled);
    // the follower must stop polling before the leader stops pushing, so it never
    // pulls a half-built index
    InOrder inOrder = inOrder(solrFollowerClient, solrLeaderClient);
    inOrder.verify(solrFollowerClient).disablePoll();
    inOrder.verify(solrLeaderClient).disableReplication();
    verify(solrLeaderClient, never()).enableReplication();
    verify(solrFollowerClient, never()).enablePoll();
  }

  @Test
  void testSetReplicationEnabled_enable_startsLeaderBeforePolling() throws Exception {
    when(solrFollowerClient.isPollingConfigured()).thenReturn(true);
    mockStatusEnabled();

    boolean enabled = testling.setReplicationEnabled(true);

    assertTrue(enabled);
    InOrder inOrder = inOrder(solrLeaderClient, solrFollowerClient);
    inOrder.verify(solrLeaderClient).enableReplication();
    inOrder.verify(solrFollowerClient).enablePoll();
    verify(solrLeaderClient, never()).disableReplication();
    verify(solrFollowerClient, never()).disablePoll();
  }

  @Test
  void testSetReplicationEnabled_withoutFollowerConfiguration_switchesOnlyLeader()
      throws Exception {
    // single solr instance (e.g. the testsystem): no follower to switch
    when(solrFollowerClient.isPollingConfigured()).thenReturn(false);
    mockStatusDisabled();

    testling.setReplicationEnabled(false);
    testling.setReplicationEnabled(true);

    verify(solrLeaderClient, times(1)).disableReplication();
    verify(solrLeaderClient, times(1)).enableReplication();
    verify(solrFollowerClient, never()).disablePoll();
    verify(solrFollowerClient, never()).enablePoll();
  }

  @Test
  void testSetReplicationEnabled_commandFails_throws() throws Exception {
    when(solrFollowerClient.isPollingConfigured()).thenReturn(true);
    doThrow(new SolrServiceException("boom")).when(solrLeaderClient).disableReplication();

    assertThrows(SolrServiceException.class, () -> testling.setReplicationEnabled(false));

    verify(solrFollowerClient, times(1)).disablePoll();
  }

  @Test
  void testDisableReplication_success_returnsFalse() throws Exception {
    when(solrFollowerClient.isPollingConfigured()).thenReturn(true);
    mockStatusDisabled();

    // return value is the replication status after the command: false = disabled
    assertFalse(testling.disableReplication());

    verify(solrFollowerClient, times(1)).disablePoll();
    verify(solrLeaderClient, times(1)).disableReplication();
  }

  @Test
  void testDisableReplication_commandNotEffective_returnsTrue() throws Exception {
    when(solrFollowerClient.isPollingConfigured()).thenReturn(true);
    // solr confirmed the command but still reports replication as enabled
    mockStatusEnabled();

    assertTrue(testling.disableReplication());
  }

  @Test
  void testDisableReplication_solrUnreachable_returnsTrue() throws Exception {
    when(solrFollowerClient.isPollingConfigured())
        .thenThrow(new SolrServiceException("solr down"));

    // unknown status counts as enabled, so callers do not index against a
    // possibly replicating solr
    assertTrue(testling.disableReplication());
  }

  @Test
  void testEnableReplication_success_returnsTrue() throws Exception {
    when(solrFollowerClient.isPollingConfigured()).thenReturn(true);
    mockStatusEnabled();

    assertTrue(testling.enableReplication());

    verify(solrLeaderClient, times(1)).enableReplication();
    verify(solrFollowerClient, times(1)).enablePoll();
  }

  @Test
  void testEnableReplication_solrUnreachable_returnsTrue() throws Exception {
    when(solrFollowerClient.isPollingConfigured())
        .thenThrow(new SolrServiceException("solr down"));

    assertTrue(testling.enableReplication());
  }

  @Test
  void testIsReplicationEnabled_leaderPushing() throws Exception {
    when(solrLeaderClient.isReplicationEnabled()).thenReturn(true);
    when(solrFollowerClient.isPollingEnabled()).thenReturn(false);

    assertTrue(testling.isReplicationEnabled());
  }

  @Test
  void testIsReplicationEnabled_followerStillPolling() throws Exception {
    // follower still polling counts as enabled even if the leader stopped pushing
    when(solrLeaderClient.isReplicationEnabled()).thenReturn(false);
    when(solrFollowerClient.isPollingEnabled()).thenReturn(true);

    assertTrue(testling.isReplicationEnabled());
  }

  @Test
  void testIsReplicationEnabled_bothDisabled() throws Exception {
    mockStatusDisabled();

    assertFalse(testling.isReplicationEnabled());
  }

  @Test
  void testIsReplicationEnabled_solrUnreachable_throws() throws Exception {
    when(solrLeaderClient.isReplicationEnabled())
        .thenThrow(new SolrServiceException("solr down"));

    assertThrows(SolrServiceException.class, () -> testling.isReplicationEnabled());
  }
}