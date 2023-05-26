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

package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import static de.staatsbibliothek.berlin.hsp.domainmodel.entities.TestdatenTest.MITTELLATEIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KulturObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Status;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 22.01.2020.
 * @version 1.0
 */
public class BeschreibungsKomponenteKopfTest {

  private Logger logger = Logger.getLogger(BeschreibungsKomponenteKopfTest.class.getName());

  @Test
  void testCreationWithBuilder() {

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withId("1")
        .withStatus(Status.VORHANDEN)
        .withKulturObjektTyp(KulturObjektTyp.DRUCK_MIT_HSL_ANTEILEN)
        .withTitel("Titel")
        .addGrundSprache(MITTELLATEIN)
        .build();

    assertNotNull(kopf);

    assertEquals("1", kopf.getId());

    assertEquals("Titel", kopf.getTitel());

    assertEquals(Status.VORHANDEN, kopf.getStatus());

    assertEquals(KulturObjektTyp.DRUCK_MIT_HSL_ANTEILEN, kopf.getKulturObjektTyp());

    assertNotNull(kopf.getGrundsprachen());

    assertTrue(kopf.getGrundsprachen().contains(MITTELLATEIN));

  }

  @Test
  void testToString() {

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withId("1")
        .withStatus(Status.VORHANDEN)
        .withKulturObjektTyp(KulturObjektTyp.DRUCK_MIT_HSL_ANTEILEN)
        .withTitel("Titel")
        .build();

    logger.log(Level.INFO, "Beschreibung Kopf " + kopf.toString());

    assertTrue(kopf.toString().contains("id='1'"));

    assertTrue(kopf.toString().contains("BeschreibungsKomponenteKopf"));
  }

  @Test
  void testEquals() {
    BeschreibungsKomponenteKopf kopf1 = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withId("1")
        .withStatus(Status.VORHANDEN)
        .withKulturObjektTyp(KulturObjektTyp.DRUCK_MIT_HSL_ANTEILEN)
        .withTitel("Titel")
        .build();

    BeschreibungsKomponenteKopf kopf2 = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .withId("2")
        .withStatus(Status.VORHANDEN)
        .withKulturObjektTyp(KulturObjektTyp.DRUCK_MIT_HSL_ANTEILEN)
        .withTitel("Titel")
        .build();

    assertNotEquals(kopf1, kopf2);
  }
}
