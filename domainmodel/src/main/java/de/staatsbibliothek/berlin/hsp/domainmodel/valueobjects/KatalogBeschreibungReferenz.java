/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects;

import java.net.URI;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
  private String beschreibungsID;

  @Column(name = "seite_von")
  private String seiteVon;

  @Column(name = "seite_bis")
  private String seiteBis;

  @Column(name = "ocr_volltext", columnDefinition = "TEXT", length = 2097152)
  private String ocrVolltext;

  @Column(name = "manifest_range_url")
  private String manifestRangeURL;

  protected KatalogBeschreibungReferenz() {
  }

  public KatalogBeschreibungReferenz(String beschreibungsID, String seiteVon,
      String seiteBis, String ocrVolltext, URI manifestRangeURL) {
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

  public String getSeiteBis() {
    return seiteBis;
  }

  public String getOcrVolltext() {
    return ocrVolltext;
  }

  public URI getManifestRangeURL() {
    if (manifestRangeURL == null) {
      return null;
    }
    return URI.create(manifestRangeURL);
  }

  public void setSeiteVon(String seiteVon) {
    this.seiteVon = seiteVon;
  }

  public void setSeiteBis(String seiteBis) {
    this.seiteBis = seiteBis;
  }

  public void setOcrVolltext(String ocrVolltext) {
    this.ocrVolltext = ocrVolltext;
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
