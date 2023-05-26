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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.11.2019.
 * @version 1.0
 */
public class BeschreibungsRepositoryTest {

  @Test
  public void testsave() {
    BeschreibungsRepository beschreibungsRepository = Mockito.mock(BeschreibungsRepository.class);
    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder().withId("123")
        .build();

    Mockito.when((beschreibungsRepository.save(beschreibung))).thenReturn(beschreibung);
    Mockito.when((beschreibungsRepository.findById("123"))).thenReturn(beschreibung);
    beschreibungsRepository.save(beschreibung);

    assertNotNull(beschreibungsRepository.findById("123"));
  }

  @Test
  public void testfindById() {
    BeschreibungsRepository beschreibungsRepository = Mockito.mock(BeschreibungsRepository.class);
    Beschreibung beschreibung = new Beschreibung.BeschreibungsBuilder().withId("123")
        .build();

    Mockito.when((beschreibungsRepository.save(beschreibung))).thenReturn(beschreibung);
    beschreibungsRepository.save(beschreibung);

    Mockito.when((beschreibungsRepository.findByIdOptional("123"))).thenReturn(Optional.of(beschreibung));

    Assertions.assertEquals(Optional.of(beschreibung),
        beschreibungsRepository.findByIdOptional("123"));
  }

}
