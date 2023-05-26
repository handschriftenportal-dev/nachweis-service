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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 07.12.2021
 */
public class FilteredFacetteModelTest {

  static Map<String, Integer> fMap;

  @BeforeAll
  static void beforeAll() {
    fMap = new LinkedHashMap<>();
    fMap.put("Berlin", 10);
    fMap.put("Karlsruhe", 20);
    fMap.put("Kiel", 30);
    fMap.put("Konstanz", null);
    fMap.put("Leipzig", 50);
    fMap.put("München", 60);
  }

  @Test
  void testConstructor() {
    FilteredFacetteModel model = new FilteredFacetteModel();
    assertNotNull(model.getFacetten());
    assertNotNull(model.getFilteredFacetten());
  }

  @Test
  void testClearQuery() {
    FilteredFacetteModel model = new FilteredFacetteModel();
    model.updateFacetten(fMap);

    String query = "kon";
    model.setQuery(query);
    model.matchQuery();

    assertEquals(1, model.getFilteredFacetten().size());

    model.clearQuery();

    assertEquals(5, model.getFilteredFacetten().size());
  }

  @Test
  void testMatchQuery() {
    FilteredFacetteModel model = new FilteredFacetteModel();
    model.updateFacetten(fMap);

    assertNotNull(model.getFilteredFacetten());
    assertEquals(5, model.getFilteredFacetten().size());

    String query = "kon";
    model.setQuery(query);
    model.matchQuery();

    assertNotNull(model.getFilteredFacetten());
    assertEquals(1, model.getFilteredFacetten().size());
    assertTrue(model.getFilteredFacetten().stream().anyMatch(ff -> "Konstanz".equals(ff.getValue())));
  }

  @Test
  void testShowCollapseButton() {
    FilteredFacetteModel model = new FilteredFacetteModel();
    model.updateFacetten(fMap);

    assertFalse(model.showCollapseButton());

    model.toggleExpanded();

    assertTrue(model.showCollapseButton());

    String query = "kon";
    model.setQuery(query);
    model.matchQuery();

    assertFalse(model.showCollapseButton());
  }

  @Test
  void testShowExpandButton() {
    FilteredFacetteModel model = new FilteredFacetteModel();
    model.updateFacetten(fMap);

    assertTrue(model.showExpandButton());
    model.toggleExpanded();

    assertFalse(model.showExpandButton());

    model.toggleExpanded();
    assertTrue(model.showExpandButton());

    String query = "kon";
    model.setQuery(query);
    model.matchQuery();

    assertFalse(model.showExpandButton());
  }

  @Test
  void toggleExpanded() {
    FilteredFacetteModel model = new FilteredFacetteModel();

    model.updateFacetten(fMap);
    assertFalse(model.isExpanded());
    assertEquals(5, model.getFilteredFacetten().size());

    model.toggleExpanded();
    assertTrue(model.isExpanded());
    assertEquals(6, model.getFilteredFacetten().size());

    model.toggleExpanded();
    assertFalse(model.isExpanded());
    assertEquals(5, model.getFilteredFacetten().size());
  }

  @Test
  void updateFacetten() {
    FilteredFacetteModel model = new FilteredFacetteModel();

    model.updateFacetten(fMap);

    assertNotNull(model.getFacetten());
    assertEquals(6, model.getFacetten().size());

    assertTrue(model.getFilteredFacetten().stream()
        .anyMatch(ff -> "Berlin".equals(ff.getValue()) && ff.getAmount() == 10));
    assertTrue(model.getFilteredFacetten().stream()
        .anyMatch(ff -> "Konstanz".equals(ff.getValue()) && ff.getAmount() == 0));

    assertNotNull(model.getFilteredFacetten());
    assertEquals(5, model.getFilteredFacetten().size());

    assertFalse(model.getFilteredFacetten().stream().anyMatch(ff -> "München".equals(ff.getValue())));
  }

}
