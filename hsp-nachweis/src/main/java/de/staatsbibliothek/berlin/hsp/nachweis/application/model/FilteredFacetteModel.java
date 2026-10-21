package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import java.io.Serial;
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
  @Serial
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
