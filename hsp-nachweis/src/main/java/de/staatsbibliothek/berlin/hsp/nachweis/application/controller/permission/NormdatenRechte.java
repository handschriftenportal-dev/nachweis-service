package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ERSCHLIESSER_COMMON_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_REDAKTEUR_APP;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serial;
import org.slf4j.LoggerFactory;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 15.06.21
 */
@SessionScoped
@Named("normdatenRechte")
public class NormdatenRechte extends RechteCommon {

  public static final String PERMISSION_NORMDATEN_ALS_LISTE_ANZEIGEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_NORMDATEN_TABELLE_VERWALTEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_NORMDATEN_ALS_EXCEL_RUNTERLADEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_NORMDATEN_ANZEIGEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_CONTROL_AUTHORITYDETECTION = B_HSP_REDAKTEUR_APP;
  @Serial
  private static final long serialVersionUID = -164319952016248074L;

  @Inject
  public NormdatenRechte(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    super(authenticationRepository, oauth2Config, LoggerFactory.getLogger(NormdatenRechte.class));
  }

  public boolean kannAlsListeAnzeigen() {
    return hasAccessWithPermission(PERMISSION_NORMDATEN_ALS_LISTE_ANZEIGEN);
  }

  public boolean kannTabelleVerwalten() {
    return hasAccessWithPermission(PERMISSION_NORMDATEN_TABELLE_VERWALTEN);
  }

  public boolean kannAlsExcelRunterladen() {
    return hasAccessWithPermission(PERMISSION_NORMDATEN_ALS_EXCEL_RUNTERLADEN);
  }

  public boolean kannAnzeigen() {
    return hasAccessWithPermission(PERMISSION_NORMDATEN_ANZEIGEN);
  }

  public boolean kannNormdatenerkennung() {
    return hasAccessWithPermission(PERMISSION_CONTROL_AUTHORITYDETECTION);
  }
}
