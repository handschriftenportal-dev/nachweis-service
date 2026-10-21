package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HttpUrlValidatorTest {

  @ParameterizedTest
  @ValueSource(strings = {
      "https://example.org",
      "http://example.org/research",
      "https://example.org:8443/research?a=b#fragment",
      "http://localhost:8080/research",
      "https://[::1]/research",
      "HTTPS://EXAMPLE.ORG/research",
      "  https://example.org/research  "
  })
  void shouldAcceptAbsoluteHttpAndHttpsUrlsWithHost(String value) {
    assertTrue(HttpUrlValidator.isValid(value));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "",
      "   ",
      "ftp://example.org/research",
      "file:///tmp/research",
      "mailto:info@example.org",
      "javascript:alert(1)",
      "example.org/research",
      "//example.org/research",
      "http:///research",
      "https:example",
      "http://:8080/research",
      "http://%20/research",
      "http://example.org/invalid path"
  })
  void shouldRejectBlankMalformedOrNonHttpUrls(String value) {
    assertFalse(HttpUrlValidator.isValid(value));
  }

  @Test
  void shouldRejectNull() {
    assertFalse(HttpUrlValidator.isValid(null));
  }
}
