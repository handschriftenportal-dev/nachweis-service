package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 29.03.2022
 */

@Path("/tei-xml")
@RegisterRestClient(configKey = "tei-validation-service")
public interface TeiXmlValidationServicePort {

  String HEADER_PARAM_SCHEMA = "de.staatsbibliothek.berlin.hsp.schema";
  String SCHEMA_TEI_ALL = "ALL";
  String SCHEMA_TEI_ODD = "ODD";

  @POST
  @Retry(maxRetries = 2)
  @Timeout(60000)
  @Consumes(MediaType.APPLICATION_XML)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/validate")
  ValidationResult validateTeiXml(String teiXml,
      @HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) String locale,
      @HeaderParam(HEADER_PARAM_SCHEMA) String schema);

}
