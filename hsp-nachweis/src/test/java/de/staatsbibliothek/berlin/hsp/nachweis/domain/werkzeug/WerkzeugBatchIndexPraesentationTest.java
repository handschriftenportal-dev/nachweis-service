package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexReplicationService;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrServiceException;
import jakarta.enterprise.event.Event;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

class WerkzeugBatchIndexPraesentationTest {

  IndexReplicationService indexReplicationService = mock(IndexReplicationService.class);
  Event<WerkzeugBatchJob> cdiEvent = mock(Event.class);
  BearbeiterBoundary bearbeiterBoundary = mock(BearbeiterBoundary.class);
  WerkzeugBatchIndexPraesentation testling = new WerkzeugBatchIndexPraesentation(
      cdiEvent, bearbeiterBoundary, indexReplicationService);

  void mockReplicationDisabled() throws SolrServiceException {
    when(indexReplicationService.isReplicationEnabled()).thenReturn(false);
  }

  @Test
  void testRefreshReplicationStatus() throws Exception {
    assertNull(testling.getReplicationEnabled());
    assertTrue(testling.isReplicationEnabled());

    when(indexReplicationService.isReplicationEnabled()).thenReturn(true);
    testling.refreshReplicationStatus();
    assertEquals(Boolean.TRUE, testling.getReplicationEnabled());
    assertTrue(testling.isReplicationEnabled());

    mockReplicationDisabled();
    testling.refreshReplicationStatus();
    assertEquals(Boolean.FALSE, testling.getReplicationEnabled());
    assertFalse(testling.isReplicationEnabled());

    // unknown status counts as enabled, so the reindex stays blocked
    when(indexReplicationService.isReplicationEnabled())
        .thenThrow(new SolrServiceException("Test"));
    testling.refreshReplicationStatus();
    assertNull(testling.getReplicationEnabled());
    assertTrue(testling.isReplicationEnabled());
  }

  @Test
  void testToggleReplication_disable() throws Exception {
    when(indexReplicationService.setReplicationEnabled(false)).thenReturn(false);
    mockReplicationDisabled();

    testling.toggleReplication(false);

    verify(indexReplicationService, times(1)).setReplicationEnabled(false);
    verify(indexReplicationService, never()).setReplicationEnabled(true);
    assertFalse(testling.isReplicationEnabled());
  }

  @Test
  void testToggleReplication_enable() throws Exception {
    when(indexReplicationService.setReplicationEnabled(true)).thenReturn(true);
    when(indexReplicationService.isReplicationEnabled()).thenReturn(true);

    testling.toggleReplication(true);

    verify(indexReplicationService, times(1)).setReplicationEnabled(true);
    verify(indexReplicationService, never()).setReplicationEnabled(false);
    assertTrue(testling.isReplicationEnabled());
  }

  @Test
  void testToggleReplication_commandFails_refreshesStatusAndRethrows() throws Exception {
    when(indexReplicationService.setReplicationEnabled(false))
        .thenThrow(new SolrServiceException("boom"));
    mockReplicationDisabled();

    assertThrows(SolrServiceException.class, () -> testling.toggleReplication(false));

    // status was refreshed in spite of the failed command
    verify(indexReplicationService, times(1)).isReplicationEnabled();
    assertFalse(testling.isReplicationEnabled());
  }

  @Test
  void testExecute() throws Exception {
    mockReplicationDisabled();
    when(cdiEvent.fireAsync(any())).thenAnswer(inv ->
        CompletableFuture.completedFuture(inv.getArgument(0)));

    testling.execute();

    verify(cdiEvent, times(1)).fireAsync(any());
    verify(bearbeiterBoundary, times(1)).getLoggedBearbeiter();
    assertEquals(WerkzeugBatchJobStatus.RUNNING, testling.getBatch().getStatus());
  }

  @Test
  void testExecute_replicationStillEnabled_throwsWithoutStartingBatch() throws Exception {
    when(indexReplicationService.isReplicationEnabled()).thenReturn(true);

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, testling::execute);

    assertTrue(exception.getMessage().contains("still enabled"));
    verify(cdiEvent, never()).fireAsync(any());
    assertEquals(WerkzeugBatchJobStatus.NOT_RUNNING, testling.getBatch().getStatus());
  }

  @Test
  void testExecute_replicationStatusUnknown_throwsWithoutStartingBatch() throws Exception {
    when(indexReplicationService.isReplicationEnabled())
        .thenThrow(new SolrServiceException("solr down"));

    assertThrows(IllegalStateException.class, testling::execute);

    verify(cdiEvent, never()).fireAsync(any());
    assertEquals(WerkzeugBatchJobStatus.NOT_RUNNING, testling.getBatch().getStatus());
  }

  @Test
  void testBatchDelegates() throws Exception {
    mockReplicationDisabled();
    Bearbeiter bearbeiter = mock(Bearbeiter.class);
    when(bearbeiter.getName()).thenReturn("Konrad");
    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);
    when(cdiEvent.fireAsync(any())).thenAnswer(inv ->
        CompletableFuture.completedFuture(inv.getArgument(0)));

    assertNull(testling.getBearbeiterName());
    assertNull(testling.getStartTime());
    assertNull(testling.getEndTime());
    assertEquals(WerkzeugBatchJobStatus.NOT_RUNNING, testling.getStatus());

    testling.execute();

    assertEquals("Konrad", testling.getBearbeiterName());
    assertNotNull(testling.getStartTime());
    assertEquals(WerkzeugBatchJobStatus.RUNNING, testling.getStatus());
  }

  @Test
  void testExecute_fireAsyncFails_setsBatchError() throws Exception {
    mockReplicationDisabled();
    when(cdiEvent.fireAsync(any())).thenAnswer(inv ->
        CompletableFuture.failedFuture(new RuntimeException("boom")));

    testling.execute();

    verify(cdiEvent, times(1)).fireAsync(any());
    verify(bearbeiterBoundary, times(1)).getLoggedBearbeiter();

    assertEquals(
        WerkzeugBatchJobStatus.ERROR,
        testling.getBatch().getStatus(),
        "Batch-Status muss ERROR sein (fireAsync fehlgeschlagen)."
    );

    assertTrue(
        Optional.ofNullable(testling.getBatch().getSuccessMessage())
            .orElse("")
            .toLowerCase()
            .contains("asynchroner observer fehlgeschlagen"),
        "Fehlermeldung muss 'Asynchroner Observer fehlgeschlagen' enthalten."
    );
  }

}