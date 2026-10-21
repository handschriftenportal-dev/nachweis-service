package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.NonNull;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.mozilla.universalchardet.UniversalDetector;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 01.06.2026
 */

@ApplicationScoped
public class SortkeyImportCSVParser {

  public SortKeyEntries parse(String filename, byte[] content)
      throws KulturObjektSortkeyUpdateException {

    if (content == null || content.length == 0) {
      throw new KulturObjektSortkeyUpdateException("kod_sortkeys_file_empty");
    }

    List<SortKeyEntry> entries = new ArrayList<>();
    List<String> missingOrEmptySortkeyLines = new ArrayList<>();

    for (CsvPayload payload : extractCsvPayloads(filename, content)) {

      checkEncoding(payload.content());
      List<SortKeyEntry> parsedEntries = parseCsv(payload.label(), payload.content());
      for (SortKeyEntry sortKeyReportEntry : parsedEntries) {
        if (StringUtils.isBlank(sortKeyReportEntry.sortkey())) {
          String reportLabel = payload.label() + ":" + sortKeyReportEntry.lineNumber();
          missingOrEmptySortkeyLines.add(reportLabel);
          continue;
        }
        entries.add(sortKeyReportEntry);
      }
    }

    return new SortKeyEntries(entries, missingOrEmptySortkeyLines);
  }

  private List<SortKeyEntry> parseCsv(String source, byte[] content)
      throws KulturObjektSortkeyUpdateException {

    List<SortKeyEntry> entries = new ArrayList<>();

    try (Reader reader =
        new InputStreamReader(new ByteArrayInputStream(content), StandardCharsets.UTF_8)) {

      CSVFormat format = CSVFormat.Builder.create()
          .setDelimiter('$')
          .setQuote('"')
          .get();

      Iterable<CSVRecord> csvRecords = format.parse(reader);

      for (CSVRecord csvRecord : csvRecords) {
        var lineNumber = csvRecord.getRecordNumber();

        if (csvRecord.size() < 2 || csvRecord.get(0).isBlank()) {
          continue;
        }
        if (csvRecord.size() > 2) {
          throw new KulturObjektSortkeyUpdateException(
              "kod.sortkeys.format.invalid",
              source,
              String.valueOf(lineNumber)
          );
        }

        String uuid = csvRecord.get(0) != null ? csvRecord.get(0).trim() : "";
        String sortkey = csvRecord.get(1) != null ? csvRecord.get(1).trim() : "";

        entries.add(new SortKeyEntry(uuid, sortkey, lineNumber));
      }

    } catch (IOException exception) {
      throw new KulturObjektSortkeyUpdateException(
          "kod.sortkeys.csv.read.error",
          exception,
          source
      );
    }

    return entries;
  }

  private List<CsvPayload> extractCsvPayloads(String filename, byte[] content)
      throws KulturObjektSortkeyUpdateException {

    if (filename.toLowerCase(Locale.ROOT).endsWith(".zip")) {

      return handleZipfFile(content);
    }

    return List.of(new CsvPayload(filename, content));
  }

  private @NonNull List<CsvPayload> handleZipfFile(byte[] content)
      throws KulturObjektSortkeyUpdateException {
    List<CsvPayload> csvPayloads = new ArrayList<>();

    try (ZipInputStream zipInputStream =
        new ZipInputStream(new ByteArrayInputStream(content))) {

      ZipEntry zipEntry;
      while ((zipEntry = zipInputStream.getNextEntry()) != null) {
        if (zipEntry.isDirectory()
            || !zipEntry.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
          continue;
        }
        csvPayloads.add(new CsvPayload(zipEntry.getName(), readZipEntry(zipInputStream)));
      }

    } catch (IOException exception) {
      throw new KulturObjektSortkeyUpdateException(
          "kod.sortkeys.zip.read.error",
          exception
      );
    }

    if (csvPayloads.isEmpty()) {
      throw new KulturObjektSortkeyUpdateException("kod.sortkeys.zip.no.csv");
    }

    return csvPayloads;
  }

  private byte[] readZipEntry(ZipInputStream zipInputStream) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    int read;

    while ((read = zipInputStream.read(buffer)) != -1) {
      outputStream.write(buffer, 0, read);
    }

    return outputStream.toByteArray();
  }

  private void checkEncoding(byte[] data)
      throws KulturObjektSortkeyUpdateException {

    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {

      String encoding = UniversalDetector.detectCharset(inputStream);

      if (encoding != null &&
          !encoding.equalsIgnoreCase("UTF-8") &&
          !encoding.equalsIgnoreCase("US-ASCII")) {
        throw new KulturObjektSortkeyUpdateException(
            "kod.sortkeys.encoding.invalid",
            encoding
        );
      }

    } catch (IOException exception) {
      throw new KulturObjektSortkeyUpdateException(
          "kod.sortkeys.encoding.error",
          exception
      );
    }
  }

  private record CsvPayload(String label, byte[] content) {

  }

}
