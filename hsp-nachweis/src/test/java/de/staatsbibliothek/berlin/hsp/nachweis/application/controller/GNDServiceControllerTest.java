package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.gndservice.GNDSynchronisationService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.gndservice.GNDSynchronization;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.05.25.
 * @version 1.0
 */

class GNDServiceControllerTest {

  @Test
  void testSetup() {
    GNDSynchronisationService service = Mockito.mock(
        GNDSynchronisationService.class);
    Mockito.when(service.findSynchronisation())
        .thenThrow(new IllegalArgumentException("Error"));
    GNDServiceController controller = new GNDServiceController(service);
    controller.setup();
    assertNotNull(controller.getError());
  }

  @Test
  void testupdateControllerSubsetState() {
    GNDServiceController controller = new GNDServiceController(null);

    assertNull(controller.getSynchronization());

    controller.updateControllerSubsetState(
        new GNDSynchronization("1", "name", 30, true, "kafka",
            Set.of(), List.of()));

    assertNotNull(controller.getSynchronization());
    assertEquals(30, controller.getIntervalInMinutes());
    assertEquals("kafka", controller.getKafkaTopicName());
    assertTrue(controller.isActive());
  }

  @Test
  void testloeschenHappyPath() {
    GNDSynchronisationService service = Mockito.mock(
        GNDSynchronisationService.class);
    GNDServiceController controller = new GNDServiceController(service);

    controller.loeschen();

    verify(service, Mockito.times(1)).unregisterSynchronisation();
  }

  @Test
  void testloeschenGivenError() {
    GNDSynchronisationService service = Mockito.mock(
        GNDSynchronisationService.class);
    final IllegalArgumentException error = new IllegalArgumentException(
        "Error");
    Mockito.doThrow(error).when(service)
        .unregisterSynchronisation();
    GNDServiceController controller = new GNDServiceController(service);

    controller.loeschen();

    verify(service, Mockito.times(1)).unregisterSynchronisation();
    assertEquals(error, controller.getError());

  }

  @Test
  void testanlegenGivenHappyPath() {
    GNDSynchronisationService service = Mockito.mock(
        GNDSynchronisationService.class);
    GNDServiceController controller = new GNDServiceController(service);
    final GNDSynchronization gndSynchronizationData = new GNDSynchronization(
        "1",
        "name", 30, true, "kafka",
        Set.of(), List.of());
    controller.updateControllerSubsetState(
        gndSynchronizationData);

    controller.anlegen();

    verify(service, Mockito.times(1))
        .registerSynchronisation(gndSynchronizationData.intervalInMinutes(),
            gndSynchronizationData.active(),
            gndSynchronizationData.kafkaTopicName());
  }

  @Test
  void testaendernGivenHappyPath() {
    GNDSynchronisationService service = Mockito.mock(
        GNDSynchronisationService.class);
    GNDServiceController controller = new GNDServiceController(service);
    final GNDSynchronization gndSynchronizationData = new GNDSynchronization(
        "1",
        "name", 30, true, "kafka",
        Set.of(), List.of());
    controller.updateControllerSubsetState(
        gndSynchronizationData);

    controller.aendern();

    verify(service, Mockito.times(1))
        .changeSynchronisation(gndSynchronizationData.intervalInMinutes(),
            gndSynchronizationData.active(),
            gndSynchronizationData.kafkaTopicName());
  }
}
