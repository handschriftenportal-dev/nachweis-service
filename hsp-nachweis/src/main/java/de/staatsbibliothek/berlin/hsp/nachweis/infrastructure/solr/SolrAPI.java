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

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 18.08.2020.
 * @version 1.0
 */
public interface SolrAPI {

  String QUERY_WILDCARD = "*";

  String HIGHLIGHTING_METHOD_FASTVECTOR = "fastVector";

  String HIGHLIGHTING_ACTIVE_KEY = "hl";

  String HIGHLIGHTING_ACTIVE_VALUE = "on";

  String HIGHLIGHTING_AND_OPERATOR = " AND ";

  String HIGHLIGHTING_FIELDS_KEY = "hl.fl";

  String HIGHLIGHTING_QUERY_KEY = "hl.q";

  String HIGHLIGHTING_METHOD_KEY = "hl.method";

  String HIGHLIGHTING_ENCODING_KEY = "hl.encoder";

  String HIGHLIGHTING_ENCODING_VALUE = "html";

  String SEARCH_QUERY_OPERATOR = "q.op";

  String SEARCH_OPERATOR_AND = "AND";

  String SEARCH_DEFAULT_FIELD = "df";

  String FIELD_SCORE = "score";

}
