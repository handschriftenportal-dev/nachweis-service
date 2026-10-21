package de.staatsbibliothek.berlin.hsp.mapper.api.exceptions;

public class HSPCommandException extends Exception {

  private static final long serialVersionUID = 3570323972471415310L;

  public HSPCommandException(String message) {
    super(message);
  }

  public HSPCommandException(String message, Throwable cause) {
    super(message, cause);
  }

}
