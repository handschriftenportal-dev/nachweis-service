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

package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.SelectBeforeUpdate;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @author Konrad.Eichstaedt@sbb.spk-berlin.de
 * @since 10.05.19
 * <p>
 * A Referenz is an external presentation showing information about the cultur object or manual description.
 */
@Entity
@Table(name = "referenz")
@SelectBeforeUpdate
@Cacheable
public class Referenz implements Serializable {

  private static final long serialVersionUID = 2297867788805731184L;

  @Id
  @Column(name = "id")
  private String id;

  @Column
  private URL url;

  @Enumerated(EnumType.STRING)
  @Column(name = "typ", length = 15)
  private ReferenzTyp typ;

  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "referenzQuelle_typ")
  private ReferenzQuelle referenzQuelle;

  public Referenz() {
  }

  public Referenz(String url, ReferenzTyp typ, ReferenzQuelle referenzQuelle) {
    try {
      this.url = new URL(url);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Wrong URL", e);
    }
    this.typ = typ;
    this.referenzQuelle = referenzQuelle;
    id = UUID.nameUUIDFromBytes(toString(false).getBytes(StandardCharsets.UTF_8)).toString();
  }

  /**
   * @return <code>URL</code> The HTTP URL to the external presentation.
   */

  public URL getUrl() {
    return this.url;
  }

  /**
   * @return <code>ReferenzQuelle</code> The external Portal or System which is hosting the information.
   */


  public ReferenzQuelle getReferenzQuelle() {
    return this.referenzQuelle;
  }

  /**
   * @return <code>ReferenzTyp</code> The Typ of Hosting.
   */

  public ReferenzTyp getTyp() {
    return typ;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Referenz)) {
      return false;
    }
    Referenz referenz = (Referenz) o;
    return url.equals(referenz.url) &&
        typ == referenz.typ &&
        referenzQuelle.equals(referenz.referenzQuelle);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.id);
  }

  @Override
  public String toString() {
    return toString(true);
  }

  private String toString(boolean withID) {
    final StringBuilder sb = new StringBuilder("Referenz{");
    sb.append("url=").append(url);
    if (withID) {
      sb.append(", id='").append(id).append('\'');
    }
    sb.append(", typ=").append(typ);
    sb.append(", referenzQuelle=").append(referenzQuelle);
    sb.append('}');
    return sb.toString();
  }
}
