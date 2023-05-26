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
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 12.05.2020.
 * @version 1.0
 */
public class TEIQueryTest {

  static TEI tei;

  static TEI teiBad;

  static TEI teiodd109;

  @BeforeAll
  static void init() throws Exception {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei", "tei-msDesc_Westphal.xml");
    Path teiBadFilePath = Paths.get("src", "test", "resources", "tei",
        "tei-msDesc_Westphal_NoID.xml");
    Path teiodd109Path = Paths.get("src", "test", "resources", "tei", "tei_odd109.xml");

    try (InputStream is = newInputStream(teiFilePath);
        InputStream is2 = newInputStream(teiBadFilePath);
        InputStream is3 = newInputStream(teiodd109Path)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      tei = teiList.get(0);

      List<TEI> teiBadList = TEIObjectFactory.unmarshal(is2);
      teiBad = teiBadList.get(0);

      List<TEI> teiodd = TEIObjectFactory.unmarshal(is3);
      teiodd109 = teiodd.get(0);
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
    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withTEIXml(TEIObjectFactory.marshal(teiodd109)).build();

    assertEquals("4007721-4",
        TEIQuery.queryForTermOriginGNDID(beschreibung).orElse(null));
  }

  @Test
  void testQueryForFileDescPupPlaceElement() throws Exception {
    assertTrue(TEIQuery.queryForFileDescPupPlaceElement(tei).isPresent());
  }
}
