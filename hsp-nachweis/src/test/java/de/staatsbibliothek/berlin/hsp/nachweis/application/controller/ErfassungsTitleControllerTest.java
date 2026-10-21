package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 29.05.2020.
 * @version 1.0
 */
public class ErfassungsTitleControllerTest {

  ResourceBundle dummyResourceBundle = new ResourceBundle() {
    @Override
    protected Object handleGetObject(String key) {
      return "Datei Upload | XML Dateien importieren";
    }

    @Override
    public Enumeration<String> getKeys() {
      return Collections.enumeration(Arrays
          .asList("document_titel_dateiupload", "document_titel_suche", "document_titel_datenuebernahme",
              "document_titel_importvorgaenge","document_titel_kodregistrieren"));
    }
  };

  @Test
  void testGetTitle() {

    FacesContext context = Mockito.mock(FacesContext.class);
    UIViewRoot viewRoot = Mockito.mock(UIViewRoot.class);
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

    when(context.getViewRoot()).thenReturn(viewRoot);
    when(viewRoot.getLocale()).thenReturn(Locale.GERMANY);
    when(request.getRequestURI()).thenReturn("/dateiupload.xhtml");

    ErfassungsTitleController controller = new ErfassungsTitleController();
    controller.setContext(context);
    controller.setBundle(dummyResourceBundle);
    controller.setup();
    controller.setRequest(request);

    assertEquals("Datei Upload | XML Dateien importieren", controller.getTitle());

    assertEquals(35, controller.getTitleMap().size());
  }
}
