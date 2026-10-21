package de.staatsbibliothek.berlin.hsp.nachweis.domain.purl;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 08.08.22
 */

public class PURLException extends Exception {

  private static final long serialVersionUID = 2693252365519164943L;

  public PURLException(String message) {
    super(message);
  }

  public PURLException(String message, Throwable cause) {
    super(message, cause);
  }
}
