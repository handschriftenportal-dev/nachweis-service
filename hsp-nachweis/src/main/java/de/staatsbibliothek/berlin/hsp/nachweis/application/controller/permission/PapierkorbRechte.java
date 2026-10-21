package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_REDAKTEUR_APP;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.LoggerFactory;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 30.11.22
 */

@SessionScoped
@Named("papierkorbRechte")
public class PapierkorbRechte extends RechteCommon {

  public static final String PERMISSION_PAPIERKORB_ALS_LISTE_ANZEIGEN = B_HSP_REDAKTEUR_APP;
  private static final long serialVersionUID = -164319952016248074L;

  @Inject
  public PapierkorbRechte(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    super(authenticationRepository, oauth2Config, LoggerFactory.getLogger(NormdatenRechte.class));
  }

  public boolean kannAlsListeAnzeigen() {
    return hasAccessWithPermission(PERMISSION_PAPIERKORB_ALS_LISTE_ANZEIGEN);
  }

}
