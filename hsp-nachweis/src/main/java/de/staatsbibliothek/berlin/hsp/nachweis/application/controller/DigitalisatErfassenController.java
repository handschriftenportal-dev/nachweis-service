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
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 06.10.2020.
 * @version 1.0
 */

@Slf4j
@ViewScoped
@Named
@LoginCheck
public class DigitalisatErfassenController implements Serializable {

  public static final String DIGITIZATION_SCHEME_TYPE = "DIGI";
  static final String REQUEST_PARAMETER_KODID = "kodid";
  static final String REQUEST_PARAMETER_DIGITALISATID = "digitalisatid";
  static final String KOD_PAGE = "/kulturObjektDokument/kulturObjektDokument.xhtml?";
  static final String FACES_REDIRECT = "faces-redirect=true";
  @Serial
  private static final long serialVersionUID = -6468318377560767678L;
  private ExternalContext externalContext;
  private transient KulturObjektDokumentBoundary kulturObjektDokumentBoundary;
  private transient NormdatenReferenzBoundary normdatenReferenzBoundary;
  private transient BearbeiterBoundary bearbeiterBoundary;

  @Delegate
  private DigitalisatErfassenModel digitalisatErfassenModel;

  private Sperre sperre;

  @Inject
  public DigitalisatErfassenController(KulturObjektDokumentBoundary kulturObjektDokumentBoundary,
      NormdatenReferenzBoundary normdatenReferenzBoundary, BearbeiterBoundary bearbeiterBoundary,
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
    if (Objects.nonNull(externalContext) && Objects.nonNull(
        externalContext.getRequestParameterMap().get(REQUEST_PARAMETER_KODID))
        && !externalContext.getRequestParameterMap().get(REQUEST_PARAMETER_KODID).isEmpty()) {

      String id = externalContext.getRequestParameterMap().get(REQUEST_PARAMETER_KODID);
      String iddigitalisat = externalContext.getRequestParameterMap()
          .get(REQUEST_PARAMETER_DIGITALISATID);

      Optional<KulturObjektDokumentViewModel> model = kulturObjektDokumentBoundary.buildKulturObjektDokumentViewModel(
          id);

      if (model.isPresent()) {
        this.setKulturObjektDokumentViewModel(model.get());
        model.get().getDigitalisate().stream().filter(d -> d.getId().equals(iddigitalisat))
            .findAny().ifPresent(dm -> {
              this.digitalisatErfassenModel.setDigitalisatViewModel(dm);
              this.placeChangedListener();
            });

        findAndCheckSperre();

        try {

          normdatenReferenzBoundary.findConceptSchemesWithType(DIGITIZATION_SCHEME_TYPE)
              .forEach(
                  normdatenReferenz -> this.digitalisatErfassenModel.getDigitalisatSchemeTypen()
                      .put(normdatenReferenz.getName(), normdatenReferenz));
        } catch (Exception error) {
          log.error("Error during find concepts", error);
          FacesContext context = FacesContext.getCurrentInstance();
          FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
              "Normdatenservice not available!", error.getMessage());
          context.addMessage(null, msg);
        }
      }
    }
  }

  public void removeSelectedConceptFromView() {

    log.info("Delete select concept {}", this.getSelectedConcept());

    if (this.getSelectedConcept() != null) {
      log.info("Delete concept for remove {}", this.getSelectedConcept());
      this.digitalisatErfassenModel.getDigitalisatViewModel().getDigitalisatTypKonzepte()
          .remove(this.getSelectedConcept());
      this.setSelectedConcept(null);
    }
  }

  public void selectConceptForDeletion(SelectEvent<NormdatenReferenz> event) {
    log.info("Select concept for remove {}", event.getObject());
    this.setSelectedConcept(event.getObject());
  }

  public void unSelectConceptForDeletion(UnselectEvent<NormdatenReferenz> event) {
    this.setSelectedConcept(null);
  }

  public void conceptTypenZuruecksetzen() {
    this.setActiveConceptType(null);
    this.setActiveSchemeType(null);
    this.getDigitalisatConceptTypen().clear();
  }

  public void conceptTypenHinzufuegen() {

    log.info("Add Konzept for View model {} {}",
        this.digitalisatErfassenModel.getDigitalisatViewModel().getDigitalisatTypKonzepte().size(),
        this.getActiveConceptType());
    this.digitalisatErfassenModel.getDigitalisatViewModel().getDigitalisatTypKonzepte()
        .add(this.getActiveConceptType());
    this.conceptTypenZuruecksetzen();
  }

  public void selectConceptSchemeListener() {

    log.info("Get Change event for Select ConceptScheme {} ", this.getActiveSchemeType());

    this.getDigitalisatConceptTypen().clear();

    this.normdatenReferenzBoundary.findConceptWithConceptSchemeReferenz(this.getActiveSchemeType())
        .forEach(normdatenReferenz -> {
          this.getDigitalisatConceptTypen().put(normdatenReferenz.getName(), normdatenReferenz);
        });
  }

  public String digitalisatHinzufuegen() {

    try {
      if (Objects.nonNull(getDigitalisatViewModel()) && Objects.nonNull(
          getDigitalisatViewModel().getOrt())) {
        // find with all fields
        NormdatenReferenz normdatenOrt = normdatenReferenzBoundary.findOneByIdOrNameAndType(
            getDigitalisatViewModel().getOrt().getId(), ORT_TYPE_NAME).orElseThrow(
            () -> new Exception(
                "Keine Normdatenreferenz gefunden für id " + getDigitalisatViewModel().getOrt()
                    .getId()));
        getDigitalisatViewModel().setOrt(normdatenOrt);
      }

      if (Objects.nonNull(getDigitalisatViewModel()) && Objects.nonNull(
          getDigitalisatViewModel().getEinrichtung())) {
        // find with all fields
        NormdatenReferenz normdatenEinrichtung = normdatenReferenzBoundary.findOneByIdOrNameAndType(
                getDigitalisatViewModel().getEinrichtung().getId(), KOERPERSCHAFT_TYPE_NAME)
            .orElseThrow(() -> new Exception("Keine Normdatenreferenz gefunden für id "
                + getDigitalisatViewModel().getEinrichtung().getId()));
        getDigitalisatViewModel().setEinrichtung(normdatenEinrichtung);
      }

      kulturObjektDokumentBoundary.digitalisatHinzufuegen(
          this.getKulturObjektDokumentViewModel().getId(),
          this.getDigitalisatViewModel());

      kulturObjektDokumentBoundary.kulturObjektDokumentEntsperren(
          getKulturObjektDokumentViewModel().getId());

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
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          "Fehler beim Speichern des Digitalisats!", e.getMessage());
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
      kulturObjektDokumentBoundary.kulturObjektDokumentEntsperren(
          getKulturObjektDokumentViewModel().getId());
    } catch (DokumentSperreException e) {
      throw new RuntimeException(
          "Error releasing Sperre for KOD " + getKulturObjektDokumentViewModel().getId(), e);
    }

    return kodRedirectView();
  }

  public void kodEntsperren() {
    try {
      if (sperre != null && bearbeiterBoundary.getLoggedBearbeiter()
          .equals(sperre.getBearbeiter())) {
        kulturObjektDokumentBoundary.kulturObjektDokumentEntsperren(
            getKulturObjektDokumentViewModel().getId());
      }
    } catch (DokumentSperreException e) {
      throw new RuntimeException(
          "Error releasing Sperre for KOD " + getKulturObjektDokumentViewModel().getId(), e);
    }
  }

  public void placeChangedListener() {
    log.info("Change Ort and active Koerperschaft");

    this.getKoerperschaftViewModelMap().clear();

    if (Objects.isNull(this.getDigitalisatViewModel().getOrt())) {
      return;
    }

    normdatenReferenzBoundary.findKoerperschaftenByOrtId(
        this.getDigitalisatViewModel().getOrt().getId(), false).stream().forEach(
        normdatum -> this.getKoerperschaftViewModelMap().put(normdatum.getName(), normdatum));
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

      if (sperre == null || !bearbeiterBoundary.getLoggedBearbeiter()
          .equals(sperre.getBearbeiter())) {
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
      throw new RuntimeException(
          "Error in checkSperre for KOD " + getKulturObjektDokumentViewModel().getId(), e);
    }
  }
}
