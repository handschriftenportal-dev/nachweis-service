package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class MusiknotationTest {

  @Test
  void testBuilder() {
    Musiknotation musiknotation = Musiknotation.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withText("yes")
        .build();

    assertNotNull(musiknotation);
    assertNotNull(musiknotation.getId());
    assertEquals("HSP-123", musiknotation.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, musiknotation.getReferenzTyp());
    assertEquals(AttributsTyp.MUSIKNOTATION, musiknotation.getAttributsTyp());
    assertEquals("yes", musiknotation.getText());
  }

  @Test
  void testToString() {
    String toString = Musiknotation.builder().build().toString();
    assertNotNull(toString);
    assertTrue(
        Arrays.stream(new String[]{Musiknotation.class.getSimpleName(),
                "id", "attributsTyp", "referenzId", "referenzTyp", "text"})
            .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    Musiknotation musiknotation_1 = Musiknotation.builder()
        .withReferenzId("HSP-123")
        .build();

    Musiknotation musiknotation_2 = Musiknotation.builder()
        .withReferenzId("HSP-123")
        .build();

    Musiknotation musiknotation_3 = Musiknotation.builder()
        .withReferenzId("HSP-456")
        .build();

    assertEquals(musiknotation_1, musiknotation_2);

    assertNotEquals(musiknotation_1, musiknotation_3);
  }

}
