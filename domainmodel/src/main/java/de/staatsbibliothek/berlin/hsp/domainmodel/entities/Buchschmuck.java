package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.io.Serial;
import java.util.Objects;
import java.util.StringJoiner;

public class Buchschmuck extends AttributsReferenz {

  @Serial
  private static final long serialVersionUID = 2937942008922697538L;

  private final String text;

  private Buchschmuck(BuchschmuckBuilder builder) {
    super(builder);
    this.text = builder.text;
  }

  public static BuchschmuckBuilder builder() {
    return new BuchschmuckBuilder();
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Buchschmuck.class.getSimpleName() + "[", "]")
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
    if (!(o instanceof Buchschmuck that)) {
      return false;
    }
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  public static class BuchschmuckBuilder extends
      AttributsReferenzBuilder<BuchschmuckBuilder, Buchschmuck> {

    private String text = "";

    public BuchschmuckBuilder() {
      super(AttributsTyp.BUCHSCHMUCK);
    }

    @Override
    public BuchschmuckBuilder getThis() {
      return this;
    }

    public BuchschmuckBuilder withText(String text) {
      this.text = text;
      return getThis();
    }

    public Buchschmuck build() {
      return new Buchschmuck(this);
    }

  }
}
