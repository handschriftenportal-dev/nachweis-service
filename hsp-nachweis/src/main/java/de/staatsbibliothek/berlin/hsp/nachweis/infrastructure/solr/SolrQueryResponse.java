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

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 18.02.2020.
 * <p>
 * SOLR JSON Response
 */
@Data
public class SolrQueryResponse<T> {

  private ResponseHeader responseHeader;
  private Response<T> response;
  private Facets facets;
  private Highlighting highlighting;

  @Data
  @JsonIgnoreProperties(value = {"QTime", "params"})
  static class ResponseHeader {

    private int status;

  }

  @Data
  static class Highlighting {

    private Map<String, Map<String, List<String>>> hits = new HashMap<>();
    private Map<String, List<String>> hitValue = new HashMap<>();

    @JsonAnySetter
    public void set(final String name, Object value) {

      hitValue = (Map<String, List<String>>) value;

      hits.put(name, hitValue);
    }
  }

  @Data
  static class Response<T> {

    private int numFound;
    private int start;
    private boolean numFoundExact;
    private List<T> docs;

  }

  @Data
  static class Facets {

    private int count;
    private Term typ;
    private Term status;
    private Term institutionen;
    private Term containsDigitalisat;
    
  }

  @Data
  static class Term {

    private List<Bucket> buckets = new ArrayList<>();

  }

  @Data
  static class Bucket {

    private String val;
    private int count;

  }

}
