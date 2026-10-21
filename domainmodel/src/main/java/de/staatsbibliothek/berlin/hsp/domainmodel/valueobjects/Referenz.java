package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @author Konrad.Eichstaedt@sbb.spk-berlin.de
 * @since 10.05.19
 * <p>A Referenz is an external presentation showing information about the cultur object or
 * manual description.
 */
@Entity
@Table(name = "referenz")
@Cacheable
public class Referenz implements Serializable {

  private static final long serialVersionUID = 2297867788805731184L;

  @Id
  @Column(name = "id")
  String id;

  @Column
  URL url;

  @Enumerated(EnumType.STRING)
  @Column(name = "typ", length = 15)
  ReferenzTyp typ;

  @ManyToOne(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "referenzQuelle_typ")
  ReferenzQuelle referenzQuelle;

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
   * @return <code>ReferenzQuelle</code> The external Portal or System which is hosting the
   * information.
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
    return url.equals(referenz.url)
        && typ == referenz.typ
        && referenzQuelle.equals(referenz.referenzQuelle);
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
