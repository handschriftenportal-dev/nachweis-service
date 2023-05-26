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

import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DigitalisatTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 06.10.2020.
 * @version 1.0
 */
@Execution(ExecutionMode.SAME_THREAD)
class TEIKulturObjektDokumentCommandTest {

  public static final String PURL_INTERNAL = "https://resolver.staatsbibliothek-berlin.de/HSP1660133922141000";
  public static final String KOD_TARGET_URL = "http://b-dev1047:8080/search?hspobjectid=HSP-00b9f2b8-f1df-32b8-9ccc-2d76cfe17d42";
  public static final String PURL_EXTERNAL = "https://doi.org/10.1111/hex.12487";
  static TEI kodTEI;

  @BeforeAll
  static void init() throws Exception {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei", "tei-HSP-kodms1.xml");
    try (InputStream is = newInputStream(teiFilePath)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      kodTEI = teiList.get(0);
    }
  }

  @Test
  void testUpdateKODSourceDescWith2BeschreibungMsDesc() throws Exception {

    Path teiFilePath = Paths.get("src", "test", "resources", "tei", "tei-msDesc_Westphal.xml");
    List<TEI> teiList = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));
    TEI teiBeschreibung1 = teiList.get(0);

    Path teiFilePath2 = Paths.get("src", "test", "resources", "tei", "tei-msDesc.xml");
    List<TEI> teiList2 = TEIObjectFactory.unmarshal(newInputStream(teiFilePath2));
    TEI teiBeschreibung2 = teiList2.get(0);

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP123")
        .withTEIXml(TEIObjectFactory.marshal(teiBeschreibung1))
        .build();

    Beschreibung beschreibung2 = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP123123123")
        .withTEIXml(TEIObjectFactory.marshal(teiBeschreibung2))
        .build();

    KulturObjektDokument kulturObjektDokument = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-12123123")
        .withTEIXml(TEIObjectFactory.marshal(kodTEI))
        .withBeschreibungsdokumentIDs(Set.of(beschreibung.getId(), beschreibung2.getId()))
        .build();

    TEIKulturObjektDokumentCommand.updateKODSourceDescWithAddBeschreibungMsDesc(kulturObjektDokument, beschreibung);
    TEIKulturObjektDokumentCommand.updateKODSourceDescWithAddBeschreibungMsDesc(kulturObjektDokument, beschreibung2);

    String updatedTEIXML = kulturObjektDokument.getTeiXML();

    assertTrue(updatedTEIXML.contains("<msDesc xml:id=\"HSP-36a88bd1-987b-4ac9-8c67-212a43681b02\""));
    assertTrue(updatedTEIXML.contains("<msDesc xml:id=\"mss_36-23-aug-2f_tei-msDesc_Westphal\""));
    assertFalse(updatedTEIXML.contains("<msDesc xml:id=\"mss_36-23-aug-2f_tei-msDesc\""));
  }

  @Test
  void testupdateKODBeschreibungenReferenzes() throws Exception {

    Path teiFilePath = Paths.get("src", "test", "resources", "tei", "tei-msDesc_Westphal.xml");
    List<TEI> teiList = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));
    TEI teiBeschreibung1 = teiList.get(0);

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP123")
        .withTEIXml(TEIObjectFactory.marshal(teiBeschreibung1))
        .build();

    KulturObjektDokument kulturObjektDokument = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-12123123")
        .withTEIXml(TEIObjectFactory.marshal(kodTEI))
        .withBeschreibungsdokumentIDs(Set.of(beschreibung.getId()))
        .build();

    TEIKulturObjektDokumentCommand.updateKODBeschreibungenReferenzes(kulturObjektDokument);

    String updatedTEIXML = kulturObjektDokument.getTeiXML();
    assertTrue(updatedTEIXML.contains("<ref target=\"HSP123\"/>"), updatedTEIXML);
  }

  @Test
  void testUpdateKODSourceDescWithRemoveBeschreibungMsDesc() throws Exception {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei", "tei-msDesc_Westphal.xml");
    List<TEI> teiList = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));
    TEI teiBeschreibung1 = teiList.get(0);

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("mss_36-23-aug-2f_tei-msDesc_Westphal")
        .withTEIXml(TEIObjectFactory.marshal(teiBeschreibung1))
        .build();

    KulturObjektDokument kulturObjektDokument = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-12123123")
        .withTEIXml(TEIObjectFactory.marshal(kodTEI))
        .withBeschreibungsdokumentIDs(Set.of(beschreibung.getId()))
        .build();

    TEIKulturObjektDokumentCommand.updateKODSourceDescWithAddBeschreibungMsDesc(kulturObjektDokument, beschreibung);

    String updatedTEIXML = kulturObjektDokument.getTeiXML();

    assertTrue(updatedTEIXML.contains("<msDesc xml:id=\"HSP-36a88bd1-987b-4ac9-8c67-212a43681b02\""));
    assertTrue(updatedTEIXML.contains("<msDesc xml:id=\"mss_36-23-aug-2f_tei-msDesc_Westphal\""));

    TEIKulturObjektDokumentCommand.updateKODSourceDescWithRemoveBeschreibungMsDesc(kulturObjektDokument,
        "not_exists");

    updatedTEIXML = kulturObjektDokument.getTeiXML();

    assertTrue(updatedTEIXML.contains("<msDesc xml:id=\"HSP-36a88bd1-987b-4ac9-8c67-212a43681b02\""));
    assertTrue(updatedTEIXML.contains("<msDesc xml:id=\"mss_36-23-aug-2f_tei-msDesc_Westphal\""));

    TEIKulturObjektDokumentCommand.updateKODSourceDescWithRemoveBeschreibungMsDesc(kulturObjektDokument,
        beschreibung.getId());

    updatedTEIXML = kulturObjektDokument.getTeiXML();

    assertTrue(updatedTEIXML.contains("<msDesc xml:id=\"HSP-36a88bd1-987b-4ac9-8c67-212a43681b02\""));
    assertFalse(updatedTEIXML.contains("<msDesc xml:id=\"mss_36-23-aug-2f_tei-msDesc_Westphal\""));
  }

  @Test
  void testUpdateKODSourceDescWithBeschreibungMsDesc() throws Exception {

    Path teiFilePath = Paths.get("src", "test", "resources", "tei", "tei-msDesc_Westphal.xml");
    List<TEI> teiList = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));
    TEI teiBeschreibung = teiList.get(0);

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("HSP123")
        .withTEIXml(TEIObjectFactory.marshal(teiBeschreibung))
        .build();

    KulturObjektDokument kulturObjektDokument = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-12123123")
        .withTEIXml(TEIObjectFactory.marshal(kodTEI))
        .build();

    TEIKulturObjektDokumentCommand.updateKODSourceDescWithAddBeschreibungMsDesc(kulturObjektDokument, beschreibung);

    String updatedTEIXML = kulturObjektDokument.getTeiXML();

    assertTrue(updatedTEIXML.contains("<msDesc xml:id=\"HSP-36a88bd1-987b-4ac9-8c67-212a43681b02\""));
    assertTrue(updatedTEIXML.contains("<msDesc xml:id=\"mss_36-23-aug-2f_tei-msDesc_Westphal\""));
  }

  @Test
  void testUpdateSurrogates() throws Exception {
    Digitalisat digitalisat = Digitalisat.DigitalisatBuilder()
        .withID("1")
        .withManifest("https://iiif.ub.uni-leipzig.de/0000009089/manifest.json")
        .withEntstehungsort(new NormdatenReferenz("123",
            "Berlin", "GDN123"))
        .withDigitalisierendeEinrichtung(new NormdatenReferenz("456",
            "Staatsbibliothek zu Berlin",
            "GND-X123"))
        .withDigitalisierungsdatum(LocalDate.parse("06.10.2020",
            DateTimeFormatter.ofPattern("dd.MM.yyyy")))
        .withTyp(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL)
        .build();

    KulturObjektDokument kulturObjektDokument = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-12123123")
        .addDigitalisat(digitalisat)
        .withTEIXml(TEIObjectFactory.marshal(kodTEI))
        .build();

    TEIKulturObjektDokumentCommand.updateSurrogatesDigitalisate(kulturObjektDokument);

    assertTrue(kulturObjektDokument.getTeiXML().contains("<surrogates>\n"
        + "                            <bibl>\n"
        + "                                <idno>1</idno>\n"
        + "                                <ref target=\"https://iiif.ub.uni-leipzig.de/0000009089/manifest.json\" subtype=\"completeFromOriginal\" type=\"manifest\"/>\n"
        + "                                <date when=\"2020-10-06\" type=\"digitized\">06.10.2020</date>\n"
        + "                                <placeName ref=\"123\">Berlin</placeName>\n"
        + "                                <orgName ref=\"456\">Staatsbibliothek zu Berlin</orgName>\n"
        + "                            </bibl>\n"
        + "                        </surrogates>"));
  }

  @Test
  void testUpdateKODTeiWithFailure() throws JAXBException {
    KulturObjektDokument kulturObjektDokument = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-12123123")
        .withTEIXml(TEIObjectFactory.marshal(kodTEI))
        .build();

    kodTEI.setId("1");

    TEIKulturObjektDokumentCommand.updateKODTEI(kulturObjektDokument, kodTEI);
  }

  @Test
  void testUpdatePURLs() throws Exception {
    PURL internalPURL = new PURL(new URI(PURL_INTERNAL), new URI(KOD_TARGET_URL), PURLTyp.INTERNAL);
    PURL externalPURL = new PURL(new URI(PURL_EXTERNAL), new URI(KOD_TARGET_URL), PURLTyp.EXTERNAL);

    KulturObjektDokument kulturObjektDokument = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-12123123")
        .withTEIXml(TEIObjectFactory.marshal(kodTEI))
        .addPURL(internalPURL)
        .addPURL(externalPURL)
        .build();

    TEIKulturObjektDokumentCommand.updatePURLs(kulturObjektDokument);

    String ptrInternal = "<ptr xml:id=\"HSP-ddd74884-974d-3007-9895-ce8d42201390\" target=\"https://resolver.staatsbibliothek-berlin.de/HSP1660133922141000\" subtype=\"hsp\" type=\"purl\"/>";
    String ptrExternal = "<ptr xml:id=\"HSP-5e9f6574-54ef-3ae2-bdf7-7f77565adb41\" target=\"https://doi.org/10.1111/hex.12487\" type=\"purl\"/>";

    assertEquals(1, StringUtils.countMatches(kulturObjektDokument.getTeiXML(), ptrInternal));
    assertEquals(1, StringUtils.countMatches(kulturObjektDokument.getTeiXML(), ptrExternal));

    TEIKulturObjektDokumentCommand.updatePURLs(kulturObjektDokument);

    assertEquals(1, StringUtils.countMatches(kulturObjektDokument.getTeiXML(), ptrInternal));
    assertEquals(1, StringUtils.countMatches(kulturObjektDokument.getTeiXML(), ptrExternal));
  }

  @Test
  void testopenTEIDokument() throws JAXBException, IOException {
    KulturObjektDokument kulturObjektDokument = new KulturObjektDokument.KulturObjektDokumentBuilder()
        .withId("HSP-12123123")
        .withTEIXml(TEIObjectFactory.marshal(kodTEI))
        .build();

    assertTrue(TEIKulturObjektDokumentCommand.openTEIDokument(kulturObjektDokument).isPresent());
  }
}
