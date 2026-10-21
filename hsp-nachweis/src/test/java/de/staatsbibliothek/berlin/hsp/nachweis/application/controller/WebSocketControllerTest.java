package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.mockito.Mockito.times;

import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewMapper;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import jakarta.faces.push.PushContext;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.11.2019.
 * @version 1.0
 */
class WebSocketControllerTest {

  PushContext pushContext = Mockito.mock(PushContext.class);
  ImportViewMapper importViewMapper = Mockito.mock(ImportViewMapper.class);

  @Test
  void onSendMessageForIndexingTest() throws ActivityStreamsException {

    WebSocketController webSocketController = new WebSocketController(pushContext,
        importViewMapper);

    ActivityStream message = ActivityStream.builder()
        .withId("1")
        .withType(ActivityStreamAction.ADD)
        .withTargetName("Message")
        .withPublished(LocalDateTime.now())
        .withActorName("konrad")
        .build();

    BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.UEBERNOMMEN,
        message);

    webSocketController.onSendMessageForIndexing(beschreibungImportJob);

    Mockito.verify(
        pushContext,
        times(1)).send("Import Message erfolgreich übernommen!");
  }

  @Test
  void testonImportMessage() throws ActivityStreamsException {

    WebSocketController webSocketController = new WebSocketController(pushContext,
        importViewMapper);

    ActivityStream message = ActivityStream.builder()
        .withId("1")
        .withType(ActivityStreamAction.ADD)
        .withTargetName("Message")
        .withPublished(LocalDateTime.now())
        .withActorName("konrad")
        .build();

    BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.UEBERNOMMEN,
        message);

    ImportViewModel viewModel = new ImportViewModel.ImportViewModelBuilder()
        .withId("view-id")
        .withBenutzer("konrad")
        .withDateiName("Message")
        .build();
    Mockito.when(importViewMapper.map(beschreibungImportJob, false)).thenReturn(viewModel);

    webSocketController.onImportMessage(beschreibungImportJob);

    Mockito.verify(
        pushContext,
        times(1)).send("Es wurde von konrad folgende Beschreibungsdatei importiert: Message");

  }
}
