package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.ImportVorgangModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport.ImportVorgangBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport.ImportVorgangException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportVorgang;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ImportVorgangModelTest {

  @Test
  public void testSelectedImportVorgangDelete() {
    ImportVorgangBoundary importVorgangBoundary = Mockito.mock(ImportVorgangBoundary.class);
    ImportVorgang importVorgang = new ImportVorgang("123", "Munhal", "MMM", LocalDateTime.now(),
        IMPORTJOB_RESULT_VALUES.FAILED, "fehler", "test");
    ImportVorgangModel importVorgangModel = new ImportVorgangModel(importVorgangBoundary, null);
    List<ImportVorgang> selectedVorgaenge = List.of(importVorgang);
    importVorgangModel.setSelectedVorgaenge(selectedVorgaenge);
    try {
      importVorgangModel.deleteSelectedVorgange();
      Assertions.assertNull(importVorgangModel.getSelectedVorgaenge());
      Mockito.verify(importVorgangBoundary, Mockito.times(1))
          .deleteVorgaenge(selectedVorgaenge);
    } catch (ImportVorgangException exception) {
      Assertions.fail(
          "Unerwartete Ausnahme beim Löschen der ausgewählten Vorgänge: " + exception.getMessage());
    }
  }
}
