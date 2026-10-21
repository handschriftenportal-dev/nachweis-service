package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class Handschriftentyp extends AttributsReferenz {

  @Serial
  private static final long serialVersionUID = 2937942008922697538L;

  private final List<NormdatenReferenz> normdatenReferenzen;

  private final String text;

  private Handschriftentyp(HandschriftentypBuilder builder) {
    super(builder);
    this.text = builder.text;
    this.normdatenReferenzen = builder.normdatenReferenzen;
  }

  public static HandschriftentypBuilder builder() {
    return new HandschriftentypBuilder();
  }

  public List<NormdatenReferenz> getNormdatenReferenzen() {
    return normdatenReferenzen;
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Handschriftentyp.class.getSimpleName() + "[", "]")
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
    if (!(o instanceof Handschriftentyp that)) {
      return false;
    }
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  public static class HandschriftentypBuilder extends
      AttributsReferenzBuilder<HandschriftentypBuilder, Handschriftentyp> {

    private final List<NormdatenReferenz> normdatenReferenzen = new ArrayList<>();

    private String text = "";

    public HandschriftentypBuilder() {
      super(AttributsTyp.HANDSCHRIFTENTYP);
    }

    @Override
    public HandschriftentypBuilder getThis() {
      return this;
    }

    public HandschriftentypBuilder withReferenzen(List<NormdatenReferenz> normdatenReferenzen) {
      this.normdatenReferenzen.clear();
      if (normdatenReferenzen != null) {
        this.normdatenReferenzen.addAll(normdatenReferenzen);
      }
      return getThis();
    }

    public HandschriftentypBuilder addReferenz(NormdatenReferenz normdatenReferenz) {
      if (normdatenReferenz != null) {
        this.normdatenReferenzen.add(normdatenReferenz);
      }
      return getThis();
    }

    public HandschriftentypBuilder withText(String text) {
      this.text = text != null ? text : "";
      return getThis();
    }

    public Handschriftentyp build() {
      if (!normdatenReferenzen.isEmpty()) {
        this.text = normdatenReferenzen.getFirst().getName();
      }
      return new Handschriftentyp(this);
    }
  }
}
