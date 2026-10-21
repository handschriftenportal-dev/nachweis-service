package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 10.06.2020.
 * @version 1.0
 */
@Path("/import/job")
@RegisterRestClient(configKey = "import-service")
public interface ImportServicePort {

  @GET
  @Retry(maxRetries = 2)
  @Timeout(1000)
  @Path("/{id}")
  @Produces("application/json")
  ImportVorgang findById(@PathParam(value = "id") String id);

  @GET
  @Retry(maxRetries = 2)
  @Timeout(1000)
  @Fallback(ImportFallbackHandler.class)
  @Produces("application/json")
  Collection<ImportVorgang> findAll();


  @DELETE
  @Retry(maxRetries = 2)
  @Timeout(1000)
  void deleteByIDs(List<String> ids);

  class ImportFallbackHandler implements FallbackHandler<Collection<ImportVorgang>> {

    @Override
    public Collection<ImportVorgang> handle(ExecutionContext context) {
      return Collections.emptyList();
    }
  }
}
