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
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.eureka.RestClientRequestFilter;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Christoph Marten on 24.09.2020 at 11:31
 */

@ViewScoped
@Named
@LoginCheck
public class BeschreibungsViewController implements Serializable {

  public static final String REQ_PARAM_PROTOTYP = "prototyp";
  public static final String REQ_PARAM_ID = "id";
  public static final String REQ_PARAM_EDIT = "edit";
  private static final long serialVersionUID = -7951285555430545723L;
  @ConfigProperty(name = "de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenGraphQLPort/mp-rest/url")
  String normdatenURL;
  private Logger logger = LoggerFactory.getLogger(BeschreibungsViewController.class);
  private BeschreibungsViewModel beschreibungsViewModel;
  private ExternalContext externalContext;
  private BeschreibungsBoundary beschreibungsBoundary;
  private BearbeiterBoundary bearbeiterBoundary;
  private Sperre sperre;
  private boolean showPrototyp = false;
  private boolean startInReadOnly = false;
  private String editParameter = "";

  @Inject
  RestClientRequestFilter restClientRequestFilter;

  BeschreibungsViewController() {
  }

  @PostConstruct
  public void setup() {

    logger.info("Constructing the View for Beschreibung");

    if (Objects.nonNull(FacesContext.getCurrentInstance())) {
      externalContext = FacesContext.getCurrentInstance().getExternalContext();
    }
    if (Objects.nonNull(externalContext)) {
      String parameterId = externalContext.getRequestParameterMap().get(REQ_PARAM_ID);
      editParameter = externalContext.getRequestParameterMap().get(REQ_PARAM_EDIT);
      if (Objects.nonNull(parameterId) && !parameterId.isEmpty()) {
        fillBeschreibungsViewModelValues(parameterId);

        if (Objects.nonNull(externalContext.getRequestParameterMap().get(REQ_PARAM_PROTOTYP))) {
          setShowPrototyp(true);
        }

        if (Objects.equals("true", editParameter)) {
          beschreibungSperren();
        } else {
          try {
            sperre = beschreibungsBoundary.findSperreForBeschreibung(parameterId).orElse(null);
          } catch (DokumentSperreException dse) {
            throw new RuntimeException("Error finding Sperre for beschreibung " + parameterId, dse);
          }
        }
      }
    }
  }

  public void loadingSperren(String beschreibungID) throws DokumentSperreException {
    logger.info("Trying to find Sperre for " + beschreibungID);
    sperre = beschreibungsBoundary.findSperreForBeschreibung(beschreibungID).orElse(null);
    logger.info("Found Sperre {}", sperre);
  }


  public boolean isEditorReadOnlyMode() {

    if (beschreibungsViewModel == null || bearbeiterBoundary == null) {
      return true;
    }

    if (VerwaltungsTyp.EXTERN == beschreibungsViewModel.getVerwaltungsTyp()) {
      return true;
    }

    if (!isOwnSperre()) {
      return true;
    }

    if ((Objects.equals("true", editParameter))
        && isOwnSperre() || isOwnSperre()) {
      return false;
    }

    return true;
  }

  boolean isOwnSperre() {
    if (sperre != null && sperre.getBearbeiter() != null) {
      return bearbeiterBoundary.getLoggedBearbeiter().equals(sperre.getBearbeiter());
    }

    return false;
  }

  public void handleBeschreibungBearbeitenEvent() {
    logger.info("Getting Beschreibung Bearbeiten Event");

    beschreibungSperren();
  }

  public void handleBeschreibungLesenEvent() {
    logger.info("Getting Beschreibung Lesen Event");

    if (sperre != null && bearbeiterBoundary.getLoggedBearbeiter().equals(sperre.getBearbeiter())) {
      try {
        beschreibungsBoundary.beschreibungEntsperren(beschreibungsViewModel.getId());
        sperre = null;
        fillBeschreibungsViewModelValues(beschreibungsViewModel.getId());
      } catch (Exception e) {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
            getMessage("beschreibung_entsperren_fehler"),
            e.getMessage());
        logger.warn("Error releasing sperre for beschreibung " + beschreibungsViewModel.getId(), e);
        PrimeFaces.current().dialog().showMessageDynamic(msg);
      }
    }
  }

  public String getNormdatenURL() {
    try {
      return restClientRequestFilter.extractServiceURIWithEureka(URI.create(normdatenURL))
          .toASCIIString();
    } catch (Error error) {
      logger.error("Error during resolving normdata url", error);
      return normdatenURL;
    }
  }

  public void fillBeschreibungsViewModelValues(String id) {

    logger.info("Fillout new BeschreibungsViewModel {}", id);

    Optional<BeschreibungsViewModel> model = beschreibungsBoundary.buildBeschreibungsViewModel(id);

    if (model.isPresent()) {
      beschreibungsViewModel = model.get();
    } else {
      logger.warn("beschreibungsViewModel not found for id: {}", id);
    }
  }

  public boolean enableToolbar() {

    if (beschreibungsViewModel == null || bearbeiterBoundary == null) {
      return false;
    }

    return VerwaltungsTyp.INTERN == beschreibungsViewModel.getVerwaltungsTyp()
        && (sperre == null || bearbeiterBoundary.getLoggedBearbeiter()
        .equals(sperre.getBearbeiter()));
  }

  public boolean showSperre() {
    if (beschreibungsViewModel == null || bearbeiterBoundary == null || sperre == null) {
      return false;
    }
    return !bearbeiterBoundary.getLoggedBearbeiter().equals(sperre.getBearbeiter());
  }

  void beschreibungSperren() {
    try {
      sperre = beschreibungsBoundary.beschreibungSperren(bearbeiterBoundary.getLoggedBearbeiter(),
          beschreibungsViewModel.getId());
    } catch (SperreAlreadyExistException saee) {
      sperre = Stream.ofNullable(saee.getSperren())
          .flatMap(List::stream)
          .findFirst()
          .orElse(null);

      String sperreBearbeiterName = Optional.ofNullable(sperre)
          .map(Sperre::getBearbeiter)
          .map(Bearbeiter::getName)
          .orElse(getMessage("beschreibung_unbekannt"));

      String sperreDatum = Optional.ofNullable(sperre)
          .map(s -> s.getStartDatum())
          .map(I18NController::formatDate)
          .orElse(getMessage("beschreibung_unbekannt"));

      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          getMessage("beschreibung_bearbeiten_sperren_fehler"),
          getMessage("beschreibung_bearbeiten_sperren_fehler_detail", sperreBearbeiterName,
              sperreDatum));
      logger.info("Sperre already exists for Beschreibung " + beschreibungsViewModel.getId(), saee);
      PrimeFaces.current().dialog().showMessageDynamic(msg);
    } catch (Exception e) {
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          getMessage("beschreibung_sperren_fehler"),
          e.getMessage());
      logger.warn("Error getting sperre for beschreibung " + beschreibungsViewModel.getId(), e);
      PrimeFaces.current().dialog().showMessageDynamic(msg);
    }
  }

  public BeschreibungsViewModel getBeschreibungsViewModel() {
    return beschreibungsViewModel;
  }

  public void setBeschreibungsViewModel(BeschreibungsViewModel beschreibungsViewModel) {
    this.beschreibungsViewModel = beschreibungsViewModel;
  }

  public boolean isShowPrototyp() {
    return showPrototyp;
  }

  public void setShowPrototyp(boolean showPrototyp) {
    this.showPrototyp = showPrototyp;
  }

  public Sperre getSperre() {
    return sperre;
  }

  public boolean isStartInReadOnly() {
    return startInReadOnly;
  }

  public void setExternalContext(ExternalContext externalContext) {
    this.externalContext = externalContext;
  }

  public StreamedContent getTeiContent() {
    if (Objects.nonNull(this.beschreibungsViewModel.getTeiXML())
        && !this.beschreibungsViewModel.getTeiXML().isEmpty()) {
      byte[] content = this.beschreibungsViewModel.getTeiXML().getBytes(StandardCharsets.UTF_8);

      return DefaultStreamedContent.builder()
          .name("tei-" + this.beschreibungsViewModel.getId() + ".xml")
          .contentType("text/xml")
          .contentLength(content.length)
          .stream(() -> new ByteArrayInputStream(content))
          .build();
    } else {
      logger.warn("Beschreibung contains no TEI-XML: " + beschreibungsViewModel.getId());
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          getMessage("beschreibung_teixml_fehler"), "");
      PrimeFaces.current().dialog().showMessageDynamic(msg);
      return null;
    }
  }

  public LocalDateTime getPublikationsDatum(List<Publikation> publikationen,
      PublikationsTyp publikationsTyp) {
    if (Objects.isNull(publikationen) || publikationen.isEmpty() || Objects.isNull(
        publikationsTyp)) {
      return null;
    }
    return publikationen.stream()
        .filter(publikation -> publikationsTyp == publikation.getPublikationsTyp())
        .findFirst()
        .map(publikation -> publikation.getDatumDerVeroeffentlichung())
        .orElse(null);
  }

  @Inject
  public void setBeschreibungsBoundary(BeschreibungsBoundary beschreibungsBoundary) {
    this.beschreibungsBoundary = beschreibungsBoundary;
  }

  @Inject
  public void setBearbeiterBoundary(BearbeiterBoundary bearbeiterBoundary) {
    this.bearbeiterBoundary = bearbeiterBoundary;
  }

}
