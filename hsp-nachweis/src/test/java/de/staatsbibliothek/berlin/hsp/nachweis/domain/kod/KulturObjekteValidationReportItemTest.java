package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class KulturObjekteValidationReportItemTest {

  @Test
  void toCSV_basics() {
    ValidationResult vr = new ValidationResult(false, null, null, "Fehler");
    KulturObjekteValidationReportItem item = KulturObjekteValidationReportItem.builder()
        .kulturobjektHspID("HSP-1")
        .signatur("Sig.,23")
        .bestandsHaltendeEinrichtung("BLB")
        .ort("Karlsruhe")
        .validationResult(vr)
        .build();

    assertEquals("HSP-1;Sig.,23;BLB;Karlsruhe;Fehler", item.toCSV(Locale.GERMAN),
        "CSV (ohne Sonderzeichen) sollte simpel mit ; getrennt sein");
  }

  @Test
  void toCSV_nullFelderWerdenLeer_ohneNPE() {
    KulturObjekteValidationReportItem item = KulturObjekteValidationReportItem.builder()
        .kulturobjektHspID("HSP-1")
        .signatur(null)
        .bestandsHaltendeEinrichtung(null)
        .ort(null)
        .validationResult(null)
        .build();
    Locale locale = Locale.GERMAN;
    assertDoesNotThrow(() -> item.toCSV(locale));
    assertEquals("HSP-1;;;;", item.toCSV(locale));
  }

  @Test
  void toCSV_validationResultOhneMessage_letzteSpalteLeer() {
    ValidationResult vrOhneMsg = new ValidationResult(false, null, null, null);

    KulturObjekteValidationReportItem item = KulturObjekteValidationReportItem.builder()
        .kulturobjektHspID("HSP-2")
        .signatur(null)
        .bestandsHaltendeEinrichtung(null)
        .ort(null)
        .validationResult(vrOhneMsg)
        .build();
    Locale locale = Locale.GERMAN;
    assertDoesNotThrow(() -> item.toCSV(locale));
    assertEquals("HSP-2;;;;", item.toCSV(locale));
  }
}
