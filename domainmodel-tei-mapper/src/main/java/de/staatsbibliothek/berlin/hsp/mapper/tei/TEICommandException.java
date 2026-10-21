package de.staatsbibliothek.berlin.hsp.mapper.tei;

import java.io.Serial;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 13.07.23.
 * @version 1.0
 */
public class TEICommandException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -8729028519723750648L;

  public TEICommandException() {
  }

  public TEICommandException(String message) {
    super(message);
  }

  public TEICommandException(String message, Throwable cause) {
    super(message, cause);
  }

  public TEICommandException(Throwable cause) {
    super(cause);
  }

  public TEICommandException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
