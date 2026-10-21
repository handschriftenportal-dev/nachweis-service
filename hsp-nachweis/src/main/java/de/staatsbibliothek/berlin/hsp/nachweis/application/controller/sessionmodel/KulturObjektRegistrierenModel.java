package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.KODRechte.PERMISSION_KOD_REGISTRIEREN;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.KulturObjektDokumentViewModel.KulturObjektDokumentViewModelBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.kodregistrieren.KulturObjektDokumentRegistrierenException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.CheckPermission;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 27.08.2020.
 * @version 1.0
 */
@SessionScoped
@Named
@Data
@Slf4j
@LoginCheck
public class KulturObjektRegistrierenModel implements Serializable {

  public static final String KOD_REGISTRIEREN_SUCCESS_PATH = "/kod-registrieren-ergebnis.xhtml";
  public static final String KOD_REGISTRIEREN_PATH = "/kod-verwalten.xhtml";
  @Serial
  private static final long serialVersionUID = -4421952037778163287L;
  private NormdatenReferenz orteViewModel;

  private NormdatenReferenz koerperschaftViewModel;

  private Map<String, NormdatenReferenz> koerperschaftViewModelMap;

  private Collection<KulturObjektDokumentViewModel> registrierteKODs;

  private Collection<KulturObjektDokumentViewModel> failureKODs;

  private LocalDateTime regisrierungsZeit;

  private KulturObjektDokumentRegistrierenException error;

  private transient KulturObjektDokumentBoundary kulturObjektDokumentBoundary;

  private transient BearbeiterBoundary bearbeiterBoundary;

  private transient NormdatenReferenzBoundary normdatenReferenzBoundary;

  private boolean renderProgressbar = false;

  @Inject
  public KulturObjektRegistrierenModel(
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary,
      BearbeiterBoundary bearbeiterBoundary,
      NormdatenReferenzBoundary normdatenReferenzBoundary) {
    this.koerperschaftViewModelMap = new HashMap<>();
    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
    this.bearbeiterBoundary = bearbeiterBoundary;
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
  }

  @CheckPermission(permission = PERMISSION_KOD_REGISTRIEREN)
  @Transactional(rollbackOn = {Exception.class})
  @TransactionConfiguration(timeout = 7200)
  public void handleFileUpload(FileUploadEvent event)
      throws IOException, SperreAlreadyExistException {

    log.info(
        "Getting File Upload for Signatur CSV File {} {} {} ",
        event,
        orteViewModel,
        koerperschaftViewModel);

    regisrierungsZeit = LocalDateTime.now();
    registrierteKODs = null;

    try {

      UploadedFile file = event.getFile();
      if (orteViewModel == null || koerperschaftViewModel == null || file == null) {
        log.warn(
            "orteViewModel = {}, koerperschaftViewModel = {},file = {}",
            orteViewModel,
            koerperschaftViewModel,
            file);
      } else {

        NormdatenReferenz ort =
            normdatenReferenzBoundary
                .findOneByIdOrNameAndType(orteViewModel.getId(), GNDEntityFact.PLACE_TYPE_NAME)
                .orElse(orteViewModel);

        NormdatenReferenz koerperschaftAllFields =
            normdatenReferenzBoundary
                .findOneByIdOrNameAndType(
                    koerperschaftViewModel.getId(), GNDEntityFact.CORPORATE_BODY_TYPE_NAME)
                .orElse(koerperschaftViewModel);

        registrierteKODs =
            kulturObjektDokumentBoundary
                .registrieren(
                    bearbeiterBoundary.getLoggedBearbeiter(),
                    ort,
                    koerperschaftAllFields,
                    file.getContent())
                .stream()
                .map(
                    kod ->
                        KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
                            .withId(kod.getId())
                            .withSignatur(kod.getGueltigeIdentifikation().getIdent())
                            .withAlternativeSignaturen(kod.getAlternativeIdentifikationen())
                            .addAttributsReferenzStatusTyp(kod.getAttributsReferenzStatusTyp())
                            .build())
                .toList();
      }
    } catch (KulturObjektDokumentRegistrierenException e) {
      handleRegistrationError(e);
    } catch (Exception error) {
      log.error("Error during file upload.", error);
    }

    resetModel();

    FacesContext facesContext = FacesContext.getCurrentInstance();
    if (facesContext != null && facesContext.getExternalContext() != null) {
      facesContext.getExternalContext().redirect(KOD_REGISTRIEREN_SUCCESS_PATH);
    }
  }

  private void handleRegistrationError(KulturObjektDokumentRegistrierenException e) {
    log.error("Error during KOD Registrieren", e);
    error = e;
    failureKODs =
        e.getKodFailureList().stream()
            .map(
                kod ->
                    KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
                        .withId(kod.getId())
                        .withSignatur(kod.getGueltigeIdentifikation().getIdent())
                        .withAlternativeSignaturen(kod.getAlternativeIdentifikationen())
                        .build())
            .toList();
  }

  protected void resetModel() {
    renderProgressbar = false;
    koerperschaftViewModel = null;
    orteViewModel = null;
  }

  @CheckPermission(permission = PERMISSION_KOD_REGISTRIEREN)
  public void neuRegistrieren() throws IOException {

    error = null;
    failureKODs = null;
    registrierteKODs = null;
    koerperschaftViewModel = null;
    orteViewModel = null;
    renderProgressbar = false;

    FacesContext facesContext = FacesContext.getCurrentInstance();
    if (facesContext != null && facesContext.getExternalContext() != null) {
      facesContext.getExternalContext().redirect(KOD_REGISTRIEREN_PATH);
    }
  }

  void setError(KulturObjektDokumentRegistrierenException error) {
    this.error = error;
  }

  public boolean isRenderProgressbar() {
    return renderProgressbar;
  }

  public void setRenderProgressbar(boolean renderProgressbar) {
    this.renderProgressbar = renderProgressbar;
  }
}
