package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.nachweis.WiremockITServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 02.10.24.
 * @version 1.0
 */

@QuarkusTest
@Execution(ExecutionMode.SAME_THREAD)
@QuarkusTestResource(WiremockITServer.class)
class ImportServicePortIT {

  @Inject
  @RestClient
  ImportServicePort importServicePort;

  @Test
  void testFindById() {
    assertNotNull(importServicePort);

    ImportVorgang vorgang = importServicePort.findById("123");

    assertNotNull(vorgang);

    assertEquals("7637a746-282b-44a7-a85f-79d2d2f6885d", vorgang.getId());
  }

  @Test
  void testDeleteImportJob() {

    assertDoesNotThrow(() -> importServicePort.deleteByIDs(List.of("123")));

  }
}
