package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.NavigationRechte.PERMISSION_REDAKTION_ANZEIGEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_LOKALE_REDAKTEUR_COMMON_APP;
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
 * @since 08.06.2022
 */
public class NavigationRechteTest extends InitPermissionsTestAuth<NavigationRechte> {

  static Set<String> roles = new HashSet<>(List.of(PERMISSION_REDAKTION_ANZEIGEN));

  public NavigationRechteTest() {
    super(roles);
  }

  @Override
  NavigationRechte createTarget(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    return new NavigationRechte(authenticationRepository, oauth2Config);
  }

  @Test
  void testKannAlsListeAnzeigen() {
    assertTrue(supervisorRechte.kannRedaktionAnzeigen());
    assertFalse(noPermissionsRechte.kannRedaktionAnzeigen());
    assertEquals(B_HSP_LOKALE_REDAKTEUR_COMMON_APP, PERMISSION_REDAKTION_ANZEIGEN);
  }

}
