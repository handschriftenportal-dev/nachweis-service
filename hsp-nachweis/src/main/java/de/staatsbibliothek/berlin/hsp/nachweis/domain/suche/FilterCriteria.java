package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrIndexFields;
import java.text.MessageFormat;

public enum FilterCriteria {

  CONTAINS_DIGITALISAT(SolrIndexFields.FIELD_CONTAINS_DIGITALISAT + ":{0}"),

  CONTAINS_BUCHSCHMUCK(SolrIndexFields.FIELD_CONTAINS_BUCHSCHMUCK + ":{0}"),

  CONTAINS_BESCHREIBUNG(SolrIndexFields.FIELD_CONTAINS_BESCHREIBUNG + ":{0}"),

  TYP(SolrIndexFields.FIELD_TYP + ":{0}"),

  PUBLIZIERT(SolrIndexFields.FIELD_PUBLIZIERT + ":{0}"),

  BESTANDSHALTENDE_INSTITUTION(
      SolrIndexFields.FIELD_BESTANDHALTENDE_INSTITUTION_NAME_FACETTE + ":{0}"),

  VERWALTUNGS_TYP(SolrIndexFields.FIELD_VERWALTUNGS_TYP + ":{0}"),

  AUTOR(SolrIndexFields.FIELD_AUTOREN + ":{0}"),

  BEARBEITER(SolrIndexFields.FIELD_BEARBEITER + ":{0}"),

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
