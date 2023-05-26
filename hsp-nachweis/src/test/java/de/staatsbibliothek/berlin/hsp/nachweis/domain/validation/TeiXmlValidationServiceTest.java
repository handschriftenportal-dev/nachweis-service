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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog.KatalogBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.ValidationResult;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.TeiXmlValidationServicePort;
import java.util.Locale;
import java.util.ResourceBundle;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 31.03.2022
 */

public class TeiXmlValidationServiceTest {

  @Test
  void testValidateTeiXml() throws TeiXmlValidationException {
    TeiXmlValidationServicePort teiXmlValidationServicePort = mock(TeiXmlValidationServicePort.class);

    when(teiXmlValidationServicePort.validateTeiXml(anyString(), anyString(), anyString()))
        .thenAnswer(i -> {
          ResourceBundle resourceBundle = ResourceBundle.getBundle(I18NController.APPLICATION_MESSAGES,
              Locale.forLanguageTag(i.getArguments()[1].toString()));

          return new ValidationResult(true, "", "",
              resourceBundle.getString("validation_result_successfull"));
        });

    TeiXmlValidationService service = new TeiXmlValidationService(teiXmlValidationServicePort);

    ValidationResult result = service.validateTeiXml("xml", "en", true);

    ArgumentCaptor<String> xmlCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> localeCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> schemaCaptor = ArgumentCaptor.forClass(String.class);

    verify(teiXmlValidationServicePort, times(1))
        .validateTeiXml(xmlCaptor.capture(), localeCaptor.capture(), schemaCaptor.capture());

    assertEquals("xml", xmlCaptor.getValue());
    assertEquals("en", localeCaptor.getValue());
    assertEquals("ODD", schemaCaptor.getValue());

    assertNotNull(result);
    assertNotNull(result.getMessage());
    assertEquals("Document is valid", result.getMessage());
    assertTrue(result.isValid());
  }

  @Test
  void testValidateKODTEIXml() throws Exception {
    TeiXmlValidationBoundary teiXmlValidationBoundary = new TeiXmlValidationServiceMock();

    KulturObjektDokument kod = new KulturObjektDokumentBuilder().withId("123").withTEIXml("<xml/>").build();
    assertTrue(teiXmlValidationBoundary.validateTeiXml(kod));

    KulturObjektDokument invalidKod = new KulturObjektDokumentBuilder().withId("123").withTEIXml("invalid").build();
    TeiXmlValidationException exception = assertThrows(TeiXmlValidationException.class,
        () -> teiXmlValidationBoundary.validateTeiXml(invalidKod));
    assertEquals("Validierungsfehler in KOD 123: Dokument ist nicht valide", exception.getMessage());
  }

  @Test
  void testValidateBeschreibungTEIXml() throws TeiXmlValidationException {
    Beschreibung beschreibung = new BeschreibungsBuilder().withId("456").withTEIXml("<xml/>").build();
    TeiXmlValidationBoundary teiXmlValidationBoundary = new TeiXmlValidationServiceMock();
    assertTrue(teiXmlValidationBoundary.validateTeiXml(beschreibung));

    Beschreibung invalidBeschreibung = new BeschreibungsBuilder().withId("456").withTEIXml("invalid").build();
    TeiXmlValidationException exception = assertThrows(TeiXmlValidationException.class,
        () -> teiXmlValidationBoundary.validateTeiXml(invalidBeschreibung));

    assertEquals("Validierungsfehler in Beschreibung 456: Dokument ist nicht valide", exception.getMessage());
  }

  @Test
  void testValidateKatalogTEIXml() throws TeiXmlValidationException {
    Katalog katalog = new KatalogBuilder().withId("789").withTEIXML("<xml/>").build();
    TeiXmlValidationBoundary teiXmlValidationBoundary = new TeiXmlValidationServiceMock();
    assertTrue(teiXmlValidationBoundary.validateTeiXml(katalog));

    Katalog invalidKatalog = new KatalogBuilder().withId("789").withTEIXML("invalid").build();
    TeiXmlValidationException exception = assertThrows(TeiXmlValidationException.class,
        () -> teiXmlValidationBoundary.validateTeiXml(invalidKatalog));

    assertEquals("Validierungsfehler in Katalog 789: Dokument ist nicht valide", exception.getMessage());
  }

}
