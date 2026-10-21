package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 08.10.2020.
 * @version 1.0
 */
public class DigitalisatErfassenModelTest {

  @Test
  void testConstruct() {

    DigitalisatErfassenModel model = new DigitalisatErfassenModel();

    model.construct();

    assertNotNull(model.getDigitalisatViewModel());

    assertEquals(0, model.getDigitalisatSchemeTypen().size());

  }
}
