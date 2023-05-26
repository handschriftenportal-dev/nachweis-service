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

package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.beschreibungsimport.ImportStatus;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Created by konrad.eichstaedt@sbb.spk-berlin.de on 06.06.2019.
 */
public class ImportViewModel implements Serializable {

  private static final long serialVersionUID = -3353342249524848644L;

  private String id;

  private String benutzer;

  private String institution;

  private String dateiName;

  private String importUrl;

  private LocalDateTime importDatum;

  private ImportStatus importStatus;

  private String fehler;

  private List<BeschreibungsViewModel> beschreibung;

  private boolean isSelectedForImport;

  ImportViewModel(ImportViewModelBuilder builder) {

    this.id = builder.id;
    this.benutzer = builder.benutzer;
    this.institution = builder.institution;
    this.dateiName = builder.dateiName;
    this.importUrl = builder.importUrl;
    this.importStatus = builder.importStatus;
    this.importDatum = builder.importDatum;
    this.beschreibung = builder.beschreibung;
    this.isSelectedForImport = false;
    this.fehler = builder.fehler;
    this.isSelectedForImport = builder.isSelectedForImport;
  }

  public String getId() {
    return id;
  }

  public String getBenutzer() {
    return benutzer;
  }

  public String getInstitution() {
    return institution;
  }

  public String getDateiName() {
    return dateiName;
  }

  public String getImportUrl() {
    return importUrl;
  }

  public LocalDateTime getImportDatum() {
    return importDatum;
  }

  public List<BeschreibungsViewModel> getBeschreibung() {
    return beschreibung;
  }

  public ImportStatus getImportStatus() {
    return importStatus;
  }

  public void setImportStatus(ImportStatus importStatus) {
    this.importStatus = importStatus;
  }

  public void setFehler(String fehler) {
    this.fehler = fehler;
  }

  public void setBeschreibung(
      List<BeschreibungsViewModel> beschreibung) {
    this.beschreibung = beschreibung;
  }

  public boolean isSelectedForImport() {
    return isSelectedForImport;
  }

  public void setSelectedForImport(boolean selectedForImport) {
    isSelectedForImport = selectedForImport;
  }

  public String getFehler() {
    return fehler;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ImportViewModel)) {
      return false;
    }
    ImportViewModel that = (ImportViewModel) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  public static class ImportViewModelBuilder {

    private String id;
    private String benutzer;
    private String institution;
    private String dateiName;
    private String importUrl;
    private LocalDateTime importDatum;
    private ImportStatus importStatus;
    private List<BeschreibungsViewModel> beschreibung;
    private String fehler;
    private boolean isSelectedForImport;

    public ImportViewModelBuilder withId(String id) {
      this.id = id;
      return this;
    }

    public ImportViewModelBuilder withBenutzer(String benutzer) {
      this.benutzer = benutzer;
      return this;
    }

    public ImportViewModelBuilder withInstitution(String institution) {
      this.institution = institution;
      return this;
    }

    public ImportViewModelBuilder withDateiName(String dateiName) {
      this.dateiName = dateiName;
      return this;
    }

    public ImportViewModelBuilder withImportUrl(String importUrl) {
      this.importUrl = importUrl;
      return this;
    }

    public ImportViewModelBuilder withImportDatum(LocalDateTime importDatum) {
      this.importDatum = importDatum;
      return this;
    }

    public ImportViewModelBuilder withImportStatus(ImportStatus importStatus) {
      this.importStatus = importStatus;
      return this;
    }

    public ImportViewModelBuilder withBeschreibung(List<BeschreibungsViewModel> beschreibung) {
      this.beschreibung = beschreibung;
      return this;
    }

    public ImportViewModelBuilder withFehler(String fehler) {
      this.fehler = fehler;
      return this;
    }

    public ImportViewModelBuilder withSelection(boolean isSelectedForImport) {
      this.isSelectedForImport = isSelectedForImport;
      return this;
    }

    public ImportViewModel build() {
      return new ImportViewModel(this);
    }
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ImportViewModel.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("benutzer='" + benutzer + "'")
        .add("institution='" + institution + "'")
        .add("dateiName='" + dateiName + "'")
        .add("importUrl='" + importUrl + "'")
        .add("importDatum='" + importDatum + "'")
        .add("importStatus=" + importStatus)
        .add("fehler='" + fehler + "'")
        .add("beschreibung=" + beschreibung)
        .add("isSelectedForImport=" + isSelectedForImport)
        .toString();
  }
}
