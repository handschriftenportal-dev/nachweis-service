package de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkeyupdate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.KulturObjektSortkeyUpdateException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.sortkey.SortkeyImportCSVParser;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;

class SortkeyUpdateImportParserTest {

  private final SortkeyImportCSVParser parser = new SortkeyImportCSVParser();

  @Test
  void shouldParseValidCsv() throws Exception {

    String csv = """
        "550e8400-e29b-41d4-a716-446655440000"$"Cbm_Cat_1"
        """;

    var result = parser.parse("test.csv", csv.getBytes(StandardCharsets.UTF_8));

    assertEquals(1, result.entries().size());

    var entry = result.entries().getFirst();
    assertEquals("550e8400-e29b-41d4-a716-446655440000", entry.uuid());
    assertEquals("Cbm_Cat_1", entry.sortkey());

    assertTrue(result.missingOrEmptySortkeyLines().isEmpty());
  }

  @Test
  void shouldSkipRowWhenUuidIsBlank() throws Exception {

    String csv = """
        ""$"Cbm_Cat_1"
        """;

    var result = parser.parse("test.csv", csv.getBytes(StandardCharsets.UTF_8));

    assertEquals(0, result.entries().size());

    assertTrue(result.missingOrEmptySortkeyLines().isEmpty());
  }

  @Test
  void shouldThrowExceptionOnEmptyCsvContent() {

    String csv = """
        """;

    assertThrows(
        KulturObjektSortkeyUpdateException.class,
        () -> parser.parse("test.csv", csv.getBytes(StandardCharsets.UTF_8))
    );
  }

  @Test
  void shouldThrowExceptionOnInvalidFormat() {

    String csv = """
        "uuid"$"key"$"extra"
        """;

    assertThrows(
        KulturObjektSortkeyUpdateException.class,
        () -> parser.parse("test.csv", csv.getBytes(StandardCharsets.UTF_8))
    );
  }

  @Test
  void shouldCollectMissingSortkey() throws Exception {

    String csv = """
        "550e8400-e29b-41d4-a716-446655440000"$""
        """;

    var result = parser.parse("test.csv", csv.getBytes(StandardCharsets.UTF_8));

    assertEquals(0, result.entries().size());
    assertEquals(1, result.missingOrEmptySortkeyLines().size());
  }

  @Test
  void shouldThrowExceptionOnEmptyFile() {

    byte[] empty = new byte[0];

    assertThrows(
        KulturObjektSortkeyUpdateException.class,
        () -> parser.parse("test.csv", empty)
    );
  }

  @Test
  void shouldParseZipWithCsv() throws Exception {

    String csv = """
        "550e8400-e29b-41d4-a716-446655440000"$"Cbm_Cat_1"
        """;

    byte[] zipBytes = createZip("file.csv", csv);

    var result = parser.parse("test.zip", zipBytes);

    assertEquals(1, result.entries().size());
  }

  @Test
  void shouldThrowExceptionWhenZipHasNoCsv() {

    byte[] zipBytes = createZip("file.txt", "text");

    assertThrows(
        KulturObjektSortkeyUpdateException.class,
        () -> parser.parse("test.zip", zipBytes)
    );
  }

  private byte[] createZip(String fileName, String content) {
    try {
      ByteArrayOutputStream base = new ByteArrayOutputStream();
      ZipOutputStream zos = new ZipOutputStream(base);

      zos.putNextEntry(new ZipEntry(fileName));
      zos.write(content.getBytes(StandardCharsets.UTF_8));
      zos.closeEntry();
      zos.close();

      return base.toByteArray();

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
