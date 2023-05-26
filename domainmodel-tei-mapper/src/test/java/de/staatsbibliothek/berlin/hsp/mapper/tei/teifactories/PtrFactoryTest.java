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
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Ptr;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.08.22.
 * @version 1.0
 */
public class PtrFactoryTest {

  public static final String PURL = "https://resolver.staatsbibliothek-berlin.de/HSP1660133922141000";
  public static final String TARGET = "http://b-dev1047:8080/search?hspobjectid=HSP-00b9f2b8-f1df-32b8-9ccc-2d76cfe17d42";

  @Test
  void testBuildPtr() throws URISyntaxException {
    PURL sbbPURL = new PURL(new URI(PURL), new URI(TARGET), PURLTyp.INTERNAL);

    Ptr pointer = PtrFactory.build(sbbPURL);

    assertNotNull(pointer);
    assertEquals(pointer.getId(), sbbPURL.getId());
    assertEquals(PURL, pointer.getTargets().stream().collect(Collectors.joining()));
    assertTrue(PtrFactory.TYPE.equals(pointer.getType()));
    assertTrue(PtrFactory.SUBTYPE.equals(pointer.getSubtype()));
  }
}
