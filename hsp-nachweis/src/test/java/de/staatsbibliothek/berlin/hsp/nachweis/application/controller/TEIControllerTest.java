package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.TEIController.CALLBACK_PARAMS_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DatenDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import jakarta.faces.context.FacesContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class TEIControllerTest {

  private static final Identifikation IDENTIFIKATION = new IdentifikationBuilder()
      .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
      .withIdent("1-1-1")
      .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
      .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
      .build();

  TEIController teiController = new TEIController();

  FacesContext context = Mockito.mock(FacesContext.class);


  BeschreibungsRepository beschreibungsRepository = Mockito.mock(BeschreibungsRepository.class);

  KulturObjektDokumentRepository kulturObjektDokumentRepository = Mockito.mock(
      KulturObjektDokumentRepository.class);

  MockedStatic<FacesContext> facesContextCurrentInstance;

  @BeforeEach
  public void setUp() {
    teiController.setBeschreibungsRepository(beschreibungsRepository);
    teiController.setKulturObjektDokumentRepository(kulturObjektDokumentRepository);
    facesContextCurrentInstance = Mockito.mockStatic(FacesContext.class);
    facesContextCurrentInstance.when(FacesContext::getCurrentInstance).thenReturn(context);
  }

  @AfterEach
  public void derigisterStaticMock() {
    facesContextCurrentInstance.close();
  }

  @Test
  void loadWithIdBeschreibung() {

    final String testIdBeschreibung = "123_Beschreibung";

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder(testIdBeschreibung, "kod1",
        IDENTIFIKATION).build();

    Map<String, String> callback_params = new HashMap<>();
    callback_params.put(TEIController.DATEN_DOKUMENT_ID, testIdBeschreibung);
    callback_params.put(TEIController.DATEN_DOKUMENT_TYP, DatenDokumentTyp.BESCHREIBUNG.toString());
    Map<Object, Object> attributes = new HashMap<>();
    attributes.put(CALLBACK_PARAMS_KEY, callback_params);

    when(context.getAttributes()).thenReturn(attributes);
    when(beschreibungsRepository.findByIdOptional(testIdBeschreibung)).thenReturn(
        Optional.ofNullable(beschreibung));

    teiController.loadWithID();

    assertEquals(testIdBeschreibung, teiController.getId());
  }

  @Test
  void loadWithIdKOD() {

    final String testIdKOD = "123_KOD";

    KulturObjektDokument kulturObjektDokument = new KulturObjektDokument.KulturObjektDokumentBuilder().withId(
        testIdKOD).build();

    Map<String, String> callback_params = new HashMap<>();
    callback_params.put(TEIController.DATEN_DOKUMENT_ID, testIdKOD);
    callback_params.put(TEIController.DATEN_DOKUMENT_TYP, DatenDokumentTyp.KOD.toString());
    Map<Object, Object> attributes = new HashMap<>();
    attributes.put(CALLBACK_PARAMS_KEY, callback_params);

    when(context.getAttributes()).thenReturn(attributes);
    when(kulturObjektDokumentRepository.findByIdOptional(testIdKOD)).thenReturn(
        Optional.ofNullable(kulturObjektDokument));

    teiController.loadWithID();

    assertEquals(testIdKOD, teiController.getId());
  }
}
