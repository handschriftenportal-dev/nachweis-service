package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 12.06.2020.
 * @version 1.0
 */
@QuarkusTest
public class LanguageControllerTest {

  @TestHTTPResource("dashboard.xhtml")
  URL dashboard;

  @Test
  void testCreation() {

    LanguageController controller = new LanguageController();
    assertNotNull(controller);
  }

  @Test
  void testLanguageMenu() throws IOException {
    try (InputStream in = dashboard.openStream()) {
      String contents = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      Assertions.assertTrue(contents.contains("languageDialog"));
    }
  }
}
