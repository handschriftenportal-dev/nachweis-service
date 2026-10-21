package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.DigitalisatsRechte.PERMISSION_DIGITALISAT_BEARBEITEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.DigitalisatsRechte.PERMISSION_DIGITALISAT_LESEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.DigitalisatsRechte.PERMISSION_DIGITALISAT_LOESCHEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.DigitalisatsRechte.PERMISSION_DIGITALISAT_MANUELL_ANLEGEN;
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
public class DigitalisatsRechteTest extends InitPermissionsTestAuth<DigitalisatsRechte> {

  static Set<String> roles = new HashSet<>(
      List.of(PERMISSION_DIGITALISAT_LESEN, PERMISSION_DIGITALISAT_LOESCHEN,
          PERMISSION_DIGITALISAT_BEARBEITEN,
          PERMISSION_DIGITALISAT_MANUELL_ANLEGEN));

  public DigitalisatsRechteTest() {
    super(roles);
  }

  @Test
  void testKannManuellAnlegen() {
    assertTrue(supervisorRechte.kannManuellAnlegen());
    assertFalse(noPermissionsRechte.kannManuellAnlegen());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP, PERMISSION_DIGITALISAT_MANUELL_ANLEGEN);
  }

  @Test
  void testKannBearbeiten() {
    assertTrue(supervisorRechte.kannBearbeiten());
    assertFalse(noPermissionsRechte.kannBearbeiten());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP, PERMISSION_DIGITALISAT_BEARBEITEN);
  }

  @Test
  void testKannLesen() {
    assertTrue(supervisorRechte.kannLesen());
    assertFalse(noPermissionsRechte.kannLesen());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP, PERMISSION_DIGITALISAT_LESEN);
  }

  @Test
  void testKannLoeschen() {
    assertTrue(supervisorRechte.kannLoeschen());
    assertFalse(noPermissionsRechte.kannLoeschen());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP, PERMISSION_DIGITALISAT_LOESCHEN);
  }

  @Override
  DigitalisatsRechte createTarget(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    return new DigitalisatsRechte(authenticationRepository, oauth2Config);
  }
}
