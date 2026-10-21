package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_ALLE_IMPORTVORGAENGE_ANSCHAUEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_BESCHREIBUNG_AUTOMATISCH_IMPORTIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_BESCHREIBUNG_IN_NACHWEIS_UEBERNEHMEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_BESCHREIBUNG_MANUELL_IMPORTIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_BEZIEHUNG_MANUELL_IMPORTIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_DIGITALISAT_MANUELL_IMPORTIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_IMPORTE_DATEI_UPLOAD;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_INITIUM_MANUELL_IMPORTIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_KATALOG_MANUELL_IMPORTIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_KOERPERSCHAFT_MANUELL_IMPORTIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_ORT_MANUELL_IMPORTIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_PERSON_MANUELL_IMPORTIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_SPRACHE_MANUELL_IMPORTIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ERSCHLIESSER_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_LOKALE_REDAKTEUR_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_NORMDATEN_REDAKTEUR_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_REDAKTEUR_APP;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 18.06.21
 */
public class ImportRechteTest extends InitPermissionsTestAuth<ImportRechte> {

  static Set<String> roles = new HashSet<>();

  static {
    roles.addAll(Arrays.asList(PERMISSION_DIGITALISAT_MANUELL_IMPORTIEREN));
    roles.addAll(Arrays.asList(PERMISSION_BESCHREIBUNG_MANUELL_IMPORTIEREN));
    roles.addAll(Arrays.asList(PERMISSION_BESCHREIBUNG_IN_NACHWEIS_UEBERNEHMEN));
    roles.addAll(Arrays.asList(PERMISSION_BEZIEHUNG_MANUELL_IMPORTIEREN));
    roles.addAll(Arrays.asList(PERMISSION_ORT_MANUELL_IMPORTIEREN));
    roles.addAll(Arrays.asList(PERMISSION_KOERPERSCHAFT_MANUELL_IMPORTIEREN));
    roles.addAll(Arrays.asList(PERMISSION_PERSON_MANUELL_IMPORTIEREN));
    roles.addAll(Arrays.asList(PERMISSION_SPRACHE_MANUELL_IMPORTIEREN));
    roles.addAll(Arrays.asList(PERMISSION_KATALOG_MANUELL_IMPORTIEREN));
    roles.addAll(Arrays.asList(PERMISSION_INITIUM_MANUELL_IMPORTIEREN));
  }

  public ImportRechteTest() {
    super(roles);
  }

  @Test
  void testKannDateiupload() {
    assertTrue(supervisorRechte.kannDateiupload());
    assertFalse(noPermissionsRechte.kannDateiupload());
    assertArrayEquals(PERMISSION_IMPORTE_DATEI_UPLOAD, new String[]{
        B_HSP_ERSCHLIESSER_APP, B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP,
        B_HSP_REDAKTEUR_APP
    });
  }

  @Override
  ImportRechte createTarget(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    return new ImportRechte(authenticationRepository, oauth2Config);
  }

  @Test
  void testKannDigitalisatManuellImportieren() {
    assertTrue(supervisorRechte.kannDigitalisatManuellImportieren());
    assertFalse(noPermissionsRechte.kannDigitalisatManuellImportieren());
    assertArrayEquals(PERMISSION_DIGITALISAT_MANUELL_IMPORTIEREN, new String[]{
        B_HSP_ERSCHLIESSER_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
    });
  }

  @Test
  void testKannOrtManuellImportieren() {
    assertTrue(supervisorRechte.kannOrtManuellImportieren());
    assertFalse(noPermissionsRechte.kannOrtManuellImportieren());
    assertArrayEquals(PERMISSION_ORT_MANUELL_IMPORTIEREN, new String[]{
        B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
    });
  }

  @Test
  void testKannKoerperschaftManuellImportieren() {
    assertTrue(supervisorRechte.kannKoerperschaftManuellImportieren());
    assertFalse(noPermissionsRechte.kannKoerperschaftManuellImportieren());
    assertArrayEquals(PERMISSION_KOERPERSCHAFT_MANUELL_IMPORTIEREN, new String[]{
        B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
    });
  }

  @Test
  void testKannPersonManuellImportieren() {
    assertTrue(supervisorRechte.kannPersonManuellImportieren());
    assertFalse(noPermissionsRechte.kannPersonManuellImportieren());
    assertArrayEquals(PERMISSION_PERSON_MANUELL_IMPORTIEREN, new String[]{
        B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
    });
  }

  @Test
  void testKannBeziehungManuellImportieren() {
    assertTrue(supervisorRechte.kannBeziehungManuellImportieren());
    assertFalse(noPermissionsRechte.kannBeziehungManuellImportieren());
    assertArrayEquals(PERMISSION_BEZIEHUNG_MANUELL_IMPORTIEREN, new String[]{
        B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
    });
  }

  @Test
  void testKannBeschreibungManuellImportieren() {
    assertTrue(supervisorRechte.kannBeschreibungManuellImportieren());
    assertFalse(noPermissionsRechte.kannBeschreibungManuellImportieren());
    assertArrayEquals(PERMISSION_BESCHREIBUNG_MANUELL_IMPORTIEREN, new String[]{
        B_HSP_ERSCHLIESSER_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
    });
  }

  @Test
  void testKannBeschreibungAutomatischImportieren() {
    assertTrue(supervisorRechte.kannBeschreibungAutomatischImportieren());
    assertFalse(noPermissionsRechte.kannBeschreibungAutomatischImportieren());
    assertArrayEquals(PERMISSION_BESCHREIBUNG_AUTOMATISCH_IMPORTIEREN, new String[]{
        B_HSP_REDAKTEUR_APP
    });
  }

  @Test
  void testKannBeschreibungInNachweisUebernehmen() {
    assertTrue(supervisorRechte.kannBeschreibungInNachweisUebernehmen());
    assertFalse(noPermissionsRechte.kannBeschreibungInNachweisUebernehmen());
    assertArrayEquals(PERMISSION_BESCHREIBUNG_IN_NACHWEIS_UEBERNEHMEN, new String[]{
        B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
    });
  }

  @Test
  void testKannAlleImportvorgaengeAnschauen() {
    assertTrue(supervisorRechte.kannAlleImportvorgaengeAnschauen());
    assertFalse(noPermissionsRechte.kannAlleImportvorgaengeAnschauen());
    assertArrayEquals(PERMISSION_ALLE_IMPORTVORGAENGE_ANSCHAUEN, new String[]{
        B_HSP_ERSCHLIESSER_APP, B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP,
        B_HSP_REDAKTEUR_APP
    });
  }

  @Test
  void testKannSpracheManuellImportieren() {
    assertTrue(supervisorRechte.kannSpracheManuellImportieren());
    assertFalse(noPermissionsRechte.kannSpracheManuellImportieren());
    assertArrayEquals(PERMISSION_SPRACHE_MANUELL_IMPORTIEREN, new String[]{
        B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
    });
  }

  @Test
  void kannKatalogManuellImportieren() {
    assertTrue(supervisorRechte.kannKatalogManuellImportieren());
    assertFalse(noPermissionsRechte.kannKatalogManuellImportieren());
    assertArrayEquals(PERMISSION_KATALOG_MANUELL_IMPORTIEREN, new String[]{
        B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
    });
  }

  @Test
  void testKannInitiumManuellImportieren() {
    assertTrue(supervisorRechte.kannInitiumManuellImportieren());
    assertFalse(noPermissionsRechte.kannInitiumManuellImportieren());
    assertArrayEquals(PERMISSION_INITIUM_MANUELL_IMPORTIEREN, new String[]{
        B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
    });
  }
}
