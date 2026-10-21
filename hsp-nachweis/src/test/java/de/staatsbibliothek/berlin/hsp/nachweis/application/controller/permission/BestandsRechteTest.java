package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BestandsRechte.PERMISSION_INDEXING_PRAESENTATION;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.nachweis.SystemConfig;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 06.06.23.
 * @version 1.0
 */
public class BestandsRechteTest extends InitPermissionsTestAuth<BestandsRechte> {

  final static Set<String> roles = new HashSet<>(
      List.of(PERMISSION_INDEXING_PRAESENTATION));

  private SystemConfig systemConfig;

  public BestandsRechteTest() {
    super(roles);
  }

  @Override
  BestandsRechte createTarget(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config) {
    if (systemConfig == null) {
      systemConfig = mock(SystemConfig.class);
    }
    return new BestandsRechte(authenticationRepository, oauth2Config, systemConfig);
  }

  @BeforeEach
  void defaultFlag() {
    reset(systemConfig);
    when(systemConfig.isPresentationIndexingEnabled()).thenReturn(true);
  }

  @Test
  void testKannPraesentationIndexing() {
    assertTrue(supervisorRechte.kannStartIndexing());
    assertFalse(noPermissionsRechte.kannStartIndexing());
  }

  @Test
  void testKannPraesentationIndexing_featureDisabled() {
    when(systemConfig.isPresentationIndexingEnabled()).thenReturn(false);
    assertFalse(supervisorRechte.kannStartIndexing());
  }
}
