package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.detail;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 13.08.2020.
 * @version 1.0
 */
public class KoerperschaftsDetailControllerTest {

  @Test
  void testFindIsil() {
    KoerperschaftsDetailController controller = new KoerperschaftsDetailController(null);
    Identifikator isil = new Identifikator("DE-1", "", "Isil");
    NormdatenReferenz model = new NormdatenReferenzBuilder()
        .withId("1")
        .withName("Staabi")
        .withGndID("12122-X")
        .addIdentifikator(new Identifikator("DE-1", "", "Isil"))
        .build();

    controller.setModel(model);

    assertEquals(isil, controller.findIsil());
  }
}
