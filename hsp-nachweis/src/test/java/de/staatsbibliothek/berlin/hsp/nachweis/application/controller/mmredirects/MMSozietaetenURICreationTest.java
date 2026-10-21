package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 18.10.23.
 * @version 1.0
 */
public class MMSozietaetenURICreationTest {

  @Test
  void testextractSozietaetenNumberFromPath() {

    MMSozietaetenURICreation mmSozietaetenURICreation = new MMSozietaetenURICreation();

    assertEquals("soz30002370",
        mmSozietaetenURICreation.extractSozietaetenNumberFromPath("dokumente/html/soz30002370"));

  }

  @Test
  void testCreate() {
    MMSozietaetenURICreation mmSozietaetenURICreation = new MMSozietaetenURICreation();

    assertEquals("https://handschriftenportal.de/search?q=%22soz_30002370%22",
        mmSozietaetenURICreation.create("dokumente/html/soz30002370").toString());
  }
}
