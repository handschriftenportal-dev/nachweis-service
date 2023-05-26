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

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact.GNDEntityFactBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GraphQlQuery.Field;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GraphQlQuery.Query;
import java.util.List;
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
      .of(GNDEntityFact.ID, GNDEntityFact.TYPE_NAME, GNDEntityFact.GND_IDENTIFIER, GNDEntityFact.PREFERRED_NAME);

  public static final String FIELDS_LIST_STRING = String.join(" ", FIELDS_LIST);

  public static final String FIELDS_SET_STRING = GNDEntityFact.FIELDS_MIN.stream()
      .map(Field::toQueryString)
      .collect(Collectors.joining(", "));

  @Test
  public void testFindCorporateBodyWithID() {
    GraphQlQuery query = new GraphQlQuery(Query.findCorporateBodyWithID(ID, FIELDS_LIST));
    assertEquals("{ findCorporateBodyWithID(id:\"" + ID + "\") { " + FIELDS_LIST_STRING + " }}", query.getQuery());
  }

  @Test
  public void testFindPersonWithName() {
    GraphQlQuery query = new GraphQlQuery(Query.findPersonWithName(NAME, FIELDS_LIST));
    assertEquals("{ findPersonWithName(name:\"" + NAME + "\") { " + FIELDS_LIST_STRING + " }}", query.getQuery());
  }

  @Test
  public void testFindLanguageWithName() {
    GraphQlQuery query = new GraphQlQuery(Query.findLanguageWithName(NAME, GNDEntityFact.FIELDS_MIN));
    assertEquals("{ findLanguageWithName (name:\"" + NAME + "\")  {" + FIELDS_SET_STRING + "} }",
        query.getQuery());
  }

  @Test
  public void testFindLanguageWithId() {
    GraphQlQuery query = new GraphQlQuery(Query.findLanguageWithID(ID, GNDEntityFact.FIELDS_MIN));
    assertEquals("{ findLanguageWithID (id:\"" + ID + "\")  {" + FIELDS_SET_STRING + "} }",
        query.getQuery());
  }

  @Test
  public void testFindGNDEntityFacts() {
    GraphQlQuery query = new GraphQlQuery(Query.findGNDEntityFacts(ID, "Language", GNDEntityFact.FIELDS_MIN));
    assertEquals(
        "{ findGNDEntityFacts (idOrName:\"" + ID + "\", nodeLabel:\"Language\") {" + FIELDS_SET_STRING + "} }",
        query.getQuery());
  }

  @Test
  public void testFindCorporateBodiesByPlaceId() {
    GraphQlQuery query = new GraphQlQuery(Query.findCorporateBodiesByPlaceId(ID, GNDEntityFact.FIELDS_MIN));
    assertEquals(
        "{ findCorporateBodiesByPlaceId (placeId:\"" + ID + "\")  {" + FIELDS_SET_STRING + "} }",
        query.getQuery());
  }

  @Test
  public void testCreateGNDEntityFactAsNode() {
    GNDEntityFact wolfenbuettel = new GNDEntityFactBuilder()
        .withTypeName(GNDEntityFact.PLACE_TYPE_NAME)
        .withPreferredName("Wolfenbüttel")
        .withGndIdentifier("4066832-0")
        .addIdentifier(new Identifier("6557395", "http://www.geonames.org/6557395", "Geonames"))
        .addIdentifier(new Identifier("7005253", "http://vocab.getty.edu/tgn/7005253", "Getty"))
        .addVariantName(new VariantName("Guelpherbytum", "de"))
        .addVariantName(new VariantName("Guelpherbyti", "de"))
        .addVariantName(new VariantName("Wolffenbüttel", null))
        .build();

    GraphQlQuery query = new GraphQlQuery(Query.createGNDEntityFactAsNode(wolfenbuettel));
    assertEquals("mutation {create(gndEntityFact: {"
            + "gndIdentifier:\"4066832-0\","
            + "preferredName:\"Wolfenbüttel\","
            + "typeName:\"Place\","
            + "identifier:[{text:\"6557395\",url:\"http://www.geonames.org/6557395\",type:\"Geonames\"},"
            + "{text:\"7005253\",url:\"http://vocab.getty.edu/tgn/7005253\",type:\"Getty\"}],"
            + "variantName:[{name:\"Wolffenbüttel\"},{name:\"Guelpherbyti\",languageCode:\"de\"},"
            + "{name:\"Guelpherbytum\",languageCode:\"de\"}]}) "
            + "{id, typeName, gndIdentifier, preferredName, "
            + "identifier  {type, text, url}, "
            + "variantName  {name, languageCode}}}",
        query.getQuery());
  }
}
