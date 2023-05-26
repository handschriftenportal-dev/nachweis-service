/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
