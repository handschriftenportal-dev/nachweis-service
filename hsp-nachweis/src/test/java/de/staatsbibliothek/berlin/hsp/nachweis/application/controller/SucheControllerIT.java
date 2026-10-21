package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.StreamReader.readStream;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 04.03.2020.
 *
 * @see SucheController
 */
@QuarkusTest
class SucheControllerIT {

  @TestHTTPResource("suche.xhtml")
  URL url;

  @Test
  void testOpenPage() throws IOException {

    try (InputStream in = url.openStream()) {
      String contents = readStream(in);
      Assertions.assertTrue(contents.contains("Suche nach Kulturobjekten und Beschreibungen"));
    }
  }

}
