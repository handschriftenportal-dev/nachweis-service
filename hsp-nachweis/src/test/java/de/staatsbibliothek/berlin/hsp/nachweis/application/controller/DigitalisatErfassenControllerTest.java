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

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.DigitalisatErfassenController.FACES_REDIRECT;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.DigitalisatErfassenController.KOD_PAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DigitalisatTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.DigitalisatErfassenModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DigitalisatViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 08.10.2020.
 * @version 1.0
 */
public class DigitalisatErfassenControllerTest {

  FacesContext context = Mockito.mock(FacesContext.class);
  ExternalContext externalContext = Mockito.mock(ExternalContext.class);
  MockedStatic<FacesContext> facesContextCurrentInstance;
  Flash flash = Mockito.mock(Flash.class);

  KulturObjektDokumentBoundary kulturObjektDokumentBoundary = Mockito.mock(
      KulturObjektDokumentBoundary.class);
  NormdatenReferenzBoundary normdatenReferenzBoundary = Mockito.mock(NormdatenReferenzBoundary.class);
  DigitalisatErfassenModel digitalisatErfassenModel = new DigitalisatErfassenModel();
  BearbeiterBoundary bearbeiterBoundary = Mockito.mock(BearbeiterBoundary.class);

  DigitalisatErfassenController controller = new DigitalisatErfassenController(kulturObjektDokumentBoundary,
      normdatenReferenzBoundary,
      bearbeiterBoundary,
      digitalisatErfassenModel);

  @BeforeEach
  void onSetup() {
    facesContextCurrentInstance = Mockito.mockStatic(FacesContext.class);
    facesContextCurrentInstance.when(FacesContext::getCurrentInstance).thenReturn(context);

    when(context.getExternalContext()).thenReturn(externalContext);
    when(externalContext.getFlash()).thenReturn(flash);
  }

  @AfterEach
  public void derigisterStaticMock() {
    facesContextCurrentInstance.close();
  }

  @Test
  void testSetup() throws DokumentSperreException {

    controller.setExternalContext(externalContext);
    digitalisatErfassenModel.construct();

    Map<String, String> requestMap = new HashMap<>();
    requestMap.put(DigitalisatErfassenController.REQUEST_PARAMETER_KODID, "1");
    requestMap.put(DigitalisatErfassenController.REQUEST_PARAMETER_DIGITALISATID, "1");

    when(externalContext.getRequestParameterMap())
        .thenReturn(requestMap);

    DigitalisatViewModel digitalisatViewModel = DigitalisatViewModel.builder()
        .id("1").build();

    digitalisatErfassenModel.setDigitalisatViewModel(digitalisatViewModel);

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModel.KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel()
        .withId("1")
        .addDigitalisat(digitalisatViewModel)
        .build();

    when(kulturObjektDokumentBoundary.buildKulturObjektDokumentViewModel("1")).thenReturn(
        Optional.of(kulturObjektDokumentViewModel));

    Bearbeiter bearbeiter = new Bearbeiter("b_1", "Bearbeitername");

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);

    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("DigitalisatAnlegen")
        .withBearbeiter(bearbeiter)
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();

    when(kulturObjektDokumentBoundary.findSperreForKulturObjektDokument(anyString())).thenReturn(Optional.of(sperre));

    controller.setup();

    verify(kulturObjektDokumentBoundary, times(1)).findSperreForKulturObjektDokument(anyString());

    verify(kulturObjektDokumentBoundary, times(1)).buildKulturObjektDokumentViewModel("1");

    assertEquals(kulturObjektDokumentViewModel, controller.getKulturObjektDokumentViewModel());

    assertEquals(digitalisatViewModel, controller.getDigitalisatViewModel());

    verify(normdatenReferenzBoundary, times(0)).findKoerperschaftenByOrtId(anyString(), anyBoolean());

  }

  @Test
  void testdigitalisatHinzufuegen() throws KulturObjektDokumentException, DokumentSperreException, IOException {

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModel.KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel()
        .withId("1")
        .build();

    controller.setKulturObjektDokumentViewModel(kulturObjektDokumentViewModel);

    String view = controller.digitalisatHinzufuegen();

    assertNull(view);

    ArgumentCaptor<String> redirectUrl = ArgumentCaptor.forClass(String.class);
    verify(externalContext, times(1)).redirect(redirectUrl.capture());
    assertEquals(KOD_PAGE + FACES_REDIRECT + "&id=1#digitalisate", redirectUrl.getValue());

    verify(kulturObjektDokumentBoundary, times(1)).digitalisatHinzufuegen(any(), any());

    verify(kulturObjektDokumentBoundary, times(1)).kulturObjektDokumentEntsperren(any());

  }

  @Test
  void testAbbrechen() throws DokumentSperreException {

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModel.KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel()
        .withId("1")
        .build();

    controller.setKulturObjektDokumentViewModel(kulturObjektDokumentViewModel);

    digitalisatErfassenModel.construct();

    digitalisatErfassenModel.getDigitalisatViewModel().setDigitalisatTyp(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL);

    String view = controller.abbrechen();

    verify(kulturObjektDokumentBoundary, times(1)).kulturObjektDokumentEntsperren(any());

    assertEquals(KOD_PAGE + FACES_REDIRECT + "&id=1", view);

    assertNull(controller.getDigitalisatViewModel().getDigitalisatTyp());
  }

  @Test
  void testKodEntsperren() throws DokumentSperreException {

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModel.KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel()
        .withId("1")
        .build();

    controller.setKulturObjektDokumentViewModel(kulturObjektDokumentViewModel);

    Bearbeiter bearbeiter = new Bearbeiter("b_1", "Bearbeitername");

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);

    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("DigitalisatAnlegen")
        .withBearbeiter(bearbeiter)
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();

    when(kulturObjektDokumentBoundary.findSperreForKulturObjektDokument(anyString())).thenReturn(Optional.of(sperre));

    controller.findAndCheckSperre();

    controller.kodEntsperren();

    verify(kulturObjektDokumentBoundary, times(1)).kulturObjektDokumentEntsperren(any());
  }

  @Test
  void testPlaceChangedListener() {

    NormdatenReferenz ort = new NormdatenReferenz("1", "Berlin", "GND1", GNDEntityFact.PLACE_TYPE_NAME);

    NormdatenReferenz koerperschaft = new NormdatenReferenz("1", "Staatsbibliothek zu Berlin",
        "GND2", NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);

    controller.setDigitalisatViewModel(DigitalisatViewModel.builder()
        .ort(ort)
        .build());

    when(normdatenReferenzBoundary.findKoerperschaftenByOrtId(ort.getId(), false))
        .thenReturn(Set.of(koerperschaft));

    controller.placeChangedListener();

    verify(normdatenReferenzBoundary, times(1))
        .findKoerperschaftenByOrtId("1", false);

    Assertions.assertEquals(1, controller.getKoerperschaftViewModelMap().size());
  }
}
