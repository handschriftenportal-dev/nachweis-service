package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import io.quarkus.oidc.client.filter.OidcClientFilter;
import io.smallrye.graphql.client.typesafe.api.ErrorOr;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;

@GraphQLClientApi(configKey = "normdaten-mutation-graphql")
@OidcClientFilter("normdaten-service")
public interface NormdatenGraphQLMutationClient {

  @Mutation("create")
  @Retry(maxRetries = 3, delay = 500)
  @Timeout(10000)
  ErrorOr<GNDEntityFact> create(@Name("gndEntityFact") GNDEntityFactGraphQLInput gndEntityFact);

}
