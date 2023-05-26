/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_ALLE_IMPORTVORGAENGE_ANSCHAUEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_BESCHREIBUNG_AUTOMATISCH_IMPORTIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_BESCHREIBUNG_IN_NACHWEIS_UEBERNEHMEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_BESCHREIBUNG_MANUELL_IMPORTIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_BEZIEHUNG_MANUELL_IMPORTIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_DIGITALISAT_MANUELL_IMPORTIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_IMPORTE_DATEI_UPLOAD;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_KATALOG_MANUELL_IMPORTIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_KOERPERSCHAFT_MANUELL_IMPORTIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_ORT_MANUELL_IMPORTIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_PERSON_MANUELL_IMPORTIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.ImportRechte.PERMISSION_SPRACHE_MANUELL_IMPORTIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ERSCHLIESSER_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_LOKALE_REDAKTEUR_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_NORMDATEN_REDAKTEUR_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_REDAKTEUR_APP;
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
  }

  public ImportRechteTest() {
    super(roles);
  }

  @Test
  void testKannDateiupload() {
    assertTrue(supervisorRechte.kannDateiupload());
    assertFalse(noPermissionsRechte.kannDateiupload());
    assertTrue(Arrays.equals(PERMISSION_IMPORTE_DATEI_UPLOAD, new String[]{
        B_HSP_ERSCHLIESSER_APP, B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
    }));
  }

  @Override
  ImportRechte createTarget(AuthenticationRepository authenticationRepository, OAUTH2Config oauth2Config) {
    return new ImportRechte(authenticationRepository, oauth2Config);
  }

  @Test
  void testKannDigitalisatManuellImportieren() {
    assertTrue(supervisorRechte.kannDigitalisatManuellImportieren());
    assertFalse(noPermissionsRechte.kannDigitalisatManuellImportieren());
    assertTrue(Arrays.equals(PERMISSION_DIGITALISAT_MANUELL_IMPORTIEREN,
        new String[]{
            B_HSP_ERSCHLIESSER_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
        }));
  }

  @Test
  void testKannOrtManuellImportieren() {
    assertTrue(supervisorRechte.kannOrtManuellImportieren());
    assertFalse(noPermissionsRechte.kannOrtManuellImportieren());
    assertTrue(Arrays.equals(PERMISSION_ORT_MANUELL_IMPORTIEREN, new String[]{
        B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
    }));
  }

  @Test
  void testKannKoerperschaftManuellImportieren() {
    assertTrue(supervisorRechte.kannKoerperschaftManuellImportieren());
    assertFalse(noPermissionsRechte.kannKoerperschaftManuellImportieren());
    assertTrue(Arrays.equals(PERMISSION_KOERPERSCHAFT_MANUELL_IMPORTIEREN, new String[]{
        B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
    }));
  }

  @Test
  void testKannPersonManuellImportieren() {
    assertTrue(supervisorRechte.kannPersonManuellImportieren());
    assertFalse(noPermissionsRechte.kannPersonManuellImportieren());
    assertTrue(Arrays.equals(PERMISSION_PERSON_MANUELL_IMPORTIEREN, new String[]{
        B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
    }));
  }

  @Test
  void testKannBeziehungManuellImportieren() {
    assertTrue(supervisorRechte.kannBeziehungManuellImportieren());
    assertFalse(noPermissionsRechte.kannBeziehungManuellImportieren());
    assertTrue(Arrays.equals(PERMISSION_BEZIEHUNG_MANUELL_IMPORTIEREN, new String[]{
        B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
    }));
  }

  @Test
  void testKannBeschreibungManuellImportieren() {
    assertTrue(supervisorRechte.kannBeschreibungManuellImportieren());
    assertFalse(noPermissionsRechte.kannBeschreibungManuellImportieren());
    assertTrue(Arrays.equals(PERMISSION_BESCHREIBUNG_MANUELL_IMPORTIEREN, new String[]{
        B_HSP_ERSCHLIESSER_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
    }));
  }

  @Test
  void testKannBeschreibungAutomatischImportieren() {
    assertTrue(supervisorRechte.kannBeschreibungAutomatischImportieren());
    assertFalse(noPermissionsRechte.kannBeschreibungAutomatischImportieren());
    assertTrue(Arrays.equals(PERMISSION_BESCHREIBUNG_AUTOMATISCH_IMPORTIEREN, new String[]{
        B_HSP_REDAKTEUR_APP
    }));
  }

  @Test
  void testKannBeschreibungInNachweisUebernehmen() {
    assertTrue(supervisorRechte.kannBeschreibungInNachweisUebernehmen());
    assertFalse(noPermissionsRechte.kannBeschreibungInNachweisUebernehmen());
    assertTrue(Arrays.equals(PERMISSION_BESCHREIBUNG_IN_NACHWEIS_UEBERNEHMEN, new String[]{
        B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
    }));
  }

  @Test
  void testKannAlleImportvorgaengeAnschauen() {
    assertTrue(supervisorRechte.kannAlleImportvorgaengeAnschauen());
    assertFalse(noPermissionsRechte.kannAlleImportvorgaengeAnschauen());
    assertTrue(Arrays.equals(PERMISSION_ALLE_IMPORTVORGAENGE_ANSCHAUEN, new String[]{
        B_HSP_ERSCHLIESSER_APP, B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
    }));
  }

  @Test
  void testKannSpracheManuellImportieren() {
    assertTrue(supervisorRechte.kannSpracheManuellImportieren());
    assertFalse(noPermissionsRechte.kannSpracheManuellImportieren());
    assertTrue(Arrays.equals(PERMISSION_SPRACHE_MANUELL_IMPORTIEREN, new String[]{
        B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
    }));
  }

  @Test
  void kannKatalogManuellImportieren() {
    assertTrue(supervisorRechte.kannKatalogManuellImportieren());
    assertFalse(noPermissionsRechte.kannKatalogManuellImportieren());
    assertTrue(Arrays.equals(PERMISSION_KATALOG_MANUELL_IMPORTIEREN, new String[]{
        B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
    }));
  }
}
