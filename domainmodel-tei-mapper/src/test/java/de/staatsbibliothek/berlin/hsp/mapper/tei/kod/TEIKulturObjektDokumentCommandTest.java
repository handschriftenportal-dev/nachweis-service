package de.staatsbibliothek.berlin.hsp.mapper.tei.kod;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.kod.TEIKulturObjektDokumentCommand.updateIndentInMsIdentifier;
import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DigitalisatTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.tei_c.ns._1.Additional;
import org.tei_c.ns._1.Bibl;
import org.tei_c.ns._1.ListBibl;
import org.tei_c.ns._1.MsIdentifier;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 06.10.2020.
 * @version 1.0
 */
@Execution(ExecutionMode.SAME_THREAD)
class TEIKulturObjektDokumentCommandTest {

  public static final String PURL_INTERNAL =
      "https://resolver.staatsbibliothek-berlin.de/HSP1660133922141000";
  public static final String KOD_TARGET_URL =
      "http://b-dev1047:8080/search?hspobjectid=HSP-00b9f2b8-f1df-32b8-9ccc-2d76cfe17d42";
  public static final String PURL_EXTERNAL = "https://doi.org/10.1111/hex.12487";
  private static final Identifikation gueltigeIdentifikation =
      new Identifikation.IdentifikationBuilder()
          .withId("HSP-0037fea7-58e0-3df9-a45c-4sdf3424")
          .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
          .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
          .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
          .build();
  static TEI kodTEI;

  @BeforeAll
  static void init() throws Exception {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei",
        "tei-HSP-kodms1.xml");
    try (InputStream is = newInputStream(teiFilePath)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      kodTEI = teiList.getFirst();
    }
  }

  private static List<Bibl> findBiblsByListType(TEI tei,
      String listType) throws Exception {

    return TEICommon.findFirst(
            Additional.class,
            tei)
        .stream()
        .map(Additional::getListBibl)
        .filter(Objects::nonNull)
        .flatMap(
            root ->
                root.getMilestonesAndGbsAndPbs()
                    .stream())
        .filter(ListBibl.class::isInstance)
        .map(ListBibl.class::cast)
        .filter(
            listBibl ->
                listType.equals(
                    listBibl.getType()))
        .flatMap(
            listBibl ->
                listBibl
                    .getMilestonesAndGbsAndPbs()
                    .stream())
        .filter(Bibl.class::isInstance)
        .map(Bibl.class::cast)
        .toList();
  }

  @Test
  void testUpdateKODSourceDescWith2BeschreibungMsDesc() throws Exception {

    Path teiFilePath = Paths.get("src", "test", "resources", "tei",
        "tei-msDesc_Westphal.xml");
    List<TEI> teiList = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));
    TEI teiBeschreibung1 = teiList.getFirst();

    Path teiFilePath2 = Paths.get("src", "test", "resources", "tei",
        "tei-msDesc.xml");
    List<TEI> teiList2 = TEIObjectFactory.unmarshal(
        newInputStream(teiFilePath2));
    TEI teiBeschreibung2 = teiList2.getFirst();

    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder("HSP123", "KOD_1",
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(teiBeschreibung1))
            .build();

    Beschreibung beschreibung2 =
        new Beschreibung.BeschreibungsBuilder("HSP123123123", "KOD_1",
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(teiBeschreibung2))
            .build();

    KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("HSP-12123123")
            .withTEIXml(TEIObjectFactory.marshal(kodTEI))
            .withBeschreibungsdokumentIDs(
                Set.of(beschreibung.getId(), beschreibung2.getId()))
            .build();

    TEIKulturObjektDokumentCommand.updateKODSourceDescWithAddBeschreibungMsDesc(
        kulturObjektDokument, beschreibung);
    TEIKulturObjektDokumentCommand.updateKODSourceDescWithAddBeschreibungMsDesc(
        kulturObjektDokument, beschreibung2);

    String updatedTEIXML = kulturObjektDokument.getTeiXML();
    final String kodMSDesc =
        """
            <msDesc type="hsp:object" xml:id="HSP-36a88bd1-987b-4ac9-8c67-212a43681b02" xml:lang="de">""";
    final String beschreibungMsDesc =
        """
            <msDesc type="hsp:description" xml:id="mss_36-23-aug-2f_tei-msDesc_Westphal" xml:lang="de">""";
    assertTrue(updatedTEIXML.contains(kodMSDesc), updatedTEIXML);
    assertTrue(updatedTEIXML.contains(beschreibungMsDesc), updatedTEIXML);
    assertFalse(updatedTEIXML.contains(
        "<msDesc type=\"hsp:description\" xml:id=\"mss_36-23-aug-2f_tei-msDesc\""));
  }

  @Test
  void testUpdateKODBeschreibungenReferenzesWithSingleDescription() throws Exception {

    Path teiFilePath = Paths.get("src", "test", "resources", "tei",
        "tei-msDesc_Westphal.xml");
    List<TEI> teiList = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));
    TEI teiBeschreibung1 = teiList.getFirst();

    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder("HSP123", "KOD_1",
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(teiBeschreibung1))
            .build();

    KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("HSP-12123123")
            .withTEIXml(TEIObjectFactory.marshal(kodTEI))
            .withBeschreibungsdokumentIDs(Set.of(beschreibung.getId()))
            .build();

    TEIKulturObjektDokumentCommand.updateKODBeschreibungenReferenzes(
        kulturObjektDokument);

    String updatedTEIXML = kulturObjektDokument.getTeiXML();
    assertTrue(updatedTEIXML.contains("<ref target=\"HSP123\"/>"),
        updatedTEIXML);

    TEI updatedTei = TEIKulturObjektDokumentCommand
        .openTEIDokument(kulturObjektDokument)
        .orElseThrow();

    List<Bibl> descriptionBibls = findBiblsByListType(updatedTei,
        TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS);

    assertEquals(1, descriptionBibls.size());
    assertEquals("true", descriptionBibls.getFirst().getDefault());

    long defaultCount =
        descriptionBibls.stream()
            .filter(
                bibl ->
                    "true".equals(
                        bibl.getDefault()))
            .count();

    assertEquals(1, defaultCount);
  }

  @Test
  void testUpdateKODBeschreibungenReferenzesWithMultipleDescriptions()
      throws Exception {

    KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("HSP-multiple-descriptions")
            .withTEIXml(
                TEIObjectFactory.marshal(kodTEI))
            .withBeschreibungsdokumentIDs(
                Set.of(
                    "HSP-description-1",
                    "HSP-description-2",
                    "HSP-description-3"))
            .build();

    TEIKulturObjektDokumentCommand.updateKODBeschreibungenReferenzes(
        kulturObjektDokument);

    String updatedTEIXML = kulturObjektDokument.getTeiXML();

    assertTrue(updatedTEIXML.contains("<ref target=\"HSP-description-1\"/>"),
        updatedTEIXML);
    assertTrue(updatedTEIXML.contains("<ref target=\"HSP-description-2\"/>"),
        updatedTEIXML);
    assertTrue(updatedTEIXML.contains("<ref target=\"HSP-description-3\"/>"),
        updatedTEIXML);

    TEI updatedTei = TEIKulturObjektDokumentCommand
        .openTEIDokument(kulturObjektDokument)
        .orElseThrow();

    List<Bibl> descriptionBibls = findBiblsByListType(updatedTei,
        TEIValues.LIST_BIBL_TYPE_DESCRIPTIONS);

    assertEquals(3, descriptionBibls.size());
    assertEquals("true", descriptionBibls.getFirst().getDefault());

    long defaultCount =
        descriptionBibls.stream()
            .filter(
                bibl ->
                    "true".equals(
                        bibl.getDefault()))
            .count();

    assertEquals(1, defaultCount);
  }

  @Test
  void testUpdateKODSourceDescWithRemoveBeschreibungMsDesc() throws Exception {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei",
        "tei-msDesc_Westphal.xml");
    List<TEI> teiList = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));
    TEI teiBeschreibung1 = teiList.getFirst();

    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder(
            "mss_36-23-aug-2f_tei-msDesc_Westphal", "KOD_1",
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(teiBeschreibung1))
            .build();

    KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("HSP-12123123")
            .withTEIXml(TEIObjectFactory.marshal(kodTEI))
            .withBeschreibungsdokumentIDs(Set.of(beschreibung.getId()))
            .build();

    TEIKulturObjektDokumentCommand.updateKODSourceDescWithAddBeschreibungMsDesc(
        kulturObjektDokument, beschreibung);

    String updatedTEIXML = kulturObjektDokument.getTeiXML();

    final String kodMSDesc =
        "<msDesc type=\"hsp:object\" xml:id=\"HSP-36a88bd1-987b-4ac9-8c67-212a43681b02\" xml:lang=\"de\">";
    assertTrue(updatedTEIXML.contains(kodMSDesc), updatedTEIXML);
    final String beschreibungMsDesc =
        "<msDesc type=\"hsp:description\" xml:id=\"mss_36-23-aug-2f_tei-msDesc_Westphal\" xml:lang=\"de\">";
    assertTrue(updatedTEIXML.contains(beschreibungMsDesc), updatedTEIXML);

    TEIKulturObjektDokumentCommand.updateKODSourceDescWithRemoveBeschreibungMsDesc(
        kulturObjektDokument, "not_exists");

    updatedTEIXML = kulturObjektDokument.getTeiXML();

    assertTrue(updatedTEIXML.contains(kodMSDesc));
    assertTrue(updatedTEIXML.contains(beschreibungMsDesc));

    TEIKulturObjektDokumentCommand.updateKODSourceDescWithRemoveBeschreibungMsDesc(
        kulturObjektDokument, beschreibung.getId());

    updatedTEIXML = kulturObjektDokument.getTeiXML();

    assertTrue(updatedTEIXML.contains(kodMSDesc));
    assertFalse(updatedTEIXML.contains(beschreibungMsDesc));
  }

  @Test
  void testUpdateKODSourceDescWithBeschreibungMsDesc() throws Exception {
    final String kodMSDesc =
        """
            <msDesc type="hsp:object" xml:id="HSP-36a88bd1-987b-4ac9-8c67-212a43681b02" xml:lang="de">""";
    final String beschreibungMsDesc =
        """
            <msDesc type="hsp:description" xml:id="mss_36-23-aug-2f_tei-msDesc_Westphal" xml:lang="de">""";
    Path teiFilePath = Paths.get("src", "test", "resources", "tei",
        "tei-msDesc_Westphal.xml");
    List<TEI> teiList = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));
    TEI teiBeschreibung = teiList.getFirst();

    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder("HSP123", "KOD_1",
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(teiBeschreibung))
            .build();

    KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("HSP-12123123")
            .withTEIXml(TEIObjectFactory.marshal(kodTEI))
            .build();

    TEIKulturObjektDokumentCommand.updateKODSourceDescWithAddBeschreibungMsDesc(
        kulturObjektDokument, beschreibung);

    String updatedTEIXML = kulturObjektDokument.getTeiXML();

    assertTrue(updatedTEIXML.contains(kodMSDesc));
    assertTrue(updatedTEIXML.contains(beschreibungMsDesc));
  }

  @Test
  void testUpdateSurrogates() throws Exception {
    Digitalisat digitalisat =
        Digitalisat.DigitalisatBuilder()
            .withID("1")
            .withManifest(
                "https://iiif.ub.uni-leipzig.de/0000009089/manifest.json")
            .withEntstehungsort(
                new NormdatenReferenz("123", "Berlin", "GDN123"))
            .withDigitalisierendeEinrichtung(
                new NormdatenReferenz("456", "Staatsbibliothek zu Berlin",
                    "GND-X123"))
            .withDigitalisierungsdatum(
                LocalDate.parse("06.10.2020",
                    DateTimeFormatter.ofPattern("dd.MM.yyyy")))
            .withTyp(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL)
            .build();

    KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("HSP-12123123")
            .addDigitalisat(digitalisat)
            .withTEIXml(TEIObjectFactory.marshal(kodTEI))
            .build();

    TEIKulturObjektDokumentCommand.updateSurrogatesDigitalisate(
        kulturObjektDokument);

    String expectedSurrogates = """
        <surrogates>\
        <bibl xml:id="HSP-1" default="true">\
        <idno>1</idno>\
        <ref target="https://iiif.ub.uni-leipzig.de/0000009089/manifest.json" type="manifest"/>\
        <date when="2020-10-06" type="digitized">06.10.2020</date>\
        <placeName type="digitizing" ref="123">Berlin</placeName>\
        <orgName ref="456" type="digitizing">Staatsbibliothek zu Berlin</orgName>\
        </bibl>\
        </surrogates>\
        """;

    assertTrue(
        kulturObjektDokument.getTeiXML().contains(expectedSurrogates),
        kulturObjektDokument.getTeiXML());
  }

  @Test
  void testUpdateKODTeiWithFailure() throws JAXBException {
    KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("HSP-12123123")
            .withTEIXml(TEIObjectFactory.marshal(kodTEI))
            .build();

    kodTEI.setId("1");

    TEIKulturObjektDokumentCommand.updateKODTEI(kulturObjektDokument, kodTEI);
  }

  @Test
  void testUpdatePURLs() throws Exception {
    PURL internalPURL = new PURL(new URI(PURL_INTERNAL),
        new URI(KOD_TARGET_URL), PURLTyp.INTERNAL);
    PURL externalPURL = new PURL(new URI(PURL_EXTERNAL),
        new URI(KOD_TARGET_URL), PURLTyp.EXTERNAL);

    KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("HSP-12123123")
            .withTEIXml(TEIObjectFactory.marshal(kodTEI))
            .addPURL(internalPURL)
            .addPURL(externalPURL)
            .build();

    TEIKulturObjektDokumentCommand.updatePURLs(kulturObjektDokument);

    String ptrInternal =
        """
            <ptr type="purl" subtype="hsp" target="https://resolver.staatsbibliothek-berlin.de/HSP1660133922141000" xml:id="HSP-ddd74884-974d-3007-9895-ce8d42201390"/>""";
    String ptrExternal =
        """
            <ptr type="purl" target="https://doi.org/10.1111/hex.12487" xml:id="HSP-5e9f6574-54ef-3ae2-bdf7-7f77565adb41"/>""";

    assertEquals(
        1,
        StringUtils.countMatches(kulturObjektDokument.getTeiXML(), ptrInternal),
        kulturObjektDokument.getTeiXML());
    assertEquals(1, StringUtils.countMatches(kulturObjektDokument.getTeiXML(),
        ptrExternal));

    TEIKulturObjektDokumentCommand.updatePURLs(kulturObjektDokument);

    assertEquals(1, StringUtils.countMatches(kulturObjektDokument.getTeiXML(),
        ptrInternal));
    assertEquals(1, StringUtils.countMatches(kulturObjektDokument.getTeiXML(),
        ptrExternal));
  }

  @Test
  void testopenTEIDokument() throws JAXBException, IOException {
    KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("HSP-12123123")
            .withTEIXml(TEIObjectFactory.marshal(kodTEI))
            .build();

    assertTrue(
        TEIKulturObjektDokumentCommand.openTEIDokument(kulturObjektDokument)
            .isPresent());
  }

  @Test
  void testupdateIndentInMsIdentifierHappyPath() throws Exception {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei",
        "tei-HSP-kodms1.xml");
    List<TEI> teiList = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));
    TEI tei = teiList.getFirst();

    Identifikation gueltigeIdentifikation1 =
        new Identifikation.IdentifikationBuilder()
            .withIdent("MS2")
            .withBesitzer(new NormdatenReferenz("1", "SBB Berlin", "GND123"))
            .withAufbewahrungsOrt(
                new NormdatenReferenz("2", "Berlin", "GND456"))
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();

    Identifikation alternativeIdentifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdent("Ms 1")
            .withBesitzer(new NormdatenReferenz("1", "SBB Berlin", "GND123"))
            .withAufbewahrungsOrt(
                new NormdatenReferenz("2", "Berlin", "GND456"))
            .withIdentTyp(IdentTyp.ALTSIGNATUR)
            .build();

    KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("HSP-12123123")
            .withTEIXml(TEIObjectFactory.marshal(tei))
            .withGueltigerIdentifikation(gueltigeIdentifikation1)
            .addAlternativeIdentifikation(alternativeIdentifikation)
            .build();

    updateIndentInMsIdentifier(kulturObjektDokument);

    assertTrue(kulturObjektDokument.getTeiXML().contains("<idno>MS2</idno>"));

    assertTrue(
        kulturObjektDokument
            .getTeiXML()
            .contains(
                "<altIdentifier type=\"alternative\"><settlement ref=\"GND456\" key=\"2\">Berlin</settlement><repository ref=\"GND123\" key=\"1\">SBB Berlin</repository><idno>Ms 1</idno></altIdentifier>"),
        kulturObjektDokument.getTeiXML());
  }

  @Test
  void testUpdateMsIdentifierAndAllAltidentifierTest() throws Exception {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei",
        "tei-HSP-kodms1.xml");
    List<TEI> teiList = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));
    TEI tei = teiList.getFirst();

    Identifikation gueltigeIdentifikation1 =
        new Identifikation.IdentifikationBuilder()
            .withIdent("MS2")
            .withBesitzer(new NormdatenReferenz("1", "SBB Berlin", "GND123"))
            .withAufbewahrungsOrt(
                new NormdatenReferenz("2", "Berlin", "GND456"))
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withSortKey("ABC123")
            .build();

    Identifikation alternativeIdentifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdent("Ms 1")
            .withBesitzer(new NormdatenReferenz("1", "SBB Berlin", "GND123"))
            .withAufbewahrungsOrt(
                new NormdatenReferenz("2", "Berlin", "GND456"))
            .withIdentTyp(IdentTyp.ALTSIGNATUR)
            .build();

    KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("HSP-12123123")
            .withTEIXml(TEIObjectFactory.marshal(tei))
            .withGueltigerIdentifikation(gueltigeIdentifikation1)
            .addAlternativeIdentifikation(alternativeIdentifikation)
            .build();

    TEIKulturObjektDokumentCommand.updateMsIdentifierAndAllAltidentifier(
        kulturObjektDokument);

    String xml = kulturObjektDokument.getTeiXML();

    assertTrue(xml.contains("<settlement ref=\"GND456\" key=\"2\">Berlin</settlement>"));

    assertTrue(xml.contains(
        "<altIdentifier type=\"former\"><settlement ref=\"GND456\" key=\"2\">Berlin</settlement><repository ref=\"GND123\" key=\"1\">SBB Berlin</repository><idno>Ms 1</idno></altIdentifier>"
    ));

    assertTrue(xml.contains("sortKey=\"ABC123\""), xml);
  }

  @Test
  void testUpdateMsIdentifier_shouldRemoveSortKey_whenNull() throws Exception {

    Identifikation identifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdent("MS1")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withSortKey("ABC")
            .build();

    KulturObjektDokument kod =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("HSP-1")
            .withTEIXml(TEIObjectFactory.marshal(kodTEI))
            .withGueltigerIdentifikation(identifikation)
            .build();

    TEIKulturObjektDokumentCommand.updateMsIdentifierAndAllAltidentifier(kod);

    assertTrue(kod.getTeiXML().contains("sortKey=\"ABC\""));

    Identifikation identifikationWithoutSortKey =
        new Identifikation.IdentifikationBuilder()
            .withIdent("MS1")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();

    kod.setGueltigeIdentifikation(identifikationWithoutSortKey);

    TEIKulturObjektDokumentCommand.updateMsIdentifierAndAllAltidentifier(kod);

    assertFalse(kod.getTeiXML().contains("sortKey="));
  }

  @Test
  void testUpdateMsIdentifier_writesSortKeyToTei_inlineXml() throws Exception {

    String xml = """
        <TEI xmlns="http://www.tei-c.org/ns/1.0">
          <teiHeader>
            <fileDesc>
              <sourceDesc>
                <msDesc>
                  <msIdentifier>
                    <idno>OLD</idno>
                  </msIdentifier>
                </msDesc>
              </sourceDesc>
            </fileDesc>
          </teiHeader>
        </TEI>
        """;

    Identifikation identifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdent("MS2")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withSortKey("MS_0002")
            .build();

    KulturObjektDokument kod =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("HSP-1")
            .withTEIXml(xml)
            .withGueltigerIdentifikation(identifikation)
            .build();

    TEIKulturObjektDokumentCommand.updateIndentInMsIdentifier(kod);

    String updatedXml = kod.getTeiXML();

    assertTrue(updatedXml.contains("MS2"), updatedXml);
    assertTrue(updatedXml.contains("sortKey=\"MS_0002\""), updatedXml);
  }

  @Test
  void testReadSortKeyFromTei_inlineXml() throws Exception {

    String xml = """
        <TEI xmlns="http://www.tei-c.org/ns/1.0">
          <teiHeader>
            <fileDesc>
              <sourceDesc>
                <msDesc>
                  <msIdentifier>
                    <idno sortKey="ABC123">MS 1</idno>
                  </msIdentifier>
                </msDesc>
              </sourceDesc>
            </fileDesc>
          </teiHeader>
        </TEI>
        """;

    Optional<TEI> teiOpt =
        TEIKulturObjektDokumentCommand.openTEIDokument(xml);

    MsIdentifier msIdentifier =
        TEICommon.findFirst(MsIdentifier.class, teiOpt.get()).get();

    String sortKey = msIdentifier.getIdnos().getFirst().getSortKey();

    assertEquals("ABC123", sortKey);
  }

  @Test
  void testUpdateMsIdentifier_doesNotWriteSortKey_whenNull() throws Exception {

    String xml = """
        <TEI xmlns="http://www.tei-c.org/ns/1.0">
          <teiHeader>
            <fileDesc>
              <sourceDesc>
                <msDesc>
                  <msIdentifier>
                    <idno>OLD</idno>
                  </msIdentifier>
                </msDesc>
              </sourceDesc>
            </fileDesc>
          </teiHeader>
        </TEI>
        """;

    Identifikation identifikation =
        new Identifikation.IdentifikationBuilder()
            .withIdent("MS2")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .build();

    KulturObjektDokument kod =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("HSP-1")
            .withTEIXml(xml)
            .withGueltigerIdentifikation(identifikation)
            .build();

    TEIKulturObjektDokumentCommand.updateIndentInMsIdentifier(kod);

    String updatedXml = kod.getTeiXML();

    assertFalse(updatedXml.contains("sortKey="), updatedXml);
  }

  @Test
  void testUpdateKODBeschreibungenReferenzesRemovesRootWhenDescriptionsAreLastChild()
      throws Exception {

    KulturObjektDokument kulturObjektDokument =
        new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("HSP-remove-last-description")
            .withTEIXml(
                TEIObjectFactory.marshal(kodTEI))
            .withBeschreibungsdokumentIDs(
                Set.of())
            .build();

    TEIKulturObjektDokumentCommand.updateKODBeschreibungenReferenzes(kulturObjektDokument);
    TEI updatedTei = TEIKulturObjektDokumentCommand.openTEIDokument(kulturObjektDokument)
        .orElseThrow();

    Additional additional = TEICommon.findFirst(Additional.class, updatedTei)
        .orElseThrow();

    assertNull(additional.getListBibl());
  }

}
