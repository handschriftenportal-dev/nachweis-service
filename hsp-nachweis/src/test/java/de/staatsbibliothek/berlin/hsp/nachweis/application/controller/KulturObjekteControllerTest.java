package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KODRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.KulturObjekteModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import org.junit.jupiter.api.Test;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 02.12.20
 */
class KulturObjekteControllerTest {

  KulturObjekteModel kulturObjekteModel = new KulturObjekteModel(
      mock(KulturObjektDokumentBoundary.class),
      mock(KODRechte.class),
      mock(BearbeiterBoundary.class));

  @Test
  void creation() {
    KulturObjekteController controller = new KulturObjekteController(kulturObjekteModel);
    assertNotNull(controller);
  }

}
