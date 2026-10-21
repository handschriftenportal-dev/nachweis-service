package de.staatsbibliothek.berlin.hsp.nachweis.domain.events;

/**
 * Fired after Kulturobjektdokumente have been registered, either by the KOD import or by manual
 * creation via the UI. Both paths create internal PURLs, so consumers such as the PURL resolver
 * file generation have to react to it.
 */
public record KulturObjektDokumentRegistriertEvent(String eventMessage) {

  public KulturObjektDokumentRegistriertEvent() {
    this("");
  }
}
