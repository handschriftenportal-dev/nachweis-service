package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Konfiguration;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.konfiguration.KonfigurationRepository;
import java.util.Optional;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 11.11.22
 */
@ApplicationScoped
public class KonfigurationRepositoryAdapter implements KonfigurationRepository {

  @Override
  public Optional<String> getWert(String schluessel) {
    return findByIdOptional(schluessel).map(Konfiguration::getWert);
  }

  @Override
  public void setWert(String schluessel, String wert) {
    saveAndFlush(new Konfiguration(schluessel, wert));
  }
}
