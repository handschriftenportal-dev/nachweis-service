package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AbmessungWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class AbmessungTest {

  @Test
  void testBuilder() {
    AbmessungWert abmessungWert_1 = AbmessungWert.builder()
        .withText("23,5 × 16,5 x 2,5")
        .withHoehe("23,5")
        .withBreite("16,5")
        .withTiefe("2,5")
        .withTyp("factual")
        .build();

    AbmessungWert abmessungWert_2 = AbmessungWert.builder()
        .withText("25 × 15 x 5")
        .withHoehe("25")
        .withBreite("15")
        .withTiefe("5")
        .withTyp("deduced")
        .build();

    Abmessung abmessung = Abmessung.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withAbmessungWerte(null)
        .withAbmessungWerte(List.of(abmessungWert_1))
        .addAbmessungWert(null)
        .addAbmessungWert(abmessungWert_2)
        .withBearbeiter(null)
        .withAttributsReferenzStatusTyp(AttributsReferenzStatusTyp.AUTOMATISCH)
        .withModificationDatum(LocalDateTime.of(2002, 8, 3, 10, 10))
        .withPublikationDatum(LocalDateTime.of(2004, 9, 4, 12, 12))
        .build();

    assertNotNull(abmessung);
    assertNotNull(abmessung.getId());
    assertEquals("HSP-123", abmessung.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, abmessung.getReferenzTyp());
    assertEquals(AttributsTyp.ABMESSUNG, abmessung.getAttributsTyp());
    assertEquals("23,5 × 16,5 x 2,5; 25 × 15 x 5", abmessung.getText());
    assertNotNull(abmessung.getAbmessungWerte());
    assertEquals(2, abmessung.getAbmessungWerte().size());
    assertTrue(abmessung.getAbmessungWerte().contains(abmessungWert_1));
    assertTrue(abmessung.getAbmessungWerte().contains(abmessungWert_2));
    assertEquals(AttributsReferenzStatusTyp.AUTOMATISCH, abmessung.getAttributsReferenzStatusTyp());
    assertEquals(LocalDateTime.of(2002, 8, 3, 10, 10), abmessung.getModificationDatum());
    assertNotNull(abmessung.toString());
  }

  @Test
  void testToString() {
    String toString = Abmessung.builder().build().toString();
    assertNotNull(toString);
    assertTrue(
        Arrays.stream(
                new String[]{
                    Abmessung.class.getSimpleName(), "id", "attributsTyp", "referenzId", "referenzTyp",
                    "text", "abmessungWerte"})
            .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    Abmessung abmessung_1 = Abmessung.builder()
        .withReferenzId("HSP-123")
        .build();

    Abmessung abmessung_2 = Abmessung.builder()
        .withReferenzId("HSP-123")
        .build();

    Abmessung abmessung_3 = Abmessung.builder()
        .withReferenzId("HSP-456")
        .build();

    assertEquals(abmessung_1, abmessung_2);
    assertNotEquals(abmessung_1, abmessung_3);
  }
}
