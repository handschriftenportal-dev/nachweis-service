package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;
import org.junit.jupiter.api.Test;

public class LizenzTest {

  @Test
  public void testLizenzBuilder() {
    String uri_1 = "https://some.url/1";
    String uri_2 = "https://some.url/2";
    String uri_3 = "https://some.url/3";
    String text = "Beschreibungstext";

    Lizenz lizenz = new Lizenz.LizenzBuilder()
        .withUris(new LinkedHashSet<>(Arrays.asList(uri_1, uri_2)))
        .addUri(uri_3)
        .withBeschreibungsText(text)
        .build();

    assertNotNull(lizenz);
    assertNotNull(lizenz.getUris());
    assertEquals(3, lizenz.getUris().size());
    assertTrue(lizenz.getUris().stream().anyMatch(u -> uri_1.equals(u)));
    assertTrue(lizenz.getUris().stream().anyMatch(u -> uri_2.equals(u)));
    assertTrue(lizenz.getUris().stream().anyMatch(u -> uri_3.equals(u)));
    assertEquals(text, lizenz.getBeschreibungsText());
  }

  @Test
  public void testToString() {
    Lizenz lizenz = new Lizenz.LizenzBuilder()
        .withUris(null)
        .withBeschreibungsText("Beschreibungstext")
        .build();

    assertEquals("Lizenz{id='" + lizenz.getId() + "', uris=null, beschreibungsText='Beschreibungstext'}",
        lizenz.toString());
  }
}
