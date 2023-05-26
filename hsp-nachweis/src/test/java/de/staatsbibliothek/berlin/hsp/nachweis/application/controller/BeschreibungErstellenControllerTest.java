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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikator;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.LizenzTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import java.util.LinkedHashSet;
import java.util.UUID;
import javax.faces.application.FacesMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.primefaces.PrimeFaces;
import org.primefaces.PrimeFaces.Dialog;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.05.2021.
 * @version 1.0
 */

public class BeschreibungErstellenControllerTest {

  BeschreibungErstellenController beschreibungErstellenController;

  NormdatenReferenzBoundary normdatenReferenzBoundary = mock(NormdatenReferenzBoundary.class);

  KulturObjektDokumentBoundary kulturObjektDokumentBoundary = mock(
      KulturObjektDokumentBoundary.class);

  BeschreibungsBoundary beschreibungsBoundary = mock(BeschreibungsBoundary.class);

  BearbeiterBoundary bearbeiterBoundary = mock(BearbeiterBoundary.class);

  MockedStatic<PrimeFaces> primefacesStatic = mockStatic(PrimeFaces.class);
  PrimeFaces primefaces = mock(PrimeFaces.class);
  Dialog primefacesDialog = mock(Dialog.class);

  @BeforeEach
  void beforeEach() {
    primefacesStatic.when(PrimeFaces::current).thenReturn(primefaces);
    when(primefaces.dialog()).thenReturn(primefacesDialog);
  }

  @AfterEach
  public void resetMocks() {
    primefacesStatic.close();
  }

  @Test
  void testCreation() {

    beschreibungErstellenController = new BeschreibungErstellenController(normdatenReferenzBoundary,
        kulturObjektDokumentBoundary, beschreibungsBoundary, bearbeiterBoundary);

    assertNotNull(beschreibungErstellenController);
    assertNotNull(beschreibungErstellenController.getBeschreibungsSprachen());
    assertNotNull(beschreibungErstellenController.getBeschreibungsSprachenNormdatenReferenzMap());
    assertNotNull(beschreibungErstellenController.getTemplates());
  }

  @Test
  void testSetup() {
    beschreibungErstellenController = new BeschreibungErstellenController(normdatenReferenzBoundary,
        kulturObjektDokumentBoundary, beschreibungsBoundary, bearbeiterBoundary);

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder().withId("12")
        .build();

    beschreibungErstellenController.setKodID(kod.getId());

    LinkedHashSet<NormdatenReferenz> sprachen = new LinkedHashSet<>();
    sprachen.add(createNormdatenReferenzZulu());
    sprachen.add(createNormdatenReferenzGerman());

    when(normdatenReferenzBoundary.findAllByIdOrNameAndType(null, NormdatenReferenz.SPRACHE_TYPE_NAME, true))
        .thenReturn(sprachen);

    beschreibungErstellenController.setup();

    assertEquals(2, beschreibungErstellenController.getBeschreibungsSprachen().size());
    assertEquals("Deutsch,Zulu",
        String.join(",", beschreibungErstellenController.getBeschreibungsSprachen().keySet()));

    assertEquals(2, beschreibungErstellenController.getBeschreibungsSprachenNormdatenReferenzMap().size());
    assertEquals(2, beschreibungErstellenController.getTemplates().size());
  }

  @Test
  void testSetSelectedAutor() {
    beschreibungErstellenController = new BeschreibungErstellenController(normdatenReferenzBoundary,
        kulturObjektDokumentBoundary, beschreibungsBoundary, bearbeiterBoundary);

    assertEquals(0, beschreibungErstellenController.getSelectedAutoren().size());

    beschreibungErstellenController.setSelectedAutor(
        new NormdatenReferenz("1", "GND", "Klaus", "Person"));

    assertEquals(1, beschreibungErstellenController.getSelectedAutoren().size());
    assertNull(beschreibungErstellenController.getSelectedAutor());
  }

  @Test
  void testAutorEntfernen() {
    beschreibungErstellenController = new BeschreibungErstellenController(normdatenReferenzBoundary,
        kulturObjektDokumentBoundary, beschreibungsBoundary, bearbeiterBoundary);

    NormdatenReferenz autorKlaus = new NormdatenReferenz("1", "Klaus", "GND", "Person");

    beschreibungErstellenController.getSelectedAutoren().add(autorKlaus);

    beschreibungErstellenController.autorEntfernen(autorKlaus);

    assertEquals(0, beschreibungErstellenController.getSelectedAutoren().size());
  }

  @Test
  void testGetKodBesitzer() {
    beschreibungErstellenController = new BeschreibungErstellenController(normdatenReferenzBoundary,
        kulturObjektDokumentBoundary, beschreibungsBoundary, bearbeiterBoundary);

    assertEquals("", beschreibungErstellenController.getKodBesitzer("12"));
  }

  @Test
  void testGetKodAufbewahrungsOrt() {
    beschreibungErstellenController = new BeschreibungErstellenController(normdatenReferenzBoundary,
        kulturObjektDokumentBoundary, beschreibungsBoundary, bearbeiterBoundary);

    assertEquals("", beschreibungErstellenController.getKodAufbewahrungsOrt("12"));
  }

  @Test
  void testGetKodSignatur() {
    beschreibungErstellenController = new BeschreibungErstellenController(normdatenReferenzBoundary,
        kulturObjektDokumentBoundary, beschreibungsBoundary, bearbeiterBoundary);

    assertEquals("", beschreibungErstellenController.getKodSignatur("12"));
  }

  @Test
  void testgetKodAufbewahrungsOrtWithNull() {
    beschreibungErstellenController = new BeschreibungErstellenController(normdatenReferenzBoundary,
        kulturObjektDokumentBoundary, beschreibungsBoundary, bearbeiterBoundary);

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder().withId("12")
        .build();

    when(kulturObjektDokumentBoundary.findById("12")).thenReturn(java.util.Optional.of(kod));

    assertEquals("", beschreibungErstellenController.getKodAufbewahrungsOrt("12"));
  }

  @Test
  void testgetKodAufbewahrungsOrtWithNull2() {
    beschreibungErstellenController = new BeschreibungErstellenController(normdatenReferenzBoundary,
        kulturObjektDokumentBoundary, beschreibungsBoundary, bearbeiterBoundary);

    KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder().withId("12")
        .withGueltigerIdentifikation(new Identifikation.IdentifikationBuilder().withId("1")
            .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR).build()).build();

    when(kulturObjektDokumentBoundary.findById("12")).thenReturn(java.util.Optional.of(kod));

    assertEquals("", beschreibungErstellenController.getKodAufbewahrungsOrt("12"));
  }

  @Test
  void testdialogSchliessen() {
    beschreibungErstellenController = new BeschreibungErstellenController(normdatenReferenzBoundary,
        kulturObjektDokumentBoundary, beschreibungsBoundary, bearbeiterBoundary);

    beschreibungErstellenController.dialogSchliessen("neueBeschreibungId");
  }

  @Test
  void testbeschreibungAnlegen() throws BeschreibungsException {
    when(beschreibungsBoundary.beschreibungErstellen(any(), any(), any(), any(), any(), any(),
        any()))
        .thenReturn(new Beschreibung.BeschreibungsBuilder().withId("1").build());

    beschreibungErstellenController = new BeschreibungErstellenController(normdatenReferenzBoundary,
        kulturObjektDokumentBoundary, beschreibungsBoundary, bearbeiterBoundary);
    beschreibungErstellenController.setLizenzTyp(LizenzTyp.CC_0);
    beschreibungErstellenController.beschreibungAnlegen();

    verify(beschreibungsBoundary,
        times(1)).beschreibungErstellen(any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void testbeschreibungAnlegen_fehlerNormdaten() throws BeschreibungsException {
    when(beschreibungsBoundary.beschreibungErstellen(any(), any(), any(), any(), any(), any(),
        any()))
        .thenReturn(new Beschreibung.BeschreibungsBuilder().withId("1").build());

    beschreibungErstellenController = new BeschreibungErstellenController(normdatenReferenzBoundary,
        kulturObjektDokumentBoundary, beschreibungsBoundary, bearbeiterBoundary);
    beschreibungErstellenController.setLizenzTyp(LizenzTyp.CC_0);

    beschreibungErstellenController.setSelectedAutor(
        new NormdatenReferenz("1", "GND", "Klaus", "Person"));

    beschreibungErstellenController.beschreibungAnlegen();

    verify(primefacesDialog, times(1)).showMessageDynamic(any(FacesMessage.class));
  }

  private NormdatenReferenz createNormdatenReferenzGerman() {
    return new NormdatenReferenz.NormdatenReferenzBuilder()
        .withId(UUID.nameUUIDFromBytes("de".getBytes()).toString())
        .withTypeName("Language")
        .withName("Deutsch")
        .addVarianterName(new VarianterName("Neuhochdeutsch", "de"))
        .addVarianterName(new VarianterName("Deutsche Sprache", "de"))
        .addVarianterName(new VarianterName("Hochdeutsch", "de"))
        .addIdentifikator(new Identifikator("de", "", ""))
        .addIdentifikator(
            new Identifikator("ger", "http://id.loc.gov/vocabulary/iso639-2/ger", "ISO_639-2"))
        .addIdentifikator(
            new Identifikator("deu", "http://id.loc.gov/vocabulary/iso639-2/deu", "ISO_639-2"))
        .addIdentifikator(
            new Identifikator("de", "http://id.loc.gov/vocabulary/iso639-1/de", "ISO_639-1"))
        .withGndID("4113292-0")
        .build();
  }

  private NormdatenReferenz createNormdatenReferenzZulu() {
    return new NormdatenReferenz.NormdatenReferenzBuilder()
        .withId(UUID.nameUUIDFromBytes("zu".getBytes()).toString())
        .withTypeName("Language")
        .withName("Zulu")
        .addVarianterName(new VarianterName("isiZulu", "de"))
        .addIdentifikator(new Identifikator("zu", "", ""))
        .addIdentifikator(
            new Identifikator("zul", "http://id.loc.gov/vocabulary/iso639-2/zul", "ISO_639-2"))
        .addIdentifikator(
            new Identifikator("zu", "http://id.loc.gov/vocabulary/iso639-1/zu", "ISO_639-1"))
        .build();
  }
}
