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

