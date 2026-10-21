package de.staatsbibliothek.berlin.hsp.domainmodel.aggregates;

import de.staatsbibliothek.berlin.hsp.domainmodel.HSP_UUI_Factory;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.ApprovalRequest;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.GlobaleBeschreibungsKomponente;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation.PublikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Approvable;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ApprovalType;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsKomponentenTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Dokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationStatus;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Referenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import jakarta.persistence.Basic;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Dieses Objekt repräsentiert eine durch einen Erschließer erstellte Handschriftenbeschreibung.
 * Eine Handschriftenbeschreibung stellt Informationen zu einer Handschrift dar, welche durch das
 * KulturObjektDokument repräsentiert wird.
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 06.06.19
 */
@Entity
@Table(name = "beschreibungsdokument")
@Cacheable
public class Beschreibung implements Approvable, Dokument, TEIDokument {

  @Id
  String id;
  @Column
  String kodID;

  @OneToMany(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST,
          CascadeType.MERGE})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  Set<Referenz> externeIDs;

  @OneToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST,
          CascadeType.MERGE})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  Identifikation gueltigeIdentifikation;

  @ElementCollection(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @CollectionTable(name = "beschreibung_altidentifier",
      joinColumns = @JoinColumn(name = "beschreibung_id"))
  Set<String> altIdentifier;

  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST,
          CascadeType.MERGE})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  NormdatenReferenz beschreibungsSprache;

  @Column
  LocalDateTime erstellungsDatum;
  @Column
  LocalDateTime aenderungsDatum;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  Set<GlobaleBeschreibungsKomponente> beschreibungsStruktur;

  @ManyToMany(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST,
          CascadeType.MERGE})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  Set<NormdatenReferenz> beschreibungsBeteiligte;

  @Column(name = "teiXML", columnDefinition = "TEXT", length = 2097152)
  @Basic(fetch = FetchType.LAZY)
  String teiXML;

  @Column
  String katalogID;

  @OneToOne(
      fetch = FetchType.EAGER,
      cascade = CascadeType.ALL)
  Publikation publikation;

  @Enumerated(EnumType.STRING)
  @Column(name = "verwaltungstyp", length = 25)
  VerwaltungsTyp verwaltungsTyp;

  @Enumerated(EnumType.STRING)
  @Column(name = "beschreibungstyp", length = 25)
  BeschreibungsTyp beschreibungsTyp;

  @Enumerated(EnumType.STRING)
  @Column(name = "dokumentObjektTyp", length = 25)
  DokumentObjektTyp dokumentObjektTyp;

  @ManyToMany(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST,
          CascadeType.MERGE})
  @JoinTable(name = "beschreibungsdokument_autoren")
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  Set<NormdatenReferenz> autoren;

  @ElementCollection(fetch = FetchType.LAZY)
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @CollectionTable(name = "beschreibung_bearbeiterids",
      joinColumns = @JoinColumn(name = "beschreibung_id"))
  Set<String> bearbeiterIDs;

  protected Beschreibung() {
    externeIDs = new LinkedHashSet<>();
    beschreibungsBeteiligte = new LinkedHashSet<>();
    beschreibungsStruktur = new LinkedHashSet<>();
    altIdentifier = new LinkedHashSet<>();
    autoren = new LinkedHashSet<>();
    bearbeiterIDs = new LinkedHashSet<>();
  }

  public Beschreibung(BeschreibungsBuilder builder) {
    this.id = builder.id;
    this.kodID = builder.kodID;
    this.teiXML = builder.teiXML;
    this.externeIDs = builder.externeIDs;
    this.beschreibungsSprache = builder.beschreibungsSprache;
    this.erstellungsDatum = builder.erstellungsDatum;
    this.aenderungsDatum = builder.aenderungsDatum;
    this.beschreibungsBeteiligte = builder.beschreibungsBeteiligte;
    this.beschreibungsStruktur = builder.beschreibungsStruktur;
    this.katalogID = builder.katalogID;
    this.altIdentifier = builder.altIdentifier;
    this.verwaltungsTyp = builder.verwaltungsTyp;
    this.beschreibungsTyp = builder.beschreibungsTyp;
    this.autoren = builder.autoren;
    this.dokumentObjektTyp = builder.dokumentObjektTyp;
    this.bearbeiterIDs = builder.bearbeiterIDs;
    this.publikation = builder.publikation;

    if (builder.gueltigeIdentifikation != null) {
      this.setGueltigeIdentifikation(builder.gueltigeIdentifikation);
    }
  }

  public Optional<BeschreibungsKomponenteKopf> findKopf() {
    if (this.beschreibungsStruktur == null || beschreibungsStruktur.isEmpty()) {
      return Optional.empty();
    }

    return beschreibungsStruktur.stream()
        .filter(k -> k.getTyp().equals(BeschreibungsKomponentenTyp.KOPF))
        .map(k -> (BeschreibungsKomponenteKopf) k).findFirst();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public void onApprovalCreated(ApprovalRequest request) {
    publikation.onApprovalCreated(request);
  }

  @Override
  public void onApproved() {
    publikation.onApproved();
  }

  @Override
  public void onRejected() {
    publikation.onRejected();
  }

  @Override
  public void onApprovalWithdrawn() {
    publikation.onApprovalWithdrawn();
  }

  @Override
  public ApprovalType getApprovalType() {
    return publikation.getApprovalType();
  }

  @Override
  public boolean canRequestApproval() {
    if (publikation == null) {
      return false;
    }
    return publikation.canRequestApproval();
  }

  @Override
  public boolean canBeApproved() {
    if (publikation == null) {
      return false;
    }
    return publikation.canBeApproved();
  }

  public Optional<ApprovalRequest> getActiveApprovalRequest() {
    return publikation.getActiveApprovalRequest();
  }

  public boolean hasActiveApprovalRequest() {
    return publikation.hasActiveApprovalRequest();
  }

  public boolean isEditable() {
    return !hasActiveApprovalRequest()
        && getPublikation().getStatus() == PublikationStatus.DRAFT;
  }

  public boolean hasBearbeiter(Bearbeiter bearbeiter) {
    if (bearbeiter == null || bearbeiter.getId() == null) {
      return false;
    }
    return hasBearbeiter(bearbeiter.getId());
  }

  public boolean hasBearbeiter(String bearbeiterID) {
    return bearbeiterIDs.contains(bearbeiterID);
  }

  public String getKodID() {
    return kodID;
  }

  public void setKodID(String kod) {
    this.kodID = kod;
  }

  public Set<Referenz> getExterneIDs() {
    return externeIDs;
  }

  public void setExterneIDs(Set<Referenz> externeIDs) {
    this.externeIDs = externeIDs;
  }

  public NormdatenReferenz getBeschreibungsSprache() {
    return beschreibungsSprache;
  }

  public void setBeschreibungsSprache(NormdatenReferenz beschreibungsSprache) {
    this.beschreibungsSprache = beschreibungsSprache;
  }

  public LocalDateTime getErstellungsDatum() {
    return erstellungsDatum;
  }

  public void setErstellungsDatum(LocalDateTime erstellungsDatum) {
    this.erstellungsDatum = erstellungsDatum;
  }

  public LocalDateTime getAenderungsDatum() {
    return aenderungsDatum;
  }

  public void setAenderungsDatum(LocalDateTime aenderungsDatum) {
    this.aenderungsDatum = aenderungsDatum;
  }

  public Set<GlobaleBeschreibungsKomponente> getBeschreibungsStruktur() {
    return beschreibungsStruktur;
  }

  public void setBeschreibungsStruktur(
      Set<GlobaleBeschreibungsKomponente> beschreibungsStruktur) {
    this.beschreibungsStruktur = beschreibungsStruktur;
  }

  public Set<NormdatenReferenz> getBeschreibungsBeteiligte() {
    return beschreibungsBeteiligte;
  }

  public void setBeschreibungsBeteiligte(
      Set<NormdatenReferenz> beschreibungsBeteiligte) {
    this.beschreibungsBeteiligte = beschreibungsBeteiligte;
  }

  public String getKatalogID() {
    return this.katalogID;
  }

  public void setKatalogID(String katalog) {
    this.katalogID = katalog;
  }

  public void addBeschreibungsBeteiligter(NormdatenReferenz beteiligte) {
    if (Objects.nonNull(beteiligte)) {
      beschreibungsBeteiligte.add(beteiligte);
    }
  }

  public void removeBeschreibungsBeteiligter(NormdatenReferenz beteiligte) {
    if (Objects.nonNull(beteiligte)) {
      beschreibungsBeteiligte.remove(beteiligte);
    }
  }

  public Publikation getPublikation() {
    return publikation;
  }

  public void setPublikation(Publikation publikation) {
    this.publikation = publikation;
  }

  public LocalDateTime getErstpublikationsDatum() {
    if (publikation == null || publikation.getErstpublikationsDatum() == null) {
      return LocalDateTime.of(1970, 1, 1, 0, 0, 0);
    }
    return publikation.getErstpublikationsDatum();
  }

  public boolean isPublished() {
    if (publikation == null) {
      return false;
    }
    return publikation.isPublished();
  }

  public String getTeiXML() {
    return teiXML;
  }

  public void setTeiXML(String teiXML) {
    this.teiXML = teiXML;
  }

  public Optional<Identifikation> getGueltigeIdentifikation() {
    return Optional.ofNullable(gueltigeIdentifikation);
  }

  public void setGueltigeIdentifikation(Identifikation gueltigeIdentifikation) {
    Objects.requireNonNull(gueltigeIdentifikation);
    if (!IdentTyp.GUELTIGE_SIGNATUR.equals(
        gueltigeIdentifikation.getIdentTyp())) {
      throw new IllegalArgumentException("Identifikation muss gueltig sein");
    }
    this.gueltigeIdentifikation = gueltigeIdentifikation;
  }

  public String getGueltigeSignatur() {
    return getGueltigeIdentifikation()
        .map(Identifikation::getIdent)
        .orElse(null);
  }

  public String formatSignatureLabel() {
    return getGueltigeIdentifikation()
        .map(Identifikation::formatSignatureLabel)
        .orElse("ohne Signatur");
  }

  public String getAufbewahrungsOrt() {
    return getGueltigeIdentifikation()
        .map(Identifikation::getAufbewahrungsOrt)
        .map(NormdatenReferenz::getName)
        .orElse(null);
  }

  public Set<String> getAltIdentifier() {
    return altIdentifier;
  }

  public void setAltIdentifier(Set<String> altIdentifier) {
    this.altIdentifier = altIdentifier;
  }

  public VerwaltungsTyp getVerwaltungsTyp() {
    return verwaltungsTyp;
  }

  public void setVerwaltungsTyp(
      VerwaltungsTyp verwaltungsTyp) {
    this.verwaltungsTyp = verwaltungsTyp;
  }

  public Set<NormdatenReferenz> getAutoren() {
    return autoren;
  }

  public void makeBeschreibungIntern() {
    this.verwaltungsTyp = VerwaltungsTyp.INTERN;
  }

  public void makeBeschreibungExtern() {
    this.verwaltungsTyp = VerwaltungsTyp.EXTERN;
  }

  public boolean isBeschreibungIntern() {
    return VerwaltungsTyp.INTERN.equals(this.verwaltungsTyp);
  }

  public boolean isGueltigeIdentifikationValid() {
    return this.getGueltigeIdentifikation().isPresent()
        && Objects.nonNull(this.getGueltigeIdentifikation().get().getBesitzer())
        && Objects.nonNull(
        this.getGueltigeIdentifikation().get().getAufbewahrungsOrt());
  }

  public Optional<String> getTitel() {
    return beschreibungsStruktur.stream()
        .filter(k -> BeschreibungsKomponentenTyp.KOPF.equals(k.getTyp()))
        .findFirst()
        .map(BeschreibungsKomponenteKopf.class::cast)
        .map(BeschreibungsKomponenteKopf::getTitel);
  }

  public BeschreibungsTyp getBeschreibungsTyp() {
    return beschreibungsTyp;
  }

  public Lizenz getLizenz() {
    if (publikation == null) {
      return null;
    }
    return publikation.getLizenz();
  }

  public DokumentObjektTyp getDokumentObjektTyp() {
    return dokumentObjektTyp;
  }

  public Set<PURL> getPURLs() {
    if (publikation == null) {
      return new LinkedHashSet<>();
    }
    return publikation.getPURLs();
  }

  public Set<String> getBearbeiterIDs() {
    return bearbeiterIDs;
  }

  public void setBearbeiterIDs(Set<String> bearbeiterIDs) {
    this.bearbeiterIDs = bearbeiterIDs;
  }

  public String generateHspID() {
    altIdentifier.add(this.id);
    this.setId("HSP-" + UUID.randomUUID());
    return this.id;
  }

  public boolean hasTeiXml() {
    return teiXML != null && !teiXML.isBlank();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Beschreibung that)) {
      return false;
    }
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

  public String toString(boolean withID) {
    final StringBuilder sb = new StringBuilder("Beschreibung{");
    sb.append("kodID='").append(kodID).append('\'');
    if (withID) {
      sb.append(", id='").append(id).append('\'');
    }
    sb.append(", externeIDs=").append(externeIDs);
    sb.append(", altIdentifier=").append(altIdentifier);
    sb.append(", beschreibungsSprache=").append(beschreibungsSprache);
    sb.append(", erstellungsDatum=").append(erstellungsDatum);
    sb.append(", aenderungsDatum=").append(aenderungsDatum);
    sb.append(", beschreibungsStruktur=").append(beschreibungsStruktur);
    sb.append(", beschreibungsBeteiligte=").append(beschreibungsBeteiligte);
    sb.append(", teiXML='").append(teiXML).append('\'');
    sb.append(", katalogID='").append(katalogID).append('\'');
    sb.append(", publikation=").append(publikation);
    sb.append(", verwaltungsTyp=").append(verwaltungsTyp);
    sb.append(", beschreibungsTyp=").append(beschreibungsTyp);
    sb.append('}');
    return sb.toString();
  }

  public static class BeschreibungsBuilder {

    private final String kodID;
    private final Set<Referenz> externeIDs;
    private final Set<GlobaleBeschreibungsKomponente> beschreibungsStruktur;
    private final Set<NormdatenReferenz> beschreibungsBeteiligte;
    private final Identifikation gueltigeIdentifikation;
    private final Set<String> altIdentifier;
    private final Set<NormdatenReferenz> autoren;
    private final Set<String> bearbeiterIDs;
    private final String id;
    private NormdatenReferenz beschreibungsSprache;
    private LocalDateTime erstellungsDatum;
    private LocalDateTime aenderungsDatum;
    private String katalogID;
    private Publikation publikation;
    private String teiXML;
    private VerwaltungsTyp verwaltungsTyp;
    private BeschreibungsTyp beschreibungsTyp;
    private DokumentObjektTyp dokumentObjektTyp;

    public BeschreibungsBuilder(String id, String kodID,
        Identifikation gueltigeIdentifikation) {
      this.id = (id == null || id.isBlank()) ? HSP_UUI_Factory.generate() : id;
      this.kodID = kodID;
      this.gueltigeIdentifikation = gueltigeIdentifikation;
      this.externeIDs = new LinkedHashSet<>();
      this.beschreibungsBeteiligte = new LinkedHashSet<>();
      this.beschreibungsStruktur = new LinkedHashSet<>();
      this.altIdentifier = new LinkedHashSet<>();
      this.autoren = new LinkedHashSet<>();
      this.bearbeiterIDs = new LinkedHashSet<>();
      this.publikation = new PublikationBuilder().build();
    }

    public BeschreibungsBuilder withTEIXml(String teiXML) {
      this.teiXML = teiXML;
      return this;
    }

    public BeschreibungsBuilder withVerwaltungsTyp(
        VerwaltungsTyp verwaltungsTyp) {
      this.verwaltungsTyp = verwaltungsTyp;
      return this;
    }

    public BeschreibungsBuilder withBeschreibungsTyp(
        BeschreibungsTyp beschreibungsTyp) {
      this.beschreibungsTyp = beschreibungsTyp;
      return this;
    }

    public BeschreibungsBuilder withDokumentObjectTyp(
        DokumentObjektTyp dokumentObjektTyp) {
      this.dokumentObjektTyp = dokumentObjektTyp;
      return this;
    }

    public BeschreibungsBuilder withExterneIDs(Set<Referenz> externeIDs) {
      if (Objects.nonNull(externeIDs)) {
        this.externeIDs.clear();
        this.externeIDs.addAll(externeIDs);
      }
      return this;
    }

    public BeschreibungsBuilder addAutoren(Set<NormdatenReferenz> autoren) {
      if (autoren != null) {
        this.autoren.addAll(autoren);
      }
      return this;
    }

    public BeschreibungsBuilder addAutor(NormdatenReferenz autor) {
      if (autor != null) {
        this.autoren.add(autor);
      }
      return this;
    }

    public BeschreibungsBuilder addExterneID(Referenz externeID) {
      this.externeIDs.add(externeID);
      return this;
    }

    public BeschreibungsBuilder withBeschreibungssprache(
        NormdatenReferenz beschreibungsSprache) {
      this.beschreibungsSprache = beschreibungsSprache;
      return this;
    }

    public BeschreibungsBuilder withErstellungsDatum(
        LocalDateTime erstellungsDatum) {
      this.erstellungsDatum = erstellungsDatum;
      return this;
    }

    public BeschreibungsBuilder withAenderungsDatum(
        LocalDateTime aenderungsDatum) {
      this.aenderungsDatum = aenderungsDatum;
      return this;
    }

    public BeschreibungsBuilder withKatalog(String katalogID) {
      this.katalogID = katalogID;
      return this;
    }

    public BeschreibungsBuilder addBeschreibungsKomponente(
        GlobaleBeschreibungsKomponente globaleBeschreibungsKomponente) {
      if (Objects.nonNull(globaleBeschreibungsKomponente)) {
        beschreibungsStruktur.add(globaleBeschreibungsKomponente);
      }
      return this;
    }

    public BeschreibungsBuilder addBeschreibungsBeteiligter(
        NormdatenReferenz beteiligte) {
      if (Objects.nonNull(beteiligte)) {
        beschreibungsBeteiligte.add(beteiligte);
      }
      return this;
    }

    public BeschreibungsBuilder withPublikation(Publikation publikation) {
      this.publikation = publikation;
      return this;
    }

    public BeschreibungsBuilder addAltIdentifier(String altIdentifier) {
      if (Objects.nonNull(altIdentifier) && !altIdentifier.isEmpty()) {
        this.altIdentifier.add(altIdentifier);
      }
      return this;
    }

    public BeschreibungsBuilder withBearbeiterIds(Bearbeiter bearbeiter) {
      this.bearbeiterIDs.add(bearbeiter.getId());
      return this;
    }

    public Beschreibung build() {
      return new Beschreibung(this);
    }
  }
}
