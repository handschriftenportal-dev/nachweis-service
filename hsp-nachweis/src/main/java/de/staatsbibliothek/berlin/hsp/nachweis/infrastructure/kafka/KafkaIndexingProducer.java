package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.ActivityStreamMessageFactory.createMessageForIndexRemoveAll;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.ActivityStreamMessageFactory.createMessageForKatalog;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.ActivityStreamMessageFactory.createMessageForKulturobjektDokument;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.common.TEIUtility;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex.KafkaIndexProducerBoundary;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;
import java.util.Objects;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 13.09.2019.
 */
@ApplicationScoped
public class KafkaIndexingProducer extends KafkaTransactionalProducer
    implements KafkaIndexProducerBoundary {

  private BeschreibungsRepository beschreibungsRepository;

  KafkaIndexingProducer() {
    super(null, null, null, null, -1);
  }

  @Inject
  public KafkaIndexingProducer(
      TransactionManager transactionManager,
      @ConfigProperty(name = "kafka.index.topic") String topic,
      @ConfigProperty(name = "kafka.bootstrapserver") String bootstrapserver,
      @ConfigProperty(name = "kafka.index.groupid") String groupid,
      @ConfigProperty(name = "kafka.transaction.timeout") int transactionTimeout,
      BeschreibungsRepository beschreibungsRepository) {
    super(transactionManager, topic, bootstrapserver, groupid, transactionTimeout);
    this.beschreibungsRepository = beschreibungsRepository;
  }

  @Transactional
  public void send(ActivityStream message, BeschreibungImport beschreibungImportJob) {

    if (Objects.nonNull(message) && Objects.nonNull(beschreibungImportJob)) {
      send(message);
    }
  }

  @Transactional
  public ActivityStream sendKulturobjektDokumentAsActivityStreamMessage(
      KulturObjektDokument kod, ActivityStreamAction action, boolean compressed, String actor)
      throws ActivityStreamsException {

    if (TEIUtility.isTEINullOrEmpty(kod)) {
      throw new IllegalArgumentException("Can't send message because of needed parameter of KOD.");
    }

    ActivityStream message =
        createMessageForKulturobjektDokument(
            kod, action, compressed, actor, beschreibungsRepository);

    send(message);
    return message;
  }

  @Transactional
  public void sendKatalogAsActivityStreamMessage(
      Katalog katalog, ActivityStreamAction action, boolean compressed, String actor)
      throws ActivityStreamsException {

    if (TEIUtility.isTEINullOrEmpty(katalog)) {
      throw new IllegalArgumentException(
          "Can't send message because of needed parameter of Katalog.");
    }

    ActivityStream message = createMessageForKatalog(katalog, action, actor, compressed);
    send(message);
  }

  @Override
  @Transactional
  public ActivityStream sendRemoveAllAsActivityStreamMessage(String actor)
      throws ActivityStreamsException {

    ActivityStream message = createMessageForIndexRemoveAll(actor);

    send(message);

    return message;
  }
}
