package de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import java.io.Serial;
import java.io.Serializable;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class DatensicherungEventHandler implements Serializable {

  @Serial
  private static final long serialVersionUID = -2338626292552177485L;

  private final DatensicherungBoundary service;

  @Inject
  public DatensicherungEventHandler(DatensicherungBoundary service) {
    this.service = service;
  }

  public void handleEvent(@ObservesAsync String datensicherungId) {
    log.info("Datensicherung Event received for Datensicherung {}", datensicherungId);
    service.starteDatensicherung(datensicherungId);
  }
}
