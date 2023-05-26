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

package de.staatsbibliothek.berlin.hsp.mapper.tei;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import de.staatsbibliothek.berlin.hsp.messaging.objectfactory.TEIObjectFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.PubPlace;
import org.tei_c.ns._1.PublicationStmt;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.Term;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 12.05.2020.
 * @version 1.0
 */
public class TEIQuery {

  public static final String ORIG_PLACE_NORM = "origPlace_norm";
  private static final Logger log = LoggerFactory.getLogger(TEIQuery.class);

  private TEIQuery() {
  }

  public static List<MsDesc> queryForMsDescAsBeschreibung(TEI tei) {

    List<MsDesc> msDescList = new ArrayList<>();
    List<MsDesc> result = new ArrayList<>();

    try {

      TEICommon.findAll(MsDesc.class, tei, msDescList);

      log.info("Found MsDesc size {} ", msDescList.size());

      result = msDescList.stream()
          .filter(msDesc -> msDesc.getId() != null && !msDesc.getId().isEmpty())
          .collect(Collectors.toList());

    } catch (Exception e) {
      log.error("Error during query for MsDesc ", e);
    }

    return result;
  }

  public static Optional<String> queryForTermOriginGNDID(Beschreibung beschreibung) {
    List<Term> termList = new ArrayList<>();
    List<MsDesc> msDescList = new ArrayList<>();
    AtomicReference<Optional<String>> result = new AtomicReference<>(Optional.empty());

    try {

      List<TEI> teis = TEIObjectFactory
          .unmarshal(
              new ByteArrayInputStream(beschreibung.getTeiXML().getBytes(StandardCharsets.UTF_8)));
      TEICommon.findAll(MsDesc.class, teis.get(0), msDescList);
      TEICommon.findAll(Term.class, msDescList.get(0).getHeads().get(0), termList);
      termList.stream().filter(t -> ORIG_PLACE_NORM.equals(t.getType()))
          .findFirst()
          .flatMap(term -> term.getReves().stream().findFirst())
          .ifPresent(ref -> {
            String gndid = ref.substring(ref.lastIndexOf("/") + 1);
            if (!gndid.isEmpty()) {
              result.set(Optional.of(gndid));
            }
          });

    } catch (Exception e) {
      log.error("Error during find GNDID of Entstehungsort", e);
    }
    return result.get();
  }


  public static Optional<PubPlace> queryForFileDescPupPlaceElement(TEI tei) throws Exception {

    Optional<PublicationStmt> publicationStmt = TEICommon.findFirst(PublicationStmt.class, tei);

    if (publicationStmt.isPresent()) {

      Optional<PubPlace> pubPlace = publicationStmt.get().getPublishersAndDistributorsAndAuthorities().stream()
          .filter(PubPlace.class::isInstance)
          .map(PubPlace.class::cast)
          .findFirst();

      if (pubPlace.isPresent()) {
        return pubPlace;
      } else {
        PubPlace newPubplace = new PubPlace();
        publicationStmt.get().getPublishersAndDistributorsAndAuthorities().add(newPubplace);
        return Optional.of(newPubplace);
      }

    }
    return Optional.empty();
  }
}
