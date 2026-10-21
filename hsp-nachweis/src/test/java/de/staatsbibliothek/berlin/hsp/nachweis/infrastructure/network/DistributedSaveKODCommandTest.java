package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.network;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary.SYSTEM_USERNAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.validation.TeiXmlValidationBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaNormdatenProducer;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 02.02.24.
 * @version 1.0
 */
public class DistributedSaveKODCommandTest {

  private final TeiXmlValidationBoundary teiXmlValidationService = mock(
      TeiXmlValidationBoundary.class);
  private final KulturObjektDokumentRepository kulturObjektDokumentRepository = mock(
      KulturObjektDokumentRepository.class);
  private final SuchDokumentBoundary suchDokumentService = mock(SuchDokumentBoundary.class);
  private final KafkaIndexingProducer kafkaIndexingProducer = mock(
      KafkaIndexingProducer.class);
  private final KafkaNormdatenProducer kafkaNormdatenProducer = mock(
      KafkaNormdatenProducer.class);
  ;

  @Test
  void testExecute() throws Exception {
    KulturObjektDokument kod = new KulturObjektDokumentBuilder().withId("1")
        .build();

    DistributedSaveKODCommand command = new DistributedSaveKODCommand(teiXmlValidationService,
        kulturObjektDokumentRepository, suchDokumentService, kafkaIndexingProducer,
        kafkaNormdatenProducer);

    command.execute(kod, ActivityStreamAction.ADD);

    verify(teiXmlValidationService, times(1)).validateTeiXml(kod);
    verify(kulturObjektDokumentRepository, times(1)).save(kod);
    verify(suchDokumentService, times(1)).kodUebernehmen(kod);
    verify(kafkaIndexingProducer, times(1)).sendKulturobjektDokumentAsActivityStreamMessage(
        kod, ActivityStreamAction.ADD, true, SYSTEM_USERNAME);
    verify(kafkaNormdatenProducer, times(1)).sendKulturobjektDokumentAsActivityStreamMessage(
        kod, ActivityStreamAction.ADD, true, SYSTEM_USERNAME);
  }
}
