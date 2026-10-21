package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 25.02.2022.
 * @version 1.0
 */
public enum DokumentObjektTyp {
  HSP_DESCRIPTION, HSP_DESCRIPTION_RETRO, HSP_OBJECT;

  public static DokumentObjektTyp fromString(String type) {
    if ("hsp:object".equals(type)) {
      return DokumentObjektTyp.HSP_OBJECT;
    }

    if ("hsp:description".equals(type)) {
      return DokumentObjektTyp.HSP_DESCRIPTION;
    }

    if ("hsp:description_retro".equals(type)) {
      return DokumentObjektTyp.HSP_DESCRIPTION_RETRO;
    }

    return null;
  }

  @Override
  public String toString() {
    return switch (this) {
      case HSP_OBJECT -> "hsp:object";
      case HSP_DESCRIPTION -> "hsp:description";
      case HSP_DESCRIPTION_RETRO -> "hsp:description_retro";
    };
  }
}
