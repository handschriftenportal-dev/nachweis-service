package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import java.util.Objects;
import java.util.Optional;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 17.03.2021.
 * @version 1.0
 * <p>
 * This enumeration class shows if an entity is maintained within the handschriftenportal or within
 * an external system
 */
public enum VerwaltungsTyp {
  INTERN,
  EXTERN;

  public static Optional<VerwaltungsTyp> fromString(String value) {
    if (Objects.isNull(value)) {
      return Optional.empty();
    }

    return switch (value.toLowerCase()) {
      case "extern" -> Optional.of(EXTERN);
      case "intern" -> Optional.of(INTERN);
      default -> Optional.empty();
    };
  }

  @Override
  public String toString() {
    return switch (this) {
      case EXTERN -> "extern";
      case INTERN -> "intern";
    };
  }

}
