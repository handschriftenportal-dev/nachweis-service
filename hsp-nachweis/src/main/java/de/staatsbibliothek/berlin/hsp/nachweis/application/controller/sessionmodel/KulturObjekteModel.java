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

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KODRechte.PERMISSION_KOD_HART_LOESCHEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KODRechte.PERMISSION_KOD_REGISTRIEREN;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.DataTableStoreController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.HspLazyDataModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KODRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.KulturObjektDokumentListDTO;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.Serializable;
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
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.primefaces.PrimeFaces;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 27.11.20
 */
@SessionScoped
@Named
@Slf4j
@LoginCheck
public class KulturObjekteModel extends DataTableStoreController implements Serializable {

  private static final long serialVersionUID = 3475288928199978004L;

  private static final String DATATABLE_WIDGET_VAR = "kodListTableWidget";
  private static final String ROOT_CLIENT_ID = "kodListform:kodListTable";

  private static final String ID_SELECTION = "selection";
  private static final String ID_GUELTIGE_SIGNATURE = "gueltigeSignature";
  private static final String ID_BESITZER = "besitzer";
  private static final String ID_AUFBEWAHRUNGSORT = "aufbewahrungsort";
  private static final String ID_HSP_ID = "hspId";
  private static final String ID_GND_ID = "gndId";
  private static final String ID_REGISTRIERUNGSDATUM = "registrierungsdatum";
  private static final String ID_BESCHREIBUNGS_IDS = "beschreibungsIDs";

  private static final List<String> COLUMS_INDEX = new ArrayList<>(
      Arrays.asList(ID_SELECTION, ID_GUELTIGE_SIGNATURE, ID_BESITZER, ID_AUFBEWAHRUNGSORT,
          ID_HSP_ID, ID_GND_ID,
          ID_REGISTRIERUNGSDATUM, ID_BESCHREIBUNGS_IDS));
  private static final List<Boolean> COLUMNS_VISABLE = new ArrayList<>(
      Arrays.asList(true, true, true, true, true, true, true, true));
  private static final List<String> COLUMNS_WIDTH = new ArrayList<>(
      Arrays.asList("16", "200", "200", "150", "200", "50", "100", "100"));

  private static final List<SortMeta> INITIAL_SORTING = Collections
      .singletonList(SortMeta.builder().field(ID_REGISTRIERUNGSDATUM).order(SortOrder.ASCENDING)
          .build());

  private LazyDataModel<KulturObjektDokumentListDTO> kulturObjektDokumenteDataModel;

  private List<KulturObjektDokumentListDTO> selectedKulturObjektDokumente;

  private KulturObjektDokumentBoundary kulturObjektDokumentBoundary;

  private BearbeiterBoundary bearbeiterBoundary;

  private KODRechte kodRechte;


  private boolean deleteEnabled;

  KulturObjekteModel() {
    super(DATATABLE_WIDGET_VAR, ROOT_CLIENT_ID, COLUMS_INDEX, INITIAL_SORTING, COLUMNS_WIDTH,
        COLUMNS_VISABLE,
        INIT_HIT_PRO_PAGE, ALL_POSSIBLE_HIT_PRO_PAGE);
    deleteEnabled = false;
  }

  @Inject
  public KulturObjekteModel(KulturObjektDokumentBoundary kulturObjektDokumentBoundary,
      KODRechte kodRechte, BearbeiterBoundary bearbeiterBoundary,
      @ConfigProperty(name = "workaround.KOD_LIST_DELETE_ENABLED") boolean deleteEnabled) {
    this();
    log.info("KOD delete enabled {}", deleteEnabled);
    this.deleteEnabled = deleteEnabled;
    this.kodRechte = kodRechte;
    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
    this.bearbeiterBoundary = bearbeiterBoundary;
  }

  @PostConstruct
  protected void setup() {
    log.info("setup started");

    this.kulturObjektDokumenteDataModel = new HspLazyDataModel<>() {
      private static final long serialVersionUID = 3080177565246695274L;

      private Map<String, Boolean> sortByBoolean;
      private Map<String, String> filterByString;

      @Override
      public List<KulturObjektDokumentListDTO> loadMapped(int first, int pageSize, Map<String, Boolean> sortByAsc,
          Map<String, String> filterBy) {

        if (!filterBy.equals(filterByString) || !sortByAsc.equals(sortByBoolean)) {
          filterByString = filterBy;
          sortByBoolean = sortByAsc;
          int totalResults = kulturObjektDokumentBoundary.countFilteredKulturObjektDokumentListDTOs(
              filterByString);
          this.setRowCount(totalResults);
        }

        return kulturObjektDokumentBoundary.findFilteredKulturObjektDokumentListDTOs(first, pageSize,
            filterByString, sortByBoolean);
      }

      @Override
      public KulturObjektDokumentListDTO getRowData(String rowKey) {
        return kulturObjektDokumentBoundary.findKulturObjektDokumentListDTOById(rowKey);
      }

      @Override
      public String getRowKey(KulturObjektDokumentListDTO object) {
        return Optional.ofNullable(object).map(KulturObjektDokumentListDTO::getHspId).orElse(null);
      }
    };

    log.info("setup finished");
  }

  public void reloadTableRecords() {
    kulturObjektDokumenteDataModel = null;
    setup();
  }

  public void onSelect() {
    log.info("Selected kod= {}", selectedKulturObjektDokumente.size());
  }

  public void onSelectAll() {
    log.info("Selected ALL kod= {}", selectedKulturObjektDokumente.size());
  }

  @CheckPermission(permission = PERMISSION_KOD_HART_LOESCHEN)
  public void deleteSelectedKods() {
    log.info("Delete selected KODs..");
    try {
      kulturObjektDokumentBoundary.delete(bearbeiterBoundary.getLoggedBearbeiter(),
          selectedKulturObjektDokumente.stream()
              .map(KulturObjektDokumentListDTO::getHspId)
              .toArray(String[]::new));

      String details = selectedKulturObjektDokumente.size() == 1 ?
          getMessage("kodList_kods_loeschen_erfolg_eins", selectedKulturObjektDokumente.get(
              0).getHspId()) :
          getMessage("kodList_kods_loeschen_erfolg_viele", selectedKulturObjektDokumente.size());

      sendViewMessage("globalmessages", new FacesMessage(getMessage("kodList_kods_loeschen_erfolg"), details));

      selectedKulturObjektDokumente = null;
      setup();
      clearAllFilters();

    } catch (SperreAlreadyExistException saee) {
      log.info("Sperren already exist for selected kods.", saee);
      FacesMessage msg = createMessageForSperreAlreadyExistException(saee);
      PrimeFaces.current().dialog().showMessageDynamic(msg);

    } catch (Exception e) {
      log.error("Error in deleteSelectedKods", e);
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          getMessage("kodList_kods_loeschen_fehler"),
          e.getMessage());
      sendViewMessage(null, msg);
    }
  }

  @CheckPermission(permission = PERMISSION_KOD_REGISTRIEREN)
  public void reindexAllKulturObjektDokumente() {
    log.info("Reindex kods..");
    try {
      kulturObjektDokumentBoundary.reindexAllKulturObjektDokumente(bearbeiterBoundary.getLoggedBearbeiter());
      sendViewMessage("globalmessages", new FacesMessage(
          getMessage("kodList_indexAktualisieren_erfolg"),
          getMessage("kodList_indexAktualisieren_erfolg_details")));
    } catch (Exception e) {
      log.error("Index konnte nicht erstellt werden {}", e.getMessage(), e);
      sendViewMessage("globalmessages", new FacesMessage(FacesMessage.SEVERITY_ERROR,
          getMessage("kodList_indexAktualisieren_fehler"),
          e.getMessage()));
    }
  }

  public List<KulturObjektDokumentListDTO> getSelectedKulturObjektDokumente() {
    return selectedKulturObjektDokumente;
  }

  public void setSelectedKulturObjektDokumente(
      List<KulturObjektDokumentListDTO> selectedKulturObjektDokumente) {
    this.selectedKulturObjektDokumente = selectedKulturObjektDokumente;
  }

  public int getSelectedKulturObjektDokumenteCount() {
    if (selectedKulturObjektDokumente == null) {
      return 0;
    }
    return selectedKulturObjektDokumente.size();
  }

  public boolean isSelectionDisabled() {
    return selectedKulturObjektDokumente == null || selectedKulturObjektDokumente.isEmpty();
  }

  public boolean isReindexDisabled() {
    return !kodRechte.kannRegistrieren();
  }

  public boolean isExcelExportDisabled() {
    return !kodRechte.kannLesen();
  }

  public boolean isDeleteEnabled() {
    return deleteEnabled && kodRechte.kannHartLoeschen();
  }

  FacesMessage createMessageForSperreAlreadyExistException(SperreAlreadyExistException saee) {
    String summery = getMessage(selectedKulturObjektDokumente.size() == 1 ?
        "kodList_kod_loeschen_sperre_fehler" : "kodList_kods_loeschen_sperre_fehler");

    List<String> detailMessages = new ArrayList<>();
    if (saee.getSperren() != null && !saee.getSperren().isEmpty()) {
      boolean idAnzeigen = selectedKulturObjektDokumente.size() > 1;
      for (KulturObjektDokumentListDTO kod : selectedKulturObjektDokumente) {
        for (Sperre sperre : saee.getSperren()) {
          if (sperre.getSperreEintraege()
              .contains(new SperreEintrag(kod.getHspId(), SperreDokumentTyp.KOD))) {
            detailMessages.add(createSperreDetailMessage(kod.getHspId(), sperre, idAnzeigen));
          }
        }
      }
    } else {
      detailMessages.add(getMessage("kodList_kods_loeschen_sperren_unbekannt"));
    }
    return new FacesMessage(FacesMessage.SEVERITY_ERROR, summery, String.join(System.lineSeparator(), detailMessages));
  }

  private String createSperreDetailMessage(String kodId, Sperre sperre, boolean showBeschreibungId) {
    String sperreBearbeiterName = Optional.ofNullable(sperre)
        .map(Sperre::getBearbeiter)
        .map(Bearbeiter::getName)
        .orElse(getMessage("kodList_unbekannt"));

    String sperreDatum = Optional.ofNullable(sperre)
        .map(Sperre::getStartDatum)
        .map(I18NController::formatDate)
        .orElse(getMessage("kodList_unbekannt"));

    return showBeschreibungId ?
        getMessage("kodList_kods_loeschen_sperren_viele_kods", kodId, sperreBearbeiterName, sperreDatum) :
        getMessage("kodList_kods_loeschen_sperren_ein_kod", sperreBearbeiterName, sperreDatum);

  }

  public LazyDataModel<KulturObjektDokumentListDTO> getKulturObjektDokumenteDataModel() {
    return kulturObjektDokumenteDataModel;
  }

  public void setKulturObjektDokumenteDataModel(
      LazyDataModel<KulturObjektDokumentListDTO> kulturObjektDokumenteDataModel) {
    this.kulturObjektDokumenteDataModel = kulturObjektDokumenteDataModel;
  }


}
