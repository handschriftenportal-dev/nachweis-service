package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oaipmh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege.SchemaPflegeDatei;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege.SchemaPflegeDateiBoundary;
import de.staatsbibliothek.berlin.oaipmh.config.Configuration;
import de.staatsbibliothek.berlin.oaipmh.config.Configuration.MetadataFormatConfig;
import de.staatsbibliothek.berlin.oaipmh.dataprovider.DataProviderContext;
import io.gdcc.xoai.dataprovider.model.MetadataFormat;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataProviderContextProducerTest {

  private SchemaPflegeDateiBoundary servicePort;
  private Configuration configuration;
  private DataProviderContextProducer producer;

  @BeforeEach
  void setUp() throws Exception {
    servicePort = mock(SchemaPflegeDateiBoundary.class);
    configuration = mock(Configuration.class);

    MetadataFormatConfig teiConfig = mock(MetadataFormatConfig.class);
    when(teiConfig.prefix()).thenReturn("oai_tei");
    when(teiConfig.namespace()).thenReturn("http://www.tei-c.org/ns/1.0");
    when(teiConfig.schemaResourceId()).thenReturn(Optional.empty());
    when(teiConfig.additionalResourceIds()).thenReturn(Optional.empty());

    MetadataFormatConfig dcConfig = mock(MetadataFormatConfig.class);
    when(dcConfig.prefix()).thenReturn("oai_dc");
    when(dcConfig.namespace()).thenReturn("http://purl.org/dc/elements/1.1/");
    when(dcConfig.schemaResourceId()).thenReturn(Optional.of("tei2dc.xsl"));
    when(dcConfig.additionalResourceIds()).thenReturn(Optional.empty());

    MetadataFormatConfig marc21Config = mock(MetadataFormatConfig.class);
    when(marc21Config.prefix()).thenReturn("oai_marc21");
    when(marc21Config.namespace()).thenReturn("http://www.loc.gov/MARC21/slim");
    when(marc21Config.schemaResourceId()).thenReturn(Optional.of("tei2marc.xsl"));
    when(marc21Config.additionalResourceIds())
        .thenReturn(Optional.of(List.of("MarcCodes_Laender_Sprachen.xml")));

    Map<String, MetadataFormatConfig> formats = new LinkedHashMap<>();
    formats.put("tei", teiConfig);
    formats.put("dc", dcConfig);
    formats.put("marc21", marc21Config);
    when(configuration.metadataFormats()).thenReturn(formats);

    String dcXsl = readResource("oai-pmh/tei2dc.xsl");
    String marcXsl = readResource("oai-pmh/tei2marc.xsl");
    String marcCodes = readResource("oai-pmh/MarcCodes_Laender_Sprachen.xml");

    when(servicePort.findById("tei2dc.xsl"))
        .thenReturn(Optional.of(SchemaPflegeDatei.builder().datei(dcXsl).build()));
    when(servicePort.findById("tei2marc.xsl"))
        .thenReturn(Optional.of(SchemaPflegeDatei.builder().datei(marcXsl).build()));
    when(servicePort.findById("MarcCodes_Laender_Sprachen.xml"))
        .thenReturn(Optional.of(SchemaPflegeDatei.builder().datei(marcCodes).build()));

    producer = new DataProviderContextProducer(servicePort, configuration);
  }

  @Test
  void testAllConfiguredFormatsAreProduced() {
    DataProviderContext result = producer.createDataProviderContext();

    List<MetadataFormat> metadataFormats = result.getContext().getMetadataFormats();
    assertEquals(3, metadataFormats.size());
  }

  @Test
  void testTeiFormatHasNoTransformer() {
    DataProviderContext result = producer.createDataProviderContext();

    MetadataFormat tei = result.getContext().formatForPrefix("oai_tei");
    assertNotNull(tei);
    assertEquals("http://www.tei-c.org/ns/1.0", tei.getNamespace());
    assertNull(tei.getTransformer());
  }

  @Test
  void testDcFormatHasTransformer() {
    DataProviderContext result = producer.createDataProviderContext();

    MetadataFormat dc = result.getContext().formatForPrefix("oai_dc");
    assertNotNull(dc);
    assertEquals("http://purl.org/dc/elements/1.1/", dc.getNamespace());
    assertNotNull(dc.getTransformer());
  }

  @Test
  void testMarc21FormatHasTransformer() {
    DataProviderContext result = producer.createDataProviderContext();

    MetadataFormat marc21 = result.getContext().formatForPrefix("oai_marc21");
    assertNotNull(marc21);
    assertEquals("http://www.loc.gov/MARC21/slim", marc21.getNamespace());
    assertNotNull(marc21.getTransformer());
  }

  @Test
  void testTemplatesAreCached() {
    producer.createDataProviderContext();
    producer.createDataProviderContext();
    producer.createDataProviderContext();

    verify(servicePort, times(1)).findById("tei2dc.xsl");
    verify(servicePort, times(1)).findById("tei2marc.xsl");
    verify(servicePort, times(1)).findById("MarcCodes_Laender_Sprachen.xml");
  }

  @Test
  void testCreateDataProviderContext_whenSchemaServiceFails_skipsFailedFormat() {
    SchemaPflegeDateiBoundary failingServicePort = mock(SchemaPflegeDateiBoundary.class);
    Configuration failingConfiguration = mock(Configuration.class);

    MetadataFormatConfig teiConfig = mock(MetadataFormatConfig.class);
    when(teiConfig.prefix()).thenReturn("oai_tei");
    when(teiConfig.namespace()).thenReturn("http://www.tei-c.org/ns/1.0");
    when(teiConfig.schemaResourceId()).thenReturn(Optional.empty());
    when(teiConfig.additionalResourceIds()).thenReturn(Optional.empty());

    MetadataFormatConfig dcConfig = mock(MetadataFormatConfig.class);
    when(dcConfig.prefix()).thenReturn("oai_dc");
    when(dcConfig.namespace()).thenReturn("http://purl.org/dc/elements/1.1/");
    when(dcConfig.schemaResourceId()).thenReturn(Optional.of("tei2dc.xsl"));
    when(dcConfig.additionalResourceIds()).thenReturn(Optional.empty());

    Map<String, MetadataFormatConfig> formats = new LinkedHashMap<>();
    formats.put("tei", teiConfig);
    formats.put("dc", dcConfig);
    when(failingConfiguration.metadataFormats()).thenReturn(formats);

    when(failingServicePort.findById(anyString()))
        .thenReturn(Optional.empty());

    DataProviderContextProducer failingProducer =
        new DataProviderContextProducer(failingServicePort, failingConfiguration);

    DataProviderContext result = failingProducer.createDataProviderContext();

    List<MetadataFormat> metadataFormats = result.getContext().getMetadataFormats();
    assertEquals(1, metadataFormats.size());
    assertNotNull(result.getContext().formatForPrefix("oai_tei"));
  }

  @Test
  void testCreateDataProviderContext_whenOneFormatFails_othersStillWork() throws Exception {
    SchemaPflegeDateiBoundary partialServicePort = mock(SchemaPflegeDateiBoundary.class);

    when(partialServicePort.findById("tei2dc.xsl"))
        .thenReturn(Optional.of(SchemaPflegeDatei.builder()
            .datei(readResource("oai-pmh/tei2dc.xsl")).build()));
    when(partialServicePort.findById("tei2marc.xsl"))
        .thenReturn(Optional.empty());

    DataProviderContextProducer partialProducer =
        new DataProviderContextProducer(partialServicePort, configuration);

    DataProviderContext result = partialProducer.createDataProviderContext();

    List<MetadataFormat> metadataFormats = result.getContext().getMetadataFormats();
    assertEquals(2, metadataFormats.size());
    assertNotNull(result.getContext().formatForPrefix("oai_tei"));
    assertNotNull(result.getContext().formatForPrefix("oai_dc"));
  }

  @Test
  void testTemplatesNotCachedOnFailure() throws Exception {
    SchemaPflegeDateiBoundary failingServicePort = mock(SchemaPflegeDateiBoundary.class);
    Configuration failingConfiguration = mock(Configuration.class);

    MetadataFormatConfig dcConfig = mock(MetadataFormatConfig.class);
    when(dcConfig.prefix()).thenReturn("oai_dc");
    when(dcConfig.namespace()).thenReturn("http://purl.org/dc/elements/1.1/");
    when(dcConfig.schemaResourceId()).thenReturn(Optional.of("tei2dc.xsl"));
    when(dcConfig.additionalResourceIds()).thenReturn(Optional.empty());

    Map<String, MetadataFormatConfig> formats = new LinkedHashMap<>();
    formats.put("dc", dcConfig);
    when(failingConfiguration.metadataFormats()).thenReturn(formats);

    when(failingServicePort.findById(anyString()))
        .thenReturn(Optional.empty());

    DataProviderContextProducer failingProducer =
        new DataProviderContextProducer(failingServicePort, failingConfiguration);

    failingProducer.getOrLoadTemplates();
    failingProducer.getOrLoadTemplates();

    verify(failingServicePort, times(2)).findById("tei2dc.xsl");
  }

  @Test
  void testCreateDataProviderContext_whenAdditionalResourceFails_skipsMarc21() throws Exception {
    SchemaPflegeDateiBoundary partialServicePort = mock(SchemaPflegeDateiBoundary.class);

    when(partialServicePort.findById("tei2dc.xsl"))
        .thenReturn(Optional.of(SchemaPflegeDatei.builder()
            .datei(readResource("oai-pmh/tei2dc.xsl")).build()));
    when(partialServicePort.findById("tei2marc.xsl"))
        .thenReturn(Optional.of(SchemaPflegeDatei.builder()
            .datei(readResource("oai-pmh/tei2marc.xsl")).build()));
    when(partialServicePort.findById("MarcCodes_Laender_Sprachen.xml"))
        .thenReturn(Optional.empty());

    DataProviderContextProducer partialProducer =
        new DataProviderContextProducer(partialServicePort, configuration);

    DataProviderContext result = partialProducer.createDataProviderContext();

    List<MetadataFormat> metadataFormats = result.getContext().getMetadataFormats();
    assertEquals(2, metadataFormats.size());
    assertNotNull(result.getContext().formatForPrefix("oai_tei"));
    assertNotNull(result.getContext().formatForPrefix("oai_dc"));
  }

  private String readResource(String path) throws Exception {
    URL url = getClass().getClassLoader().getResource(path);
    return Files.readString(Paths.get(Objects.requireNonNull(url).toURI()));
  }
}
