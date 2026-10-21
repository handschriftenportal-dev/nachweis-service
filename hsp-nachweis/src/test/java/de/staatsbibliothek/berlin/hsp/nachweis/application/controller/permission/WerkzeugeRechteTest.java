package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.WerkzeugeRechte.PERMISSION_KANN_BESCHREIBUNGEN_INDEX_AKTUALISIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.WerkzeugeRechte.PERMISSION_KANN_KODS_INDEX_AKTUALISIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.WerkzeugeRechte.PERMISSION_KANN_PRAESENTATION_INDEX_AKTUALISIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.WerkzeugeRechte.PERMISSION_WERKZEUGE_ANWENDEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_REDAKTEUR_APP;
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
 * @since 06.03.2023
 */
class WerkzeugeRechteTest extends InitPermissionsTestAuth<WerkzeugeRechte> {

  static Set<String> roles = new HashSet<>(
      List.of(PERMISSION_WERKZEUGE_ANWENDEN, PERMISSION_KANN_BESCHREIBUNGEN_INDEX_AKTUALISIEREN));

  public WerkzeugeRechteTest() {
    super(roles);
  }

  @Override
  WerkzeugeRechte createTarget(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    return new WerkzeugeRechte(authenticationRepository, oauth2Config);
  }

  @Test
  void testKannWerkzeugeAnwenden() {
    assertTrue(supervisorRechte.kannWerkzeugeAnwenden());
    assertFalse(noPermissionsRechte.kannWerkzeugeAnwenden());
    assertEquals(B_HSP_REDAKTEUR_APP, PERMISSION_WERKZEUGE_ANWENDEN);
  }

  @Test
  void testKannBeschreibungenIndexAktualisieren() {
    assertTrue(supervisorRechte.kannBeschreibungenIndexAktualisieren());
    assertFalse(noPermissionsRechte.kannBeschreibungenIndexAktualisieren());
    assertEquals(B_HSP_SUPER_REDAKTEUR_APP, PERMISSION_KANN_BESCHREIBUNGEN_INDEX_AKTUALISIEREN);
  }

  @Test
  void testKannKodsIndexAktualisieren() {
    assertTrue(supervisorRechte.kannKodsIndexAktualisieren());
    assertFalse(noPermissionsRechte.kannKodsIndexAktualisieren());
    assertEquals(B_HSP_SUPER_REDAKTEUR_APP, PERMISSION_KANN_KODS_INDEX_AKTUALISIEREN);
  }

  @Test
  void testKannPraesentationIndexAktualisieren() {
    assertTrue(supervisorRechte.kannPraesentationIndexAktualisieren());
    assertFalse(noPermissionsRechte.kannPraesentationIndexAktualisieren());
    assertEquals(B_HSP_SUPER_REDAKTEUR_APP, PERMISSION_KANN_PRAESENTATION_INDEX_AKTUALISIEREN);
  }
}
