package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.DatensicherungBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.DatensicherungException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.entities.Datensicherung;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DatensicherungDownload;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DokumentTyp;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.event.Event;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@ViewScoped
@Named
@LoginCheck
@Data
@Slf4j
public class DatensicherungController implements Serializable {

  @Serial
  private static final long serialVersionUID = -2338626292552177485L;
  private static final String DATENSICHERUNG_JSF = "datensicherung.jsf";
  @Getter(AccessLevel.NONE)
  private final BearbeiterBoundary bearbeiterService;
  @Getter(AccessLevel.NONE)
  private final DatensicherungBoundary service;
  @Getter(AccessLevel.NONE)
  private final Event<String> datensicherungEvent;
  private SortedSet<DokumentTyp> dokumentOptionen;
  private Set<DokumentTyp> dokumentAuswahl;
  private transient List<Datensicherung> datensicherungHistorie;

  @Inject
  public DatensicherungController(DatensicherungBoundary service,
      BearbeiterBoundary bearbeiterService, Event<String> datensicherungEvent) {
    this.service = service;
    this.bearbeiterService = bearbeiterService;
    this.datensicherungEvent = datensicherungEvent;
  }

  @PostConstruct
  public void init() {
    this.dokumentAuswahl = Collections.emptySet();
    this.dokumentOptionen = service.getDokumenteAuswahlOptionen();
    this.datensicherungHistorie = sortDatensicherungen(
        service.getAlleDatensicherungen());
  }

  public String starteDatensicherung() {
    String datensicherungId;
    try {
      datensicherungId = service.createInitialDatensicherung(
          dokumentAuswahl,
          bearbeiterService.getLoggedBearbeiter().getFullName()
      );

      datensicherungEvent.fireAsync(datensicherungId).handle((d, t) -> {
        if (t != null) {
          log.error("Datensicherung Event has thrown an error: {}", t.getMessage(),
              t.fillInStackTrace());
          showMessage(FacesMessage.SEVERITY_ERROR, getMessage("datensicherung_growl_error"));
          return null;
        }
        showMessage(FacesMessage.SEVERITY_INFO, getMessage("datensicherung_growl_started"));
        return d;
      });

    } catch (DatensicherungException e) {
      log.error(e.getMessage(), e.getId(), e);
      showMessage(FacesMessage.SEVERITY_ERROR, getMessage("datensicherung_growl_error"));
    }
    return DATENSICHERUNG_JSF;
  }

  public void updateDatensicherungHistorie() {
    this.datensicherungHistorie = sortDatensicherungen(
        service.getAlleDatensicherungen());
  }

  List<Datensicherung> sortDatensicherungen(
      List<Datensicherung> datensicherung) {
    var datensicherungen = new ArrayList<>(datensicherung);
    datensicherungen.sort((a, b) -> b.getStartZeit().compareTo(a.getStartZeit()));

    return datensicherungen;
  }

  public String delete(String id) {
    boolean isDeleted = this.service.deleteDatensicherung(id);
    if (!isDeleted) {
      showMessage(FacesMessage.SEVERITY_ERROR, getMessage("datensicherung_growl_delete_error"));
    } else {
      showMessage(FacesMessage.SEVERITY_INFO, getMessage("datensicherung_growl_deleted"));
    }
    return DATENSICHERUNG_JSF;
  }

  public StreamedContent download(String id) {
    try {
      DatensicherungDownload download = this.service.getFileByDatensicherungId(id);
      return DefaultStreamedContent.builder()
          .contentType("application/gzip")
          .name(download.dateiName())
          .stream(() -> new ByteArrayInputStream(download.data()))
          .build();
    } catch (DatensicherungException e) {
      showMessage(FacesMessage.SEVERITY_ERROR, getMessage("datensicherung_msg_download_error"));
      log.error("Error during download of Datensicherung", e);
    }
    return null;
  }

  private void showMessage(FacesMessage.Severity severity, String message, String details) {
    FacesMessage msg = new FacesMessage(severity, message, details);
    FacesContext ctx = FacesContext.getCurrentInstance();
    if (ctx != null) {
      ctx.addMessage(null, msg);
    }
  }

  private void showMessage(FacesMessage.Severity severity, String message) {
    showMessage(severity, message, "");
  }

}
