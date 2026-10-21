package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.normdatenerkennung.NormdatenErkennungErgebnis;
import java.util.List;
import java.util.Objects;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.04.2021.
 * @version 1.0
 * <p>
 * This class represent a GRAPHQL Response Object using JSON Serialization
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

  public static class Data {

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

    @JsonProperty("findSkosConceptsWithConceptScheme")
    private List<Concept> findConcepteWithSchemeType;

    @JsonProperty("findSkosConceptSchemesWithinSchemeConceptScheme")
    private List<ConceptScheme> findSkosConceptSchemesWithinSchemeConceptScheme;

    @JsonProperty("normdatenerkennung")
    private NormdatenErkennungErgebnis normdatenerkennung;

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

    public List<Concept> getFindConcepteWithSchemeType() {
      return findConcepteWithSchemeType;
    }

    public List<ConceptScheme> getFindSkosConceptSchemesWithinSchemeConceptScheme() {
      return findSkosConceptSchemesWithinSchemeConceptScheme;
    }

    public NormdatenErkennungErgebnis getNormdatenerkennung() {
      return normdatenerkennung;
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
          ", findConcepteWithSchemeType=" + findConcepteWithSchemeType +
          ", findSkosConceptSchemesWithinSchemeConceptScheme="
          + findSkosConceptSchemesWithinSchemeConceptScheme +
          '}';
    }
  }
}
