package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 28.09.2017.
 */
public enum Status {

  UNBEKANNT, ZERSTOERT, VORHANDEN, VERSCHOLLEN;

  @Override
  public String toString() {

    return switch (this) {
      case UNBEKANNT -> "unbekannt";
      case VORHANDEN -> "vorhanden";
      case ZERSTOERT -> "zerstört";
      case VERSCHOLLEN -> "verschollen";
      default -> "Ohne Angabe";
    };
  }

}
