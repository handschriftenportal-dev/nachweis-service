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

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationsTyp;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Dieses Objekt repräsentiert eine Veröffentlichung einer Beschreibung zu einem KulturObjektDokument. Die
 * Veröffentlichung kann sowohl in der Form im Web als auch in der Form eines Katalog als Printmedium bzw. PDF
 * erfolgen.
 *
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 22.01.2020.
 * @version 1.0
 */
@Entity
@Table(name = "Publikation")
@Cacheable
public class Publikation {

  @Id
  private String id;

  @Column(name = "datum_der_veroeffentlichung", nullable = false)
  private LocalDateTime datumDerVeroeffentlichung;

  @Column(name = "publikations_typ", length = 32, nullable = false)
  @Enumerated(EnumType.STRING)
  private PublikationsTyp publikationsTyp;

  public Publikation() {
  }

  public Publikation(String id, LocalDateTime datumDerVeroeffentlichung, PublikationsTyp publikationsTyp) {
    this.id = id;
    this.datumDerVeroeffentlichung = datumDerVeroeffentlichung;
    this.publikationsTyp = publikationsTyp;
  }

  public String getId() {
    return id;
  }

  public LocalDateTime getDatumDerVeroeffentlichung() {
    return datumDerVeroeffentlichung;
  }

  public PublikationsTyp getPublikationsTyp() {
    return publikationsTyp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Publikation)) {
      return false;
    }
    Publikation that = (Publikation) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Publikation{");
    sb.append("id='").append(id).append('\'');
    sb.append(", datumDerVeroeffentlichung=").append(datumDerVeroeffentlichung);
    sb.append(", publikationsTyp=").append(publikationsTyp);
    sb.append('}');
    return sb.toString();
  }
}
