package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 26.10.2021
 */
@Entity
@Table(name = "Lizenz")
@Cacheable
public class Lizenz implements Serializable {

  @Serial
  private static final long serialVersionUID = -5889019474669752187L;

  @Id
  String id;

  @ElementCollection
  Set<String> uris;

  @Column(name = "beschreibungs_text", columnDefinition = "TEXT")
  String beschreibungsText;

  protected Lizenz() {
    this.id = UUID.randomUUID().toString();
    this.uris = new LinkedHashSet<>();
  }

  public Lizenz(LizenzBuilder lizenzBuilder) {
    this();
    this.uris = lizenzBuilder.uris;
    this.beschreibungsText = lizenzBuilder.beschreibungsText;
  }

  public Lizenz(LizenzTyp lizenzTyp) {
    this();
    this.uris.add(lizenzTyp.getUrl());
    this.beschreibungsText = lizenzTyp.getBeschreibung();
  }

  public String getId() {
    return id;
  }

  public Set<String> getUris() {
    return uris;
  }

  public String getBeschreibungsText() {
    return beschreibungsText;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Lizenz that)) {
      return false;
    }

    return Objects.equals(id, that.id);
  }

  @Override
  public String toString() {
    return "Lizenz{"
        + "id='"
        + id
        + '\''
        + ", uris="
        + (uris != null ? String.join(",", uris) : "null")
        + ", beschreibungsText='"
        + beschreibungsText
        + '\''
        + '}';
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

  public static class LizenzBuilder {

    private Set<String> uris;
    private String beschreibungsText;

    public LizenzBuilder() {
      this.uris = new LinkedHashSet<>();
    }

    public LizenzBuilder withUris(Set<String> uris) {
      this.uris = uris;
      return this;
    }

    public LizenzBuilder addUri(String uri) {
      this.uris.add(uri);
      return this;
    }

    public LizenzBuilder withBeschreibungsText(String beschreibungsText) {
      this.beschreibungsText = beschreibungsText;
      return this;
    }

    public Lizenz build() {
      return new Lizenz(this);
    }
  }
}
