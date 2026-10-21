package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.SucheRechte.PERMISSION_AUFRUFEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.SucheRechte.PERMISSION_ERGEBNISSE_SEHEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.SucheRechte.PERMISSION_FILTERN_UND_FACETTIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ERSCHLIESSER_COMMON_APP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 18.06.21
 */
public class SucheRechteTest extends InitPermissionsTestAuth<SucheRechte> {

  static Set<String> roles = new HashSet<>(
      List.of(PERMISSION_AUFRUFEN, PERMISSION_ERGEBNISSE_SEHEN,
          PERMISSION_FILTERN_UND_FACETTIEREN));

  public SucheRechteTest() {
    super(roles);
  }

  @Test
  void testKannAufrufen() {
    assertTrue(supervisorRechte.kannAufrufen());
    assertFalse(noPermissionsRechte.kannAufrufen());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP, PERMISSION_AUFRUFEN);
  }

  @Test
  void testKannErgebnisseSehen() {
    assertTrue(supervisorRechte.kannErgebnisseSehen());
    assertFalse(noPermissionsRechte.kannErgebnisseSehen());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP, PERMISSION_ERGEBNISSE_SEHEN);
  }

  @Test
  void testKannFilternUndFacettieren() {
    assertTrue(supervisorRechte.kannFilternUndFacettieren());
    assertFalse(noPermissionsRechte.kannFilternUndFacettieren());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP, PERMISSION_FILTERN_UND_FACETTIEREN);
  }

  @Override
  SucheRechte createTarget(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    return new SucheRechte(authenticationRepository, oauth2Config);
  }
}
