package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Ergebnis;
import java.util.List;
import java.util.Map;
import org.apache.solr.client.solrj.response.json.BucketBasedJsonFacet;
import org.apache.solr.client.solrj.response.json.BucketJsonFacet;
import org.apache.solr.common.util.NamedList;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.03.24.
 * @version 1.0
 */
public class FacetBearbeiterTest {

  @Test
  void testHandleResult() {

    FacetBearbeiter facetBearbeiter = new FacetBearbeiter();
    Ergebnis ergebnis = new Ergebnis();
    BucketJsonFacet bucket = new BucketJsonFacet(
        new NamedList<>(Map.of("val", "Eichstaedt, Konrad")));
    BucketBasedJsonFacet bucketBasedJsonFacet = Mockito.mock(BucketBasedJsonFacet.class);
    Mockito.when(bucketBasedJsonFacet.getBuckets()).thenReturn(List.of(bucket));

    assertNotNull(facetBearbeiter);

    facetBearbeiter.handleResult(ergebnis, bucketBasedJsonFacet);

    assertNotNull(ergebnis);

    assertNotNull(ergebnis.getBearbeiter());

    assertEquals(1, ergebnis.getBearbeiter().size());
  }
}
