package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KatalogRechte.PERMISSION_KATALOGE_ALS_LISTE_ANZEIGEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KatalogRechte.PERMISSION_KATALOG_ANZEIGEN;
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
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 21.10.2022
 */
class KatalogRechteTest extends InitPermissionsTestAuth<KatalogRechte> {

  static Set<String> roles = new HashSet<>(
      List.of(PERMISSION_KATALOG_ANZEIGEN, PERMISSION_KATALOGE_ALS_LISTE_ANZEIGEN));

  public KatalogRechteTest() {
    super(roles);
  }

  @Test
  void testKannAlsListeAnzeigen() {
    assertTrue(supervisorRechte.kannAlsListeAnzeigen());
    assertFalse(noPermissionsRechte.kannAlsListeAnzeigen());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP, PERMISSION_KATALOGE_ALS_LISTE_ANZEIGEN);
  }

  @Test
  void testKannAnzeigen() {
    assertTrue(supervisorRechte.kannAnzeigen());
    assertFalse(noPermissionsRechte.kannAnzeigen());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP, PERMISSION_KATALOG_ANZEIGEN);
  }

  @Override
  KatalogRechte createTarget(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    return new KatalogRechte(authenticationRepository, oauth2Config);
  }
}
