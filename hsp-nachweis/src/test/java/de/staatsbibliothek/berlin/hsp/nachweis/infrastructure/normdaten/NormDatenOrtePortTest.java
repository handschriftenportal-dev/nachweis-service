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
public class NormDatenOrtePortTest {

  private static Runner RUNNER;

  @RestClient
  @Inject
  NormDatenOrtePort client;

  @BeforeAll
  public static void setup() throws URISyntaxException, IOException {
    var url = NormDatenOrtePortTest.class.getClassLoader().getResource("4005728-8.json");
    var json = Files.readString(Paths.get(url.toURI()));

    var server = httpServer(8899, log());
    server.post(by(uri("/rest/place")))
        .response(json(json), header("content-type", "application/json"));

    RUNNER = Runner.runner(server);
    RUNNER.start();
  }

  @Test
  void testCreatePlaceWithGNDEntityFact() {

    GNDEntityFact berlin = new GNDEntityFact.GNDEntityFactBuilder()
        .withPreferredName("Berlin")
        .addVariantName(new VariantName("Berlina"))
        .build();

    GNDEntityFact result = client.createPlaceWithGNDEnityFact(berlin);

    assertNotNull(result);
    assertEquals("Berlin", result.getPreferredName());
  }

  @AfterAll
  static void tearDown() {
    RUNNER.stop();
  }
}
