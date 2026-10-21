package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege.SchemaPflegeDatei;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collection;
import java.util.Collections;
import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 28.03.22
 */
@Path("/schemaresourcefile")
@RegisterRestClient(configKey = "schema-pflege-datei-service")
public interface SchemaPflegeDateiServicePort {

  @GET
  @Retry(maxRetries = 3, delay = 300)
  @Timeout(3000)
  @Path("/{id}")
  @Produces("application/json")
  SchemaPflegeDatei findById(@PathParam(value = "id") String id);

  @GET
  @Retry(maxRetries = 3, delay = 300)
  @Timeout(5000)
  @Fallback(SchemaResourceDateiFallbackHandler.class)
  @Produces("application/json")
  Collection<SchemaPflegeDatei> findAll();

  @PUT
  @Retry(maxRetries = 3, delay = 300)
  @Timeout(5000)
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Path("/{id}")
  Response upload(
      @PathParam("id") String id,
      SchemaPflegeDateiMultipartBody multipartBody);

  class SchemaResourceDateiFallbackHandler
      implements FallbackHandler<Collection<SchemaPflegeDatei>> {

    private static final Logger logger = LoggerFactory.getLogger(
        SchemaResourceDateiFallbackHandler.class);

    @Override
    public Collection<SchemaPflegeDatei> handle(ExecutionContext context) {
      logger.error("SchemaPflegeDateiServicePort.{} failed, returning empty list",
          context.getMethod().getName(), context.getFailure());
      return Collections.emptyList();
    }
  }
}
