package de.staatsbibliothek.berlin.hsp.nachweis.domain.konfiguration;

import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 11.11.22
 */
@Slf4j
@ApplicationScoped
public class KonfigurationService implements KonfigurationBoundary {

  private final KonfigurationRepository konfigurationRepository;

  @Inject
  public KonfigurationService(KonfigurationRepository konfigurationRepository) {
    this.konfigurationRepository = konfigurationRepository;
  }

  @CacheResult(cacheName = "config")
  @Override
  public Optional<String> getWert(String schluessel) {
    log.debug("getWert: schluessel={}", schluessel);
    return konfigurationRepository.getWert(schluessel);
  }

  @Override
  @CacheInvalidateAll(cacheName = "config")
  @Transactional
  public void setWert(String schluessel, String wert) {
    log.info("setWert: schluessel={}, wert={}", schluessel, wert);
    konfigurationRepository.setWert(schluessel, wert);
  }
}
