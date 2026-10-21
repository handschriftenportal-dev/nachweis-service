package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Dokument;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 12.02.2020.
 */
@ApplicationScoped
public class SuchDokumentService implements SuchDokumentBoundary, Serializable {

  @Serial
  private static final long serialVersionUID = 8472385579007592015L;
  private static final Logger log = org.slf4j.LoggerFactory.getLogger(SuchDokumentService.class);

  private final SuchPort suchPort;
  private final KulturObjektDokumentRepository kulturObjektDokumentRepository;
  private final BeschreibungsRepository beschreibungsRepository;
  private final ObjectMapper objectMapper;
  private final int documentsPerUpdate;

  private final BearbeiterBoundary bearbeiterBoundary;

  @Inject
  public SuchDokumentService(
      SuchPort suchPort,
      KulturObjektDokumentRepository kulturObjektDokumentRepository,
      BeschreibungsRepository beschreibungsRepository,
      ObjectMapper objectMapper,
      @ConfigProperty(
          name = "suchdokumentservice.reindexall.documents_per_update",
          defaultValue = "20")
      int documentsPerUpdate,
      BearbeiterBoundary bearbeiterBoundary) {
    this.suchPort = suchPort;
    this.kulturObjektDokumentRepository = kulturObjektDokumentRepository;
    this.beschreibungsRepository = beschreibungsRepository;
    this.objectMapper = objectMapper;
    this.documentsPerUpdate = documentsPerUpdate;
    this.bearbeiterBoundary = bearbeiterBoundary;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void kodUebernehmen(final KulturObjektDokument kod) throws SolrUebernahmeException {
    log.info("KOD uebernehmen into index...");

    SuchDokument suchDokumentKOD = erzeugeSuchDokument(kod);
    suchDokumentUebernehmen(suchDokumentKOD);
    log.info(
        "KOD updated. (ID: {}, Signatur: {})",
        kod.getId(),
        kod.getGueltigeIdentifikation().getIdent());
  }

  @Transactional
  @Override
  public void allKODsUebernehmen(List<KulturObjektDokument> kods) throws SolrUebernahmeException {

    suchDokumentUebernehmen(
        kods.stream()
            .filter(Objects::nonNull)
            .map(
                kod -> {
                  try {
                    return SuchDokumentMapper.map(kod, mapDokumentToJson(kod, kod.getId()));
                  } catch (SolrUebernahmeException e) {
                    throw new RuntimeException(e);
                  }
                })
            .toArray(SuchDokument[]::new));
  }

  @Override
  @Transactional
  public void allBeschreibungenUebernehmen(List<Beschreibung> beschreibungen)
      throws SolrUebernahmeException {
    suchDokumentUebernehmen(
        beschreibungen.stream()
            .filter(Objects::nonNull)
            .map(
                beschreibung -> {
                  try {
                    return SuchDokumentMapper.map(
                        beschreibung,
                        mapDokumentToJson(beschreibung, beschreibung.getId()),
                        bearbeiterBoundary);
                  } catch (SolrUebernahmeException e) {
                    throw new RuntimeException(e);
                  }
                })
            .toArray(SuchDokument[]::new));
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void beschreibungUebernehmen(final Beschreibung beschreibung)
      throws SolrUebernahmeException {
    log.info("Beschreibung uebernehmen into index...");

    SuchDokument suchDokumentBeschreibung = erzeugeSuchDokument(beschreibung);
    suchDokumentUebernehmen(suchDokumentBeschreibung);
    log.info("Beschreibung updated (ID: {} ).", beschreibung.getId());
  }

  @Override
  public void reindexAllKulturObjektDokumente() throws SolrUebernahmeException {
    deleteAllByTyp(SuchDokumentTyp.KOD);

    List<SuchDokument> suchDokumente = new ArrayList<>(documentsPerUpdate);
    log.info("Load all KulturObjektDokument from database...");
    List<KulturObjektDokument> all = kulturObjektDokumentRepository.listAll();

    int totalPages = getTotalPages(all.size(), documentsPerUpdate);

    int currentPage = 0;
    try {
      for (KulturObjektDokument kulturObjektDokument : all) {
        suchDokumente.add(erzeugeSuchDokument(kulturObjektDokument));
        if (suchDokumente.size() >= documentsPerUpdate) {
          currentPage = commitPageWithoutTransaction(suchDokumente, currentPage, totalPages);
        }
      }
      commitPageWithoutTransaction(suchDokumente, currentPage, totalPages);
    } catch (SolrUebernahmeException e) {
      log.error("Error during commit to solr for KulturObjektDokument", e);
      throw e;
    }
  }

  @Override
  public void reindexAllBeschreibungen() throws SolrUebernahmeException {
    deleteAllByTyp(SuchDokumentTyp.BS);

    beschreibungenUebernehmenOhneSOLRTransaktion();
    log.info("Send Beschreibungen to SOLR - Done");
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void dokumentLoeschen(String... iDs) throws SolrUebernahmeException {
    try {
      suchPort.delete(iDs);
    } catch (Exception error) {
      throw new SolrUebernahmeException(
          "Error during delete Dokument with id " + Arrays.toString(iDs), error);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  void suchDokumentUebernehmen(final SuchDokument... suchDokumente) throws SolrUebernahmeException {
    try {
      String details = Arrays.stream(suchDokumente)
          .map(doc -> doc.getSuchDokumentTyp() + " " + doc.getId())
          .collect(Collectors.joining(","));
      log.info("Send Update to Apache Solr for documents {}", details);
      suchPort.update(true, suchDokumente);
    } catch (Exception e) {
      throw new SolrUebernahmeException(
          "Error during Dokument uebernehmen with id " + Arrays.toString(
              suchDokumente), e);
    }
  }

  @Override
  public Ergebnis search(final Suche suche) throws SolrServiceException {
    return suchPort.search(suche);
  }

  void beschreibungenUebernehmenOhneSOLRTransaktion() throws SolrUebernahmeException {
    List<SuchDokument> suchDokumente = new ArrayList<>(documentsPerUpdate);
    log.info("Load all Beschreibungen suche dokuments from database...");
    List<Beschreibung> all = beschreibungsRepository.listAll();

    int totalPages = getTotalPages(all.size(), documentsPerUpdate);

    int currentPage = 0;
    try {
      for (Beschreibung beschreibung : all) {
        suchDokumente.add(erzeugeSuchDokument(beschreibung));
        if (suchDokumente.size() >= documentsPerUpdate) {
          currentPage = commitPageWithoutTransaction(suchDokumente, currentPage, totalPages);
        }
      }
      commitPageWithoutTransaction(suchDokumente, currentPage, totalPages);
    } catch (SolrUebernahmeException e) {
      log.error("Error during commit to solr for Beschreibung", e);
      throw e;
    }
  }

  private int getTotalPages(int collectionSize, int documentsPerUpdate) {
    int totalPages = collectionSize / documentsPerUpdate;
    if (totalPages * documentsPerUpdate < collectionSize) {
      totalPages++;
    }
    return totalPages;
  }

  int commitPageWithoutTransaction(List<SuchDokument> suchDokumente, int currentPage, int totalPages)
      throws SolrUebernahmeException {
    if (!suchDokumente.isEmpty()) {
      currentPage++;
      log.info("Indexing {} page {} of {}.", suchDokumente.getFirst().getSuchDokumentTyp(), currentPage, totalPages);
      suchDokumentUebernehmenOhneTransaktion(suchDokumente.toArray(new SuchDokument[0]));
      commitSOLR();
      suchDokumente.clear();
    }
    return currentPage;
  }

  void suchDokumentUebernehmenOhneTransaktion(final SuchDokument... suchDokumente)
      throws SolrUebernahmeException {
    try {
      suchPort.updateWithoutTransaction(false, suchDokumente);
    } catch (Exception e) {
      throw new SolrUebernahmeException(
          "Error during Dokument uebernehmen with id " + Arrays.toString(
              suchDokumente), e);
    }
  }

  void deleteAllByTyp(SuchDokumentTyp suchDokumentTyp) throws SolrUebernahmeException {
    Objects.requireNonNull(suchDokumentTyp, "suchDokumentTyp is required");

    try {
      log.info("Deleting all existing documents from SOLR for type {}", suchDokumentTyp);
      suchPort.deleteBySuchDokumentTypWithoutTransaction(suchDokumentTyp);
      log.info("Deleted all existing documents from SOLR for type {} - Done", suchDokumentTyp);
    } catch (Exception e) {
      throw new SolrUebernahmeException(
          "Error deleting all documents of type "
              + suchDokumentTyp
              + " from SOLR: "
              + e.getMessage(),
          e);
    }
  }

  void commitSOLR() throws SolrUebernahmeException {
    try {
      suchPort.commitWithoutTransaction();
      log.info("Comitted on Solr");
    } catch (Exception e) {
      throw new SolrUebernahmeException("Error committing SOLR: " + e.getMessage(), e);
    }
  }


  SuchDokument erzeugeSuchDokument(final KulturObjektDokument kod)
      throws SolrUebernahmeException {
    if (Objects.isNull(kod)) {
      throw new SolrUebernahmeException("KulturObjektDokument must not be null!");
    }

    return SuchDokumentMapper.map(kod, mapDokumentToJson(kod, kod.getId()));
  }

  SuchDokument erzeugeSuchDokument(final Beschreibung beschreibung) throws SolrUebernahmeException {
    if (Objects.isNull(beschreibung)) {
      throw new SolrUebernahmeException("Beschreibung must not be null!");
    }
    return SuchDokumentMapper.map(
        beschreibung, mapDokumentToJson(beschreibung, beschreibung.getId()), bearbeiterBoundary);
  }

  String mapDokumentToJson(Dokument dokument, String id) throws SolrUebernahmeException {
    try {
      return objectMapper.writeValueAsString(dokument);
    } catch (JsonProcessingException e) {
      throw new SolrUebernahmeException("Could not write JSON String of Dokument: " + id, e);
    }
  }
}
