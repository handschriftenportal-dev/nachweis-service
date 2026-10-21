package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Ergebnis;
import org.apache.solr.client.solrj.response.json.BucketBasedJsonFacet;
import org.apache.solr.client.solrj.response.json.BucketJsonFacet;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.03.24.
 * @version 1.0
 */
public class FacetBearbeiter implements Facet {

  public static final String BEARBEITER_FACET_NAME = "bearbeiter_ss";

  @Override
  public void handleResult(Ergebnis result, BucketBasedJsonFacet bucketBasedFacets) {

    if (bucketBasedFacets != null && bucketBasedFacets.getBuckets() != null) {
      for (BucketJsonFacet bucket : bucketBasedFacets.getBuckets()) {
        result.getBearbeiter().put((String) bucket.getVal(), (int) bucket.getCount());
      }
    }
  }
}
