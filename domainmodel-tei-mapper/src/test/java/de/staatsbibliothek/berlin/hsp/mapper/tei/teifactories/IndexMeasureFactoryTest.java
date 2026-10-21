package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.UMFANG_TERM_TYP_NO_OF_LEAVES;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.UMFANG_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.assertTerm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Umfang;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Index;

class IndexMeasureFactoryTest {

  private static final String REFERENZ_URL = "https://resolver.spk-berlin.de/123456";
  static Umfang createUmfang() {
    return Umfang.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("ME-1")
        .withReferenzURL(REFERENZ_URL)
        .withText("I + 394 Bl.")
        .withBlattzahl("394")
        .withChangeId("JJJ-003")
        .build();
  }

  @Test
  void testBuild() {
    List<Index> ergebnis = new ArrayList<>();

    IndexMeasureFactory.build(null, ergebnis);
    assertTrue(ergebnis.isEmpty());

    Umfang umfang = createUmfang();
    IndexMeasureFactory.build(umfang, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertEquals(AttributsTyp.UMFANG.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(REFERENZ_URL,index.getSources().getFirst());
    assertEquals(TEIValues.TEI_ANKER_TAG+umfang.getReferenzId(),index.getCopyOf());
    assertTerm(index, UMFANG_TERM_TYP_TEXT, umfang.getText());
    assertTerm(index, UMFANG_TERM_TYP_NO_OF_LEAVES, umfang.getBlattzahl());
    assertNotNull(index.getChanges());
    assertFalse(index.getChanges().isEmpty());
    assertEquals("#JJJ-003", index.getChanges().getFirst());
  }

  @Test
  void testBuild_empty() {
    Umfang umfang = Umfang.builder().build();

    List<Index> ergebnis = new ArrayList<>();

    IndexMeasureFactory.build(umfang, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertEquals(AttributsTyp.UMFANG.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(0, index.getSources().size());
    assertNotNull(index.getChanges());
    assertEquals(0, index.getChanges().size());

    assertTerm(index, UMFANG_TERM_TYP_TEXT, null);
    assertTerm(index, UMFANG_TERM_TYP_NO_OF_LEAVES, null);
  }

}
