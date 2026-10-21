package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ERSCHLIESSER_COMMON_APP;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.LoggerFactory;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 15.06.21
 */
@SessionScoped
@Named("digitalisatsRechte")
public class DigitalisatsRechte extends RechteCommon {

  public static final String PERMISSION_DIGITALISAT_MANUELL_ANLEGEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_DIGITALISAT_BEARBEITEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_DIGITALISAT_LESEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_DIGITALISAT_LOESCHEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  private static final long serialVersionUID = -5400457903567762988L;

  @Inject
  public DigitalisatsRechte(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    super(authenticationRepository, oauth2Config,
        LoggerFactory.getLogger(DigitalisatsRechte.class));
  }

  public boolean kannManuellAnlegen() {
    return hasAccessWithPermission(PERMISSION_DIGITALISAT_MANUELL_ANLEGEN);
  }

  public boolean kannBearbeiten() {
    return hasAccessWithPermission(PERMISSION_DIGITALISAT_BEARBEITEN);
  }

  public boolean kannLesen() {
    return hasAccessWithPermission(PERMISSION_DIGITALISAT_LESEN);
  }

  public boolean kannLoeschen() {
    return hasAccessWithPermission(PERMISSION_DIGITALISAT_LOESCHEN);
  }
}
