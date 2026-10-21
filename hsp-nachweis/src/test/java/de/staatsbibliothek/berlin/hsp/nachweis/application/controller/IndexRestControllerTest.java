package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJob;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJobDocumentType;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexJobService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.IndexReplicationService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.ArgumentCaptor;

@QuarkusTest
@Execution(ExecutionMode.SAME_THREAD)
class IndexRestControllerTest {

  @InjectMock
  IndexJobService indexJobService;

  @InjectMock
  IndexReplicationService replicationService;

  @BeforeEach
  void setup() {
    // false = replication successfully disabled, indexing may proceed
    when(replicationService.disableReplication()).thenReturn(false);
  }

  @Test
  void indexJobStatusKodOk() {
    given()
        .when().post("/rest/index?doctype=KOD")
        .then()
        .statusCode(204);
    verify(indexJobService, timeout(1000)).startIndexingDocuments(any(IndexJob.class));
  }

  @Test
  void indexJobStatusKatalogOk() {
    given()
        .when().post("/rest/index?doctype=KATALOG")
        .then()
        .statusCode(204);
    verify(indexJobService, timeout(1000)).startIndexingDocuments(any(IndexJob.class));
  }

  @Test
  void indexJobStatusAllOk() {
    given()
        .when().post("/rest/index?doctype=ALL")
        .then()
        .statusCode(204);
    verify(indexJobService, timeout(1000)).startIndexingDocuments(any(IndexJob.class));
  }

  @Test
  void indexJobStatusDefault() {
    given()
        .when().post("/rest/index")
        .then()
        .statusCode(204);
    verify(indexJobService, timeout(1000)).startIndexingDocuments(any(IndexJob.class));
  }

  @Test
  void indexJobStatusKodWithIdOk() {
    given()
        .when().post("/rest/index?doctype=KOD&id=2")
        .then()
        .statusCode(204);

    ArgumentCaptor<IndexJob> indexJobCaptor = ArgumentCaptor.forClass(IndexJob.class);
    verify(indexJobService, timeout(1000)).startIndexingDocuments(indexJobCaptor.capture());
    assertEquals("2", indexJobCaptor.getValue().getDocumentId());
    assertEquals(IndexJobDocumentType.KOD, indexJobCaptor.getValue().getIndexJobDocumentType());
    assertEquals(BearbeiterBoundary.SYSTEM_USERNAME, indexJobCaptor.getValue().getActorName());
  }

  @Test
  void indexJobStatusEnumBadRequest() {
    given()
        .when().post("/rest/index?doctype=FALSCH&id=2")
        .then()
        .statusCode(400)
        .body(containsString("Invalid doctype"));
    verify(replicationService, never()).disableReplication();
    verify(indexJobService, never()).startIndexingDocuments(any(IndexJob.class));
  }

  @Test
  void indexJobReplicationNotDisabled_serviceUnavailable() {
    // true = replication is still enabled after the disable attempt
    when(replicationService.disableReplication()).thenReturn(true);

    given()
        .when().post("/rest/index?doctype=KOD")
        .then()
        .statusCode(503)
        .body(containsString("Replication could not be disabled"));
    verify(indexJobService, never()).startIndexingDocuments(any(IndexJob.class));
  }

}