package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.DigitalisatErfassenController.FACES_REDIRECT;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.DigitalisatErfassenController.KOD_PAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
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
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
  NormdatenReferenzBoundary normdatenReferenzBoundary = Mockito.mock(
      NormdatenReferenzBoundary.class);
  DigitalisatErfassenModel digitalisatErfassenModel = new DigitalisatErfassenModel();
  BearbeiterBoundary bearbeiterBoundary = Mockito.mock(BearbeiterBoundary.class);

  DigitalisatErfassenController controller = new DigitalisatErfassenController(
      kulturObjektDokumentBoundary,
      normdatenReferenzBoundary,
      bearbeiterBoundary,
      digitalisatErfassenModel);
  public static final String ID = UUID.randomUUID().toString();
  public static final String CONCEPT_TYPE_NAME = "SourceTyp";
  public static final String URI = "";
  public static final String NAME = "";

  public static final String CONCEPTSCHEME_ID = UUID.randomUUID().toString();
  public static final String CONCEPTSCHEME_TYPE_NAME = "DIGI";
  public static final String CONCEPTSCHEME_URI = "";
  public static final String CONCEPTSCHEME_NAME = "";

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

    when(kulturObjektDokumentBoundary.findSperreForKulturObjektDokument(anyString())).thenReturn(
        Optional.of(sperre));

    controller.setup();

    verify(kulturObjektDokumentBoundary, times(1)).findSperreForKulturObjektDokument(anyString());

    verify(kulturObjektDokumentBoundary, times(1)).buildKulturObjektDokumentViewModel("1");

    assertEquals(kulturObjektDokumentViewModel, controller.getKulturObjektDokumentViewModel());

    assertEquals(digitalisatViewModel, controller.getDigitalisatViewModel());

    verify(normdatenReferenzBoundary, times(0)).findKoerperschaftenByOrtId(anyString(),
        anyBoolean());

  }

  @Test
  void testdigitalisatHinzufuegen()
      throws KulturObjektDokumentException, DokumentSperreException, IOException {

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

    String view = controller.abbrechen();

    verify(kulturObjektDokumentBoundary, times(1)).kulturObjektDokumentEntsperren(any());

    assertEquals(KOD_PAGE + FACES_REDIRECT + "&id=1", view);

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

    when(kulturObjektDokumentBoundary.findSperreForKulturObjektDokument(anyString())).thenReturn(
        Optional.of(sperre));

    controller.findAndCheckSperre();

    controller.kodEntsperren();

    verify(kulturObjektDokumentBoundary, times(1)).kulturObjektDokumentEntsperren(any());
  }

  @Test
  void testPlaceChangedListener() {

    NormdatenReferenz ort = new NormdatenReferenz("1", "Berlin", "GND1",
        GNDEntityFact.PLACE_TYPE_NAME);

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


  @Test
  void testConceptTypenHinzufuegen() {
    NormdatenReferenz activeConceptType = new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId(ID)
            .withTypeName(CONCEPT_TYPE_NAME)
            .withName(NAME)
            .withUri(URI)
            .build();
    controller.setDigitalisatViewModel(new DigitalisatViewModel());
    controller.setActiveConceptType(activeConceptType);
    controller.conceptTypenHinzufuegen();

    assertEquals(1,controller.getDigitalisatViewModel().getDigitalisatTypKonzepte().size());

  }

  @Test
  void testSelectConceptForDeletion(){
    NormdatenReferenz selectedConcept = new NormdatenReferenz.NormdatenReferenzBuilder().withId(ID).build();
    controller.setSelectedConcept(selectedConcept);
    controller.setDigitalisatViewModel(new DigitalisatViewModel());
    controller.setSelectedConcept(selectedConcept);
    controller.getSelectedConcept();

    assertNotNull(controller.getSelectedConcept());

  }

  @Test
  void testRemoveSelectedConceptFromView(){
    NormdatenReferenz activeConceptType = new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId(ID)
            .withTypeName(CONCEPT_TYPE_NAME)
            .withName(NAME)
            .withUri(URI)
            .build();
    NormdatenReferenz selectedConcept = new NormdatenReferenz.NormdatenReferenzBuilder().withId(ID).build();
    controller.setSelectedConcept(selectedConcept);

      controller.setDigitalisatViewModel(new DigitalisatViewModel());
      controller.setActiveConceptType(activeConceptType);
      controller.removeSelectedConceptFromView();

      assertEquals(0,controller.getDigitalisatViewModel().getDigitalisatTypKonzepte().size());

  }

  @Test
  void testConceptTypenZuruecksetzen(){

    NormdatenReferenz activeConceptType = new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId(ID)
            .withTypeName(CONCEPT_TYPE_NAME)
            .withName(NAME)
            .withUri(URI)
            .build();

    NormdatenReferenz activeSchemeType = new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId(ID)
            .withTypeName(CONCEPTSCHEME_TYPE_NAME)
            .withName(NAME)
            .withUri(URI)
            .build();

    controller.setDigitalisatViewModel(new DigitalisatViewModel());
    controller.setActiveConceptType(activeConceptType);
    controller.setActiveSchemeType(activeSchemeType);

    controller.conceptTypenHinzufuegen();

    assertEquals(1,controller.getDigitalisatViewModel().getDigitalisatTypKonzepte().size());
    assertNull(controller.getActiveConceptType());

    assertNull(controller.getActiveConceptType());

  }

}
