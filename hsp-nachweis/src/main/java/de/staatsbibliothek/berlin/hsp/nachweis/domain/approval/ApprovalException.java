package de.staatsbibliothek.berlin.hsp.nachweis.domain.approval;

import java.io.Serial;

public class ApprovalException extends Exception {

  @Serial
  private static final long serialVersionUID = 1L;

  public ApprovalException(String message) {
    super(message);
  }

  public ApprovalException(String message, Throwable cause) {
    super(message, cause);
  }
}