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

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz.LizenzBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.LizenzTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzBoundary;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
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

  private static final long serialVersionUID = -3389228600145101726L;

  private static final String PARAM_KODID = "kodid";

  private final transient NormdatenReferenzBoundary normdatenReferenzBoundary;
  private final transient KulturObjektDokumentBoundary kulturObjektDokumentBoundary;
  private final transient BeschreibungsBoundary beschreibungsBoundary;
  private final transient BearbeiterBoundary bearbeiterBoundary;
  private final transient Set<NormdatenReferenz> selectedAutoren = new LinkedHashSet<>();
  private final transient Map<String, String> templates = new HashMap<>();

  private String beschreibungsSprache;
  private LizenzTyp lizenzTyp;
  private String template;
  private String kodID;
  private transient Map<String, String> beschreibungsSprachen = new TreeMap<>();
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

    templates.put(getMessage("beschreibung_erstellen_text_Handschrift"),
        TEIValues.HSP_BESCHREIBUNG_MITTELALTERLICHE_HS);
    templates.put(getMessage("beschreibung_erstellen_illuminierte_Handschrift"),
        TEIValues.HSP_BESCHREIBUNG_ILLUMINIERTE_HS);
  }

  public NormdatenReferenz getSelectedAutor() {
    return null;
  }

  public void setSelectedAutor(NormdatenReferenz selectedAutor) {
    if (selectedAutor != null) {
      this.selectedAutoren.add(selectedAutor);
    }
  }

  public boolean templateIsActive(String value) {
    return value.equals(TEIValues.HSP_BESCHREIBUNG_ILLUMINIERTE_HS);
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
    template = null;
    if (FacesContext.getCurrentInstance() != null) {
      PrimeFaces.current().dialog().closeDynamic(beschreibungId);
    }
  }

  public void beschreibungAnlegen() {
    log.info("Beschreibung wird angelegt ... {} ", kodID);
    try {
      Lizenz lizenz = Optional.ofNullable(lizenzTyp)
          .map(l -> new LizenzBuilder()
              .addUri(lizenzTyp.getUrl())
              .withBeschreibungsText(lizenzTyp.getBeschreibung())
              .build())
          .orElse(null);

      Set<NormdatenReferenz> autorenAllFields = selectedAutoren.stream()
          .map(autor -> normdatenReferenzBoundary.findOneByIdOrNameAndType(autor.getId(), autor.getTypeName()))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toSet());

      if (autorenAllFields.size() != selectedAutoren.size()) {
        throw new Exception(getMessage("beschreibung_erstellen_autoren_fehler"));
      }

      Beschreibung beschreibung = beschreibungsBoundary
          .beschreibungErstellen(bearbeiterBoundary.getLoggedBearbeiter(), kodID,
              beschreibungsSprachenNormdatenReferenzMap.get(beschreibungsSprache),
              autorenAllFields, lizenz, template, kulturObjektDokumentBoundary);
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
    Set<NormdatenReferenz> sprachen = normdatenReferenzBoundary
        .findAllByIdOrNameAndType(null, NormdatenReferenz.SPRACHE_TYPE_NAME, true);

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

  public Set<NormdatenReferenz> getSelectedAutoren() {
    return selectedAutoren;
  }

  public Map<String, String> getBeschreibungsSprachen() {
    return beschreibungsSprachen;
  }

  public String getBeschreibungsSprache() {
    return beschreibungsSprache;
  }

  public void setBeschreibungsSprache(String beschreibungsSprache) {
    this.beschreibungsSprache = beschreibungsSprache;
  }

  public Map<String, String> getTemplates() {
    return templates;
  }

  public String getTemplate() {
    return template;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  public String getKodID() {
    return kodID;
  }

  public void setKodID(String kodID) {
    this.kodID = kodID;
  }

  public Map<String, NormdatenReferenz> getBeschreibungsSprachenNormdatenReferenzMap() {
    return beschreibungsSprachenNormdatenReferenzMap;
  }

  public LizenzTyp getLizenzTyp() {
    return lizenzTyp;
  }

  public void setLizenzTyp(LizenzTyp lizenzTyp) {
    this.lizenzTyp = lizenzTyp;
  }

  public LizenzTyp[] getLizenzTypen() {
    return LizenzTyp.values();
  }

}
