package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 18.05.2021.
 * @version 1.0
 * <p>
 * Typ of manual description
 */
public enum BeschreibungsTyp {
  MEDIEVAL, ILLUMINATED;

  @Override
  public String toString() {
    switch (this) {
      case MEDIEVAL:
        return "medieval";
      case ILLUMINATED:
        return "illuminated";
      default:
        return "";
    }
  }
}
