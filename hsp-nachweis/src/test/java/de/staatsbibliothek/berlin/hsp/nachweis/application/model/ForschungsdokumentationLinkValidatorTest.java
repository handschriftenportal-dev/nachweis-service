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
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
class ForschungsdokumentationLinkValidatorTest {

  private final ForschungsdokumentationLinkValidator validator =
      new ForschungsdokumentationLinkValidator();

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "\t\n"})
  void shouldAcceptBlankLink(String link) {
    assertDoesNotThrow(() -> validator.validate(null, null, link));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "https://example.org",
      "http://example.org/research",
      "  HTTPS://example.org/research  "
  })
  void shouldAcceptValidLink(String link) {
    assertDoesNotThrow(() -> validator.validate(null, null, link));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "http:/",
      "example.org/research",
      "ftp://example.org/research",
      "http:///research"
  })
  void shouldRejectInvalidLink(String link) {
    ValidatorException exception = assertThrows(
        ValidatorException.class,
        () -> validator.validate(null, null, link));

    FacesMessage message = exception.getFacesMessage();
    assertNotNull(message);
    assertEquals(FacesMessage.SEVERITY_ERROR, message.getSeverity());
    assertNotNull(message.getSummary());
    assertEquals(message.getSummary(), message.getDetail());
  }
}
