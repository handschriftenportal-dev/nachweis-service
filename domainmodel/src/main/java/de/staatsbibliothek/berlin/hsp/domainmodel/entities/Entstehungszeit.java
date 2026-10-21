package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.EntstehungszeitWert;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class Entstehungszeit extends AttributsReferenz {

  @Serial
  private static final long serialVersionUID = -8245489483621624837L;

  private final String text;
  private final List<EntstehungszeitWert> entstehungszeitWerte;

  private Entstehungszeit(EntstehungszeitBuilder builder) {
    super(builder);
    this.entstehungszeitWerte = builder.entstehungszeitWerte;
    this.text = builder.entstehungszeitWerte.stream()
        .map(EntstehungszeitWert::getText)
        .collect(Collectors.joining("; "));
  }

  public static EntstehungszeitBuilder builder() {
    return new EntstehungszeitBuilder();
  }

  @Override
  public String getText() {
    return text;
  }

  public List<EntstehungszeitWert> getEntstehungszeitWerte() {
    return entstehungszeitWerte;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Entstehungszeit.class.getSimpleName() + "[", "]")
        .add("id='" + getId() + "'")
        .add("attributsTyp=" + getAttributsTyp())
        .add("referenzId='" + getReferenzId() + "'")
        .add("referenzTyp=" + getReferenzTyp())
        .add("text='" + text + "'")
        .add("entstehungszeitWerte=" + entstehungszeitWerte)
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
    if (!(o instanceof Entstehungszeit that)) {
      return false;
    }
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  public static class EntstehungszeitBuilder extends
      AttributsReferenzBuilder<EntstehungszeitBuilder, Entstehungszeit> {

    private final List<EntstehungszeitWert> entstehungszeitWerte = new ArrayList<>();

    protected EntstehungszeitBuilder() {
      super(AttributsTyp.ENTSTEHUNGSZEIT);
    }

    @Override
    public EntstehungszeitBuilder getThis() {
      return this;
    }

    public EntstehungszeitBuilder withEntstehungszeitWerte(
        List<EntstehungszeitWert> entstehungszeitWerte) {
      this.entstehungszeitWerte.clear();
      if (Objects.nonNull(entstehungszeitWerte)) {
        this.entstehungszeitWerte.addAll(entstehungszeitWerte);
      }
      return this;
    }

    public EntstehungszeitBuilder addEntstehungszeitWert(EntstehungszeitWert entstehungszeitWert) {
      if (Objects.nonNull(entstehungszeitWert)) {
        this.entstehungszeitWerte.add(entstehungszeitWert);
      }
      return this;
    }

    public Entstehungszeit build() {
      return new Entstehungszeit(this);
    }
  }

}
