package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.io.Serial;
import java.util.Objects;
import java.util.StringJoiner;

public class Umfang extends AttributsReferenz {

  @Serial
  private static final long serialVersionUID = 580583629974776067L;

  private final String text;

  private final String blattzahl;

  private Umfang(UmfangBuilder builder) {
    super(builder);
    this.text = builder.text;
    this.blattzahl = builder.blattzahl;
  }

  public static UmfangBuilder builder() {
    return new UmfangBuilder();
  }

  @Override
  public String getText() {
    return text;
  }

  public String getBlattzahl() {
    return blattzahl;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Umfang.class.getSimpleName() + "[", "]")
        .add("id='" + getId() + "'")
        .add("attributsTyp=" + getAttributsTyp())
        .add("referenzId='" + getReferenzId() + "'")
        .add("referenzTyp=" + getReferenzTyp())
        .add("text='" + text + "'")
        .add("blattzahl=" + blattzahl)
        .add("changeID='" + getChangeID() + "'").add("changeID='" + getChangeID() + "'")
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
    if (!(o instanceof Umfang that)) {
      return false;
    }
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  public static class UmfangBuilder extends AttributsReferenzBuilder<UmfangBuilder, Umfang> {

    private String text = "";
    private String blattzahl;

    public UmfangBuilder() {
      super(AttributsTyp.UMFANG);
    }

    @Override
    public UmfangBuilder getThis() {
      return this;
    }

    public UmfangBuilder withText(String text) {
      this.text = text;
      return this;
    }

    public UmfangBuilder withBlattzahl(String blattzahl) {
      this.blattzahl = blattzahl;
      return this;
    }

    public Umfang build() {
      return new Umfang(this);
    }
  }

}
