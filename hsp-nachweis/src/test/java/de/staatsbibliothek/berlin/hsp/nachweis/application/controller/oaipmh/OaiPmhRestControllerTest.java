package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.oaipmh;

import static io.restassured.RestAssured.given;
import static io.restassured.config.XmlConfig.xmlConfig;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.TestUtils.configuration.OaiPmhTestConfig;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.schemapflege.SchemaPflegeDatei;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.SchemaPflegeDateiServicePort;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import io.quarkus.test.InjectMock;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@QuarkusTest
@TestProfile(OaiPmhTestConfig.class)
@Execution(ExecutionMode.SAME_THREAD)
class OaiPmhRestControllerTest {

  private static final ZoneId ZONE_ID = ZoneId.systemDefault();
  final String xml4469;
  final String xmlacd5;
  final String xml19ad;
  final String oaiMetadata = "OAI-PMH.GetRecord.record.metadata.";
  private final Map<String, String> namespaces =
      Map.of(
          "oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/",
          "dc", "http://purl.org/dc/elements/1.1/");

  @Inject
  EntityManager em;
  @Inject
  KulturObjektDokumentRepository kulturObjektDokumentRepository;
  @Inject
  NormdatenReferenzRepository normdatenReferenzRepository;


  @InjectMock
  @RestClient
  SchemaPflegeDateiServicePort schemaPflegeDateiServicePort;

  public OaiPmhRestControllerTest() throws Exception {
    URL url4469 =
        this.getClass()
            .getClassLoader()
            .getResource("oai-pmh/tei-HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469.xml");
    xml4469 = Files.readString(Paths.get(Objects.requireNonNull(url4469).toURI()));
    URL urlacd5 =
        this.getClass()
            .getClassLoader()
            .getResource("oai-pmh/tei-HSP-fb0f8564-3ba1-3971-a1b7-9d8e7471acd5.xml");
    xmlacd5 = Files.readString(Paths.get(Objects.requireNonNull(urlacd5).toURI()));
    URL url19ad =
        this.getClass()
            .getClassLoader()
            .getResource("oai-pmh/tei-HSP-26fe0b64-3c9d-3d4b-bbea-aec4238e19ad.xml");
    xml19ad = Files.readString(Paths.get(Objects.requireNonNull(url19ad).toURI()));
  }

  @BeforeEach
  void setUp() throws URISyntaxException, IOException {
    createTestData();

    var dcXsl = readTestResource("oai-pmh/tei2dc.xsl");
    when(schemaPflegeDateiServicePort.findById("tei2dc.xsl"))
        .thenReturn(SchemaPflegeDatei.builder()
            .dateiName("tei2dc.xsl")
            .datei(dcXsl)
            .build());

    var marcXsl = readTestResource("oai-pmh/tei2marc.xsl");
    when(schemaPflegeDateiServicePort.findById("tei2marc.xsl"))
        .thenReturn(SchemaPflegeDatei.builder()
            .dateiName("tei2marc.xsl")
            .datei(marcXsl)
            .build());

    var marcCodes = readTestResource("oai-pmh/MarcCodes_Laender_Sprachen.xml");
    when(schemaPflegeDateiServicePort.findById("MarcCodes_Laender_Sprachen.xml"))
        .thenReturn(SchemaPflegeDatei.builder()
            .dateiName("MarcCodes_Laender_Sprachen.xml")
            .datei(marcCodes)
            .build());
  }

  @AfterEach
  @Transactional(TxType.REQUIRES_NEW)
  void cleanUp() {
    em.createQuery("DELETE FROM KulturObjektDokument").executeUpdate();
    em.createQuery("DELETE FROM Identifikation").executeUpdate();
    em.createQuery("DELETE FROM NormdatenReferenz").executeUpdate();
  }

  @Test
  void testEndpointExists() {
    var res =
        given()
            .header("Accept", "text/xml")
            .queryParam("verb", "Identify")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();

    res.then().statusCode(200);
  }

  @Test
  void testIdentifyGet() {
    var res =
        given()
            .header("Accept", "text/xml")
            .queryParam("verb", "Identify")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();

    res.then()
        .statusCode(200)
        .body("OAI-PMH.Identify.repositoryName", equalTo("Handschriftenportal"))
        .body("OAI-PMH.Identify.baseURL", equalTo("http://localhost:9298/rest/oai-pmh"))
        .body("OAI-PMH.Identify.protocolVersion", equalTo("2.0"))
        .body("OAI-PMH.Identify.adminEmail[0]", equalTo("admin@handschriftenportal.de"))
        .body("OAI-PMH.Identify.adminEmail[1]", equalTo("admin2@handschriftenportal.de"))
        .body("OAI-PMH.Identify.earliestDatestamp", equalTo("2000-01-01T00:00:00Z"))
        .body("OAI-PMH.Identify.deletedRecord", equalTo("no"))
        .body("OAI-PMH.Identify.granularity", equalTo("YYYY-MM-DD"));
  }

  @Test
  void testIdentifyPost() {
    var res =
        given()
            .header("Accept", "text/xml")
            .contentType("application/x-www-form-urlencoded")
            .formParam("verb", "Identify")
            .when()
            .post("/rest/oai-pmh");

    res.prettyPrint();

    res.then()
        .statusCode(200)
        .body("OAI-PMH.Identify.repositoryName", equalTo("Handschriftenportal"))
        .body("OAI-PMH.Identify.baseURL", equalTo("http://localhost:9298/rest/oai-pmh"))
        .body("OAI-PMH.Identify.protocolVersion", equalTo("2.0"))
        .body("OAI-PMH.Identify.adminEmail[0]", equalTo("admin@handschriftenportal.de"))
        .body("OAI-PMH.Identify.adminEmail[1]", equalTo("admin2@handschriftenportal.de"))
        .body("OAI-PMH.Identify.earliestDatestamp", equalTo("2000-01-01T00:00:00Z"))
        .body("OAI-PMH.Identify.deletedRecord", equalTo("no"))
        .body("OAI-PMH.Identify.granularity", equalTo("YYYY-MM-DD"));
  }

  @Test
  void testBadVerbGet() {
    var res = given().queryParam("verb", "Identif").when().post("/rest/oai-pmh");

    res.prettyPrint();

    res.then().statusCode(200).body("OAI-PMH.error.@code", equalTo("badVerb"));
  }

  @Test
  void testBadVerbPost() {
    var res =
        given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("verb", "Identif")
            .when()
            .post("/rest/oai-pmh");

    res.prettyPrint();

    res.then().statusCode(200).body("OAI-PMH.error.@code", equalTo("badVerb"));
  }

  @Test
  @TestTransaction
  void testGetRecordTeiGet() {
    var res =
        given()
            .header("Accept", "text/xml")
            .queryParam("verb", "GetRecord")
            .queryParam("identifier", "HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469")
            .queryParam("metadataPrefix", "oai_tei")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();

    res.then()
        .statusCode(200)
        .body(
            "OAI-PMH.GetRecord.record.header.identifier",
            equalTo("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469"))
        .body("OAI-PMH.GetRecord.record.header.setSpec", equalTo("NORM-1"))
        .body(
            oaiMetadata + "TEI.teiHeader.fileDesc.titleStmt.title",
            containsString(
                "Kulturobjektdokument von Berlin, Staatsbibliothek zu Berlin, Ms. Ham. 166"))
        .body(oaiMetadata + "TEI.teiHeader.fileDesc.titleStmt.respStmt.name.@type", equalTo("org"))
        .body(
            oaiMetadata + "TEI.teiHeader.fileDesc.titleStmt.respStmt.name",
            equalTo("Handschriftenportal"));
  }

  @Test
  @TestTransaction
  void testGetRecordIdDoesNotExist() {
    var res =
        given()
            .header("Accept", "text/xml")
            .queryParam("verb", "GetRecord")
            .queryParam("identifier", "HSP-814ec961-7bf0-382f-86ff-ebb16b3f446")
            .queryParam("metadataPrefix", "oai_tei")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();

    res.then()
        .statusCode(200)
        .body(
            "OAI-PMH.error.@code",
            equalTo("idDoesNotExist"));
  }

  @Test
  @TestTransaction
  void testGetRecordTeiPost() {
    var res =
        given()
            .header("Accept", "text/xml")
            .contentType("application/x-www-form-urlencoded")
            .formParam("verb", "GetRecord")
            .formParam("identifier", "HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469")
            .formParam("metadataPrefix", "oai_tei")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();

    res.then()
        .statusCode(200)
        .body(
            "OAI-PMH.GetRecord.record.header.identifier",
            equalTo("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469"))
        .body("OAI-PMH.GetRecord.record.header.setSpec", equalTo("NORM-1"))
        .body(
            oaiMetadata + "TEI.teiHeader.fileDesc.titleStmt.title",
            containsString(
                "Kulturobjektdokument von Berlin, Staatsbibliothek zu Berlin, Ms. Ham. 166"))
        .body(oaiMetadata + "TEI.teiHeader.fileDesc.titleStmt.respStmt.name.@type", equalTo("org"))
        .body(
            oaiMetadata + "TEI.teiHeader.fileDesc.titleStmt.respStmt.name",
            equalTo("Handschriftenportal"));
  }

  @Test
  @TestTransaction
  void testGetRecordDcGet() {
    var res =
        given()
            .config(RestAssured.config().xmlConfig(xmlConfig().declareNamespaces(namespaces)))
            .header("Accept", "text/xml")
            .queryParam("verb", "GetRecord")
            .queryParam("identifier", "HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469")
            .queryParam("metadataPrefix", "oai_dc")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();

    res.then()
        .statusCode(200)
        .body("OAI-PMH.request.@verb", equalTo("GetRecord"))
        .body("OAI-PMH.request.@metadataPrefix", equalTo("oai_dc"))
        .body("OAI-PMH.request.@identifier", equalTo("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469"))
        .body("OAI-PMH.GetRecord.record.header.setSpec", equalTo("NORM-1"))
        .body(
            "OAI-PMH.GetRecord.record.metadata.oai_dc:dc.dc:identifier[0]",
            equalTo("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469"))
        .body(
            "OAI-PMH.GetRecord.record.metadata.oai_dc:dc.dc:identifier[1]",
            equalTo(
                """
                    Berlin
                                ,\s
                                  Staatsbibliothek zu Berlin
                                , Ms. Ham. 166"""))
        .body(
            "OAI-PMH.GetRecord.record.metadata.oai_dc:dc.dc:title",
            containsString(
                "Kulturobjektdokument von Berlin, Staatsbibliothek zu Berlin, Ms. Ham. 166"))
        .body("OAI-PMH.GetRecord.record.metadata.oai_dc:dc.dc:type", equalTo("Text"));
  }

  @Test
  @TestTransaction
  void testGetRecordDcPost() {
    var res =
        given()
            .config(RestAssured.config().xmlConfig(xmlConfig().declareNamespaces(namespaces)))
            .header("Accept", "text/xml")
            .formParam("verb", "GetRecord")
            .formParam("identifier", "HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469")
            .formParam("metadataPrefix", "oai_dc")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();

    res.then()
        .statusCode(200)
        .body("OAI-PMH.request.@verb", equalTo("GetRecord"))
        .body("OAI-PMH.request.@metadataPrefix", equalTo("oai_dc"))
        .body("OAI-PMH.request.@identifier", equalTo("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469"))
        .body("OAI-PMH.GetRecord.record.header.setSpec", equalTo("NORM-1"))
        .body(
            "OAI-PMH.GetRecord.record.metadata.oai_dc:dc.dc:identifier[0]",
            equalTo("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469"))
        .body(
            "OAI-PMH.GetRecord.record.metadata.oai_dc:dc.dc:identifier[1]",
            equalTo(
                """
                    Berlin
                                ,\s
                                  Staatsbibliothek zu Berlin
                                , Ms. Ham. 166"""))
        .body(
            "OAI-PMH.GetRecord.record.metadata.oai_dc:dc.dc:title",
            containsString(
                "Kulturobjektdokument von Berlin, Staatsbibliothek zu Berlin, Ms. Ham. 166"))
        .body("OAI-PMH.GetRecord.record.metadata.oai_dc:dc.dc:type", equalTo("Text"));
  }

  @Test
  @TestTransaction
  void testListSetsGet() {
    var res =
        given()
            .config(RestAssured.config().xmlConfig(xmlConfig().declareNamespaces(namespaces)))
            .header("Accept", "text/xml")
            .queryParam("verb", "ListSets")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();

    res.then()
        .statusCode(200)
        .body("OAI-PMH.ListSets.set[0].setSpec", equalTo("NORM-1"))
        .body("OAI-PMH.ListSets.set[0].setName", equalTo("Normdatum1"))
        .body("OAI-PMH.ListSets.set[1].setSpec", equalTo("NORM-3"))
        .body("OAI-PMH.ListSets.set[1].setName", equalTo("Normdatum3"));
  }

  @Test
  void testListSetsPost() {
    var res =
        given()
            .config(RestAssured.config().xmlConfig(xmlConfig().declareNamespaces(namespaces)))
            .header("Accept", "text/xml")
            .formParam("verb", "ListSets")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();

    res.then()
        .statusCode(200)
        .body("OAI-PMH.ListSets.set[0].setSpec", equalTo("NORM-1"))
        .body("OAI-PMH.ListSets.set[0].setName", equalTo("Normdatum1"))
        .body("OAI-PMH.ListSets.set[1].setSpec", equalTo("NORM-3"))
        .body("OAI-PMH.ListSets.set[1].setName", equalTo("Normdatum3"));
  }

  @Test
  @TestTransaction
  void testListRecordsGet() {
    var res =
        given()
            .config(RestAssured.config().xmlConfig(xmlConfig().declareNamespaces(namespaces)))
            .header("Accept", "text/xml")
            .queryParam("verb", "ListRecords")
            .queryParam("metadataPrefix", "oai_dc")
            .queryParam("from", "2020-05-01")
            .queryParam("until", "2020-07-01")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();

    res.then()
        .statusCode(200)
        .body("OAI-PMH.ListRecords.record.size()", equalTo(2))
        .body(
            "OAI-PMH.ListRecords.record[0].header.identifier",
            equalTo("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469"))
        .body(
            "OAI-PMH.ListRecords.record[0].metadata.oai_dc:dc.dc:identifier[0]",
            equalTo("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469"))
        .body(
            "OAI-PMH.ListRecords.record[0].metadata.oai_dc:dc.dc:identifier[1]",
            equalTo(
                """
                    Berlin
                                ,\s
                                  Staatsbibliothek zu Berlin
                                , Ms. Ham. 166"""))
        .body(
            "OAI-PMH.ListRecords.record[0].metadata.oai_dc:dc.dc:title",
            containsString(
                "Kulturobjektdokument von Berlin, Staatsbibliothek zu Berlin, Ms. Ham. 166"))
        .body("OAI-PMH.ListRecords.record[0].metadata.oai_dc:dc.dc:type", equalTo("Text"))
        .body(
            "OAI-PMH.ListRecords.record[1].header.identifier",
            equalTo("HSP-fb0f8564-3ba1-3971-a1b7-9d8e7471acd5"))
        .body(
            "OAI-PMH.ListRecords.record[1].metadata.oai_dc:dc.dc:identifier[0]",
            equalTo("HSP-fb0f8564-3ba1-3971-a1b7-9d8e7471acd5"))
        .body(
            "OAI-PMH.ListRecords.record[1].metadata.oai_dc:dc.dc:identifier[1]",
            equalTo(
                """
                    New Haven,
                                  Conn.
                                , Yale Center
                                  for British Art. Paul Mellon Collection
                                , U860.F46 1450"""))
        .body(
            "OAI-PMH.ListRecords.record[1].metadata.oai_dc:dc.dc:title",
            containsString(
                "Kulturobjektdokument von New Haven, Conn., Yale Center for British Art. Paul Mellon"))
        .body(
            "OAI-PMH.ListRecords.record[1].metadata.oai_dc:dc.dc:title",
            containsString("Collection, U860.F46 1450"))
        .body("OAI-PMH.ListRecords.record[1].metadata.oai_dc:dc.dc:type", equalTo("Text"));
  }

  @Test
  @TestTransaction
  void testListRecordsTEIGet() {
    var res =
        given()
            .config(RestAssured.config().xmlConfig(xmlConfig().declareNamespaces(namespaces)))
            .header("Accept", "text/xml")
            .queryParam("verb", "ListRecords")
            .queryParam("metadataPrefix", "oai_tei")
            .queryParam("from", "2020-05-01")
            .queryParam("until", "2020-07-01")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();

    res.then()
        .statusCode(200)
        .body("OAI-PMH.ListRecords.record.size()", equalTo(2))
        .body(containsString("<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">"));
  }

  @Test
  @TestTransaction
  void testListRecordsPost() {
    var res =
        given()
            .config(RestAssured.config().xmlConfig(xmlConfig().declareNamespaces(namespaces)))
            .header("Accept", "text/xml")
            .formParam("verb", "ListRecords")
            .formParam("metadataPrefix", "oai_dc")
            .formParam("from", "2020-04-01")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();

    res.then()
        .statusCode(200)
        .body("OAI-PMH.ListRecords.record.size()", equalTo(2))
        .body(
            "OAI-PMH.ListRecords.record[0].header.identifier",
            equalTo("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469"))
        .body(
            "OAI-PMH.ListRecords.record[0].metadata.oai_dc:dc.dc:identifier[0]",
            equalTo("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469"))
        .body(
            "OAI-PMH.ListRecords.record[0].metadata.oai_dc:dc.dc:identifier[1]",
            equalTo(
                """
                    Berlin
                                ,\s
                                  Staatsbibliothek zu Berlin
                                , Ms. Ham. 166"""))
        .body(
            "OAI-PMH.ListRecords.record[0].metadata.oai_dc:dc.dc:title",
            containsString(
                "Kulturobjektdokument von Berlin, Staatsbibliothek zu Berlin, Ms. Ham. 166"))
        .body("OAI-PMH.ListRecords.record[0].metadata.oai_dc:dc.dc:type", equalTo("Text"))
        .body(
            "OAI-PMH.ListRecords.record[1].header.identifier",
            equalTo("HSP-fb0f8564-3ba1-3971-a1b7-9d8e7471acd5"))
        .body(
            "OAI-PMH.ListRecords.record[1].metadata.oai_dc:dc.dc:identifier[0]",
            equalTo("HSP-fb0f8564-3ba1-3971-a1b7-9d8e7471acd5"))
        .body(
            "OAI-PMH.ListRecords.record[1].metadata.oai_dc:dc.dc:identifier[1]",
            equalTo(
                """
                    New Haven,
                                  Conn.
                                , Yale Center
                                  for British Art. Paul Mellon Collection
                                , U860.F46 1450"""))
        .body(
            "OAI-PMH.ListRecords.record[1].metadata.oai_dc:dc.dc:title",
            containsString(
                "Kulturobjektdokument von New Haven, Conn., Yale Center for British Art. Paul Mellon"))
        .body(
            "OAI-PMH.ListRecords.record[1].metadata.oai_dc:dc.dc:title",
            containsString("Collection, U860.F46 1450"))
        .body("OAI-PMH.ListRecords.record[1].metadata.oai_dc:dc.dc:type", equalTo("Text"));
  }

  @Test
  void testListRecordsWithSetGet() {
    var res =
        given()
            .config(RestAssured.config().xmlConfig(xmlConfig().declareNamespaces(namespaces)))
            .header("Accept", "text/xml")
            .queryParam("verb", "ListRecords")
            .queryParam("metadataPrefix", "oai_dc")
            .queryParam("set", "NORM-1")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();
    res.then()
        .statusCode(200)
        .body("OAI-PMH.ListRecords.record.size()", equalTo(2))
        .body(
            "OAI-PMH.ListRecords.record[0].header.identifier",
            equalTo("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469"))
        .body(
            "OAI-PMH.ListRecords.record[0].metadata.oai_dc:dc.dc:identifier[0]",
            equalTo("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469"))
        .body(
            "OAI-PMH.ListRecords.record[0].metadata.oai_dc:dc.dc:identifier[1]",
            equalTo(
                """
                    Berlin
                                ,\s
                                  Staatsbibliothek zu Berlin
                                , Ms. Ham. 166"""))
        .body(
            "OAI-PMH.ListRecords.record[0].metadata.oai_dc:dc.dc:title",
            containsString(
                "Kulturobjektdokument von Berlin, Staatsbibliothek zu Berlin, Ms. Ham. 166"))
        .body("OAI-PMH.ListRecords.record[0].metadata.oai_dc:dc.dc:type", equalTo("Text"))
        .body(
            "OAI-PMH.ListRecords.record[1].header.identifier",
            equalTo("HSP-fb0f8564-3ba1-3971-a1b7-9d8e7471acd5"))
        .body(
            "OAI-PMH.ListRecords.record[1].metadata.oai_dc:dc.dc:identifier[0]",
            equalTo("HSP-fb0f8564-3ba1-3971-a1b7-9d8e7471acd5"))
        .body(
            "OAI-PMH.ListRecords.record[1].metadata.oai_dc:dc.dc:identifier[1]",
            equalTo(
                """
                    New Haven,
                                  Conn.
                                , Yale Center
                                  for British Art. Paul Mellon Collection
                                , U860.F46 1450"""))
        .body(
            "OAI-PMH.ListRecords.record[1].metadata.oai_dc:dc.dc:title",
            containsString(
                "Kulturobjektdokument von New Haven, Conn., Yale Center for British Art. Paul Mellon"))
        .body(
            "OAI-PMH.ListRecords.record[1].metadata.oai_dc:dc.dc:title",
            containsString("Collection, U860.F46 1450"))
        .body("OAI-PMH.ListRecords.record[1].metadata.oai_dc:dc.dc:type", equalTo("Text"));
  }

  @Test
  void testListRecordsWithSetPost() {
    var res =
        given()
            .config(RestAssured.config().xmlConfig(xmlConfig().declareNamespaces(namespaces)))
            .header("Accept", "text/xml")
            .formParam("verb", "ListRecords")
            .formParam("metadataPrefix", "oai_dc")
            .formParam("set", "NORM-1")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();
    res.then()
        .statusCode(200)
        .body("OAI-PMH.ListRecords.record.size()", equalTo(2))
        .body(
            "OAI-PMH.ListRecords.record[0].header.identifier",
            equalTo("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469"))
        .body(
            "OAI-PMH.ListRecords.record[0].metadata.oai_dc:dc.dc:identifier[0]",
            equalTo("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469"))
        .body(
            "OAI-PMH.ListRecords.record[0].metadata.oai_dc:dc.dc:identifier[1]",
            equalTo(
                """
                    Berlin
                                ,\s
                                  Staatsbibliothek zu Berlin
                                , Ms. Ham. 166"""))
        .body(
            "OAI-PMH.ListRecords.record[0].metadata.oai_dc:dc.dc:title",
            containsString(
                "Kulturobjektdokument von Berlin, Staatsbibliothek zu Berlin, Ms. Ham. 166"))
        .body("OAI-PMH.ListRecords.record[0].metadata.oai_dc:dc.dc:type", equalTo("Text"))
        .body(
            "OAI-PMH.ListRecords.record[1].header.identifier",
            equalTo("HSP-fb0f8564-3ba1-3971-a1b7-9d8e7471acd5"))
        .body(
            "OAI-PMH.ListRecords.record[1].metadata.oai_dc:dc.dc:identifier[0]",
            equalTo("HSP-fb0f8564-3ba1-3971-a1b7-9d8e7471acd5"))
        .body(
            "OAI-PMH.ListRecords.record[1].metadata.oai_dc:dc.dc:identifier[1]",
            equalTo(
                """
                    New Haven,
                                  Conn.
                                , Yale Center
                                  for British Art. Paul Mellon Collection
                                , U860.F46 1450"""))
        .body(
            "OAI-PMH.ListRecords.record[1].metadata.oai_dc:dc.dc:title",
            containsString(
                "Kulturobjektdokument von New Haven, Conn., Yale Center for British Art. Paul Mellon"))
        .body(
            "OAI-PMH.ListRecords.record[1].metadata.oai_dc:dc.dc:title",
            containsString("Collection, U860.F46 1450"))
        .body("OAI-PMH.ListRecords.record[1].metadata.oai_dc:dc.dc:type", equalTo("Text"));
  }

  @Test
  void testListIdentifiersGet() {
    var res =
        given()
            .config(RestAssured.config().xmlConfig(xmlConfig().declareNamespaces(namespaces)))
            .header("Accept", "text/xml")
            .queryParam("verb", "ListIdentifiers")
            .queryParam("metadataPrefix", "oai_dc")
            .queryParam("from", "2020-03-01")
            .queryParam("until", "2020-07-01")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();
    res.then()
        .statusCode(200)
        .body("OAI-PMH.ListIdentifiers.header.size()", equalTo(3))
        .body(
            "OAI-PMH.ListIdentifiers.header[0].identifier",
            equalTo("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469"))
        .body("OAI-PMH.ListIdentifiers.header[0].setSpec", equalTo("NORM-1"))
        .body("OAI-PMH.ListIdentifiers.header[0].datestamp", equalTo("2020-07-01"))
        .body(
            "OAI-PMH.ListIdentifiers.header[1].identifier",
            equalTo("HSP-fb0f8564-3ba1-3971-a1b7-9d8e7471acd5"))
        .body("OAI-PMH.ListIdentifiers.header[1].setSpec", equalTo("NORM-1"))
        .body("OAI-PMH.ListIdentifiers.header[1].datestamp", equalTo("2020-05-01"))
        .body(
            "OAI-PMH.ListIdentifiers.header[2].identifier",
            equalTo("HSP-26fe0b64-3c9d-3d4b-bbea-aec4238e19ad"))
        .body("OAI-PMH.ListIdentifiers.header[2].setSpec", equalTo("NORM-3"))
        .body("OAI-PMH.ListIdentifiers.header[2].datestamp", equalTo("2020-03-01"));
  }

  @Test
  void testListIdentifiersPost() {
    var res =
        given()
            .config(RestAssured.config().xmlConfig(xmlConfig().declareNamespaces(namespaces)))
            .header("Accept", "text/xml")
            .formParam("verb", "ListIdentifiers")
            .formParam("metadataPrefix", "oai_dc")
            .formParam("from", "2020-03-01")
            .formParam("until", "2020-07-01")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();
    res.then()
        .statusCode(200)
        .body("OAI-PMH.ListIdentifiers.header.size()", equalTo(3))
        .body(
            "OAI-PMH.ListIdentifiers.header[0].identifier",
            equalTo("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469"))
        .body("OAI-PMH.ListIdentifiers.header[0].setSpec", equalTo("NORM-1"))
        .body("OAI-PMH.ListIdentifiers.header[0].datestamp", equalTo("2020-07-01"))
        .body(
            "OAI-PMH.ListIdentifiers.header[1].identifier",
            equalTo("HSP-fb0f8564-3ba1-3971-a1b7-9d8e7471acd5"))
        .body("OAI-PMH.ListIdentifiers.header[1].setSpec", equalTo("NORM-1"))
        .body("OAI-PMH.ListIdentifiers.header[1].datestamp", equalTo("2020-05-01"))
        .body(
            "OAI-PMH.ListIdentifiers.header[2].identifier",
            equalTo("HSP-26fe0b64-3c9d-3d4b-bbea-aec4238e19ad"))
        .body("OAI-PMH.ListIdentifiers.header[2].setSpec", equalTo("NORM-3"))
        .body("OAI-PMH.ListIdentifiers.header[2].datestamp", equalTo("2020-03-01"));
  }

  @Test
  void testListRecordsGetNoRecords() {
    var res =
        given()
            .config(RestAssured.config().xmlConfig(xmlConfig().declareNamespaces(namespaces)))
            .header("Accept", "text/xml")
            .queryParam("verb", "ListRecords")
            .queryParam("metadataPrefix", "oai_dc")
            .queryParam("from", "2011-01-01")
            .queryParam("until", "2012-01-01")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();
    res.then().statusCode(200).body("OAI-PMH.error.@code", equalTo("noRecordsMatch"));
  }

  @Test
  void testListRecordsPostNoRecords() {
    var res =
        given()
            .config(RestAssured.config().xmlConfig(xmlConfig().declareNamespaces(namespaces)))
            .header("Accept", "text/xml")
            .formParam("verb", "ListRecords")
            .formParam("metadataPrefix", "oai_dc")
            .formParam("from", "2011-01-01")
            .formParam("until", "2012-01-01")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();
    res.then().statusCode(200).body("OAI-PMH.error.@code", equalTo("noRecordsMatch"));
  }

  @Test
  void testListMetadataFormatsGet() {
    var res =
        given()
            .header("Accept", "text/xml")
            .queryParam("verb", "ListMetadataFormats")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();

    res.then()
        .statusCode(200)
        .body(containsString("oai_tei"))
        .body(containsString("oai_dc"))
        .body(containsString("oai_marc21"));
  }

  @Test
  void testListMetadataFormatsPost() {
    var res =
        given()
            .header("Accept", "text/xml")
            .contentType("application/x-www-form-urlencoded")
            .formParam("verb", "ListMetadataFormats")
            .when()
            .post("/rest/oai-pmh");

    res.prettyPrint();

    res.then()
        .statusCode(200)
        .body(containsString("oai_tei"))
        .body(containsString("oai_dc"))
        .body(containsString("oai_marc21"));
  }

  @Test
  @TestTransaction
  void testGetRecordCannotDisseminateFormat() {
    var res =
        given()
            .header("Accept", "text/xml")
            .queryParam("verb", "GetRecord")
            .queryParam("identifier", "HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469")
            .queryParam("metadataPrefix", "oai_nonexistent")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();

    res.then()
        .statusCode(200)
        .body("OAI-PMH.error.@code", equalTo("cannotDisseminateFormat"));
  }

  @Test
  @TestTransaction
  void testGetRecordMarc21Get() {
    var res =
        given()
            .header("Accept", "text/xml")
            .queryParam("verb", "GetRecord")
            .queryParam("identifier", "HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469")
            .queryParam("metadataPrefix", "oai_marc21")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();

    res.then()
        .statusCode(200)
        .body(
            "OAI-PMH.GetRecord.record.header.identifier",
            equalTo("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469"))
        .body("OAI-PMH.GetRecord.record.header.setSpec", equalTo("NORM-1"))
        .body("OAI-PMH.GetRecord.record.metadata.record.@type", equalTo("Bibliographic"))
        .body(containsString(
            "<controlfield tag=\"001\">(DE-629)HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469</controlfield>"))
        .body(containsString("<controlfield tag=\"003\">DE-629</controlfield>"))
        .body(containsString("<controlfield tag=\"007\">tu</controlfield>"))
        .body(containsString("Ms. Ham. 166"));
  }

  @Test
  @TestTransaction
  void testGetRecordMarc21Post() {
    var res =
        given()
            .header("Accept", "text/xml")
            .contentType("application/x-www-form-urlencoded")
            .formParam("verb", "GetRecord")
            .formParam("identifier", "HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469")
            .formParam("metadataPrefix", "oai_marc21")
            .when()
            .post("/rest/oai-pmh");

    res.prettyPrint();

    res.then()
        .statusCode(200)
        .body(
            "OAI-PMH.GetRecord.record.header.identifier",
            equalTo("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469"))
        .body("OAI-PMH.GetRecord.record.header.setSpec", equalTo("NORM-1"))
        .body("OAI-PMH.GetRecord.record.metadata.record.@type", equalTo("Bibliographic"))
        .body(containsString(
            "<controlfield tag=\"001\">(DE-629)HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469</controlfield>"))
        .body(containsString("<controlfield tag=\"003\">DE-629</controlfield>"))
        .body(containsString("<controlfield tag=\"007\">tu</controlfield>"))
        .body(containsString("Ms. Ham. 166"));
  }

  @Test
  @TestTransaction
  void testListRecordsMarc21Get() {
    var res =
        given()
            .header("Accept", "text/xml")
            .queryParam("verb", "ListRecords")
            .queryParam("metadataPrefix", "oai_marc21")
            .queryParam("from", "2020-05-01")
            .queryParam("until", "2020-07-01")
            .when()
            .get("/rest/oai-pmh");

    res.prettyPrint();

    res.then()
        .statusCode(200)
        .body("OAI-PMH.ListRecords.record.size()", equalTo(2))
        .body(
            "OAI-PMH.ListRecords.record[0].header.identifier",
            equalTo("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469"))
        .body(containsString("type=\"Bibliographic\""))
        .body(containsString("Ms. Ham. 166"));
  }

  @Test
  @TestTransaction
  void testListRecordsMarc21Post() {
    var res =
        given()
            .header("Accept", "text/xml")
            .contentType("application/x-www-form-urlencoded")
            .formParam("verb", "ListRecords")
            .formParam("metadataPrefix", "oai_marc21")
            .formParam("from", "2020-05-01")
            .formParam("until", "2020-07-01")
            .when()
            .post("/rest/oai-pmh");

    res.prettyPrint();

    res.then()
        .statusCode(200)
        .body("OAI-PMH.ListRecords.record.size()", equalTo(2))
        .body(
            "OAI-PMH.ListRecords.record[0].header.identifier",
            equalTo("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469"))
        .body(containsString("type=\"Bibliographic\""))
        .body(containsString("Ms. Ham. 166"));
  }

  @Transactional(TxType.REQUIRES_NEW)
  void createTestData() {
    var normdatumBesitzer =
        new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId("NORM-1")
            .withName("Normdatum1")
            .withTypeName(GNDEntityFact.CORPORATE_BODY_TYPE_NAME)
            .build();
    var identifikation =
        new Identifikation.IdentifikationBuilder()
            .withId("ID1")
            .withBesitzer(normdatumBesitzer)
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();
    var kod =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withRegistrierungsDatum(
                LocalDateTime.ofInstant(Instant.parse("2020-07-01T00:00:00Z"), ZONE_ID))
            .withId("HSP-814ec961-7bf0-382f-86ff-ebb16b3f4469")
            .withTEIXml(xml4469)
            .withGueltigerIdentifikation(identifikation)
            .build();

    var identifikation2 =
        new Identifikation.IdentifikationBuilder()
            .withId("ID2")
            .withBesitzer(normdatumBesitzer)
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();
    var kod2 =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withRegistrierungsDatum(
                LocalDateTime.ofInstant(Instant.parse("2020-05-01T00:00:00Z"), ZONE_ID))
            .withId("HSP-fb0f8564-3ba1-3971-a1b7-9d8e7471acd5")
            .withTEIXml(xmlacd5)
            .withGueltigerIdentifikation(identifikation2)
            .build();

    var normdatumBesitzer3 =
        new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId("NORM-3")
            .withName("Normdatum3")
            .withTypeName(GNDEntityFact.CORPORATE_BODY_TYPE_NAME)
            .build();
    var identifikation3 =
        new Identifikation.IdentifikationBuilder()
            .withId("ID3")
            .withBesitzer(normdatumBesitzer3)
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();
    var kod3 =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withRegistrierungsDatum(
                LocalDateTime.ofInstant(Instant.parse("2020-03-01T00:00:00Z"), ZONE_ID))
            .withId("HSP-26fe0b64-3c9d-3d4b-bbea-aec4238e19ad")
            .withTEIXml(xml19ad)
            .withGueltigerIdentifikation(identifikation3)
            .build();
    var kods = new ArrayList<KulturObjektDokument>();
    kods.add(kod);
    kods.add(kod2);
    kods.add(kod3);
    kods.forEach(kulturObjektDokumentRepository::persist);
    var normdatumBesitzer4 =
        new NormdatenReferenz.NormdatenReferenzBuilder()
            .withId("NORM-4")
            .withName("Normdatum4")
            .withTypeName(GNDEntityFact.CORPORATE_BODY_TYPE_NAME)
            .build();
    normdatenReferenzRepository.persist(normdatumBesitzer4);
  }

  private String readTestResource(String path) throws URISyntaxException, IOException {
    URL url = getClass().getClassLoader().getResource(path);
    return Files.readString(Paths.get(Objects.requireNonNull(url).toURI()));
  }
}
