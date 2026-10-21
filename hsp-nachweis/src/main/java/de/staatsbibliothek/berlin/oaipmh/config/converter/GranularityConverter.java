package de.staatsbibliothek.berlin.oaipmh.config.converter;

import io.gdcc.xoai.model.oaipmh.Granularity;
import java.util.Objects;

/**
 * Converts String value in configuration to Granularity.
 *
 * @see Granularity
 */
public class GranularityConverter {

  private GranularityConverter() {}

  public static Granularity convert(String s) {
    if (Objects.isNull(s)) {
      return Granularity.Lenient;
    }
    return switch (s.toLowerCase()) {
      case "second", "seconds" -> Granularity.Second;
      case "day", "days" -> Granularity.Day;
      default -> Granularity.Lenient;
    };
  }
}
