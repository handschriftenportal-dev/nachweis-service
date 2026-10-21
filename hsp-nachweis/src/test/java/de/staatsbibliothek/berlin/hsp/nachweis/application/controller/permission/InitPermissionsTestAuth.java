package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.entity.Authentication;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import de.staatsbibliothek.berlin.javaee.authentication.repository.AuthenticationSessionRepository;
import de.staatsbibliothek.berlin.javaee.authentication.services.AuthenticationImpl;
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
        Collections.emptySet(), Set.of("HSP"), "Piotr", "Czarnecki",
        "piotr.czarnecki@sbb.spk-berlin.de", validTo);

    Authentication supervisorAuth = new AuthenticationImpl("accessToken", true, "b-pc101", roles,
        roles, Set.of("HSP"), "Piotr", "Czarnecki", "piotr.czarnecki@sbb.spk-berlin.de", validTo);

    OAUTH2Config oauth2Config = new OAUTH2Config(null, null, true, "IDENTITYMANAGEMENT", "",
        "OAUTH", true, true, "http://localhost",
        "clientId",
        "clientSecret", "/dashboard.xhtml", "/dashboard.xhtml", "/error_authentication.xhtml");

    AuthenticationRepository noPermissionAuthRepository = new AuthenticationSessionRepository();
    noPermissionAuthRepository.save(noPermissionsUserAuth);

    AuthenticationRepository supervisorAuthRepository = new AuthenticationSessionRepository();
    supervisorAuthRepository.save(supervisorAuth);

    supervisorRechte = createTarget(supervisorAuthRepository, oauth2Config);
    noPermissionsRechte = createTarget(noPermissionAuthRepository, oauth2Config);
  }

  abstract T createTarget(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config);

}
