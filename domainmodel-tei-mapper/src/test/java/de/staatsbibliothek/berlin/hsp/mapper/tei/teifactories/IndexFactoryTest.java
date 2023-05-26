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

package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexDecorationFactoryTest.createBuchschmuck;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexDimensionsFactoryTest.createAbmessung;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFormFactoryTest.createHandschriftentyp;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFormatFactoryTest.createFormat;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexMaterialFactoryTest.createBeschreibstoff;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexMeasureFactoryTest.createUmfang;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexMusicNotationFactoryTest.createMusiknotation;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexOrigDateFactoryTest.createEntstehungszeit;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexOrigPlaceFactoryTest.createEntstehungsort;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexStatusFactoryTest.createStatus;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexTextLangFactoryTest.createGrundSprache;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexTitleFactoryTest.createTitel;
import static de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon.getContentAsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.AttributsReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Beschreibstoff;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Buchschmuck;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Handschriftentyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Musiknotation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Status;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Titel;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Umfang;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.Term;

class IndexFactoryTest {

  static Optional<Term> findTerm(Index index, String type) {
    List<Term> result = findTerms(index, type);
    return result.size() == 1 ? result.stream().findFirst() : Optional.empty();
  }

  static List<Term> findTerms(Index index, String type) {
    return Stream.ofNullable(index)
        .flatMap(idx -> idx.getTermsAndIndices().stream())
        .filter(Term.class::isInstance)
        .map(Term.class::cast)
        .filter(term -> type.equals(term.getType()))
        .collect(Collectors.toList());
  }

  static void assertTerm(Index index, String type, String content) {
    Optional<Term> term = IndexFactoryTest.findTerm(index, type);
    assertTrue(term.isPresent());
    if (Objects.isNull(content)) {
      assertTrue(term.get().getContent().isEmpty());
    } else {
      assertEquals(content, getContentAsString(term.get().getContent()));
    }
  }

  @Test
  void testBuild() {
    Titel titel = createTitel();
    Beschreibstoff beschreibstoff = createBeschreibstoff();
    Umfang umfang = createUmfang();
    Abmessung abmessung = createAbmessung();
    Format format = createFormat();
    Entstehungszeit entstehungszeit = createEntstehungszeit();
    Entstehungsort entstehungsort = createEntstehungsort();
    Grundsprache grundSprache = createGrundSprache();
    Handschriftentyp handschriftentyp = createHandschriftentyp();
    Status status = createStatus();
    Buchschmuck buchschmuck = createBuchschmuck();
    Musiknotation musiknotation = createMusiknotation();

    Set<AttributsReferenz> attributsReferenzen = new LinkedHashSet<>();
    attributsReferenzen.add(titel);
    attributsReferenzen.add(beschreibstoff);
    attributsReferenzen.add(umfang);
    attributsReferenzen.add(abmessung);
    attributsReferenzen.add(format);
    attributsReferenzen.add(entstehungszeit);
    attributsReferenzen.add(entstehungsort);
    attributsReferenzen.add(grundSprache);
    attributsReferenzen.add(handschriftentyp);
    attributsReferenzen.add(status);
    attributsReferenzen.add(buchschmuck);
    attributsReferenzen.add(musiknotation);

    List<Index> indices = IndexFactory.build(attributsReferenzen);

    assertNotNull(indices);
    assertEquals(15, indices.size());

    assertIndex(indices, AttributsTyp.TITEL, TEIValues.TEI_ANKER_TAG + titel.getReferenzId());
    assertIndex(indices, AttributsTyp.BESCHREIBSTOFF, TEIValues.TEI_ANKER_TAG + beschreibstoff.getReferenzId());
    assertIndex(indices, AttributsTyp.UMFANG, TEIValues.TEI_ANKER_TAG + umfang.getReferenzId());
    assertTwoIndices(indices, AttributsTyp.ABMESSUNG, TEIValues.TEI_ANKER_TAG + abmessung.getReferenzId());
    assertTwoIndices(indices, AttributsTyp.FORMAT, TEIValues.TEI_ANKER_TAG + format.getReferenzId());
    assertIndex(indices, AttributsTyp.ENTSTEHUNGSORT, TEIValues.TEI_ANKER_TAG + entstehungsort.getReferenzId());
    assertTwoIndices(indices, AttributsTyp.ENTSTEHUNGSZEIT, TEIValues.TEI_ANKER_TAG + entstehungszeit.getReferenzId());
    assertIndex(indices, AttributsTyp.GRUNDSPRACHE, TEIValues.TEI_ANKER_TAG + grundSprache.getReferenzId());
    assertIndex(indices, AttributsTyp.HANDSCHRIFTENTYP, TEIValues.TEI_ANKER_TAG + handschriftentyp.getReferenzId());
    assertIndex(indices, AttributsTyp.STATUS, TEIValues.TEI_ANKER_TAG + status.getReferenzId());
    assertIndex(indices, AttributsTyp.BUCHSCHMUCK, TEIValues.TEI_ANKER_TAG + buchschmuck.getReferenzId());
    assertIndex(indices, AttributsTyp.MUSIKNOTATION, TEIValues.TEI_ANKER_TAG + musiknotation.getReferenzId());
  }

  private void assertIndex(List<Index> indices, AttributsTyp attributsTyp, String referenzId) {
    assertTrue(indices.stream()
            .anyMatch(index -> attributsTyp.getIndexName().equals(index.getIndexName())
                && referenzId.equals(index.getCopyOf())),
        attributsTyp.name());
  }

  private void assertTwoIndices(List<Index> indices, AttributsTyp attributsTyp, String referenzId) {
    assertEquals(2, indices.stream()
            .filter(index -> attributsTyp.getIndexName().equals(index.getIndexName())
                && referenzId.equals(index.getCopyOf()))
            .count(),
        attributsTyp.name());
  }

  @Test
  void testSetIndexSourceAndCopyOf() {
    Index emptyIndex = new Index();
    IndexFactory.setIndexSourceAndCopyOf(emptyIndex, "1", "");
    assertEquals("#1", emptyIndex.getCopyOf());
    assertEquals(0, emptyIndex.getSources().size());
    IndexFactory.setIndexSourceAndCopyOf(emptyIndex, "1", "https://test.de");
    assertEquals(1, emptyIndex.getSources().size());
  }
}
