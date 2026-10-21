package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.normdatenerkennung;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung.NormdatenErkennungAuftragService;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.LobidOrgPort;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link NormdatenErkennungAuftragController#translateErklaerungsTyp(String)}.
 *
 * <p>Test strategy:
 * <ul>
 *   <li>The method depends on {@link FacesContext#getCurrentInstance()} to obtain the current
 *       view root locale, then loads the application resource bundle. We mock {@code FacesContext}
 *       statically (inline, with try-with-resources) following the pattern in
 *       {@code BeschreibungCardControllerTest} so each test scopes its own JSF context.</li>
 *   <li>The actual {@code messages_de.properties} / {@code messages_en.properties} files are on
 *       the test classpath, so the real {@link java.util.ResourceBundle} resolution is used —
 *       we do not mock the bundle. This guarantees we test the wiring against the production
 *       translation files.</li>
 *   <li>Coverage spans: null guard, happy-path lookup in two locales, and the
 *       {@link java.util.MissingResourceException} fallback that returns the raw key.</li>
 *   <li>The four collaborators in the constructor are pure mocks; the method under test does
 *       not interact with any of them, so no stubbing is required.</li>
 * </ul>
 */
class NormdatenErkennungAuftragControllerTest {

  private NormdatenErkennungAuftragController controller;

  @BeforeEach
  void setUp() {
    NormdatenErkennungAuftragService auftragService = mock(
        NormdatenErkennungAuftragService.class);
    BearbeiterBoundary bearbeiterService = mock(BearbeiterBoundary.class);
    BeschreibungsRepository beschreibungsRepository = mock(BeschreibungsRepository.class);
    LobidOrgPort lobidPort = mock(LobidOrgPort.class);

    controller = new NormdatenErkennungAuftragController(
        auftragService, bearbeiterService, beschreibungsRepository, lobidPort);
  }

  @Test
  @DisplayName("returns null when typ is null (no FacesContext interaction)")
  void translateErklaerungsTyp_returnsNull_whenTypIsNull() {
    var result = controller.translateErklaerungsTyp(null);
    assertNull(result);
  }

  @Nested
  @DisplayName("with mocked FacesContext")
  class WithFacesContext {

    @Test
    @DisplayName("returns German translation for known type 'NAME_EXAKT'")
    void translateErklaerungsTyp_returnsTranslation_forKnownType() {
      try (var facesContextMock = mockStatic(FacesContext.class)) {
        var ctx = mock(FacesContext.class);
        var viewRoot = mock(UIViewRoot.class);
        facesContextMock.when(FacesContext::getCurrentInstance).thenReturn(ctx);
        when(ctx.getViewRoot()).thenReturn(viewRoot);
        when(viewRoot.getLocale()).thenReturn(Locale.GERMAN);

        var result = controller.translateErklaerungsTyp("NAME_EXAKT");
        assertEquals("Name (exakt)", result);
      }
    }

    @Test
    @DisplayName("returns English translation for known type 'NAME_EXAKT'")
    void translateErklaerungsTyp_returnsTranslation_forKnownType_englishLocale() {
      try (var facesContextMock = mockStatic(FacesContext.class)) {
        var ctx = mock(FacesContext.class);
        var viewRoot = mock(UIViewRoot.class);
        facesContextMock.when(FacesContext::getCurrentInstance).thenReturn(ctx);
        when(ctx.getViewRoot()).thenReturn(viewRoot);
        when(viewRoot.getLocale()).thenReturn(Locale.ENGLISH);

        var result = controller.translateErklaerungsTyp("NAME_EXAKT");
        assertEquals("Name (exact)", result);
      }
    }

    @Test
    @DisplayName("returns the raw type string when the bundle key is missing")
    void translateErklaerungsTyp_returnsRawString_forUnknownType() {
      try (var facesContextMock = mockStatic(FacesContext.class)) {
        var ctx = mock(FacesContext.class);
        var viewRoot = mock(UIViewRoot.class);
        facesContextMock.when(FacesContext::getCurrentInstance).thenReturn(ctx);
        when(ctx.getViewRoot()).thenReturn(viewRoot);
        when(viewRoot.getLocale()).thenReturn(Locale.GERMAN);

        var result = controller.translateErklaerungsTyp("FUTURE_UNKNOWN_TYPE");
        assertEquals("FUTURE_UNKNOWN_TYPE", result);
      }
    }
  }
}
