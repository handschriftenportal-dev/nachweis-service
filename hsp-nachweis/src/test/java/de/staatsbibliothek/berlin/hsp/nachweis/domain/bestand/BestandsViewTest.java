package de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.04.23.
 * @version 1.0
 */
public class BestandsViewTest {

  @Test
  void testCreation() {

    BestandsView bestandsView = new BestandsView();

    assertNotNull(bestandsView);
    assertNotNull(bestandsView.getBestandsElemente());
    assertEquals(0, bestandsView.getBestandsElemente().size());
    assertEquals(0, bestandsView.getBestandInstitutionElementeForKODs().size());
    assertEquals(0, bestandsView.getBestandInstitutionElementeForBeschreibungen().size());
    assertEquals(0, bestandsView.getBestandInstitutionElementeForDigitalisate().size());
  }

  @Test
  void testCreationForError() {
    BestandsView bestandsView = new BestandsView("Error during Creating View");
    assertNotNull(bestandsView);
    assertFalse(bestandsView.getFehlerMeldung().isEmpty());
  }
}
