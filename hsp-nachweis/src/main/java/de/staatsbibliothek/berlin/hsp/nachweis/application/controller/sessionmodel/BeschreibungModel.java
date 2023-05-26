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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte.PERMISSION_BESCHREIBUNG_ALLER_DRITTER_HART_LOESCHEN;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.DataTableStoreController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.HspLazyDataModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.BeschreibungListDTO;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.primefaces.PrimeFaces;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

/**
 * @author Christoph Marten
 * @since 12.01.21
 */
@SessionScoped
@Named
@Slf4j
@LoginCheck
public class BeschreibungModel extends DataTableStoreController implements Serializable {

  private static final long serialVersionUID = 3475288928199978004L;

  private static final String DATATABLE_WIDGET_VAR = "beschreibungListTableWidget";
  private static final String ROOT_CLIENT_ID = "beschreibungListform:beschreibungListTable";

  private static final String ID_SELECTION = "selection";
  private static final String ID_GUELTIGE_SIGNATUR = "gueltigeSignatur";
  private static final String ID_BESITZER = "besitzer";
  private static final String ID_AUFBEWAHRUNGSORT = "aufbewahrungsort";
  private static final String ID_AUTOR = "autor";
  private static final String ID_HSP_ID = "hspId";
  private static final String ID_KOD_ID = "kodId";
  private static final String ID_AENDERUNGS_DATUM = "aenderungsdatum";
  private static final String ID_ERSTELLUNGS_DATUM = "erstellungsdatum";
  private static final String ID_VERWALTUNGS_TYP = "verwaltungsTyp";
  private static final String ID_KATALOG_ID = "katalogId";
  private static final String ID_DOKUMENT_OBJECT_TYP = "dokumentObjektTyp";

  private static final List<String> COLUMS_INDEX = new ArrayList<>(
      Arrays.asList(ID_SELECTION, ID_VERWALTUNGS_TYP, ID_GUELTIGE_SIGNATUR, ID_BESITZER,
          ID_AUFBEWAHRUNGSORT, ID_AUTOR, ID_HSP_ID, ID_KOD_ID, ID_AENDERUNGS_DATUM,
          ID_ERSTELLUNGS_DATUM, ID_DOKUMENT_OBJECT_TYP, ID_KATALOG_ID));
  private static final List<Boolean> COLUMNS_VISABLE = new ArrayList<>(
      Arrays.asList(true, true, true, true, true, true, false, true, true, false, false, false));
  private static final List<String> COLUMNS_WIDTH = new ArrayList<>(
      Arrays.asList("20", "30", "100", "100", "100", "100", "100", "75", "50", "50", "75", "50"));

  private static final List<SortMeta> INITIAL_SORTING = Collections
      .singletonList(SortMeta.builder().field(ID_ERSTELLUNGS_DATUM).order(SortOrder.ASCENDING).build());

  private LazyDataModel<BeschreibungListDTO> beschreibungenDataModel;

  private List<BeschreibungListDTO> selectedBeschreibungen;

  private BeschreibungsBoundary beschreibungBoundary;

  private BearbeiterBoundary bearbeiterBoundary;

  private BeschreibungsRechte beschreibungsRechte;

  private boolean deleteEnabled;

  BeschreibungModel() {
    super(DATATABLE_WIDGET_VAR, ROOT_CLIENT_ID, COLUMS_INDEX, INITIAL_SORTING, COLUMNS_WIDTH,
        COLUMNS_VISABLE, INIT_HIT_PRO_PAGE, ALL_POSSIBLE_HIT_PRO_PAGE);
    deleteEnabled = true;
  }

  @Inject
  public BeschreibungModel(BeschreibungsBoundary beschreibungBoundary,
      BeschreibungsRechte beschreibungsRechte,
      BearbeiterBoundary bearbeiterBoundary,
      @ConfigProperty(name = "workaround.BESCHREIBUNG_LIST_DELETE_ENABLED") boolean deleteEnabled) {
    this();
    log.info("BESCHREIBUNG delete enabled {}", deleteEnabled);
    this.deleteEnabled = deleteEnabled;
    this.beschreibungBoundary = beschreibungBoundary;
    this.beschreibungsRechte = beschreibungsRechte;
    this.bearbeiterBoundary = bearbeiterBoundary;
  }

  @PostConstruct
  protected void setup() {
    log.info("setup started");

    this.beschreibungenDataModel = new HspLazyDataModel<>() {
      private static final long serialVersionUID = 3080177565246695274L;

      private Map<String, Boolean> sortByBoolean;
      private Map<String, String> filterByString;

      @Override
      public List<BeschreibungListDTO> loadMapped(int first, int pageSize, Map<String, Boolean> sortByAsc,
          Map<String, String> filterBy) {

        if (!filterBy.equals(filterByString) || !sortByAsc.equals(sortByBoolean)) {
          filterByString = filterBy;
          sortByBoolean = sortByAsc;
          int totalResults = beschreibungBoundary.countFilteredBeschreibungListDTOs(
              filterByString);
          this.setRowCount(totalResults);
        }

        return beschreibungBoundary.findFilteredBeschreibungListDTOs(first, pageSize,
            filterByString, sortByBoolean);
      }

      @Override
      public BeschreibungListDTO getRowData(String rowKey) {
        return beschreibungBoundary.findBeschreibungListDTOId(rowKey);
      }

      @Override
      public String getRowKey(BeschreibungListDTO object) {
        return Optional.ofNullable(object).map(BeschreibungListDTO::getHspId).orElse(null);
      }
    };

    log.info("setup finished");
  }

  public void reloadTableRecords() {
    log.info("reloadTableRecords");
    beschreibungenDataModel = null;
    setup();
  }

  public void onSelect() {
    log.info("Selected beschreibung= {}", selectedBeschreibungen.size());
  }

  public void onSelectAll() {
    log.info("Selected ALL beschreibung= {}", selectedBeschreibungen.size());
  }

  @CheckPermission(permission = PERMISSION_BESCHREIBUNG_ALLER_DRITTER_HART_LOESCHEN)
  @TransactionConfiguration(timeout = 7200)
  @Transactional
  public void deleteSelectedBeschreibungen() {
    log.info("Delete selected Beschreibungen..");
    try {
      Beschreibung[] beschreibungen = selectedBeschreibungen.stream()
          .map(beschreibungListDTO -> beschreibungBoundary.findById(beschreibungListDTO.getHspId()))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .toArray(Beschreibung[]::new);

      beschreibungBoundary.delete(bearbeiterBoundary.getLoggedBearbeiter(), beschreibungen);
      if (selectedBeschreibungen.size() == 1) {
        sendViewMessage(null,
            new FacesMessage("Erfolg",
                "Es wurde die Beschreibung mit dem ID=" + selectedBeschreibungen.get(0).getHspId()
                    + " erfolgreich Gelöscht."));
      } else {
        sendViewMessage(null,
            new FacesMessage("Erfolg",
                "Es wurden " + selectedBeschreibungen.size()
                    + " Beschreibungen erfolgreich gelöscht."));
      }
      selectedBeschreibungen = null;
      setup();
      clearAllFilters();
    } catch (SperreAlreadyExistException saee) {
      FacesMessage msg = createMessageForSperreAlreadyExistException(saee);

      log.info("Sperre already exists for selected beschreibungen.", saee);
      PrimeFaces.current().dialog().showMessageDynamic(msg);
    } catch (Exception e) {
      log.error("Fehler beim Löschen der Beschreibungen! {}", e.getMessage(), e);
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          "Fehler beim Löschen der Beschreibungen!",
          e.getMessage());
      sendViewMessage(null, msg);
    }
  }

  @CheckPermission(permission = PERMISSION_BESCHREIBUNG_ALLER_DRITTER_HART_LOESCHEN)
  public void reindexAllBeschreibungen() {
    log.info("Reindex beschreibungen..");
    try {
      beschreibungBoundary.reindexAllBeschreibungen(bearbeiterBoundary.getLoggedBearbeiter());

      sendViewMessage(null, new FacesMessage("Erfolg", "Index ist erfolgreich abgeschlossen!"));
    } catch (Exception e) {
      log.error("Index konnte nicht erstellt werden {}", e.getMessage(), e);
      sendViewMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Index konnte nicht erstellt werden!",
              e.getMessage()));
    }
  }


  FacesMessage createMessageForSperreAlreadyExistException(SperreAlreadyExistException saee) {
    String summery = selectedBeschreibungen.size() == 1 ?
        "Diese Beschreibung kann im Moment nicht gelöscht werden." :
        "Diese Beschreibungen können im Moment nicht gelöscht werden.";

    String detailMessage;
    if (saee.getSperren() != null && !saee.getSperren().isEmpty()) {
      StringBuilder messageBuilder = new StringBuilder();
      for (BeschreibungListDTO beschreibung : this.selectedBeschreibungen) {
        for (Sperre sperre : saee.getSperren()) {
          if (sperre.getSperreEintraege()
              .contains(new SperreEintrag(beschreibung.getHspId(), SperreDokumentTyp.BESCHREIBUNG))
              || sperre.getSperreEintraege()
              .contains(new SperreEintrag(beschreibung.getKodId(), SperreDokumentTyp.KOD))) {
            messageBuilder.append(createSperreDetailMessage(beschreibung.getHspId(), sperre,
                selectedBeschreibungen.size() > 1));
          }
        }
      }
      detailMessage = messageBuilder.toString();
    } else {
      detailMessage = "Mindestens eine Beschreibung oder ihr Kulturobjekt ist durch einen anderen Bearbeiter gesperrt.";
    }
    return new FacesMessage(FacesMessage.SEVERITY_ERROR, summery, detailMessage);
  }

  private String createSperreDetailMessage(String beschreibungId, Sperre sperre,
      boolean showBeschreibungId) {
    String sperreBearbeiterName = Optional.ofNullable(sperre)
        .map(Sperre::getBearbeiter)
        .map(Bearbeiter::getName)
        .orElse("[unbekannt]");

    String sperreDatum = Optional.ofNullable(sperre)
        .map(Sperre::getStartDatum)
        .map(sd -> sd.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
        .orElse("[unbekannt]");

    return showBeschreibungId ?
        "Die Beschreibung " + beschreibungId + " oder ihr Kulturobjekt ist gesperrt durch "
            + sperreBearbeiterName + " seit " + sperreDatum + ".\n" :
        "Sie oder ihr Kulturobjekt ist gesperrt durch " + sperreBearbeiterName + " seit "
            + sperreDatum + ".\n";
  }

  public List<BeschreibungListDTO> getSelectedBeschreibungen() {
    return selectedBeschreibungen;
  }

  public void setSelectedBeschreibungen(List<BeschreibungListDTO> selectedBeschreibungen) {
    this.selectedBeschreibungen = selectedBeschreibungen;
  }

  public int getSelectedBeschreibungenCount() {
    if (selectedBeschreibungen == null) {
      return 0;
    }
    return selectedBeschreibungen.size();
  }

  public boolean isSelectionDisabled() {
    return selectedBeschreibungen == null || selectedBeschreibungen.isEmpty()
        || !beschreibungsRechte.kannAllerDritterhartLoeschen();
  }

  public boolean isDeleteEnabled() {
    return deleteEnabled && beschreibungsRechte.kannAllerDritterhartLoeschen();
  }

  public boolean isReindexDisabled() {
    return !beschreibungsRechte.kannAllerDritterhartLoeschen();
  }

  public LazyDataModel<BeschreibungListDTO> getBeschreibungenDataModel() {
    return beschreibungenDataModel;
  }

  public void setBeschreibungenDataModel(
      LazyDataModel<BeschreibungListDTO> beschreibungenDataModel) {
    this.beschreibungenDataModel = beschreibungenDataModel;
  }
}
