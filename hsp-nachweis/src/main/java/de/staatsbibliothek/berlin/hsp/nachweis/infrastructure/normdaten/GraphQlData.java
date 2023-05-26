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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.04.2021.
 * @version 1.0
 */
public class GraphQlData {

  @JsonProperty("data")
  private Data data;

  public Data getData() {
    return data;
  }

  public void setData(Data data) {
    this.data = data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GraphQlData that = (GraphQlData) o;
    return Objects.equals(data, that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data);
  }

  @Override
  public String toString() {
    return "GraphQlData{" +
        "data=" + data +
        '}';
  }

  public class Data {

    @JsonProperty("findCorporateBodyWithID")
    private GNDEntityFact findCorporateBodyWithID;

    @JsonProperty("findPersonWithName")
    private List<GNDEntityFact> findPersonWithName;

    @JsonProperty("findLanguageWithName")
    private List<GNDEntityFact> findLanguageWithName;

    @JsonProperty("findLanguageWithID")
    private GNDEntityFact findLanguageWithID;

    @JsonProperty("findGNDEntityFacts")
    private List<GNDEntityFact> findGNDEntityFacts;

    @JsonProperty("findCorporateBodiesByPlaceId")
    private List<GNDEntityFact> findCorporateBodiesByPlaceId;

    @JsonProperty("create")
    private GNDEntityFact create;

    public GNDEntityFact getFindCorporateBodyWithID() {
      return findCorporateBodyWithID;
    }

    public List<GNDEntityFact> getFindPersonWithName() {
      return findPersonWithName;
    }

    public List<GNDEntityFact> getFindLanguageWithName() {
      return findLanguageWithName;
    }

    public GNDEntityFact getFindLanguageWithID() {
      return findLanguageWithID;
    }

    public List<GNDEntityFact> getFindGNDEntityFacts() {
      return findGNDEntityFacts;
    }

    public List<GNDEntityFact> getFindCorporateBodiesByPlaceId() {
      return findCorporateBodiesByPlaceId;
    }

    public GNDEntityFact getCreate() {
      return create;
    }

    @Override
    public String toString() {
      return "Data{" +
          "findCorporateBodyWithID=" + findCorporateBodyWithID +
          ", findPersonWithName=" + findPersonWithName +
          ", findLanguageWithName=" + findLanguageWithName +
          ", findLanguageWithID=" + findLanguageWithID +
          ", findGNDEntityFacts=" + findGNDEntityFacts +
          ", findCorporateBodiesByPlaceId=" + findCorporateBodiesByPlaceId +
          ", create=" + create +
          '}';
    }
  }
}
