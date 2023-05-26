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

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrAPI.QUERY_WILDCARD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Ergebnis;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.ErgebnisEintrag;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Page;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Sort;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SortCriteria;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SortType;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Suche;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.jta.MockedTransactionManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.response.json.NestableJsonFacet;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.mockito.Mockito;

/**
 * Created by udo.boysen@sbb.spk-berlin.de on 12.02.2020.
 *
 * @see SolrSuchAdapter
 */
@Slf4j
// Each Test uses same transactionId:
@Execution(SAME_THREAD)
class SolrSuchAdapterTest extends MockedTransactionManager {

  SolrSuchAdapter testling;
  HttpSolrClient masterClient;

  @BeforeEach
  void setup() {
    testling = mock(SolrSuchAdapter.class, CALLS_REAL_METHODS);
    masterClient = mock(HttpSolrClient.class);
    testling.masterClient = masterClient;
    testling.transactionManager = transactionManagerMOCK;
    testling.core = "testCore";
    testling.masterURL = "http://localhost:8983/solr";
  }

  @Test
  void testUpdate() throws Exception {
    final SuchDokument test = SuchDokument.builder()
        .withId("JUNIT Test")
        .withTitel("some title")
        .withSuchDokumentTyp(SuchDokumentTyp.BS)
        .withSearchableValues("irgendwas#und#nix")
        .withLastUpdate(LocalDateTime.now())
        .build();

    final UpdateResponse response = mock(UpdateResponse.class);
    when(masterClient.add(anyString(), anyList())).thenReturn(response);
    when(response.getStatus()).thenReturn(1);

    testling.update(test);
    assertThrows(SolrServiceException.class, () -> testling.prepareTransaction(txId));
    verify(masterClient, times(1)).rollback(eq("testCore"));

    when(response.getStatus()).thenReturn(0);

    testling.update(test);
    testling.prepareTransaction(txId);

    verify(masterClient, times(2)).add(anyString(), anyList());
  }

  @Test
  void createErgebnis() {
    SolrErgebnisEintrag solrErgebnisEintrag = new SolrErgebnisEintrag();
    solrErgebnisEintrag.bearbeiter = "Tom";
    solrErgebnisEintrag.id = "123";
    solrErgebnisEintrag.bestandhaltendeInstitutionName = "bestandhaltendeInstitutionName";
    solrErgebnisEintrag.bestandhaltendeInstitutionOrt = "bestandhaltendeInstitutionOrt";
    solrErgebnisEintrag.containsBuchschmuck = true;
    solrErgebnisEintrag.containsDigitalisat = true;
    solrErgebnisEintrag.containsBeschreibung = true;
    solrErgebnisEintrag.jahrDerPublikation = 32;
    solrErgebnisEintrag.lastUpdate = new Date();
    solrErgebnisEintrag.verwaltungsTyp = "intern";
    solrErgebnisEintrag.sichtbarkeit = "sichtbarkeit";
    solrErgebnisEintrag.signatur = "signatur";
    solrErgebnisEintrag.publiziert = false;
    solrErgebnisEintrag.title = "title";
    solrErgebnisEintrag.typ = "KOD";

    QueryResponse response = Mockito.mock(QueryResponse.class);
    List<SolrErgebnisEintrag> responseBeans = new ArrayList<>();
    responseBeans.add(solrErgebnisEintrag);
    SolrDocumentList responseResults = new SolrDocumentList();

    NamedList<Object> facetNL = new NamedList<>();
    NamedList<Object> bucketBasedFacet = new NamedList<>();

    NamedList<Object> bucketVerwaltungstypIntern = new NamedList<>();
    bucketVerwaltungstypIntern.add("val", "intern");
    bucketVerwaltungstypIntern.add("count", 1);
    NamedList<Object> bucketVerwaltungstypExtern = new NamedList<>();
    bucketVerwaltungstypExtern.add("val", "extern");
    bucketVerwaltungstypExtern.add("count", 44);

    List<NamedList<Object>> buckets = new ArrayList<>();
    buckets.add(bucketVerwaltungstypIntern);
    buckets.add(bucketVerwaltungstypExtern);

    bucketBasedFacet.add("buckets", buckets);
    facetNL.add("verwaltungsTyp", bucketBasedFacet);

    NestableJsonFacet jsonFacetingResponse = new NestableJsonFacet(facetNL);

    when(response.getBeans(SolrErgebnisEintrag.class)).thenReturn(responseBeans);
    when(response.getResults()).thenReturn(responseResults);
    when(response.getJsonFacetingResponse()).thenReturn(jsonFacetingResponse);

    Ergebnis ergebnis = SolrSuchAdapter.createErgebnis(response, null, null);

    assertEquals(1, ergebnis.getFacetCountVerwaltungIntern());
    assertEquals(44, ergebnis.getFacetCountVerwaltungExtern());
    List<ErgebnisEintrag> eintragListe = ergebnis.getEintragListe();
    assertEquals("intern", eintragListe.get(0).getVerwaltungsTyp());
  }

  @Test
  void mapEintrag() {
    SolrErgebnisEintrag solrErgebnisEintrag = new SolrErgebnisEintrag();
    solrErgebnisEintrag.bearbeiter = "Tom";
    solrErgebnisEintrag.id = "123";
    solrErgebnisEintrag.bestandhaltendeInstitutionName = "bestandhaltendeInstitutionName";
    solrErgebnisEintrag.bestandhaltendeInstitutionOrt = "bestandhaltendeInstitutionOrt";
    solrErgebnisEintrag.containsBuchschmuck = true;
    solrErgebnisEintrag.containsDigitalisat = true;
    solrErgebnisEintrag.containsBeschreibung = true;
    solrErgebnisEintrag.jahrDerPublikation = 32;
    solrErgebnisEintrag.lastUpdate = new Date();
    solrErgebnisEintrag.sichtbarkeit = "sichtbarkeit";
    solrErgebnisEintrag.signatur = "signatur";
    solrErgebnisEintrag.publiziert = null;
    solrErgebnisEintrag.title = "title";
    solrErgebnisEintrag.typ = "KOD";

    ErgebnisEintrag ergebnisEintrag = SolrSuchAdapter.mapEintrag(solrErgebnisEintrag);

    assertEquals("123", ergebnisEintrag.getId());
    assertEquals("Tom", ergebnisEintrag.getBearbeiter());
    assertEquals("bestandhaltendeInstitutionName",
        ergebnisEintrag.getBestandhaltendeInstitutionName());
    assertEquals("bestandhaltendeInstitutionOrt",
        ergebnisEintrag.getBestandhaltendeInstitutionOrt());
    assertEquals(true, ergebnisEintrag.getContainsBuchschmuck());
    assertEquals(true, ergebnisEintrag.getContainsDigitalisat());
    assertEquals(true, ergebnisEintrag.getContainsBeschreibung());
    assertEquals(32, ergebnisEintrag.getJahrDerPublikation());
    assertNotNull(ergebnisEintrag.getLastUpdate());
    assertEquals("sichtbarkeit", ergebnisEintrag.getSichtbarkeit());
    assertEquals("signatur", ergebnisEintrag.getSignatur());
    assertNull(ergebnisEintrag.getPubliziert());
    assertEquals("title", ergebnisEintrag.getTitle());
    assertEquals(SuchDokumentTyp.KOD, ergebnisEintrag.getTyp());
  }

  @Test
  void addSortToQueryWithSignatur() {
    Suche suche = Suche.builder()
        .withQuery(QUERY_WILDCARD)
        .withSort(new Sort(SortCriteria.SIGNATUR, SortType.ASC))
        .build();
    SolrQueryCommand queryCommand = new SolrQueryCommand("searchableValues_t:" + suche.getQuery());

    SolrSuchAdapter.addSortToQuery(suche, queryCommand);
    assertEquals("signatur_t asc", queryCommand.getSort());
  }

  @Test
  void addSortToQuery() {
    Suche suche = Suche.builder()
        .withQuery("*")
        .withSort(new Sort(SortCriteria.RELEVANZ, SortType.DESC))
        .build();
    SolrQueryCommand queryCommand = new SolrQueryCommand("searchableValues_t:" + suche.getQuery());

    SolrSuchAdapter.addSortToQuery(suche, queryCommand);
    assertEquals("score desc", queryCommand.getSort());
  }

  @Test
  void addFilterToQuery() {
    Suche suche = Suche.builder().withQuery(QUERY_WILDCARD)
        .withFilterContainsBuchschmuck(Boolean.TRUE)
        .withFilterContainsDigitalisat(Boolean.TRUE)
        .withFilterContainsBeschreibung(Boolean.TRUE)
        .withFilterSuchDokumentTyp(SuchDokumentTyp.KOD)
        .withFilterPubliziert(Boolean.TRUE)
        .withFilterBestandshaltendeInstitutionen("SBB")
        .withFilterVerwaltungsTyp(VerwaltungsTyp.EXTERN)
        .withFilterAutoren("Autor")
        .withFilterLastUpdateFrom(LocalDate.of(2022, 1, 1))
        .withFilterLastUpdateTo(LocalDate.of(2022, 12, 31))
        .build();

    SolrQueryCommand queryCommand = new SolrQueryCommand("searchableValues_t:" + suche.getQuery());

    SolrSuchAdapter.addFilterToQuery(suche, queryCommand);

    assertTrue(queryCommand.getFilter().stream().anyMatch("containsBuchschmuck_b:true"::equals));
    assertTrue(queryCommand.getFilter().stream().anyMatch("containsDigitalisat_b:true"::equals));
    assertTrue(queryCommand.getFilter().stream().anyMatch("containsBeschreibung_b:true"::equals));
    assertTrue(queryCommand.getFilter().stream().anyMatch("typ_s:KOD"::equals));
    assertFalse(queryCommand.getFilter().stream().anyMatch("publiziert_b:true"::equals));
    assertTrue(queryCommand.getFilter().stream().anyMatch("bestandhaltendeInstitutionName_s:(\"SBB\")"::equals));
    assertFalse(queryCommand.getFilter().stream().anyMatch("verwaltungsTyp_s:extern"::equals));
    assertFalse(queryCommand.getFilter().stream().anyMatch("autoren:(\"Autor\")"::equals));
    assertTrue(queryCommand.getFilter().stream().anyMatch("lastUpdate_dt:[2022-01-01T00:00:00Z TO *]"::equals));
    assertTrue(queryCommand.getFilter().stream().anyMatch("lastUpdate_dt:[* TO 2022-12-31T23:59:59Z]"::equals));
  }

  @Test
  void addHighlightingToQueryWithStarQuery() {
    Suche suche = Suche.builder()
        .withQuery(QUERY_WILDCARD)
        .withHighlighting(true)
        .build();
    SolrQueryCommand queryCommand = new SolrQueryCommand("searchableValues_t:" + suche.getQuery());

    SolrSuchAdapter.addHighlightingToQuery(suche, queryCommand);

    assertNull(queryCommand.getParams());
  }

  @Test
  void addHighlightingToQuery() {
    Suche suche = Suche.builder()
        .withQuery("Cod")
        .withHighlighting(true)
        .withFilterContainsBuchschmuck(Boolean.TRUE)
        .withFilterContainsDigitalisat(Boolean.TRUE)
        .withFilterContainsBeschreibung(Boolean.TRUE)
        .withFilterSuchDokumentTyp(SuchDokumentTyp.KOD)
        .withFilterBestandshaltendeInstitutionen("SBB")
        .build();
    SolrQueryCommand queryCommand = new SolrQueryCommand("searchableValues_t:" + suche.getQuery());

    SolrSuchAdapter.addHighlightingToQuery(suche, queryCommand);

    assertEquals(
        "{hl=on, hl.q=searchableValues_t:Cod AND signatur_t:Cod "
            + "AND title_t:Cod AND bestandhaltendeInstitutionName_t:Cod "
            + "AND bestandhaltendeInstitutionOrt_t:Cod, "
            + "hl.fl=searchableValues_t,signatur_t,title_t,bestandhaltendeInstitutionName_t,"
            + "bestandhaltendeInstitutionOrt_t, hl.method=fastVector, hl.encoder=html}",
        queryCommand.getParams().toString());
  }

  @Test
  void testDelete() throws Exception {
    when(xidImpleMOCK.getTransactionUid()).thenReturn(uid);
    when(transactionManagerMOCK.getTransaction()).thenReturn(transactionMOCK);
    when(transactionMOCK.getTxId()).thenReturn(xidImpleMOCK);

    final SuchDokument test = SuchDokument.builder()
        .withId("JUNIT Test")
        .withTitel("some title")
        .withSuchDokumentTyp(SuchDokumentTyp.BS)
        .withSearchableValues("irgendwas#und#nix")
        .build();

    final UpdateResponse response = mock(UpdateResponse.class);
    when(response.getStatus()).thenReturn(-1);
    when(response.getException()).thenReturn(null);
    when(masterClient.deleteById(eq(testling.core), anyList())).thenReturn(response);

    testling.delete(test.getId());
    assertThrows(SolrServiceException.class, () -> testling.prepareTransaction(txId));
    verify(masterClient, times(1)).rollback(eq("testCore"));

    when(response.getStatus()).thenReturn(0);

    testling.delete(test.getId());
    testling.prepareTransaction(txId);

    verify(masterClient, times(2)).deleteById(eq(testling.core), anyList());
  }

  @Test
  void testDeleteBySuchDokumentTypWithoutTransaction() throws Exception {
    final UpdateResponse response = mock(UpdateResponse.class);
    when(response.getStatus()).thenReturn(0);
    when(response.getException()).thenReturn(null);
    when(masterClient.deleteByQuery(eq(testling.core), anyString())).thenReturn(response);

    testling.deleteBySuchDokumentTypWithoutTransaction(SuchDokumentTyp.BS);

    verify(masterClient, times(1)).deleteByQuery(eq(testling.core), anyString());
  }

  @Test
  void testCreateQuery() throws JsonProcessingException {
    String result = "{  \"query\" : \"searchableValues_t:*\",  \"limit\" : 25,  \"offset\" : 12,  \"facet\" : {    "
        + "\"verwaltungsTyp\" : {      \"type\" : \"terms\",      \"field\" : \"verwaltungsTyp_s\",      "
        + "\"limit\" : 2,      \"mincount\" : 0    },    \"institutionen\" : {      \"type\" : \"terms\",      "
        + "\"field\" : \"bestandhaltendeInstitutionName_s\",      \"limit\" : 99,      \"mincount\" : 0    },    "
        + "\"autoren\" : {      \"type\" : \"terms\",      \"field\" : \"autoren\",      \"limit\" : 99,      "
        + "\"mincount\" : 0    },    \"containsDigitalisat\" : {      \"type\" : \"terms\",      "
        + "\"field\" : \"containsDigitalisat_b\",      \"limit\" : 2,      \"mincount\" : 0    },    "
        + "\"typ\" : {      \"type\" : \"terms\",      \"field\" : \"typ_s\",      \"limit\" : 2,      "
        + "\"mincount\" : 0    },    \"containsBeschreibung\" : {      \"type\" : \"terms\",      "
        + "\"field\" : \"containsBeschreibung_b\",      \"limit\" : 2,      \"mincount\" : 0    },    "
        + "\"publiziert\" : {      \"type\" : \"terms\",      \"field\" : \"publiziert_b\",      \"limit\" : 2,      "
        + "\"mincount\" : 0    }  },  \"params\" : {    \"df\" : \"searchableValues_t\",    \"q.op\" : \"AND\"  }}";

    final Suche suche = Suche.builder()
        .withQuery("*")
        .withHighlighting(true)
        .build();
    suche.setPage(new Page(25, 12));

    String suchejson = new SolrSuchAdapter().createQuery(suche);

    assertEquals(result.replaceAll("[\r\n]", ""), suchejson.replaceAll("[\r\n]", ""));
  }

  @Test
  void testHandleHighlightingResponse() {
    final Suche suche = Suche.builder()
        .withQuery("Test")
        .withHighlighting(true)
        .build();
    suche.setPage(new Page(25, 12));

    SolrQueryResponse<SolrErgebnisEintrag> response = new SolrQueryResponse<>();

    Map<String, Map<String, List<String>>> hits = new HashMap<>();
    Map<String, List<String>> hitsValue = new HashMap<>();
    hitsValue.put(SolrIndexFields.FIELD_TITEL, Collections.singletonList("<em>Test</em>Titel"));
    hits.put("1", hitsValue);

    response.setHighlighting(new SolrQueryResponse.Highlighting());
    response.getHighlighting().setHits(hits);

    Ergebnis ergebnis = new Ergebnis();
    ErgebnisEintrag eintrag = new ErgebnisEintrag();
    eintrag.setId("1");
    eintrag.setTitle("TestTitel");

    ergebnis.getEintragListe().add(eintrag);

    SolrSuchAdapter.handleHighlightingResponse(suche, ergebnis, hits);

    assertEquals("<em>Test</em>Titel", eintrag.getTitle());

  }

  @Test
  void testUpdateWithoutTransaction() throws Exception {
    final SuchDokument[] suchDokumente = new SuchDokument[]{
        SuchDokument.builder().withId("S-1").withSuchDokumentTyp(SuchDokumentTyp.KOD).build(),
        SuchDokument.builder().withId("S-2").withSuchDokumentTyp(SuchDokumentTyp.BS).build()};

    final UpdateResponse response = mock(UpdateResponse.class);
    when(response.getStatus()).thenReturn(0);
    when(masterClient.add(anyString(), anyList())).thenReturn(response);

    testling.updateWithoutTransaction(true, suchDokumente);

    verify(masterClient, times(1)).deleteById(eq(testling.core), anyList());
    verify(masterClient, times(1)).add(eq(testling.core), anyList());
  }

  @Test
  void testCommitWithoutTransaction() throws Exception {
    testling.commitWithoutTransaction();
    verify(masterClient, times(1)).commit(eq(testling.core));
  }

  @Test
  void testDeleteBeforeUpdate() throws Exception {
    SuchDokument[] suchDokumente = new SuchDokument[]{
        SuchDokument.builder().withId("S-1").withSuchDokumentTyp(SuchDokumentTyp.KOD).build(),
        SuchDokument.builder().withId("S-2").withSuchDokumentTyp(SuchDokumentTyp.BS).build()};

    testling.deleteBeforeUpdate(false, suchDokumente);

    verify(masterClient, times(1)).deleteById(testling.core, Arrays.asList("S-1", "S-2"));

    Mockito.doThrow(new SolrServerException("Test")).when(masterClient).deleteById(anyString(), anyList());

    Exception exception = assertThrows(Exception.class,
        () -> testling.deleteBeforeUpdate(true, suchDokumente));
    assertEquals("Unable to delete solr entry with iDs = [S-1, S-2]", exception.getMessage());
  }
}
