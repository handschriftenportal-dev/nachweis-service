package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.KulturObjektRegistrierenModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 02.07.21
 */
public class KulturObjektRegistrierenControllerTest {

  private final KulturObjektDokumentBoundary kulturObjektDokumentBoundary = mock(KulturObjektDokumentBoundary.class);
  private final NormdatenReferenzBoundary normdatenReferenzBoundary = mock(NormdatenReferenzBoundary.class);
  private final BearbeiterBoundary bearbeiterBoundary = mock(BearbeiterBoundary.class);

  @Test
  void testConstruction() {
    KulturObjektRegistrierenModel dataModel = new KulturObjektRegistrierenModel(
        kulturObjektDokumentBoundary, bearbeiterBoundary, normdatenReferenzBoundary);
    KulturObjektRegistrierenController kulturObjektRegistrierenController
        = new KulturObjektRegistrierenController(normdatenReferenzBoundary, dataModel);

    assertNotNull(kulturObjektRegistrierenController.getKoerperschaftViewModelMap());
  }

  @Test
  void testPlaceChangedListener() {
    KulturObjektRegistrierenModel dataModel = new KulturObjektRegistrierenModel(
        kulturObjektDokumentBoundary, bearbeiterBoundary, normdatenReferenzBoundary);
    KulturObjektRegistrierenController kulturObjektRegistrierenController
        = new KulturObjektRegistrierenController(normdatenReferenzBoundary, dataModel);

    NormdatenReferenz ort = buildNormDatenOrt();
    NormdatenReferenz koerperschaft = buildNormdatenKoerperschaft();

    dataModel.setOrteViewModel(ort);

    when(normdatenReferenzBoundary.findKoerperschaftenByOrtId(ort.getId(), false))
        .thenReturn(Set.of(koerperschaft));

    kulturObjektRegistrierenController.placeChangedListener();

    verify(normdatenReferenzBoundary, times(1)).findKoerperschaftenByOrtId(anyString(), anyBoolean());

    assertNotNull(dataModel.getKoerperschaftViewModelMap());
    assertEquals(1, dataModel.getKoerperschaftViewModelMap().size());
  }

  @Test
  void testPlaceChangedListener_noPlace() {
    KulturObjektRegistrierenModel dataModel = new KulturObjektRegistrierenModel(
        kulturObjektDokumentBoundary, bearbeiterBoundary, normdatenReferenzBoundary);

    KulturObjektRegistrierenController kulturObjektRegistrierenController
        = new KulturObjektRegistrierenController(normdatenReferenzBoundary, dataModel);

    dataModel.setOrteViewModel(null);

    kulturObjektRegistrierenController.placeChangedListener();

    verify(normdatenReferenzBoundary, never()).findKoerperschaftenByOrtId(nullable(String.class), anyBoolean());

    assertNotNull(dataModel.getKoerperschaftViewModelMap());
    assertEquals(0, dataModel.getKoerperschaftViewModelMap().size());
  }

  private NormdatenReferenz buildNormDatenOrt() {
    return new NormdatenReferenzBuilder()
        .withId("20")
        .withGndID("4005728-8")
        .withName("Berlin")
        .withTypeName(NormdatenReferenz.ORT_TYPE_NAME)
        .build();
  }

  private NormdatenReferenz buildNormdatenKoerperschaft() {
    return new NormdatenReferenz("10", "Staatsbibliothek zu Berlin", "5036103-X",
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);
  }

}
