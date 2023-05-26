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
