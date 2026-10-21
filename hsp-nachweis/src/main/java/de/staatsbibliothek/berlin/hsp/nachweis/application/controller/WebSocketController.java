package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewMapper;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.ImportUpdate;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.neueImporte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.sendIndexMessage;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportVorgang;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.faces.event.WebsocketEvent;
import jakarta.faces.event.WebsocketEvent.Closed;
import jakarta.faces.event.WebsocketEvent.Opened;
import jakarta.faces.push.Push;
import jakarta.faces.push.PushContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.11.2019.
 * @version 1.0
 */

@ApplicationScoped
@Named
public class WebSocketController {

  private final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
  private final PushContext importMessagePush;
  private final ImportViewMapper importViewMapper;

  @Inject
  public WebSocketController(@Push(channel = "importMessagePush") PushContext importMessagePush,
      ImportViewMapper importViewMapper) {
    this.importMessagePush = importMessagePush;
    this.importViewMapper = importViewMapper;
  }

  public void onSendMessageForIndexing(
      @Observes @sendIndexMessage BeschreibungImport beschreibungImportJob) {
    logger.info("Getting Index Update");

    if (Objects.nonNull(beschreibungImportJob) && beschreibungImportJob.getStatus()
        .equals(ImportStatus.UEBERNOMMEN)) {

      importMessagePush.send(
          "Import " + beschreibungImportJob.getMessageDTO().getTarget().getName()
              + " erfolgreich übernommen!");
    } else if (Objects.nonNull(beschreibungImportJob) && beschreibungImportJob.getStatus()
        .equals(ImportStatus.FEHLGESCHLAGEN)) {

      importMessagePush.send(
          "Import " + beschreibungImportJob.getMessageDTO().getTarget().getName()
              + " fehlgeschlagen!");
    }
  }

  public void onImportMessage(@Observes @neueImporte BeschreibungImport importe) {
    ImportViewModel importView = importViewMapper.map(importe, false);

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
