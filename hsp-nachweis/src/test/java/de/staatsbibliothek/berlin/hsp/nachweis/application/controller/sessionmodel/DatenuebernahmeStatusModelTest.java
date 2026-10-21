package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewModel.ImportViewModelBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import org.junit.jupiter.api.Test;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 09.06.21
 */
class DatenuebernahmeStatusModelTest {

  DatenuebernahmeStatusModel datenuebernahmeStatusModel = new DatenuebernahmeStatusModel();

  @Test
  void testAddActiveJob() {
    String id = "testAddActiveJob";
    assertFalse(datenuebernahmeStatusModel.getActiveJobs().contains(id));
    datenuebernahmeStatusModel.addActiveJob(id);
    assertTrue(datenuebernahmeStatusModel.getActiveJobs().contains(id));
    datenuebernahmeStatusModel.removeActiveJob(id);
  }

  @Test
  void testRemoveActiveJob() {
    String id = "testRemoveActiveJob";
    datenuebernahmeStatusModel.addActiveJob(id);
    assertTrue(datenuebernahmeStatusModel.getActiveJobs().contains(id));
    datenuebernahmeStatusModel.removeActiveJob(id);
    assertFalse(datenuebernahmeStatusModel.getActiveJobs().contains(id));
  }

  @Test
  void testGetStatusForImportJob() {
    String id = "testGetStatusForImportJob";
    ImportViewModel testImportViewModel = new ImportViewModelBuilder().withId(id).withImportStatus(ImportStatus.OFFEN)
        .build();
    assertEquals(ImportStatus.OFFEN, datenuebernahmeStatusModel.getStatusForImportJob(testImportViewModel));
    datenuebernahmeStatusModel.addActiveJob(id);
    assertEquals(ImportStatus.IN_UEBERNAHME, datenuebernahmeStatusModel.getStatusForImportJob(testImportViewModel));
    datenuebernahmeStatusModel.removeActiveJob(id);
  }

  @Test
  void testGetActiveJobs() {
    String id = "testGetActiveJobs";
    datenuebernahmeStatusModel.addActiveJob(id);
    assertTrue(datenuebernahmeStatusModel.getActiveJobs().contains(id));
    assertTrue(datenuebernahmeStatusModel.getActiveJobs().contains(id));

    datenuebernahmeStatusModel.removeActiveJob(id);
  }
}
