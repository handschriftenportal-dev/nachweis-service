package de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception;

/**
 * Throw if any Dokument Sperre repository related problem occurs
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 17.08.21
 */
public class DokumentSperreRepositoryException extends Exception {

  private static final long serialVersionUID = 4952986276284622330L;

  public DokumentSperreRepositoryException() {
  }

  public DokumentSperreRepositoryException(String message) {
    super(message);
  }

  public DokumentSperreRepositoryException(String message, Throwable cause) {
    super(message, cause);
  }

  public DokumentSperreRepositoryException(Throwable cause) {
    super(cause);
  }

  public DokumentSperreRepositoryException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
