package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationStatus.DRAFT;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationStatus.IN_REVIEW;
import static de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationStatus.PUBLISHED;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Approvable;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ApprovalType;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Lizenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.LizenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationStatus;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Formula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a publication of a Beschreibung. A publication can be in DRAFT status (not publicly
 * visible) or PUBLISHED status (live and accessible).
 *
 * <p>The approval workflow is handled separately by {@link ApprovalRequest}.
 * This class implements {@link Approvable} to receive callbacks during the approval process.</p>
 *
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 22.01.2020.
 * @version 2.0
 */
@Entity
@Table(name = "Publikation")
@Cacheable
public class Publikation {

  private static final Logger log = LoggerFactory.getLogger(Publikation.class);

  @Transient
  private static final int DEFAULT_AUFLAGENNUMMER = 1;
  @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(
      name = "publikation_approval",
      joinColumns = @JoinColumn(name = "publikation_id"),
      inverseJoinColumns = @JoinColumn(name = "approval_request_id")
  )
  private final Set<ApprovalRequest> approvalRequests = new LinkedHashSet<>();
  @Id
  private String id;
  @Column(name = "auflagen_nummer")
  private Integer auflagenNummer;
  @OneToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST,
          CascadeType.MERGE})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  @JoinColumn(name = "previousPublikation_id")
  private Publikation previousPublikation;
  @OneToOne(
      fetch = FetchType.LAZY,
      cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST,
          CascadeType.MERGE})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private Lizenz lizenz;
  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "publikation_id")
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private Set<PURL> purls = new LinkedHashSet<>();
  @Column(name = "erstpublikations_datum")
  private LocalDateTime erstpublikationsDatum;

  @Column(name = "publikations_datum_hsp")
  private LocalDateTime publikationsDatumHSP;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private PublikationStatus status;

  /**
   * Read-only existence flag for the publikation_tei row. Computed via SQL EXISTS at SELECT time,
   * not stored — keeps the snapshot blob completely off the Publikation graph while still letting
   * the UI gate the download button without an extra round-trip.
   */
  @Formula("(EXISTS(SELECT 1 FROM publikation_tei pt WHERE pt.publikation_id = id))")
  private boolean hasTeiSnapshot;

  protected Publikation() {
  }

  private Publikation(PublikationBuilder builder) {
    this();
    this.id = builder.id;
    this.status = builder.status;
    this.erstpublikationsDatum =
        builder.erstpublikationsDatum != null
            ? builder.erstpublikationsDatum
            : builder.publikationsDatumHSP;
    this.publikationsDatumHSP = builder.publikationsDatumHSP;
    this.previousPublikation = builder.previous;
    this.auflagenNummer = calculateAuflagenNummer();
    this.lizenz = builder.lizenz;
    this.purls.addAll(builder.purls);
    this.approvalRequests.addAll(builder.approvalRequests);
  }

  public void onApprovalCreated(ApprovalRequest request) {
    this.addApprovalRequest(request);
    this.status = IN_REVIEW;
    log.info("Approval {} requested for Publikation {}, status set to IN_REVIEW", request.getId(),
        this.id);
  }

  public void onApproved() {
    LocalDateTime now = LocalDateTime.now();
    if (this.erstpublikationsDatum == null) {
      this.erstpublikationsDatum = now;
    }
    this.publikationsDatumHSP = now;
    this.status = PUBLISHED;
    log.info("Publikation {} approved and published", id);
  }

  public void onRejected() {
    this.status = DRAFT;
    log.info("Publikation {} approval rejected, status set back to DRAFT", id);
  }

  public void onApprovalWithdrawn() {
    this.status = DRAFT;
    log.info("Approval request withdrawn for publication {}, status set back to DRAFT", id);
  }

  public ApprovalType getApprovalType() {
    return ApprovalType.PUBLICATION;
  }

  public boolean canRequestApproval() {
    return status == DRAFT && !hasActiveApprovalRequest();
  }

  public boolean canBeApproved() {
    return status == IN_REVIEW && hasActiveApprovalRequest();
  }

  public void addPURLs(Set<PURL> newPurls) {
    if (newPurls != null) {
      if (this.purls == null) {
        this.purls = new LinkedHashSet<>();
      }
      this.purls.addAll(newPurls);
    }
  }

  public void addPURL(PURL purl) {
    if (purl != null) {
      if (this.purls == null) {
        this.purls = new LinkedHashSet<>();
      }
      this.purls.add(purl);
    }
  }

  public boolean hasInternalPURL() {
    if (purls == null) {
      return false;
    }
    return purls.stream()
        .anyMatch(purl -> purl.getTyp() == PURLTyp.INTERNAL);
  }

  public boolean isDraft() {
    return status == DRAFT;
  }

  public boolean isInReview() {
    return status == IN_REVIEW;
  }

  public boolean isPublished() {
    return status == PUBLISHED;
  }

  public String getId() {
    return id;
  }

  public LocalDateTime getErstpublikationsDatum() {
    return erstpublikationsDatum;
  }

  public LocalDateTime getPublikationsDatumHSP() {
    return publikationsDatumHSP;
  }

  public void setPublikationsDatumHSP(LocalDateTime publikationsDatumHSP) {
    if (erstpublikationsDatum == null) {
      erstpublikationsDatum = publikationsDatumHSP;
    }
    this.publikationsDatumHSP = publikationsDatumHSP;
  }

  public Integer getAuflagenNummer() {
    return auflagenNummer;
  }

  public Optional<Publikation> getPreviousPublikation() {
    return Optional.ofNullable(previousPublikation);
  }

  public Lizenz getLizenz() {
    return lizenz;
  }

  public void setLizenz(Lizenz lizenz) {
    this.lizenz = lizenz;
  }

  public void setLizenz(LizenzTyp lizenzTyp) {
    this.lizenz = new Lizenz(lizenzTyp);
  }

  public Set<PURL> getPURLs() {
    return purls;
  }

  public boolean hasTeiSnapshot() {
    return hasTeiSnapshot;
  }

  public PublikationStatus getStatus() {
    return status;
  }

  public Set<ApprovalRequest> getApprovalRequests() {
    return approvalRequests;
  }

  public void addApprovalRequest(ApprovalRequest request) {
    if (request == null) {
      return;
    }

    getActiveApprovalRequest()
        .ifPresent(activeRequest -> {
              throw new IllegalStateException(
                  "Publication " + id + " has an active approval request " + activeRequest.getId());
            }
        );
    this.approvalRequests.add(request);

  }

  public Optional<ApprovalRequest> getActiveApprovalRequest() {
    return approvalRequests.stream().filter(ApprovalRequest::isActive).findFirst();
  }

  public boolean hasActiveApprovalRequest() {
    return approvalRequests.stream().anyMatch(ApprovalRequest::isActive);
  }

  private int calculateAuflagenNummer() {
    if (previousPublikation == null) {
      return DEFAULT_AUFLAGENNUMMER;
    }
    int neueAuflage = previousPublikation.getAuflagenNummer() + 1;
    log.info("Neue Auflagennummer für Publikation {}: {}", id, neueAuflage);
    return neueAuflage;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Publikation that)) {
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
    return "Publikation{" + "id='" + id + '\''
        + ", erstpublikationsDatum=" + erstpublikationsDatum
        + ", publikationsDatumHSP=" + publikationsDatumHSP
        + ", auflagenNummer=" + auflagenNummer
        + ", vorherigePublikation=" + (previousPublikation != null ? previousPublikation.getId()
        : null)
        + ", lizenz=" + lizenz
        + ", status=" + status
        + ", purls=" + (purls != null ? purls.size() : 0)
        + '}';
  }

  public static class PublikationBuilder {

    private List<ApprovalRequest> approvalRequests = new ArrayList<>();
    private String id;
    private PublikationStatus status = DRAFT;
    private LocalDateTime erstpublikationsDatum;
    private LocalDateTime publikationsDatumHSP;
    private Publikation previous;
    private Lizenz lizenz;
    private Set<PURL> purls = new LinkedHashSet<>();
    private boolean generateNewId = false;

    public PublikationBuilder() {
      generateNewId = true;
    }

    public PublikationBuilder(Publikation copy) {
      this();
      this.id = copy.getId();
      this.status = copy.getStatus();
      this.erstpublikationsDatum = copy.getErstpublikationsDatum();
      this.publikationsDatumHSP = copy.getPublikationsDatumHSP();
      this.approvalRequests.addAll(copy.getApprovalRequests());
      this.previous = copy.getPreviousPublikation().orElse(null);
      this.lizenz = copy.getLizenz();
      this.purls = new LinkedHashSet<>(copy.getPURLs());
    }

    public PublikationBuilder withNewId() {
      generateNewId = true;
      return this;
    }

    public PublikationBuilder withPrevious(Publikation previous) {
      this.previous = previous;
      return this;
    }

    public PublikationBuilder withErstpublikationsDatum(LocalDateTime erstpublikationsDatum) {
      this.erstpublikationsDatum = erstpublikationsDatum;
      return this;
    }

    public PublikationBuilder withPublikationsDatumHSP(LocalDateTime publikationsDatumHSP) {
      this.publikationsDatumHSP = publikationsDatumHSP;
      return this;
    }

    public PublikationBuilder withLizenz(Lizenz lizenz) {
      this.lizenz = lizenz;
      return this;
    }

    public PublikationBuilder withPURLs(Set<PURL> purls) {
      this.purls = purls;
      return this;
    }

    public PublikationBuilder addPURL(PURL purl) {
      this.purls.add(purl);
      return this;
    }

    public Publikation build() {
      if (generateNewId) {
        this.id = UUID.randomUUID().toString();
      }
      return new Publikation(this);
    }
  }
}
