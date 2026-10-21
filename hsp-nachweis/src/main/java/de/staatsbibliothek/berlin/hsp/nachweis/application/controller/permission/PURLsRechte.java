package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_SUPER_REDAKTEUR_APP;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import org.slf4j.LoggerFactory;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 09.08.22
 */
@SessionScoped
@Named("pURLsRechte")
public class PURLsRechte extends RechteCommon {

  public static final String PERMISSION_LISTE_LESEN = B_HSP_SUPER_REDAKTEUR_APP;
  public static final String PERMISSION_GENERIEREN = B_HSP_SUPER_REDAKTEUR_APP;
  public static final String PERMISSION_AKTUALISIEREN = B_HSP_SUPER_REDAKTEUR_APP;
  public static final String PERMISSION_RESOLVER_DATEI_GENERIEREN = B_HSP_SUPER_REDAKTEUR_APP;

  private static final long serialVersionUID = 8299118395619791117L;

  public PURLsRechte(AuthenticationRepository authenticationRepository, OAUTH2Config oauth2Config) {
    super(authenticationRepository, oauth2Config, LoggerFactory.getLogger(PURLsRechte.class));
  }

  public boolean kannAlsListeAnzeigen() {
    return hasAccessWithPermission(PERMISSION_LISTE_LESEN);
  }

  public boolean kannGenerieren() {
    return hasAccessWithPermission(PERMISSION_GENERIEREN);
  }

  public boolean kannAktualisieren() {
    return hasAccessWithPermission(PERMISSION_AKTUALISIEREN);
  }

  public boolean kannResolverDateiGenerieren() {
    return hasAccessWithPermission(PERMISSION_RESOLVER_DATEI_GENERIEREN);
  }


}
