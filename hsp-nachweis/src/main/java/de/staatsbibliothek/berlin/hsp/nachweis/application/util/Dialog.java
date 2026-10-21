package de.staatsbibliothek.berlin.hsp.nachweis.application.util;

import jakarta.faces.context.FacesContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.primefaces.PrimeFaces;

/**
 * Fluent builder for opening PrimeFaces dynamic dialogs.
 *
 * <p>Example usage:
 * <pre>{@code
 * Dialog.forOutcome("/handschriftenportal/createPublikationDialog")
 *     .withSize("600", "400")
 *     .withParam("beschreibungID", beschreibungId)
 *     .closable(true)
 *     .open();
 * }</pre>
 *
 * <p>Default options applied to all dialogs:
 * <ul>
 *   <li>modal: true</li>
 *   <li>closable: false</li>
 *   <li>draggable: false</li>
 *   <li>resizable: false</li>
 *   <li>width: 500</li>
 *   <li>height: 400</li>
 *   <li>contentWidth: 100%</li>
 *   <li>contentHeight: 100%</li>
 *   <li>headerElement: headerTitle</li>
 * </ul>
 *
 * <p>To close a dialog from within the dialog page, use {@link #close(Object)} or {@link #close()}.
 */
public final class Dialog {

  private static final String DEFAULT_WIDTH = "500";
  private static final String DEFAULT_HEIGHT = "400";

  private final String outcome;
  private final Map<String, Object> options;
  private final Map<String, List<String>> params;

  private Dialog(String outcome) {
    this.outcome = outcome;
    this.options = createDefaultOptions();
    this.params = new HashMap<>();
  }

  public static Dialog forOutcome(String outcome) {
    return new Dialog(outcome);
  }

  public static void close(Object result) {
    if (FacesContext.getCurrentInstance() != null) {
      PrimeFaces.current().dialog().closeDynamic(result);
    }
  }

  public static void close() {
    close(null);
  }

  private static Map<String, Object> createDefaultOptions() {
    Map<String, Object> options = new HashMap<>();
    options.put("modal", true);
    options.put("closable", false);
    options.put("draggable", false);
    options.put("resizable", false);
    options.put("width", DEFAULT_WIDTH);
    options.put("height", DEFAULT_HEIGHT);
    options.put("contentWidth", "100%");
    options.put("contentHeight", "100%");
    options.put("headerElement", "headerTitle");
    return options;
  }

  public Dialog withSize(String width, String height) {
    options.put("width", width);
    options.put("height", height);
    return this;
  }

  public Dialog withParam(String name, String value) {
    params.put(name, List.of(value));
    return this;
  }

  public Dialog withParams(Map<String, List<String>> params) {
    this.params.putAll(params);
    return this;
  }

  public Dialog closable(boolean closable) {
    options.put("closable", closable);
    return this;
  }

  public Dialog draggable(boolean draggable) {
    options.put("draggable", draggable);
    return this;
  }

  public Dialog resizable(boolean resizable) {
    options.put("resizable", resizable);
    return this;
  }

  public Dialog modal(boolean modal) {
    options.put("modal", modal);
    return this;
  }

  public Dialog withOption(String key, Object value) {
    options.put(key, value);
    return this;
  }

  public void open() {
    PrimeFaces.current().dialog().openDynamic(outcome, options, params);
  }
}
