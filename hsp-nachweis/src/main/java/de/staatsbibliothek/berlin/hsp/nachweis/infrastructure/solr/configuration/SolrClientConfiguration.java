package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.configuration;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "solr")
public interface SolrClientConfiguration {

  @WithName("url")
  Url url();

  @WithName("core")
  Core core();

  @WithName("connectionTimeout")
  @WithDefault("10000")
  int connectionTimeout();

  @WithName("requestTimeout")
  @WithDefault("90000")
  int requestTimeout();

  interface Url {

    @WithName("leader")
    @WithDefault("http://localhost/solr")
    String leader();

    @WithName("follower")
    @WithDefault("http://localhost/solr")
    String follower();
  }

  interface Core {

    @WithName("nachweis")
    @WithDefault("hsp-nachweis")
    String nachweis();

    @WithName("praesentation")
    @WithDefault("hsp")
    String praesentation();
  }
}
