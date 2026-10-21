package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.StreamReader.readStream;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 19.05.25.
 * @version 1.0
 */

@QuarkusTest
public class GNDServiceControllerIT {


  @TestHTTPResource("gndservice.xhtml")
  URL gndserviceRessource;

  @Test
  void testOpenPage() throws IOException {

    try (InputStream in = gndserviceRessource.openStream()) {
      String contents = readStream(in);
      assertTrue(contents.contains("GND-Service"));
    }
  }

}
