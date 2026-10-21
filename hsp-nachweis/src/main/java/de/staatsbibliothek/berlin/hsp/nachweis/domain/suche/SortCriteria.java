package de.staatsbibliothek.berlin.hsp.nachweis.domain.suche;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrAPI;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrIndexFields;
import lombok.Getter;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 26.02.2020.
 */
@Getter
public enum SortCriteria {

  RELEVANZ(SolrAPI.FIELD_SCORE),
  SIGNATUR(SolrIndexFields.FIELD_SIGNATUR);

  private final String sortField;

  SortCriteria(String sortField) {
    this.sortField = sortField;
  }

}
