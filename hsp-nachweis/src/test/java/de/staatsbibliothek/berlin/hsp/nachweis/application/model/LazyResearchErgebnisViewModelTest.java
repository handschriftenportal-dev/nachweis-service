package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Ergebnis;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.ErgebnisEintrag;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Suche;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrServiceException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 04.03.2020.
 *
 * @see LazyResearchErgebnisViewModel
 */
class LazyResearchErgebnisViewModelTest {

  @Test
  void testSearch() throws SolrServiceException {
    final SuchDokumentBoundary suchDokumentService = Mockito.mock(SuchDokumentService.class);
    final LazyResearchErgebnisViewModel testling = new LazyResearchErgebnisViewModel(suchDokumentService);

    final Suche suche = Suche.builder().build();
    final Ergebnis ergebnis = new Ergebnis();
    ergebnis.setEintragListe(new ArrayList<>());
    Mockito.when(suchDokumentService.search(suche)).thenReturn(ergebnis);

    final List<ErgebnisEintrag> ergebnisEintragList = testling.search(suche);

    Assertions.assertEquals(ergebnis.getEintragListe(), ergebnisEintragList);
  }

  @Test
  void testLoad() throws SolrServiceException {
    final SuchDokumentBoundary suchDokumentService = Mockito.mock(SuchDokumentService.class);
    final LazyResearchErgebnisViewModel testling = new LazyResearchErgebnisViewModel(suchDokumentService);

    final Suche suche = Suche.builder().build();
    final Ergebnis ergebnis = new Ergebnis();
    ergebnis.setEintragListe(new ArrayList<>());
    Mockito.when(suchDokumentService.search(suche)).thenReturn(ergebnis);

    testling.suche = suche;

    final List<ErgebnisEintrag> ergebnisEintragList = testling.load(11, 25, null, null,
        null);

    Assertions.assertEquals(ergebnis.getEintragListe(), ergebnisEintragList);
    Assertions.assertEquals(25, testling.suche.getPage().getNumberOfRows());
    Assertions.assertEquals(11, testling.suche.getPage().getOffset());
  }

  @Test
  void testGetRowData() {
    final SuchDokumentBoundary suchDokumentService = Mockito.mock(SuchDokumentService.class);
    final LazyResearchErgebnisViewModel testling = new LazyResearchErgebnisViewModel(suchDokumentService);

    final ErgebnisEintrag eintrag = new ErgebnisEintrag();
    eintrag.setId("SomeID");
    final Ergebnis ergebnis = new Ergebnis();
    ergebnis.setEintragListe(Collections.singletonList(eintrag));
    testling.ergebnis = ergebnis;

    final ErgebnisEintrag result = testling.getRowData("SomeID");

    Assertions.assertEquals(eintrag, result);
  }

  @Test
  void testGetRowKey() {
    final SuchDokumentBoundary suchDokumentService = Mockito.mock(SuchDokumentService.class);
    final LazyResearchErgebnisViewModel testling = new LazyResearchErgebnisViewModel(suchDokumentService);

    final ErgebnisEintrag eintrag = new ErgebnisEintrag();
    eintrag.setId("SomeID");
    final Ergebnis ergebnis = new Ergebnis();
    ergebnis.setEintragListe(Collections.singletonList(eintrag));
    testling.ergebnis = ergebnis;

    final Object id = testling.getRowKey(eintrag);

    Assertions.assertEquals("SomeID", id);
  }

}
