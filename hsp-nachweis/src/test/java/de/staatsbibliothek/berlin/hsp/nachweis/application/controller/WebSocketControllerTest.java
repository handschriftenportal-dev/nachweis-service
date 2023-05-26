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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import java.time.LocalDateTime;
import javax.faces.push.PushContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.11.2019.
 * @version 1.0
 */
public class WebSocketControllerTest {

  private PushContext pushContext = mock(PushContext.class);

  private ImportBoundary importBoundary = mock(ImportBoundary.class);

  @Test
  public void onSendMessageForIndexingTest() throws ActivityStreamsException {

    WebSocketController webSocketController = new WebSocketController(pushContext, importBoundary);

    ActivityStream message = ActivityStream.builder()
        .withId("1")
        .withType(ActivityStreamAction.ADD)
        .withTargetName("Message")
        .withPublished(LocalDateTime.now())
        .withActorName("konrad")
        .build();

    BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.UEBERNOMMEN, message);

    webSocketController.onSendMessageForIndexing(beschreibungImportJob);

    Mockito.verify(
        pushContext,
        times(1)).send("Import Message erfolgreich übernommen!");
  }

  @Test
  public void testonImportMessage() throws ActivityStreamsException {

    WebSocketController webSocketController = new WebSocketController(pushContext, importBoundary);

    ActivityStream message = ActivityStream.builder()
        .withId("1")
        .withType(ActivityStreamAction.ADD)
        .withTargetName("Message")
        .withPublished(LocalDateTime.now())
        .withActorName("konrad")
        .build();

    BeschreibungImport beschreibungImportJob = new BeschreibungImport(ImportStatus.UEBERNOMMEN, message);

    webSocketController.onImportMessage(beschreibungImportJob);

    Mockito.verify(
        pushContext,
        times(1)).send("Es wurde von konrad folgende Beschreibungsdatei importiert: Message");

  }
}
