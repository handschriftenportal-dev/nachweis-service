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

import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJobStatus;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJobStatus.IndexJobStatusBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJobStatusRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class IndexJobStatusRepositoryTest {

  @Test
  public void testsave() {
    IndexJobStatusRepository indexJobStatusRepository = Mockito.mock(IndexJobStatusRepository.class);
    IndexJobStatus indexJobStatus = IndexJobStatusBuilder.anIndexJobStatus().withId(1l).build();
    Mockito.when(indexJobStatusRepository.save(indexJobStatus)).thenReturn(indexJobStatus);
    Mockito.when(indexJobStatusRepository.findById(1l)).thenReturn(indexJobStatus);

    indexJobStatusRepository.save(indexJobStatus);

    Assertions.assertNotNull(indexJobStatusRepository.findById(1l));
  }

  @Test
  public void testfindById() {
    IndexJobStatusRepository indexJobStatusRepository = Mockito.mock(IndexJobStatusRepository.class);
    IndexJobStatus indexJobStatus = IndexJobStatusBuilder.anIndexJobStatus().withId(1).build();
    Mockito.when(indexJobStatusRepository.save(indexJobStatus)).thenReturn(indexJobStatus);
    Mockito.when(indexJobStatusRepository.findById(1l)).thenReturn(indexJobStatus);

    IndexJobStatus save = indexJobStatusRepository.save(indexJobStatus);
    Assertions.assertEquals(save, indexJobStatusRepository.findById(save.getId()));
  }


  @Test
  public void delete() {
    IndexJobStatusRepository indexJobStatusRepository = Mockito.mock(IndexJobStatusRepository.class);
    IndexJobStatus indexJobStatus = IndexJobStatusBuilder.anIndexJobStatus().withId(1L).build();
    Mockito.when(indexJobStatusRepository.save(indexJobStatus)).thenReturn(indexJobStatus);
    Mockito.when(indexJobStatusRepository.findByIdOptional(1l)).thenReturn(Optional.empty());

    indexJobStatusRepository.save(indexJobStatus);
    indexJobStatusRepository.delete(indexJobStatus);

    Assertions.assertEquals(Optional.empty(),
        indexJobStatusRepository.findByIdOptional(1L));
  }
}
