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

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.entity.Authentication;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationSessionRepository;
import de.staatsbibliothek.berlin.javaee.authentication.service.AuthenticationImpl;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 18.06.21
 */
public abstract class InitPermissionsTestAuth<T extends RechteCommon> {

  final T supervisorRechte;
  final T noPermissionsRechte;

  public InitPermissionsTestAuth(Set<String> roles) {
    LocalDateTime validTo = LocalDateTime.now().plusDays(1L);
    Authentication noPermissionsUserAuth = new AuthenticationImpl("accessToken", true, "b-pc101",
        Collections.emptySet(),
        Collections.emptySet(), Set.of("HSP"), "Piotr", "Czarnecki", "piotr.czarnecki@sbb.spk-berlin.de", validTo);

    Authentication supervisorAuth = new AuthenticationImpl("accessToken", true, "b-pc101", roles,
        roles, Set.of("HSP"), "Piotr", "Czarnecki", "piotr.czarnecki@sbb.spk-berlin.de", validTo);

    OAUTH2Config oauth2Config = new OAUTH2Config(null, null, true, true, true, "http://localhost", "clientId",
        "clientSecret", "/dashboard.xhtml", "/dashboard.xhtml", "/error_authentication.xhtml");

    AuthenticationRepository noPermissionAuthRepository = new AuthenticationSessionRepository();
    noPermissionAuthRepository.save(noPermissionsUserAuth);

    AuthenticationRepository supervisorAuthRepository = new AuthenticationSessionRepository();
    supervisorAuthRepository.save(supervisorAuth);

    supervisorRechte = createTarget(supervisorAuthRepository, oauth2Config);
    noPermissionsRechte = createTarget(noPermissionAuthRepository, oauth2Config);
  }

  abstract T createTarget(AuthenticationRepository authenticationRepository, OAUTH2Config oauth2Config);

}
