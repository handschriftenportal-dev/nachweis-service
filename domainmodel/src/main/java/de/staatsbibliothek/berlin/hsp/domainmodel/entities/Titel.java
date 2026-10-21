package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.io.Serial;
import java.util.Objects;
import java.util.StringJoiner;

public class Titel extends AttributsReferenz {

  @Serial
  private static final long serialVersionUID = 2937942008922697538L;

  private final String text;

  private Titel(TitelBuilder builder) {
    super(builder);
    this.text = builder.text;
  }

  public static TitelBuilder builder() {
    return new TitelBuilder();
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Titel.class.getSimpleName() + "[", "]")
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
    if (!(o instanceof Titel that)) {
      return false;
    }
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  public static class TitelBuilder extends AttributsReferenzBuilder<TitelBuilder, Titel> {

    private String text = "";

    public TitelBuilder() {
      super(AttributsTyp.TITEL);
    }

    @Override
    public TitelBuilder getThis() {
      return this;
    }

    public TitelBuilder withText(String text) {
      this.text = text;
      return getThis();
    }

    public Titel build() {
      return new Titel(this);
    }

  }
}
