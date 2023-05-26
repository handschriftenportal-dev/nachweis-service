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

package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * This class represent an object to use for identifikation of an cultur object. The most import attributs are ident,
 * owner and place.
 *
 * @author Konrad.Eichstaedt@sbb.spk-berlin.de
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 10.05.19
 */
@Entity
@Table(indexes = @Index(columnList = "ident", name = "identifikation_ident_index"), name = "Identifikation")
@Cacheable
public class Identifikation implements Serializable {

  private static final long serialVersionUID = 7695473925536819618L;

  @Id
  @Column
  private String id;

  @ElementCollection(fetch = FetchType.EAGER)
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn
  private Set<String> sammlungIDs;

  @Column
  private String ident;

  @Enumerated(EnumType.STRING)
  @Column(name = "identtyp", length = 25)
  private IdentTyp identTyp;

  @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST,
      CascadeType.MERGE})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private NormdatenReferenz aufbewahrungsOrt;

  @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST,
      CascadeType.MERGE})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private NormdatenReferenz besitzer;

  protected Identifikation() {
    sammlungIDs = new LinkedHashSet<>();
  }

  public Identifikation(IdentifikationBuilder builder) {
    this.id = builder.id;
    this.besitzer = builder.besitzer;
    this.aufbewahrungsOrt = builder.aufbewahrungsOrt;
    this.sammlungIDs = builder.sammlungIDs;
    this.ident = builder.ident;
    this.identTyp = builder.identTyp;
    if (id == null) {
      id = UUID.nameUUIDFromBytes(toString(false).getBytes(StandardCharsets.UTF_8)).toString();
    }
  }

  /**
   * @return <code>String</code> The internal UUID generated by this module.
   */

  public String getId() {
    return this.id;
  }

  /**
   * @param id as <code>String</code> the internal UUID generated by this module.
   */

  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return <code>Set</code> Every Identifikation object has been used over time for an cultur object. This could be a
   * part of an collection. The Attribute represents the collection ID of the associated cultur object.
   */

  public Set<String> getSammlungIDs() {
    return this.sammlungIDs;
  }

  /**
   * @param sammlungIDs as <code>Set</code> Every Identifikation object has been used over time for an cultur object.
   *                    This could be a * part of an collection. The Attribute represents the collection ID of the
   *                    associated cultur object.
   */

  public void setSammlungIDs(Set<String> sammlungIDs) {
    this.sammlungIDs = sammlungIDs;
  }

  /**
   * @return <code>String</code> Signature of an cultur object.
   */

  public String getIdent() {
    return this.ident;
  }

  /**
   * @param ident as <code>String</code> Set the signature of an cultur object.
   */

  public void setIdent(String ident) {
    this.ident = ident;
  }

  /**
   * @return <code>IdentTyp</code> The identifikation type of the identifikation object.
   */

  public IdentTyp getIdentTyp() {
    return this.identTyp;
  }

  /**
   * @param identTyp as <code>String</code> Set the identifikation type of the identifikation object.
   */

  public void setIdentTyp(IdentTyp identTyp) {
    this.identTyp = identTyp;
  }

  /**
   * @return <code>NormdatenReferenz</code> Get the reference object of the metadata service as representation of the
   * settlement of cultur object.
   */

  public NormdatenReferenz getAufbewahrungsOrt() {
    return aufbewahrungsOrt;
  }

  /**
   * @param aufbewahrungsOrt as <code>NormdatenReferenz</code> Set the reference object of the metadata service as
   *                         representation of the settlement of cultur object.
   */

  public void setAufbewahrungsOrt(NormdatenReferenz aufbewahrungsOrt) {
    this.aufbewahrungsOrt = aufbewahrungsOrt;
  }

  /**
   * @return <code>NormdatenReferenz</code> Get the reference object of the metadata service as representation of the
   * repository (corporateBody) of cultur object.
   */

  public NormdatenReferenz getBesitzer() {
    return besitzer;
  }

  /**
   * @param besitzer as <code>NormdatenReferenz</code> Set the reference object of the metadata service as
   *                 representation of the repository of cultur object.
   */

  public void setBesitzer(NormdatenReferenz besitzer) {
    this.besitzer = besitzer;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Identifikation)) {
      return false;
    }
    Identifikation that = (Identifikation) o;
    return Objects.equals(ident, that.ident) &&
        identTyp == that.identTyp &&
        Objects.equals(aufbewahrungsOrt, that.aufbewahrungsOrt) &&
        Objects.equals(besitzer, that.besitzer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ident, identTyp, aufbewahrungsOrt, besitzer);
  }

  @Override
  public String toString() {
    return toString(true);
  }

  public String toString(boolean withID) {
    final StringBuilder sb = new StringBuilder("Identifikation{");
    sb.append("ident='").append(ident).append('\'');
    if (withID) {
      sb.append(", id='").append(id).append('\'');
    }
    sb.append(", sammlungIDs=").append(sammlungIDs);
    sb.append(", identTyp=").append(identTyp);
    sb.append(", aufbewahrungsOrt=").append(aufbewahrungsOrt);
    sb.append(", besitzer=").append(besitzer);
    sb.append('}');
    return sb.toString();
  }

  public static class IdentifikationBuilder {

    private String id;
    private NormdatenReferenz aufbewahrungsOrt;
    private NormdatenReferenz besitzer;
    private Set<String> sammlungIDs;
    private String ident;
    private IdentTyp identTyp;

    public IdentifikationBuilder() {
      sammlungIDs = new LinkedHashSet<>();
    }

    public IdentifikationBuilder withId(String id) {
      this.id = id;
      return this;
    }

    public IdentifikationBuilder withBesitzer(NormdatenReferenz besitzer) {
      this.besitzer = besitzer;
      return this;
    }

    public IdentifikationBuilder withAufbewahrungsOrt(NormdatenReferenz aufbewahrungsOrt) {
      this.aufbewahrungsOrt = aufbewahrungsOrt;
      return this;
    }

    public IdentifikationBuilder withSammlungIDs(Set<String> sammlungen) {
      this.sammlungIDs = sammlungen;
      return this;
    }

    public IdentifikationBuilder addSammlungID(String sammlungsid) {
      if (sammlungsid != null) {
        this.sammlungIDs.add(sammlungsid);
      }
      return this;
    }

    public IdentifikationBuilder withIdent(String ident) {
      this.ident = ident;
      return this;
    }

    public IdentifikationBuilder withIdentTyp(IdentTyp identTyp) {
      this.identTyp = identTyp;
      return this;
    }

    public Identifikation build() {
      return new Identifikation(this);
    }
  }
}