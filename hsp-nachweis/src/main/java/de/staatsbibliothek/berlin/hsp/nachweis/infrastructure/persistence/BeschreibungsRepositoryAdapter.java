package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ApprovalStatus;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationStatus;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.VerwaltungsTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.BeschreibungXmlDTO;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.XMLDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungInReviewDbView;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungInReviewDbView_;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungListDbView;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungRejectedDbView;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.jpa.HibernateHints;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 09.11.20
 */
@ApplicationScoped
@Slf4j
public class BeschreibungsRepositoryAdapter implements BeschreibungsRepository {

  private static final String BEARBEITER_COL = BeschreibungInReviewDbView_.BEARBEITER;

  @Override
  public List<Beschreibung> findAllById(Collection<String> ids) {
    return list("id in ?1", ids);
  }

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
  public Optional<Beschreibung> findByPublikation(Publikation publikation) {
    if (publikation == null) {
      return Optional.empty();
    }
    return find("publikation", publikation).firstResultOptional();
  }

  @Override
  public List<BeschreibungListDbView> findFilteredBeschreibungListDTOs(int first, int pageSize,
      Map<String, String> filterBy, Map<String, Boolean> sortByAsc) {
    Objects.requireNonNull(filterBy, "filterBy is required");
    Objects.requireNonNull(sortByAsc, "sortByAsc is required");

    CriteriaQuery<BeschreibungListDbView> criteriaQuery =
        CriteriaQueryBuilder.buildListQuery(BeschreibungListDbView.class, getEntityManager(),
            filterBy,
            sortByAsc);

    TypedQuery<BeschreibungListDbView> typedQuery = getEntityManager().createQuery(criteriaQuery);
    typedQuery.setFirstResult(first);
    typedQuery.setMaxResults(pageSize);

    return typedQuery.getResultList();
  }

  @Override
  public int countFilteredBeschreibungListDTOs(Map<String, String> filterBy) {
    Objects.requireNonNull(filterBy, "filterBy is required");

    CriteriaQuery<Long> criteriaQuery =
        CriteriaQueryBuilder.buildCountQuery(BeschreibungListDbView.class, getEntityManager(),
            filterBy);
    return Optional.ofNullable(getEntityManager().createQuery(criteriaQuery).getSingleResult())
        .map(Long::intValue)
        .orElse(0);
  }

  @Override
  public BeschreibungListDbView findBeschreibungListDTOById(String id) {
    return getEntityManager().find(BeschreibungListDbView.class, id);
  }

  @Override
  public List<BeschreibungListDbView> findLatestModifiedByBearbeiter(int maxResults,
      String bearbeiterFilter) {
    final String aenderungsdatum = "aenderungsdatum";

    CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<BeschreibungListDbView> criteriaQuery =
        criteriaBuilder.createQuery(BeschreibungListDbView.class);

    Root<BeschreibungListDbView> root = criteriaQuery.from(BeschreibungListDbView.class);

    Predicate anderungsDatumNotNull = criteriaBuilder.isNotNull(root.get(aenderungsdatum));
    Predicate bearbeiterEquals = criteriaBuilder.like(root.get(BEARBEITER_COL),
        "%" + bearbeiterFilter + "%");

    Predicate finalPredicate = (bearbeiterFilter == null || bearbeiterFilter.isBlank())
        ? anderungsDatumNotNull
        : criteriaBuilder.and(anderungsDatumNotNull, bearbeiterEquals);

    criteriaQuery = criteriaQuery
        .select(root)
        .where(finalPredicate)
        .orderBy(criteriaBuilder.desc(root.get(aenderungsdatum)));

    TypedQuery<BeschreibungListDbView> typedQuery = getEntityManager().createQuery(criteriaQuery);
    typedQuery.setFirstResult(0);
    typedQuery.setMaxResults(maxResults);

    return typedQuery.getResultList();
  }

  @Override
  public List<Beschreibung> findAllBeschreibungWithActiveApprovalRequest() {
    return find("""
            SELECT b
            FROM Beschreibung b
            JOIN Publikation p ON b.publikation = p
            JOIN p.approvalRequests ar
            WHERE ar.status = ?1 OR ar.status = ?2
            ORDER BY ar.requestedAt DESC
            """,
        ApprovalStatus.REQUESTED,
        ApprovalStatus.IN_REVIEW)
        .stream()
        .toList();
  }


  /**
   * @return the latest 10 modified BeschreibungListDTOs
   */
  @Override
  public List<BeschreibungListDbView> findLatestModified() {
    return findLatestModified(10);
  }

  @Override
  public List<BeschreibungListDbView> findLatestModified(int maxResults) {
    return findLatestModifiedByBearbeiter(maxResults, null);
  }

  @Override
  public List<BeschreibungListDbView> findPublikationenByBearbeiter(String bearbeiter) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<BeschreibungListDbView> cq = cb.createQuery(BeschreibungListDbView.class);
    Root<BeschreibungListDbView> root = cq.from(BeschreibungListDbView.class);

    Predicate published = cb.equal(root.get("publikationStatus"), PublikationStatus.PUBLISHED);
    Predicate bearbeiterLike = cb.like(root.get(BEARBEITER_COL), "%" + bearbeiter + "%");

    cq.select(root)
        .where(cb.and(published, bearbeiterLike))
        .orderBy(cb.desc(root.get("publikationsDatumHsp")));

    return getEntityManager().createQuery(cq).getResultList();
  }

  @Override
  public List<BeschreibungListDbView> findLatestPublikationen(int maxResults) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<BeschreibungListDbView> cq = cb.createQuery(BeschreibungListDbView.class);
    Root<BeschreibungListDbView> root = cq.from(BeschreibungListDbView.class);

    Predicate published = cb.equal(root.get("publikationStatus"), PublikationStatus.PUBLISHED);
    Predicate intern = cb.equal(root.get("verwaltungsTyp"), VerwaltungsTyp.INTERN.name());

    cq.select(root)
        .where(cb.and(published, intern))
        .orderBy(cb.desc(root.get("publikationsDatumHsp")));

    TypedQuery<BeschreibungListDbView> typedQuery = getEntityManager().createQuery(cq);
    typedQuery.setMaxResults(maxResults);

    return typedQuery.getResultList();
  }

  @Override
  public List<BeschreibungInReviewDbView> findInReviewByBearbeiter(String bearbeiter) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<BeschreibungInReviewDbView> cq = cb.createQuery(BeschreibungInReviewDbView.class);
    Root<BeschreibungInReviewDbView> root = cq.from(BeschreibungInReviewDbView.class);

    cq.select(root)
        .where(cb.like(root.get(BEARBEITER_COL), "%" + bearbeiter + "%"))
        .orderBy(cb.desc(root.get("requestedAt")));

    return getEntityManager().createQuery(cq).getResultList();
  }

  @Override
  public List<BeschreibungRejectedDbView> findRejectedByBearbeiter(String bearbeiter) {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<BeschreibungRejectedDbView> cq = cb.createQuery(BeschreibungRejectedDbView.class);
    Root<BeschreibungRejectedDbView> root = cq.from(BeschreibungRejectedDbView.class);

    cq.select(root)
        .where(cb.like(root.get(BEARBEITER_COL), "%" + bearbeiter + "%"))
        .orderBy(cb.desc(root.get("reviewedAt")));

    return getEntityManager().createQuery(cq).getResultList();
  }

  @Override
  public Stream<? extends XMLDokument> selectAllTeiXml() {
    EntityManager entityManager = getEntityManager();
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    CriteriaQuery<BeschreibungXmlDTO> query = cb.createQuery(BeschreibungXmlDTO.class);
    Root<BeschreibungXmlDTO> root = query.from(BeschreibungXmlDTO.class);

    query.select(root);

    TypedQuery<BeschreibungXmlDTO> typedQuery = entityManager.createQuery(query);
    typedQuery.setHint(HibernateHints.HINT_FETCH_SIZE, 5);

    return typedQuery.getResultStream();
  }


}
