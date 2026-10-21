package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_SUPER_REDAKTEUR_APP;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serial;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 19.05.25.
 * @version 1.0
 */

@Named("gndserviceRechte")
@RequestScoped
public class GNDServiceRechte extends RechteCommon {

  @Inject
  public GNDServiceRechte(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    super(authenticationRepository, oauth2Config,
        LoggerFactory.getLogger(GNDServiceRechte.class));
  }

  @Serial
  private static final long serialVersionUID = -3826167584172656037L;

  public static final String PERMISSION_CONTROL_GNDSERVICE = B_HSP_SUPER_REDAKTEUR_APP;

  public boolean kannGNDServiceVerwalten() {
    return hasAccessWithPermission(PERMISSION_CONTROL_GNDSERVICE);
  }
}
