package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController.getMessage;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 04.05.2021.
 * @version 1.0
 */

@ViewScoped
@Named
@LoginCheck
@Slf4j
public class BeschreibungErstellenController implements Serializable {

  @Serial
  private static final long serialVersionUID = -3389228600145101726L;

  private static final String PARAM_KODID = "kodid";

  private final transient NormdatenReferenzBoundary normdatenReferenzBoundary;
  private final transient KulturObjektDokumentBoundary kulturObjektDokumentBoundary;
  private final transient BeschreibungsBoundary beschreibungsBoundary;
  private final transient BearbeiterBoundary bearbeiterBoundary;
  @Getter
  private final transient Set<NormdatenReferenz> selectedAutoren = new LinkedHashSet<>();

  @Setter
  @Getter
  private String beschreibungsSprache;
  @Setter
  @Getter
  private String kodID;
  @Getter
  private transient Map<String, String> beschreibungsSprachen = new TreeMap<>();
  @Getter
  private transient Map<String, NormdatenReferenz> beschreibungsSprachenNormdatenReferenzMap = new HashMap<>();

  @Inject
  public BeschreibungErstellenController(
      NormdatenReferenzBoundary normdatenReferenzBoundary,
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary,
      BeschreibungsBoundary beschreibungsBoundary,
      BearbeiterBoundary bearbeiterBoundary) {
    this.normdatenReferenzBoundary = normdatenReferenzBoundary;
    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
    this.beschreibungsBoundary = beschreibungsBoundary;
    this.bearbeiterBoundary = bearbeiterBoundary;
  }

  @PostConstruct
  public void setup() {
    ExternalContext externalContext = null;
    if (Objects.nonNull(FacesContext.getCurrentInstance())) {
      externalContext = FacesContext.getCurrentInstance().getExternalContext();
    }
    if (Objects.nonNull(externalContext) && Objects
        .nonNull(externalContext.getRequestParameterMap().get(PARAM_KODID))
        && !externalContext.getRequestParameterMap().get(PARAM_KODID).isEmpty()) {

      kodID = externalContext.getRequestParameterMap().get(PARAM_KODID);
    }

    loadBeschreibungsSprachen();
  }

  public NormdatenReferenz getSelectedAutor() {
    return null;
  }

  public void setSelectedAutor(NormdatenReferenz selectedAutor) {
    if (selectedAutor != null) {
      this.selectedAutoren.add(selectedAutor);
    }
  }

  public void autorEntfernen(NormdatenReferenz autor) {
    log.info("Remove selected Autor from List {} ", autor);

    if (autor != null) {
      selectedAutoren.remove(autor);
    }
  }

  public String getKodSignatur(String kodid) {
    String kodSignature = "";
    Optional<KulturObjektDokument> kulturObjektDokumentOptional = kulturObjektDokumentBoundary.findById(
        kodid);
    if (kulturObjektDokumentOptional.isPresent()) {
      KulturObjektDokument kulturObjektDokument = kulturObjektDokumentOptional.get();
      kodSignature = kulturObjektDokument.getGueltigeSignatur();
    }
    return kodSignature;
  }

  public String getKodBesitzer(String kodid) {
    String kodBesitzer = "";
    Optional<KulturObjektDokument> kulturObjektDokumentOptional = kulturObjektDokumentBoundary.findById(
        kodid);
    if (kulturObjektDokumentOptional.isPresent()) {
      KulturObjektDokument kulturObjektDokument = kulturObjektDokumentOptional.get();

      if (kulturObjektDokument.getGueltigeIdentifikation() != null
          && kulturObjektDokument.getGueltigeIdentifikation().getBesitzer() != null) {
        kodBesitzer = kulturObjektDokument.getGueltigeIdentifikation().getBesitzer().getName();
      }
    }
    return kodBesitzer;
  }

  public String getKodAufbewahrungsOrt(String kodid) {
    String kodAufbewahrungsOrt = "";
    Optional<KulturObjektDokument> kulturObjektDokumentOptional = kulturObjektDokumentBoundary.findById(
        kodid);
    if (kulturObjektDokumentOptional.isPresent()) {
      KulturObjektDokument kulturObjektDokument = kulturObjektDokumentOptional.get();
      if (kulturObjektDokument.getGueltigeIdentifikation() != null
          && kulturObjektDokument.getGueltigeIdentifikation().getAufbewahrungsOrt() != null) {
        kodAufbewahrungsOrt = kulturObjektDokument.getGueltigeIdentifikation().getAufbewahrungsOrt()
            .getName();
      }
    }
    return kodAufbewahrungsOrt;
  }

  public void dialogSchliessen(String beschreibungId) {
    beschreibungsSprache = null;
    if (FacesContext.getCurrentInstance() != null) {
      PrimeFaces.current().dialog().closeDynamic(beschreibungId);
    }
  }

  public void beschreibungAnlegen() {
    log.info("Beschreibung wird angelegt ... {} ", kodID);
    try {
      Set<NormdatenReferenz> autorenAllFields = selectedAutoren.stream()
          .map(autor -> normdatenReferenzBoundary.findOneByIdOrNameAndType(autor.getId(),
              autor.getTypeName()))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toSet());

      if (autorenAllFields.size() != selectedAutoren.size()) {
        throw new Exception(getMessage("beschreibung_erstellen_autoren_fehler"));
      }

      Beschreibung beschreibung = beschreibungsBoundary
          .beschreibungErstellen(bearbeiterBoundary.getLoggedBearbeiter(), kodID,
              beschreibungsSprachenNormdatenReferenzMap.get(beschreibungsSprache),
              autorenAllFields, kulturObjektDokumentBoundary);
      dialogSchliessen(beschreibung.getId());

    } catch (Exception e) {
      log.error("Error in beschreibungAnlegen for kodID=" + kodID, e);
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          getMessage("beschreibung_erstellen_anlegen_fehler"),
          e.getMessage());
      PrimeFaces.current().dialog().showMessageDynamic(msg);
    }
  }

  void loadBeschreibungsSprachen() {
    Set<NormdatenReferenz> sprachen = null;

    try {
      sprachen = normdatenReferenzBoundary.findAllByIdOrNameAndType(
          null, NormdatenReferenz.SPRACHE_TYPE_NAME, true);
    } catch (Exception e) {
      log.error("Fehler beim Laden der Sprachen", e);
    }

    if (Objects.isNull(sprachen) || sprachen.isEmpty()) {
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          getMessage("beschreibung_erstellen_initalisieren_fehler"), "");
      PrimeFaces.current().dialog().showMessageDynamic(msg);
      return;
    }

    beschreibungsSprachenNormdatenReferenzMap = sprachen.stream()
        .collect(Collectors.toMap(NormdatenReferenz::getId, g -> g));

    beschreibungsSprachen = sprachen.stream()
        .collect(Collectors.toMap(NormdatenReferenz::getName, NormdatenReferenz::getId,
            (e1, e2) -> e1, TreeMap::new));
  }

}
