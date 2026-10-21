package de.staatsbibliothek.berlin.hsp.domainmodel.aggregates;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.AttributsReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BestandNachweisDTO;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Dokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Referenz;
import jakarta.persistence.Basic;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * <div class="de">Documentation in Deutsch</div> Alle bibliografischen Informationen zu einer
 * mittelalterliche Handschrift
 *
 * <p><div class="en">Documentation in English</div> All bibliographic information on a manuscript
 * or other text-bearing object. This object is a main root aggregate in the context of domain-driven design.
 *
 * <p>* @author Konrad.Eichstaedt@sbb.spk-berlin.de * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * * @since 01.03.2017
 */
@SqlResultSetMapping(
    name = "BestandNachweisDTOMapping",
    classes =
    @ConstructorResult(
        targetClass = BestandNachweisDTO.class,
        columns = {
            @ColumnResult(name = "anzahlKOD", type = Long.class),
            @ColumnResult(name = "anzahlBeschreibungen", type = Long.class),
            @ColumnResult(name = "anzahlBeschreibungenIntern", type = Long.class),
            @ColumnResult(name = "anzahlBeschreibungenExtern", type = Long.class),
            @ColumnResult(name = "anzahlBeschreibungenKatalog", type = Long.class),
            @ColumnResult(name = "anzahlDigitalisat", type = Long.class),
            @ColumnResult(name = "anzahlKatalog", type = Long.class)
        }))
@Entity
@Table(name = "KulturObjektDokument")
@Cacheable
public class KulturObjektDokument implements Dokument, TEIDokument,
    Serializable {

  private static final String ERROR_MSG_NOT_THE_PROPER_TYPE =
      "The Identifikation Objekt has not the proper type;";
  @Serial
  private static final long serialVersionUID = -6414050461052299818L;

  @Getter
  @Setter
  @Id
  private String id;

  @Getter
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "gueltigeIdentifikation_id")
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private Identifikation gueltigeIdentifikation;

  @Getter
  @Setter
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "alternativeIdentifikationen_id")
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private Set<Identifikation> alternativeIdentifikationen;

  @Setter
  @Getter
  @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST,
      CascadeType.REFRESH})
  @JoinTable(
      name = "kulturobjektdokument_digitalisate",
      joinColumns = @JoinColumn(name = "kulturobjektdokument_id"),
      inverseJoinColumns = @JoinColumn(name = "digitalisat_id"))
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private Set<Digitalisat> digitalisate;

  @Setter
  @Getter
  @ElementCollection(fetch = FetchType.LAZY)
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @CollectionTable(name = "kulturobjektdokument_beschreibungenids",
      joinColumns = @JoinColumn(name = "kulturobjektdokument_id"))
  private Set<String> beschreibungenIDs;

  @Getter
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "externeReferenzen_id")
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private Set<Referenz> externeReferenzen;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "kulturobjektdokument_id")
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private Set<PURL> purls;

  @Getter
  @Setter
  @Column
  private String gndIdentifier;

  @Setter
  @Getter
  @Column
  private LocalDateTime registrierungsDatum;

  @Setter
  @Getter
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "komponenten_id")
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private Set<KulturObjektKomponente> komponenten;

  @Setter
  @Getter
  @Column(columnDefinition = "TEXT", length = 2097152)
  @Basic(fetch = FetchType.LAZY)
  private String teiXML;

  @Getter
  @Setter
  @Transient
  private Map<AttributsTyp, AttributsReferenz> attributsReferenzen;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(length = 50, name = "attributsstatustyp")
  private AttributsReferenzStatusTyp attributsReferenzStatusTyp;

  protected KulturObjektDokument() {
    registrierungsDatum = LocalDateTime.now(ZoneId.systemDefault());
    alternativeIdentifikationen = new LinkedHashSet<>();
    digitalisate = new LinkedHashSet<>();
    beschreibungenIDs = new LinkedHashSet<>();
    komponenten = new LinkedHashSet<>();
    externeReferenzen = new LinkedHashSet<>();
    purls = new LinkedHashSet<>();
    attributsReferenzen = new EnumMap<>(AttributsTyp.class);
  }

  public KulturObjektDokument(KulturObjektDokumentBuilder builder) {
    this.id = builder.id;
    this.gndIdentifier = builder.gndIdentifier;
    this.registrierungsDatum = builder.registrierungsDatum;
    this.gueltigeIdentifikation = builder.gueltigeIdentifikation;
    this.alternativeIdentifikationen = builder.alternativeIdentifikationen;
    this.digitalisate = builder.digitalisaten;
    this.beschreibungenIDs = builder.beschreibungenIDs;
    this.komponenten = builder.komponenten;
    this.teiXML = builder.teiXML;
    this.externeReferenzen = builder.externeReferenzen;
    this.purls = builder.purls;
    this.attributsReferenzen = builder.attributsReferenzen;
    this.attributsReferenzStatusTyp = builder.attributsReferenzStatusTyp;
  }

  /**
   * @param gueltigeIdentifikation <code>Identifikation</code> Cultur Object Identifier that is
   *                               used within a library to find a cultural object
   */
  public void setGueltigeIdentifikation(Identifikation gueltigeIdentifikation) {

    if (!IdentTyp.GUELTIGE_SIGNATUR.equals(
        gueltigeIdentifikation.getIdentTyp())) {
      throw new IllegalArgumentException(ERROR_MSG_NOT_THE_PROPER_TYPE);
    }

    this.gueltigeIdentifikation = gueltigeIdentifikation;
  }

  /**
   * @return <code>String</code> Cultur Object Signature that is used within a library to find the
   * proper cultural object within a magazine.
   */
  public String getGueltigeSignatur() {

    if (gueltigeIdentifikation != null
        && gueltigeIdentifikation.getIdent() != null) {
      return gueltigeIdentifikation.getIdent();
    }

    return "";
  }

  /**
   * Adds the given Digitalisat, replacing an already present one with the same identity (remove and
   * add), so re-importing a Digitalisat updates it instead of duplicating it.
   */
  public void addDigitalisat(Digitalisat digitalisat) {
    digitalisate.remove(digitalisat);
    digitalisate.add(digitalisat);
  }

  public void removeDigitalisat(Digitalisat digitalisat) {
    digitalisate.remove(digitalisat);
  }

  public void addBeschreibungsdokument(String beschreibungID) {
    this.beschreibungenIDs.add(beschreibungID);
  }

  public void removeBeschreibungsdokument(String beschreibungID) {
    this.beschreibungenIDs.remove(beschreibungID);
  }

  public Set<PURL> getPURLs() {
    return this.purls;
  }

  public void setPURLs(Set<PURL> purls) {
    this.purls = purls;
  }

  public void addPURL(PURL purl) {
    purls.add(purl);
  }

  public void removePURL(PURL purl) {
    purls.remove(purl);
  }

  public AttributsReferenz getAttributsReferenz(AttributsTyp attributsTyp) {
    return attributsReferenzen.get(attributsTyp);
  }

  public void addAttributsReferenz(AttributsReferenz attributsReferenz) {
    if (attributsReferenzen == null) {
      attributsReferenzen = new EnumMap<>(AttributsTyp.class);
    }
    attributsReferenzen.putIfAbsent(attributsReferenz.getAttributsTyp(), attributsReferenz);
    this.updateAttributsReferenzStatusTyp();

  }

  public void setAttributsReferenzenAndAttributsReferenzStatusTyp(
      Map<AttributsTyp, AttributsReferenz> attributsReferenzen) {
    this.attributsReferenzen = attributsReferenzen;
    this.updateAttributsReferenzStatusTyp();
  }

  public void clearAttributsReferenzen() {
    if (attributsReferenzen == null) {
      attributsReferenzen = new EnumMap<>(AttributsTyp.class);
    }
    attributsReferenzen.clear();
  }

  /**
   * @param referenz <code>Referenz</code> Add external reference for this cultural object.
   */
  public void addExterneReferenz(Referenz referenz) {
    if (Objects.nonNull(referenz)) {
      this.externeReferenzen.add(referenz);
    }
  }

  /**
   * @param referenz <code>Referenz</code> Remove external reference for this cultural object.
   */
  public void removeExterneReferenz(Referenz referenz) {
    this.externeReferenzen.remove(referenz);
  }

  public void updateAttributsReferenzStatusTyp() {

    Predicate<AttributsReferenz> isEmptyReferenz = r ->
        (r == null || r.getReferenzId() == null || r.getReferenzId().isEmpty())
            && (r == null || r.getChangeID() == null ||
            r.getChangeID().isEmpty());

    if (this.attributsReferenzen == null || attributsReferenzen.isEmpty()
        || attributsReferenzen.values().stream().allMatch(isEmptyReferenz)) {
      this.attributsReferenzStatusTyp = AttributsReferenzStatusTyp.LEER;
      return;
    }

    this.attributsReferenzStatusTyp = attributsReferenzen.values().stream()
        .filter(Objects::nonNull)
        .anyMatch(r -> AttributsReferenzStatusTyp.MANUELL.equals(
            r.getAttributsReferenzStatusTyp()))
        ? AttributsReferenzStatusTyp.MANUELL
        : AttributsReferenzStatusTyp.AUTOMATISCH;
  }

  public String getSortKey() {
    return gueltigeIdentifikation != null
        ? gueltigeIdentifikation.getSortKey()
        : null;
  }

  public boolean hasBeschreibungen() {
    return beschreibungenIDs != null && !beschreibungenIDs.isEmpty();
  }

  public boolean hasTeiXml() {
    return teiXML != null && !teiXML.isBlank();
  }

  /**
   * Kontrakt für Index-/Kafka-Propagation: KOD wird nur dann an den Index gesendet, wenn sowohl
   * TEI-XML als auch mindestens eine referenzierte Beschreibung vorhanden sind. Leere KODs gehören
   * nicht in den Such-Index.
   */
  public boolean isIndexable() {
    return hasTeiXml() && hasBeschreibungen();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KulturObjektDokument that = (KulturObjektDokument) o;
    return Objects.equals(this.id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.id);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ",
        KulturObjektDokument.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("gndIdentifier=" + gndIdentifier)
        .add("registrierungsDatum=" + registrierungsDatum)
        .add("gueltigeIdentifikation=" + gueltigeIdentifikation)
        .add("alternativeSignaturen=" + alternativeIdentifikationen.size())
        .add("digitalisaten=" + digitalisate.size())
        .add("beschreibungenIDs=" + beschreibungenIDs.size())
        .add("Komponenten=" + komponenten.size())
        .add("ExterneReferenzen=" + externeReferenzen.size())
        .add("purls=" + purls.size())
        .add("TEIXml=" + teiXML)
        .add("AttributsReferenzen=" + attributsReferenzen.size())
        .add("AttributsReferenzStatusTyp" + attributsReferenzStatusTyp)
        .toString();
  }

  public static class KulturObjektDokumentBuilder {

    private final Set<Identifikation> alternativeIdentifikationen;
    private final Set<KulturObjektKomponente> komponenten;
    private String id;
    private String gndIdentifier;
    private LocalDateTime registrierungsDatum;
    private Identifikation gueltigeIdentifikation;
    private Set<Digitalisat> digitalisaten;
    private Set<String> beschreibungenIDs;
    private String teiXML = "";
    private Set<Referenz> externeReferenzen;
    private Set<PURL> purls;
    private Map<AttributsTyp, AttributsReferenz> attributsReferenzen;
    private AttributsReferenzStatusTyp attributsReferenzStatusTyp;

    public KulturObjektDokumentBuilder() {
      registrierungsDatum = LocalDateTime.now(ZoneId.systemDefault());
      alternativeIdentifikationen = new LinkedHashSet<>();
      digitalisaten = new LinkedHashSet<>();
      beschreibungenIDs = new LinkedHashSet<>();
      komponenten = new LinkedHashSet<>();
      externeReferenzen = new LinkedHashSet<>();
      purls = new LinkedHashSet<>();
      attributsReferenzen = new EnumMap<>(AttributsTyp.class);
    }

    public KulturObjektDokumentBuilder(String id) {
      this();
      this.id = id;
    }

    public KulturObjektDokumentBuilder withId(String id) {
      this.id = id;
      return this;
    }

    public KulturObjektDokumentBuilder withGndIdentifier(String gndIdentifier) {
      this.gndIdentifier = gndIdentifier;
      return this;
    }

    public KulturObjektDokumentBuilder withRegistrierungsDatum(
        LocalDateTime registrierungsDatum) {
      this.registrierungsDatum = registrierungsDatum;
      return this;
    }

    public KulturObjektDokumentBuilder withGueltigerIdentifikation(
        Identifikation gueltigeIdentifikation) {
      if (!IdentTyp.GUELTIGE_SIGNATUR.equals(
          gueltigeIdentifikation.getIdentTyp())) {
        throw new IllegalArgumentException(ERROR_MSG_NOT_THE_PROPER_TYPE);
      }
      this.gueltigeIdentifikation = gueltigeIdentifikation;
      return this;
    }

    public KulturObjektDokumentBuilder addAlternativeIdentifikation(
        Identifikation alternativeIdentifikation) {

      if (IdentTyp.GUELTIGE_SIGNATUR.equals(
          alternativeIdentifikation.getIdentTyp())) {
        throw new IllegalArgumentException(ERROR_MSG_NOT_THE_PROPER_TYPE);
      }

      this.alternativeIdentifikationen.add(alternativeIdentifikation);
      return this;
    }

    public KulturObjektDokumentBuilder addAllAlternativeIdentifikationen(
        Set<Identifikation> alternativeIdentifikationen) {
      this.alternativeIdentifikationen.addAll(alternativeIdentifikationen);
      return this;
    }

    public KulturObjektDokumentBuilder addKomponente(
        KulturObjektKomponente kulturObjektKomponente) {
      this.komponenten.add(kulturObjektKomponente);
      return this;
    }

    public KulturObjektDokumentBuilder withDigitalisaten(
        Set<Digitalisat> digitalisaten) {
      this.digitalisaten = digitalisaten;
      return this;
    }

    public KulturObjektDokumentBuilder addDigitalisat(Digitalisat digitalisat) {
      this.digitalisaten.add(digitalisat);
      return this;
    }

    public KulturObjektDokumentBuilder withBeschreibungsdokumentIDs(
        Set<String> beschreibungIDs) {
      this.beschreibungenIDs = beschreibungIDs;
      return this;
    }

    public KulturObjektDokumentBuilder addBeschreibungsdokumentID(
        String beschreibungsIDs) {
      this.beschreibungenIDs.add(beschreibungsIDs);
      return this;
    }

    public KulturObjektDokumentBuilder withTEIXml(String teiXML) {
      this.teiXML = teiXML;
      return this;
    }

    public KulturObjektDokumentBuilder withExterneReferenzen(
        Set<Referenz> externeReferenzen) {
      this.externeReferenzen = externeReferenzen;
      return this;
    }

    public KulturObjektDokumentBuilder withPURLs(Set<PURL> purls) {
      this.purls = purls;
      return this;
    }

    public KulturObjektDokumentBuilder addPURL(PURL purl) {
      this.purls.add(purl);
      return this;
    }

    public KulturObjektDokumentBuilder withAttributsReferenzen(
        Map<AttributsTyp, AttributsReferenz> attributsReferenzen) {
      this.attributsReferenzen = attributsReferenzen;
      return this;
    }

    public KulturObjektDokumentBuilder withAttributsReferenzenTyp(
        AttributsReferenzStatusTyp attributsReferenzenTyp) {
      this.attributsReferenzStatusTyp = attributsReferenzenTyp;
      return this;
    }

    public KulturObjektDokument build() {
      return new KulturObjektDokument(this);
    }
  }
}
