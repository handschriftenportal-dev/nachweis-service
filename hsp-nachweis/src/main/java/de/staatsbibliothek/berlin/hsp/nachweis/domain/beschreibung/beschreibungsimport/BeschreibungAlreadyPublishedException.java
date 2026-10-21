package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport;

import java.io.Serial;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 20.05.2020.
 * @version 1.0
 */
public class BeschreibungAlreadyPublishedException extends Exception {

  @Serial
  private static final long serialVersionUID = 699634769131287912L;

  public BeschreibungAlreadyPublishedException() {
  }

  public BeschreibungAlreadyPublishedException(String message) {
    super(message);
  }

  public BeschreibungAlreadyPublishedException(String message, Throwable cause) {
    super(message, cause);
  }

  public BeschreibungAlreadyPublishedException(Throwable cause) {
    super(cause);
  }

  public BeschreibungAlreadyPublishedException(String message, Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
