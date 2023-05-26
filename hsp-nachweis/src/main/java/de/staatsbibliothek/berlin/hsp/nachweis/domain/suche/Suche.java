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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrAPI.HIGHLIGHTING_METHOD_FASTVECTOR;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrAPI.QUERY_WILDCARD;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrDateService;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrIndexFields;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 14.02.2020.
 */
@Data
public class Suche implements Serializable {

  private static final long serialVersionUID = 616768906809033730L;

  private String query;
  private Page page;
  private Sort sort;
  private Highlighting highlighting;

  @Setter(AccessLevel.NONE)
  private Map<FilterCriteria, String> filter;

  private Suche(SucheBuilder sucheBuilder) {
    this.query = sucheBuilder.query;
    this.page = sucheBuilder.page;
    this.sort = sucheBuilder.sort;
    this.highlighting = sucheBuilder.highlighting;
    this.filter = sucheBuilder.filter;
  }

  public static SucheBuilder builder() {
    return new SucheBuilder();
  }

  public static class SucheBuilder {

    private final Map<FilterCriteria, String> filter = new EnumMap<>(FilterCriteria.class);
    private String query = QUERY_WILDCARD;
    private Page page;
    private Sort sort;
    private Highlighting highlighting;
    private SuchDokumentTyp suchDokumentTyp;

    public SucheBuilder withQuery(String query) {
      if (Objects.nonNull(query) && !query.isEmpty()) {
        this.query = query;
      }
      return this;
    }

    public SucheBuilder withPage(Page page) {
      this.page = page;
      return this;
    }

    public SucheBuilder withSort(Sort sort) {
      this.sort = sort;
      return this;
    }

    public SucheBuilder withHighlighting(boolean active) {
      if (active) {
        this.highlighting = new Highlighting(true, HIGHLIGHTING_METHOD_FASTVECTOR,
            Collections.singletonList(SolrIndexFields.FIELD_SEARCHABLE_VALUES));
      } else {
        this.highlighting = null;
      }
      return this;
    }

    public SucheBuilder withFilterSuchDokumentTyp(SuchDokumentTyp suchDokumentTyp) {
      this.suchDokumentTyp = suchDokumentTyp;

      if (Objects.nonNull(suchDokumentTyp)) {
        filter.put(FilterCriteria.TYP, suchDokumentTyp.toString());
      } else {
        filter.remove(FilterCriteria.TYP);
      }
      return this;
    }

    public SucheBuilder withFilterContainsBuchschmuck(boolean containsBuchschmuck) {
      if (containsBuchschmuck) {
        filter.put(FilterCriteria.CONTAINS_BUCHSCHMUCK, Boolean.TRUE.toString());
      } else {
        filter.remove(FilterCriteria.CONTAINS_BUCHSCHMUCK);
      }
      return this;
    }

    public SucheBuilder withFilterContainsDigitalisat(boolean containsDigitalisat) {
      if (containsDigitalisat) {
        filter.put(FilterCriteria.CONTAINS_DIGITALISAT, Boolean.TRUE.toString());
      } else {
        filter.remove(FilterCriteria.CONTAINS_DIGITALISAT);
      }
      return this;
    }

    public SucheBuilder withFilterContainsBeschreibung(boolean containsBeschreibung) {
      if (containsBeschreibung) {
        filter.put(FilterCriteria.CONTAINS_BESCHREIBUNG, Boolean.TRUE.toString());
      } else {
        filter.remove(FilterCriteria.CONTAINS_BESCHREIBUNG);
      }
      return this;
    }

    public SucheBuilder withFilterVerwaltungsTyp(VerwaltungsTyp verwaltungsTyp) {
      if (Objects.nonNull(verwaltungsTyp)) {
        filter.put(FilterCriteria.VERWALTUNGS_TYP, verwaltungsTyp.toString());
      } else {
        filter.remove(FilterCriteria.VERWALTUNGS_TYP);
      }
      return this;
    }

    public SucheBuilder withFilterBestandshaltendeInstitutionen(String... institutionen) {
      if (Objects.nonNull(institutionen) && institutionen.length > 0) {
        filter.put(FilterCriteria.BESTANDSHALTENDE_INSTITUTION, joinFilterValues(institutionen));
      } else {
        filter.remove(FilterCriteria.BESTANDSHALTENDE_INSTITUTION);
      }
      return this;
    }

    public SucheBuilder withFilterAutoren(String... autoren) {
      if (Objects.nonNull(autoren) && autoren.length > 0) {
        filter.put(FilterCriteria.AUTOR, joinFilterValues(autoren));
      } else {
        filter.remove(FilterCriteria.AUTOR);
      }
      return this;
    }

    public SucheBuilder withFilterLastUpdateFrom(LocalDate lastUpdateFrom) {
      if (Objects.nonNull(lastUpdateFrom)) {
        filter.put(FilterCriteria.LAST_UPDATE_FROM,
            SolrDateService.formatLocalDateTime(lastUpdateFrom.atTime(LocalTime.MIN)));
      } else {
        filter.remove(FilterCriteria.LAST_UPDATE_FROM);
      }
      return this;
    }

    public SucheBuilder withFilterLastUpdateTo(LocalDate lastUpdateTo) {
      if (Objects.nonNull(lastUpdateTo)) {
        filter.put(FilterCriteria.LAST_UPDATE_TO,
            SolrDateService.formatLocalDateTime(lastUpdateTo.atTime(LocalTime.MAX)));
      } else {
        filter.remove(FilterCriteria.LAST_UPDATE_TO);
      }
      return this;
    }

    public SucheBuilder withFilterPubliziert(boolean publiziert) {
      if (publiziert) {
        filter.put(FilterCriteria.PUBLIZIERT, Boolean.TRUE.toString());
      } else {
        filter.remove(FilterCriteria.PUBLIZIERT);
      }
      return this;
    }

    public Suche build() {
      if (SuchDokumentTyp.KOD == suchDokumentTyp) {
        filter.remove(FilterCriteria.PUBLIZIERT);
        filter.remove(FilterCriteria.VERWALTUNGS_TYP);
        filter.remove(FilterCriteria.AUTOR);
      } else if (SuchDokumentTyp.BS == suchDokumentTyp) {
        filter.remove(FilterCriteria.CONTAINS_BESCHREIBUNG);
        filter.remove(FilterCriteria.CONTAINS_DIGITALISAT);
        filter.remove(FilterCriteria.CONTAINS_BUCHSCHMUCK);
        filter.remove(FilterCriteria.BESTANDSHALTENDE_INSTITUTION);
      }

      return new Suche(this);
    }

    private String joinFilterValues(String... values) {
      return Arrays.stream(values).collect(Collectors.joining("\" AND \"", "(\"", "\")"));
    }
  }
}
