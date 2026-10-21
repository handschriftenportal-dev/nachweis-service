package de.staatsbibliothek.berlin.oaipmh.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.staatsbibliothek.berlin.oaipmh.config.converter.EarliestDatestampConverter;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class InstantConverterTest {

  @Test
  void testConvert() {
    assertEquals(
        EarliestDatestampConverter.convert("2021-01-01T00:00:00Z"),
        Instant.parse("2021-01-01T00:00:00Z"));
    assertThrows(
        IllegalArgumentException.class,
        () -> EarliestDatestampConverter.convert("2021-01-01T00:00:00"));
    assertThrows(IllegalArgumentException.class, () -> EarliestDatestampConverter.convert("test"));
    assertThrows(NullPointerException.class, () -> EarliestDatestampConverter.convert(null));
  }
}
