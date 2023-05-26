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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BearbeiterRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterService;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions;
import de.staatsbibliothek.berlin.javaee.authentication.service.AuthenticationImpl;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@TestTransaction
class LoginControllerTest {

  @Inject
  OAUTH2Config oauth2Config;

  BearbeiterService bearbeiterService;
  BearbeiterRechte bearbeiterRechte = Mockito.mock(BearbeiterRechte.class);
  Set<String> permission = new HashSet<>(Arrays.asList(
      Permissions.B_HSP_ADMIN_APP,
      Permissions.B_HSP_REDAKTEUR_APP,
      Permissions.B_HSP_ERSCHLIESSER_APP,
      Permissions.B_HSP_ENDNUTZER_APP));

  AuthenticationImpl authentication = new AuthenticationImpl("", true, "Admin", permission, permission,
      permission, "King", "Einerlei", "a@b.de",
      LocalDateTime.now());

  @BeforeEach
  public void before() {
    bearbeiterService = Mockito.mock(BearbeiterService.class);
    when(bearbeiterService.getAuthentication()).thenReturn(authentication);
    when(bearbeiterService.getRole()).thenReturn("Admin");
  }

  @Test
  void isUserLoggedIn() {
    LoginController loginController = new LoginController(bearbeiterService, oauth2Config, bearbeiterRechte);
    assertTrue(loginController.getLoggedIn());
  }

  @Test
  void getUserRole() {
    LoginController loginController = new LoginController(bearbeiterService, oauth2Config, bearbeiterRechte);
    assertEquals("Admin", loginController.getRole());
    verify(bearbeiterService, times(1)).getRole();
  }
}
