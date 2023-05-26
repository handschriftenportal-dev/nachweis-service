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

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KulturObjektTyp;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPMapperException;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.TEI;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.05.2020.
 * @version 1.0
 */
public class BeschreibungsKomponenteKopfFactoryTest {

  static TEI tei;
  static MsDesc msDesc;

  static {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei", "tei-msDesc_Westphal_head.xml");
    try (InputStream is = newInputStream(teiFilePath)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      tei = teiList.get(0);

      List<MsDesc> msDescList = new ArrayList<>();
      findAll(MsDesc.class, tei, msDescList);

      msDesc = msDescList.get(0);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  BeschreibungsKomponenteKopfFactory factory = new BeschreibungsKomponenteKopfFactory();

  @Test
  void testBuildKomponenteKopf() throws HSPMapperException {

    BeschreibungsKomponenteKopf kopf = factory.build(msDesc, tei);

    assertNotNull(kopf);

    assertNotNull(kopf.getId());

    assertEquals(KulturObjektTyp.CODEX, kopf.getKulturObjektTyp());

    assertEquals("Index term title", kopf.getTitel());
  }

  @Test
  void testBuildTitleString() {

    Assertions.assertEquals("Index term title",
        BeschreibungsKomponenteKopfFactory.buildTitleString(msDesc));
  }
}
