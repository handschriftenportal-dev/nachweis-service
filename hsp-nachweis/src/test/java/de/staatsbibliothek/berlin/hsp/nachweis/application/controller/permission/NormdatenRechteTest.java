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
  NormdatenRechte createTarget(AuthenticationRepository authenticationRepository, OAUTH2Config oauth2Config) {
    return new NormdatenRechte(authenticationRepository, oauth2Config);
  }
}
