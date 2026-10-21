package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oaipmh;

import de.staatsbibliothek.berlin.oaipmh.config.Configuration;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;
import java.util.List;

@ConfigMapping(prefix = "oaipmh", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface OAIConfiguration extends Configuration {

  @WithName("adminEmail")
  List<String> adminEmails();

  @WithDefault("YYYY-MM-DD")
  String granularity();

  @WithDefault(value = "1000")
  Integer maxListIdentifiers();

  @WithDefault(value = "1000")
  Integer maxListRecords();

  @WithDefault(value = "1000")
  Integer maxListSets();

  @WithDefault("true")
  boolean enabled();
}
