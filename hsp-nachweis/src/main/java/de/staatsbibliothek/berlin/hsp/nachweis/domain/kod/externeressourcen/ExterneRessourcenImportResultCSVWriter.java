package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;

/**
 * Writes the result of an external resources import as a downloadable CSV file.
 *
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 21.08.2026
 */
@ApplicationScoped
public class ExterneRessourcenImportResultCSVWriter {

  private static final CSVFormat RESULT_FORMAT = CSVFormat.Builder.create()
      .setDelimiter(';')
      .setQuote('"')
      .setRecordSeparator('\n')
      .get();

  public byte[] write(List<ExterneRessourcenImportResult> results, Locale locale)
      throws ExterneRessourcenImportException {

    Objects.requireNonNull(results, "results must not be null");
    Objects.requireNonNull(locale, "locale must not be null");

    try (StringWriter writer = new StringWriter();
        CSVPrinter printer = new CSVPrinter(writer, RESULT_FORMAT)) {

      printer.printRecord(
          getMessage(locale, "kod_externe_ressourcen_import_result_header_uuid"),
          getMessage(locale, "kod_externe_ressourcen_import_result_header_resource"),
          getMessage(locale, "kod_externe_ressourcen_import_result_header_link"),
          getMessage(locale, "kod_externe_ressourcen_import_result_header_status"),
          getMessage(locale, "kod_externe_ressourcen_import_result_header_error"));

      for (ExterneRessourcenImportResult result : results) {
        printer.printRecord(
            StringUtils.defaultString(result.kodUuid()),
            StringUtils.defaultString(result.ressource()),
            StringUtils.defaultString(result.link()),
            getMessage(locale, switch (result.status()) {
              case ERFOLGREICH -> "kod_externe_ressourcen_import_status_success";
              case FEHLER -> "kod_externe_ressourcen_import_status_failed";
            }),
            localizeError(result, locale));
      }

      printer.flush();
      return writer.toString().getBytes(StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new ExterneRessourcenImportException(
          ExterneRessourcenImportErrorMessageKey.RESULT_WRITE_ERROR, exception);
    }
  }

  private String localizeError(ExterneRessourcenImportResult result, Locale locale) {
    if (result.errorMessageKey() == null) {
      return "";
    }

    Object[] messageArgs = result.errorMessageArgs();
    String message = getMessage(
        locale,
        result.errorMessageKey().getKey(),
        messageArgs == null ? new Object[0] : messageArgs);
    return getMessage(
        locale,
        "kod_externe_ressourcen_import_error_with_line",
        result.lineNumber(),
        message);
  }
}
