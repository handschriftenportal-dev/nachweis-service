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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.Locale;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 01.12.21
 */
public class I18NControllerTest {

  @Test
  void getLocale() {
    Locale locale = I18NController.getLocale();
    assertNotNull(locale);
    assertEquals(Locale.GERMAN, locale);
  }

  @Test
  void getMessage() {
    String nullMessage = I18NController.getMessage(null);
    assertEquals("???no_key???", nullMessage);

    String missingMessage = I18NController.getMessage("this_does_not_exist");
    assertEquals("???this_does_not_exist???", missingMessage);

    String realMessage = I18NController.getMessage("kodList_kod_loeschen_sperre_fehler");
    assertEquals("Dieses Kulturobjekt kann im Moment nicht gelöscht werden.", realMessage);

    String argumentsMessage = I18NController.getMessage("kodList_kods_loeschen_erfolg_viele", 42);
    assertEquals("Es wurden 42 Kulturobjekte erfolgreich gelöscht.", argumentsMessage);
  }

  @Test
  void getMessageWithLocale() {
    Locale localeDe = Locale.GERMAN;

    String nullMessage = I18NController.getMessage((Locale) null, null);
    assertEquals("???no_key???", nullMessage);

    String missingMessage = I18NController.getMessage(localeDe, "this_does_not_exist");
    assertEquals("???this_does_not_exist???", missingMessage);

    String realMessage = I18NController.getMessage(localeDe, "kodList_kod_loeschen_sperre_fehler");
    assertEquals("Dieses Kulturobjekt kann im Moment nicht gelöscht werden.", realMessage);

    String argumentsMessage = I18NController.getMessage(localeDe, "kodList_kods_loeschen_erfolg_viele", 42000);
    assertEquals("Es wurden 42.000 Kulturobjekte erfolgreich gelöscht.", argumentsMessage);

    Locale localeEn = Locale.ENGLISH;
    String argumentsMessageEn = I18NController.getMessage(localeEn, "kodList_kods_loeschen_erfolg_viele", 42000);
    assertEquals("42,000 cultural objects were successfully deleted.", argumentsMessageEn);
  }

  @Test
  void formatDate() {
    String nullDate = I18NController.formatDate(null);
    assertEquals("", nullDate);

    LocalDateTime date = LocalDateTime.of(2021, 7, 8, 12, 34, 56);
    String formatted = I18NController.formatDate(date);
    assertEquals("8.7.2021", formatted);
  }

}
