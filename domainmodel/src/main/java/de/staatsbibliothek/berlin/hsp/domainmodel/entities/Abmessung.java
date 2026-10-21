package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AbmessungWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class Abmessung extends AttributsReferenz {

  @Serial
  private static final long serialVersionUID = -8245489483621624837L;

  private final String text;
  private final List<AbmessungWert> abmessungWerte;

  private Abmessung(AbmessungBuilder builder) {
    super(builder);
    this.abmessungWerte = builder.abmessungWerte;
    this.text = builder.abmessungWerte.stream()
        .map(AbmessungWert::getText)
        .collect(Collectors.joining("; "));
  }

  public static Abmessung.AbmessungBuilder builder() {
    return new Abmessung.AbmessungBuilder();
  }

  @Override
  public String getText() {
    return text;
  }

  public List<AbmessungWert> getAbmessungWerte() {
    return abmessungWerte;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Abmessung.class.getSimpleName() + "[", "]")
        .add("id='" + getId() + "'")
        .add("attributsTyp=" + getAttributsTyp())
        .add("referenzId='" + getReferenzId() + "'")
        .add("referenzTyp=" + getReferenzTyp())
        .add("text='" + text + "'")
        .add("abmessungWerte=" + abmessungWerte)
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
    if (!(o instanceof Abmessung that)) {
      return false;
    }
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  public static class AbmessungBuilder extends
      AttributsReferenzBuilder<Abmessung.AbmessungBuilder, Abmessung> {

    private final List<AbmessungWert> abmessungWerte = new ArrayList<>();

    protected AbmessungBuilder() {
      super(AttributsTyp.ABMESSUNG);
    }

    @Override
    public Abmessung.AbmessungBuilder getThis() {
      return this;
    }

    public Abmessung.AbmessungBuilder withAbmessungWerte(List<AbmessungWert> abmessungWerte) {
      this.abmessungWerte.clear();
      if (Objects.nonNull(abmessungWerte)) {
        this.abmessungWerte.addAll(abmessungWerte);
      }
      return this;
    }

    public Abmessung.AbmessungBuilder addAbmessungWert(AbmessungWert abmessungWert) {
      if (Objects.nonNull(abmessungWert)) {
        this.abmessungWerte.add(abmessungWert);
      }
      return this;
    }

    public Abmessung build() {
      return new Abmessung(this);
    }
  }

}
