package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import de.staatsbibliothek.berlin.hsp.domainmodel.HSP_UUI_Factory;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 02.08.2022
 */
@Table(name = "PURL")
@Entity
@Cacheable
public class PURL implements Serializable {

  @Serial
  private static final long serialVersionUID = 4572333871374686042L;

  @Id
  String id;

  @Column(nullable = false, unique = true)
  String purl;

  @Column(nullable = false)
  String target;

  @Enumerated(EnumType.STRING)
  @Column(length = 50, nullable = false)
  PURLTyp typ;

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

  public boolean isInternal() {
    return typ == PURLTyp.INTERNAL;
  }

  public boolean isExternal() {
    return typ == PURLTyp.EXTERNAL;
  }

  @Override
  public String toString() {
    return "PURL{"
        + "id='"
        + id
        + '\''
        + ", purl='"
        + purl
        + '\''
        + ", target='"
        + target
        + '\''
        + ", typ="
        + typ
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PURL otherPurl)) {
      return false;
    }
    return Objects.equals(id, otherPurl.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
