package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.04.2021.
 * @version 1.0
 */
@RegisterRestClient(configKey = "normdaten-graphql-rest")
public interface NormdatenGraphQLPort {

  @POST
  @Retry(maxRetries = 4)
  @Timeout(10000)
  @Consumes("application/json")
  @Produces("application/json")
  GraphQlData findByQuery(GraphQlQuery query) throws InterruptedException;
}
