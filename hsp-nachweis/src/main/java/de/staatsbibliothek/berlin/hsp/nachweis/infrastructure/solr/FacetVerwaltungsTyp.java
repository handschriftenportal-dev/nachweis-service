package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Ergebnis;
import org.apache.solr.client.solrj.response.json.BucketBasedJsonFacet;
import org.apache.solr.client.solrj.response.json.BucketJsonFacet;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 16.11.2021.
 * @version 1.0
 */
public class FacetVerwaltungsTyp implements Facet {

  public static final String VERWALTUNGS_TYP_FACET_NAME = "verwaltungsTyp";

  @Override
  public void handleResult(Ergebnis result, BucketBasedJsonFacet bucketBasedFacets) {
    for (BucketJsonFacet bucket : bucketBasedFacets.getBuckets()) {
      switch ((String) bucket.getVal()) {
        case "intern":
          result.setFacetCountVerwaltungIntern((int) bucket.getCount());
          break;
        case "extern":
          result.setFacetCountVerwaltungExtern((int) bucket.getCount());
          break;
      }
    }
  }
}
