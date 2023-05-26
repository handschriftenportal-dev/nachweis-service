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

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ERSCHLIESSER_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_LOKALE_REDAKTEUR_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_NORMDATEN_REDAKTEUR_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_REDAKTEUR_APP;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.LoggerFactory;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 15.06.21
 */
@SessionScoped
@Named("importRechte")
public class ImportRechte extends RechteCommon {

  public static final String[] PERMISSION_IMPORTE_DATEI_UPLOAD = {
      B_HSP_ERSCHLIESSER_APP, B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
  };

  public static final String[] PERMISSION_DIGITALISAT_MANUELL_IMPORTIEREN = {
      B_HSP_ERSCHLIESSER_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
  };

  public static final String[] PERMISSION_ORT_MANUELL_IMPORTIEREN = {
      B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
  };

  public static final String[] PERMISSION_KOERPERSCHAFT_MANUELL_IMPORTIEREN = {
      B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
  };

  public static final String[] PERMISSION_PERSON_MANUELL_IMPORTIEREN = {
      B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
  };

  public static final String[] PERMISSION_BEZIEHUNG_MANUELL_IMPORTIEREN = {
      B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
  };

  public static final String[] PERMISSION_SPRACHE_MANUELL_IMPORTIEREN = {
      B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
  };

  public static final String[] PERMISSION_BESCHREIBUNG_MANUELL_IMPORTIEREN = {
      B_HSP_ERSCHLIESSER_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
  };

  public static final String[] PERMISSION_BESCHREIBUNG_AUTOMATISCH_IMPORTIEREN = {
      B_HSP_REDAKTEUR_APP
  };

  public static final String[] PERMISSION_KATALOG_MANUELL_IMPORTIEREN = {
      B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
  };

  public static final String[] PERMISSION_BESCHREIBUNG_IN_NACHWEIS_UEBERNEHMEN = {
      B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
  };

  public static final String[] PERMISSION_ALLE_IMPORTVORGAENGE_ANSCHAUEN = {
      B_HSP_ERSCHLIESSER_APP, B_HSP_NORMDATEN_REDAKTEUR_APP, B_HSP_LOKALE_REDAKTEUR_APP, B_HSP_REDAKTEUR_APP
  };

  private static final long serialVersionUID = 4686422891282686729L;

  ImportRechte() {
  }

  @Inject
  public ImportRechte(AuthenticationRepository authenticationRepository, OAUTH2Config oauth2Config) {
    super(authenticationRepository, oauth2Config, LoggerFactory.getLogger(ImportRechte.class));
  }

  public boolean kannDateiupload() {
    return hasAccessWithPermission(PERMISSION_IMPORTE_DATEI_UPLOAD);
  }

  public boolean kannDigitalisatManuellImportieren() {
    return hasAccessWithPermission(PERMISSION_DIGITALISAT_MANUELL_IMPORTIEREN);
  }

  public boolean kannOrtManuellImportieren() {
    return hasAccessWithPermission(PERMISSION_ORT_MANUELL_IMPORTIEREN);
  }

  public boolean kannKoerperschaftManuellImportieren() {
    return hasAccessWithPermission(PERMISSION_KOERPERSCHAFT_MANUELL_IMPORTIEREN);
  }

  public boolean kannPersonManuellImportieren() {
    return hasAccessWithPermission(PERMISSION_PERSON_MANUELL_IMPORTIEREN);
  }

  public boolean kannBeziehungManuellImportieren() {
    return hasAccessWithPermission(PERMISSION_BEZIEHUNG_MANUELL_IMPORTIEREN);
  }

  public boolean kannBeschreibungManuellImportieren() {
    return hasAccessWithPermission(PERMISSION_BESCHREIBUNG_MANUELL_IMPORTIEREN);
  }

  public boolean kannBeschreibungAutomatischImportieren() {
    return hasAccessWithPermission(PERMISSION_BESCHREIBUNG_AUTOMATISCH_IMPORTIEREN);
  }

  public boolean kannBeschreibungInNachweisUebernehmen() {
    return hasAccessWithPermission(PERMISSION_BESCHREIBUNG_IN_NACHWEIS_UEBERNEHMEN);
  }

  public boolean kannAlleImportvorgaengeAnschauen() {
    return hasAccessWithPermission(PERMISSION_ALLE_IMPORTVORGAENGE_ANSCHAUEN);
  }

  public boolean kannSpracheManuellImportieren() {
    return hasAccessWithPermission(PERMISSION_SPRACHE_MANUELL_IMPORTIEREN);
  }

  public boolean kannKatalogManuellImportieren() {
    return hasAccessWithPermission(PERMISSION_KATALOG_MANUELL_IMPORTIEREN);
  }
}
