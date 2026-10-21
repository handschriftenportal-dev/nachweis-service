package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.io.Serial;
import java.util.Objects;
import java.util.StringJoiner;

public class Beschreibstoff extends AttributsReferenz {

  @Serial
  private static final long serialVersionUID = 580583629974776067L;

  private final String text;

  private Beschreibstoff(BeschreibstoffBuilder builder) {
    super(builder);
    this.text = builder.text;
  }

  public static BeschreibstoffBuilder builder() {
    return new BeschreibstoffBuilder();
  }

  @Override
  public String getText() {
    return text;
  }


  @Override
  public String toString() {
    return new StringJoiner(", ", Beschreibstoff.class.getSimpleName() + "[", "]")
        .add("id='" + getId() + "'")
        .add("attributsTyp=" + getAttributsTyp())
        .add("referenzId='" + getReferenzId() + "'")
        .add("referenzTyp=" + getReferenzTyp())
        .add("text='" + text + "'")
        .add("normdatenReferenzen=" + normdatenReferenzen)
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
    if (!(o instanceof Beschreibstoff that)) {
      return false;
    }
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  public static class BeschreibstoffBuilder extends
      AttributsReferenzBuilder<BeschreibstoffBuilder, Beschreibstoff> {

    private String text = "";

    public BeschreibstoffBuilder() {
      super(AttributsTyp.BESCHREIBSTOFF);
    }

    @Override
    public BeschreibstoffBuilder getThis() {
      return this;
    }

    public BeschreibstoffBuilder withText(String text) {
      this.text = text;
      return this;
    }

    public Beschreibstoff build() {
      return new Beschreibstoff(this);
    }
  }

}
