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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 24.11.22
 */
class HspLazyDataModelTest {

  @Test
  void testMapFilterBy() {
    HspLazyDataModel<Object> model = createHspLazyDataModel();

    Map<String, String> mappedFilterBy = model.mapFilterBy(null);
    assertNotNull(mappedFilterBy);
    assertEquals(0, mappedFilterBy.entrySet().size());

    mappedFilterBy = model.mapFilterBy(null);
    assertNotNull(mappedFilterBy);
    assertEquals(0, mappedFilterBy.entrySet().size());

    Map<String, FilterMeta> filterBy = new HashMap<>();
    filterBy.put(null, null);
    mappedFilterBy = model.mapFilterBy(filterBy);
    assertNotNull(mappedFilterBy);
    assertEquals(0, mappedFilterBy.entrySet().size());

    filterBy = new HashMap<>();
    filterBy.put("key", FilterMeta.builder().field("field").filterValue("value").build());
    mappedFilterBy = model.mapFilterBy(filterBy);
    assertNotNull(mappedFilterBy);
    assertEquals(1, mappedFilterBy.entrySet().size());
    assertTrue(mappedFilterBy.entrySet().stream()
        .anyMatch(entry -> "key".equals(entry.getKey()) && "value".equals(entry.getValue())));
  }

  @Test
  void testMapSortBy() {
    HspLazyDataModel<Object> model = createHspLazyDataModel();

    Map<String, Boolean> mappedSortBy = model.mapSortBy(null);
    assertNotNull(mappedSortBy);
    assertEquals(0, mappedSortBy.entrySet().size());

    mappedSortBy = model.mapSortBy(null);
    assertNotNull(mappedSortBy);
    assertEquals(0, mappedSortBy.entrySet().size());

    Map<String, SortMeta> sortBy = new HashMap<>();
    sortBy.put(null, null);
    mappedSortBy = model.mapSortBy(sortBy);
    assertNotNull(mappedSortBy);
    assertEquals(0, mappedSortBy.entrySet().size());

    sortBy = new HashMap<>();
    sortBy.put("key", SortMeta.builder().field("field").order(SortOrder.ASCENDING).build());
    mappedSortBy = model.mapSortBy(sortBy);
    assertNotNull(mappedSortBy);
    assertEquals(1, mappedSortBy.entrySet().size());
    assertTrue(mappedSortBy.entrySet().stream()
        .anyMatch(entry -> "key".equals(entry.getKey()) && Boolean.TRUE == entry.getValue()));
  }

  HspLazyDataModel<Object> createHspLazyDataModel() {
    return new HspLazyDataModel<>() {

      private static final long serialVersionUID = 5094442585478509787L;

      @Override
      public List<Object> loadMapped(int first, int pageSize, Map<String, Boolean> sortBy,
          Map<String, String> filterBy) {
        return Collections.emptyList();
      }
    };
  }


}
