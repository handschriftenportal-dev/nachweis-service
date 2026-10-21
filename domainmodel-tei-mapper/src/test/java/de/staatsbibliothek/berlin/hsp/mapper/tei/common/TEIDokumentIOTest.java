package de.staatsbibliothek.berlin.hsp.mapper.tei.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.mapper.tei.TEICommandException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.TEI;

class TEIDokumentIOTest {

  private static final Path TEI_FILE_PATH =
      Paths.get("src", "test", "resources", "tei", "tei-HSP-kodms1.xml");

  private static String readFixture() throws IOException {
    return Files.readString(TEI_FILE_PATH, StandardCharsets.UTF_8);
  }

  @Test
  void shouldUnmarshalTeiXml() throws IOException {
    TEI tei = TEIDokumentIO.unmarshal(readFixture());

    assertNotNull(tei);
    assertNotNull(tei.getTeiHeader());
  }

  @Test
  void shouldRoundTripWithoutLosingTheDocument() throws IOException {
    String teiXML = readFixture();

    String marshalled = TEIDokumentIO.marshal(TEIDokumentIO.unmarshal(teiXML));

    assertTrue(marshalled.contains("<TEI"), marshalled);
    assertEquals(
        marshalled,
        TEIDokumentIO.marshal(TEIDokumentIO.unmarshal(marshalled)),
        "a second round trip must not change the XML any further");
  }

  @Test
  void shouldRejectUnparsableTeiXml() {
    TEICommandException exception = assertThrows(TEICommandException.class,
        () -> TEIDokumentIO.unmarshal("Kein TEI XML"));

    assertEquals("Unable to read TEI-XML", exception.getMessage());
  }

  @Test
  void shouldRejectXmlWithoutTeiElement() {
    assertThrows(TEICommandException.class,
        () -> TEIDokumentIO.unmarshal("<?xml version=\"1.0\"?><root/>"));
  }

  @Test
  void shouldRejectNullArguments() {
    NullPointerException unmarshalNpe = assertThrows(NullPointerException.class,
        () -> TEIDokumentIO.unmarshal(null));
    assertEquals("teiXML is required", unmarshalNpe.getMessage());

    NullPointerException marshalNpe = assertThrows(NullPointerException.class,
        () -> TEIDokumentIO.marshal(null));
    assertEquals("tei is required", marshalNpe.getMessage());
  }
}
