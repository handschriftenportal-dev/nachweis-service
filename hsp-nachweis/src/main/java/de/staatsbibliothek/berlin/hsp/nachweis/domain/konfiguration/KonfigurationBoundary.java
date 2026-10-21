package de.staatsbibliothek.berlin.hsp.nachweis.domain.konfiguration;

import java.util.Optional;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 11.11.22
 */
public interface KonfigurationBoundary {

  Optional<String> getWert(String schluessel);

  void setWert(String schluessel, String wert);

}
