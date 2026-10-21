package de.staatsbibliothek.berlin.hsp.nachweis.domain.digitalisatimport;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 19.11.2020.
 * @version 1.0
 */
public class DigitalisatImportException extends RuntimeException {

  private static final long serialVersionUID = -1880111275664174621L;

  public DigitalisatImportException() {
  }

  public DigitalisatImportException(String message) {
    super(message);
  }

  public DigitalisatImportException(String message, Throwable cause) {
    super(message, cause);
  }

  public DigitalisatImportException(Throwable cause) {
    super(cause);
  }

  public DigitalisatImportException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
