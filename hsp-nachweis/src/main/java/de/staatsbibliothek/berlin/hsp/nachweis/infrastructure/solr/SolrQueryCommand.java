package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 18.02.2020.
 */
@Data
@JsonInclude(Include.NON_NULL)
public class SolrQueryCommand {

  private String query;

  private Integer limit = null;
  private Integer offset = null;

  private String sort;

  private List<String> filter = null;

  private Map<String, FacetTerm> facet = null;

  private Map<String, String> params = null;

  public SolrQueryCommand(final String query) {
    this.query = query;
  }

  public void addFilter(String filterEntry) {
    if (Objects.isNull(filter)) {
      filter = new ArrayList<>();
    }

    this.filter.add(filterEntry);
  }

  public void addFacet(final String name, final String field, final int limit, final int mincount) {
    if (Objects.isNull(facet)) {
      facet = new HashMap<>();
    }

    facet.put(name, new FacetTerm("terms", field, limit, mincount));
  }

  public void addParam(String key, String value) {
    if (Objects.isNull(params)) {
      params = new HashMap<>();
    }

    params.put(key, value);
  }

  @AllArgsConstructor
  @Data
  public static class FacetTerm {

    private String type;
    private String field;
    private int limit;
    private int mincount;

  }

}

