package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import jakarta.ws.rs.core.Response;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class KulturObjektDokumentRestControllerTest {

  private static final Identifikation IDENTIFIKATION = new Identifikation.IdentifikationBuilder()
      .withIdent("Ms1")
      .withIdentTyp(IdentTyp.ALTSIGNATUR)
      .build();

  private final KulturObjektDokumentBoundary kulturObjektDokumentBoundary = mock(
      KulturObjektDokumentBoundary.class);

  @Test
  void downloadKulturObjektDokumentTeiReturns200WithContentDisposition() {
    KulturObjektDokumentRestController controller = new KulturObjektDokumentRestController(
        kulturObjektDokumentBoundary);

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("kod-1")
        .addAlternativeIdentifikation(IDENTIFIKATION)
        .withTEIXml("<TEI>kod</TEI>")
        .build();

    when(kulturObjektDokumentBoundary.findById("kod-1")).thenReturn(Optional.of(kod));

    try (Response response = controller.downloadKulturObjektDokumentTei("kod-1")) {
      assertEquals(200, response.getStatus());
      assertEquals("<TEI>kod</TEI>", response.getEntity());
      assertEquals("attachment; filename=\"tei-kod-1.xml\"",
          response.getHeaderString("Content-Disposition"));
    }
  }

  @Test
  void downloadKulturObjektDokumentTeiReturns404WhenNotFound() {
    KulturObjektDokumentRestController controller = new KulturObjektDokumentRestController(
        kulturObjektDokumentBoundary);

    when(kulturObjektDokumentBoundary.findById("missing")).thenReturn(Optional.empty());

    try (Response response = controller.downloadKulturObjektDokumentTei("missing")) {
      assertEquals(404, response.getStatus());
    }
  }

  @Test
  void downloadKulturObjektDokumentTeiReturns404WhenTeiBlank() {
    KulturObjektDokumentRestController controller = new KulturObjektDokumentRestController(
        kulturObjektDokumentBoundary);

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("kod-1")
        .addAlternativeIdentifikation(IDENTIFIKATION)
        .build();

    when(kulturObjektDokumentBoundary.findById("kod-1")).thenReturn(Optional.of(kod));

    try (Response response = controller.downloadKulturObjektDokumentTei("kod-1")) {
      assertEquals(404, response.getStatus());
    }
  }
}