package de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog;


public class KatalogException extends RuntimeException {

  private static final long serialVersionUID = 2814085836827225871L;

  public KatalogException() {
  }

  public KatalogException(String message) {
    super(message);
  }

  public KatalogException(String message, Throwable cause) {
    super(message, cause);
  }

  public KatalogException(Throwable cause) {
    super(cause);
  }

  public KatalogException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
