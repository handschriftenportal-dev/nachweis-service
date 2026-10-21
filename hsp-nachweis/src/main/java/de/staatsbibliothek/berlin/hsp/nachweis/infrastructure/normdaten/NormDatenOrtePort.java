package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 06.06.2019.
 */

@Path("/place")
@RegisterRestClient(configKey = "normdaten-orte")
public interface NormDatenOrtePort {

  @POST
  @Retry(maxRetries = 2)
  @Timeout(10000)
  @Consumes("application/json")
  @Produces("application/json")
  GNDEntityFact createPlaceWithGNDEnityFact(GNDEntityFact gndEntityFact);

}
