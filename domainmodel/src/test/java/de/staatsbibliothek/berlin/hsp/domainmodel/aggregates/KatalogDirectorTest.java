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

package de.staatsbibliothek.berlin.hsp.domainmodel.aggregates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog.KatalogBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.02.2022.
 * @version 1.0
 */
public class KatalogDirectorTest {

  public static final String TITLE = "Title";
  public static final String TEI_XML = "XML";
  public static final String PUBLIKATIONS_JAHR = "1980";
  public static final String HSK_ID = "440";
  public static final NormdatenReferenz VERLAG = new NormdatenReferenz("Harrassowitz", "");

  @Test
  void testCreateSimpleHSKKatalog() {

    KatalogDirector director = new KatalogDirector(new KatalogBuilder());

    Katalog hskKatalog = director.createSimpleHSKKatalog(TITLE, TEI_XML, PUBLIKATIONS_JAHR, HSK_ID,
        VERLAG);

    assertNotNull(hskKatalog);

    assertEquals(TITLE, hskKatalog.getTitle());
    assertEquals(TEI_XML, hskKatalog.getTeiXML());
    assertEquals(PUBLIKATIONS_JAHR, hskKatalog.getPublikationsJahr());
    assertEquals(HSK_ID, hskKatalog.getHskID());
    assertEquals(VERLAG, hskKatalog.getVerlag());
  }
}
