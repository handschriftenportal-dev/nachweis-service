package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BearbeiterRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterService;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions;
import de.staatsbibliothek.berlin.javaee.authentication.services.AuthenticationImpl;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@TestTransaction
class LoginControllerTest {

  @Inject
  OAUTH2Config oauth2Config;

  LoginController loginController;

  BearbeiterService bearbeiterService;
  BearbeiterRechte bearbeiterRechte = Mockito.mock(BearbeiterRechte.class);
  Set<String> permission = new HashSet<>(Arrays.asList(
      Permissions.B_HSP_SUPER_REDAKTEUR_APP,
      Permissions.B_HSP_REDAKTEUR_APP,
      Permissions.B_HSP_ERSCHLIESSER_APP,
      Permissions.B_HSP_ENDNUTZER_APP));

  AuthenticationImpl authentication = new AuthenticationImpl("", true, "Admin", permission,
      permission,
      permission, "King", "Einerlei", "a@b.de",
      LocalDateTime.now());

  @BeforeEach
  public void before() {
    bearbeiterService = Mockito.mock(BearbeiterService.class);
    when(bearbeiterService.getAuthentication()).thenReturn(authentication);
    when(bearbeiterService.getRole()).thenReturn("Admin");
    loginController = new LoginController(bearbeiterService, oauth2Config,
        bearbeiterRechte);
  }

  @Test
  void isUserLoggedIn() {
    assertTrue(loginController.getLoggedIn());
  }

  @Test
  void getUserRole() {
    assertEquals("Admin", loginController.getRole());
    verify(bearbeiterService, times(1)).getRole();
  }
}
