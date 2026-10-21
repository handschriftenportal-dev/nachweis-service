package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.normdatenerkennung;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.normdatenerkennung.NormdatenErkennungAuftragErgebnisViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung.NormdatenErkennungAuftrag;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung.NormdatenErkennungAuftragService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung.NormdatenErkennungEintrag;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.LobidEntityFact;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.LobidOrgPort;
import jakarta.ws.rs.WebApplicationException;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Named("normdatenErkennungAuftragController")
@ViewScoped
@LoginCheck
@Slf4j
public class NormdatenErkennungAuftragController implements Serializable {

  private final NormdatenErkennungAuftragService auftragService;
  private final BearbeiterBoundary bearbeiterService;
  private final BeschreibungsRepository beschreibungsRepository;
  private final LobidOrgPort lobidPort;

  @Getter @Setter private String auftragId;
  @Getter private NormdatenErkennungAuftrag auftrag;
  @Getter private List<NormdatenErkennungAuftragErgebnisViewModel> ergebnisse = List.of();
  @Getter private List<NormdatenErkennungEintrag> errorErgebnisse = List.of();
  @Getter private String gueltigeSignatur;

  @Inject
  public NormdatenErkennungAuftragController(
      NormdatenErkennungAuftragService auftragService,
      BearbeiterBoundary bearbeiterService,
      BeschreibungsRepository beschreibungsRepository,
      @RestClient LobidOrgPort lobidPort) {
    this.auftragService = auftragService;
    this.bearbeiterService = bearbeiterService;
    this.beschreibungsRepository = beschreibungsRepository;
    this.lobidPort = lobidPort;
  }

  public void load() {
    if (auftragId == null || auftragId.isBlank()) {
      log.warn("load() called without auftragId");
      return;
    }
    var found = auftragService.findByIdWithErgebnisse(auftragId);
    if (found.isEmpty()) {
      log.warn("No auftrag found for id {}", auftragId);
      return;
    }
    auftrag = found.get();
    ergebnisse = auftrag.getErgebnisse().stream()
        .filter(r -> r.getErrorMessage() == null)
        .map(NormdatenErkennungAuftragErgebnisViewModel::new)
        .toList();
    errorErgebnisse = auftrag.getErgebnisse().stream()
        .filter(r -> r.getErrorMessage() != null)
        .toList();
    var dto = beschreibungsRepository.findBeschreibungListDTOById(auftrag.getBeschreibungsdokumentId());
    this.gueltigeSignatur = (dto != null && dto.getGueltigeSignatur() != null)
        ? dto.getGueltigeSignatur()
        : auftrag.getBeschreibungsdokumentId();
  }

  public int getTotalCount() {
    return ergebnisse.size();
  }

  public int getReviewedCount() {
    return (int) ergebnisse.stream()
        .filter(NormdatenErkennungAuftragErgebnisViewModel::isReviewed)
        .count();
  }

  public boolean canApply() {
    return auftrag != null && auftrag.isAwaitingReview() && !ergebnisse.isEmpty();
  }

  public String onApply() {
    auftragService.startApplying(auftragId, bearbeiterService.getLoggedBearbeiter());
    return "normdatenerkennung.xhtml?faces-redirect=true";
  }

  public String onCancel() {
    auftragService.cancelAuftrag(auftragId);
    return "normdatenerkennung.xhtml?faces-redirect=true";
  }

  public void onAuswahlChange(String id) {
    var vm = findErgebnis(id);
    if (vm == null) {
      return;
    }
    vm.setReviewed(true);
    vm.setManualGndId(null);
    vm.setResolvedFact(null);
    vm.clearLookupError();
    if (vm.isManualMode()) {
      auftragService.setSelectionNone(vm.getId(), bearbeiterService.getLoggedBearbeiter());
    } else {
      persistAuswahl(vm);
    }
  }

  public void loadManualGndIds() {
    ergebnisse.stream()
        .filter(vm -> vm.isManualMode()
            && vm.getManualGndId() != null
            && !vm.getManualGndId().isBlank())
        .forEach(vm -> {
          try {
            var lobidFact = lobidPort.findByGNDID(vm.getManualGndId());
            if (lobidFact != null) {
              var resolvedType = lobidFact.resolveTypeName();
              vm.setResolvedFact(lobidFact.toGNDEntityFact(resolvedType));
              applyTypeMismatchIfAny(vm, lobidFact, resolvedType);
            }
          } catch (Exception e) {
            log.warn("Background GND load failed for {}: {}", vm.getManualGndId(), e.getMessage());
          }
        });
  }

  public void lookupManualGndId(String id) {
    var vm = findErgebnis(id);
    if (vm == null || vm.getManualGndId() == null || vm.getManualGndId().isBlank()) {
      return;
    }
    var bearbeiter = bearbeiterService.getLoggedBearbeiter();
    vm.setResolvedFact(null);
    vm.clearLookupError();
    var lobidFact = fetchLobidFact(vm, bearbeiter);
    if (lobidFact == null) {
      return;
    }
    var resolvedType = lobidFact.resolveTypeName();
    log.info("GND lookup {}: types={}, resolved={}, expected={}",
        vm.getManualGndId(), lobidFact.getTypes(), resolvedType, vm.getNormdatenTyp().name());
    vm.setResolvedFact(lobidFact.toGNDEntityFact(resolvedType));
    if (applyTypeMismatchIfAny(vm, lobidFact, resolvedType)) {
      auftragService.setSelectionNone(vm.getId(), bearbeiter);
    } else {
      vm.setReviewed(true);
      auftragService.setSelectionManualIdentifier(vm.getId(), vm.getManualGndId(), bearbeiter);
    }
  }

  private LobidEntityFact fetchLobidFact(
      NormdatenErkennungAuftragErgebnisViewModel vm, Bearbeiter bearbeiter) {
    LobidEntityFact lobidFact;
    try {
      lobidFact = lobidPort.findByGNDID(vm.getManualGndId());
    } catch (WebApplicationException e) {
      if (e.getResponse().getStatus() == 404) {
        vm.setResolveError(vm.getManualGndId());
      } else {
        log.warn("Lobid lookup failed for {}: {}", vm.getManualGndId(), e.getMessage());
        vm.setTechnicalError(e.getMessage());
      }
      auftragService.setSelectionNone(vm.getId(), bearbeiter);
      return null;
    } catch (Exception e) {
      log.warn("Lobid lookup failed for {}: {}", vm.getManualGndId(), e.getMessage());
      vm.setTechnicalError(e.getMessage());
      auftragService.setSelectionNone(vm.getId(), bearbeiter);
      return null;
    }
    if (lobidFact == null) {
      vm.setResolveError(vm.getManualGndId());
      auftragService.setSelectionNone(vm.getId(), bearbeiter);
    }
    return lobidFact;
  }

  public String getStatusSeverity() {
    if (auftrag == null) {
      return "";
    }
    return switch (auftrag.getStatus()) {
      case PENDING, PROCESSING, APPLYING -> "warn";
      case AWAITING_REVIEW -> "success";
      case APPLIED, CANCELLED -> "";
      case ERROR -> "danger";
    };
  }

  private boolean applyTypeMismatchIfAny(NormdatenErkennungAuftragErgebnisViewModel vm,
      LobidEntityFact lobidFact, String resolvedType) {
    var mismatchLabel = resolveMismatchLabel(lobidFact, resolvedType);
    if (mismatchLabel != null && !vm.getNormdatenTyp().name().equals(resolvedType)) {
      vm.setTypeMismatchError(mismatchLabel);
      return true;
    }
    return false;
  }

  private String resolveMismatchLabel(LobidEntityFact lobidFact, String resolvedType) {
    if (resolvedType != null) {
      return resolvedType;
    }
    var types = lobidFact.getTypes();
    if (types == null || types.isEmpty()) {
      return null;
    }
    return types.stream()
        .filter(t -> !t.equals("AuthorityResource"))
        .collect(Collectors.joining(", "));
  }

  private NormdatenErkennungAuftragErgebnisViewModel findErgebnis(String id) {
    return ergebnisse.stream()
        .filter(vm -> vm.getId().equals(id))
        .findFirst()
        .orElse(null);
  }

  private void persistAuswahl(NormdatenErkennungAuftragErgebnisViewModel vm) {
    var bearbeiter = bearbeiterService.getLoggedBearbeiter();
    var treffer = vm.getSelectedTreffer();
    if (treffer == null) {
      auftragService.setSelectionNone(vm.getId(), bearbeiter);
    } else if (treffer.isEmpfehlung()) {
      auftragService.setSelectionRecommendation(vm.getId(), bearbeiter);
    } else {
      auftragService.setSelectionMatchIndex(vm.getId(), treffer.originalTrefferIndex(), bearbeiter);
    }
  }

  public String translateErklaerungsTyp(String typ) {
    if (typ == null) {
      return null;
    }
    try {
      var locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
      var bundle = ResourceBundle.getBundle(
          "de.staatsbibliothek.berlin.hsp.application.messages", locale);
      return bundle.getString("normdatenerkennung_erklaerungstyp_" + typ);
    } catch (MissingResourceException e) {
      return typ;
    }
  }
}
