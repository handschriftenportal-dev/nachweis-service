package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import static com.github.dreamhead.moco.Moco.by;
import static com.github.dreamhead.moco.Moco.header;
import static com.github.dreamhead.moco.Moco.httpServer;
import static com.github.dreamhead.moco.Moco.json;
import static com.github.dreamhead.moco.Moco.log;
import static com.github.dreamhead.moco.Moco.uri;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.dreamhead.moco.Moco;
import com.github.dreamhead.moco.Runner;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 20.05.25.
 * @version 1.0
 */

@QuarkusTest
class NormdatenGNDServicePortTest {

  @RestClient
  @Inject
  NormdatenGNDServicePort normdatenGNDServicePort;

  private static Runner stubServer;

  @BeforeAll
  static void setup() throws URISyntaxException, IOException {
    var url = NormdatenGNDServicePortTest.class.getClassLoader()
        .getResource("normdaten_gndservice.json");
    var json = Files.readString(Paths.get(url.toURI()));

    var server = httpServer(8899, log());

    /*
     * Normdatenservice Synchronisation API Contract to control the Handling for
     * the GNDService behind.
     */

    server.get(by(uri("/rest/gndservice/sync")))
        .response(json(json), header("content-type", "application/json"));

    server.post(by(uri("/rest/gndservice/sync")))
        .response(json(json), header("content-type", "application/json"));

    server.delete(by(uri("/rest/gndservice/sync")))
        .response(Moco.status(200), header("content-type", "application/json"));

    server.put(by(uri("/rest/gndservice/sync")))
        .response(json(json), header("content-type", "application/json"));

    stubServer = Runner.runner(server);
    stubServer.start();
  }

  @AfterAll
  static void tearDown() {
    stubServer.stop();
  }

  @Test
  void testInjectPort() {
    assertNotNull(normdatenGNDServicePort);
  }

  @Test
  void testGetGNDServiceSyncSubset() {
    GNDServiceSubset subset = normdatenGNDServicePort.findGNDServiceSubset();
    assertNotNull(subset);
    assertEquals("8eb42a5c-ba8f-40f7-a8d2-ab4e367c40d8", subset.getId());
    assertEquals("Handschriftenportal-Normdatenservice", subset.getName());
    assertTrue(subset.isActive());
    assertEquals(3, subset.getGndIDs().size());
    assertEquals(30, subset.getIntervalInMinutes());
  }

  @Test
  void testRegistration() {
    assertDoesNotThrow(() -> normdatenGNDServicePort.registerSynchronistation(
        new NormdatenserviceSync(30, false, "gnddata")));
  }

  @Test
  void testChangeSynchronisation() {
    GNDServiceSubset subset = normdatenGNDServicePort.updateSynchronistation(
        new NormdatenserviceSync(30, false, "gnddata"));

    assertNotNull(subset);
    assertEquals("Handschriftenportal-Normdatenservice", subset.getName());
  }

  @Test
  void testUnregistration() {
    assertDoesNotThrow(
        () -> normdatenGNDServicePort.unregisterSynchronistation());
  }
}
