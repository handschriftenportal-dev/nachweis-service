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
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.PURLsRechte.PERMISSION_AKTUALISIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.PURLsRechte.PERMISSION_GENERIEREN;
import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.PURLsModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLViewModel;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@ViewScoped
@Named
@LoginCheck
@Slf4j
public class PURLsController implements Serializable {

  private static final long serialVersionUID = 875395979591841095L;

  private final PURLsModel purlsModel;

  private final PURLBoundary purlBoundary;
  private final KulturObjektDokumentBoundary kulturObjektDokumentBoundary;
  private final BeschreibungsBoundary beschreibungsBoundary;

  @Getter
  private boolean edit;

  @Getter
  @Setter
  private String kodTargetUrlTemplate;

  @Getter
  @Setter
  private String beschreibungTargetUrlTemplate;

  @Getter
  @Setter
  private String beschreibungRetroTargetUrlTemplate;

  private Map<PURLViewModel, PURL> generiertePURLsErfolg;
  private Map<PURLViewModel, String> generiertePURLsFehler;

  @Inject
  PURLsController(PURLsModel purlsModel,
      PURLBoundary purlBoundary,
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary,
      BeschreibungsBoundary beschreibungsBoundary) {
    this.purlsModel = purlsModel;
    this.purlBoundary = purlBoundary;
    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
    this.beschreibungsBoundary = beschreibungsBoundary;

    initTargetTemplates();
  }

  @CheckPermission(permission = {PERMISSION_GENERIEREN})
  public void purlsGenerieren() {
    log.debug("purlsGenerieren");

    generiertePURLsErfolg = new LinkedHashMap<>();
    generiertePURLsFehler = new LinkedHashMap<>();

    for (PURLViewModel purlViewModel : purlsModel.getSelectedPURLViewModels()) {
      try {
        PURL purl = purlGenerieren(purlViewModel.getDokumentId(), purlViewModel.getDokumentObjektTyp());
        generiertePURLsErfolg.put(purlViewModel, purl);
      } catch (Exception e) {
        log.error("Error creating purl for dokumentObjektTyp " + purlViewModel.getDokumentObjektTyp()
            + " and dokumentId " + purlViewModel.getDokumentId(), e);
        generiertePURLsFehler.put(purlViewModel, e.getMessage());
      }
    }
    purlsModel.getSelectedPURLViewModels().clear();
    purlsModel.resetTable();
    purlsModel.loadAllData();
  }

  @Transactional(rollbackOn = Exception.class)
  @TransactionConfiguration(timeout = 14400)
  @CheckPermission(permission = {PERMISSION_AKTUALISIEREN})
  public void purlsAktualisieren() {
    log.debug("purlsAktualisieren");

    try {
      int updated = purlBoundary.updateInternalPURLs(purlsModel.getPurlViewModels());
      purlsModel.resetTable();
      purlsModel.loadAllData();
      showMessage(SEVERITY_INFO, getMessage("purls_erfolg_purls_aktualisieren", updated), "");
    } catch (Exception e) {
      log.error("Error in purlsAktualisieren", e);
      showMessage(SEVERITY_ERROR, getMessage("purls_fehler_purls_aktualisieren"), e.getMessage());
    }
  }

  @CheckPermission(permission = {PERMISSION_GENERIEREN})
  public void resolverDateiGenerieren() {
    log.debug("resolverDateiGenerieren");

    try {
      Set<PURL> internalPURLs = purlsModel.getPurlViewModels().stream()
          .flatMap(model -> model.getInternalPURLs().stream())
          .collect(Collectors.toSet());

      Set<PURL> purlsDBM = purlBoundary.createDBMFile(internalPURLs);
      log.info("Created Resolver-File for {} purls.", Objects.isNull(purlsDBM) ? 0 : purlsDBM.size());

      showMessage(SEVERITY_INFO, getMessage("purls_erfolg_resolver_generieren"), "");
    } catch (Exception e) {
      log.error("Error in resolverDateiGenerieren", e);
      showMessage(SEVERITY_ERROR, getMessage("purls_fehler_resolver_generieren"), e.getMessage());
    }
  }

  @CheckPermission(permission = {PERMISSION_AKTUALISIEREN})
  public void templatesAktualisieren() {
    log.debug("templatesAktualisieren");
    try {
      purlBoundary.updateTargetTemplate(DokumentObjektTyp.HSP_OBJECT, kodTargetUrlTemplate);
      purlBoundary.updateTargetTemplate(DokumentObjektTyp.HSP_DESCRIPTION, beschreibungTargetUrlTemplate);
      purlBoundary.updateTargetTemplate(DokumentObjektTyp.HSP_DESCRIPTION_RETRO, beschreibungRetroTargetUrlTemplate);
      initTargetTemplates();
      edit = false;
      showMessage(SEVERITY_INFO, getMessage("purls_templates_aktualisieren_ergebnis"), "");
    } catch (Exception e) {
      log.error("Error in resolverDateiGenerieren", e);
      showMessage(SEVERITY_ERROR, getMessage("purls_fehler_template_aktualisieren"), e.getMessage());
    }
  }

  public void validateTargetTemplate(final FacesContext context, final UIComponent component, final Object value) {
    if (Objects.nonNull(value) && !purlBoundary.isTemplateValid(value.toString())) {
      throw new ValidatorException(
          new FacesMessage(SEVERITY_ERROR,
              getMessage("purls_fehler_template_invalid", value.toString()),
              getMessage("purls_fehler_template_invalid_detail")));
    }
  }

  public void editieren() {
    log.info("editieren");
    edit = true;
  }

  public void abbrechen() {
    log.info("abbrechen");
    edit = false;
    initTargetTemplates();
  }

  @Transactional(rollbackOn = Exception.class)
  PURL purlGenerieren(String dokumentId, DokumentObjektTyp dokumentObjektTyp)
      throws PURLException, KulturObjektDokumentException, BeschreibungsException {
    PURL purl = purlBoundary.createNewPURL(dokumentId, dokumentObjektTyp);
    switch (dokumentObjektTyp) {
      case HSP_OBJECT:
        kulturObjektDokumentBoundary.addPURL(dokumentId, purl);
        break;
      case HSP_DESCRIPTION:
      case HSP_DESCRIPTION_RETRO:
        beschreibungsBoundary.addPURL(dokumentId, purl);
        break;
      default:
        throw new PURLException(getMessage("purls_dokumenttyp_not_supported", dokumentObjektTyp));
    }
    return purl;
  }

  public Map<PURLViewModel, PURL> getGeneriertePURLsErfolg() {
    return generiertePURLsErfolg;
  }

  public Map<PURLViewModel, String> getGeneriertePURLsFehler() {
    return generiertePURLsFehler;
  }

  void initTargetTemplates() {
    this.kodTargetUrlTemplate = purlBoundary.getTargetTemplate(DokumentObjektTyp.HSP_OBJECT);
    this.beschreibungTargetUrlTemplate = purlBoundary.getTargetTemplate(DokumentObjektTyp.HSP_DESCRIPTION);
    this.beschreibungRetroTargetUrlTemplate = purlBoundary.getTargetTemplate(DokumentObjektTyp.HSP_DESCRIPTION_RETRO);
  }

  private void showMessage(FacesMessage.Severity severity, String message, String details) {
    FacesMessage msg = new FacesMessage(severity, message, details);
    FacesContext.getCurrentInstance().addMessage(null, msg);
  }

}

