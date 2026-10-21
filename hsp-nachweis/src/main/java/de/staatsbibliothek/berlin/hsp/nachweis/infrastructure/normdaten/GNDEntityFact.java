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
import lombok.Getter;
import lombok.Setter;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 31.08.2020.
 * @version 1.0
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class GNDEntityFact {

  public static final String ID = "id";
  public static final String URI = "uri";
  public static final String TYPE_NAME = "typeName";
  public static final String GND_IDENTIFIER = "gndIdentifier";
  public static final String PREFERRED_NAME = "preferredName";
  public static final String IDENTIFIER = "identifier";
  public static final String VARIANT_NAME = "variantName";

  public static final String PERSON_TYPE_NAME = "Person";
  public static final String PLACE_TYPE_NAME = "Place";
  public static final String CORPORATE_BODY_TYPE_NAME = "CorporateBody";
  public static final String LANGUAGE_TYPE_NAME = "Language";

  public static final Set<Field> FIELDS = new LinkedHashSet<>(Arrays.asList(
      Field.of(GNDEntityFact.ID),
      Field.of(GNDEntityFact.URI),
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

  @Getter
  @JsonProperty(ID)
  private String id;

  @Getter
  @JsonProperty(URI)
  private String uri;

  @Getter
  @Setter
  @JsonProperty(TYPE_NAME)
  private String typeName;

  @Getter
  @JsonProperty(GND_IDENTIFIER)
  private String gndIdentifier;

  @Getter
  @JsonProperty(PREFERRED_NAME)
  private String preferredName;

  @Getter
  @JsonProperty(IDENTIFIER)
  private Set<Identifier> identifier;

  @Getter
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

  public GNDEntityFact(String id, String gndIdentifier, String preferredName, String typeName,
      Set<Identifier> identifier, Set<VariantName> variantName) {
    this.id = id;
    this.gndIdentifier = gndIdentifier;
    this.preferredName = preferredName;
    this.typeName = typeName;
    this.identifier = identifier;
    this.variantName = variantName;
  }

  public GNDEntityFact(GNDEntityFactBuilder gndEntityFactBuilder) {
    this.id = gndEntityFactBuilder.id;
    this.uri = gndEntityFactBuilder.uri;
    this.typeName = gndEntityFactBuilder.typeName;
    this.gndIdentifier = gndEntityFactBuilder.gndIdentifier;
    this.preferredName = gndEntityFactBuilder.preferredName;
    this.identifier = gndEntityFactBuilder.identifier;
    this.variantName = gndEntityFactBuilder.variantName;
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
    if (!(o instanceof GNDEntityFact that)) {
      return false;
    }
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  public static class GNDEntityFactBuilder {

    private String id;
    private String uri;
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

    public GNDEntityFactBuilder withUri(String uri) {
      this.uri = uri;
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

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("GNDEntityFact{");
    sb.append("id='").append(id).append('\'');
    sb.append(", uri='").append(uri).append('\'');
    sb.append(", typeName='").append(typeName).append('\'');
    sb.append(", gndIdentifier='").append(gndIdentifier).append('\'');
    sb.append(", preferredName='").append(preferredName).append('\'');
    sb.append(", identifier=").append(identifier);
    sb.append(", variantName=").append(variantName);
    sb.append('}');
    return sb.toString();
  }
}
