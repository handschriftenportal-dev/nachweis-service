package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.KOD_LOCKED;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.LINK_INVALID;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.SAVE_ERROR;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportResult.Status.ERFOLGREICH;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportResult.Status.FEHLER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

class ExterneRessourcenImportResultCSVWriterTest {

  private static final List<String> HEADER = List.of(
      "UUID-Kulturobjekt", "Ressource", "Link", "Status", "Fehlermeldung");

  private final ExterneRessourcenImportResultCSVWriter writer =
      new ExterneRessourcenImportResultCSVWriter();

  @Test
  void shouldWriteHeaderAndResultsInInputOrder() throws Exception {
    List<ExterneRessourcenImportResult> results = List.of(
        result("HSP-1", "hsc", "https://example.org/1", ERFOLGREICH, null, 2),
        result("HSP-2", "kdih", "https://example.org/2", FEHLER,
            KOD_LOCKED, 3));

    List<CSVRecord> records = parse(writer.write(results, Locale.GERMAN));

    assertEquals(HEADER, List.of(records.getFirst().values()));
    assertEquals(
        List.of("HSP-1", "hsc", "https://example.org/1", "ERFOLGREICH", ""),
        List.of(records.get(1).values()));
    assertEquals(
        List.of("HSP-2", "kdih", "https://example.org/2", "FEHLER",
            "Zeile 3: Das Kulturobjektdokument ist gesperrt und konnte nicht aktualisiert werden."),
        List.of(records.get(2).values()));
  }

  @Test
  void shouldWriteOnlyHeaderForEmptyResults() throws Exception {
    List<CSVRecord> records = parse(writer.write(List.of(), Locale.GERMAN));

    assertEquals(1, records.size());
    assertEquals(HEADER, List.of(records.getFirst().values()));
  }

  @Test
  void shouldWriteUtf8AndEscapeDelimiterAndQuotes() throws Exception {
    ExterneRessourcenImportResult result = result(
        "HSP-Ü", "hsc", "https://example.org", FEHLER,
        SAVE_ERROR, 7,
        "Ungültig; Ursache: \"Test\"");

    byte[] content = writer.write(List.of(result), Locale.GERMAN);
    List<CSVRecord> records = parse(content);

    assertEquals(
        "Zeile 7: Die externe Ressource konnte nicht gespeichert werden: "
            + "Ungültig; Ursache: \"Test\"",
        records.get(1).get(4));
    assertEquals("HSP-Ü", records.get(1).get(0));
  }

  @Test
  void shouldWriteEnglishHeaderStatusAndErrorMessage() throws Exception {
    ExterneRessourcenImportResult result = result(
        "HSP-1", "hsc", "invalid", FEHLER,
        LINK_INVALID, 1);

    List<CSVRecord> englishRecords = parse(writer.write(List.of(result), Locale.ENGLISH));
    List<CSVRecord> germanRecords = parse(writer.write(List.of(result), Locale.GERMAN));

    assertEquals(
        List.of("UUID cultural object", "Resource", "Link", "Status", "Error message"),
        List.of(englishRecords.getFirst().values()));
    assertEquals("ERROR", englishRecords.get(1).get(3));
    assertEquals("Line 1: Link is invalid.", englishRecords.get(1).get(4));
    assertEquals("FEHLER", germanRecords.get(1).get(3));
    assertEquals("Zeile 1: Link ist ungültig.", germanRecords.get(1).get(4));
  }

  private static ExterneRessourcenImportResult result(
      String kodUuid,
      String ressource,
      String link,
      ExterneRessourcenImportResult.Status status,
      ExterneRessourcenImportErrorMessageKey errorMessageKey,
      int lineNumber,
      Object... errorMessageArgs) {

    return new ExterneRessourcenImportResult(
        kodUuid, ressource, link, status, errorMessageKey, errorMessageArgs, lineNumber);
  }

  private static List<CSVRecord> parse(byte[] content) throws IOException {
    CSVFormat format = CSVFormat.Builder.create()
        .setDelimiter(';')
        .setQuote('"')
        .get();
    try (CSVParser parser = format.parse(
        new StringReader(new String(content, StandardCharsets.UTF_8)))) {
      return parser.getRecords();
    }
  }
}
