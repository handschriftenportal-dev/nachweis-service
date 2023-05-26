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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenGraphQLBoundary;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 07.12.20
 */
class NormdatenReferenzServiceTest {

  private NormdatenGraphQLBoundary normdatenGraphQLBoundaryMock = Mockito.mock(NormdatenGraphQLBoundary.class);
  private NormdatenReferenzService service = new NormdatenReferenzService(normdatenGraphQLBoundaryMock);

  @Test
  void testFindOneByIdOrNameAndType() {
    service.findOneByIdOrNameAndType("1", NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);
    Mockito.verify(normdatenGraphQLBoundaryMock, Mockito.times(1))
        .findOneByIdOrNameAndNodeLabel("1", NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);
  }

  @Test
  void findAllByIdOrNameAndType() {
    service.findAllByIdOrNameAndType("2", NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME, true);
    Mockito.verify(normdatenGraphQLBoundaryMock, Mockito.times(1))
        .findAllByIdOrNameAndNodeLabel("2", NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME, true);
  }

  @Test
  void testFindSpracheById() {
    service.findSpracheById("de");
    Mockito.verify(normdatenGraphQLBoundaryMock, Mockito.times(1)).findLanguageById("de");
  }

  @Test
  void testFindKoerperschaftenByOrtId() {
    service.findKoerperschaftenByOrtId("123", false);
    Mockito.verify(normdatenGraphQLBoundaryMock, Mockito.times(1))
        .findCorporateBodiesByPlaceId("123", false);
  }

}
