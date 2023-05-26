/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.mapper.tei;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.PTR_TYPE_HSP;
import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.KODTemplateMapper;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.JAXBException;
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
  public static final String KATALOG_MANIFEST_URL = "https://iiif.ub.uni-leipzig.de/0000034537/manifest.json";
  public static final String INTERNAL_PURL = "https://resolver.staatsbibliothek-berlin.de/HSP1660133922141000";
  public static final String BESCHREIBUNG_TARGET_URL = "https://handschriftenportal.de//search?hspobjectid=HSP-00b9f2b8-f1df-32b8-9ccc-2d76cfe17d42";
  public static final String EXTERNAL_PURL = "https://doi.org/10.1111/hex.12487";

  static TEI tei;
  static TEI tei_retro;
  static TEI tei_Aurich;
  static TEI tei_ODD109;

  @BeforeAll
  static void init() throws Exception {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei", "tei-msDesc_Westphal.xml");
    Path teiFilePathRetro = Paths.get("src", "test", "resources", "tei",
        "tei-msDesc_Westphal_Retro.xml");
    Path teiFilePathAurich = Paths.get("src", "test", "resources", "tei",
        "tei-msDesc_Aurich.xml");
    Path tei_ODD109Path = Paths.get("src", "test", "resources", "tei",
        "tei_odd109.xml");
    try (InputStream is = newInputStream(teiFilePath)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      tei = teiList.get(0);
    }

    try (InputStream is = newInputStream(teiFilePathRetro)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      tei_retro = teiList.get(0);
    }
    try (InputStream is = newInputStream(teiFilePathAurich)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      tei_Aurich = teiList.get(0);
    }
    try (InputStream is = newInputStream(tei_ODD109Path)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      tei_ODD109 = teiList.get(0);
    }
  }

  @Test
  void testUpdateBeschreibungRetroIDAndType() throws JAXBException {
    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP123")
        .withTEIXml(TEIObjectFactory.marshal(tei_retro))
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .withKodID("HSP-123456")
        .build();

    TEIBeschreibungCommand.updateBeschreibungsIDAndType(beschreibung);
    assertTrue(beschreibung.getTeiXML().contains("type=\"hsp:description_retro\""));
  }

  @Test
  void testupdateBeschreibungsIDAndType() throws JAXBException {

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP123")
        .withTEIXml(TEIObjectFactory.marshal(tei))
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .withKodID("HSP-123456")
        .build();

    TEIBeschreibungCommand.updateBeschreibungsIDAndType(beschreibung);

    assertTrue(beschreibung.getTeiXML().contains("type=\"hsp:description\""));

    assertTrue(beschreibung.getTeiXML().contains("status=\"intern\""));

    assertTrue(beschreibung.getTeiXML().contains("id=\"HSP123\""));

    assertTrue(beschreibung.getTeiXML()
        .contains("<idno>mss_36-23-aug-2f_tei-msDesc_Westphal</idno>"));

    assertTrue(beschreibung.getTeiXML()
        .contains("<idno>HSP-123456</idno>"));

    assertTrue(beschreibung.getTeiXML()
        .contains("<altIdentifier type=\"hsp-ID\">"));

    assertTrue(beschreibung.getTeiXML()
        .contains("<collection>" + TEIValues.KOD_ID_COLLECTION + "</collection>"));
  }

  @Test
  void testupdateKODBeschreibungenReferenzes() throws Exception {

    KulturObjektDokument kulturObjektDokument = createTestKOD();

    TEIKulturObjektDokumentCommand.updateKODBeschreibungenReferenzes(kulturObjektDokument);

    assertTrue(kulturObjektDokument.getTeiXML().replaceAll("\\s", "")
            .contains(
                "<listBibl><bibl><reftarget=\"HSP123\"/></bibl><bibl><reftarget=\"HSPTestBeschreibung\"/></bibl></listBibl>"),
        kulturObjektDokument.getTeiXML().replaceAll("\\s", ""));
  }

  private KulturObjektDokument createTestKOD() throws Exception {
    NormdatenReferenz besitzer = new NormdatenReferenzBuilder()
        .withId("774909e2-f687-30cb-a5c4-ddc95806d6be")
        .withName("Stiftung Preußischer Kulturbesitz. Staatsbibliothek zu Berlin")
        .withTypeName("CorporateBody")
        .withGndID("4005728-8")
        .addVarianterName(
            new VarianterName("Stiftung Preußischer Kulturbesitz. Staatsbibliothek zu Berlin",
                "de"))
        .addIdentifikator(
            new Identifikator("DE-1", "https://sigel.staatsbibliothek-berlin.de/suche/?isil=DE-1",
                "ISIL"))
        .build();

    NormdatenReferenz aufbewahrungsort = new NormdatenReferenzBuilder()
        .withId("ee1611b6-1f56-38e7-8c12-b40684dbb395")
        .withName("Berlin")
        .withTypeName("Place")
        .withGndID("4005728-8")
        .addVarianterName(new VarianterName("Berlino", "it"))
        .addIdentifikator(
            new Identifikator("2950159", "https://www.geonames.org/2950159", "GEONAMES"))
        .build();

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(aufbewahrungsort)
        .build();

    KulturObjektDokument kulturObjektDokument = new KulturObjektDokumentBuilder()
        .withId(TEIValues.UUID_PREFIX + UUID.randomUUID())
        .withRegistrierungsDatum(LocalDateTime.now())
        .withGueltigerIdentifikation(identifikation)
        .addBeschreibungsdokumentID("HSP123")
        .addBeschreibungsdokumentID("HSPTestBeschreibung")
        .build();

    kulturObjektDokument
        .setTeiXML(TEIObjectFactory
            .marshal(KODTemplateMapper.createTEIFromInitialTemplate(kulturObjektDokument)));
    return kulturObjektDokument;
  }

  @Test
  void testUpdateSettlementAndRepositoryIDs() throws Exception {
    KulturObjektDokument kod = createTestKOD();

    Identifikation gueltigeIdentifikation = new Identifikation.IdentifikationBuilder()
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(kod.getGueltigeIdentifikation().getBesitzer())
        .withAufbewahrungsOrt(kod.getGueltigeIdentifikation().getAufbewahrungsOrt())
        .build();

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .build();
    kopf.getIdentifikationen().add(gueltigeIdentifikation);

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP123")
        .withTEIXml(TEIObjectFactory.marshal(tei))
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .withKodID(kod.getId())
        .addBeschreibungsKomponente(kopf)
        .build();

    TEIBeschreibungCommand.updateSettlementAndRepositoryIDs(beschreibung, kod);

    NormdatenReferenz kodBesitzer = kod.getGueltigeIdentifikation().getBesitzer();
    NormdatenReferenz beschreibungBesitzer = beschreibung.getGueltigeIdentifikation()
        .map(Identifikation::getBesitzer)
        .orElse(null);
    assertNormdatenReferenzEqual(kodBesitzer, beschreibungBesitzer);

    NormdatenReferenz kodAufbewahrungsort = kod.getGueltigeIdentifikation().getBesitzer();
    NormdatenReferenz beschreibungAufbewahrungsort = kod.getGueltigeIdentifikation().getBesitzer();
    assertNormdatenReferenzEqual(kodAufbewahrungsort, beschreibungAufbewahrungsort);

    assertEquals(kod.getGueltigeIdentifikation().getAufbewahrungsOrt().getId(),
        beschreibung.getGueltigeIdentifikation().get().getAufbewahrungsOrt().getId());

    assertTrue(beschreibung.getTeiXML()
        .contains(
            "<repository key=\"774909e2-f687-30cb-a5c4-ddc95806d6be\" ref=\"1234\" rend=\"HAB\">"));

    assertTrue(beschreibung.getTeiXML()
        .contains("<settlement key=\"ee1611b6-1f56-38e7-8c12-b40684dbb395\">"));
  }

  @Test
  void testUpdateKatalogId() throws Exception {
    KulturObjektDokument kod = createTestKOD();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP123")
        .withTEIXml(TEIObjectFactory.marshal(tei))
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .withKodID(kod.getId())
        .build();

    TEIBeschreibungCommand.updateKatalogId(beschreibung, "HSK-123");
    assertTrue(beschreibung.getTeiXML()
        .contains("<ptr target=\"HSK-123\" type=\"hsp\"/>"));

    TEIBeschreibungCommand.updateKatalogId(beschreibung, "HSK-456");
    assertFalse(beschreibung.getTeiXML()
        .contains("<ptr target=\"HSK-123\" type=\"hsp\"/>"));
    assertTrue(beschreibung.getTeiXML()
        .contains("<ptr target=\"HSK-456\" type=\"hsp\"/>"));
  }

  @Test
  void testUpdateVolltext() throws Exception {
    KulturObjektDokument kod = createTestKOD();

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP123")
        .withTEIXml(TEIObjectFactory.marshal(tei))
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .withKodID(kod.getId())
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
    assertTrue(beschreibung.getTeiXML().contains("<lb n=\"Page32_Block1_Line1\"/>"));
    assertTrue(beschreibung.getTeiXML().contains("Initialen, nicht oder kaum verziert.</p>"));

    TEIBeschreibungCommand.updateVolltext(beschreibung, new Div());

    assertFalse(beschreibung.getTeiXML().contains("<p n=\"Page32_Block1\">"));
    assertFalse(beschreibung.getTeiXML().contains("<pb n=\"Page32\"/>"));
    assertFalse(beschreibung.getTeiXML().contains("<lb n=\"Page32_Block1_Line1\"/>"));
    assertFalse(beschreibung.getTeiXML().contains("Initialen, nicht oder kaum verziert.</p>"));
  }

  @Test
  void testupdateKatalogRangeReference() throws Exception {

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP123")
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
    Path teiFilePathNoBiblScope = Paths.get("src", "test", "resources", "tei",
        "tei-msdec-noBiblScope.xml");
    TEI tei_noBiblScopeTEI;
    try (InputStream is = newInputStream(teiFilePathNoBiblScope)) {
      tei_noBiblScopeTEI = TEIObjectFactory.unmarshalOne(is).orElseThrow();
    }

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP123")
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
    LocalDateTime aenderungsdatum = LocalDateTime.of(2020, Month.FEBRUARY, 2, 20, 2);

    String teiOriginal = TEIObjectFactory.marshal(tei);
    Beschreibung beschreibung1 = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP123")
        .withTEIXml(teiOriginal)
        .withAenderungsDatum(aenderungsdatum)
        .build();

    Diff beschreibung1Diff = DiffBuilder.compare(teiOriginal)
        .withTest(Input.fromString(beschreibung1.getTeiXML()))
        .ignoreComments()
        .withNodeFilter(
            node -> !(node.getNodeName().equals("date") && "2020-02-02 20:02:00".equals(node.getTextContent())))
        .ignoreWhitespace()
        .build();

    assertFalse(beschreibung1Diff.hasDifferences(), beschreibung1Diff.toString());

    TEI emptyTEI = new TEI();
    TeiHeader header = new TeiHeader();
    header.setRevisionDesc(new RevisionDesc());
    emptyTEI.setTeiHeader(header);
    Beschreibung beschreibung2 = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP456")
        .withTEIXml(TEIObjectFactory.marshal(emptyTEI))
        .withAenderungsDatum(aenderungsdatum)
        .build();

    TEIBeschreibungCommand.updateAenderungsdatum(beschreibung2);
    TEIBeschreibungCommand.updateAenderungsdatum(beschreibung2);
    TEIBeschreibungCommand.updateAenderungsdatum(beschreibung2);

    String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
        + "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:dcr=\"http://www.isocat.org/ns/dcr\" xmlns:egXML=\"http://www.tei-c.org/ns/Examples\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
        + "    <teiHeader>\n"
        + "        <revisionDesc>\n"
        + "            <change>\n"
        + "                <date when=\"2020-02-02\">2020-02-02 20:02:00</date>\n"
        + "            </change>\n"
        + "        </revisionDesc>\n"
        + "    </teiHeader>\n"
        + "</TEI>";

    Diff beschreibung2Diff = DiffBuilder.compare(expected)
        .withTest(Input.fromString(beschreibung2.getTeiXML()))
        .ignoreComments()
        .build();

    assertFalse(beschreibung2Diff.hasDifferences(), beschreibung2.getTeiXML());
  }

  @Test
  void testUpdatePublikationsdatumHSP() throws Exception {
    LocalDateTime pubDate = LocalDateTime.of(2020, Month.FEBRUARY, 1, 20, 2);

    Beschreibung beschreibung1 = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP123")
        .withTEIXml(TEIObjectFactory.marshal(tei))
        .addPublikation(new Publikation("123", pubDate, PublikationsTyp.PUBLIKATION_HSP))
        .build();

    assertTrue(beschreibung1.getTeiXML()
        .contains("<date when=\"2020-02-03\" type=\"secondary\">03.02.2020</date>"));

    TEIBeschreibungCommand.updatePublikationsdatumHSP(beschreibung1);
    assertTrue(beschreibung1.getTeiXML()
        .contains("<date when=\"2020-02-01\" type=\"secondary\">2020-02-01 20:02:00</date>"));
    assertFalse(beschreibung1.getTeiXML()
        .contains("<date when=\"2020-02-03\" type=\"secondary\">03.02.2020</date>"));

    Beschreibung beschreibung2 = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP456")
        .addPublikation(new Publikation("123", pubDate, PublikationsTyp.PUBLIKATION_HSP))
        .build();

    TEI minimalTEI = new TEI();
    TeiHeader teiHeader = new TeiHeader();
    minimalTEI.setTeiHeader(teiHeader);
    beschreibung2.setTeiXML(TEIObjectFactory.marshal(minimalTEI));

    String exceptionMessage = "TEI contains no valid PublicationStmt, beschreibungId=HSP456";
    Exception exception = assertThrows(Exception.class,
        () -> TEIBeschreibungCommand.updatePublikationsdatumHSP(beschreibung2));
    assertEquals(exceptionMessage, exception.getMessage());

    FileDesc fileDesc = new FileDesc();
    teiHeader.setFileDesc(fileDesc);
    beschreibung2.setTeiXML(TEIObjectFactory.marshal(minimalTEI));

    exception = assertThrows(Exception.class,
        () -> TEIBeschreibungCommand.updatePublikationsdatumHSP(beschreibung2));
    assertEquals(exceptionMessage, exception.getMessage());

    PublicationStmt publicationStmt = new PublicationStmt();
    fileDesc.setPublicationStmt(publicationStmt);
    beschreibung2.setTeiXML(TEIObjectFactory.marshal(minimalTEI));

    exception = assertThrows(Exception.class,
        () -> TEIBeschreibungCommand.updatePublikationsdatumHSP(beschreibung2));
    assertEquals(exceptionMessage, exception.getMessage());

    Publisher publisher = new Publisher();
    publicationStmt.getPublishersAndDistributorsAndAuthorities().add(publisher);
    beschreibung2.setTeiXML(TEIObjectFactory.marshal(minimalTEI));

    TEIBeschreibungCommand.updatePublikationsdatumHSP(beschreibung2);
    assertTrue(beschreibung1.getTeiXML()
        .contains("<date when=\"2020-02-01\" type=\"secondary\">2020-02-01 20:02:00</date>"));
    assertFalse(beschreibung1.getTeiXML()
        .contains("<date when=\"2020-02-03\" type=\"secondary\">03.02.2020</date>"));
  }

  @Test
  void testupdateEntstehungsortID() throws Exception {
    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP123")
        .withTEIXml(TEIObjectFactory.marshal(tei_ODD109))
        .build();
    TEIBeschreibungCommand.updateEntstehungsortID(beschreibung, "GND123", "Berlin");

    assertTrue(
        beschreibung.getTeiXML()
            .contains(
                "<term key=\"GND123\" ref=\"http://d-nb.info/gnd/4007721-4\" type=\"origPlace_norm\">Berlin</term>"));
  }

  private void assertNormdatenReferenzEqual(NormdatenReferenz expected, NormdatenReferenz actual) {
    assertNotNull(expected);
    assertNotNull(actual);

    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getTypeName(), actual.getTypeName());
    assertEquals(expected.getName(), actual.getName());
    assertEquals(expected.getGndID(), actual.getGndID());
    assertIterableEquals(expected.getIdentifikator(), actual.getIdentifikator());
    assertIterableEquals(expected.getVarianterName(), actual.getVarianterName());
  }

  @Test
  void testUpdatePURLs() throws Exception {
    PURL internalPURL = new PURL(URI.create(INTERNAL_PURL), URI.create(BESCHREIBUNG_TARGET_URL), PURLTyp.INTERNAL);
    PURL externalPURL = new PURL(URI.create(EXTERNAL_PURL), URI.create(BESCHREIBUNG_TARGET_URL), PURLTyp.EXTERNAL);

    String ptrInternalOld = "<ptr target=\"https://resolver.staatsbibliothek-berlin.de/HSP1660133922141000\" subtype=\"hsp\" type=\"purl\"/>";
    String ptrInternal = "<ptr xml:id=\"HSP-ddd74884-974d-3007-9895-ce8d42201390\" target=\"https://resolver.staatsbibliothek-berlin.de/HSP1660133922141000\" subtype=\"hsp\" type=\"purl\"/>";
    String ptrExternal = "<ptr xml:id=\"HSP-5e9f6574-54ef-3ae2-bdf7-7f77565adb41\" target=\"https://doi.org/10.1111/hex.12487\" type=\"purl\"/>";
    String ptrExternalExisting = "<ptr target=\"https://diglib.hab.de/mss/36-23-aug-2f/start.htm\" type=\"purl\"/>";
    String ptrThumbnail = "<ptr target=\"https://diglib.hab.de/mss/36-23-aug-2f/thumbs/00001.jpg\" type=\"thumbnailForPresentations\"/>";

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP-12123123")
        .withTEIXml(TEIObjectFactory.marshal(tei))
        .addPURL(internalPURL)
        .addPURL(externalPURL)
        .build();

    assertEquals(1, StringUtils.countMatches(beschreibung.getTeiXML(), ptrInternalOld));
    assertEquals(1, StringUtils.countMatches(beschreibung.getTeiXML(), ptrExternalExisting));
    assertEquals(1, StringUtils.countMatches(beschreibung.getTeiXML(), ptrThumbnail));

    TEIBeschreibungCommand.updatePURLs(beschreibung);

    assertEquals(0, StringUtils.countMatches(beschreibung.getTeiXML(), ptrInternalOld));
    assertEquals(1, StringUtils.countMatches(beschreibung.getTeiXML(), ptrInternal));
    assertEquals(1, StringUtils.countMatches(beschreibung.getTeiXML(), ptrExternal));
    assertEquals(0, StringUtils.countMatches(beschreibung.getTeiXML(), ptrExternalExisting));
    assertEquals(1, StringUtils.countMatches(beschreibung.getTeiXML(), ptrThumbnail));

    TEIBeschreibungCommand.updatePURLs(beschreibung);

    assertEquals(1, StringUtils.countMatches(beschreibung.getTeiXML(), ptrInternal));
    assertEquals(1, StringUtils.countMatches(beschreibung.getTeiXML(), ptrExternal));
    assertEquals(0, StringUtils.countMatches(beschreibung.getTeiXML(), ptrExternalExisting));
    assertEquals(1, StringUtils.countMatches(beschreibung.getTeiXML(), ptrThumbnail));
  }

  @Test
  void testOpenTEIDokument() throws JAXBException, IOException {
    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP-12123123")
        .withTEIXml(TEIObjectFactory.marshal(tei))
        .build();

    assertTrue(TEIBeschreibungCommand.openTEIDokument(beschreibung).isPresent());
  }

  @Test
  void testCheckKatalogRequirements() {
    Beschreibung beschreibung = new BeschreibungsBuilder().withId("B-1").withTEIXml("<TEI/>").build();
    URI rangeIdentifier = URI.create("https://range.identifier");
    URI katalogManifestURI = URI.create("https://katalog.manifest.json");

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> TEIBeschreibungCommand.checkKatalogRequirements(null, beschreibung, katalogManifestURI));
    assertEquals("Update Katalog failed because of missing needed parameter rangeIdentifier for .",
        exception.getMessage());

    beschreibung.getBeschreibungsStruktur().add(new BeschreibungsKomponenteKopfBuilder()
        .withIndentifikationen(new HashSet<>(List.of(
            new IdentifikationBuilder().withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
                .withIdent("SIG-123").build())))
        .build());

    exception = assertThrows(IllegalArgumentException.class,
        () -> TEIBeschreibungCommand.checkKatalogRequirements(null, beschreibung, katalogManifestURI));
    assertEquals("Update Katalog failed because of missing needed parameter rangeIdentifier for SIG-123",
        exception.getMessage());

    exception = assertThrows(IllegalArgumentException.class,
        () -> TEIBeschreibungCommand.checkKatalogRequirements(rangeIdentifier, beschreibung, null));
    assertEquals("Update Katalog failed because of missing needed parameter katalogManifestURI.",
        exception.getMessage());

    exception = assertThrows(IllegalArgumentException.class,
        () -> TEIBeschreibungCommand.checkKatalogRequirements(rangeIdentifier, null, katalogManifestURI));
    assertEquals("Update Katalog failed because of missing needed parameter beschreibung-tei.",
        exception.getMessage());

    beschreibung.setTeiXML(null);
    exception = assertThrows(IllegalArgumentException.class,
        () -> TEIBeschreibungCommand.checkKatalogRequirements(rangeIdentifier, beschreibung, katalogManifestURI));
    assertEquals("Update Katalog failed because of missing needed parameter beschreibung-tei.",
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
    assertTrue(bibl_2.getContent().stream()
        .filter(Ptr.class::isInstance)
        .map(Ptr.class::cast)
        .anyMatch(ptr -> PTR_TYPE_HSP.equals(ptr.getType())
            && katalogManifest.equals(String.join(",", ptr.getTargets()))));
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
    assertEquals(rangeIdentifier, String.join(",", existingBiblScope.getFacs()));

    Bibl bibl_2 = new Bibl();
    TEIBeschreibungCommand.updateBiblScope(rangeIdentifierURI, bibl_2);

    assertNotNull(bibl_2.getContent());
    assertEquals(1, bibl_2.getContent().size());
    assertTrue(bibl_2.getContent().stream()
        .filter(BiblScope.class::isInstance)
        .map(BiblScope.class::cast)
        .anyMatch(biblScope ->
            rangeIdentifier.equals(String.join(",", biblScope.getFacs()))));
  }
}
