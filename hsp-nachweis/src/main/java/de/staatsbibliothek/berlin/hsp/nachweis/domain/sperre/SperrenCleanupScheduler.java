package de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import io.quarkus.scheduler.Scheduled;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class SperrenCleanupScheduler {

  private final DokumentSperreBoundary service;

  @Inject
  public SperrenCleanupScheduler(DokumentSperreBoundary service) {
    this.service = service;
  }

  @Scheduled(every = "{sperre.cleanup.interval}")
  void cleanupSperren() {
    List<Sperre> expiredSperren = getExpiredSperren();
    expiredSperren.forEach(sperreToRelease -> {
          try {
            service.releaseSperre(sperreToRelease);
          } catch (DokumentSperreException e) {
            log.error("Failed to release sperre: {}", e.getMessage());
          }
        }
    );
  }

  private List<Sperre> getExpiredSperren() {
    return service.findExpiredSperren();
  }
}
