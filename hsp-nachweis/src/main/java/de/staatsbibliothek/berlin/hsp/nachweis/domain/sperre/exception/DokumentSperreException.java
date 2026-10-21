package de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception;

/**
 * Provide information about problems associated to DokumentSperreBoundary
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 19.08.21
 */
public class DokumentSperreException extends Exception {

  private static final long serialVersionUID = -5464822577105252553L;

  public DokumentSperreException() {
  }

  public DokumentSperreException(String message) {
    super(message);
  }

  public DokumentSperreException(String message, Throwable cause) {
    super(message, cause);
  }

  public DokumentSperreException(Throwable cause) {
    super(cause);
  }

  public DokumentSperreException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
