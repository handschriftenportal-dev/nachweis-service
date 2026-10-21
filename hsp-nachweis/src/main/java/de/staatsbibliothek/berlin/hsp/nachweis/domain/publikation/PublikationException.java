package de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation;

import java.io.Serial;
import lombok.Getter;

public class PublikationException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 42L;
  @Getter
  private final String beschreibungId;
  @Getter
  private final Object[] messageArgs;

  public PublikationException(String message, String beschreibungId) {
    this(message, beschreibungId, null, new String[0]);
  }

  public PublikationException(String message, String beschreibungId, Throwable cause) {
    this(message, beschreibungId, cause, new String[0]);
  }

  public PublikationException(String message, String beschreibungId, String... messageArgs) {
    this(message, beschreibungId, null, messageArgs);
  }

  public PublikationException(String message, String beschreibungId, Throwable cause,
      String... messageArgs) {
    super(message, cause);
    this.beschreibungId = beschreibungId;
    this.messageArgs = messageArgs != null ? messageArgs : new String[0];
  }
}
