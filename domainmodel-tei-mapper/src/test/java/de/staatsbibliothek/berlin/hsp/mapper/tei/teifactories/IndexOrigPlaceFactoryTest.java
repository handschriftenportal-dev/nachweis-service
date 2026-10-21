package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSORT_TERM_TYP_NORM;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSORT_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.assertTerm;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.findTerm;
import static de.staatsbibliothek.berlin.hsp.messaging.common.TEICommon.getContentAsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungsort;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.Term;

class IndexOrigPlaceFactoryTest {
  private static final String REFERENZ_URL = "https://resolver.spk-berlin.de/123456";
  static Entstehungsort createEntstehungsort() {
    return Entstehungsort.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("OP-1")
        .withReferenzURL(REFERENZ_URL)
        .withText("Weißenburg")
        .withChangeId("RRR-008")
        .addNormdatenReferenz(
            new NormdatenReferenz("NORM-f5c6758c-3c33-3785-933a-999b0fa2a4a0",
                "Weißenburg (Elsass)",
                "https://d-nb.info/gnd/4079134-8",
                NormdatenReferenz.ORT_TYPE_NAME)
        ).build();
  }

  @Test
  void testBuild() {
    List<Index> ergebnis = new ArrayList<>();

    IndexOrigPlaceFactory.build(null, ergebnis);
    assertTrue(ergebnis.isEmpty());

    Entstehungsort entstehungsort = createEntstehungsort();
    IndexOrigPlaceFactory.build(entstehungsort, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertEquals(AttributsTyp.ENTSTEHUNGSORT.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(REFERENZ_URL,index.getSources().getFirst());
    assertEquals(TEIValues.TEI_ANKER_TAG+entstehungsort.getReferenzId(),index.getCopyOf());
    assertNotNull(index.getChanges());
    assertFalse(index.getChanges().isEmpty());
    assertEquals("#RRR-008", index.getChanges().getFirst());
    assertTerm(index, ENTSTEHUNGSORT_TERM_TYP_TEXT, entstehungsort.getText());

    assertNotNull(entstehungsort.getNormdatenReferenzen());
    assertEquals(1, entstehungsort.getNormdatenReferenzen().size());
    NormdatenReferenz normdatum = entstehungsort.getNormdatenReferenzen().getFirst();

    Optional<Term> optNormTerm = findTerm(index, ENTSTEHUNGSORT_TERM_TYP_NORM);
    assertTrue(optNormTerm.isPresent());
    Term normTerm = optNormTerm.get();

    assertEquals(normdatum.getId(), normTerm.getKey());
    assertEquals(normdatum.getName(), getContentAsString(normTerm.getContent()));
    assertEquals(normdatum.getGndUrl(), getContentAsString(normTerm.getReves()));
  }

  @Test
  void testBuild_empty() {
    Entstehungsort entstehungsort = Entstehungsort.builder().build();

    List<Index> ergebnis = new ArrayList<>();

    IndexOrigPlaceFactory.build(entstehungsort, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertNotNull(index.getSources());
    assertEquals(0, index.getSources().size());
    assertNotNull(index.getChanges());
    assertEquals(0, index.getChanges().size());

    assertTerm(index, ENTSTEHUNGSORT_TERM_TYP_TEXT, null);
    assertTerm(index, ENTSTEHUNGSORT_TERM_TYP_NORM, null);
  }
}
