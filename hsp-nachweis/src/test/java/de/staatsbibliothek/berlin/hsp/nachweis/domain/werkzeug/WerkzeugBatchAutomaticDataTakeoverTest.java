package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportBoundary;
import jakarta.faces.event.ValueChangeEvent;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 27.06.23.
 * @version 1.0
 */
public class WerkzeugBatchAutomaticDataTakeoverTest {

  @Test
  void testExecute() {
    ImportBoundary importBoundary = mock(ImportBoundary.class);
    BearbeiterBoundary bearbeiterBoundary = mock(BearbeiterBoundary.class);
    WerkzeugBatchAutomaticDataTakeover batchAutomaticDataTakeover = new WerkzeugBatchAutomaticDataTakeover(importBoundary,bearbeiterBoundary);
    ValueChangeEvent valueChangeEvent = mock(ValueChangeEvent.class);
    when(valueChangeEvent.getNewValue()).thenReturn("true");

    batchAutomaticDataTakeover.execute(valueChangeEvent);

    assertNotNull(batchAutomaticDataTakeover);
    verify(bearbeiterBoundary, times(1)).getLoggedBearbeiter();
    assertEquals(WerkzeugBatchJobStatus.NOT_RUNNING,batchAutomaticDataTakeover.getBatch().getStatus());
  }

}
