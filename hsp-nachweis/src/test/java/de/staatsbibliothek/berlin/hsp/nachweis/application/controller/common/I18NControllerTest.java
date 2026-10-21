package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey;
import java.time.LocalDateTime;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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
  void getExterneRessourcenImportMessages() {
    assertEquals(
        "Datei fehlt oder ist leer.",
        I18NController.getMessage(
            Locale.GERMAN, "kod_externe_ressourcen_import_file_empty"));
    assertEquals(
        "UUID-Kulturobjekt",
        I18NController.getMessage(
            Locale.GERMAN, "kod_externe_ressourcen_import_result_header_uuid"));
    assertEquals(
        "UUID cultural object",
        I18NController.getMessage(
            Locale.ENGLISH, "kod_externe_ressourcen_import_result_header_uuid"));
    assertEquals(
        "Die CSV-Zeile entspricht nicht dem erwarteten Format.",
        I18NController.getMessage(
            Locale.GERMAN, "kod_externe_ressourcen_import_row_invalid"));
    assertEquals(
        "Die CSV-Datei muss UTF-8-codiert sein.",
        I18NController.getMessage(
            Locale.GERMAN, "kod_externe_ressourcen_import_encoding_invalid"));
    assertEquals(
        "Die Ergebnis-CSV konnte nicht erstellt werden.",
        I18NController.getMessage(
            Locale.GERMAN, "kod_externe_ressourcen_import_result_write_error"));
    assertEquals(
        "Das Kulturobjektdokument konnte anhand der angegebenen Identifikationsdaten nicht gefunden werden.",
        I18NController.getMessage(
            Locale.GERMAN, "kod_externe_ressourcen_import_kod_not_found"));
    assertEquals(
        "Die angegebenen Identifikationsdaten sind nicht eindeutig; es wurden mehrere "
            + "Kulturobjektdokumente gefunden.",
        I18NController.getMessage(
            Locale.GERMAN, "kod_externe_ressourcen_import_kod_ambiguous"));
    assertEquals(
        "Die angegebenen Identifikationsdaten verweisen auf unterschiedliche "
            + "Kulturobjektdokumente.",
        I18NController.getMessage(
            Locale.GERMAN, "kod_externe_ressourcen_import_kod_conflicting"));
    assertEquals(
        "Wenn UUID und Resolver-Adresse fehlen, müssen Aufbewahrungsort, Besitzende "
            + "Einrichtung und Führende Signatur angegeben werden.",
        I18NController.getMessage(
            Locale.GERMAN, "kod_externe_ressourcen_import_identification_incomplete"));
    assertEquals(
        "The cultural object document could not be found using the provided identification data.",
        I18NController.getMessage(
            Locale.ENGLISH, "kod_externe_ressourcen_import_kod_not_found"));
    assertEquals(
        "The provided identification data is ambiguous; multiple cultural object documents "
            + "were found.",
        I18NController.getMessage(
            Locale.ENGLISH, "kod_externe_ressourcen_import_kod_ambiguous"));
    assertEquals(
        "The provided identification data refers to different cultural object documents.",
        I18NController.getMessage(
            Locale.ENGLISH, "kod_externe_ressourcen_import_kod_conflicting"));
    assertEquals(
        "If UUID and resolver address are missing, holding location, owning institution, "
            + "and primary shelfmark must be provided.",
        I18NController.getMessage(
            Locale.ENGLISH, "kod_externe_ressourcen_import_identification_incomplete"));
    assertEquals(
        "Der Import externer Ressourcen ist aufgrund eines unerwarteten Fehlers fehlgeschlagen.",
        I18NController.getMessage(
            Locale.GERMAN, "kod_externe_ressourcen_import_unexpected_error"));
    assertEquals(
        "The external resources import failed due to an unexpected error.",
        I18NController.getMessage(
            Locale.ENGLISH, "kod_externe_ressourcen_import_unexpected_error"));
  }

  @Test
  void getForschungsdokumentationDeleteMessages() {
    assertEquals(
        "Möchten Sie die Forschungsdokumentation wirklich löschen?",
        I18NController.getMessage(
            Locale.GERMAN, "kod_forschungsdokumentation_delete_confirm"));
    assertEquals(
        "Do you really want to delete the research documentation?",
        I18NController.getMessage(
            Locale.ENGLISH, "kod_forschungsdokumentation_delete_confirm"));
    assertEquals(
        "Do you really want to delete the research documentation?",
        I18NController.getMessage(
            Locale.ROOT, "kod_forschungsdokumentation_delete_confirm"));
  }

  @ParameterizedTest
  @EnumSource(ExterneRessourcenImportErrorMessageKey.class)
  void shouldProvideEveryExterneRessourcenImportErrorInAllBundles(
      ExterneRessourcenImportErrorMessageKey messageKey) {

    for (Locale locale : new Locale[]{Locale.ROOT, Locale.GERMAN, Locale.ENGLISH}) {
      assertFalse(
          I18NController.getMessage(locale, messageKey.getKey()).startsWith("???"),
          () -> "Missing message key " + messageKey.getKey() + " for locale " + locale);
    }
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
