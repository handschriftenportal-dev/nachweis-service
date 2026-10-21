package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import java.util.Locale;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 06.05.24.
 * @version 1.0
 */
public class BeschreibungValidationReportItemTest {

  @Test
  void testToCSV() {
    BeschreibungValidationReportItem reportItem = new BeschreibungValidationReportItem.BeschreibungValidationReportItemBuilder()
        .beschreibungsID("HSP1")
        .nameBesitzer("Staabi")
        .nameAufbewahrungsort("Berlin")
        .validationResult(new ValidationResult(false, "", "", "Fehler"))
        .build();

    assertEquals("HSP1;;Staabi;Berlin;Fehler", reportItem.toCSV(Locale.GERMAN));
  }

  @Test
  void toCSV_nullFelderWerdenLeer_ohneNPE() {
    BeschreibungValidationReportItem reportItem = BeschreibungValidationReportItem.builder()
        .beschreibungsID("HSP2")
        .signatur(null)
        .nameBesitzer(null)
        .nameAufbewahrungsort(null)
        .validationResult(null)
        .build();

    final Locale locale = Locale.GERMAN;
    assertDoesNotThrow(() -> reportItem.toCSV(locale));
    assertEquals("HSP2;;;;", reportItem.toCSV(locale));
  }

  @Test
  void toCSV_validationResultOhneMessage_letzteSpalteLeer() {
    ValidationResult vrOhneMsg = new ValidationResult(false, null, null, null);

    BeschreibungValidationReportItem reportItem = BeschreibungValidationReportItem.builder()
        .beschreibungsID("HSP3")
        .signatur(null)
        .nameBesitzer(null)
        .nameAufbewahrungsort(null)
        .validationResult(vrOhneMsg)
        .build();

    final Locale locale = Locale.GERMAN;
    assertDoesNotThrow(() -> reportItem.toCSV(locale));
    assertEquals("HSP3;;;;", reportItem.toCSV(locale));
  }
}
