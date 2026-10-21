package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "kulturobjektdokument_beschreibungenids")
public class KodBeschreibungJoin implements Serializable {

  @Serial private static final long serialVersionUID = 123423423445669L;

  @Id
  @Column(name = "beschreibungenids")
  String beschreibungId;

  @Id
  @Column(name = "kulturobjektdokument_id")
  String kulturobjektdokumentId;

  public KodBeschreibungJoin() {}

  public KodBeschreibungJoin(String kulturobjektdokumentId, String beschreibungId) {
    this.kulturobjektdokumentId = kulturobjektdokumentId;
    this.beschreibungId = beschreibungId;
  }

  public String getBeschreibungId() {
    return beschreibungId;
  }

  public void setBeschreibungId(String beschreibungId) {
    this.beschreibungId = beschreibungId;
  }

  public String getKulturobjektdokumentId() {
    return kulturobjektdokumentId;
  }

  public void setKulturobjektdokumentId(String kulturobjektdokumentId) {
    this.kulturobjektdokumentId = kulturobjektdokumentId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    KodBeschreibungJoin that = (KodBeschreibungJoin) o;
    return Objects.equals(beschreibungId, that.beschreibungId)
        && Objects.equals(kulturobjektdokumentId, that.kulturobjektdokumentId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(beschreibungId, kulturobjektdokumentId);
  }
}
