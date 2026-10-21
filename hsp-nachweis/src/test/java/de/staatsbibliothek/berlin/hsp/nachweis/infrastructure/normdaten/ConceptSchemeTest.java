package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 29.09.23.
 * @version 1.0
 */
public class ConceptSchemeTest {

  @Test
  void testCreation() {

    ConceptScheme conceptscheme = new ConceptScheme("UUID",
        "https://normdatenservice/conceptscheme", "completeness");

    assertNotNull(conceptscheme);

    assertEquals("UUID", conceptscheme.getId());

    assertNotNull(conceptscheme.getLabels());

    assertTrue(conceptscheme.getLabels().isEmpty());
  }

}
