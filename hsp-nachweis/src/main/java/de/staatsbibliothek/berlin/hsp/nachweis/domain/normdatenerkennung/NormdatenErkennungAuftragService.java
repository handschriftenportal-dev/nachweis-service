package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.normdatenerkennung.NormdatenErkennungAnfrage;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.normdatenerkennung.NormdatenErkennungGraphQLClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class NormdatenErkennungAuftragService {

  private final NormdatenErkennungAuftragRepository auftragRepository;
  private final NormdatenErkennungEintragRepository eintragRepository;
  private final NormdatenErkennungGraphQLClient normdatenErkennungClient;
  private final NormdatenErkennungRequestParamExtractor paramExtractor;
  private final BeschreibungsBoundary beschreibungsBoundary;
  private final int cleanupProcessingMinutes;

  @Inject
  public NormdatenErkennungAuftragService(
      NormdatenErkennungAuftragRepository auftragRepository,
      NormdatenErkennungEintragRepository eintragRepository,
      NormdatenErkennungGraphQLClient normdatenErkennungClient,
      NormdatenErkennungRequestParamExtractor paramExtractor,
      BeschreibungsBoundary beschreibungsBoundary,
      @ConfigProperty(name = "normdatenerkennung.cleanup.processing-minutes", defaultValue = "10")
      int cleanupProcessingMinutes) {
    this.auftragRepository = auftragRepository;
    this.eintragRepository = eintragRepository;
    this.normdatenErkennungClient = normdatenErkennungClient;
    this.paramExtractor = paramExtractor;
    this.beschreibungsBoundary = beschreibungsBoundary;
    this.cleanupProcessingMinutes = cleanupProcessingMinutes;
  }

  @Transactional(TxType.REQUIRED)
  public void enqueueBeschreibungAuftraege(List<String> beschreibungIds, Bearbeiter bearbeiter) {
    for (String id : beschreibungIds) {
      var auftrag = new NormdatenErkennungAuftrag(id, bearbeiter);
      auftragRepository.save(auftrag);
      log.info("Enqueued auftrag {} for beschreibung {}", auftrag.getId(), id);
    }
  }

  @Transactional(TxType.REQUIRED)
  public List<NormdatenErkennungAuftrag> findAllAuftraege() {
    return auftragRepository.listAll();
  }

  /** REQUIRES_NEW: each stale reset commits independently even if the scheduler invocation later fails. */
  @Transactional(TxType.REQUIRES_NEW)
  public int resetStaleProcessingAuftraege() {
    var threshold = LocalDateTime.now().minusMinutes(cleanupProcessingMinutes);
    var stale = auftragRepository
        .find("status = ?1 and updatedAt < ?2", NormdatenErkennungAuftragStatus.PROCESSING, threshold)
        .list();
    for (var auftrag : stale) {
      auftrag.resetToPending();
    }
    return stale.size();
  }

  /** REQUIRES_NEW: claim commits immediately so another scheduler instance cannot pick the same auftrag. */
  @Transactional(TxType.REQUIRES_NEW)
  public Optional<NormdatenErkennungAuftrag> claimNextPendingAuftrag() {
    var auftrag = auftragRepository
        .findFirstByStatus(NormdatenErkennungAuftragStatus.PENDING);
    auftrag.ifPresent(a -> {
      a.startProcessing();
      auftragRepository.save(a);
    });
    return auftrag;
  }

  @Transactional(TxType.REQUIRED)
  public void processAuftrag(String auftragId) {
    var auftrag = auftragRepository.findById(auftragId);
    if (auftrag == null) {
      log.error("processAuftrag: auftrag {} not found", auftragId);
      return;
    }

    List<NormdatenErkennungRequestParam> params;
    try {
      var beschreibung = beschreibungsBoundary.findById(auftrag.getBeschreibungsdokumentId())
          .orElseThrow(() -> new IllegalStateException(
              "Beschreibung not found: " + auftrag.getBeschreibungsdokumentId()));
      params = paramExtractor.extract(beschreibung.getTeiXML());
    } catch (Exception e) {
      log.error("Error loading request params for auftrag {}", auftragId, e);
      auftrag.markError(e.getMessage());
      auftragRepository.save(auftrag);
      return;
    }

    var ergebnisse = params.stream()
        .map(this::queryNormdatum)
        .toList();
    ergebnisse.forEach(auftrag::addErgebnis);
    auftrag.finishProcessing();
    auftragRepository.save(auftrag);
  }

  @Transactional(TxType.REQUIRED)
  public void startApplying(String auftragId, Bearbeiter bearbeiter) {
    var auftrag = auftragRepository.findById(auftragId);
    if (auftrag == null) {
      return;
    }
    auftrag.startApplying(bearbeiter);
    auftragRepository.save(auftrag);
  }

  @Transactional(TxType.REQUIRED)
  public Optional<NormdatenErkennungAuftrag> getNextApplyingAuftrag() {
    return auftragRepository.findFirstByStatus(NormdatenErkennungAuftragStatus.APPLYING);
  }

  @Transactional(TxType.REQUIRED)
  public void cancelAuftrag(String auftragId) {
    var auftrag = auftragRepository.findById(auftragId);
    if (auftrag == null) {
      return;
    }
    auftrag.cancel();
    auftragRepository.save(auftrag);
  }

  @Transactional
  public void setSelectionNone(String eintragId, Bearbeiter reviewer) {
    var eintrag = eintragRepository.findById(eintragId);
    if (eintrag == null) {
      return;
    }
    eintrag.setSelectionNone(reviewer);
    eintragRepository.save(eintrag);
  }

  @Transactional
  public void setSelectionRecommendation(String eintragId, Bearbeiter reviewer) {
    var eintrag = eintragRepository.findById(eintragId);
    if (eintrag == null) {
      return;
    }
    eintrag.setSelectionRecommendation(reviewer);
    eintragRepository.save(eintrag);
  }

  @Transactional
  public void setSelectionMatchIndex(String eintragId, int index, Bearbeiter reviewer) {
    var eintrag = eintragRepository.findById(eintragId);
    if (eintrag == null) {
      return;
    }
    eintrag.setSelectionMatchIndex(index, reviewer);
    eintragRepository.save(eintrag);
  }

  @Transactional
  public void setSelectionManualIdentifier(String eintragId, String identifier,
      Bearbeiter reviewer) {
    var eintrag = eintragRepository.findById(eintragId);
    if (eintrag == null) {
      return;
    }
    eintrag.setSelectionManualIdentifier(identifier, reviewer);
    eintragRepository.save(eintrag);
  }

  private NormdatenErkennungEintrag queryNormdatum(NormdatenErkennungRequestParam param) {
    var ergebnis = new NormdatenErkennungEintrag(
        param.xpath(),
        param.typ(),
        param.displayText()
    );
    var anfrage = new NormdatenErkennungAnfrage(
        param.existingId(),
        param.displayText(),
        param.typ()
    );
    try {
      ergebnis.setAnfrage(anfrage);
      log.debug("Querying normdaten for '{}' at {}", param.displayText(), param.xpath());
      normdatenErkennungClient.normdatenerkennung(anfrage)
          .handle(
              ergebnis::setErgebnis,
              ergebnis::setErrorMessage
          );
    } catch (Exception e) {
      log.error("Error querying normdaten for '{}'", param.displayText(), e);
      ergebnis.setErrorMessage(e);
    }
    return ergebnis;
  }

  @Transactional
  public Optional<NormdatenErkennungAuftrag> findById(String id) {
    return Optional.ofNullable(auftragRepository.findById(id));
  }

  @Transactional
  public Optional<NormdatenErkennungAuftrag> findByIdWithErgebnisse(String id) {
    return auftragRepository.findByIdWithErgebnisse(id);
  }

  @Transactional
  public void save(NormdatenErkennungAuftrag auftrag) {
    auftragRepository.persist(auftrag);
  }
}
