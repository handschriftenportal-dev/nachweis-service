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

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte.PERMISSION_BESCHREIBUNG_ALLER_DRITTER_HART_LOESCHEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte.PERMISSION_BESCHREIBUNG_EIGENE_LESEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte.PERMISSION_BESCHREIBUNG_INTERNE_ANLEGEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte.PERMISSION_BESCHREIBUNG_LISTE_ALLER_DRITTER_LESEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte.PERMISSION_BESCHREIBUNG_NICHT_GESPERRTE_ALLE_DRITTER_LESEN;
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
public class BeschreibungsRechteTest extends InitPermissionsTestAuth<BeschreibungsRechte> {

  final static Set<String> roles = new HashSet<>(
      List.of(
          PERMISSION_BESCHREIBUNG_ALLER_DRITTER_HART_LOESCHEN,
          PERMISSION_BESCHREIBUNG_EIGENE_LESEN,
          PERMISSION_BESCHREIBUNG_LISTE_ALLER_DRITTER_LESEN,
          PERMISSION_BESCHREIBUNG_INTERNE_ANLEGEN,
          PERMISSION_BESCHREIBUNG_NICHT_GESPERRTE_ALLE_DRITTER_LESEN));

  public BeschreibungsRechteTest() {
    super(roles);
  }

  @Test
  void testKannNichtGesperrteAllerDritterLesen() {
    assertTrue(supervisorRechte.kannNichtGesperrteAllerDritterLesen());
    assertFalse(noPermissionsRechte.kannNichtGesperrteAllerDritterLesen());
    assertEquals(B_HSP_ERSCHLIESSER_COMMON_APP, PERMISSION_BESCHREIBUNG_NICHT_GESPERRTE_ALLE_DRITTER_LESEN);
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
    assertEquals(B_HSP_LOKALE_REDAKTEUR_COMMON_APP, PERMISSION_BESCHREIBUNG_ALLER_DRITTER_HART_LOESCHEN);
  }

  @Override
  BeschreibungsRechte createTarget(AuthenticationRepository authenticationRepository, OAUTH2Config oauth2Config) {
    return new BeschreibungsRechte(authenticationRepository, oauth2Config);
  }
}
