package de.staatsbibliothek.berlin.oaipmh.rest;

import de.staatsbibliothek.berlin.oaipmh.dataprovider.DataProvider;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface RestBoundary<G extends OaiPmhRequestBase, P extends OaiPmhRequestBase> {

  Logger log = LoggerFactory.getLogger(RestBoundary.class);

  Response doGet(G request);

  Response doPost(P request);

  default Response handleRequest(Map<String, String[]> params, DataProvider dataProvider) {
    try {
      final String response = dataProvider.handleRequest(params);
      return Response.ok(response).build();
    } catch (XMLStreamException e) {
      log.error("Error while processing OAI-PMH request", e);
      return Response.serverError().entity(e.getMessage()).build();
    } catch (Exception e) {
      log.error("Unexpected error while processing OAI-PMH request", e);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity("OAI-PMH service temporarily unavailable")
          .build();
    }
  }
}
