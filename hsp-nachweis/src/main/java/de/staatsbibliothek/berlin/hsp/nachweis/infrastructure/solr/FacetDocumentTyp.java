package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Ergebnis;
import org.apache.solr.client.solrj.response.json.BucketBasedJsonFacet;
import org.apache.solr.client.solrj.response.json.BucketJsonFacet;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 16.11.2021.
 * @version 1.0
 */
public class FacetDocumentTyp implements Facet {

  public static final String TYP_FACET_NAME = "typ";

  @Override
  public void handleResult(Ergebnis result, BucketBasedJsonFacet bucketBasedFacets) {
    for (BucketJsonFacet bucket : bucketBasedFacets.getBuckets()) {
      switch ((String) bucket.getVal()) {
        case "BS":
          result.setFacetCountTypeBeschreibung((int) bucket.getCount());
          break;
        case "KOD":
          result.setFacetCountTypeKOD((int) bucket.getCount());
          break;
      }
    }
  }
}
