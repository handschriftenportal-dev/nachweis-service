package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.http;

import static com.github.dreamhead.moco.Moco.by;
import static com.github.dreamhead.moco.Moco.header;
import static com.github.dreamhead.moco.Moco.httpServer;
import static com.github.dreamhead.moco.Moco.json;
import static com.github.dreamhead.moco.Moco.log;
import static com.github.dreamhead.moco.Moco.uri;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.dreamhead.moco.Runner;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.DNBCulturgraphPortTest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 20.07.22
 */

public class HttpServiceTest {

  private static Runner RUNNER;

  @BeforeAll
  public static void setup() throws URISyntaxException, IOException {
    var url = DNBCulturgraphPortTest.class.getClassLoader().getResource("manifest-katalog_Biberach.json");
    var json = Files.readString(Paths.get(url.toURI()));

    var server = httpServer(8899, log());
    server.request(by(uri("/0000034258/manifest.json")))
        .response(json(json), header("content-type", "application/json"));

    RUNNER = Runner.runner(server);
    RUNNER.start();
  }

  @AfterAll
  public static void tearDown() {
    RUNNER.stop();
  }

  @Test
  void testFindGNDEntityFactByID() throws Exception {
    HttpService httpService = new HttpService(null, null);

    URI targetURL = URI.create("http://localhost:8899/0000034258/manifest.json");

    Optional<String> body = httpService.loadBodyFromURL(targetURL);

    assertNotNull(body);
    assertTrue(body.isPresent());
    assertTrue(body.get().contains("https://iiif.ub.uni-leipzig.de/0000034258/manifest.json"));
  }

}
