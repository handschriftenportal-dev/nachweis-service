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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 13.06.2019.
 */
public class BeschreibungsViewModelTest {

  @Test
  public void test_GivenValues_Creation() {
    final LocalDateTime erstellungsDatum = LocalDateTime.of(2020, 11, 15, 23, 10, 23);
    final LocalDateTime aenderungsDatum = LocalDateTime.of(2020, 12, 16, 22, 9, 22);
    final NormdatenReferenz deutsch = new NormdatenReferenz("17", "deutsch", "4113292-0",
        "Language");

    BeschreibungsViewModel view = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withImportJobID("1")
        .withID("2")
        .withTitel("Titel der Beschreibung")
        .withSignatur("Signatur")
        .withExterneID("123")
        .withKODView("KOD123")
        .withTEIXML("")
        .withVerwaltungsTyp(VerwaltungsTyp.INTERN)
        .withErstellungsDatum(erstellungsDatum)
        .withAenderungsDatum(aenderungsDatum)
        .withBeschreibungsSprache(deutsch)
        .withGedrucktemKatalog(true)
        .withHspPurl("https://resolver.staatsbibliothek-berlin.de/HSP0000002300000000")
        .build();

    assertEquals(view.getId(), "2");

    assertEquals(view.getImportJobID(), "1");

    assertEquals(view.getTitel(), "Titel der Beschreibung");

    assertEquals(view.getSignatur(), "Signatur");

    assertEquals(view.getExterneID(), "123");

    assertEquals(view.getErstellungsDatum(), erstellungsDatum);

    assertEquals(view.getAenderungsDatum(), aenderungsDatum);

    assertEquals(view.getBeschreibungsSprache(), deutsch);

    assertEquals(view.getExterneID(), "123");

    assertEquals(VerwaltungsTyp.INTERN, view.getVerwaltungsTyp());

    assertTrue(view.getAusGedrucktemKatalog());

    assertEquals(view.getHspPurl(), "https://resolver.staatsbibliothek-berlin.de/HSP0000002300000000");
  }

  @Test
  public void test_GivenBeschreibungen_Equals() {
    BeschreibungsViewModel view1 = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withImportJobID("1")
        .withID("2")
        .withTitel("Titel der Beschreibung")
        .withSignatur("Signatur")
        .withExterneID("123")
        .withKODView("KOD123")
        .withTEIXML("").build();

    BeschreibungsViewModel view2 = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withImportJobID("1")
        .withID("3")
        .withTitel("Titel der Beschreibung")
        .withSignatur("Signatur")
        .withExterneID("123")
        .withKODView("KOD123")
        .withTEIXML("").build();

    assertNotEquals(view1, view2);

  }

  @Test
  void testCreationWithEmptyLists() {
    BeschreibungsViewModel view1 = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withID("2").build();

    assertDoesNotThrow(view1::toString);
  }

  @Test
  void testCreationWith2Publications() {
    List<Publikation> publikations = List.of(
        new Publikation("12", LocalDateTime.of(1967, 1, 1, 0, 0), PublikationsTyp.ERSTPUBLIKATION),
        new Publikation("123", LocalDateTime.of(2020, 10, 20, 0, 0), PublikationsTyp.PUBLIKATION_HSP));
    BeschreibungsViewModel view1 = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withID("2")
        .withPublikationen(publikations).build();

    assertEquals(
        "[Publikation{id='12', datumDerVeroeffentlichung=1967-01-01T00:00, publikationsTyp=ERSTPUBLIKATION},"
            + " Publikation{id='123', datumDerVeroeffentlichung=2020-10-20T00:00, publikationsTyp=PUBLIKATION_HSP}]",
        Arrays.toString(view1.getPublikationen().toArray()));
  }

  @Test
  void testCreationWith2Beteiligte() {
    List<NormdatenReferenz> beteiligte = List.of(
        new NormdatenReferenz("12", "Klaus", "wedr", "Person"),
        new NormdatenReferenz("123", "Olaf", "gndid", "Person"));
    BeschreibungsViewModel view1 = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withID("2")
        .withBeteiligte(beteiligte).build();

    assertEquals(
        "[NormdatenReferenz{gndID='wedr', id='12', typeName='Person', name='Klaus', varianteNamen='}, NormdatenReferenz{gndID='gndid', id='123', typeName='Person', name='Olaf', varianteNamen='}]",
        Arrays.toString(view1.getBeteiligte().toArray()));
  }

  @Test
  public void testGetFormattedAutoren() {
    List<BeschreibungsAutorView> autoren = new ArrayList<BeschreibungsAutorView>();
    autoren.add(new BeschreibungsAutorView("Carter, Randolph", ""));
    autoren.add(new BeschreibungsAutorView("Armitage, Henry", ""));
    BeschreibungsViewModel view = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withID("1")
        .withAutoren(autoren)
        .build();

    assertEquals(2, view.getAutoren().size());
    assertEquals("Carter, Randolph; Armitage, Henry", view.getFormattedAutoren());

    view = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withID("2")
        .build();

    assertEquals(0, view.getAutoren().size());
    assertEquals("", view.getFormattedAutoren());
  }

  @Test
  public void testAddExternenLink() {
    BeschreibungsViewModel view = new BeschreibungsViewModel.BeschreibungsViewBuilder()
        .withID("1")
        .withExterneLinks(Arrays.asList("http://doi.org/123", "http://doi.org/456"))
        .addExternenLink("http://doi.org/789")
        .build();

    assertNotNull(view.getExterneLinks());
    assertEquals(3, view.getExterneLinks().size());
    assertTrue(view.getExterneLinks().contains("http://doi.org/123"));
    assertTrue(view.getExterneLinks().contains("http://doi.org/456"));
    assertTrue(view.getExterneLinks().contains("http://doi.org/789"));
  }

}
