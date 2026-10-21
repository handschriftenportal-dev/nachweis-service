package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;
import static java.nio.charset.StandardCharsets.UTF_8;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.WerkzeugeRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchAllBeschreibungenValidate;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchAllKODAsManuscript;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchAllKatalogeValidate;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchAllKulturobjekteValidate;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchAutomaticDataTakeover;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchIndexAllBeschreibungen;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchIndexAllKODs;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchIndexPraesentation;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchJobStatus;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchUpdateAllKODTEI;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugBatchUpdateNormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug.WerkzeugOverrideAttributsReferenzen;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.omnifaces.util.Messages;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 06.03.2023
 */
@Slf4j
@ViewScoped
@Named
@LoginCheck
public class WerkzeugeController implements Serializable {

  static final String REPLICATION_STATUS_ENABLED_KEY = "werkzeug_praesentation_replication_enabled";
  static final String REPLICATION_STATUS_DISABLED_KEY = "werkzeug_praesentation_replication_disabled";
  static final String REPLICATION_STATUS_UNKNOWN_KEY = "werkzeug_praesentation_replication_unknown";

  @Serial
  private static final long serialVersionUID = 3733676236470666514L;
  private static final String APPLICATION_CSV = "application/csv";
  private static final String EMPTY = "<empty>";
  private final DokumentSperreBoundary dokumentSperreBoundary;
  private final WerkzeugBatchIndexAllKODs werkzeugBatchJobIndexKODs;
  private final WerkzeugBatchIndexAllBeschreibungen werkzeugBatchJobIndexBeschreibungen;
  private final WerkzeugBatchAutomaticDataTakeover werkzeugBatchJobDatenuebernahme;
  @Getter
  private final WerkzeugBatchAllKODAsManuscript werkzeugBatchAllKODAsManuscript;
  @Getter
  private final WerkzeugBatchAllBeschreibungenValidate werkzeugBatchAllBeschreibungenValidate;
  @Getter
  private final WerkzeugBatchUpdateNormdatenReferenz werkzeugBatchUpdateNormdatenReferenz;
  @Getter
  private final WerkzeugOverrideAttributsReferenzen werkzeugOverrideAttributsReferenzen;
  @Getter
  private final WerkzeugBatchAllKulturobjekteValidate werkzeugBatchAllKulturobjekteValidate;
  @Getter
  private final WerkzeugBatchUpdateAllKODTEI werkzeugBatchUpdateAllKODTEI;
  @Getter
  private final WerkzeugBatchAllKatalogeValidate werkzeugBatchAllKatalogeValidate;
  @Getter
  private final WerkzeugBatchIndexPraesentation werkzeugBatchIndexPraesentation;
  private final WerkzeugeRechte werkzeugeRechte;
  @Getter
  private boolean speereExists;
  @Getter
  @Setter
  private boolean replicationToggle;
  @Getter
  @Setter
  private boolean attributsreferenzenOverride;

  @Inject
  public WerkzeugeController(
      DokumentSperreBoundary dokumentSperreBoundary,
      WerkzeugBatchIndexAllKODs werkzeugBatchJobIndexKODs,
      WerkzeugBatchIndexAllBeschreibungen werkzeugBatchIndexAllBeschreibungen,
      WerkzeugBatchAutomaticDataTakeover werkzeugBatchAutomaticDataTakeover,
      WerkzeugBatchAllKODAsManuscript werkzeugBatchAllKODAsManuscript,
      WerkzeugBatchAllBeschreibungenValidate werkzeugBatchAllBeschreibungenValidate,
      WerkzeugBatchUpdateNormdatenReferenz werkzeugBatchUpdateNormdatenReferenz,
      WerkzeugOverrideAttributsReferenzen werkzeugOverrideAttributsReferenzen,
      WerkzeugBatchAllKulturobjekteValidate werkzeugBatchAllKulturobjekteValidate,
      WerkzeugBatchUpdateAllKODTEI werkzeugBatchUpdateAllKODTEI,
      WerkzeugBatchAllKatalogeValidate werkzeugBatchAllKatalogeValidate,
      WerkzeugBatchIndexPraesentation werkzeugBatchIndexPraesentation,
      WerkzeugeRechte werkzeugeRechte) {

    this.dokumentSperreBoundary = dokumentSperreBoundary;
    this.werkzeugBatchJobIndexKODs = werkzeugBatchJobIndexKODs;
    this.werkzeugBatchJobIndexBeschreibungen = werkzeugBatchIndexAllBeschreibungen;
    this.werkzeugBatchJobDatenuebernahme = werkzeugBatchAutomaticDataTakeover;
    this.werkzeugBatchAllKODAsManuscript = werkzeugBatchAllKODAsManuscript;
    this.werkzeugBatchAllBeschreibungenValidate = werkzeugBatchAllBeschreibungenValidate;
    this.werkzeugBatchUpdateNormdatenReferenz = werkzeugBatchUpdateNormdatenReferenz;
    this.werkzeugOverrideAttributsReferenzen = werkzeugOverrideAttributsReferenzen;
    this.werkzeugBatchAllKulturobjekteValidate = werkzeugBatchAllKulturobjekteValidate;
    this.werkzeugBatchUpdateAllKODTEI = werkzeugBatchUpdateAllKODTEI;
    this.werkzeugBatchAllKatalogeValidate = werkzeugBatchAllKatalogeValidate;
    this.werkzeugBatchIndexPraesentation = werkzeugBatchIndexPraesentation;
    this.werkzeugeRechte = werkzeugeRechte;
  }

  public void setup() {
    log.debug("setup started");

    refreshReplicationStatus();
    attributsreferenzenOverride = werkzeugOverrideAttributsReferenzen
        .isAttributsreferenzenOverride();

    try {
      speereExists = !dokumentSperreBoundary.findAll().isEmpty();
    } catch (Exception e) {
      speereExists = true;
      log.error("error in find all sperren", e);
      sendViewMessage(
          Messages.createError(
              getMessage("werkzeuge_attributsreferenzen_sperren_fehler"),
              e.getMessage()));
    }
  }

  protected void sendViewMessage(FacesMessage message) {

    FacesContext context = FacesContext.getCurrentInstance();

    if (context != null) {
      context.addMessage("globalmessages", message);
    }
  }

  public WerkzeugBatchIndexAllKODs getWerkzeugEventIndexKODs() {
    return werkzeugBatchJobIndexKODs;
  }

  public WerkzeugBatchIndexAllBeschreibungen getWerkzeugEventIndexBeschreibungen() {
    return werkzeugBatchJobIndexBeschreibungen;
  }

  public WerkzeugBatchAutomaticDataTakeover getWerkzeugEventDatenuebernahme() {
    return werkzeugBatchJobDatenuebernahme;
  }

  public boolean isCsvValidationDownloadAvailableODD() {
    return werkzeugBatchAllBeschreibungenValidate.getBatchOdd() != null
        && werkzeugBatchAllBeschreibungenValidate.getBatchOdd().getDownload() != null
        && werkzeugBatchAllBeschreibungenValidate.getBatchOdd().getDownload().length > 0;
  }

  public boolean isCsvValidationDownloadAvailableKOD() {
    return werkzeugBatchAllKulturobjekteValidate != null
        && werkzeugBatchAllKulturobjekteValidate.getBatch() != null
        && werkzeugBatchAllKulturobjekteValidate.getBatch().getDownload() != null
        && werkzeugBatchAllKulturobjekteValidate.getBatch().getDownload().length > 0;
  }

  public boolean isCsvValidationDownloadAvailableBeschALL() {
    return werkzeugBatchAllBeschreibungenValidate.getBatchAll() != null
        && werkzeugBatchAllBeschreibungenValidate.getBatchAll().getDownload() != null
        && werkzeugBatchAllBeschreibungenValidate.getBatchAll().getDownload().length > 0;
  }

  public boolean isCsvValidationDownloadAvailableKatalog() {
    return werkzeugBatchAllKatalogeValidate != null
        && werkzeugBatchAllKatalogeValidate.getBatch() != null
        && werkzeugBatchAllKatalogeValidate.getBatch().getDownload() != null
        && werkzeugBatchAllKatalogeValidate.getBatch().getDownload().length > 0;
  }

  public StreamedContent csvValidationDownloadKOD() {
    byte[] data = (werkzeugBatchAllKulturobjekteValidate.getBatch() != null)
        ? werkzeugBatchAllKulturobjekteValidate.getBatch().getDownload() : null;
    if (log.isDebugEnabled()) {
      log.debug("Download CSV (KOD) {}",
          data != null ? new String(data, UTF_8) : EMPTY);
    }
    return DefaultStreamedContent.builder()
        .name("CSV-KulturObjektValidationResult.csv")
        .contentType(APPLICATION_CSV)
        .stream(() -> new ByteArrayInputStream(data != null ? data : new byte[0]))
        .build();
  }

  public StreamedContent csvValidationDownloadBeschODD() {
    byte[] data = werkzeugBatchAllBeschreibungenValidate.getBatchOdd().getDownload();
    if (log.isDebugEnabled()) {
      log.debug("Download CSV (Beschreibungen ODD) {}",
          data != null ? new String(data, UTF_8) : EMPTY);
    }
    return DefaultStreamedContent.builder()
        .name("CSV-BeschreibungValidationResult-TEI-ODD.csv")
        .contentType(APPLICATION_CSV)
        .stream(() -> new ByteArrayInputStream(data != null ? data : new byte[0]))
        .build();
  }

  public StreamedContent csvValidationDownloadBeschALL() {
    byte[] data = werkzeugBatchAllBeschreibungenValidate.getBatchAll().getDownload();
    if (log.isDebugEnabled()) {
      log.debug("Download CSV (Beschreibungen ALL) {}",
          data != null ? new String(data, UTF_8) : EMPTY);
    }
    return DefaultStreamedContent.builder()
        .name("CSV-BeschreibungValidationResult-TEI-ALL.csv")
        .contentType(APPLICATION_CSV)
        .stream(() -> new ByteArrayInputStream(data != null ? data : new byte[0]))
        .build();
  }

  public StreamedContent csvValidationDownloadKatalog() {
    byte[] data = (werkzeugBatchAllKatalogeValidate.getBatch() != null)
        ? werkzeugBatchAllKatalogeValidate.getBatch().getDownload() : null;
    if (log.isDebugEnabled()) {
      log.debug("Download CSV (Kataloge) {}",
          data != null ? new String(data, UTF_8) : EMPTY);
    }
    return DefaultStreamedContent.builder()
        .name("CSV-KatalogValidationResult-TEI-ALL.csv")
        .contentType(APPLICATION_CSV)
        .stream(() -> new ByteArrayInputStream(data != null ? data : new byte[0]))
        .build();
  }

  public boolean isAnyRunning() {
    boolean beschOddRunning =
        werkzeugBatchAllBeschreibungenValidate != null
            && werkzeugBatchAllBeschreibungenValidate.getBatchOdd() != null
            && werkzeugBatchAllBeschreibungenValidate.getBatchOdd().getStatus()
            == WerkzeugBatchJobStatus.RUNNING;

    boolean beschAllRunning =
        werkzeugBatchAllBeschreibungenValidate != null
            && werkzeugBatchAllBeschreibungenValidate.getBatchAll() != null
            && werkzeugBatchAllBeschreibungenValidate.getBatchAll().getStatus()
            == WerkzeugBatchJobStatus.RUNNING;

    boolean kodRunning =
        werkzeugBatchAllKulturobjekteValidate != null
            && werkzeugBatchAllKulturobjekteValidate.getBatch() != null
            && werkzeugBatchAllKulturobjekteValidate.getBatch().getStatus()
            == WerkzeugBatchJobStatus.RUNNING;

    boolean katalogRunning =
        werkzeugBatchAllKatalogeValidate != null
            && werkzeugBatchAllKatalogeValidate.getBatch() != null
            && werkzeugBatchAllKatalogeValidate.getBatch().getStatus()
            == WerkzeugBatchJobStatus.RUNNING;

    return beschOddRunning || beschAllRunning || kodRunning || katalogRunning;
  }

  public boolean isNormdatenAktualisierenDisabled() {
    return !werkzeugeRechte.kannNormdatenAktualisieren()
        || werkzeugBatchUpdateNormdatenReferenz.getBatch().getStatus()
        == WerkzeugBatchJobStatus.RUNNING;
  }

  public boolean isKodKerndatenBeiImportUeberschreibenDisabled() {
    return !werkzeugeRechte.kannKodKerndatenBeiImportUeberschreiben();
  }

  public boolean isAktualisierenAlleKodXmlDisabled() {
    return !werkzeugeRechte.kannAktualisierenAlleKodXml()
        || werkzeugBatchUpdateAllKODTEI.getBatch().getStatus() == WerkzeugBatchJobStatus.RUNNING;
  }

  public boolean isIndexAktualisierenBeschreibungenDisabled() {
    return !werkzeugeRechte.kannBeschreibungenIndexAktualisieren()
        || werkzeugBatchJobIndexBeschreibungen.getBatch().getStatus()
        == WerkzeugBatchJobStatus.RUNNING;
  }

  public boolean isIndexAktualisierenKodsDisabled() {
    return !werkzeugeRechte.kannKodsIndexAktualisieren()
        || werkzeugBatchJobIndexKODs.getBatch().getStatus() == WerkzeugBatchJobStatus.RUNNING;
  }

  public boolean isIndexPraesentationDisabled() {
    return !werkzeugeRechte.kannPraesentationIndexAktualisieren()
        || werkzeugBatchIndexPraesentation.getBatch().getStatus() == WerkzeugBatchJobStatus.RUNNING
        || werkzeugBatchIndexPraesentation.isReplicationEnabled();
  }

  public boolean isReplicationToggleDisabled() {
    return !werkzeugeRechte.kannPraesentationIndexAktualisieren()
        || werkzeugBatchIndexPraesentation.getBatch().getStatus() == WerkzeugBatchJobStatus.RUNNING;
  }

  public String getReplicationStatusKey() {
    Boolean replicationEnabled = werkzeugBatchIndexPraesentation.getReplicationEnabled();
    if (replicationEnabled == null) {
      return REPLICATION_STATUS_UNKNOWN_KEY;
    }
    return replicationEnabled ? REPLICATION_STATUS_ENABLED_KEY : REPLICATION_STATUS_DISABLED_KEY;
  }

  public boolean isReplicationStatusUnknown() {
    return werkzeugBatchIndexPraesentation.getReplicationEnabled() == null;
  }

  public void refreshReplicationStatus() {
    werkzeugBatchIndexPraesentation.refreshReplicationStatus();
    // unknown state renders as "enabled" so the execute-button stays disabled
    replicationToggle = werkzeugBatchIndexPraesentation.isReplicationEnabled();
  }

  public void indexPraesentationStarten() {
    try {
      werkzeugBatchIndexPraesentation.execute();
    } catch (IllegalStateException error) {
      log.warn("Presentation reindex rejected: {}", error.getMessage());
      sendViewMessage(Messages.createWarn(
          getMessage("werkzeug_praesentation_index_fehler"),
          getMessage("werkzeug_praesentation_index_replikation_aktiv")));
    } catch (Exception error) {
      log.error("Error starting the presentation reindex", error);
      sendViewMessage(Messages.createWarn(
          getMessage("werkzeug_praesentation_index_fehler"),
          error.getMessage()));
    }
  }

  public void onAttributsreferenzenOverrideToggle() {
    try {
      werkzeugOverrideAttributsReferenzen.setAttributsreferenzenOverride(
          attributsreferenzenOverride);
    } catch (Exception error) {
      log.error("Error setting the attributsreferenzen override", error);
      sendViewMessage(Messages.createError(
          getMessage("werkzeug_attributsReferenzenToggle_fehler"), error.getMessage()));
    } finally {
      // re-sync the toggle with the actual konfiguration, e.g., after a failed switch
      attributsreferenzenOverride = werkzeugOverrideAttributsReferenzen
          .isAttributsreferenzenOverride();
    }
  }

  public void onReplicationToggle() {
    try {
      werkzeugBatchIndexPraesentation.toggleReplication(replicationToggle);
    } catch (Exception error) {
      log.error("Error toggling solr replication", error);
      sendViewMessage(Messages.createError(
          getMessage("werkzeug_praesentation_replication_toggle_fehler"), error.getMessage()));
    } finally {
      // re-sync the toggle with the actual solr state, e.g., after a half-failed switch
      replicationToggle = werkzeugBatchIndexPraesentation.isReplicationEnabled();
    }
  }
}
