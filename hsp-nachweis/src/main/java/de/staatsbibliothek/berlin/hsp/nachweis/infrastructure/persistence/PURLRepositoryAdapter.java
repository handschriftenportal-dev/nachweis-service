package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLViewModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class PURLRepositoryAdapter implements PURLRepository {

  private static final String PARAM_DOKUMENT_ID = "dokument_id";
  private static final String PARAM_DOKUMENT_OBJEKT_TYP = "dokument_objekt_typ";
  private static final String PARAM_PURL_ID = "purl_id";
  private static final String PARAM_PURL_PURL = "purl_purl";
  private static final String PARAM_PURL_TARGET = "purl_target";
  private static final String PARAM_PURL_TYP = "purl_typ";

  private static final String QUERY_ALL_VIEW_MODELS = """
      SELECT k.id AS %1$s, 'HSP_OBJECT' AS %2$s, p.id AS %3$s, p.purl AS %4$s, p.target AS %5$s, p.typ AS %6$s
      FROM KulturObjektDokument k
      LEFT JOIN purl p ON k.id = p.kulturobjektdokument_id
      UNION ALL
      SELECT b.id AS %1$s, b.dokumentobjekttyp AS %2$s, p.id AS %3$s, p.purl AS %4$s, p.target AS %5$s, p.typ AS %6$s
      FROM beschreibungsdokument b
      LEFT JOIN publikation pub ON b.publikation_id = pub.id
      LEFT JOIN purl p ON pub.id = p.publikation_id
      """.formatted(
      PARAM_DOKUMENT_ID, PARAM_DOKUMENT_OBJEKT_TYP, PARAM_PURL_ID,
      PARAM_PURL_PURL, PARAM_PURL_TARGET, PARAM_PURL_TYP);

  private static final String QUERY_BY_DOKUMENT_ID_AND_PURL_TYP = """
      SELECT p.* FROM purl p
      LEFT JOIN beschreibungsdokument b ON b.publikation_id = p.publikation_id
      WHERE (p.kulturobjektdokument_id = :dokumentId OR b.id = :dokumentId)
      AND p.typ = :purlTyp
      """;

  @Override
  public List<PURLViewModel> findAllAsViewModels() {
    Query query = getEntityManager().createNativeQuery(QUERY_ALL_VIEW_MODELS, Tuple.class);
    List<Tuple> resultList = query.getResultList();

    Map<String, PURLViewModel> viewModelsMap = new HashMap<>();

    for (Tuple tuple : resultList) {
      PURLRecord row = new PURLRecord(tuple);

      String key = row.dokumentId() + row.dokumentObjektTyp();
      PURLViewModel purlViewModel = viewModelsMap.computeIfAbsent(key,
          k -> new PURLViewModel(row.dokumentId(), row.dokumentObjektTyp(), new HashSet<>()));

      Optional<PURL> purl = row.toPURL();
      purl.filter(PURL::isInternal).ifPresent(purlViewModel.getInternalPURLs()::add);
      purl.filter(PURL::isExternal).ifPresent(purlViewModel.getExternalPURLs()::add);
    }
    return new ArrayList<>(viewModelsMap.values());
  }

  public Optional<PURL> findByPurlOptional(URI purl) {
    Objects.requireNonNull(purl, "purl is required");
    return find("purl", purl.toASCIIString()).singleResultOptional();
  }

  public Set<PURL> findByDokumentIdAndPURLTyp(String dokumentId, PURLTyp purlTyp) {
    Objects.requireNonNull(dokumentId, "dokumentId is required");
    Objects.requireNonNull(purlTyp, "purlTyp is required");

    Query nativeQuery = getEntityManager().createNativeQuery(QUERY_BY_DOKUMENT_ID_AND_PURL_TYP,
        PURL.class);
    nativeQuery.setParameter("dokumentId", dokumentId);
    nativeQuery.setParameter("purlTyp", purlTyp.toString());

    return new HashSet<>(nativeQuery.getResultList());
  }

  private record PURLRecord(String dokumentId, DokumentObjektTyp dokumentObjektTyp,
                            String purlId, String purl,
                            String target, String purlTyp) {

    PURLRecord(Tuple tuple) {
      this(
          tuple.get(PARAM_DOKUMENT_ID, String.class),
          DokumentObjektTyp.valueOf(tuple.get(PARAM_DOKUMENT_OBJEKT_TYP, String.class)),
          tuple.get(PARAM_PURL_ID, String.class),
          tuple.get(PARAM_PURL_PURL, String.class),
          tuple.get(PARAM_PURL_TARGET, String.class),
          tuple.get(PARAM_PURL_TYP, String.class));
    }

    Optional<PURL> toPURL() {
      if (Objects.isNull(purlId)) {
        return Optional.empty();
      }
      return Optional.of(new PURL(
          purlId,
          URI.create(purl),
          URI.create(target),
          PURLTyp.getPURLTypFromString(purlTyp))
      );
    }
  }
}
