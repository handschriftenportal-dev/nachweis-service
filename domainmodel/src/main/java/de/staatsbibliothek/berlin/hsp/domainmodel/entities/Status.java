package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.io.Serial;
import java.util.Objects;
import java.util.StringJoiner;

public class Status extends AttributsReferenz {

  @Serial
  private static final long serialVersionUID = 2937942008922697538L;

  private final String text;

  private Status(StatusBuilder builder) {
    super(builder);
    this.text = builder.text;
  }

  public static StatusBuilder builder() {
    return new StatusBuilder();
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Status.class.getSimpleName() + "[", "]")
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
    if (!(o instanceof Status that)) {
      return false;
    }
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  public static class StatusBuilder extends AttributsReferenzBuilder<StatusBuilder, Status> {

    private String text = "";

    public StatusBuilder() {
      super(AttributsTyp.STATUS);
    }

    @Override
    public StatusBuilder getThis() {
      return this;
    }

    public StatusBuilder withText(String text) {
      this.text = text;
      return getThis();
    }

    public Status build() {
      return new Status(this);
    }

  }
}
