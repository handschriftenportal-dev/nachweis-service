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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VarianterName;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Author: Christoph Marten on 11.03.2021 at 16:34
 */
class PersonModelTest {

  NormdatenReferenzBoundary normdatenReferenzBoundary = Mockito.mock(NormdatenReferenzBoundary.class);

  PersonModel personModel;

  NormdatenReferenz busch;

  NormdatenReferenz berg;

  @BeforeEach
  void onSetup() {

    busch = new NormdatenReferenzBuilder()
        .withId("12")
        .addVarianterName(new VarianterName("beusche", "de"))
        .addVarianterName(new VarianterName("buscha", "de"))
        .build();

    berg = new NormdatenReferenzBuilder()
        .withId("123")
        .withGndID("GNDIdent")
        .build();
    when(normdatenReferenzBoundary.findAllByIdOrNameAndType(null, NormdatenReferenz.PERSON_TYPE_NAME, true))
        .thenReturn(Set.of(busch, berg));

    personModel = new PersonModel(normdatenReferenzBoundary);
  }

  @Test
  void setup() {
    personModel.setup();
    assertEquals(2, personModel.getNormdatenPersonViews().size());
  }

  @Test
  void alternativeNamen() {
    personModel.setup();
    String alternativeNames = busch.getVarianteNamenAlsString();

    assertEquals("beusche; buscha", alternativeNames);
  }

}
