package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class EntstehungszeitWertTest {

  @Test
  void testBuilder() {
    EntstehungszeitWert entstehungszeitWert = EntstehungszeitWert.builder()
        .withText("ca. 1220 (Fasz. I)")
        .withNichtVor("1210")
        .withNichtNach("1230")
        .withTyp("datable")
        .build();

    assertNotNull(entstehungszeitWert);
    assertNotNull(entstehungszeitWert.getId());
    assertEquals("ca. 1220 (Fasz. I)", entstehungszeitWert.getText());
    assertEquals("1210", entstehungszeitWert.getNichtVor());
    assertEquals("1230", entstehungszeitWert.getNichtNach());
    assertEquals("datable", entstehungszeitWert.getTyp());
  }

  @Test
  void testToString() {
    String toString = EntstehungszeitWert.builder().build().toString();
    assertNotNull(toString);
    assertTrue(Arrays.stream(new String[]{
            EntstehungszeitWert.class.getSimpleName(), "id", "text", "nichtVor", "nichtNach", "typ"})
        .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    assertNotEquals(EntstehungszeitWert.builder().build(), EntstehungszeitWert.builder().build());
  }

}
