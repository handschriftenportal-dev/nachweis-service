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

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.WerkzeugeRechte.PERMISSION_WERKZEUGE_ANWENDEN;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.DokumentSperreBoundary;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.Serializable;
import java.util.Objects;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 06.03.2023
 */

@Slf4j
@ViewScoped
@Named
@LoginCheck
public class WerkzeugeController implements Serializable {

  private static final long serialVersionUID = 3733676236470666514L;

  private final KulturObjektDokumentBoundary kulturObjektDokumentBoundary;
  private final DokumentSperreBoundary dokumentSperreBoundary;

  private boolean speereExists;

  @Inject
  public WerkzeugeController(
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary,
      DokumentSperreBoundary dokumentSperreBoundary) {

    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
    this.dokumentSperreBoundary = dokumentSperreBoundary;
  }

  public void setup() {
    log.debug("setup started");

    try {
      speereExists = !dokumentSperreBoundary.findAll().isEmpty();
    } catch (Exception e) {
      speereExists = true;
      log.error("error in find all sperren", e);
      if (Objects.nonNull(FacesContext.getCurrentInstance())) {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
            getMessage("werkzeuge_attributsreferenzen_sperren_fehler"), e.getMessage());
        FacesContext.getCurrentInstance().addMessage("globalmessages", msg);
      }
    }
  }

  @CheckPermission(permission = PERMISSION_WERKZEUGE_ANWENDEN)
  public void migrateAttributsReferenzen() {
    log.info("migrateAttributsReferenzen started");
    try {
      kulturObjektDokumentBoundary.migrateAttributsReferenzen();
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
          getMessage("werkzeuge_attributsreferenzen_migirieren_finished"), "");
      FacesContext.getCurrentInstance().addMessage("globalmessages", msg);
    } catch (Exception e) {
      log.error("error in migrateAttributsReferenzen", e);
      if (Objects.nonNull(FacesContext.getCurrentInstance())) {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
            getMessage("werkzeuge_attributsreferenzen_migirieren_fehler"), e.getMessage());
        FacesContext.getCurrentInstance().addMessage("globalmessages", msg);
      }
    }
    log.info("migrateAttributsReferenzen finished");
  }

  public boolean isSpeereExists() {
    return speereExists;
  }

}
