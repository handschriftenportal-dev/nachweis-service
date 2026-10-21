package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.HSP_UUI_Factory;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DigitalisatTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * @author Konrad.Eichstaedt@sbb.spk-berlin.de
 * @since 06.10.2020
 * <p>This object presents the fact of digititation of an manual script object. Most important
 * part is the IIIF manifest URL to enable the view within an IIIF Viewer like Mirador.
 */
@Entity
@Table(name = "Digitalisat")
@Cacheable
public class Digitalisat implements Serializable {

  @Serial
  private static final long serialVersionUID = -5254882342439794181L;

  @Id
  String id;

  @Column
  String manifestURL;

  @Column
  String alternativeURL;

  @Column
  String thumbnailURL;

  @ManyToOne(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  NormdatenReferenz entstehungsOrt;

  @ManyToOne(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  NormdatenReferenz digitalisierendeEinrichtung;

  @Column
  LocalDate digitalisierungsDatum;

  @Column
  LocalDate erstellungsDatum;

  @Column
  LocalDate publikationsDatum;

  @Enumerated(EnumType.STRING)
  @Column
  DigitalisatTyp typ;

  @OneToMany(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  Set<NormdatenReferenz> digitalisatsKonzepte;

  public Digitalisat() {
    this.digitalisatsKonzepte = new HashSet<>();
  }

  private Digitalisat(Builder builder) {
    this.id = builder.id;
    this.manifestURL = (builder.manifestURL != null ? builder.manifestURL.toASCIIString() : null);
    this.alternativeURL =
        builder.alternativeURL != null ? builder.alternativeURL.toASCIIString() : null;
    this.thumbnailURL = builder.thumbnailURL != null ? builder.thumbnailURL.toASCIIString() : null;
    this.entstehungsOrt = builder.entstehungsOrt;
    this.digitalisierendeEinrichtung = builder.digitalisierendeEinrichtung;
    this.digitalisierungsDatum = builder.digitalisierungsDatum;
    this.typ = builder.typ;
    this.erstellungsDatum = builder.erstellungsDatum;
    this.publikationsDatum = builder.publikationsDatum;
    if (id == null) {
      String valueForIDGeneration;
      if (manifestURL != null) {
        valueForIDGeneration = manifestURL;
      } else {
        valueForIDGeneration = toString(false);
      }
      id = HSP_UUI_Factory.generate(valueForIDGeneration.getBytes(StandardCharsets.UTF_8));
    }
    this.digitalisatsKonzepte = builder.digitalisatsKonzepte;
  }

  public static Builder DigitalisatBuilder() {
    return new Builder();
  }

  public String getId() {
    return id;
  }

  public URI getManifestURL() {
    return manifestURL != null ? URI.create(manifestURL) : null;
  }

  public URI getAlternativeURL() {
    return alternativeURL != null ? URI.create(alternativeURL) : null;
  }

  public URI getThumbnailURL() {
    return thumbnailURL != null ? URI.create(thumbnailURL) : null;
  }

  public NormdatenReferenz getEntstehungsOrt() {
    return entstehungsOrt;
  }

  public NormdatenReferenz getDigitalisierendeEinrichtung() {
    return digitalisierendeEinrichtung;
  }

  public LocalDate getDigitalisierungsDatum() {
    return digitalisierungsDatum;
  }

  public DigitalisatTyp getTyp() {
    return typ;
  }

  public LocalDate getErstellungsDatum() {
    return erstellungsDatum;
  }

  public LocalDate getPublikationsDatum() {
    return publikationsDatum;
  }

  public Set<NormdatenReferenz> getDigitalisatsKonzepte() {
    return digitalisatsKonzepte;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Digitalisat)) {
      return false;
    }
    Digitalisat that = (Digitalisat) o;
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

  private String toString(boolean withId) {
    final StringBuilder sb = new StringBuilder("Digitalisat{");
    sb.append("manifestURL=").append(manifestURL);
    if (withId) {
      sb.append(", id='").append(id).append('\'');
    }
    sb.append(", alternativeURL=").append(alternativeURL);
    sb.append(", thumbnailURL=").append(thumbnailURL);
    sb.append(", entstehungsOrt=").append(entstehungsOrt);
    sb.append(", digitalisierendeEinrichtung=").append(digitalisierendeEinrichtung);
    sb.append(", digitalisierungsDatum=").append(digitalisierungsDatum);
    sb.append(", erstellungsDatum=").append(erstellungsDatum);
    sb.append(", publikationsDatum=").append(publikationsDatum);
    sb.append(", typ=").append(typ);
    sb.append(", konzepte=")
        .append(
            digitalisatsKonzepte != null
                ? digitalisatsKonzepte.stream()
                .map(NormdatenReferenz::toString)
                .collect(Collectors.joining())
                : ";");
    sb.append('}');
    return sb.toString();
  }

  public static final class Builder {

    private final Set<NormdatenReferenz> digitalisatsKonzepte;
    private String id;
    private URI manifestURL;
    private URI alternativeURL;
    private URI thumbnailURL;
    private NormdatenReferenz entstehungsOrt;
    private NormdatenReferenz digitalisierendeEinrichtung;
    private LocalDate digitalisierungsDatum;
    private DigitalisatTyp typ;
    private LocalDate erstellungsDatum;
    private LocalDate publikationsDatum;

    private Builder() {
      this.digitalisatsKonzepte = new HashSet<>();
    }

    public Builder withManifest(String manifest) {
      if (manifest != null && !manifest.isEmpty()) {
        try {
          this.manifestURL = new URI(manifest);
        } catch (URISyntaxException e) {
          throw new IllegalArgumentException("Illegal URI passed!", e);
        }
      }
      return this;
    }

    public Builder withAlternativeUrl(String alternativeUrl) {
      if (alternativeUrl != null && !alternativeUrl.isEmpty()) {
        try {
          this.alternativeURL = new URI(alternativeUrl);
        } catch (URISyntaxException e) {
          throw new IllegalArgumentException("Illegal URI passed!", e);
        }
      }
      return this;
    }

    public Builder withThumbnail(String thumbnail) {
      if (thumbnail != null && !thumbnail.isEmpty()) {
        try {
          this.thumbnailURL = new URI(thumbnail);
        } catch (URISyntaxException e) {
          throw new IllegalArgumentException("Illegal URI passed!", e);
        }
      }
      return this;
    }

    public Builder withEntstehungsort(NormdatenReferenz entstehungsOrt) {
      this.entstehungsOrt = entstehungsOrt;
      return this;
    }

    public Builder withDigitalisierendeEinrichtung(NormdatenReferenz digitalisierendeEinrichtung) {
      this.digitalisierendeEinrichtung = digitalisierendeEinrichtung;
      return this;
    }

    public Builder withDigitalisierungsdatum(LocalDate digitalisierungsDatum) {
      this.digitalisierungsDatum = digitalisierungsDatum;
      return this;
    }

    public Builder withErstellungsdatum(LocalDate erstellungsDatum) {
      this.erstellungsDatum = erstellungsDatum;
      return this;
    }

    public Builder withPublikationsdatum(LocalDate publikationsdatum) {
      this.publikationsDatum = publikationsdatum;
      return this;
    }

    public Builder withTyp(DigitalisatTyp typ) {
      this.typ = typ;
      return this;
    }

    public Builder withID(String id) {
      this.id = id;
      return this;
    }

    public Builder addDigitalisatsKonzept(NormdatenReferenz konzept) {
      this.digitalisatsKonzepte.add(konzept);
      return this;
    }

    public Builder addDigitalisatsKonzepte(Set<NormdatenReferenz> konzepte) {
      this.digitalisatsKonzepte.addAll(konzepte);
      return this;
    }

    public Digitalisat build() {
      return new Digitalisat(this);
    }
  }
}
