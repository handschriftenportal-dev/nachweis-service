package de.staatsbibliothek.berlin.oaipmh.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.staatsbibliothek.berlin.hsp.nachweis.TestUtils.configuration.OaiPmhTestConfig;
import io.gdcc.xoai.dataprovider.repository.RepositoryConfiguration;
import io.gdcc.xoai.model.oaipmh.DeletedRecord;
import io.gdcc.xoai.model.oaipmh.Granularity;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(OaiPmhTestConfig.class)
class ConfigMapperTest {

  @Inject Configuration configuration;

  @Test
  void testMap() {
    RepositoryConfiguration repoConfig = ConfigMapper.getOAIRepositoryConfiguration(configuration);
    assertEquals("Handschriftenportal", repoConfig.getRepositoryName());
    assertEquals("http://localhost:9298/rest/oai-pmh", repoConfig.getBaseUrl());
    assertEquals(
        List.of("admin@handschriftenportal.de", "admin2@handschriftenportal.de"),
        repoConfig.getAdminEmails());
    assertEquals(DeletedRecord.NO, repoConfig.getDeleteMethod());
    assertEquals(Granularity.Day, repoConfig.getGranularity());
    assertEquals(Instant.parse("2000-01-01T00:00:00Z"), repoConfig.getEarliestDate());
  }
}
