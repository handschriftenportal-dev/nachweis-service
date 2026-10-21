package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 20.05.25.
 * @version 1.0
 */

public record NormdatenserviceSync(int intervalInMinutes, boolean active,
                                   String kafkaTopic) {

  @Override
  public String toString() {
    return "NormdatenserviceSync[" +
        "intervalInMinutes=" + intervalInMinutes + ", " +
        "active=" + active + ", " +
        "kafkaTopic=" + kafkaTopic + ']';
  }


}
