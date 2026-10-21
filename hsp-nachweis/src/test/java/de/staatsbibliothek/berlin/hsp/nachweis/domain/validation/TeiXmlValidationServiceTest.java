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
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.TeiXmlValidationServicePort;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 31.03.2022
 */

class TeiXmlValidationServiceTest {

  private static final Identifikation IDENTIFIKATION = new IdentifikationBuilder()
      .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
      .withIdent("1-1-1")
      .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
      .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
      .build();

  @Test
  void testValidateTeiXml() throws TeiXmlValidationException {
    TeiXmlValidationServicePort teiXmlValidationServicePort = mock(
        TeiXmlValidationServicePort.class);

    when(teiXmlValidationServicePort.validateTeiXml(anyString(), anyString(), anyString()))
        .thenAnswer(i -> {
          Locale locale = Locale.forLanguageTag(i.getArguments()[1].toString());
          return new ValidationResult(true, "", "",
              I18NController.getMessage(locale, "validation_result_successfull"));
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
    assertEquals("Document is valid", result.getMessageWithDetails(Locale.ENGLISH));
    assertTrue(result.isValid());
  }

  @Test
  void testValidateKODTEIXml() throws Exception {
    TeiXmlValidationBoundary teiXmlValidationBoundary = new TeiXmlValidationServiceMock();

    KulturObjektDokument kod = new KulturObjektDokumentBuilder().withId("123").withTEIXml("<xml/>")
        .build();
    assertTrue(teiXmlValidationBoundary.validateTeiXml(kod));

    KulturObjektDokument invalidKod = new KulturObjektDokumentBuilder().withId("123")
        .withTEIXml("invalid").build();
    TeiXmlValidationException exception = assertThrows(TeiXmlValidationException.class,
        () -> teiXmlValidationBoundary.validateTeiXml(invalidKod));
    assertEquals("Validierungsfehler in KOD 123: Dokument ist nicht valide Zeilennummer: 1 Spaltennummer: 1",
        exception.getMessage());
  }

  @Test
  void testValidateBeschreibungTEIXml() throws TeiXmlValidationException {
    Beschreibung beschreibung = new BeschreibungsBuilder("456", "KOD-1", IDENTIFIKATION).withTEIXml(
        "<xml/>").build();
    TeiXmlValidationBoundary teiXmlValidationBoundary = new TeiXmlValidationServiceMock();
    assertTrue(teiXmlValidationBoundary.validateTeiXml(beschreibung, true));

    Beschreibung invalidBeschreibung = new BeschreibungsBuilder("456", "KOD-1",
        IDENTIFIKATION).withTEIXml("invalid").build();
    TeiXmlValidationException exception = assertThrows(TeiXmlValidationException.class,
        () -> teiXmlValidationBoundary.validateTeiXml(invalidBeschreibung, true));

    assertEquals("Validierungsfehler in Beschreibung 456: Dokument ist nicht valide Zeilennummer: 1 Spaltennummer: 1",
        exception.getMessage());
  }

  @Test
  void testValidateKatalogTEIXml() throws TeiXmlValidationException {
    Katalog katalog = new KatalogBuilder().withId("789").withTEIXML("<xml/>").build();
    TeiXmlValidationBoundary teiXmlValidationBoundary = new TeiXmlValidationServiceMock();
    assertTrue(teiXmlValidationBoundary.validateTeiXml(katalog));

    Katalog invalidKatalog = new KatalogBuilder().withId("789").withTEIXML("invalid").build();
    TeiXmlValidationException exception = assertThrows(TeiXmlValidationException.class,
        () -> teiXmlValidationBoundary.validateTeiXml(invalidKatalog));

    assertEquals("Validierungsfehler in Katalog 789: Dokument ist nicht valide Zeilennummer: 1 Spaltennummer: 1",
        exception.getMessage());
  }

}
