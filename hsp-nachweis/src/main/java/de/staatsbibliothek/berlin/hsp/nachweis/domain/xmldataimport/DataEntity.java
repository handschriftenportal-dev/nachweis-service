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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 10.06.2020.
 * @version 1.0
 */
public class DataEntity {

  public DataEntity() {
  }

  public DataEntity(String id, String label, String url) {
    this.id = id;
    this.label = label;
    this.url = url;
  }

  @JsonProperty(ImportServiceAPI.IMPORTJOB_ID_FIELD)
  private String id;

  @JsonProperty(ImportServiceAPI.IMPORTJOB_LABEL_FIELD)
  private String label;

  @JsonProperty(ImportServiceAPI.IMPORTJOB_URL_FIELD)
  private String url;

  @JsonIgnore
  private String dateiName;

  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public String getUrl() {
    return url;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getDateiName() {
    return dateiName;
  }

  public void setDateiName(String dateiName) {
    this.dateiName = dateiName;
  }
}
