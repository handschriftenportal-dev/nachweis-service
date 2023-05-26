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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJob;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJobStatus;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJobStatus.IndexJobStatusBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJobStatusBoundary;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Optional;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mockito;

@QuarkusTest
@Execution(ExecutionMode.SAME_THREAD)
class IndexRestControllerTest {

  private final IndexJobStatusBoundary indexJobStatusBoundry = mock(IndexJobStatusBoundary.class);

  private final KulturObjektDokumentBoundary kulturObjektDokumentBoundary = mock(KulturObjektDokumentBoundary.class);

  private final Event<IndexJob> indexJobEvent = mock(Event.class);

  @Inject
  public IndexRestController indexRestController;

  @BeforeEach
  public void setUp() {
    indexRestController.setIndexJobStatusBoundry(indexJobStatusBoundry);
    indexRestController.setIndexJobEvent(indexJobEvent);

    IndexJobStatus indexJobStatus = IndexJobStatusBuilder.anIndexJobStatus().withId(1L).withStatusInPercentage("0%").build();
    when(indexJobStatusBoundry.save(Mockito.any())).thenReturn(java.util.Optional.ofNullable(indexJobStatus));
  }

  @Test
  void indexJobStatus() {

    IndexJobStatus indexJobStatus = IndexJobStatusBuilder.anIndexJobStatus().withId(2L).withStatusInPercentage("30%").build();
    when(indexJobStatusBoundry.findById(2L)).thenReturn(Optional.of(indexJobStatus));

    given()
        .when().get("/rest/indexkods/status/2")
        .then()
        .statusCode(200)
        .body("id", equalTo(2), "statusInPercentage", equalTo("30%"));
  }

  @Test
  void testIndexingKodWithId() {
    indexRestController.setKulturObjektDokumentBoundary(kulturObjektDokumentBoundary);
    KulturObjektDokument kulturObjektDokument = new KulturObjektDokumentBuilder().withId("2").build();
    when(kulturObjektDokumentBoundary.findById("2")).thenReturn(java.util.Optional.ofNullable(kulturObjektDokument));

    given()
        .when().post("/rest/indexkods/2")
        .then()
        .statusCode(201);

    verify(indexJobEvent, times(1)).fireAsync(Mockito.any());
  }

  @Test
  void testIndexingKods() {
    IndexJobStatus indexJobStatus = IndexJobStatusBuilder.anIndexJobStatus().withId(1L).withStatusInPercentage("0%")
        .build();
    when(indexJobStatusBoundry.save(Mockito.any())).thenReturn(java.util.Optional.ofNullable(indexJobStatus));

    given()
        .when().post("/rest/indexkods")
        .then()
        .statusCode(201)
        .body("uriToIndexJobStatus", equalTo("/rest/indexkods/status/1"));

    verify(indexJobEvent, times(1)).fireAsync(Mockito.any());
  }
}
