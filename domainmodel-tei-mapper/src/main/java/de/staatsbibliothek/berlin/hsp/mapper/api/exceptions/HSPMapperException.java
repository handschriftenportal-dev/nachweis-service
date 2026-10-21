package de.staatsbibliothek.berlin.hsp.mapper.api.exceptions;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 20.04.20
 */
public class HSPMapperException extends Exception {

  private static final long serialVersionUID = -6608707508444672955L;

  public HSPMapperException() {
  }

  public HSPMapperException(String message) {
    super(message);
  }

  public HSPMapperException(String message, Throwable cause) {
    super(message, cause);
  }

  public HSPMapperException(Throwable cause) {
    super(cause);
  }

  public HSPMapperException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
