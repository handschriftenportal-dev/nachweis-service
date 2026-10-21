package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class StatusTest {

  @Test
  void testBuilder() {
    Status status = Status.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withText("displaced")
        .build();

    assertNotNull(status);
    assertNotNull(status.getId());
    assertEquals("HSP-123", status.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, status.getReferenzTyp());
    assertEquals(AttributsTyp.STATUS, status.getAttributsTyp());
    assertEquals("displaced", status.getText());
  }

  @Test
  void testToString() {
    String toString = Status.builder().build().toString();
    assertNotNull(toString);
    assertTrue(
        Arrays.stream(new String[]{
                Status.class.getSimpleName(), "id", "attributsTyp", "referenzId", "referenzTyp", "text"})
            .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    Status status_1 = Status.builder()
        .withReferenzId("HSP-123")
        .build();

    Status status_2 = Status.builder()
        .withReferenzId("HSP-123")
        .build();

    Status status_3 = Status.builder()
        .withReferenzId("HSP-456")
        .build();

    assertEquals(status_1, status_2);

    assertNotEquals(status_1, status_3);
  }

}
