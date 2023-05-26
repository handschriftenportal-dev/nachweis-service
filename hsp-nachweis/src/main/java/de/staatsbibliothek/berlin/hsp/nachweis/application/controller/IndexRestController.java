/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.sendIndexJobEvent;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJob;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJobStatus;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJobStatusBoundary;
import java.util.Optional;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/indexkods")
public class IndexRestController {

  Event<IndexJob> indexJobEvent;

  private static final Logger logger = LoggerFactory.getLogger(IndexRestController.class);

  IndexJobStatusBoundary indexJobStatusBoundry;

  KulturObjektDokumentBoundary kulturObjektDokumentBoundary;

  @Inject
  public void setIndexJobEvent(@sendIndexJobEvent Event<IndexJob> indexJobEvent) {
    this.indexJobEvent = indexJobEvent;
  }

  @Inject
  public void setKulturObjektDokumentBoundary(KulturObjektDokumentBoundary kulturObjektDokumentBoundary) {
    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
  }

  @Inject
  public void setIndexJobStatusBoundry(IndexJobStatusBoundary indexJobStatusBoundry) {
    this.indexJobStatusBoundry = indexJobStatusBoundry;
  }

  @GET()
  @Path("/status/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response indexJobStatus(@PathParam("id") String id) {

    logger.info("REST API indexkods Request for IndexJob Status with id {} ", id);

    try {
      Optional<IndexJobStatus> indexJobStatusOptional = indexJobStatusBoundry.findById(Long.parseLong(id));

      return indexJobStatusOptional
          .map(indexJobStatus -> Response.status(Status.OK).entity(indexJobStatus).build())
          .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).entity("Entity not found for indexJobStatus: " + id).build());
    } catch (Exception exception) {
      return Response.serverError().entity(exception.getMessage()).build();
    }
  }

  @POST()
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response indexingKod(@PathParam("id") String id) {
    logger.info("REST API indexkods Request for KOD with id {} ", id);
    IndexJob indexJob = new IndexJob(indexJobStatusBoundry, id, false);
    return processAsyncIndexJobEvent(indexJob);
  }

  @POST()
  @Produces({MediaType.APPLICATION_JSON})
  public Response indexingKods() {
    logger.info("REST API indexkods Request for indexing KODs");
    IndexJob indexJob = new IndexJob(indexJobStatusBoundry);
    return processAsyncIndexJobEvent(indexJob);
  }

  private Response processAsyncIndexJobEvent(IndexJob indexJob) {
    Response response;
    try {
      logger.info("fireAsync");
      indexJobEvent.fireAsync(indexJob);
      response = Response.status(Status.CREATED).entity(indexJob).build();
    } catch (
        IllegalArgumentException illegalArgumentException) {
      logger.error(illegalArgumentException.getMessage());
      response = Response.status(Status.INTERNAL_SERVER_ERROR).entity("illegalArgumentException").build();
    }
    return response;
  }

}
