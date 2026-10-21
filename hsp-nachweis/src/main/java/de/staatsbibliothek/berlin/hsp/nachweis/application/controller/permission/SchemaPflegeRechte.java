package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_SUPER_REDAKTEUR_APP;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.LoggerFactory;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 29.03.2022
 */
@SessionScoped
@Named("schemaPflegeRechte")
public class SchemaPflegeRechte extends RechteCommon {

  public static final String PERMISSION_SCHEMAPFLEGEDATEN_ALS_LISTE_ANZEIGEN = B_HSP_SUPER_REDAKTEUR_APP;

  public static final String PERMISSION_SCHEMAPFLEGEDATEN_ERSETZEN = B_HSP_SUPER_REDAKTEUR_APP;

  public static final String PERMISSION_AUTOMATISCHE_UEBERNAHME_AKTIVIEREN = B_HSP_SUPER_REDAKTEUR_APP;

  private static final long serialVersionUID = -5674178853479367637L;

  @Inject
  public SchemaPflegeRechte(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    super(authenticationRepository, oauth2Config,
        LoggerFactory.getLogger(SchemaPflegeRechte.class));
  }

  public boolean kannAlsListeAnzeigen() {
    return hasAccessWithPermission(PERMISSION_SCHEMAPFLEGEDATEN_ALS_LISTE_ANZEIGEN);
  }

  public boolean kannDateienErsetzen() {
    return hasAccessWithPermission(PERMISSION_SCHEMAPFLEGEDATEN_ERSETZEN);
  }

  public boolean kannAutomatischeUebernahmeAktivieren() {
    return hasAccessWithPermission(PERMISSION_AUTOMATISCHE_UEBERNAHME_AKTIVIEREN);
  }

}
