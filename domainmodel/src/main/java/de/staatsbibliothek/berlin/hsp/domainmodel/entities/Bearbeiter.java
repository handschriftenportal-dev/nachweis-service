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

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SelectBeforeUpdate;

/**
 * This entity is used for persist user info provided by the identity management domain
 *
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 29.09.2017.
 */

@Entity
@SelectBeforeUpdate
@Table(name = "bearbeiter")
public class Bearbeiter implements Serializable {

  private static final long serialVersionUID = 8053601050766237409L;

  @Id
  @Column(name = "id", length = 64)
  private String id;

  @Column(name = "bearbeitername", length = 64)
  private String bearbeitername;

  @Column(name = "vorname")
  private String vorname;

  @Column(name = "nachname")
  private String nachname;

  @Column(name = "email", length = 512)
  private String email;

  @Column(name = "rolle")
  private String rolle;

  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE,
      CascadeType.DETACH, CascadeType.REFRESH})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private NormdatenReferenz institution;

  @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE,
      CascadeType.DETACH, CascadeType.REFRESH})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private NormdatenReferenz person;

  protected Bearbeiter() {
  }

  public Bearbeiter(String id, String bearbeitername) {
    this.id = id;
    this.bearbeitername = bearbeitername;
  }

  private Bearbeiter(BearbeiterBuilder bearbeiterBuilder) {
    this.id = bearbeiterBuilder.id;
    this.bearbeitername = bearbeiterBuilder.bearbeitername;
    this.vorname = bearbeiterBuilder.vorname;
    this.nachname = bearbeiterBuilder.nachname;
    this.email = bearbeiterBuilder.email;
    this.rolle = bearbeiterBuilder.rolle;
    this.institution = bearbeiterBuilder.institution;
    this.person = bearbeiterBuilder.person;
  }

  public static BearbeiterBuilder newBuilder() {
    return new BearbeiterBuilder();
  }

  public String getId() {
    return id;
  }

  public String getBearbeitername() {
    return bearbeitername;
  }

  public void setBearbeitername(String benutzername) {
    this.bearbeitername = benutzername;
  }

  public NormdatenReferenz getInstitution() {
    return institution;
  }

  public void setInstitution(NormdatenReferenz institution) {
    this.institution = institution;
  }

  public String getVorname() {
    return vorname;
  }

  public void setVorname(String vorname) {
    this.vorname = vorname;
  }

  public String getNachname() {
    return nachname;
  }

  public void setNachname(String nachname) {
    this.nachname = nachname;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getRolle() {
    return rolle;
  }

  public void setRolle(String rolle) {
    this.rolle = rolle;
  }

  public NormdatenReferenz getPerson() {
    return person;
  }

  public void setPerson(NormdatenReferenz person) {
    this.person = person;
  }

  public String getName() {
    if (Objects.nonNull(vorname) || Objects.nonNull(nachname)) {
      return Stream.of(vorname, nachname)
          .filter(n -> Objects.nonNull(n) && !n.isEmpty())
          .collect(Collectors.joining(" "));
    } else if (Objects.nonNull(bearbeitername)) {
      return bearbeitername;
    } else {
      return "unbekannt";
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Bearbeiter bearbeiter = (Bearbeiter) o;
    return Objects.equals(id, bearbeiter.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Bearbeiter{");
    sb.append("id='").append(id).append('\'');
    sb.append(", bearbeitername='").append(bearbeitername).append('\'');
    sb.append(", vorname='").append(vorname).append('\'');
    sb.append(", nachname='").append(nachname).append('\'');
    sb.append(", email='").append(email).append('\'');
    sb.append(", rolle='").append(rolle).append('\'');
    sb.append(", institution=").append(institution);
    sb.append(", person=").append(person);
    sb.append('}');
    return sb.toString();
  }

  public static class BearbeiterBuilder {

    private String id;
    private String bearbeitername;
    private String vorname;
    private String nachname;
    private String email;
    private String rolle;
    private NormdatenReferenz institution;
    private NormdatenReferenz person;

    public BearbeiterBuilder() {
    }

    public BearbeiterBuilder withBearbeitername(String bearbeitername) {
      this.id = bearbeitername;
      this.bearbeitername = bearbeitername;
      return this;
    }

    public BearbeiterBuilder withVorname(String vorname) {
      this.vorname = vorname;
      return this;
    }

    public BearbeiterBuilder withNachname(String nachname) {
      this.nachname = nachname;
      return this;
    }

    public BearbeiterBuilder withEmail(String email) {
      this.email = email;
      return this;
    }

    public BearbeiterBuilder withRolle(String rolle) {
      this.rolle = rolle;
      return this;
    }

    public BearbeiterBuilder withInstitution(NormdatenReferenz institution) {
      this.institution = institution;
      return this;
    }

    public BearbeiterBuilder withPerson(NormdatenReferenz person) {
      this.person = person;
      return this;
    }

    public Bearbeiter build() {
      return new Bearbeiter(this);
    }

  }

}
