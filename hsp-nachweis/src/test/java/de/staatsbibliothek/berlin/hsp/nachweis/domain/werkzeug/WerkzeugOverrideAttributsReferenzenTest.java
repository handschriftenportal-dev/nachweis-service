package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.konfiguration.KonfigurationBoundary;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 27.03.25.
 * @version 1.0
 */
public class WerkzeugOverrideAttributsReferenzenTest {

  KonfigurationBoundary konfigurationBoundary = Mockito.mock(KonfigurationBoundary.class);
  BearbeiterBoundary bearbeiterBoundary = Mockito.mock(BearbeiterBoundary.class);
  WerkzeugOverrideAttributsReferenzen testling = new WerkzeugOverrideAttributsReferenzen(
      konfigurationBoundary, bearbeiterBoundary);

  @Test
  void testCreation() {
    assertNotNull(testling);
  }

  @Test
  void testSetAttributsreferenzenOverride() {
    testling.setAttributsreferenzenOverride(true);

    Mockito.verify(konfigurationBoundary, Mockito.times(1)).setWert(
        WerkzeugOverrideAttributsReferenzen.ATTRIBUTSREFERENZEN_OVERRIDE,
        "true");
    Mockito.verify(bearbeiterBoundary, Mockito.times(1)).getLoggedBearbeiter();
    assertEquals(WerkzeugBatchJobStatus.NOT_RUNNING, testling.getBatch().getStatus());
    assertNotNull(testling.getBatch().getEndTime());
  }

  @Test
  void testIsAttributsreferenzenOverride() {
    Mockito.when(konfigurationBoundary.getWert(
            WerkzeugOverrideAttributsReferenzen.ATTRIBUTSREFERENZEN_OVERRIDE))
        .thenReturn(Optional.empty());
    assertFalse(testling.isAttributsreferenzenOverride());

    Mockito.when(konfigurationBoundary.getWert(
            WerkzeugOverrideAttributsReferenzen.ATTRIBUTSREFERENZEN_OVERRIDE))
        .thenReturn(Optional.of("true"));
    assertTrue(testling.isAttributsreferenzenOverride());

    Mockito.when(konfigurationBoundary.getWert(
            WerkzeugOverrideAttributsReferenzen.ATTRIBUTSREFERENZEN_OVERRIDE))
        .thenReturn(Optional.of("false"));
    assertFalse(testling.isAttributsreferenzenOverride());
  }
}