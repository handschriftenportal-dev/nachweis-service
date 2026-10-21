package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr;

import java.io.Serial;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 13.01.21
 */
public class SolrServiceException extends Exception {

  @Serial
  private static final long serialVersionUID = -1799594213035320669L;

  public SolrServiceException() {
  }

  public SolrServiceException(String message) {
    super(message);
  }

  public SolrServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  public SolrServiceException(Throwable cause) {
    super(cause);
  }

  public SolrServiceException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
