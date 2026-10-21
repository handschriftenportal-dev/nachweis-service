package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.TEI;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 18.05.2020.
 * @version 1.0
 */
class KODTemplateMapperTest {

  private static final String EXPECTED_RESULT_FILE = "tei/kodinitial_expected_result.xml";

  private static final Identifikation gueltigeIdentifikation = new Identifikation.IdentifikationBuilder()
      .withId("HSP-0037fea7-58e0-3df9-a45c-4sdf3424")
      .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
      .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
      .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
      .build();

  private static String loadExpectedResult() throws Exception {
    Path path = Path.of(Objects.requireNonNull(KODTemplateMapperTest.class.getClassLoader()
        .getResource(EXPECTED_RESULT_FILE)).toURI());
    return Files.readString(path);
  }

  @Test
  void testCreateTEIFromInitial() throws Exception {

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("HSP789", "kod-1",
        gueltigeIdentifikation).build();

    NormdatenReferenz besitzer = new NormdatenReferenz("1",
        "Sächsische Landesbibliothek - Staats- und Universitätsbibliothek Dresden", "");
    NormdatenReferenz aufbewahrungsort = new NormdatenReferenz("1", "Dresden", "");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(aufbewahrungsort)
        .build();

    PURL purl = new PURL(
        URI.create("https://resolver.staatsbibliothek-berlin.de/HSP1660133922141000"),
        URI.create("https://handschriftenportal.de//search?hspobjectid=HSP12345"),
        PURLTyp.INTERNAL);

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP12345")
        .withGueltigerIdentifikation(identifikation)
        .addBeschreibungsdokumentID(beschreibung.getId())
        .addPURL(purl)
        .build();

    TEI tei = KODTemplateMapper.createTEIFromInitialTemplate(kod);

    assertNotNull(tei);

    String xml = TEIObjectFactory.marshal(tei);

    Diff kodDiff = DiffBuilder.compare(Input.fromString(loadExpectedResult()))
        .withTest(Input.fromString(xml))
        .ignoreComments()
        .withNodeFilter(node -> !node.getNodeName().equals("date"))
        .ignoreWhitespace()
        .build();

    assertFalse(kodDiff.hasDifferences(), kodDiff.toString());
  }

  @Test
  void testCreateTEIFromInitial_withSortKey() throws Exception {

    Identifikation identifikationMitSortKey =
        new Identifikation.IdentifikationBuilder()
            .withIdent(gueltigeIdentifikation.getIdent())
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(gueltigeIdentifikation.getBesitzer())
            .withAufbewahrungsOrt(gueltigeIdentifikation.getAufbewahrungsOrt())
            .withSortKey("DRESD_A_000111")
            .build();

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP12345")
        .withGueltigerIdentifikation(identifikationMitSortKey)
        .build();

    TEI tei = KODTemplateMapper.createTEIFromInitialTemplate(kod);

    String xml = TEIObjectFactory.marshal(tei);

    assertTrue(xml.contains("sortKey=\"DRESD_A_000111\""), xml);
  }

  @Test
  void testCreateTEIFromInitial_withoutSortKey_shouldNotContainSortKeyInXml() throws Exception {

    Identifikation identifikationOhneSortKey =
        new Identifikation.IdentifikationBuilder()
            .withIdent(gueltigeIdentifikation.getIdent())
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
            .withBesitzer(gueltigeIdentifikation.getBesitzer())
            .withAufbewahrungsOrt(gueltigeIdentifikation.getAufbewahrungsOrt())
            .build();

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP12345")
        .withGueltigerIdentifikation(identifikationOhneSortKey)
        .build();

    TEI tei = KODTemplateMapper.createTEIFromInitialTemplate(kod);

    String xml = TEIObjectFactory.marshal(tei);

    assertFalse(xml.contains("sortKey="), xml);
  }

  @Test
  void testCreateTEIFromInitial_withMultipleBeschreibungenIDs_allSurvive() throws Exception {

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-multi")
        .withGueltigerIdentifikation(gueltigeIdentifikation)
        .addBeschreibungsdokumentID("BSP-001")
        .addBeschreibungsdokumentID("BSP-002")
        .addBeschreibungsdokumentID("BSP-003")
        .build();

    TEI tei = KODTemplateMapper.createTEIFromInitialTemplate(kod);
    assertNotNull(tei);

    String xml = TEIObjectFactory.marshal(tei);

    assertTrue(xml.contains("listBibl type=\"root\""), xml);
    assertTrue(xml.contains("type=\"descriptions\""), xml);
    assertEquals(3, StringUtils.countMatches(xml, "<bibl"), xml);
    assertTrue(xml.contains("BSP-001"), xml);
    assertTrue(xml.contains("BSP-002"), xml);
    assertTrue(xml.contains("BSP-003"), xml);
  }

  @Test
  void testCreateTEIFromInitial_withEmptyBeschreibungenIDs_noListBibl() throws Exception {

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-empty")
        .withGueltigerIdentifikation(gueltigeIdentifikation)
        .build();

    TEI tei = KODTemplateMapper.createTEIFromInitialTemplate(kod);
    assertNotNull(tei);

    String xml = TEIObjectFactory.marshal(tei);

    assertFalse(xml.contains("listBibl type=\"root\""), xml);
    assertFalse(xml.contains("listBibl type=\"descriptions\""), xml);
  }
}
