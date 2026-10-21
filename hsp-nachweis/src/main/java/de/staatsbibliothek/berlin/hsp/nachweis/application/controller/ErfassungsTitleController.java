package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 29.05.2020.
 * @version 1.0
 * <p>
 * View Controller for getting for every page a indivual page title of the html document. WCAG2.0
 */

@Named
@RequestScoped
public class ErfassungsTitleController {

  private static final String APPLICATION_MESSAGES = "de.staatsbibliothek.berlin.hsp.application.messages";
  private static final Logger logger = LoggerFactory.getLogger(
      ErfassungsTitleController.class);
  private final Map<String, String> titleMap = new HashMap<>();
  private ResourceBundle bundle;

  private FacesContext context;

  private HttpServletRequest request;

  @PostConstruct
  public void setup() {
    titleMap.put("/barrierefreiheit.xhtml",
        getMessage("document_titel_barrierefreiheit"));
    titleMap.put("/bearbeiter.xhtml", getMessage("document_titel_bearbeiter"));
    titleMap.put("/beschreibung/beschreibungen.xhtml",
        getMessage("document_titel_beschreibungen"));
    titleMap.put("/beschreibung/beschreibung.xhtml",
        getMessage("document_titel_beschreibung"));
    titleMap.put("/dashboard.xhtml", getMessage("document_titel_dashboard"));
    titleMap.put("/dateiupload.xhtml",
        getMessage("document_titel_dateiupload"));
    titleMap.put("/datenuebernahme.xhtml",
        getMessage("document_titel_datenuebernahme"));
    titleMap.put("/importvorgaenge.xhtml",
        getMessage("document_titel_importvorgaenge"));
    titleMap.put("/importvorgang-detail-view.xhtml",
        getMessage("document_titel_importvorgaenge"));
    titleMap.put("/kataloge.xhtml", getMessage("document_titel_kataloge"));
    titleMap.put("/katalog-detail-view.xhtml",
        getMessage("document_titel_katalog"));
    titleMap.put("/kod-verwalten.xhtml",
        getMessage("document_titel_kodregistrieren"));
    titleMap.put("/kod-registrieren-ergebnis.xhtml",
        getMessage("document_titel_kodregistrieren"));
    titleMap.put("/koerperschaften.xhtml",
        getMessage("document_titel_koerperschaften"));
    titleMap.put("/koerperschaft-detail-view.xhtml",
        getMessage("document_titel_koerperschaft"));
    titleMap.put("/kulturobjekte.xhtml",
        getMessage("document_titel_kulturobjektdokumente"));
    titleMap.put("/kulturObjektDokument/kulturObjektDokument.xhtml",
        getMessage("document_titel_kulturobjektdokument"));
    titleMap.put("/kulturObjektDokument/digitalisat-anlegen.xhtml",
        getMessage("document_titel_digitalisat"));
    titleMap.put("/orte.xhtml", getMessage("document_titel_orte"));
    titleMap.put("/orte-detail-view.xhtml", getMessage("document_titel_ort"));
    titleMap.put("/papierkorb.xhtml", getMessage("document_titel_papierkorb"));
    titleMap.put("/person/personen.xhtml",
        getMessage("document_titel_personen"));
    titleMap.put("/person/person.xhtml", getMessage("document_titel_person"));
    titleMap.put("/schemapflege.xhtml", getMessage("document_titel_schema"));
    titleMap.put("/sperren.xhtml", getMessage("document_titel_sperren"));
    titleMap.put("/sprachen.xhtml", getMessage("document_titel_sprachen"));
    titleMap.put("/sprache-detail-view.xhtml",
        getMessage("document_titel_sprache"));
    titleMap.put("/suche.xhtml", getMessage("document_titel_suche"));
    titleMap.put("/purls.xhtml", getMessage("document_titel_purls"));
    titleMap.put("/werkzeuge.xhtml", getMessage("document_titel_werkzeuge"));
    titleMap.put("/bestand.xhtml", getMessage("bestand_title"));
    titleMap.put("/erfassungshilfe.xhtml", getMessage("erfassungshilfe_title"));
    titleMap.put("/gndservice.xhtml", getMessage("gndservice_page_title"));
    titleMap.put("/normdatenerkennung.xhtml", getMessage("normdatenerkennung_uebersicht_title"));
    titleMap.put("/normdatenerkennung-auftrag.xhtml", getMessage("normdatenerkennung_auftrag_title"));
  }

  public String getTitle() {
    if (titleMap.containsKey(request.getRequestURI())) {
      return titleMap.get(request.getRequestURI());
    } else {
      logger.warn("Could not get Page Title for {}", request.getRequestURI());
    }

    return "Handschriftenportal Erfassung";
  }

  Map<String, String> getTitleMap() {
    return titleMap;
  }

  String getMessage(String key) {

    if (context == null) {
      context = FacesContext.getCurrentInstance();
    }

    if (bundle == null) {
      bundle = ResourceBundle.getBundle(APPLICATION_MESSAGES, context.getViewRoot().getLocale());
    }

    if (bundle.containsKey(key)) {
      return bundle.getString(key);
    }

    logger.error("Could not get Page Title for {}", key);
    return "ERR-" + key + "-ERR";
  }

  void setContext(FacesContext context) {
    this.context = context;
  }

  void setBundle(ResourceBundle bundle) {
    this.bundle = bundle;
  }

  @Inject
  void setRequest(HttpServletRequest request) {
    this.request = request;
  }
}
