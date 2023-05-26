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

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLRepository;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.purl.PURLViewModel;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.Query;
import javax.persistence.Tuple;

@ApplicationScoped
public class PURLRepositoryAdapter implements PURLRepository {

  private static final String PARAM_DOKUMENT_ID = "dokument_id";
  private static final String PARAM_DOKUMENT_OBJEKT_TYP = "dokument_objekt_typ";
  private static final String PARAM_PURL_ID = "purl_id";
  private static final String PARAM_PURL_PURL = "purl_purl";
  private static final String PARAM_PURL_TARGET = "purl_target";
  private static final String PARAM_PURL_TYP = "purl_typ";

  private static final String QUERY_ALL_VIEW_MODELS =
      "SELECT * FROM ((SELECT k.id AS " + PARAM_DOKUMENT_ID
          + ", 'HSP_OBJECT' AS " + PARAM_DOKUMENT_OBJEKT_TYP
          + ", p.id AS " + PARAM_PURL_ID
          + ", p.purl AS " + PARAM_PURL_PURL
          + ", p.target AS " + PARAM_PURL_TARGET
          + ", p.typ AS " + PARAM_PURL_TYP
          + " FROM KulturObjektDokument k"
          + " LEFT JOIN purl p on k.id = p.kulturobjektdokument_id)"
          + " UNION ALL"
          + " (SELECT b.id AS " + PARAM_DOKUMENT_ID
          + ", b.dokumentobjekttyp AS " + PARAM_DOKUMENT_OBJEKT_TYP
          + ", p.id AS " + PARAM_PURL_ID
          + ", p.purl AS " + PARAM_PURL_PURL
          + ", p.target AS " + PARAM_PURL_TARGET
          + ", p.typ AS " + PARAM_PURL_TYP
          + " FROM beschreibungsdokument b"
          + " LEFT JOIN purl p on b.id = p.beschreibungsdokument_id)) as document_purls"
          + " ORDER BY document_purls." + PARAM_DOKUMENT_ID;

  private static final String QUERY_BY_DOKUMENT_ID_AND_PURL_TYP =
      "SELECT * FROM purl p WHERE"
          + " (p.kulturobjektdokument_id=:dokumentId or p.beschreibungsdokument_id=:dokumentId)"
          + " AND p.typ=:purlTyp";

  @Override
  public Set<PURLViewModel> findAllAsViewModels() {
    Query query = getEntityManager().createNativeQuery(QUERY_ALL_VIEW_MODELS, Tuple.class);
    List<Tuple> result = query.getResultList();

    Map<String, PURLViewModel> viewModelsMap = new HashMap<>();

    for (Tuple tuple : result) {
      String dokumentId = tuple.get(PARAM_DOKUMENT_ID, String.class);
      DokumentObjektTyp dokumentObjektTyp = DokumentObjektTyp.valueOf(
          tuple.get(PARAM_DOKUMENT_OBJEKT_TYP, String.class));
      Optional<PURL> purl = Optional.empty();
      Optional<String> purlId = Optional.ofNullable(tuple.get(PARAM_PURL_ID, String.class));

      if (purlId.isPresent()) {
        purl = Optional.of(new PURL(purlId.get(),
            URI.create(tuple.get(PARAM_PURL_PURL, String.class)),
            URI.create(tuple.get(PARAM_PURL_TARGET, String.class)),
            PURLTyp.getPURLTypFromString(tuple.get(PARAM_PURL_TYP, String.class))));
      }

      final String key = dokumentId + dokumentObjektTyp;
      viewModelsMap.computeIfAbsent(key, k -> new PURLViewModel(dokumentId, dokumentObjektTyp, new HashSet<>()));
      final PURLViewModel purlViewModel = viewModelsMap.get(key);
      purl.filter(p -> PURLTyp.INTERNAL == p.getTyp()).ifPresent(purlViewModel.getInternalPURLs()::add);
      purl.filter(p -> PURLTyp.EXTERNAL == p.getTyp()).ifPresent(purlViewModel.getExternalPURLs()::add);
    }
    return new HashSet<>(viewModelsMap.values());
  }

  public Optional<PURL> findByPurlOptional(URI purl) {
    Objects.requireNonNull(purl, "purl is required");
    return find("purl", purl.toASCIIString()).singleResultOptional();
  }

  public Set<PURL> findByDokumentIdAndPURLTyp(String dokumentId, PURLTyp purlTyp) {
    Objects.requireNonNull(dokumentId, "dokumentId is required");
    Objects.requireNonNull(purlTyp, "purlTyp is required");

    Query nativeQuery = getEntityManager().createNativeQuery(QUERY_BY_DOKUMENT_ID_AND_PURL_TYP, PURL.class);
    nativeQuery.setParameter("dokumentId", dokumentId);
    nativeQuery.setParameter("purlTyp", purlTyp.toString());

    return new HashSet<>(nativeQuery.getResultList());
  }

}
