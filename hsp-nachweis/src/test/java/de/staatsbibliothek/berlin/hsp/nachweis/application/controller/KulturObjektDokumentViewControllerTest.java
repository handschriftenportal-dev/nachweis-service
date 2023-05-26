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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.DigitalisatErfassenModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.KODKerndatenModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DigitalisatViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel.KulturObjektDokumentViewModelBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;

/**
 * Created by Christoph Marten on 01.10.2020 at 14:04
 */
class KulturObjektDokumentViewControllerTest {

  KulturObjektDokumentViewController kulturObjektDokumentViewController;
  ExternalContext externalContext = mock(ExternalContext.class);
  KulturObjektDokumentBoundary kulturObjektDokumentBoundary = mock(KulturObjektDokumentBoundary.class);
  BearbeiterBoundary bearbeiterBoundary = mock(BearbeiterBoundary.class);
  ImportBoundary importBoundary = mock(ImportBoundary.class);
  DigitalisatErfassenModel digitalisatErfassenModel = mock(DigitalisatErfassenModel.class);

  MockedStatic<FacesContext> contextStatic = mockStatic(FacesContext.class);
  FacesContext facesContext = mock(FacesContext.class);
  MockedStatic<PrimeFaces> primeFacesStatic = mockStatic(PrimeFaces.class);
  PrimeFaces primeFaces = mock(PrimeFaces.class);
  PrimeFaces.Dialog primeFacesDialog = mock(PrimeFaces.Dialog.class);

  @BeforeEach
  void beforeEach() {
    contextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
    when(facesContext.getExternalContext()).thenReturn(externalContext);
    primeFacesStatic.when(PrimeFaces::current).thenReturn(primeFaces);
    when(primeFaces.dialog()).thenReturn(primeFacesDialog);

    kulturObjektDokumentViewController = new KulturObjektDokumentViewController();
  }

  @AfterEach
  void resetMocks() {
    primeFacesStatic.close();
    contextStatic.close();
  }

  @Test
  void testConstruction() {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setId("1");

    KODKerndatenModel kodKerndatenModel = mock(KODKerndatenModel.class);
    kulturObjektDokumentViewController.setKODKerndatenModel(kodKerndatenModel);

    when(kulturObjektDokumentBoundary.buildKulturObjektDokumentViewModel("1")).thenReturn(Optional.of(
        KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
            .withId("1")
            .build()));

    kulturObjektDokumentViewController.setup();

    KulturObjektDokumentViewModel model = kulturObjektDokumentViewController.getKulturObjektDokumentViewModel();

    assertNotNull(model);
    assertEquals(kulturObjektDokumentViewController.getId(), model.getId());

    verify(kodKerndatenModel, times(1))
        .init(any(KulturObjektDokumentViewModel.class));
  }

  @Test
  void testOnPreDestroy() throws DokumentSperreException {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setId("1");

    KODKerndatenModel kodKerndatenModel = mock(KODKerndatenModel.class);
    kulturObjektDokumentViewController.setKODKerndatenModel(kodKerndatenModel);

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(kulturObjektDokumentViewModel);

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(new Bearbeiter("b_1", "bearbeiterName"));
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);

    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("KerndatenBearbeiten")
        .withBearbeiter(new Bearbeiter("b_1", "Bearbeitername"))
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();

    when(kulturObjektDokumentBoundary
        .kulturObjektDokumentSperren(any(Bearbeiter.class), anyString(), anyString()))
        .thenReturn(sperre);

    kulturObjektDokumentViewController.kerndatenBearbeiten();

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentSperren(any(Bearbeiter.class), anyString(), anyString());

    assertNotNull(kulturObjektDokumentViewController.getSperre());

    assertTrue(kulturObjektDokumentViewController.isEditKerndaten());

    kulturObjektDokumentViewController.onPreDestroy();

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentEntsperren(anyString());
  }

  @Test
  void testConstructionUnhappyPath() {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setKODKerndatenModel(new KODKerndatenModel());
    kulturObjektDokumentViewController.setId("1");

    when(kulturObjektDokumentBoundary.findById("1")).thenReturn(null);

    kulturObjektDokumentViewController.setup();

    assertNull(kulturObjektDokumentViewController.getKulturObjektDokumentViewModel());
  }

  @Test
  void testDigitalisatAnlegen() throws DokumentSperreException, SperreAlreadyExistException {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setDigitalisatErfassenModel(digitalisatErfassenModel);

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(new Bearbeiter("b_1", "bearbeiterName"));
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(kulturObjektDokumentViewModel);

    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("DigitalisatAnlegen")
        .withBearbeiter(new Bearbeiter("b_1", "Bearbeitername"))
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();

    when(kulturObjektDokumentBoundary
        .kulturObjektDokumentSperren(any(Bearbeiter.class), anyString(), anyString()))
        .thenReturn(sperre);

    String view = kulturObjektDokumentViewController.digitalisatAnlegen();
    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentSperren(any(Bearbeiter.class), anyString(), anyString());

    assertEquals("/kulturObjektDokument/digitalisat-anlegen.xhtml?faces-redirect=true&kodid=1", view);
  }

  @Test
  void testDigitalisatBearbeiten() throws DokumentSperreException, SperreAlreadyExistException {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setDigitalisatErfassenModel(digitalisatErfassenModel);

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(new Bearbeiter("b_1", "bearbeiterName"));
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(kulturObjektDokumentViewModel);

    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("DigitalisatBearbeiten")
        .withBearbeiter(new Bearbeiter("b_1", "Bearbeitername"))
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();

    when(kulturObjektDokumentBoundary
        .kulturObjektDokumentSperren(any(Bearbeiter.class), anyString(), anyString()))
        .thenReturn(sperre);

    DigitalisatViewModel digitalisatViewModel = new DigitalisatViewModel();
    digitalisatViewModel.setId("dvm_1");

    String view = kulturObjektDokumentViewController.digitalisatBearbeiten(digitalisatViewModel);
    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentSperren(any(Bearbeiter.class), anyString(), anyString());

    assertEquals("/kulturObjektDokument/digitalisat-anlegen.xhtml?faces-redirect=true&kodid=1&digitalisatid=dvm_1",
        view);
  }

  @Test
  void testDigitalisatLoeschen() throws DokumentSperreException, SperreAlreadyExistException {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setDigitalisatErfassenModel(digitalisatErfassenModel);

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(new Bearbeiter("b_1", "bearbeiterName"));
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(kulturObjektDokumentViewModel);

    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("DigitalisatLöschen")
        .withBearbeiter(new Bearbeiter("b_1", "Bearbeitername"))
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();

    when(kulturObjektDokumentBoundary
        .kulturObjektDokumentSperren(any(Bearbeiter.class), anyString(), anyString()))
        .thenReturn(sperre);

    DigitalisatViewModel digitalisatViewModel = new DigitalisatViewModel();

    kulturObjektDokumentViewController.digitalisatLoeschen(digitalisatViewModel);

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentSperren(any(Bearbeiter.class), anyString(), anyString());

    verify(kulturObjektDokumentBoundary, times(1))
        .digitalisatLoeschen(any(KulturObjektDokumentViewModel.class), any(DigitalisatViewModel.class));

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentEntsperren(anyString());
  }

  @Test
  void testNeueBeschreibungErstellen() throws DokumentSperreException, SperreAlreadyExistException {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setId("1");

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(new Bearbeiter("b_1", "bearbeiterName"));
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(kulturObjektDokumentViewModel);

    kulturObjektDokumentViewController.setImportBoundary(importBoundary);

    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("BeschreibungErstellen")
        .withBearbeiter(new Bearbeiter("b_1", "Bearbeitername"))
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();

    when(kulturObjektDokumentBoundary
        .kulturObjektDokumentSperren(any(Bearbeiter.class), anyString(), anyString()))
        .thenReturn(sperre);

    kulturObjektDokumentViewController.neueBeschreibungErstellen();

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentSperren(any(Bearbeiter.class), anyString(), anyString());

    verify(primeFacesDialog, times(1)).openDynamic(anyString(), anyMap(), anyMap());
  }

  @Test
  void testNeueBeschreibungAnzeigen() throws DokumentSperreException, SperreAlreadyExistException, IOException {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setId("1");

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(kulturObjektDokumentViewModel);

    String beschreibungID = "b_1";
    SelectEvent<String> selectEvent = (SelectEvent<String>) mock(SelectEvent.class);
    when(selectEvent.getObject()).thenReturn(beschreibungID);

    kulturObjektDokumentViewController.neueBeschreibungAnzeigen(selectEvent);

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentEntsperren(anyString());

    verify(externalContext, times(1))
        .redirect("/beschreibung/beschreibung.xhtml?edit=true&id=" + beschreibungID);
  }

  @Test
  void testKodSperren() throws DokumentSperreException, SperreAlreadyExistException {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setId("1");

    when(bearbeiterBoundary.getLoggedBearbeiter())
        .thenReturn(new Bearbeiter("b_1", "bearbeiterName_1"));
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(kulturObjektDokumentViewModel);

    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("testKodSperren")
        .withBearbeiter(new Bearbeiter("b_2", "bearbeiterName_2"))
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .withStartDatum(LocalDateTime.of(2021, 11, 11, 11, 11, 11))
        .build();

    when(kulturObjektDokumentBoundary
        .kulturObjektDokumentSperren(any(Bearbeiter.class), anyString(), anyString()))
        .thenThrow(new SperreAlreadyExistException("Sperre already exists!", List.of(sperre)));

    boolean gesperrt = kulturObjektDokumentViewController.kodSperren("sperreGrund", "summary");
    assertFalse(gesperrt);

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentSperren(any(Bearbeiter.class), anyString(), anyString());

    ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
    verify(primeFacesDialog, times(1)).showMessageDynamic(captor.capture());

    FacesMessage facesMessage = captor.getValue();
    assertNotNull(facesMessage);
    assertEquals(FacesMessage.SEVERITY_ERROR, facesMessage.getSeverity());
    assertEquals("summary", facesMessage.getSummary());
    assertEquals("Das Kulturobjekt ist gesperrt durch bearbeiterName_2 seit 11.11.2021.",
        facesMessage.getDetail());
  }

  @Test
  void testIsAutomatischeUebernahmeAktiv() {
    kulturObjektDokumentViewController.setImportBoundary(importBoundary);

    kulturObjektDokumentViewController.isAutomatischeUebernahmeAktiv();

    verify(importBoundary, times(1)).isAutomatischenUebernahmeAktiv();
  }

  @Test
  void testIsDeleteEnabled() {
    BeschreibungsRechte beschreibungsRechte = mock(BeschreibungsRechte.class);
    when(beschreibungsRechte.kannAllerDritterhartLoeschen()).thenReturn(false);

    kulturObjektDokumentViewController.setDeleteEnabled(false);
    kulturObjektDokumentViewController.setBeschreibungsRechte(beschreibungsRechte);

    assertFalse(kulturObjektDokumentViewController.isDeleteEnabled());

    kulturObjektDokumentViewController.setDeleteEnabled(true);
    assertFalse(kulturObjektDokumentViewController.isDeleteEnabled());

    kulturObjektDokumentViewController.setDeleteEnabled(false);
    when(beschreibungsRechte.kannAllerDritterhartLoeschen()).thenReturn(true);
    assertFalse(kulturObjektDokumentViewController.isDeleteEnabled());

    kulturObjektDokumentViewController.setDeleteEnabled(true);
    assertTrue(kulturObjektDokumentViewController.isDeleteEnabled());
  }

  @Test
  void testKerndatenBearbeiten() throws DokumentSperreException {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setId("1");

    KODKerndatenModel kodKerndatenModel = mock(KODKerndatenModel.class);
    kulturObjektDokumentViewController.setKODKerndatenModel(kodKerndatenModel);

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(kulturObjektDokumentViewModel);

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(new Bearbeiter("b_1", "bearbeiterName"));
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);

    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("KerndatenBearbeiten")
        .withBearbeiter(new Bearbeiter("b_1", "Bearbeitername"))
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();

    when(kulturObjektDokumentBoundary
        .kulturObjektDokumentSperren(any(Bearbeiter.class), anyString(), anyString()))
        .thenReturn(sperre);

    kulturObjektDokumentViewController.kerndatenBearbeiten();

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentSperren(any(Bearbeiter.class), anyString(), anyString());

    assertNotNull(kulturObjektDokumentViewController.getSperre());

    assertTrue(kulturObjektDokumentViewController.isEditKerndaten());

    verify(kodKerndatenModel, times(1)).bearbeiten();
  }

  @Test
  void testKerndatenBearbeitenAbbrechen() throws DokumentSperreException {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setId("1");

    KODKerndatenModel kodKerndatenModel = mock(KODKerndatenModel.class);
    kulturObjektDokumentViewController.setKODKerndatenModel(kodKerndatenModel);

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").build();

    when(kulturObjektDokumentBoundary
        .buildKulturObjektDokumentViewModel(anyString()))
        .thenReturn(Optional.of(kulturObjektDokumentViewModel));

    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(kulturObjektDokumentViewModel);

    kulturObjektDokumentViewController.kerndatenBearbeitenAbbrechen();

    assertFalse(kulturObjektDokumentViewController.isEditKerndaten());
    assertNull(kulturObjektDokumentViewController.getSperre());

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentEntsperren(anyString());

    verify(kodKerndatenModel, times(1)).init(any(KulturObjektDokumentViewModel.class));
  }

  @Test
  void testKerndatenSpeichern() throws DokumentSperreException {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setId("1");

    KODKerndatenModel kodKerndatenModel = mock(KODKerndatenModel.class);
    kulturObjektDokumentViewController.setKODKerndatenModel(kodKerndatenModel);

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(kulturObjektDokumentViewModel);

    when(kulturObjektDokumentBoundary
        .buildKulturObjektDokumentViewModel(anyString()))
        .thenReturn(Optional.of(kulturObjektDokumentViewModel));

    kulturObjektDokumentViewController.kerndatenSpeichern();

    assertFalse(kulturObjektDokumentViewController.isEditKerndaten());
    assertNull(kulturObjektDokumentViewController.getSperre());

    verify(kodKerndatenModel, times(1))
        .kerndatenUebernehmen(any(KulturObjektDokumentViewModel.class));

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentAktualisieren(any(KulturObjektDokumentViewModel.class));

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentEntsperren(anyString());

    verify(kulturObjektDokumentBoundary, times(1))
        .buildKulturObjektDokumentViewModel(anyString());

    verify(kodKerndatenModel, times(1))
        .init(any(KulturObjektDokumentViewModel.class));
  }

  @Test
  void testShowFacesInfoMessage() {
    ArgumentCaptor<FacesMessage> messageCaptor = ArgumentCaptor.forClass(FacesMessage.class);

    kulturObjektDokumentViewController.showFacesInfoMessage("kod_detail_kerndatenspeichern_success", "details");

    verify(facesContext, times(1)).addMessage(anyString(), messageCaptor.capture());

    FacesMessage facesMessage = messageCaptor.getValue();
    assertNotNull(facesMessage);
    assertEquals(FacesMessage.SEVERITY_INFO, facesMessage.getSeverity());
    assertEquals("Die Kerndaten wurden erfolgreich gespeichert.", facesMessage.getSummary());
    assertEquals("details", facesMessage.getDetail());
  }

  @Test
  void testShowFacesErrorMessage() {
    ArgumentCaptor<FacesMessage> messageCaptor = ArgumentCaptor.forClass(FacesMessage.class);

    kulturObjektDokumentViewController.showFacesErrorMessage("kod_detail_kerndatenspeichern_fehler", "details");

    verify(facesContext, times(1)).addMessage(anyString(), messageCaptor.capture());

    FacesMessage facesMessage = messageCaptor.getValue();
    assertNotNull(facesMessage);
    assertEquals(FacesMessage.SEVERITY_ERROR, facesMessage.getSeverity());
    assertEquals("Fehler beim Speichern der Kerndaten!", facesMessage.getSummary());
    assertEquals("details", facesMessage.getDetail());
  }

}
