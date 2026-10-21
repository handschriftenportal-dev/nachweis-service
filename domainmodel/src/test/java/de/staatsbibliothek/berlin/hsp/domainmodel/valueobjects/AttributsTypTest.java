package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AttributsTypTest {

  @Test
  void testGetIndexName() {
    assertEquals("norm_title", AttributsTyp.TITEL.getIndexName());
    assertEquals("norm_material", AttributsTyp.BESCHREIBSTOFF.getIndexName());
    assertEquals("norm_measure", AttributsTyp.UMFANG.getIndexName());
    assertEquals("norm_dimensions", AttributsTyp.ABMESSUNG.getIndexName());
    assertEquals("norm_format", AttributsTyp.FORMAT.getIndexName());
    assertEquals("norm_origPlace", AttributsTyp.ENTSTEHUNGSORT.getIndexName());
    assertEquals("norm_origDate", AttributsTyp.ENTSTEHUNGSZEIT.getIndexName());
    assertEquals("norm_textLang", AttributsTyp.GRUNDSPRACHE.getIndexName());
    assertEquals("norm_form", AttributsTyp.HANDSCHRIFTENTYP.getIndexName());
    assertEquals("norm_status", AttributsTyp.STATUS.getIndexName());
    assertEquals("norm_decoration", AttributsTyp.BUCHSCHMUCK.getIndexName());
    assertEquals("norm_musicNotation", AttributsTyp.MUSIKNOTATION.getIndexName());
  }

}
