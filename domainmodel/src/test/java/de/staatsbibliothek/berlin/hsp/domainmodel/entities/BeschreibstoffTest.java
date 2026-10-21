package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class BeschreibstoffTest {

  @Test
  void testBuilder() {

    Beschreibstoff beschreibstoff = Beschreibstoff.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withText("Pergament, Papyrus")
        .addReferenz(new NormdatenReferenzBuilder()
            .withId("1234")
            .withName("Pergament")
            .withGndID("4001234-5")
            .withTypeName("Concept")
            .withUri("http://d-nb.info/gnd/4001234-5")
            .build())
        .build();

    assertNotNull(beschreibstoff);
    assertNotNull(beschreibstoff.getId());
    assertEquals("HSP-123", beschreibstoff.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, beschreibstoff.getReferenzTyp());
    assertEquals(AttributsTyp.BESCHREIBSTOFF, beschreibstoff.getAttributsTyp());
    assertEquals("Pergament, Papyrus", beschreibstoff.getText());
    assertNotNull(beschreibstoff.getNormdatenReferenzen());
    assertEquals(1, beschreibstoff.getNormdatenReferenzen().size());
    var normdatenReferenz = beschreibstoff.getNormdatenReferenzen().getFirst();
    assertEquals("1234", normdatenReferenz.getId());
    assertEquals("Pergament", normdatenReferenz.getName());
    assertEquals("4001234-5", normdatenReferenz.getGndID());
    assertEquals("Concept", normdatenReferenz.getTypeName());
    assertEquals("http://d-nb.info/gnd/4001234-5", normdatenReferenz.getUri());
  }

  @Test
  void testToString() {
    String toString = Beschreibstoff.builder().build().toString();
    assertNotNull(toString);
    assertTrue(
        Arrays.stream(
                new String[]{Beschreibstoff.class.getSimpleName(),
                    "id", "attributsTyp", "referenzId", "referenzTyp", "text",
                    "normdatenReferenzen"})
            .allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    Beschreibstoff beschreibstoff_1 = Beschreibstoff.builder()
        .withReferenzId("HSP-123")
        .build();

    Beschreibstoff beschreibstoff_2 = Beschreibstoff.builder()
        .withReferenzId("HSP-123")
        .build();

    Beschreibstoff beschreibstoff_3 = Beschreibstoff.builder()
        .withReferenzId("HSP-456")
        .build();

    assertEquals(beschreibstoff_1, beschreibstoff_2);

    assertNotEquals(beschreibstoff_1, beschreibstoff_3);
  }
}
