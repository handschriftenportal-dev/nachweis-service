package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary.SYSTEM_USERNAME;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.sendIndexJobEvent;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJob;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJobDocumentType;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexReplicationService;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.Arrays;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter(AccessLevel.PROTECTED)
@Path("/")
public class IndexRestController {

  private Event<IndexJob> indexJobEvent;
  private IndexReplicationService replicationService;

  @Inject
  public IndexRestController(
      @sendIndexJobEvent Event<IndexJob> indexJobEvent,
      IndexReplicationService replicationService) {
    this.indexJobEvent = indexJobEvent;
    this.replicationService = replicationService;
  }


  @POST
  @Path("index")
  @Produces(MediaType.APPLICATION_JSON)
  public Response indexing(
      @QueryParam("doctype") @DefaultValue("ALL") IndexJobDocumentType docType,
      @QueryParam("id") String id) {
    log.info("[index] Request for indexing with parameters id={}, doctype={}", id, docType);
    if (docType == null) {
      return generateInvalidDoctypeResponse();
    }

    if (replicationService.disableReplication()) {
      return generateReplicationFailureResponse();
    }
    IndexJob indexJob = new IndexJob(id, SYSTEM_USERNAME, docType);
    indexJobEvent.fireAsync(indexJob);
    return Response
        .status(Status.NO_CONTENT)
        .entity(indexJob)
        .build();
  }

  private Response generateReplicationFailureResponse() {
    return Response
        .status(Status.SERVICE_UNAVAILABLE)
        .entity("Replication could not be disabled. Try again later.")
        .build();
  }

  private Response generateInvalidDoctypeResponse() {
    return Response.status(Status.BAD_REQUEST)
        .entity(
            "Invalid doctype. Valid values = ["
                + Arrays.toString(IndexJobDocumentType.values())
                + "]")
        .build();
  }
}


