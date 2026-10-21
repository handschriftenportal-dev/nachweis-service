package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import org.hibernate.annotations.Immutable;
import org.hibernate.proxy.HibernateProxy;

@Entity
@Immutable
@Table(name = "beschreibung_in_review_view")
@Getter
public class BeschreibungInReviewDbView implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "hsp_id")
  private String hspId;

  @Column(name = "kod_id")
  private String kodId;

  @Column(name = "gueltige_signatur")
  private String gueltigeSignatur;

  @Column(name = "besitzer")
  private String besitzer;

  @Column(name = "aufbewahrungsort")
  private String aufbewahrungsort;

  @Column(name = "bearbeiter")
  private String bearbeiter;

  @Column(name = "aenderungsdatum")
  private LocalDateTime aenderungsdatum;

  @Column(name = "aenderungsdatum_string")
  private String aenderungsdatumString;

  @Column(name = "publikations_datum_hsp")
  private LocalDateTime publikationsDatumHsp;

  @Column(name = "publikation_status")
  private String publikationStatus;

  @Column(name = "requested_at")
  private LocalDateTime requestedAt;

  @Column(name = "assigned_to")
  private String assignedTo;

  protected BeschreibungInReviewDbView() {
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    Class<?> oEffectiveClass = o instanceof HibernateProxy
        ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy
        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
        : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) {
      return false;
    }
    BeschreibungInReviewDbView that = (BeschreibungInReviewDbView) o;
    return getHspId() != null && Objects.equals(getHspId(), that.getHspId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
        .hashCode() : getClass().hashCode();
  }
}
