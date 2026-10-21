package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.io.Serial;
import java.util.Objects;
import java.util.StringJoiner;

public class Musiknotation extends AttributsReferenz {

  @Serial
  private static final long serialVersionUID = 2937942008922697538L;

  private final String text;

  private Musiknotation(MusiknotationBuilder builder) {
    super(builder);
    this.text = builder.text;
  }

  public static MusiknotationBuilder builder() {
    return new MusiknotationBuilder();
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Musiknotation.class.getSimpleName() + "[", "]")
        .add("id='" + getId() + "'")
        .add("attributsTyp=" + getAttributsTyp())
        .add("referenzId='" + getReferenzId() + "'")
        .add("referenzTyp=" + getReferenzTyp())
        .add("text='" + text + "'")
        .add("changeID='" + getChangeID() + "'")
        .add("publikationDatum='" + getPublikationDatum())
        .add("bearbeiter=" + getBearbeiter())
        .add("modificationDatum='" + getModificationDatum() + "'")
        .add("attributsReferenzStatusTyp='" + getAttributsReferenzStatusTyp() + "'")
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Musiknotation that)) {
      return false;
    }
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  public static class MusiknotationBuilder extends
      AttributsReferenzBuilder<MusiknotationBuilder, Musiknotation> {

    private String text = "";

    public MusiknotationBuilder() {
      super(AttributsTyp.MUSIKNOTATION);
    }

    @Override
    public MusiknotationBuilder getThis() {
      return this;
    }

    public MusiknotationBuilder withText(String text) {
      this.text = text;
      return getThis();
    }

    public Musiknotation build() {
      return new Musiknotation(this);
    }

  }
}
