package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationService;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TeiXmlValidationRestController}.
 */
class TeiXmlValidationRestControllerTest {

  private final TeiXmlValidationService validationService = mock(TeiXmlValidationService.class);

  private static final String VALID_TEI_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\n"
      + "  <teiHeader>\n"
      + "    <fileDesc>\n"
      + "      <titleStmt>\n"
      + "        <title>Test</title>\n"
      + "      </titleStmt>\n"
      + "    </fileDesc>\n"
      + "  </teiHeader>\n"
      + "  <text>\n"
      + "    <body>\n"
      + "      <p>Test content</p>\n"
      + "    </body>\n"
      + "  </text>\n"
      + "</TEI>";

  private static final String INVALID_TEI_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\n"
      + "  <invalidElement>\n"
      + "  </invalidElement>\n"
      + "</TEI>";

  @Test
  void validateTeiWithValidResult() {
    TeiXmlValidationRestController controller = new TeiXmlValidationRestController(
        validationService);

    ValidationResult validResult = new ValidationResult(true, null, null, "Valid TEI XML");
    when(validationService.validateTei(VALID_TEI_XML, "de"))
        .thenReturn(validResult);

    Response response = controller.validateTei(VALID_TEI_XML, "de");
      assertEquals(200, response.getStatus());
      assertEquals(validResult, response.getEntity());
  }

  @Test
  void validateTeiReturns400WithInvalidResult() {
    TeiXmlValidationRestController controller = new TeiXmlValidationRestController(
        validationService);

    ValidationResult invalidResult = new ValidationResult(false, "10", "5", "Invalid TEI XML");
    when(validationService.validateTei(INVALID_TEI_XML, "de"))
        .thenReturn(invalidResult);

    Response response = controller.validateTei(INVALID_TEI_XML, "de");
    assertEquals(200, response.getStatus());
    assertEquals(invalidResult, response.getEntity());
  }

  @Test
  void validateOddWithValidResult() throws TeiXmlValidationException {
    TeiXmlValidationRestController controller = new TeiXmlValidationRestController(
        validationService);

    ValidationResult validResult = new ValidationResult(true, null, null, "Valid ODD XML");
    when(validationService.validateOdd(VALID_TEI_XML, "en"))
        .thenReturn(validResult);

    Response response = controller.validateOdd(VALID_TEI_XML, "en");
    assertEquals(200, response.getStatus());
    assertEquals(validResult, response.getEntity());
  }

  @Test
  void validateOddWithInvalidResult() throws TeiXmlValidationException {
    TeiXmlValidationRestController controller = new TeiXmlValidationRestController(
        validationService);

    ValidationResult invalidResult = new ValidationResult(false, "20", "10", "Invalid ODD XML");
    when(validationService.validateOdd(INVALID_TEI_XML, "en"))
        .thenReturn(invalidResult);

    Response response = controller.validateOdd(INVALID_TEI_XML, "en");
    assertEquals(200, response.getStatus());
    assertEquals(invalidResult, response.getEntity());
  }
}
