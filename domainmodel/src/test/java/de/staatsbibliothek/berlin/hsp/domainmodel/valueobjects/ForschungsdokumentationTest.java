package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ForschungsdokumentationTest {

  private static final String RESSOURCE = "Handschriftenforschung und ihr Weg ins Digitale";
  private static final String LINK =
      "https://blog.sbb.berlin/termin/handschriftenforschung-digital/";

  @Test
  void shouldCreateWithLink() {
    Forschungsdokumentation dokumentation = new Forschungsdokumentation(RESSOURCE, LINK);

    assertEquals(RESSOURCE, dokumentation.ressource());
    assertEquals(LINK, dokumentation.link());
    assertTrue(dokumentation.hasLink());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "\t", "\n"})
  void shouldCreateWithoutOptionalLink(String link) {
    Forschungsdokumentation dokumentation = new Forschungsdokumentation(RESSOURCE, link);

    assertTrue(Forschungsdokumentation.isValidLinkValue(link));
    assertNull(dokumentation.link());
    assertFalse(dokumentation.hasLink());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "\t", "\n"})
  void shouldRejectMissingRessource(String ressource) {
    assertThrows(
        Forschungsdokumentation.InvalidRessourceException.class,
        () -> new Forschungsdokumentation(ressource, LINK));
  }

  @Test
  void shouldNormalizeRessourceWhitespace() {
    Forschungsdokumentation dokumentation = new Forschungsdokumentation(
        "  Handschriftenforschung\n\tund   ihr Weg ins Digitale  ",
        null);

    assertEquals(RESSOURCE, dokumentation.ressource());
  }

  @Test
  void shouldTrimLink() {
    Forschungsdokumentation dokumentation = new Forschungsdokumentation(
        RESSOURCE,
        "  " + LINK + "  ");

    assertEquals(LINK, dokumentation.link());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "example.org/documentation",
      "ftp://example.org/documentation",
      "https:///documentation",
      "not a link"
  })
  void shouldRejectInvalidLink(String link) {
    assertFalse(Forschungsdokumentation.isValidLinkValue(link));
    assertThrows(
        Forschungsdokumentation.InvalidLinkException.class,
        () -> new Forschungsdokumentation(RESSOURCE, link));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "http://example.org/documentation",
      "https://example.org/documentation",
      "HTTP://example.org/documentation",
      "HTTPS://example.org/documentation"
  })
  void shouldAcceptValidLink(String link) {
    assertTrue(Forschungsdokumentation.isValidLinkValue(link));
    assertDoesNotThrow(() -> new Forschungsdokumentation(RESSOURCE, link));
  }

  @Test
  void shouldCompareNormalizedValues() {
    Forschungsdokumentation normalized = new Forschungsdokumentation(RESSOURCE, LINK);
    Forschungsdokumentation padded = new Forschungsdokumentation(
        "  Handschriftenforschung  und ihr Weg ins Digitale ",
        " " + LINK + " ");
    assertEquals(normalized, padded);
  }
}
