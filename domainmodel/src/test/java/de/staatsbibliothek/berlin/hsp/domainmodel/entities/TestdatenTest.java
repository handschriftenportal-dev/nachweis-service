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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 16.08.19
 */
public class TestdatenTest {

  public final static NormdatenReferenz SPRACHE_DEUTSCH = new NormdatenReferenzBuilder()
      .withId(UUID.randomUUID().toString())
      .withGndID("4113292-0")
      .withName("Deutsch")
      .withTypeName("Language")
      .addVarianterName(new VarianterName("Neuhochdeutsch", "de"))
      .addVarianterName(new VarianterName("Deutsche Sprache", "de"))
      .addVarianterName(new VarianterName("Hochdeutsch", "de"))
      .addIdentifikator(new Identifikator("de", null, null))
      .addIdentifikator(new Identifikator("de", "http://id.loc.gov/vocabulary/iso639-1/de", "ISO_639-1"))
      .addIdentifikator(new Identifikator("deu", "http://id.loc.gov/vocabulary/iso639-2/deu", "ISO_639-2"))
      .addIdentifikator(new Identifikator("ger", "http://id.loc.gov/vocabulary/iso639-2/ger", "ISO_639-2"))
      .build();

  public static final Identifikation IDENTIFIKATION_SIGNATUR = createTestIdentifikation(
      "THEOL. LAT. FOL. 718", IdentTyp.GUELTIGE_SIGNATUR, "12345");

  public static final NormdatenReferenz MITTELLATEIN = new NormdatenReferenz("1", "Mittellatein", "4039691-5");

  public static KulturObjektDokument createTestKulturObjektDokument(String gndIdentifier,
      Identifikation identifikation, Beschreibung beschreibung) {
    KulturObjektDokumentBuilder kulturObjektDokument = new KulturObjektDokumentBuilder()
        .withId(String.valueOf(new Random().nextInt(100) + 1))
        .withGndIdentifier(gndIdentifier)
        .withRegistrierungsDatum(LocalDateTime.now())
        .withGueltigerIdentifikation(identifikation);

    if (Objects.nonNull(beschreibung)) {
      kulturObjektDokument.addBeschreibungsdokumentID(beschreibung.getId());
    }

    return kulturObjektDokument.build();
  }

  public static Identifikation createTestIdentifikation(String ident, IdentTyp identTyp, String sammlungId) {

    IdentifikationBuilder identifikationBuilder = new IdentifikationBuilder()
        .withId(UUID.randomUUID().toString())
        .addSammlungID(sammlungId)
        .withIdent(ident)
        .withIdentTyp(identTyp);
    return identifikationBuilder.build();
  }

  @Test
  public void testTestDaten() {

    Beschreibung beschreibung = createTestBeschreibungsdokument();

    KulturObjektDokument kulturObjektDokument = createTestKulturObjektDokument(
        "23456",
        IDENTIFIKATION_SIGNATUR,
        beschreibung
    );

    beschreibung.setKodID(kulturObjektDokument.getId());

    assertNotNull(kulturObjektDokument.toString());
    assertNotNull(beschreibung.toString());

    assertEquals(kulturObjektDokument.getId(), beschreibung.getKodID());

    KulturObjektDokument kulturObjektVirtuell = createTestKulturObjektDokument(
        "1877787-2",
        IDENTIFIKATION_SIGNATUR,
        null
    );

    assertEquals(IDENTIFIKATION_SIGNATUR, kulturObjektVirtuell.getGueltigeIdentifikation());
    assertNotNull(kulturObjektVirtuell.toString());

  }

  public Beschreibung createTestBeschreibungsdokument() {
    BeschreibungsBuilder builder = new BeschreibungsBuilder();

    builder
        .withId("BS1")
        .withKatalog("1")
        .addBeschreibungsBeteiligter(new NormdatenReferenz("1", "BECKER, Peter Jörg", ""));

    return builder.build();
  }
}
