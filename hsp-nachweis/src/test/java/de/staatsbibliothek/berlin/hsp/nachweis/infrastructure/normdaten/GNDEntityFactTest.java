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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;


import static org.junit.jupiter.api.Assertions.assertEquals;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact.GNDEntityFactBuilder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 22.07.21
 */

public class GNDEntityFactTest {

  public static final String ID = UUID.randomUUID().toString();
  public static final String TYPE_NAME = "Language";
  public static final String PREFERRED_NAME = "Deutsch";
  public static final String GND_ID = "4113292-0";
  public static final VariantName VARIANT_NAME_1 = new VariantName("Neuhochdeutsch", "de");
  public static final VariantName VARIANT_NAME_2 = new VariantName("Deutsche Sprache", "de");
  public static final VariantName VARIANT_NAME_3 = new VariantName("Hochdeutsch", null);
  public static final Identifier IDENTIFIER_1
      = new Identifier("deu", "http://id.loc.gov/vocabulary/iso639-2/deu", "ISO_639-2");
  public static final Identifier IDENTIFIER_2
      = new Identifier("ger", "http://id.loc.gov/vocabulary/iso639-2/ger", "ISO_639-2");
  public static final Identifier IDENTIFIER_3 = new Identifier("de", null, null);

  @Test
  public void testGNDEntityFactBuilder() {
    GNDEntityFact german = new GNDEntityFactBuilder()
        .withId(ID)
        .withTypeName(TYPE_NAME)
        .withPreferredName(PREFERRED_NAME)
        .withGndIdentifier(GND_ID)
        .withIdentifier(new HashSet<>(Arrays.asList(IDENTIFIER_1, IDENTIFIER_2)))
        .addIdentifier(IDENTIFIER_3)
        .withVariantName(new HashSet<>(Arrays.asList(VARIANT_NAME_1, VARIANT_NAME_2)))
        .addVariantName(VARIANT_NAME_3)
        .build();

    assertEquals(ID, german.getId());
    assertEquals(TYPE_NAME, german.getTypeName());
    assertEquals(PREFERRED_NAME, german.getPreferredName());
    assertEquals(GND_ID, german.getGndIdentifier());
    assertEquals(3, german.getIdentifier().size());
    assertEquals(3, german.getVariantName().size());
    assertEquals(1, german.getIdentifierByType(null).size());
    assertEquals(2, german.getIdentifierByType("ISO_639-2").size());
    assertEquals(1, german.getVariantNameBylanguageCode(null).size());
    assertEquals(2, german.getVariantNameBylanguageCode("de").size());
  }
}
