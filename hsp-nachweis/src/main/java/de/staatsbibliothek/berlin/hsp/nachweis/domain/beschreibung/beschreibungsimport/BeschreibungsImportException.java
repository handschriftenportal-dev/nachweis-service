package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport;

import java.io.Serial;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 24.11.20
 */
public class BeschreibungsImportException extends Exception {

  @Serial
  private static final long serialVersionUID = -5382469934897065515L;

  public BeschreibungsImportException() {
  }

  public BeschreibungsImportException(String message) {
    super(message);
  }

  public BeschreibungsImportException(String message, Throwable cause) {
    super(message, cause);
  }

  public BeschreibungsImportException(Throwable cause) {
    super(cause);
  }

  public BeschreibungsImportException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
