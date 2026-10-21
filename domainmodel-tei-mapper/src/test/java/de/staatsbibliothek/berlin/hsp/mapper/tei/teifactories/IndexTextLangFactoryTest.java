package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.GRUNDSPRACHE_TERM_NORM;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.GRUNDSPRACHE_TERM_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.assertTerm;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.findTerms;
import static de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon.getContentAsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Grundsprache;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.Term;

class IndexTextLangFactoryTest {

  private static final String REFERENZ_URL = "https://resolver.spk-berlin.de/123456";

  static Grundsprache createGrundSprache() {
    return Grundsprache.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("GS-1")
        .withText("Alt- und Mittelhochdeutsch")
        .withReferenzen(
            List.of(new NormdatenReferenz("NORM1", "deutsch", "4001523-3"),
                new NormdatenReferenz("NORM-233", "englisch", "4039687-3")))
        .withReferenzURL(REFERENZ_URL)
        .withChangeId("DDD-011")
        .build();
  }

  @Test
  void testBuild() {
    List<Index> ergebnis = new ArrayList<>();

    IndexTextLangFactory.getInstance().build(null, ergebnis);
    assertTrue(ergebnis.isEmpty());

    Grundsprache grundSprache = createGrundSprache();
    IndexTextLangFactory.getInstance().build(grundSprache, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertEquals(AttributsTyp.GRUNDSPRACHE.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(REFERENZ_URL, index.getSources().getFirst());
    assertEquals(TEIValues.TEI_ANKER_TAG + grundSprache.getReferenzId(),
        index.getCopyOf());
    assertTerm(index, GRUNDSPRACHE_TERM_TEXT, grundSprache.getText());
    assertNotNull(index.getChanges());
    assertFalse(index.getChanges().isEmpty());
    assertEquals("#DDD-011", index.getChanges().getFirst());

    List<Term> terms = findTerms(index, GRUNDSPRACHE_TERM_NORM);
    assertNotNull(terms);
    assertEquals(2, terms.size());
    assertTrue(terms.stream()
        .map(term -> getContentAsString(term.getContent()))
        .anyMatch("deutsch"::equals));
    assertTrue(terms.stream()
        .map(term -> getContentAsString(term.getContent()))
        .anyMatch("englisch"::equals));
  }

  @Test
  void testBuild_empty() {
    List<Index> ergebnis = new ArrayList<>();

    Grundsprache grundSprache = Grundsprache.builder().build();
    IndexTextLangFactory.getInstance().build(grundSprache, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertNotNull(index.getSources());
    assertEquals(0, index.getSources().size());
    assertNotNull(index.getChanges());
    assertEquals(0, index.getChanges().size());

    assertTerm(index, GRUNDSPRACHE_TERM_TEXT, null);
    assertTerm(index, GRUNDSPRACHE_TERM_NORM, null);
  }

  @Test
  void testcreateTerm() {
    NormdatenReferenz grundsprache = new NormdatenReferenz.NormdatenReferenzBuilder()
        .withId("NORM-1")
        .withGndID("GND_ID")
        .withName("Name")
        .build();

    Term textLang = IndexTextLangFactory.getInstance()
        .createNormdatenTerm(grundsprache, GRUNDSPRACHE_TERM_NORM);

    assertNotNull(textLang);
    assertEquals("NORM-1", textLang.getKey());
    assertEquals("", TEICommon.getContentAsString(textLang.getReves()));
    assertEquals("Name", TEICommon.getContentAsString(textLang.getContent()));
    assertEquals(GRUNDSPRACHE_TERM_NORM, textLang.getType());
  }
}
