package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class UmfangTest {

  @Test
  void testBuilder() {

    Umfang umfang = Umfang.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withText("I + 394 Bl.")
        .withBlattzahl("394")
        .build();

    assertNotNull(umfang);
    assertNotNull(umfang.getId());
    assertEquals("HSP-123", umfang.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, umfang.getReferenzTyp());
    assertEquals(AttributsTyp.UMFANG, umfang.getAttributsTyp());
    assertEquals("I + 394 Bl.", umfang.getText());
    assertEquals("394", umfang.getBlattzahl());
  }

  @Test
  void testToString() {
    String toString = Umfang.builder().build().toString();
    assertNotNull(toString);
    assertTrue(
        Arrays.stream(
                new String[]{Umfang.class.getSimpleName(),
                    "id", "attributsTyp", "referenzId", "referenzTyp", "text", "blattzahl"})
            .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    Umfang umfang_1 = Umfang.builder()
        .withReferenzId("HSP-123")
        .build();

    Umfang umfang_2 = Umfang.builder()
        .withReferenzId("HSP-123")
        .build();

    Umfang umfang_3 = Umfang.builder()
        .withReferenzId("HSP-456")
        .build();

    assertEquals(umfang_1, umfang_2);

    assertNotEquals(umfang_1, umfang_3);
  }

}
