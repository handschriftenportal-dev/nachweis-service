package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.BUCHSCHMUCK_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.assertTerm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Buchschmuck;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Index;

class IndexDecorationFactoryTest {

  public static final String REFERENZ_URL = "https://resolver.spk-berlin.de/123456";

  static Buchschmuck createBuchschmuck() {
    return Buchschmuck.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("DE-1")
        .withReferenzURL(REFERENZ_URL)
        .withText("yes")
        .withChangeId("VVV-099")
        .build();
  }

  @Test
  void testBuild() {
    List<Index> ergebnis = new ArrayList<>();

    IndexDecorationFactory.build(null, ergebnis);
    assertTrue(ergebnis.isEmpty());

    Buchschmuck buchschmuck = createBuchschmuck();
    IndexDecorationFactory.build(buchschmuck, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertEquals(AttributsTyp.BUCHSCHMUCK.getIndexName(), index.getIndexName());
    assertEquals(REFERENZ_URL,buchschmuck.getReferenzURL());
    assertNotNull(index.getSources());
    assertNotNull(index.getChanges());
    assertFalse(index.getChanges().isEmpty());
    assertEquals("#VVV-099", index.getChanges().getFirst());

    assertTerm(index, BUCHSCHMUCK_TERM_TYP_TEXT, buchschmuck.getText());
    assertEquals(TEIValues.TEI_ANKER_TAG+buchschmuck.getReferenzId(),index.getCopyOf());
  }

  @Test
  void testBuild_empty() {
    List<Index> ergebnis = new ArrayList<>();

    Buchschmuck buchschmuck = Buchschmuck.builder().build();
    IndexDecorationFactory.build(buchschmuck, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertNotNull(index.getSources());
    assertEquals(0, index.getSources().size());
    assertNotNull(index.getChanges());
    assertEquals(0, index.getChanges().size());

    assertTerm(index, BUCHSCHMUCK_TERM_TYP_TEXT, null);
  }
}
