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
import javax.inject.Named;
import org.slf4j.LoggerFactory;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 17.06.21
 */
@SessionScoped
@Named("sucheRechte")
public class SucheRechte extends RechteCommon {

  private static final long serialVersionUID = 9078120026054104836L;

  public static final String PERMISSION_AUFRUFEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_ERGEBNISSE_SEHEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_FILTERN_UND_FACETTIEREN = B_HSP_ERSCHLIESSER_COMMON_APP;

  public SucheRechte(AuthenticationRepository authenticationRepository, OAUTH2Config oauth2Config) {
    super(authenticationRepository, oauth2Config, LoggerFactory.getLogger(SucheRechte.class));
  }

  public boolean kannAufrufen() {
    return hasAccessWithPermission(PERMISSION_AUFRUFEN);
  }

  public boolean kannErgebnisseSehen() {
    return hasAccessWithPermission(PERMISSION_ERGEBNISSE_SEHEN);
  }

  public boolean kannFilternUndFacettieren() {
    return hasAccessWithPermission(PERMISSION_FILTERN_UND_FACETTIEREN);
  }

}
