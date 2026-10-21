package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class BuchschmuckTest {

  @Test
  void testBuilder() {
    Buchschmuck buchschmuck = Buchschmuck.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withText("yes")
        .build();

    assertNotNull(buchschmuck);
    assertNotNull(buchschmuck.getId());
    assertEquals("HSP-123", buchschmuck.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, buchschmuck.getReferenzTyp());
    assertEquals(AttributsTyp.BUCHSCHMUCK, buchschmuck.getAttributsTyp());
    assertEquals("yes", buchschmuck.getText());
  }

  @Test
  void testToString() {
    String toString = Buchschmuck.builder().build().toString();
    assertNotNull(toString);
    assertTrue(
        Arrays.stream(new String[]{Buchschmuck.class.getSimpleName(),
                "id", "attributsTyp", "referenzId", "referenzTyp", "text"})
            .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    Buchschmuck buchschmuck_1 = Buchschmuck.builder()
        .withReferenzId("HSP-123")
        .build();

    Buchschmuck buchschmuck_2 = Buchschmuck.builder()
        .withReferenzId("HSP-123")
        .build();

    Buchschmuck buchschmuck_3 = Buchschmuck.builder()
        .withReferenzId("HSP-456")
        .build();

    assertEquals(buchschmuck_1, buchschmuck_2);

    assertNotEquals(buchschmuck_1, buchschmuck_3);
  }

}
