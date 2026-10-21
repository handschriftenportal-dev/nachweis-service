package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessource;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import java.util.Objects;

/**
 * Rejects an invalid url while the user types instead of on save.
 *
 * <p>Blank is accepted here — it only means the form is not filled in yet, which the disabled save
 * button already covers.</p>
 */
@FacesValidator("externeRessourceUrlValidator")
public class ExterneRessourceUrlValidator implements Validator<String> {

  @Override
  public void validate(FacesContext context, UIComponent component, String url) {

    if (Objects.isNull(url) || url.isBlank() || ExterneRessource.isValidUrl(url)) {
      return;
    }

    String message = getMessage("kod_externe_ressource_url_invalid");

    throw new ValidatorException(
        new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
  }
}
