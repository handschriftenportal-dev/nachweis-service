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

package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.TEI;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 18.05.2020.
 * @version 1.0
 */
public class KODTemplateMapperTest {

  private static final String result =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
          + "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:dcr=\"http://www.isocat.org/ns/dcr\" xmlns:egXML=\"http://www.tei-c.org/ns/Examples\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
          + "    <teiHeader xml:lang=\"de\">\n"
          + "        <fileDesc>\n"
          + "            <titleStmt>\n"
          + "                <title>Kulturobjektdokument von Dresden, Sächsische Landesbibliothek - Staats- und Universitätsbibliothek Dresden, Mscr.Dresd.A.111</title>\n"
          + "                <respStmt>\n"
          + "                    <resp>Herausgegeben vom</resp>\n"
          + "                    <orgName>Handschriftenportal</orgName>\n"
          + "                </respStmt>\n"
          + "            </titleStmt>\n"
          + "            <editionStmt>\n"
          + "                <edition>Elektronische Ausgabe nach TEI P5</edition>\n"
          + "                <respStmt>\n"
          + "                    <resp>Automatisch generierte Ausgabe</resp>\n"
          + "                    <orgName>Handschriftenportal</orgName>\n"
          + "                </respStmt>\n"
          + "            </editionStmt>\n"
          + "            <publicationStmt>\n"
          + "                <publisher>\n"
          + "                    <orgName>Handschriftenportal</orgName>\n"
          + "                    <ptr target=\"http://www.handschriftenportal.de\"/>\n"
          + "                </publisher>\n"
          + "                <date when=\"2021-05-17\" type=\"issued\">2021-05-17</date>\n"
          + "                <distributor>Handschriftenportal</distributor>\n"
          + "                <availability status=\"free\">\n"
          + "                  <licence target=\"https://creativecommons.org/publicdomain/zero/1.0/deed.de\">\n"
          + "                    <p>Lizenziert nach https://creativecommons.org/publicdomain/zero/1.0/deed.de</p>\n"
          + "                  </licence>\n"
          + "                </availability> \n"
          + "                <pubPlace>\n"
          + "                    <ptr xml:id=\"HSP-ddd74884-974d-3007-9895-ce8d42201390\" target=\"https://resolver.staatsbibliothek-berlin.de/HSP1660133922141000\" subtype=\"hsp\" type=\"purl\"/>\n"
          + "                </pubPlace>\n"
          + "            </publicationStmt>\n"
          + "            <sourceDesc>\n"
          + "                <msDesc xml:id=\"HSP12345\" xml:lang=\"de\" type=\"hsp:object\">\n"
          + "                    <msIdentifier>\n"
          + "                        <settlement key=\"1\">Dresden</settlement>\n"
          + "                        <repository key=\"1\">Sächsische Landesbibliothek - Staats- und Universitätsbibliothek Dresden</repository>\n"
          + "                        <idno>Mscr.Dresd.A.111</idno>\n"
          + "                    </msIdentifier>\n"
          + "                    <head>\n"
          + "                      <index indexName=\"norm_title\">\n"
          + "                        <term type=\"title\"></term>\n"
          + "                      </index>\n"
          + "                      <index indexName=\"norm_material\">\n"
          + "                        <term type=\"material\"></term>\n"
          + "                        <term type=\"material_type\"></term>\n"
          + "                      </index>\n"
          + "                      <index indexName=\"norm_measure\">\n"
          + "                        <term type=\"measure\"></term>\n"
          + "                        <term type=\"measure_noOfLeaves\"></term>\n"
          + "                      </index>\n"
          + "                      <index indexName=\"norm_dimensions\">\n"
          + "                        <term type=\"dimensions\"></term>\n"
          + "                        <term type=\"height\"></term>\n"
          + "                        <term type=\"width\"></term>\n"
          + "                        <term type=\"depth\"/>\n"
          + "                        <term type=\"dimensions_typeOfInformation\"></term>\n"
          + "                      </index>\n"
          + "                      <index indexName=\"norm_format\">\n"
          + "                        <term type=\"format\"></term>\n"
          + "                        <term type=\"format_typeOfInformation\"></term>\n"
          + "                      </index>\n"
          + "                      <index indexName=\"norm_origPlace\">\n"
          + "                        <term type=\"origPlace\"></term>\n"
          + "                        <term type=\"origPlace_norm\"></term>\n"
          + "                      </index>\n"
          + "                      <index indexName=\"norm_origDate\">\n"
          + "                        <term type=\"origDate\"></term>\n"
          + "                        <term type=\"origDate_notBefore\"></term>\n"
          + "                        <term type=\"origDate_notAfter\"></term>\n"
          + "                        <term type=\"origDate_type\"></term>\n"
          + "                      </index>\n"
          + "                      <index indexName=\"norm_textLang\">\n"
          + "                        <term type=\"textLang\"></term>\n"
          + "                        <term type=\"textLang-ID\"></term>\n"
          + "                      </index>\n"
          + "                      <index indexName=\"norm_form\">\n"
          + "                        <term type=\"form\"></term>\n"
          + "                      </index>\n"
          + "                      <index indexName=\"norm_status\">\n"
          + "                        <term type=\"status\"></term>\n"
          + "                      </index>\n"
          + "                      <index indexName=\"norm_decoration\">\n"
          + "                        <term type=\"decoration\"/>\n"
          + "                      </index>\n"
          + "                      <index indexName=\"norm_musicNotation\">\n"
          + "                        <term type=\"musicNotation\"></term>\n"
          + "                      </index>\n"
          + "                    </head>\n"
          + "                    <additional>\n"
          + "                      <adminInfo>\n"
          + "                        <availability status=\"free\">\n"
          + "                          <licence target=\"https://creativecommons.org/publicdomain/zero/1.0/deed.de\">\n"
          + "                              <p>Lizenziert nach https://creativecommons.org/publicdomain/zero/1.0/deed.de</p>\n"
          + "                          </licence>\n"
          + "                        </availability>\n"
          + "                      </adminInfo>\n"
          + "                      <listBibl>\n"
          + "                        <bibl>\n"
          + "                          <ref target=\"HSP789\"/>\n"
          + "                        </bibl>\n"
          + "                      </listBibl>\n"
          + "                    </additional>\n"
          + "                </msDesc>\n"
          + "            </sourceDesc>\n"
          + "        </fileDesc>\n"
          + "    </teiHeader>\n"
          + "    <text>\n"
          + "        <body>\n"
          + "            <p/>\n"
          + "        </body>\n"
          + "    </text>\n"
          + "</TEI>\n";

  @Test
  void testCreateTEIFromInitial() throws Exception {

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder().withId("HSP789").build();

    NormdatenReferenz besitzer = new NormdatenReferenz("1",
        "Sächsische Landesbibliothek - Staats- und Universitätsbibliothek Dresden", "");
    NormdatenReferenz aufbewahrungsort = new NormdatenReferenz("1", "Dresden", "");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("Mscr.Dresd.A.111")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(aufbewahrungsort)
        .build();

    PURL purl = new PURL(URI.create("https://resolver.staatsbibliothek-berlin.de/HSP1660133922141000"),
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

    Diff kodDiff = DiffBuilder.compare(Input.fromString(result))
        .withTest(Input.fromString(xml))
        .ignoreComments()
        .withNodeFilter(node -> !node.getNodeName().equals("date"))
        .ignoreWhitespace()
        .build();

    assertFalse(kodDiff.hasDifferences(), kodDiff.toString());


  }

}
