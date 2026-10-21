package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_LOKALE_REDAKTEUR_COMMON_APP;

import de.staatsbibliothek.berlin.hsp.nachweis.SystemConfig;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serial;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 06.06.23.
 * @version 1.0
 */

@SessionScoped
@Named("bestandsRechte")
public class BestandsRechte extends RechteCommon {

  public static final String PERMISSION_INDEXING_PRAESENTATION = B_HSP_LOKALE_REDAKTEUR_COMMON_APP;
  @Serial
  private static final long serialVersionUID = -4136571716799777415L;

  private final SystemConfig systemConfig;

  @Inject
  public BestandsRechte(
      AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config,
      SystemConfig systemConfig) {
    super(authenticationRepository, oauth2Config, LoggerFactory.getLogger(BestandsRechte.class));
    this.systemConfig = systemConfig;
  }

  public boolean kannStartIndexing() {
    return systemConfig.isPresentationIndexingEnabled()
        && hasAccessWithPermission(PERMISSION_INDEXING_PRAESENTATION);
  }
}
