package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Verifies the HTTP wiring of the TEI download endpoint: routing under {@code /rest}, reachability
 * without an authentication token ({@code @PermitAll}) and the {@code Content-Disposition} header
 * on the wire. The branch logic itself is covered by {@link BeschreibungsRestControllerTest}.
 */
@QuarkusTest
@Execution(ExecutionMode.SAME_THREAD)
class BeschreibungsRestControllerEndpointTest {

  private static final Identifikation IDENTIFIKATION = new IdentifikationBuilder()
      .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
      .withIdent("1-1-1")
      .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
      .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
      .build();

  @InjectMock
  BeschreibungsBoundary beschreibungsBoundary;

  @Test
  void teiDownloadIsRoutedAndReturnsAttachment() {
    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("2324", "1", IDENTIFIKATION)
        .withTEIXml("<TEI>content</TEI>")
        .build();

    when(beschreibungsBoundary.findById("2324")).thenReturn(Optional.of(beschreibung));

    given()
        .when().get("/rest/beschreibungen/2324/tei")
        .then()
        .statusCode(200)
        .header("Content-Disposition", "attachment; filename=\"tei-2324.xml\"")
        .body(equalTo("<TEI>content</TEI>"));
  }

  @Test
  void teiDownloadReturns404WhenUnknown() {
    when(beschreibungsBoundary.findById("missing")).thenReturn(Optional.empty());

    given()
        .when().get("/rest/beschreibungen/missing/tei")
        .then()
        .statusCode(404);
  }

  @Test
  void teiSnapshotDownloadIsRoutedAndReturnsAttachment() {
    when(beschreibungsBoundary.findTeiSnapshotZip("2324")).thenReturn(
        Optional.of(new byte[]{1, 2, 3}));

    given()
        .when().get("/rest/beschreibungen/2324/tei-snapshot")
        .then()
        .statusCode(200)
        .header("Content-Disposition", "attachment; filename=\"tei-2324.zip\"");
  }

  @Test
  void teiSnapshotDownloadReturns404WhenNoSnapshot() {
    when(beschreibungsBoundary.findTeiSnapshotZip("missing")).thenReturn(Optional.empty());

    given()
        .when().get("/rest/beschreibungen/missing/tei-snapshot")
        .then()
        .statusCode(404);
  }
}
