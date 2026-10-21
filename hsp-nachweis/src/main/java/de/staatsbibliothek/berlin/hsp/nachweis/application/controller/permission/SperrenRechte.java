package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ERSCHLIESSER_COMMON_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_REDAKTEUR_APP;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import org.slf4j.LoggerFactory;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 25.08.21
 */
@SessionScoped
@Named("sperrenRechte")
public class SperrenRechte extends RechteCommon {

  public static final String PERMISSION_LISTE_LESEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_LOESCHEN = B_HSP_REDAKTEUR_APP;

  private static final long serialVersionUID = 8299118395619791117L;

  public SperrenRechte(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    super(authenticationRepository, oauth2Config, LoggerFactory.getLogger(SucheRechte.class));
  }

  public boolean kannAlsListeAnzeigen() {
    return hasAccessWithPermission(PERMISSION_LISTE_LESEN);
  }

  public boolean kannLoeschen() {
    return hasAccessWithPermission(PERMISSION_LOESCHEN);
  }

}
