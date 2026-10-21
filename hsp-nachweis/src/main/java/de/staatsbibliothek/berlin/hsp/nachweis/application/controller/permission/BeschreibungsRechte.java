package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ERSCHLIESSER_COMMON_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_LOKALE_REDAKTEUR_COMMON_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_REDAKTEUR_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_SUPER_REDAKTEUR_APP;

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
@Named("beschreibungsRechte")
public class BeschreibungsRechte extends RechteCommon {

  public static final String PERMISSION_BESCHREIBUNG_NICHT_GESPERRTE_ALLE_DRITTER_LESEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_BESCHREIBUNG_LISTE_ALLER_DRITTER_LESEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_BESCHREIBUNG_INTERNE_ANLEGEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_BESCHREIBUNG_EIGENE_LESEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_BESCHREIBUNG_ALLER_DRITTER_HART_LOESCHEN = B_HSP_SUPER_REDAKTEUR_APP;
  public static final String PERMISSION_BESCHREIBUNG_BEARBEITER_ENTZIEHEN = B_HSP_LOKALE_REDAKTEUR_COMMON_APP;
  public static final String PERMISSION_BESCHREIBUNG_LOESCHEN = B_HSP_ERSCHLIESSER_COMMON_APP;
  public static final String PERMISSION_BESCHREIBUNG_PUBLIZIEREN_SEHEN = B_HSP_REDAKTEUR_APP;
  public static final String PERMISSION_BESCHREIBUNG_PUBLIZIEREN_VERWALTEN = B_HSP_SUPER_REDAKTEUR_APP;
  public static final String PERMISSION_KOD_UPDATE_TEI = B_HSP_REDAKTEUR_APP;
  @Serial
  private static final long serialVersionUID = 7248634030303811840L;

  @Inject
  public BeschreibungsRechte(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    super(authenticationRepository, oauth2Config,
        LoggerFactory.getLogger(BeschreibungsRechte.class));
  }

  public boolean kannNichtGesperrteAllerDritterLesen() {
    return hasAccessWithPermission(PERMISSION_BESCHREIBUNG_NICHT_GESPERRTE_ALLE_DRITTER_LESEN);
  }

  public boolean kannListeAllerDritterLesen() {
    return hasAccessWithPermission(PERMISSION_BESCHREIBUNG_LISTE_ALLER_DRITTER_LESEN);
  }

  public boolean kannInterneAnlegen() {
    return hasAccessWithPermission(PERMISSION_BESCHREIBUNG_INTERNE_ANLEGEN);
  }

  public boolean kannEigeneLesen() {
    return hasAccessWithPermission(PERMISSION_BESCHREIBUNG_EIGENE_LESEN);
  }

  public boolean kannAllerDritterhartLoeschen() {
    return hasAccessWithPermission(PERMISSION_BESCHREIBUNG_ALLER_DRITTER_HART_LOESCHEN);
  }

  public boolean kannLoeschen() {
    return hasAccessWithPermission(PERMISSION_BESCHREIBUNG_LOESCHEN);
  }

  public boolean kannPublikationSehen() {
    return hasAccessWithPermission(PERMISSION_BESCHREIBUNG_PUBLIZIEREN_SEHEN);
  }

  public boolean kannPublikationVerwalten() {
    return hasAccessWithPermission(PERMISSION_BESCHREIBUNG_PUBLIZIEREN_VERWALTEN);
  }
}
