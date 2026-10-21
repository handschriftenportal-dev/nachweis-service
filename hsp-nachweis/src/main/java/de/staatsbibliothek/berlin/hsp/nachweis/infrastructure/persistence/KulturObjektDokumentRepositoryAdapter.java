package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.KodBeschreibungJoin;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.KodXmlDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.XMLDokument;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.jpa.QueryHints;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 09.11.20
 */
@ApplicationScoped
@Slf4j
public class KulturObjektDokumentRepositoryAdapter implements KulturObjektDokumentRepository {

  public static final String QUERY_KODIDS_BESCHREIBUNGSIDS =
      "select k from " + "KodBeschreibungJoin k";

  @Override
  public Map<String, List<String>> getAllBeschreibungsIdsWithKodIDs() {
    TypedQuery<KodBeschreibungJoin> nativeQuery =
        getEntityManager().createQuery(QUERY_KODIDS_BESCHREIBUNGSIDS, KodBeschreibungJoin.class);
    List<KodBeschreibungJoin> kodBeschreibungJoins = nativeQuery.getResultList();

    if (kodBeschreibungJoins.isEmpty()) {
      return Collections.emptyMap();
    }

    Map<String, List<String>> result = new HashMap<>(kodBeschreibungJoins.size());

    kodBeschreibungJoins.forEach(
        element -> {
          String kulturobjektdokumentId = element.getKulturobjektdokumentId();
          String beschreibungId = element.getBeschreibungId();

          result.computeIfAbsent(kulturobjektdokumentId, k -> new ArrayList<>());
          result.computeIfPresent(
              kulturobjektdokumentId,
              (k, v) -> {
                v.add(beschreibungId);
                return v;
              });
        });

    return result;
  }

  @Override
  public KulturObjektDokument findByBeschreibung(String beschreibungId) {
    return find(
        "select distinct k from KulturObjektDokument k "
            + " join fetch k.beschreibungenIDs b"
            + " join k.beschreibungenIDs bsq"
            + " where bsq = ?1",
        beschreibungId)
        .firstResult();
  }

  @Override
  public List<KulturObjektDokument> findByIdentifikationIdent(String ident) {
    return find(
        "from KulturObjektDokument k "
            + " join fetch k.gueltigeIdentifikation gi"
            + " where gi.ident=?1",
        ident)
        .list();
  }

  @Override
  public Optional<KulturObjektDokument> findByPurl(String purl) {
    return find(
        "select distinct k from KulturObjektDokument k"
            + " join k.purls p"
            + " where p.purl=?1",
        purl)
        .singleResultOptional();
  }

  public List<KulturObjektDokument> findByIdentifikationIdentAndBesitzerIDAndAufbewahrungsortID(
      String ident, String besitzerID, String aufbewahrungsOrtID) {
    return find(
        "from KulturObjektDokument k "
            + " join fetch k.gueltigeIdentifikation gi"
            + " join fetch gi.besitzer bi"
            + " join fetch gi.aufbewahrungsOrt ao"
            + " where gi.ident=?1 and bi.id=?2 and ao.id=?3",
        ident,
        besitzerID,
        aufbewahrungsOrtID)
        .list();
  }

  public Optional<KulturObjektDokument>
  findByIdentifikationIdentAndBesitzerNameAndAufbewahrungsortName(
      String ident, String besitzerName, String aufbewahrungsOrtName) {
    return find(
        "select distinct k from KulturObjektDokument k"
            + " join k.gueltigeIdentifikation gi "
            + " join gi.besitzer nrb "
            + " join gi.aufbewahrungsOrt nra "
            + " left join nrb.varianterName vnrb"
            + " left join nra.varianterName vnra"
            + " where gi.ident=?1 and (nrb.name=?2 or vnrb.name=?2) and (nra.name=?3 or vnra.name=?3)",
        ident,
        besitzerName,
        aufbewahrungsOrtName)
        .singleResultOptional();
  }

  public Optional<KulturObjektDokument>
  findByAlternativeIdentifikationIdentAndBesitzerNameAndAufbewahrungsortName(
      String ident, String besitzerName, String aufbewahrungsOrtName) {
    return find(
        "select distinct k from KulturObjektDokument k"
            + " join fetch k.alternativeIdentifikationen "
            + " join k.alternativeIdentifikationen ai "
            + " join ai.besitzer nrb "
            + " left join nrb.varianterName vnrb"
            + " join ai.aufbewahrungsOrt nra "
            + " left join nra.varianterName vnra "
            + " where ai.ident=?1 and (nrb.name=?2 or vnrb.name=?2) and (nra.name=?3 or vnra.name=?3)",
        ident,
        besitzerName,
        aufbewahrungsOrtName)
        .singleResultOptional();
  }

  @Override
  public List<KulturObjektDokument> findByAlternativeIdentifikationIdent(String ident) {
    return find(
        "select distinct k from KulturObjektDokument k "
            + " join fetch k.gueltigeIdentifikation"
            + " join fetch k.alternativeIdentifikationen"
            + " join k.alternativeIdentifikationen ai "
            + " where ai.ident=?1",
        ident)
        .list();
  }

  @Override
  public Optional<KulturObjektDokument> findByIdentifikationBeteiligeKoerperschaftName(
      String name) {
    return find(
        "select distinct k from KulturObjektDokument k "
            + " join fetch k.gueltigeIdentifikation "
            + " join fetch k.alternativeIdentifikationen "
            + " left join k.alternativeIdentifikationen ai "
            + " left join k.gueltigeIdentifikation gi "
            + " left join gi.besitzer bgi "
            + " left join ai.besitzer bai "
            + "where bgi.name=?1 or bai.name=?2",
        name,
        name)
        .singleResultOptional();
  }

  @Override
  public List<KulturObjektDokumentListDTO> findFilteredKulturObjektDokumentListDTOs(
      int first, int pageSize, Map<String, String> filterBy, Map<String, Boolean> sortByAsc) {
    Objects.requireNonNull(filterBy, "filterBy is required");
    Objects.requireNonNull(sortByAsc, "sortByAsc is required");

    CriteriaQuery<KulturObjektDokumentListDTO> criteriaQuery =
        CriteriaQueryBuilder.buildListQuery(
            KulturObjektDokumentListDTO.class, getEntityManager(), filterBy, sortByAsc);

    TypedQuery<KulturObjektDokumentListDTO> typedQuery =
        getEntityManager().createQuery(criteriaQuery);
    typedQuery.setFirstResult(first);
    typedQuery.setMaxResults(pageSize);

    return typedQuery.getResultList();
  }

  @Override
  public int countFilteredKulturObjektDokumentListDTOs(Map<String, String> filterBy) {
    Objects.requireNonNull(filterBy, "filterBy is required");

    CriteriaQuery<Long> criteriaQuery =
        CriteriaQueryBuilder.buildCountQuery(
            KulturObjektDokumentListDTO.class, getEntityManager(), filterBy);
    return Optional.ofNullable(getEntityManager().createQuery(criteriaQuery).getSingleResult())
        .map(Long::intValue)
        .orElse(0);
  }

  @Override
  public KulturObjektDokumentListDTO findKulturObjektDokumentListDTOById(String id) {
    return getEntityManager().find(KulturObjektDokumentListDTO.class, id);
  }

  @Override
  public Stream<? extends XMLDokument> selectAllTeiXml() {
    EntityManager entityManager = getEntityManager();
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    CriteriaQuery<KodXmlDTO> query = cb.createQuery(KodXmlDTO.class);
    Root<KodXmlDTO> root = query.from(KodXmlDTO.class);
    query.select(root);

    TypedQuery<KodXmlDTO> typedQuery = entityManager.createQuery(query);
    typedQuery.setHint(QueryHints.HINT_FETCH_SIZE, 5);

    return typedQuery.getResultStream();
  }
}
