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
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrAPI.SEARCH_DEFAULT_FIELD;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrAPI.SEARCH_OPERATOR_AND;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrAPI.SEARCH_QUERY_OPERATOR;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrIndexFields.FIELD_SEARCHABLE_VALUES;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr.SolrMasterActionType.DELETE_BEFORE_UPDATE;
import static javax.transaction.Status.STATUS_ACTIVE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Ergebnis;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.ErgebnisEintrag;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.FilterCriteria;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Page;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokumentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchPort;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.Suche;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BaseHttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient.Builder;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.response.json.BucketBasedJsonFacet;
import org.apache.solr.client.solrj.response.json.NestableJsonFacet;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.MultiMapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 13.01.21
 */
@Singleton
@Slf4j
public class SolrSuchAdapter implements SuchPort {

  public static final int STATUS_OK = 0;
  public static final List<String> FIELDS_FOR_HIGHLIGHTING = List
      .of(SolrIndexFields.FIELD_SEARCHABLE_VALUES, SolrIndexFields.FIELD_SIGNATUR,
          SolrIndexFields.FIELD_TITEL,
          SolrIndexFields.FIELD_BESTANDHALTENDE_INSTITUTION_NAME,
          SolrIndexFields.FIELD_BESTANDHALTENDE_INSTITUTION_ORT);
  private static final ReentrantLock lock = new ReentrantLock();
  private static final Map<String, SolrMasterXAResource> activeSolrXAResources = new ConcurrentHashMap<>();
  protected TransactionManager transactionManager;
  protected HttpSolrClient slaveClient;
  protected HttpSolrClient masterClient;
  String core;
  String masterURL;

  protected SolrSuchAdapter() {
  }

  @Inject
  public SolrSuchAdapter(
      TransactionManager transactionManager,
      @ConfigProperty(name = "solr.url.master") String masterURL,
      @ConfigProperty(name = "solr.url.slave") String slaveURL,
      @ConfigProperty(name = "solr.core") String core,
      @ConfigProperty(name = "solr.connectionTimeout") int connectionTimeout,
      @ConfigProperty(name = "solr.socketTimeout") int socketTimeout) {
    this.transactionManager = transactionManager;
    this.core = core;
    log.info("Initializing master client..");
    masterClient = new Builder(masterURL)
        .withConnectionTimeout(connectionTimeout)
        .withSocketTimeout(socketTimeout)
        .build();

    slaveClient = new Builder(slaveURL)
        .withConnectionTimeout(connectionTimeout)
        .withSocketTimeout(socketTimeout)
        .build();
  }

  static void addHighlightingToQuery(Suche suche, SolrQueryCommand queryCommand) {
    if (suche.getHighlighting() != null && suche.getHighlighting().isActive() && !suche.getQuery()
        .equals(QUERY_WILDCARD)) {
      queryCommand.addParam(SolrAPI.HIGHLIGHTING_ACTIVE_KEY, SolrAPI.HIGHLIGHTING_ACTIVE_VALUE);
      queryCommand.addParam(SolrAPI.HIGHLIGHTING_QUERY_KEY,
          String.join(":" + suche.getQuery() + SolrAPI.HIGHLIGHTING_AND_OPERATOR,
                  FIELDS_FOR_HIGHLIGHTING)
              .concat(":" + suche.getQuery()));
      queryCommand.addParam(SolrAPI.HIGHLIGHTING_FIELDS_KEY,
          String.join(",", FIELDS_FOR_HIGHLIGHTING));
      queryCommand.addParam(SolrAPI.HIGHLIGHTING_ENCODING_KEY,
          SolrAPI.HIGHLIGHTING_ENCODING_VALUE);
      queryCommand.addParam(SolrAPI.HIGHLIGHTING_METHOD_KEY, suche.getHighlighting().getMethod());
    }
  }

  static void addFilterToQuery(Suche suche, SolrQueryCommand queryCommand) {
    if (Objects.nonNull(suche.getFilter())) {

      for (Map.Entry<FilterCriteria, String> criteriaEntry : suche.getFilter().entrySet()) {
        String filter = criteriaEntry.getKey().createFilter(criteriaEntry.getValue());
        queryCommand.addFilter(filter);
      }
    }
  }

  static void addSortToQuery(Suche suche, SolrQueryCommand queryCommand) {
    if (Objects.nonNull(suche.getSort())) {
      String sort = suche.getSort().getCriteria().getSortField()
          + " " + suche.getSort().getType().name().toLowerCase();
      queryCommand.setSort(sort);
    }
  }

  static ErgebnisEintrag mapEintrag(final SolrErgebnisEintrag solrEintrag) {
    ErgebnisEintrag result = new ErgebnisEintrag();

    result.setId(solrEintrag.getId());
    result.setBearbeiter(solrEintrag.getBearbeiter());
    result.setBestandhaltendeInstitutionName(solrEintrag.getBestandhaltendeInstitutionName());
    result.setBestandhaltendeInstitutionOrt(solrEintrag.getBestandhaltendeInstitutionOrt());
    result.setContainsBuchschmuck(solrEintrag.getContainsBuchschmuck());
    result.setContainsDigitalisat(solrEintrag.getContainsDigitalisat());
    result.setContainsBeschreibung(solrEintrag.getContainsBeschreibung());
    result.setJahrDerPublikation(solrEintrag.getJahrDerPublikation());
    if (solrEintrag.getLastUpdate() != null) {
      result.setLastUpdate(
          LocalDateTime.ofInstant(solrEintrag.getLastUpdate().toInstant(), ZoneId.systemDefault()));
    }
    result.setSichtbarkeit(solrEintrag.getSichtbarkeit());
    result.setVerwaltungsTyp(solrEintrag.getVerwaltungsTyp());
    result.setSignatur(solrEintrag.getSignatur());
    result.setPubliziert(solrEintrag.getPubliziert());
    result.setTitle(solrEintrag.getTitle());
    result.setTyp(SuchDokumentTyp.fromString(solrEintrag.getTyp()));
    result.setAutoren(solrEintrag.getAutoren());

    return result;
  }

  static Ergebnis createErgebnis(final QueryResponse response, final Page page, Suche suche) {

    log.debug("SOLR RESPONSE DOCUMENT {} ", response);

    Ergebnis result = new Ergebnis();

    List<SolrErgebnisEintrag> responseBeans = response.getBeans(SolrErgebnisEintrag.class);
    for (SolrErgebnisEintrag solrErgebnisEintrag : responseBeans) {
      result.addErgebnisEintrag(mapEintrag(solrErgebnisEintrag));
    }

    if (Objects.nonNull(page) && Objects.nonNull(response.getResults())) {
      result.addErgebnisePageEnries(response.getResults(), page);
    }

    handleFacets(response, result);

    handleHighlightingResponse(suche, result, response.getHighlighting());

    return result;
  }

  static void handleFacets(QueryResponse response, Ergebnis result) {
    NestableJsonFacet jsonFacetingResponse = response.getJsonFacetingResponse();
    Map<String, Facet> facets = new HashMap<>();

    facets.put(FacetDocumentTyp.TYP_FACET_NAME, new FacetDocumentTyp());
    facets.put(FacetDigitalisat.CONTAINS_DIGITALISAT_FACET_NAME, new FacetDigitalisat());
    facets.put(FacetBuchschmuck.CONTAINS_BUCHSCHMUCK_FACET_NAME, new FacetBuchschmuck());
    facets.put(FacetBeschreibung.CONTAINS_BESCHREIBUNG_FACET_NAME, new FacetBeschreibung());
    facets.put(FacetPubliziert.PUBLIZIERT_FACET_NAME, new FacetPubliziert());
    facets.put(FacetInstitution.INSTITUTIONEN_FACET_NAME, new FacetInstitution());
    facets.put(FacetVerwaltungsTyp.VERWALTUNGS_TYP_FACET_NAME, new FacetVerwaltungsTyp());
    facets.put(FacetAutoren.AUTOREN_FACET_NAME, new FacetAutoren());

    if (jsonFacetingResponse == null) {
      log.warn("jsonFacetingResponse was NULL!!!");
    } else {
      for (String facetName : jsonFacetingResponse.getBucketBasedFacetNames()) {
        BucketBasedJsonFacet bucketBasedFacets = jsonFacetingResponse
            .getBucketBasedFacets(facetName);

        Optional.ofNullable(facets.get(facetName))
            .ifPresentOrElse(f -> f.handleResult(result, bucketBasedFacets),
                () -> log.warn("No Facet found for facetName {} ", facetName));

      }
    }
  }

  static void handleHighlightingResponse(Suche suche, Ergebnis result,
      Map<String, Map<String, List<String>>> highlighting) {

    if (highlighting != null && !highlighting.isEmpty() && suche.getQuery().length() > 1 && !suche
        .getQuery()
        .equals("*")) {

      log.info("start handle Highlighting");

      highlighting.keySet().forEach(uuid -> {

        log.debug("Highlighting Content {} {} ", uuid, highlighting.get(uuid));

        Optional<ErgebnisEintrag> eintragOptional = result.getEintragListe().stream()
            .filter(eintrag -> eintrag.getId().equals(uuid)).findFirst();

        highlighting.get(uuid).keySet().forEach(highlightFieldKey -> {

          List<String> highLightFieldValueList = highlighting.get(uuid).get(highlightFieldKey);
          String highLightFieldValue = highLightFieldValueList.get(0);

          String fieldValue = highLightFieldValue
              .substring(0,
                  highLightFieldValue.contains("\",") ? highLightFieldValue.indexOf("\",")
                      : highLightFieldValue.length())
              .replaceAll("\"", "");

          log.debug("Highlighting field value: {}", fieldValue);

          if (eintragOptional.isPresent()) {
            ErgebnisEintrag ergebnisEintrag = eintragOptional.get();

            if (highlightFieldKey.equals(SolrIndexFields.FIELD_BESTANDHALTENDE_INSTITUTION_NAME)) {
              ergebnisEintrag.setBestandhaltendeInstitutionName(fieldValue);
            }

            if (highlightFieldKey.equals(SolrIndexFields.FIELD_BESTANDHALTENDE_INSTITUTION_ORT)) {
              ergebnisEintrag.setBestandhaltendeInstitutionOrt(fieldValue);
            }

            if (highlightFieldKey.equals(SolrIndexFields.FIELD_SIGNATUR)) {
              ergebnisEintrag.setSignatur(fieldValue);
            }

            if (highlightFieldKey.equals(SolrIndexFields.FIELD_TITEL)) {
              ergebnisEintrag.setTitle(fieldValue);
            }
          }
        });
      });
    }
  }

  private static void addQueryOperatorAndDefaultSearchField(SolrQueryCommand queryCommand) {
    queryCommand.addParam(SEARCH_QUERY_OPERATOR, SEARCH_OPERATOR_AND);
    queryCommand.addParam(SEARCH_DEFAULT_FIELD, FIELD_SEARCHABLE_VALUES);
  }

  @Override
  @Transactional
  public void update(SuchDokument... suchDokumente) throws SolrServiceException {
    update(false, suchDokumente);
  }

  @Transactional
  @Override
  public void update(boolean deleteBeforeUpdate, SuchDokument... suchDokumente)
      throws SolrServiceException {
    SolrMasterAction update = SolrMasterAction.newUpdate(deleteBeforeUpdate, suchDokumente);
    storeActionInTransaction(update);
  }

  @Override
  public void updateWithoutTransaction(boolean deleteBeforeUpdate, final SuchDokument... suchDokumente)
      throws SolrServiceException {
    updateIntern(deleteBeforeUpdate, suchDokumente);
  }

  @Override
  public void commitWithoutTransaction() throws SolrServiceException {
    try {
      masterClient.commit(core);
    } catch (Exception e) {
      log.error("Problem with the master-client commit {}", e.getMessage(), e);
      throw new SolrServiceException("Problem with the master-client commit " + e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public void delete(String... iDs) throws SolrServiceException {
    SolrMasterAction delete = SolrMasterAction.newDelete(iDs);
    storeActionInTransaction(delete);
  }

  @Transactional
  @Override
  public void deleteAll() throws SolrServiceException {
    SolrMasterAction deleteAll = SolrMasterAction.newDeleteAll();
    storeActionInTransaction(deleteAll);
  }

  @Override
  public void deleteBySuchDokumentTypWithoutTransaction(SuchDokumentTyp suchDokumentTyp) throws SolrServiceException {
    try {
      UpdateResponse deleteResponse = masterClient.deleteByQuery(core,
          SolrIndexFields.FIELD_TYP + ":" + suchDokumentTyp);
      if (deleteResponse.getStatus() != STATUS_OK) {
        String message = "Unable to deleteBySuchDokumentTyp solr documents for suchDokumentTyp " + suchDokumentTyp
            + ". Response status: " + deleteResponse.getStatus();
        if (deleteResponse.getException() == null) {
          throw new SolrServiceException(message);
        }
        throw new SolrServiceException(message, deleteResponse.getException());
      }
    } catch (SolrServerException | RemoteSolrException | IOException e) {
      log.error("Unable to deleteBySuchDokumentTyp in solr {}", e.getMessage(), e);
      throw new SolrServiceException("Unable to deleteBySuchDokumentTyp in solr " + e.getMessage(), e);
    }
  }

  private void updateIntern(boolean deleteBeforeUpdate, SuchDokument... suchDokumente)
      throws SolrServiceException {
    try {
      if (deleteBeforeUpdate) {
        deleteBeforeUpdate(false, suchDokumente);
      }
      UpdateResponse updateResponse = masterClient.add(core, Arrays.asList(suchDokumente));
      if (updateResponse.getStatus() != STATUS_OK) {
        String message =
            "Unable to update solr document. Response status: " + updateResponse.getStatus();
        if (updateResponse.getException() == null) {
          throw new SolrServiceException(message);
        }
        throw new SolrServiceException(message, updateResponse.getException());
      }
    } catch (Exception e) {
      log.error("Problem with the update of the document {}", suchDokumente, e);
      throw new SolrServiceException(
          "Problem with the update of the document " + Arrays.toString(suchDokumente), e);
    }
  }

  void deleteBeforeUpdate(boolean withCommit, SuchDokument... suchDokumente) throws Exception {
    try {
      int size = suchDokumente.length;
      ArrayList<String> ids = new ArrayList<>(size);
      for (SuchDokument suchDokument : suchDokumente) {
        String id = suchDokument.getId();
        if (id == null || id.trim().isEmpty()) {
          log.warn("Suchdokument without ID {}", suchDokument);
        } else {
          ids.add(id);
        }
      }
      if (!ids.isEmpty()) {
        deleteIntern(ids.toArray(new String[size]));
      }
    } catch (Exception ex) {
      log.warn("Delete Before Update was not clean {}", ex.getMessage(), ex);
      if (withCommit) {
        throw ex;
      }
    }
  }

  @Transactional
  void deleteIntern(String... iDs) throws SolrServiceException {
    try {
      UpdateResponse deleteResponse = masterClient.deleteById(core, Arrays.asList(iDs));
      if (deleteResponse.getStatus() != STATUS_OK) {
        String message =
            "Unable to delete solr document with iDs=" + Arrays.toString(iDs)
                + ". Response status: " + deleteResponse
                .getStatus();
        if (deleteResponse.getException() == null) {
          throw new SolrServiceException(message);
        }
        throw new SolrServiceException(message, deleteResponse.getException());
      }
    } catch (SolrServerException | RemoteSolrException | IOException e) {
      log.error("Unable to delete solr entry with iDs = {}", iDs, e);
      throw new SolrServiceException(
          "Unable to delete solr entry with iDs = " + Arrays.toString(iDs), e);
    }
  }

  @Transactional
  void deleteAllIntern() throws SolrServiceException {
    try {
      UpdateResponse deleteResponse = masterClient.deleteByQuery(core, "*:*");
      if (deleteResponse.getStatus() != STATUS_OK) {
        String message =
            "Unable to deleteAll solr documents. Response status: " + deleteResponse
                .getStatus();
        if (deleteResponse.getException() == null) {
          throw new SolrServiceException(message);
        }
        throw new SolrServiceException(message, deleteResponse.getException());
      }
    } catch (SolrServerException | RemoteSolrException | IOException e) {
      log.error("Unable to deleteAll in solr {}", e.getMessage(), e);
      throw new SolrServiceException("Unable to deleteAll in solr " + e.getMessage(), e);
    }
  }

  @Override
  public Ergebnis search(Suche suche) throws SolrServiceException {

    log.debug("Starting Searching in Solr {} ", suche);

    Map<String, String[]> queryParamMap = new HashMap<>();

    try {
      String jsonQuery = createQuery(suche);
      queryParamMap.put(CommonParams.JSON, new String[]{jsonQuery});
      SolrParams queryParams = new MultiMapSolrParams(queryParamMap);
      QueryResponse response = slaveClient.query(core, queryParams, METHOD.POST);

      log.debug(queryParams.toQueryString());
      log.debug(response.toString());

      if (response.getStatus() != 0) {
        String message =
            "Unable to search solr document with suche=" + suche + ". Response status: " + response
                .getStatus();
        if (response.getException() == null) {
          throw new SolrServiceException(message);
        } else {
          throw new SolrServiceException(message, response.getException());
        }
      }

      return createErgebnis(response, suche.getPage(), suche);
    } catch (SolrServerException | IOException e) {
      log.error("Problem with the search {}", e.getMessage(), e);
      throw new SolrServiceException("Problem with the search " + suche, e);
    }
  }

  String createQuery(final Suche suche) throws JsonProcessingException {
    final SolrQueryCommand queryCommand = new SolrQueryCommand(
        FIELD_SEARCHABLE_VALUES + ":" + suche.getQuery());

    if (Objects.nonNull(suche.getPage())) {
      queryCommand.setLimit(suche.getPage().getNumberOfRows());
      queryCommand.setOffset(suche.getPage().getOffset());
    }

    addSortToQuery(suche, queryCommand);

    addFilterToQuery(suche, queryCommand);

    addFacetsToQuery(queryCommand);

    addHighlightingToQuery(suche, queryCommand);

    addQueryOperatorAndDefaultSearchField(queryCommand);

    final ObjectMapper mapper = createMapper();

    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(queryCommand);
  }

  private void addFacetsToQuery(SolrQueryCommand queryCommand) {
    queryCommand.addFacet(FacetDocumentTyp.TYP_FACET_NAME, SolrIndexFields.FIELD_TYP, 2, 0);
    queryCommand.addFacet(FacetInstitution.INSTITUTIONEN_FACET_NAME,
        SolrIndexFields.FIELD_BESTANDHALTENDE_INSTITUTION_NAME_FACETTE, 99, 0);
    queryCommand.addFacet(FacetDigitalisat.CONTAINS_DIGITALISAT_FACET_NAME,
        SolrIndexFields.FIELD_CONTAINS_DIGITALISAT, 2, 0);
    queryCommand.addFacet(FacetBeschreibung.CONTAINS_BESCHREIBUNG_FACET_NAME,
        SolrIndexFields.FIELD_CONTAINS_BESCHREIBUNG, 2, 0);
    queryCommand.addFacet(FacetPubliziert.PUBLIZIERT_FACET_NAME,
        SolrIndexFields.FIELD_PUBLIZIERT, 2, 0);
    queryCommand.addFacet(FacetVerwaltungsTyp.VERWALTUNGS_TYP_FACET_NAME,
        SolrIndexFields.FIELD_VERWALTUNGS_TYP, 2, 0);
    queryCommand.addFacet(FacetAutoren.AUTOREN_FACET_NAME, SolrIndexFields.FIELD_AUTOREN, 99, 0);
  }

  void beginTransaction(String txId, SolrMasterXAResource solrMasterXAResource) {
    if (txId == null) {
      log.error("TXID is null");
    }
  }

  void prepareTransaction(String txId) throws SolrServiceException {
    try {
      lock.lock();
      SolrMasterXAResource currentXAResource = activeSolrXAResources.get(txId);
      if (currentXAResource == null) {
        log.warn("Current SOLR XAResource is null");
        throw new SolrServiceException("Current SOLR XAResource is null");
      }
      List<SolrMasterAction> storedActions = currentXAResource.getStoredActions();
      for (SolrMasterAction action : storedActions) {
        switch (action.getType()) {
          case UPDATE:
          case DELETE_BEFORE_UPDATE:
            List<SuchDokument> suchDokumentsToUpdate = action.getSuchDokumentsToUpdate();
            updateIntern(action.getType() == DELETE_BEFORE_UPDATE,
                suchDokumentsToUpdate.toArray(new SuchDokument[0]));
            break;
          case DELETE:
            List<String> idSToDelete = action.getIdSToDelete();
            deleteIntern(idSToDelete.toArray(new String[0]));
            break;
          case DELETE_ALL:
            deleteAllIntern();
            break;
        }
      }
      commitTransaction(txId);
    } catch (Exception e) {
      log.error("Problem with the commit transaction {}", e.getMessage(), e);
      rollbackTransaction(txId);
      throw new SolrServiceException("Problem with the commit transaction " + e.getMessage(), e);
    } finally {
      lock.unlock();
    }
  }

  void commitTransaction(String txId) throws SolrServiceException {
    try {
      masterClient.commit(core);
      activeSolrXAResources.remove(txId);
    } catch (Exception e) {
      log.error("Problem with the commit transaction {}", e.getMessage(), e);
      throw new SolrServiceException("Problem with the commit transaction " + e.getMessage(), e);
    }
  }

  void rollbackTransaction(String txId) throws SolrServiceException {
    try {
      masterClient.rollback(core);
      activeSolrXAResources.remove(txId);
    } catch (Exception e) {
      log.error("Problem with the transaction rollback  {}", e.getMessage(), e);
      throw new SolrServiceException("Problem with the transaction rollback " + e.getMessage(), e);
    }
  }

  ObjectMapper createMapper() {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    return mapper;
  }

  @Transactional
  String getTransactionId() {
    try {
      Transaction jtaTransaction = transactionManager.getTransaction();
      String solrTransactionId = SolrMasterXAResource.getTransactionId(jtaTransaction);
      if (!activeSolrXAResources.containsKey(solrTransactionId)) {
        if (jtaTransaction.getStatus() == STATUS_ACTIVE) {
          activeSolrXAResources.put(solrTransactionId, new SolrMasterXAResource(this));
          jtaTransaction.enlistResource(activeSolrXAResources.get(solrTransactionId));
        } else {
          log.warn(
              "Unable to register SolrMasterXAResource for transactionID= {} due to transaction status={}",
              solrTransactionId, jtaTransaction.getStatus());
        }
      }
      return solrTransactionId;
    } catch (Exception e) {
      throw new RuntimeException("Unable to enlistResource " + e.getMessage(), e);
    }
  }

  @Transactional
  void storeActionInTransaction(SolrMasterAction action) throws SolrServiceException {
    String txId = getTransactionId();
    SolrMasterXAResource solrMasterXAResource = getSolrMasterXAResource(txId);
    solrMasterXAResource.storeActionInTransaction(action);
  }

  private SolrMasterXAResource getSolrMasterXAResource(String txId) throws SolrServiceException {
    SolrMasterXAResource solrMasterXAResource = activeSolrXAResources.get(txId);
    if (solrMasterXAResource == null) {
      throw new SolrServiceException("Transaction is not initialized");
    }
    return solrMasterXAResource;
  }

}
