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

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.AttributsReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.DokumentObjektTyp;
import de.staatsbibliothek.berlin.hsp.mapper.api.factory.ElementFactory;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.MsDesc;
import org.tei_c.ns._1.TEI;
import org.tei_c.ns._1.Term;

public abstract class AttributsReferenzFactory<T extends AttributsReferenz> implements ElementFactory<T, MsDesc, TEI> {

  protected AttributsReferenzFactory() {
    // do nothing
  }

  protected static List<Index> findIndices(MsDesc msDesc, String indexName) {
    return findFilteredIndexStream(msDesc, indexName).collect(Collectors.toList());
  }

  protected static Optional<Index> findIndex(MsDesc msDesc, String indexName) {
    return findFilteredIndexStream(msDesc, indexName).findFirst();
  }

  protected static List<Term> findTerme(Index index) {
    return Stream.ofNullable(index)
        .flatMap(idx -> idx.getTermsAndIndices().stream())
        .filter(Term.class::isInstance)
        .map(Term.class::cast)
        .collect(Collectors.toList());
  }

  protected static String mapTermText(Index index, String termType) {
    return findTerme(index).stream()
        .filter(term -> termType.equals(term.getType()))
        .map(term -> TEICommon.getContentAsString(term.getContent()))
        .findFirst()
        .orElse(null);
  }

  private static Stream<Index> findFilteredIndexStream(MsDesc msDesc, String indexName) {
    return Stream.ofNullable(msDesc)
        .flatMap(m -> m.getHeads().stream())
        .findFirst()
        .stream()
        .flatMap(head -> head.getContent().stream())
        .filter(Index.class::isInstance)
        .map(Index.class::cast)
        .filter(index -> indexName.equals(index.getIndexName()));
  }

  protected static String getCopyOfValue(String copyOf, MsDesc msDesc) {
    if(StringUtils.isBlank(copyOf) && !DokumentObjektTyp.HSP_OBJECT.equals(
        DokumentObjektTyp.fromString(msDesc.getType()))) {
      return msDesc.getId();
    }

    return copyOf != null ? copyOf.replaceAll(TEIValues.TEI_ANKER_TAG,"") : copyOf;
  }

}
