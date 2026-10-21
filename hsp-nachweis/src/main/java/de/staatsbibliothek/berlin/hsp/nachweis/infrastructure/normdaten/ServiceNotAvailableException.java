package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import java.io.Serial;

public class ServiceNotAvailableException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 17567567L;
  
  public ServiceNotAvailableException(String message) {
    super(message);
  }

  public ServiceNotAvailableException(String message, Throwable cause) {
    super(message, cause);
  }

  public ServiceNotAvailableException(Throwable cause) {
    super(cause);
  }
}
