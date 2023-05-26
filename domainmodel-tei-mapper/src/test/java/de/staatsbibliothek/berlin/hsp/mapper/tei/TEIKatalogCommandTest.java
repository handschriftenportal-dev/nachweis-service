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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.TEI;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 28.02.2022
 */

public class TEIKatalogCommandTest {

  static TEI tei;

  @BeforeAll
  static void init() throws Exception {
    Path teiFilePath = Paths.get("src", "test", "resources", "tei", "tei-katalog_Aurich.xml");
    try (InputStream is = newInputStream(teiFilePath)) {
      List<TEI> teiList = TEIObjectFactory.unmarshal(is);
      tei = teiList.get(0);
    }
  }

  @Test
  void testUpdateId() throws Exception {
    Katalog katalog1 = new Katalog.KatalogBuilder()
        .withId("HSP-123")
        .withHSKID("440")
        .withTEIXML(TEIObjectFactory.marshal(tei))
        .build();

    assertTrue(katalog1.getTeiXML().contains("<idno type=\"hsk\">440</idno>"));
    assertFalse(
        katalog1.getTeiXML().contains("<idno type=\"hsp\">HSP-123</idno>"));

    TEIKatalogCommand.updateId(katalog1);

    assertTrue(katalog1.getTeiXML().contains("<idno type=\"hsk\">440</idno>"));
    assertTrue(katalog1.getTeiXML().contains("<idno type=\"hsp\">HSP-123</idno>"));

    Katalog katalog2 = new Katalog.KatalogBuilder()
        .withId("HSP-456")
        .withHSKID("440")
        .withTEIXML(katalog1.getTeiXML())
        .build();

    TEIKatalogCommand.updateId(katalog2);

    assertTrue(katalog2.getTeiXML().contains("<idno type=\"hsk\">440</idno>"));
    assertFalse(katalog2.getTeiXML().contains("<idno type=\"hsp\">HSP-123</idno>"));
    assertTrue(katalog2.getTeiXML().contains("<idno type=\"hsp\">HSP-456</idno>"));
  }

}
