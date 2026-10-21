package de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung;

import java.io.Serial;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class DatensicherungException extends RuntimeException {

  @Getter(AccessLevel.NONE)
  @Serial
  private static final long serialVersionUID = -3083401497433010967L;

  private final String id;

  public DatensicherungException(String message, String id, Throwable cause) {
    super(message, cause);
    this.id = id;
  }

  public DatensicherungException(String message) {
    this(message, "", null);
  }

  public DatensicherungException(String message, String id) {
    this(message, id, null);
  }

  public DatensicherungException(String message, Throwable cause) {
    this(message, "", cause);
  }


  public DatensicherungException(Throwable cause) {
    this(null, "", cause);
  }

}
