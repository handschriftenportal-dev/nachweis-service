package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.person;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.person.PersonFilter;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

/**
 * @author Munhal.Mammadov@sbb.spk-berlin.de
 * @since 24.03.2026
 */
@GraphQLClientApi(configKey = "person-graphql")
public interface PersonGraphQLClient {

  @Query("findPersons")
  @Retry(maxRetries = 3, delay = 500)
  @Timeout(10000)
  PersonPageResult findPersons(
      @Name("limit") Integer limit,
      @Name("pageNumber") Integer pageNumber,
      @Name("filter") PersonFilter filter
  );
}
