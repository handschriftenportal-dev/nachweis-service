package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen;

import java.util.Arrays;
import lombok.Builder;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 18.08.2026
 */
@Builder
public record ExterneRessourcenImportResult(
    String kodUuid,
    String ressource,
    String link,
    Status status,
    ExterneRessourcenImportErrorMessageKey errorMessageKey,
    Object[] errorMessageArgs,
    int lineNumber) {

  public ExterneRessourcenImportResult {
    errorMessageArgs = errorMessageArgs == null
        ? null
        : Arrays.copyOf(errorMessageArgs, errorMessageArgs.length);
  }

  @Override
  public Object[] errorMessageArgs() {
    return errorMessageArgs == null
        ? null
        : Arrays.copyOf(errorMessageArgs, errorMessageArgs.length);
  }

  public enum Status {
    ERFOLGREICH,
    FEHLER
  }
}
