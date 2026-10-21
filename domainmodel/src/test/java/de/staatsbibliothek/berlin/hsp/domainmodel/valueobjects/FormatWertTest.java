package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class FormatWertTest {


  @Test
  void testBuilder() {
    FormatWert formatWert = FormatWert.builder()
        .withReferenz(new NormdatenReferenz("id1", "larger than folio", "gndId1"))
        .withTyp("computed")
        .build();

    assertNotNull(formatWert);
    assertNotNull(formatWert.getId());
    assertNotNull(formatWert.getNormdatenReferenz());
    assertEquals("id1", formatWert.getNormdatenReferenz().getId());
    assertEquals("gndId1", formatWert.getNormdatenReferenz().getGndID());
    assertEquals("larger than folio", formatWert.getNormdatenReferenz().getName());
    assertEquals("larger than folio", formatWert.getText());
    assertEquals("computed", formatWert.getTyp());
  }

  @Test
  void testToString() {
    String toString = FormatWert.builder().build().toString();
    assertNotNull(toString);
    assertTrue(Arrays.stream(new String[]{FormatWert.class.getSimpleName(), "id", "text", "typ"})
        .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    assertNotEquals(FormatWert.builder().build(), FormatWert.builder().build());
  }

}
