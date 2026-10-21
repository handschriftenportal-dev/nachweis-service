package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum ExterneRessourceTyp {

  HSC("hsc", "HSC"),
  KDIH("kdih", "KdiH"),
  MANUSCRIPTAAT("manuscriptaat", "manuscripta.at"),
  MANUSCRIPTAPL("manuscriptapl", "manuscripta.pl"),
  FRAGMENTARIUM("fragmentarium", "Fragmentarium"),
  QALAMOS("qalamos", "Qalamos"),
  PINAKES("pinakes", "Pinakes"),
  WZIS("wzis", "WZIS"),
  EBDB("ebdb", "EBDB"),
  HSA("hsa", "HSA"),
  BESCHREIBUNG("beschreibung", "Beschreibung"),
  GESCHICHTSQUELLEN("geschichtsquellen", "Geschichtsquellen"),
  JORDANUS("jordanus", "Jordanus"),
  ECODICES("ecodices", "e-codices"),
  KOLOPHONE("kolophone", "Kolophone"),
  SONSTIGES("sonstiges", "Sonstiges");

  private static final String MESSAGE_KEY_PREFIX = "externe_ressource_typ_";

  private static final Map<String, ExterneRessourceTyp> BY_TEI_TYPE =
      Arrays.stream(values())
          .collect(Collectors.toUnmodifiableMap(
              ExterneRessourceTyp::getTeiType,
              Function.identity()));

  private final String teiType;
  private final String teiCaption;

  ExterneRessourceTyp(String teiType, String teiCaption) {
    this.teiType = teiType;
    this.teiCaption = teiCaption;
  }

  public String getMessageKey() {
    return MESSAGE_KEY_PREFIX + name();
  }

  public static Optional<ExterneRessourceTyp> fromTeiType(String teiType) {
    return Optional.ofNullable(teiType)
        .map(String::strip)
        .filter(value -> !value.isEmpty())
        .map(value -> value.toLowerCase(Locale.ROOT))
        .map(BY_TEI_TYPE::get);
  }
}
