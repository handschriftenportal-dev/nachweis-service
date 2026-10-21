package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * This entity is used for persist user info provided by the identity management domain
 *
 * <p>Created by konrad.eichstaedt@sbb.spk-berlin.de on 29.09.2017.
 */
@Entity
@Table(name = "bearbeiter")
public class Bearbeiter implements Serializable {

  @Serial
  private static final long serialVersionUID = 8053601050766237409L;

  @Id
  @Column(name = "id", length = 64)
  String id;

  @Column(name = "bearbeitername", length = 64)
  String bearbeitername;

  @Column(name = "vorname")
  String vorname;

  @Column(name = "nachname")
  String nachname;

  @Column(name = "email", length = 512)
  String email;

  @Column(name = "rolle")
  String rolle;

  @ManyToOne(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  NormdatenReferenz institution;

  @OneToOne(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  NormdatenReferenz person;

  @Column(name = "editor_config", columnDefinition = "TEXT")
  private String editorConfig;

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
    this.editorConfig = bearbeiterBuilder.editorConfig;
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

  public String getFullName() {
    if (Objects.nonNull(vorname) || Objects.nonNull(nachname)) {
      return String.join(", ", nachname, vorname);
    } else {
      return "unbekannt";
    }
  }

  public String getEditorConfig() {
    return editorConfig;
  }

  public void setEditorConfig(String editorConfig) {
    this.editorConfig = editorConfig;
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
    sb.append(", editorConfig='").append(editorConfig).append('\'');
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
    private String editorConfig;

    public BearbeiterBuilder() {
    }

    public BearbeiterBuilder withId(String id) {
      this.id = id;
      return this;
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

    public BearbeiterBuilder withEditorConfig(String editorConfig) {
      this.editorConfig = editorConfig;
      return this;
    }

    public Bearbeiter build() {
      return new Bearbeiter(this);
    }
  }
}
