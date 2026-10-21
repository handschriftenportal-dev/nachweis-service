package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 06.10.2020.
 * @version 1.0
 */
@Table(name = "NormdatenReferenz")
@Entity
@Cacheable
public class NormdatenReferenz implements Serializable {

  public static final String CONCEPT_TYPE = "Concept";
  public static final String PERSON_TYPE_NAME = "Person";
  public static final String ORT_TYPE_NAME = "Place";
  public static final String KOERPERSCHAFT_TYPE_NAME = "CorporateBody";
  public static final String SPRACHE_TYPE_NAME = "Language";
  public static final String DNB_BASE_URL = "http://d-nb.info/gnd/";

  @Serial
  private static final long serialVersionUID = -5532329786024649463L;

  @Id
  String id;

  @Column(length = 255)
  String typeName;

  @Column(length = 1024)
  String name;

  @Column
  String gndID;

  @Column(length = 4096)
  String uri;

  @ElementCollection(fetch = FetchType.LAZY, targetClass = Identifikator.class)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @CollectionTable(
      name = "NormdatenReferenz_Identifikator",
      joinColumns =
      @JoinColumn(
          name = "normdatenReferenz_id",
          foreignKey = @ForeignKey(name = "FK_NR_Identifikator_NormdatenReferenz")))
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  Set<Identifikator> identifikator;

  @ElementCollection(fetch = FetchType.EAGER, targetClass = VarianterName.class)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @CollectionTable(
      name = "NormdatenReferenz_VarianterName",
      joinColumns =
      @JoinColumn(
          name = "normdatenReferenz_id",
          foreignKey = @ForeignKey(name = "FK_NR_VarianterName_NormdatenReferenz")))
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  Set<VarianterName> varianterName;

  protected NormdatenReferenz() {
  }

  public NormdatenReferenz(String id, String name, String gndID) {
    this(id, name, gndID, null);
  }

  public NormdatenReferenz(String id, String name, String gndID,
      String typeName) {
    this.id = id;
    this.name = name;
    this.gndID = gndID;
    this.typeName = typeName;
    identifikator = new HashSet<>();
    varianterName = new HashSet<>();
  }

  public NormdatenReferenz(NormdatenReferenzBuilder builder) {
    this(builder.id, builder.name, builder.gndID, builder.typeName);
    this.identifikator.addAll(builder.identifikator);
    this.varianterName.addAll(builder.varianterName);
    this.uri = builder.uri;
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

  public void setGndID(String gndID) {
    this.gndID = gndID;
  }

  public String getUri() {
    return uri;
  }

  @JsonIgnore
  public String getGndUrl() {
    return Optional.ofNullable(gndID).map(gid -> DNB_BASE_URL + gid)
        .orElse(null);
  }

  public Set<Identifikator> getIdentifikator() {
    return identifikator;
  }

  public Set<Identifikator> getIdentifikatorByTypeName(String typeName) {
    return identifikator.stream()
        .filter(
            i ->
                (Objects.isNull(typeName) && Objects.isNull(i.getType()))
                    || (Objects.nonNull(i.getType()) && i.getType()
                    .equals(typeName)))
        .collect(Collectors.toSet());
  }

  public Set<VarianterName> getVarianterName() {
    return varianterName;
  }

  public Set<VarianterName> getVarianterNameBylanguageCode(
      String languageCode) {
    return varianterName.stream()
        .filter(
            i ->
                (Objects.isNull(languageCode) && Objects.isNull(
                    i.getLanguageCode()))
                    || (Objects.nonNull(i.getLanguageCode())
                    && i.getLanguageCode().equals(languageCode)))
        .collect(Collectors.toSet());
  }

  @JsonIgnore
  public String getVarianteNamenAlsString() {
    return varianterName.stream().map(VarianterName::getName)
        .collect(Collectors.joining("; "));
  }

  @JsonIgnore
  public String getVariantName(String languageCode) {
    if (languageCode == null || varianterName == null
        || varianterName.isEmpty()) {
      return name;
    }

    return this.varianterName.stream()
        .filter(v -> languageCode.equals(v.getLanguageCode()))
        .map(VarianterName::getName)
        .findAny()
        .orElse(name);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NormdatenReferenz that = (NormdatenReferenz) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("NormdatenReferenz{");
    sb.append("gndID='").append(gndID).append('\'');
    sb.append(", id='").append(id).append('\'');
    sb.append(", typeName='").append(typeName).append('\'');
    sb.append(", name='").append(name).append('\'');
    sb.append(", varianteNamen='")
        .append(
            varianterName.stream().map(VarianterName::getName)
                .collect(Collectors.joining(",")));
    sb.append(", uri='").append(uri).append('\'');
    sb.append('}');
    return sb.toString();
  }

  public static class NormdatenReferenzBuilder {

    private String id;
    private String typeName;
    private String name;
    private String gndID;
    private String uri;
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

    public NormdatenReferenzBuilder withUri(String uri) {
      this.uri = uri;
      return this;
    }

    public NormdatenReferenzBuilder withIdentifikator(
        Set<Identifikator> identifikator) {
      this.identifikator.clear();
      if (Objects.nonNull(identifikator) && !identifikator.isEmpty()) {
        this.identifikator.addAll(identifikator);
      }
      return this;
    }

    public NormdatenReferenzBuilder addVarianterName(
        VarianterName varianterName) {
      if (Objects.nonNull(varianterName)) {
        this.varianterName.add(varianterName);
      }
      return this;
    }

    public NormdatenReferenzBuilder withVarianterName(
        Set<VarianterName> varianterName) {
      this.varianterName.clear();
      if (Objects.nonNull(varianterName) && !varianterName.isEmpty()) {
        this.varianterName.addAll(varianterName);
      }
      return this;
    }

    public NormdatenReferenzBuilder addIdentifikator(
        Identifikator identifikator) {
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
