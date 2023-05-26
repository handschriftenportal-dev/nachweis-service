/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreEintrag;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.SelectBeforeUpdate;

/**
 * Aggregate set of locked entities together with the corresponding user, the lock reason, start date and the id of the
 * associated transaction optionally
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 17.08.21
 */
@Entity
@SelectBeforeUpdate
@Table(name = "sperre")
public class Sperre implements Serializable, Comparable<Sperre> {
  
  private static final long serialVersionUID = -8699538932105978301L;
  @Id
  @Column(name = "id", length = 64)
  private String id;

  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.REFRESH})
  private Bearbeiter bearbeiter;

  @Column(name = "start_datum")
  private LocalDateTime startDatum;

  @Column(name = "sperre_typ", length = 32)
  @Enumerated(EnumType.STRING)
  private SperreTyp sperreTyp;

  @Column(name = "sperre_grund", length = 512)
  private String sperreGrund;

  @Column(name = "tx_id", length = 64)
  private String txId;

  @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, orphanRemoval = true)
  @JoinColumn(name = "sperre_id", referencedColumnName = "id")
  private Set<SperreEintrag> sperreEintraege;

  public Sperre() {
  }

  private Sperre(SperreBuilder builder) {
    this.id = builder.id;
    this.startDatum = builder.startDatum;
    this.sperreGrund = builder.sperreGrund;
    this.bearbeiter = builder.bearbeiter;
    this.sperreTyp = builder.sperreTyp;
    this.sperreEintraege = builder.sperreEintraege;
    this.txId = builder.txId;
  }

  public static SperreBuilder newBuilder() {
    return new SperreBuilder();
  }

  public String getId() {
    return id;
  }

  public LocalDateTime getStartDatum() {
    return startDatum;
  }

  public Bearbeiter getBearbeiter() {
    return bearbeiter;
  }

  public SperreTyp getSperreTyp() {
    return sperreTyp;
  }

  public String getSperreGrund() {
    return sperreGrund;
  }

  public Set<SperreEintrag> getSperreEintraege() {
    return sperreEintraege;
  }

  public String getTxId() {
    return txId;
  }

  @Override
  public int compareTo(Sperre other) {
    if (other == null) {
      return 1;
    }
    return toString().compareTo(other.toString());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Sperre sperre = (Sperre) o;
    return Objects.equals(id, sperre.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Sperre.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("benutzer=" + bearbeiter)
        .add("startDatum=" + startDatum)
        .add("sperreTyp=" + sperreTyp)
        .add("sperreGrund='" + sperreGrund + "'")
        .add("txId='" + txId + "'")
        .add("sperreEintraege=" + sperreEintraege.size())
        .toString();
  }

  public static class SperreBuilder {

    private String id;
    private LocalDateTime startDatum;
    private Bearbeiter bearbeiter;
    private String sperreGrund;
    private Set<SperreEintrag> sperreEintraege;
    private String txId;
    private SperreTyp sperreTyp;

    public SperreBuilder() {
      sperreEintraege = new LinkedHashSet<>();
      id = UUID.randomUUID().toString();
      startDatum = LocalDateTime.now();
    }

    public SperreBuilder withId(String id) {
      this.id = id;
      return this;
    }

    public SperreBuilder withStartDatum(LocalDateTime startDatum) {
      this.startDatum = startDatum;
      return this;
    }

    public SperreBuilder withBearbeiter(Bearbeiter bearbeiter) {
      this.bearbeiter = bearbeiter;
      return this;
    }

    public SperreBuilder withSperreGrund(String sperreGrund) {
      this.sperreGrund = sperreGrund;
      return this;
    }

    public SperreBuilder withSperreTyp(SperreTyp sperreTyp) {
      this.sperreTyp = sperreTyp;
      return this;
    }

    public SperreBuilder withSperreEintraege(Set<SperreEintrag> sperreEintraege) {
      this.sperreEintraege.clear();
      if (sperreEintraege != null) {
        this.sperreEintraege.addAll(sperreEintraege);
      }
      return this;
    }

    public SperreBuilder addSperreEintrag(SperreEintrag sperreEintrag) {
      if (sperreEintrag != null) {
        this.sperreEintraege.add(sperreEintrag);
      }
      return this;
    }

    public SperreBuilder withTxId(String txId) {
      this.txId = txId;
      return this;
    }

    public Sperre build() {
      return new Sperre(this);
    }

  }
}
