package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.GNDServiceRechte.PERMISSION_CONTROL_GNDSERVICE;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.05.25.
 * @version 1.0
 */
public class GNDServiceRechteTest extends
    InitPermissionsTestAuth<GNDServiceRechte> {

  static Set<String> permission = new HashSet<>(
      List.of(PERMISSION_CONTROL_GNDSERVICE));

  public GNDServiceRechteTest() {
    super(permission);
  }

  @Test
  void testKannGNDServiceVerwalten() {
    assertTrue(supervisorRechte.kannGNDServiceVerwalten());
  }

  @Override
  GNDServiceRechte createTarget(
      AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    return new GNDServiceRechte(authenticationRepository, oauth2Config);
  }
}
