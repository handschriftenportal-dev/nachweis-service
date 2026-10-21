package de.staatsbibliothek.berlin.hsp.nachweis.domain.gndservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDServiceSubset;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenGNDServicePort;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenserviceSync;
import jakarta.ws.rs.WebApplicationException;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 20.05.25.
 * @version 1.0
 */

class GNDSynchronisationServiceTest {


  @Test
  void testCreateService() {
    final GNDSynchronisationService gndSynchronisationService = createMockedService();

    assertNotNull(gndSynchronisationService);
    assertNotNull(gndSynchronisationService.getNormdatenGNDServicePort());
  }

  private static GNDSynchronisationService createMockedService() {
    NormdatenGNDServicePort normdatenGNDServicePort = Mockito.mock(
        NormdatenGNDServicePort.class);
    return new GNDSynchronisationService(
        normdatenGNDServicePort);
  }

  @Test
  void testRegisterSynchronization() {
    final GNDSynchronisationService gndSynchronisationService = createMockedService();

    gndSynchronisationService.registerSynchronisation(
        30, true, "gnddata");

    verify(gndSynchronisationService.getNormdatenGNDServicePort(),
        times(1)).registerSynchronistation(any());
  }

  @Test
  void testChange() {
    final GNDSynchronisationService gndSynchronisationService = createMockedService();
    NormdatenserviceSync sync = new NormdatenserviceSync(140, true, "test");
    GNDServiceSubset subset = new GNDServiceSubset("1", "Test", true, Set.of(),
        140, "gnddata", Set.of());
    Mockito.when(gndSynchronisationService.getNormdatenGNDServicePort()
        .updateSynchronistation(sync)).thenReturn(subset);

    GNDSynchronization result = gndSynchronisationService.changeSynchronisation(
        140, true, "test");

    assertNotNull(result);
    assertEquals("1", result.id());
    assertEquals("Test", result.name());
  }

  @Test
  void testfindSynchronisationHappyPath() {
    final GNDSynchronisationService gndSynchronisationService = createMockedService();
    Mockito.when(gndSynchronisationService.getNormdatenGNDServicePort()
        .findGNDServiceSubset()).thenReturn(
        new GNDServiceSubset("1", "Test", true, Set.of(), 140, "gnddata",
            Set.of()));

    final Optional<GNDSynchronization> result = gndSynchronisationService.findSynchronisation();
    assertTrue(
        result.isPresent());

    assertEquals("1", result.get().id());
  }

  @Test
  void testfindSynchronisation404Error() {
    final GNDSynchronisationService gndSynchronisationService = createMockedService();
    Mockito.when(gndSynchronisationService.getNormdatenGNDServicePort()
        .findGNDServiceSubset()).thenThrow(new WebApplicationException(404));

    final Optional<GNDSynchronization> result = gndSynchronisationService.findSynchronisation();
    assertFalse(
        result.isPresent());
  }

  @Test
  void testfindSynchronisationOtherError() {
    final GNDSynchronisationService gndSynchronisationService = createMockedService();
    Mockito.when(gndSynchronisationService.getNormdatenGNDServicePort()
        .findGNDServiceSubset()).thenThrow(new WebApplicationException(500));

    assertThrows(WebApplicationException.class,
        gndSynchronisationService::findSynchronisation);
  }
}
