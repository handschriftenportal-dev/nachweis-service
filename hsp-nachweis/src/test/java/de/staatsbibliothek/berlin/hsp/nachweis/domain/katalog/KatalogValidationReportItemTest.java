package de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog;

import static org.junit.jupiter.api.Assertions.*;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class KatalogValidationReportItemTest {

  @Test
  void toCSV_basics() {
    ValidationResult vr = new ValidationResult(false, null, null, "Fehler");
    KatalogValidationReportItem item = KatalogValidationReportItem.builder()
        .hspId("HSP-1")
        .titel("Titel,23")
        .publikationsjahr("2024")
        .validationResult(vr)
        .build();

    assertEquals("HSP-1;Titel,23;2024;Fehler", item.toCSV(Locale.GERMAN),
        "CSV (ohne Sonderzeichen) sollte simpel mit ; getrennt sein");
  }

  @Test
  void toCSV_nullFelderWerdenLeer_ohneNPE() {
    KatalogValidationReportItem item = KatalogValidationReportItem.builder()
        .hspId("HSP-1")
        .titel(null)
        .publikationsjahr(null)
        .validationResult(null)
        .build();
    Locale locale = Locale.GERMAN;

    assertDoesNotThrow(() -> item.toCSV(locale));
    assertEquals("HSP-1;;;", item.toCSV(locale));
  }

  @Test
  void toCSV_validationResultOhneMessage_letzteSpalteLeer() {
    ValidationResult vrOhneMsg = new ValidationResult(false, null, null, null);

    KatalogValidationReportItem item = KatalogValidationReportItem.builder()
        .hspId("HSP-2")
        .titel(null)
        .publikationsjahr(null)
        .validationResult(vrOhneMsg)
        .build();
    Locale locale = Locale.GERMAN;

    assertDoesNotThrow(() -> item.toCSV(locale));
    assertEquals("HSP-2;;;", item.toCSV(locale));
  }
}