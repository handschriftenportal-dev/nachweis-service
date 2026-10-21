package de.staatsbibliothek.berlin.oaipmh.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.staatsbibliothek.berlin.oaipmh.config.converter.GranularityConverter;
import io.gdcc.xoai.model.oaipmh.Granularity;
import org.junit.jupiter.api.Test;

class GranularityConverterTest {

  @Test
  void testConvert() {
    assertEquals(Granularity.Second, GranularityConverter.convert("second"));
    assertEquals(Granularity.Second, GranularityConverter.convert("seconds"));
    assertEquals(Granularity.Day, GranularityConverter.convert("day"));
    assertEquals(Granularity.Day, GranularityConverter.convert("days"));
    assertEquals(Granularity.Lenient, GranularityConverter.convert("lenient"));
    assertEquals(Granularity.Lenient, GranularityConverter.convert("default"));
    assertEquals(Granularity.Lenient, GranularityConverter.convert("random12123"));
  }
}
