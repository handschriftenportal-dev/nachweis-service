package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KODRechte.PERMISSION_KOD_HART_LOESCHEN;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.DataTableStoreController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KODRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.KulturObjektDokumentListDTO;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
public class KulturObjekteModel extends DataTableStoreController implements
    Serializable {

  @Serial
  private static final long serialVersionUID = 3475288928199978004L;

  private static final String DATATABLE_WIDGET_VAR = "kodListTableWidget";
  private static final String ROOT_CLIENT_ID = "kodListform:kodListTable";

  private static final String ID_SELECTION = "selection";
  private static final String ID_GUELTIGE_SIGNATURE = "gueltigeSignature";
  private static final String ID_SORTKEY = "sortKey";
  private static final String ID_BESITZER = "besitzer";
  private static final String ID_AUFBEWAHRUNGSORT = "aufbewahrungsort";
  private static final String ID_MIT_BESCHREIBUNGEN = "mitBeschreibungen";
  private static final String ID_MIT_DIGITALISAT = "mitDigitalisat";
  private static final String ID_HSP_ID = "hspId";
  private static final String ID_GND_ID = "gndId";
  private static final String ID_REGISTRIERUNGSDATUM = "registrierungsdatum";
  private static final String ID_BESCHREIBUNGS_IDS = "beschreibungsIDs";
  private static final String PURL = "purl";
  private static final String KERNDATEN_STATUS = "kerndaten";

  private static final boolean VISIBLE_SELECTION = true;
  private static final boolean VISIBLE_GUELTIGE_SIGNATURE = true;
  private static final boolean VISIBLE_SORTKEY = false;
  private static final boolean VISIBLE_BESITZER = true;
  private static final boolean VISIBLE_AUFBEWAHRUNGSORT = true;
  private static final boolean VISIBLE_MIT_BESCHREIBUNGEN = true;
  private static final boolean VISIBLE_MIT_DIGITALISAT = true;
  private static final boolean VISIBLE_HSP_ID = false;
  private static final boolean VISIBLE_GND_ID = false;
  private static final boolean VISIBLE_REGISTRIERUNGSDATUM = true;
  private static final boolean VISIBLE_BESCHREIBUNGS_IDS = true;
  private static final boolean VISIBLE_PURL = false;
  private static final boolean VISIBLE_KERNDATEN_STATUS = false;

  private static final String WIDTH_SELECTION = "16";
  private static final String WIDTH_GUELTIGE_SIGNATURE = "200";
  private static final String WIDTH_SORTKEY = "150";
  private static final String WIDTH_BESITZER = "200";
  private static final String WIDTH_AUFBEWAHRUNGSORT = "150";
  private static final String WIDTH_MIT_BESCHREIBUNGEN = "100";
  private static final String WIDTH_MIT_DIGITALISAT = "100";
  private static final String WIDTH_HSP_ID = "200";
  private static final String WIDTH_GND_ID = "50";
  private static final String WIDTH_REGISTRIERUNGSDATUM = "100";
  private static final String WIDTH_BESCHREIBUNGS_IDS = "100";
  private static final String WIDTH_PURL = "100";
  private static final String WIDTH_KERNDATEN_STATUS = "100";

  private static final List<String> COLUMS_INDEX = new ArrayList<>(
      Arrays.asList(
          ID_SELECTION,
          ID_GUELTIGE_SIGNATURE,
          ID_SORTKEY,
          ID_BESITZER,
          ID_AUFBEWAHRUNGSORT,
          ID_MIT_BESCHREIBUNGEN,
          ID_MIT_DIGITALISAT,
          ID_HSP_ID, ID_GND_ID,
          ID_REGISTRIERUNGSDATUM, ID_BESCHREIBUNGS_IDS, PURL,
          KERNDATEN_STATUS));
  private static final List<Boolean> COLUMNS_VISIBLE = new ArrayList<>(
      Arrays.asList(
          VISIBLE_SELECTION,
          VISIBLE_GUELTIGE_SIGNATURE,
          VISIBLE_SORTKEY,
          VISIBLE_BESITZER,
          VISIBLE_AUFBEWAHRUNGSORT,
          VISIBLE_MIT_BESCHREIBUNGEN,
          VISIBLE_MIT_DIGITALISAT,
          VISIBLE_HSP_ID,
          VISIBLE_GND_ID,
          VISIBLE_REGISTRIERUNGSDATUM,
          VISIBLE_BESCHREIBUNGS_IDS,
          VISIBLE_PURL,
          VISIBLE_KERNDATEN_STATUS));
  private static final List<String> COLUMNS_WIDTH = new ArrayList<>(
      Arrays.asList(
          WIDTH_SELECTION,
          WIDTH_GUELTIGE_SIGNATURE,
          WIDTH_SORTKEY,
          WIDTH_BESITZER,
          WIDTH_AUFBEWAHRUNGSORT,
          WIDTH_MIT_BESCHREIBUNGEN,
          WIDTH_MIT_DIGITALISAT,
          WIDTH_HSP_ID,
          WIDTH_GND_ID,
          WIDTH_REGISTRIERUNGSDATUM,
          WIDTH_BESCHREIBUNGS_IDS,
          WIDTH_PURL,
          WIDTH_KERNDATEN_STATUS));

  private static final List<SortMeta> INITIAL_SORTING = Collections
      .singletonList(SortMeta.builder().field(ID_REGISTRIERUNGSDATUM)
          .order(SortOrder.ASCENDING)
          .build());

  @Setter
  @Getter
  private LazyDataModel<KulturObjektDokumentListDTO> kulturObjektDokumenteDataModel;

  @Setter
  @Getter
  private List<KulturObjektDokumentListDTO> selectedKulturObjektDokumente;

  private KulturObjektDokumentBoundary kulturObjektDokumentBoundary;

  private BearbeiterBoundary bearbeiterBoundary;

  private KODRechte kodRechte;

  @Setter
  @Getter
  private String filterMitBeschreibungen;

  @Setter
  @Getter
  private String filterMitDigitalisat;


  KulturObjekteModel() {
    super(DATATABLE_WIDGET_VAR, ROOT_CLIENT_ID, COLUMS_INDEX, INITIAL_SORTING,
        COLUMNS_WIDTH,
        COLUMNS_VISIBLE,
        INIT_HIT_PRO_PAGE, ALL_POSSIBLE_HIT_PRO_PAGE);
  }

  @Inject
  public KulturObjekteModel(
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary,
      KODRechte kodRechte, BearbeiterBoundary bearbeiterBoundary) {
    this();
    this.kodRechte = kodRechte;
    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
    this.bearbeiterBoundary = bearbeiterBoundary;
  }

  @PostConstruct
  protected void setup() {
    log.info("setup started");

    this.kulturObjektDokumenteDataModel = new KulturObjektDokumentLazyDataModel(
        kulturObjektDokumentBoundary);

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
      kulturObjektDokumentBoundary.delete(
          bearbeiterBoundary.getLoggedBearbeiter(),
          selectedKulturObjektDokumente.stream()
              .map(KulturObjektDokumentListDTO::getHspId)
              .toArray(String[]::new));

      String details =
          selectedKulturObjektDokumente.size() == 1
              ? getMessage("kodList_kods_loeschen_erfolg_eins",
              selectedKulturObjektDokumente.getFirst().getHspId())
              : getMessage("kodList_kods_loeschen_erfolg_viele",
                  selectedKulturObjektDokumente.size());

      sendViewMessage("globalmessages",
          new FacesMessage(getMessage("kodList_kods_loeschen_erfolg"),
              details));

      selectedKulturObjektDokumente = null;
      setup();
      resetDataTableToDefaults();

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

  public int getSelectedKulturObjektDokumenteCount() {
    if (selectedKulturObjektDokumente == null) {
      return 0;
    }
    return selectedKulturObjektDokumente.size();
  }

  public boolean isSelectionDisabled() {
    return selectedKulturObjektDokumente == null
        || selectedKulturObjektDokumente.isEmpty();
  }

  public boolean isReindexDisabled() {
    return !kodRechte.kannRegistrieren();
  }

  public boolean isExcelExportDisabled() {
    return !kodRechte.kannLesen();
  }

  public boolean isDeleteVisible() {
    return kodRechte.kannHartLoeschen();
  }

  public boolean isUmziehenVisible() {
    return kodRechte.kannKodUmziehen();
  }

  public boolean isSelectionVisible() {
    return kodRechte.kannKodUmziehen() || kodRechte.kannHartLoeschen();
  }

  FacesMessage createMessageForSperreAlreadyExistException(
      SperreAlreadyExistException saee) {
    String summery = getMessage(selectedKulturObjektDokumente.size() == 1 ?
        "kodList_kod_loeschen_sperre_fehler"
        : "kodList_kods_loeschen_sperre_fehler");

    List<String> detailMessages = new ArrayList<>();
    if (saee.getSperren() != null && !saee.getSperren().isEmpty()) {
      boolean idAnzeigen = selectedKulturObjektDokumente.size() > 1;
      for (KulturObjektDokumentListDTO kod : selectedKulturObjektDokumente) {
        for (Sperre sperre : saee.getSperren()) {
          if (sperre.getSperreEintraege()
              .contains(
                  new SperreEintrag(kod.getHspId(), SperreDokumentTyp.KOD))) {
            detailMessages.add(
                createSperreDetailMessage(kod.getHspId(), sperre, idAnzeigen));
          }
        }
      }
    } else {
      detailMessages.add(getMessage("kodList_kods_loeschen_sperren_unbekannt"));
    }
    return new FacesMessage(FacesMessage.SEVERITY_ERROR, summery,
        String.join(System.lineSeparator(), detailMessages));
  }

  private String createSperreDetailMessage(String kodId, Sperre sperre,
      boolean showBeschreibungId) {
    String sperreBearbeiterName = Optional.ofNullable(sperre)
        .map(Sperre::getBearbeiter)
        .map(Bearbeiter::getName)
        .orElse(getMessage("kodList_unbekannt"));

    String sperreDatum = Optional.ofNullable(sperre)
        .map(Sperre::getStartDatum)
        .map(I18NController::formatDate)
        .orElse(getMessage("kodList_unbekannt"));

    return showBeschreibungId ?
        getMessage("kodList_kods_loeschen_sperren_viele_kods", kodId,
            sperreBearbeiterName,
            sperreDatum) :
        getMessage("kodList_kods_loeschen_sperren_ein_kod",
            sperreBearbeiterName, sperreDatum);

  }
}
