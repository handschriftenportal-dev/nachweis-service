package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport;

import java.io.Serial;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 05.12.23.
 * @version 1.0
 */
public class KulturObjektDokumentMismatchException extends Exception {

  @Serial
  private static final long serialVersionUID = 7042246392858624376L;

  public KulturObjektDokumentMismatchException() {
  }

  public KulturObjektDokumentMismatchException(String message) {
    super(message);
  }

  public KulturObjektDokumentMismatchException(String message, Throwable cause) {
    super(message, cause);
  }

  public KulturObjektDokumentMismatchException(Throwable cause) {
    super(cause);
  }

  public KulturObjektDokumentMismatchException(String message, Throwable cause,
      boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
