package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

/**
 * This enum represents the fact that a values has been taken from a Beschreibung manually, automatically or never. If this enum is
 * used by an KulturObjektDokument this means that any reference has been taken manually or all are automatically set. In case of empty
 * means that no reference could be taken because of no Beschreibung exists.
 */

public enum AttributsReferenzStatusTyp {

  LEER,
  MANUELL,
  AUTOMATISCH
}
