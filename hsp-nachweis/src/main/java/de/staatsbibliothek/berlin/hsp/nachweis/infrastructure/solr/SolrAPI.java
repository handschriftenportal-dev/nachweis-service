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
