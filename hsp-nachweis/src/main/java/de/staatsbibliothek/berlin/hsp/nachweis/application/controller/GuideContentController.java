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

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
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
