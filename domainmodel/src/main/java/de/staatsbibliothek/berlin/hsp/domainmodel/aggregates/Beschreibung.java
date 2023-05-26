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

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.GlobaleBeschreibungsKomponente;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsKomponentenTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Dokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Referenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Dieses Objekt repräsentiert eine durch einen Erschließer erstellte Handschriftenbeschreibung. Eine
 * Handschriftenbeschreibung stellt Informationen zu einer Handschrift dar, welche durch das KulturObjektDokument
 * repräsentiert wird.
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 06.06.19
 */
@Entity
@Table(name = "beschreibungsdokument")
@Cacheable
public class Beschreibung implements Dokument {

  @Id
  private String id;
  @Column
  private String kodID;
  @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.REFRESH,
      CascadeType.PERSIST,
      CascadeType.MERGE})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private Set<Referenz> externeIDs;
  @ElementCollection(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn
  private Set<String> altIdentifier;
  @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.REFRESH,
      CascadeType.PERSIST,
      CascadeType.MERGE})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private NormdatenReferenz beschreibungsSprache;
  @Column
  private LocalDateTime erstellungsDatum;
  @Column
  private LocalDateTime aenderungsDatum;
  @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.REFRESH,
      CascadeType.PERSIST,
      CascadeType.MERGE})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private Set<GlobaleBeschreibungsKomponente> beschreibungsStruktur;
  @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.REFRESH,
      CascadeType.PERSIST,
      CascadeType.MERGE})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private Set<NormdatenReferenz> beschreibungsBeteiligte;
  @Column(name = "teiXML", columnDefinition = "TEXT", length = 2097152)
  @Basic(fetch = FetchType.LAZY)
  private String teiXML;
  @Column
  private String katalogID;
  @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.REFRESH,
      CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private Set<Publikation> publikationen;
  @Enumerated(EnumType.STRING)
  @Column(name = "verwaltungstyp", length = 25)
  private VerwaltungsTyp verwaltungsTyp;
  @Enumerated(EnumType.STRING)
  @Column(name = "beschreibungstyp", length = 25)
  private BeschreibungsTyp beschreibungsTyp;
  @Enumerated(EnumType.STRING)
  @Column(name = "dokumentObjektTyp", length = 25)
  private DokumentObjektTyp dokumentObjektTyp;
  @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.REFRESH,
      CascadeType.PERSIST, CascadeType.MERGE})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private Lizenz lizenz;
  @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.REFRESH,
      CascadeType.PERSIST,
      CascadeType.MERGE})
  @JoinTable(name = "beschreibungsdokument_autoren")
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private Set<NormdatenReferenz> autoren;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "beschreibungsdokument_id")
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private Set<PURL> purls;

  protected Beschreibung() {
    externeIDs = new LinkedHashSet<>();
    beschreibungsBeteiligte = new LinkedHashSet<>();
    beschreibungsStruktur = new LinkedHashSet<>();
    publikationen = new LinkedHashSet<>();
    altIdentifier = new LinkedHashSet<>();
    autoren = new LinkedHashSet<>();
    purls = new LinkedHashSet<>();
  }

  public Beschreibung(BeschreibungsBuilder builder) {

    this.id = builder.id;
    this.kodID = builder.kodID;
    this.teiXML = builder.teiXML;
    this.externeIDs = builder.externeIDs;
    this.beschreibungsSprache = builder.beschreibungsSprache;
    this.erstellungsDatum = builder.erstellungsDatum;
    this.aenderungsDatum = builder.aenderungsDatum;
    this.beschreibungsBeteiligte = builder.beschreibungsBeteiligte;
    this.beschreibungsStruktur = builder.beschreibungsStruktur;
    this.katalogID = builder.katalogID;
    this.publikationen = builder.publikationen;
    this.altIdentifier = builder.altIdentifier;
    this.verwaltungsTyp = builder.verwaltungsTyp;
    this.beschreibungsTyp = builder.beschreibungsTyp;
    this.lizenz = builder.lizenz;
    this.autoren = builder.autoren;
    this.dokumentObjektTyp = builder.dokumentObjektTyp;
    this.purls = builder.purls;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getKodID() {
    return kodID;
  }

  public void setKodID(String kod) {
    this.kodID = kod;
  }

  public Set<Referenz> getExterneIDs() {
    return externeIDs;
  }

  public void setExterneIDs(Set<Referenz> externeIDs) {
    this.externeIDs = externeIDs;
  }

  public NormdatenReferenz getBeschreibungsSprache() {
    return beschreibungsSprache;
  }

  public void setBeschreibungsSprache(
      NormdatenReferenz beschreibungsSprache) {
    this.beschreibungsSprache = beschreibungsSprache;
  }

  public LocalDateTime getErstellungsDatum() {
    return erstellungsDatum;
  }

  public void setErstellungsDatum(LocalDateTime erstellungsDatum) {
    this.erstellungsDatum = erstellungsDatum;
  }

  public LocalDateTime getAenderungsDatum() {
    return aenderungsDatum;
  }

  public void setAenderungsDatum(LocalDateTime aenderungsDatum) {
    this.aenderungsDatum = aenderungsDatum;
  }

  public Set<GlobaleBeschreibungsKomponente> getBeschreibungsStruktur() {
    return beschreibungsStruktur;
  }

  public void setBeschreibungsStruktur(
      Set<GlobaleBeschreibungsKomponente> beschreibungsStruktur) {
    this.beschreibungsStruktur = beschreibungsStruktur;
  }

  public Set<NormdatenReferenz> getBeschreibungsBeteiligte() {
    return beschreibungsBeteiligte;
  }

  public void setBeschreibungsBeteiligte(
      Set<NormdatenReferenz> beschreibungsBeteiligte) {
    this.beschreibungsBeteiligte = beschreibungsBeteiligte;
  }

  public String getKatalogID() {
    return this.katalogID;
  }

  public void setKatalogID(String katalog) {
    this.katalogID = katalog;
  }

  public void addBeschreibungsBeteiligter(NormdatenReferenz beteiligte) {
    if (Objects.nonNull(beteiligte)) {
      beschreibungsBeteiligte.add(beteiligte);
    }
  }

  public void removeBeschreibungsBeteiligter(NormdatenReferenz beteiligte) {
    if (Objects.nonNull(beteiligte)) {
      beschreibungsBeteiligte.remove(beteiligte);
    }
  }

  public Set<Publikation> getPublikationen() {
    return publikationen;
  }

  public void setPublikationen(Set<Publikation> publikationen) {
    this.publikationen = publikationen;
  }

  public String getTeiXML() {
    return teiXML;
  }

  public void setTeiXML(String teiXML) {
    this.teiXML = teiXML;
  }

  public Set<String> getAltIdentifier() {
    return altIdentifier;
  }

  public void setAltIdentifier(Set<String> altIdentifier) {
    this.altIdentifier = altIdentifier;
  }

  public VerwaltungsTyp getVerwaltungsTyp() {
    return verwaltungsTyp;
  }

  public Set<NormdatenReferenz> getAutoren() {
    return autoren;
  }

  public void makeBeschreibungIntern() {
    this.verwaltungsTyp = VerwaltungsTyp.INTERN;
  }

  public void makeBeschreibungExtern() {
    this.verwaltungsTyp = VerwaltungsTyp.EXTERN;
  }

  public boolean isBeschreibungIntern() {
    return VerwaltungsTyp.INTERN.equals(this.verwaltungsTyp);
  }

  public Optional<Identifikation> getGueltigeIdentifikation() {
    return beschreibungsStruktur.stream()
        .filter(k -> BeschreibungsKomponentenTyp.KOPF.equals(k.getTyp()))
        .findFirst()
        .map(k -> k.getIdentifikationen().stream().filter(i -> IdentTyp.GUELTIGE_SIGNATUR
            .equals(
                i.getIdentTyp())).findFirst().get());
  }

  public Optional<String> getTitel() {
    return beschreibungsStruktur.stream()
        .filter(k -> BeschreibungsKomponentenTyp.KOPF.equals(k.getTyp()))
        .findFirst()
        .map(BeschreibungsKomponenteKopf.class::cast)
        .map(BeschreibungsKomponenteKopf::getTitel);
  }

  public BeschreibungsTyp getBeschreibungsTyp() {
    return beschreibungsTyp;
  }

  public Lizenz getLizenz() {
    return lizenz;
  }

  public DokumentObjektTyp getDokumentObjektTyp() {
    return dokumentObjektTyp;
  }

  public Set<PURL> getPURLs() {
    return this.purls;
  }

  public void setPURLs(Set<PURL> purls) {
    this.purls = purls;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Beschreibung)) {
      return false;
    }
    Beschreibung that = (Beschreibung) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return toString(true);
  }

  public String toString(boolean withID) {
    final StringBuilder sb = new StringBuilder("Beschreibung{");
    sb.append("kodID='").append(kodID).append('\'');
    if (withID) {
      sb.append(", id='").append(id).append('\'');
    }
    sb.append(", externeIDs=").append(externeIDs);
    sb.append(", altIdentifier=").append(altIdentifier);
    sb.append(", beschreibungsSprache=").append(beschreibungsSprache);
    sb.append(", erstellungsDatum=").append(erstellungsDatum);
    sb.append(", aenderungsDatum=").append(aenderungsDatum);
    sb.append(", beschreibungsStruktur=").append(beschreibungsStruktur);
    sb.append(", beschreibungsBeteiligte=").append(beschreibungsBeteiligte);
    sb.append(", teiXML='").append(teiXML).append('\'');
    sb.append(", katalogID='").append(katalogID).append('\'');
    sb.append(", publikationen=").append(publikationen);
    sb.append(", verwaltungsTyp=").append(verwaltungsTyp);
    sb.append(", beschreibungsTyp=").append(beschreibungsTyp);
    sb.append(", lizenz=").append(lizenz);
    sb.append(", purls=").append(purls.size());
    sb.append('}');
    return sb.toString();
  }

  public static class BeschreibungsBuilder {


    private String id;
    private String kodID;
    private Set<Referenz> externeIDs;
    private NormdatenReferenz beschreibungsSprache;
    private LocalDateTime erstellungsDatum;
    private LocalDateTime aenderungsDatum;
    private Set<GlobaleBeschreibungsKomponente> beschreibungsStruktur;
    private Set<NormdatenReferenz> beschreibungsBeteiligte;
    private String katalogID;
    private Set<Publikation> publikationen;
    private String teiXML;
    private Set<String> altIdentifier;
    private VerwaltungsTyp verwaltungsTyp;
    private BeschreibungsTyp beschreibungsTyp;
    private DokumentObjektTyp dokumentObjektTyp;
    private Lizenz lizenz;
    private Set<NormdatenReferenz> autoren;
    private Set<PURL> purls;

    public BeschreibungsBuilder() {
      externeIDs = new LinkedHashSet<>();
      beschreibungsBeteiligte = new LinkedHashSet<>();
      beschreibungsStruktur = new LinkedHashSet<>();
      publikationen = new LinkedHashSet<>();
      altIdentifier = new LinkedHashSet<>();
      autoren = new LinkedHashSet<>();
      purls = new LinkedHashSet<>();
    }

    public BeschreibungsBuilder(String id, String kodID) {
      this.id = id;
      this.kodID = kodID;
      externeIDs = new LinkedHashSet<>();
      beschreibungsBeteiligte = new LinkedHashSet<>();
      beschreibungsStruktur = new LinkedHashSet<>();
      publikationen = new LinkedHashSet<>();
      altIdentifier = new LinkedHashSet<>();
      autoren = new LinkedHashSet<>();
      purls = new LinkedHashSet<>();
    }

    public BeschreibungsBuilder withId(String id) {
      this.id = id;
      return this;
    }

    public BeschreibungsBuilder withKodID(String kodID) {
      this.kodID = kodID;
      return this;
    }

    public BeschreibungsBuilder withTEIXml(String teiXML) {
      this.teiXML = teiXML;
      return this;
    }

    public BeschreibungsBuilder withVerwaltungsTyp(VerwaltungsTyp verwaltungsTyp) {
      this.verwaltungsTyp = verwaltungsTyp;
      return this;
    }

    public BeschreibungsBuilder withBeschreibungsTyp(BeschreibungsTyp beschreibungsTyp) {
      this.beschreibungsTyp = beschreibungsTyp;
      return this;
    }

    public BeschreibungsBuilder withDokumentObjectTyp(DokumentObjektTyp dokumentObjektTyp) {
      this.dokumentObjektTyp = dokumentObjektTyp;
      return this;
    }

    public BeschreibungsBuilder withLizenz(Lizenz lizenz) {
      this.lizenz = lizenz;
      return this;
    }

    public BeschreibungsBuilder withExterneIDs(Set<Referenz> externeIDs) {
      if (Objects.nonNull(externeIDs)) {
        this.externeIDs.clear();
        this.externeIDs.addAll(externeIDs);
      }
      return this;
    }

    public BeschreibungsBuilder addAutoren(Set<NormdatenReferenz> autoren) {
      if (autoren != null) {
        this.autoren.addAll(autoren);
      }
      return this;
    }

    public BeschreibungsBuilder addAutor(NormdatenReferenz autor) {
      if (autor != null) {
        this.autoren.add(autor);
      }
      return this;
    }

    public BeschreibungsBuilder addExterneID(Referenz externeID) {
      this.externeIDs.add(externeID);
      return this;
    }


    public BeschreibungsBuilder withBeschreibungssprache(NormdatenReferenz beschreibungsSprache) {
      this.beschreibungsSprache = beschreibungsSprache;
      return this;
    }

    public BeschreibungsBuilder withErstellungsDatum(LocalDateTime erstellungsDatum) {
      this.erstellungsDatum = erstellungsDatum;
      return this;
    }

    public BeschreibungsBuilder withAenderungsDatum(LocalDateTime aenderungsDatum) {
      this.aenderungsDatum = aenderungsDatum;
      return this;
    }

    public BeschreibungsBuilder withKatalog(String katalogID) {
      this.katalogID = katalogID;
      return this;
    }

    public BeschreibungsBuilder addBeschreibungsKomponente(
        GlobaleBeschreibungsKomponente globaleBeschreibungsKomponente) {
      if (Objects.nonNull(globaleBeschreibungsKomponente)) {
        beschreibungsStruktur.add(globaleBeschreibungsKomponente);
      }
      return this;
    }

    public BeschreibungsBuilder removeBeschreibungKomponente(
        GlobaleBeschreibungsKomponente globaleBeschreibungsKomponente) {
      if (Objects.nonNull(globaleBeschreibungsKomponente)) {
        beschreibungsStruktur.remove(globaleBeschreibungsKomponente);
      }
      return this;
    }


    public BeschreibungsBuilder addBeschreibungsBeteiligter(NormdatenReferenz beteiligte) {
      if (Objects.nonNull(beteiligte)) {
        beschreibungsBeteiligte.add(beteiligte);
      }
      return this;
    }

    public BeschreibungsBuilder removeBeschreibungsBeteiligter(NormdatenReferenz beteiligte) {
      if (Objects.nonNull(beteiligte)) {
        beschreibungsBeteiligte.remove(beteiligte);
      }
      return this;
    }

    public BeschreibungsBuilder addPublikation(Publikation publikation) {
      if (Objects.nonNull(publikation)) {
        publikationen.add(publikation);
      }
      return this;
    }

    public BeschreibungsBuilder removePublikation(Publikation publikation) {
      if (Objects.nonNull(publikation)) {
        publikationen.remove(publikation);
      }
      return this;
    }

    public BeschreibungsBuilder withPublikationen(Set<Publikation> publikationen) {
      if (Objects.nonNull(publikationen)) {
        this.publikationen.clear();
        this.publikationen.addAll(publikationen);
      }
      return this;
    }

    public BeschreibungsBuilder addAltIdentifier(String altIdentifier) {
      if (Objects.nonNull(altIdentifier) && !altIdentifier.isEmpty()) {
        this.altIdentifier.add(altIdentifier);
      }
      return this;
    }

    public BeschreibungsBuilder removeAltIdentifier(String altIdentifier) {
      this.altIdentifier.remove(altIdentifier);
      return this;
    }

    public BeschreibungsBuilder withPURLs(Set<PURL> purls) {
      this.purls = purls;
      return this;
    }

    public BeschreibungsBuilder addPURL(PURL purl) {
      this.purls.add(purl);
      return this;
    }

    public Beschreibung build() {
      return new Beschreibung(this);
    }
  }
}
