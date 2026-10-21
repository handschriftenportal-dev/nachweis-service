package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import de.staatsbibliothek.berlin.hsp.domainmodel.HSP_UUI_Factory;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

public class FormatWert implements Serializable {

  @Serial
  private static final long serialVersionUID = -2608474152307474382L;

  private final String id;
  private final String text;
  private final String typ;
  private final NormdatenReferenz normdatenReferenz;

  private FormatWert(FormatWertBuilder builder) {
    this.id = HSP_UUI_Factory.generate();
    this.normdatenReferenz = builder.normdatenReferenz;
    this.text = builder.normdatenReferenz != null ? builder.normdatenReferenz.getName() : null;
    this.typ = builder.typ;
  }

  public static FormatWertBuilder builder() {
    return new FormatWertBuilder();
  }

  public String getId() {
    return id;
  }

  public String getText() {
    return text;
  }

  public String getTyp() {
    return typ;
  }

  public NormdatenReferenz getNormdatenReferenz() {
    return normdatenReferenz;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FormatWert that)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), id);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", FormatWert.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("text='" + text + "'")
        .add("normdatenReferenz=" + normdatenReferenz)
        .add("typ='" + typ + "'")
        .toString();
  }

  public static class FormatWertBuilder {

    private String typ;
    private NormdatenReferenz normdatenReferenz;

    public FormatWertBuilder withTyp(String typ) {
      this.typ = typ;
      return this;
    }

    public FormatWertBuilder withReferenz(NormdatenReferenz normdatenReferenz) {
      this.normdatenReferenz = normdatenReferenz;
      return this;
    }

    public FormatWert build() {
      return new FormatWert(this);
    }
  }
}
