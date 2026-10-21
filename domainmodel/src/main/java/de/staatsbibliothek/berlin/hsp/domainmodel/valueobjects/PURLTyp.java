package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import java.util.Objects;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 02.08.2022
 */

public enum PURLTyp {
  INTERNAL, EXTERNAL;

  public static PURLTyp getPURLTypFromString(String purlTyp) {
    if (Objects.isNull(purlTyp)) {
      return null;
    }

    return switch (purlTyp) {
      case "INTERNAL" -> INTERNAL;
      case "EXTERNAL" -> EXTERNAL;
      default -> null;
    };
  }
}
