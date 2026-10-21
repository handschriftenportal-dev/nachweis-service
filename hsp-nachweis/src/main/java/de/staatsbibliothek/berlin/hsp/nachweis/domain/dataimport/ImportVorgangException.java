package de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport;

import java.io.Serial;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 27.05.2020.
 * @version 1.0
 */
public class ImportVorgangException extends Exception {

  @Serial
  private static final long serialVersionUID = -11141600270699102L;

  public ImportVorgangException() {
  }

  public ImportVorgangException(String message) {
    super(message);
  }

  public ImportVorgangException(String message, Throwable cause) {
    super(message, cause);
  }

  public ImportVorgangException(Throwable cause) {
    super(cause);
  }

  public ImportVorgangException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
