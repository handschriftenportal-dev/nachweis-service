package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Verifies the HTTP wiring of the TEI download endpoint: routing under {@code /rest}, reachability
 * without an authentication token ({@code @PermitAll}) and the {@code Content-Disposition} header
 * on the wire. The branch logic itself is covered by
 * {@link KulturObjektDokumentRestControllerTest}.
 */
@QuarkusTest
@Execution(ExecutionMode.SAME_THREAD)
class KulturObjektDokumentRestControllerEndpointTest {

  private static final Identifikation IDENTIFIKATION = new Identifikation.IdentifikationBuilder()
      .withIdent("Ms1")
      .withIdentTyp(IdentTyp.ALTSIGNATUR)
      .build();

  @InjectMock
  KulturObjektDokumentBoundary kulturObjektDokumentBoundary;

  @Test
  void teiDownloadIsRoutedAndReturnsAttachment() {
    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("kod-1")
        .addAlternativeIdentifikation(IDENTIFIKATION)
        .withTEIXml("<TEI>kod</TEI>")
        .build();

    when(kulturObjektDokumentBoundary.findById("kod-1")).thenReturn(Optional.of(kod));

    given()
        .when().get("/rest/kulturobjektdokumente/kod-1/tei")
        .then()
        .statusCode(200)
        .header("Content-Disposition", "attachment; filename=\"tei-kod-1.xml\"")
        .body(equalTo("<TEI>kod</TEI>"));
  }

  @Test
  void teiDownloadReturns404WhenUnknown() {
    when(kulturObjektDokumentBoundary.findById("missing")).thenReturn(Optional.empty());

    given()
        .when().get("/rest/kulturobjektdokumente/missing/tei")
        .then()
        .statusCode(404);
  }
}
