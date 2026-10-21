package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodregistrieren;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import java.util.ArrayList;
import java.util.List;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 25.08.2020.
 * @version 1.0
 */
public class KulturObjektDokumentRegistrierenException extends Exception {

  private static final long serialVersionUID = -3525630010074159014L;

  private List<KulturObjektDokument> kodFailureList = new ArrayList<>();
  private ERROR_TYPE errorType;

  public KulturObjektDokumentRegistrierenException() {
  }

  public KulturObjektDokumentRegistrierenException(String message) {
    super(message);
  }

  public KulturObjektDokumentRegistrierenException(String message,
      ERROR_TYPE errorType, Throwable cause) {
    super(message, cause);
    this.errorType = errorType;
  }

  public KulturObjektDokumentRegistrierenException(String message,
      List<KulturObjektDokument> kodFailureList, ERROR_TYPE error_type) {
    super(message);
    this.kodFailureList.addAll(kodFailureList);
    this.errorType = error_type;
  }

  public KulturObjektDokumentRegistrierenException(String message, Throwable cause) {
    super(message, cause);
  }

  public KulturObjektDokumentRegistrierenException(Throwable cause) {
    super(cause);
  }

  public KulturObjektDokumentRegistrierenException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public List<KulturObjektDokument> getKodFailureList() {
    return kodFailureList;
  }

  public ERROR_TYPE getErrorType() {
    return errorType;
  }

  public enum ERROR_TYPE {TECHNICAL, GUELTIGE_SIGNATUR_IN_SYSTEM, GUELTIGE_SIGNATUR_IN_DATEI}
}
