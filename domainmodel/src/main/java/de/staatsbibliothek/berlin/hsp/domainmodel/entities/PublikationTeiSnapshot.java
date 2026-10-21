package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;

/**
 * Frozen TEI-XML state of a Beschreibung at the moment its Publikation was approved (Ticket 23861).
 * Stored as a ZIP byte array in its own table so the blob is fetched only on explicit demand
 * (download), not with every Publikation load. The PK doubles as a FK to Publikation.id with ON
 * DELETE CASCADE; no JPA relationship from Publikation back to this entity, so the bytes never
 * appear on the Publikation graph.
 */
@Getter
@Entity
@Table(name = "publikation_tei")
public class PublikationTeiSnapshot {

  @Id
  @Column(name = "publikation_id")
  private String publikationId;

  @Lob
  @JdbcTypeCode(Types.VARBINARY)
  @Column(name = "tei_zip", nullable = false)
  private byte[] teiZip;

  @Column(name = "captured_at", nullable = false)
  private LocalDateTime capturedAt;

  protected PublikationTeiSnapshot() {
  }

  public PublikationTeiSnapshot(String publikationId, byte[] teiZip) {
    Objects.requireNonNull(publikationId, "publikationId must not be null");
    Objects.requireNonNull(teiZip, "teiZip must not be null");
    if (teiZip.length == 0) {
      throw new IllegalArgumentException("teiZip must not be empty");
    }
    this.publikationId = publikationId;
    this.teiZip = teiZip;
    this.capturedAt = LocalDateTime.now(ZoneId.systemDefault());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PublikationTeiSnapshot that)) {
      return false;
    }
    return Objects.equals(publikationId, that.publikationId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(publikationId);
  }
}
