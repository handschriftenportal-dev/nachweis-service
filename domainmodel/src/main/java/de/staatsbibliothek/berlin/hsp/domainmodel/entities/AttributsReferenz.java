package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.HSP_UUI_Factory;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzStatusTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class AttributsReferenz implements Serializable {

  @Serial
  private static final long serialVersionUID = 2388197042451507870L;
  protected final List<NormdatenReferenz> normdatenReferenzen;
  private final String id;
  private final AttributsTyp attributsTyp;
  private final String referenzId;
  private final AttributsReferenzTyp referenzTyp;
  private final Bearbeiter bearbeiter;
  private String referenzURL;
  private LocalDateTime publikationDatum;
  private AttributsReferenzStatusTyp attributsReferenzStatusTyp;
  private LocalDateTime modificationDatum;
  private String changeID;


  protected AttributsReferenz(
      AttributsReferenzBuilder<? extends AttributsReferenzBuilder<?, ?>, ? extends AttributsReferenz> builder) {
    Objects.requireNonNull(builder.attributsTyp, "attributsTyp is required!");

    this.id = HSP_UUI_Factory.generate(String.join("_",
        Objects.nonNull(builder.referenzId) ? builder.referenzId
            : UUID.randomUUID().toString(),
        builder.attributsTyp.name()).getBytes(StandardCharsets.UTF_8));

    this.normdatenReferenzen = builder.normdatenReferenzen;
    this.attributsTyp = builder.attributsTyp;
    this.referenzId = builder.referenzId;
    this.referenzTyp = builder.referenzTyp;
    this.referenzURL = builder.referenzURL;
    this.publikationDatum = builder.publikationDatum;
    this.attributsReferenzStatusTyp = builder.attributsReferenzStatusTyp;
    this.modificationDatum = builder.modificationDatum;
    this.bearbeiter = builder.bearbeiter;
    this.changeID = builder.changeID;
  }

  public String getId() {
    return id;
  }

  public AttributsTyp getAttributsTyp() {
    return attributsTyp;
  }

  public String getReferenzId() {
    return referenzId;
  }

  public AttributsReferenzTyp getReferenzTyp() {
    return referenzTyp;
  }

  public String getReferenzURL() {
    return referenzURL;
  }

  public void setReferenzURL(String referenzURL) {
    this.referenzURL = referenzURL;
  }

  public List<NormdatenReferenz> getNormdatenReferenzen() {
    return normdatenReferenzen;
  }

  public void setNormdatenReferenzen(List<NormdatenReferenz> normdatenReferenzen) {
    this.normdatenReferenzen.clear();
    this.normdatenReferenzen.addAll(normdatenReferenzen);
  }

  public abstract String getText();

  public LocalDateTime getPublikationDatum() {
    return publikationDatum;
  }

  public void setPublikationDatum(LocalDateTime publikationDatum) {
    this.publikationDatum = publikationDatum;
  }

  public String getChangeID() {
    return changeID;
  }

  public void setChangeID(String changeID) {
    this.changeID = changeID;
  }

  public AttributsReferenzStatusTyp getAttributsReferenzStatusTyp() {
    return attributsReferenzStatusTyp;
  }

  public void setAttributsReferenzStatusTyp(
      AttributsReferenzStatusTyp attributsReferenzStatusTyp) {
    this.attributsReferenzStatusTyp = attributsReferenzStatusTyp;
  }

  public void setAttributsReferenzStatusTyp(String changeValue) {
    this.attributsReferenzStatusTyp = AttributsReferenzStatusTyp.valueOf(
        changeValue);
  }

  public LocalDateTime getModificationDatum() {
    return modificationDatum;
  }

  public void setModificationDatum(LocalDateTime modificationDatum) {
    this.modificationDatum = modificationDatum;
  }

  public Bearbeiter getBearbeiter() {
    return bearbeiter;
  }

  @Override
  public String toString() {
    return "AttributsReferenz{" + "id='" + id + '\''
        + ", attributsTyp=" + attributsTyp
        + ", referenzId='" + referenzId + '\''
        + ", referenzURL='" + referenzURL + '\''
        + ", referenzTyp=" + referenzTyp
        + ", publikationDatum=" + publikationDatum
        + ", attributsReferenzStatusTyp=" + attributsReferenzStatusTyp
        + ", modificationDatum=" + modificationDatum
        + ", bearbeiter=" + bearbeiter
        + ", changeID=" + changeID
        + '}';
  }

  public boolean hasChanged(AttributsReferenz attributsReferenz) {
    if (attributsReferenz == null) {
      return true;
    }
    return !attributsReferenz.getReferenzId().equals(this.referenzId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AttributsReferenz that)) {
      return false;
    }
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  public abstract static class AttributsReferenzBuilder<T extends AttributsReferenzBuilder<T, V>, V extends AttributsReferenz> {

    protected final List<NormdatenReferenz> normdatenReferenzen = new ArrayList<>();
    protected final AttributsTyp attributsTyp;
    protected String referenzId;
    protected String referenzURL;
    protected AttributsReferenzTyp referenzTyp;
    protected LocalDateTime publikationDatum;
    protected AttributsReferenzStatusTyp attributsReferenzStatusTyp;
    protected LocalDateTime modificationDatum;
    protected Bearbeiter bearbeiter;
    protected String changeID;

    protected AttributsReferenzBuilder(AttributsTyp attributsTyp) {
      this.attributsTyp = attributsTyp;
    }

    public abstract T getThis();

    public abstract V build();

    public T withReferenzId(String referenzId) {
      this.referenzId = referenzId;
      return getThis();
    }

    public T withReferenzURL(String referenzURL) {
      this.referenzURL = referenzURL;
      return getThis();
    }

    public T withReferenzTyp(AttributsReferenzTyp referenzTyp) {
      this.referenzTyp = referenzTyp;
      return getThis();
    }

    public T withPublikationDatum(LocalDateTime publikationDatum) {
      this.publikationDatum = publikationDatum;
      return getThis();
    }

    public T withAttributsReferenzStatusTyp(
        AttributsReferenzStatusTyp attributsReferenzStatusTyp) {
      this.attributsReferenzStatusTyp = attributsReferenzStatusTyp;
      return getThis();
    }

    public T withModificationDatum(LocalDateTime modificationDatum) {
      this.modificationDatum = modificationDatum;
      return getThis();
    }

    public T withBearbeiter(Bearbeiter bearbeiter) {
      this.bearbeiter = bearbeiter;
      return getThis();
    }

    public T withReferenzen(List<NormdatenReferenz> normdatenReferenzen) {
      this.normdatenReferenzen.clear();
      this.normdatenReferenzen.addAll(normdatenReferenzen);
      return getThis();
    }

    public T addReferenz(NormdatenReferenz normdatenReferenz) {
      this.normdatenReferenzen.add(normdatenReferenz);
      return getThis();
    }

    public T withChangeId(String changeId) {
      this.changeID = changeId;
      return getThis();
    }
  }
}
