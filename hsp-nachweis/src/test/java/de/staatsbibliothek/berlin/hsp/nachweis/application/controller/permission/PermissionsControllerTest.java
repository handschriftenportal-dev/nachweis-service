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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.RechteCommon.DEFAULT_ACTION;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.RechteCommon.STYLE_ALLOWED;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.RechteCommon.STYLE_DISALLOWED;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.SucheRechte.PERMISSION_AUFRUFEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 18.06.21
 */
public class PermissionsControllerTest extends InitPermissionsTestAuth<RechteCommon> {

  public static final String ACTION = "ACTION";
  static Set<String> roles = new HashSet<>(List.of(PERMISSION_AUFRUFEN));

  public PermissionsControllerTest() {
    super(roles);
  }

  @Test
  void testHasAccessWithRole() {
    assertTrue(supervisorRechte.hasAccessWithPermission(PERMISSION_AUFRUFEN));
    assertFalse(noPermissionsRechte.hasAccessWithPermission(PERMISSION_AUFRUFEN));
  }

  @Test
  void testGetAllowedAction() {
    assertEquals(ACTION, supervisorRechte.getAllowedAction(ACTION, PERMISSION_AUFRUFEN));
    assertEquals(DEFAULT_ACTION, noPermissionsRechte.getAllowedAction(ACTION, PERMISSION_AUFRUFEN));
  }

  @Test
  void testGetAllowedStyle() {
    assertEquals(STYLE_ALLOWED, supervisorRechte.getAllowedStyle(PERMISSION_AUFRUFEN));
    assertEquals(STYLE_DISALLOWED, noPermissionsRechte.getAllowedStyle(PERMISSION_AUFRUFEN));
  }

  @Override
  RechteCommon createTarget(AuthenticationRepository authenticationRepository, OAUTH2Config oauth2Config) {
    return new RechteCommon(authenticationRepository, oauth2Config, LoggerFactory.getLogger(
        RechteCommon.class));
  }
}
