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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 31.08.20
 */
public class ImportFile {

  @JsonProperty(ImportServiceAPI.IMPORTFILE_ID_FIELD)
  private String id;

  @JsonProperty(ImportServiceAPI.IMPORTFILE_PATH_FIELD)
  private String path;

  @JsonProperty(ImportServiceAPI.IMPORTFILE_DATEINAME_FIELD)
  private String dateiName;

  @JsonProperty(ImportServiceAPI.IMPORTFILE_DATEITYP_FIELD)
  private String dateiTyp;

  @JsonProperty(ImportServiceAPI.IMPORTFILE_DATEIFORMAT_FIELD)
  private String dateiFormat;

  @JsonProperty(ImportServiceAPI.IMPORTJOB_ENTITYDATA_FIELD)
  private List<DataEntity> dataEntityList;

  @JsonProperty(ImportServiceAPI.IMPORTFILE_ERROR_FIELD)
  private boolean error;

  @JsonProperty(ImportServiceAPI.IMPORTFILE_MESSAGE_FIELD)
  private String message;

  public ImportFile() {
    this.dataEntityList = new ArrayList<>();
  }

  public String getId() {
    return id;
  }

  public String getPath() {
    return path;
  }

  public String getDateiName() {
    return dateiName;
  }

  public String getDateiTyp() {
    return dateiTyp;
  }

  public String getDateiFormat() {
    return dateiFormat;
  }

  public List<DataEntity> getDataEntityList() {
    return dataEntityList;
  }

  public boolean isError() {
    return error;
  }

  public String getMessage() {
    return message;
  }

  public String getMessageIfExists(String alternative) {
    if (message != null && !message.isEmpty()) {
      return message;
    }
    return alternative;
  }

  public String getImportedEntities() {
    if (dataEntityList == null) {
      return "";
    }
    Set<String> result = new LinkedHashSet<>();
    for (DataEntity dataEntity : dataEntityList) {
      if (dataEntity.getLabel() != null) {
        result.add(dataEntity.getLabel());
      }
    }
    return String.join(", ", result);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ImportFile that = (ImportFile) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }


  @Override
  public String toString() {
    return new StringJoiner(", ", ImportFile.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("path='" + path + "'")
        .add("dateiName='" + dateiName + "'")
        .add("dateiTyp='" + dateiTyp + "'")
        .add("dateiFormat='" + dateiFormat + "'")
        .add("dataEntityList=" + dataEntityList)
        .add("error=" + error)
        .add("message='" + message + "'")
        .toString();
  }
}
