package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common;

import jakarta.faces.context.FacesContext;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 04.09.2020.
 * @version 1.0
 */
@Slf4j
public class I18NController {

  public static final String APPLICATION_MESSAGES = "de.staatsbibliothek.berlin.hsp.application.messages";
  public static final String DATE_PATTERN_DE = "d.M.yyyy";
  public static final String DATE_PATTERN_EN = "M/d/yyyy";

  private static final Map<Locale, ResourceBundle> BUNDLES = new ConcurrentHashMap<>();

  public static Locale getLocale() {

    FacesContext currentInstance = FacesContext.getCurrentInstance();
    if (Objects.nonNull(currentInstance) && Objects.nonNull(currentInstance.getViewRoot())) {
      return currentInstance.getViewRoot().getLocale();
    }
    return Locale.GERMAN;
  }

  public static String getMessage(String key, Object... arguments) {
    return getMessage(I18NController.getLocale(), key, arguments);
  }

  public static String getMessage(Locale locale, String key, Object... arguments) {
    if (locale == null) {
      locale = getLocale();
    }
    if (!BUNDLES.containsKey(locale)) {
      BUNDLES.put(locale, ResourceBundle.getBundle(I18NController.APPLICATION_MESSAGES, locale));
    }
    ResourceBundle messages = BUNDLES.get(locale);

    if (Objects.isNull(key)) {
      return "???no_key???";
    }
    String value;
    try {
      value = messages.getString(key);
    } catch (MissingResourceException mre) {
      log.error("No message found for key {}", key);
      return "???" + key + "???";
    }

    if (arguments.length > 0) {
      MessageFormat messageFormat = new MessageFormat(value, locale);
      value = messageFormat.format(arguments);
    }
    return value;
  }

  public static String formatDate(LocalDateTime date) {
    if (Objects.isNull(date)) {
      return "";
    }
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(Locale.ENGLISH.equals(I18NController.getLocale())
        ? DATE_PATTERN_EN : DATE_PATTERN_DE);
    return date.format(dateFormatter);
  }
}
