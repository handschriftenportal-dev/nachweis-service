package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(access = AccessLevel.PUBLIC)
@EqualsAndHashCode(of = {"kulturobjektHspID"})
@ToString
public class KulturObjekteValidationReportItem {

  private String kulturobjektHspID;

  private String signatur;

  private String bestandsHaltendeEinrichtung;

  private String ort;

  private ValidationResult validationResult;

  public String toCSV(Locale locale) {
    return String.join(";",
        csv(kulturobjektHspID),
        csv(signatur),
        csv(bestandsHaltendeEinrichtung),
        csv(ort),
        csv(validationResult != null ? validationResult.getMessageWithDetails(locale) : ""));
  }

  private static String csv(String v) {
    if (v == null) return "";
    return v.replace('\r', ' ')
        .replace('\n', ' ')
        .replace('\t', ' ');
  }
}
