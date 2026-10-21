package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 03.06.2022.
 * @version 1.0
 */

@RegisterRestClient(configKey = "dnb-culturegraph")
public interface DNBCulturgraphPort {

  @GET
  @Retry(maxRetries = 2)
  @Timeout(500)
  @Path("/{gndid}")
  @Produces("application/json")
  @Consumes("application/json")
  GNDEntityFact findByGNDID(@PathParam("gndid") String gndid);

}
