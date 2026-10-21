package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import jakarta.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 11.06.2020.
 * @version 1.0
 */

@Named
@SessionScoped
@Slf4j
public class LanguageController implements Serializable {

  private static final long serialVersionUID = 637848458502367170L;

  private Locale locale;

  @PostConstruct
  public void init() {

    if (FacesContext.getCurrentInstance() != null
        && FacesContext.getCurrentInstance().getExternalContext() != null) {
      locale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
    }
  }

  public Locale getLocale() {
    return locale;
  }

  public String getLanguage() {
    return Objects.nonNull(locale) ? locale.getLanguage() : "";
  }

  public void setLanguage(String language) {
    log.debug("Set new locale {}", language);
    locale = new Locale(language);
    if (FacesContext.getCurrentInstance() != null
        && FacesContext.getCurrentInstance().getViewRoot() != null) {
      FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
    }
  }

  public String getDisplayLanguage() {
    return Objects.nonNull(locale) ? locale.getDisplayLanguage(locale) : "";
  }

}
