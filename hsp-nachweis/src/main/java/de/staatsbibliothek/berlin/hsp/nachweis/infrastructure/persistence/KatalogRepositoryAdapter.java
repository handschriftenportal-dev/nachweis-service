package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Katalog;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KatalogBeschreibungReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.katalog.KatalogRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.XMLDokument;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.datensicherung.KatalogXmlDTO;
import java.util.Optional;
import java.util.stream.Stream;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 21.02.2022.
 * @version 1.0
 */

@Slf4j
@ApplicationScoped
public class KatalogRepositoryAdapter implements KatalogRepository {

  public static final String ATTRIBUTE_HSK_ID = "hskID";
  public static final String ATTRIBUTE_BESCHREIBUNGS_ID = "beschreibungsID";

  @Override
  public Optional<Katalog> findByHskId(String hskId) {
    CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<Katalog> query = criteriaBuilder.createQuery(Katalog.class);

    Root<Katalog> root = query.from(Katalog.class);
    try {

      query.where(criteriaBuilder.equal(root.get(ATTRIBUTE_HSK_ID), hskId));

      return Optional.of(getEntityManager().createQuery(query).getSingleResult());

    } catch (PersistenceException error) {
      log.error("Error during find Katalog by HSK_ID {}", error.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public Optional<KatalogBeschreibungReferenz> findKatalogBeschreibungReferenzByBeschreibungsID(
      String beschreibungsID) {
    CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<KatalogBeschreibungReferenz> query = criteriaBuilder.createQuery(
        KatalogBeschreibungReferenz.class);

    Root<KatalogBeschreibungReferenz> root = query.from(KatalogBeschreibungReferenz.class);
    try {

      query.where(criteriaBuilder.equal(root.get(ATTRIBUTE_BESCHREIBUNGS_ID), beschreibungsID));

      return Optional.of(getEntityManager().createQuery(query).getSingleResult());

    } catch (PersistenceException error) {
      log.error("Error during find KatalogBeschreibungReferenz by Beschreibung_ID {}",
          error.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public Stream<? extends XMLDokument> selectAllTeiXml() {
    EntityManager entityManager = getEntityManager();
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();

    CriteriaQuery<KatalogXmlDTO> query = cb.createQuery(KatalogXmlDTO.class);
    Root<KatalogXmlDTO> root = query.from(KatalogXmlDTO.class);
    query.select(root);

    TypedQuery<KatalogXmlDTO> typedQuery = entityManager.createQuery(query);

    return typedQuery.getResultStream();
  }
}
