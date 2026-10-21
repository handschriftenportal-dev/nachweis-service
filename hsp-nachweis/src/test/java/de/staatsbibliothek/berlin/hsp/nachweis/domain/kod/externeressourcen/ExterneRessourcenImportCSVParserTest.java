package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.FILE_EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ExterneRessourcenImportCSVParserTest {

  private final ExterneRessourcenImportCSVParser parser =
      new ExterneRessourcenImportCSVParser();

  @Test
  void shouldParseValidCsv() throws Exception {
    String csv = """
        "Berlin"$"Staatsbibliothek zu Berlin"$"Ms. germ. fol. 1"$"HSP-d1956e88-612b-33bc-8772-91e9b12f893b"$"https://resolver.staatsbibliothek-berlin.de/HSP00035FC400000000"$"HSC"$"https://www.handschriftencensus.de/4374"
        """;

    ParseOutcome outcome = parse(csv);

    assertEquals(1, outcome.entries().size());
    assertEquals(List.of(), outcome.invalidLineNumbers());
    ExterneRessourcenImportEntry entry = outcome.entries().getFirst();
    assertEquals("Berlin", entry.aufbewahrungsort());
    assertEquals("Staatsbibliothek zu Berlin", entry.besitzendeEinrichtung());
    assertEquals("Ms. germ. fol. 1", entry.fuehrendeSignatur());
    assertEquals("HSP-d1956e88-612b-33bc-8772-91e9b12f893b", entry.kodUuid());
    assertEquals(
        "https://resolver.staatsbibliothek-berlin.de/HSP00035FC400000000",
        entry.resolverAdresse());
    assertEquals("HSC", entry.ressource());
    assertEquals("https://www.handschriftencensus.de/4374", entry.link());
    assertEquals(1, entry.lineNumber());
  }

  @Test
  void shouldParseEntryContainingOnlyResolverResourceAndLink() throws Exception {
    String csv = """
        ""$""$""$""$"https://resolver.staatsbibliothek-berlin.de/HSP00034C0C00000000"$"kdih"$"https://kdih.badw.de/datenbank/handschrift/101/0/2"
        """;

    ExterneRessourcenImportEntry entry = parse(csv).entries().getFirst();

    assertEquals("", entry.aufbewahrungsort());
    assertEquals("", entry.besitzendeEinrichtung());
    assertEquals("", entry.fuehrendeSignatur());
    assertEquals("", entry.kodUuid());
    assertEquals(
        "https://resolver.staatsbibliothek-berlin.de/HSP00034C0C00000000",
        entry.resolverAdresse());
    assertEquals("kdih", entry.ressource());
    assertEquals("https://kdih.badw.de/datenbank/handschrift/101/0/2", entry.link());
  }

  @Test
  void shouldIgnoreEmptyLinesAndKeepPhysicalLineNumber() throws Exception {
    String csv = "\n\n" + """
        ""$""$""$""$"resolver"$"hsc"$"https://example.org"
        """;

    ExterneRessourcenImportEntry entry = parse(csv).entries().getFirst();

    assertEquals(3, entry.lineNumber());
  }

  @Test
  void shouldTrimValues() throws Exception {
    String csv = """
        " Berlin "$" Einrichtung "$" Signatur "$" UUID "$" Resolver "$" HSC "$" https://example.org/1 "
        """;

    ExterneRessourcenImportEntry entry = parse(csv).entries().getFirst();

    assertEquals("Berlin", entry.aufbewahrungsort());
    assertEquals("HSC", entry.ressource());
    assertEquals("https://example.org/1", entry.link());
  }

  @Test
  void shouldAcceptUtf8Bom() throws Exception {
    String csv = "\uFEFF" + """
        ""$""$""$""$"resolver"$"hsc"$"https://example.org"
        """;

    assertEquals(1, parse(csv).entries().size());
  }

  @Test
  void shouldRejectEmptyFile() {
    ExterneRessourcenImportException exception = assertThrows(
        ExterneRessourcenImportException.class,
        () -> parse(""));

    assertEquals(FILE_EMPTY, exception.getMessageKey());
  }

  @ParameterizedTest
  @MethodSource("invalidRows")
  void shouldReportStructurallyInvalidRow(String row) throws Exception {
    ParseOutcome outcome = parse(row);

    assertEquals(List.of(), outcome.entries());
    assertEquals(List.of(1), outcome.invalidLineNumbers());
  }

  @Test
  void shouldContinueAfterStructurallyInvalidRow() throws Exception {
    String validRow =
        "\"\"$\"\"$\"\"$\"\"$\"resolver\"$\"hsc\"$\"https://example.org\"";
    String csv = validRow + "\n" + "invalid" + "\n" + validRow;

    ParseOutcome outcome = parse(csv);

    assertEquals(List.of(1, 3),
        outcome.entries().stream().map(ExterneRessourcenImportEntry::lineNumber).toList());
    assertEquals(List.of(2), outcome.invalidLineNumbers());
  }

  @Test
  void shouldParseEscapedQuote() throws Exception {
    String csv = """
        "Berlin"$"Staatsbibliothek ""Haus 2\"""$"Ms. 1"$"UUID"$"resolver"$"hsc"$"https://example.org"
        """;

    ExterneRessourcenImportEntry entry = parse(csv).entries().getFirst();

    assertEquals("Staatsbibliothek \"Haus 2\"", entry.besitzendeEinrichtung());
  }

  private static Stream<String> invalidRows() {
    return Stream.of(
        "\"\"$\"\"$\"\"$\"\"$\"hsc\"$\"https://example.org\"",
        "\"\"$\"\"$\"\"$\"\"$\"resolver\"$\"hsc\"$\"https://example.org\"$\"extra\"",
        "\"\"$\"\"$\"\"$\"\"$\"resolver\"$\"hsc\"$\"https://example.org",
        "\"\"$\"\"$\"\"$\"\"$resolver$\"hsc\"$\"https://example.org\"",
        "\"\"$\"\"$\"\"$\"\"\"resolver\"$\"hsc\"$\"https://example.org\"");
  }

  private ParseOutcome parse(String csv) throws Exception {
    List<ExterneRessourcenImportEntry> entries = new ArrayList<>();
    List<Integer> invalidLineNumbers = new ArrayList<>();
    parser.parse(
        csv.getBytes(StandardCharsets.UTF_8),
        entries::add,
        invalidLineNumbers::add);
    return new ParseOutcome(List.copyOf(entries), List.copyOf(invalidLineNumbers));
  }

  private record ParseOutcome(
      List<ExterneRessourcenImportEntry> entries,
      List<Integer> invalidLineNumbers) {
  }
}
