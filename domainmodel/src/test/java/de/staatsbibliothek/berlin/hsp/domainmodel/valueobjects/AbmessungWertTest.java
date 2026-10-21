package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class AbmessungWertTest {

  @Test
  void testBuilder() {
    AbmessungWert abmessungWert = AbmessungWert.builder()
        .withText("23,5 × 16,5 x 2,5")
        .withHoehe("23,5")
        .withBreite("16,5")
        .withTiefe("2,5")
        .withTyp("factual")
        .build();

    assertNotNull(abmessungWert);
    assertNotNull(abmessungWert.getId());
    assertEquals("23,5 × 16,5 x 2,5", abmessungWert.getText());
    assertEquals("23,5", abmessungWert.getHoehe());
    assertEquals("16,5", abmessungWert.getBreite());
    assertEquals("2,5", abmessungWert.getTiefe());
    assertEquals("factual", abmessungWert.getTyp());
  }

  @Test
  void testToString() {
    String toString = AbmessungWert.builder().build().toString();
    assertNotNull(toString);
    assertTrue(Arrays.stream(
            new String[]{AbmessungWert.class.getSimpleName(), "id", "text", "hoehe", "breite", "tiefe", "typ"})
        .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    assertNotEquals(AbmessungWert.builder().build(), AbmessungWert.builder().build());
  }
}
