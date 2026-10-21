package de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchJob;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchJobStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class IndexJobEventHandlerTest {

  IndexJobBoundary mockIndexJobBoundary = Mockito.mock(IndexJobService.class);

  @Test
  void onEvent() {
    IndexJobEventHandler eventHandler = new IndexJobEventHandler(mockIndexJobBoundary);
    IndexJob indexJob = new IndexJob("id", "SYSTEM_USERNAME", IndexJobDocumentType.KOD);
    eventHandler.onEvent(indexJob);

    verify(mockIndexJobBoundary, times(1)).startIndexingDocuments(Mockito.any());
  }

  @Test
  void onWerkzeugIndexPraesentation() {
    IndexJobEventHandler eventHandler = new IndexJobEventHandler(mockIndexJobBoundary);
    WerkzeugBatchJob batch = new WerkzeugBatchJob("Indexing all documents in the presentation");

    eventHandler.onWerkzeugIndexPraesentation(batch);

    ArgumentCaptor<IndexJob> captor = ArgumentCaptor.forClass(IndexJob.class);
    verify(mockIndexJobBoundary, times(1)).startIndexingDocuments(captor.capture());
    assertEquals(IndexJobDocumentType.ALL, captor.getValue().getIndexJobDocumentType());
    assertEquals(BearbeiterBoundary.SYSTEM_USERNAME, captor.getValue().getActorName());
    assertEquals(WerkzeugBatchJobStatus.NOT_RUNNING, batch.getStatus());
    assertNotNull(batch.getEndTime());
  }

  @Test
  void onWerkzeugIndexPraesentation_indexingFails_setsBatchError() {
    Mockito.doThrow(new RuntimeException("boom"))
        .when(mockIndexJobBoundary).startIndexingDocuments(Mockito.any());
    IndexJobEventHandler eventHandler = new IndexJobEventHandler(mockIndexJobBoundary);
    WerkzeugBatchJob batch = new WerkzeugBatchJob("Indexing all documents in the presentation");

    eventHandler.onWerkzeugIndexPraesentation(batch);

    assertEquals(WerkzeugBatchJobStatus.ERROR, batch.getStatus());
    assertEquals("boom", batch.getSuccessMessage());
  }
}