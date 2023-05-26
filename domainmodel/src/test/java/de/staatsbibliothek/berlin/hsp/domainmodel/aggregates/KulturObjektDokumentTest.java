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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DigitalisatTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentifikationTest;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Referenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ReferenzQuelle;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ReferenzTyp;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * Testing Object for KulturObjektDokument.
 *
 * @author Konrad.Eichstaedt@sbb.spk-berlin.de
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 09.07.19
 */
public class KulturObjektDokumentTest {

  public static final LocalDateTime REGISTRIERUNGS_DATUM = LocalDateTime.now();
  public static final String ID = "testID";
  private static final Identifikation GUELTIGE_IDENTIFIKATION = IdentifikationTest.createGueltigeSignatur();
  private static final Identifikation ALTERNATIVE_IDENTIFIKATION = IdentifikationTest.createAlternativeSignatur();
  private static final String SIGNATUR = "S 247";

  private static final String NAME_BESITZENDE_EINRICHTUNG = "Universitäts- und Landesbibliothek Bonn";

  private static final String ORT_BESITZENDE_EINRICHTUNG = "Bonn";

  private static final Digitalisat DIGITALISAT = Digitalisat.DigitalisatBuilder().withID(UUID.randomUUID().toString())
      .withManifest("http://manifest.json")
      .withTyp(DigitalisatTyp.KOMPLETT_VOM_ORIGINAL)
      .withEntstehungsort(new NormdatenReferenz("1", ORT_BESITZENDE_EINRICHTUNG, ""))
      .build();

  private static final Referenz referenz = new Referenz("https://test.de", ReferenzTyp.BESCHREIBUNG,
      new ReferenzQuelle("e-codices", "ECODICES"));

  private static final PURL purl = new PURL(
      URI.create("https://resolver.staatsbibliothek-berlin.de/HSP0000002300000000"),
      URI.create("https://handschriftenportal.de/HSP-123"), PURLTyp.INTERNAL);

  public static KulturObjektDokument createTestKulturObjektDokument() {
    return new KulturObjektDokumentBuilder()
        .withId(ID)
        .withRegistrierungsDatum(REGISTRIERUNGS_DATUM)
        .withGueltigerIdentifikation(GUELTIGE_IDENTIFIKATION)
        .addAlternativeIdentifikation(ALTERNATIVE_IDENTIFIKATION)
        .addDigitalisat(DIGITALISAT)
        .withGndIdentifier("GND1")
        .withTEIXml("<xml></xml>")
        .withExterneReferenzen(new HashSet<>(List.of(referenz)))
        .addPURL(purl)
        .build();
  }

  @Test
  void createWithMinimumValues() {

    KulturObjektDokument kod = new KulturObjektDokumentBuilder("1")
        .build();

    assertEquals("1", kod.getId());
    assertNotNull(kod.getRegistrierungsDatum());
    assertNotNull(kod.getKomponenten());
    assertNotNull(kod.getAlternativeIdentifikationen());
    assertNotNull(kod.getBeschreibungenIDs());
    assertNotNull(kod.getDigitalisate());
    assertNotNull(kod.getTeiXML());
    assertNotNull(kod.getExterneReferenzen());
  }

  @Test
  void testWithGueltigeIdentifikation() {

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withId("1")
        .withGueltigerIdentifikation(GUELTIGE_IDENTIFIKATION)
        .build();

    assertEquals("1", kod.getId());

    assertEquals(GUELTIGE_IDENTIFIKATION, kod.getGueltigeIdentifikation());

    assertNotNull(kod.getRegistrierungsDatum());
  }

  @Test
  void testWithNOGueltigeIdentifikation() {

    Assertions.assertThrows(IllegalArgumentException.class, () -> new KulturObjektDokumentBuilder()
        .withId("1")
        .withGueltigerIdentifikation(ALTERNATIVE_IDENTIFIKATION)
        .build());
  }

  @Test
  void equalsTests() {

    Identifikation gueltigeSignatur_1 = new IdentifikationBuilder()
        .withId("1")
        .withIdent("S 247")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .build();

    Identifikation gueltigeSignatur_2 = new IdentifikationBuilder()
        .withId("2")
        .withIdent("S 248")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .build();

    KulturObjektDokument kod1 = new KulturObjektDokumentBuilder().withId("1")
        .withGueltigerIdentifikation(gueltigeSignatur_1)
        .build();
    KulturObjektDokument kod2 = new KulturObjektDokumentBuilder().withId("2")
        .withGueltigerIdentifikation(gueltigeSignatur_2)
        .build();

    assertNotEquals(kod1, kod2);

    kod2.setId("1");

    assertEquals(kod1,kod2);
  }

  @Test
  void createKulturObjektDokumentWithMinimalDaten() {

   NormdatenReferenz bonn = new NormdatenReferenz("1",ORT_BESITZENDE_EINRICHTUNG,"");

   NormdatenReferenz unibib = new NormdatenReferenz("2",NAME_BESITZENDE_EINRICHTUNG,"");

    Identifikation gueltigeSignatur = new IdentifikationBuilder()
        .withIdent(SIGNATUR)
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withBesitzer(unibib)
        .withAufbewahrungsOrt(bonn)
        .build();

    KulturObjektDokument kod = new KulturObjektDokumentBuilder()
        .withGueltigerIdentifikation(gueltigeSignatur)
        .build();

    assertNotNull(kod);

    assertNotNull(kod.getGueltigeIdentifikation());

    assertEquals(unibib,
        kod.getGueltigeIdentifikation().getBesitzer());

    assertEquals(bonn,
        kod.getGueltigeIdentifikation().getAufbewahrungsOrt());

    assertNotNull(kod.getRegistrierungsDatum());

    assertNotNull(kod.getRegistrierungsDatum());

    assertNotNull(kod.getExterneReferenzen());
  }

  @Test
  public void testBuilder() {

    KulturObjektDokument kod = createTestKulturObjektDokument();
    assertEquals(ID, kod.getId());
    assertEquals(REGISTRIERUNGS_DATUM, kod.getRegistrierungsDatum());
    assertNotNull(kod.getGueltigeIdentifikation());
    assertEquals(GUELTIGE_IDENTIFIKATION, kod.getGueltigeIdentifikation());
    assertNotNull(kod.toString());
    assertNotNull(kod.getAlternativeIdentifikationen());
    assertTrue(kod.getAlternativeIdentifikationen().contains(ALTERNATIVE_IDENTIFIKATION));
    assertNotNull(kod.getTeiXML());
    assertFalse(kod.getTeiXML().isEmpty());
    assertEquals(1, kod.getDigitalisate().size());
    assertFalse(kod.getGndIdentifier().isEmpty());
    assertTrue(kod.getExterneReferenzen().contains(referenz));
    assertFalse(kod.getPURLs().isEmpty());
    assertTrue(kod.getPURLs().contains(purl));
  }

  @Test
  public void testCreateWithKomponenten() {
    Identifikation identifikation = new IdentifikationBuilder()
        .withId("100")
        .withIdent("THEOL. LAT. FOL. 718")
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .build();

    KulturObjektKomponente komponente = new KulturObjektKomponente("1");

    KulturObjektDokument kulturObjektDokument = new KulturObjektDokumentBuilder()
        .withId("66")
        .withGndIdentifier("1877787-2")
        .withRegistrierungsDatum(LocalDateTime.now())
        .withGueltigerIdentifikation(identifikation)
        .addKomponente(komponente)
        .build();

    assertNotNull(kulturObjektDokument);

    assertTrue(kulturObjektDokument.getKomponenten().contains(komponente));
  }

  @Test
  void testRemoveExterneReferenz() {

    KulturObjektDokument kod = createTestKulturObjektDokument();

    kod.removeExterneReferenz(referenz);

    Assertions.assertTrue(kod.getExterneReferenzen().isEmpty());
  }

  @Test
  void testAdExterneReferenz() {

    KulturObjektDokument kod = new KulturObjektDokumentBuilder("1").build();

    Assertions.assertTrue(kod.getExterneReferenzen().isEmpty());

    kod.addExterneReferenz(referenz);

    assertTrue(kod.getExterneReferenzen().contains(referenz));
  }

  @Test
  void testRemovePURL() {
    KulturObjektDokument kod = createTestKulturObjektDokument();
    kod.removePURL(purl);
    Assertions.assertTrue(kod.getPURLs().isEmpty());
  }

  @Test
  void testAddPURL() {
    KulturObjektDokument kod = new KulturObjektDokumentBuilder("1").build();
    kod.addPURL(purl);
    assertTrue(kod.getPURLs().contains(purl));
  }

}
