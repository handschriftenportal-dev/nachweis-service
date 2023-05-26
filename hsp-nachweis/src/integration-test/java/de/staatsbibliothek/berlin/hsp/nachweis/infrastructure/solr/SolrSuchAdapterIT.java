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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel.SucheModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Ergebnis;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.ErgebnisEintrag;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Suche;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 06.11.2020.
 * @version 1.0
 */

@QuarkusTest
@TestTransaction
class SolrSuchAdapterIT {


  @Inject
  SolrSuchAdapter solrSuchAdapter;

  @Inject
  SuchDokumentBoundary suchDokumentBoundary;

  private final SucheModel sucheModel = new SucheModel(suchDokumentBoundary);

  @Test
  void testCreation() {

    assertNotNull(solrSuchAdapter);
  }

  @Disabled("Failed always on master must be replaced with Docker Solr!")
  @RepeatedTest(10)
  void searchTest() throws Exception {
    String id = UUID.randomUUID().toString();
    final SuchDokument test = SuchDokument.builder()
        .withId(id)
        .withTitel("some title ")
        .withSuchDokumentTyp(SuchDokumentTyp.BS)
        .withSearchableValues("irgendwas#und#nix#" + id)
        .withLastUpdate(LocalDateTime.now())
        .build();

    updateInSOLR(test);

    sucheModel.init();
    sucheModel.setQuery(id);
    sucheModel.search();
    assertNotNull(sucheModel.getSuche());

    Suche suche = sucheModel.getSuche();
    assertNotNull(suche);

    Ergebnis ergebnis = solrSuchAdapter.search(suche);
    assertNotNull(ergebnis);
    assertNotNull(ergebnis.getEintragListe());
    assertFalse(ergebnis.getEintragListe().isEmpty());

    deleteFromSOLR(test.getId());

    ergebnis = solrSuchAdapter.search(suche);
    assertNotNull(ergebnis);
    assertNotNull(ergebnis.getEintragListe());
    assertTrue(ergebnis.getEintragListe().isEmpty());
  }

  @Transactional(TxType.REQUIRES_NEW)
  void updateInSOLR(SuchDokument... suchDokuments) throws Exception {
    solrSuchAdapter.update(suchDokuments);
  }

  @Transactional(TxType.REQUIRES_NEW)
  void deleteFromSOLR(String... ids) throws Exception {
    solrSuchAdapter.delete(ids);
  }

  @Transactional(TxType.REQUIRES_NEW)
  void deleteAll() throws Exception {
    solrSuchAdapter.deleteAll();
  }

  @RepeatedTest(10)
  void testTransactionRollback() throws SolrServiceException {
    String id = UUID.randomUUID().toString();
    final SuchDokument test = SuchDokument.builder()
        .withId(id)
        .withTitel("some title" + id)
        .withSuchDokumentTyp(SuchDokumentTyp.BS)
        .withSearchableValues("irgendwas#und#nix#" + id)
        .withLastUpdate(LocalDateTime.now())
        .build();
    final SuchDokument testWrong = SuchDokument.builder()
        .withId(null)
        .withTitel("some title2")
        .withSuchDokumentTyp(SuchDokumentTyp.BS)
        .withSearchableValues("irgendwas#und#nix2")
        .withLastUpdate(LocalDateTime.now())
        .build();

    sucheModel.init();
    sucheModel.setQuery(id);
    sucheModel.buildNewSuche();

    Suche suche = sucheModel.getSuche();
    assertNotNull(suche);

    Ergebnis ergebnis = solrSuchAdapter.search(suche);
    assertNotNull(ergebnis);
    assertNotNull(ergebnis.getEintragListe());
    assertTrue(ergebnis.getEintragListe().isEmpty());

    assertThrows(Exception.class, () -> updateInSOLR(test, testWrong));

    ergebnis = solrSuchAdapter.search(suche);
    assertNotNull(ergebnis);
    assertNotNull(ergebnis.getEintragListe());
    assertTrue(ergebnis.getEintragListe().isEmpty());
  }

  @RepeatedTest(10)
  void testDeleteBeforeUpdate() throws Exception {
    List<SuchDokument> testSuchDokuments = new ArrayList<>();
    String testSucheValue = prepareTestSuchDocuments(testSuchDokuments);

    String[] ids = new String[4];
    for (int i = 0; i < 4; i++) {
      ids[i] = testSuchDokuments.get(i).getId();
    }

    assertEquals(4, testSuchDokuments.size());

    updateInSOLR(testSuchDokuments.toArray(new SuchDokument[0]));

    sucheModel.init();
    sucheModel.setQuery(testSucheValue);
    sucheModel.buildNewSuche();

    Suche suche = sucheModel.getSuche();
    assertNotNull(suche);

    Ergebnis ergebnis = solrSuchAdapter.search(suche);
    assertNotNull(ergebnis);
    List<ErgebnisEintrag> eintragListe = ergebnis.getEintragListe();
    assertNotNull(eintragListe);
    assertEquals(4, eintragListe.size());

    testSuchDokuments.clear();
    for (ErgebnisEintrag eintrag : eintragListe) {
      testSuchDokuments.add(
          SuchDokument.builder()
              .withId(eintrag.getId())
              .withTitel("some title")
              .withSuchDokumentTyp(SuchDokumentTyp.BS)
              .withSearchableValues(testSucheValue)
              .build()
      );
    }

    deleteFromSOLR(ids);

    ergebnis = solrSuchAdapter.search(suche);
    assertNotNull(ergebnis);
    eintragListe = ergebnis.getEintragListe();
    assertNotNull(eintragListe);
    assertTrue(eintragListe.isEmpty());

  }

  @RepeatedTest(10)
  void testDeleteAll() throws Exception {
    List<SuchDokument> testSuchDokuments = new ArrayList<>();
    String testSucheValue = prepareTestSuchDocuments(testSuchDokuments);
    updateInSOLR(testSuchDokuments.toArray(new SuchDokument[0]));

    sucheModel.init();
    sucheModel.setQuery(testSucheValue);
    sucheModel.buildNewSuche();

    Suche suche = sucheModel.getSuche();
    assertNotNull(suche);

    Ergebnis ergebnis = solrSuchAdapter.search(suche);
    assertNotNull(ergebnis);
    List<ErgebnisEintrag> eintragListe = ergebnis.getEintragListe();
    assertNotNull(eintragListe);
    assertEquals(4, eintragListe.size());
    deleteAll();

    ergebnis = solrSuchAdapter.search(suche);
    assertNotNull(ergebnis);
    eintragListe = ergebnis.getEintragListe();
    assertNotNull(eintragListe);
    assertTrue(eintragListe.isEmpty());
  }

  private String prepareTestSuchDocuments(List<SuchDokument> testSuchDokuments) {
    String id = UUID.randomUUID().toString();
    String testSucheValue = "piotr" + id;

    for (int i = 0; i < 4; i++) {
      String currentId = "JUNIT" + id + " Test" + i;
      testSuchDokuments.add(
          SuchDokument.builder()
              .withId(currentId)
              .withTitel("some title")
              .withSuchDokumentTyp(SuchDokumentTyp.BS)
              .withSearchableValues(testSucheValue)
              .build()
      );
    }
    return testSucheValue;
  }
}
