package de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(access = AccessLevel.PUBLIC)
@EqualsAndHashCode(of = {"hspId"})
@ToString
public class KatalogValidationReportItem {

  private String hspId;

  private String titel;

  private String publikationsjahr;

  private ValidationResult validationResult;

  public String toCSV(Locale locale) {
    return String.join(";",
        csv(hspId),
        csv(titel),
        csv(publikationsjahr),
        csv(validationResult != null ? validationResult.getMessageWithDetails(locale) : "")
    );
  }

  private static String csv(String v) {
    if (v == null) return "";
    return v.replace('\r', ' ')
        .replace('\n', ' ')
        .replace('\t', ' ');
  }
}
