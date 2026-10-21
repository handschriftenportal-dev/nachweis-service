package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;


import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.SperrenRechte.PERMISSION_LISTE_LESEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.SperrenRechte.PERMISSION_LOESCHEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ERSCHLIESSER_COMMON_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_REDAKTEUR_APP;
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
 * @since 25.08.21
 */
public class SperrenRechteTest extends InitPermissionsTestAuth<SperrenRechte> {

  static Set<String> roles = new HashSet<>(
      List.of(PERMISSION_LISTE_LESEN, PERMISSION_LOESCHEN));

  public SperrenRechteTest() {
    super(roles);
  }

  @Test
  void testKannAlsListeAnzeigen() {
    assertTrue(supervisorRechte.kannAlsListeAnzeigen());
    assertFalse(noPermissionsRechte.kannAlsListeAnzeigen());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP, PERMISSION_LISTE_LESEN);
  }

  @Test
  void testKannLoeschen() {
    assertTrue(supervisorRechte.kannLoeschen());
    assertFalse(noPermissionsRechte.kannLoeschen());
    assertEquals(B_HSP_REDAKTEUR_APP, PERMISSION_LOESCHEN);
  }

  @Override
  SperrenRechte createTarget(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    return new SperrenRechte(authenticationRepository, oauth2Config);
  }
}
