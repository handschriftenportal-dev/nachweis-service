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

import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 11.06.2020.
 * @version 1.0
 */

@Named
@SessionScoped
@LoginCheck
@Slf4j
public class LanguageController implements Serializable {

  private static final long serialVersionUID = 637848458502367170L;

  private Locale locale;

  @PostConstruct
  public void init() {

    if (FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().getExternalContext() != null) {
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
    if (FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().getViewRoot() != null) {
      FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
    }
  }

  public String getDisplayLanguage() {
    return Objects.nonNull(locale) ? locale.getDisplayLanguage(locale) : "";
  }

}
