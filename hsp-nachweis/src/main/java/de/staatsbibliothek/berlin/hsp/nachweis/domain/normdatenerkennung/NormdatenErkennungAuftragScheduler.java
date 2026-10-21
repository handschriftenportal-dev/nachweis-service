package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class NormdatenErkennungAuftragScheduler {

  private final NormdatenErkennungAuftragService auftragService;
  private final NormdatenErkennungApplyService applyService;

  @Inject
  public NormdatenErkennungAuftragScheduler(NormdatenErkennungAuftragService auftragService,
      NormdatenErkennungApplyService applyService) {
    this.auftragService = auftragService;
    this.applyService = applyService;
  }

  @Scheduled(every = "${normdatenerkennung.scheduler.process-interval:10s}",
      concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
  public void processQueue() {
    auftragService.claimNextPendingAuftrag().ifPresent(auftrag -> {
      String auftragId = auftrag.getId();
      log.info("Processing Normdatenerkennung Auftrag {}", auftragId);
      auftragService.processAuftrag(auftragId);
    });
  }

  @Scheduled(every = "${normdatenerkennung.scheduler.apply-interval:10s}",
      concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
  public void applyQueue() {
    auftragService.getNextApplyingAuftrag().ifPresent(auftrag -> {
      String auftragId = auftrag.getId();
      log.info("Applying Normdatenerkennung Auftrag {}", auftragId);
      applyService.applyAuftrag(auftragId);
    });
  }

  @Scheduled(every = "${normdatenerkennung.scheduler.stale-recovery-interval:5m}",
      concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
  public void recoverStaleProcessing() {
    int recovered = auftragService.resetStaleProcessingAuftraege();
    if (recovered > 0) {
      log.warn("Reset {} stale PROCESSING Aufträge to PENDING", recovered);
    }
  }
}
