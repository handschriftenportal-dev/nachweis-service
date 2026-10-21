package de.staatsbibliothek.berlin.oaipmh.config;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Configuration {

  String repositoryName();

  String baseUrl();

  Optional<String> protocolVersion();

  List<String> adminEmails();

  Optional<String> earliestDatestamp();

  String deletedRecord();

  String granularity();

  Integer maxListIdentifiers();

  Integer maxListRecords();

  Integer maxListSets();

  Map<String, MetadataFormatConfig> metadataFormats();

  interface MetadataFormatConfig {

    String prefix();

    String namespace();

    Optional<String> schemaResourceId();

    Optional<List<String>> additionalResourceIds();
  }
}
