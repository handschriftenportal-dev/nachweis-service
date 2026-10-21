package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Ergebnis;
import org.apache.solr.client.solrj.response.json.BucketBasedJsonFacet;
import org.apache.solr.client.solrj.response.json.BucketJsonFacet;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 16.11.2021.
 * @version 1.0
 */
public class FacetDigitalisat implements Facet {

  public static final String CONTAINS_DIGITALISAT_FACET_NAME = "containsDigitalisat";

  @Override
  public void handleResult(Ergebnis result, BucketBasedJsonFacet bucketBasedFacets) {
    for (BucketJsonFacet bucket : bucketBasedFacets.getBuckets()) {
      if ((Boolean) bucket.getVal()) {
        result.setFacetCountContainsDigitalisat((int) bucket.getCount());
      }
    }
  }
}
