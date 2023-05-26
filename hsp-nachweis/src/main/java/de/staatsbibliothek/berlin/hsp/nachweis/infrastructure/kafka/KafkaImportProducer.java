/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.ActivityStreamMessageFactory.createMessageForUpload;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStreamObjectTag;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamsDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportUploadDatei;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportVorgang;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 28.05.2020.
 * @version 1.0
 * <p>:
 * Kafka Producer Class for sending all xml data import messages to import service module. These messages contain data
 * for Kulturobjektdokumente, Beschreibungen and all Recherchedaten.
 */

@Singleton
public class KafkaImportProducer extends KafkaTransactionalProducer {

  KafkaImportProducer() {
    super(null, null, null, null, -1);
  }

  @Inject
  public KafkaImportProducer(
      TransactionManager transactionManager,
      @ConfigProperty(name = "kafka.dataimport.topic") String topic,
      @ConfigProperty(name = "kafka.bootstrapserver") String bootstrapserver,
      @ConfigProperty(name = "kafka.dataimport.groupid") String groupid,
      @ConfigProperty(name = "kafka.transaction.timeout") int transactionTimeout) {
    super(transactionManager, topic, bootstrapserver, groupid, transactionTimeout);
  }

  private ActivityStreamMessageFactory activityStreamMessageFactory;

  @Transactional
  public ActivityStream sendImportDateiAsActivityStreamMessage(ImportUploadDatei datei, ActivityStreamsDokumentTyp dokumentTyp,
      ActivityStreamAction action, List<ActivityStreamObjectTag> tags, String actor) throws ActivityStreamsException {

    ActivityStream message = createMessageForUpload(datei,dokumentTyp,action,tags,actor);

    send(message);

    return message;
  }

  public void sendImportJobAsActivityStreamMessage(ImportVorgang importVorgang)
      throws JsonProcessingException, ActivityStreamsException {
    ActivityStream message = activityStreamMessageFactory.createMessageForImportJob(importVorgang);
    send(message);
  }
}
