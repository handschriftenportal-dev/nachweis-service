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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Konrad.Eichstaedt@sbb.spk-berlin.de
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 09.07.19
 */
public class IdentifikationTest {

  public static final String SAMMLUNG_ID = UUID.randomUUID().toString();
  private static final String ID = UUID.randomUUID().toString();
  private static final IdentTyp IDENT_TYP = IdentTyp.GUELTIGE_SIGNATUR;
  private static final String IDENT = "12345";
  private static final NormdatenReferenz STAABI = new NormdatenReferenz("1234",
      "Staatsbibliothek zu Berlin", "GND-53424");
  private static final NormdatenReferenz BERLIN = new NormdatenReferenz("123", "Berlin", "GND-X1");

  public static Identifikation createGueltigeSignatur() {
    return new IdentifikationBuilder()
        .withId(ID)
        .addSammlungID(SAMMLUNG_ID)
        .withIdent(IDENT)
        .withIdentTyp(IDENT_TYP)
        .withAufbewahrungsOrt(BERLIN)
        .withBesitzer(STAABI)
        .build();
  }

  public static Identifikation createAlternativeSignatur() {
    return new IdentifikationBuilder()
        .withId(ID)
        .addSammlungID(SAMMLUNG_ID)
        .withIdent(IDENT)
        .withIdentTyp(IdentTyp.ALTSIGNATUR)
        .withAufbewahrungsOrt(BERLIN)
        .withBesitzer(STAABI)
        .build();
  }

  @Test
  void testEquals() {

    Identifikation identifikation_s147 = new IdentifikationBuilder()
        .withId(ID)
        .withIdent("S 147")
        .withBesitzer(STAABI)
        .withAufbewahrungsOrt(BERLIN)
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .build();

    Identifikation identifikation_s147E = new IdentifikationBuilder()
        .withId(ID)
        .withIdent("S 147")
        .withBesitzer(STAABI)
        .withAufbewahrungsOrt(BERLIN)
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .build();

    Identifikation identifikation_s148 = new IdentifikationBuilder()
        .withId(ID)
        .withIdent("S 148")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .build();

    assertNotEquals(identifikation_s147, identifikation_s148);

    assertEquals(identifikation_s147, identifikation_s147E);
  }

  @Test
  public void testIdentifikationBuilder() {
    Identifikation identifikation = createGueltigeSignatur();
    assertEquals(ID, identifikation.getId());
    assertEquals(1, identifikation.getSammlungIDs().size());
    assertEquals(IDENT, identifikation.getIdent());
    assertEquals(IDENT_TYP, identifikation.getIdentTyp());
    assertNotNull(identifikation.toString());
    assertEquals(BERLIN,identifikation.getAufbewahrungsOrt());
    assertEquals(STAABI,identifikation.getBesitzer());
  }

}
