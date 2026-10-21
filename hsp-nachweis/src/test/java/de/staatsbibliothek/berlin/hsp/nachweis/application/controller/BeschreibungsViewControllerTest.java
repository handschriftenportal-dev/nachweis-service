package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.PublikationsService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.stork.StorkDiscovery;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.tei.TeiSnapshotZipper;
import de.staatsbibliothek.berlin.javaee.authentication.entity.Authentication;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.primefaces.PrimeFaces;
import org.primefaces.model.StreamedContent;

/**
 * Created by Christoph Marten on 29.09.2020 at 13:03
 */
class BeschreibungsViewControllerTest {

  ExternalContext externalContext = mock(ExternalContext.class);
  BeschreibungsBoundary beschreibungsBoundary = mock(BeschreibungsBoundary.class);
  BearbeiterBoundary bearbeiterBoundary = mock(BearbeiterBoundary.class);
  PublikationsService publikationsService = mock(PublikationsService.class);

  Authentication authentication = mock(Authentication.class);
  BeschreibungsViewController beschreibungsViewController = new BeschreibungsViewController("", "",
      beschreibungsBoundary, bearbeiterBoundary, publikationsService, null, authentication);

  @Test
  void testConstruction() {
    Map<String, String> requestParameterMap = externalContext.getRequestParameterMap();
    requestParameterMap.put("id", "1");

    when(externalContext.getRequestParameterMap()).thenReturn(requestParameterMap);
    when(beschreibungsBoundary.buildBeschreibungsViewModel("1")).thenReturn(
        Optional.of(new BeschreibungsViewModel.BeschreibungsViewBuilder()
            .withID("1")
            .withTitel("Flavius Josephus, de bello Judaico")
            .withSignatur("Mscr.Dresd.A.111")
            .withBestandhaltendeInstitutionName(
                "Sächsische Landesbibliothek - Staats- und Universitätsbibliothek Dresden")
            .withBestandhaltendeInstitutionOrt("Berlin")
            .build()));

    FacesContext facesContext = mock(FacesContext.class);
    when(facesContext.getExternalContext()).thenReturn(externalContext);

    try (MockedStatic<FacesContext> facesContextStatic = mockStatic(FacesContext.class)) {
      facesContextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
      beschreibungsViewController.setup();
    }

    String id = beschreibungsViewController.getBeschreibungsViewModel().getId();
    String bestandhaltendeInstitutionName = beschreibungsViewController.getBeschreibungsViewModel()
        .getBestandhaltendeInstitutionName();
    String bestandhaltendeInstitutionOrt = beschreibungsViewController.getBeschreibungsViewModel()
        .getBestandhaltendeInstitutionOrt();
    String signatur = beschreibungsViewController.getBeschreibungsViewModel().getSignatur();
    String titel = beschreibungsViewController.getBeschreibungsViewModel().getTitel();

    assertEquals("1", id);
    assertEquals("Sächsische Landesbibliothek - Staats- und Universitätsbibliothek Dresden",
        bestandhaltendeInstitutionName);
    assertEquals("Berlin", bestandhaltendeInstitutionOrt);
    assertEquals("Mscr.Dresd.A.111", signatur);
    assertEquals("Flavius Josephus, de bello Judaico", titel);
  }

  @Test
  void testGetTeiContent() {
    Map<String, String> requestParameterMap = externalContext.getRequestParameterMap();
    requestParameterMap.put("id", "1");

    when(externalContext.getRequestParameterMap()).thenReturn(requestParameterMap);
    when(beschreibungsBoundary.buildBeschreibungsViewModel("1")).thenReturn(
        Optional.of(new BeschreibungsViewModel.BeschreibungsViewBuilder()
            .withID("1")
            .withTEIXML("<TEI/>")
            .build()));

    FacesContext facesContext = mock(FacesContext.class);
    when(facesContext.getExternalContext()).thenReturn(externalContext);

    try (MockedStatic<FacesContext> facesContextStatic = mockStatic(FacesContext.class)) {
      facesContextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
      beschreibungsViewController.setup();
    }

    StreamedContent teiContent = beschreibungsViewController.getTeiContent();
    assertNotNull(teiContent);
    assertEquals("tei-1.xml", teiContent.getName());
    assertEquals("text/xml", teiContent.getContentType());
    assertEquals(6, teiContent.getContentLength());
  }

  @Test
  void getTeiSnapshotContent_happyPath() {
    String beschreibungId = "B1";
    byte[] zipBytes = TeiSnapshotZipper.zip("<TEI/>", beschreibungId);
    when(publikationsService.findTeiSnapshotZip(beschreibungId)).thenReturn(Optional.of(zipBytes));
    beschreibungsViewController.setBeschreibungsViewModel(
        new BeschreibungsViewModel.BeschreibungsViewBuilder().withID(beschreibungId).build());

    StreamedContent content = beschreibungsViewController.getTeiSnapshotContent();

    assertNotNull(content);
    assertEquals("tei-B1.zip", content.getName());
    assertEquals("application/zip", content.getContentType());
    assertEquals(zipBytes.length, content.getContentLength());
  }

  @Test
  void getTeiSnapshotContent_returnsNullWhenSnapshotMissing() {
    String beschreibungId = "B1";
    when(publikationsService.findTeiSnapshotZip(beschreibungId)).thenReturn(Optional.empty());
    beschreibungsViewController.setBeschreibungsViewModel(
        new BeschreibungsViewModel.BeschreibungsViewBuilder().withID(beschreibungId).build());

    PrimeFaces primeFaces = mock(PrimeFaces.class);
    PrimeFaces.Dialog dialog = mock(PrimeFaces.Dialog.class);
    when(primeFaces.dialog()).thenReturn(dialog);
    try (MockedStatic<PrimeFaces> primeFacesStatic = mockStatic(PrimeFaces.class)) {
      primeFacesStatic.when(PrimeFaces::current).thenReturn(primeFaces);
      assertNull(beschreibungsViewController.getTeiSnapshotContent());
    }
  }

  @Test
  void testgetOauth2Token() {

    beschreibungsViewController.getOauth2Token();

    verify(authentication, times(1)).getAccessToken();
  }

  @Test
  void getNormdatenURL_resolvesViaStorkDiscovery() {
    StorkDiscovery storkDiscovery = mock(StorkDiscovery.class);
    String url = "http://b-dev1047.pk.de:9299/rest/graphql";
    when(storkDiscovery.resolveUri(URI.create("stork://normdaten-service/rest/graphql")))
        .thenReturn(URI.create(url));
    BeschreibungsViewController controller = new BeschreibungsViewController(
        "stork://normdaten-service/rest/graphql", "",
        beschreibungsBoundary, bearbeiterBoundary, publikationsService, storkDiscovery,
        authentication);

    assertEquals(url, controller.getNormdatenURL());
  }

  @Test
  void getNormdatenURL_storkDiscoveryThrows_returnsOriginal() {
    StorkDiscovery storkDiscovery = mock(StorkDiscovery.class);
    String originalUrl = "stork://normdaten-service/rest/graphql";
    when(storkDiscovery.resolveUri(URI.create(originalUrl)))
        .thenThrow(new IllegalStateException("boom"));
    BeschreibungsViewController controller = new BeschreibungsViewController(
        originalUrl, "",
        beschreibungsBoundary, bearbeiterBoundary, publikationsService, storkDiscovery,
        authentication);

    assertEquals(originalUrl, controller.getNormdatenURL());
  }

  @Test
  void getValidationURL_appendsTeiXmlValidatePath() {
    StorkDiscovery storkDiscovery = mock(StorkDiscovery.class);
    String url = "http://b-dev1047.pk.de:9296/rest";
    when(storkDiscovery.resolveUri(URI.create("stork://importservice/rest")))
        .thenReturn(URI.create(url));
    BeschreibungsViewController controller = new BeschreibungsViewController(
        "", "stork://importservice/rest",
        beschreibungsBoundary, bearbeiterBoundary, publikationsService, storkDiscovery,
        authentication);

    assertEquals(url + "/tei-xml/validate",
        controller.getValidationURL());
  }

  @Test
  void getValidationURL_storkDiscoveryThrows_appendsToOriginal() {
    StorkDiscovery storkDiscovery = mock(StorkDiscovery.class);
    String originalUrl = "stork://importservice/rest";
    when(storkDiscovery.resolveUri(URI.create(originalUrl)))
        .thenThrow(new IllegalStateException("boom"));
    BeschreibungsViewController controller = new BeschreibungsViewController(
        "", originalUrl,
        beschreibungsBoundary, bearbeiterBoundary, publikationsService, storkDiscovery,
        authentication);

    assertEquals(originalUrl + "/tei-xml/validate", controller.getValidationURL());
  }

  // ==========================================================================
  // releaseEditorSperreOnDestroy()
  // ==========================================================================

  @Test
  void releaseEditorSperreOnDestroy_doesNothing_whenSperreNotAcquired() throws Exception {
    Bearbeiter foreign = Bearbeiter.newBuilder().withId("U2").withBearbeitername("foreign").build();
    Sperre foreignSperre = Sperre.newBuilder().withId("S-foreign")
        .withBearbeiter(foreign).withSperreTyp(SperreTyp.MANUAL).build();
    when(beschreibungsBoundary.findSperreForBeschreibung("B-1"))
        .thenReturn(Optional.of(foreignSperre));

    BeschreibungsViewController controller = newController();
    setupReadOnly(controller, "B-1");

    controller.releaseEditorSperreOnDestroy();

    verify(beschreibungsBoundary, never()).beschreibungEntsperren(any());
  }

  @Test
  void releaseEditorSperreOnDestroy_releasesSperre_whenAcquiredByThisView() throws Exception {
    Bearbeiter logged = Bearbeiter.newBuilder().withId("U1").withBearbeitername("me").build();
    Sperre acquired = Sperre.newBuilder().withId("S-1")
        .withBearbeiter(logged).withSperreTyp(SperreTyp.MANUAL).build();
    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(logged);
    when(beschreibungsBoundary.beschreibungSperrenForEdit(logged, "B-1")).thenReturn(acquired);

    BeschreibungsViewController controller = newController();
    setupForEdit(controller, "B-1");

    controller.releaseEditorSperreOnDestroy();

    verify(beschreibungsBoundary, times(1)).beschreibungEntsperren("B-1");
  }

  @Test
  void releaseEditorSperreOnDestroy_doesNotCallGetLoggedBearbeiter() throws Exception {
    Bearbeiter logged = Bearbeiter.newBuilder().withId("U1").withBearbeitername("me").build();
    Sperre acquired = Sperre.newBuilder().withId("S-1")
        .withBearbeiter(logged).withSperreTyp(SperreTyp.MANUAL).build();
    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(logged);
    when(beschreibungsBoundary.beschreibungSperrenForEdit(logged, "B-1")).thenReturn(acquired);

    BeschreibungsViewController controller = newController();
    setupForEdit(controller, "B-1");
    clearInvocations(bearbeiterBoundary);

    controller.releaseEditorSperreOnDestroy();

    verify(bearbeiterBoundary, never()).getLoggedBearbeiter();
  }

  @Test
  void releaseEditorSperreOnDestroy_skipsRelease_whenBeschreibungIdIsNull() throws Exception {
    Bearbeiter logged = Bearbeiter.newBuilder().withId("U1").withBearbeitername("me").build();
    Sperre acquired = Sperre.newBuilder().withId("S-1")
        .withBearbeiter(logged).withSperreTyp(SperreTyp.MANUAL).build();
    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(logged);
    when(beschreibungsBoundary.beschreibungSperrenForEdit(logged, "B-1")).thenReturn(acquired);

    BeschreibungsViewController controller = newController();
    setupForEdit(controller, "B-1");
    controller.setBeschreibungsViewModel(null);

    controller.releaseEditorSperreOnDestroy();

    verify(beschreibungsBoundary, never()).beschreibungEntsperren(any());
  }

  @Test
  void releaseEditorSperreOnDestroy_logsErrorButSwallows_whenBoundaryThrows() throws Exception {
    Bearbeiter logged = Bearbeiter.newBuilder().withId("U1").withBearbeitername("me").build();
    Sperre acquired = Sperre.newBuilder().withId("S-1")
        .withBearbeiter(logged).withSperreTyp(SperreTyp.MANUAL).build();
    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(logged);
    when(beschreibungsBoundary.beschreibungSperrenForEdit(logged, "B-1")).thenReturn(acquired);
    doThrow(new DokumentSperreException("boom"))
        .when(beschreibungsBoundary).beschreibungEntsperren("B-1");

    BeschreibungsViewController controller = newController();
    setupForEdit(controller, "B-1");

    assertDoesNotThrow(controller::releaseEditorSperreOnDestroy);
    verify(beschreibungsBoundary, times(1)).beschreibungEntsperren("B-1");
  }

  // ==========================================================================
  // handleBeschreibungLesenEvent() — foreign Sperre must not be released
  // ==========================================================================

  @Test
  void handleBeschreibungLesenEvent_doesNotEntsperren_whenSperreIsForeign() throws Exception {
    Bearbeiter logged = Bearbeiter.newBuilder().withId("U1").withBearbeitername("me").build();
    Bearbeiter foreign = Bearbeiter.newBuilder().withId("U2").withBearbeitername("foreign").build();
    Sperre foreignSperre = Sperre.newBuilder().withId("S-foreign")
        .withBearbeiter(foreign).withSperreTyp(SperreTyp.MANUAL).build();
    when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(logged);
    when(beschreibungsBoundary.findSperreForBeschreibung("B-1"))
        .thenReturn(Optional.of(foreignSperre));

    BeschreibungsViewController controller = newController();
    setupReadOnly(controller, "B-1");

    controller.handleBeschreibungLesenEvent();

    verify(beschreibungsBoundary, never()).beschreibungEntsperren(any());
  }

  // -- helpers --

  private BeschreibungsViewController newController() {
    return new BeschreibungsViewController("", "",
        beschreibungsBoundary, bearbeiterBoundary, publikationsService, null, authentication);
  }

  private void setupReadOnly(BeschreibungsViewController controller, String id) {
    runSetup(controller, id, false);
  }

  private void setupForEdit(BeschreibungsViewController controller, String id) {
    runSetup(controller, id, true);
  }

  private void runSetup(BeschreibungsViewController controller, String id, boolean edit) {
    when(beschreibungsBoundary.buildBeschreibungsViewModel(id)).thenReturn(
        Optional.of(new BeschreibungsViewModel.BeschreibungsViewBuilder().withID(id).build()));
    ExternalContext ctx = mock(ExternalContext.class);
    when(ctx.getRequestParameterMap())
        .thenReturn(Map.of("id", id, "edit", Boolean.toString(edit)));
    FacesContext facesContext = mock(FacesContext.class);
    when(facesContext.getExternalContext()).thenReturn(ctx);
    try (MockedStatic<FacesContext> facesContextStatic = mockStatic(FacesContext.class)) {
      facesContextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
      controller.setup();
    }
  }
}
