package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.SpracheModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konstantin.goerlitz@sbb.spk-berlin.de
 */
public class SpracheControllerTest {

  private NormdatenReferenzBoundary normdatenReferenzBoundary = Mockito.mock(NormdatenReferenzBoundary.class);

  private SpracheModel spracheModel = new SpracheModel(normdatenReferenzBoundary);

  @Test
  void creation() {

    SpracheController controller = new SpracheController(spracheModel);

    assertNotNull(controller);
  }
}
