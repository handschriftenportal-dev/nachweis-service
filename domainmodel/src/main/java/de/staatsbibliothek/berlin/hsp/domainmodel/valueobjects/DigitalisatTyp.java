package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 11.11.20
 */
public enum DigitalisatTyp {
  KOMPLETT_VOM_ORIGINAL, KOMPLETT_VON_REPRODUKTION, WASSERZEICHEN, TEIL_VOM_ORIGINAL, TEIL_VON_REPRODUKTION, UNBEKANNT;

  @Override
  public String toString() {
    switch (this) {

      case KOMPLETT_VOM_ORIGINAL:
        return "completeFromOriginal";
      case KOMPLETT_VON_REPRODUKTION:
        return "completeFromSurrogate";
      case WASSERZEICHEN:
        return "watermark";
      case TEIL_VOM_ORIGINAL:
        return "partFromOriginal";
      case TEIL_VON_REPRODUKTION:
        return "partFromSurrogate";
      case UNBEKANNT:
        return "unknown";
      default:
        return "";
    }
  }

  public static DigitalisatTyp fromString(String value) {

    if ("komplettVomOriginal".equals(value) || "completeFromOriginal".equals(value)) {
      return DigitalisatTyp.KOMPLETT_VOM_ORIGINAL;
    }

    if ("komplettVonReproduktion".equals(value) || "completeFromSurrogate".equals(value)) {
      return DigitalisatTyp.KOMPLETT_VON_REPRODUKTION;
    }

    if ("Wasserzeichen".equals(value) || "watermark".equals(value)) {
      return DigitalisatTyp.WASSERZEICHEN;
    }

    if ("TeilVomOriginal".equals(value) || "partFromOriginal".equals(value)) {
      return DigitalisatTyp.TEIL_VOM_ORIGINAL;
    }

    if ("TeilVonReproduktion".equals(value) || "partFromSurrogate".equals(value)) {
      return DigitalisatTyp.TEIL_VON_REPRODUKTION;
    }

    return DigitalisatTyp.UNBEKANNT;
  }

  public String toLabel() {
    switch (this) {

      case KOMPLETT_VOM_ORIGINAL:
        return "Komplett Vom Original";
      case KOMPLETT_VON_REPRODUKTION:
        return "Komplett Von Reproduktion";
      case WASSERZEICHEN:
        return "Wasserzeichen";
      case TEIL_VOM_ORIGINAL:
        return "Teil Vom Original";
      case TEIL_VON_REPRODUKTION:
        return "Teil Von Reproduktion";
      case UNBEKANNT:
        return "Unbekannt";
      default:
        return "";
    }
  }
}
