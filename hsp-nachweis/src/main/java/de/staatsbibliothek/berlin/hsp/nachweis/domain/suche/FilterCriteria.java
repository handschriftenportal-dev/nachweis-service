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

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrIndexFields;
import java.text.MessageFormat;

public enum FilterCriteria {

  CONTAINS_DIGITALISAT(SolrIndexFields.FIELD_CONTAINS_DIGITALISAT + ":{0}"),

  CONTAINS_BUCHSCHMUCK(SolrIndexFields.FIELD_CONTAINS_BUCHSCHMUCK + ":{0}"),

  CONTAINS_BESCHREIBUNG(SolrIndexFields.FIELD_CONTAINS_BESCHREIBUNG + ":{0}"),

  TYP(SolrIndexFields.FIELD_TYP + ":{0}"),

  PUBLIZIERT(SolrIndexFields.FIELD_PUBLIZIERT + ":{0}"),

  BESTANDSHALTENDE_INSTITUTION(SolrIndexFields.FIELD_BESTANDHALTENDE_INSTITUTION_NAME_FACETTE + ":{0}"),

  VERWALTUNGS_TYP(SolrIndexFields.FIELD_VERWALTUNGS_TYP + ":{0}"),

  AUTOR(SolrIndexFields.FIELD_AUTOREN + ":{0}"),

  LAST_UPDATE_FROM(SolrIndexFields.FIELD_LAST_UPDATE + ":[{0} TO *]"),

  LAST_UPDATE_TO(SolrIndexFields.FIELD_LAST_UPDATE + ":[* TO {0}]");

  private final MessageFormat filterFormatter;

  FilterCriteria(String filterTemplate) {
    filterFormatter = new MessageFormat(filterTemplate);
  }

  public String createFilter(Object... filterValue) {
    return filterFormatter.format(filterValue);
  }

}
