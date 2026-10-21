package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsKomponentenTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KulturObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Status;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.io.Serial;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Dieses Objekt repräsentiert einen Beschreibungsteil in welchem die Metainformation zu einer
 * Handschrift bezogen auf den Beschreibungskopf zusammengefasst sind. Bezogen auf die DFG
 * Erschließungsrichtlinien sind dies Informationen zur Signatur, Sachtitel und zur Schlagzeile.
 *
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 22.01.2020.
 * @version 1.0
 */
@Entity
@Table(name = "BeschreibungsKomponenteKopf")
@Cacheable
public class BeschreibungsKomponenteKopf extends
    GlobaleBeschreibungsKomponente {

  @Serial
  private static final long serialVersionUID = -698293909620708268L;

  @Column
  private Status status;

  @Column(length = 4096)
  private String titel;

  @ManyToMany(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST})
  @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
  @JoinTable(name = "BeschreibungsKomponenteKopf_Grundsprachen")
  private Set<NormdatenReferenz> grundsprachen;

  @Enumerated(EnumType.STRING)
  @Column(name = "kulturObjektTyp")
  private KulturObjektTyp kulturObjektTyp;

  protected BeschreibungsKomponenteKopf() {
  }

  public BeschreibungsKomponenteKopf(
      BeschreibungsKomponenteKopfBuilder builder) {
    super(builder);
    this.status = builder.status;
    this.grundsprachen = builder.grundsprachen;
    this.kulturObjektTyp = builder.kulturObjektTyp;
    this.titel = builder.titel;
  }

  public Status getStatus() {
    return status;
  }

  public String getTitel() {
    return titel;
  }

  public KulturObjektTyp getKulturObjektTyp() {
    return kulturObjektTyp;
  }

  @Override
  public BeschreibungsKomponentenTyp getTyp() {
    return BeschreibungsKomponentenTyp.KOPF;
  }

  public Set<NormdatenReferenz> getGrundsprachen() {
    return grundsprachen;
  }

  public void removeGrundspracheWithoutNORMUUID() {
    this.getGrundsprachen().removeIf(
        sprache -> sprache.getId() == null || sprache.getId().isEmpty() ||
            !sprache.getId().contains("NORM"));
  }

  @Override
  public String toString() {
    return "BeschreibungsKomponenteKopf{"
        + super.toString()
        + "status="
        + status
        + ", titel='"
        + titel
        + '\''
        + ", grundsprachen="
        + grundsprachen.stream().map(g -> String.valueOf(g.getId()))
        .collect(Collectors.joining())
        + ", kulturObjektyp="
        + kulturObjektTyp
        + '}';
  }

  public static class BeschreibungsKomponenteKopfBuilder
      extends GlobaleBeschreibungsKomponenteBuilder<
      BeschreibungsKomponenteKopfBuilder, BeschreibungsKomponenteKopf> {

    private Status status;

    private String titel;

    private Set<NormdatenReferenz> grundsprachen;

    private KulturObjektTyp kulturObjektTyp;

    public BeschreibungsKomponenteKopfBuilder() {
      this.grundsprachen = new HashSet<>();
    }

    public BeschreibungsKomponenteKopfBuilder withStatus(Status zustand) {
      this.status = zustand;
      return this;
    }

    public BeschreibungsKomponenteKopfBuilder withTitel(String titel) {
      this.titel = titel;
      return this;
    }

    public BeschreibungsKomponenteKopfBuilder withKulturObjektTyp(
        KulturObjektTyp kulturObjektTyp) {
      this.kulturObjektTyp = kulturObjektTyp;
      return this;
    }

    public BeschreibungsKomponenteKopfBuilder withGrundsprachen(
        Set<NormdatenReferenz> grundsprachen) {
      this.grundsprachen = grundsprachen;
      return this;
    }

    public BeschreibungsKomponenteKopfBuilder addGrundSprache(
        NormdatenReferenz schreibsprache) {

      if (Objects.nonNull(schreibsprache)) {
        this.grundsprachen.add(schreibsprache);
      }

      return this;
    }

    public BeschreibungsKomponenteKopfBuilder removeGrundSprache(
        NormdatenReferenz schreibsprache) {
      if (Objects.nonNull(schreibsprache)) {
        this.grundsprachen.remove(schreibsprache);
      }

      return this;
    }

    @Override
    public BeschreibungsKomponenteKopf build() {
      return new BeschreibungsKomponenteKopf(this);
    }
  }
}
