package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de on 25.01.2023.
 * @version 1.0
 */
class PraesentationsControllerTest {

  private String praesentationbaseurl = "https://handschriftenportal.de";

  @Test
  void creation() {

    PraesentationsController controller = new PraesentationsController(praesentationbaseurl);

    assertNotNull(controller);

    assertEquals(praesentationbaseurl, controller.getPraesentationBaseUrl());
    assertEquals(PraesentationsController.ERFASSUNGSHILFE_URL,controller.getErfassungsGuideUrl());
  }

}
