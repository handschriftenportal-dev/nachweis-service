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

import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewMapper;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.ImportUpdate;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.neueImporte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.sendIndexMessage;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgang;
import java.util.Objects;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.faces.event.WebsocketEvent;
import javax.faces.event.WebsocketEvent.Closed;
import javax.faces.event.WebsocketEvent.Opened;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.11.2019.
 * @version 1.0
 */

@ApplicationScoped
@Named
public class WebSocketController {

  WebSocketController() {
  }

  @Inject
  public WebSocketController(@Push(channel = "importMessagePush") PushContext importMessagePush,
      ImportBoundary importBoundary) {
    this.importMessagePush = importMessagePush;
    this.importBoundary = importBoundary;
  }

  private Logger logger = LoggerFactory.getLogger(WebSocketController.class);

  private PushContext importMessagePush;

  private ImportBoundary importBoundary;

  public void onSendMessageForIndexing(@Observes @sendIndexMessage BeschreibungImport beschreibungImportJob) {

    logger.info("Getting Index Update");

    if (Objects.nonNull(beschreibungImportJob) && beschreibungImportJob.getStatus().equals(ImportStatus.UEBERNOMMEN)) {

      importMessagePush.send(
          "Import " + beschreibungImportJob.getMessageDTO().getTarget().getName() + " erfolgreich übernommen!");

    } else if (Objects.nonNull(beschreibungImportJob) && beschreibungImportJob.getStatus()
        .equals(ImportStatus.FEHLGESCHLAGEN)) {

      importMessagePush.send(
          "Import " + beschreibungImportJob.getMessageDTO().getTarget().getName() + " fehlgeschlagen!");

    }


  }

  public void onImportMessage(@Observes @neueImporte BeschreibungImport importe) {

    ImportViewModel importView = ImportViewMapper.map(importe, importBoundary, false);

    if (Objects.nonNull(importe) && Objects.nonNull(importView)) {

      logger.info("Getting new Import {} ", importView.getDateiName());

      importMessagePush.send(
          "Es wurde von " + importView.getBenutzer()
              + " folgende Beschreibungsdatei importiert: " + importView
              .getDateiName());

    }
  }

  public void onImportVorgang(@Observes @ImportUpdate ImportVorgang importVorgang) {

    if (Objects.nonNull(importVorgang)) {

      logger.info("Getting Import Vorgang Update {} ", importVorgang.getName());

      importMessagePush.send(
          "Es wurde von " + importVorgang.getBenutzerName()
              + " folgende importiert: " + importVorgang.getName());

    }

  }

  public void onOpen(@Observes @Opened WebsocketEvent opened) {
    logger.debug("Web Socket opened {} ", opened.getChannel());
  }

  public void onClose(@Observes @Closed WebsocketEvent closed) {
    logger.debug("Web Socket closed {} ", closed.getChannel());
  }

}
