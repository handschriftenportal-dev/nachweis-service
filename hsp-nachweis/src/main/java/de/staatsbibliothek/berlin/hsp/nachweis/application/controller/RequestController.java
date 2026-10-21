package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 12.06.2020.
 * @version 1.0
 */

@Named
@RequestScoped
public class RequestController {

  private static final Logger logger = LoggerFactory.getLogger(RequestController.class);
  private static final String LOCALE_PARAMETER = "lang";
  private LanguageController languageController;
  private String locale;
  private ExternalContext context;

  @Inject
  public RequestController(LanguageController languageController) {
    this.languageController = languageController;
  }

  @PostConstruct
  public void request() {

    if (context == null) {
      context = FacesContext.getCurrentInstance().getExternalContext();
    }

    if (context != null) {
      Map<String, String> requestMap = context.getRequestParameterMap();

      logger.debug("Getting Request {}, {}", requestMap.keySet().toArray()
          , requestMap.values().toArray());

      if (requestMap.containsKey(LOCALE_PARAMETER)) {

        locale = requestMap.get(LOCALE_PARAMETER);

        logger.debug("Getting Request with locale {} ", locale);

        languageController.setLanguage(locale);
      }
    }
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public LanguageController getLanguageController() {
    return languageController;
  }

  void setContext(ExternalContext context) {
    this.context = context;
  }
}
