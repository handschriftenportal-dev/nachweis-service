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
  RechteCommon createTarget(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    return new RechteCommon(authenticationRepository, oauth2Config, LoggerFactory.getLogger(
        RechteCommon.class));
  }
}
