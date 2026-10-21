package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 12.02.2020.
 */
public enum SuchDokumentTyp {

  KOD,
  BS;

  public static String toString(SuchDokumentTyp suchDokumentTyp) {
    switch (suchDokumentTyp){
      case KOD:
        return "KOD";
      case BS:
        return "BS";
      default:
        return "";
    }
  }

  public static SuchDokumentTyp fromString(String suchDokumentTypAsString) {
    switch (suchDokumentTypAsString) {
      case "KOD":
        return KOD;
      case "BS":
        return BS;
      default:
        return null;
    }
  }

}
