package de.staatsbibliothek.berlin.hsp.nachweis.domain.validation;

import java.io.Serial;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 29.03.2022
 */

public class TeiXmlValidationException extends Exception {

  @Serial
  private static final long serialVersionUID = 6622377090800477730L;

  public TeiXmlValidationException(String message) {
    super(message);
  }

  public TeiXmlValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
