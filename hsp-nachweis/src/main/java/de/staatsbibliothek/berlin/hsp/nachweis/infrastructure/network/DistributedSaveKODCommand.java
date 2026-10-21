package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.network;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary.SYSTEM_USERNAME;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaNormdatenProducer;
import jakarta.enterprise.context.Dependent;
import jakarta.transaction.Transactional;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 02.02.24.
 * @version 1.0
 */
@Dependent
public class DistributedSaveKODCommand implements DistributedSaveCommand<KulturObjektDokument> {

  private final TeiXmlValidationBoundary teiXmlValidationService;
  private final KulturObjektDokumentRepository kulturObjektDokumentRepository;
  private final SuchDokumentBoundary suchDokumentService;
  private final KafkaIndexingProducer kafkaIndexingProducer;
  private final KafkaNormdatenProducer kafkaNormdatenProducer;

    public DistributedSaveKODCommand(
            TeiXmlValidationBoundary teiXmlValidationService,
      KulturObjektDokumentRepository kulturObjektDokumentRepository,
            SuchDokumentBoundary suchDokumentService,
            KafkaIndexingProducer kafkaIndexingProducer,
      KafkaNormdatenProducer kafkaNormdatenProducer) {
    this.teiXmlValidationService = teiXmlValidationService;
    this.kulturObjektDokumentRepository = kulturObjektDokumentRepository;
    this.suchDokumentService = suchDokumentService;
    this.kafkaIndexingProducer = kafkaIndexingProducer;
    this.kafkaNormdatenProducer = kafkaNormdatenProducer;
  }

  @Override
  @Transactional
  public void execute(KulturObjektDokument kod, ActivityStreamAction action) throws Exception {
    teiXmlValidationService.validateTeiXml(kod);
    kulturObjektDokumentRepository.save(kod);
    suchDokumentService.kodUebernehmen(kod);
      kafkaIndexingProducer.sendKulturobjektDokumentAsActivityStreamMessage(
              kod, action, true, SYSTEM_USERNAME);
      kafkaNormdatenProducer.sendKulturobjektDokumentAsActivityStreamMessage(
              kod, action, true, SYSTEM_USERNAME);
  }
}
