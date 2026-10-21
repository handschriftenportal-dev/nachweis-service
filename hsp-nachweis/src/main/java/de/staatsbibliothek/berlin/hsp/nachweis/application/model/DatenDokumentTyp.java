package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 29.05.2020.
 * @version 1.0
 * <p>
 * Enum to present the domain entity types which can be imported.
 */
public enum DatenDokumentTyp {

  KOD,
  BESCHREIBUNG,
  KATALOG,
  ORT,
  KOERPERSCHAFT,
  BEZIEHUNG,
  DIGITALISAT,
  PERSON,
  SPRACHE,
  KONZEPT,
  ONTOLOGIE,
  INITIEN;

  @Override
  public String toString() {

    return switch (this) {
      case KOD -> "Kulturobjektdokument";
      case BESCHREIBUNG -> "Beschreibung";
      case KATALOG -> "Katalog";
      case ORT -> "Ort";
      case KOERPERSCHAFT -> "Körperschaft";
      case BEZIEHUNG -> "Beziehung";
      case DIGITALISAT -> "Digitalisat";
      case PERSON -> "Person";
      case SPRACHE -> "Sprache";
      case KONZEPT -> "Konzept";
      case ONTOLOGIE -> "Ontologie";
      case INITIEN -> "Initien";
    };

  }
}
