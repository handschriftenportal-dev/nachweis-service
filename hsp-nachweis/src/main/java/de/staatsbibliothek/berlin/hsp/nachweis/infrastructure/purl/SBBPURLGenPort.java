package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.purl;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 02.08.22
 */

@RegisterRestClient(configKey = "purl-gen")
public interface SBBPURLGenPort {

  String PARAM_PID = "HSP";
  String PARAM_MVID = "NEW";

  @GET
  @Retry(maxRetries = 2)
  @Timeout(2000)
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
  SBBPURLGenResponse createPURL(
      @QueryParam("PID") String pid,
      @QueryParam("MVID") String mvid);

}
