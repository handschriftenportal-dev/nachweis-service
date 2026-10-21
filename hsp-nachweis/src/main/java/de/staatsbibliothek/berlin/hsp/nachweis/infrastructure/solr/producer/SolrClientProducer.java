package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.producer;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrAdminClient;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.configuration.SolrClientConfiguration;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.producer.SolrClientQualifiers.FollowerNachweis;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.producer.SolrClientQualifiers.FollowerPraesentation;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.producer.SolrClientQualifiers.LeaderNachweis;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.producer.SolrClientQualifiers.LeaderPraesentation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.concurrent.TimeUnit;
import org.apache.solr.client.solrj.impl.Http2SolrClient;

@ApplicationScoped
public class SolrClientProducer {

  private final SolrClientConfiguration solrClientConfig;

  @Inject
  public SolrClientProducer(SolrClientConfiguration solrClientConfig) {
    this.solrClientConfig = solrClientConfig;
  }

  @Produces
  @ApplicationScoped
  @LeaderNachweis
  public SolrAdminClient leaderNachweisClient() {
    return createSolrAdminClient(solrClientConfig.url().leader(),
        solrClientConfig.core().nachweis());
  }

  @Produces
  @ApplicationScoped
  @FollowerNachweis
  public SolrAdminClient followerNachweisClient() {
    return createSolrAdminClient(solrClientConfig.url().follower(),
        solrClientConfig.core().nachweis());
  }

  @Produces
  @ApplicationScoped
  @LeaderPraesentation
  public SolrAdminClient leaderPraesentationClient() {
    return createSolrAdminClient(solrClientConfig.url().leader(),
        solrClientConfig.core().praesentation());
  }

  @Produces
  @ApplicationScoped
  @FollowerPraesentation
  public SolrAdminClient followerPraesentationClient() {
    return createSolrAdminClient(solrClientConfig.url().follower(),
        solrClientConfig.core().praesentation());
  }

  // Singleton instead of ApplicationScoped: Http2SolrClient has no no-args constructor,
  // so ArC cannot create the client proxy required for normal-scoped beans
  @Produces
  @Singleton
  @LeaderNachweis
  public Http2SolrClient leaderHttp2Client() {
    return createHttp2Client(solrClientConfig.url().leader());
  }

  @Produces
  @Singleton
  @FollowerNachweis
  public Http2SolrClient followerHttp2Client() {
    return createHttp2Client(solrClientConfig.url().follower());
  }

  private Http2SolrClient createHttp2Client(String baseUrl) {
    return new Http2SolrClient.Builder(baseUrl)
        .withConnectionTimeout(solrClientConfig.connectionTimeout(), TimeUnit.MILLISECONDS)
        .withRequestTimeout(solrClientConfig.requestTimeout(), TimeUnit.MILLISECONDS)
        .build();
  }

  private SolrAdminClient createSolrAdminClient(String baseUrl, String core) {
    return new SolrAdminClient(createHttp2Client(baseUrl), core);
  }

}
