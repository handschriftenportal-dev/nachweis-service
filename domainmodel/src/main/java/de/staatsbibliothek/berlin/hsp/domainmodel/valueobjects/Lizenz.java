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
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 26.10.2021
 */

@Entity
@Table(name = "Lizenz")
@Cacheable
public class Lizenz implements Serializable {

  private static final long serialVersionUID = -5889019474669752187L;

  @Id
  private String id;

  @ElementCollection
  private Set<String> uris;

  @Column(name = "beschreibungs_text", columnDefinition = "TEXT")
  private String beschreibungsText;

  protected Lizenz() {
    this.id = UUID.randomUUID().toString();
    this.uris = new LinkedHashSet<>();
  }

  public Lizenz(LizenzBuilder lizenzBuilder) {
    this.id = UUID.randomUUID().toString();
    this.uris = lizenzBuilder.uris;
    this.beschreibungsText = lizenzBuilder.beschreibungsText;
  }

  public String getId() {
    return id;
  }

  public Set<String> getUris() {
    return uris;
  }

  public String getBeschreibungsText() {
    return beschreibungsText;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Lizenz)) {
      return false;
    }

    Lizenz lizenz = (Lizenz) o;

    return Objects.equals(id, lizenz.id);
  }

  @Override
  public String toString() {
    return "Lizenz{" + "id='" + id + '\''
        + ", uris=" + (uris != null ? String.join(",", uris) : "null")
        + ", beschreibungsText='" + beschreibungsText + '\''
        + '}';
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

  public static class LizenzBuilder {

    private Set<String> uris;
    private String beschreibungsText;

    public LizenzBuilder() {
      this.uris = new LinkedHashSet<>();
    }

    public LizenzBuilder withUris(Set<String> uris) {
      this.uris = uris;
      return this;
    }

    public LizenzBuilder addUri(String uri) {
      this.uris.add(uri);
      return this;
    }

    public LizenzBuilder withBeschreibungsText(String beschreibungsText) {
      this.beschreibungsText = beschreibungsText;
      return this;
    }

    public Lizenz build() {
      return new Lizenz(this);
    }
  }

}
