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

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KODRechte.PERMISSION_KOD_BEARBEITEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KODRechte.PERMISSION_KOD_HART_LOESCHEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KODRechte.PERMISSION_KOD_LESEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KODRechte.PERMISSION_KOD_LISTE_LESEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KODRechte.PERMISSION_KOD_REGISTRIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ERSCHLIESSER_COMMON_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_LOKALE_REDAKTEUR_COMMON_APP;
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
public class KODRechteTest extends InitPermissionsTestAuth<KODRechte> {

  static Set<String> roles = new HashSet<>(
      List.of(PERMISSION_KOD_HART_LOESCHEN, PERMISSION_KOD_LESEN, PERMISSION_KOD_LISTE_LESEN,
          PERMISSION_KOD_REGISTRIEREN));

  public KODRechteTest() {
    super(roles);
  }

  @Test
  void testKannRegistrieren() {
    assertTrue(supervisorRechte.kannRegistrieren());
    assertFalse(noPermissionsRechte.kannRegistrieren());
    assertEquals(B_HSP_LOKALE_REDAKTEUR_COMMON_APP, PERMISSION_KOD_REGISTRIEREN);
  }

  @Test
  void testKannLesen() {
    assertTrue(supervisorRechte.kannLesen());
    assertFalse(noPermissionsRechte.kannLesen());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP, PERMISSION_KOD_LESEN);
  }

  @Test
  void testKannListeLesen() {
    assertTrue(supervisorRechte.kannListeLesen());
    assertFalse(noPermissionsRechte.kannListeLesen());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP, PERMISSION_KOD_LISTE_LESEN);
  }

  @Test
  void testKannHartLoeschen() {
    assertTrue(supervisorRechte.kannHartLoeschen());
    assertFalse(noPermissionsRechte.kannHartLoeschen());
    assertEquals(B_HSP_LOKALE_REDAKTEUR_COMMON_APP, PERMISSION_KOD_HART_LOESCHEN);
  }

  @Test
  void testKannBearbeiten() {
    assertTrue(supervisorRechte.kannBearbeiten());
    assertFalse(noPermissionsRechte.kannBearbeiten());
    assertEquals(B_HSP_LOKALE_REDAKTEUR_COMMON_APP, PERMISSION_KOD_BEARBEITEN);
  }

  @Override
  KODRechte createTarget(AuthenticationRepository authenticationRepository, OAUTH2Config oauth2Config) {
    return new KODRechte(authenticationRepository, oauth2Config);
  }
}
