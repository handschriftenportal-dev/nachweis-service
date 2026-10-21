package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.datatype;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.BeschreibungModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import org.junit.jupiter.api.Test;

class BeschreibungsDataTypeViewControllerTest {

  private BeschreibungModel model = new BeschreibungModel(
      mock(BeschreibungsBoundary.class),
      mock(BeschreibungsRechte.class),
      mock(BearbeiterBoundary.class));

  @Test
  void creation() {
    BeschreibungsDataTypeViewController controller = new BeschreibungsDataTypeViewController(model);
    assertNotNull(controller);
  }

}
