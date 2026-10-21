package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;
import static java.lang.Boolean.parseBoolean;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.PublikationsService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.stork.StorkDiscovery;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.tei.TeiSnapshotZipper;
import de.staatsbibliothek.berlin.javaee.authentication.entity.Authentication;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.omnifaces.cdi.ViewScoped;
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

  public static final String REQ_PARAM_ID = "id";
  public static final String REQ_PARAM_EDIT = "edit";
  @Serial
  private static final long serialVersionUID = -7951285555430545723L;
  private static final Logger logger = LoggerFactory.getLogger(BeschreibungsViewController.class);
  final StorkDiscovery storkDiscovery;
  private final BeschreibungsBoundary beschreibungsBoundary;
  private final BearbeiterBoundary bearbeiterBoundary;
  private final PublikationsService publikationsService;
  @Getter
  private final boolean startInReadOnly = false;
  String normdatenURL;
  String validationURL;
  Authentication authentication;
  @Getter
  @Setter
  private BeschreibungsViewModel beschreibungsViewModel;
  @Getter
  private Sperre sperre;
  private String acquiredEditorSperreId;
  private boolean isEditable = false;

  @Inject
  public BeschreibungsViewController(
      @ConfigProperty(name = "hsp.services.normdaten.graphql") String normdatenURL,
      @ConfigProperty(name = "hsp.services.importservice.rest") String validationURL,
      BeschreibungsBoundary beschreibungsBoundary,
      BearbeiterBoundary bearbeiterBoundary,
      PublikationsService publikationsService,
      StorkDiscovery storkDiscovery,
      Authentication authentication) {
    this.normdatenURL = normdatenURL;
    this.validationURL = validationURL;
    this.beschreibungsBoundary = beschreibungsBoundary;
    this.bearbeiterBoundary = bearbeiterBoundary;
    this.publikationsService = publikationsService;
    this.storkDiscovery = storkDiscovery;
    this.authentication = authentication;
  }

  @PostConstruct
  public void setup() {

    logger.info("Constructing the View for Beschreibung");
    var externalContext = FacesContext.getCurrentInstance().getExternalContext();
    var parameterId = externalContext.getRequestParameterMap().get(REQ_PARAM_ID);
    this.isEditable = parseBoolean(externalContext.getRequestParameterMap().get(REQ_PARAM_EDIT));
    if (StringUtils.isBlank(parameterId)) {
      return;
    }
    fillBeschreibungsViewModelValues(parameterId);

    if (this.isEditable) {
      beschreibungSperren();
    } else {
      findSperre(parameterId);
    }
  }

  @PreDestroy
  void releaseEditorSperreOnDestroy() {
    if (acquiredEditorSperreId == null) {
      return;
    }
    String beschreibungId = beschreibungsViewModel != null ? beschreibungsViewModel.getId() : null;
    if (beschreibungId == null) {
      logger.error("Cannot release editor sperre {}: beschreibungsViewModel was null on destroy",
          acquiredEditorSperreId);
      return;
    }
    if (sperre != null
        && (!acquiredEditorSperreId.equals(sperre.getId())
        || sperre.getSperreTyp() != SperreTyp.MANUAL)) {
      logger.error("Skipping release: tracked editor sperre {} no longer matches loaded sperre "
              + "(id={}, typ={}) for beschreibung {}",
          acquiredEditorSperreId, sperre.getId(), sperre.getSperreTyp(), beschreibungId);
      return;
    }
    try {
      beschreibungsBoundary.beschreibungEntsperren(beschreibungId);
      logger.info("Released editor sperre {} for beschreibung {} on view destroy",
          acquiredEditorSperreId, beschreibungId);
      acquiredEditorSperreId = null;
      sperre = null;
    } catch (Exception e) {
      logger.error("LEAKED editor sperre {} for beschreibung {} on view destroy "
              + "— manual cleanup may be required",
          acquiredEditorSperreId, beschreibungId, e);
    }
  }

  private void findSperre(String parameterId) {
    try {
      sperre = beschreibungsBoundary.findSperreForBeschreibung(parameterId).orElse(null);
    } catch (DokumentSperreException dse) {
      throw new RuntimeException("Error finding Sperre for beschreibung " + parameterId, dse);
    }
  }

  public void loadingSperren(String beschreibungID) throws DokumentSperreException {
    logger.info("Trying to find Sperre for {}", beschreibungID);
    sperre = beschreibungsBoundary.findSperreForBeschreibung(beschreibungID).orElse(null);
    logger.info("Found Sperre {}", sperre);
  }

  public boolean isEditorReadOnlyMode() {
    return isInitializationIncomplete()
        || beschreibungsViewModel.isPublished()
        || !isBeschreibungActionable()
        || !isOwnSperre();
  }

  private boolean isInitializationIncomplete() {
    return beschreibungsViewModel == null || bearbeiterBoundary == null;
  }

  private boolean isBeschreibungActionable() {
    return beschreibungsBoundary.isBeschreibungActionableForLoggedInBearbeiter(
        beschreibungsViewModel.getId());
  }

  boolean isOwnSperre() {
    if (sperre != null && sperre.getBearbeiter() != null) {
      return bearbeiterBoundary.getLoggedBearbeiter().equals(sperre.getBearbeiter());
    }

    return isEditable;
  }

  public void handleBeschreibungBearbeitenEvent() {
    logger.info("Getting Beschreibung Bearbeiten Event");

    beschreibungSperren();
  }

  public void handleBeschreibungLesenEvent() {
    logger.info("Getting Beschreibung Lesen Event");

    if (sperre == null || !isOwnSperre()) {
      return;
    }
    try {
      beschreibungsBoundary.beschreibungEntsperren(beschreibungsViewModel.getId());
      sperre = null;
      acquiredEditorSperreId = null;
      fillBeschreibungsViewModelValues(beschreibungsViewModel.getId());
    } catch (Exception e) {
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          getMessage("beschreibung_entsperren_fehler"),
          e.getMessage());
      logger.warn("Error releasing sperre for beschreibung {}", beschreibungsViewModel.getId(), e);
      PrimeFaces.current().dialog().showMessageDynamic(msg);
    }
  }

  public String getNormdatenURL() {
    try {
      return storkDiscovery.resolveUri(URI.create(normdatenURL))
          .toASCIIString();
    } catch (RuntimeException e) {
      logger.error("Error during resolving normdata url", e);
      return normdatenURL;
    }
  }

  public String getValidationURL() {

    // TODO Remove Workaround after Importservice is in DMZ

    StringBuilder validationURLResult = new StringBuilder();

    try {
      validationURLResult.append(
          storkDiscovery.resolveUri(URI.create(validationURL))
              .toASCIIString());
    } catch (RuntimeException e) {
      logger.error("Error during resolving validation url", e);
      validationURLResult.append(validationURL);

    } finally {
      validationURLResult.append("/tei-xml/validate");
    }

    return validationURLResult.toString();
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

    if (!beschreibungsBoundary.isBeschreibungActionableForLoggedInBearbeiter(
        beschreibungsViewModel.getId())) {
      return false;
    }
    return !beschreibungsViewModel.isPublished()
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
      sperre = beschreibungsBoundary.beschreibungSperrenForEdit(
          bearbeiterBoundary.getLoggedBearbeiter(),
          beschreibungsViewModel.getId());
      acquiredEditorSperreId = sperre.getId();
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
          .map(Sperre::getStartDatum)
          .map(I18NController::formatDate)
          .orElse(getMessage("beschreibung_unbekannt"));

      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          getMessage("beschreibung_bearbeiten_sperren_fehler"),
          getMessage("beschreibung_bearbeiten_sperren_fehler_detail", sperreBearbeiterName,
              sperreDatum));
      logger.info("Sperre already exists for Beschreibung {}", beschreibungsViewModel.getId(),
          saee);
      PrimeFaces.current().dialog().showMessageDynamic(msg);
    } catch (Exception e) {
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          getMessage("beschreibung_sperren_fehler"),
          e.getMessage());
      logger.warn("Error getting sperre for beschreibung {}", beschreibungsViewModel.getId(), e);
      PrimeFaces.current().dialog().showMessageDynamic(msg);
    }
  }

  public StreamedContent getTeiContent() {
    if (Objects.nonNull(this.beschreibungsViewModel.getTeiXML())
        && !this.beschreibungsViewModel.getTeiXML().isEmpty()) {
      byte[] content = this.beschreibungsViewModel.getTeiXML().getBytes(StandardCharsets.UTF_8);

      return DefaultStreamedContent.builder()
          .name(TeiSnapshotZipper.teiXmlFilename(this.beschreibungsViewModel.getId()))
          .contentType("text/xml")
          .contentLength((long) content.length)
          .stream(() -> new ByteArrayInputStream(content))
          .build();
    } else {
      logger.warn("Beschreibung contains no TEI-XML: {}", beschreibungsViewModel.getId());
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          getMessage("beschreibung_teixml_fehler"), "");
      PrimeFaces.current().dialog().showMessageDynamic(msg);
      return null;
    }
  }

  public StreamedContent getTeiSnapshotContent() {
    String beschreibungId = beschreibungsViewModel.getId();
    Optional<byte[]> zipBytesOpt = publikationsService.findTeiSnapshotZip(beschreibungId);
    if (zipBytesOpt.isEmpty()) {
      logger.warn("No TEI snapshot available for Beschreibung {}", beschreibungId);
      return failTeiSnapshotDownload();
    }
    byte[] zipBytes = zipBytesOpt.get();
    return DefaultStreamedContent.builder()
        .name(TeiSnapshotZipper.teiSnapshotZipFilename(beschreibungId))
        .contentType("application/zip")
        .contentLength((long) zipBytes.length)
        .stream(() -> new ByteArrayInputStream(zipBytes))
        .build();
  }

  private StreamedContent failTeiSnapshotDownload() {
    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
        getMessage("beschreibung_tei_fehler"), "");
    PrimeFaces.current().dialog().showMessageDynamic(msg);
    return null;
  }


  public String getOauth2Token() {

    if (authentication == null) {
      return "";
    }

    return authentication.getAccessToken();
  }

}
