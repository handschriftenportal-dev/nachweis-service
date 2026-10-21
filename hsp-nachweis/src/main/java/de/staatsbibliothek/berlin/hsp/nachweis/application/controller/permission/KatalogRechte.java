package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ERSCHLIESSER_COMMON_APP;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.LoggerFactory;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 21.10.2022
 */
@SessionScoped
@Named("katalogRechte")
public class KatalogRechte extends RechteCommon {

  public static final String PERMISSION_KATALOGE_ALS_LISTE_ANZEIGEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_KATALOG_ANZEIGEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  private static final long serialVersionUID = 246053147688451645L;

  @Inject
  public KatalogRechte(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    super(authenticationRepository, oauth2Config, LoggerFactory.getLogger(KatalogRechte.class));
  }

  public boolean kannAlsListeAnzeigen() {
    return hasAccessWithPermission(PERMISSION_KATALOGE_ALS_LISTE_ANZEIGEN);
  }

  public boolean kannAnzeigen() {
    return hasAccessWithPermission(PERMISSION_KATALOG_ANZEIGEN);
  }
}
