package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 22.03.2021.
 * @version 1.0
 */
public class NachweisErfassungResponse {

  private boolean success;

  private String message;

  private String level;

  private Object content;

  public boolean isSuccess() {
    return success;
  }

  public void withSuccess(String message) {
    this.success = true;
    this.message = message;
    this.level = "info";
    this.content = "";
  }

  public void successWithContent(String message, Object content) {
    withSuccess(message);
    this.content = content;
  }

  public void withError(String message) {
    this.success = false;
    this.message = message;
    this.level = "error";
    this.content = "";
  }

  public void errorWithContent(String message, Object content) {
    withError(message);
    this.content = content;
  }

  public String getMessage() {
    return message;
  }

  public String getLevel() {
    return level;
  }

  public Object getContent() {
    return content;
  }

}
