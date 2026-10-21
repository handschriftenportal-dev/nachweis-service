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

class HandschriftentypTest {

  @Test
  void testBuilder() {
    Handschriftentyp handschriftentyp = Handschriftentyp.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .build();

    assertNotNull(handschriftentyp);
    assertNotNull(handschriftentyp.getId());
    assertEquals("HSP-123", handschriftentyp.getReferenzId());
    assertEquals(AttributsReferenzTyp.BESCHREIBUNG, handschriftentyp.getReferenzTyp());
    assertEquals(AttributsTyp.HANDSCHRIFTENTYP, handschriftentyp.getAttributsTyp());
    assertEquals("", handschriftentyp.getText());
  }

  @Test
  void testToString() {
    String toString = Handschriftentyp.builder().build().toString();
    assertNotNull(toString);
    assertTrue(Arrays.stream(new String[]{
        Handschriftentyp.class.getSimpleName(),
        "id", "attributsTyp", "referenzId", "referenzTyp", "text", "normdatenReferenzen"
    }).allMatch(toString::contains));
  }

  @Test
  void testEquals() {
    Handschriftentyp handschriftentyp_1 = Handschriftentyp.builder()
        .withReferenzId("HSP-123")
        .build();

    Handschriftentyp handschriftentyp_2 = Handschriftentyp.builder()
        .withReferenzId("HSP-123")
        .build();

    Handschriftentyp handschriftentyp_3 = Handschriftentyp.builder()
        .withReferenzId("HSP-456")
        .build();

    assertEquals(handschriftentyp_1, handschriftentyp_2);

    assertNotEquals(handschriftentyp_1, handschriftentyp_3);
  }

  @Test
  void testBuilder_withReferenzenAndText() {
    Handschriftentyp h = Handschriftentyp.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzen(List.of(
            new NormdatenReferenz.NormdatenReferenzBuilder()
                .withId("NORM-8edc413b-0b70-3366-b78c-fdc091ab3ab2")
                .withUri("https://normdaten.staatsbibliothek-berlin.de/hsp/vocabulary/FORM-X709")
                .withName("Fragment")
                .build(),
            new NormdatenReferenz.NormdatenReferenzBuilder()
                .withId("NORM-fcdc9965-677e-3c69-9a70-0c266893e376")
                .withUri("https://normdaten.staatsbibliothek-berlin.de/hsp/vocabulary/FORM-A890")
                .build()
        ))
        .build();

    assertNotNull(h);
    assertEquals("Fragment", h.getText());
    assertEquals(2, h.getNormdatenReferenzen().size());
    assertEquals("NORM-8edc413b-0b70-3366-b78c-fdc091ab3ab2",
        h.getNormdatenReferenzen().getFirst().getId());
    assertEquals("https://normdaten.staatsbibliothek-berlin.de/hsp/vocabulary/FORM-X709",
        h.getNormdatenReferenzen().getFirst().getUri());
  }
}
