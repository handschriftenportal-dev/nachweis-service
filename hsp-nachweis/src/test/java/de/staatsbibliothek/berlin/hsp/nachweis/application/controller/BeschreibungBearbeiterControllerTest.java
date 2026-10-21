package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.primefaces.PrimeFaces;

public class BeschreibungBearbeiterControllerTest {

  private static final Identifikation IDENTIFIKATION = new IdentifikationBuilder()
      .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
      .withIdent("1-1-1")
      .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
      .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
      .build();

  BeschreibungBearbeiterController beschreibungBearbeiterController;

  BeschreibungsBoundary beschreibungsBoundary = mock(BeschreibungsBoundary.class);

  BearbeiterBoundary bearbeiterBoundary = mock(BearbeiterBoundary.class);
  ExternalContext externalContext = mock(ExternalContext.class);
  MockedStatic<FacesContext> contextStatic = mockStatic(FacesContext.class);
  FacesContext facesContext = mock(FacesContext.class);
  MockedStatic<PrimeFaces> primeFacesStatic = mockStatic(PrimeFaces.class);
  PrimeFaces primeFaces = mock(PrimeFaces.class);
  PrimeFaces.Dialog primeFacesDialog = mock(PrimeFaces.Dialog.class);
  Flash flash = mock(Flash.class);


  @BeforeEach
  void beforeEach() {
    contextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
    when(facesContext.getExternalContext()).thenReturn(externalContext);
    primeFacesStatic.when(PrimeFaces::current).thenReturn(primeFaces);
    when(primeFaces.dialog()).thenReturn(primeFacesDialog);

  }

  @AfterEach
  void resetMocks() {
    primeFacesStatic.close();
    contextStatic.close();
  }


  @Test
  void testCreation() {

    beschreibungBearbeiterController = new BeschreibungBearbeiterController(beschreibungsBoundary,
        bearbeiterBoundary);

    assertNotNull(beschreibungBearbeiterController);
    assertNull(beschreibungBearbeiterController.getSelectedBearbeiter());
  }

  @Test
  void testSetup() {

    beschreibungBearbeiterController = new BeschreibungBearbeiterController(beschreibungsBoundary,
        bearbeiterBoundary);

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("123", "kod-1",
        IDENTIFIKATION).build();

    beschreibungBearbeiterController.setBeschreibungID(beschreibung.getId());

    beschreibungBearbeiterController.setup();

    assertEquals("123",
        beschreibungBearbeiterController.getBeschreibungID());

  }

  @Test
  void testSetSelectedBearbeiter() {
    beschreibungBearbeiterController = new BeschreibungBearbeiterController(beschreibungsBoundary,
        bearbeiterBoundary);

    assertNull(beschreibungBearbeiterController.getSelectedBearbeiter());

    beschreibungBearbeiterController.setSelectedBearbeiter(
        new Bearbeiter("1", "1"));

    assertNotNull(beschreibungBearbeiterController.getSelectedBearbeiter());

    assertEquals("1",
        beschreibungBearbeiterController.getSelectedBearbeiter().getBearbeitername());

  }

  @Test
  void testdialogSchliessen() {
    beschreibungBearbeiterController = new BeschreibungBearbeiterController(beschreibungsBoundary,
        bearbeiterBoundary);

    beschreibungBearbeiterController.dialogSchliessen("bearbeiterZuweisenForm");
  }

  @Test
  void testbearbeiterZuweisen() throws BeschreibungsException {

    beschreibungBearbeiterController = new BeschreibungBearbeiterController(beschreibungsBoundary,
        bearbeiterBoundary);
    beschreibungBearbeiterController.setBeschreibungID("11");
    beschreibungBearbeiterController.setSelectedBearbeiter(
        new Bearbeiter.BearbeiterBuilder().withBearbeitername("b-00").build());
    when(facesContext.getExternalContext().getFlash()).thenReturn(flash);
    when(facesContext.getExternalContext().getFlash().isKeepMessages()).thenReturn(true);

    beschreibungBearbeiterController.bearbeiterZuweisen();

    verify(beschreibungsBoundary,
        times(1)).addBearbeiterToBeschreibung(any(), anySet());

  }

  @Test
  void testbearbeiterEntziehen() throws BeschreibungsException, DokumentSperreException {

    beschreibungBearbeiterController = new BeschreibungBearbeiterController(beschreibungsBoundary,
        null);
    beschreibungBearbeiterController.setBeschreibungID("555");

    Bearbeiter bearbeiter = new Bearbeiter.BearbeiterBuilder()
        .withBearbeitername("bearbeitername").build();

    beschreibungBearbeiterController.setSelectedBearbeiter(bearbeiter);
    when(facesContext.getExternalContext().getFlash()).thenReturn(flash);
    when(facesContext.getExternalContext().getFlash().isKeepMessages()).thenReturn(true);

    beschreibungBearbeiterController.bearbeiterEntziehen();

    verify(beschreibungsBoundary,
        times(1)).removeBearbeiterFromBeschreibung("555", "bearbeitername");

  }

}
