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

package de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories;

import static de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon.getContentAsString;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.IdentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation.IdentifikationBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPMapperException;
import de.staatsbibliothek.berlin.hsp.mapper.api.factory.ElementFactory;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.MsIdentifier;
import org.tei_c.ns._1.TEI;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 22.04.20
 */
public class IdentifikationSetFactory implements ElementFactory<Set<Identifikation>, MsDesc, TEI> {

  @Override
  public Set<Identifikation> build(MsDesc local, TEI global) throws HSPMapperException {
    Set<Identifikation> result = new LinkedHashSet<>();
    //msDesc/msIdentifier
    MsIdentifier msIdentifier = local.getMsIdentifier();
    NormdatenReferenz besitzer = findBesitzer(msIdentifier);
    NormdatenReferenz aufbewahrungsOrt = findAufbewahrungsOrt(msIdentifier);

    getIdent(msIdentifier);
    String ident = getIdent(msIdentifier);

    Set<String> sammlungIds = findSammlungIds(msIdentifier);

    Identifikation identifikation = new IdentifikationBuilder()
        .withId(UUID.randomUUID().toString())
        .withAufbewahrungsOrt(aufbewahrungsOrt)
        .withBesitzer(besitzer)
        .withIdent(ident)
        .withIdentTyp(IdentTyp.GUELTIGE_SIGNATUR)
        .withSammlungIDs(sammlungIds)
        .build();
    result.add(identifikation);
    return result;
  }

  String getIdent(MsIdentifier msIdentifier) {
    //msDesc/msIdentifier/idno/text()
    StringBuilder stringBuilder = new StringBuilder();

    Optional.ofNullable(msIdentifier).map(MsIdentifier::getIdnos).stream()
        .flatMap(il -> il.stream())
        .forEach(i -> stringBuilder.append(getContentAsString(i.getContent(), " ; ")));

    return stringBuilder.toString();
  }

  NormdatenReferenz findBesitzer(MsIdentifier msIdentifier) {

    final AtomicReference<NormdatenReferenz> koerperschaftAtomicReference = new AtomicReference<>();

    Optional.ofNullable(msIdentifier).map(MsIdentifier::getRepository)
        .ifPresent(repository -> {
          koerperschaftAtomicReference.set(new NormdatenReferenz(
              repository.getKey(),
              TEICommon.getContentAsString(repository.getContent()), ""));
        });

    return koerperschaftAtomicReference.get();
  }

  NormdatenReferenz findAufbewahrungsOrt(MsIdentifier msIdentifier) {

    final AtomicReference<NormdatenReferenz> ort = new AtomicReference<>();

    Optional.ofNullable(msIdentifier).map(MsIdentifier::getSettlement)
        .ifPresent(settlement -> {
          ort.set(new NormdatenReferenz(settlement.getKey(),
              TEICommon.getContentAsString(settlement.getContent()), ""));
        });

    return ort.get();
  }

  Set<String> findSammlungIds(MsIdentifier msIdentifier) {
    return Optional.ofNullable(msIdentifier)
        .map(MsIdentifier::getCollections)
        .stream()
        .flatMap(java.util.Collection::stream)
        .map(collection -> TEICommon.getContentAsString(collection.getContent()))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }
}
