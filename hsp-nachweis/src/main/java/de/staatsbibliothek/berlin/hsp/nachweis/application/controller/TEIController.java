package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.DatenDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 12.05.2020.
 * @version 1.0
 */
@ViewScoped
@Named
public class TEIController {

  private static final Logger logger = LoggerFactory.getLogger(TEIController.class);

  static final String CALLBACK_PARAMS_KEY = "CALLBACK_PARAMS";

  public static final String DATEN_DOKUMENT_TYP = "DATEN_DOKUMENT_TYP";

  public static final String DATEN_DOKUMENT_ID = "DATEN_DOKUMENT_ID";

  BeschreibungsRepository beschreibungsRepository;

  KulturObjektDokumentRepository kulturObjektDokumentRepository;

  private String xml;

  private String id;

  @PostConstruct
  public void loadWithID() {
    FacesContext context = FacesContext.getCurrentInstance();
    Map<String, String> paramMap = (Map<String, String>) context.getAttributes().get(CALLBACK_PARAMS_KEY);
    String id = paramMap.get(DATEN_DOKUMENT_ID);
    String datenDokumentTyp = paramMap.get(DATEN_DOKUMENT_TYP);

    if (DatenDokumentTyp.KOD.toString().equals(datenDokumentTyp)) {
      logger.info("Loading TEI XML with KOD {}", id);

      Optional<KulturObjektDokument> optionalKulturObjektDokument = kulturObjektDokumentRepository.findByIdOptional(id);
      if (optionalKulturObjektDokument.isPresent()) {
        KulturObjektDokument kod = optionalKulturObjektDokument.get();
        loadTEIDocument(kod.getTeiXML(), kod.getId());
      }
    }

    if (DatenDokumentTyp.BESCHREIBUNG.toString().equals(datenDokumentTyp)) {
      logger.info("Loading TEI XML with Beschreibungen {} ", id);

      Optional<Beschreibung> optionalBeschreibung = beschreibungsRepository.findByIdOptional(id);
      if (optionalBeschreibung.isPresent()) {
        Beschreibung beschreibung = optionalBeschreibung.get();
        loadTEIDocument(beschreibung.getTeiXML(), beschreibung.getId());
      }
    }
  }

  private void loadTEIDocument(String teiXML, String id) {
    this.xml = teiXML;
    this.id = id;
  }

  public String getXml() {
    return xml;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Inject
  public void setBeschreibungsRepository(BeschreibungsRepository beschreibungsRepository) {
    this.beschreibungsRepository = beschreibungsRepository;
  }

  @Inject
  public void setKulturObjektDokumentRepository(KulturObjektDokumentRepository kulturObjektDokumentRepository) {
    this.kulturObjektDokumentRepository = kulturObjektDokumentRepository;
  }
}
