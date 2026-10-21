package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Beinhaltet Kultur Objekt Dokument Typen (Gültig nur für DokumentTyp=KULTUR_OBJEKT_DOKUMENT)
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 10.05.19
 */
public enum KulturObjektTyp {
  CODEX,
  FRAGMENT_SELBSTAENDIG_AUSGELOEST,
  FRAGMENT_SELBSTAENDIG_IN_DRUCK,
  FRAGMENT_SELBSTAENDIG_ABKLATSCH,
  EINZELBLATT,
  SAMMELBAND,
  SAMMLUNG,
  DRUCK_MIT_HSL_ANTEILEN,
  DRUCK_ALS_TRAEGEBAND,
  SONSTIGES,
  ROLLE,
  ZUSAMMENGESETZTE_HANDSCHRIFT;

  private static final Logger log = LoggerFactory.getLogger(KulturObjektTyp.class);

  public static KulturObjektTyp getKulturObjektTypFromString(String kulturObjektTypAsString) {
    if (Objects.isNull(kulturObjektTypAsString) || kulturObjektTypAsString.isEmpty()) {
      return null;
    }
    //Codex;zusammengesetzteHs;Sammelband;Einzelblatthandschrift;Rolle;Fragment;Sammlung;DruckHslAntl;DruckTrgbd;Sonstiges
    kulturObjektTypAsString = kulturObjektTypAsString.toUpperCase();
    switch (kulturObjektTypAsString) {
      case "CODEX":
        return CODEX;
      case "ZUSAMMENGESETZTEHS":
      case "ZUSAMMENGESETZTE HANDSCHRIFT":
        return ZUSAMMENGESETZTE_HANDSCHRIFT;
      case "SAMMELBAND":
        return SAMMELBAND;
      case "EINZELBLATT":
      case "EINZELBLATTHANDSCHRIFT":
        return EINZELBLATT;
      case "ROLLE":
        return ROLLE;
      case "FRAGMENT SELBSTAENDIG AUSGELOEST":
      case "FRAGMENT":
        return FRAGMENT_SELBSTAENDIG_AUSGELOEST;
      case "FRAGMENT SELBSTAENDIG ABKLATSCH":
        return FRAGMENT_SELBSTAENDIG_ABKLATSCH;
      case "FRAGMENT SELBSTAENDIG IN DRUCK":
        return FRAGMENT_SELBSTAENDIG_IN_DRUCK;
      case "SAMMLUNG":
        return SAMMLUNG;
      case "DRUCKHSLANTL":
      case "DRUCK MIT HSL ANTEILEN":
        return DRUCK_MIT_HSL_ANTEILEN;
      case "DRUCK IN TRAGEBAND":
      case "DRUCKTRGBD":
        return DRUCK_ALS_TRAEGEBAND;
      case "SONSTIGES":
        return SONSTIGES;
      default:
        log.warn("Not handled KulturObjektTyp: {}", kulturObjektTypAsString);
        return null;
    }

  }
}
