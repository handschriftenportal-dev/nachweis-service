package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsKomponentenTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Dokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KulturObjektKomponenteTyp;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 01.03.2017
 */
@Entity
@Table(name = "globalebeschreibungskomponente")
@Cacheable
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class GlobaleBeschreibungsKomponente implements Dokument, Serializable {

  @Serial
  private static final long serialVersionUID = 6191989940238744630L;

  @Id
  protected String id;

  @Column
  protected String beschreibungsFreitext;

  @Enumerated(EnumType.STRING)
  @Column
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  protected KulturObjektKomponenteTyp kulturObjektKomponenteTyp;

  @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.REFRESH,
      CascadeType.PERSIST,
      CascadeType.MERGE})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  protected Set<Identifikation> identifikationen;

  protected GlobaleBeschreibungsKomponente() {
    super();
    identifikationen = new LinkedHashSet<>();
  }

  protected GlobaleBeschreibungsKomponente(GlobaleBeschreibungsKomponenteBuilder builder) {
    this.id = builder.id;
    this.beschreibungsFreitext = builder.beschreibungsFreitext;
    this.kulturObjektKomponenteTyp = builder.kulturObjektKomponenteTyp;
    this.identifikationen = builder.identifikationen;
  }

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getBeschreibungsFreitext() {
    return this.beschreibungsFreitext;
  }

  public void setBeschreibungsFreitext(String beschreibungsFreitext) {
    this.beschreibungsFreitext = beschreibungsFreitext;
  }

  public KulturObjektKomponenteTyp getKulturObjektKomponenteTyp() {
    return this.kulturObjektKomponenteTyp;
  }

  public void setKulturObjektKomponenteTyp(KulturObjektKomponenteTyp kulturObjektKomponenteTyp) {
    this.kulturObjektKomponenteTyp = kulturObjektKomponenteTyp;
  }

  public Set<Identifikation> getIdentifikationen() {
    return identifikationen;
  }

  @Transient
  public abstract BeschreibungsKomponentenTyp getTyp();

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GlobaleBeschreibungsKomponente that = (GlobaleBeschreibungsKomponente) o;
    return Objects.equals(this.id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.id);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", GlobaleBeschreibungsKomponente.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("beschreibungsFreitext='" + beschreibungsFreitext + "'")
        .add("kulturObjektKomponenteTyp=" + kulturObjektKomponenteTyp)
        .add("identifikationen=" + identifikationen.size())
        .toString();
  }

  public abstract static class GlobaleBeschreibungsKomponenteBuilder<T, V> {

    protected String id;
    protected String beschreibungsFreitext;
    protected KulturObjektKomponenteTyp kulturObjektKomponenteTyp;
    protected Set<Identifikation> identifikationen;

    public GlobaleBeschreibungsKomponenteBuilder() {
      identifikationen = new LinkedHashSet<>();
    }

    public T withId(String id) {
      this.id = id;
      return (T) this;
    }

    public T withBeschreibungsFreitext(String beschreibungsFreitext) {
      this.beschreibungsFreitext = beschreibungsFreitext;
      return (T) this;
    }

    public T withKulturObjektKomponenteTyp(KulturObjektKomponenteTyp kulturObjektKomponenteTyp) {
      this.kulturObjektKomponenteTyp = kulturObjektKomponenteTyp;
      return (T) this;
    }

    public T withAlternativeIdentifikationen(Set<Identifikation> identifikationen) {
      this.identifikationen = identifikationen;
      return (T) this;
    }

    public abstract V build();
  }
}
