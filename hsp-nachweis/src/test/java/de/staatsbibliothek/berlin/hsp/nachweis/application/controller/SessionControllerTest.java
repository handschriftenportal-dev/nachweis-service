package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterService;
import de.staatsbibliothek.berlin.javaee.authentication.services.AuthenticationImpl;
import io.quarkus.test.InjectMock;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 06.09.24.
 * @version 1.0
 */

@QuarkusTest
@TestTransaction
class SessionControllerTest {

  FacesContext facesContext;
  @InjectMock
  ExternalContext externalContext;
  @InjectMock
  HttpSession httpSession;
  @InjectMock
  BearbeiterService bearbeiterService;
  private SessionController controller;

  @BeforeEach
  void before() {
    facesContext = ContextMocker.mockFacesContext();
    when(facesContext.getExternalContext()).thenReturn(externalContext);
    when(externalContext.getSession(false)).thenReturn(httpSession);
    when(httpSession.getLastAccessedTime()).thenReturn(System.currentTimeMillis() - 10000);
    when(httpSession.getMaxInactiveInterval()).thenReturn(120);

    AuthenticationImpl authentication = new AuthenticationImpl(
        "",
        true,
        "",
        new HashSet<>(),
        new HashSet<>(),
        new HashSet<>(),
        "",
        "",
        "",
        LocalDateTime.now());

    when(bearbeiterService.getAuthentication()).thenReturn(authentication);
    controller = new SessionController(bearbeiterService);
  }

  @Test
  void testConstructionOf() {
    assertNotNull(controller);
  }

  @Test
  void testRenewSession() {
    assertDoesNotThrow(() -> controller.renewSession());
  }

  @Test
  void invalidateSession() {
    assertDoesNotThrow(() -> controller.invalidateSession());
    verify(bearbeiterService, times(1)).logout();
  }

  @Test
  void testIsExpired() {
    assertTrue(controller.isExpired());
  }
}
