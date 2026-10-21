package de.staatsbibliothek.berlin.oaipmh.config;

import de.staatsbibliothek.berlin.oaipmh.config.converter.DeletedRecordConverter;
import de.staatsbibliothek.berlin.oaipmh.config.converter.EarliestDatestampConverter;
import de.staatsbibliothek.berlin.oaipmh.config.converter.GranularityConverter;
import io.gdcc.xoai.dataprovider.repository.RepositoryConfiguration;
import java.time.Instant;

public class ConfigMapper {

  private ConfigMapper() {
  }

  public static RepositoryConfiguration getOAIRepositoryConfiguration(Configuration configuration) {
    RepositoryConfiguration.RepositoryConfigurationBuilder builder =
        new RepositoryConfiguration.RepositoryConfigurationBuilder()
            .withRepositoryName(configuration.repositoryName())
            .withBaseUrl(configuration.baseUrl())
            .setAdminEmails(configuration.adminEmails())
            .withDeleteMethod(DeletedRecordConverter.convert(configuration.deletedRecord()))
            .withGranularity(GranularityConverter.convert(configuration.granularity()))
            .withEarliestDate(
                EarliestDatestampConverter.convert(
                    configuration.earliestDatestamp().orElse(Instant.ofEpochSecond(0).toString())))
            .withMaxListRecords(configuration.maxListRecords())
            .withMaxListIdentifiers(configuration.maxListIdentifiers())
            .withMaxListSets(configuration.maxListSets());

    return builder.build();
  }
}
