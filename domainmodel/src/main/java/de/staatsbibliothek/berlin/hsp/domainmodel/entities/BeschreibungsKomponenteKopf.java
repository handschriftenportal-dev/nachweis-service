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

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.BeschreibungsKomponentenTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KulturObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Status;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Dieses Objekt repräsentiert einen Beschreibungsteil in welchem die Metainformation zu einer Handschrift bezogen auf
 * den Beschreibungskopf zusammengefasst sind. Bezogen auf die DFG Erschließungsrichtlinien sind dies Informationen zur
 * Signatur, Sachtitel und zur Schlagzeile.
 *
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 22.01.2020.
 * @version 1.0
 */
@Entity
@Table(name = "BeschreibungsKomponenteKopf")
@Cacheable
public class BeschreibungsKomponenteKopf extends GlobaleBeschreibungsKomponente {

  private static final long serialVersionUID = -698293909620708268L;

  @Column
  private Status status;

  @Column (length = 4096)
  private String titel;

  @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST,
      CascadeType.MERGE})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  @JoinTable(name = "BeschreibungsKomponenteKopf_Grundsprachen")
  private Set<NormdatenReferenz> grundsprachen;

  @Enumerated(EnumType.STRING)
  @Column(name = "kulturObjektTyp")
  private KulturObjektTyp kulturObjektTyp;

  protected BeschreibungsKomponenteKopf() {
  }

  public BeschreibungsKomponenteKopf(BeschreibungsKomponenteKopfBuilder builder) {
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

  @Override
  public String toString() {
    return "BeschreibungsKomponenteKopf{" + super.toString() +
        "status=" + status +
        ", titel='" + titel + '\'' +
        ", grundsprachen=" + grundsprachen.stream().map(g -> String.valueOf(g.getId())).collect(Collectors.joining()) +
        ", kulturObjektyp=" + kulturObjektTyp +
        '}';
  }

  public static class BeschreibungsKomponenteKopfBuilder extends
      GlobaleBeschreibungsKomponenteBuilder<BeschreibungsKomponenteKopfBuilder, BeschreibungsKomponenteKopf> {

    private Status status;

    private String titel;

    private Set<NormdatenReferenz> grundsprachen;

    private KulturObjektTyp kulturObjektTyp;

    public BeschreibungsKomponenteKopfBuilder() {
      this.grundsprachen = new LinkedHashSet<>();
    }

    public BeschreibungsKomponenteKopfBuilder withStatus(Status zustand) {
      this.status = zustand;
      return this;
    }

    public BeschreibungsKomponenteKopfBuilder withTitel(String titel) {
      this.titel = titel;
      return this;
    }

    public BeschreibungsKomponenteKopfBuilder withKulturObjektTyp(KulturObjektTyp kulturObjektTyp) {
      this.kulturObjektTyp = kulturObjektTyp;
      return this;
    }

    public BeschreibungsKomponenteKopfBuilder withGrundsprachen(Set<NormdatenReferenz> grundsprachen) {
      this.grundsprachen = grundsprachen;
      return this;
    }

    public BeschreibungsKomponenteKopfBuilder addGrundSprache(NormdatenReferenz schreibsprache) {

      if (Objects.nonNull(schreibsprache)) {
        this.grundsprachen.add(schreibsprache);
      }

      return this;
    }

    public BeschreibungsKomponenteKopfBuilder removeGrundSprache(NormdatenReferenz schreibsprache) {
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
