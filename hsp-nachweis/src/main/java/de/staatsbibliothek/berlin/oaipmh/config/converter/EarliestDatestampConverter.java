package de.staatsbibliothek.berlin.oaipmh.config.converter;

import java.time.Instant;
import java.util.Objects;

/**
 * Converts String value in configuration to Instant object.
 *
 * @see Instant
 */
public class EarliestDatestampConverter {

  private EarliestDatestampConverter() {}

  public static Instant convert(String s) {
    Objects.requireNonNull(s);
    try {
      return Instant.parse(s);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid Instant format: " + s);
    }
  }
}
