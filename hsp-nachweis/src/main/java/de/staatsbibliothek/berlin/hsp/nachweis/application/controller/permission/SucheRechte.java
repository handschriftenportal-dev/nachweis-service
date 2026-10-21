package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ERSCHLIESSER_COMMON_APP;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import org.slf4j.LoggerFactory;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 17.06.21
 */
@SessionScoped
@Named("sucheRechte")
public class SucheRechte extends RechteCommon {

  public static final String PERMISSION_AUFRUFEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_ERGEBNISSE_SEHEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_FILTERN_UND_FACETTIEREN = B_HSP_ERSCHLIESSER_COMMON_APP;
  private static final long serialVersionUID = 9078120026054104836L;

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
