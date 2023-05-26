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

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 09.11.20
 */
@ApplicationScoped
@Slf4j
public class BeschreibungsRepositoryAdapter implements BeschreibungsRepository {

  @Override
  public Beschreibung findByAltIdentifier(String altIdentifier) {
    return find("from Beschreibung b "
        + " join fetch b.altIdentifier ai"
        + " where ai=?1", altIdentifier)
        .range(0, 1)
        .firstResult();
  }

  @Override
  public List<Beschreibung> findByKatalogId(String katalogID) {
    return list("katalogID", katalogID);
  }

  @Override
  public List<BeschreibungListDTO> findFilteredBeschreibungListDTOs(int first, int pageSize,
      Map<String, String> filterBy, Map<String, Boolean> sortByAsc) {
    Objects.requireNonNull(filterBy, "filterBy is required");
    Objects.requireNonNull(sortByAsc, "sortByAsc is required");

    CriteriaQuery<BeschreibungListDTO> criteriaQuery =
        CriteriaQueryBuilder.buildListQuery(BeschreibungListDTO.class, getEntityManager(), filterBy,
            sortByAsc);

    TypedQuery<BeschreibungListDTO> typedQuery = getEntityManager().createQuery(criteriaQuery);
    typedQuery.setFirstResult(first);
    typedQuery.setMaxResults(pageSize);

    return typedQuery.getResultList();
  }

  @Override
  public int countFilteredBeschreibungListDTOs(Map<String, String> filterBy) {
    Objects.requireNonNull(filterBy, "filterBy is required");

    CriteriaQuery<Long> criteriaQuery =
        CriteriaQueryBuilder.buildCountQuery(BeschreibungListDTO.class, getEntityManager(), filterBy);
    return Optional.ofNullable(getEntityManager().createQuery(criteriaQuery).getSingleResult())
        .map(Long::intValue)
        .orElse(0);
  }

  @Override
  public BeschreibungListDTO findBeschreibungListDTOById(String id) {
    return getEntityManager().find(BeschreibungListDTO.class, id);
  }

  @Override
  public List<BeschreibungListDTO> findLatestModified() {
    final String aenderungsdatum = "aenderungsdatum";

    CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<BeschreibungListDTO> criteriaQuery =
        criteriaBuilder.createQuery(BeschreibungListDTO.class);

    Root<BeschreibungListDTO> root = criteriaQuery.from(BeschreibungListDTO.class);

    criteriaQuery = criteriaQuery
        .select(root)
        .where(criteriaBuilder.isNotNull(root.get(aenderungsdatum)))
        .orderBy(criteriaBuilder.desc(root.get(aenderungsdatum)));

    TypedQuery<BeschreibungListDTO> typedQuery = getEntityManager().createQuery(criteriaQuery);
    typedQuery.setFirstResult(0);
    typedQuery.setMaxResults(10);

    return typedQuery.getResultList();
  }

}
