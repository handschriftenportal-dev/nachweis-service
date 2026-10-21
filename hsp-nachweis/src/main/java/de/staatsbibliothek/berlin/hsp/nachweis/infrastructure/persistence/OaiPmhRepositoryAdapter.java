package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument;
import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.KulturObjektDokument_;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation_;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz_;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class OaiPmhRepositoryAdapter implements CRUDRepository<KulturObjektDokument, String> {

  public List<KulturObjektDokument> findFilteredKulturObjektDokumente(
      Instant from, Instant until, String setSpec, int maxResponseLength, int offset) {
    return findFilteredKulturObjektDokumente(
        Optional.ofNullable(from),
        Optional.ofNullable(until),
        Optional.ofNullable(setSpec),
        maxResponseLength,
        offset);
  }

  public List<KulturObjektDokument> findFilteredKulturObjektDokumente(
      Optional<Instant> from,
      Optional<Instant> until,
      Optional<String> setSpec,
      int maxResponseLength,
      int offset) {

    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();

    CriteriaQuery<KulturObjektDokument> query = cb.createQuery(KulturObjektDokument.class);
    Root<KulturObjektDokument> kodRoot = query.from(KulturObjektDokument.class);
    Join<KulturObjektDokument, Identifikation> identifikationJoin =
        kodRoot.join(KulturObjektDokument_.GUELTIGE_IDENTIFIKATION);
    Join<Identifikation, NormdatenReferenz> normdatenReferenzJoin =
        identifikationJoin.join(Identifikation_.BESITZER);

    var predicatesArray =
        getPredicatesArray(from, until, setSpec, cb, kodRoot, normdatenReferenzJoin);

    var selectQuery = query.select(kodRoot);
    if (predicatesArray.length > 0) {
      selectQuery.where(predicatesArray);
    }

    selectQuery.orderBy(
        cb.desc(kodRoot.get(KulturObjektDokument_.REGISTRIERUNGS_DATUM)),
        cb.desc(kodRoot.get(KulturObjektDokument_.ID)));

    TypedQuery<KulturObjektDokument> tQuery = getEntityManager().createQuery(query);
    tQuery.setFirstResult(offset);
    tQuery.setMaxResults(maxResponseLength);
    return tQuery.getResultList();
  }

  public long countFilteredKulturObjektDokumente(Instant from, Instant until, String setSpec) {
    return countFilteredKulturObjektDokumente(
        Optional.ofNullable(from), Optional.ofNullable(until), Optional.ofNullable(setSpec));
  }

  public long countFilteredKulturObjektDokumente(
      Optional<Instant> from, Optional<Instant> until, Optional<String> setSpec) {

    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();

    CriteriaQuery<Long> query = cb.createQuery(Long.class);
    Root<KulturObjektDokument> kodRoot = query.from(KulturObjektDokument.class);
    Join<KulturObjektDokument, Identifikation> identifikationJoin =
        kodRoot.join(KulturObjektDokument_.gueltigeIdentifikation);
    Join<Identifikation, NormdatenReferenz> normdatenReferenzJoin =
        identifikationJoin.join(Identifikation_.BESITZER);

    var predicatesArray =
        getPredicatesArray(from, until, setSpec, cb, kodRoot, normdatenReferenzJoin);

    var selectQuery = query.select(cb.count(normdatenReferenzJoin));
    selectQuery.where(predicatesArray);

    return getEntityManager().createQuery(selectQuery).getSingleResult();
  }

  Predicate[] getPredicatesArray(
      Optional<Instant> from,
      Optional<Instant> until,
      Optional<String> setSpec,
      CriteriaBuilder cb,
      Root<KulturObjektDokument> kodRoot,
      Join<Identifikation, NormdatenReferenz> normdatenReferenzJoin) {

    List<Predicate> predicates = new ArrayList<>();
    from.ifPresent(
        f ->
            predicates.add(
                CriteriaUtils.greaterThanOrEqualTo(
                    cb, kodRoot.get(KulturObjektDokument_.REGISTRIERUNGS_DATUM), f)));
    until.ifPresent(
        u ->
            predicates.add(
                CriteriaUtils.lessThanOrEqualTo(
                    cb, kodRoot.get(KulturObjektDokument_.REGISTRIERUNGS_DATUM), u)));
    setSpec
        .filter(s -> !s.isBlank())
        .ifPresent(
            s -> predicates.add(cb.equal(normdatenReferenzJoin.get(NormdatenReferenz_.ID), s)));

    return predicates.toArray(new Predicate[0]);
  }
}
