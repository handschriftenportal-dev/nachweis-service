package de.staatsbibliothek.berlin.hsp.nachweis.domain.purl;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.events.KulturObjektDokumentRegistriertEvent;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.events.PublikationEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class PURLResolverFileScheduler {

  private final PURLBoundary purlBoundary;
  @Setter
  private boolean updateNeeded = true;

  @Inject
  public PURLResolverFileScheduler(PURLBoundary purlBoundary) {
    this.purlBoundary = purlBoundary;
  }

  @Scheduled(every = "${purl.resolver.scheduler-every:off}", concurrentExecution = SKIP)
  void createPURLResolverFile() {
    try {
      if (updateNeeded) {
        purlBoundary.resolverDateiGenerieren();
        setUpdateNeeded(false);
        log.info("PURL Resolver Datei erfolgreich erstellt.");
      } else {
        log.info("PURL Resolver Datei ist aktuell, keine Aktualisierung erforderlich.");
      }
    } catch (Exception e) {
      log.error("Fehler bei der Erstellung der PURL Resolver Datei: {}", e.getMessage(), e);
      setUpdateNeeded(true);
    }
  }

  public void onEvent(@Observes(during = TransactionPhase.AFTER_SUCCESS) PublikationEvent event) {
    updateNeeded = true;
  }

  public void onEvent(@Observes(during = TransactionPhase.AFTER_SUCCESS)
  KulturObjektDokumentRegistriertEvent event) {
    setUpdateNeeded(true);
  }

}
