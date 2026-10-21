package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 11.04.24.
 * @version 1.0
 */
public class WerkzeugBatchUpdateNormdatenReferenzTest {

  private final BearbeiterBoundary bearbeiterBoundary = Mockito.mock(BearbeiterBoundary.class);

  private final NormdatenReferenzBoundary normdatenReferenzBoundary = Mockito.mock(
      NormdatenReferenzService.class);

  @Test
  void testExecution() {

    WerkzeugBatchUpdateNormdatenReferenz werkzeugBatchUpdateNormdatenReferenz = new WerkzeugBatchUpdateNormdatenReferenz(
        bearbeiterBoundary, normdatenReferenzBoundary);

    werkzeugBatchUpdateNormdatenReferenz.execute();

    assertSame(werkzeugBatchUpdateNormdatenReferenz.getBatch().getStatus(),
        WerkzeugBatchJobStatus.NOT_RUNNING);

    verify(normdatenReferenzBoundary, Mockito.times(1)).updateAllNormdatenReferenzen();
  }
}
