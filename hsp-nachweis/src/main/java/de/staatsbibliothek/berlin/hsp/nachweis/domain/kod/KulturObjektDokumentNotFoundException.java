package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 04.09.2020.
 * @version 1.0
 */
public class KulturObjektDokumentNotFoundException extends Exception {

  private static final long serialVersionUID = -7714533936142098113L;

  public KulturObjektDokumentNotFoundException() {
  }

  public KulturObjektDokumentNotFoundException(String message) {
    super(message);
  }

  public KulturObjektDokumentNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public KulturObjektDokumentNotFoundException(Throwable cause) {
    super(cause);
  }

  public KulturObjektDokumentNotFoundException(String message, Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
