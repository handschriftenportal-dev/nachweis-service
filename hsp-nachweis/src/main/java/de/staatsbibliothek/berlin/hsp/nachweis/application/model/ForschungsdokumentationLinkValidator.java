package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Forschungsdokumentation;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/** Validates the optional link of a Forschungsdokumentation before confirmation. */
@FacesValidator("forschungsdokumentationLinkValidator")
public class ForschungsdokumentationLinkValidator implements Validator<String> {

  @Override
  public void validate(FacesContext context, UIComponent component, String link) {
    if (Forschungsdokumentation.isValidLinkValue(link)) {
      return;
    }

    String message = getMessage("kod_forschungsdokumentation_link_invalid");
    throw new ValidatorException(
        new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
  }
}
