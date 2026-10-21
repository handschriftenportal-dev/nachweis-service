package de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex;

public enum IndexJobDocumentType {
  KOD, KATALOG, ALL;

  public static IndexJobDocumentType fromString(String value) {
    try {
      return valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
