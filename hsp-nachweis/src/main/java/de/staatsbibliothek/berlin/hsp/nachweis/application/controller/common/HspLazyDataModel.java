package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 24.11.22
 */
public abstract class HspLazyDataModel<T> extends LazyDataModel<T> {

  @Serial
  private static final long serialVersionUID = -8360737263274736018L;

  private static String toFilterString(Object filterValue) {
    return switch (filterValue) {
      case null -> null;
      case Collection<?> collection -> collection.stream()
          .filter(Objects::nonNull)
          .map(String::valueOf)
          .collect(Collectors.joining(","));
      case Object[] array -> Stream.of(array)
          .filter(Objects::nonNull)
          .map(String::valueOf)
          .collect(Collectors.joining(","));
      default -> {
        String stringValue = String.valueOf(filterValue);
        if (stringValue.startsWith("[") && stringValue.endsWith("]")) {
          yield stringValue.substring(1, stringValue.length() - 1);
        }
        yield stringValue;
      }
    };

  }

  public abstract List<T> loadMapped(int firstEntry, int pageSize, Map<String, Boolean> sortByAsc,
      Map<String, String> filterBy);

  @Override
  public List<T> load(int firstEntry, int pageSize, Map<String, SortMeta> sortBy,
      Map<String, FilterMeta> filterBy) {
    Map<String, Boolean> sortByAsc = mapSortBy(sortBy);
    Map<String, String> filterByString = mapFilterBy(filterBy);

    List<T> results = loadMapped(firstEntry, pageSize, sortByAsc, filterByString);

    setRowCount(count(filterBy));

    return results;
  }

  protected Map<String, String> mapFilterBy(Map<String, FilterMeta> filterBy) {
    return Optional.ofNullable(filterBy)
        .orElse(Collections.emptyMap())
        .entrySet()
        .stream()
        .filter(entry -> Objects.nonNull(entry.getKey()) && Objects.nonNull(entry.getValue()))
        .filter(entry -> Objects.nonNull(entry.getValue().getFilterValue()))
        .collect(Collectors.toMap(Entry::getKey,
            entry -> toFilterString(entry.getValue().getFilterValue())));
  }

  Map<String, Boolean> mapSortBy(Map<String, SortMeta> sortBy) {
    return Optional.ofNullable(sortBy)
        .orElse(Collections.emptyMap())
        .entrySet()
        .stream()
        .filter(entry -> Objects.nonNull(entry.getKey())
            && Objects.nonNull(entry.getValue())
            && Objects.nonNull(entry.getValue().getOrder()))
        .collect(
            Collectors.toMap(Entry::getKey, entry -> entry.getValue().getOrder().isAscending()));
  }
}
