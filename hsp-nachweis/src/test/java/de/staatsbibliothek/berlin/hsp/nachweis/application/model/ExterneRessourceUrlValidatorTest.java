package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.validator.ValidatorException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
class ExterneRessourceUrlValidatorTest {

  private final ExterneRessourceUrlValidator validator = new ExterneRessourceUrlValidator();

  @ParameterizedTest
  @ValueSource(strings = {
      "https://example.org",
      "http://example.org/test",
      "  https://example.org/test  "
  })
  void shouldAcceptValidUrl(String url) {
    assertDoesNotThrow(() -> validator.validate(null, null, url));
  }

  @Test
  void shouldAcceptBlankUrlBecauseTheSaveButtonHandlesEmptiness() {
    assertDoesNotThrow(() -> validator.validate(null, null, null));
    assertDoesNotThrow(() -> validator.validate(null, null, ""));
    assertDoesNotThrow(() -> validator.validate(null, null, "   "));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "http:/",
      "handschriftencensus.de/123",
      "ftp://example.org/test",
      "http:///test"
  })
  void shouldRejectInvalidUrl(String url) {
    ValidatorException exception = assertThrows(ValidatorException.class,
        () -> validator.validate(null, null, url));

    FacesMessage message = exception.getFacesMessage();

    assertNotNull(message);
    assertEquals(FacesMessage.SEVERITY_ERROR, message.getSeverity());
    assertNotNull(message.getSummary());
  }
}
