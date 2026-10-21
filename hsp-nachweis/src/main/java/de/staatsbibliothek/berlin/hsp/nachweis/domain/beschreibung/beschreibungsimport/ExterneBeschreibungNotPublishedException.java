package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport;

import java.io.Serial;

public class ExterneBeschreibungNotPublishedException extends Exception {

  @Serial
  private static final long serialVersionUID = 6996347691234232L;

  public ExterneBeschreibungNotPublishedException() {
  }

  public ExterneBeschreibungNotPublishedException(String message) {
    super(message);
  }

  public ExterneBeschreibungNotPublishedException(String message, Throwable cause) {
    super(message, cause);
  }

  public ExterneBeschreibungNotPublishedException(Throwable cause) {
    super(cause);
  }

  public ExterneBeschreibungNotPublishedException(String message, Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
