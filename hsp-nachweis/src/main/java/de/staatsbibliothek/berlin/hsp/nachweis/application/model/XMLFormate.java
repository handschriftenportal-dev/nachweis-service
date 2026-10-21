package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 02.06.2020.
 * @version 1.0
 */
public enum XMLFormate {

  MXML, TEI_ALL, MARC21;

  @Override
  public String toString() {

    switch (this) {
      case MXML:
        return "MXML";
      case TEI_ALL:
        return "TEI_ALL";
      case MARC21:
        return "MARC21";
      default:
        return "";
    }

  }
}
