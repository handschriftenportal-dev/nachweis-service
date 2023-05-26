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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 07.10.2020.
 * @version 1.0
 */
public class AutocompleteNormdatenControllerTest {

  NormdatenReferenzBoundary normdatenReferenzBoundary = Mockito.mock(NormdatenReferenzBoundary.class);

  @Test
  void testAutocompleteKoerperschaft() {
    NormdatenReferenz stabi = new NormdatenReferenz("1", "Staatsbibliothek zu Berlin", "5036103-X",
        NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);

    when(normdatenReferenzBoundary.findAllByIdOrNameAndType("Berlin", NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME,
        false)).thenReturn(Set.of(stabi));

    AutocompleteNormdatenController controller = new AutocompleteNormdatenController(normdatenReferenzBoundary);
    assertEquals(1, controller.autocompleteKoerperschaft("Berlin").size());
    assertEquals(0, controller.autocompleteKoerperschaft("Stadtbibliothek").size());
    verify(normdatenReferenzBoundary, times(2)).findAllByIdOrNameAndType(any(), any(), anyBoolean());
  }

  @Test
  void testAutocompletePerson() {
    NormdatenReferenz luther = new NormdatenReferenz("1", "Luther, Martin", "118575449",
        NormdatenReferenz.PERSON_TYPE_NAME);

    when(normdatenReferenzBoundary.findAllByIdOrNameAndType("Martin", NormdatenReferenz.PERSON_TYPE_NAME,
        false)).thenReturn(Set.of(luther));

    AutocompleteNormdatenController controller = new AutocompleteNormdatenController(normdatenReferenzBoundary);
    assertEquals(1, controller.autocompletePerson("Martin").size());
    assertEquals(0, controller.autocompletePerson("Achim").size());
    verify(normdatenReferenzBoundary, times(2)).findAllByIdOrNameAndType(any(), any(), anyBoolean());
  }

  @Test
  void testAutocompleteOrt() {
    NormdatenReferenz berlin = new NormdatenReferenz("1", "Berlin", "4005728-8",
        GNDEntityFact.PLACE_TYPE_NAME);

    when(normdatenReferenzBoundary.findAllByIdOrNameAndType("Berlin", NormdatenReferenz.ORT_TYPE_NAME,
        false)).thenReturn(Set.of(berlin));

    AutocompleteNormdatenController controller = new AutocompleteNormdatenController(normdatenReferenzBoundary);
    assertEquals(1, controller.autocompleteOrt("Berlin").size());
    assertEquals(0, controller.autocompleteOrt("Bonn").size());
    verify(normdatenReferenzBoundary, times(2)).findAllByIdOrNameAndType(any(), any(), anyBoolean());
  }

}
