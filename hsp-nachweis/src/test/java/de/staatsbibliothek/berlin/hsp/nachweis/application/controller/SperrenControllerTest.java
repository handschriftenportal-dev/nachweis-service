package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.SperrenModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 */
public class SperrenControllerTest {

  private DokumentSperreBoundary dokumentSperreBoundary = Mockito.mock(DokumentSperreBoundary.class);
  private KulturObjektDokumentBoundary kulturObjektDokumentBoundary = Mockito.mock(KulturObjektDokumentBoundary.class);
  private BeschreibungsBoundary beschreibungsBoundary = Mockito.mock(BeschreibungsBoundary.class);

  private SperrenModel sperrenModel = new SperrenModel(dokumentSperreBoundary, kulturObjektDokumentBoundary,
      beschreibungsBoundary);


  @Test
  void creation() {

    SperrenController controller = new SperrenController(sperrenModel);

    assertNotNull(controller);
  }
}
