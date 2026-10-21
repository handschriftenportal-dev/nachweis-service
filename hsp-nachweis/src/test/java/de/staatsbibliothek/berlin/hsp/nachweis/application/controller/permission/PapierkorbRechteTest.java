package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.PapierkorbRechte.PERMISSION_PAPIERKORB_ALS_LISTE_ANZEIGEN;
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
 * @since 30.11.22
 */
class PapierkorbRechteTest extends InitPermissionsTestAuth<PapierkorbRechte> {

  static Set<String> roles = new HashSet<>(
      List.of(PERMISSION_PAPIERKORB_ALS_LISTE_ANZEIGEN));

  PapierkorbRechteTest() {
    super(roles);
  }

  @Override
  PapierkorbRechte createTarget(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    return new PapierkorbRechte(authenticationRepository, oauth2Config);
  }

  @Test
  void testKannAlsListeAnzeigen() {
    assertTrue(supervisorRechte.kannAlsListeAnzeigen());
    assertFalse(noPermissionsRechte.kannAlsListeAnzeigen());
    assertEquals(B_HSP_REDAKTEUR_APP, PERMISSION_PAPIERKORB_ALS_LISTE_ANZEIGEN);
  }

}
