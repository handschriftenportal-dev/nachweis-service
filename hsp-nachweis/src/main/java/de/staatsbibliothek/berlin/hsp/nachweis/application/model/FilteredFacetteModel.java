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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 06.12.2021
 */

@Data
@Slf4j
public class FilteredFacetteModel implements Serializable {

  public static final int FILTER_LIMIT = 5;
  private static final long serialVersionUID = 2812617912768485856L;
  private List<Facette> facetten;
  private List<Facette> filteredFacetten;
  private String query;
  private boolean expanded;

  public FilteredFacetteModel() {
    this.facetten = new ArrayList<>();
    matchQuery();
  }

  public void toggleExpanded() {
    this.expanded = !this.expanded;
    matchQuery();
  }

  public void matchQuery() {
    String lcQuery = Objects.isNull(query) || query.isBlank() ? null : query.toLowerCase(Locale.ROOT);
    this.filteredFacetten = this.facetten.stream()
        .filter(f -> Objects.isNull(lcQuery)
            || (Objects.nonNull(f.getValue()) && f.getValue().toLowerCase(Locale.ROOT).contains(lcQuery)))
        .limit(expanded || Objects.nonNull(lcQuery) ? Integer.MAX_VALUE : FILTER_LIMIT)
        .collect(Collectors.toList());
  }

  public void updateFacetten(Map<String, Integer> fMap) {
    if (Objects.isNull(fMap)) {
      this.facetten = new ArrayList<>();
    } else {
      this.facetten = fMap.entrySet().stream()
          .map(e -> new Facette(e.getKey(), Objects.isNull(e.getValue()) ? 0 : e.getValue()))
          .collect(Collectors.toList());
    }
    matchQuery();
  }

  public void clearQuery() {
    this.query = null;
    matchQuery();
  }

  public boolean showExpandButton() {
    return !expanded && (Objects.isNull(query) || query.isBlank())
        && this.facetten.size() > FILTER_LIMIT;
  }

  public boolean showCollapseButton() {
    return expanded && (Objects.isNull(query) || query.isBlank())
        && this.facetten.size() > FILTER_LIMIT;
  }

}
