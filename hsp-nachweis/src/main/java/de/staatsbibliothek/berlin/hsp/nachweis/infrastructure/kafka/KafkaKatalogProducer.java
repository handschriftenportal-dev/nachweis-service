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

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.ActivityStreamMessageFactory.createMessageForKatalog;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.exceptions.ActivityStreamsException;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.ActivityStream;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
  public class KafkaKatalogProducer extends KafkaTransactionalProducer {

  KafkaKatalogProducer() {
    super(null, null, null, null, -1);
  }

  @Inject
  public KafkaKatalogProducer(
      TransactionManager transactionManager,
      @ConfigProperty(name = "kafka.katalog.topic") String topic,
      @ConfigProperty(name = "kafka.bootstrapserver") String bootstrapserver,
      @ConfigProperty(name = "kafka.katalog.groupid") String groupid,
      @ConfigProperty(name = "kafka.transaction.timeout") int transactionTimeout) {
    super(transactionManager, topic, bootstrapserver, groupid, transactionTimeout);
  }

  @Transactional
  public void send(ActivityStream message, BeschreibungImport beschreibungImportJob) {

    if (Objects.nonNull(message) && Objects.nonNull(beschreibungImportJob)) {
      send(message);
    }
  }

  @Transactional
  public ActivityStream sendKatalogAsActivityStreamMessage(
      Katalog katalog, ActivityStreamAction action, boolean compressed, String actor)
      throws ActivityStreamsException {

    if(Objects.isNull(katalog) || Objects.isNull(katalog.getId()) || Objects.isNull(katalog.getTeiXML()) || katalog.getTeiXML().isEmpty())
    {
      throw new IllegalArgumentException("Can't send message because of needed parameter of Katalog.");
    }

    ActivityStream message = createMessageForKatalog(katalog,action,actor,compressed);

    send(message);

    return message;
  }
}
