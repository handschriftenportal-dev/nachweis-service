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
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
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
  public static final String KOD_REGISTRIEREN_PATH = "/kod-registrieren.xhtml";
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
  public KulturObjektRegistrierenModel(KulturObjektDokumentBoundary kulturObjektDokumentBoundary,
      BearbeiterBoundary bearbeiterBoundary, NormdatenReferenzBoundary normdatenReferenzBoundary) {
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

    log.info("Getting File Upload for Signatur CSV File {} {} {} ", event,
        orteViewModel,
        koerperschaftViewModel);

    regisrierungsZeit = LocalDateTime.now();
    registrierteKODs = null;

    try {

      UploadedFile file = event.getFile();
      if (orteViewModel == null || koerperschaftViewModel == null || file == null) {
        log.warn("orteViewModel = {}, koerperschaftViewModel = {},file = {}", orteViewModel,
            koerperschaftViewModel,
            file);
      } else {

        NormdatenReferenz ort = normdatenReferenzBoundary
            .findOneByIdOrNameAndType(orteViewModel.getId(), GNDEntityFact.PLACE_TYPE_NAME)
            .orElse(orteViewModel);

        NormdatenReferenz koerperschaftAllFields = normdatenReferenzBoundary
            .findOneByIdOrNameAndType(koerperschaftViewModel.getId(), GNDEntityFact.CORPORATE_BODY_TYPE_NAME)
            .orElse(koerperschaftViewModel);

        registrierteKODs = kulturObjektDokumentBoundary
            .registrieren(bearbeiterBoundary.getLoggedBearbeiter(), ort, koerperschaftAllFields,
                file.getContent()).stream()
            .map(kod -> KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
                .withId(kod.getId())
                .withSignatur(kod.getGueltigeIdentifikation().getIdent())
                .withAlternativeSignaturen(kod.getAlternativeIdentifikationen().stream()
                    .map(i -> i.getIdent())
                    .collect(Collectors.toList()))
                .build())
            .collect(Collectors.toList());
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
    failureKODs = error.getKodFailureList().stream()
        .map(kod -> KulturObjektDokumentViewModelBuilder.KulturObjektDokumentViewModel()
            .withId(kod.getId())
            .withSignatur(kod.getGueltigeIdentifikation().getIdent())
            .withAlternativeSignaturen(kod.getAlternativeIdentifikationen().stream()
                .map(i -> i.getIdent())
                .collect(Collectors.toList()))
            .build())
        .collect(Collectors.toList());
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

  void setError(
      KulturObjektDokumentRegistrierenException error) {
    this.error = error;
  }

  public boolean isRenderProgressbar() {
    return renderProgressbar;
  }

  public void setRenderProgressbar(boolean renderProgressbar) {
    this.renderProgressbar = renderProgressbar;
  }
}
