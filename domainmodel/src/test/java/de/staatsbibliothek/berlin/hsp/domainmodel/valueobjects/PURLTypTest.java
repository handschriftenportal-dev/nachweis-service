package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 02.08.2022
 */

public class PURLTypTest {

  @Test
  void testGetPURLTypFromString() {
    assertEquals(PURLTyp.INTERNAL, PURLTyp.getPURLTypFromString("INTERNAL"));
    assertEquals(PURLTyp.EXTERNAL, PURLTyp.getPURLTypFromString("EXTERNAL"));
    assertNull(PURLTyp.getPURLTypFromString("Invalid"));
    assertNull(PURLTyp.getPURLTypFromString(null));
  }
}
