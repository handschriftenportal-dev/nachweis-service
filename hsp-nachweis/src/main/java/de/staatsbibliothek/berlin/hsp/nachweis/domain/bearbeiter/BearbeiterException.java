package de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 10.11.2021
 */
public class BearbeiterException extends Exception {

  private static final long serialVersionUID = -2627165949839258189L;

  public BearbeiterException() {
  }

  public BearbeiterException(String message) {
    super(message);
  }

  public BearbeiterException(String message, Throwable cause) {
    super(message, cause);
  }

  public BearbeiterException(Throwable cause) {
    super(cause);
  }

  public BearbeiterException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
