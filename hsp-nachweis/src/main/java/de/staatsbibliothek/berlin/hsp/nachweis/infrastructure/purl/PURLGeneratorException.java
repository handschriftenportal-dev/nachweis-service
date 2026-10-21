package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.purl;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 02.08.22
 */
public class PURLGeneratorException extends Exception {

  private static final long serialVersionUID = -3849712565401912564L;

  public PURLGeneratorException(String message) {
    super(message);
  }

  public PURLGeneratorException(String message, Throwable cause) {
    super(message, cause);
  }

}
