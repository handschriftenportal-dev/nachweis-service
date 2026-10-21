package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import jakarta.enterprise.event.Event;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 27.06.23.
 * @version 1.0
 */
public class WerkzeugBatchIndexAllBeschreibungenTest {

  @Test
  void testExecute() {
    Event<WerkzeugBatchJob> cdiEvent = mock(Event.class);
    BearbeiterBoundary bearbeiterBoundary = mock(BearbeiterBoundary.class);
    WerkzeugBatchIndexAllBeschreibungen batchIndexAllBeschreibungen =
        new WerkzeugBatchIndexAllBeschreibungen(cdiEvent, bearbeiterBoundary);

    batchIndexAllBeschreibungen.execute();

    assertNotNull(batchIndexAllBeschreibungen);
    verify(cdiEvent, times(1)).fireAsync(any());
    verify(bearbeiterBoundary, times(1)).getLoggedBearbeiter();
    assertEquals(
        WerkzeugBatchJobStatus.RUNNING, batchIndexAllBeschreibungen.getBatch().getStatus());
  }
}
