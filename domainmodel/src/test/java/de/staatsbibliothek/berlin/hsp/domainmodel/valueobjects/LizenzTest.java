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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;
import org.junit.jupiter.api.Test;

public class LizenzTest {

  @Test
  public void testLizenzBuilder() {
    String uri_1 = "https://some.url/1";
    String uri_2 = "https://some.url/2";
    String uri_3 = "https://some.url/3";
    String text = "Beschreibungstext";

    Lizenz lizenz = new Lizenz.LizenzBuilder()
        .withUris(new LinkedHashSet<>(Arrays.asList(uri_1, uri_2)))
        .addUri(uri_3)
        .withBeschreibungsText(text)
        .build();

    assertNotNull(lizenz);
    assertNotNull(lizenz.getUris());
    assertEquals(3, lizenz.getUris().size());
    assertTrue(lizenz.getUris().stream().anyMatch(u -> uri_1.equals(u)));
    assertTrue(lizenz.getUris().stream().anyMatch(u -> uri_2.equals(u)));
    assertTrue(lizenz.getUris().stream().anyMatch(u -> uri_3.equals(u)));
    assertEquals(text, lizenz.getBeschreibungsText());
  }

  @Test
  public void testToString() {
    Lizenz lizenz = new Lizenz.LizenzBuilder()
        .withUris(null)
        .withBeschreibungsText("Beschreibungstext")
        .build();

    assertEquals("Lizenz{id='" + lizenz.getId() + "', uris=null, beschreibungsText='Beschreibungstext'}",
        lizenz.toString());
  }
}
