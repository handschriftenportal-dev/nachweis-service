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

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.ORT_TYPE_NAME;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.DigitalisatErfassenModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DigitalisatViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 06.10.2020.
 * @version 1.0
 */

@Slf4j
@ViewScoped
@Named
@LoginCheck
public class DigitalisatErfassenController implements Serializable {

  static final String REQUEST_PARAMETER_KODID = "kodid";
  static final String REQUEST_PARAMETER_DIGITALISATID = "digitalisatid";
  static final String KOD_PAGE = "/kulturObjektDokument/kulturObjektDokument.xhtml?";
  static final String FACES_REDIRECT = "faces-redirect=true";
  private static final long serialVersionUID = -6468318377560767678L;
  private ExternalContext externalContext;
  private transient KulturObjektDokumentBoundary kulturObjektDokumentBoundary;
  private transient NormdatenReferenzBoundary normdatenReferenzBoundary;
  private transient BearbeiterBoundary bearbeiterBoundary;

  @Delegate
  private DigitalisatErfassenModel digitalisatErfassenModel;

  private Sperre sperre;

  @Inject
  public DigitalisatErfassenController(
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary,
      NormdatenReferenzBoundary normdatenReferenzBoundary,
      BearbeiterBoundary bearbeiterBoundary,
      DigitalisatErfassenModel digitalisatErfassenModel) {
    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
    this.bearbeiterBoundary = bearbeiterBoundary;
    this.digitalisatErfassenModel = digitalisatErfassenModel;
  }

  @PostConstruct
  public void setup() {
    if (Objects.nonNull(FacesContext.getCurrentInstance())) {
      externalContext = FacesContext.getCurrentInstance().getExternalContext();
    }
    if (Objects.nonNull(externalContext) && Objects
        .nonNull(externalContext.getRequestParameterMap().get(REQUEST_PARAMETER_KODID))
        && !externalContext.getRequestParameterMap().get(REQUEST_PARAMETER_KODID).isEmpty()) {

      String id = externalContext.getRequestParameterMap().get(REQUEST_PARAMETER_KODID);
      String iddigitalisat = externalContext.getRequestParameterMap().get(REQUEST_PARAMETER_DIGITALISATID);

      Optional<KulturObjektDokumentViewModel> model = kulturObjektDokumentBoundary
          .buildKulturObjektDokumentViewModel(id);

      if (model.isPresent()) {
        this.setKulturObjektDokumentViewModel(model.get());
        model.get().getDigitalisate().stream().filter(d -> d.getId().equals(iddigitalisat)).findAny().ifPresent(dm -> {
          this.digitalisatErfassenModel.setDigitalisatViewModel(dm);
          this.placeChangedListener();
        });

        findAndCheckSperre();
      }
    }
  }

  public String digitalisatHinzufuegen() {

    try {
      if (Objects.nonNull(getDigitalisatViewModel()) && Objects.nonNull(getDigitalisatViewModel().getOrt())) {
        // find with all fields
        NormdatenReferenz normdatenOrt = normdatenReferenzBoundary
            .findOneByIdOrNameAndType(getDigitalisatViewModel().getOrt().getId(), ORT_TYPE_NAME)
            .orElseThrow(() -> new Exception(
                "Keine Normdatenreferenz gefunden für id " + getDigitalisatViewModel().getOrt().getId()));
        getDigitalisatViewModel().setOrt(normdatenOrt);
      }

      if (Objects.nonNull(getDigitalisatViewModel()) && Objects.nonNull(getDigitalisatViewModel().getEinrichtung())) {
        // find with all fields
        NormdatenReferenz normdatenEinrichtung = normdatenReferenzBoundary
            .findOneByIdOrNameAndType(getDigitalisatViewModel().getEinrichtung().getId(), KOERPERSCHAFT_TYPE_NAME)
            .orElseThrow(() -> new Exception(
                "Keine Normdatenreferenz gefunden für id " + getDigitalisatViewModel().getEinrichtung().getId()));
        getDigitalisatViewModel().setEinrichtung(normdatenEinrichtung);
      }

      kulturObjektDokumentBoundary
          .digitalisatHinzufuegen(this.getKulturObjektDokumentViewModel(), this.getDigitalisatViewModel());

      kulturObjektDokumentBoundary.kulturObjektDokumentEntsperren(getKulturObjektDokumentViewModel().getId());

      this.setDigitalisatViewModel(new DigitalisatViewModel());

      final FacesContext facesContext = FacesContext.getCurrentInstance();
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
          getMessage("digitalisat_erfassen_erfolg"), "");

      if (Objects.nonNull(facesContext) && facesContext.getExternalContext() != null) {
        Flash flash = facesContext.getExternalContext().getFlash();
        flash.setKeepMessages(true);
        facesContext.addMessage("globalmessages", msg);

        facesContext.getExternalContext().redirect(kodRedirectView() + "#digitalisate");
      }

      return null;

    } catch (Exception e) {
      log.error("Error during digitalisat hinzufügen!", e);
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler beim Speichern des Digitalisats!",
          e.getMessage());
      FacesContext.getCurrentInstance().addMessage("globalmessages", msg);
      return null;
    }
  }

  String kodRedirectView() {
    if (getKulturObjektDokumentViewModel() != null) {
      return KOD_PAGE + FACES_REDIRECT + "&id=" + getKulturObjektDokumentViewModel().getId();
    } else {
      return KOD_PAGE + FACES_REDIRECT;
    }
  }

  public String abbrechen() {
    this.setDigitalisatViewModel(new DigitalisatViewModel());

    try {
      kulturObjektDokumentBoundary.kulturObjektDokumentEntsperren(getKulturObjektDokumentViewModel().getId());
    } catch (DokumentSperreException e) {
      throw new RuntimeException("Error releasing Sperre for KOD " + getKulturObjektDokumentViewModel().getId(), e);
    }

    return kodRedirectView();
  }

  public void kodEntsperren() {
    try {
      if (sperre != null && bearbeiterBoundary.getLoggedBearbeiter().equals(sperre.getBearbeiter())) {
        kulturObjektDokumentBoundary.kulturObjektDokumentEntsperren(getKulturObjektDokumentViewModel().getId());
      }
    } catch (DokumentSperreException e) {
      throw new RuntimeException("Error releasing Sperre for KOD " + getKulturObjektDokumentViewModel().getId(), e);
    }
  }

  public void placeChangedListener() {
    log.info("Change Ort and active Koerperschaft");

    this.getKoerperschaftViewModelMap().clear();

    if (Objects.isNull(this.getDigitalisatViewModel().getOrt())) {
      return;
    }

    normdatenReferenzBoundary.findKoerperschaftenByOrtId(this.getDigitalisatViewModel().getOrt().getId(), false)
        .stream()
        .forEach(normdatum -> this.getKoerperschaftViewModelMap().put(normdatum.getName(), normdatum));
  }

  public ExternalContext getExternalContext() {
    return externalContext;
  }

  void setExternalContext(ExternalContext externalContext) {
    this.externalContext = externalContext;
  }

  void findAndCheckSperre() {
    try {
      // sperre has been acquired from KulturObjektDokumentViewController:
      sperre = kulturObjektDokumentBoundary.findSperreForKulturObjektDokument(
          getKulturObjektDokumentViewModel().getId()).orElse(null);

      if (sperre == null || !bearbeiterBoundary.getLoggedBearbeiter().equals(sperre.getBearbeiter())) {
        final FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
            getMessage("digitalisat_erfassen_kod_sperren_fehler"),
            getMessage("digitalisat_erfassen_kod_sperren_fehler_detail"));

        if (Objects.nonNull(facesContext) && facesContext.getExternalContext() != null) {
          Flash flash = facesContext.getExternalContext().getFlash();
          flash.setKeepMessages(true);
          facesContext.addMessage("globalmessages", msg);

          externalContext.redirect(KOD_PAGE + "id=" + getKulturObjektDokumentViewModel().getId());
        }
      }
    } catch (DokumentSperreException | IOException e) {
      throw new RuntimeException("Error in checkSperre for KOD " + getKulturObjektDokumentViewModel().getId(), e);
    }
  }
}
