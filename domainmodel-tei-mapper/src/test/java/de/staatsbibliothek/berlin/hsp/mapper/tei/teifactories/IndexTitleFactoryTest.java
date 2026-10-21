package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.TITEL_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.assertTerm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Titel;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Index;

class IndexTitleFactoryTest {

  private static final String REFERENZ_URL = "https://resolver.spk-berlin.de/123456";

  static Titel createTitel() {
    return Titel.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("TI-1")
        .withReferenzURL(REFERENZ_URL)
        .withText("Titel der Veschreibung")
        .withChangeId("CHG-123")
        .build();
  }

  @Test
  void testBuild() {
    List<Index> ergebnis = new ArrayList<>();

    IndexTitleFactory.build(null, ergebnis);
    assertTrue(ergebnis.isEmpty());

    Titel titel = createTitel();
    IndexTitleFactory.build(titel, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertEquals(AttributsTyp.TITEL.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(REFERENZ_URL, index.getSources().getFirst());
    assertEquals(TEIValues.TEI_ANKER_TAG + titel.getReferenzId(), index.getCopyOf());
    assertTerm(index, TITEL_TERM_TYP_TEXT, titel.getText());
    assertNotNull(index.getChanges());
    assertFalse(index.getChanges().isEmpty());
    assertEquals("#CHG-123", index.getChanges().getFirst());
  }

  @Test
  void testBuild_empty() {
    List<Index> ergebnis = new ArrayList<>();

    Titel titel = Titel.builder().build();
    IndexTitleFactory.build(titel, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertNotNull(index.getSources());
    assertEquals(0, index.getSources().size());
    assertTerm(index, TITEL_TERM_TYP_TEXT, null);
    assertNotNull(index.getChanges());
    assertEquals(0, index.getChanges().size());
  }

  @Test
  void testsetIndexSourceAndCopyOf() {
    Index index = new Index();

    assertNotNull(index);

    IndexFactory.setIndexSourceAndCopyOf(index, "BS1", "");

    assertEquals("#BS1", index.getCopyOf());
  }
}
