package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class Entstehungsort extends AttributsReferenz {

  @Serial
  private static final long serialVersionUID = 7913375471947530821L;

  private final String text;

  private final List<NormdatenReferenz> normdatenReferenzen;

  private Entstehungsort(EntstehungsortBuilder builder) {
    super(builder);
    this.text = builder.text;
    this.normdatenReferenzen = builder.normdatenReferenzen;
  }

  public static EntstehungsortBuilder builder() {
    return new EntstehungsortBuilder();
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
    return new StringJoiner(", ", Entstehungsort.class.getSimpleName() + "[", "]")
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
    if (!(o instanceof Entstehungsort that)) {
      return false;
    }
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  public static class EntstehungsortBuilder extends
      AttributsReferenzBuilder<EntstehungsortBuilder, Entstehungsort> {

    private final List<NormdatenReferenz> normdatenReferenzen = new ArrayList<>();
    private String text = "";

    public EntstehungsortBuilder() {
      super(AttributsTyp.ENTSTEHUNGSORT);
    }

    @Override
    public EntstehungsortBuilder getThis() {
      return this;
    }

    public EntstehungsortBuilder withText(String text) {
      this.text = text;
      return this;
    }

    public EntstehungsortBuilder withNormdatenReferenzen(
        List<NormdatenReferenz> normdatenReferenzen) {
      this.normdatenReferenzen.clear();
      if (Objects.nonNull(normdatenReferenzen)) {
        this.normdatenReferenzen.addAll(normdatenReferenzen);
      }
      return this;
    }

    public EntstehungsortBuilder addNormdatenReferenz(NormdatenReferenz normdatenReferenz) {
      if (Objects.nonNull(normdatenReferenz)) {
        this.normdatenReferenzen.add(normdatenReferenz);
      }
      return this;
    }

    public Entstehungsort build() {
      return new Entstehungsort(this);
    }

  }
}
