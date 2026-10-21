package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.BESCHREIBSTOFF_TERM_NORM;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.BESCHREIBSTOFF_TERM_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.assertTerm;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.findTerms;
import static de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon.getContentAsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Beschreibstoff;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.Term;

class IndexMaterialFactoryTest {

  private static final String REFERENZ_URL = "https://resolver.spk-berlin.de/123456";

  static Beschreibstoff createBeschreibstoff() {
    return Beschreibstoff.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("MA-1")
        .withReferenzURL(REFERENZ_URL)
        .withReferenzen(
            List.of(new NormdatenReferenz("NORM1", "Pergament", "001-3"),
                new NormdatenReferenz("NORM-233", "Papyrus", "00233-3")))
        .withText("Pergament, Papyrus")
        .withChangeId("MMM-001")
        .build();
  }

  @Test
  void testBuild() {
    List<Index> ergebnis = new ArrayList<>();

    IndexMaterialFactory.getInstance().build(null, ergebnis);
    assertTrue(ergebnis.isEmpty());

    Beschreibstoff beschreibstoff = createBeschreibstoff();
    IndexMaterialFactory.getInstance().build(beschreibstoff, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertEquals(AttributsTyp.BESCHREIBSTOFF.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(REFERENZ_URL, index.getSources().getFirst());
    assertEquals(TEIValues.TEI_ANKER_TAG + beschreibstoff.getReferenzId(), index.getCopyOf());

    assertTerm(index, BESCHREIBSTOFF_TERM_TEXT, beschreibstoff.getText());

    List<Term> terme = findTerms(index, BESCHREIBSTOFF_TERM_NORM);

    var actualTermContents = terme.stream()
        .map(term -> getContentAsString(term.getContent()))
        .toList();
    var expectedContent = "Pergament";

    assertTrue(
        actualTermContents.contains(expectedContent),
        "Expected to find type '%s' in terms, but found: %s".formatted(
            expectedContent,
            actualTermContents
        )
    );

    expectedContent = "Papyrus";
    assertTrue(actualTermContents.contains(expectedContent),
        "Expected to find type '%s' in terms, but found: %s".formatted(
            expectedContent,
            actualTermContents
        )
    );
    assertNotNull(index.getChanges());
    assertFalse(index.getChanges().isEmpty());
    assertEquals("#MMM-001", index.getChanges().getFirst());
  }

  @Test
  void testBuild_empty() {
    Beschreibstoff beschreibstoff = Beschreibstoff.builder().build();

    List<Index> ergebnis = new ArrayList<>();

    IndexMaterialFactory.getInstance().build(beschreibstoff, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertNotNull(index.getSources());
    assertEquals(0, index.getSources().size());
    assertNotNull(index.getChanges());
    assertEquals(0, index.getChanges().size());

    assertTerm(index, BESCHREIBSTOFF_TERM_TEXT, null);
    assertTerm(index, BESCHREIBSTOFF_TERM_NORM, null);
  }

}
