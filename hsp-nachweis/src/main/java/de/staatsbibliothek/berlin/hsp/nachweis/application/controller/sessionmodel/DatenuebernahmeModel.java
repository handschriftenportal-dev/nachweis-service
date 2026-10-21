package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.DataTableStoreController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewMapper;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ImportViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.BeschreibungImport;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.primefaces.PrimeFaces;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 09.07.20
 */
@EqualsAndHashCode(callSuper = true)
@Named
@SessionScoped
@Data
@LoginCheck
public class DatenuebernahmeModel extends DataTableStoreController implements Serializable {

  @Serial
  private static final long serialVersionUID = -9034977801263221245L;
  private static final String DATATABLE_WIDGET_VAR = "importTable";
  private static final String ROOT_CLIENT_ID = "hspnachweisimport:hspnachweisimportTable";

  private static final String ID_DATEN_UEBERPRUEFEN = "datenueberpruefen";
  private static final String ID_IMPORTDATUM = "importDatum";
  private static final String ID_BENUTZER = "benutzer";
  private static final String ID_DATEINAME = "dateiName";
  private static final String ID_FEHLER = "fehler";
  private static final String ID_IMPORTSTATUS = "importStatus";
  private static final List<String> COLUMS_INDEX = new ArrayList<>(
      Arrays.asList(ID_DATEN_UEBERPRUEFEN, ID_IMPORTDATUM, ID_BENUTZER, ID_DATEINAME,
          ID_FEHLER, ID_IMPORTSTATUS));
  private static final List<Boolean> COLUMNS_VISABLE = new ArrayList<>(
      Arrays.asList(true, true, true, true, true, true, true));
  private static final List<String> COLUMNS_WIDTH = new ArrayList<>(
      Arrays.asList("15", "15", "120", "80", "220", "300", "80"));
  private static final List<SortMeta> INITIAL_SORTING = Collections.singletonList(
      SortMeta.builder().field(ID_IMPORTDATUM).order(SortOrder.DESCENDING).build());
  private Logger logger = LoggerFactory.getLogger(DatenuebernahmeModel.class);
  private ImportBoundary importBoundary;

  private ImportViewMapper importViewMapper;

  private String importServerBaseUrl;

  private List<ImportViewModel> allImporte;

  private List<ImportViewModel> filteredImporte;

  private ImportViewModel beschreibungToCheck;

  private BeschreibungImport beschreibungImportJobToAnnehmenOrAblehnen;
  private String beschreibungImportJobToAnnehmenOrAblehnenImportViewId;

  private boolean alleBeschreibungenZugeordnet = false;

  private String beschreibungsVerwaltungsType;

  private ManagedExecutor managedExecutor;

  private DatenuebernahmeStatusModel datenuebernahmeStatusModel;

  DatenuebernahmeModel() {
    super(DATATABLE_WIDGET_VAR, ROOT_CLIENT_ID, COLUMS_INDEX,
        INITIAL_SORTING, COLUMNS_WIDTH, COLUMNS_VISABLE, INIT_HIT_PRO_PAGE,
        ALL_POSSIBLE_HIT_PRO_PAGE);
  }

  @Inject
  public DatenuebernahmeModel(ImportBoundary importBoundary,
      ImportViewMapper importViewMapper,
      ManagedExecutor managedExecutor,
      DatenuebernahmeStatusModel datenuebernahmeStatusModel,
      @ConfigProperty(name = "importserverbaseurl") String importServerBaseUrl) {
    this();
    this.allImporte = new ArrayList<>();
    this.importBoundary = importBoundary;
    this.importViewMapper = importViewMapper;
    this.importServerBaseUrl = importServerBaseUrl;
    this.managedExecutor = managedExecutor;
    this.datenuebernahmeStatusModel = datenuebernahmeStatusModel;

    alleImporteAnzeigen();
  }

  public void importUebernehmen() {
    final BeschreibungImport beschreibungImport = beschreibungImportJobToAnnehmenOrAblehnen;
    FacesContext context = FacesContext.getCurrentInstance();
    if (Objects.nonNull(context)) {
      context.addMessage(ROOT_CLIENT_ID,
          new FacesMessage(FacesMessage.SEVERITY_INFO,
              beschreibungImportJobToAnnehmenOrAblehnenImportViewId,
              "ImportJob wird übernommen."));
    }
    Runnable datenuebernahmeTask = datenuebernahmeTask(beschreibungImport, context);
    if (managedExecutor == null) {
      logger.info(
          "ManagedExecutor not available falling to sync execution instead - for unittest ist this normal behaviour.");
      datenuebernahmeTask.run();
    } else {
      managedExecutor.runAsync(datenuebernahmeTask);
    }
    updateImportJobView();
  }

  private Runnable datenuebernahmeTask(BeschreibungImport beschreibungImport,
      FacesContext context) {
    return () -> {
      final String uebernahmeJobId = beschreibungImportJobToAnnehmenOrAblehnenImportViewId;
      try {
        datenuebernahmeStatusModel.addActiveJob(uebernahmeJobId);
        logger.info("Import uebernommen called for id:{}", beschreibungImport.getId());

        importBoundary.importeUebernehmen(beschreibungImport.getId(), beschreibungsVerwaltungsType);
        datenuebernahmeStatusModel.removeActiveJob(uebernahmeJobId);
        updateImportJobView();
      } catch (Exception ex) {
        if (Objects.nonNull(context)) {
          context.addMessage(ROOT_CLIENT_ID,
              new FacesMessage(FacesMessage.SEVERITY_ERROR, "Beschreibungsimport fehlgeschlagen!",
                  ex.getMessage()));
        }
      } finally {
        datenuebernahmeStatusModel.removeActiveJob(uebernahmeJobId);
      }
    };
  }

  protected void updateImportJobView() {
    // Re-fetch the currently shown jobs and rebuild the list off to the side, then swap the
    // reference in one assignment. Mutating the live list here (it is also called from the async
    // import task) raced with the datatable render — see alleImporteAnzeigen.
    this.allImporte = new ArrayList<>(this.allImporte).stream()
        .map(view -> importBoundary.findById(view.getId()))
        .filter(Optional::isPresent)
        .map(job -> importViewMapper.map(job.get(), false))
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(ArrayList::new));
    this.filteredImporte = null;
  }

  public void importAblehnen() {
    try {
      logger.info("Import abgelehnt called for {}", beschreibungImportJobToAnnehmenOrAblehnen);
      if (beschreibungImportJobToAnnehmenOrAblehnen != null) {
        importBoundary.importeAblehnen(List.of(beschreibungImportJobToAnnehmenOrAblehnen.getId()));

        updateImportJobView();
      }
    } catch (Exception ex) {
      FacesContext context = FacesContext.getCurrentInstance();
      if (Objects.nonNull(context)) {
        context.addMessage(ROOT_CLIENT_ID,
            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Beschreibungsimport ablehnen fehlgeschlagen!",
                ex.getMessage()));
      }
    }
  }

  public void mapChoosenImport(String id) {
    logger.info("mapChoosenImport id:{}", id);
    PrimeFaces current = PrimeFaces.current();

    Optional<BeschreibungImport> job = importBoundary.findById(id);
    if (job.isPresent()) {
      beschreibungToCheck = addImportViewModel(job.get(), true);

      beschreibungImportJobToAnnehmenOrAblehnen = job.get();
      current.executeScript("PF('idleDialog').hide();");
      current.executeScript("PF('datenPruefungsDialog').show();");

      beschreibungImportJobToAnnehmenOrAblehnenImportViewId = id;
    } else {
      current.executeScript("PF('idleDialog').hide();");
      FacesContext context = FacesContext.getCurrentInstance();
      if (Objects.nonNull(context)) {
        context.addMessage(ROOT_CLIENT_ID,
            new FacesMessage(FacesMessage.SEVERITY_WARN, "Kein Import Job!",
                "ImportJob mit ID nicht vorhanden"));
      }
    }
  }

  public void checkBeschreibungen() {
    if (beschreibungToCheck != null) {
      if (beschreibungToCheck.getFehler() != null) {
        alleBeschreibungenZugeordnet = false;
      } else if (beschreibungToCheck.getBeschreibung() != null) {
        alleBeschreibungenZugeordnet = beschreibungToCheck.getBeschreibung().stream()
            .allMatch(BeschreibungsViewModel::isPreconditionState);
      }
    }
  }

  public void alleImporteAnzeigen() {
    logger.info("alleImporteAnzeigen...");

    // Build the list locally and swap the reference in one assignment. The PrimeFaces datatable
    // sorts allImporte in place during render; if we filled the live list element-by-element (as
    // before), a render running while this — possibly slow — load is still in progress would sort
    // a list being structurally modified, causing ConcurrentModificationException.
    List<ImportViewModel> fresh = importBoundary.alleImporteAnzeigen().stream()
        .map(job -> importViewMapper.map(job, false))
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(ArrayList::new));
    this.allImporte = fresh;
    this.filteredImporte = null;
  }

  public ImportViewModel addImportViewModel(BeschreibungImport job,
      boolean doExtractBeschreibungFromActivityStream) {
    ImportViewModel importViewModel = importViewMapper.map(job,
        doExtractBeschreibungFromActivityStream);

    // Mutate a copy and swap, so we never add to the live list while the datatable is rendering.
    List<ImportViewModel> updated = new ArrayList<>(this.allImporte);
    updated.stream()
        .filter(i -> i.getId().equals(Objects.requireNonNull(importViewModel).getId())).findFirst()
        .ifPresentOrElse(j -> {
          j.setSelectedForImport(Objects.requireNonNull(importViewModel).isSelectedForImport());
          j.setFehler(importViewModel.getFehler());
          j.setBeschreibung(importViewModel.getBeschreibung());
          j.setImportStatus(importViewModel.getImportStatus());
        }, () -> updated.add(importViewModel));
    this.allImporte = updated;

    return importViewModel;
  }

  public ImportStatus[] getImportStatus() {
    return ImportStatus.values();
  }

  public ImportStatus getImportStatusForImportView(ImportViewModel importViewModel) {
    return datenuebernahmeStatusModel.getStatusForImportJob(importViewModel);
  }

}
