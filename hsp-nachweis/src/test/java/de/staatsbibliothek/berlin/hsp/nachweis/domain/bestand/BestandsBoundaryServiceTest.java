/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand;

import static com.github.dreamhead.moco.Moco.by;
import static com.github.dreamhead.moco.Moco.header;
import static com.github.dreamhead.moco.Moco.httpServer;
import static com.github.dreamhead.moco.Moco.json;
import static com.github.dreamhead.moco.Moco.log;
import static com.github.dreamhead.moco.Moco.uri;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.dreamhead.moco.Runner;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.hsp_frontend.DiscoveryServicePortTest;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import javax.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.04.23.
 * @version 1.0
 */

@QuarkusTest
public class BestandsBoundaryServiceTest {

  @Inject
  private BestandsBoundary bestandsBoundary;

  private static Runner RUNNER;

  @BeforeAll
  public static void setup() throws URISyntaxException, IOException {
    var url = DiscoveryServicePortTest.class.getClassLoader().getResource("hsp_fo_discovery_response.json");
    var json = Files.readString(Paths.get(Objects.requireNonNull(url).toURI()));

    var server = httpServer(8888, log());
    server.request(by(uri("/api/info")))
        .response(json(json), header("content-type", "application/json"));

    RUNNER = Runner.runner(server);
    RUNNER.start();
  }

  @Test
  void testCreation() {

    assertNotNull(bestandsBoundary);
    assertNotNull(((BestandsBoundaryService) bestandsBoundary).getBestandsRepository());
    assertNotNull(((BestandsBoundaryService) bestandsBoundary).getDiscoveryServicePort());
  }

  @Test
  void testcreateBestandsView() {

    BestandsView view = bestandsBoundary.createBestandsView();

    assertNotNull(view);

    assertEquals(133232, view.getKulturobjekteBestandsElement().get().getBestandPraesentation());
    assertEquals(0, view.getKulturobjekteBestandsElement().get().getBestandNachweis());
  }

  @AfterAll
  static void afterAll() {
    RUNNER.stop();
  }
}
