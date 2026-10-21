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
 * Created by michael.hintersonnleitner@sbb.spk-berlin.de on 31.08.2021.
 */
@QuarkusTest
public class SperrenControllerIT {

  @TestHTTPResource("sperren.xhtml")
  URL url;

  @Test
  void testOpenPage() throws IOException {

    try (InputStream in = url.openStream()) {
      String contents = readStream(in);
      Assertions.assertTrue(contents.contains("Sperren"));
    }
  }
}
