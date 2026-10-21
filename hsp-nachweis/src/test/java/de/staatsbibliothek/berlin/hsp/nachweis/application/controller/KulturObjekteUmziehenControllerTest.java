package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.KulturObjekteModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturobjektDokumentUmziehenErgebnis;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 27.02.24.
 * @version 1.0
 */

public class KulturObjekteUmziehenControllerTest {

  KulturObjekteUmziehenController controller;

  @Test
  void testControllerSetup() {
    KulturObjekteModel kulturObjekteModel = mock(KulturObjekteModel.class);
    KulturObjektDokumentBoundary kulturObjektDokumentBoundary = mock(
        KulturObjektDokumentBoundary.class);
    NormdatenReferenzBoundary normdatenReferenzBoundary = mock(NormdatenReferenzBoundary.class);
    BearbeiterBoundary bearbeiterBoundary = mock(BearbeiterBoundary.class);

    controller = new KulturObjekteUmziehenController(kulturObjekteModel,
        kulturObjektDokumentBoundary, normdatenReferenzBoundary, bearbeiterBoundary);

    assertNotNull(controller);
    assertNotNull(controller.getKoerperschaftViewModelMap());
    assertNotNull(controller.getKulturObjektDokumentBoundary());
    assertNotNull(controller.getNormdatenReferenzBoundary());
  }

  @Test
  void testexecuteKODUmziehenPruefung() {

    KulturObjekteModel kulturObjekteModel = mock(KulturObjekteModel.class);
    KulturObjektDokumentBoundary kulturObjektDokumentBoundary = mock(
        KulturObjektDokumentBoundary.class);
    NormdatenReferenzBoundary normdatenReferenzBoundary = mock(NormdatenReferenzBoundary.class);
    BearbeiterBoundary bearbeiterBoundary = mock(BearbeiterBoundary.class);

    controller = new KulturObjekteUmziehenController(kulturObjekteModel,
        kulturObjektDokumentBoundary, normdatenReferenzBoundary, bearbeiterBoundary);

    assertNotNull(controller);

    controller.executeKODUmziehenPruefung();

    verify(kulturObjektDokumentBoundary, times(1))
        .pruefeKODUmziehen(anyMap(), isNull(), isNull());
  }

  @Test
  void testKODUmziehenExecution() throws Exception {
    KulturObjekteModel kulturObjekteModel = mock(KulturObjekteModel.class);
    KulturObjektDokumentBoundary kulturObjektDokumentBoundary = mock(
        KulturObjektDokumentBoundary.class);
    NormdatenReferenzBoundary normdatenReferenzBoundary = mock(NormdatenReferenzBoundary.class);
    BearbeiterBoundary bearbeiterBoundary = mock(BearbeiterBoundary.class);

    Bearbeiter bearbeiter = new Bearbeiter("b-ke101", "Konrad Eichstaedt");
    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);

    controller = new KulturObjekteUmziehenController(kulturObjekteModel,
        kulturObjektDokumentBoundary, normdatenReferenzBoundary, bearbeiterBoundary);

    assertNotNull(controller);

    List<KulturobjektDokumentUmziehenErgebnis> kodUmziehenErgebnis = new ArrayList<>();
    controller.setKodUmziehenErgebnis(kodUmziehenErgebnis);

    controller.kodUmziehen();

    verify(kulturObjektDokumentBoundary, times(1)).umziehen(kodUmziehenErgebnis, bearbeiter);

    verify(kulturObjekteModel, times(1)).reloadTableRecords();
  }

  @Test
  void testplaceChangedListener() {
    KulturObjekteModel kulturObjekteModel = mock(KulturObjekteModel.class);
    KulturObjektDokumentBoundary kulturObjektDokumentBoundary = mock(
        KulturObjektDokumentBoundary.class);
    NormdatenReferenzBoundary normdatenReferenzBoundary = mock(NormdatenReferenzBoundary.class);
    BearbeiterBoundary bearbeiterBoundary = mock(BearbeiterBoundary.class);

    controller = new KulturObjekteUmziehenController(kulturObjekteModel,
        kulturObjektDokumentBoundary, normdatenReferenzBoundary, bearbeiterBoundary);

    assertNotNull(controller);
    controller.setNeuerOrt(new NormdatenReferenz("1", "Berlin", "GND1"));

    controller.placeChangedListener();

    verify(normdatenReferenzBoundary, times(1))
        .findKoerperschaftenByOrtId("1", false);
  }
}
