package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.producer;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import jakarta.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CDI qualifiers to select one of the four produced {@link org.apache.solr.client.solrj.SolrClient}
 * instances:
 * <p>
 * {leader, follower} x {nachweis, praesentation}.
 */
public class SolrClientQualifiers {

  @Qualifier
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface LeaderNachweis {

  }

  @Qualifier
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface LeaderPraesentation {

  }

  @Qualifier
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface FollowerNachweis {

  }

  @Qualifier
  @Target({FIELD, PARAMETER, METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface FollowerPraesentation {

  }
}
