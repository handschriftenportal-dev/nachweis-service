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

import static de.staatsbibliothek.berlin.hsp.domainmodel.entities.TestdatenTest.SPRACHE_DEUTSCH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung.BeschreibungsBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz.LizenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Referenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ReferenzQuelle;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 23.08.19
 */
public class BeschreibungTest {

  public static final String ID = "BS1";

  private static final Logger logger = Logger.getLogger(BeschreibungTest.class.getName());

  @Test
  void createWithMinimalValues() {
    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder("1", "KOD_1").build();

    assertEquals("1", beschreibung.getId());
    assertEquals("KOD_1", beschreibung.getKodID());
  }

  @Test
  void test_ToString() {

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .addBeschreibungsKomponente(
            new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder().build())
        .build();

    assertNotNull(beschreibung.toString());
  }


  @Test
  public void testBuilder() {

    Referenz hsa = new Referenz("https://test.de", ReferenzTyp.BESCHREIBUNG,
        new ReferenzQuelle("Manumed", "Manuscripta Medivalia"));
    NormdatenReferenz author_becker = new NormdatenReferenz("1", "BECKER, Peter Jörg", "");
    NormdatenReferenz author_bandis = new NormdatenReferenz("2", "Tilo BRANDIS", "");
    LocalDateTime erstellungsDatum = LocalDateTime.of(2008, 5, 23, 0, 0);
    LocalDateTime aenderungsDatum = LocalDateTime.of(2019, 5, 23, 0, 0);
    Publikation publikation = new Publikation("1", LocalDateTime.of(2020, 1, 1, 0, 0), PublikationsTyp.ERSTPUBLIKATION);

    Lizenz lizenz = new LizenzBuilder().addUri("https://creativecommons.org/publicdomain/mark/1.0/")
        .build();

    PURL purl = new PURL(URI.create("https://resolver.staatsbibliothek-berlin.de/HSP0000002300000000"),
        URI.create("https://handschriftenportal.de/HSP-123"), PURLTyp.INTERNAL);

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .withId("BS1")
        .withKodID("THEOL. LAT. FOL. 718")
        .addExterneID(hsa)
        .withTEIXml("<TEI></TEI>")
        .withBeschreibungssprache(SPRACHE_DEUTSCH)
        .withErstellungsDatum(erstellungsDatum)
        .withAenderungsDatum(aenderungsDatum)
        .addBeschreibungsBeteiligter(author_becker)
        .addBeschreibungsBeteiligter(author_bandis)
        .addPublikation(publikation)
        .withKatalog("Katalog 1")
        .addAltIdentifier("mstest")
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .withLizenz(lizenz)
        .withDokumentObjectTyp(DokumentObjektTyp.HSP_DESCRIPTION)
        .addPURL(purl)
        .build();

    assertEquals("BS1", beschreibung.getId());
    assertEquals("THEOL. LAT. FOL. 718", beschreibung.getKodID());
    assertTrue(beschreibung.getExterneIDs().contains(hsa));
    assertEquals(SPRACHE_DEUTSCH, beschreibung.getBeschreibungsSprache());
    assertEquals(erstellungsDatum, beschreibung.getErstellungsDatum());
    assertEquals(aenderungsDatum, beschreibung.getAenderungsDatum());
    assertEquals(2, beschreibung.getBeschreibungsBeteiligte().size());
    beschreibung.removeBeschreibungsBeteiligter(author_becker);
    assertEquals(1, beschreibung.getBeschreibungsBeteiligte().size());
    beschreibung.removeBeschreibungsBeteiligter(author_bandis);
    assertEquals(0, beschreibung.getBeschreibungsBeteiligte().size());
    assertTrue(beschreibung.getPublikationen().contains(publikation));
    assertEquals("<TEI></TEI>", beschreibung.getTeiXML());
    assertNotNull(beschreibung.toString());
    assertTrue(beschreibung.getAltIdentifier().contains("mstest"));
    assertEquals("Katalog 1", beschreibung.getKatalogID());
    assertEquals(VerwaltungsTyp.INTERN, beschreibung.getVerwaltungsTyp());
    assertEquals(lizenz, beschreibung.getLizenz());
    assertEquals(DokumentObjektTyp.HSP_DESCRIPTION, beschreibung.getDokumentObjektTyp());
    assertNotNull(beschreibung.getPURLs());
    assertEquals(1, beschreibung.getPURLs().size());
    assertTrue(beschreibung.getPURLs().contains(purl));

    logger.log(Level.INFO, "Beschreibungs Test " + beschreibung);
  }

  @Test
  void testisBeschreibungIntern() {

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder().build();

    Assertions.assertFalse(beschreibung.isBeschreibungIntern());

    beschreibung.makeBeschreibungIntern();

    assertTrue(beschreibung.isBeschreibungIntern());

    beschreibung.makeBeschreibungExtern();

    Assertions.assertFalse(beschreibung.isBeschreibungIntern());
  }

  @Test
  void testGetGueltigeIdentifikation() {

    Identifikation gueltigeIdentifikation = new Identifikation.IdentifikationBuilder()
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(new NormdatenReferenz("1", "SBB", "GND1"))
        .withAufbewahrungsOrt(new NormdatenReferenz("2", "Berlin", "GND2"))
        .build();

    BeschreibungsKomponenteKopf kopf = new BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder()
        .build();
    kopf.getIdentifikationen().add(gueltigeIdentifikation);

    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder()
        .addBeschreibungsKomponente(kopf)
        .build();

    assertTrue(beschreibung.getGueltigeIdentifikation().isPresent());
  }

  @Test
  void testBeschreibungWithAutoren() {
    BeschreibungsBuilder builder = new Beschreibung.BeschreibungsBuilder();
    Beschreibung beschreibung = builder.build();

    assertNotNull(beschreibung);
    assertNotNull(beschreibung.getAutoren());

    builder.addAutor(new NormdatenReferenz("Test", "GNDID"));

    beschreibung = builder.build();

    assertEquals(1, beschreibung.getAutoren().size());
  }
}
