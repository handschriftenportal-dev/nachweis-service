package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.NormdatenRechte.PERMISSION_NORMDATEN_ALS_EXCEL_RUNTERLADEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.NormdatenRechte.PERMISSION_NORMDATEN_ALS_LISTE_ANZEIGEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.NormdatenRechte.PERMISSION_NORMDATEN_ANZEIGEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.NormdatenRechte.PERMISSION_NORMDATEN_TABELLE_VERWALTEN;
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
public class NormdatenRechteTest extends InitPermissionsTestAuth<NormdatenRechte> {

  static Set<String> roles = new HashSet<>(
      List.of(PERMISSION_NORMDATEN_ALS_LISTE_ANZEIGEN, PERMISSION_NORMDATEN_ANZEIGEN,
          PERMISSION_NORMDATEN_ALS_EXCEL_RUNTERLADEN, PERMISSION_NORMDATEN_TABELLE_VERWALTEN));

  public NormdatenRechteTest() {
    super(roles);
  }

  @Test
  void testKannAlsListeAnzeigen() {
    assertTrue(supervisorRechte.kannAlsListeAnzeigen());
    assertFalse(noPermissionsRechte.kannAlsListeAnzeigen());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP, PERMISSION_NORMDATEN_ALS_LISTE_ANZEIGEN);
  }

  @Test
  void testKannTabelleVerwalten() {
    assertTrue(supervisorRechte.kannTabelleVerwalten());
    assertFalse(noPermissionsRechte.kannTabelleVerwalten());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP, PERMISSION_NORMDATEN_TABELLE_VERWALTEN);
  }

  @Test
  void testKannAlsExcelRunterladen() {
    assertTrue(supervisorRechte.kannAlsExcelRunterladen());
    assertFalse(noPermissionsRechte.kannAlsExcelRunterladen());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP, PERMISSION_NORMDATEN_ALS_EXCEL_RUNTERLADEN);
  }

  @Test
  void testKannAnzeigen() {
    assertTrue(supervisorRechte.kannAnzeigen());
    assertFalse(noPermissionsRechte.kannAnzeigen());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP, PERMISSION_NORMDATEN_ANZEIGEN);
  }

  @Override
  NormdatenRechte createTarget(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    return new NormdatenRechte(authenticationRepository, oauth2Config);
  }
}
