package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 20.05.25.
 * @version 1.0
 */

@RegisterRestClient(configKey = "normdaten-gnd-service")
public interface NormdatenGNDServicePort {

  @GET
  @Retry(maxRetries = 2)
  @Timeout(500)
  @Path("/gndservice/sync")
  @Produces("application/json")
  @Consumes("application/json")
  GNDServiceSubset findGNDServiceSubset();

  @POST
  @Path("/gndservice/sync")
  @Consumes(MediaType.APPLICATION_JSON)
  void registerSynchronistation(
      NormdatenserviceSync normdatenserviceSync);

  @PUT
  @Path("/gndservice/sync")
  @Consumes(MediaType.APPLICATION_JSON)
  GNDServiceSubset updateSynchronistation(
      NormdatenserviceSync normdatenserviceSync);

  @DELETE
  @Path("/gndservice/sync")
  void unregisterSynchronistation();
}
