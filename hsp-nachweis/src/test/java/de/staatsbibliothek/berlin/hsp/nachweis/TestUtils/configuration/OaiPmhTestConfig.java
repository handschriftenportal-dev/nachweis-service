package de.staatsbibliothek.berlin.hsp.nachweis.TestUtils.configuration;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class OaiPmhTestConfig implements QuarkusTestProfile {

  @Override
  public Map<String, String> getConfigOverrides() {
    return Map.ofEntries(
        Map.entry("oaipmh.protocolVersion", "2.0"),
        Map.entry("oaipmh.deletedRecord", "no"),
        Map.entry("oaipmh.baseUrl", "http://localhost:9298/rest/oai-pmh"),
        Map.entry("oaipmh.repositoryName", "Handschriftenportal"),
        Map.entry("oaipmh.adminEmail[0]", "admin@handschriftenportal.de"),
        Map.entry("oaipmh.adminEmail[1]", "admin2@handschriftenportal.de"),
        Map.entry("oaipmh.earliestDatestamp", "2000-01-01T00:00:00Z"),
        Map.entry("oaipmh.granularity", "day"),
        Map.entry("oaipmh.metadataFormats.tei.prefix", "oai_tei"),
        Map.entry("oaipmh.metadataFormats.tei.namespace", "http://www.tei-c.org/ns/1.0"),
        Map.entry("oaipmh.metadataFormats.dc.prefix", "oai_dc"),
        Map.entry("oaipmh.metadataFormats.dc.namespace", "http://purl.org/dc/elements/1.1/"),
        Map.entry("oaipmh.metadataFormats.dc.schemaResourceId", "tei2dc.xsl"),
        Map.entry("oaipmh.metadataFormats.marc21.prefix", "oai_marc21"),
        Map.entry("oaipmh.metadataFormats.marc21.namespace", "http://www.loc.gov/MARC21/slim"),
        Map.entry("oaipmh.metadataFormats.marc21.schemaResourceId", "tei2marc.xsl"),
        Map.entry("oaipmh.metadataFormats.marc21.additionalResourceIds[0]",
            "MarcCodes_Laender_Sprachen.xml"));
  }
}
