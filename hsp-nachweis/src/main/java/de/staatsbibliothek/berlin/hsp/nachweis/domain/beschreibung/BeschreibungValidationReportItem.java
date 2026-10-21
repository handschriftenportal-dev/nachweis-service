package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 06.05.24.
 * @version 1.0
 */
@Getter
@Builder(access = AccessLevel.PUBLIC)
@EqualsAndHashCode(of = {"beschreibungsID"})
@ToString
public class BeschreibungValidationReportItem {

  private String beschreibungsID;
  private String signatur;
  private String nameBesitzer;
  private String nameAufbewahrungsort;
  private String mxmlID;
  private ValidationResult validationResult;

  public String toCSV(Locale locale) {
    return String.join(";",
        csv(beschreibungsID),
        csv(signatur),
        csv(nameBesitzer),
        csv(nameAufbewahrungsort),
        csv(validationResult != null ? validationResult.getMessageWithDetails(locale) : ""));
  }

  private static String csv(String v) {
    if (v == null) {
      return "";
    }
    return v.replace('\r', ' ')
        .replace('\n', ' ')
        .replace('\t', ' ');
  }
}
