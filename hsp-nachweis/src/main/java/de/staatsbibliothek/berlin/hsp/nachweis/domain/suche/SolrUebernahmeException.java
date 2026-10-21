package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import java.io.Serial;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 06.01.2021.
 * @version 1.0
 */
public class SolrUebernahmeException extends Exception {

  @Serial
  private static final long serialVersionUID = 4157743897357254010L;

  public SolrUebernahmeException() {
  }

  public SolrUebernahmeException(String message) {
    super(message);
  }

  public SolrUebernahmeException(String message, Throwable cause) {
    super(message, cause);
  }

  public SolrUebernahmeException(Throwable cause) {
    super(cause);
  }

  public SolrUebernahmeException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
