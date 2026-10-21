package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.net.URI;
import java.util.Objects;

/**
 * Object to Reference a description from catalog
 *
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 18.02.2022.
 * @version 1.0
 */
@Entity
@Table(name = "katalog_beschreibung_referenz")
public class KatalogBeschreibungReferenz {

  @Id
  @Column(name = "beschreibungs_id")
  String beschreibungsID;

  @Column(name = "seite_von")
  String seiteVon;

  @Column(name = "seite_bis")
  String seiteBis;

  @Column(name = "ocr_volltext", columnDefinition = "TEXT", length = 2097152)
  String ocrVolltext;

  @Column(name = "manifest_range_url")
  String manifestRangeURL;

  protected KatalogBeschreibungReferenz() {}

  public KatalogBeschreibungReferenz(
      String beschreibungsID,
      String seiteVon,
      String seiteBis,
      String ocrVolltext,
      URI manifestRangeURL) {
    this.beschreibungsID = beschreibungsID;
    this.seiteVon = seiteVon;
    this.seiteBis = seiteBis;
    this.ocrVolltext = ocrVolltext;
    setManifestRangeURL(manifestRangeURL);
  }

  public String getBeschreibungsID() {
    return beschreibungsID;
  }

  public void setBeschreibungsID(String beschreibungsID) {
    this.beschreibungsID = beschreibungsID;
  }

  public String getSeiteVon() {
    return seiteVon;
  }

  public void setSeiteVon(String seiteVon) {
    this.seiteVon = seiteVon;
  }

  public String getSeiteBis() {
    return seiteBis;
  }

  public void setSeiteBis(String seiteBis) {
    this.seiteBis = seiteBis;
  }

  public String getOcrVolltext() {
    return ocrVolltext;
  }

  public void setOcrVolltext(String ocrVolltext) {
    this.ocrVolltext = ocrVolltext;
  }

  public URI getManifestRangeURL() {
    if (manifestRangeURL == null) {
      return null;
    }
    return URI.create(manifestRangeURL);
  }

  public void setManifestRangeURL(URI manifestRangeURL) {
    if (manifestRangeURL == null) {
      this.manifestRangeURL = null;
    } else {
      this.manifestRangeURL = manifestRangeURL.toASCIIString();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KatalogBeschreibungReferenz that = (KatalogBeschreibungReferenz) o;
    return Objects.equals(beschreibungsID, that.beschreibungsID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(beschreibungsID);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("KatalogBeschreibungReferenz{");
    sb.append("beschreibungsID='").append(beschreibungsID).append('\'');
    sb.append(", seiteVon='").append(seiteVon).append('\'');
    sb.append(", seiteBis='").append(seiteBis).append('\'');
    sb.append(", ocrVolltext='").append(ocrVolltext).append('\'');
    sb.append(", manifestRangeURL='").append(manifestRangeURL).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
