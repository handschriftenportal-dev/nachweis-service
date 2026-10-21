package de.staatsbibliothek.berlin.hsp.nachweis.domain.werkzeug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 *
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 30.09.25.
 * @version 1.0
 */

class WerkzeugBatchUpdateAllKODTEITest {

  @Test
  void testexecuteBatchJob() throws Exception {
    KulturObjektDokumentBoundary kulturObjektDokumentBoundary = Mockito.mock(KulturObjektDokumentBoundary.class);
    BearbeiterBoundary bearbeiterBoundary = Mockito.mock(BearbeiterBoundary.class);
    WerkzeugBatchUpdateAllKODTEI werkzeugBatchUpdateAllKODTEIT = new WerkzeugBatchUpdateAllKODTEI(
        kulturObjektDokumentBoundary, bearbeiterBoundary);
    Mockito.when(kulturObjektDokumentBoundary.findAll())
        .thenReturn(List.of(new KulturObjektDokument.KulturObjektDokumentBuilder()
            .withId("HSP-123").withBeschreibungsdokumentIDs(Set.of("HSP-BS1")).build()));

    assertNotNull(werkzeugBatchUpdateAllKODTEIT);

    assertEquals(0, werkzeugBatchUpdateAllKODTEIT.getCounter());

    werkzeugBatchUpdateAllKODTEIT.execute();

    assertEquals(1, werkzeugBatchUpdateAllKODTEIT.getCounter());
    Mockito.verify(kulturObjektDokumentBoundary, Mockito.times(1)).updateKODXML("HSP-123");
  }
}
