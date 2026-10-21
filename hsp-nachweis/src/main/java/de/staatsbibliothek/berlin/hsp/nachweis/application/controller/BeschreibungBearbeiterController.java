package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;


import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsException;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;

@ViewScoped
@Named
@LoginCheck
@Slf4j
public class BeschreibungBearbeiterController implements Serializable {

  @Serial
  private static final long serialVersionUID = -3389228600145101726L;
  private static final String PARAM_BESCHREIBUNGID = "beschreibungID";
  private final transient BeschreibungsBoundary beschreibungsBoundary;
  private final transient BearbeiterBoundary bearbeiterBoundary;
  @Getter
  private Bearbeiter selectedBearbeiter;
  @Setter
  @Getter
  private String beschreibungID;


  @Inject
  public BeschreibungBearbeiterController(
      BeschreibungsBoundary beschreibungsBoundary,
      BearbeiterBoundary bearbeiterBoundary) {
    this.beschreibungsBoundary = beschreibungsBoundary;
    this.bearbeiterBoundary = bearbeiterBoundary;

  }

  @PostConstruct
  public void setup() {
    ExternalContext externalContext = null;
    if (Objects.nonNull(FacesContext.getCurrentInstance())) {
      externalContext = FacesContext.getCurrentInstance().getExternalContext();
    }
    if (Objects.nonNull(externalContext) && Objects
        .nonNull(externalContext.getRequestParameterMap().get(PARAM_BESCHREIBUNGID))
        && !externalContext.getRequestParameterMap().get(PARAM_BESCHREIBUNGID).isEmpty()) {

      beschreibungID = externalContext.getRequestParameterMap().get(PARAM_BESCHREIBUNGID);
    }

  }

  public void dialogSchliessen(String dialogId) {
    if (FacesContext.getCurrentInstance() != null) {
      PrimeFaces.current().dialog().closeDynamic(dialogId);
    }
  }

  public void setSelectedBearbeiter(Bearbeiter selectedBearbeiter) {
    this.selectedBearbeiter = selectedBearbeiter;
    log.info("setBearbeiter {}", selectedBearbeiter);
  }

  public void bearbeiterZuweisen() throws BeschreibungsException {
    Bearbeiter selectedBearbeiterName = getSelectedBearbeiter();
    Set<String> bearbeiter = new LinkedHashSet<>();
    bearbeiter.add(selectedBearbeiterName.getBearbeitername());
    this.beschreibungsBoundary.addBearbeiterToBeschreibung(this.beschreibungID, bearbeiter);

    dialogSchliessen("bearbeiterZuweisenForm");
    showMessageBearbeiterZugewiesen();

  }

  public void bearbeiterEntziehen() {

    Bearbeiter selectedBearbeiterName = getSelectedBearbeiter();
    try {
      this.beschreibungsBoundary.removeBearbeiterFromBeschreibung(this.beschreibungID,
          selectedBearbeiterName.getId());
      dialogSchliessen("bearbeiterEntziehenForm");
      showMessageBearbeiterEntzogen();

    } catch (Exception e) {
      FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Message",
          e.getMessage());
      PrimeFaces.current().dialog().showMessageDynamic(message);
      PrimeFaces.current().resetInputs("bearbeiterEntziehenForm");
    }
  }

  public List<Bearbeiter> autocompleteBearbeiter(String query) {
    return bearbeiterBoundary.findBearbeiterByFirstnameOrLastname(query);
  }

  public Set<Bearbeiter> findBearbeiter() throws BeschreibungsException {
    return beschreibungsBoundary.findBearbeiterEineBeschreibung(beschreibungID);

  }

  private void showMessageBearbeiterZugewiesen() {
    FacesContext context = FacesContext.getCurrentInstance();
    context.addMessage("globalmessages",
        new FacesMessage(FacesMessage.SEVERITY_INFO,
            "Info", I18NController.getMessage("beschreibung_bearbeiter_zuweisen_message")));
    context.getExternalContext().getFlash().setKeepMessages(true);

  }

  private void showMessageBearbeiterEntzogen() {
    FacesContext context = FacesContext.getCurrentInstance();
    context.addMessage("globalmessages",
        new FacesMessage(FacesMessage.SEVERITY_INFO,
            "Info", I18NController.getMessage("beschreibung_bearbeiter_entziehen_message")));
    context.getExternalContext().getFlash().setKeepMessages(true);
  }


}
