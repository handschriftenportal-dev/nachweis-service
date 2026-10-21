package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.impl.mapper.ObjectMapperFactory;
import jakarta.inject.Inject;
import java.util.Locale;
import org.junit.jupiter.api.Test;

/**
 * ValidationResult response from json
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 15/10/25
 */
class ValidationResultTest {

  @Inject
  public static final String JSON =
      """
          {
              "line": "",
              "column": "",
              "message": "XML ist nicht valide.",
              "details": [
                {
                  "xpath": "/*:TEI[1]/*:text[1]/*:body[1]/*:msDesc[1]/*:msPart[1]/*:physDesc[1]/*:decoDesc[1]/*:decoNote[1]",
                  "error": "It's possible to describe some illuminated aspects of a manuscript here",
                  "diagnostics": []
                },
                {
                  "xpath": "/*:TEI[1]/*:text[1]/*:body[1]/*:msDesc[1]/*:msPart[1]/*:physDesc[1]/*:decoDesc[1]/*:decoNote[2]",
                  "error": "It's possible to describe some illuminated aspects of a manuscript here",
                  "diagnostics": []
                },
                {
                  "xpath": "/*:TEI[1]/*:text[1]/*:body[1]/*:msDesc[1]/*:msPart[1]/*:physDesc[1]/*:decoDesc[1]/*:decoNote[3]",
                  "error": "It's possible to describe some illuminated aspects of a manuscript here",
                  "diagnostics": []
                },
                {
                  "xpath": "/*:TEI[1]/*:text[1]/*:body[1]/*:msDesc[1]/*:msPart[1]/*:physDesc[1]/*:decoDesc[1]/*:decoNote[4]",
                  "error": "It's possible to describe some illuminated aspects of a manuscript here",
                  "diagnostics": []
                },
                {
                  "xpath": "/*:TEI[1]/*:text[1]/*:body[1]/*:msDesc[1]/*:msPart[2]/*:physDesc[1]/*:decoDesc[1]/*:decoNote[1]",
                  "error": "It's possible to describe some illuminated aspects of a manuscript here",
                  "diagnostics": []
                }
              ],
              "valid": false
            }
          """;
  public static final String RESPONSE_WITH_DETAILS =
      "XML ist nicht valide. -> ( "
          + "Xpath: /*:TEI[1]/*:text[1]/*:body[1]/*:msDesc[1]/*:msPart[1]/*:physDesc[1]/*:decoDesc[1]/*:decoNote[1] "
          + "Fehlernachricht: It's possible to describe some illuminated aspects of a manuscript here "
          + "Xpath: /*:TEI[1]/*:text[1]/*:body[1]/*:msDesc[1]/*:msPart[1]/*:physDesc[1]/*:decoDesc[1]/*:decoNote[2] "
          + "Fehlernachricht: It's possible to describe some illuminated aspects of a manuscript here "
          + "Xpath: /*:TEI[1]/*:text[1]/*:body[1]/*:msDesc[1]/*:msPart[1]/*:physDesc[1]/*:decoDesc[1]/*:decoNote[3] "
          + "Fehlernachricht: It's possible to describe some illuminated aspects of a manuscript here "
          + "Xpath: /*:TEI[1]/*:text[1]/*:body[1]/*:msDesc[1]/*:msPart[1]/*:physDesc[1]/*:decoDesc[1]/*:decoNote[4] "
          + "Fehlernachricht: It's possible to describe some illuminated aspects of a manuscript here "
          + "Xpath: /*:TEI[1]/*:text[1]/*:body[1]/*:msDesc[1]/*:msPart[2]/*:physDesc[1]/*:decoDesc[1]/*:decoNote[1] "
          + "Fehlernachricht: It's possible to describe some illuminated aspects of a manuscript here )";

  @Test
  void getMessage() throws JsonProcessingException {
    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    ValidationResult validationResult = objectMapper.readValue(JSON, ValidationResult.class);
    assertEquals("XML ist nicht valide.", validationResult.getMessage());

    assertEquals(RESPONSE_WITH_DETAILS, validationResult.getMessageWithDetails(Locale.GERMAN));
  }
}
