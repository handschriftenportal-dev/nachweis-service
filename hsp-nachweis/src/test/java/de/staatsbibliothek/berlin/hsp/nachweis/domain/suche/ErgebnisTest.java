package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 25.03.24.
 * @version 1.0
 */
public class ErgebnisTest {

  @Test
  void testErgebnis() {
    Ergebnis ergebnis = new Ergebnis();

    assertNotNull(ergebnis);
    assertNotNull(ergebnis.getBearbeiter());
    assertNotNull(ergebnis.getAutoren());
    assertNotNull(ergebnis.getInstitutionen());
    assertNotNull(ergebnis.getEintragListe());
  }
}
