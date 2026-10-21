package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.tei;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Packs a single TEI-XML document into a ZIP byte array. Used by the publication approval flow to
 * freeze the TEI at the moment of publication (Ticket 23861).
 */
public final class TeiSnapshotZipper {

  private static final String PREFIX = "tei-";

  private TeiSnapshotZipper() {
  }

  public static String teiXmlFilename(String beschreibungId) {
    return PREFIX + beschreibungId + ".xml";
  }

  public static String teiSnapshotZipFilename(String beschreibungId) {
    return PREFIX + beschreibungId + ".zip";
  }

  public static byte[] zip(String teiXml, String beschreibungId) {
    Objects.requireNonNull(teiXml, "teiXml must not be null");
    Objects.requireNonNull(beschreibungId, "beschreibungId must not be null");

    byte[] xmlBytes = teiXml.getBytes(StandardCharsets.UTF_8);
    ByteArrayOutputStream out = new ByteArrayOutputStream(xmlBytes.length);
    try (ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
      ZipEntry entry = new ZipEntry(teiXmlFilename(beschreibungId));
      zip.putNextEntry(entry);
      zip.write(xmlBytes);
      zip.closeEntry();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to create TEI ZIP", e);
    }
    return out.toByteArray();
  }
}
