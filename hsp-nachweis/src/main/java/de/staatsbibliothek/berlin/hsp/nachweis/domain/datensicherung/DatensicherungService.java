package de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.entities.Datensicherung;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DatensicherungDownload;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.datensicherung.valueobjects.DokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.KatalogRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.DatensicherungDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.XMLDokument;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Getter
@Slf4j
public class DatensicherungService implements DatensicherungBoundary {

  private final DatensicherungRepository datensicherungRepository;
  private final KulturObjektDokumentRepository kodRepository;
  private final BeschreibungsRepository beschreibungRepository;
  private final KatalogRepository katalogRepository;
  private final ZipDateisystemRepository fileRepository;

  @Inject
  public DatensicherungService(
      DatensicherungRepository datensicherungRepository,
      KulturObjektDokumentRepository kodRepository,
      BeschreibungsRepository beschreibungRepository,
      KatalogRepository katalogRepository,
      ZipDateisystemRepository fileRepository) {
    this.datensicherungRepository = datensicherungRepository;
    this.kodRepository = kodRepository;
    this.beschreibungRepository = beschreibungRepository;
    this.katalogRepository = katalogRepository;
    this.fileRepository = fileRepository;
  }

  @Override
  public SortedSet<DokumentTyp> getDokumenteAuswahlOptionen() {
    return new TreeSet<>(Arrays.asList(DokumentTyp.values()));
  }

  @Override
  public void starteDatensicherung(final String datensicherungId) {
    try {
      loadDataAsStreamAndSaveToFilesystem(datensicherungId);
      handleSuccess(datensicherungId);
    } catch (Exception error) {
      log.error(
          "There was an error during fetching TEI XML of Datensicherung {}",
          datensicherungId,
          error);
      handleError(datensicherungId, error.getMessage());
    }
  }

  @Transactional(TxType.REQUIRES_NEW)
  @TransactionConfiguration(timeout = 9000)
  void loadDataAsStreamAndSaveToFilesystem(final String datensicherungId) throws Exception {
    final Datensicherung datensicherung = loadDatensicherung(datensicherungId);

    if (datensicherung.getDokumentenAuswahl().contains(DokumentTyp.KOD)) {
      saveDataStream(datensicherung, kodRepository);
      log.info("Loading data stream for KOD successfully");
    }
    if (datensicherung.getDokumentenAuswahl().contains(DokumentTyp.KATALOG)) {
      saveDataStream(datensicherung, katalogRepository);
      log.info("Loading data stream for Katalog successfully");
    }
    if (datensicherung.getDokumentenAuswahl().contains(DokumentTyp.BESCHREIBUNG)) {
      saveDataStream(datensicherung, beschreibungRepository);
      log.info("Loading data stream for Beschreibung successfully");
    }
  }

  @Transactional(TxType.REQUIRES_NEW)
  @TransactionConfiguration(timeout = 7200)
  void saveDataStream(Datensicherung datensicherung, DatensicherungSelectAllTei repository)
      throws IOException {
    Stream<? extends XMLDokument> documentStream = repository.selectAllTeiXml();
    fileRepository.saveToFile(datensicherung.getDateiName(), documentStream);
  }

  @Override
  public List<Datensicherung> getAlleDatensicherungen() {
    return this.datensicherungRepository.listAll(true).stream().map(Datensicherung::map).toList();
  }

  @Override
  @Transactional(TxType.REQUIRES_NEW)
  public String createInitialDatensicherung(final Set<DokumentTyp> auswahl, final String benutzer)
      throws DatensicherungException {
    final Datensicherung datensicherung = Datensicherung.initialisieren(auswahl, benutzer);
    try {
      final DatensicherungDTO dsDTO =
          datensicherungRepository.save(DatensicherungDTO.map(datensicherung));
      return Datensicherung.map(dsDTO).getId();
    } catch (Exception e) {
      handleError(datensicherung.getId(), e.getMessage());
      throw new DatensicherungException(
          "Error during creation of initial Datensicherung", datensicherung.getId(), e);
    }
  }

  @Override
  @Transactional
  public boolean deleteDatensicherung(final String id) {
    Datensicherung ds = Datensicherung.map(this.datensicherungRepository.findById(id));
    if (ds == null) {
      return false;
    }
    try {
      this.fileRepository.deleteFile(ds.getDateiName());
    } catch (IOException ioe) {
      log.error(
          "There was an error with {} of Datensicherung {} during deletion",
          ds.getDateiName(),
          ds.getId());
      throw new DatensicherungException(
          "There was an error with "
              + ds.getDateiName()
              + " of Datensicherung {"
              + ds.getId()
              + "} during deletion",
          ioe);
    }
    return this.datensicherungRepository.deleteByIdAndFlush(ds.getId());
  }

  @Transactional
  Datensicherung loadDatensicherung(String datensicherungId) {
    DatensicherungDTO dsDTO = datensicherungRepository.findById(datensicherungId);
    if (dsDTO == null) {
      throw new DatensicherungException(
          "Datensicherung with id {" + datensicherungId + "} could not be found", datensicherungId);
    }
    return Datensicherung.map(dsDTO);
  }

  @Transactional
  void handleError(final String datensicherungId, String errorMessage) {
    if (datensicherungId != null) {
      Datensicherung datensicherung = loadDatensicherung(datensicherungId);
      datensicherung.fehlgeschlagen(errorMessage);
      this.datensicherungRepository.save(DatensicherungDTO.map(datensicherung));
    }
  }

  @Transactional
  void handleSuccess(final String datensicherungId) {
    Datensicherung datensicherung = loadDatensicherung(datensicherungId);
    if (datensicherung != null && datensicherung.abschliessen()) {
      datensicherungRepository.save(DatensicherungDTO.map(datensicherung));
    }
  }

  @Override
  @Transactional
  public DatensicherungDownload getFileByDatensicherungId(final String id)
      throws DatensicherungException {
    try {
      Datensicherung ds = Datensicherung.map(this.datensicherungRepository.findById(id));
      return this.fileRepository.getFileDownload(ds.getDateiName());
    } catch (IOException | IllegalArgumentException ioe) {
      throw new DatensicherungException("Error while reading Datensicherung file", ioe);
    }
  }
}
