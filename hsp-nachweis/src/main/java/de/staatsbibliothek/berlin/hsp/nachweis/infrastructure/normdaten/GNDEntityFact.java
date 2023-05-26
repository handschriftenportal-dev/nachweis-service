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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GraphQlQuery.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 31.08.2020.
 * @version 1.0
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class GNDEntityFact {

  public static final String ID = "id";
  public static final String TYPE_NAME = "typeName";
  public static final String GND_IDENTIFIER = "gndIdentifier";
  public static final String PREFERRED_NAME = "preferredName";
  public static final String IDENTIFIER = "identifier";
  public static final String VARIANT_NAME = "variantName";

  public static final String PERSON_TYPE_NAME = "Person";
  public static final String PLACE_TYPE_NAME = "Place";
  public static final String CORPORATE_BODY_TYPE_NAME = "CorporateBody";

  public static final Set<Field> FIELDS = new LinkedHashSet<>(Arrays.asList(
      Field.of(GNDEntityFact.ID),
      Field.of(GNDEntityFact.TYPE_NAME),
      Field.of(GNDEntityFact.GND_IDENTIFIER),
      Field.of(GNDEntityFact.PREFERRED_NAME),
      Field.of(GNDEntityFact.IDENTIFIER, Identifier.FIELDS),
      Field.of(GNDEntityFact.VARIANT_NAME, VariantName.FIELDS)));

  public static final Set<Field> FIELDS_MIN = new LinkedHashSet<>(Arrays.asList(
      Field.of(GNDEntityFact.ID),
      Field.of(GNDEntityFact.TYPE_NAME),
      Field.of(GNDEntityFact.GND_IDENTIFIER),
      Field.of(GNDEntityFact.PREFERRED_NAME)));

  @JsonProperty(ID)
  private String id;
  @JsonProperty(TYPE_NAME)
  private String typeName;
  @JsonProperty(GND_IDENTIFIER)
  private String gndIdentifier;
  @JsonProperty(PREFERRED_NAME)
  private String preferredName;
  @JsonProperty(IDENTIFIER)
  private Set<Identifier> identifier;
  @JsonProperty(VARIANT_NAME)
  private Set<VariantName> variantName;

  public GNDEntityFact() {
  }

  @Deprecated
  public GNDEntityFact(String id, String gndIdentifier, String preferredName) {
    this.id = id;
    this.gndIdentifier = gndIdentifier;
    this.preferredName = preferredName;
    this.identifier = new HashSet<>();
    this.variantName = new HashSet<>();
  }

  public GNDEntityFact(String id, String gndIdentifier, String preferredName, String typeName) {
    this.id = id;
    this.gndIdentifier = gndIdentifier;
    this.preferredName = preferredName;
    this.typeName = typeName;
    this.identifier = new HashSet<>();
    this.variantName = new HashSet<>();
  }

  public GNDEntityFact(GNDEntityFactBuilder gndEntityFactBuilder) {
    this.id = gndEntityFactBuilder.id;
    this.typeName = gndEntityFactBuilder.typeName;
    this.gndIdentifier = gndEntityFactBuilder.gndIdentifier;
    this.preferredName = gndEntityFactBuilder.preferredName;
    this.identifier = gndEntityFactBuilder.identifier;
    this.variantName = gndEntityFactBuilder.variantName;
  }

  public String getId() {
    return id;
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public String getGndIdentifier() {
    return gndIdentifier;
  }

  public String getPreferredName() {
    return preferredName;
  }

  public Set<VariantName> getVariantName() {
    return variantName;
  }

  public Set<Identifier> getIdentifier() {
    return identifier;
  }

  public Set<Identifier> getIdentifierByType(String type) {
    return identifier.stream()
        .filter(i -> (Objects.isNull(type) && Objects.isNull(i.getType()))
            || (Objects.nonNull(i.getType()) && i.getType().equals(type)))
        .collect(Collectors.toSet());
  }

  public Set<VariantName> getVariantNameBylanguageCode(String languageCode) {
    return variantName.stream()
        .filter(i -> (Objects.isNull(languageCode) && Objects.isNull(i.getLanguageCode()))
            || (Objects.nonNull(i.getLanguageCode()) && i.getLanguageCode().equals(languageCode)))
        .collect(Collectors.toSet());
  }

  public String getVariantNameAsString() {
    if (variantName == null || variantName.isEmpty()) {
      return "";
    }

    return variantName.stream().map(VariantName::getName).collect(Collectors.joining("; "));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof GNDEntityFact)) {
      return false;
    }
    GNDEntityFact that = (GNDEntityFact) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "GNDEntityFact{" +
        "id='" + id + '\'' +
        ", typeName='" + typeName + '\'' +
        ", gndIdentifier='" + gndIdentifier + '\'' +
        ", preferredName='" + preferredName + '\'' +
        ", identifier=" + identifier +
        ", variantName=" + variantName +
        '}';
  }

  public static class GNDEntityFactBuilder {

    private String id;
    private String typeName;
    private String gndIdentifier;
    private String preferredName;
    private Set<Identifier> identifier;
    private Set<VariantName> variantName;

    public GNDEntityFactBuilder() {
      this.identifier = new HashSet<>();
      this.variantName = new HashSet<>();
    }

    public GNDEntityFactBuilder withId(String id) {
      this.id = id;
      return this;
    }

    public GNDEntityFactBuilder withTypeName(String typeName) {
      this.typeName = typeName;
      return this;
    }

    public GNDEntityFactBuilder withGndIdentifier(String gndIdentifier) {
      this.gndIdentifier = gndIdentifier;
      return this;
    }

    public GNDEntityFactBuilder withPreferredName(String preferredName) {
      this.preferredName = preferredName;
      return this;
    }

    public GNDEntityFactBuilder withIdentifier(Set<Identifier> identifier) {
      this.identifier.clear();
      if (Objects.nonNull(identifier) && !identifier.isEmpty()) {
        this.identifier.addAll(identifier);
      }
      return this;
    }

    public GNDEntityFactBuilder addIdentifier(Identifier identifier) {
      if (Objects.nonNull(identifier)) {
        this.identifier.add(identifier);
      }
      return this;
    }

    public GNDEntityFactBuilder withVariantName(Set<VariantName> variantName) {
      this.variantName.clear();
      if (Objects.nonNull(variantName) && !variantName.isEmpty()) {
        this.variantName.addAll(variantName);
      }
      return this;
    }

    public GNDEntityFactBuilder addVariantName(VariantName variantName) {
      if (Objects.nonNull(variantName)) {
        this.variantName.add(variantName);
      }
      return this;
    }

    public GNDEntityFact build() {
      return new GNDEntityFact(this);
    }
  }
}
