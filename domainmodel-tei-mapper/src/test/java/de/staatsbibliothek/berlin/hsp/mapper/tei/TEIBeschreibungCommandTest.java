package de.staatsbibliothek.berlin.hsp.mapper.tei;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.PTR_TYPE_HSP;
import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.kod.TEIKulturObjektDokumentCommand;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.KODTemplateMapper;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Bibl;
import org.tei_c.ns._1.BiblScope;
import org.tei_c.ns._1.Div;
import org.tei_c.ns._1.FileDesc;
import org.tei_c.ns._1.Lb;
import org.tei_c.ns._1.P;
import org.tei_c.ns._1.Pb;
import org.tei_c.ns._1.Ptr;
import org.tei_c.ns._1.PublicationStmt;
import org.tei_c.ns._1.Publisher;
import org.tei_c.ns._1.RevisionDesc;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.TeiHeader;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 12.05.2020.
 * @version 1.0
 */
class TEIBeschreibungCommandTest {

  public static final String RANGE_URL = "https://iiif.ub.uni-leipzig.de/0000034537/range/LOG_0010";
  public static final String KATALOG_MANIFEST_URL =
      "https://iiif.ub.uni-leipzig.de/0000034537/manifest.json";
  public static final String INTERNAL_PURL =
      "https://resolver.staatsbibliothek-berlin.de/HSP1660133922141000";
  public static final String BESCHREIBUNG_TARGET_URL =
      "https://handschriftenportal.de//search?hspobjectid=HSP-00b9f2b8-f1df-32b8-9ccc-2d76cfe17d42";
  public static final String EXTERNAL_PURL = "https://doi.org/10.1111/hex.12487";
  private static final Identifikation gueltigeIdentifikation =
      new Identifikation.IdentifikationBuilder()
          .withId("HSP-0037fea7-58e0-3df9-a45c-4sdf3424")
          .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
          .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
          .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
          .build();
  static TEI tei;
  static TEI tei_retro;
  static TEI tei_Aurich;
  static TEI tei_ODD109;

  @BeforeAll
  static void init() throws Exception {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei",
        "tei-msDesc_Westphal.xml");
    Path teiFilePathRetro =
        Paths.get("src", "test", "resources", "tei",
            "tei-msDesc_Westphal_Retro.xml");
    Path teiFilePathAurich = Paths.get("src", "test", "resources", "tei",
        "tei-msDesc_Aurich.xml");
    Path tei_ODD109Path = Paths.get("src", "test", "resources", "tei",
        "tei_odd109.xml");
    try (InputStream is = newInputStream(teiFilePath)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      tei = teiList.getFirst();
    }

    try (InputStream is = newInputStream(teiFilePathRetro)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      tei_retro = teiList.getFirst();
    }
    try (InputStream is = newInputStream(teiFilePathAurich)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      tei_Aurich = teiList.getFirst();
    }
    try (InputStream is = newInputStream(tei_ODD109Path)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      tei_ODD109 = teiList.getFirst();
    }
  }

  @Test
  void testUpdateBeschreibungRetroIDAndType()
      throws JAXBException, IOException {
    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder("HSP123", "HSP-123456",
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(tei_retro))
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .build();

    TEIBeschreibungCommand.openTEIDokument(beschreibung);
    TEIBeschreibungCommand.updateBeschreibungsIDAndType(beschreibung);
    TEIBeschreibungCommand.writeXML(beschreibung);
    assertTrue(
        beschreibung.getTeiXML().contains("type=\"hsp:description_retro\""));
  }

  @Test
  void testupdateBeschreibungsIDAndType() throws JAXBException, IOException {

    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder("HSP123", "HSP-123456",
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(tei))
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .build();

    TEIBeschreibungCommand.openTEIDokument(beschreibung);
    TEIBeschreibungCommand.updateBeschreibungsIDAndType(beschreibung);
    TEIBeschreibungCommand.writeXML(beschreibung);

    assertTrue(beschreibung.getTeiXML().contains("type=\"hsp:description\""));

    assertTrue(beschreibung.getTeiXML().contains("status=\"intern\""));

    assertTrue(beschreibung.getTeiXML().contains("id=\"HSP123\""));

    assertTrue(
        beschreibung.getTeiXML()
            .contains("<idno>mss_36-23-aug-2f_tei-msDesc_Westphal</idno>"));
  }

  @Test
  void testupdateKODBeschreibungenReferenzes() throws Exception {

    KulturObjektDokument kulturObjektDokument = createTestKOD();

    TEIKulturObjektDokumentCommand.updateKODBeschreibungenReferenzes(
        kulturObjektDokument);

    assertTrue(
        kulturObjektDokument
            .getTeiXML()
            .replaceAll("\\s", "")
            .contains("""
                listBibltype="root">
                <listBibldefault="true"type="descriptions">
                <biblxml:id="HSP123"default="true">
                <reftarget="HSP123"/></bibl>
                <biblxml:id="HSPTestBeschreibung">
                <reftarget="HSPTestBeschreibung"/></bibl>
                </listBibl>
                </listBibl>
                """.replaceAll("\\s", "")),
        kulturObjektDokument.getTeiXML().replaceAll("\\s", ""));
  }

  private KulturObjektDokument createTestKOD() throws Exception {
    NormdatenReferenz besitzer =
        new NormdatenReferenzBuilder()
            .withId("774909e2-f687-30cb-a5c4-ddc95806d6be")
            .withName(
                "Stiftung Preußischer Kulturbesitz. Staatsbibliothek zu Berlin")
            .withTypeName("CorporateBody")
            .withGndID("4005728-8")
            .addVarianterName(
                new VarianterName(
                    "Stiftung Preußischer Kulturbesitz. Staatsbibliothek zu Berlin",
                    "de"))
            .addIdentifikator(
                new Identifikator(
                    "DE-1",
                    "https://sigel.staatsbibliothek-berlin.de/suche/?isil=DE-1",
                    "ISIL"))
            .build();

    NormdatenReferenz aufbewahrungsort =
        new NormdatenReferenzBuilder()
            .withId("ee1611b6-1f56-38e7-8c12-b40684dbb395")
            .withName("Berlin")
            .withTypeName("Place")
            .withGndID("4005728-8")
            .addVarianterName(new VarianterName("Berlino", "it"))
            .addIdentifikator(
                new Identifikator("2950159", "https://www.geonames.org/2950159",
                    "GEONAMES"))
            .build();

    Identifikation identifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdent("Mscr.Dresd.A.111")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(besitzer)
            .withAufbewahrungsOrt(aufbewahrungsort)
            .build();

    KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokumentBuilder()
            .withId(TEIValues.UUID_PREFIX + UUID.randomUUID())
            .withRegistrierungsDatum(LocalDateTime.now())
            .withGueltigerIdentifikation(identifikation)
            .addBeschreibungsdokumentID("HSP123")
            .addBeschreibungsdokumentID("HSPTestBeschreibung")
            .build();

    kulturObjektDokument.setTeiXML(
        TEIObjectFactory.marshal(
            KODTemplateMapper.createTEIFromInitialTemplate(
                kulturObjektDokument)));
    return kulturObjektDokument;
  }

  @Test
  void testUpdateSettlementAndRepositoryIDs() throws Exception {
    KulturObjektDokument kod = createTestKOD();

    Identifikation gueltigeIdentifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(kod.getGueltigeIdentifikation().getBesitzer())
            .withAufbewahrungsOrt(
                kod.getGueltigeIdentifikation().getAufbewahrungsOrt())
            .build();

    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder("HSP123", kod.getId(),
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(tei))
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .build();

    TEIBeschreibungCommand.openTEIDokument(beschreibung);
    TEIBeschreibungCommand.updateSettlementAndRepositoryIDs(beschreibung);
    TEIBeschreibungCommand.writeXML(beschreibung);

    assertTrue(
        beschreibung
            .getTeiXML()
            .contains(
                "<repository ref=\"GND-ID1234\" key=\"774909e2-f687-30cb-a5c4-ddc95806d6be\" rend=\"HAB\">"),
        beschreibung.getTeiXML());

    assertTrue(
        beschreibung
            .getTeiXML()
            .contains(
                "<settlement ref=\"GND-ID3456\" key=\"ee1611b6-1f56-38e7-8c12-b40684dbb395\">"));
  }

  @Test
  void testupdatePersonAsAutoren() throws Exception {
    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder("HSP123", "KOD1",
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(tei))
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .addAutor(
                new NormdatenReferenz.NormdatenReferenzBuilder()
                    .withId("NORM-ID")
                    .withGndID("GNDID")
                    .withName("Hoffmann, Werner")
                    .build())
            .build();
    TEIBeschreibungCommand.openTEIDokument(beschreibung);
    TEIBeschreibungCommand.updatePersonAsAutoren(beschreibung);
    TEIBeschreibungCommand.writeXML(beschreibung);

    assertTrue(
        beschreibung
            .getTeiXML()
            .contains(
                "persName ref=\"http://d-nb.info/gnd/GNDID\" key=\"NORM-ID\" role=\"author\">Hoffmann, Werner</persName>"),
        beschreibung.getTeiXML());
  }

  @Test
  void testupdatePersonAsAutorenWithoutBeschreibungsAutorenRelevanz()
      throws Exception {
    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder("HSP123", "KOD1",
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(tei))
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .build();

    TEIBeschreibungCommand.openTEIDokument(beschreibung);
    TEIBeschreibungCommand.updatePersonAsAutoren(beschreibung);
    TEIBeschreibungCommand.writeXML(beschreibung);

    assertTrue(
        beschreibung.getTeiXML().contains(
            "<persName key=\"NORM-345346\" role=\"author\">Werner Hoffmann</persName>"),
        beschreibung.getTeiXML());
  }

  @Test
  void testUpdateKatalogId() throws Exception {
    KulturObjektDokument kod = createTestKOD();

    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder("HSP123", kod.getId(),
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(tei))
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .build();

    TEIBeschreibungCommand.updateKatalogId(beschreibung, "HSK-123");
    assertTrue(beschreibung.getTeiXML()
        .contains("<ptr type=\"hsp\" target=\"HSK-123\"/>"));

    TEIBeschreibungCommand.updateKatalogId(beschreibung, "HSK-456");
    assertFalse(beschreibung.getTeiXML()
        .contains("<ptr type=\"hsp\" target=\"HSK-123\"/>"));
    assertTrue(beschreibung.getTeiXML()
        .contains("<ptr type=\"hsp\" target=\"HSK-456\"/>"));
  }

  @Test
  void testUpdateVolltext() throws Exception {
    KulturObjektDokument kod = createTestKOD();

    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder("HSP123", kod.getId(),
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(tei))
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .build();

    Div div = new Div();

    P p = new P();
    p.setN("Page32_Block1");
    div.getMeetingsAndBylinesAndDatelines().add(p);

    Pb pb = new Pb();
    pb.setN("Page32");
    p.getContent().add(pb);

    Lb lb = new Lb();
    lb.setN("Page32_Block1_Line1");
    p.getContent().add(lb);

    p.getContent().add("Initialen, nicht oder kaum verziert.");

    TEIBeschreibungCommand.updateVolltext(beschreibung, div);

    assertTrue(beschreibung.getTeiXML().contains("<p n=\"Page32_Block1\">"));
    assertTrue(beschreibung.getTeiXML().contains("<pb n=\"Page32\"/>"));
    assertTrue(
        beschreibung.getTeiXML().contains("<lb n=\"Page32_Block1_Line1\"/>"));
    assertTrue(beschreibung.getTeiXML()
        .contains("Initialen, nicht oder kaum verziert.</p>"));

    TEIBeschreibungCommand.updateVolltext(beschreibung, new Div());

    assertFalse(beschreibung.getTeiXML().contains("<p n=\"Page32_Block1\">"));
    assertFalse(beschreibung.getTeiXML().contains("<pb n=\"Page32\"/>"));
    assertFalse(
        beschreibung.getTeiXML().contains("<lb n=\"Page32_Block1_Line1\"/>"));
    assertFalse(beschreibung.getTeiXML()
        .contains("Initialen, nicht oder kaum verziert.</p>"));
  }

  @Test
  void testupdateKatalogRangeReference() throws Exception {

    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder("HSP123", "HSP-123456",
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(tei_Aurich))
            .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
            .build();
    TEIBeschreibungCommand.updateKatalogReferences(
        new URI(RANGE_URL), beschreibung, new URI(KATALOG_MANIFEST_URL));

    assertTrue(beschreibung.getTeiXML().contains(RANGE_URL));

    assertTrue(beschreibung.getTeiXML().contains(KATALOG_MANIFEST_URL));
  }

  @Test
  void testUpdateKatalogRangeReference_noBiblScope_noBiblN() throws Exception {
    Path teiFilePathNoBiblScope =
        Paths.get("src", "test", "resources", "tei",
            "tei-msdec-noBiblScope.xml");
    TEI tei_noBiblScopeTEI;
    try (InputStream is = newInputStream(teiFilePathNoBiblScope)) {
      tei_noBiblScopeTEI = TEIObjectFactory.unmarshalOne(is).orElseThrow();
    }

    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder("HSP123", "HSP-123456",
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(tei_noBiblScopeTEI))
            .withVerwaltungsTyp(VerwaltungsTyp.EXTERN)
            .build();
    TEIBeschreibungCommand.updateKatalogReferences(
        new URI(RANGE_URL), beschreibung, new URI(KATALOG_MANIFEST_URL));

    assertTrue(beschreibung.getTeiXML().contains(RANGE_URL));

    assertTrue(beschreibung.getTeiXML().contains(KATALOG_MANIFEST_URL));
  }

  @Test
  void testUpdateAenderungsdatum() throws Exception {
    LocalDateTime aenderungsdatum = LocalDateTime.of(2020, Month.FEBRUARY, 2,
        20, 2);

    String teiOriginal = TEIObjectFactory.marshal(tei);
    Beschreibung beschreibung1 =
        new Beschreibung.BeschreibungsBuilder("HSP123", "HSP-123456",
            gueltigeIdentifikation)
            .withTEIXml(teiOriginal)
            .withAenderungsDatum(aenderungsdatum)
            .build();

    Diff beschreibung1Diff =
        DiffBuilder.compare(teiOriginal)
            .withTest(Input.fromString(beschreibung1.getTeiXML()))
            .ignoreComments()
            .withNodeFilter(
                node ->
                    !(node.getNodeName().equals("date")
                        && "2020-02-02 20:02:00".equals(node.getTextContent())))
            .ignoreWhitespace()
            .build();

    assertFalse(beschreibung1Diff.hasDifferences(),
        beschreibung1Diff.toString());

    TEI emptyTEI = new TEI();
    TeiHeader header = new TeiHeader();
    header.setRevisionDesc(new RevisionDesc());
    emptyTEI.setTeiHeader(header);
    Beschreibung beschreibung2 =
        new Beschreibung.BeschreibungsBuilder("HSP456", "HSP-123456",
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(emptyTEI))
            .withAenderungsDatum(aenderungsdatum)
            .build();
    TEIBeschreibungCommand.updateAenderungsdatum(beschreibung2);
    TEIBeschreibungCommand.updateAenderungsdatum(beschreibung2);
    TEIBeschreibungCommand.updateAenderungsdatum(beschreibung2);
    TEIBeschreibungCommand.writeXML(beschreibung2);

    String expected =
        """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <TEI xmlns="http://www.tei-c.org/ns/1.0" xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:dcr="http://www.isocat.org/ns/dcr" xmlns:egXML="http://www.tei-c.org/ns/Examples" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                <teiHeader>
                    <revisionDesc>
                        <change>
                            <date when="2020-02-02">2020-02-02 20:02:00</date>
                        </change>
                    </revisionDesc>
                </teiHeader>
            </TEI>
            """;

    Diff beschreibung2Diff =
        DiffBuilder.compare(expected)
            .withTest(Input.fromString(beschreibung2.getTeiXML()))
            .ignoreWhitespace()
            .ignoreComments()
            .build();

    assertFalse(beschreibung2Diff.hasDifferences(), beschreibung2.getTeiXML());

    LocalDateTime aenderungsdatumSecond = LocalDateTime.of(2025, Month.FEBRUARY, 2,
        20, 2);
    beschreibung2.setAenderungsDatum(aenderungsdatumSecond);
    TEIBeschreibungCommand.updateAenderungsdatum(beschreibung2);
    TEIBeschreibungCommand.writeXML(beschreibung2);

    String expectedSecond =
        """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <TEI xmlns="http://www.tei-c.org/ns/1.0" xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:dcr="http://www.isocat.org/ns/dcr" xmlns:egXML="http://www.tei-c.org/ns/Examples" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                <teiHeader>
                    <revisionDesc>
                        <change>
                            <date when="2025-02-02">2025-02-02 20:02:00</date>
                        </change>
                    </revisionDesc>
                </teiHeader>
            </TEI>
            """;

    Diff beschreibung2DiffSecond =
        DiffBuilder.compare(expectedSecond)
            .withTest(Input.fromString(beschreibung2.getTeiXML()))
            .ignoreWhitespace()
            .ignoreComments()
            .build();

    assertFalse(beschreibung2DiffSecond.hasDifferences(), beschreibung2.getTeiXML());
  }

  @Test
  void testUpdatePublikationsdatumHSP() throws Exception {
    LocalDateTime pubDate = LocalDateTime.of(2020, Month.FEBRUARY, 1, 20, 2);

    Publikation publikation = new Publikation.PublikationBuilder()
        .withPublikationsDatumHSP(pubDate)
        .build();

    Beschreibung beschreibung1 =
        new Beschreibung.BeschreibungsBuilder("HSP123", "HSP-123456",
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(tei))
            .withPublikation(publikation)
            .build();

    assertTrue(
        beschreibung1
            .getTeiXML()
            .contains(
                "<date when=\"2020-02-03\" type=\"secondary\">03.02.2020</date>"),
        beschreibung1.getTeiXML());

    TEIBeschreibungCommand.openTEIDokument(beschreibung1);
    TEIBeschreibungCommand.updatePublikationsdatumHSP(beschreibung1);
    TEIBeschreibungCommand.writeXML(beschreibung1);
    assertTrue(
        beschreibung1
            .getTeiXML()
            .contains(
                "<date when=\"2020-02-01\" type=\"secondary\">2020-02-01 20:02:00</date>"));
    assertFalse(
        beschreibung1
            .getTeiXML()
            .contains(
                "<date when=\"2020-02-03\" type=\"secondary\">03.02.2020</date>"));

    Publikation publikation2 = new Publikation.PublikationBuilder()
        .withPublikationsDatumHSP(pubDate)
        .build();

    Beschreibung beschreibung2 =
        new Beschreibung.BeschreibungsBuilder("HSP456", "HSP-123456",
            gueltigeIdentifikation)
            .withPublikation(publikation2)
            .build();

    TEI minimalTEI = new TEI();
    TeiHeader teiHeader = new TeiHeader();
    minimalTEI.setTeiHeader(teiHeader);
    beschreibung2.setTeiXML(TEIObjectFactory.marshal(minimalTEI));
    TEIBeschreibungCommand.openTEIDokument(beschreibung2);

    String exceptionMessage = "TEI contains no valid PublicationStmt, beschreibungId=HSP456";
    Exception exception =
        assertThrows(
            Exception.class,
            () -> TEIBeschreibungCommand.updatePublikationsdatumHSP(
                beschreibung2));
    assertEquals(exceptionMessage, exception.getMessage());

    FileDesc fileDesc = new FileDesc();
    teiHeader.setFileDesc(fileDesc);
    beschreibung2.setTeiXML(TEIObjectFactory.marshal(minimalTEI));

    exception =
        assertThrows(
            Exception.class,
            () -> TEIBeschreibungCommand.updatePublikationsdatumHSP(
                beschreibung2));
    assertEquals(exceptionMessage, exception.getMessage());

    PublicationStmt publicationStmt = new PublicationStmt();
    fileDesc.setPublicationStmt(publicationStmt);
    beschreibung2.setTeiXML(TEIObjectFactory.marshal(minimalTEI));

    exception =
        assertThrows(
            Exception.class,
            () -> TEIBeschreibungCommand.updatePublikationsdatumHSP(
                beschreibung2));
    assertEquals(exceptionMessage, exception.getMessage());

    Publisher publisher = new Publisher();
    publicationStmt.getPublishersAndDistributorsAndAuthorities().add(publisher);
    beschreibung2.setTeiXML(TEIObjectFactory.marshal(minimalTEI));

    TEIBeschreibungCommand.openTEIDokument(beschreibung2);
    TEIBeschreibungCommand.updatePublikationsdatumHSP(beschreibung2);
    TEIBeschreibungCommand.writeXML(beschreibung2);

    assertTrue(
        beschreibung1
            .getTeiXML()
            .contains(
                "<date when=\"2020-02-01\" type=\"secondary\">2020-02-01 20:02:00</date>"));
    assertFalse(
        beschreibung1
            .getTeiXML()
            .contains(
                "<date when=\"2020-02-03\" type=\"secondary\">03.02.2020</date>"));
  }

  @Test
  void testupdateEntstehungsortID() throws Exception {
    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder("HSP123", "HSP-123456",
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(tei_ODD109))
            .build();
    TEIBeschreibungCommand.openTEIDokument(beschreibung);
    TEIBeschreibungCommand.updateEntstehungsortID(beschreibung,
        List.of(new EntstehungsortParameter("1122121",
            "Kiel", "4030481-4")));
    TEIBeschreibungCommand.writeXML(beschreibung);

    assertTrue(
        beschreibung
            .getTeiXML()
            .contains(
                "<term ref=\"http://dnb.indo/4030481-4\" key=\"1122121\" type=\"origPlace_norm\">Kiel</term>"),
        beschreibung.getTeiXML());

    beschreibung.setTeiXML(TEIObjectFactory.marshal(tei_ODD109));
    TEIBeschreibungCommand.openTEIDokument(beschreibung);
    TEIBeschreibungCommand.updateEntstehungsortID(
        beschreibung, List.of(
            new EntstehungsortParameter("1122121", "Bordesholm", "4007721-4")));
    TEIBeschreibungCommand.writeXML(beschreibung);

    assertTrue(
        beschreibung
            .getTeiXML()
            .contains(
                "<term ref=\"http://d-nb.info/gnd/4007721-4\" key=\"1122121\" type=\"origPlace_norm\">Bordesholm</term>"));
  }

  @Test
  void testUpdatePURLs() throws Exception {
    PURL internalPURL =
        new PURL(URI.create(INTERNAL_PURL), URI.create(BESCHREIBUNG_TARGET_URL),
            PURLTyp.INTERNAL);
    PURL externalPURL =
        new PURL(URI.create(EXTERNAL_PURL), URI.create(BESCHREIBUNG_TARGET_URL),
            PURLTyp.EXTERNAL);

    String ptrInternalOld =
        "<ptr type=\"purl\" subtype=\"hsp\" target=\"https://resolver.staatsbibliothek-berlin.de/HSP1660133922141000\"/>";
    String ptrInternal =
        "<ptr type=\"purl\" subtype=\"hsp\" target=\"https://resolver.staatsbibliothek-berlin.de/HSP1660133922141000\" xml:id=\"HSP-ddd74884-974d-3007-9895-ce8d42201390\"/>";
    String ptrExternal =
        "<ptr type=\"purl\" target=\"https://doi.org/10.1111/hex.12487\" xml:id=\"HSP-5e9f6574-54ef-3ae2-bdf7-7f77565adb41\"/>";
    String ptrExternalExisting =
        "<ptr type=\"purl\" target=\"https://diglib.hab.de/mss/36-23-aug-2f/start.htm\"/>";
    String ptrThumbnail =
        "<ptr type=\"thumbnailForPresentations\" target=\"https://diglib.hab.de/mss/36-23-aug-2f/thumbs/00001.jpg\"/>";

    Publikation publikation = new Publikation.PublikationBuilder()
        .addPURL(internalPURL)
        .addPURL(externalPURL)
        .build();

    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder("HSP-12123123", "HSP-123456",
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(tei))
            .withPublikation(publikation)
            .build();

    assertEquals(
        1,
        StringUtils.countMatches(beschreibung.getTeiXML(), ptrInternalOld),
        beschreibung.getTeiXML());
    assertEquals(
        1,
        StringUtils.countMatches(beschreibung.getTeiXML(), ptrExternalExisting),
        beschreibung.getTeiXML());
    assertEquals(1,
        StringUtils.countMatches(beschreibung.getTeiXML(), ptrThumbnail));

    TEIBeschreibungCommand.openTEIDokument(beschreibung);
    TEIBeschreibungCommand.updatePURLs(beschreibung);
    TEIBeschreibungCommand.writeXML(beschreibung);

    assertEquals(0,
        StringUtils.countMatches(beschreibung.getTeiXML(), ptrInternalOld));
    assertEquals(
        1,
        StringUtils.countMatches(beschreibung.getTeiXML(), ptrInternal),
        beschreibung.getTeiXML());
    assertEquals(
        1,
        StringUtils.countMatches(beschreibung.getTeiXML(), ptrExternal),
        beschreibung.getTeiXML());
    assertEquals(0, StringUtils.countMatches(beschreibung.getTeiXML(),
        ptrExternalExisting));
    assertEquals(1,
        StringUtils.countMatches(beschreibung.getTeiXML(), ptrThumbnail));

    TEIBeschreibungCommand.updatePURLs(beschreibung);

    assertEquals(1,
        StringUtils.countMatches(beschreibung.getTeiXML(), ptrInternal));
    assertEquals(1,
        StringUtils.countMatches(beschreibung.getTeiXML(), ptrExternal));
    assertEquals(0, StringUtils.countMatches(beschreibung.getTeiXML(),
        ptrExternalExisting));
    assertEquals(1,
        StringUtils.countMatches(beschreibung.getTeiXML(), ptrThumbnail));
  }

  @Test
  void testOpenTEIDokument() throws JAXBException, IOException {
    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder("HSP-12123123", "HSP-123456",
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(tei))
            .build();
    TEIBeschreibungCommand.openTEIDokument(beschreibung);
    assertTrue(TEIBeschreibungCommand.TEI_XML.isPresent());
  }

  @Test
  void testCheckKatalogRequirements() {
    Beschreibung beschreibung =
        new BeschreibungsBuilder("B-1", "kod-1", gueltigeIdentifikation)
            .withTEIXml("<TEI/>")
            .build();
    URI rangeIdentifier = URI.create("https://range.identifier");
    URI katalogManifestURI = URI.create("https://katalog.manifest.json");

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                TEIBeschreibungCommand.checkKatalogRequirements(
                    null, beschreibung, katalogManifestURI));
    assertEquals(
        "Update Katalog failed because of missing needed parameter rangeIdentifier for null",
        exception.getMessage());

    beschreibung.setGueltigeIdentifikation(
        new IdentifikationBuilder()
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withIdent("SIG-123")
            .build());

    exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                TEIBeschreibungCommand.checkKatalogRequirements(
                    null, beschreibung, katalogManifestURI));
    assertEquals(
        "Update Katalog failed because of missing needed parameter rangeIdentifier for SIG-123",
        exception.getMessage());

    exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                TEIBeschreibungCommand.checkKatalogRequirements(
                    rangeIdentifier, beschreibung, null));
    assertEquals(
        "Update Katalog failed because of missing needed parameter katalogManifestURI.",
        exception.getMessage());

    exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                TEIBeschreibungCommand.checkKatalogRequirements(
                    rangeIdentifier, null, katalogManifestURI));
    assertEquals(
        "Update Katalog failed because of missing needed parameter beschreibung-tei.",
        exception.getMessage());

    beschreibung.setTeiXML(null);
    exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                TEIBeschreibungCommand.checkKatalogRequirements(
                    rangeIdentifier, beschreibung, katalogManifestURI));
    assertEquals(
        "Update Katalog failed because of missing needed parameter beschreibung-tei.",
        exception.getMessage());
  }

  @Test
  void testUpdateBibl() {
    String katalogManifest = "https://katalog.manifest.json";
    URI katalogManifestURI = URI.create(katalogManifest);
    Bibl bibl_1 = new Bibl();
    Ptr existingPtr = new Ptr();
    existingPtr.setType(PTR_TYPE_HSP);
    existingPtr.getTargets().add("https://old.manifest.json");
    bibl_1.getContent().add(existingPtr);

    TEIBeschreibungCommand.updateBibl(katalogManifestURI, bibl_1);
    assertNotNull(bibl_1.getContent());
    assertTrue(bibl_1.getContent().contains(existingPtr));
    assertEquals(katalogManifest, String.join(",", existingPtr.getTargets()));

    Bibl bibl_2 = new Bibl();
    TEIBeschreibungCommand.updateBibl(katalogManifestURI, bibl_2);

    assertNotNull(bibl_2.getContent());
    assertEquals(1, bibl_2.getContent().size());
    assertTrue(
        bibl_2.getContent().stream()
            .filter(Ptr.class::isInstance)
            .map(Ptr.class::cast)
            .anyMatch(
                ptr ->
                    PTR_TYPE_HSP.equals(ptr.getType())
                        && katalogManifest.equals(
                        String.join(",", ptr.getTargets()))));
  }

  @Test
  void testUpdateBiblScope() {
    String rangeIdentifier = "https://range.identifier";
    URI rangeIdentifierURI = URI.create(rangeIdentifier);

    Bibl bibl_1 = new Bibl();
    BiblScope existingBiblScope = new BiblScope();
    existingBiblScope.getFacs().add("https://old.identifier");
    bibl_1.getContent().add(existingBiblScope);

    TEIBeschreibungCommand.updateBiblScope(rangeIdentifierURI, bibl_1);
    assertNotNull(bibl_1.getContent());
    assertTrue(bibl_1.getContent().contains(existingBiblScope));
    assertEquals(rangeIdentifier,
        String.join(",", existingBiblScope.getFacs()));

    Bibl bibl_2 = new Bibl();
    TEIBeschreibungCommand.updateBiblScope(rangeIdentifierURI, bibl_2);

    assertNotNull(bibl_2.getContent());
    assertEquals(1, bibl_2.getContent().size());
    assertTrue(
        bibl_2.getContent().stream()
            .filter(BiblScope.class::isInstance)
            .map(BiblScope.class::cast)
            .anyMatch(biblScope -> rangeIdentifier.equals(
                String.join(",", biblScope.getFacs()))));
  }

  @Test
  void testUpdateGrundsprachenIDs() throws Exception {
    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder("HSP-12123123", "HSP-123456",
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(tei))
            .addBeschreibungsKomponente(
                new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
                    .addGrundSprache(
                        new NormdatenReferenz.NormdatenReferenzBuilder().withId(
                                "NORM-de")
                            .withGndID("GND_ID_de").withName("deutsch").build())
                    .addGrundSprache(
                        new NormdatenReferenz.NormdatenReferenzBuilder().withId(
                                "NORM-la")
                            .withGndID("GND_ID_la").withName("deutsch").build())
                    .build())
            .build();

    assertNotNull(beschreibung);

    TEIBeschreibungCommand.openTEIDokument(beschreibung);
    TEIBeschreibungCommand.updateGrundsprachenIDs(beschreibung);
    TEIBeschreibungCommand.writeXML(beschreibung);

    assertTrue(beschreibung.getTeiXML().contains("key=\"NORM-de\""));
    assertTrue(beschreibung.getTeiXML().contains("key=\"NORM-la\""));
  }

  @Test
  void testUpdateBeschreibungsIDAndTypeDoesNotDuplicateKodId()
      throws JAXBException, IOException {

    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder(
            "HSP123",
            "HSP-a8b3c8c3-0085-3af5-9886-73fc0609d390",
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(tei))
            .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
            .build();

    String kodId = "HSP-a8b3c8c3-0085-3af5-9886-73fc0609d390";

    assertTrue(beschreibung.getTeiXML().contains(kodId));
    assertEquals(
        1,
        StringUtils.countMatches(beschreibung.getTeiXML(), kodId));

    assertEquals(
        1,
        StringUtils.countMatches(
            beschreibung.getTeiXML(),
            "<altIdentifier type=\"hsp-ID\">"));

    TEIBeschreibungCommand.openTEIDokument(beschreibung);
    TEIBeschreibungCommand.updateBeschreibungsIDAndType(beschreibung);
    TEIBeschreibungCommand.writeXML(beschreibung);

    assertTrue(beschreibung.getTeiXML().contains(kodId));
    assertEquals(
        1,
        StringUtils.countMatches(beschreibung.getTeiXML(), kodId));

    assertEquals(
        1,
        StringUtils.countMatches(
            beschreibung.getTeiXML(),
            "<altIdentifier type=\"hsp-ID\">"));

    assertTrue(beschreibung.getTeiXML().contains("type=\"hsp:description\""));
    assertTrue(beschreibung.getTeiXML().contains("status=\"intern\""));
    assertTrue(beschreibung.getTeiXML().contains("id=\"HSP123\""));

    assertTrue(
        beschreibung.getTeiXML()
            .contains("<idno>mss_36-23-aug-2f_tei-msDesc_Westphal</idno>"));
  }
}
