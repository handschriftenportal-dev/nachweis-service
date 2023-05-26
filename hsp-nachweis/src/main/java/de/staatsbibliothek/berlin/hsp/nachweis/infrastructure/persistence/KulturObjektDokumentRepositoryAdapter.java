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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 09.11.20
 */
@ApplicationScoped
public class KulturObjektDokumentRepositoryAdapter implements KulturObjektDokumentRepository {

  public static final String QUERY_KODIDS_BESCHREIBUNGSIDS = "select k.kulturobjektdokument_id, k.beschreibungenids from kulturobjektdokument_beschreibungenids k";
  public static final String KOD_TUPLE_ALIAS = "kulturobjektdokument_id";
  public static final String BESCHREIBUNG_TUPLE_ALIAS = "beschreibungenids";

  @Override
  public Map<String, List<String>> getAllBeschreibungsIdsWithKodIDs() {
    Query nativeQuery = getEntityManager().createNativeQuery(QUERY_KODIDS_BESCHREIBUNGSIDS, Tuple.class);
    List<Tuple> beschreibungsIds = nativeQuery.getResultList();

    if (beschreibungsIds.isEmpty()) {
      return Collections.emptyMap();
    }

    Map<String, List<String>> result = new HashMap<>(beschreibungsIds.size());

    for (Tuple element : beschreibungsIds) {

      String kulturobjektdokument_id = element.get(KOD_TUPLE_ALIAS, String.class);
      String beschreibungenids = element.get(BESCHREIBUNG_TUPLE_ALIAS, String.class);

      if (result.get(kulturobjektdokument_id) == null) {
        List<String> beschreibungsIdsArrl = new ArrayList<>();
        beschreibungsIdsArrl.add(beschreibungenids);
        result.put(kulturobjektdokument_id, beschreibungsIdsArrl);
      } else {
        result.get(kulturobjektdokument_id).add(beschreibungenids);
      }
    }
    return result;
  }

  @Override
  public KulturObjektDokument findByBeschreibung(String beschreibungId) {
    return find("from KulturObjektDokument k "
        + " join fetch k.beschreibungenIDs bs"
        + " join k.beschreibungenIDs bsq"
        + " where bsq=?1", beschreibungId)
        .range(0, 1)
        .firstResult();
  }

  @Override
  public List<KulturObjektDokument> findByIdentifikationIdent(String ident) {
    return find("from KulturObjektDokument k "
        + " join fetch k.gueltigeIdentifikation gi"
        + " where gi.ident=?1", ident).list();
  }

  public List<KulturObjektDokument> findByIdentifikationIdentAndBesitzerIDAndAufbewahrungsortID(String ident, String besitzerID, String aufbewahrungsOrtID) {
    return find("from KulturObjektDokument k "
        + " join fetch k.gueltigeIdentifikation gi"
        + " join fetch gi.besitzer bi"
        + " join fetch gi.aufbewahrungsOrt ao"
        + " where gi.ident=?1 and bi.id=?2 and ao.id=?3", ident, besitzerID, aufbewahrungsOrtID).list();
  }

  public Optional<KulturObjektDokument> findByIdentifikationIdentAndBesitzerNameAndAufbewahrungsortName(String ident, String besitzerName, String aufbewahrungsOrtName) {
    return find("select distinct k from KulturObjektDokument k"
        + " join k.gueltigeIdentifikation gi "
        + " join gi.besitzer nrb "
        + " join gi.aufbewahrungsOrt nra "
        + " left join nrb.varianterName vnrb"
        + " left join nra.varianterName vnra"
        + " where gi.ident=?1 and (nrb.name=?2 or vnrb.name=?2) and (nra.name=?3 or vnra.name=?3)", ident, besitzerName, aufbewahrungsOrtName)
        .singleResultOptional();
  }

  public Optional<KulturObjektDokument> findByAlternativeIdentifikationIdentAndBesitzerNameAndAufbewahrungsortName(String ident, String besitzerName, String aufbewahrungsOrtName) {
    return find("select distinct k from KulturObjektDokument k"
        + " join fetch k.alternativeIdentifikationen "
        + " join k.alternativeIdentifikationen ai "
        + " join ai.besitzer nrb "
        + " left join nrb.varianterName vnrb"
        + " join ai.aufbewahrungsOrt nra "
        + " left join nra.varianterName vnra "
        + " where ai.ident=?1 and (nrb.name=?2 or vnrb.name=?2) and (nra.name=?3 or vnra.name=?3)", ident, besitzerName, aufbewahrungsOrtName).singleResultOptional();
  }

  @Override
  public List<KulturObjektDokument> findByAlternativeIdentifikationIdent(String ident) {
    return find("select distinct k from KulturObjektDokument k "
        + " join fetch k.gueltigeIdentifikation"
        + " join fetch k.alternativeIdentifikationen"
        + " join k.alternativeIdentifikationen ai "
        + " where ai.ident=?1", ident)
        .list();
  }

  @Override
  public Optional<KulturObjektDokument> findByIdentifikationBeteiligeKoerperschaftName(String name) {
    return find("select distinct k from KulturObjektDokument k "
        + " join fetch k.gueltigeIdentifikation "
        + " join fetch k.alternativeIdentifikationen "
        + " left join k.alternativeIdentifikationen ai "
        + " left join k.gueltigeIdentifikation gi "
        + " left join gi.besitzer bgi "
        + " left join ai.besitzer bai "
        + "where bgi.name=?1 or bai.name=?2", name, name)
        .singleResultOptional();
  }

  @Override
  public List<KulturObjektDokumentListDTO> findFilteredKulturObjektDokumentListDTOs(int first, int pageSize,
      Map<String, String> filterBy, Map<String, Boolean> sortByAsc) {
    Objects.requireNonNull(filterBy, "filterBy is required");
    Objects.requireNonNull(sortByAsc, "sortByAsc is required");

    CriteriaQuery<KulturObjektDokumentListDTO> criteriaQuery =
        CriteriaQueryBuilder.buildListQuery(KulturObjektDokumentListDTO.class, getEntityManager(), filterBy,
            sortByAsc);

    TypedQuery<KulturObjektDokumentListDTO> typedQuery = getEntityManager().createQuery(criteriaQuery);
    typedQuery.setFirstResult(first);
    typedQuery.setMaxResults(pageSize);

    return typedQuery.getResultList();
  }

  @Override
  public int countFilteredKulturObjektDokumentListDTOs(Map<String, String> filterBy) {
    Objects.requireNonNull(filterBy, "filterBy is required");

    CriteriaQuery<Long> criteriaQuery =
        CriteriaQueryBuilder.buildCountQuery(KulturObjektDokumentListDTO.class, getEntityManager(), filterBy);
    return Optional.ofNullable(getEntityManager().createQuery(criteriaQuery).getSingleResult())
        .map(Long::intValue)
        .orElse(0);
  }

  @Override
  public KulturObjektDokumentListDTO findKulturObjektDokumentListDTOById(String id) {
    return getEntityManager().find(KulturObjektDokumentListDTO.class, id);
  }

}
