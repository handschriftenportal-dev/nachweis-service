package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.ActivityStreamMessageFactory.createMessageForKulturobjektDokumentOnly;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 30.01.24.
 * @version 1.0
 */
@ApplicationScoped
public class KafkaNormdatenProducer extends KafkaTransactionalProducer {

  public KafkaNormdatenProducer() {
    super(null, null, null, null, 0);
  }
  
  @Inject
  KafkaNormdatenProducer(
      TransactionManager transactionManager,
      @ConfigProperty(name = "kafka.normdaten.topic") String topic,
      @ConfigProperty(name = "kafka.bootstrapserver") String bootstrapserver,
      @ConfigProperty(name = "kafka.normdaten.groupid") String groupid,
      @ConfigProperty(name = "kafka.transaction.timeout") int transactionTimeout) {
    super(transactionManager, topic, bootstrapserver, groupid, transactionTimeout);
  }

  @Transactional
  public ActivityStream sendKulturobjektDokumentAsActivityStreamMessage(
      KulturObjektDokument kod, ActivityStreamAction action, boolean compressed, String actor)
      throws ActivityStreamsException {

    ActivityStream message =
        createMessageForKulturobjektDokumentOnly(kod, action, compressed, actor);

    send(message);

    return message;
  }
}
