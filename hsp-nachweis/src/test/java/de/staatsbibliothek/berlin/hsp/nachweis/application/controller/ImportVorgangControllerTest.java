package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.ImportVorgangModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport.ImportVorgangBoundary;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 11.06.2020.
 * @version 1.0
 */
public class ImportVorgangControllerTest {

  private ImportVorgangBoundary importVorgangBoundary = mock(ImportVorgangBoundary.class);
  private BearbeiterBoundary bearbeiterBoundary = mock(BearbeiterBoundary.class);
  private ImportVorgangModel importVorgangModel = new ImportVorgangModel(importVorgangBoundary,
      bearbeiterBoundary);

  @Test
  void testloadAllVorgaenge() {

    ImportVorgangController controller = new ImportVorgangController(importVorgangModel);

    controller.loadAllVorgaenge();

    verify(importVorgangBoundary, times(1)).findAll();

    assertNotNull(controller.getVorgaenge());
  }
}
