package de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsView.BestandElementTyps;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.BestandInstitutionElementDTO;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 20.11.23.
 * @version 1.0
 */
class BestandInstitutionElementTest {

  @Test
  void testCreation() {
    BestandInstitutionElement institutionElement = new BestandInstitutionElement("SBB", "Berlin",
        10, 12,
        BestandElementTyps.KULTUROBJEKTE);

    assertNotNull(institutionElement);

    assertEquals("SBB", institutionElement.getBesitzendeEinrichtung());
    assertEquals("Berlin", institutionElement.getAufbewahrungsOrt());
    assertEquals(2, institutionElement.getBestandDifferenz());
    assertEquals(10, institutionElement.getBestandPraesentation());
    assertEquals(12, institutionElement.getBestandNachweis());

  }

  @Test
  void testCreationWithDTO() {
    BestandInstitutionElementDTO bestandInstitutionElementDTO = new BestandInstitutionElementDTO(
        "SBB", "12", "Berlin", "23", 10);

    BestandInstitutionElement institutionElement = new BestandInstitutionElement(
        bestandInstitutionElementDTO,
        BestandElementTyps.KULTUROBJEKTE);

    assertNotNull(bestandInstitutionElementDTO);

    assertEquals("SBB", institutionElement.getBesitzendeEinrichtung());
    assertEquals("Berlin", institutionElement.getAufbewahrungsOrt());
    assertEquals(10, institutionElement.getBestandDifferenz());

  }
}
