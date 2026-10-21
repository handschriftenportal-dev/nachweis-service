package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument_;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation_;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.NormdatenReferenzRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.Objects;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 03.12.20
 */
@ApplicationScoped
public class NormdatenReferenzRepositoryAdapter implements NormdatenReferenzRepository {

  @Override
  public NormdatenReferenz findByName(String name) {
    return find("name", name).range(0, 1).firstResult();
  }

  @Override
  public NormdatenReferenz findByGndID(String gndID) {
    return find("gndID", gndID).range(0, 1).firstResult();
  }

  @Override
  public NormdatenReferenz findByIdOrIdentifikator(String id) {
    return find(
        "select distinct nr from NormdatenReferenz nr"
            + " join nr.identifikator i"
            + " where nr.id = :id"
            + " or i.text = :id",
        Parameters.with("id", id).map())
        .firstResult();
  }

  @Override
  public NormdatenReferenz findByIdOrName(String idOrName, String type) throws Exception {
    if (Objects.isNull(idOrName) || idOrName.isEmpty()) {
      throw new Exception("Missing or empty parameter idOrName!");
    }
    if (Objects.isNull(type) || type.isEmpty()) {
      throw new Exception("Missing or empty parameter type!");
    }

    PanacheQuery<NormdatenReferenz> query =
        find(
            "select distinct nr from NormdatenReferenz nr"
                + " left join nr.identifikator i "
                + " left join nr.varianterName vn "
                + " where nr.typeName = :type"
                + " and (nr.id = :idOrName"
                + " or nr.gndID = :idOrName"
                + " or nr.name = :idOrName"
                + " or i.text = :idOrName"
                + " or vn.name = :idOrName)",
            Parameters.with("idOrName", idOrName).and("type", type).map());
    try {
      return query.singleResult();
    } catch (NoResultException nre) {
      return null;
    }
  }

  @Override
  public List<NormdatenReferenz> findReferencedCorporateBodies() {
    EntityManager em = getEntityManager();
    CriteriaBuilder cb = em.getCriteriaBuilder();

    CriteriaQuery<NormdatenReferenz> query = cb.createQuery(NormdatenReferenz.class);

    Root<KulturObjektDokument> kodRoot = query.from(KulturObjektDokument.class);

    Join<KulturObjektDokument, Identifikation> identifikationJoin =
        kodRoot.join(KulturObjektDokument_.gueltigeIdentifikation);
    Join<Identifikation, NormdatenReferenz> normdatenReferenzJoin =
        identifikationJoin.join(Identifikation_.BESITZER);

    query.select(normdatenReferenzJoin).distinct(true);

    TypedQuery<NormdatenReferenz> tQuery = em.createQuery(query);
    return tQuery.getResultList();
  }
}
