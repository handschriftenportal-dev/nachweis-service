package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.SchemaPflegeRechte.PERMISSION_AUTOMATISCHE_UEBERNAHME_AKTIVIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.SchemaPflegeRechte.PERMISSION_SCHEMAPFLEGEDATEN_ALS_LISTE_ANZEIGEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.SchemaPflegeRechte.PERMISSION_SCHEMAPFLEGEDATEN_ERSETZEN;
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
 * @since 29.03.2022
 */
public class SchemaPflegeRechteTest extends InitPermissionsTestAuth<SchemaPflegeRechte> {

  static Set<String> roles = new HashSet<>(
      List.of(PERMISSION_SCHEMAPFLEGEDATEN_ALS_LISTE_ANZEIGEN,
          PERMISSION_SCHEMAPFLEGEDATEN_ERSETZEN));

  public SchemaPflegeRechteTest() {
    super(roles);
  }

  @Override
  SchemaPflegeRechte createTarget(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    return new SchemaPflegeRechte(authenticationRepository, oauth2Config);
  }

  @Test
  void testKannAlsListeAnzeigen() {
    assertTrue(supervisorRechte.kannAlsListeAnzeigen());
    assertFalse(noPermissionsRechte.kannAlsListeAnzeigen());
    assertEquals(B_HSP_SUPER_REDAKTEUR_APP, PERMISSION_SCHEMAPFLEGEDATEN_ALS_LISTE_ANZEIGEN);
  }

  @Test
  void testKannDateienErsetzen() {
    assertTrue(supervisorRechte.kannDateienErsetzen());
    assertFalse(noPermissionsRechte.kannDateienErsetzen());
    assertEquals(B_HSP_SUPER_REDAKTEUR_APP, PERMISSION_SCHEMAPFLEGEDATEN_ERSETZEN);
  }

  @Test
  void testKannAutomatischeUebernahmeAktivieren() {
    assertTrue(supervisorRechte.kannAutomatischeUebernahmeAktivieren());
    assertFalse(noPermissionsRechte.kannAutomatischeUebernahmeAktivieren());
    assertEquals(B_HSP_SUPER_REDAKTEUR_APP, PERMISSION_AUTOMATISCHE_UEBERNAHME_AKTIVIEREN);
  }

}
