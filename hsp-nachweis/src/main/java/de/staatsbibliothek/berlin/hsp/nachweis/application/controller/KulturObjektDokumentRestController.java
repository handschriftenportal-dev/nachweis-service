package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.tei.TeiSnapshotZipper;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller to implement a REST API for KulturObjektDokumente.
 */
@Path("/kulturobjektdokumente")
public class KulturObjektDokumentRestController {

  private static final Logger logger = LoggerFactory.getLogger(
      KulturObjektDokumentRestController.class);

  private final KulturObjektDokumentBoundary kulturObjektDokumentBoundary;

  @Inject
  public KulturObjektDokumentRestController(
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary) {
    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
  }

  @Path("/{id}/tei")
  @GET()
  @PermitAll
  @Produces(MediaType.APPLICATION_XML)
  public Response downloadKulturObjektDokumentTei(@PathParam("id") String id) {
    logger.info("REST API GET TEI download for KulturObjektDokument with id {} ", id);

    return kulturObjektDokumentBoundary.findById(id)
        .map(KulturObjektDokument::getTeiXML)
        .filter(StringUtils::isNotBlank)
        .map(tei -> Response.ok(tei)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + TeiSnapshotZipper.teiXmlFilename(id) + "\"")
            .build())
        .orElseGet(() -> Response.status(Status.NOT_FOUND).build());
  }
}
