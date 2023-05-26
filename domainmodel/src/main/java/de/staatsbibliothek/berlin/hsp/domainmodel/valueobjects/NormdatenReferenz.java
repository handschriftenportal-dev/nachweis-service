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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.Cacheable;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SelectBeforeUpdate;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 06.10.2020.
 * @version 1.0
 */
@Table(name = "NormdatenReferenz")
@Entity
@SelectBeforeUpdate
@Cacheable
public class NormdatenReferenz implements Serializable {

  public static final String PERSON_TYPE_NAME = "Person";
  public static final String ORT_TYPE_NAME = "Place";
  public static final String KOERPERSCHAFT_TYPE_NAME = "CorporateBody";
  public static final String SPRACHE_TYPE_NAME = "Language";

  public static final String DNB_BASE_URL = "http://d-nb.info/gnd/";

  private static final long serialVersionUID = -5532329786024649463L;

  @Id
  private String id;

  @Column(length = 255)
  private String typeName;

  @Column(length = 1024)
  private String name;

  @Column
  private String gndID;

  @ElementCollection(fetch = FetchType.LAZY, targetClass = Identifikator.class)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn
  @CollectionTable(name = "NormdatenReferenz_Identifikator",
      joinColumns = @JoinColumn(name = "normdatenReferenz_id",
          foreignKey = @ForeignKey(name = "FK_NR_Identifikator_NormdatenReferenz")))
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private Set<Identifikator> identifikator;

  @ElementCollection(fetch = FetchType.LAZY, targetClass = VarianterName.class)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn
  @CollectionTable(name = "NormdatenReferenz_VarianterName",
      joinColumns = @JoinColumn(name = "normdatenReferenz_id",
          foreignKey = @ForeignKey(name = "FK_NR_VarianterName_NormdatenReferenz")))
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private Set<VarianterName> varianterName;

  public NormdatenReferenz() {
  }

  public NormdatenReferenz(String name, String gndID) {
    this(null, name, gndID, null);
  }

  public NormdatenReferenz(String id, String name, String gndID) {
    this(id, name, gndID, null);
  }

  public NormdatenReferenz(String id, String name, String gndID, String typeName) {
    this.id = id;
    this.name = name;
    this.gndID = gndID;
    this.typeName = typeName;

    if (id == null) {
      this.id = UUID.nameUUIDFromBytes(
              String.join("$", name, gndID, typeName).getBytes(StandardCharsets.UTF_8))
          .toString();
    }
    identifikator = new HashSet<>();
    varianterName = new HashSet<>();
  }

  public NormdatenReferenz(NormdatenReferenzBuilder builder) {
    this(builder.id, builder.name, builder.gndID, builder.typeName);
    this.identifikator.addAll(builder.identifikator);
    this.varianterName.addAll(builder.varianterName);
  }

  public String getId() {
    return id;
  }

  public String getTypeName() {
    return typeName;
  }

  public String getName() {
    return name;
  }

  public String getGndID() {
    return gndID;
  }

  @JsonIgnore
  public String getGndUrl() {
    return Optional.ofNullable(gndID).map(gid -> DNB_BASE_URL + gid).orElse(null);
  }

  public Set<Identifikator> getIdentifikator() {
    return identifikator;
  }

  public Set<Identifikator> getIdentifikatorByTypeName(String typeName) {
    return identifikator.stream()
        .filter(i -> (Objects.isNull(typeName) && Objects.isNull(i.getType()))
            || (Objects.nonNull(i.getType()) && i.getType().equals(typeName)))
        .collect(Collectors.toSet());
  }

  public Set<VarianterName> getVarianterName() {
    return varianterName;
  }

  public Set<VarianterName> getVarianterNameBylanguageCode(String languageCode) {
    return varianterName.stream()
        .filter(i -> (Objects.isNull(languageCode) && Objects.isNull(i.getLanguageCode()))
            || (Objects.nonNull(i.getLanguageCode()) && i.getLanguageCode().equals(languageCode)))
        .collect(Collectors.toSet());
  }

  @JsonIgnore
  public String getVarianteNamenAlsString() {
    return varianterName.stream()
        .map(VarianterName::getName)
        .collect(Collectors.joining("; "));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NormdatenReferenz)) {
      return false;
    }
    NormdatenReferenz that = (NormdatenReferenz) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("NormdatenReferenz{");
    sb.append("gndID='").append(gndID).append('\'');
    sb.append(", id='").append(id).append('\'');
    sb.append(", typeName='").append(typeName).append('\'');
    sb.append(", name='").append(name).append('\'');
    sb.append(", varianteNamen='")
        .append(varianterName.stream().map(VarianterName::getName).collect(
            Collectors.joining(",")));
    sb.append('}');
    return sb.toString();
  }

  public static class NormdatenReferenzBuilder {

    private String id;
    private String typeName;
    private String name;
    private String gndID;
    private Set<Identifikator> identifikator;
    private Set<VarianterName> varianterName;

    public NormdatenReferenzBuilder() {
      this.identifikator = new HashSet<>();
      this.varianterName = new HashSet<>();
    }

    public NormdatenReferenzBuilder withId(String id) {
      this.id = id;
      return this;
    }

    public NormdatenReferenzBuilder withName(String name) {
      this.name = name;
      return this;
    }

    public NormdatenReferenzBuilder withTypeName(String typeName) {
      this.typeName = typeName;
      return this;
    }

    public NormdatenReferenzBuilder withGndID(String gndID) {
      this.gndID = gndID;
      return this;
    }

    public NormdatenReferenzBuilder withIdentifikator(Set<Identifikator> identifikator) {
      this.identifikator.clear();
      if (Objects.nonNull(identifikator) && !identifikator.isEmpty()) {
        this.identifikator.addAll(identifikator);
      }
      return this;
    }

    public NormdatenReferenzBuilder addVarianterName(VarianterName varianterName) {
      if (Objects.nonNull(varianterName)) {
        this.varianterName.add(varianterName);
      }
      return this;
    }

    public NormdatenReferenzBuilder withVarianterName(Set<VarianterName> varianterName) {
      this.varianterName.clear();
      if (Objects.nonNull(varianterName) && !varianterName.isEmpty()) {
        this.varianterName.addAll(varianterName);
      }
      return this;
    }

    public NormdatenReferenzBuilder addIdentifikator(Identifikator identifikator) {
      if (Objects.nonNull(identifikator)) {
        this.identifikator.add(identifikator);
      }
      return this;
    }

    public NormdatenReferenz build() {
      return new NormdatenReferenz(this);
    }

  }
}
