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

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 11.06.2019.
 */
public class BeschreibungsViewModel implements Serializable {

  private static final long serialVersionUID = -8499789047921037458L;
  private String importJobID;
  private String id;
  private String titel;
  private String signatur;
  private String externeID;
  private String preconditionResult;
  private boolean preconditionState = false;
  private String teiXML;
  private boolean selected;
  private String kodID;
  private String kodSignatur;
  private String bestandhaltendeInstitutionName;
  private String bestandhaltendeInstitutionOrt;
  private List<Publikation> publikationen;
  private List<NormdatenReferenz> beteiligte;
  private VerwaltungsTyp verwaltungsTyp;
  private LocalDateTime erstellungsDatum;
  private LocalDateTime aenderungsDatum;
  private NormdatenReferenz beschreibungsSprache;
  private LizenzViewModel lizenz;
  private List<BeschreibungsAutorView> autoren;
  private boolean ausGedrucktemKatalog;
  private String hspPurl;
  private List<String> externeLinks;
  private LocalDateTime datumErstpublikation;

  public BeschreibungsViewModel() {
  }

  public BeschreibungsViewModel(BeschreibungsViewBuilder beschreibungsViewBuilder) {
    this.importJobID = beschreibungsViewBuilder.importJobID;
    this.id = beschreibungsViewBuilder.id;
    this.titel = beschreibungsViewBuilder.titel;
    this.signatur = beschreibungsViewBuilder.signatur;
    this.externeID = beschreibungsViewBuilder.externeID;
    this.preconditionResult = beschreibungsViewBuilder.kodView;
    this.teiXML = beschreibungsViewBuilder.teiXML;
    this.bestandhaltendeInstitutionName = beschreibungsViewBuilder.bestandhaltendeInstitutionName;
    this.bestandhaltendeInstitutionOrt = beschreibungsViewBuilder.bestandhaltendeInstitutionOrt;
    this.publikationen = beschreibungsViewBuilder.publikationen;
    this.beteiligte = beschreibungsViewBuilder.beteiligte;
    this.verwaltungsTyp = beschreibungsViewBuilder.verwaltungsTyp;
    this.kodID = beschreibungsViewBuilder.kodID;
    this.kodSignatur = beschreibungsViewBuilder.kodSignatur;
    this.aenderungsDatum = beschreibungsViewBuilder.aenderungsDatum;
    this.erstellungsDatum = beschreibungsViewBuilder.erstellungsDatum;
    this.beschreibungsSprache = beschreibungsViewBuilder.beschreibungsSprache;
    this.lizenz = beschreibungsViewBuilder.lizenz;
    this.autoren = beschreibungsViewBuilder.autoren;
    this.ausGedrucktemKatalog = beschreibungsViewBuilder.ausGedrucktemKatalog;
    this.hspPurl = beschreibungsViewBuilder.hspPurl;
    this.externeLinks = beschreibungsViewBuilder.externeLinks;
    this.datumErstpublikation = beschreibungsViewBuilder.datumErstpublikation;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitel() {
    return titel;
  }

  public void setTitel(String titel) {
    this.titel = titel;
  }

  public String getSignatur() {
    return signatur;
  }

  public void setSignatur(String signatur) {
    this.signatur = signatur;
  }

  public String getImportJobID() {
    return importJobID;
  }

  public void setImportJobID(String importJobID) {
    this.importJobID = importJobID;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public String getExterneID() {
    return externeID;
  }

  public String getPreconditionResult() {
    return preconditionResult;
  }

  public void setPreconditionResult(String preconditionResult) {
    this.preconditionResult = preconditionResult;
  }

  public String getTeiXML() {
    return teiXML;
  }

  public String getKodSignatur() {
    return kodSignatur;
  }

  public String getKodID() {
    return kodID;
  }

  public void setKodID(String kodID) {
    this.kodID = kodID;
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

  public List<Publikation> getPublikationen() {
    return publikationen;
  }

  public void setPublikationen(List<Publikation> publikationen) {
    this.publikationen = publikationen;
  }

  public List<NormdatenReferenz> getBeteiligte() {
    return beteiligte;
  }

  public void setBeteiligte(List<NormdatenReferenz> beteiligte) {
    this.beteiligte = beteiligte;
  }

  public boolean isPreconditionState() {
    return preconditionState;
  }

  public void setPreconditionState(boolean preconditionState) {
    this.preconditionState = preconditionState;
  }

  public VerwaltungsTyp getVerwaltungsTyp() {
    return verwaltungsTyp;
  }

  public LocalDateTime getAenderungsDatum() {
    return aenderungsDatum;
  }

  public void setAenderungsDatum(LocalDateTime aenderungsDatum) {
    this.aenderungsDatum = aenderungsDatum;
  }

  public LocalDateTime getErstellungsDatum() {
    return erstellungsDatum;
  }

  public void setErstellungsDatum(LocalDateTime erstellungsDatum) {
    this.erstellungsDatum = erstellungsDatum;
  }

  public NormdatenReferenz getBeschreibungsSprache() {
    return beschreibungsSprache;
  }

  public void setBeschreibungsSprache(NormdatenReferenz beschreibungsSprache) {
    this.beschreibungsSprache = beschreibungsSprache;
  }

  public LizenzViewModel getLizenz() {
    return lizenz;
  }

  public void setLizenz(LizenzViewModel lizenz) {
    this.lizenz = lizenz;
  }

  public List<BeschreibungsAutorView> getAutoren() {
    return autoren;
  }

  public String getFormattedAutoren() {
    return Optional.ofNullable(autoren)
        .stream()
        .flatMap(Collection::stream)
        .map(BeschreibungsAutorView::getPreferredName)
        .collect(Collectors.joining("; "));
  }

  public boolean getAusGedrucktemKatalog() {
    return ausGedrucktemKatalog;
  }

  public String getHspPurl() {
    return hspPurl;
  }

  public List<String> getExterneLinks() {
    return externeLinks;
  }

  public LocalDateTime getDatumErstpublikation() {
    return datumErstpublikation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BeschreibungsViewModel)) {
      return false;
    }
    BeschreibungsViewModel that = (BeschreibungsViewModel) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "BeschreibungsViewModel{" +
        "importJobID='" + importJobID + '\'' +
        ", id='" + id + '\'' +
        ", titel='" + titel + '\'' +
        ", signatur='" + signatur + '\'' +
        ", externeID='" + externeID + '\'' +
        ", kodView='" + preconditionResult + '\'' +
        ", kodID='" + kodID + '\'' +
        ", selected=" + selected +
        ", kodGefunden=" + preconditionState +
        ", bestandhaltendeInstitutionName='" + bestandhaltendeInstitutionName + '\'' +
        ", bestandhaltendeInstitutionOrt='" + bestandhaltendeInstitutionOrt + '\'' +
        ", publikationen='" + ((publikationen == null) ? "" + '\''
        : Arrays.toString(publikationen.toArray()) + '\'') +
        ", beteiligte='" + ((beteiligte == null) ? "" + '\''
        : Arrays.toString(beteiligte.toArray()) + '\'') +
        ", verwaltuntsTyp='" + verwaltungsTyp + '\'' +
        ", anderungsDatum='" + aenderungsDatum + '\'' +
        ", erstellungsDatum='" + erstellungsDatum + '\'' +
        ", beschreibungsSprache='" + beschreibungsSprache + '\'' +
        ", lizenz='" + lizenz + '\'' +
        ", autoren='" + autoren + '\'' +
        ", hspPurl='" + hspPurl + '\'' +
        ", externeLinks='" + externeLinks + '\'' +
        ", datumErstpublikation='" + datumErstpublikation + '\'' +
        '}';
  }

  public static class BeschreibungsViewBuilder {

    private final List<BeschreibungsAutorView> autoren = new ArrayList<>();
    private final List<String> externeLinks = new ArrayList<>();
    private String importJobID;
    private String id;
    private String titel;
    private String signatur;
    private String externeID;
    private String kodView;
    private String teiXML;
    private String bestandhaltendeInstitutionName;
    private String bestandhaltendeInstitutionOrt;
    private List<Publikation> publikationen;
    private List<NormdatenReferenz> beteiligte;
    private VerwaltungsTyp verwaltungsTyp;
    private String kodSignatur;
    private String kodID;
    private LocalDateTime aenderungsDatum;
    private LocalDateTime erstellungsDatum;
    private NormdatenReferenz beschreibungsSprache;
    private LizenzViewModel lizenz;
    private boolean ausGedrucktemKatalog;
    private String hspPurl;
    private LocalDateTime datumErstpublikation;

    public BeschreibungsViewBuilder withID(String id) {

      this.id = id;
      return this;
    }

    public BeschreibungsViewBuilder withImportJobID(String importJobID) {

      this.importJobID = importJobID;
      return this;
    }

    public BeschreibungsViewBuilder withTitel(String titel) {

      this.titel = titel;
      return this;
    }

    public BeschreibungsViewBuilder withSignatur(String signatur) {

      this.signatur = signatur;
      return this;
    }

    public BeschreibungsViewBuilder withExterneID(String externeID) {

      this.externeID = externeID;
      return this;
    }

    public BeschreibungsViewBuilder withKODView(String kodView) {

      this.kodView = kodView;
      return this;
    }

    public BeschreibungsViewBuilder withTEIXML(String teiXML) {
      this.teiXML = teiXML;
      return this;
    }

    public BeschreibungsViewBuilder withPublikationen(List<Publikation> publikationen) {
      this.publikationen = publikationen;
      return this;
    }

    public BeschreibungsViewBuilder withBeteiligte(List<NormdatenReferenz> beteiligte) {
      this.beteiligte = beteiligte;
      return this;
    }

    public BeschreibungsViewBuilder withBestandhaltendeInstitutionName(
        String bestandhaltendeInstitutionName) {
      this.bestandhaltendeInstitutionName = bestandhaltendeInstitutionName;
      return this;
    }

    public BeschreibungsViewBuilder withBestandhaltendeInstitutionOrt(
        String bestandhaltendeInstitutionOrt) {
      this.bestandhaltendeInstitutionOrt = bestandhaltendeInstitutionOrt;
      return this;
    }

    public BeschreibungsViewBuilder withVerwaltungsTyp(VerwaltungsTyp verwaltungsTyp) {
      this.verwaltungsTyp = verwaltungsTyp;
      return this;
    }

    public BeschreibungsViewBuilder withKODID(String kodID) {
      this.kodID = kodID;
      return this;
    }

    public BeschreibungsViewBuilder withKODSignatur(String signatur) {
      this.kodSignatur = signatur;
      return this;
    }

    public BeschreibungsViewBuilder withAenderungsDatum(LocalDateTime aenderungsDatum) {
      this.aenderungsDatum = aenderungsDatum;
      return this;
    }

    public BeschreibungsViewBuilder withErstellungsDatum(LocalDateTime erstellungsDatum) {
      this.erstellungsDatum = erstellungsDatum;
      return this;
    }

    public BeschreibungsViewBuilder withBeschreibungsSprache(
        NormdatenReferenz beschreibungsSprache) {
      this.beschreibungsSprache = beschreibungsSprache;
      return this;
    }

    public BeschreibungsViewBuilder withLizenz(LizenzViewModel lizenz) {
      this.lizenz = lizenz;
      return this;
    }

    public BeschreibungsViewBuilder withAutoren(List<BeschreibungsAutorView> autoren) {
      this.autoren.addAll(autoren);
      return this;
    }

    public BeschreibungsViewBuilder withGedrucktemKatalog(boolean ausGedrucktemKatalog) {
      this.ausGedrucktemKatalog = ausGedrucktemKatalog;
      return this;
    }

    public BeschreibungsViewBuilder withHspPurl(String hspPurl) {
      this.hspPurl = hspPurl;
      return this;
    }

    public BeschreibungsViewBuilder withExterneLinks(List<String> externeLinks) {
      this.externeLinks.clear();
      if (Objects.nonNull(externeLinks)) {
        this.externeLinks.addAll(externeLinks);
      }
      return this;
    }

    public BeschreibungsViewBuilder addExternenLink(String externerLink) {
      this.externeLinks.add(externerLink);
      return this;
    }

    public BeschreibungsViewBuilder withDatumErstpublikation(LocalDateTime datumErstpublikation) {
      this.datumErstpublikation = datumErstpublikation;
      return this;
    }

    public BeschreibungsViewModel build() {
      return new BeschreibungsViewModel(this);
    }

  }
}
