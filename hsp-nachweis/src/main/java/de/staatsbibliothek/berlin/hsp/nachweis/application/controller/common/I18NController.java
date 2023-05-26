/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
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

  public static Locale getLocale() {

    if (Objects.nonNull(FacesContext.getCurrentInstance()) && Objects
        .nonNull(FacesContext.getCurrentInstance().getViewRoot())) {
      return FacesContext.getCurrentInstance().getViewRoot().getLocale();
    } else {
      return Locale.GERMAN;
    }
  }

  public static String getMessage(String key, Object... arguments) {
    return getMessage(I18NController.getLocale(), key, arguments);
  }

  public static String getMessage(Locale locale, String key, Object... arguments) {
    ResourceBundle messages = ResourceBundle.getBundle(I18NController.APPLICATION_MESSAGES,
        Optional.ofNullable(locale).orElse(getLocale()));

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
