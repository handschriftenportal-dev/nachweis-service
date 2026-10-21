package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte.PERMISSION_BESCHREIBUNG_ALLER_DRITTER_HART_LOESCHEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte.PERMISSION_BESCHREIBUNG_EIGENE_LESEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte.PERMISSION_BESCHREIBUNG_INTERNE_ANLEGEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte.PERMISSION_BESCHREIBUNG_LISTE_ALLER_DRITTER_LESEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte.PERMISSION_BESCHREIBUNG_LOESCHEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte.PERMISSION_BESCHREIBUNG_NICHT_GESPERRTE_ALLE_DRITTER_LESEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte.PERMISSION_BESCHREIBUNG_PUBLIZIEREN_SEHEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte.PERMISSION_BESCHREIBUNG_PUBLIZIEREN_VERWALTEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ERSCHLIESSER_COMMON_APP;
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
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 18.06.21
 */
public class BeschreibungsRechteTest extends InitPermissionsTestAuth<BeschreibungsRechte> {

  final static Set<String> roles = new HashSet<>(
      List.of(
          PERMISSION_BESCHREIBUNG_ALLER_DRITTER_HART_LOESCHEN,
          PERMISSION_BESCHREIBUNG_EIGENE_LESEN,
          PERMISSION_BESCHREIBUNG_LISTE_ALLER_DRITTER_LESEN,
          PERMISSION_BESCHREIBUNG_INTERNE_ANLEGEN,
          PERMISSION_BESCHREIBUNG_NICHT_GESPERRTE_ALLE_DRITTER_LESEN,
          PERMISSION_BESCHREIBUNG_LOESCHEN,
          PERMISSION_BESCHREIBUNG_PUBLIZIEREN_SEHEN,
          PERMISSION_BESCHREIBUNG_PUBLIZIEREN_VERWALTEN));

  public BeschreibungsRechteTest() {
    super(roles);
  }

  @Test
  void testKannNichtGesperrteAllerDritterLesen() {
    assertTrue(supervisorRechte.kannNichtGesperrteAllerDritterLesen());
    assertFalse(noPermissionsRechte.kannNichtGesperrteAllerDritterLesen());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP,
        PERMISSION_BESCHREIBUNG_NICHT_GESPERRTE_ALLE_DRITTER_LESEN);
  }

  @Test
  void testKannListeAllerDritterLesen() {
    assertTrue(supervisorRechte.kannListeAllerDritterLesen());
    assertFalse(noPermissionsRechte.kannListeAllerDritterLesen());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP, PERMISSION_BESCHREIBUNG_LISTE_ALLER_DRITTER_LESEN);
  }

  @Test
  void testKannInterneAnlegen() {
    assertTrue(supervisorRechte.kannInterneAnlegen());
    assertFalse(noPermissionsRechte.kannInterneAnlegen());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP, PERMISSION_BESCHREIBUNG_INTERNE_ANLEGEN);
  }

  @Test
  void testKannEigeneLesen() {
    assertTrue(supervisorRechte.kannEigeneLesen());
    assertFalse(noPermissionsRechte.kannEigeneLesen());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP, PERMISSION_BESCHREIBUNG_EIGENE_LESEN);
  }

  @Test
  void testKannAllerDritterhartLoeschen() {
    assertTrue(supervisorRechte.kannAllerDritterhartLoeschen());
    assertFalse(noPermissionsRechte.kannAllerDritterhartLoeschen());
    assertEquals(B_HSP_SUPER_REDAKTEUR_APP,
        PERMISSION_BESCHREIBUNG_ALLER_DRITTER_HART_LOESCHEN);
  }

  @Test
  void testKannLoeschen() {
    assertTrue(supervisorRechte.kannLoeschen());
    assertFalse(noPermissionsRechte.kannLoeschen());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP, PERMISSION_BESCHREIBUNG_LOESCHEN);
  }


  @Test
  void testKannPublikationSehen() {
    assertTrue(supervisorRechte.kannPublikationSehen());
    assertFalse(noPermissionsRechte.kannPublikationSehen());
    assertEquals(B_HSP_REDAKTEUR_APP, PERMISSION_BESCHREIBUNG_PUBLIZIEREN_SEHEN);
  }

  @Test
  void testKannPublikationVerwalten() {
    assertTrue(supervisorRechte.kannPublikationVerwalten());
    assertFalse(noPermissionsRechte.kannPublikationVerwalten());
    assertEquals(B_HSP_SUPER_REDAKTEUR_APP, PERMISSION_BESCHREIBUNG_PUBLIZIEREN_VERWALTEN);
  }

  @Override
  BeschreibungsRechte createTarget(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    return new BeschreibungsRechte(authenticationRepository, oauth2Config);
  }
}
