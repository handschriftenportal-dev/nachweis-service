/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
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

    assertEquals(31, controller.getTitleMap().size());
  }
}
