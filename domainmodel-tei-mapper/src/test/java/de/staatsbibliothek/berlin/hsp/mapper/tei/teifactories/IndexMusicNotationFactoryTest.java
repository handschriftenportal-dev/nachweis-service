package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.MUSIKNOTATION_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.assertTerm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Musiknotation;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Index;

class IndexMusicNotationFactoryTest {

  private static final String REFERENZ_URL = "https://resolver.spk-berlin.de/123456";
  static Musiknotation createMusiknotation() {
    return Musiknotation.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("MU-1")
        .withReferenzURL(REFERENZ_URL)
        .withText("yes")
        .withChangeId("LLL-003")
        .build();
  }

  @Test
  void testBuild() {
    List<Index> ergebnis = new ArrayList<>();

    IndexMusicNotationFactory.build(null, ergebnis);
    assertTrue(ergebnis.isEmpty());

    Musiknotation musiknotation = createMusiknotation();
    IndexMusicNotationFactory.build(musiknotation, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertEquals(AttributsTyp.MUSIKNOTATION.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(REFERENZ_URL,index.getSources().getFirst());
    assertEquals(TEIValues.TEI_ANKER_TAG+musiknotation.getReferenzId(),index.getCopyOf());
    assertNotNull(index.getChanges());
    assertFalse(index.getChanges().isEmpty());
    assertEquals("#LLL-003", index.getChanges().getFirst());
    assertTerm(index, MUSIKNOTATION_TERM_TYP_TEXT, musiknotation.getText());
  }

  @Test
  void testBuild_empty() {
    List<Index> ergebnis = new ArrayList<>();

    Musiknotation musiknotation = Musiknotation.builder().build();
    IndexMusicNotationFactory.build(musiknotation, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertNotNull(index.getSources());
    assertEquals(0, index.getSources().size());
    assertNotNull(index.getChanges());
    assertEquals(0, index.getChanges().size());

    assertTerm(index, MUSIKNOTATION_TERM_TYP_TEXT, null);
  }
}
