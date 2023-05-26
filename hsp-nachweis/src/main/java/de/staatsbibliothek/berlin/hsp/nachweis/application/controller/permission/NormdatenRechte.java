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

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ERSCHLIESSER_COMMON_APP;

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
@Named("normdatenRechte")
public class NormdatenRechte extends RechteCommon {

  public static final String PERMISSION_NORMDATEN_ALS_LISTE_ANZEIGEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_NORMDATEN_TABELLE_VERWALTEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_NORMDATEN_ALS_EXCEL_RUNTERLADEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_NORMDATEN_ANZEIGEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  private static final long serialVersionUID = -164319952016248074L;

  @Inject
  public NormdatenRechte(AuthenticationRepository authenticationRepository, OAUTH2Config oauth2Config) {
    super(authenticationRepository, oauth2Config, LoggerFactory.getLogger(NormdatenRechte.class));
  }

  public boolean kannAlsListeAnzeigen() {
    return hasAccessWithPermission(PERMISSION_NORMDATEN_ALS_LISTE_ANZEIGEN);
  }

  public boolean kannTabelleVerwalten() {
    return hasAccessWithPermission(PERMISSION_NORMDATEN_TABELLE_VERWALTEN);
  }

  public boolean kannAlsExcelRunterladen() {
    return hasAccessWithPermission(PERMISSION_NORMDATEN_ALS_EXCEL_RUNTERLADEN);
  }

  public boolean kannAnzeigen() {
    return hasAccessWithPermission(PERMISSION_NORMDATEN_ANZEIGEN);
  }
}