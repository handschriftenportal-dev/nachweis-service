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

package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.wildfly.common.Assert.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Beschreibstoff;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Buchschmuck;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Handschriftentyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Musiknotation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Status;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Titel;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Umfang;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AbmessungWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.EntstehungszeitWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.FormatWert;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel.KulturObjektDokumentViewModelBuilder;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.primefaces.model.StreamedContent;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 15.09.2022
 */
class KulturObjektDokumentViewModelTest {

  @Test
  void testBuilder() {
    Titel titel = createTitel();
    Entstehungsort entstehungsort = buildEntstehungsort();
    Entstehungszeit entstehungszeit = buildEntstehungszeit();
    Beschreibstoff beschreibstoff = createBeschreibstoff();
    Umfang umfang = createUmfang();
    Abmessung abmessung = createAbmessung();
    Format format = createFormat();
    Grundsprache grundsprache = createGrundSprache();
    Buchschmuck buchschmuck = createBuchschmuck();
    Handschriftentyp handschriftentyp = createHandschriftentyp();
    Musiknotation musiknotation = createMusiknotation();
    Status status = createStatus();

    KulturObjektDokumentViewModel kodViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel()
        .addAlternativeSignatur("altSignatur_1")
        .addAlternativeSignatur("altSignatur_2")
        .addBeschreibung(new BeschreibungsViewModel.BeschreibungsViewBuilder().withID("b1").build())
        .addBeschreibung(new BeschreibungsViewModel.BeschreibungsViewBuilder().withID("b2").build())
        .addDigitalisat(new DigitalisatViewModel.DigitalisatViewModelBuilder().id("d1").build())
        .addDigitalisat(new DigitalisatViewModel.DigitalisatViewModelBuilder().id("d2").build())
        .withAbmessung(abmessung)
        .withBeschreibstoff(beschreibstoff)
        .withBestandhaltendeInstitutionName("BestandhaltendeInstitutionName")
        .withBestandhaltendeInstitutionOrt("BestandhaltendeInstitutionOrt")
        .withBuchschmuck(buchschmuck)
        .withEntstehungsort(entstehungsort)
        .withEntstehungszeit(entstehungszeit)
        .withFormat(format)
        .withUmfang(umfang)
        .withHspPurl("HspPurl")
        .withId("Id")
        .withMusiknotation(musiknotation)
        .withHandschriftentyp(handschriftentyp)
        .withSignatur("Signatur")
        .withGrundSprache(grundsprache)
        .withStatus(status)
        .withTeiXML("<TEI/>")
        .withTitel(titel)
        .withExterneLinks(Arrays.asList("el1", "el2"))
        .addExternenLink("el3")
        .build();

    assertEquals(abmessung, kodViewModel.getAbmessung());
    assertEquals(beschreibstoff, kodViewModel.getBeschreibstoff());
    assertEquals("BestandhaltendeInstitutionName", kodViewModel.getBestandhaltendeInstitutionName());
    assertEquals("BestandhaltendeInstitutionOrt", kodViewModel.getBestandhaltendeInstitutionOrt());
    assertEquals(buchschmuck, kodViewModel.getBuchschmuck());
    assertEquals(entstehungsort, kodViewModel.getEntstehungsort());
    assertEquals(entstehungszeit, kodViewModel.getEntstehungszeit());
    assertEquals(format, kodViewModel.getFormat());
    assertEquals(umfang, kodViewModel.getUmfang());
    assertEquals("HspPurl", kodViewModel.getHspPurl());
    assertEquals("Id", kodViewModel.getId());
    assertEquals(musiknotation, kodViewModel.getMusiknotation());
    assertEquals(handschriftentyp, kodViewModel.getHandschriftentyp());
    assertEquals("Signatur", kodViewModel.getSignatur());
    assertEquals(grundsprache, kodViewModel.getGrundsprache());
    assertEquals(status, kodViewModel.getStatus());
    assertEquals("<TEI/>", kodViewModel.getTeiXML());
    assertEquals(titel, kodViewModel.getTitel());

    assertNotNull(kodViewModel.getAlternativeSignaturen());
    assertEquals(2, kodViewModel.getAlternativeSignaturen().size());
    assertTrue(kodViewModel.getAlternativeSignaturen().contains("altSignatur_1"));
    assertTrue(kodViewModel.getAlternativeSignaturen().contains("altSignatur_2"));

    assertNotNull(kodViewModel.getBeschreibungen());
    assertEquals(2, kodViewModel.getBeschreibungen().size());
    assertTrue(kodViewModel.getBeschreibungen().stream().anyMatch(b -> "b1".equals(b.getId())));
    assertTrue(kodViewModel.getBeschreibungen().stream().anyMatch(b -> "b2".equals(b.getId())));

    assertNotNull(kodViewModel.getDigitalisate());
    assertEquals(2, kodViewModel.getDigitalisate().size());
    assertTrue(kodViewModel.getDigitalisate().stream().anyMatch(d -> "d1".equals(d.getId())));
    assertTrue(kodViewModel.getDigitalisate().stream().anyMatch(d -> "d2".equals(d.getId())));

    assertNotNull(kodViewModel.getExterneLinks());
    assertEquals(3, kodViewModel.getExterneLinks().size());
    assertTrue(kodViewModel.getExterneLinks().contains("el1"));
    assertTrue(kodViewModel.getExterneLinks().contains("el2"));
    assertTrue(kodViewModel.getExterneLinks().contains("el3"));
  }

  @Test
  void testGetTeiFile() {
    KulturObjektDokumentViewModel kodViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel()
        .withId("ID")
        .build();
    assertNull(kodViewModel.getTeiFile());

    kodViewModel = KulturObjektDokumentViewModelBuilder
        .KulturObjektDokumentViewModel()
        .withId("ID")
        .withTeiXML("TEIXML")
        .build();

    StreamedContent streamedContent = kodViewModel.getTeiFile();
    assertNotNull(streamedContent);
    assertEquals("tei-ID.xml", streamedContent.getName());
    assertEquals("text/xml", streamedContent.getContentType());
    assertNotNull(streamedContent.getStream());
  }

  private Titel createTitel() {
    return Titel.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("b1")
        .withText("Titel der Beschreibung")
        .build();
  }

  private Entstehungsort buildEntstehungsort() {
    return Entstehungsort.builder()
        .withReferenzId("b1")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withText("Entstehungsort")
        .build();
  }

  private Entstehungszeit buildEntstehungszeit() {
    return Entstehungszeit.builder()
        .withReferenzId("b1")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .addEntstehungszeitWert(EntstehungszeitWert.builder()
            .withText("um 1200")
            .withNichtVor("1190")
            .withNichtNach("1210")
            .withTyp("datable")
            .build())
        .build();
  }

  private Beschreibstoff createBeschreibstoff() {
    return Beschreibstoff.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("b1")
        .withText("Pergament, Papyrus")
        .withTypen(List.of("parchment", "papyrus"))
        .build();
  }

  private Umfang createUmfang() {
    return Umfang.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("b1")
        .withText("I + 394 Bl.")
        .withBlattzahl("394")
        .build();
  }

  private Abmessung createAbmessung() {
    return Abmessung.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .addAbmessungWert(AbmessungWert.builder()
            .withText("23,5 × 16,5 x 2,5")
            .withHoehe("23,5")
            .withBreite("16,5")
            .withTiefe("2,5")
            .withTyp("factual")
            .build())
        .build();
  }

  private Format createFormat() {
    return Format.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .addFormatWert(FormatWert.builder()
            .withText("long and narrow")
            .withTyp("deduced")
            .build())
        .build();
  }

  private Grundsprache createGrundSprache() {
    return Grundsprache.builder()
        .withReferenzId("HSP-123")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withText("Mittelhochdeutsch")
        .addNormdatenId("4039687-3")
        .build();
  }

  private Buchschmuck createBuchschmuck() {
    return Buchschmuck.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("HSP-123")
        .withText("yes")
        .build();
  }

  private Handschriftentyp createHandschriftentyp() {
    return Handschriftentyp.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("BS-1")
        .withText("composite")
        .build();
  }

  private Musiknotation createMusiknotation() {
    return Musiknotation.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("b1")
        .withText("yes")
        .build();
  }

  private Status createStatus() {
    return Status.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("b1")
        .withText("displaced")
        .build();
  }

}
