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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 24.11.22
 */
public abstract class HspLazyDataModel<T> extends LazyDataModel<T> {

  private static final long serialVersionUID = -8360737263274736018L;

  public abstract List<T> loadMapped(int first, int pageSize, Map<String, Boolean> sortByAsc,
      Map<String, String> filterBy);

  public List<T> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
    return loadMapped(first, pageSize, mapSortBy(sortBy), mapFilterBy(filterBy));
  }

  Map<String, String> mapFilterBy(Map<String, FilterMeta> filterBy) {
    return Optional.ofNullable(filterBy)
        .orElse(Collections.emptyMap())
        .entrySet()
        .stream()
        .filter(entry -> Objects.nonNull(entry.getKey()) && Objects.nonNull(entry.getValue()))
        .collect(Collectors.toMap(Entry::getKey,
            entry -> String.valueOf(entry.getValue().getFilterValue())));
  }

  Map<String, Boolean> mapSortBy(Map<String, SortMeta> sortBy) {
    return Optional.ofNullable(sortBy)
        .orElse(Collections.emptyMap())
        .entrySet()
        .stream()
        .filter(entry -> Objects.nonNull(entry.getKey())
            && Objects.nonNull(entry.getValue())
            && Objects.nonNull(entry.getValue().getOrder()))
        .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getOrder().isAscending()));
  }

}
