package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import org.omnifaces.exceptionhandler.ViewExpiredExceptionHandler;

/**
 * Informs the user after a {@link jakarta.faces.application.ViewExpiredException} was transparently
 * handled by the OmniFaces {@link ViewExpiredExceptionHandler}: the handler refreshes the requested
 * page, which discards any unsaved input, so a global warn message explains why that happened.
 */
@Named
@RequestScoped
public class ViewExpiredController {

  public void addMessageIfViewExpired() {
    if (ViewExpiredExceptionHandler.wasViewExpired()) {
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_WARN,
              I18NController.getMessage("view_expired"),
              I18NController.getMessage("view_expired_detail")));
    }
  }
}
