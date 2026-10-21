package de.staatsbibliothek.berlin.hsp.nachweis.domain.konfiguration;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Konfiguration;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.CRUDRepository;
import java.util.Optional;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 11.11.22
 */
public interface KonfigurationRepository extends CRUDRepository<Konfiguration, String> {

  Optional<String> getWert(String schluessel);

  void setWert(String schluessel, String wert);

}
