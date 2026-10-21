package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Ergebnis;
import org.apache.solr.client.solrj.response.json.BucketBasedJsonFacet;
import org.apache.solr.client.solrj.response.json.BucketJsonFacet;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 20.01.23
 */
public class FacetBeschreibung implements Facet {

  public static final String CONTAINS_BESCHREIBUNG_FACET_NAME = "containsBeschreibung";

  @Override
  public void handleResult(Ergebnis result, BucketBasedJsonFacet bucketBasedFacets) {
    for (BucketJsonFacet bucket : bucketBasedFacets.getBuckets()) {
      if ((Boolean) bucket.getVal()) {
        result.setFacetCountContainsBeschreibung((int) bucket.getCount());
      }
    }
  }
}
