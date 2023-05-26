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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.webindex;

import static de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary.SYSTEM_USERNAME;

import com.google.common.collect.Lists;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.event.DomainEvents.sendIndexJobEvent;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingNoneTransactionalProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingProducer;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class IndexService {

  public static final Logger log = LoggerFactory.getLogger(IndexService.class);
  private final KulturObjektDokumentRepository kulturObjektDokumentRepository;
  private final KafkaIndexingProducer kafkaIndexingProducer;
  private final KafkaIndexingNoneTransactionalProducer kafkaIndexingNoneTransactionalProducer;
  private final int documentsPerUpdate;

  @Inject
  public IndexService(
      KulturObjektDokumentRepository kulturObjektDokumentRepository,
      KafkaIndexingProducer kafkaIndexingProducer,
      KafkaIndexingNoneTransactionalProducer kafkaIndexingNoneTransactionalProducer,
      @ConfigProperty(name = "indexservice.reindexall.documents_per_update", defaultValue = "1000")
      int documentsPerUpdate) {
    this.kulturObjektDokumentRepository = kulturObjektDokumentRepository;
    this.kafkaIndexingProducer = kafkaIndexingProducer;
    this.kafkaIndexingNoneTransactionalProducer = kafkaIndexingNoneTransactionalProducer;
    this.documentsPerUpdate = documentsPerUpdate;
  }

  @Transactional
  public void indexKulturObjektDokumentWithKafkaTransaction(KulturObjektDokument kod,
      ActivityStreamAction activityStreamAction) {
    indexKulturObjektDokument(kod, activityStreamAction, kafkaIndexingProducer);
  }

  private void indexKulturObjektDokument(KulturObjektDokument kod, ActivityStreamAction activityStreamAction, KafkaIndexProducerBoundary kafkaIndexProducerBoundary) {

    if (Objects.nonNull(kod) && Objects.nonNull(kod.getTeiXML()) && !kod.getTeiXML().isEmpty()) {
      log.info("indexKulturObjektDokument: kodId={}, activityStreamAction={}", kod.getId(), activityStreamAction);

      try {
        kafkaIndexProducerBoundary.sendKulturobjektDokumentAsActivityStreamMessage(kod,activityStreamAction,true,SYSTEM_USERNAME);
      } catch (Exception exception) {
        log.error("Error during index producer message creation", exception);
      }
    } else {
      log.error("Missing Elements for sending Kafka Message");
    }
  }

  public void onSendFrontEndIndexEvent(@ObservesAsync @sendIndexJobEvent IndexJob indexJob) {

    log.info("Received onSendFrontEndIndexEvent");

    List<String> kods = findKodsForIndexJob(indexJob);

    ActivityStreamAction activityStreamAction = indexJob.getActivityStreamAction().orElse(ActivityStreamAction.ADD);

    log.info("Start Indexing of KOD's {} ", kods.size());

    if (indexJob.isWithTransaction()) {
      Consumer<BiConsumer<KulturObjektDokument, ActivityStreamAction>> indexAllKODs = createIndexAllKODsConsumer(
          indexJob,
          kods, activityStreamAction);
      indexKODsWithKafkaTransaction(indexAllKODs);
    } else {
      indexingInSublists(indexJob, kods, activityStreamAction);
    }
    log.info("Finishing Indexing of KOD's");
  }

  @Transactional
  @TransactionConfiguration(timeoutFromConfigProperty = "kafka.transaction.timeout")
  void indexKODsWithKafkaTransaction(Consumer<BiConsumer<KulturObjektDokument, ActivityStreamAction>> consumer) {
    consumer.accept(this::indexKulturObjektDokumentWithKafkaTransaction);
  }

  @Transactional(TxType.REQUIRES_NEW)
  @TransactionConfiguration(timeout = 7200)
  void indexKODsWithoutKafkaTransaction(Consumer<BiConsumer<KulturObjektDokument, ActivityStreamAction>> consumer) {
    consumer.accept(this::indexKulturObjektDokumentWithoutTransaction);
  }

  Consumer<BiConsumer<KulturObjektDokument, ActivityStreamAction>> createIndexAllKODsConsumer(IndexJob indexJob,
      Collection<String> kods, ActivityStreamAction activityStreamAction) {

    return indexConsumer -> {
      float counter = 0;

      for (String kodId : kods) {
        counter++;
        log.info("Starting FrontEnd Index Update for Kod Id {} number of KOD {} ", kodId, counter);
        indexConsumer.accept(kulturObjektDokumentRepository.findById(kodId), activityStreamAction);
        final float actualStatusPercentage = counter / kods.size() * 100;
        DecimalFormat df = new DecimalFormat("#.##");
        indexJob.getIndexJobStatus()
            .ifPresent(indexJobStatus -> indexJobStatus.setStatusInPercentage(df.format(actualStatusPercentage) + "%"));
      }

    };
  }

  public void indexKulturObjektDokumentWithoutTransaction(KulturObjektDokument kod,
      ActivityStreamAction activityStreamAction) {
    indexKulturObjektDokument(kod, activityStreamAction, kafkaIndexingNoneTransactionalProducer);
  }

  private void indexingInSublists(IndexJob indexJob, List<String> kods, ActivityStreamAction activityStreamAction) {
    List<List<String>> subLists = Lists.partition(kods, this.documentsPerUpdate);

    log.info("Sending {} KODs to Kafka in {} subLists.", kods.size(), subLists.size());
    int subListCounter = 0;
    for (List<String> sublist : subLists) {
      log.info("Sending subList {}.", ++subListCounter);

      Consumer<BiConsumer<KulturObjektDokument, ActivityStreamAction>> indexSublistKODs =
          createIndexAllKODsConsumer(indexJob, sublist, activityStreamAction);
      indexKODsWithoutKafkaTransaction(indexSublistKODs);
    }
  }

  private List<String> findKodsForIndexJob(IndexJob indexJob) {
    List<String> kods = new ArrayList<>();

    indexJob.getKodId()
        .ifPresentOrElse(
            kodId -> kulturObjektDokumentRepository.findByIdOptional(kodId).map(KulturObjektDokument::getId)
                .ifPresent(kods::add), () ->
                kods.addAll(kulturObjektDokumentRepository.listAll().stream().map(KulturObjektDokument::getId).collect(
                    Collectors.toList()))
        );
    return kods;
  }
}
