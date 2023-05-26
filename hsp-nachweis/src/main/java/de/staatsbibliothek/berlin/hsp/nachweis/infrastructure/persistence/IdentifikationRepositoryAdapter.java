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

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.SignatureValue;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten.IdentifikationRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;

/**
 * Identifikation CRUD Repository
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 06.08.21
 */
@ApplicationScoped
@Slf4j
public class IdentifikationRepositoryAdapter implements IdentifikationRepository {

  public static final String PARAM_AGGREGATE_ID = "aggregateId";
  public static final String PARAM_SIGNATURE = "signature";
  public static final String PARAM_AUFBEWAHRUNGS_ORT = "aufbewahrungsOrt";
  public static final String PARAM_BESITZER = "besitzer";

  public static final String QUERY_SINGLE_BESCHREIBUNG_SIGNATURE = "select bs.id as " + PARAM_AGGREGATE_ID
      + ", i.ident as " + PARAM_SIGNATURE
      + ", ao.name as " + PARAM_AUFBEWAHRUNGS_ORT
      + ", ow.name as " + PARAM_BESITZER + "  from beschreibungsdokument bs"
      + " join beschreibungsdokument_globalebeschreibungskomponente bg"
      + " on bs.id = :" + PARAM_AGGREGATE_ID
      + " and bs.id = bg.beschreibung_id"
      + " join globalebeschreibungskomponente g"
      + " on bg.beschreibungsstruktur_id = g.id"
      + " join beschreibungskomponentekopf k"
      + " on g.id = k.id"
      + " join globalebeschreibungskomponente_identifikation gi"
      + " on g.id = gi.globalebeschreibungskomponente_id"
      + " join identifikation i "
      + " on gi.identifikationen_id = i.id"
      + " AND i.identtyp = '" + IdentTyp.GUELTIGE_SIGNATUR + '\''
      + " left join normdatenreferenz ao"
      + " on ao.id = i.aufbewahrungsort_id"
      + " left join normdatenreferenz ow"
      + " on ow.id = i.besitzer_id";

  public static final String QUERY_KOD_SIGNATUREN = "select distinct k.id as " + PARAM_AGGREGATE_ID
      + ", i.ident as " + PARAM_SIGNATURE
      + ", ao.name as " + PARAM_AUFBEWAHRUNGS_ORT
      + ", ow.name as " + PARAM_BESITZER + "  from kulturobjektdokument k"
      + " join identifikation i "
      + " on k.gueltigeidentifikation_id = i.id"
      + " AND i.identtyp = '" + IdentTyp.GUELTIGE_SIGNATUR + '\''
      + " left join normdatenreferenz ao"
      + " on ao.id = i.aufbewahrungsort_id"
      + " left join normdatenreferenz ow"
      + " on ow.id = i.besitzer_id";

  public static final String QUERY_KOD_GUELTIGE_SIGNATUR = "select distinct k.id as " + PARAM_AGGREGATE_ID
      + ", i.ident as " + PARAM_SIGNATURE
      + ", ao.name as " + PARAM_AUFBEWAHRUNGS_ORT
      + ", ow.name as " + PARAM_BESITZER + "  from kulturobjektdokument k"
      + " join identifikation i "
      + " on k.gueltigeidentifikation_id = i.id"
      + " AND i.identtyp = '" + IdentTyp.GUELTIGE_SIGNATUR + '\''
      + " AND k.id=:kodID"
      + " left join normdatenreferenz ao"
      + " on ao.id = i.aufbewahrungsort_id"
      + " left join normdatenreferenz ow"
      + " on ow.id = i.besitzer_id";

  public static final String QUERY_KOD_ALTERNATIVE_SIGNATUREN =
      "select distinct i.alternativeidentifikationen_id as " + PARAM_AGGREGATE_ID
          + ", i.ident as " + PARAM_SIGNATURE
          + ", ao.name as " + PARAM_AUFBEWAHRUNGS_ORT
          + ", ow.name as " + PARAM_BESITZER + " from identifikation i"
          + " left join normdatenreferenz ao"
          + " on ao.id = i.aufbewahrungsort_id"
          + " left join normdatenreferenz ow"
          + " on ow.id = i.besitzer_id"
          + " WHERE i.identtyp = '" + IdentTyp.ALTSIGNATUR + '\''
          + " AND i.alternativeidentifikationen_id=:kodID";

  public static final String QUERY_BESCHREIBUNG_SIGNATUREN = "select distinct bs.id as " + PARAM_AGGREGATE_ID
      + ", i.ident as " + PARAM_SIGNATURE
      + ", ao.name as " + PARAM_AUFBEWAHRUNGS_ORT
      + ", ow.name as " + PARAM_BESITZER + "  from beschreibungsdokument bs"
      + " join beschreibungsdokument_globalebeschreibungskomponente bg"
      + " on bs.id = bg.beschreibung_id"
      + " join globalebeschreibungskomponente g"
      + " on bg.beschreibungsstruktur_id = g.id"
      + " join beschreibungskomponentekopf k"
      + " on g.id = k.id"
      + " join globalebeschreibungskomponente_identifikation gi"
      + " on g.id = gi.globalebeschreibungskomponente_id"
      + " join identifikation i "
      + " on gi.identifikationen_id = i.id"
      + " AND i.identtyp = '" + IdentTyp.GUELTIGE_SIGNATUR + '\''
      + " left join normdatenreferenz ao"
      + " on ao.id = i.aufbewahrungsort_id"
      + " left join normdatenreferenz ow"
      + " on ow.id = i.besitzer_id";

  @Override
  public SignatureValue getSignatureByBeschreibungID(String beschreibungsID) {
    SignatureValue signatureValue = null;
    Query nativeQuery = getEntityManager().createNativeQuery(QUERY_SINGLE_BESCHREIBUNG_SIGNATURE, Tuple.class);
    List<Tuple> signaturen = nativeQuery
        .setParameter(PARAM_AGGREGATE_ID, beschreibungsID)
        .getResultList();
    for (Tuple element : signaturen) {
      signatureValue = getSignatureValue(element);
      break;
    }

    return signatureValue;
  }

  public Map<String, SignatureValue> getAllSignaturesWithBeschreibungIDs() {
    return prepareSignaturesMap(QUERY_BESCHREIBUNG_SIGNATUREN);
  }


  @Override
  public Map<String, SignatureValue> getAllSignaturesWithKodIDs() {
    return prepareSignaturesMap(QUERY_KOD_SIGNATUREN);
  }

  private Map<String, SignatureValue> prepareSignaturesMap(String signaturesQuery) {
    Query nativeQuery = getEntityManager().createNativeQuery(signaturesQuery, Tuple.class);
    List<Tuple> signaturen = nativeQuery.getResultList();

    if (signaturen.isEmpty()) {
      return Collections.emptyMap();
    }

    Map<String, SignatureValue> result = new HashMap<>(signaturen.size());
    for (Tuple element : signaturen) {
      SignatureValue signatureValue = getSignatureValue(element);
      result.put(signatureValue.getDokumentId(), signatureValue);
    }
    return result;
  }

  @Override
  public Optional<SignatureValue> getGueltigeSignatureByKodID(String kodID) {
    Query nativeQuery = getEntityManager().createNativeQuery(QUERY_KOD_GUELTIGE_SIGNATUR, Tuple.class);
    nativeQuery.setParameter("kodID", kodID);
    try {
      Tuple gueltigeSignatur = (Tuple) nativeQuery.getSingleResult();
      return Optional.ofNullable(getSignatureValue(gueltigeSignatur));
    } catch (NonUniqueResultException | NoResultException ne) {
      log.warn("Invalid result for kodID=" + kodID, ne);
      return Optional.empty();
    }
  }

  @Override
  public Set<SignatureValue> getAlternativeSignaturesByKodID(String kodID) {
    Query nativeQuery = getEntityManager().createNativeQuery(QUERY_KOD_ALTERNATIVE_SIGNATUREN, Tuple.class);
    nativeQuery.setParameter("kodID", kodID);

    List<Tuple> alternativeSignaturen = (List<Tuple>) nativeQuery.getResultList();
    return alternativeSignaturen.stream()
        .map(tuple -> getSignatureValue(tuple))
        .collect(Collectors.toSet());
  }

  private SignatureValue getSignatureValue(Tuple element) {
    SignatureValue signatureValue = new SignatureValue(
        element.get(PARAM_AGGREGATE_ID, String.class),
        element.get(PARAM_SIGNATURE, String.class),
        element.get(PARAM_AUFBEWAHRUNGS_ORT, String.class),
        element.get(PARAM_BESITZER, String.class)
    );
    return signatureValue;
  }

}
