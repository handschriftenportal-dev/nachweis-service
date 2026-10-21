package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import jakarta.enterprise.event.Event;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 27.06.23.
 * @version 1.0
 */
public class WerkzeugBatchIndexAllKODsTest {

  @Test
  void testExecute() {
    Event<WerkzeugBatchJob> cdiEvent = mock(Event.class);
    BearbeiterBoundary bearbeiterBoundary = mock(BearbeiterBoundary.class);
    WerkzeugBatchIndexAllKODs batchIndexAllKODs =
        new WerkzeugBatchIndexAllKODs(cdiEvent, bearbeiterBoundary);

    when(cdiEvent.fireAsync(any())).thenAnswer(inv ->
        CompletableFuture.completedFuture(inv.getArgument(0)));

    batchIndexAllKODs.execute();

    assertNotNull(batchIndexAllKODs);
    verify(cdiEvent, times(1)).fireAsync(any());
    verify(bearbeiterBoundary, times(1)).getLoggedBearbeiter();
    assertEquals(WerkzeugBatchJobStatus.RUNNING, batchIndexAllKODs.getBatch().getStatus());
  }

  @Test
  void testExecute_fireAsyncFails_setsBatchError() {
    Event<WerkzeugBatchJob> cdiEvent = mock(Event.class);
    BearbeiterBoundary bearbeiterBoundary = mock(BearbeiterBoundary.class);

    when(cdiEvent.fireAsync(any())).thenAnswer(inv ->
        CompletableFuture.failedFuture(new RuntimeException("boom")));

    WerkzeugBatchIndexAllKODs batchIndexAllKODs =
        new WerkzeugBatchIndexAllKODs(cdiEvent, bearbeiterBoundary);

    batchIndexAllKODs.execute();

    verify(cdiEvent, times(1)).fireAsync(any());
    verify(bearbeiterBoundary, times(1)).getLoggedBearbeiter();

    assertEquals(
        WerkzeugBatchJobStatus.ERROR,
        batchIndexAllKODs.getBatch().getStatus(),
        "Batch-Status muss ERROR sein (fireAsync fehlgeschlagen)."
    );

    assertTrue(
        Optional.ofNullable(batchIndexAllKODs.getBatch().getSuccessMessage())
            .orElse("")
            .toLowerCase()
            .contains("asynchroner observer fehlgeschlagen"),
        "Fehlermeldung muss 'Asynchroner Observer fehlgeschlagen' enthalten."
    );
  }
}
