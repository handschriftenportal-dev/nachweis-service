package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen;

import lombok.Getter;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 18.08.2026
 */
@Getter
public class ExterneRessourcenImportException extends Exception {

  private final ExterneRessourcenImportErrorMessageKey messageKey;
  private final Object[] args;

  public ExterneRessourcenImportException(
      ExterneRessourcenImportErrorMessageKey messageKey, Object... args) {
    this.messageKey = messageKey;
    this.args = args;
  }

  public ExterneRessourcenImportException(
      ExterneRessourcenImportErrorMessageKey messageKey, Throwable cause, Object... args) {
    super(cause);
    this.messageKey = messageKey;
    this.args = args;
  }
}
