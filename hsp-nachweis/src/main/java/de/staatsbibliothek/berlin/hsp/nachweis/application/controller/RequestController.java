/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
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
