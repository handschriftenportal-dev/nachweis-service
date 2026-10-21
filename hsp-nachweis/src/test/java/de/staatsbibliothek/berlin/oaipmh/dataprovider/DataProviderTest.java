package de.staatsbibliothek.berlin.oaipmh.dataprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oaipmh.OAIConfiguration;
import de.staatsbibliothek.berlin.oaipmh.dataprovider.DataProviderContext.DataProviderContextBuilder;
import de.staatsbibliothek.berlin.oaipmh.model.OaiItem;
import de.staatsbibliothek.berlin.oaipmh.repository.ClientItemRepository;
import de.staatsbibliothek.berlin.oaipmh.transformer.TemplatesBuilder;
import io.gdcc.xoai.dataprovider.model.MetadataFormat;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@Slf4j
class DataProviderTest {

  private final String xml;
  private final OAIConfiguration oaiConfiguration =
      new SmallRyeConfigBuilder()
          .withMapping(OAIConfiguration.class)
          .withDefaultValues(
              Map.of(
                  "oaipmh.repositoryName", "Handschriftenportal",
                  "oaipmh.baseUrl", "http://test.handschriftenportal.dummy/rest/oai-pmh",
                  "oaipmh.protocolVersion", "2.0",
                  "oaipmh.adminEmail[0]", "admin@handschriftenportal.de",
                  "oaipmh.earliestDatestamp", "2000-01-01T00:00:00Z",
                  "oaipmh.deletedRecord", "persistent",
                  "oaipmh.granularity", "second",
                  "oaipmh.maxListIdentifiers", "200",
                  "oaipmh.maxListRecords", "200",
                  "oaipmh.maxListSets", "200"))
          .withDefaultValue("oaipmh.enabled", "true")
          .build()
          .unwrap(SmallRyeConfig.class)
          .getConfigMapping(OAIConfiguration.class);
  private final ClientItemRepository itemRepoMock = Mockito.mock(ClientItemRepository.class);
  private DataProvider dataProvider;

  public DataProviderTest() throws Exception {
    URL url =
        this.getClass()
            .getClassLoader()
            .getResource("oai-pmh/tei-HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469.xml");
    xml = Files.readString(Paths.get(Objects.requireNonNull(url).toURI()));
  }

  private DataProviderContext getDataProviderContext()
      throws URISyntaxException, IOException {
    URL dcUrl = this.getClass().getClassLoader().getResource("oai-pmh/tei2dc.xsl");
    String dcXslt = Files.readString(Paths.get(Objects.requireNonNull(dcUrl).toURI()));
    MetadataFormat tei = new MetadataFormat()
        .withNamespace("http://www.tei-c.org/ns/1.0")
        .withPrefix("oai_tei");
    MetadataFormat dc = new MetadataFormat()
        .withNamespace("http://purl.org/dc/elements/1.1/")
        .withPrefix("oai_dc")
        .withTransformer(new TemplatesBuilder(dcXslt).compile().build());

    URL marcUrl = this.getClass().getClassLoader().getResource("oai-pmh/tei2marc.xsl");
    String marcXslt = Files.readString(Paths.get(Objects.requireNonNull(marcUrl).toURI()));
    URL marcCodesUrl =
        this.getClass().getClassLoader().getResource("oai-pmh/MarcCodes_Laender_Sprachen.xml");
    String marcCodesXml =
        Files.readString(Paths.get(Objects.requireNonNull(marcCodesUrl).toURI()));
    MetadataFormat marc21 = new MetadataFormat()
        .withNamespace("http://www.loc.gov/MARC21/slim")
        .withPrefix("oai_marc21")
        .withTransformer(
            new TemplatesBuilder(marcXslt, Map.of("MarcCodes_Laender_Sprachen.xml", marcCodesXml))
                .compile().build());

    return new DataProviderContextBuilder()
        .addMetadataFormat(tei)
        .addMetadataFormat(dc)
        .addMetadataFormat(marc21)
        .build();
  }

  @BeforeEach
  void setUp() throws Exception {
    DataProviderContext context = getDataProviderContext();
    this.dataProvider =
        new DataProvider(
            itemRepoMock,
            new ClientSetRepositoryMock(),
            oaiConfiguration,
            () -> context);
  }

  @Test
  void testConfigurationInitialization() {
    assertNotNull(oaiConfiguration);
  }

  @Test
  void testNullParameters() {
    NullPointerException exception =
        assertThrows(NullPointerException.class, () -> dataProvider.handleRequest(null));
    assertEquals("The parameter Map<String,String[]> must be non-null", exception.getMessage());
  }

  @Test
  void testEmptyParameters() throws XMLStreamException {
    assertTrue(dataProvider.handleRequest(Map.of()).contains("No argument 'verb' found"));
  }

  @Test
  void testBadVerb() throws XMLStreamException {
    assertTrue(
        dataProvider
            .handleRequest(Map.of("verb", new String[]{"BadVerb"}))
            .contains("<error code=\"badVerb\">BadVerb</error>"));
  }

  @Test
  void testIdentify() throws XMLStreamException {
    assertTrue(
        dataProvider
            .handleRequest(Map.of("verb", new String[]{"Identify"}))
            .contains(
                "<request verb=\"Identify\">http://test.handschriftenportal.dummy/rest/oai-pmh</request>"
                    + "<Identify>"
                    + "<repositoryName>Handschriftenportal</repositoryName>"
                    + "<baseURL>http://test.handschriftenportal.dummy/rest/oai-pmh</baseURL>"
                    + "<protocolVersion>2.0</protocolVersion>"
                    + "<adminEmail>admin@handschriftenportal.de</adminEmail>"
                    + "<earliestDatestamp>2000-01-01T00:00:00Z</earliestDatestamp>"
                    + "<deletedRecord>persistent</deletedRecord>"
                    + "<granularity>YYYY-MM-DDThh:mm:ssZ</granularity>"
                    + "</Identify>"));
  }

  @Test
  void testLitsSets() throws XMLStreamException {
    var response = dataProvider.handleRequest(Map.of("verb", new String[]{"ListSets"}));

    assertTrue(response.contains("<request verb=\"ListSets\">"));
    assertTrue(
        response.contains(
            "<ListSets>"
                + "<set><setSpec>HSP-1</setSpec><setName>A</setName></set>"
                + "<set><setSpec>HSP-2</setSpec><setName>B</setName></set>"
                + "<set><setSpec>HSP-3</setSpec><setName>C</setName></set>"
                + "</ListSets>"));
  }

  @Test
  void testListRecords() throws Exception {
    Mockito.when(
            itemRepoMock.countWithFilters(
                Instant.parse("2004-01-01T00:00:00Z"),
                Instant.parse("2011-01-02T00:00:01Z"),
                "HSP-1"))
        .thenReturn(1L);
    Mockito.when(
            itemRepoMock.findWithFilters(
                Instant.parse("2004-01-01T00:00:00Z"),
                Instant.parse("2011-01-02T00:00:01Z"),
                "HSP-1",
                200,
                0))
        .thenReturn(
            Stream.of(
                    new OaiItem(
                        "HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469",
                        xml,
                        Instant.parse("2010-01-01T00:00:00Z"),
                        "HSP-1"))
                .toList());
    var response =
        dataProvider.handleRequest(
            Map.of(
                "verb", new String[]{"ListRecords"},
                "metadataPrefix", new String[]{"oai_dc"},
                "from", new String[]{"2004-01-01T00:00:00Z"},
                "until", new String[]{"2011-01-02T00:00:00Z"},
                "set", new String[]{"HSP-1"}));

    assertTrue(
        response.contains(
            "<request verb=\"ListRecords\" metadataPrefix=\"oai_dc\" from=\"2004-01-01T00:00:00Z\" until=\"2011-01-02T00:00:01Z\" set=\"HSP-1\">"));
    assertTrue(
        response.contains(
            "<dc:identifier xmlns:dc=\"http://purl.org/dc/elements/1.1/\">HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469</dc:identifier>"));
    assertTrue(
        response.contains(
            "<dc:title xmlns:dc=\"http://purl.org/dc/elements/1.1/\">Kulturobjektdokument von Berlin, Staatsbibliothek zu Berlin, Ms. Ham. 166"));
  }

  @Test
  void testListRecordsMarc21() throws Exception {
    Mockito.when(
            itemRepoMock.countWithFilters(
                Instant.parse("2004-01-01T00:00:00Z"),
                Instant.parse("2011-01-02T00:00:01Z"),
                "HSP-1"))
        .thenReturn(1L);
    Mockito.when(
            itemRepoMock.findWithFilters(
                Instant.parse("2004-01-01T00:00:00Z"),
                Instant.parse("2011-01-02T00:00:01Z"),
                "HSP-1",
                200,
                0))
        .thenReturn(
            Stream.of(
                    new OaiItem(
                        "HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469",
                        xml,
                        Instant.parse("2010-01-01T00:00:00Z"),
                        "HSP-1"))
                .toList());
    var response =
        dataProvider.handleRequest(
            Map.of(
                "verb", new String[]{"ListRecords"},
                "metadataPrefix", new String[]{"oai_marc21"},
                "from", new String[]{"2004-01-01T00:00:00Z"},
                "until", new String[]{"2011-01-02T00:00:00Z"},
                "set", new String[]{"HSP-1"}));

    assertTrue(
        response.contains(
            "<request verb=\"ListRecords\" metadataPrefix=\"oai_marc21\" from=\"2004-01-01T00:00:00Z\" until=\"2011-01-02T00:00:01Z\" set=\"HSP-1\">"));
    // Verify MARC21 record structure
    assertTrue(
        response.contains("type=\"Bibliographic\""),
        "Response should contain a MARC21 bibliographic record");
    // controlfield 001: HSP-ID with DE-629 prefix
    assertTrue(
        response.contains(
            "<controlfield tag=\"001\">(DE-629)HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469</controlfield>"),
        "Response should contain controlfield 001 with HSP-ID");
    // controlfield 003: institution code
    assertTrue(
        response.contains("<controlfield tag=\"003\">DE-629</controlfield>"),
        "Response should contain controlfield 003 with institution code DE-629");
    // controlfield 007: general material type
    assertTrue(
        response.contains("<controlfield tag=\"007\">tu</controlfield>"),
        "Response should contain controlfield 007 for text material");
    // datafield 852: location info (settlement + repository + shelfmark from the TEI fixture)
    assertTrue(
        response.contains("tag=\"852\""),
        "Response should contain datafield 852 (location)");
    // The title "Cicero: Epistolae" should appear in field 246
    assertTrue(
        response.contains("Cicero: Epistolae"),
        "Response should contain the title from the TEI source");
    // Shelfmark "Ms. Ham. 166" should appear
    assertTrue(
        response.contains("Ms. Ham. 166"),
        "Response should contain the shelfmark from the TEI source");
  }

  @Test
  void testListRecordsNoRecordsInSet() throws XMLStreamException {
    var response =
        dataProvider.handleRequest(
            Map.of(
                "verb", new String[]{"ListRecords"},
                "metadataPrefix", new String[]{"oai_dc"},
                "from", new String[]{"2004-01-01T00:00:00Z"},
                "until", new String[]{"2011-01-01T23:59:59Z"},
                "set", new String[]{"HSP-2"}));

    assertTrue(response.contains("<error code=\"noRecordsMatch\"/>"));
  }

  @Test
  void testListRecordsSetDoesNotExist() throws XMLStreamException {
    var response =
        dataProvider.handleRequest(
            Map.of(
                "verb", new String[]{"ListRecords"},
                "metadataPrefix", new String[]{"oai_dc"},
                "from", new String[]{"2004-01-01T00:00:00Z"},
                "until", new String[]{"2011-01-01T23:59:59Z"},
                "set", new String[]{"HSP-6"}));

    assertTrue(
        response.contains(
            "<error code=\"noRecordsMatch\">Requested set 'HSP-6' does not exist</error>"));
  }
}
