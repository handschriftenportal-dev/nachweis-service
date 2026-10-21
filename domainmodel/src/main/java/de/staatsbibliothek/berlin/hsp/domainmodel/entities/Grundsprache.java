package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.io.Serial;
import java.util.Objects;
import java.util.StringJoiner;

public class Grundsprache extends AttributsReferenz {

  @Serial
  private static final long serialVersionUID = 2937942008922697538L;

  private final String text;

  private Grundsprache(GrundspracheBuilder builder) {
    super(builder);
    this.text = builder.text;
  }

  public static GrundspracheBuilder builder() {
    return new GrundspracheBuilder();
  }


  @Override
  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Grundsprache.class.getSimpleName() + "[", "]")
        .add("id='" + getId() + "'")
        .add("attributsTyp=" + getAttributsTyp())
        .add("referenzId='" + getReferenzId() + "'")
        .add("referenzTyp=" + getReferenzTyp())
        .add("text='" + text + "'")
        .add("normdatenIds=" + normdatenReferenzen)
        .add("changeID='" + getChangeID() + "'")
        .add("publikationDatum='" + getPublikationDatum())
        .add("bearbeiter=" + getBearbeiter())
        .add("modificationDatum='" + getModificationDatum() + "'")
        .add("attributsReferenzStatusTyp='" + getAttributsReferenzStatusTyp()
            + "'")
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Grundsprache that)) {
      return false;
    }
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  public static class GrundspracheBuilder extends
      AttributsReferenzBuilder<GrundspracheBuilder, Grundsprache> {

    private String text = "";

    public GrundspracheBuilder() {
      super(AttributsTyp.GRUNDSPRACHE);
    }

    @Override
    public GrundspracheBuilder getThis() {
      return this;
    }

    public GrundspracheBuilder withText(String text) {
      this.text = text;
      return getThis();
    }

    public Grundsprache build() {
      return new Grundsprache(this);
    }

  }

}
