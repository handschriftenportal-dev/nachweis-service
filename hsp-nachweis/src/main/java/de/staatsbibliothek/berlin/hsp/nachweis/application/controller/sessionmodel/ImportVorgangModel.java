package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.DataTableStoreController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport.ImportVorgangBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.dataimport.ImportVorgangException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportVorgang;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 01.09.20
 */
@EqualsAndHashCode(callSuper = true)
@Named
@ViewScoped
@Slf4j
@Data
@LoginCheck
public class ImportVorgangModel extends DataTableStoreController implements Serializable {

  private static final long serialVersionUID = -9198417990704556695L;

  private static final String DATATABLE_WIDGET_VAR = "vorgaengeTableWidget";
  private static final String ROOT_CLIENT_ID = "importvorgaengeForm:importvorgaengeTable";
  private static final String ID_STATUS_INDICATOR = "statusIndicator";
  private static final String ID_STATUS = "status";
  private static final String ID_SELECTION = "selection";
  private static final String ID_DATUM = "datum";
  private static final String ID_DATEN_TYP = "datentyp";
  private static final String ID_DATEINAME = "dateiname";
  private static final String ID_DATENFORMAT = "datenformat";
  private static final String ID_BENUTZER_NAME = "benutzerName";
  private static final String ID_FEHLER_TEIL1 = "fehlerTeil1";
  private static final String ID_FEHLER_TEIL2 = "fehlerTeil2";
  private static final String ID_FEHLER_TEIL3 = "fehlerTeil3";
  private static final String ID_FEHLER_TEIL4 = "fehlerTeil4";

  private static final List<String> COLUMS_INDEX = new ArrayList<>(
      Arrays.asList(ID_SELECTION, ID_STATUS_INDICATOR, ID_STATUS, ID_DATUM, ID_DATEN_TYP,
          ID_DATEINAME,
          ID_DATENFORMAT, ID_BENUTZER_NAME, ID_FEHLER_TEIL1, ID_FEHLER_TEIL2, ID_FEHLER_TEIL3,
          ID_FEHLER_TEIL4));
  private static final List<Boolean> COLUMNS_VISABLE = new ArrayList<>(
      Arrays.asList(true, true, true, true, true, true, true, true, false, false, false, false));
  private static final List<String> COLUMNS_WIDTH = new ArrayList<>(
      Arrays.asList("10", "20", "100", "100", "100", "300", "100", "100", "100", "100", "100",
          "100"));

  private static final List<SortMeta> INITIAL_SORTING = Collections
      .singletonList(SortMeta.builder().field(ID_DATUM).order(SortOrder.DESCENDING).build());

  private ImportVorgangBoundary importVorgangBoundary;

  private List<ImportVorgang> vorgaenge;

  private List<ImportVorgang> filteredVorgaenge;

  private List<ImportVorgang> selectedVorgaenge;

  private BearbeiterBoundary bearbeiterBoundary;

  ImportVorgangModel() {
    super(DATATABLE_WIDGET_VAR, ROOT_CLIENT_ID, COLUMS_INDEX,
        INITIAL_SORTING, COLUMNS_WIDTH, COLUMNS_VISABLE, INIT_HIT_PRO_PAGE,
        ALL_POSSIBLE_HIT_PRO_PAGE);
  }

  @Inject
  public ImportVorgangModel(ImportVorgangBoundary importVorgangBoundary,
      BearbeiterBoundary bearbeiterBoundary) {
    this();
    this.importVorgangBoundary = importVorgangBoundary;
    this.bearbeiterBoundary = bearbeiterBoundary;
  }

  @PostConstruct
  public void loadAllVorgaenge() {
    List<ImportVorgang> all = new ArrayList<>(importVorgangBoundary.findAll());
    if (vorgaenge == null) {
      vorgaenge = new ArrayList<>(all);
    } else {
      vorgaenge.clear();
      vorgaenge.addAll(all);
    }

    vorgaenge.sort(Comparator.comparing(ImportVorgang::getDatum));
    filteredVorgaenge = null;
    selectedVorgaenge = null;
  }

  public String mapBenutzernameToBearbeitername(String benutzerName) {
    Bearbeiter bearbeiter = bearbeiterBoundary.findBearbeiterWithName(benutzerName);

    if (bearbeiter == null) {
      bearbeiter = bearbeiterBoundary.getUnbekannterBearbeiter();
    }

    if (bearbeiter.getVorname() != null && bearbeiter.getNachname() != null) {
      return bearbeiter.getVorname() + " " + bearbeiter.getNachname();
    } else {
      return bearbeiter.getBearbeitername();
    }
  }

  public void deleteSelectedVorgange() {
    try {
      if (selectedVorgaenge != null) {
        importVorgangBoundary.deleteVorgaenge(selectedVorgaenge);
      }
    } catch (ImportVorgangException exception) {
      log.error("Error in deleteSelectedImportJobs", exception);
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          getMessage("import_jobs_loeschen_fehler"),
          exception.getMessage());
      sendViewMessage(null, msg);
    } finally {
      loadAllVorgaenge();
    }
  }
}
