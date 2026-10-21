package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.OrteModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 19.06.2020.
 * @version 1.0
 */
public class OrteControllerTest {

  private NormdatenReferenzBoundary normdatenReferenzBoundary = Mockito.mock(NormdatenReferenzBoundary.class);

  private OrteModel orteModel = new OrteModel(normdatenReferenzBoundary);

  @Test
  void creation() {

    OrteController controller = new OrteController(orteModel);

    assertNotNull(controller);
  }
}
