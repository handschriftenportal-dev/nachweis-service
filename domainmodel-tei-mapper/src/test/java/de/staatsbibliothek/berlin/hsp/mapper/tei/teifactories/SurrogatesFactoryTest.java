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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DigitalisatTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import javax.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Additional;
import org.tei_c.ns._1.Bibl;
import org.tei_c.ns._1.FileDesc;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.SourceDesc;
import org.tei_c.ns._1.Surrogates;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.TeiHeader;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 06.10.2020.
 * @version 1.0
 */
public class SurrogatesFactoryTest {

  private static final String XML_RESULT =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
          + "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:dcr=\"http://www.isocat.org/ns/dcr\" xmlns:egXML=\"http://www.tei-c.org/ns/Examples\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
          + "    <teiHeader>\n"
          + "        <fileDesc>\n"
          + "            <sourceDesc>\n"
          + "                <msDesc>\n"
          + "                    <additional>\n"
          + "                        <surrogates>\n"
          + "                            <bibl>\n"
          + "                                <idno>12</idno>\n"
          + "                                <ref target=\"https://iiif.ub.uni-leipzig.de/0000009089/manifest.json\" subtype=\"completeFromOriginal\" type=\"manifest\"/>\n"
          + "                                <ref target=\"https://iiif.ub.uni-leipzig.de/0000009089/bild.png\" subtype=\"completeFromOriginal\" type=\"other\"/>\n"
          + "                                <ref target=\"https://iiif.ub.uni-leipzig.de/fcgi-bin/iipsrv.fcgi?iiif=/j2k/0000/0090/0000009089/00000057.jpx/full/100,/0/default.jpg\" type=\"thumbnail\"/>\n"
          + "                                <date when=\"2020-10-06\" type=\"digitized\">06.10.2020</date>\n"
          + "                                <date when=\"2020-10-07\" type=\"issued\">07.10.2020</date>\n"
          + "                                <placeName ref=\"123\">Berlin</placeName>\n"
          + "                                <orgName ref=\"456\">Staatsbibliothek zu Berlin</orgName>\n"
          + "                            </bibl>\n"
          + "                        </surrogates>\n"
          + "                    </additional>\n"
          + "                </msDesc>\n"
          + "            </sourceDesc>\n"
          + "        </fileDesc>\n"
          + "    </teiHeader>\n"
          + "</TEI>\n";

  @Test
  void testBuildSurrogate() throws JAXBException {

    Digitalisat digitalisat = Digitalisat.DigitalisatBuilder()
        .withID("12")
        .withManifest("https://iiif.ub.uni-leipzig.de/0000009089/manifest.json")
        .withAlternativeUrl("https://iiif.ub.uni-leipzig.de/0000009089/bild.png")
        .withThumbnail(
            "https://iiif.ub.uni-leipzig.de/fcgi-bin/iipsrv.fcgi?iiif=/j2k/0000/0090/0000009089/00000057.jpx/full/100,/0/default.jpg")
        .withEntstehungsort(new NormdatenReferenz("123",
            "Berlin", "GDN123"))
        .withDigitalisierendeEinrichtung(new NormdatenReferenz("456",
            "Staatsbibliothek zu Berlin",
            "GND-X123"))
        .withDigitalisierungsdatum(LocalDate.parse("06.10.2020",
            DateTimeFormatter.ofPattern("dd.MM.yyyy")))
        .withErstellungsdatum(LocalDate.parse("07.10.2020",
            DateTimeFormatter.ofPattern("dd.MM.yyyy")))
        .withTyp(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL)
        .build();

    Surrogates surrogates = SurrogatesFactory.build(Arrays.asList(digitalisat));

    assertNotNull(surrogates);

    assertEquals(1, surrogates.getContent().size());

    Bibl bibl = (Bibl) surrogates.getContent().get(0);

    assertEquals(8, bibl.getContent().size());

    TEI tei = new TEI();
    TeiHeader teiHeader = new TeiHeader();
    tei.setTeiHeader(teiHeader);

    FileDesc fileDesc = new FileDesc();
    teiHeader.setFileDesc(fileDesc);

    SourceDesc sourceDesc = new SourceDesc();
    fileDesc.getSourceDescs().add(sourceDesc);

    MsDesc msDesc = new MsDesc();
    sourceDesc.getBiblsAndBiblStructsAndListBibls().add(msDesc);

    Additional additional = new Additional();
    msDesc.setAdditional(additional);
    additional.setSurrogates(surrogates);

    assertEquals(XML_RESULT, TEIObjectFactory.marshal(tei));
  }
}
