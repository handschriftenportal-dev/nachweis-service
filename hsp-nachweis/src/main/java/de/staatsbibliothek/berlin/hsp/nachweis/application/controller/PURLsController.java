package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.PURLsRechte.PERMISSION_AKTUALISIEREN;
import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.PURLsRechte.PERMISSION_GENERIEREN;
import static jakarta.faces.application.FacesMessage.SEVERITY_ERROR;
import static jakarta.faces.application.FacesMessage.SEVERITY_INFO;

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
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@ViewScoped
@Named
@LoginCheck
@Slf4j
public class PURLsController implements Serializable {

  @Serial
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

  @Getter
  private Map<PURLViewModel, PURL> generiertePURLsErfolg;
  @Getter
  private Map<PURLViewModel, String> generiertePURLsFehler;

  @Inject
  PURLsController(
      PURLsModel purlsModel,
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
        PURL purl =
            purlGenerieren(purlViewModel.getDokumentId(), purlViewModel.getDokumentObjektTyp());
        generiertePURLsErfolg.put(purlViewModel, purl);
      } catch (Exception e) {
        log.error(
            "Error creating purl for dokumentObjektTyp {} and dokumentId {}",
            purlViewModel.getDokumentObjektTyp(),
            purlViewModel.getDokumentId(),
            e);
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
      purlBoundary.resolverDateiGenerieren();
      showMessage(SEVERITY_INFO, getMessage("purls_erfolg_resolver_generieren"), "");
    } catch (Exception e) {
      log.error("Error in resolverDateiGenerieren", e);
      showMessage(SEVERITY_ERROR,
          getMessage("purls_fehler_resolver_generieren"), e.getMessage()
      );
    }
  }

  @CheckPermission(permission = {PERMISSION_AKTUALISIEREN})
  public void templatesAktualisieren() {
    log.debug("templatesAktualisieren");
    try {
      purlBoundary.updateTargetTemplate(DokumentObjektTyp.HSP_OBJECT, kodTargetUrlTemplate);
      purlBoundary.updateTargetTemplate(
          DokumentObjektTyp.HSP_DESCRIPTION, beschreibungTargetUrlTemplate);
      purlBoundary.updateTargetTemplate(
          DokumentObjektTyp.HSP_DESCRIPTION_RETRO, beschreibungRetroTargetUrlTemplate);
      initTargetTemplates();
      edit = false;
      showMessage(SEVERITY_INFO, getMessage("purls_templates_aktualisieren_ergebnis"), "");
    } catch (Exception e) {
      log.error("Error in resolverDateiGenerieren", e);
      showMessage(SEVERITY_ERROR, getMessage("purls_fehler_template_aktualisieren"),
          e.getMessage());
    }
  }

  public void validateTargetTemplate(
      final FacesContext context, final UIComponent component, final Object value) {
    if (Objects.nonNull(value) && !purlBoundary.isTemplateValid(value.toString())) {
      throw new ValidatorException(
          new FacesMessage(
              SEVERITY_ERROR,
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
      case HSP_DESCRIPTION, HSP_DESCRIPTION_RETRO:
        beschreibungsBoundary.addPURL(dokumentId, purl);
        break;
      default:
        throw new PURLException(getMessage("purls_dokumenttyp_not_supported", dokumentObjektTyp));
    }
    return purl;
  }

  void initTargetTemplates() {
    this.kodTargetUrlTemplate = purlBoundary.getTargetTemplate(DokumentObjektTyp.HSP_OBJECT);
    this.beschreibungTargetUrlTemplate =
        purlBoundary.getTargetTemplate(DokumentObjektTyp.HSP_DESCRIPTION);
    this.beschreibungRetroTargetUrlTemplate =
        purlBoundary.getTargetTemplate(DokumentObjektTyp.HSP_DESCRIPTION_RETRO);
  }

  private void showMessage(FacesMessage.Severity severity, String message, String details) {
    FacesMessage msg = new FacesMessage(severity, message, details);
    FacesContext.getCurrentInstance().addMessage(null, msg);
  }
}
