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

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.AltIdentifier;
import org.tei_c.ns._1.MsIdentifier;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 20.05.2020.
 * @version 1.0
 */
public class MsIdentifierFactoryTest {

  @Test
  void testMappingIdentifier() {

    NormdatenReferenz besitzer = new NormdatenReferenz("12345", "Staatsbibliothek zu Berlin", "");
    NormdatenReferenz aufbewahrungsort = new NormdatenReferenz("234", "Berlin", "");

    Identifikation identifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("MS 67")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(aufbewahrungsort)
        .build();

    Identifikation alternativeIdentifikation = new Identifikation.IdentifikationBuilder()
        .withIdent("MS 67 Alternative")
        .withIdentTyp(IdentTyp.ALTSIGNATUR)
        .withBesitzer(besitzer)
        .withAufbewahrungsOrt(aufbewahrungsort)
        .build();

    MsIdentifier msIdentifier = MsIdentifierFactory
        .build(identifikation, Set.of(alternativeIdentifikation));

    assertNotNull(msIdentifier.getIdnos());

    assertEquals(1, msIdentifier.getIdnos().size());

    assertEquals(identifikation.getIdent(), msIdentifier.getIdnos().get(0).getContent().get(0));

    assertEquals(besitzer.getName(), msIdentifier.getRepository().getContent().get(0));

    assertEquals(besitzer.getId(), msIdentifier.getRepository().getKey());

    assertEquals(aufbewahrungsort.getName(), msIdentifier.getSettlement().getContent().get(0));

    assertEquals(aufbewahrungsort.getId(),
        msIdentifier.getSettlement().getKey());

    assertEquals(1, msIdentifier.getMsNamesAndObjectNamesAndAltIdentifiers().size());

    assertEquals(alternativeIdentifikation.getIdent(),
        ((AltIdentifier) msIdentifier.getMsNamesAndObjectNamesAndAltIdentifiers().get(0)).getIdno()
            .getContent().get(0));

    assertEquals(TEIValues.ALTERNATIVE_IDNO_TYPE,
        ((AltIdentifier) msIdentifier.getMsNamesAndObjectNamesAndAltIdentifiers().get(0))
            .getType());
  }
}
