package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.hsp_frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 24.04.23.
 * @version 1.0
 */

@QuarkusTest
@Execution(ExecutionMode.SAME_THREAD)
class DiscoveryServicePortTest extends DiscoveryServiceStubSetup {

  @Inject
  @RestClient
  DiscoveryServicePort discoveryServicePort;

  @Test
  void testCreation() {
    assertNotNull(discoveryServicePort);
  }

  @Test
  void testFindInfo() {

    DiscoveryServiceInfoDTO infoDTO = discoveryServicePort.findInfo();

    assertNotNull(infoDTO);

    assertEquals("hsp-fo-discovery", infoDTO.getComponent());
    assertEquals("Handschriftenportal Front Office Discovery Service", infoDTO.getDescription());
    assertEquals("1.5.0", infoDTO.getVersion());
    assertEquals(133232, infoDTO.getItems().getHspObjects());
    assertEquals(16772, infoDTO.getItems().getHspDescriptions());
    assertEquals(22341, infoDTO.getItems().getHspDigitized());
    assertEquals(40155, infoDTO.getItems().getHspDescriptionsRetro());
  }

  @Test
  void testFindStats() {
    DiscoveryServiceStatsDTO discoveryServiceStatsDTO = discoveryServicePort.findStats();

    assertNotNull(discoveryServiceStatsDTO);

    assertEquals(240, discoveryServiceStatsDTO.getKods().getItems().size());

    assertEquals(71, discoveryServiceStatsDTO.getDigitizedItems().getItems().size());

    assertEquals(46066, discoveryServiceStatsDTO.getDescription().getDescriptionRetro());

    assertEquals(33388, discoveryServiceStatsDTO.getDescription().getExternal());

    assertEquals(0, discoveryServiceStatsDTO.getDescription().getInternal());
  }
}
