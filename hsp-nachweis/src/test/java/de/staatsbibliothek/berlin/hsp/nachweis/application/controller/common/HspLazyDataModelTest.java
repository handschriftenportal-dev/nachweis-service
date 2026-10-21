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

      @Override
      public int count(Map<String, FilterMeta> map) {
        return 0;
      }

      private static final long serialVersionUID = 5094442585478509787L;

      @Override
      public List<Object> loadMapped(int firstEntry, int pageSize, Map<String, Boolean> sortBy,
          Map<String, String> filterBy) {
        return Collections.emptyList();
      }
    };
  }


}
