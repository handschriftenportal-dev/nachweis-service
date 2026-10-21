package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.KoerperschaftModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 13.08.2020.
 * @version 1.0
 */
public class KoerperschaftControllerTest {

  NormdatenReferenzBoundary normdatenReferenzBoundary = Mockito.mock(NormdatenReferenzBoundary.class);

  KoerperschaftModel koerperschaftModel = new KoerperschaftModel(normdatenReferenzBoundary);

  @Test
  void creation() {
    KoerperschaftController controller = new KoerperschaftController(koerperschaftModel);
    assertNotNull(controller);
  }

}
