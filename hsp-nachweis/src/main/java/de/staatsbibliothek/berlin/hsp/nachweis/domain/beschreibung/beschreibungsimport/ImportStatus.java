package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 06.06.2019.
 */
public enum ImportStatus {

  UEBERNOMMEN, IN_UEBERNAHME, OFFEN, ABGELEHNT, FEHLGESCHLAGEN;

  @Override
  public String toString() {

    switch (this) {
      case OFFEN:
        return "offen";
      case IN_UEBERNAHME:
        return "in Übernahme";
      case UEBERNOMMEN:
        return "übernommen";
      case FEHLGESCHLAGEN:
        return "fehlgeschlagen";
      case ABGELEHNT:
        return "abgelehnt";
      default:
        return "";
    }

  }
}
