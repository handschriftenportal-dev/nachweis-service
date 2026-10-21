package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.purl;

import static com.github.dreamhead.moco.Moco.by;
import static com.github.dreamhead.moco.Moco.header;
import static com.github.dreamhead.moco.Moco.httpServer;
import static com.github.dreamhead.moco.Moco.json;
import static com.github.dreamhead.moco.Moco.log;
import static com.github.dreamhead.moco.Moco.uri;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.purl.SBBPURLGenPort.PARAM_MVID;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.purl.SBBPURLGenPort.PARAM_PID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.dreamhead.moco.Runner;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 04.08.22
 */

@QuarkusTest
public class SBBPURLGenPortTest {

  private static Runner RUNNER;

  @Inject
  @RestClient
  SBBPURLGenPort sbbpURLGenPort;

  @BeforeAll
  public static void setup() throws URISyntaxException, IOException {
    var url = SBBPURLGenPortTest.class.getClassLoader().getResource("purlgen.json");
    var json = Files.readString(Paths.get(Objects.requireNonNull(url).toURI()));

    var server = httpServer(8899, log());
    server.request(by(uri("/rest/purl/example")))
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
    URI baseURI = URI.create("http://localhost:8899/sbbpurl/purlgen.pl");

    SBBPURLGenResponse response = sbbpURLGenPort.createPURL(PARAM_PID, PARAM_MVID);

    assertNotNull(response);
    assertEquals("OK", response.getStatus());
    assertEquals("HSP", response.getPid());
    assertEquals("00000023", response.getMvid());
    assertEquals("0000", response.getVid());
    assertEquals("0000", response.getPage());
    assertEquals("HSP0000002300000000", response.getFullPid());
  }
}
