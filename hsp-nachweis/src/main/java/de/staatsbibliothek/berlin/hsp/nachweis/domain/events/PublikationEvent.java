package de.staatsbibliothek.berlin.hsp.nachweis.domain.events;

public record PublikationEvent(String eventMessage) {

  public PublikationEvent() {
    this("");
  }
}
