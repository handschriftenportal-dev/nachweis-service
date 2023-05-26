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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.xmldataimport.ImportServiceAPI.IMPORTJOB_RESULT_VALUES;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.05.2020.
 * @version 1.0
 */
public class ImportVorgang {

  @JsonProperty(ImportServiceAPI.IMPORTJOB_ID_FIELD)
  private String id;

  @JsonProperty(ImportServiceAPI.IMPORTJOB_BENUTZER_FIELD)
  private String benutzerName;

  @JsonProperty(ImportServiceAPI.IMPORTJOB_NAME_FIELD)
  private String name;

  @JsonProperty(ImportServiceAPI.IMPORTJOB_CREATIONDATE_FIELD)
  private LocalDateTime datum;

  @JsonProperty(ImportServiceAPI.IMPORTJOB_RESULT_FIELD)
  private ImportServiceAPI.IMPORTJOB_RESULT_VALUES status;

  @JsonProperty(ImportServiceAPI.IMPORTJOB_ERRORMESSAGE_FIELD)
  private String fehler;

  @JsonProperty(ImportServiceAPI.IMPORTJOB_DATATYPE_FIELD)
  private String datentyp;

  @JsonProperty(ImportServiceAPI.IMPORTJOB_IMPORT_DIR)
  private String importDir;

  @JsonProperty(ImportServiceAPI.IMPORTJOB_IMPORTFILES_FIELD)
  private List<ImportFile> importFiles;

  public ImportVorgang() {
    this.importFiles = new ArrayList<>();
  }

  public ImportVorgang(String id, String benutzerName, String name, LocalDateTime datum,
      ImportServiceAPI.IMPORTJOB_RESULT_VALUES status, String fehler,
      String datentyp) {
    this.id = id;
    this.benutzerName = benutzerName;
    this.name = name;
    this.datum = datum;
    this.status = status;
    this.fehler = fehler;
    this.datentyp = datentyp;
    this.importFiles = new ArrayList<>();
  }

  public String getId() {
    return id;
  }

  public String getBenutzerName() {
    return benutzerName;
  }

  public String getName() {
    return name;
  }

  public IMPORTJOB_RESULT_VALUES getStatus() {
    return status;
  }

  public void setStatus(
      IMPORTJOB_RESULT_VALUES status) {
    this.status = status;
  }

  public boolean isError() {
    return IMPORTJOB_RESULT_VALUES.FAILED == status;
  }

  public boolean isOtherStatus() {
    return IMPORTJOB_RESULT_VALUES.FAILED != status && IMPORTJOB_RESULT_VALUES.SUCCESS != status;
  }

  public String getFehler() {
    return fehler;
  }

  public void setFehler(String fehler) {
    this.fehler = fehler;
  }

  public String getDatentyp() {
    return datentyp;
  }

  public String getDateiFormatAsString() {
    if (importFiles == null) {
      return "";
    }
    Set<String> result = new LinkedHashSet<>();
    for (ImportFile importFile : importFiles) {
      if (importFile.getDateiFormat() != null) {
        result.add(importFile.getDateiFormat());
      }
    }
    return String.join(", ", result);
  }

  public List<ImportFile> getImportFiles() {
    return importFiles;
  }

  public List<DataEntity> getDataEntities() {
    List<DataEntity> result = new ArrayList<>();
    for (ImportFile importFile : importFiles) {
      for (DataEntity dataEntity : importFile.getDataEntityList()) {
        dataEntity.setDateiName(importFile.getDateiName());
        result.add(dataEntity);
      }
    }
    return result;
  }

  public LocalDateTime getDatum() {
    return datum;
  }

  public String getImportDir() {
    return importDir;
  }

  public boolean hasImportEntityData() {
    return importFiles.stream().anyMatch(importFile -> !importFile.getDataEntityList().isEmpty());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ImportVorgang that = (ImportVorgang) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }


  @Override
  public String toString() {
    return new StringJoiner(", ", ImportVorgang.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("benutzerName='" + benutzerName + "'")
        .add("name='" + name + "'")
        .add("datum=" + datum)
        .add("status=" + status)
        .add("fehler='" + fehler + "'")
        .add("datentyp='" + datentyp + "'")
        .add("importDir='" + importDir + "'")
        .add("importFiles=" + importFiles)
        .toString();
  }
}
