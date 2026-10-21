package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte.PERMISSION_BESCHREIBUNG_ALLER_DRITTER_HART_LOESCHEN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte.PERMISSION_BESCHREIBUNG_LOESCHEN;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.LizenzTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ApprovalEventViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ApprovalRequestViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.PublikationViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.util.Dialog;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.PublikationException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.PublikationsService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;

@ViewScoped
@LoginCheck
@Slf4j
@Named("beschreibungCardController")
public class BeschreibungCardController implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;
  private static final String BESCHREIBUNG_ID = "beschreibungID";

  private final PublikationsService service;
  private final BearbeiterBoundary bearbeiterBoundary;
  private final BeschreibungsBoundary beschreibungsBoundary;
  private final BeschreibungsRechte beschreibungsRechte;
  private final KulturObjektDokumentViewController kulturObjektDokumentViewController;
  @Getter
  private boolean teiValidationLoading = true;

  @Getter
  @Setter
  private String comment;

  @Getter
  private LizenzTyp selectedLizenzTyp;

  @Getter
  @Setter
  private String beschreibungID;

  @Getter
  private boolean teiXmlValid;

  @Getter
  private transient PublikationViewModel publikation;

  @Inject
  public BeschreibungCardController(PublikationsService service,
      BearbeiterBoundary bearbeiterBoundary,
      BeschreibungsBoundary beschreibungsBoundary,
      BeschreibungsRechte beschreibungsRechte,
      KulturObjektDokumentViewController kulturObjektDokumentViewController
  ) {
    this.service = service;
    this.bearbeiterBoundary = bearbeiterBoundary;
    this.beschreibungsBoundary = beschreibungsBoundary;
    this.beschreibungsRechte = beschreibungsRechte;
    this.kulturObjektDokumentViewController = kulturObjektDokumentViewController;
  }

  @PostConstruct
  public void init() {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    if (facesContext == null) {
      return;
    }
    beschreibungID = facesContext.getExternalContext().getRequestParameterMap()
        .get(BESCHREIBUNG_ID);
  }


  public void setSelectedLizenzTyp(LizenzTyp selectedLizenzTyp) {
    log.debug("setSelectedLizenzTyp called with: {}", selectedLizenzTyp);
    this.selectedLizenzTyp = selectedLizenzTyp;
  }

  public List<LizenzTyp> getLizenzTypen() {
    return Arrays.asList(LizenzTyp.values());
  }

  // --- Dynamic Dialog Methods ---

  public void openCreatePublikationDialog(String beschreibungId) {
    Dialog.forOutcome("/handschriftenportal/createPublikationDialog")
        .withSize("700", "500")
        .withParam(BESCHREIBUNG_ID, beschreibungId)
        .closable(true)
        .open();
  }

  public void openWithdrawDialog(String beschreibungId) {
    Dialog.forOutcome("/handschriftenportal/withdrawPublikationDialog")
        .withSize("700", "500")
        .withParam(BESCHREIBUNG_ID, beschreibungId)
        .closable(true)
        .open();
  }

  public void openHistoryDialog(String beschreibungId) {
    Dialog.forOutcome("/handschriftenportal/showPublikationHistoryDialog")
        .withSize("700", "600")
        .withParam(BESCHREIBUNG_ID, beschreibungId)
        .closable(true)
        .open();
  }

  public void dialogSchliessen(String result) {
    log.info("dialogSchliessen called with result={}", result);
    Dialog.close(result);
  }

  public void onPublicationRequested(SelectEvent<String> event) {
    reloadViewModel();
    addMessage(FacesMessage.SEVERITY_INFO, "publikation_success_approval_requested");
  }

  public void onPublicationWithdrawn(SelectEvent<String> event) {
    reloadViewModel();
    addMessage(FacesMessage.SEVERITY_INFO, "publikation_success_withdrawn");
  }

  // --- Publication Request Methods ---

  public void createPublicationRequest() {
    createPublicationRequest(this.beschreibungID);
  }

  public void createPublicationRequest(String beschreibungIdParam) {
    try {
      String idToUse = beschreibungIdParam != null ? beschreibungIdParam : this.beschreibungID;
      service.applyForPublication(idToUse, selectedLizenzTyp, getCurrentUser(), comment);
      dialogSchliessen(idToUse);
    } catch (PublikationException e) {
      log.error("Failed to request approval", e);
      addMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessageArgs());
    } catch (Exception e) {
      log.error("Failed to request approval", e);
      addMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
    } finally {
      resetFields();
    }
  }

  public void withdraw() {
    withdraw(this.beschreibungID);
  }

  public void withdraw(String beschreibungIdParam) {
    try {
      String idToUse = beschreibungIdParam != null ? beschreibungIdParam : this.beschreibungID;
      service.withdraw(idToUse, getCurrentUser(), comment);
      dialogSchliessen(idToUse);
    } catch (PublikationException e) {
      log.error("Failed to withdraw request", e);
      addMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessageArgs());
    } catch (Exception e) {
      log.error("Failed to withdraw request", e);
      addMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
    } finally {
      resetFields();
    }
  }

  public void loadPublikation() {
    if (beschreibungID == null) {
      return;
    }
    try {
      var beschreibung = service.reloadBeschreibung(beschreibungID);
      this.publikation = PublikationViewModel.fromEntity(beschreibung.getPublikation());
    } catch (PublikationException e) {
      log.error("Failed to load publication for beschreibungID={}", beschreibungID, e);
      addMessage(FacesMessage.SEVERITY_ERROR, "publikation_error_load");
    }
  }


  public ApprovalRequestViewModel getActiveApprovalRequest() {
    return publikation == null
        ? null
        : publikation.getActiveApprovalRequest().orElse(null);
  }

  public List<ApprovalEventViewModel> getApprovalRequestsHistory() {
    return publikation == null
        ? Collections.emptyList()
        : getApprovalRequestsHistory(publikation.approvalRequests());
  }

  public List<ApprovalEventViewModel> getApprovalRequestsHistory(
      List<ApprovalRequestViewModel> approvalRequests) {
    if (approvalRequests == null || approvalRequests.isEmpty()) {
      return Collections.emptyList();
    }
    return approvalRequests.stream()
        .flatMap(request -> request.events().stream())
        .sorted(Comparator.comparing(ApprovalEventViewModel::createdAt).reversed())
        .toList();
  }

  public void validateTEI(String locale) {
    log.info("Requesting TEI XML validation for beschreibungID={} with locale={}", beschreibungID,
        locale);
    if (beschreibungID == null) {
      return;
    }
    try {
      var beschreibung = service.reloadBeschreibung(beschreibungID);
      var result = beschreibungsBoundary.validateXML(beschreibung.getTeiXML(), "ODD", locale);
      teiValidationLoading = false;
      teiXmlValid = result.isValid();
      if (teiXmlValid) {
        addMessage(FacesMessage.SEVERITY_INFO, "validation_result_successfull");
      } else {
        addMessage(FacesMessage.SEVERITY_ERROR, "validation_result_failed");
      }
    } catch (PublikationException e) {
      teiValidationLoading = false;
      log.error("Failed to reload beschreibung for TEI validation", e);
      addMessage(FacesMessage.SEVERITY_ERROR, "beschreibung_not_found");
    }
  }

  public boolean validateTEI(BeschreibungsViewModel beschreibung, String locale) {
    var result = beschreibungsBoundary.validateXML(beschreibung.getTeiXML(), "ODD", locale);
    teiXmlValid = result.isValid();
    if (teiXmlValid) {
      addMessage(FacesMessage.SEVERITY_INFO, "tei_validation_successful");
    } else {
      addMessage(FacesMessage.SEVERITY_ERROR, "some_key_with_placeholder",
          result.getMessageWithDetails(Locale.of(locale)));
    }
    return teiXmlValid;
  }

  // --- Beschreibung card state check methods ---

  public boolean isEditable(BeschreibungsViewModel beschreibung) {
    return beschreibung.isEditable();
  }

  public boolean isEditableForCurrentUser(BeschreibungsViewModel beschreibung) {
    return isEditable(beschreibung)
        && beschreibungsBoundary.isBeschreibungActionableForLoggedInBearbeiter(
        beschreibung.getId());
  }

  public boolean hasNoAssignedBearbeiter(BeschreibungsViewModel beschreibung) {
    Set<String> bearbeiterIds = beschreibung.getBearbeiterNames();
    return bearbeiterIds == null || bearbeiterIds.isEmpty();
  }

  public boolean canRequestPublication(BeschreibungsViewModel beschreibung) {
    try {
      boolean editable = beschreibung.isEditable();
      boolean canRequest = beschreibung.canRequestApproval();
      boolean actionable = beschreibungsBoundary.isBeschreibungActionableForLoggedInBearbeiter(
          beschreibung.getId());
      boolean result = editable && canRequest && actionable;

      log.debug("canRequestPublication({})={}: editable={}, canRequestApproval={}, "
              + "actionable={}, pubStatus={}, hasActiveRequest={}",
          beschreibung.getId(), result, editable, canRequest, actionable,
          beschreibung.getPublikation() != null
              ? beschreibung.getPublikation().status()
              : "null",
          beschreibung.getPublikation() != null
              ? beschreibung.getPublikation().hasActiveApprovalRequest()
              : "null"
      );
      return result;
    } catch (Exception e) {
      log.error("Error checking canRequestPublication for Beschreibung {}", beschreibung.getId(),
          e);
      return false;
    }
  }

  public boolean canWithdrawPublication(BeschreibungsViewModel beschreibung) {
    return beschreibung.getPublikation() != null
        && beschreibung.getPublikation().hasActiveApprovalRequest()
        && beschreibungsBoundary.isBeschreibungActionableForLoggedInBearbeiter(beschreibung.getId())
        && !beschreibung.isPublished();
  }

  public boolean canViewPublicationHistory(BeschreibungsViewModel beschreibung) {
    return beschreibung.getPublikation() != null
        && !beschreibung.getPublikation().approvalRequests().isEmpty()
        && beschreibungsBoundary.isBeschreibungAssignedToLoggedInBearbeiter(
        beschreibung.getId());
  }

  public boolean isDeletableForCurrentUser(BeschreibungsViewModel beschreibung) {
    return beschreibung.isEditable()
        && beschreibungsBoundary.isBeschreibungDeletableForLoggedInBearbeiter(beschreibung.getId());
  }

  public boolean isDeleteVisible() {
    return beschreibungsRechte.kannAllerDritterhartLoeschen() || beschreibungsRechte.kannLoeschen();
  }

  public boolean isEntziehenDialogVisible() {
    return Objects.equals(bearbeiterBoundary.getLoggedBearbeiter().getRolle(), "Redakteur");
  }

  public void openBearbeiterZuweisenDialog(String beschreibungId) {
    Dialog.forOutcome("bearbeiterZuweisen")
        .withSize("500", "430")
        .withParam(BESCHREIBUNG_ID, beschreibungId)
        .open();
  }

  public void openBearbeiterEntziehenDialog(String beschreibungId) {
    Dialog.forOutcome("bearbeiterEntziehen")
        .withSize("500", "430")
        .withParam(BESCHREIBUNG_ID, beschreibungId)
        .open();
  }

  @CheckPermission(
      permission = {
          PERMISSION_BESCHREIBUNG_ALLER_DRITTER_HART_LOESCHEN,
          PERMISSION_BESCHREIBUNG_LOESCHEN
      })
  @TransactionConfiguration(timeout = 7200)
  @Transactional
  public void beschreibungLoeschen(String beschreibungsID) {
    Optional<Beschreibung> beschreibung = beschreibungsBoundary.findById(beschreibungsID);
    if (beschreibung.isEmpty()) {
      addMessage(FacesMessage.SEVERITY_ERROR, "kod_detail_beschreibungloeschen_fehler");
      return;
    }

    try {
      log.info("Beschreibung loeschen {} ", beschreibungsID);
      beschreibungsBoundary.delete(bearbeiterBoundary.getLoggedBearbeiter(), beschreibung.get());
      kulturObjektDokumentViewController.reloadViewModel();
    } catch (SperreAlreadyExistException saee) {
      log.error("Sperre already exists for Beschreibung {}", beschreibungsID, saee);
      handleSperreAlreadyExistException(saee);
    } catch (DokumentSperreException dse) {
      log.error("Sperre konnte nicht gesetzt werden für Beschreibung {}", beschreibungsID, dse);
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          getMessage("kod_detail_beschreibungloeschen_sperre_fehler"),
          dse.getMessage());
      PrimeFaces.current().dialog().showMessageDynamic(msg);
    } catch (BeschreibungsException be) {
      log.error("Fehler beim Löschen der Beschreibung {}", beschreibungsID, be);
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          getMessage("kod_detail_beschreibungloeschen_fehler"),
          be.getMessage());
      PrimeFaces.current().dialog().showMessageDynamic(msg);
    } catch (Exception error) {
      log.error("Unerwarteter Fehler beim Löschen der Beschreibung {}", beschreibungsID, error);
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          getMessage("kod_detail_beschreibungloeschen_fehler"),
          error.getMessage());
      PrimeFaces.current().dialog().showMessageDynamic(msg);
    }
  }

  public void reloadViewModel() {
    log.info("Update kodVieModel from beschreibungCardController");
    kulturObjektDokumentViewController.reloadViewModel();
  }

  public void resetDialogFields() {
    comment = null;
    selectedLizenzTyp = null;
  }

  // --- Private helper methods ---

  private void handleSperreAlreadyExistException(SperreAlreadyExistException saee) {
    Sperre sperre = Stream.ofNullable(saee.getSperren())
        .flatMap(List::stream).findFirst().orElse(null);
    String bearbeiterName = Optional.ofNullable(sperre)
        .map(Sperre::getBearbeiter).map(Bearbeiter::getName)
        .orElse(getMessage("kod_detail_unbekannt"));
    String sperreDatum = Optional.ofNullable(sperre)
        .map(Sperre::getStartDatum).map(I18NController::formatDate)
        .orElse(getMessage("kod_detail_unbekannt"));
    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
        getMessage("kod_detail_beschreibungloeschen_sperre_fehler"),
        getMessage("kod_detail_beschreibungloeschen_sperre_fehler_detail",
            bearbeiterName, sperreDatum));
    PrimeFaces.current().dialog().showMessageDynamic(msg);
  }

  private void resetFields() {
    resetDialogFields();
  }

  private String getCurrentUser() {
    var auth = bearbeiterBoundary.getAuthentication();
    if (auth == null) {
      return "Gast";
    }
    return auth.getFirstname() + " " + auth.getLastname();
  }

  private void addMessage(FacesMessage.Severity severity, String messageKey, Object... args) {
    FacesContext.getCurrentInstance().addMessage(null,
        new FacesMessage(severity, getMessage(messageKey, args), null));
  }
}
