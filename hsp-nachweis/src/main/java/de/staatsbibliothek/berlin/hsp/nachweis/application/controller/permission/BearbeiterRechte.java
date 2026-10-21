package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ERSCHLIESSER_COMMON_APP;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.LoggerFactory;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 11.11.2021
 */
@SessionScoped
@Named("bearbeiterRechte")
public class BearbeiterRechte extends RechteCommon {

  public static final String PERMISSION_BEARBEITER_ANZEIGEN = B_HSP_ERSCHLIESSER_COMMON_APP;

  private static final long serialVersionUID = 9221181578279834751L;

  @Inject
  public BearbeiterRechte(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    super(authenticationRepository, oauth2Config, LoggerFactory.getLogger(BearbeiterRechte.class));
  }

  public boolean kannAnzeigen() {
    return hasAccessWithPermission(PERMISSION_BEARBEITER_ANZEIGEN);
  }

}
