package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.hsp_frontend;

import static com.github.dreamhead.moco.Moco.by;
import static com.github.dreamhead.moco.Moco.header;
import static com.github.dreamhead.moco.Moco.httpServer;
import static com.github.dreamhead.moco.Moco.json;
import static com.github.dreamhead.moco.Moco.uri;

import com.github.dreamhead.moco.HttpServer;
import com.github.dreamhead.moco.Runner;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 17.11.23.
 * @version 1.0
 */
public class DiscoveryServiceStubSetup {

  private static Runner RUNNER;

  @BeforeAll
  static void setup() throws URISyntaxException, IOException {
    URL urlInfoDoc = DiscoveryServicePortTest.class.getClassLoader()
        .getResource("hsp_fo_discovery_response.json");
    String jsonInfoDoc = Files.readString(Paths.get(Objects.requireNonNull(urlInfoDoc).toURI()));
    URL urlStatsDoc = DiscoveryServicePortTest.class.getClassLoader()
        .getResource("hsp_fo_discovery_stats_response.json");
    String jsonStatsDoc = Files.readString(Paths.get(Objects.requireNonNull(urlStatsDoc).toURI()));

    HttpServer server = httpServer(8899);
    server.request(by(uri("/api/info")))
        .response(json(jsonInfoDoc), header("content-type", "application/json"));

    server.request(by(uri("/api/stats")))
        .response(json(jsonStatsDoc), header("content-type", "application/json"));

    RUNNER = Runner.runner(server);
    RUNNER.start();
  }

  @AfterAll
  static void afterAll() {
    RUNNER.stop();
  }

}
