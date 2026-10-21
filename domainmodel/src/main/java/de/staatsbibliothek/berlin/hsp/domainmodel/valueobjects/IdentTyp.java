package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 10.05.19
 */
public enum IdentTyp {
  GUELTIGE_SIGNATUR, ALTSIGNATUR, FRUEHERE_ALTSIGNATUR, INDIVIDUALNAME, ALTERNATIVE_SCHREIBUNG, ACCESSIONSNUMMER, KATALOGNUMMER,
  HSP_ALT_ID, HSP_ALTIDENTIFIER_MXML;

  public static String indentTypToTEIEncoding(IdentTyp typ) {
    switch (typ) {

      case GUELTIGE_SIGNATUR -> {
        return null;
      }
      case ALTSIGNATUR, ACCESSIONSNUMMER, ALTERNATIVE_SCHREIBUNG -> {
        return "alternative";
      }
      case FRUEHERE_ALTSIGNATUR -> {
        return "former";
      }
      case INDIVIDUALNAME -> {
        return "individual";
      }
      case KATALOGNUMMER -> {
        return "catalog";
      }
      case HSP_ALT_ID -> {
        return "hsp-ID";
      }
      case HSP_ALTIDENTIFIER_MXML -> {
        return "mxml-ID";
      }
    }
    return null;
  }

}
