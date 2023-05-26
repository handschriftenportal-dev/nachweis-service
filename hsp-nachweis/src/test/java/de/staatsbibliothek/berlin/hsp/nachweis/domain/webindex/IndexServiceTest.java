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
import static java.nio.file.Files.newInputStream;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument.KulturObjektDokumentBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEI2BeschreibungMapper;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.KODTemplateMapper;
import de.staatsbibliothek.berlin.hsp.messaging.activitystreams.api.model.enums.ActivityStreamAction;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingNoneTransactionalProducer;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.kafka.KafkaIndexingProducer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.TEI;

class IndexServiceTest {

  static String TEI_FILENAME = "tei-msDesc_Westphal.xml";
  static TEI tei = null;
  static int DOCUMENTS_PER_UPDATE = 1000;

  IndexService indexService;
  IndexJobStatusRepository indexJobStatusRepository;
  IndexJobStatusBoundary indexJobStatusBoundry;
  BeschreibungsRepository beschreibungsRepository;
  KafkaIndexingProducer kafkaIndexingProducer;
  KafkaIndexingNoneTransactionalProducer kafkaIndexingNoneTransactionalProducer;
  KulturObjektDokumentRepository kulturObjektDokumentRepository;

  @BeforeEach
  void startUp() throws IOException, JAXBException {
    this.indexJobStatusRepository = mock(IndexJobStatusRepository.class);
    this.indexJobStatusBoundry = new IndexJobStatusService(indexJobStatusRepository);
    this.beschreibungsRepository = mock(BeschreibungsRepository.class);
    this.kafkaIndexingProducer = mock(KafkaIndexingProducer.class);
    this.kafkaIndexingNoneTransactionalProducer = mock(KafkaIndexingNoneTransactionalProducer.class);
    this.kulturObjektDokumentRepository = mock(KulturObjektDokumentRepository.class);

    this.indexService = new IndexService(kulturObjektDokumentRepository,
        kafkaIndexingProducer,
        kafkaIndexingNoneTransactionalProducer,
        DOCUMENTS_PER_UPDATE);

    if (Objects.isNull(tei)) {
      Path teiFilePath = Paths
          .get("../domainmodel-tei-mapper/src", "test", "resources", "tei", TEI_FILENAME);

      try (InputStream is = newInputStream(teiFilePath)) {
        List<TEI> allTEI = TEIObjectFactory.unmarshal(is);
        tei = allTEI.get(0);
      }
    }
  }

  @Test
  void sendKafkaMessageToIndexWithId() throws Exception {
    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = beschreibungMapper.map(tei);

    Beschreibung beschreibung = beschreibungen.get(0);

    when(beschreibungsRepository.findByIdOptional(any())).thenReturn(Optional.of(beschreibung));

    KulturObjektDokument kod = createDummyKulturObjektDokument(beschreibung);

    when(kulturObjektDokumentRepository.findByIdOptional(any())).thenReturn(Optional.of(kod));
    when(kulturObjektDokumentRepository.findById(any())).thenReturn(kod);

    IndexJob indexJob = new IndexJob(indexJobStatusBoundry, "1", false);

    indexService.onSendFrontEndIndexEvent(indexJob);

    verify(kafkaIndexingNoneTransactionalProducer, times(1)).sendKulturobjektDokumentAsActivityStreamMessage(kod,ActivityStreamAction.ADD,true,SYSTEM_USERNAME);
  }

  @Test
  void sendKafkaMessageToIndexWithoutId() throws Exception {
    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = beschreibungMapper.map(tei);

    Beschreibung beschreibung = beschreibungen.get(0);

    when(beschreibungsRepository.findByIdOptional(any())).thenReturn(Optional.of(beschreibung));

    KulturObjektDokument kod = createDummyKulturObjektDokument(beschreibung);

    KulturObjektDokument kod2 = createDummyKulturObjektDokument(beschreibung);

    when(kulturObjektDokumentRepository.listAll()).thenReturn(List.of(kod, kod2));
    when(kulturObjektDokumentRepository.findById(kod.getId())).thenReturn(kod);
    when(kulturObjektDokumentRepository.findById(kod2.getId())).thenReturn(kod2);

    IndexJob indexJob = new IndexJob(indexJobStatusBoundry);

    indexService.onSendFrontEndIndexEvent(indexJob);

    verify(kafkaIndexingNoneTransactionalProducer, times(2)).sendKulturobjektDokumentAsActivityStreamMessage(any(),any(),
        eq(true),eq(SYSTEM_USERNAME));
  }

  @Test
  void testIndexKulturObjektDokument() throws Exception {
    TEI2BeschreibungMapper beschreibungMapper = new TEI2BeschreibungMapper();
    List<Beschreibung> beschreibungen = beschreibungMapper.map(tei);

    Beschreibung beschreibung = beschreibungen.get(0);

    when(beschreibungsRepository.findByIdOptional(any())).thenReturn(Optional.of(beschreibung));

    KulturObjektDokument kod = createDummyKulturObjektDokument(beschreibung);

    indexService.indexKulturObjektDokumentWithoutTransaction(kod, ActivityStreamAction.UPDATE);

    verify(kafkaIndexingNoneTransactionalProducer, times(1)).sendKulturobjektDokumentAsActivityStreamMessage(kod,ActivityStreamAction.UPDATE,true,SYSTEM_USERNAME);
  }

  private KulturObjektDokument createDummyKulturObjektDokument(Beschreibung beschreibung) throws Exception {

    KulturObjektDokument kulturObjektDokument = new KulturObjektDokumentBuilder()
        .withId(TEIValues.UUID_PREFIX + UUID.randomUUID())
        .withRegistrierungsDatum(LocalDateTime.now())
        .withTEIXml("<xml></xml>")
        .build();

    if (beschreibung.getBeschreibungsStruktur() != null
        && beschreibung.getBeschreibungsStruktur().size() > 0) {
      Optional<BeschreibungsKomponenteKopf> kopf = beschreibung.getBeschreibungsStruktur()
          .stream()
          .filter(k -> k instanceof BeschreibungsKomponenteKopf)
          .map(k -> (BeschreibungsKomponenteKopf) k).findFirst();

      kopf.flatMap(beschreibungsKomponenteKopf -> beschreibungsKomponenteKopf.getIdentifikationen().stream()
          .filter(i -> i.getIdentTyp().equals(IdentTyp.GUELTIGE_SIGNATUR))
          .findAny()).ifPresent(kulturObjektDokument::setGueltigeIdentifikation);
    }

    String xml = TEIObjectFactory.marshal(KODTemplateMapper.createTEIFromInitialTemplate(kulturObjektDokument));
    kulturObjektDokument.setTeiXML(xml);

    kulturObjektDokument.getBeschreibungenIDs().add("1");

    return kulturObjektDokument;
  }

}
