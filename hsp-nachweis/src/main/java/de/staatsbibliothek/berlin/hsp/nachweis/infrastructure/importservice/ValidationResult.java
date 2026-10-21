package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by Christoph Marten on 21.04.2021 at 08:54
 */
@Setter
@Getter
@ToString
public class ValidationResult implements Serializable {

  @Serial
  private static final long serialVersionUID = -7381765780499522417L;

  public static final String KEY_IMPORT_ERROR_VALIDATE_XML_LINE_NUMBER = "import.error.validate.xml.line.number";
  public static final String KEY_IMPORT_ERROR_VALIDATE_XML_COLUMN_NUMBER = "import.error.validate.xml.column.number";
  public static final String KEY_IMPORT_ERROR_VALIDATE_XML_ERRORMESSAGE = "import.error.validate.xml.errormessage";

  boolean isValid;

  String line;

  String column;

  String message;

  private List<ValidationDetails> details;

  public ValidationResult() {
    details = new ArrayList<>();
  }

  public ValidationResult(boolean isValid, String line, String column, String message) {
    this();
    this.isValid = isValid;
    this.line = line;
    this.column = column;
    this.message = message;
  }

  @JsonIgnore
  public String getMessageWithDetails(Locale locale) {
    if (message == null) {
      return "";
    }

    StringBuilder sb = new StringBuilder(256);
    sb.append(message);
    addValueToMessage(line, KEY_IMPORT_ERROR_VALIDATE_XML_LINE_NUMBER, locale, sb);
    addValueToMessage(column, KEY_IMPORT_ERROR_VALIDATE_XML_COLUMN_NUMBER, locale, sb);
    String detailsMsg = getDetailsMsg(locale);
    if (!detailsMsg.isEmpty()) {
      sb.append(" -> (").append(detailsMsg).append(" )");
    }
    return sb.toString();
  }

  @JsonIgnore
  private String getDetailsMsg(Locale locale) {
    StringBuilder result = new StringBuilder(128);
    String language = locale.getLanguage();
    for (ValidationDetails detail : details) {
      String xpath = detail.getXpath();
      String error = detail.getError();

      if (xpath != null && !xpath.isEmpty()) {
        result.append(' ').append("Xpath: ").append(xpath);
      }

      addValueToMessage(error, KEY_IMPORT_ERROR_VALIDATE_XML_ERRORMESSAGE, locale, result);

      String messageWithDetails = detail.getMessageByLanguageCode(language);
      if (!messageWithDetails.isEmpty()) {
        result.append(' ').append(messageWithDetails);
      }
    }
    return result.toString();
  }

  @JsonIgnore
  private void addValueToMessage(String value, String key, Locale locale, StringBuilder sb) {
    if (value != null && !value.isEmpty()) {
      String msg = I18NController.getMessage(locale, key);
      sb.append(' ').append(msg).append(": ").append(value);
    }
  }

  @ToString
  public static class ValidationDetails {

    private String xpath;
    private String error;
    private List<Diagnostic> diagnostics = new ArrayList<>();

    public ValidationDetails() {
    }

    public ValidationDetails(String xpath, String error) {
      this.xpath = xpath;
      this.error = error;
    }

    public String getXpath() {
      return xpath;
    }

    public String getError() {
      return error;
    }

    public void appendDiagnostics(String lang, String error) {
      this.diagnostics.add(new Diagnostic(lang, error));
    }

    public List<Diagnostic> getDiagnostics() {
      return this.diagnostics;
    }


    @JsonIgnore
    public String getMessageByLanguageCode(String lang) {
      if (diagnostics == null || diagnostics.isEmpty()) {
        return "";
      }

      for (Diagnostic d : this.diagnostics) {
        if (lang.equals(d.languageCode)) {
          return d.getMessage();
        }
      }
      return diagnostics.getFirst().getMessage();
    }

  }

  @ToString
  public static class Diagnostic {

    private String languageCode;
    private String message;

    public Diagnostic() {
    }

    public Diagnostic(String languageCode, String message) {
      this.languageCode = languageCode;
      this.message = message;
    }

    public String getLanguageCode() {
      return languageCode;
    }

    public String getMessage() {
      return message;
    }
  }
}
