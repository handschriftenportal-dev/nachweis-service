package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_LOKALE_REDAKTEUR_COMMON_APP;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.LoggerFactory;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 08.06.2022
 */
@SessionScoped
@Named("navigationRechte")
public class NavigationRechte extends RechteCommon {

  public static final String PERMISSION_REDAKTION_ANZEIGEN = B_HSP_LOKALE_REDAKTEUR_COMMON_APP;

  private static final long serialVersionUID = -4423638903001345348L;

  @Inject
  public NavigationRechte(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    super(authenticationRepository, oauth2Config, LoggerFactory.getLogger(NavigationRechte.class));
  }

  public boolean kannRedaktionAnzeigen() {
    return hasAccessWithPermission(PERMISSION_REDAKTION_ANZEIGEN);
  }
}
