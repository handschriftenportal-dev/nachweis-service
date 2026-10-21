package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import static com.github.dreamhead.moco.Moco.by;
import static com.github.dreamhead.moco.Moco.header;
import static com.github.dreamhead.moco.Moco.httpServer;
import static com.github.dreamhead.moco.Moco.json;
import static com.github.dreamhead.moco.Moco.log;
import static com.github.dreamhead.moco.Moco.uri;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 03.06.2022.
 * @version 1.0
 */

@QuarkusTest
public class LobidOrgPortTest {

  private static Runner RUNNER;

  @RestClient
  @Inject
  LobidOrgPort client;

  @BeforeAll
  public static void setup() throws URISyntaxException, IOException {
    var url = LobidOrgPortTest.class.getClassLoader().getResource("4005728-8.json");
    var json = Files.readString(Paths.get(url.toURI()));

    var server = httpServer(8899, log());
    server.request(by(uri("/gnd/4005728-8.json")))
        .response(json(json), header("content-type", "application/json"));

    RUNNER = Runner.runner(server);
    RUNNER.start();
  }

  @AfterAll
  public static void tearDown() {
    RUNNER.stop();
  }

  @Test
  void testFindGNDEntityFactByID() {

    var berlin = client.findByGNDID("4005728-8.json");

    assertNotNull(berlin);
    assertEquals("Berlin", berlin.getPreferredName());
    assertEquals(25, berlin.getVariantName().size());
  }
}
