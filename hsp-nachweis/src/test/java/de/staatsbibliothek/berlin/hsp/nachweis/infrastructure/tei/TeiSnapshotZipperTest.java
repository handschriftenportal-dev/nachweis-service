package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.tei;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;

class TeiSnapshotZipperTest {

  @Test
  void zip_producesValidZipWithSingleEntry() throws IOException {
    String xml = "<TEI><text>content</text></TEI>";
    byte[] zipped = TeiSnapshotZipper.zip(xml, "BES-1");

    assertNotNull(zipped);
    assertTrue(zipped.length > 0);

    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipped))) {
      ZipEntry entry = zis.getNextEntry();
      assertNotNull(entry);
      assertEquals("tei-BES-1.xml", entry.getName());

      String unpacked = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
      assertEquals(xml, unpacked);

      assertNull(zis.getNextEntry(), "Snapshot must contain exactly one entry");
    }
  }

  @Test
  void zip_preservesUtf8Content() throws IOException {
    String xml = "<TEI>Ümlaute and emoji 📜</TEI>";
    byte[] zipped = TeiSnapshotZipper.zip(xml, "BES-1");

    try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipped))) {
      zis.getNextEntry();
      assertEquals(xml, new String(zis.readAllBytes(), StandardCharsets.UTF_8));
    }
  }

  @Test
  void zip_nullArgumentsRejected() {
    assertThrows(NullPointerException.class, () -> TeiSnapshotZipper.zip(null, "BES-1"));
    assertThrows(NullPointerException.class, () -> TeiSnapshotZipper.zip("<xml/>", null));
  }

  @Test
  void filenameHelpers() {
    assertEquals("tei-BES-1.xml", TeiSnapshotZipper.teiXmlFilename("BES-1"));
    assertEquals("tei-BES-1.zip", TeiSnapshotZipper.teiSnapshotZipFilename("BES-1"));
  }
}
