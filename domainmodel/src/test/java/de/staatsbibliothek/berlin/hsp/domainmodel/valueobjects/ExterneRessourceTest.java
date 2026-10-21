package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ExterneRessourceTest {

  private static final String HSC_URL = "https://hsc.example.org/de/objekt/123";

  @ParameterizedTest
  @ValueSource(strings = {
      "https://example.org",
      "http://example.org/test",
      "https://hsc.example.org/de/objekt/123",
      "https://example.org:8443/test?a=b#fragment",
      "http://localhost:8080/test",
      "https://[::1]/test",
      "HTTPS://EXAMPLE.ORG/test",
      "  https://example.org/test  "
  })
  void shouldAcceptValidUrl(String url) {
    assertDoesNotThrow(() -> new ExterneRessource(ExterneRessourceTyp.HSC, url));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "ftp://example.org/test",
      "file:///tmp/test",
      "mailto:info@example.org",
      "javascript:alert(1)",
      "example.org/test",
      "//example.org/test",
      "http:///test",
      "https:example",
      "http://:8080/test",
      "http://%20/test",
      "http://example.org/pa th",
      "",
      "   "
  })
  void shouldRejectInvalidUrl(String url) {
    assertThrows(IllegalArgumentException.class,
        () -> new ExterneRessource(ExterneRessourceTyp.HSC, url));
  }

  @ParameterizedTest
  @ValueSource(strings = {"https://example.org", "  https://example.org/test  ", "https://[::1]/x"})
  void isValidUrlShouldAcceptTheSameUrlsAsTheConstructor(String url) {
    assertTrue(ExterneRessource.isValidUrl(url));
  }

  @ParameterizedTest
  @ValueSource(strings = {"http:/", "example.org/test", "ftp://example.org", "http:///x", "",
      "   "})
  void isValidUrlShouldRejectTheSameUrlsAsTheConstructor(String url) {
    assertFalse(ExterneRessource.isValidUrl(url));
    assertThrows(IllegalArgumentException.class,
        () -> new ExterneRessource(ExterneRessourceTyp.HSC, url));
  }

  @Test
  void isValidUrlShouldRejectNull() {
    assertFalse(ExterneRessource.isValidUrl(null));
  }

  @Test
  void shouldRejectNullUrl() {
    NullPointerException npe = assertThrows(NullPointerException.class,
        () -> new ExterneRessource(ExterneRessourceTyp.HSC, null));

    assertEquals("url is required", npe.getMessage());
  }

  @Test
  void shouldRejectNullTyp() {
    NullPointerException npe = assertThrows(NullPointerException.class,
        () -> new ExterneRessource(null, HSC_URL));

    assertEquals("typ is required", npe.getMessage());
  }

  @Test
  void shouldStripSurroundingWhitespaceFromUrl() {
    ExterneRessource externeRessource =
        new ExterneRessource(ExterneRessourceTyp.HSC, "  " + HSC_URL + "  ");

    assertEquals(HSC_URL, externeRessource.url());
  }

  @Test
  void shouldBeEqualForSameTypAndUrlIgnoringPadding() {
    ExterneRessource externeRessource = new ExterneRessource(ExterneRessourceTyp.HSC, HSC_URL);
    ExterneRessource padded = new ExterneRessource(ExterneRessourceTyp.HSC, " " + HSC_URL + " ");

    assertEquals(externeRessource, padded);
    assertEquals(externeRessource.hashCode(), padded.hashCode());
  }

  @Test
  void shouldNotBeEqualForDifferentTyp() {
    assertNotEquals(
        new ExterneRessource(ExterneRessourceTyp.HSC, HSC_URL),
        new ExterneRessource(ExterneRessourceTyp.ECODICES, HSC_URL));
  }
}
