package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte.PERMISSION_BESCHREIBUNG_ALLER_DRITTER_HART_LOESCHEN;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationStatus;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.DataTableStoreController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.HspLazyDataModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungListDbView;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import java.io.Serial;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
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

  @Serial
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
  private static final String ID_BEARBEITER_ID = "bearbeiterId";
  private static final String ID_PUBLIKATION_STATUS = "publikationStatus";
  private static final String ID_PUBLIKATIONS_DATUM_HSP = "publikationsDatumHsp";
  private static final String ID_HSP_PURL = "hspPurl";

  private static final List<String> COLUMS_INDEX =
      new ArrayList<>(
          Arrays.asList(
              ID_SELECTION,
              ID_GUELTIGE_SIGNATUR,
              ID_BESITZER,
              ID_AUFBEWAHRUNGSORT,
              ID_PUBLIKATION_STATUS,
              ID_PUBLIKATIONS_DATUM_HSP,
              ID_AUTOR,
              ID_HSP_ID,
              ID_KOD_ID,
              ID_HSP_PURL,
              ID_AENDERUNGS_DATUM,
              ID_ERSTELLUNGS_DATUM,
              ID_VERWALTUNGS_TYP,
              ID_DOKUMENT_OBJECT_TYP,
              ID_KATALOG_ID,
              ID_BEARBEITER_ID));
  private static final List<Boolean> COLUMNS_VISIBLE =
      new ArrayList<>(
          Arrays.asList(
              true, true, true, true, true, false, true, false, true, true, true, false, false,
              false, false, true));
  private static final List<String> COLUMNS_WIDTH =
      new ArrayList<>(
          Arrays.asList(
              "20", "100", "100", "100", "50", "100", "100", "100", "75", "100", "75", "50", "50",
              "30", "75", "50", "50"));

  private static final List<SortMeta> INITIAL_SORTING =
      Collections.singletonList(
          SortMeta.builder().field(ID_ERSTELLUNGS_DATUM).order(SortOrder.ASCENDING).build());

  @Getter
  @Setter
  private HspLazyDataModel<BeschreibungListDbView> beschreibungenDataModel;

  @Setter
  @Getter
  private List<BeschreibungListDbView> selectedBeschreibungen;

  @Setter
  @Getter
  private List<BeschreibungListDbView> filteredBeschreibungen;

  private BeschreibungsBoundary beschreibungBoundary;

  private BearbeiterBoundary bearbeiterBoundary;

  private BeschreibungsRechte beschreibungsRechte;

  @Getter
  @Setter
  private String bearbeiterFilterValue;

  @Getter
  @Setter
  private String selectedPublikationStatus;

  BeschreibungModel() {
    super(
        DATATABLE_WIDGET_VAR,
        ROOT_CLIENT_ID,
        COLUMS_INDEX,
        INITIAL_SORTING,
        COLUMNS_WIDTH,
        COLUMNS_VISIBLE,
        INIT_HIT_PRO_PAGE,
        ALL_POSSIBLE_HIT_PRO_PAGE);
  }

  @Inject
  public BeschreibungModel(
      BeschreibungsBoundary beschreibungBoundary,
      BeschreibungsRechte beschreibungsRechte,
      BearbeiterBoundary bearbeiterBoundary) {
    this();
    this.beschreibungBoundary = beschreibungBoundary;
    this.beschreibungsRechte = beschreibungsRechte;
    this.bearbeiterBoundary = bearbeiterBoundary;
    this.beschreibungenDataModel = new BeschreibungLazyDataModel(beschreibungBoundary);
  }

  @PostConstruct
  protected void setup() {
    log.info("setup started");
    this.beschreibungenDataModel = new BeschreibungLazyDataModel(beschreibungBoundary);
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

  public SelectItem[] getPublikationStatusItems() {
    return Arrays.stream(PublikationStatus.values())
        .map(s -> new SelectItem(s.name(),
            I18NController.getMessage("publikationStatus_" + s.name())))
        .toArray(SelectItem[]::new);
  }

  @CheckPermission(permission = PERMISSION_BESCHREIBUNG_ALLER_DRITTER_HART_LOESCHEN)
  @TransactionConfiguration(timeout = 7200)
  @Transactional
  public void deleteSelectedBeschreibungen() {
    log.info("Delete selected Beschreibungen..");
    try {
      Beschreibung[] beschreibungen =
          selectedBeschreibungen.stream()
              .map(
                  beschreibungListDTO ->
                      beschreibungBoundary.findById(beschreibungListDTO.getHspId()))
              .filter(Optional::isPresent)
              .map(Optional::get)
              .toArray(Beschreibung[]::new);

      beschreibungBoundary.delete(bearbeiterBoundary.getLoggedBearbeiter(), beschreibungen);
      if (selectedBeschreibungen.size() == 1) {
        sendViewMessage(
            null,
            new FacesMessage(
                "Erfolg",
                "Es wurde die Beschreibung mit dem ID="
                    + selectedBeschreibungen.getFirst().getHspId()
                    + " erfolgreich Gelöscht."));
      } else {
        sendViewMessage(
            null,
            new FacesMessage(
                "Erfolg",
                "Es wurden "
                    + selectedBeschreibungen.size()
                    + " Beschreibungen erfolgreich gelöscht."));
      }
      selectedBeschreibungen = null;
      setup();
      refreshDataTable(true);
    } catch (SperreAlreadyExistException saee) {
      FacesMessage msg = createMessageForSperreAlreadyExistException(saee);

      log.info("Sperre already exists for selected beschreibungen.", saee);
      PrimeFaces.current().dialog().showMessageDynamic(msg);
    } catch (Exception e) {
      log.error("Fehler beim Löschen der Beschreibungen! {}", e.getMessage(), e);
      FacesMessage msg =
          new FacesMessage(
              FacesMessage.SEVERITY_ERROR,
              "Fehler beim Löschen der Beschreibungen!",
              e.getMessage());
      sendViewMessage(null, msg);
    }
  }

  FacesMessage createMessageForSperreAlreadyExistException(SperreAlreadyExistException saee) {
    String summery =
        selectedBeschreibungen.size() == 1
            ? "Diese Beschreibung kann im Moment nicht gelöscht werden."
            : "Diese Beschreibungen können im Moment nicht gelöscht werden.";

    String detailMessage;
    if (saee.getSperren() != null && !saee.getSperren().isEmpty()) {
      StringBuilder messageBuilder = new StringBuilder();
      for (BeschreibungListDbView beschreibung : this.selectedBeschreibungen) {
        for (Sperre sperre : saee.getSperren()) {
          if (sperre
              .getSperreEintraege()
              .contains(
                  new SperreEintrag(beschreibung.getHspId(), SperreDokumentTyp.BESCHREIBUNG))
              || sperre
              .getSperreEintraege()
              .contains(new SperreEintrag(beschreibung.getKodId(), SperreDokumentTyp.KOD))) {
            messageBuilder.append(
                createSperreDetailMessage(
                    beschreibung.getHspId(), sperre, selectedBeschreibungen.size() > 1));
          }
        }
      }
      detailMessage = messageBuilder.toString();
    } else {
      detailMessage =
          "Mindestens eine Beschreibung oder ihr Kulturobjekt ist durch einen anderen Bearbeiter gesperrt.";
    }
    return new FacesMessage(FacesMessage.SEVERITY_ERROR, summery, detailMessage);
  }

  private String createSperreDetailMessage(
      String beschreibungId, Sperre sperre, boolean showBeschreibungId) {
    String sperreBearbeiterName =
        Optional.ofNullable(sperre)
            .map(Sperre::getBearbeiter)
            .map(Bearbeiter::getName)
            .orElse("[unbekannt]");

    String sperreDatum =
        Optional.ofNullable(sperre)
            .map(Sperre::getStartDatum)
            .map(sd -> sd.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
            .orElse("[unbekannt]");

    return showBeschreibungId
        ? "Die Beschreibung "
          + beschreibungId
          + " oder ihr Kulturobjekt ist gesperrt durch "
          + sperreBearbeiterName
          + " seit "
          + sperreDatum
          + ".\n"
        : "Sie oder ihr Kulturobjekt ist gesperrt durch "
          + sperreBearbeiterName
          + " seit "
          + sperreDatum
          + ".\n";
  }

  public int getSelectedBeschreibungenCount() {
    if (selectedBeschreibungen == null) {
      return 0;
    }
    return selectedBeschreibungen.size();
  }

  public boolean isSelectionDisabled() {
    return selectedBeschreibungen == null
        || selectedBeschreibungen.isEmpty()
        || !beschreibungsRechte.kannAllerDritterhartLoeschen();
  }

  public boolean isDeleteVisible() {
    return beschreibungsRechte.kannAllerDritterhartLoeschen();
  }

  public String getDokumentObjektTyp(BeschreibungListDbView beschreibungListDbView) {
    String dokumentObjektTyp = beschreibungListDbView.getDokumentObjektTyp();
    return I18NController.getMessage(dokumentObjektTyp);
  }

  public String filterByBearbeiter(String bearbeiterName) {
    try {
      PrimeFaces.current().multiViewState().clearAll();
    } catch (Exception exception) {
      log.warn("MultiViewState.clearAll() failed in filterByBearbeiter: {}", exception.getMessage(),
          exception);
    }
    this.bearbeiterFilterValue = null;
    Map<String, String> filterBy = new java.util.HashMap<>();
    if (bearbeiterName != null && !bearbeiterName.isBlank()) {
      this.bearbeiterFilterValue = bearbeiterName;
      filterBy.put("bearbeiter", bearbeiterName);
    }
    filteredBeschreibungen =
        beschreibungenDataModel.loadMapped(0, INIT_HIT_PRO_PAGE, Collections.emptyMap(), filterBy);
    return "beschreibung/beschreibungen.xhtml?faces-redirect=true";
  }

  public void resetBearbeiterFilter() {
    log.info("resetBearbeiterFilter – clear bearbeiterFilterValue and MultiViewState");
    this.bearbeiterFilterValue = null;
    this.filteredBeschreibungen = null;
    try {
      PrimeFaces.current().multiViewState().clearAll();
    } catch (Exception exception) {
      log.warn("Could not clear MultiViewState in resetBearbeiterFilter", exception);
    }
  }

  public void clearSelectedBeschreibungen() {
    log.info("clearSelectedBeschreibungen");
    selectedBeschreibungen = null;
    refreshDataTable(false);
  }
}
