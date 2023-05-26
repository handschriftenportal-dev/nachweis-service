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

package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Beschreibstoff;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Buchschmuck;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Handschriftentyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Musiknotation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Status;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Titel;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Umfang;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.08.2020.
 * @version 1.0
 */
public class KulturObjektDokumentViewModel implements Serializable {

  private static final long serialVersionUID = 3098529522399106936L;

  private String id;

  private String signatur;

  private List<String> alternativeSignaturen;

  private String teiXML;

  private String bestandhaltendeInstitutionName;

  private String bestandhaltendeInstitutionOrt;

  private Status status;

  private Titel titel;

  private Entstehungszeit entstehungszeit;

  private Entstehungsort entstehungsort;

  private Handschriftentyp handschriftentyp;

  private Beschreibstoff beschreibstoff;

  private Format format;

  private Umfang umfang;

  private Abmessung abmessung;

  private Buchschmuck buchschmuck;

  private Musiknotation musiknotation;

  private Grundsprache grundsprache;

  private List<DigitalisatViewModel> digitalisate;

  private List<BeschreibungsViewModel> beschreibungen;

  private String hspPurl;

  private List<String> externeLinks;

  public KulturObjektDokumentViewModel() {
  }

  public String getTeiXML() {
    return teiXML;
  }

  public void setTeiXML(String teiXML) {
    this.teiXML = teiXML;
  }

  public StreamedContent getTeiFile() {
    if (Objects.nonNull(this.teiXML) && !this.teiXML.isEmpty() && Objects.nonNull(this.id)) {
      return DefaultStreamedContent.builder()
          .name("tei-" + this.id + ".xml")
          .contentType("text/xml")
          .stream(() -> new ByteArrayInputStream(teiXML.getBytes(StandardCharsets.UTF_8)))
          .build();
    } else {
      return null;
    }
  }

  public String getBestandhaltendeInstitutionName() {
    return bestandhaltendeInstitutionName;
  }

  public void setBestandhaltendeInstitutionName(String bestandhaltendeInstitutionName) {
    this.bestandhaltendeInstitutionName = bestandhaltendeInstitutionName;
  }

  public String getBestandhaltendeInstitutionOrt() {
    return bestandhaltendeInstitutionOrt;
  }

  public void setBestandhaltendeInstitutionOrt(String bestandhaltendeInstitutionOrt) {
    this.bestandhaltendeInstitutionOrt = bestandhaltendeInstitutionOrt;
  }

  public List<DigitalisatViewModel> getDigitalisate() {
    return digitalisate;
  }

  public List<BeschreibungsViewModel> getBeschreibungen() {
    return beschreibungen;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof KulturObjektDokumentViewModel)) {
      return false;
    }
    KulturObjektDokumentViewModel that = (KulturObjektDokumentViewModel) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSignatur() {
    return signatur;
  }

  public void setSignatur(String signatur) {
    this.signatur = signatur;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Titel getTitel() {
    return titel;
  }

  public void setTitel(Titel titel) {
    this.titel = titel;
  }

  public Entstehungszeit getEntstehungszeit() {
    return entstehungszeit;
  }

  public void setEntstehungszeit(Entstehungszeit entstehungszeit) {
    this.entstehungszeit = entstehungszeit;
  }

  public Entstehungsort getEntstehungsort() {
    return entstehungsort;
  }

  public void setEntstehungsort(Entstehungsort entstehungsort) {
    this.entstehungsort = entstehungsort;
  }

  public Handschriftentyp getHandschriftentyp() {
    return handschriftentyp;
  }

  public void setHandschriftentyp(Handschriftentyp handschriftentyp) {
    this.handschriftentyp = handschriftentyp;
  }

  public Beschreibstoff getBeschreibstoff() {
    return beschreibstoff;
  }

  public void setBeschreibstoff(Beschreibstoff beschreibstoff) {
    this.beschreibstoff = beschreibstoff;
  }

  public Format getFormat() {
    return format;
  }

  public void setFormat(Format format) {
    this.format = format;
  }

  public Umfang getUmfang() {
    return umfang;
  }

  public void setUmfang(Umfang umfang) {
    this.umfang = umfang;
  }

  public Abmessung getAbmessung() {
    return abmessung;
  }

  public void setAbmessung(Abmessung abmessung) {
    this.abmessung = abmessung;
  }

  public Buchschmuck getBuchschmuck() {
    return buchschmuck;
  }

  public void setBuchschmuck(Buchschmuck buchschmuck) {
    this.buchschmuck = buchschmuck;
  }

  public Musiknotation getMusiknotation() {
    return musiknotation;
  }

  public void setMusiknotation(Musiknotation musiknotation) {
    this.musiknotation = musiknotation;
  }

  public Grundsprache getGrundsprache() {
    return grundsprache;
  }

  public void setGrundsprache(Grundsprache grundsprache) {
    this.grundsprache = grundsprache;
  }

  public List<String> getAlternativeSignaturen() {
    return alternativeSignaturen;
  }

  public void setAlternativeSignaturen(List<String> alternativeSignaturen) {
    this.alternativeSignaturen = alternativeSignaturen;
  }

  public String getHspPurl() {
    return hspPurl;
  }

  public void setHspPurl(String hspPurl) {
    this.hspPurl = hspPurl;
  }

  public List<String> getExterneLinks() {
    return externeLinks;
  }

  public void setExterneLinks(List<String> externeLinks) {
    this.externeLinks = externeLinks;
  }

  public static final class KulturObjektDokumentViewModelBuilder {

    private String id;
    private String signatur;
    private List<String> alternativeSignaturen = new ArrayList<>();
    private String teiXML;
    private String bestandhaltendeInstitutionName;
    private String bestandhaltendeInstitutionOrt;
    private Status status;
    private Titel titel;
    private Entstehungszeit entstehungszeit;
    private Entstehungsort entstehungsort;
    private Handschriftentyp handschriftentyp;
    private Beschreibstoff beschreibstoff;
    private Umfang umfang;
    private Format format;
    private Abmessung abmessung;
    private Buchschmuck buchschmuck;
    private Musiknotation musiknotation;
    private Grundsprache grundsprache;
    private List<DigitalisatViewModel> digitalisate = new ArrayList<>();
    private List<BeschreibungsViewModel> beschreibungen = new ArrayList<>();
    private String hspPurl;
    private List<String> externeLinks = new ArrayList<>();

    private KulturObjektDokumentViewModelBuilder() {
    }

    public static KulturObjektDokumentViewModelBuilder KulturObjektDokumentViewModel() {
      return new KulturObjektDokumentViewModelBuilder();
    }

    public KulturObjektDokumentViewModelBuilder withId(String id) {
      this.id = id;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withSignatur(String signatur) {
      this.signatur = signatur;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withAlternativeSignaturen(List<String> alternativeSignaturen) {
      this.alternativeSignaturen = alternativeSignaturen;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder addAlternativeSignatur(String alternativeSignatur) {
      this.alternativeSignaturen.add(alternativeSignatur);
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withTeiXML(String teiXML) {
      this.teiXML = teiXML;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withBestandhaltendeInstitutionName(
        String bestandhaltendeInstitutionName) {
      this.bestandhaltendeInstitutionName = bestandhaltendeInstitutionName;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withBestandhaltendeInstitutionOrt(
        String bestandhaltendeInstitutionOrt) {
      this.bestandhaltendeInstitutionOrt = bestandhaltendeInstitutionOrt;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withStatus(Status status) {
      this.status = status;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withTitel(Titel titel) {
      this.titel = titel;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withEntstehungszeit(Entstehungszeit entstehungszeit) {
      this.entstehungszeit = entstehungszeit;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withEntstehungsort(Entstehungsort entstehungsort) {
      this.entstehungsort = entstehungsort;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withHandschriftentyp(Handschriftentyp handschriftentyp) {
      this.handschriftentyp = handschriftentyp;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withBeschreibstoff(Beschreibstoff beschreibstoff) {
      this.beschreibstoff = beschreibstoff;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withAbmessung(Abmessung abmessung) {
      this.abmessung = abmessung;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withFormat(Format format) {
      this.format = format;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withUmfang(Umfang umfang) {
      this.umfang = umfang;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withBuchschmuck(Buchschmuck buchschmuck) {
      this.buchschmuck = buchschmuck;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withMusiknotation(Musiknotation musiknotation) {
      this.musiknotation = musiknotation;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withGrundSprache(Grundsprache grundSprache) {
      this.grundsprache = grundSprache;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder addDigitalisat(
        DigitalisatViewModel digitalisatViewModel) {
      this.digitalisate.add(digitalisatViewModel);
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withDigitalisats(
        List<DigitalisatViewModel> digitalisatViewModel) {
      this.digitalisate.addAll(digitalisatViewModel);
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withBeschreibungen(
        List<BeschreibungsViewModel> beschreibungenViewModel) {
      this.beschreibungen.addAll(beschreibungenViewModel);
      return this;
    }

    public KulturObjektDokumentViewModelBuilder addBeschreibung(BeschreibungsViewModel beschreibungenViewModel) {
      this.beschreibungen.add(beschreibungenViewModel);
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withHspPurl(String hspPurl) {
      this.hspPurl = hspPurl;
      return this;
    }

    public KulturObjektDokumentViewModelBuilder withExterneLinks(List<String> externeLinks) {
      this.externeLinks.clear();
      if (Objects.nonNull(externeLinks)) {
        this.externeLinks.addAll(externeLinks);
      }
      return this;
    }

    public KulturObjektDokumentViewModelBuilder addExternenLink(String externerLink) {
      this.externeLinks.add(externerLink);
      return this;
    }

    public KulturObjektDokumentViewModel build() {
      KulturObjektDokumentViewModel kulturObjektDokumentViewModel = new KulturObjektDokumentViewModel();
      kulturObjektDokumentViewModel.setId(id);
      kulturObjektDokumentViewModel.setSignatur(signatur);
      kulturObjektDokumentViewModel.setAlternativeSignaturen(alternativeSignaturen);
      kulturObjektDokumentViewModel.setTeiXML(teiXML);
      kulturObjektDokumentViewModel.setBestandhaltendeInstitutionName(bestandhaltendeInstitutionName);
      kulturObjektDokumentViewModel.setBestandhaltendeInstitutionOrt(bestandhaltendeInstitutionOrt);
      kulturObjektDokumentViewModel.setStatus(status);
      kulturObjektDokumentViewModel.setTitel(titel);
      kulturObjektDokumentViewModel.setEntstehungszeit(entstehungszeit);
      kulturObjektDokumentViewModel.setEntstehungsort(entstehungsort);
      kulturObjektDokumentViewModel.setHandschriftentyp(handschriftentyp);
      kulturObjektDokumentViewModel.setBeschreibstoff(beschreibstoff);
      kulturObjektDokumentViewModel.setAbmessung(abmessung);
      kulturObjektDokumentViewModel.setFormat(format);
      kulturObjektDokumentViewModel.setUmfang(umfang);
      kulturObjektDokumentViewModel.setBuchschmuck(buchschmuck);
      kulturObjektDokumentViewModel.setMusiknotation(musiknotation);
      kulturObjektDokumentViewModel.setGrundsprache(grundsprache);
      kulturObjektDokumentViewModel.digitalisate = digitalisate;
      kulturObjektDokumentViewModel.beschreibungen = beschreibungen;
      kulturObjektDokumentViewModel.hspPurl = hspPurl;
      kulturObjektDokumentViewModel.externeLinks = externeLinks;
      return kulturObjektDokumentViewModel;
    }
  }
}
