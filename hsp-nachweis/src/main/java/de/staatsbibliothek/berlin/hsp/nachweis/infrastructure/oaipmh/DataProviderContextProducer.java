package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oaipmh;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege.SchemaPflegeDatei;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege.SchemaPflegeDateiBoundary;
import de.staatsbibliothek.berlin.oaipmh.config.Configuration;
import de.staatsbibliothek.berlin.oaipmh.config.Configuration.MetadataFormatConfig;
import de.staatsbibliothek.berlin.oaipmh.dataprovider.DataProviderContext;
import de.staatsbibliothek.berlin.oaipmh.dataprovider.DataProviderContext.DataProviderContextBuilder;
import de.staatsbibliothek.berlin.oaipmh.transformer.TemplateCompilationException;
import de.staatsbibliothek.berlin.oaipmh.transformer.TemplatesBuilder;
import de.staatsbibliothek.berlin.oaipmh.transformer.TemplatesBuilder.TransformerBuilder;
import io.gdcc.xoai.dataprovider.model.MetadataFormat;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class DataProviderContextProducer {

  private final SchemaPflegeDateiBoundary schemaPflegeDateiService;
  private final Configuration configuration;

  private Map<String, TransformerBuilder> cachedTemplates;

  @Inject
  public DataProviderContextProducer(
      SchemaPflegeDateiBoundary schemaPflegeDateiService,
      Configuration configuration) {
    this.schemaPflegeDateiService = schemaPflegeDateiService;
    this.configuration = configuration;
  }

  @Produces
  @RequestScoped
  public DataProviderContext createDataProviderContext() {
    Map<String, TransformerBuilder> templates = getOrLoadTemplates();
    var dataProviderContextBuilder = new DataProviderContextBuilder();

    for (var entry : configuration.metadataFormats().entrySet()) {
      createMetadataFormat(entry.getKey(), entry.getValue(), templates)
          .ifPresent(dataProviderContextBuilder::addMetadataFormat);
    }
    return dataProviderContextBuilder.build();
  }

  private Optional<MetadataFormat> createMetadataFormat(
      String key, MetadataFormatConfig config, Map<String, TransformerBuilder> templates) {

    boolean needsTransformer = config.schemaResourceId().isPresent();
    TransformerBuilder cachedTemplate = templates.get(key);

    if (needsTransformer && cachedTemplate == null) {
      log.warn("Skipping format '{}' — templates not available", key);
      return Optional.empty();
    }

    MetadataFormat format = new MetadataFormat()
        .withPrefix(config.prefix())
        .withNamespace(config.namespace());

    if (cachedTemplate != null) {
      try {
        format.withTransformer(cachedTemplate.build());
      } catch (TemplateCompilationException e) {
        log.warn("Failed to create transformer for format '{}': {}", key, e.getMessage(), e);
        return Optional.empty();
      }
    }

    return Optional.of(format);
  }

  synchronized Map<String, TransformerBuilder> getOrLoadTemplates() {
    if (cachedTemplates != null) {
      return cachedTemplates;
    }
    log.info("Loading and compiling transformation templates...");
    Map<String, TransformerBuilder> templates = new HashMap<>();
    boolean anyFailed = false;

    for (var entry : configuration.metadataFormats().entrySet()) {
      MetadataFormatConfig config = entry.getValue();
      if (config.schemaResourceId().isPresent()) {
        try {
          Optional<TransformerBuilder> builder = buildTransformer(config);
          if (builder.isPresent()) {
            templates.put(entry.getKey(), builder.get());
          } else {
            log.warn("Skipping format '{}' — schema resource not found", entry.getKey());
            anyFailed = true;
          }
        } catch (TemplateCompilationException e) {
          log.warn("Failed to compile templates for format '{}': {}",
              entry.getKey(), e.getMessage(), e);
          anyFailed = true;
        } catch (RuntimeException e) {
          log.warn("Unexpected error loading templates for format '{}': {}",
              entry.getKey(), e.getMessage(), e);
          anyFailed = true;
        }
      }
    }

    if (anyFailed) {
      log.warn("Some transformation templates failed to compile — will retry on next request");
    } else {
      cachedTemplates = Map.copyOf(templates);
      log.info("Transformation templates compiled successfully");
    }
    return templates;
  }

  private Optional<TransformerBuilder> buildTransformer(MetadataFormatConfig config) {
    Optional<String> xsl = config.schemaResourceId().flatMap(this::fetchResource);
    if (xsl.isEmpty()) {
      return Optional.empty();
    }

    Map<String, String> additionalResources = new HashMap<>();
    for (String id : config.additionalResourceIds().orElse(List.of())) {
      Optional<String> resource = fetchResource(id);
      if (resource.isEmpty()) {
        return Optional.empty();
      }
      additionalResources.put(id, resource.get());
    }

    return Optional.of(new TemplatesBuilder(xsl.get(), additionalResources).compile());
  }

  private Optional<String> fetchResource(String id) {
    return schemaPflegeDateiService.findById(id)
        .map(SchemaPflegeDatei::getDatei);
  }

}
