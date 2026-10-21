package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.faces.context.ExternalContext;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 15.06.2020.
 * @version 1.0
 */
class RequestControllerTest {

  LanguageController languageController = Mockito.mock(LanguageController.class);

  @Test
  void testCreation() {

    RequestController controller = new RequestController(languageController);

    assertNotNull(controller);

    assertNotNull(controller.getLanguageController());
  }

  @Test
  void testRequest() {

    RequestController controller = new RequestController(languageController);
    ExternalContext context = Mockito.mock(ExternalContext.class);
    controller.setContext(context);
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put("lang", "de");
    requestMap.put("query", "test");
    when(context.getRequestParameterMap()).thenReturn(requestMap);

    controller.request();

    verify(languageController, times(1)).setLanguage("de");

  }
}
