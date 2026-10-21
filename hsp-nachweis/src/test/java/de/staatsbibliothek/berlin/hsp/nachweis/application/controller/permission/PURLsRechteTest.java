package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.PURLsRechte.PERMISSION_AKTUALISIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.PURLsRechte.PERMISSION_GENERIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.PURLsRechte.PERMISSION_LISTE_LESEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.PURLsRechte.PERMISSION_RESOLVER_DATEI_GENERIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_SUPER_REDAKTEUR_APP;
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
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 09.08.22
 */

public class PURLsRechteTest extends InitPermissionsTestAuth<PURLsRechte> {

  static Set<String> roles = new HashSet<>(
      List.of(PERMISSION_LISTE_LESEN, PERMISSION_GENERIEREN, PERMISSION_AKTUALISIEREN,
          PERMISSION_RESOLVER_DATEI_GENERIEREN));

  public PURLsRechteTest() {
    super(roles);
  }

  @Override
  PURLsRechte createTarget(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    return new PURLsRechte(authenticationRepository, oauth2Config);
  }

  @Test
  void testKannAlsListeAnzeigen() {
    assertTrue(supervisorRechte.kannAlsListeAnzeigen());
    assertFalse(noPermissionsRechte.kannAlsListeAnzeigen());
    assertEquals(B_HSP_SUPER_REDAKTEUR_APP, PERMISSION_LISTE_LESEN);
  }

  @Test
  void testKannGenerieren() {
    assertTrue(supervisorRechte.kannGenerieren());
    assertFalse(noPermissionsRechte.kannGenerieren());
    assertEquals(B_HSP_SUPER_REDAKTEUR_APP, PERMISSION_GENERIEREN);
  }

  @Test
  void testKannAktualisieren() {
    assertTrue(supervisorRechte.kannAktualisieren());
    assertFalse(noPermissionsRechte.kannAktualisieren());
    assertEquals(B_HSP_SUPER_REDAKTEUR_APP, PERMISSION_AKTUALISIEREN);
  }

  @Test
  void testKannResolverDateiGenerieren() {
    assertTrue(supervisorRechte.kannResolverDateiGenerieren());
    assertFalse(noPermissionsRechte.kannResolverDateiGenerieren());
    assertEquals(B_HSP_SUPER_REDAKTEUR_APP, PERMISSION_RESOLVER_DATEI_GENERIEREN);
  }
}
