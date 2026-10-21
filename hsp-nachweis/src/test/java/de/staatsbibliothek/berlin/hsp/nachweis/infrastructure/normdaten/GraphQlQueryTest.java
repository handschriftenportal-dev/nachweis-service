package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact.GNDEntityFactBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GraphQlQuery.Field;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GraphQlQuery.Query;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.graphql.normdatenerkennung.NormdatenErkennungAnfrage;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 22.07.21
 */

public class GraphQlQueryTest {

  public static final String ID = "test_id";
  public static final String NAME = "test_name";
  public static final List<String> FIELDS_LIST = List
      .of(GNDEntityFact.ID, GNDEntityFact.TYPE_NAME,
          GNDEntityFact.GND_IDENTIFIER,
          GNDEntityFact.PREFERRED_NAME);

  public static final String FIELDS_LIST_STRING = String.join(" ", FIELDS_LIST);

  public static final String FIELDS_SET_STRING = GNDEntityFact.FIELDS_MIN.stream()
      .map(Field::toQueryString)
      .collect(Collectors.joining(", "));

  public static final String KONZEPTSCHEME_NOTATION = "completeness";

  @Test
  public void testFindCorporateBodyWithID() {
    GraphQlQuery query = new GraphQlQuery(
        Query.findCorporateBodyWithID(ID, FIELDS_LIST));
    assertEquals(
        "{ findCorporateBodyWithID(id:\"" + ID + "\") { " + FIELDS_LIST_STRING
            + " }}",
        query.getQuery());
  }

  @Test
  public void testFindPersonWithName() {
    GraphQlQuery query = new GraphQlQuery(
        Query.findPersonWithName(NAME, FIELDS_LIST));
    assertEquals(
        "{ findPersonWithName(name:\"" + NAME + "\") { " + FIELDS_LIST_STRING
            + " }}",
        query.getQuery());
  }

  @Test
  public void testFindLanguageWithName() {
    GraphQlQuery query = new GraphQlQuery(
        Query.findLanguageWithName(NAME, GNDEntityFact.FIELDS_MIN));
    assertEquals(
        "{ findLanguageWithName (name:\"" + NAME + "\")  {" + FIELDS_SET_STRING
            + "} }",
        query.getQuery());
  }

  @Test
  public void testFindLanguageWithId() {
    GraphQlQuery query = new GraphQlQuery(
        Query.findLanguageWithID(ID, GNDEntityFact.FIELDS_MIN));
    assertEquals(
        "{ findLanguageWithID (id:\"" + ID + "\")  {" + FIELDS_SET_STRING
            + "} }",
        query.getQuery());
  }

  @Test
  public void testFindGNDEntityFacts() {
    GraphQlQuery query = new GraphQlQuery(
        Query.findGNDEntityFacts(ID, "Language", GNDEntityFact.FIELDS_MIN));
    assertEquals(
        "{ findGNDEntityFacts (idOrName:\"" + ID
            + "\", nodeLabel:\"Language\") {"
            + FIELDS_SET_STRING + "} }",
        query.getQuery());
  }

  @Test
  public void testFindCorporateBodiesByPlaceId() {
    GraphQlQuery query = new GraphQlQuery(
        Query.findCorporateBodiesByPlaceId(ID, GNDEntityFact.FIELDS_MIN));
    assertEquals(
        "{ findCorporateBodiesByPlaceId (placeId:\"" + ID + "\")  {"
            + FIELDS_SET_STRING + "} }",
        query.getQuery());
  }

  @Test
  void testfindConcepteWithSchemeType() {

    Set<Field> fields = new LinkedHashSet<>(Arrays.asList(
        Field.of(Concept.ID),
        Field.of(Concept.URI),
        Field.of(Concept.NOTATION)
    ));

    GraphQlQuery query = new GraphQlQuery(
        Query.findConcepteWithSchemeType(KONZEPTSCHEME_NOTATION, fields));

    assertEquals(
        "{ findSkosConceptsWithConceptScheme (notation:\"completeness\")  {id, uri, notation} }",
        query.getQuery());
  }

  @Test
  void testfindSkosConceptSchemesWithinSchemeConceptScheme() {

    Set<Field> fields = new LinkedHashSet<>(Arrays.asList(
        Field.of(Concept.ID),
        Field.of(Concept.URI),
        Field.of(Concept.NOTATION)
    ));

    GraphQlQuery query = new GraphQlQuery(
        Query.findSkosConceptSchemesWithinSchemeConceptScheme(
            KONZEPTSCHEME_NOTATION, fields));

    assertEquals(
        "{ findSkosConceptSchemesWithinSchemeConceptScheme (notation:\"completeness\")  {id, uri, notation} }",
        query.getQuery());
  }

  @Test
  public void testCreateGNDEntityFactAsNode() {
    GNDEntityFact wolfenbuettel = new GNDEntityFactBuilder()
        .withTypeName(GNDEntityFact.PLACE_TYPE_NAME)
        .withPreferredName("Wolfenbüttel")
        .withGndIdentifier("4066832-0")
        .addIdentifier(
            new Identifier("6557395", "http://www.geonames.org/6557395",
                "Geonames"))
        .addIdentifier(
            new Identifier("7005253", "http://vocab.getty.edu/tgn/7005253",
                "Getty"))
        .addVariantName(new VariantName("Guelpherbytum", "de"))
        .addVariantName(new VariantName("Guelpherbyti", "de"))
        .addVariantName(new VariantName("Wolffenbüttel", null))
        .build();

    GraphQlQuery query = new GraphQlQuery(
        Query.createGNDEntityFactAsNode(wolfenbuettel));
    assertEquals("mutation {create(gndEntityFact: {"
            + "gndIdentifier:\"4066832-0\","
            + "preferredName:\"Wolfenbüttel\","
            + "typeName:\"Place\","
            + "identifier:[{text:\"6557395\",url:\"http://www.geonames.org/6557395\",type:\"Geonames\"},"
            + "{text:\"7005253\",url:\"http://vocab.getty.edu/tgn/7005253\",type:\"Getty\"}],"
            + "variantName:[{name:\"Wolffenbüttel\"},{name:\"Guelpherbyti\",languageCode:\"de\"},"
            + "{name:\"Guelpherbytum\",languageCode:\"de\"}]}) "
            + "{id, uri, typeName, gndIdentifier, preferredName, "
            + "identifier  {type, text, url}, "
            + "variantName  {name, languageCode}}}",
        query.getQuery());
  }

  @Test
  void testNormdatenErkennungQuery() {
    NormdatenErkennungAnfrage anfrage = new NormdatenErkennungAnfrage(
        "1234567890", "Eichstädt", NormdatenTyp.Person);

    GraphQlQuery query = new GraphQlQuery(Query.normdatenerkennung(anfrage));

    assertEquals(
        """
            { normdatenerkennung(anfrage:{id:"1234567890",name:"Eichstädt",typ:Person}) {\s
              error,id,treffer{id,gndid,name,quelle, otherIDs},empfehlung{aehnlichkeit,treffer{id,gndid,name,quelle}}
            }}""",
        query.getQuery());
  }
}
