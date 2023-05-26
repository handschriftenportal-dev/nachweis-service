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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex;

import java.io.Serializable;
import java.util.Optional;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Dependent
public class IndexJobStatusService implements IndexJobStatusBoundary, Serializable {

  private static final long serialVersionUID = -1212711500122848245L;

  private transient IndexJobStatusRepository indexJobStatusRepository;

  @Inject
  public IndexJobStatusService(IndexJobStatusRepository indexJobStatusRepository) {
    this.indexJobStatusRepository = indexJobStatusRepository;
  }

  @Override
  public Optional<IndexJobStatus> findById(long id) {
    return indexJobStatusRepository.findByIdOptional(id);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Optional<IndexJobStatus> save(IndexJobStatus indexJobStatus) {
    return Optional.ofNullable(indexJobStatusRepository.save(indexJobStatus));
  }

  @Override
  public void delete(IndexJobStatus indexJobStatus) {
    indexJobStatusRepository.delete(indexJobStatus);
  }

}
