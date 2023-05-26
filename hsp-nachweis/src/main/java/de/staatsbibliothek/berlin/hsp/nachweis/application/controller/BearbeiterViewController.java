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

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 10.11.2021
 */
@ViewScoped
@Named
@LoginCheck
@Slf4j
public class BearbeiterViewController implements Serializable {

  private static final long serialVersionUID = -5911675184501712982L;
  private final transient BearbeiterBoundary bearbeiterBoundary;
  private final transient OAUTH2Config oauth2Config;
  private final transient NormdatenReferenzBoundary normdatenReferenzBoundary;
  private transient Bearbeiter bearbeiter;
  private transient boolean bearbeiterNew;
  private transient boolean edit;
  private transient NormdatenReferenz selectedPerson;
  private transient NormdatenReferenz selectedInstitution;

  @Inject
  public BearbeiterViewController(BearbeiterBoundary bearbeiterBoundary,
      OAUTH2Config oauth2Config,
      NormdatenReferenzBoundary normdatenReferenzBoundary) {
    this.bearbeiterBoundary = bearbeiterBoundary;
    this.oauth2Config = oauth2Config;
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
  }

  @PostConstruct
  public void setup() {
    bearbeiter = bearbeiterBoundary.getLoggedBearbeiter();

    if (Objects.isNull(bearbeiter)) {
      throw new RuntimeException("Bearbeiter must not be null!");
    }

    if (oauth2Config.isLoginCheckEnabled() && bearbeiterBoundary.getUnbekannterBearbeiter().equals(bearbeiter)) {
      throw new RuntimeException("The right Bearbeiter is not available!");
    }

    bearbeiterNew = bearbeiterBoundary.isBearbeiterNew();
    edit = bearbeiterNew;

    selectedPerson = bearbeiter.getPerson();
    selectedInstitution = bearbeiter.getInstitution();
  }

  public String bearbeiterAktualisieren() {
    try {
      NormdatenReferenz person = findWithAllFields(selectedPerson);
      NormdatenReferenz institution = findWithAllFields(selectedInstitution);

      boolean isBearbeiterNew = bearbeiterBoundary.isBearbeiterNew();
      bearbeiterBoundary.updateLoggedBearbeiter(person, institution);

      edit = false;

      FacesContext facesContext = FacesContext.getCurrentInstance();
      facesContext.getExternalContext().getFlash().setKeepMessages(isBearbeiterNew);
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
          getMessage("bearbeiter_aktualisieren_erfolg"),
          getMessage("bearbeiter_aktualisieren_erfolg_details"));
      facesContext.addMessage("globalmessages", msg);

      return isBearbeiterNew ? "/dashboard.xhtml?faces-redirect=true" : null;
    } catch (BearbeiterException e) {
      log.error("Error updating logged Bearbeiter", e);

      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          getMessage("bearbeiter_aktualisieren_fehler"),
          e.getMessage());
      PrimeFaces.current().dialog().showMessageDynamic(msg);
      return null;
    }
  }

  public Bearbeiter getBearbeiter() {
    return bearbeiter;
  }

  public void setBearbeiter(Bearbeiter bearbeiter) {
    this.bearbeiter = bearbeiter;
  }

  public NormdatenReferenz getSelectedPerson() {
    return selectedPerson;
  }

  public void setSelectedPerson(NormdatenReferenz selectedPerson) {
    this.selectedPerson = selectedPerson;
  }

  public NormdatenReferenz getSelectedInstitution() {
    return selectedInstitution;
  }

  public void setSelectedInstitution(NormdatenReferenz selectedInstitution) {
    this.selectedInstitution = selectedInstitution;
  }

  public boolean isBearbeiterNew() {
    return bearbeiterNew;
  }

  public boolean isEdit() {
    return edit;
  }

  public void editieren() {
    log.info("editieren");
    edit = true;
  }

  public void abbrechen() {
    log.info("abbrechen");
    edit = false;
    selectedPerson = bearbeiter.getPerson();
    selectedInstitution = bearbeiter.getInstitution();
  }

  private NormdatenReferenz findWithAllFields(NormdatenReferenz normdatenReferenz) throws NoSuchElementException {
    if (Objects.isNull(normdatenReferenz)) {
      return null;
    }
    return normdatenReferenzBoundary
        .findOneByIdOrNameAndType(normdatenReferenz.getId(), normdatenReferenz.getTypeName())
        .orElseThrow(() -> new NoSuchElementException(getMessage("bearbeiter_aktualisieren_fehler_normdatum",
            normdatenReferenz.getId())));
  }

}
