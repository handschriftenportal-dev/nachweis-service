package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.normdatenerkennung;

import io.smallrye.graphql.client.typesafe.api.ErrorOr;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

@GraphQLClientApi(configKey = "normdaten-erkennung-graphql")
public interface NormdatenErkennungGraphQLClient {

  @Query("normdatenerkennung")
  @Retry(maxRetries = 3, delay = 500)
  @Timeout(10000)
  ErrorOr<NormdatenErkennungErgebnis> normdatenerkennung(
      @Name("anfrage") NormdatenErkennungAnfrage anfrage
  );

}
