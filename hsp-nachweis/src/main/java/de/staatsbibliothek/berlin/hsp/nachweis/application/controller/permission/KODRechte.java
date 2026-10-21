package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ERSCHLIESSER_COMMON_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_LOKALE_REDAKTEUR_COMMON_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_SUPER_REDAKTEUR_APP;

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
@Named("kODRechte")
public class KODRechte extends RechteCommon {

  public static final String PERMISSION_KOD_REGISTRIEREN = B_HSP_LOKALE_REDAKTEUR_COMMON_APP;
  public static final String PERMISSION_KOD_LESEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_KOD_LISTE_LESEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_KOD_HART_LOESCHEN = B_HSP_SUPER_REDAKTEUR_APP;
  public static final String PERMISSION_KOD_BEARBEITEN = B_HSP_LOKALE_REDAKTEUR_COMMON_APP;
  public static final String PERMISSION_KOD_UMZIEHEN = B_HSP_LOKALE_REDAKTEUR_COMMON_APP;
  private static final long serialVersionUID = 9221181578279834751L;

  @Inject
  public KODRechte(AuthenticationRepository authenticationRepository, OAUTH2Config oauth2Config) {
    super(authenticationRepository, oauth2Config, LoggerFactory.getLogger(KODRechte.class));
  }

  public boolean kannRegistrieren() {
    return hasAccessWithPermission(PERMISSION_KOD_REGISTRIEREN);
  }


  public boolean kannLesen() {
    return hasAccessWithPermission(PERMISSION_KOD_LESEN);
  }

  public boolean kannListeLesen() {
    return hasAccessWithPermission(PERMISSION_KOD_LISTE_LESEN);
  }

  public boolean kannHartLoeschen() {
    return hasAccessWithPermission(PERMISSION_KOD_HART_LOESCHEN);
  }

  public boolean kannKodUmziehen() {
    return hasAccessWithPermission(PERMISSION_KOD_UMZIEHEN);
  }

  public boolean kannBearbeiten() {
    return hasAccessWithPermission(PERMISSION_KOD_BEARBEITEN);
  }

}
