package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.FormatWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class FormatTest {

  @Test
  void testBuilder() {

    FormatWert formatWert_1 = FormatWert.builder()
        .withReferenz(new NormdatenReferenz("id1", "smaller than octavo", "gndId1"))
        .withTyp("deduced")
        .build();

    FormatWert formatWert_2 = FormatWert.builder()
        .withReferenz(new NormdatenReferenz("id2", "long and narrow", "gndId2"))
        .withTyp("factual")
        .build();

    Format format = Format.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withFormatWerte(null)
        .withFormatWerte(List.of(formatWert_1))
        .addFormatWert(null)
        .addFormatWert(formatWert_2)
        .build();

    assertNotNull(format);
    assertNotNull(format.getId());
    assertEquals("HSP-123", format.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, format.getReferenzTyp());
    assertEquals(AttributsTyp.FORMAT, format.getAttributsTyp());
    assertEquals("smaller than octavo;long and narrow", format.getText());
    assertNotNull(format.getFormatWerte());
    assertEquals(2, format.getFormatWerte().size());
    assertTrue(format.getFormatWerte().contains(formatWert_1));
    assertTrue(format.getFormatWerte().contains(formatWert_2));

    assertNotNull(format.toString());
  }

  @Test
  void testToString() {
    String toString = Format.builder().build().toString();
    assertNotNull(toString);
    assertTrue(
        Arrays.stream(
                new String[]{
                    Format.class.getSimpleName(),
                    "id",
                    "attributsTyp",
                    "normdatenReferenz",
                    "referenzId",
                    "referenzTyp",
                    "text",
                    "formatWerte"})
            .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    Format format_1 = Format.builder()
        .withReferenzId("HSP-123")
        .build();

    Format format_2 = Format.builder()
        .withReferenzId("HSP-123")
        .build();

    Format format_3 = Format.builder()
        .withReferenzId("HSP-456")
        .build();

    assertEquals(format_1, format_2);
    assertNotEquals(format_1, format_3);
  }
}
