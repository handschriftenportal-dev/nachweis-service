package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import de.staatsbibliothek.berlin.hsp.domainmodel.HSP_UUI_Factory;
import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

public class AbmessungWert implements Serializable {

  private static final long serialVersionUID = 2701734105358266259L;

  private String id;
  private String text;
  private String hoehe;
  private String breite;
  private String tiefe;
  private String typ;

  private AbmessungWert(AbmessungWertBuilder builder) {
    this.id = HSP_UUI_Factory.generate();
    this.text = builder.text;
    this.hoehe = builder.hoehe;
    this.breite = builder.breite;
    this.tiefe = builder.tiefe;
    this.typ = builder.typ;
  }

  public static AbmessungWertBuilder builder() {
    return new AbmessungWertBuilder();
  }

  public String getId() {
    return id;
  }

  public String getText() {
    return text;
  }

  public String getHoehe() {
    return hoehe;
  }

  public String getBreite() {
    return breite;
  }

  public String getTiefe() {
    return tiefe;
  }

  public String getTyp() {
    return typ;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AbmessungWert that = (AbmessungWert) o;

    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", AbmessungWert.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("text='" + text + "'")
        .add("hoehe='" + hoehe + "'")
        .add("breite='" + breite + "'")
        .add("tiefe='" + tiefe + "'")
        .add("typ='" + typ + "'")
        .toString();
  }

  public static class AbmessungWertBuilder {

    private String text;
    private String hoehe;
    private String breite;
    private String tiefe;
    private String typ;

    public AbmessungWertBuilder withText(String text) {
      this.text = text;
      return this;
    }

    public AbmessungWertBuilder withHoehe(String hoehe) {
      this.hoehe = hoehe;
      return this;
    }

    public AbmessungWertBuilder withBreite(String breite) {
      this.breite = breite;
      return this;
    }

    public AbmessungWertBuilder withTiefe(String tiefe) {
      this.tiefe = tiefe;
      return this;
    }

    public AbmessungWertBuilder withTyp(String typ) {
      this.typ = typ;
      return this;
    }

    public AbmessungWert build() {
      return new AbmessungWert(this);
    }
  }

}
