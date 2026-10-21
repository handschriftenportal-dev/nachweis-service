package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessource;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessourceTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Forschungsdokumentation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.DigitalisatErfassenModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.KODKerndatenModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DigitalisatViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel.KulturObjektDokumentViewModelBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import java.io.IOException;
import java.text.Collator;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
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

  public static final String CAPTURE_TYPE = "captureType";
  KulturObjektDokumentViewController kulturObjektDokumentViewController;
  ExternalContext externalContext = mock(ExternalContext.class);
  KulturObjektDokumentBoundary kulturObjektDokumentBoundary = mock(
      KulturObjektDokumentBoundary.class);
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
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setId("1");

    KODKerndatenModel kodKerndatenModel = mock(KODKerndatenModel.class);
    kulturObjektDokumentViewController.setKODKerndatenModel(kodKerndatenModel);

    Identifikation altidentifikation = new Identifikation.IdentifikationBuilder()
        .withId("2")
        .withIdent("Signatur")
        .withAufbewahrungsOrt(new NormdatenReferenz.NormdatenReferenzBuilder()
            .withName("AufbewahrungsOrt ALT").withGndID("").build())
        .withBesitzer(new NormdatenReferenz.NormdatenReferenzBuilder()
            .withName("Besitzer ALT").withGndID("").build())
        .build();

    when(kulturObjektDokumentBoundary.buildKulturObjektDokumentViewModel("1")).thenReturn(
        Optional.of(
            KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
                .withId("1")
                .withSignatur("Signatur")
                .withAlternativeSignaturen(Set.of(altidentifikation))
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
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setId("1");

    KODKerndatenModel kodKerndatenModel = mock(KODKerndatenModel.class);
    kulturObjektDokumentViewController.setKODKerndatenModel(kodKerndatenModel);

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(
        kulturObjektDokumentViewModel);

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(
        new Bearbeiter("b_1", "bearbeiterName"));
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
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setKODKerndatenModel(new KODKerndatenModel());
    kulturObjektDokumentViewController.setId("1");

    when(kulturObjektDokumentBoundary.findById("1")).thenReturn(null);

    kulturObjektDokumentViewController.setup();

    assertNull(kulturObjektDokumentViewController.getKulturObjektDokumentViewModel());
  }

  @Test
  void testDigitalisatAnlegen() throws DokumentSperreException, SperreAlreadyExistException {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setDigitalisatErfassenModel(digitalisatErfassenModel);

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(
        new Bearbeiter("b_1", "bearbeiterName"));
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(
        kulturObjektDokumentViewModel);

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

    assertEquals("/kulturObjektDokument/digitalisat-anlegen.xhtml?faces-redirect=true&kodid=1",
        view);
  }

  @Test
  void testDigitalisatBearbeiten() throws DokumentSperreException, SperreAlreadyExistException {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setDigitalisatErfassenModel(digitalisatErfassenModel);

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(
        new Bearbeiter("b_1", "bearbeiterName"));
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(
        kulturObjektDokumentViewModel);

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

    assertEquals(
        "/kulturObjektDokument/digitalisat-anlegen.xhtml?faces-redirect=true&kodid=1&digitalisatid=dvm_1",
        view);
  }

  @Test
  void testDigitalisatLoeschen() throws DokumentSperreException, SperreAlreadyExistException {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setDigitalisatErfassenModel(digitalisatErfassenModel);

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(
        new Bearbeiter("b_1", "bearbeiterName"));
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(
        kulturObjektDokumentViewModel);

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
        .digitalisatLoeschen(anyString(),
            any(DigitalisatViewModel.class));

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentEntsperren(anyString());
  }

  @Test
  void testNeueBeschreibungErstellen() throws DokumentSperreException, SperreAlreadyExistException {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setId("1");

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(
        new Bearbeiter("b_1", "bearbeiterName"));
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(
        kulturObjektDokumentViewModel);

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
  void testNeueBeschreibungAnzeigen()
      throws DokumentSperreException, SperreAlreadyExistException, IOException {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setId("1");

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(
        kulturObjektDokumentViewModel);

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
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setId("1");

    when(bearbeiterBoundary.getLoggedBearbeiter())
        .thenReturn(new Bearbeiter("b_1", "bearbeiterName_1"));
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(
        kulturObjektDokumentViewModel);

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
  void testKerndatenBearbeiten() throws DokumentSperreException {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setId("1");

    KODKerndatenModel kodKerndatenModel = mock(KODKerndatenModel.class);
    kulturObjektDokumentViewController.setKODKerndatenModel(kodKerndatenModel);

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(
        kulturObjektDokumentViewModel);

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(
        new Bearbeiter("b_1", "bearbeiterName"));
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
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setId("1");

    KODKerndatenModel kodKerndatenModel = mock(KODKerndatenModel.class);
    kulturObjektDokumentViewController.setKODKerndatenModel(kodKerndatenModel);

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").build();

    when(kulturObjektDokumentBoundary
        .buildKulturObjektDokumentViewModel(anyString()))
        .thenReturn(Optional.of(kulturObjektDokumentViewModel));

    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(
        kulturObjektDokumentViewModel);

    kulturObjektDokumentViewController.kerndatenBearbeitenAbbrechen();

    assertFalse(kulturObjektDokumentViewController.isEditKerndaten());
    assertNull(kulturObjektDokumentViewController.getSperre());

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentEntsperren(anyString());

    verify(kodKerndatenModel, times(1)).init(any(KulturObjektDokumentViewModel.class));
  }

  @Test
  void testKerndatenSpeichern() throws DokumentSperreException {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setId("1");

    KODKerndatenModel kodKerndatenModel = mock(KODKerndatenModel.class);
    kulturObjektDokumentViewController.setKODKerndatenModel(kodKerndatenModel);

    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(
        kulturObjektDokumentViewModel);

    when(kulturObjektDokumentBoundary
        .buildKulturObjektDokumentViewModel(anyString()))
        .thenReturn(Optional.of(kulturObjektDokumentViewModel));

    kulturObjektDokumentViewController.kerndatenSpeichern();

    assertFalse(kulturObjektDokumentViewController.isEditKerndaten());
    assertNull(kulturObjektDokumentViewController.getSperre());

    verify(kodKerndatenModel, times(1))
        .kerndatenUebernehmen(any(KulturObjektDokumentViewModel.class));

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentAttributsreferenzenAktualisieren(
            any(KulturObjektDokumentViewModel.class));

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

    kulturObjektDokumentViewController.showFacesInfoMessage("kod_detail_kerndatenspeichern_success",
        "details");

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

    kulturObjektDokumentViewController.showFacesErrorMessage("kod_detail_kerndatenspeichern_fehler",
        "details");

    verify(facesContext, times(1)).addMessage(anyString(), messageCaptor.capture());

    FacesMessage facesMessage = messageCaptor.getValue();
    assertNotNull(facesMessage);
    assertEquals(FacesMessage.SEVERITY_ERROR, facesMessage.getSeverity());
    assertEquals("Fehler beim Speichern der Kerndaten!", facesMessage.getSummary());
    assertEquals("details", facesMessage.getDetail());
  }

  @Test
  void testgueltigeSignaturBearbeiten() throws Exception {
    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("KODSignatureAendern")
        .withBearbeiter(new Bearbeiter("b_1", "Bearbeitername"))
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();
    kulturObjektDokumentViewController.setSperre(sperre);
    kulturObjektDokumentViewController.setId("1");
    KulturObjektDokumentBoundary boundary = mock(KulturObjektDokumentBoundary.class);
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(boundary);
    KODKerndatenModel kodKerndatenModel = mock(KODKerndatenModel.class);
    kulturObjektDokumentViewController.setKODKerndatenModel(kodKerndatenModel);
    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").withSignatur("NeueSignatur").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(
        kulturObjektDokumentViewModel);
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setNeueSignature("NeueSignatur");

    when(kulturObjektDokumentBoundary
        .buildKulturObjektDokumentViewModel(anyString()))
        .thenReturn(Optional.of(kulturObjektDokumentViewModel));

    kulturObjektDokumentViewController.gueltigeSignaturBearbeiten();

    verify(kulturObjektDokumentBoundary, times(1))
        .gueltigeSignaturVeraendern("NeueSignatur", "1", sperre);

    assertFalse(kulturObjektDokumentViewController.isEditSignature());
  }

  @Test
  void testgetSignatureChangeConfirmMessage() {
    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").withSignatur("AlteSignatur").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(
        kulturObjektDokumentViewModel);
    kulturObjektDokumentViewController.setNeueSignature("NeueSignatur");

    assertEquals("""
            Wollen Sie die gültige Signatur von "AlteSignatur" zu "NeueSignatur" ändern?""",
        kulturObjektDokumentViewController.getSignatureChangeConfirmMessage());
  }

  @Test
  void testEnableDisableEditSignature() throws DokumentSperreException {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);
    kulturObjektDokumentViewController.setId("1");
    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").withSignatur("NeueSignatur").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(
        kulturObjektDokumentViewModel);
    kulturObjektDokumentViewController.setNeueSignature("NeueSignatur");
    Bearbeiter bearbeiter = Bearbeiter.newBuilder().build();
    KODKerndatenModel kodKerndatenModel = mock(KODKerndatenModel.class);
    kulturObjektDokumentViewController.setKODKerndatenModel(kodKerndatenModel);

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);

    kulturObjektDokumentViewController.enableEditSignature();

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentSperren(any(), any(), any());

    assertTrue(kulturObjektDokumentViewController.isEditSignature());

    kulturObjektDokumentViewController.disableEditSignature();

    assertFalse(kulturObjektDokumentViewController.isEditSignature());

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentEntsperren(any());

  }

  @Test
  void testEnableAddAlternativeSignature() throws DokumentSperreException {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);
    kulturObjektDokumentViewController.setId("1");
    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").withSignatur("Signature").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(
        kulturObjektDokumentViewModel);
    kulturObjektDokumentViewController.setNeueAlternativeSignature("NeueAlternativeSignatur");
    Bearbeiter bearbeiter = Bearbeiter.newBuilder().build();
    KODKerndatenModel kodKerndatenModel = mock(KODKerndatenModel.class);
    kulturObjektDokumentViewController.setKODKerndatenModel(kodKerndatenModel);

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);

    kulturObjektDokumentViewController.enableAddAlternativeSignature();

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentSperren(any(), any(), any());

    assertTrue(kulturObjektDokumentViewController.isAddAlternativeSignature());
  }

  @Test
  void testDisableAddAlternativeSignature() throws DokumentSperreException {
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);
    kulturObjektDokumentViewController.setId("1");
    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").withSignatur("Signature").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(
        kulturObjektDokumentViewModel);
    kulturObjektDokumentViewController.setNeueAlternativeSignature("NeueAlternativeSignatur");
    Bearbeiter bearbeiter = Bearbeiter.newBuilder().build();
    KODKerndatenModel kodKerndatenModel = mock(KODKerndatenModel.class);
    kulturObjektDokumentViewController.setKODKerndatenModel(kodKerndatenModel);

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);

    kulturObjektDokumentViewController.disableAddAlternativeSignature();

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentEntsperren(any());

    assertFalse(kulturObjektDokumentViewController.isAddAlternativeSignature());
  }

  @Test
  void testalternativeSignatureHinzufuegen() throws Exception {
    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("KODAlternativeSignatureHinzufügen")
        .withBearbeiter(new Bearbeiter("b_1", "Bearbeitername"))
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();
    kulturObjektDokumentViewController.setSperre(sperre);
    kulturObjektDokumentViewController.setId("1");
    KulturObjektDokumentBoundary boundary = mock(KulturObjektDokumentBoundary.class);
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(boundary);
    KODKerndatenModel kodKerndatenModel = mock(KODKerndatenModel.class);
    kulturObjektDokumentViewController.setKODKerndatenModel(kodKerndatenModel);
    KulturObjektDokumentViewModel kulturObjektDokumentViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel().withId("1").withSignatur("Signature").build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(
        kulturObjektDokumentViewModel);
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setNeueAlternativeSignature("NeueAlternativeSignatur");

    when(kulturObjektDokumentBoundary
        .buildKulturObjektDokumentViewModel(anyString()))
        .thenReturn(Optional.of(kulturObjektDokumentViewModel));

    kulturObjektDokumentViewController.alternativeSignatureHinzufuegen();

    verify(kulturObjektDokumentBoundary, times(1))
        .alternativeSignaturHinzufuegen("NeueAlternativeSignatur", "1", sperre);

    assertFalse(kulturObjektDokumentViewController.isAddAlternativeSignature());
  }

  @Test
  void testEnableAddExterneRessource() throws Exception {

    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);
    kulturObjektDokumentViewController.setId("1");

    KulturObjektDokumentViewModel viewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel()
        .withId("1")
        .build();

    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(viewModel);

    Bearbeiter bearbeiter = new Bearbeiter("b_1", "Bearbeitername");

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);

    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("KODAddExterneRessource")
        .withBearbeiter(bearbeiter)
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();

    when(kulturObjektDokumentBoundary.kulturObjektDokumentSperren(any(Bearbeiter.class),
        anyString(),
        anyString()))
        .thenReturn(sperre);

    kulturObjektDokumentViewController.enableAddExterneRessource();

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentSperren(any(Bearbeiter.class), anyString(), anyString());

    assertTrue(kulturObjektDokumentViewController.isAddExterneRessource());
    assertNotNull(kulturObjektDokumentViewController.getSperre());
  }

  @Test
  void testEnableAddExterneRessourceWhenLockFails() throws Exception {

    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);
    kulturObjektDokumentViewController.setId("1");

    KulturObjektDokumentViewModel viewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel()
        .withId("1")
        .build();

    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(viewModel);

    when(bearbeiterBoundary.getLoggedBearbeiter())
        .thenReturn(new Bearbeiter("b_1", "Bearbeitername"));

    when(kulturObjektDokumentBoundary
        .kulturObjektDokumentSperren(any(Bearbeiter.class), anyString(), anyString()))
        .thenThrow(new RuntimeException("Unable to acquire lock"));

    kulturObjektDokumentViewController.enableAddExterneRessource();

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentSperren(any(Bearbeiter.class), anyString(), anyString());

    assertFalse(kulturObjektDokumentViewController.isAddExterneRessource());

    assertNull(kulturObjektDokumentViewController.getSperre());
  }

  @Test
  void testDisableAddExterneRessource() throws Exception {

    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setId("1");

    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("KODAddExterneRessource")
        .withBearbeiter(
            new Bearbeiter("b_1", "Bearbeitername"))
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(
            new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();

    kulturObjektDokumentViewController.setSperre(sperre);
    kulturObjektDokumentViewController.setAddExterneRessource(true);
    kulturObjektDokumentViewController.setNeueExterneRessourceTyp(ExterneRessourceTyp.HSC);
    kulturObjektDokumentViewController.setNeueExterneRessourceUrl(
        "https://handschriftencensus.de/123");

    kulturObjektDokumentViewController.disableAddExterneRessource();

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentEntsperren("1");

    assertNull(kulturObjektDokumentViewController.getSperre());
    assertFalse(kulturObjektDokumentViewController.isAddExterneRessource());
    assertNull(kulturObjektDokumentViewController.getNeueExterneRessourceTyp());
    assertEquals("", kulturObjektDokumentViewController.getNeueExterneRessourceUrl());
  }

  @Test
  void testExterneRessourceHinzufuegen() throws Exception {

    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setId("1");

    KODKerndatenModel kodKerndatenModel = mock(KODKerndatenModel.class);

    kulturObjektDokumentViewController.setKODKerndatenModel(kodKerndatenModel);

    KulturObjektDokumentViewModel viewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel()
        .withId("1")
        .build();

    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(viewModel);

    when(kulturObjektDokumentBoundary.buildKulturObjektDokumentViewModel("1"))
        .thenReturn(Optional.of(viewModel));

    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("KODAddExterneRessource")
        .withBearbeiter(new Bearbeiter("b_1", "Bearbeitername"))
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();

    kulturObjektDokumentViewController.setSperre(sperre);
    kulturObjektDokumentViewController.setAddExterneRessource(true);
    kulturObjektDokumentViewController.setNeueExterneRessourceTyp(ExterneRessourceTyp.HSC);
    kulturObjektDokumentViewController.setNeueExterneRessourceUrl(
        "https://handschriftencensus.de/123");

    kulturObjektDokumentViewController.externeRessourceHinzufuegen();

    verify(kulturObjektDokumentBoundary, times(1))
        .externeRessourceHinzufuegen("1", ExterneRessourceTyp.HSC,
            "https://handschriftencensus.de/123", sperre);

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentEntsperren("1");

    verify(kulturObjektDokumentBoundary, times(1))
        .buildKulturObjektDokumentViewModel("1");

    verify(kodKerndatenModel, times(1))
        .init(any(KulturObjektDokumentViewModel.class));

    assertNull(kulturObjektDokumentViewController.getSperre());
    assertFalse(kulturObjektDokumentViewController.isAddExterneRessource());
    assertNull(kulturObjektDokumentViewController.getNeueExterneRessourceTyp());
    assertEquals("", kulturObjektDokumentViewController.getNeueExterneRessourceUrl());
  }

  @Test
  void testEnableAddForschungsdokumentation() throws Exception {
    prepareForschungsdokumentationController();
    Bearbeiter bearbeiter = new Bearbeiter("b_1", "Bearbeitername");
    Sperre sperre = testSperre(bearbeiter, "KODAddForschungsdokumentation");
    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);
    when(kulturObjektDokumentBoundary.kulturObjektDokumentSperren(
        bearbeiter, "KODAddForschungsdokumentation", "1")).thenReturn(sperre);

    kulturObjektDokumentViewController.enableAddForschungsdokumentation();

    assertTrue(kulturObjektDokumentViewController.isAddForschungsdokumentation());
    assertEquals(sperre, kulturObjektDokumentViewController.getSperre());
  }

  @Test
  void testDisableAddForschungsdokumentationClearsFormAndReleasesLock() throws Exception {
    prepareForschungsdokumentationController();
    kulturObjektDokumentViewController.setSperre(
        testSperre(new Bearbeiter("b_1", "Bearbeitername"),
            "KODAddForschungsdokumentation"));
    kulturObjektDokumentViewController.setAddForschungsdokumentation(true);
    kulturObjektDokumentViewController.setNeueForschungsdokumentationRessource("Forschung");
    kulturObjektDokumentViewController.setNeueForschungsdokumentationLink(
        "https://example.org/research");

    kulturObjektDokumentViewController.disableAddForschungsdokumentation();

    verify(kulturObjektDokumentBoundary).kulturObjektDokumentEntsperren("1");
    assertNull(kulturObjektDokumentViewController.getSperre());
    assertFalse(kulturObjektDokumentViewController.isAddForschungsdokumentation());
    assertEquals("",
        kulturObjektDokumentViewController.getNeueForschungsdokumentationRessource());
    assertEquals("", kulturObjektDokumentViewController.getNeueForschungsdokumentationLink());
  }

  @Test
  void testForschungsdokumentationHinzufuegen() throws Exception {
    KulturObjektDokumentViewModel viewModel = prepareForschungsdokumentationController();
    KODKerndatenModel kodKerndatenModel = mock(KODKerndatenModel.class);
    Sperre sperre = testSperre(
        new Bearbeiter("b_1", "Bearbeitername"), "KODAddForschungsdokumentation");
    kulturObjektDokumentViewController.setKODKerndatenModel(kodKerndatenModel);
    kulturObjektDokumentViewController.setSperre(sperre);
    kulturObjektDokumentViewController.setAddForschungsdokumentation(true);
    kulturObjektDokumentViewController.setNeueForschungsdokumentationRessource("Forschung");
    kulturObjektDokumentViewController.setNeueForschungsdokumentationLink(
        "https://example.org/research");
    when(kulturObjektDokumentBoundary.buildKulturObjektDokumentViewModel("1"))
        .thenReturn(Optional.of(viewModel));

    kulturObjektDokumentViewController.forschungsdokumentationHinzufuegen();

    verify(kulturObjektDokumentBoundary).forschungsdokumentationHinzufuegen(
        "1", "Forschung", "https://example.org/research", sperre);
    verify(kulturObjektDokumentBoundary).kulturObjektDokumentEntsperren("1");
    verify(kulturObjektDokumentBoundary).buildKulturObjektDokumentViewModel("1");
    verify(kodKerndatenModel).init(viewModel);
    assertNull(kulturObjektDokumentViewController.getSperre());
    assertFalse(kulturObjektDokumentViewController.isAddForschungsdokumentation());
    assertEquals("",
        kulturObjektDokumentViewController.getNeueForschungsdokumentationRessource());
    assertEquals("", kulturObjektDokumentViewController.getNeueForschungsdokumentationLink());
  }

  @Test
  void testForschungsdokumentationHinzufuegenKeepsFormAndLockOnError() throws Exception {
    prepareForschungsdokumentationController();
    Sperre sperre = testSperre(
        new Bearbeiter("b_1", "Bearbeitername"), "KODAddForschungsdokumentation");
    kulturObjektDokumentViewController.setSperre(sperre);
    kulturObjektDokumentViewController.setAddForschungsdokumentation(true);
    kulturObjektDokumentViewController.setNeueForschungsdokumentationRessource("Forschung");
    kulturObjektDokumentViewController.setNeueForschungsdokumentationLink("kein-link");
    doThrow(new KulturObjektDokumentException("Es ist kein Link."))
        .when(kulturObjektDokumentBoundary)
        .forschungsdokumentationHinzufuegen(
            "1", "Forschung", "kein-link", sperre);

    kulturObjektDokumentViewController.forschungsdokumentationHinzufuegen();

    verify(kulturObjektDokumentBoundary, never()).kulturObjektDokumentEntsperren(anyString());
    verify(kulturObjektDokumentBoundary, never())
        .buildKulturObjektDokumentViewModel(anyString());
    assertEquals(sperre, kulturObjektDokumentViewController.getSperre());
    assertTrue(kulturObjektDokumentViewController.isAddForschungsdokumentation());
    assertEquals("Forschung",
        kulturObjektDokumentViewController.getNeueForschungsdokumentationRessource());
    assertEquals("kein-link",
        kulturObjektDokumentViewController.getNeueForschungsdokumentationLink());
  }

  @Test
  void testForschungsdokumentationBestaetigenOpensConfirmDialog() {
    kulturObjektDokumentViewController.forschungsdokumentationBestaetigen();

    verify(primeFaces).executeScript("PF('addForschungsdokumentationConfirm').show()");
  }

  @Test
  void testForschungsdokumentationSpeichernDisabledForBlankRessource() {
    kulturObjektDokumentViewController.setNeueForschungsdokumentationRessource(null);
    assertTrue(kulturObjektDokumentViewController.isForschungsdokumentationSpeichernDisabled());

    kulturObjektDokumentViewController.setNeueForschungsdokumentationRessource(" \n\t ");
    assertTrue(kulturObjektDokumentViewController.isForschungsdokumentationSpeichernDisabled());

    kulturObjektDokumentViewController.setNeueForschungsdokumentationRessource("Forschung");
    assertFalse(kulturObjektDokumentViewController.isForschungsdokumentationSpeichernDisabled());
  }

  @Test
  void testOnPreDestroyReleasesForschungsdokumentationLockOwnedByCurrentUser()
      throws Exception {
    prepareForschungsdokumentationController();
    Bearbeiter bearbeiter = new Bearbeiter("b_1", "Bearbeitername");
    kulturObjektDokumentViewController.setSperre(
        testSperre(bearbeiter, "KODAddForschungsdokumentation"));
    kulturObjektDokumentViewController.setAddForschungsdokumentation(true);
    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);

    kulturObjektDokumentViewController.onPreDestroy();

    verify(kulturObjektDokumentBoundary).kulturObjektDokumentEntsperren("1");
    assertNull(kulturObjektDokumentViewController.getSperre());
  }

  private KulturObjektDokumentViewModel prepareForschungsdokumentationController() {
    KulturObjektDokumentViewModel viewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel()
        .withId("1")
        .build();
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);
    kulturObjektDokumentViewController.setId("1");
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(viewModel);
    return viewModel;
  }

  private Sperre testSperre(Bearbeiter bearbeiter, String sperreGrund) {
    return Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund(sperreGrund)
        .withBearbeiter(bearbeiter)
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();
  }

  @Test
  void testOnPreDestroyWithAddExterneRessource() throws Exception {

    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);
    kulturObjektDokumentViewController.setId("1");

    Bearbeiter bearbeiter = new Bearbeiter("b_1", "Bearbeitername");

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);

    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("KODAddExterneRessource")
        .withBearbeiter(bearbeiter)
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();

    kulturObjektDokumentViewController.setSperre(sperre);
    kulturObjektDokumentViewController.setAddExterneRessource(true);

    kulturObjektDokumentViewController.onPreDestroy();

    verify(kulturObjektDokumentBoundary, times(1))
        .kulturObjektDokumentEntsperren("1");

    assertNull(kulturObjektDokumentViewController.getSperre());
  }

  @Test
  void testGetExterneRessourceTypenShouldBeSortedAlphabeticallyWithSonstigesLast() {

    when(externalContext.getRequestLocale()).thenReturn(Locale.GERMAN);

    ExterneRessourceTyp[] result = kulturObjektDokumentViewController.getExterneRessourceTypen();

    assertNotNull(result);
    assertEquals(ExterneRessourceTyp.values().length, result.length);
    assertEquals(ExterneRessourceTyp.SONSTIGES, result[result.length - 1]);

    Collator collator = Collator.getInstance(Locale.GERMAN);

    List<String> actualLabels = Arrays.stream(result)
        .filter(typ -> typ != ExterneRessourceTyp.SONSTIGES)
        .map(typ -> getMessage(typ.getMessageKey()))
        .toList();

    List<String> expectedLabels = actualLabels.stream()
        .sorted(collator)
        .toList();

    assertEquals(expectedLabels, actualLabels);
  }

  @Test
  void testGetExterneRessourcenShouldBeSortedAlphabeticallyWithSonstigesLast() {
    when(externalContext.getRequestLocale()).thenReturn(Locale.GERMAN);

    String ebdbUrl = "https://example.org/ebdb";
    String qalamosUrl1 = "https://example.org/qalamos/1";
    String qalamosUrl2 = "https://example.org/qalamos/2";
    String sonstigesUrl = "https://example.org/sonstiges";

    KulturObjektDokumentViewModel viewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel()
        .withExterneRessourcen(List.of(
            new ExterneRessource(ExterneRessourceTyp.SONSTIGES, sonstigesUrl),
            new ExterneRessource(ExterneRessourceTyp.QALAMOS, qalamosUrl1),
            new ExterneRessource(ExterneRessourceTyp.QALAMOS, qalamosUrl2),
            new ExterneRessource(ExterneRessourceTyp.EBDB, ebdbUrl)))
        .build();

    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(viewModel);

    List<ExterneRessource> result = kulturObjektDokumentViewController.getExterneRessourcen();

    assertNotNull(result);
    assertEquals(4, result.size());

    // sorted by localized caption, SONSTIGES last; document order kept within one typ
    assertEquals(List.of(
            new ExterneRessource(ExterneRessourceTyp.EBDB, ebdbUrl),
            new ExterneRessource(ExterneRessourceTyp.QALAMOS, qalamosUrl1),
            new ExterneRessource(ExterneRessourceTyp.QALAMOS, qalamosUrl2),
            new ExterneRessource(ExterneRessourceTyp.SONSTIGES, sonstigesUrl)),
        result);
  }

  @Test
  void testGetExterneRessourcenWithoutViewModel() {
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(null);

    List<ExterneRessource> result = kulturObjektDokumentViewController.getExterneRessourcen();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testGetForschungsdokumentationenShouldBeSortedByRessource() {
    when(externalContext.getRequestLocale()).thenReturn(Locale.GERMAN);
    List<Forschungsdokumentation> original = List.of(
        new Forschungsdokumentation("Zebra", "https://example.org/z"),
        new Forschungsdokumentation("Überblick", null),
        new Forschungsdokumentation("Apfel", "https://example.org/a"));
    KulturObjektDokumentViewModel viewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel()
        .withForschungsdokumentationen(original)
        .build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(viewModel);

    assertEquals(List.of(original.get(2), original.get(1), original.get(0)),
        kulturObjektDokumentViewController.getForschungsdokumentationen());
    assertEquals(original, viewModel.getForschungsdokumentationen());
  }

  @Test
  void testGetForschungsdokumentationenKeepsEqualRessourceOrder() {
    Forschungsdokumentation first =
        new Forschungsdokumentation("Gleicher Titel", "https://example.org/1");
    Forschungsdokumentation second =
        new Forschungsdokumentation("Gleicher Titel", "https://example.org/2");
    KulturObjektDokumentViewModel viewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel()
        .withForschungsdokumentationen(List.of(second, first))
        .build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(viewModel);

    assertEquals(List.of(second, first),
        kulturObjektDokumentViewController.getForschungsdokumentationen());
  }

  @Test
  void testGetForschungsdokumentationenWithoutViewModel() {
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(null);

    assertTrue(kulturObjektDokumentViewController.getForschungsdokumentationen().isEmpty());
  }

  @Test
  void testExterneRessourceBestaetigenOpensTheConfirmDialog() {

    kulturObjektDokumentViewController.externeRessourceBestaetigen();

    verify(primeFaces).executeScript("PF('addExterneRessourceConfirm').show()");
  }

  @Test
  void testAddExterneRessourceConfirmMessageShouldBeEmptyWhenTypeIsMissing() {

    kulturObjektDokumentViewController.setNeueExterneRessourceTyp(null);
    kulturObjektDokumentViewController.setNeueExterneRessourceUrl(
        "https://handschriftencensus.de/123");

    assertEquals("", kulturObjektDokumentViewController.addExterneRessourceConfirmMessage());
  }

  @Test
  void testAddExterneRessourceConfirmMessageShouldContainTypeAndUrl() {

    when(externalContext.getRequestLocale()).thenReturn(Locale.GERMAN);

    kulturObjektDokumentViewController.setNeueExterneRessourceTyp(ExterneRessourceTyp.HSC);
    kulturObjektDokumentViewController.setNeueExterneRessourceUrl(
        "https://handschriftencensus.de/123");

    String message = kulturObjektDokumentViewController.addExterneRessourceConfirmMessage();

    assertTrue(message.contains("HSC"), message);
    assertTrue(message.contains("https://handschriftencensus.de/123"), message);
  }

  @Test
  void testExterneRessourceLoeschen() throws Exception {

    String url = "https://handschriftencensus.de/123";
    Bearbeiter bearbeiter = new Bearbeiter("b_1", "Bearbeitername");
    ExterneRessource ressource =
        new ExterneRessource(ExterneRessourceTyp.HSC, url);

    kulturObjektDokumentViewController.setId("1");
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);

    KODKerndatenModel kodKerndatenModel = mock(KODKerndatenModel.class);
    kulturObjektDokumentViewController.setKODKerndatenModel(kodKerndatenModel);

    KulturObjektDokumentViewModel viewModel =
        KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
            .withId("1")
            .build();

    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(viewModel);

    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("KODDeleteExterneRessource")
        .withBearbeiter(bearbeiter)
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);
    when(kulturObjektDokumentBoundary.kulturObjektDokumentSperren(
        bearbeiter, "KODDeleteExterneRessource", "1")).thenReturn(sperre);
    when(kulturObjektDokumentBoundary.buildKulturObjektDokumentViewModel("1"))
        .thenReturn(Optional.of(viewModel));

    kulturObjektDokumentViewController.externeRessourceLoeschen(ressource);

    verify(kulturObjektDokumentBoundary).externeRessourceLoeschen(
        "1", ExterneRessourceTyp.HSC, url, sperre);

    // the action acquired the Sperre itself, so it must release it again
    verify(kulturObjektDokumentBoundary).kulturObjektDokumentEntsperren("1");

    verify(kulturObjektDokumentBoundary)
        .buildKulturObjektDokumentViewModel("1");

    verify(kodKerndatenModel).init(viewModel);

    ArgumentCaptor<FacesMessage> messageCaptor =
        ArgumentCaptor.forClass(FacesMessage.class);

    verify(facesContext).addMessage(
        eq("hspGrowl"),
        messageCaptor.capture());

    FacesMessage message = messageCaptor.getValue();

    assertEquals(FacesMessage.SEVERITY_INFO, message.getSeverity());
    assertEquals("Die externe Ressource wurde erfolgreich gelöscht.", message.getSummary());
    assertEquals("", message.getDetail());
  }

  @Test
  void testExterneRessourceLoeschenKeepsTheSperreOfAnOpenEditForm() throws Exception {

    String url = "https://handschriftencensus.de/123";
    Bearbeiter bearbeiter = new Bearbeiter("b_1", "Bearbeitername");
    ExterneRessource ressource = new ExterneRessource(ExterneRessourceTyp.HSC, url);

    Sperre sperreDesFormulars = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("KODAddExterneRessource")
        .withBearbeiter(bearbeiter)
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();

    KulturObjektDokumentViewModel viewModel =
        KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
            .withId("1")
            .build();

    kulturObjektDokumentViewController.setId("1");
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);
    kulturObjektDokumentViewController.setKODKerndatenModel(mock(KODKerndatenModel.class));
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(viewModel);
    kulturObjektDokumentViewController.setAddExterneRessource(true);
    kulturObjektDokumentViewController.setSperre(sperreDesFormulars);

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);
    when(kulturObjektDokumentBoundary.buildKulturObjektDokumentViewModel("1"))
        .thenReturn(Optional.of(viewModel));

    kulturObjektDokumentViewController.externeRessourceLoeschen(ressource);

    verify(kulturObjektDokumentBoundary).externeRessourceLoeschen(
        "1", ExterneRessourceTyp.HSC, url, sperreDesFormulars);

    verify(kulturObjektDokumentBoundary, never())
        .kulturObjektDokumentSperren(any(), anyString(), anyString());

    verify(kulturObjektDokumentBoundary, never())
        .kulturObjektDokumentEntsperren(anyString());
  }

  @Test
  void testExterneRessourceLoeschenReleasesAPreExistingSperreItDidNotOwn() throws Exception {

    String url = "https://handschriftencensus.de/123";
    Bearbeiter bearbeiter = new Bearbeiter("b_1", "Bearbeitername");
    ExterneRessource ressource = new ExterneRessource(ExterneRessourceTyp.HSC, url);

    Sperre vorhandeneSperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("TESTGRUND")
        .withBearbeiter(bearbeiter)
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();

    KulturObjektDokumentViewModel viewModel =
        KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
            .withId("1")
            .build();

    kulturObjektDokumentViewController.setId("1");
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);
    kulturObjektDokumentViewController.setKODKerndatenModel(mock(KODKerndatenModel.class));
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(viewModel);

    // picked up by setup() on page load, owned by no form of this view
    kulturObjektDokumentViewController.setSperre(vorhandeneSperre);

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);
    when(kulturObjektDokumentBoundary.kulturObjektDokumentSperren(
        bearbeiter, "KODDeleteExterneRessource", "1")).thenReturn(vorhandeneSperre);
    when(kulturObjektDokumentBoundary.buildKulturObjektDokumentViewModel("1"))
        .thenReturn(Optional.of(viewModel));

    kulturObjektDokumentViewController.externeRessourceLoeschen(ressource);

    verify(kulturObjektDokumentBoundary).kulturObjektDokumentEntsperren("1");
  }

  @Test
  void testBearbeitungAktivReflectsTheOpenEditForms() {

    assertFalse(kulturObjektDokumentViewController.isBearbeitungAktiv());

    kulturObjektDokumentViewController.setAddExterneRessource(true);
    assertTrue(kulturObjektDokumentViewController.isBearbeitungAktiv());
    kulturObjektDokumentViewController.setAddExterneRessource(false);

    kulturObjektDokumentViewController.setEditKerndaten(true);
    assertTrue(kulturObjektDokumentViewController.isBearbeitungAktiv());
    kulturObjektDokumentViewController.setEditKerndaten(false);

    kulturObjektDokumentViewController.setEditSignature(true);
    assertTrue(kulturObjektDokumentViewController.isBearbeitungAktiv());
    kulturObjektDokumentViewController.setEditSignature(false);

    kulturObjektDokumentViewController.setAddAlternativeSignature(true);
    assertTrue(kulturObjektDokumentViewController.isBearbeitungAktiv());
  }

  @Test
  void testExterneRessourceLoeschenError() throws Exception {

    String url = "https://handschriftencensus.de/123";
    Bearbeiter bearbeiter = new Bearbeiter("b_1", "Bearbeitername");
    ExterneRessource ressource =
        new ExterneRessource(ExterneRessourceTyp.HSC, url);

    kulturObjektDokumentViewController.setId("1");
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);

    KulturObjektDokumentViewModel viewModel =
        KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
            .withId("1")
            .build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(viewModel);

    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("KODDeleteExterneRessource")
        .withBearbeiter(bearbeiter)
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);
    when(kulturObjektDokumentBoundary.kulturObjektDokumentSperren(
        bearbeiter, "KODDeleteExterneRessource", "1")).thenReturn(sperre);

    doThrow(new KulturObjektDokumentException("Delete failed"))
        .when(kulturObjektDokumentBoundary)
        .externeRessourceLoeschen(
            "1", ExterneRessourceTyp.HSC, url, sperre);

    kulturObjektDokumentViewController.externeRessourceLoeschen(ressource);

    // even on failure the self-acquired Sperre must be released
    verify(kulturObjektDokumentBoundary).kulturObjektDokumentEntsperren("1");

    verify(kulturObjektDokumentBoundary, never())
        .buildKulturObjektDokumentViewModel(anyString());

    ArgumentCaptor<FacesMessage> messageCaptor =
        ArgumentCaptor.forClass(FacesMessage.class);

    verify(facesContext).addMessage(
        eq("hspGrowl"),
        messageCaptor.capture());

    FacesMessage message = messageCaptor.getValue();

    assertEquals(FacesMessage.SEVERITY_ERROR, message.getSeverity());
    assertEquals("Die externe Ressource konnte nicht gelöscht werden.", message.getSummary());
    assertEquals("Delete failed", message.getDetail());
  }

  @Test
  void testExterneRessourceLoeschenGesperrt() throws Exception {

    String url = "https://handschriftencensus.de/123";
    Bearbeiter bearbeiter = new Bearbeiter("b_1", "Bearbeitername");
    Bearbeiter sperrenderBearbeiter =
        new Bearbeiter("b_2", "Sperrender Bearbeiter");

    ExterneRessource ressource =
        new ExterneRessource(ExterneRessourceTyp.HSC, url);

    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("TESTGRUND")
        .withBearbeiter(sperrenderBearbeiter)
        .withSperreTyp(SperreTyp.MANUAL)
        .withStartDatum(LocalDateTime.of(2026, 7, 30, 10, 0))
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();

    SperreAlreadyExistException exception =
        new SperreAlreadyExistException(
            "Sperre already exists",
            List.of(sperre));

    kulturObjektDokumentViewController.setId("1");
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);

    KulturObjektDokumentViewModel viewModel =
        KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
            .withId("1")
            .build();
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(viewModel);

    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);

    doThrow(exception)
        .when(kulturObjektDokumentBoundary)
        .kulturObjektDokumentSperren(
            bearbeiter, "KODDeleteExterneRessource", "1");

    kulturObjektDokumentViewController.externeRessourceLoeschen(ressource);

    assertEquals(sperre, kulturObjektDokumentViewController.getSperre());

    // acquiring failed, so the delete must not run at all
    verify(kulturObjektDokumentBoundary, never())
        .externeRessourceLoeschen(anyString(), any(), anyString(), any());

    verify(kulturObjektDokumentBoundary, never())
        .buildKulturObjektDokumentViewModel(anyString());

    ArgumentCaptor<FacesMessage> messageCaptor =
        ArgumentCaptor.forClass(FacesMessage.class);

    verify(primeFacesDialog).showMessageDynamic(
        messageCaptor.capture());

    FacesMessage message = messageCaptor.getValue();

    assertEquals(FacesMessage.SEVERITY_ERROR, message.getSeverity());
    assertTrue(message.getDetail().contains("Sperrender Bearbeiter"));
  }

  @Test
  void testDeleteExterneRessourceConfirmMessageShouldBeEmptyForNull() {

    assertEquals(
        "",
        kulturObjektDokumentViewController
            .deleteExterneRessourceConfirmMessage(null));
  }

  @Test
  void testDeleteExterneRessourceConfirmMessageShouldContainTypeAndUrl() {

    when(externalContext.getRequestLocale()).thenReturn(Locale.GERMAN);

    String url = "https://handschriftencensus.de/123";

    ExterneRessource ressource = new ExterneRessource(ExterneRessourceTyp.HSC, url);

    String message =
        kulturObjektDokumentViewController
            .deleteExterneRessourceConfirmMessage(ressource);

    assertTrue(message.contains("HSC"), message);
    assertTrue(message.contains(url), message);
  }

  @Test
  void testForschungsdokumentationLoeschenDelegatesToBoundary() throws Exception {
    Bearbeiter bearbeiter = new Bearbeiter("b_1", "Bearbeitername");
    Forschungsdokumentation forschungsdokumentation =
        new Forschungsdokumentation("Forschung", "https://example.org/research");
    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("KODDeleteForschungsdokumentation")
        .withBearbeiter(bearbeiter)
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();
    KulturObjektDokumentViewModel viewModel =
        KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
            .withId("1")
            .build();

    kulturObjektDokumentViewController.setId("1");
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);
    kulturObjektDokumentViewController.setKODKerndatenModel(mock(KODKerndatenModel.class));
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(viewModel);
    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);
    when(kulturObjektDokumentBoundary.kulturObjektDokumentSperren(
        bearbeiter, "KODDeleteForschungsdokumentation", "1")).thenReturn(sperre);
    when(kulturObjektDokumentBoundary.buildKulturObjektDokumentViewModel("1"))
        .thenReturn(Optional.of(viewModel));

    kulturObjektDokumentViewController.forschungsdokumentationLoeschen(forschungsdokumentation);

    verify(kulturObjektDokumentBoundary).forschungsdokumentationLoeschen(
        "1", "Forschung", "https://example.org/research", sperre);
    verify(kulturObjektDokumentBoundary).kulturObjektDokumentEntsperren("1");
    verify(kulturObjektDokumentBoundary).buildKulturObjektDokumentViewModel("1");
  }

  @Test
  void testForschungsdokumentationLoeschenKeepsTheSperreOfAnOpenEditForm() throws Exception {
    Bearbeiter bearbeiter = new Bearbeiter("b_1", "Bearbeitername");
    Forschungsdokumentation documentation =
        new Forschungsdokumentation("Forschung", null);
    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("KODAddForschungsdokumentation")
        .withBearbeiter(bearbeiter)
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();

    kulturObjektDokumentViewController.setId("1");
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);
    kulturObjektDokumentViewController.setKODKerndatenModel(mock(KODKerndatenModel.class));
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(
        KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
            .withId("1").build());
    kulturObjektDokumentViewController.setAddForschungsdokumentation(true);
    kulturObjektDokumentViewController.setSperre(sperre);

    kulturObjektDokumentViewController.forschungsdokumentationLoeschen(documentation);

    verify(kulturObjektDokumentBoundary).forschungsdokumentationLoeschen(
        "1", "Forschung", null, sperre);
    verify(kulturObjektDokumentBoundary, never())
        .kulturObjektDokumentEntsperren(anyString());
  }

  @Test
  void testForschungsdokumentationLoeschenReleasesPreExistingSperre() throws Exception {
    Bearbeiter bearbeiter = new Bearbeiter("b_1", "Bearbeitername");
    Forschungsdokumentation documentation =
        new Forschungsdokumentation("Forschung", null);
    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("OTHER")
        .withBearbeiter(bearbeiter)
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();

    kulturObjektDokumentViewController.setId("1");
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);
    kulturObjektDokumentViewController.setKODKerndatenModel(mock(KODKerndatenModel.class));
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(
        KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
            .withId("1").build());
    kulturObjektDokumentViewController.setSperre(sperre);
    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);
    when(kulturObjektDokumentBoundary.kulturObjektDokumentSperren(
        bearbeiter, "KODDeleteForschungsdokumentation", "1")).thenReturn(sperre);

    kulturObjektDokumentViewController.forschungsdokumentationLoeschen(documentation);

    verify(kulturObjektDokumentBoundary).kulturObjektDokumentEntsperren("1");
  }

  @Test
  void testForschungsdokumentationLoeschenErrorReleasesSperre() throws Exception {
    Bearbeiter bearbeiter = new Bearbeiter("b_1", "Bearbeitername");
    Forschungsdokumentation documentation =
        new Forschungsdokumentation("Forschung", "https://example.org/research");
    Sperre sperre = Sperre.newBuilder()
        .withId("sp_1")
        .withSperreGrund("KODDeleteForschungsdokumentation")
        .withBearbeiter(bearbeiter)
        .withSperreTyp(SperreTyp.MANUAL)
        .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
        .build();

    kulturObjektDokumentViewController.setId("1");
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);
    kulturObjektDokumentViewController.setKODKerndatenModel(mock(KODKerndatenModel.class));
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(
        KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
            .withId("1").build());
    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);
    when(kulturObjektDokumentBoundary.kulturObjektDokumentSperren(
        bearbeiter, "KODDeleteForschungsdokumentation", "1")).thenReturn(sperre);
    doThrow(new KulturObjektDokumentException("Delete failed"))
        .when(kulturObjektDokumentBoundary)
        .forschungsdokumentationLoeschen(
            "1", "Forschung", "https://example.org/research", sperre);

    kulturObjektDokumentViewController.forschungsdokumentationLoeschen(documentation);

    verify(kulturObjektDokumentBoundary).kulturObjektDokumentEntsperren("1");
    verify(kulturObjektDokumentBoundary, never())
        .buildKulturObjektDokumentViewModel(anyString());
  }

  @Test
  void testForschungsdokumentationLoeschenDoesNotRunWhenKODIsLocked() throws Exception {
    Bearbeiter bearbeiter = new Bearbeiter("b_1", "Bearbeitername");
    Bearbeiter lockingBearbeiter = new Bearbeiter("b_2", "Sperrender Bearbeiter");
    Forschungsdokumentation documentation =
        new Forschungsdokumentation("Forschung", null);
    SperreAlreadyExistException exception = new SperreAlreadyExistException(
        "Sperre already exists",
        List.of(Sperre.newBuilder()
            .withId("sp_1")
            .withSperreGrund("OTHER")
            .withBearbeiter(lockingBearbeiter)
            .withSperreTyp(SperreTyp.MANUAL)
            .addSperreEintrag(new SperreEintrag("1", SperreDokumentTyp.KOD))
            .build()));

    kulturObjektDokumentViewController.setId("1");
    kulturObjektDokumentViewController.setKulturObjektDokumentBoundary(
        kulturObjektDokumentBoundary);
    kulturObjektDokumentViewController.setBearbeiterBoundary(bearbeiterBoundary);
    kulturObjektDokumentViewController.setKulturObjektDokumentViewModel(
        KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
            .withId("1").build());
    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);
    doThrow(exception).when(kulturObjektDokumentBoundary).kulturObjektDokumentSperren(
        bearbeiter, "KODDeleteForschungsdokumentation", "1");

    kulturObjektDokumentViewController.forschungsdokumentationLoeschen(documentation);

    verify(kulturObjektDokumentBoundary, never())
        .forschungsdokumentationLoeschen(anyString(), anyString(), any(), any());
    verify(kulturObjektDokumentBoundary, never())
        .buildKulturObjektDokumentViewModel(anyString());
  }
}
