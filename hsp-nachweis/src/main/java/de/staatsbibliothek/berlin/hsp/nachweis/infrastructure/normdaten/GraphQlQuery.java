package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.normdatenerkennung.NormdatenErkennungAnfrage;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.ToString;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.04.2021.
 * @version 1.0
 */

@ToString
public class GraphQlQuery {

  @JsonProperty("query")
  private final String query;

  public GraphQlQuery(Query query) {
    this.query = query.value;
  }

  public String getQuery() {
    return query;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GraphQlQuery that = (GraphQlQuery) o;
    return Objects.equals(query, that.query);
  }

  @Override
  public int hashCode() {
    return Objects.hash(query);
  }

  public static class Query {

    private final String value;

    private Query(String value) {
      this.value = value;
    }

    public static Query findCorporateBodyWithID(String id,
        List<String> fields) {
      return new Query(
          "{ findCorporateBodyWithID(id:\"" + id + "\") { " + String.join(" ", fields) + " }}");
    }

    public static Query findPersonWithName(String name, List<String> fields) {
      return new Query(
          "{ findPersonWithName(name:\"" + name + "\") { " + String.join(" ",
              fields) + " }}");
    }

    public static Query findLanguageWithName(String name, Set<Field> fields) {
      return new Query("{ findLanguageWithName "
          + (Objects.isNull(name) ? "" : "(name:\"" + name + "\") ")
          + fields.stream()
          .map(Field::toQueryString)
          .filter(f -> !f.isBlank())
          .collect(Collectors.joining(", ", " {", "}"))
          + " }");
    }

    public static Query findLanguageWithID(String id, Set<Field> fields) {
      return new Query("{ findLanguageWithID "
          + (Objects.isNull(id) ? "" : "(id:\"" + id + "\") ")
          + fields.stream()
          .map(Field::toQueryString)
          .filter(f -> !f.isBlank())
          .collect(Collectors.joining(", ", " {", "}"))
          + " }");
    }

    public static Query findGNDEntityFacts(String idOrName, String nodeLabel,
        Set<Field> fields) {
      Optional<String> optIdOrName = Optional.ofNullable(idOrName);
      Optional<String> optTypeName = Optional.ofNullable(nodeLabel);

      return new Query("{ findGNDEntityFacts "
          + (optIdOrName.isPresent() || optTypeName.isPresent() ? "(" : "")
          + (optIdOrName.isPresent() ? "idOrName:\"" + idOrName + "\"" : "")
          + (optIdOrName.isPresent() && optTypeName.isPresent() ? ", " : "")
          + (optTypeName.isPresent() ? "nodeLabel:\"" + nodeLabel + "\"" : "")
          + (optIdOrName.isPresent() || optTypeName.isPresent() ? ") " : "")
          + fields.stream()
          .map(Field::toQueryString)
          .filter(f -> !f.isBlank())
          .collect(Collectors.joining(", ", "{", "}"))
          + " }");
    }

    public static Query findCorporateBodiesByPlaceId(String placeId,
        Set<Field> fields) {
      return new Query("{ findCorporateBodiesByPlaceId "
          + "(placeId:\"" + placeId + "\") "
          + fields.stream()
          .map(Field::toQueryString)
          .filter(f -> !f.isBlank())
          .collect(Collectors.joining(", ", " {", "}"))
          + " }");
    }

    public static Query createGNDEntityFactAsNode(GNDEntityFact gndEntityFact) {
      String identifiers = Stream.ofNullable(gndEntityFact.getIdentifier())
          .flatMap(Set::stream)
          .map(identifier -> "{"
              + Identifier.TEXT + ":\"" + identifier.getText() + "\""
              + (Objects.nonNull(identifier.getUrl()) ?
              "," + Identifier.URL + ":\"" + identifier.getUrl() + "\"" : "")
              + (Objects.nonNull(identifier.getType()) ?
              "," + Identifier.TYPE + ":\"" + identifier.getType() + "\"" : "")
              + "}")
          .collect(Collectors.joining(",", "[", "]"));

      String variantNames = Stream.ofNullable(gndEntityFact.getVariantName())
          .flatMap(Set::stream)
          .map(variantName -> "{"
              + VariantName.NAME + ":\"" + variantName.getName() + "\""
              + (Objects.nonNull(variantName.getLanguageCode()) ?
              "," + VariantName.LANGUAGE_CODE + ":\""
              + variantName.getLanguageCode() + "\"" : "")
              + "}")
          .collect(Collectors.joining(",", "[", "]"));

      return new Query("mutation {create(gndEntityFact: {"
          + GNDEntityFact.GND_IDENTIFIER + ":\""
          + gndEntityFact.getGndIdentifier() + "\","
          + GNDEntityFact.PREFERRED_NAME + ":\""
          + gndEntityFact.getPreferredName() + "\","
          + GNDEntityFact.TYPE_NAME + ":\"" + gndEntityFact.getTypeName()
          + "\","
          + GNDEntityFact.IDENTIFIER + ":" + identifiers + ","
          + GNDEntityFact.VARIANT_NAME + ":" + variantNames + "})"
          + GNDEntityFact.FIELDS.stream()
          .map(Field::toQueryString)
          .filter(f -> !f.isBlank())
          .collect(Collectors.joining(", ", " {", "}"))
          + "}");
    }

    public static Query findConcepteWithSchemeType(String notation,
        Set<Field> fields) {
      return new Query("{ findSkosConceptsWithConceptScheme "
          + (Objects.isNull(notation) ? "" : "(notation:\"" + notation + "\") ")
          + fields.stream()
          .map(Field::toQueryString)
          .filter(f -> !f.isBlank())
          .collect(Collectors.joining(", ", " {", "}"))
          + " }");
    }

    public static Query findSkosConceptSchemesWithinSchemeConceptScheme(
        String notation,
        Set<Field> fields) {
      return new Query("{ findSkosConceptSchemesWithinSchemeConceptScheme "
          + (Objects.isNull(notation) ? "" : "(notation:\"" + notation + "\") ")
          + fields.stream()
          .map(Field::toQueryString)
          .filter(f -> !f.isBlank())
          .collect(Collectors.joining(", ", " {", "}"))
          + " }");
    }

    public static Query normdatenerkennung(NormdatenErkennungAnfrage anfrage) {
      return new Query("{ normdatenerkennung(anfrage:{id:\"" + anfrage.id()
          + "\",name:\"" + anfrage.name() + "\",typ:" + anfrage.typ().name()
          + "}) { \n"
          + "  error,id,treffer{id,gndid,name,quelle, otherIDs},empfehlung{aehnlichkeit,treffer{id,gndid,name,quelle}}\n"
          + "}}");
    }
  }

  public static class Field {

    private final String name;

    private final Set<Field> fields;

    public Field(String name) {
      this.name = name;
      this.fields = Collections.emptySet();
    }

    public Field(String name, Set<Field> fields) {
      this.name = name;
      this.fields = fields;
    }

    public static Field of(String name) {
      return new Field(name);
    }

    public static Field of(String name, Set<Field> fields) {
      return new Field(name, fields);
    }

    public String toQueryString() {
      if (Objects.isNull(name) || name.isBlank()) {
        return "";
      }
      if (Objects.isNull(fields) || fields.isEmpty()) {
        return name;
      }

      return name + " " + fields.stream()
          .map(Field::toQueryString)
          .filter(f -> !f.isBlank())
          .collect(Collectors.joining(", ", " {", "}"));
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Field field = (Field) o;
      return Objects.equals(name, field.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name);
    }

    @Override
    public String toString() {
      return "SubField{" +
          "name='" + name + '\'' +
          ", fields=" + fields +
          ", subFields=" + fields +
          '}';
    }
  }
}
