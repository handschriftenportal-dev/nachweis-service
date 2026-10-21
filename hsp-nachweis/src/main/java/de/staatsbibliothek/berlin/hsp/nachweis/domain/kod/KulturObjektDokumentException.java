package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 06.10.2020.
 * @version 1.0
 */
public class KulturObjektDokumentException extends RuntimeException {

  private static final long serialVersionUID = -3083401497433010967L;

  public KulturObjektDokumentException() {
  }

  public KulturObjektDokumentException(String message) {
    super(message);
  }

  public KulturObjektDokumentException(String message, Throwable cause) {
    super(message, cause);
  }

  public KulturObjektDokumentException(Throwable cause) {
    super(cause);
  }

  public KulturObjektDokumentException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
