package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class TitelTest {

  @Test
  void testBuilder() {
    Titel titel = Titel.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withText("Titel der Beschreibung")
        .build();

    assertNotNull(titel);
    assertNotNull(titel.getId());
    assertEquals("HSP-123", titel.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, titel.getReferenzTyp());
    assertEquals(AttributsTyp.TITEL, titel.getAttributsTyp());
    assertEquals("Titel der Beschreibung", titel.getText());
  }

  @Test
  void testToString() {
    String toString = Titel.builder().build().toString();
    assertNotNull(toString);
    assertTrue(
        Arrays.stream(new String[]{
                Titel.class.getSimpleName(), "id", "attributsTyp", "referenzId", "referenzTyp", "text"})
            .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    Titel titel_1 = Titel.builder()
        .withReferenzId("HSP-123")
        .build();

    Titel titel_2 = Titel.builder()
        .withReferenzId("HSP-123")
        .build();

    Titel titel_3 = Titel.builder()
        .withReferenzId("HSP-456")
        .build();

    assertEquals(titel_1, titel_2);

    assertNotEquals(titel_1, titel_3);
  }

}
