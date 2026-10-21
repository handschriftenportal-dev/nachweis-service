package de.staatsbibliothek.berlin.oaipmh.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.staatsbibliothek.berlin.oaipmh.config.converter.DeletedRecordConverter;
import io.gdcc.xoai.model.oaipmh.DeletedRecord;
import org.junit.jupiter.api.Test;

class DeletedRecordConverterTest {

  @Test
  void testConvert() {
    assertEquals(DeletedRecord.TRANSIENT, DeletedRecordConverter.convert("transient"));
    assertEquals(DeletedRecord.PERSISTENT, DeletedRecordConverter.convert("persistent"));
    assertEquals(DeletedRecord.NO, DeletedRecordConverter.convert("no"));
    assertEquals(DeletedRecord.NO, DeletedRecordConverter.convert(""));
    assertEquals(DeletedRecord.NO, DeletedRecordConverter.convert("65r6cvyj"));
    assertThrows(NullPointerException.class, () -> DeletedRecordConverter.convert(null));
  }
}
