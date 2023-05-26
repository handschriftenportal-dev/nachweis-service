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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 04.08.22
 */

public class SBBPURLGenPortTest {

  private static Runner RUNNER;

  @BeforeAll
  public static void setup() throws URISyntaxException, IOException {
    var url = SBBPURLGenPortTest.class.getClassLoader().getResource("purlgen.json");
    var json = Files.readString(Paths.get(Objects.requireNonNull(url).toURI()));

    var server = httpServer(8899, log());
    server.request(by(uri("/sbbpurl/purlgen.pl")))
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
    SBBPURLGenPort client = RestClientBuilder.newBuilder().
        baseUri(baseURI).
        build(SBBPURLGenPort.class);

    SBBPURLGenResponse response = client.createPURL(PARAM_PID, PARAM_MVID);

    assertNotNull(response);
    assertEquals("OK", response.getStatus());
    assertEquals("HSP", response.getPid());
    assertEquals("00000023", response.getMvid());
    assertEquals("0000", response.getVid());
    assertEquals("0000", response.getPage());
    assertEquals("HSP0000002300000000", response.getFullPid());
  }
}
