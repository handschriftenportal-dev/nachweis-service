package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte.PERMISSION_BESCHREIBUNG_PUBLIZIEREN_VERWALTEN;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.PublikationException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.PublikationsService;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@ViewScoped
@LoginCheck
@Slf4j
@Named("approvalRequestController")
public class ApprovalRequestController implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient PublikationsService service;
  private final transient BearbeiterBoundary bearbeiterBoundary;

  @Getter
  private transient List<Beschreibung> beschreibungen;

  @Getter
  @Setter
  private transient Beschreibung selectedBeschreibung;

  @Getter
  @Setter
  private String comment;

  @Inject
  public ApprovalRequestController(PublikationsService service,
      BearbeiterBoundary bearbeiterBoundary) {
    this.service = service;
    this.bearbeiterBoundary = bearbeiterBoundary;
  }

  @PostConstruct
  public void init() {
    loadApprovalRequests();
  }

  public void loadApprovalRequests() {
    beschreibungen = service.findAllBeschreibungWithActiveApprovalRequest();
    log.debug("Loaded {} publications for approval", beschreibungen.size());
  }

  public void assign(Beschreibung beschreibung) {
    try {
      service.assignReviewer(beschreibung, getCurrentUser());
      addMessage(FacesMessage.SEVERITY_INFO, "publikation_success_assigned", getCurrentUser());
      loadApprovalRequests();
    } catch (PublikationException e) {
      log.error("Failed to assign request", e);
      addMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessageArgs());
    } catch (Exception e) {
      log.error("Failed to assign request", e);
      addMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
    } finally {
      resetFields();
    }
  }

  @CheckPermission(permission = PERMISSION_BESCHREIBUNG_PUBLIZIEREN_VERWALTEN)
  public void approve(Beschreibung beschreibung) {
    try {
      service.approve(beschreibung, getCurrentUser(), comment);
      addMessage(FacesMessage.SEVERITY_INFO, "publikation_success_approved");
      loadApprovalRequests();
    } catch (PublikationException e) {
      log.error("Failed to approve request", e);
      addMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessageArgs());
    } catch (Exception e) {
      log.error("Failed to approve request", e);
      addMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
    } finally {
      resetFields();
    }
  }

  @CheckPermission(permission = PERMISSION_BESCHREIBUNG_PUBLIZIEREN_VERWALTEN)
  public void reject(Beschreibung beschreibung) {
    try {
      service.reject(beschreibung, getCurrentUser(), comment);
      addMessage(FacesMessage.SEVERITY_INFO, "publikation_success_rejected");
      loadApprovalRequests();
    } catch (PublikationException e) {
      log.error("Failed to reject request", e);
      addMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessageArgs());
    } catch (Exception e) {
      log.error("Failed to reject request", e);
      addMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
    } finally {
      resetFields();
    }
  }

  private void resetFields() {
    selectedBeschreibung = null;
    comment = null;
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
        new FacesMessage(severity, I18NController.getMessage(messageKey, args), null));
  }

}
