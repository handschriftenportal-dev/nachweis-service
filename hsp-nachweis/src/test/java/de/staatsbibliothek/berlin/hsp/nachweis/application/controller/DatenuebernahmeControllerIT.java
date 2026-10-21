package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;


import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.StreamReader.readStream;

import de.staatsbibliothek.berlin.hsp.nachweis.WiremockITServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 12.06.2019.
 */

@QuarkusTest
@QuarkusTestResource(WiremockITServer.class)
class DatenuebernahmeControllerIT {

  @TestHTTPResource("importvorgaenge.xhtml")
  URL url;

  @Test
  void testOpenPage() throws IOException {

    try (InputStream in = url.openStream()) {
      String contents = readStream(in);
      Assertions.assertTrue(contents.contains("Importvorgänge"));
    }
  }

}
