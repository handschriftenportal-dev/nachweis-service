package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 18.08.2020.
 * @version 1.0
 */
class SolrQueryCommandTest {

  @Test
  void addParameterforHighlighting() {

    SolrQueryCommand command = new SolrQueryCommand("*");
    command.addParam(SolrAPI.HIGHLIGHTING_ACTIVE_KEY, SolrAPI.HIGHLIGHTING_ACTIVE_VALUE);

    Assertions.assertTrue(command.getParams().containsKey(SolrAPI.HIGHLIGHTING_ACTIVE_KEY));
  }
}
