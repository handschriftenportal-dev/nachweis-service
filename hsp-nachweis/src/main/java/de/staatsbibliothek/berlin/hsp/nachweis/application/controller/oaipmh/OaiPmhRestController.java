package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.oaipmh;

import de.staatsbibliothek.berlin.oaipmh.dataprovider.DataProvider;
import de.staatsbibliothek.berlin.oaipmh.rest.RestBoundary;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/oai-pmh")
public class OaiPmhRestController implements RestBoundary<OaiPmhGetRequest, OaiPmhPostRequest> {

  private final DataProvider dataProvider;

  private final Logger log = LoggerFactory.getLogger(OaiPmhRestController.class);

  private final boolean isEnabled;

  @Inject
  public OaiPmhRestController(
      DataProvider dataProvider,
      @ConfigProperty(name = "oaipmh.enabled", defaultValue = "true") boolean isEnabled) {
    this.dataProvider = dataProvider;
    this.isEnabled = isEnabled;
  }

  @GET
  @Produces("text/xml")
  @Operation(
      description =
          """
              Encoding an OAI-PMH request in a URL for an HTTP GET
              URLs for GET requests have keyword arguments appended to the base URL, \
              separated from it by a question mark [?]. For example, the URL of a \
              GetRecord request to a repository with base URL that is \
              https://an.oa.org/OAI-script might be:
              https://an.oa.org/OAI-script?verb=GetRecord&identifier=oai:arXiv.org:hep-th/9901001&metadataPrefix=oai_dc
              However, since special characters in URIs must be encoded, the correct \
              form of the above GET request URL is:
              https://an.oa.org/OAI-script?verb=GetRecord&identifier=oai%3AarXiv.org%3Ahep-th%2F9901001&metadataPrefix=oai_dc\
              """)
  @Override
  public Response doGet(@BeanParam OaiPmhGetRequest request) {
    log.info("[OAI-PMH] Received GET request");
    if (isEnabled) {
      return handleRequest(request.toMap(), dataProvider);
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }

  @POST
  @Produces("text/xml")
  @Consumes("application/x-www-form-urlencoded")
  @Operation(
      description =
          """
              Encoding an OAI-PMH request in an HTTP POST
              Keyword arguments are carried in the message body of the HTTP POST. The Content-Type of
              the request must be application/x-www-form-urlencoded. For example, submitting the same
              request as above using the POST method would use just the base URL as the URL, with the format of the POST being:
              
              ```
              POST https://an.oa.org/OAI-script HTTP/1.0
              Content-Length: 82
              Content-Type: application/x-www-form-urlencoded
              
              verb=GetRecord&identifier=oai%3AarXiv.org%3Ahep-th%2F9901001&metadataPrefix=oai_dc
              ```
              """)
  @Override
  public Response doPost(@BeanParam OaiPmhPostRequest request) {
    log.info("[OAI-PMH] Received POST request");
    if (isEnabled) {
      return handleRequest(request.toMap(), dataProvider);
    }
    return Response.status(Response.Status.NOT_FOUND).build();
  }
}
