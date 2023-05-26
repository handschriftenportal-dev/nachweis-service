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

package de.staatsbibliothek.berlin.hsp.domainmodel.aggregates;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BestandNachweisDTO;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Dokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Referenz;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * <div class="de">Documentation in Deutsch</div>
 * Alle bibliografischen Informationen zu einer mittelalterliche Handschrift
 *
 * <div class="en">Documentation in English</div>
 * All bibliographic information on a manuscript or other text-bearing object. This object is a main root aggregate in the context of domain driven design.
 * <p>
 *  * @author Konrad.Eichstaedt@sbb.spk-berlin.de
 *  * @author Piotr.Czarnecki@sbb.spk-berlin.de
 *  * @since 01.03.2017
 */
@SqlResultSetMapping(
    name = "BestandNachweisDTOMapping",
    classes = @ConstructorResult(
        targetClass = BestandNachweisDTO.class,
        columns = {
            @ColumnResult(name = "anzahlKOD", type = Long.class),
            @ColumnResult(name = "anzahlBeschreibungen", type = Long.class),
            @ColumnResult(name = "anzahlBeschreibungenIntern", type = Long.class),
            @ColumnResult(name = "anzahlBeschreibungenExtern", type = Long.class),
            @ColumnResult(name = "anzahlBeschreibungenKatalog", type = Long.class),
            @ColumnResult(name = "anzahlDigitalisat", type = Long.class),
            @ColumnResult(name = "anzahlKatalog", type = Long.class)}))
@Entity
@Table(name = "KulturObjektDokument")
@Cacheable
public class KulturObjektDokument implements Dokument, Serializable {

  public static final String ERROR_MSG_NOT_THE_PROPER_TYPE = "The Identifikation Objekt has not the proper type;";
  private static final long serialVersionUID = -6414050461052299818L;
  @Id
  String id;
  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "gueltigeIdentifikation_id")
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  Identifikation gueltigeIdentifikation;
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "alternativeIdentifikationen_id")
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  Set<Identifikation> alternativeIdentifikationen;
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "digitalisate_id")
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  Set<Digitalisat> digitalisate;
  @ElementCollection(fetch = FetchType.LAZY)
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn
  Set<String> beschreibungenIDs;
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "externeReferenzen_id")
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  Set<Referenz> externeReferenzen;
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "kulturobjektdokument_id")
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  Set<PURL> purls;
  @Column
  private String gndIdentifier;
  @Column
  private LocalDateTime registrierungsDatum;
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "komponenten_id")
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private Set<KulturObjektKomponente> komponenten;
  @Column(columnDefinition = "TEXT", length = 2097152)
  @Basic(fetch = FetchType.LAZY)
  private String teiXML;

  protected KulturObjektDokument() {
    registrierungsDatum = LocalDateTime.now();
    alternativeIdentifikationen = new LinkedHashSet<>();
    digitalisate = new LinkedHashSet<>();
    beschreibungenIDs = new LinkedHashSet<>();
    komponenten = new LinkedHashSet<>();
    externeReferenzen = new LinkedHashSet<>();
    purls = new LinkedHashSet<>();
  }

  public KulturObjektDokument(KulturObjektDokumentBuilder builder) {
    this.id = builder.id;
    this.gndIdentifier = builder.gndIdentifier;
    this.registrierungsDatum = builder.registrierungsDatum;
    this.gueltigeIdentifikation = builder.gueltigeIdentifikation;
    this.alternativeIdentifikationen = builder.alternativeIdentifikationen;
    this.digitalisate = builder.digitalisaten;
    this.beschreibungenIDs = builder.beschreibungenIDs;
    this.komponenten = builder.komponenten;
    this.teiXML = builder.teiXML;
    this.externeReferenzen = builder.externeReferenzen;
    this.purls = builder.purls;
  }

  /**
   * @return <code>String</code> The internal UUID generated by this module during the registration process.
   */

  public String getId() {
    return this.id;
  }

  /**
   * @param id as <code>String</code> the internal UUID generated by this module during the registration process.
   */

  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return <code>String</code> GND Identifier which is used within the Deutsche National Bibliothek System as
   * Metadata Identifier. <a href="https://portal.dnb.de/opac.htm?">DNB</a>
   */

  public String getGndIdentifier() {
    return gndIdentifier;
  }

  /**
   * @param gndIdentifier <code>String</code> GND Identifier which is used within the Deutsche National Bibliothek
   * System as Metadata Identifier. <a href="https://portal.dnb.de/opac.htm?">DNB</a>
   */

  public void setGndIdentifier(String gndIdentifier) {
    this.gndIdentifier = gndIdentifier;
  }

  /**
   * @return <code>Identifikation</code> Cultur Object Identifier which is used within a library to find the proper
   * cultur object
   */

  public Identifikation getGueltigeIdentifikation() {
    return gueltigeIdentifikation;
  }

  /**
   * @param gueltigeIdentifikation <code>Identifikation</code> Cultur Object Identifier which is used within a library
   * to find cultur object
   */
  public void setGueltigeIdentifikation(Identifikation gueltigeIdentifikation) {

    if (!IdentTyp.GUELTIGE_SIGNATUR.equals(gueltigeIdentifikation.getIdentTyp())) {
      throw new IllegalArgumentException(ERROR_MSG_NOT_THE_PROPER_TYPE);
    }

    this.gueltigeIdentifikation = gueltigeIdentifikation;
  }

  /**
   * @return <code>String</code> Cultur Object Signature which is used within a library to find the proper cultur object
   * within an magazin.
   */

  public String getGueltigeSignatur() {

    if (gueltigeIdentifikation != null && gueltigeIdentifikation.getIdent() != null) {
      return gueltigeIdentifikation.getIdent();
    }

    return "";
  }

  /**
   * @return <code>Set of Identifikation</code> All alternative and former cultur object identifikation which are used
   * within a library to find cultur object..
   */

  public Set<Identifikation> getAlternativeIdentifikationen() {
    return alternativeIdentifikationen;
  }

  /**
   * @param alternativeIdentifikationen <code>Identifikation</code> All alternative identifikation and former  cultur
   * object identifier which is used within a library to find cultur object
   */

  public void setAlternativeIdentifikationen(
      Set<Identifikation> alternativeIdentifikationen) {
    this.alternativeIdentifikationen = alternativeIdentifikationen;
  }

  /**
   * @return <code>LocalDateTime</code> Return the date of the registration within the Nachweis of this.
   * KulturObjektDokument
   */

  public LocalDateTime getRegistrierungsDatum() {
    return this.registrierungsDatum;
  }

  /**
   * @param registrierungsDatumDatum <code>LocalDateTime</code> Return the date of the registration within the Nachweis
   * of this KulturObjektDokument
   */

  public void setRegistrierungsDatum(LocalDateTime registrierungsDatumDatum) {
    this.registrierungsDatum = registrierungsDatumDatum;
  }


  /**
   * @return <code>KulturObjektKomponente</code> Return part of an cultur object .
   */

  public Set<KulturObjektKomponente> getKomponenten() {
    return komponenten;
  }

  /**
   * @param komponenten <code>KulturObjektKomponente</code> Set all components of an cultur object
   */

  public void setKomponenten(
      Set<KulturObjektKomponente> komponenten) {
    this.komponenten = komponenten;
  }

  /**
   * @return <code>Digitalisat</code> Return a digitalisation object as result of an digitalisation process.
   * Main use case is the view of an IIIF presentation for the cultur object.
   */
  public Set<Digitalisat> getDigitalisate() {
    return this.digitalisate;
  }

  /**
   * @param digitalisate <code>Digitalisat</code> Return a digitalisation object as result of an digitalisation process.
   * Main use case is the view of an IIIF presentation for the cultur object.
   */

  public void setDigitalisate(Set<Digitalisat> digitalisate) {
    this.digitalisate = digitalisate;
  }

  public void addDigitalisat(Digitalisat digitalisat) {
    digitalisate.remove(digitalisat);
  }

  public void removeDigitalisat(Digitalisat digitalisat) {
    digitalisate.remove(digitalisat);
  }

  /**
   * @return <code>Set</code> Return all id's of Beschreibungen (manual description) which are related to the cultur
   * object.
   */

  public Set<String> getBeschreibungenIDs() {
    return this.beschreibungenIDs;
  }

  /**
   * @param beschreibungenIDs <code>Set</code> Set all unique id's of Beschreibungen (manual description) which are
   * related to the cultur * object.
   */

  public void setBeschreibungenIDs(Set<String> beschreibungenIDs) {
    this.beschreibungenIDs = beschreibungenIDs;
  }

  public void addBeschreibungsdokument(String beschreibungID) {
    this.beschreibungenIDs.add(beschreibungID);
  }

  public void removeBeschreibungsdokument(String beschreibungID) {
    this.beschreibungenIDs.remove(beschreibungID);
  }

  public Set<PURL> getPURLs() {
    return this.purls;
  }

  public void setPURLs(Set<PURL> purls) {
    this.purls = purls;
  }

  public void addPURL(PURL purl) {
    purls.add(purl);
  }

  public void removePURL(PURL purl) {
    purls.remove(purl);
  }

  /**
   * @return <code>String</code> Return the TEI XML document of the cultur object.
   * object.
   */

  public String getTeiXML() {
    return teiXML;
  }

  /**
   * @param teiXML <code>String</code> Set the TEI XML document of an cultur object.
   */

  public void setTeiXML(String teiXML) {
    this.teiXML = teiXML;
  }

  /**
   * @param referenz <code>Referenz</code> Add external reference for this cultur object.
   */

  public void addExterneReferenz(Referenz referenz) {
    if (Objects.nonNull(referenz)) {
      this.externeReferenzen.add(referenz);
    }
  }

  /**
   * @param referenz <code>Referenz</code> Remove external reference for this cultur object.
   */

  public void removeExterneReferenz(Referenz referenz) {
    this.externeReferenzen.remove(referenz);
  }

  /**
   * @return <code>Set</code> Return all external URL and presentation references which regards to the culture object.
   */

  public Set<Referenz> getExterneReferenzen() {
    return externeReferenzen;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KulturObjektDokument that = (KulturObjektDokument) o;
    return Objects.equals(this.id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.id);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", KulturObjektDokument.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("gndIdentifier=" + gndIdentifier)
        .add("registrierungsDatum=" + registrierungsDatum)
        .add("gueltigeIdentifikation=" + gueltigeIdentifikation)
        .add("alternativeSignaturen=" + alternativeIdentifikationen.size())
        .add("digitalisaten=" + digitalisate.size())
        .add("beschreibungenIDs=" + beschreibungenIDs.size())
        .add("Komponenten=" + komponenten.size())
        .add("ExterneReferenzen=" + externeReferenzen.size())
        .add("purls=" + purls.size())
        .add("TEIXml=" + teiXML)
        .toString();
  }

  public static class KulturObjektDokumentBuilder {

    private String id;
    private String gndIdentifier;
    private LocalDateTime registrierungsDatum;
    private Identifikation gueltigeIdentifikation;
    private Set<Identifikation> alternativeIdentifikationen;
    private Set<Digitalisat> digitalisaten;
    private Set<String> beschreibungenIDs;
    private Set<KulturObjektKomponente> komponenten;
    private String teiXML = "";
    private Set<Referenz> externeReferenzen;
    private Set<PURL> purls;

    public KulturObjektDokumentBuilder() {
      registrierungsDatum = LocalDateTime.now();
      alternativeIdentifikationen = new LinkedHashSet<>();
      digitalisaten = new LinkedHashSet<>();
      beschreibungenIDs = new LinkedHashSet<>();
      komponenten = new LinkedHashSet<>();
      externeReferenzen = new LinkedHashSet<>();
      purls = new LinkedHashSet<>();
    }

    public KulturObjektDokumentBuilder(String id) {
      this.id = id;
      this.registrierungsDatum = LocalDateTime.now();
      alternativeIdentifikationen = new LinkedHashSet<>();
      digitalisaten = new LinkedHashSet<>();
      beschreibungenIDs = new LinkedHashSet<>();
      komponenten = new LinkedHashSet<>();
      externeReferenzen = new LinkedHashSet<>();
      purls = new LinkedHashSet<>();
    }

    public KulturObjektDokumentBuilder withId(String id) {
      this.id = id;
      return this;
    }

    public KulturObjektDokumentBuilder withGndIdentifier(String gndIdentifier) {
      this.gndIdentifier = gndIdentifier;
      return this;
    }

    public KulturObjektDokumentBuilder withRegistrierungsDatum(LocalDateTime registrierungsDatum) {
      this.registrierungsDatum = registrierungsDatum;
      return this;
    }

    public KulturObjektDokumentBuilder withGueltigerIdentifikation(Identifikation gueltigeIdentifikation) {
      if (!IdentTyp.GUELTIGE_SIGNATUR.equals(gueltigeIdentifikation.getIdentTyp())) {
        throw new IllegalArgumentException(ERROR_MSG_NOT_THE_PROPER_TYPE);
      }
      this.gueltigeIdentifikation = gueltigeIdentifikation;
      return this;
    }

    public KulturObjektDokumentBuilder addAlternativeIdentifikation(Identifikation alternativeIdentifikation) {

      if (IdentTyp.GUELTIGE_SIGNATUR.equals(alternativeIdentifikation.getIdentTyp())) {
        throw new IllegalArgumentException(ERROR_MSG_NOT_THE_PROPER_TYPE);
      }

      this.alternativeIdentifikationen.add(alternativeIdentifikation);
      return this;
    }

    public KulturObjektDokumentBuilder addAllAlternativeIdentifikationen(
        Set<Identifikation> alternativeIdentifikationen) {
      this.alternativeIdentifikationen.addAll(alternativeIdentifikationen);
      return this;
    }

    public KulturObjektDokumentBuilder addKomponente(KulturObjektKomponente kulturObjektKomponente) {
      this.komponenten.add(kulturObjektKomponente);
      return this;
    }

    public KulturObjektDokumentBuilder withDigitalisaten(Set<Digitalisat> digitalisaten) {
      this.digitalisaten = digitalisaten;
      return this;
    }

    public KulturObjektDokumentBuilder addDigitalisat(Digitalisat digitalisat) {
      this.digitalisaten.add(digitalisat);
      return this;
    }

    public KulturObjektDokumentBuilder withBeschreibungsdokumentIDs(Set<String> beschreibungIDs) {
      this.beschreibungenIDs = beschreibungIDs;
      return this;
    }

    public KulturObjektDokumentBuilder addBeschreibungsdokumentID(String beschreibungsIDs) {
      this.beschreibungenIDs.add(beschreibungsIDs);
      return this;
    }

    public KulturObjektDokumentBuilder withTEIXml(String teiXML) {
      this.teiXML = teiXML;
      return this;
    }

    public KulturObjektDokumentBuilder withExterneReferenzen(Set<Referenz> externeReferenzen) {
      this.externeReferenzen = externeReferenzen;
      return this;
    }

    public KulturObjektDokumentBuilder withPURLs(Set<PURL> purls) {
      this.purls = purls;
      return this;
    }

    public KulturObjektDokumentBuilder addPURL(PURL purl) {
      this.purls.add(purl);
      return this;
    }

    public KulturObjektDokument build() {
      return new KulturObjektDokument(this);
    }
  }
}
