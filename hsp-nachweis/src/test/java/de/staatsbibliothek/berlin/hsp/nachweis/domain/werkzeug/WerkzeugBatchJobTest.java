package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 27.06.23.
 * @version 1.0
 */
public class WerkzeugBatchJobTest {

  @Test
  void testCreation() {
    WerkzeugBatchJob werkzeugBatchJob = new WerkzeugBatchJob("Erster Job");

    assertNotNull(werkzeugBatchJob);
    assertEquals(WerkzeugBatchJobStatus.NOT_RUNNING, werkzeugBatchJob.getStatus());
    assertEquals("", werkzeugBatchJob.getSuccessMessage());
    assertEquals("Erster Job", werkzeugBatchJob.getName());
  }

  @Test
  void testStart() {
    WerkzeugBatchJob werkzeugBatchJob = new WerkzeugBatchJob("Erster Job");
    Bearbeiter bearbeiter = Bearbeiter.newBuilder().build();
    werkzeugBatchJob.start(bearbeiter);

    assertEquals(WerkzeugBatchJobStatus.RUNNING, werkzeugBatchJob.getStatus());
    assertEquals(bearbeiter, werkzeugBatchJob.getBearbeiter());
  }

  @Test
  void testEnd() {
    WerkzeugBatchJob werkzeugBatchJob = new WerkzeugBatchJob("Erster Job");
    werkzeugBatchJob.end("Success");

    assertEquals(WerkzeugBatchJobStatus.NOT_RUNNING, werkzeugBatchJob.getStatus());
    assertEquals("Success", werkzeugBatchJob.getSuccessMessage());
  }

  @Test
  void testFail() {
    WerkzeugBatchJob werkzeugBatchJob = new WerkzeugBatchJob("Erster Job");
    werkzeugBatchJob.fail("Error");

    assertEquals(WerkzeugBatchJobStatus.ERROR, werkzeugBatchJob.getStatus());
    assertEquals("Error", werkzeugBatchJob.getSuccessMessage());
  }
}
