package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaNormdatenProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.XMLDokument;
import java.util.Iterator;
import jakarta.ejb.Asynchronous;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 13.02.24.
 * @version 1.0
 */
@ApplicationScoped
@Slf4j
public class WerkzeugBatchAllKODAsManuscript {

  private final KulturObjektDokumentRepository kulturObjektDokumentRepository;

  private final KafkaNormdatenProducer kafkaNormdatenProducer;

  private final BearbeiterBoundary bearbeiterBoundary;

  @Getter
  private final WerkzeugBatchJob batch = new WerkzeugBatchJob(
      "Transfer all KOD's into Normdatenservice");


  @Inject
  public WerkzeugBatchAllKODAsManuscript(
      KulturObjektDokumentRepository kulturObjektDokumentRepository,
      KafkaNormdatenProducer kafkaNormdatenProducer, BearbeiterBoundary bearbeiterBoundary) {
    this.kulturObjektDokumentRepository = kulturObjektDokumentRepository;
    this.kafkaNormdatenProducer = kafkaNormdatenProducer;
    this.bearbeiterBoundary = bearbeiterBoundary;
  }

  @Asynchronous
  public void execute() {

    log.info("Starting tranfer of KOD's to Normdatenservice");

    batch.start(bearbeiterBoundary.getLoggedBearbeiter());

    try {

      Iterator<? extends XMLDokument> iterator = kulturObjektDokumentRepository.selectAllTeiXml()
          .iterator();
      int numberOfProcessedKODs = 0;

      while (iterator.hasNext()) {
        XMLDokument kodxml = iterator.next();

        KulturObjektDokument kod = new KulturObjektDokument.KulturObjektDokumentBuilder().withId(
                kodxml.getId())
            .withTEIXml(kodxml.getTeiXml()).build();

        kafkaNormdatenProducer.sendKulturobjektDokumentAsActivityStreamMessage(kod,
            ActivityStreamAction.ADD, true,
            bearbeiterBoundary.getLoggedBearbeiter().getBearbeitername());

        numberOfProcessedKODs++;
      }

      batch.end("Successful transferred number of KOD's " + numberOfProcessedKODs);
      log.info("Ending transfer of KOD's to Normdatenservice {}", numberOfProcessedKODs);
    } catch (Exception error) {
      log.error("Error during transfer KOD's to Normdatenservice", error);
      batch.fail(error.getMessage());
    }
  }

}
