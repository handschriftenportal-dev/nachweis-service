package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.initium;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.initium.InitiumFilter;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 28.11.2025
 */
@GraphQLClientApi(configKey = "initium-graphql")
public interface InitiumGraphQLClient {

  @Query("findInitia")
  @Retry(maxRetries = 3, delay = 500)
  @Timeout(10000)
  InitiumPageResult findInitia(
      @Name("limit") Integer limit,
      @Name("pageNumber") Integer pageNumber,
      @Name("filter") InitiumFilter filter
  );
}
