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

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJobStatus.IndexJobStatusBuilder;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IndexJob {

  @JsonIgnore
  private final Optional<IndexJobStatus> indexJobStatus;
  private String uriToIndexJobStatus;
  private Optional<String> kodId = Optional.empty();
  private Optional<ActivityStreamAction> activityStreamAction = Optional.empty();

  private boolean withTransaction;

  public IndexJob(IndexJobStatusBoundary indexJobStatusBoundry, String kodId2, boolean withTransaction) {
    indexJobStatus = indexJobStatusBoundry.save(
        IndexJobStatusBuilder.anIndexJobStatus().withStatusInPercentage("0%").build());

    indexJobStatus.ifPresentOrElse(
        indexJobStatus1 -> uriToIndexJobStatus = "/rest/indexkods/status/" + indexJobStatus1.getId(),
        () -> log.error("Couldn't save indexJobStatusFromService"));
    this.kodId = Optional.of(kodId2);
    this.withTransaction = withTransaction;
  }

  public IndexJob(IndexJobStatusBoundary indexJobStatusBoundry, String kodId,
      ActivityStreamAction activityStreamAction, boolean withTransaction) {
    this(indexJobStatusBoundry, kodId, withTransaction);
    this.activityStreamAction = Optional.ofNullable(activityStreamAction);
  }

  public IndexJob(IndexJobStatusBoundary indexJobStatusBoundry) {
    indexJobStatus = indexJobStatusBoundry.save(
        IndexJobStatusBuilder.anIndexJobStatus().withStatusInPercentage("0%").build());

    indexJobStatus.ifPresentOrElse(
        indexJobStatus1 -> uriToIndexJobStatus = "/rest/indexkods/status/" + indexJobStatus1.getId(), () ->
            log.error("Couldn't save indexJobStatusFromService"));
  }

  public Optional<String> getKodId() {
    return kodId;
  }

  public String getUriToIndexJobStatus() {
    return uriToIndexJobStatus;
  }

  public Optional<IndexJobStatus> getIndexJobStatus() {
    return indexJobStatus;
  }

  public Optional<ActivityStreamAction> getActivityStreamAction() {
    return activityStreamAction;
  }

  public boolean isWithTransaction() {
    return withTransaction;
  }
}
