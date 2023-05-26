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

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.SchemaPflegeRechte.PERMISSION_AUTOMATISCHE_UEBERNAHME_AKTIVIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.SchemaPflegeRechte.PERMISSION_SCHEMAPFLEGEDATEN_ALS_LISTE_ANZEIGEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.SchemaPflegeRechte.PERMISSION_SCHEMAPFLEGEDATEN_ERSETZEN;
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
 * @since 29.03.2022
 */
public class SchemaPflegeRechteTest extends InitPermissionsTestAuth<SchemaPflegeRechte> {

  static Set<String> roles = new HashSet<>(
      List.of(PERMISSION_SCHEMAPFLEGEDATEN_ALS_LISTE_ANZEIGEN, PERMISSION_SCHEMAPFLEGEDATEN_ERSETZEN));

  public SchemaPflegeRechteTest() {
    super(roles);
  }

  @Override
  SchemaPflegeRechte createTarget(AuthenticationRepository authenticationRepository, OAUTH2Config oauth2Config) {
    return new SchemaPflegeRechte(authenticationRepository, oauth2Config);
  }

  @Test
  void testKannAlsListeAnzeigen() {
    assertTrue(supervisorRechte.kannAlsListeAnzeigen());
    assertFalse(noPermissionsRechte.kannAlsListeAnzeigen());
    assertEquals(B_HSP_REDAKTEUR_APP, PERMISSION_SCHEMAPFLEGEDATEN_ALS_LISTE_ANZEIGEN);
  }

  @Test
  void testKannDateienErsetzen() {
    assertTrue(supervisorRechte.kannDateienErsetzen());
    assertFalse(noPermissionsRechte.kannDateienErsetzen());
    assertEquals(B_HSP_REDAKTEUR_APP, PERMISSION_SCHEMAPFLEGEDATEN_ERSETZEN);
  }

  @Test
  void testKannAutomatischeUebernahmeAktivieren() {
    assertTrue(supervisorRechte.kannAutomatischeUebernahmeAktivieren());
    assertFalse(noPermissionsRechte.kannAutomatischeUebernahmeAktivieren());
    assertEquals(B_HSP_REDAKTEUR_APP, PERMISSION_AUTOMATISCHE_UEBERNAHME_AKTIVIEREN);
  }

}
