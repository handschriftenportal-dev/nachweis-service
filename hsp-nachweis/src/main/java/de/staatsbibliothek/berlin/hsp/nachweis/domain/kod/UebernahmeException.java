package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod;

public class UebernahmeException extends RuntimeException {

  private static final long serialVersionUID = -6942987765817657714L;

  public UebernahmeException(final String message) {
    super(message);
  }

  public UebernahmeException(final String message, final Exception cause) {
    super(message, cause);
  }

  public UebernahmeException(final Exception cause) {
    super(cause);
  }

}
