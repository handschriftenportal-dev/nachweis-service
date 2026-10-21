package de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 29.05.2020.
 * @version 1.0
 */
public class BeschreibungImportDateiTest {

  @Test
  void testCreation() {

    ImportUploadDatei importUploadDatei = new ImportUploadDatei("test.xml", "Test".getBytes(), "text/xml");

    assertEquals("test.xml", importUploadDatei.getName());
    assertEquals("Test".getBytes().length, importUploadDatei.getContent().length);
    assertEquals("text/xml", importUploadDatei.getContentType());
  }
}
