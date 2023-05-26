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


import de.staatsbibliothek.berlin.hsp.domainmodel.HSP_UUI_Factory;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.SelectBeforeUpdate;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 02.08.2022
 */
@Table(name = "PURL")
@Entity
@SelectBeforeUpdate
@Cacheable
public class PURL implements Serializable {

  private static final long serialVersionUID = 4572333871374686042L;

  @Id
  private String id;

  @Column(nullable = false, unique = true)
  private String purl;

  @Column(nullable = false)
  private String target;

  @Enumerated(EnumType.STRING)
  @Column(length = 50, nullable = false)
  private PURLTyp typ;

  public PURL() {
  }

  public PURL(URI purl, URI target, PURLTyp typ) {
    Objects.requireNonNull(purl);
    Objects.requireNonNull(target);
    Objects.requireNonNull(typ);

    this.purl = purl.toASCIIString();
    this.target = target.toASCIIString();
    this.typ = typ;
    this.id = HSP_UUI_Factory.generate(this.purl.getBytes(StandardCharsets.UTF_8));
  }

  public PURL(String id, URI purl, URI target, PURLTyp typ) {
    Objects.requireNonNull(id);
    Objects.requireNonNull(purl);
    Objects.requireNonNull(target);
    Objects.requireNonNull(typ);

    this.id = id;
    this.purl = purl.toASCIIString();
    this.target = target.toASCIIString();
    this.typ = typ;
  }

  public String getId() {
    return id;
  }

  public URI getPurl() {
    return Objects.nonNull(purl) ? URI.create(purl) : null;
  }

  public URI getTarget() {
    return Objects.nonNull(target) ? URI.create(target) : null;
  }

  public void setTarget(URI target) {
    Objects.requireNonNull(target);
    this.target = target.toASCIIString();
  }

  public PURLTyp getTyp() {
    return typ;
  }

  @Override
  public String toString() {
    return "PURL{" + "id='" + id + '\''
        + ", purl='" + purl + '\''
        + ", target='" + target + '\''
        + ", typ=" + typ
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PURL)) {
      return false;
    }
    PURL otherPurl = (PURL) o;
    return Objects.equals(id, otherPurl.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
