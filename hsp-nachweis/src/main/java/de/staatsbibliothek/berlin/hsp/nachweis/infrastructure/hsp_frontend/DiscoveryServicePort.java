package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.hsp_frontend;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 24.04.23.
 * @version 1.0
 */

@Path("/api")
@RegisterRestClient(configKey = "discovery-service")
public interface DiscoveryServicePort {

  @GET
  @Retry(maxRetries = 2)
  @Timeout(1000)
  @Path("/info")
  @Produces("application/json")
  DiscoveryServiceInfoDTO findInfo();

  @GET
  @Retry(maxRetries = 2)
  @Timeout(1000)
  @Path("/stats")
  @Produces("application/json")
  DiscoveryServiceStatsDTO findStats();

}
