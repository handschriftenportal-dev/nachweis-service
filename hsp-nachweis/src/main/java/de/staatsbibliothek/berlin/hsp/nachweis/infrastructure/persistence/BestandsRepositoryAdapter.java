package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BestandNachweisDTO;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Digitalisat;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bestand.BestandsRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.Getter;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 24.04.23.
 * @version 1.0
 */
@Getter
@ApplicationScoped
public class BestandsRepositoryAdapter implements BestandsRepository {

  @Inject
  private EntityManager entityManager;

  @Transactional
  @Override
  public BestandNachweisDTO findAllBestaende() {
    entityManager.flush();
    Query nq =
        entityManager.createNativeQuery(
            "select * from (select count(1) anzahlKOD from kulturobjektdokument kods) as k, "
                + "(select count(1) anzahlBeschreibungen from beschreibungsdokument) as b, (select count(1) anzahlBeschreibungenIntern from beschreibungsdokument where verwaltungstyp = 'INTERN') as bi, "
                + "(select count(1) anzahlBeschreibungenExtern from beschreibungsdokument where verwaltungstyp = 'EXTERN') as be,(select count(1) anzahlBeschreibungenKatalog from beschreibungsdokument where dokumentobjekttyp = 'HSP_DESCRIPTION_RETRO') as bk,"
                + "(select count(1) anzahlDigitalisat from digitalisat) as d, "
                + "(select count(1) anzahlKatalog from katalog) as c",
            "BestandNachweisDTOMapping");

    return (BestandNachweisDTO) nq.getSingleResult();
  }

  @Transactional
  @Override
  public List<BestandInstitutionElementDTO> findAllKulturObjekteForInstitution() {
    entityManager.flush();
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<BestandInstitutionElementDTO> criteriaQuery =
        criteriaBuilder.createQuery(BestandInstitutionElementDTO.class);

    Root<KulturObjektDokument> kulturobjektdokumentRoot =
        criteriaQuery.from(KulturObjektDokument.class);
    Join<KulturObjektDokument, Identifikation> identifikationJoin =
        kulturobjektdokumentRoot.join("gueltigeIdentifikation", JoinType.INNER);
    Join<Identifikation, NormdatenReferenz> besitzerJoin =
        identifikationJoin.join("besitzer", JoinType.INNER);
    Join<Identifikation, NormdatenReferenz> aufbewahrungsortJoin =
        identifikationJoin.join("aufbewahrungsOrt", JoinType.INNER);

    criteriaQuery.multiselect(
        besitzerJoin.get("name").alias("besitzendeEinrichtung"),
        besitzerJoin.get("id").alias("besitzendeEinrichtungID"),
        aufbewahrungsortJoin.get("name").alias("aufbewahrungsOrt"),
        aufbewahrungsortJoin.get("id").alias("aufbewahrungsOrtID"),
        criteriaBuilder
            .count(kulturobjektdokumentRoot.get("id"))
            .as(Integer.class)
            .alias("bestandNachweis"));

    criteriaQuery.groupBy(
        besitzerJoin.get("name"),
        besitzerJoin.get("id"),
        aufbewahrungsortJoin.get("name"),
        aufbewahrungsortJoin.get("id"));
    criteriaQuery.orderBy(
        criteriaBuilder.desc(criteriaBuilder.count(kulturobjektdokumentRoot.get("id"))));

    TypedQuery<BestandInstitutionElementDTO> typedQuery = entityManager.createQuery(criteriaQuery);
    return typedQuery.getResultList();
  }

  @Transactional
  @Override
  public List<BestandInstitutionElementDTO> findAllBeschreibungenForInstitution() {
    entityManager.flush();
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<BestandInstitutionElementDTO> cq =
        cb.createQuery(BestandInstitutionElementDTO.class);
    Root<Beschreibung> bsRoot = cq.from(Beschreibung.class);

    Join<Beschreibung, Identifikation> iJoin = bsRoot.join("gueltigeIdentifikation", JoinType.LEFT);
    Join<Identifikation, NormdatenReferenz> aoJoin = iJoin.join("aufbewahrungsOrt", JoinType.LEFT);
    Join<Identifikation, NormdatenReferenz> owJoin = iJoin.join("besitzer", JoinType.LEFT);

    Predicate condition = cb.equal(iJoin.get("identTyp"), IdentTyp.GUELTIGE_SIGNATUR);
    cq.where(condition);

    cq.multiselect(
        owJoin.get("name").alias("besitzendeEinrichtung"),
        owJoin.get("id").alias("besitzendeEinrichtungID"),
        aoJoin.get("name").alias("aufbewahrungsOrt"),
        aoJoin.get("id").alias("aufbewahrungsOrtID"),
        cb.count(bsRoot.get("id")).as(Long.class).alias("bestandNachweis"));

    cq.groupBy(aoJoin.get("name"), aoJoin.get("id"), owJoin.get("name"), owJoin.get("id"));
    cq.orderBy(cb.desc(cb.count(bsRoot.get("id"))));

    TypedQuery<BestandInstitutionElementDTO> query = entityManager.createQuery(cq);
    return query.getResultList();
  }

  @Override
  @Transactional
  public List<BestandInstitutionElementDTO> findAllDigitalisateForKOD() {
    entityManager.flush();
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<BestandInstitutionElementDTO> criteriaQuery =
        criteriaBuilder.createQuery(BestandInstitutionElementDTO.class);

    Root<KulturObjektDokument> kulturobjektdokumentRoot =
        criteriaQuery.from(KulturObjektDokument.class);
    Join<KulturObjektDokument, Digitalisat> digitalisatJoin =
        kulturobjektdokumentRoot.join("digitalisate", JoinType.INNER);
    Join<KulturObjektDokument, Identifikation> identifikationJoin =
        kulturobjektdokumentRoot.join("gueltigeIdentifikation", JoinType.INNER);
    Join<Identifikation, NormdatenReferenz> besitzerJoin =
        identifikationJoin.join("besitzer", JoinType.INNER);
    Join<Identifikation, NormdatenReferenz> aufbewahrungsortJoin =
        identifikationJoin.join("aufbewahrungsOrt", JoinType.INNER);

    criteriaQuery.multiselect(
        besitzerJoin.get("name").alias("besitzendeEinrichtung"),
        besitzerJoin.get("id").alias("besitzendeEinrichtungID"),
        aufbewahrungsortJoin.get("name").alias("aufbewahrungsOrt"),
        aufbewahrungsortJoin.get("id").alias("aufbewahrungsOrtID"),
        criteriaBuilder.count(digitalisatJoin).as(Integer.class).alias("bestandNachweis"));

    criteriaQuery.groupBy(
        besitzerJoin.get("name"),
        besitzerJoin.get("id"),
        aufbewahrungsortJoin.get("name"),
        aufbewahrungsortJoin.get("id"));
    criteriaQuery.orderBy(criteriaBuilder.desc(criteriaBuilder.count(digitalisatJoin)));

    TypedQuery<BestandInstitutionElementDTO> typedQuery = entityManager.createQuery(criteriaQuery);
    return typedQuery.getResultList();
  }
}
