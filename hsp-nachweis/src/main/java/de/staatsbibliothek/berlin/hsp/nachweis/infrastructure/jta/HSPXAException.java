package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta;

import javax.transaction.xa.XAException;

/**
 * Custom XAException to allow us to provide additional information It is thrown by the Resource Manager to inform the
 * Transaction Manager of an error encountered by the involved transaction.
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 04.03.21
 */
public class HSPXAException extends XAException {

  private static final long serialVersionUID = 690701490953030354L;

  public HSPXAException(String message, int errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  public HSPXAException(String message, Throwable cause, int errorCode) {
    super(message);
    this.initCause(cause);
    this.errorCode = errorCode;
  }

  public HSPXAException(Throwable cause, int errorCode) {
    super(errorCode);
    this.initCause(cause);
  }
}
