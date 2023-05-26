/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
