package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

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
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 06.03.2023
 */
@SessionScoped
@Named("werkzeugeRechte")
public class WerkzeugeRechte extends RechteCommon {

  public static final String PERMISSION_WERKZEUGE_ANWENDEN = B_HSP_REDAKTEUR_APP;
  public static final String PERMISSION_KANN_NORMDATEN_AKTUALISIEREN = B_HSP_SUPER_REDAKTEUR_APP;
  public static final String PERMISSION_KANN_KOD_KERNDATEN_BEI_IMPORT_UEBERSCHREIBEN = B_HSP_SUPER_REDAKTEUR_APP;
  public static final String PERMISSION_KANN_AKTUALISIEREN_ALLE_KOD_XML = B_HSP_SUPER_REDAKTEUR_APP;
  public static final String PERMISSION_KANN_BESCHREIBUNGEN_INDEX_AKTUALISIEREN = B_HSP_SUPER_REDAKTEUR_APP;
  public static final String PERMISSION_KANN_KODS_INDEX_AKTUALISIEREN = B_HSP_SUPER_REDAKTEUR_APP;
  public static final String PERMISSION_KANN_PRAESENTATION_INDEX_AKTUALISIEREN = B_HSP_SUPER_REDAKTEUR_APP;
  @Serial
  private static final long serialVersionUID = 3236390569356295183L;

  @Inject
  public WerkzeugeRechte(
      AuthenticationRepository authenticationRepository, OAUTH2Config oauth2Config) {
    super(
        authenticationRepository, oauth2Config, LoggerFactory.getLogger(SchemaPflegeRechte.class));
  }

  public boolean kannWerkzeugeAnwenden() {
    return hasAccessWithPermission(PERMISSION_WERKZEUGE_ANWENDEN);
  }

  public boolean kannNormdatenAktualisieren() {
    return hasAccessWithPermission(PERMISSION_KANN_NORMDATEN_AKTUALISIEREN);
  }

  public boolean kannKodKerndatenBeiImportUeberschreiben() {
    return hasAccessWithPermission(PERMISSION_KANN_KOD_KERNDATEN_BEI_IMPORT_UEBERSCHREIBEN);
  }

  public boolean kannAktualisierenAlleKodXml() {
    return hasAccessWithPermission(PERMISSION_KANN_AKTUALISIEREN_ALLE_KOD_XML);
  }

  public boolean kannBeschreibungenIndexAktualisieren() {
    return hasAccessWithPermission(PERMISSION_KANN_BESCHREIBUNGEN_INDEX_AKTUALISIEREN);
  }

  public boolean kannKodsIndexAktualisieren() {
    return hasAccessWithPermission(PERMISSION_KANN_KODS_INDEX_AKTUALISIEREN);
  }

  public boolean kannPraesentationIndexAktualisieren() {
    return hasAccessWithPermission(PERMISSION_KANN_PRAESENTATION_INDEX_AKTUALISIEREN);
  }
}
