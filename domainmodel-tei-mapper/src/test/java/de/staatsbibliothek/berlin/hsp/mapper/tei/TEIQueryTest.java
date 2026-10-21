package de.staatsbibliothek.berlin.hsp.mapper.tei;

import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Idno;
import org.tei_c.ns._1.MsIdentifier;
import org.tei_c.ns._1.PersName;
import org.tei_c.ns._1.Ref;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 12.05.2020.
 * @version 1.0
 */
public class TEIQueryTest {

  public static final String Signature = "Cod. ms. Bord. 1";
  private static final Identifikation gueltigeIdentifikation =
      new Identifikation.IdentifikationBuilder()
          .withId("HSP-0037fea7-58e0-3df9-a45c-4sdf3424")
          .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
          .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
          .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
          .build();
  static TEI tei;
  static TEI teiBad;
  static TEI teiodd109;
  static TEI digitalisatImportTEI;

  @BeforeAll
  static void init() throws Exception {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei",
        "tei-msDesc_Westphal.xml");
    Path teiBadFilePath =
        Paths.get("src", "test", "resources", "tei",
            "tei-msDesc_Westphal_NoID.xml");
    Path teiodd109Path = Paths.get("src", "test", "resources", "tei",
        "tei_odd109.xml");
    Path teiDigitalisatImport =
        Paths.get("src", "test", "resources", "tei",
            "03_Bad_Homburg_Stadtarchiv.xml");

    try (InputStream is = newInputStream(teiFilePath);
        InputStream is2 = newInputStream(teiBadFilePath);
        InputStream is3 = newInputStream(teiodd109Path);
        InputStream is4 = newInputStream(teiDigitalisatImport)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      tei = teiList.getFirst();

      List<TEI> teiBadList = TEIObjectFactory.unmarshal(is2);
      teiBad = teiBadList.getFirst();

      List<TEI> teiodd = TEIObjectFactory.unmarshal(is3);
      teiodd109 = teiodd.getFirst();

      List<TEI> teiDigitalisatImporte = TEIObjectFactory.unmarshal(is4);
      digitalisatImportTEI = teiDigitalisatImporte.getFirst();
    }
  }

  @Test
  void testQueryForMsDescAsBeschreibung() {

    assertEquals(1, TEIQuery.queryForMsDescAsBeschreibung(tei).size());
  }

  @Test
  void testQueryForMsDescAsBeschreibungNoID() {

    assertEquals(0, TEIQuery.queryForMsDescAsBeschreibung(teiBad).size());
  }

  @Test
  void testQueryForGNDIDEntstehungsOrt() throws JAXBException {
    Beschreibung beschreibung =
        new Beschreibung.BeschreibungsBuilder("id1", "kod-1",
            gueltigeIdentifikation)
            .withTEIXml(TEIObjectFactory.marshal(teiodd109))
            .build();

    List<String> gndids = TEIQuery.queryForTermOriginGNDID(beschreibung);

    assertNotNull(gndids);
    assertEquals(2, gndids.size());
    assertTrue(gndids.contains("4007721-4"));
    assertTrue(gndids.contains("4030481-4"));
  }

  @Test
  void testQueryForFileDescPupPlaceElement() throws Exception {
    assertTrue(TEIQuery.queryForFileDescPupPlaceElement(tei).isPresent());
  }

  @Test
  void testextraxtSignature() throws Exception {
    List<MsIdentifier> msIdentifiers = new ArrayList<>();
    TEICommon.findAll(MsIdentifier.class, teiodd109, msIdentifiers);
    MsIdentifier msIdentifier =
        msIdentifiers.stream()
            .filter(
                identifier ->
                    Signature.equals(
                        TEICommon.getContentAsString(
                            identifier.getIdnos().getFirst().getContent())))
            .findFirst()
            .get();

    assertEquals(Signature, TEIQuery.extractSignature(msIdentifier));
  }

  /**
   * Regression test: TEI {@code <idno>} content occasionally carries leading/trailing whitespace
   * from the source XML (e.g. {@code <idno>Becker-Sammlung, II 1 4° 1 </idno>}). The extracted
   * signature must be trimmed, otherwise downstream exact-match DB lookups (e.g.
   * {@code findByIdentifikationIdent}) silently return no results.
   */
  @Test
  void testextraxtSignature_trimsWhitespaceInIdnoContent() {
    Idno idno = new Idno();
    idno.getContent().add(" Becker-Sammlung, II 1 4° 1 ");

    MsIdentifier msIdentifier = new MsIdentifier();
    msIdentifier.getIdnos().add(idno);

    assertEquals("Becker-Sammlung, II 1 4° 1",
        TEIQuery.extractSignature(msIdentifier));
  }

  @Test
  void testqueryForMsDescOfType() {

    assertEquals(1,
        TEIQuery.queryForMsDescOfType(teiodd109, TEIValues.HSP_DESCRIPTION)
            .size());
  }

  @Test
  void testfindManifestRefs() throws Exception {
    List<Ref> refs = new ArrayList<>();
    TEICommon.findAll(Ref.class, digitalisatImportTEI, refs);

    Optional<Ref> result = TEIQuery.findManifestRefs(refs);

    assertTrue(result.isPresent());

    assertEquals(
        "https://iiif.ub.uni-marburg.de/presentation/v2/corvey61/manifest",
        String.join("", result.get().getTargets()));
  }

  @Test
  void testfindDateByType() throws Exception {
    List<org.tei_c.ns._1.Date> dates = new ArrayList<>();
    TEICommon.findAll(org.tei_c.ns._1.Date.class, digitalisatImportTEI, dates);

    Optional<LocalDate> result = TEIQuery.findDateByType(dates,
        TEIValues.DIGITIZED);

    assertTrue(result.isPresent());

    assertEquals(LocalDate.of(2021, 2, 12), result.get());

    result = TEIQuery.findDateByType(dates, TEIValues.DATE_PUBLISHED);

    assertEquals(LocalDate.of(2021, 10, 14), result.orElseThrow());
  }

  @Test
  void testParseDate() {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
        TEIValues.LOCAL_DATE_FORMAT);

    assertNull(TEIQuery.parseDate(null));
    assertNull(TEIQuery.parseDate(""));
    assertNull(TEIQuery.parseDate(" "));
    assertNull(TEIQuery.parseDate("asd"));
    assertNull(TEIQuery.parseDate("1.1.2020"));

    assertEquals(
        "2020-01-01",
        formatter.format(Objects.requireNonNull(TEIQuery.parseDate("2020"))));
    assertEquals(
        "2020-03-01", formatter.format(
            Objects.requireNonNull(TEIQuery.parseDate("2020-03"))));
    assertEquals(
        "2020-04-03", formatter.format(
            Objects.requireNonNull(TEIQuery.parseDate("2020-04-03"))));
  }

  @Test
  void testFinDGNDiD() {
    assertEquals(
        "511252-7",
        TEIQuery.findGNDIDinListOfStrings(
            List.of(
                "http://d-nb.info/gnd/511252-7",
                "https://sigel.staatsbibliothek-berlin.de/suche/?isil=DE-2468")));

    assertEquals(
        "511252-7",
        TEIQuery.findGNDIDinListOfStrings(
            List.of("511252-7",
                "https://sigel.staatsbibliothek-berlin.de/suche/?isil=DE-2468")));

    assertEquals("511252-7",
        TEIQuery.findGNDIDinListOfStrings(List.of("511252-7")));
  }

  @Test
  void TestfindAllPersnamesWithPath() {

    Map<String, PersName> result = TEIQuery.findAllPersNamesWithPath(tei);

    assertNotNull(result);
    assertEquals(3, result.size());
    assertTrue(
        result.containsKey(
            "/TEI[1]/teiHeader[1]/fileDesc[1]/titleStmt[1]/respStmt[1]/persName[1]/Werner Hoffmann"));
    assertNotNull(
        result.entrySet().stream().findFirst().orElseThrow().getValue());
  }

  @Test
  void TestfindAllPersnamesWithPath_GivenManyPersnames()
      throws IOException, JAXBException {

    Path teiPath = Paths.get("src", "test", "resources", "tei",
        "tei-HSP-persNames.xml");
    Map<String, PersName> result = TEIQuery.findAllPersNamesWithPath(
        TEIObjectFactory.unmarshal(
            Files.newInputStream(teiPath)).getFirst());

    assertNotNull(result);
    assertEquals(8, result.size());
  }
}
