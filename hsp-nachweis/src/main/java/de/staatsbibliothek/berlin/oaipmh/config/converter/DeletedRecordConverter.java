package de.staatsbibliothek.berlin.oaipmh.config.converter;

import io.gdcc.xoai.model.oaipmh.DeletedRecord;
import java.util.Objects;

/**
 * Converts String value in configuration to DeletedRecord.
 *
 * @see DeletedRecord
 */
public class DeletedRecordConverter {

  private DeletedRecordConverter() {}

  public static DeletedRecord convert(String s) {
    Objects.requireNonNull(s);
    return switch (s.toLowerCase()) {
      case "transient" -> DeletedRecord.TRANSIENT;
      case "persistent" -> DeletedRecord.PERSISTENT;
      default -> DeletedRecord.NO;
    };
  }
}
