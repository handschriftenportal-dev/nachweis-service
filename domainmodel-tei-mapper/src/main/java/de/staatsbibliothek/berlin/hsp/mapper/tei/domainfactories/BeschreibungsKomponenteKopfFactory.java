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


import static de.staatsbibliothek.berlin.hsp.mapper.tei.domainfactories.ElementFactoryRegistry.buildElement;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.BeschreibungsKomponenteKopf.BeschreibungsKomponenteKopfBuilder;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Identifikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.KulturObjektTyp;
import de.staatsbibliothek.berlin.hsp.mapper.api.exceptions.HSPMapperException;
import de.staatsbibliothek.berlin.hsp.mapper.api.factory.ElementFactory;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.ObjectDesc;
import org.tei_c.ns._1.PhysDesc;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.Term;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 22.04.20
 */
public class BeschreibungsKomponenteKopfFactory implements ElementFactory<BeschreibungsKomponenteKopf, MsDesc, TEI> {

  //msDesc/head/title/text()
  static String buildTitleString(MsDesc local) {

    final AtomicReference<ArrayList<Object>> allTitles = new AtomicReference<>(new ArrayList<>());
    if (Objects.nonNull(local)) {
       local.getHeads()
           .stream()
           .flatMap(head -> head.getContent().stream())
           .filter(index -> index instanceof Index)
           .filter(index -> ((Index) index).getIndexName().equals("norm_title"))
           .findFirst()
           .map(object -> (Index) object)
           .flatMap(normTitle -> normTitle.getTermsAndIndices()
               .stream()
               .findFirst()
               .map(object -> (Term) object))
           .ifPresent(term -> allTitles.set((ArrayList<Object>) term.getContent()));
    }

    return TEICommon.getContentAsString(allTitles.get());
  }

  @Override
  public BeschreibungsKomponenteKopf build(MsDesc local, TEI global) throws HSPMapperException {

    KulturObjektTyp kulturObjektTyp = findKulturObjektTyp(local);

    BeschreibungsKomponenteKopf beschreibungsKomponenteKopf = new BeschreibungsKomponenteKopfBuilder()
        .withId(UUID.randomUUID().toString())
        .withKulturObjektTyp(kulturObjektTyp)
        .withTitel(buildTitleString(local))
        .withIndentifikationen(buildElement(Identifikation.class, local, global))
        .build();

    return beschreibungsKomponenteKopf;
  }

  KulturObjektTyp findKulturObjektTyp(MsDesc local) {
    final AtomicReference<KulturObjektTyp> kulturObjektTyp = new AtomicReference<>();

    Optional.ofNullable(local)
        .map(MsDesc::getPhysDesc)
        .map(PhysDesc::getObjectDesc)
        .map(ObjectDesc::getForm)
        .ifPresent(s -> {
          kulturObjektTyp.set(KulturObjektTyp.getKulturObjektTypFromString(s));
        });

    return kulturObjektTyp.get();
  }
}
