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

package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon.findAll;
import static java.nio.file.Files.newInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPMapperException;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.MsIdentifier;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.05.2020.
 * @version 1.0
 */
public class IdentifikationSetFactoryTest {

  static TEI tei;
  static MsDesc msDesc;
  IdentifikationSetFactory factory = new IdentifikationSetFactory();

  @BeforeAll
  static void beforeAll() throws Exception {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei", "tei-msDesc_Westphal.xml");
    List<TEI> teiList = TEIObjectFactory.unmarshal(newInputStream(teiFilePath));
    tei = teiList.get(0);

    List<MsDesc> msDescList = new ArrayList<>();
    findAll(MsDesc.class, tei, msDescList);

    msDesc = msDescList.get(0);
  }

  @Test
  void testgetAufbewahrungsOrt() {

    NormdatenReferenz aufbewahrendeInstitution = factory.findBesitzer(msDesc.getMsIdentifier());
    NormdatenReferenz aufbewahrungsOrt = factory.findAufbewahrungsOrt(msDesc.getMsIdentifier());

    assertNotNull(aufbewahrendeInstitution);

    assertEquals("Herzog August Bibliothek", aufbewahrendeInstitution.getName());

    assertEquals("7df60c21-e28a-3bc2-9f95-8315bfc6ce9e", aufbewahrendeInstitution.getId());

    assertNotNull(aufbewahrungsOrt);

    assertEquals("Wolfenbüttel", aufbewahrungsOrt.getName());
  }

  @Test
  void testgetIdent() {
    String signature = factory.getIdent(msDesc.getMsIdentifier());

    assertNotNull(signature);

    assertEquals("Cod. Guelf. 36.23 Aug. 2°", signature);

    assertEquals("", factory.getIdent(new MsIdentifier()));
  }

  @Test
  void testBuildIdentifikation() throws HSPMapperException {

    Set<Identifikation> identifikation = factory.build(msDesc, tei);

    assertNotNull(identifikation);

    assertEquals(1, identifikation.size());

    identifikation.stream().findFirst().ifPresent(i -> {

      assertNotNull(i.getId());

      assertEquals("Cod. Guelf. 36.23 Aug. 2°", i.getIdent());

      assertNotNull(i.getAufbewahrungsOrt());

      assertEquals("Wolfenbüttel", i.getAufbewahrungsOrt().getName());

      assertNotNull(i.getBesitzer());

      assertEquals("Herzog August Bibliothek", i.getBesitzer().getName());

      assertEquals(IdentTyp.GUELTIGE_SIGNATUR, i.getIdentTyp());

      assertNotNull(i.getSammlungIDs());

      assertEquals(1, i.getSammlungIDs().size());

      assertEquals("Augusteer", i.getSammlungIDs().iterator().next());

    });
  }
}