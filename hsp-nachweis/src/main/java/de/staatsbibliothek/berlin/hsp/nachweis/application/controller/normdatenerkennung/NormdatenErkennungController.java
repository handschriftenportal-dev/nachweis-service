package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.normdatenerkennung;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung.NormdatenErkennungAuftrag;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung.NormdatenErkennungAuftragService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung.NormdatenErkennungAuftragStatus;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungListDbView;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Named("normdatenErkennungController")
@ViewScoped
@Slf4j
public class NormdatenErkennungController implements Serializable {

  private final NormdatenErkennungAuftragService auftragService;
  private final BearbeiterBoundary bearbeiterService;
  private final BeschreibungsRepository beschreibungsRepository;

  @Getter
  @Setter
  private List<NormdatenErkennungAuftragStatus> selectedStatuses = new ArrayList<>(Arrays.asList(
      NormdatenErkennungAuftragStatus.PENDING,
      NormdatenErkennungAuftragStatus.PROCESSING,
      NormdatenErkennungAuftragStatus.AWAITING_REVIEW,
      NormdatenErkennungAuftragStatus.APPLYING,
      NormdatenErkennungAuftragStatus.ERROR
  ));

  private final Map<String, String> signatureCache = new HashMap<>();

  @Inject
  public NormdatenErkennungController(
      NormdatenErkennungAuftragService auftragService,
      BearbeiterBoundary bearbeiterService,
      BeschreibungsRepository beschreibungsRepository
  ) {
    this.auftragService = auftragService;
    this.bearbeiterService = bearbeiterService;
    this.beschreibungsRepository = beschreibungsRepository;
  }

  public void enqueueAuftraege(List<BeschreibungListDbView> beschreibungen) {
    if (beschreibungen == null || beschreibungen.isEmpty()) {
      return;
    }
    var ids = beschreibungen.stream()
        .map(BeschreibungListDbView::getHspId)
        .filter(Objects::nonNull)
        .toList();
    auftragService.enqueueBeschreibungAuftraege(ids, bearbeiterService.getLoggedBearbeiter());
    log.info("Enqueued {} auftraege", ids.size());

    var facesContext = FacesContext.getCurrentInstance();
    if (facesContext != null) {
      facesContext.getExternalContext().getFlash().setKeepMessages(true);
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
          I18NController.getMessage("normdatenerkennung_startmessage_title"),
          I18NController.getMessage("normdatenerkennung_startmessage", ids.size()));
      facesContext.addMessage("globalmessages", msg);
    }
  }

  public List<NormdatenErkennungAuftrag> getAuftraege() {
    return auftragService.findAllAuftraege().stream()
        .filter(a -> selectedStatuses.isEmpty() || selectedStatuses.contains(a.getStatus()))
        .sorted(Comparator.comparing(
            a -> a.getUpdatedAt() != null ? a.getUpdatedAt() : a.getCreatedAt(),
            Comparator.nullsLast(Comparator.reverseOrder())
        ))
        .toList();
  }

  public SelectItem[] getStatusItems() {
    return Arrays.stream(NormdatenErkennungAuftragStatus.values())
        .map(s -> new SelectItem(s, I18NController.getMessage("normdatenerkennung_status_" + s)))
        .toArray(SelectItem[]::new);
  }

  public String getStatusSeverity(NormdatenErkennungAuftragStatus status) {
    return switch (status) {
      case PENDING, PROCESSING, APPLYING -> "warn";
      case AWAITING_REVIEW -> "success";
      case APPLIED, CANCELLED -> "";
      case ERROR -> "danger";
    };
  }

  public String getGueltigeSignatur(String beschreibungId) {
    return signatureCache.computeIfAbsent(beschreibungId,
        id -> {
          var dto = beschreibungsRepository.findBeschreibungListDTOById(id);
          return dto != null && dto.getGueltigeSignatur() != null
              ? dto.getGueltigeSignatur()
              : id;
        });
  }

  public boolean noActiveAuftraege() {
    return auftragService.findAllAuftraege().stream()
        .noneMatch(NormdatenErkennungAuftrag::isInProgress);
  }
}
