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

package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DigitalisatTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * @author Konrad.Eichstaedt@sbb.spk-berlin.de
 * @since 06.10.2020
 * <p>
 * This object presents the fact of digititation of an manual script object. Most important part is
 * the IIIF manifest URL to enable the view within an IIIF Viewer like Mirador.
 */
@Entity
@Table(name = "Digitalisat")
@Cacheable
public class Digitalisat implements Serializable {

  private static final long serialVersionUID = -5254882342439794181L;

  @Id
  private String id;

  @Column
  private String manifestURL;

  @Column
  private String alternativeURL;

  @Column
  private String thumbnailURL;

  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.REFRESH,
      CascadeType.PERSIST,
      CascadeType.MERGE})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private NormdatenReferenz entstehungsOrt;

  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.REFRESH,
      CascadeType.PERSIST,
      CascadeType.MERGE})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private NormdatenReferenz digitalisierendeEinrichtung;

  @Column
  private LocalDate digitalisierungsDatum;

  @Column
  private LocalDate erstellungsDatum;


  @Enumerated(EnumType.STRING)
  @Column
  private DigitalisatTyp typ;


  public Digitalisat() {
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
    if (id == null) {
      String valueForIDGeneration;
      if (manifestURL != null) {
        valueForIDGeneration = manifestURL.toString();
      } else {
        valueForIDGeneration = toString(false);
      }
      id = UUID.nameUUIDFromBytes(valueForIDGeneration.getBytes(StandardCharsets.UTF_8)).toString();
    }
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
    sb.append(", typ=").append(typ);
    sb.append('}');
    return sb.toString();
  }

  public static final class Builder {

    private String id;

    private URI manifestURL;

    private URI alternativeURL;

    private URI thumbnailURL;

    private NormdatenReferenz entstehungsOrt;

    private NormdatenReferenz digitalisierendeEinrichtung;

    private LocalDate digitalisierungsDatum;

    private DigitalisatTyp typ;

    private LocalDate erstellungsDatum;

    private Builder() {
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

    public Builder withTyp(DigitalisatTyp typ) {
      this.typ = typ;
      return this;
    }

    public Builder withID(String id) {
      this.id = id;
      return this;
    }

    public Digitalisat build() {
      return new Digitalisat(this);
    }
  }
}
