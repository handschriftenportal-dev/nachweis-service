package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 10.03.21
 */
public class BeschreibungsException extends Exception {

  private static final long serialVersionUID = -1606856764974863866L;

  public BeschreibungsException() {
  }

  public BeschreibungsException(String message) {
    super(message);
  }

  public BeschreibungsException(String message, Throwable cause) {
    super(message, cause);
  }

  public BeschreibungsException(Throwable cause) {
    super(cause);
  }

  public BeschreibungsException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
