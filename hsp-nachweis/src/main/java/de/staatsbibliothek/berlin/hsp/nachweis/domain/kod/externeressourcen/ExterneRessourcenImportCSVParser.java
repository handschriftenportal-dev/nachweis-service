package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.ENCODING_INVALID;
import static de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.externeressourcen.ExterneRessourcenImportErrorMessageKey.FILE_EMPTY;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 18.08.2026
 */
@ApplicationScoped
public class ExterneRessourcenImportCSVParser {

  private static final int COLUMN_COUNT = 7;
  private static final Pattern ROW_PATTERN = Pattern.compile(
      "^\"(?:[^\"]|\"\")*\"(?:\\$\"(?:[^\"]|\"\")*\"){6}$");

  private static final CSVFormat CSV_FORMAT = CSVFormat.Builder.create()
      .setDelimiter('$')
      .setQuote('"')
      .get();

  public void parse(
      byte[] content,
      Consumer<ExterneRessourcenImportEntry> validEntryConsumer,
      IntConsumer invalidRowConsumer)
      throws ExterneRessourcenImportException {

    if (content == null || content.length == 0) {
      throw new ExterneRessourcenImportException(FILE_EMPTY);
    }

    String csvContent = decodeUtf8(content);
    if (csvContent.startsWith("\uFEFF")) {
      csvContent = csvContent.substring(1);
    }

    Iterator<String> lines = csvContent.lines().iterator();
    int lineNumber = 0;
    while (lines.hasNext()) {
      lineNumber++;
      String line = lines.next();
      if (StringUtils.isBlank(line)) {
        continue;
      }
      parseLine(line, lineNumber, validEntryConsumer, invalidRowConsumer);
    }
  }

  private String decodeUtf8(byte[] content) throws ExterneRessourcenImportException {
    try {
      return StandardCharsets.UTF_8.newDecoder()
          .onMalformedInput(CodingErrorAction.REPORT)
          .onUnmappableCharacter(CodingErrorAction.REPORT)
          .decode(ByteBuffer.wrap(content))
          .toString();
    } catch (CharacterCodingException exception) {
      throw new ExterneRessourcenImportException(
          ENCODING_INVALID, exception);
    }
  }

  private void parseLine(
      String line,
      int lineNumber,
      Consumer<ExterneRessourcenImportEntry> validEntryConsumer,
      IntConsumer invalidRowConsumer) {

    if (!ROW_PATTERN.matcher(line).matches()) {
      invalidRowConsumer.accept(lineNumber);
      return;
    }

    ExterneRessourcenImportEntry entry;
    try {
      CSVRecord record = parseRecord(line);
      if (record.size() != COLUMN_COUNT) {
        invalidRowConsumer.accept(lineNumber);
        return;
      }
      entry = toEntry(record, lineNumber);
    } catch (IOException | UncheckedIOException exception) {
      invalidRowConsumer.accept(lineNumber);
      return;
    }

    validEntryConsumer.accept(entry);
  }

  private CSVRecord parseRecord(String line) throws IOException {
    try (CSVParser parser = CSV_FORMAT.parse(new StringReader(line))) {
      List<CSVRecord> records = parser.getRecords();
      if (records.size() != 1) {
        throw new IOException("Expected exactly one CSV record");
      }
      return records.getFirst();
    }
  }

  private ExterneRessourcenImportEntry toEntry(CSVRecord record, int lineNumber) {
    return ExterneRessourcenImportEntry.builder()
        .aufbewahrungsort(StringUtils.trimToEmpty(record.get(0)))
        .besitzendeEinrichtung(StringUtils.trimToEmpty(record.get(1)))
        .fuehrendeSignatur(StringUtils.trimToEmpty(record.get(2)))
        .kodUuid(StringUtils.trimToEmpty(record.get(3)))
        .resolverAdresse(StringUtils.trimToEmpty(record.get(4)))
        .ressource(StringUtils.trimToEmpty(record.get(5)))
        .link(StringUtils.trimToEmpty(record.get(6)))
        .lineNumber(lineNumber)
        .build();
  }
}
