package de.staatsbibliothek.berlin.hsp.domainmodel.aggregates;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KatalogBeschreibungReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 16.03.2017
 */
@Entity
@Table(name = "Katalog")
public class Katalog implements TEIDokument, Serializable {

  @Serial
  private static final long serialVersionUID = -1656605474240620782L;

  @Id
  @Column(name = "id")
  String id;

  @Column(name = "title", length = 4096)
  String title;

  @ManyToMany(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(name = "katalog_autoren")
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  Set<NormdatenReferenz> autoren;

  @OneToOne(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST, CascadeType.MERGE})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  NormdatenReferenz verlag;

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  Digitalisat digitalisat;

  @Column(name = "publikations_jahr")
  String publikationsJahr;

  @Column(name = "lizenz_uri")
  String lizenzUri;

  @Column(name = "hsk_id")
  String hskID;

  @Column(name = "erstellDatum")
  LocalDateTime erstellDatum;

  @Column(name = "aenderungsDatum")
  LocalDateTime aenderungsDatum;

  @OneToMany(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(name = "katalog_beschreibungs_referenzen")
  Set<KatalogBeschreibungReferenz> beschreibungen;

  @Column(name = "tei_xml", columnDefinition = "TEXT", length = 2097152)
  @Basic(fetch = FetchType.LAZY)
  String teiXML;

  protected Katalog() {}

  public Katalog(KatalogBuilder builder) {
    this.id = builder.id;
    this.title = builder.title;
    this.digitalisat = builder.digitalisat;
    this.publikationsJahr = builder.publikationsJahr;
    this.autoren = builder.autoren;
    this.verlag = builder.verlag;
    this.lizenzUri = builder.lizenzURI;
    this.hskID = builder.hskID;
    this.erstellDatum = builder.erstellDatum;
    this.aenderungsDatum = builder.aenderungsDatum;
    this.beschreibungen = builder.beschreibungen;
    this.teiXML = builder.teiXML;
  }

  public String getId() {
    return this.id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Set<NormdatenReferenz> getAutoren() {
    return autoren;
  }

  public Digitalisat getDigitalisat() {
    return this.digitalisat;
  }

  public void setDigitalisat(Digitalisat digitalisat) {
    this.digitalisat = digitalisat;
  }

  public String getPublikationsJahr() {
    return publikationsJahr;
  }

  public void setPublikationsJahr(String publikationsJahr) {
    this.publikationsJahr = publikationsJahr;
  }

  public NormdatenReferenz getVerlag() {
    return verlag;
  }

  public void setVerlag(NormdatenReferenz verlag) {
    this.verlag = verlag;
  }

  public Set<KatalogBeschreibungReferenz> getBeschreibungen() {
    return beschreibungen;
  }

  public String getLizenzUri() {
    return lizenzUri;
  }

  public void setLizenzUri(String lizenzUri) {
    this.lizenzUri = lizenzUri;
  }

  public String getHskID() {
    return hskID;
  }

  public LocalDateTime getErstellDatum() {
    return erstellDatum;
  }

  public LocalDateTime getAenderungsDatum() {
    return aenderungsDatum;
  }

  public void setAenderungsDatum(LocalDateTime aenderungsDatum) {
    this.aenderungsDatum = aenderungsDatum;
  }

  public String getTeiXML() {
    return teiXML;
  }

  public void setTeiXML(String teiXML) {
    this.teiXML = teiXML;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Katalog katalog = (Katalog) o;
    return id.equals(katalog.id) && Objects.equals(hskID, katalog.hskID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, hskID);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Katalog{");
    sb.append("id='").append(id).append('\'');
    sb.append(", title='").append(title).append('\'');
    sb.append(", autoren=").append(autoren);
    sb.append(", verlag=").append(verlag);
    sb.append(", digitalisat=").append(digitalisat);
    sb.append(", publikationsJahr='").append(publikationsJahr).append('\'');
    sb.append(", lizenzUri='").append(lizenzUri).append('\'');
    sb.append(", hskID='").append(hskID).append('\'');
    sb.append(", erstellDatum=").append(erstellDatum);
    sb.append(", aenderungsDatum=").append(aenderungsDatum);
    sb.append(", beschreibungen=").append(beschreibungen);
    sb.append(", teiXML='").append(teiXML).append('\'');
    sb.append('}');
    return sb.toString();
  }

  public static class KatalogBuilder {

    private String id;
    private String title;
    private Digitalisat digitalisat;
    private String publikationsJahr;
    private Set<NormdatenReferenz> autoren = new LinkedHashSet<>();
    private NormdatenReferenz verlag;
    private String lizenzURI;
    private String hskID;
    private LocalDateTime erstellDatum;
    private LocalDateTime aenderungsDatum;
    private Set<KatalogBeschreibungReferenz> beschreibungen = new LinkedHashSet<>();
    private String teiXML;

    public KatalogBuilder withId(String id) {
      this.id = id;
      return this;
    }

    public KatalogBuilder withTitle(String title) {
      this.title = title;
      return this;
    }

    public KatalogBuilder withPublikationsJahr(String publikationsJahr) {
      this.publikationsJahr = publikationsJahr;
      return this;
    }

    public KatalogBuilder withLizenzURI(String lizenzURI) {
      this.lizenzURI = lizenzURI;
      return this;
    }

    public KatalogBuilder withHSKID(String hskID) {
      this.hskID = hskID;
      return this;
    }

    public KatalogBuilder withTEIXML(String teiXML) {
      this.teiXML = teiXML;
      return this;
    }

    public KatalogBuilder withErstelldatum(LocalDateTime erstellDatum) {
      this.erstellDatum = erstellDatum;
      return this;
    }

    public KatalogBuilder withAenderungsdatum(LocalDateTime aenderungsDatum) {
      this.aenderungsDatum = aenderungsDatum;
      return this;
    }

    public KatalogBuilder withDigitalisat(Digitalisat digitalisat) {
      if (digitalisat != null) {
        this.digitalisat = digitalisat;
      }
      return this;
    }

    public KatalogBuilder withVerlag(NormdatenReferenz verlag) {
      if (verlag != null) {
        this.verlag = verlag;
      }
      return this;
    }

    public KatalogBuilder addAutor(NormdatenReferenz autor) {
      if (autor != null && autoren != null) {
        this.autoren.add(autor);
      }
      return this;
    }

    public KatalogBuilder addKatalogBeschreibungReferenz(
        KatalogBeschreibungReferenz katalogBeschreibungReferenz) {
      if (katalogBeschreibungReferenz != null && beschreibungen != null) {
        this.beschreibungen.add(katalogBeschreibungReferenz);
      }
      return this;
    }

    public Katalog build() {
      return new Katalog(this);
    }
  }
}
