package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class GrundspracheTest {

  @Test
  void testBuilder() {

    Grundsprache grundsprache = Grundsprache.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withText("Alt- und Mittelhochdeutsch")
        .withReferenzen(
            List.of(new NormdatenReferenz("de", "deutsch", "GND-ID")))
        .build();

    assertNotNull(grundsprache);
    assertNotNull(grundsprache.getId());
    assertEquals("HSP-123", grundsprache.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG,
        grundsprache.getReferenzTyp());
    assertEquals(AttributsTyp.GRUNDSPRACHE, grundsprache.getAttributsTyp());
    assertEquals("Alt- und Mittelhochdeutsch", grundsprache.getText());
    assertNotNull(grundsprache.getNormdatenReferenzen());
    assertEquals(1, grundsprache.getNormdatenReferenzen().size());
    assertEquals("de",
        grundsprache.getNormdatenReferenzen().getFirst().getId());
  }

  @Test
  void testToString() {
    String toString = Grundsprache.builder().build().toString();
    assertNotNull(toString);
    assertTrue(
        Arrays.stream(new String[]{Grundsprache.class.getSimpleName(),
                "id", "attributsTyp", "referenzId", "referenzTyp", "text",
                "normdatenIds"})
            .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    Grundsprache grundsprache_1 = Grundsprache.builder()
        .withReferenzId("HSP-123")
        .build();

    Grundsprache grundsprache_2 = Grundsprache.builder()
        .withReferenzId("HSP-123")
        .build();

    Grundsprache grundsprache_3 = Grundsprache.builder()
        .withReferenzId("HSP-456")
        .build();

    assertEquals(grundsprache_1, grundsprache_2);

    assertNotEquals(grundsprache_1, grundsprache_3);
  }

}
