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

package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import org.junit.jupiter.api.Test;

public class PURLTest {

  static final String ID = "123";
  static final URI PURL_URI = URI.create("https://resolver.url/HSP-123");
  static final URI TARGET_URI = URI.create("https://target.url/HSP-123");
  static final PURLTyp PURL_TYP = PURLTyp.INTERNAL;

  @Test
  void testConstruct() {
    PURL purl = new PURL(PURL_URI, TARGET_URI, PURL_TYP);
    assertNotNull(purl);
    assertNotNull(purl.getId());
    assertEquals(PURL_URI, purl.getPurl());
    assertEquals(TARGET_URI, purl.getTarget());
    assertEquals(PURL_TYP, purl.getTyp());
    assertEquals("PURL{id='HSP-f0ddaf97-7830-321d-8337-b1d44173ccfd', purl='https://resolver.url/HSP-123', "
            + "target='https://target.url/HSP-123', typ=INTERNAL}",
        purl.toString());

    PURL purlWithId = new PURL(ID, PURL_URI, TARGET_URI, PURL_TYP);
    assertNotNull(purlWithId);
    assertEquals(ID, purlWithId.getId());
    assertEquals(PURL_URI, purlWithId.getPurl());
    assertEquals(TARGET_URI, purlWithId.getTarget());
    assertEquals(PURL_TYP, purlWithId.getTyp());
    assertEquals("PURL{id='123', purl='https://resolver.url/HSP-123', target='https://target.url/HSP-123', "
        + "typ=INTERNAL}", purlWithId.toString());

    assertFalse(purlWithId.equals(purl));

    purlWithId.setTarget(URI.create("https://target.url/HSP-1234"));
    assertNotEquals(TARGET_URI, purlWithId.getTarget());
    assertEquals(URI.create("https://target.url/HSP-1234"), purlWithId.getTarget());
  }

}
