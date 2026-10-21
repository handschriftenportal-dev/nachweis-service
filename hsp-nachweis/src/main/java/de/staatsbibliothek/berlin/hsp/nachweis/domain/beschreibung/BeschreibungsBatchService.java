package de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary.SYSTEM_USERNAME;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SolrUebernahmeException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentService;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingProducer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Persistiert KOD- und Beschreibungs-Batches aus dem Import und propagiert sie an Solr und Kafka.
 * Erwartet, dass die TEI-Dokumente bereits via {@code BeschreibungsSynchronizer} aktualisiert
 * wurden; diese Klasse kümmert sich nur noch um das Persistieren und Indexieren.
 */
@Slf4j
@ApplicationScoped
public class BeschreibungsBatchService {

  private KulturObjektDokumentRepository kulturObjektDokumentRepository;
  private BeschreibungsRepository beschreibungsRepository;
  private SuchDokumentService suchDokumentService;
  private KafkaIndexingProducer kafkaIndexingProducer;

  protected BeschreibungsBatchService() {
  }

  @Inject
  public BeschreibungsBatchService(
      KulturObjektDokumentRepository kulturObjektDokumentRepository,
      BeschreibungsRepository beschreibungsRepository,
      SuchDokumentService suchDokumentService,
      KafkaIndexingProducer kafkaIndexingProducer) {
    this.kulturObjektDokumentRepository = kulturObjektDokumentRepository;
    this.beschreibungsRepository = beschreibungsRepository;
    this.suchDokumentService = suchDokumentService;
    this.kafkaIndexingProducer = kafkaIndexingProducer;
  }

  /**
   * Speichert KODs und Beschreibungen in Batch, übernimmt beide in Solr und sendet jeden KOD mit
   * TEI und Beschreibungen an das Kafka-Index-Topic.
   *
   * <p>KODs ohne TEI oder ohne Beschreibungen werden beim Kafka-Send übersprungen (sie werden aber
   * dennoch gespeichert und in Solr übernommen).
   */
  @Transactional
  public void publizierenBatch(Collection<KulturObjektDokument> kods,
      Collection<Beschreibung> beschreibungen)
      throws ActivityStreamsException, SolrUebernahmeException {
    log.info("publizierenBatch: {} KOD(s), {} Beschreibung(en)", kods.size(),
        beschreibungen.size());
    List<KulturObjektDokument> kodList = new ArrayList<>(kods);
    List<Beschreibung> beschreibungList = new ArrayList<>(beschreibungen);

    saveAggregates(kodList, beschreibungList);
    sendToNachweisSolr(kodList, beschreibungList);
    sendToIndexer(kodList);
  }

  private void sendToNachweisSolr(List<KulturObjektDokument> kodList,
      List<Beschreibung> beschreibungList)
      throws SolrUebernahmeException {
    suchDokumentService.allKODsUebernehmen(kodList);
    suchDokumentService.allBeschreibungenUebernehmen(beschreibungList);
  }

  private void saveAggregates(List<KulturObjektDokument> kods, List<Beschreibung> beschreibungen) {
    kulturObjektDokumentRepository.saveAll(kods);
    beschreibungsRepository.saveAll(beschreibungen);
  }

  private void sendToIndexer(List<KulturObjektDokument> kodList) throws ActivityStreamsException {
    for (KulturObjektDokument kod : kodList) {
      sendToIndexer(kod);
    }
  }

  private void sendToIndexer(KulturObjektDokument kod) throws ActivityStreamsException {
    if (kod == null || !kod.isIndexable()) {
      log.debug("Skipping Kafka send for KOD {} (not indexable)",
          kod == null ? null : kod.getId());
      return;
    }
    kafkaIndexingProducer.sendKulturobjektDokumentAsActivityStreamMessage(
        kod, ActivityStreamAction.UPDATE, true, SYSTEM_USERNAME);
  }
}
