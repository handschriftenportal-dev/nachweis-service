package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.NachweisErfassungResponse;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import de.staatsbibliothek.berlin.javaee.authentication.repository.jwt.OAuth2JWTParser;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.11.2019.
 * @version 1.0
 */

class BeschreibungsRestControllerTest {

  private static final String BEARER_TOKEN = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJmaXJzdG5hbWUiOiJLb25yYWQiLCJ1c2VyX25hbWUiOiJiLWtlMTAxIiwicm9sZXMiOlsiQi1IU1BSRUQtQVBQIl0sImlzcyI6Imh0dHBzOi8vaW1kLnN0YWF0c2JpYmxpb3RoZWstYmVybGluLmRlIiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9CLUhTUFJFRC1BUFAiLCJST0xFX1JlZGFrdGV1ciIsIlJPTEVfQi1FREFETS1BUFAiXSwiY2xpZW50X2lkIjoiaHNwIiwibGFzdG5hbWUiOiJFaWNoc3RhZWR0IiwibmJmIjoxNzM0NDE0NTQzMDIyLCJwZXJtaXNzaW9ucyI6WyJCLUhTUEVSU0NIQ01OLUFQUCIsIkItSFNQTFJFRENNTi1BUFAiLCJCLUhTUFJFRC1BUFAiLCJCLUxSRURDTU4tQVBQIl0sInNjb3BlIjpbInN0YWF0c2JpYmxpb3RoZWtCZXJsaW4iLCJoYW5kc2NocmlmdGVucG9ydGFsIl0sImV4cCI6MTczNDQ1Nzc0MywianRpIjoiZ3VSc2Zld2hfZG1hN3FFVERxYlpkams4d2lVIiwiZW1haWwiOiJLb25yYWQuRWljaHN0YWVkdEBzYmIuc3BrLWJlcmxpbi5kZSJ9.pI95nObspafDwDB5B8M6CQkhRsqhQURfgWYerA4o0CmF9rzlQ_iwybHNZ7J8lWlfHeTTxRyH6SpKAdwNiF95grZwLEu5y7O-9hLm2-3wPoCSuoTMnykYo8nBnfXgdQSTZ0-iUyQzvSYKC6_T6VxlXlY2I4A3Kr41aR2dUuw_Ji8-jzu3_24IblqCOqW2fDyXcGkyBaxbwuaT9QI_zCjsgWbJzcbvYKOsC_Un_O3WoDJgVE1ruoNCnwCk6EjKJe9jVMAdApPfYqeF5Q43zXjy4OVH1uSZgTxeRUprHT-AJWMeJPkFTp3Lcd215HwbzYYGzan7uxa7wLKnP7cnE5ltcg";
  private static final Identifikation IDENTIFIKATION = new IdentifikationBuilder()
      .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
      .withIdent("1-1-1")
      .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
      .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
      .build();
  private static final Logger logger = LoggerFactory
      .getLogger(BeschreibungsRestControllerTest.class);
  private final BeschreibungsBoundary beschreibungsBoundary = mock(BeschreibungsBoundary.class);
  private final KulturObjektDokumentBoundary kulturObjektDokumentBoundary = mock(
      KulturObjektDokumentBoundary.class);
  private final DokumentSperreBoundary dokumentSperreBoundary = mock(DokumentSperreBoundary.class);
  private final OAuth2JWTParser jwtParser = new OAuth2JWTParser();

  @Test
  void getBeschreibungsdokumentTestWith404() {

    BeschreibungsRestController beschreibungsRestController = new BeschreibungsRestController(
        beschreibungsBoundary,
        null,
        null,
        null
    );

    Response response = beschreibungsRestController.getBeschreibungsdokument("123");

    verify(beschreibungsBoundary, times(1)).findById("123");

    assertEquals(404, response.getStatus());

  }

  @Test
  void getBeschreibungsdokumentSignaturenTestWith200() {

    BeschreibungsRestController beschreibungsRestController = new BeschreibungsRestController(
        beschreibungsBoundary,
        kulturObjektDokumentBoundary,
        dokumentSperreBoundary,
        jwtParser
    );

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Ms1")
        .withIdentTyp(IdentTyp.ALTSIGNATUR)
        .build();

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("1")
        .addAlternativeIdentifikation(identifikation)
        .build();

    Beschreibung beschreibungsdokument = new Beschreibung.BeschreibungsBuilder("2324", "1",
        IDENTIFIKATION)
        .withErstellungsDatum(LocalDateTime.of(2019, 11, 29, 0, 0, 0))
        .build();

    when(beschreibungsBoundary.findById("2324")).thenReturn(Optional.of(beschreibungsdokument));
    when(kulturObjektDokumentBoundary.findById("1")).thenReturn(Optional.of(kod));

    Response response = beschreibungsRestController.getKODSignaturenForBeschreibungen("2324");

    verify(beschreibungsBoundary, times(1)).findById("2324");

    assertEquals(200, response.getStatus());

    assertEquals(List.of("Ms1"), response.getEntity());
  }

  @Test
  void getBeschreibungsdokumentTestWith200() {

    BeschreibungsRestController beschreibungsRestController = new BeschreibungsRestController(
        beschreibungsBoundary,
        null,
        null,
        jwtParser
    );

    Beschreibung beschreibungsdokument = new Beschreibung.BeschreibungsBuilder("2324", "1",
        IDENTIFIKATION)
        .withErstellungsDatum(LocalDateTime.of(2019, 11, 29, 0, 0, 0))
        .build();

    when(beschreibungsBoundary.findById("2324")).thenReturn(Optional.of(beschreibungsdokument));

    Response response = beschreibungsRestController.getBeschreibungsdokument("2324");

    verify(beschreibungsBoundary, times(1)).findById("2324");

    assertEquals(200, response.getStatus());
  }

  @Test
  void test_GivenBeschreibung_WithContent() {

    BeschreibungsRestController beschreibungsRestController = new BeschreibungsRestController(
        beschreibungsBoundary,
        null,
        null,
        jwtParser
    );

    NormdatenReferenz besitzer = new NormdatenReferenz("", "Staatsbibliothek zu Berlin", "");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .build();

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withId("1212")
        .withTitel("Flavius Josephus, de bello Judaico")
        .build();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("40170459", "1234",
        identifikation)
        .addBeschreibungsKomponente(kopf)
        .build();

    when(beschreibungsBoundary.findById("40170459")).thenReturn(Optional.of(beschreibung));

    Response response = beschreibungsRestController.getBeschreibungsdokument("40170459");

    verify(beschreibungsBoundary, times(1)).findById("40170459");

    assertEquals(200, response.getStatus());

    logger.info("Getting response from REST Interface: {} ", response.getEntity());

  }

  @ParameterizedTest
  @ValueSource(strings = {"de", "en"})
  void testupdateBeschreibungWithTEI(String language) {

    BeschreibungsRestController beschreibungsRestController = new BeschreibungsRestController(
        beschreibungsBoundary,
        null,
        null,
        jwtParser
    );

    try (Response response = beschreibungsRestController.updateBeschreibungWithTEI("1", "xml",
        language, BEARER_TOKEN)) {

      assertEquals(200, response.getStatus());
      assertTrue(((NachweisErfassungResponse) response.getEntity()).isSuccess());
      assertEquals("info", ((NachweisErfassungResponse) response.getEntity()).getLevel());
    }
  }

  @Test
  void testValidateXML() {
    BeschreibungsRestController beschreibungsRestController = new BeschreibungsRestController(
        beschreibungsBoundary,
        null,
        null,
        jwtParser
    );
    ValidationResult validationResult = new ValidationResult(true, "", "", "Is valid");

    when(beschreibungsBoundary.validateXML("", "TEI", "de")).thenReturn(validationResult);

    try (Response response = beschreibungsRestController.validateBeschreibungTEI("", "TEI", "de")) {
      assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
      assertEquals("ValidationResult(isValid=true, line=, column=, message=Is valid, details=[])",
          ((NachweisErfassungResponse) response.getEntity()).getContent().toString());
    }
  }

  @Test
  void downloadBeschreibungTeiReturns200WithContentDisposition() {
    BeschreibungsRestController beschreibungsRestController = new BeschreibungsRestController(
        beschreibungsBoundary, null, null, null);

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("2324", "1", IDENTIFIKATION)
        .withTEIXml("<TEI>content</TEI>")
        .build();

    when(beschreibungsBoundary.findById("2324")).thenReturn(Optional.of(beschreibung));

    try (Response response = beschreibungsRestController.downloadBeschreibungTei("2324")) {
      assertEquals(200, response.getStatus());
      assertEquals("<TEI>content</TEI>", response.getEntity());
      assertEquals("attachment; filename=\"tei-2324.xml\"",
          response.getHeaderString("Content-Disposition"));
    }
  }

  @Test
  void downloadBeschreibungTeiReturns404WhenNotFound() {
    BeschreibungsRestController beschreibungsRestController = new BeschreibungsRestController(
        beschreibungsBoundary, null, null, null);

    when(beschreibungsBoundary.findById("404")).thenReturn(Optional.empty());

    try (Response response = beschreibungsRestController.downloadBeschreibungTei("404")) {
      assertEquals(404, response.getStatus());
    }
  }

  @Test
  void downloadBeschreibungTeiReturns404WhenTeiBlank() {
    BeschreibungsRestController beschreibungsRestController = new BeschreibungsRestController(
        beschreibungsBoundary, null, null, null);

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("2324", "1", IDENTIFIKATION)
        .withTEIXml("   ")
        .build();

    when(beschreibungsBoundary.findById("2324")).thenReturn(Optional.of(beschreibung));

    try (Response response = beschreibungsRestController.downloadBeschreibungTei("2324")) {
      assertEquals(404, response.getStatus());
    }
  }

  @Test
  void downloadBeschreibungTeiSnapshotReturns200WithContentDisposition() {
    BeschreibungsRestController beschreibungsRestController = new BeschreibungsRestController(
        beschreibungsBoundary, null, null, null);

    byte[] zip = {1, 2, 3};
    when(beschreibungsBoundary.findTeiSnapshotZip("2324")).thenReturn(Optional.of(zip));

    try (Response response = beschreibungsRestController.downloadBeschreibungTeiSnapshot("2324")) {
      assertEquals(200, response.getStatus());
      assertEquals(zip, response.getEntity());
      assertEquals("attachment; filename=\"tei-2324.zip\"",
          response.getHeaderString("Content-Disposition"));
    }
  }

  @Test
  void downloadBeschreibungTeiSnapshotReturns404WhenNoSnapshot() {
    BeschreibungsRestController beschreibungsRestController = new BeschreibungsRestController(
        beschreibungsBoundary, null, null, null);

    when(beschreibungsBoundary.findTeiSnapshotZip("2324")).thenReturn(Optional.empty());

    try (Response response = beschreibungsRestController.downloadBeschreibungTeiSnapshot("2324")) {
      assertEquals(404, response.getStatus());
    }
  }

  @Test
  void testGetSperrenForBeschreibung() throws DokumentSperreException {

    BeschreibungsRestController beschreibungsRestController = new BeschreibungsRestController(
        null,
        null,
        dokumentSperreBoundary,
        jwtParser
    );
    assertEquals(0,
        ((List<Sperre>) beschreibungsRestController.getSperrenForBeschreibungen("1")
            .getEntity()).size());

    Sperre sperre = new Sperre();
    when(dokumentSperreBoundary.findBySperreEintraege(
        new SperreEintrag("1", SperreDokumentTyp.BESCHREIBUNG))).thenReturn(List.of(sperre));

    assertEquals(Response.Status.OK.getStatusCode(),
        beschreibungsRestController.getSperrenForBeschreibungen("1").getStatus());

    assertEquals(1,
        ((List<Sperre>) beschreibungsRestController.getSperrenForBeschreibungen("1")
            .getEntity()).size());
  }
}
