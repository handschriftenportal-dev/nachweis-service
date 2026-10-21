package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 05.10.21
 */
public enum LizenzTyp {

  CC_BY("CC-BY", "https://creativecommons.org/licenses/by/3.0/de/",
      "Dieses Dokument steht unter einer Creative Commons Namensnennung 3.0 Deutschland Lizenz (CC BY)."),

  CC_BY_SA("CC-BY-SA", "https://creativecommons.org/licenses/by-sa/3.0/de/",
      "Dieses Dokument steht unter einer Creative Commons Namensnennung-Weitergabe "
          + "unter gleichen Bedingungen 3.0 Deutschland Lizenz (CC BY-SA)."),

  CC_BY_NC_SA("CC-BY-NC-SA", "https://creativecommons.org/licenses/by-nc-sa/3.0/de/",
      "Dieses Dokument steht unter einer Creative Commons Namensnennung - "
          + "Nicht-kommerziell - Weitergabe unter gleichen Bedingungen 3.0 Deutschland Lizenz(CC BY-NC-SA 3.0 DE)."),

  CC_0("CC-0", "https://creativecommons.org/publicdomain/zero/1.0/deed.de",
      "Dieses Dokument steht unter einer CC0 1.0 Universell Public Domain Dedication Lizenz (CC0 1.0).");

  private final String label;
  private final String url;
  private final String beschreibung;

  LizenzTyp(String label, String url, String beschreibung) {
    this.label = label;
    this.url = url;
    this.beschreibung = beschreibung;
  }

  public String getLabel() {
    return label;
  }

  public String getUrl() {
    return url;
  }

  public String getBeschreibung() {
    return beschreibung;
  }

  @Override
  public String toString() {
    return "Lizenz{"
        + "label='" + label + '\''
        + ", url='" + url + '\''
        + ", beschreibung='" + beschreibung + '\''
        + '}';
  }

}
