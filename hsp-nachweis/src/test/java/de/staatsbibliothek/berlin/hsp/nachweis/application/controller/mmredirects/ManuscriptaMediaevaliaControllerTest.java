package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.mmredirects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 17.10.23.
 * @version 1.0
 */
public class ManuscriptaMediaevaliaControllerTest {

  @Test
  void testCreation() {

    MMBeschreibungURICreation mmBeschreibungURICreation = mock(
        MMBeschreibungURICreation.class);
    MMKatalogURICreation mmKatalogURICreation = mock(MMKatalogURICreation.class);
    MMSozietaetenURICreation mmSozietaetenURICreation = mock(MMSozietaetenURICreation.class);
    MMSignaturendokumenteURICreation mmSignaturendokumenteURICreation = mock(
        MMSignaturendokumenteURICreation.class);
    ManuscriptaMediaevaliaController controller = new ManuscriptaMediaevaliaController(
        mmBeschreibungURICreation, mmKatalogURICreation, mmSozietaetenURICreation,
        mmSignaturendokumenteURICreation);

    assertNotNull(controller);
  }

  @ParameterizedTest
  @MethodSource("getStaticURIMap")
  void testHandleRequestsForStaticPages(String path, URI target) {
    MMBeschreibungURICreation mmBeschreibungURICreation = mock(
        MMBeschreibungURICreation.class);
    MMKatalogURICreation mmKatalogURICreation = mock(MMKatalogURICreation.class);
    MMSozietaetenURICreation mmSozietaetenURICreation = mock(MMSozietaetenURICreation.class);
    MMSignaturendokumenteURICreation mmSignaturendokumenteURICreation = mock(
        MMSignaturendokumenteURICreation.class);

    ManuscriptaMediaevaliaController controller = new ManuscriptaMediaevaliaController(
        mmBeschreibungURICreation, mmKatalogURICreation, mmSozietaetenURICreation,
        mmSignaturendokumenteURICreation);

    Response response = controller.handleRequestForStaticMMRedirect(path);

    assertNotNull(response);

    assertEquals(301, response.getStatus());

    assertEquals(target,
        response.getLocation());
  }

  @Test
  void testHandleRequestForMMBilder() {
    MMBeschreibungURICreation mmBeschreibungURICreation = mock(
        MMBeschreibungURICreation.class);
    MMKatalogURICreation mmKatalogURICreation = mock(MMKatalogURICreation.class);
    MMSozietaetenURICreation mmSozietaetenURICreation = mock(MMSozietaetenURICreation.class);
    MMSignaturendokumenteURICreation mmSignaturendokumenteURICreation = mock(
        MMSignaturendokumenteURICreation.class);
    ManuscriptaMediaevaliaController controller = new ManuscriptaMediaevaliaController(
        mmBeschreibungURICreation, mmKatalogURICreation, mmSozietaetenURICreation,
        mmSignaturendokumenteURICreation);

    final String katalogResource = "hs//kataloge/HSK0496.htm";
    Response response = controller.handleRequestForMMBilderRedirect(katalogResource);

    assertNotNull(response);

    assertEquals(URI.create(
            ManuscriptaMediaevaliaController.HTTP_BILDER_MANUSCRIPTA_MEDIAEVALIA_DE + katalogResource),
        response.getLocation());
  }

  public static Stream<Arguments> getStaticURIMap() {

    return ManuscriptaMediaevaliaController.getStaticRedirects().entrySet().stream()
        .map((entry) -> Arguments.of(entry.getKey().toString(), entry.getValue()));
  }

  @Test
  void testfindProperDynamicStrategie() {
    MMBeschreibungURICreation mmBeschreibungURICreation = mock(
        MMBeschreibungURICreation.class);
    MMKatalogURICreation mmKatalogURICreation = mock(MMKatalogURICreation.class);
    MMSozietaetenURICreation mmSozietaetenURICreation = mock(MMSozietaetenURICreation.class);
    MMSignaturendokumenteURICreation mmSignaturendokumenteURICreation = mock(
        MMSignaturendokumenteURICreation.class);
    ManuscriptaMediaevaliaController controller = new ManuscriptaMediaevaliaController(
        mmBeschreibungURICreation, mmKatalogURICreation, mmSozietaetenURICreation,
        mmSignaturendokumenteURICreation);

    assertTrue(controller.findProperDynamicStrategie("obj31576567.html").isPresent());

    assertTrue(controller.findProperDynamicStrategie("dokumente/html/obj31581516").isPresent());

    assertTrue(controller.findProperDynamicStrategie("HSK0496.htm").isPresent());

    assertTrue(controller.findProperDynamicStrategie("hs//kataloge/HSK0496").isPresent());

    assertTrue(controller.findProperDynamicStrategie("/dokumente/html/obj90506982,T").isPresent());
  }
}
