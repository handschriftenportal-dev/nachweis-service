package de.staatsbibliothek.berlin.hsp.domainmodel.entities;

import de.staatsbibliothek.berlin.hsp.domainmodel.HSP_UUI_Factory;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.StringJoiner;
import org.hibernate.annotations.Immutable;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 29.11.22
 */

@Entity
@Table(name = "geloeschtesdokument")
@Immutable
public class GeloeschtesDokument implements Serializable {

  @Serial
  private static final long serialVersionUID = -1780027320641218964L;
  @Id
  String id;
  @Column(name = "dokument_id", length = 1024, nullable = false)
  String dokumentId;
  @Column(name = "dokument_objekt_typ", length = 64, nullable = false)
  @Enumerated(EnumType.STRING)
  DokumentObjektTyp dokumentObjektTyp;
  @Column(name = "gueltige_signatur", length = 1024)
  String gueltigeSignatur;
  @Column(name = "alternative_signaturen", length = 4098)
  String alternativeSignaturen;
  @Column(name = "interne_purls", length = 4098)
  String internePurls;
  @Column(name = "besitzer_id", length = 64, nullable = false)
  String besitzerId;
  @Column(name = "besitzer_name", length = 1024, nullable = false)
  String besitzerName;
  @Column(name = "aufbewahrungsort_id", length = 64, nullable = false)
  String aufbewahrungsortId;
  @Column(name = "aufbewahrungsort_name", length = 1024, nullable = false)
  String aufbewahrungsortName;
  @Column(name = "tei_xml", columnDefinition = "TEXT", length = 2097152)
  @Basic(fetch = FetchType.LAZY)
  String teiXML;
  @Column(name = "loeschdatum", nullable = false)
  LocalDateTime loeschdatum;
  @Column(name = "bearbeiter_id", length = 64, nullable = false)
  String bearbeiterId;
  @Column(name = "bearbeiter_name", length = 1024, nullable = false)
  String bearbeiterName;

  protected GeloeschtesDokument() {

  }

  private GeloeschtesDokument(GeloeschtesDokumentBuilder builder) {
    this.id = HSP_UUI_Factory.generate();
    this.dokumentId = builder.dokumentId;
    this.dokumentObjektTyp = builder.dokumentObjektTyp;
    this.gueltigeSignatur = builder.gueltigeSignatur;
    this.alternativeSignaturen = builder.alternativeSignaturen;
    this.internePurls = builder.internePurls;
    this.besitzerId = builder.besitzerId;
    this.besitzerName = builder.besitzerName;
    this.aufbewahrungsortId = builder.aufbewahrungsortId;
    this.aufbewahrungsortName = builder.aufbewahrungsortName;
    this.teiXML = builder.teiXML;
    this.loeschdatum = builder.loeschdatum;
    this.bearbeiterId = builder.bearbeiterId;
    this.bearbeiterName = builder.bearbeiterName;
  }

  public static GeloeschtesDokumentBuilder builder() {
    return new GeloeschtesDokumentBuilder();
  }

  public String getId() {
    return id;
  }

  public String getDokumentId() {
    return dokumentId;
  }

  public DokumentObjektTyp getDokumentObjektTyp() {
    return dokumentObjektTyp;
  }

  public String getGueltigeSignatur() {
    return gueltigeSignatur;
  }

  public String getAlternativeSignaturen() {
    return alternativeSignaturen;
  }

  public String getInternePurls() {
    return internePurls;
  }

  public String getBesitzerId() {
    return besitzerId;
  }

  public String getBesitzerName() {
    return besitzerName;
  }

  public String getAufbewahrungsortId() {
    return aufbewahrungsortId;
  }

  public String getAufbewahrungsortName() {
    return aufbewahrungsortName;
  }

  public String getTeiXML() {
    return teiXML;
  }

  public LocalDateTime getLoeschdatum() {
    return loeschdatum;
  }

  public String getBearbeiterId() {
    return bearbeiterId;
  }

  public String getBearbeiterName() {
    return bearbeiterName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof GeloeschtesDokument)) {
      return false;
    }
    GeloeschtesDokument that = (GeloeschtesDokument) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", GeloeschtesDokument.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("dokumentId='" + dokumentId + "'")
        .add("dokumentObjektTyp='" + dokumentObjektTyp + "'")
        .add("gueltigeSignatur='" + gueltigeSignatur + "'")
        .add("alternativeSignaturen='" + alternativeSignaturen + "'")
        .add("internePurls='" + internePurls + "'")
        .add("besitzerId='" + besitzerId + "'")
        .add("besitzerName='" + besitzerName + "'")
        .add("aufbewahrungsortId='" + aufbewahrungsortId + "'")
        .add("aufbewahrungsortName='" + aufbewahrungsortName + "'")
        .add("loeschdatum=" + loeschdatum)
        .add("bearbeiterId='" + bearbeiterId + "'")
        .add("bearbeiterName='" + bearbeiterName + "'")
        .add("teiXML='" + (Objects.isNull(teiXML) ? "null" : teiXML.length()) + "'")
        .toString();
  }

  public static class GeloeschtesDokumentBuilder {

    private String dokumentId;
    private DokumentObjektTyp dokumentObjektTyp;
    private String gueltigeSignatur;
    private String alternativeSignaturen;
    private String internePurls;
    private String besitzerId;
    private String besitzerName;
    private String aufbewahrungsortId;
    private String aufbewahrungsortName;
    private String teiXML;
    private LocalDateTime loeschdatum;
    private String bearbeiterId;
    private String bearbeiterName;

    public GeloeschtesDokumentBuilder withDokumentId(String dokumentId) {
      this.dokumentId = dokumentId;
      return this;
    }

    public GeloeschtesDokumentBuilder withDokumentObjektTyp(DokumentObjektTyp dokumentObjektTyp) {
      this.dokumentObjektTyp = dokumentObjektTyp;
      return this;
    }

    public GeloeschtesDokumentBuilder withGueltigeSignatur(String gueltigeSignatur) {
      this.gueltigeSignatur = gueltigeSignatur;
      return this;
    }

    public GeloeschtesDokumentBuilder withAlternativeSignaturen(String alternativeSignaturen) {
      this.alternativeSignaturen = alternativeSignaturen;
      return this;
    }

    public GeloeschtesDokumentBuilder withInternePurls(String internePurls) {
      this.internePurls = internePurls;
      return this;
    }

    public GeloeschtesDokumentBuilder withBesitzerId(String besitzerId) {
      this.besitzerId = besitzerId;
      return this;
    }

    public GeloeschtesDokumentBuilder withBesitzerName(String besitzerName) {
      this.besitzerName = besitzerName;
      return this;
    }

    public GeloeschtesDokumentBuilder withAufbewahrungsortId(String aufbewahrungsortId) {
      this.aufbewahrungsortId = aufbewahrungsortId;
      return this;
    }

    public GeloeschtesDokumentBuilder withAufbewahrungsortName(String aufbewahrungsortName) {
      this.aufbewahrungsortName = aufbewahrungsortName;
      return this;
    }

    public GeloeschtesDokumentBuilder withTeiXML(String teiXML) {
      this.teiXML = teiXML;
      return this;
    }

    public GeloeschtesDokumentBuilder withLoeschdatum(LocalDateTime loeschdatum) {
      this.loeschdatum = loeschdatum;
      return this;
    }

    public GeloeschtesDokumentBuilder withBearbeiterId(String bearbeiterId) {
      this.bearbeiterId = bearbeiterId;
      return this;
    }

    public GeloeschtesDokumentBuilder withBearbeiterName(String bearbeiterName) {
      this.bearbeiterName = bearbeiterName;
      return this;
    }

    public GeloeschtesDokument build() {
      return new GeloeschtesDokument(this);
    }
  }

}
