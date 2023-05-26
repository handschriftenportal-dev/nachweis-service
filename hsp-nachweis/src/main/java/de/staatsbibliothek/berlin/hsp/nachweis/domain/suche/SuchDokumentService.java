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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Dokument;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrServiceException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 12.02.2020.
 */
@ApplicationScoped
@Slf4j
public class SuchDokumentService implements SuchDokumentBoundary, Serializable {

  private static final long serialVersionUID = 8472385579007592015L;

  private final SuchPort suchPort;
  private final KulturObjektDokumentRepository kulturObjektDokumentRepository;
  private final BeschreibungsRepository beschreibungsRepository;
  private final ObjectMapper objectMapper;
  private final int documentsPerUpdate;

  @Inject
  public SuchDokumentService(SuchPort suchPort,
      KulturObjektDokumentRepository kulturObjektDokumentRepository,
      BeschreibungsRepository beschreibungsRepository,
      ObjectMapper objectMapper,
      @ConfigProperty(name = "suchdokumentservice.reindexall.documents_per_update",
          defaultValue = "20") int documentsPerUpdate) {
    this.suchPort = suchPort;
    this.kulturObjektDokumentRepository = kulturObjektDokumentRepository;
    this.beschreibungsRepository = beschreibungsRepository;
    this.objectMapper = objectMapper;
    this.documentsPerUpdate = documentsPerUpdate;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void kodUebernehmen(final KulturObjektDokument kod) throws SolrUebernahmeException {
    log.info("KOD uebernehmen into index...");

    SuchDokument suchDokumentKOD = erzeugeSuchDokument(kod);
    suchDokumentUebernehmen(suchDokumentKOD);
    log.info("KOD updated. (ID: {}, Signatur: {})", kod.getId(), kod.getGueltigeIdentifikation().getIdent());
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void beschreibungUebernehmen(final Beschreibung beschreibung) throws SolrUebernahmeException {
    log.info("Beschreibung uebernehmen into index...");

    SuchDokument suchDokumentBeschreibung = erzeugeSuchDokument(beschreibung);
    suchDokumentUebernehmen(suchDokumentBeschreibung);
    log.info("Beschreibung updated (ID: {} ).", beschreibung.getId());
  }

  @Override
  public void reindexAllKulturObjektDokumente() throws SolrUebernahmeException {
    deleteAllByTyp(SuchDokumentTyp.KOD);

    List<String> kodIds = kulturObjektDokumentRepository.listAll()
        .stream().map(KulturObjektDokument::getId)
        .collect(Collectors.toList());

    List<List<String>> sublists = Lists.partition(kodIds, documentsPerUpdate);

    log.info("Sending {} KODs to SOLR in {} sublists.", kodIds.size(), sublists.size());
    int sublistCounter = 0;
    for (List<String> sublist : sublists) {
      log.info("Indexing sublist {}.", ++sublistCounter);
      kodsUebernehmenOhneSOLRTransaktion(sublist);
    }

    commitSOLR();
    log.info("Send {} KODs to SOLR - Ready", kodIds.size());
  }

  @Override
  public void reindexAllBeschreibungen() throws SolrUebernahmeException {
    deleteAllByTyp(SuchDokumentTyp.BS);

    List<String> beschreibungenIds = beschreibungsRepository.listAll()
        .stream().map(Beschreibung::getId)
        .collect(Collectors.toList());

    List<List<String>> sublists = Lists.partition(beschreibungenIds, documentsPerUpdate);

    log.info("Sending {} Beschreibungen to SOLR in {} sublists.", beschreibungenIds.size(), sublists.size());
    int sublistCounter = 0;
    for (List<String> sublist : sublists) {
      log.info("Indexing sublist {}.", ++sublistCounter);
      beschreibungenUebernehmenOhneSOLRTransaktion(sublist);
    }

    commitSOLR();

    log.info("Send {} Beschreibungen to SOLR - Ready", beschreibungenIds.size());
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void dokumentLoeschen(String... iDs) throws SolrUebernahmeException {
    try {
      suchPort.delete(iDs);
    } catch (Exception e) {
      log.error("Error during delete Dokument with id {} {}", iDs, e);
      throw new SolrUebernahmeException("Error during delete Dokument with id " + Arrays.toString(iDs), e);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  void suchDokumentUebernehmen(final SuchDokument... suchDokumente)
      throws SolrUebernahmeException {
    try {
      suchPort.update(true, suchDokumente);
    } catch (Exception e) {
      log.error("Error during Dokument uebernehmen with id {} {}", suchDokumente, e);
      throw new SolrUebernahmeException(e);
    }
  }

  @Override
  public Ergebnis search(final Suche suche) throws SolrServiceException {
    return suchPort.search(suche);
  }

  void kodsUebernehmenOhneSOLRTransaktion(List<String> kodIds) throws SolrUebernahmeException {
    List<SuchDokument> suchDokumente = new ArrayList<>(kodIds.size());
    for (String kodId : kodIds) {
      KulturObjektDokument kod = kulturObjektDokumentRepository.findById(kodId);
      suchDokumente.add(erzeugeSuchDokument(kod));
    }
    suchDokumentUebernehmenOhneTransaktion(suchDokumente.toArray(new SuchDokument[0]));
  }

  void beschreibungenUebernehmenOhneSOLRTransaktion(List<String> beschreibungenIds) throws SolrUebernahmeException {
    List<SuchDokument> suchDokumente = new ArrayList<>(beschreibungenIds.size());
    for (String beschreibungId : beschreibungenIds) {
      Beschreibung beschreibung = beschreibungsRepository.findById(beschreibungId);
      suchDokumente.add(erzeugeSuchDokument(beschreibung));
    }
    suchDokumentUebernehmenOhneTransaktion(suchDokumente.toArray(new SuchDokument[0]));
  }

  void suchDokumentUebernehmenOhneTransaktion(final SuchDokument... suchDokumente)
      throws SolrUebernahmeException {
    try {
      suchPort.updateWithoutTransaction(false, suchDokumente);
    } catch (Exception e) {
      log.error("Error during Dokument uebernehmen with id {} {}", suchDokumente, e);
      throw new SolrUebernahmeException(e);
    }
  }

  void deleteAllByTyp(SuchDokumentTyp suchDokumentTyp) throws SolrUebernahmeException {
    Objects.requireNonNull(suchDokumentTyp, "suchDokumentTyp is required");

    try {
      log.info("Deleting all existing documents from SOLR for type {}", suchDokumentTyp);
      suchPort.deleteBySuchDokumentTypWithoutTransaction(suchDokumentTyp);
      log.info("Deleted all existing documents from SOLR for type {} - Ready", suchDokumentTyp);
    } catch (Exception e) {
      log.error("Error deleting all documents of type {} from SOLR: {}", suchDokumentTyp, e.getMessage(), e);
      throw new SolrUebernahmeException(
          "Error deleting all documents of type " + suchDokumentTyp + " from SOLR: " + e.getMessage(), e);
    }
  }

  void commitSOLR() throws SolrUebernahmeException {
    log.info("Committing SOLR...");
    try {
      suchPort.commitWithoutTransaction();
      log.info("Committing SOLR - Ready");
    } catch (Exception e) {
      log.error("Error committing SOLR: {}", e.getMessage(), e);
      throw new SolrUebernahmeException("Error committing SOLR: " + e.getMessage(), e);
    }
  }

  SuchDokument erzeugeSuchDokument(final KulturObjektDokument kod) throws SolrUebernahmeException {
    if (Objects.isNull(kod)) {
      throw new SolrUebernahmeException("KulturObjektDokument must not be null!");
    }

    return SuchDokumentMapper.map(kod, mapDokumentToJson(kod, kod.getId()));
  }

  SuchDokument erzeugeSuchDokument(final Beschreibung beschreibung) throws SolrUebernahmeException {
    if (Objects.isNull(beschreibung)) {
      throw new SolrUebernahmeException("Beschreibung must not be null!");
    }
    return SuchDokumentMapper.map(beschreibung, mapDokumentToJson(beschreibung, beschreibung.getId()));
  }

  String mapDokumentToJson(Dokument dokument, String id) throws SolrUebernahmeException {
    try {
      return objectMapper.writeValueAsString(dokument);
    } catch (JsonProcessingException e) {
      throw new SolrUebernahmeException("Could not write JSON String of Dokument: " + id, e);
    }
  }

}
