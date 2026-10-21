package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.FormatWert;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class Format extends AttributsReferenz {

  @Serial
  private static final long serialVersionUID = -4262551536537015661L;

  private final List<FormatWert> formatWerte;
  private final String text;

  private Format(FormatBuilder builder) {
    super(builder);
    this.formatWerte = builder.formatWerte;
    this.text = builder.formatWerte != null && !builder.formatWerte.isEmpty()
        ? builder.formatWerte.stream()
        .map(FormatWert::getText)
        .filter(Objects::nonNull)
        .collect(java.util.stream.Collectors.joining(";"))
        : "";
  }

  public static FormatBuilder builder() {
    return new FormatBuilder();
  }

  public List<FormatWert> getFormatWerte() {
    return formatWerte;
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Format.class.getSimpleName() + "[", "]")
        .add("id='" + getId() + "'")
        .add("attributsTyp=" + getAttributsTyp())
        .add("referenzId='" + getReferenzId() + "'")
        .add("referenzTyp=" + getReferenzTyp())
        .add("normdatenReferenzen=" + normdatenReferenzen)
        .add("text='" + text + "'")
        .add("formatWerte=" + formatWerte)
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
    if (!(o instanceof Format that)) {
      return false;
    }
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }

  public static class FormatBuilder extends AttributsReferenzBuilder<FormatBuilder, Format> {

    private List<FormatWert> formatWerte = new ArrayList<>();

    protected FormatBuilder() {
      super(AttributsTyp.FORMAT);
    }

    @Override
    public FormatBuilder getThis() {
      return this;
    }

    public FormatBuilder withFormatWerte(List<FormatWert> formatWerte) {
      this.formatWerte = formatWerte != null ? new ArrayList<>(formatWerte) : new ArrayList<>();
      return this;
    }

    public FormatBuilder addFormatWert(FormatWert formatWert) {
      if (formatWert != null) {
        this.formatWerte.add(formatWert);
      }
      return this;
    }

    public Format build() {
      return new Format(this);
    }
  }
}
