package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 29.05.2020.
 * @version 1.0
 */

@Named
@RequestScoped
public class GuideContentController {

  private final StreamedContent kodteivorlage;

  private final StreamedContent beschreibungteivorlage;

  private final StreamedContent ortteivorlage;

  private final StreamedContent koerperschaftteivorlage;

  private final StreamedContent beziehungenteivorlage;

  private final StreamedContent digitalisatteiVorlage;

  private final StreamedContent personteiVorlage;

  private final StreamedContent spracheteiVorlage;

  public GuideContentController() {

    this.kodteivorlage = DefaultStreamedContent.builder()
        .name("hsp_kodvorlage.xml")
        .contentType("text/xml")
        .stream(() -> FacesContext.getCurrentInstance().getExternalContext()
            .getResourceAsStream("/tei/kodinitial.xml"))
        .build();

    this.beschreibungteivorlage = DefaultStreamedContent.builder()
        .name("hsp_beschreibungvorlage.xml")
        .contentType("text/xml")
        .stream(() -> FacesContext.getCurrentInstance().getExternalContext()
            .getResourceAsStream("/tei/beschreibunginitial_medieval.xml"))
        .build();

    this.ortteivorlage = DefaultStreamedContent.builder()
        .name("hsp_ortvorlage.xml")
        .contentType("text/xml")
        .stream(() -> FacesContext.getCurrentInstance().getExternalContext()
            .getResourceAsStream("/tei/ortinitial.xml"))
        .build();

    this.koerperschaftteivorlage = DefaultStreamedContent.builder()
        .name("hsp_koerperschaftvorlage.xml")
        .contentType("text/xml")
        .stream(() -> FacesContext.getCurrentInstance().getExternalContext()
            .getResourceAsStream("/tei/koerperschaftinitial.xml"))
        .build();

    this.beziehungenteivorlage = DefaultStreamedContent.builder()
        .name("hsp_beziehungenvorlage.xml")
        .contentType("text/xml")
        .stream(() -> FacesContext.getCurrentInstance().getExternalContext()
            .getResourceAsStream("/tei/beziehungeninitial.xml"))
        .build();

    this.digitalisatteiVorlage = DefaultStreamedContent.builder()
        .name("hsp_kodsurrogatevorlage.xml")
        .contentType("text/xml")
        .stream(() -> FacesContext.getCurrentInstance().getExternalContext()
            .getResourceAsStream("/tei/kodsurrogatesinitial.xml"))
        .build();

    this.personteiVorlage = DefaultStreamedContent.builder()
        .name("hsp_personvorlage.xml")
        .contentType("text/xml")
        .stream(() -> FacesContext.getCurrentInstance().getExternalContext()
            .getResourceAsStream("/tei/personinitial.xml"))
        .build();

    this.spracheteiVorlage = DefaultStreamedContent.builder()
        .name("hsp_sprachevorlage.xml")
        .contentType("text/xml")
        .stream(() -> FacesContext.getCurrentInstance().getExternalContext()
            .getResourceAsStream("/tei/spracheinitial.xml"))
        .build();
  }

  public StreamedContent getKodteivorlage() {
    return kodteivorlage;
  }

  public StreamedContent getBeschreibungteivorlage() {
    return beschreibungteivorlage;
  }

  public StreamedContent getOrtteivorlage() {
    return ortteivorlage;
  }

  public StreamedContent getKoerperschaftteivorlage() {
    return koerperschaftteivorlage;
  }

  public StreamedContent getBeziehungenteivorlage() {
    return beziehungenteivorlage;
  }

  public StreamedContent getDigitalisatteiVorlage() {
    return digitalisatteiVorlage;
  }

  public StreamedContent getPersonteiVorlage() {
    return personteiVorlage;
  }

  public StreamedContent getSpracheteiVorlage() {
    return spracheteiVorlage;
  }
}
