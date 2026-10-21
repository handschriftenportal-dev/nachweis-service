package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 19.09.23.
 * @version 1.0
 */
public class ConceptTest {

  @Test
  void testCreation() {

    Concept digitalisationtypeColor = new Concept("UUID", "https://normdatenservice", "color");

    assertNotNull(digitalisationtypeColor);

    assertNotNull(digitalisationtypeColor.getLabels());
  }
}
