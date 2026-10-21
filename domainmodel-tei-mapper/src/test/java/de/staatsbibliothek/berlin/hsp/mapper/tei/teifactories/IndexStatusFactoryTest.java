package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.STATUS_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.assertTerm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Status;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Index;

class IndexStatusFactoryTest {

  private static final String REFERENZ_URL = "https://resolver.spk-berlin.de/123456";
  static Status createStatus() {
    return Status.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzURL(REFERENZ_URL)
        .withReferenzId("ST-1")
        .withText("displaced")
        .withChangeId("NNN-999")
        .build();
  }

  @Test
  void testBuild() {
    List<Index> ergebnis = new ArrayList<>();

    IndexStatusFactory.build(null, ergebnis);
    assertTrue(ergebnis.isEmpty());

    Status status = createStatus();
    IndexStatusFactory.build(status, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertEquals(AttributsTyp.STATUS.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(REFERENZ_URL,index.getSources().getFirst());
    assertEquals(TEIValues.TEI_ANKER_TAG+status.getReferenzId(),index.getCopyOf());
    assertNotNull(index.getChanges());
    assertFalse(index.getChanges().isEmpty());
    assertEquals("#NNN-999", index.getChanges().getFirst());
    assertTerm(index, STATUS_TERM_TYP_TEXT, status.getText());
  }

  @Test
  void testBuild_empty() {
    List<Index> ergebnis = new ArrayList<>();

    Status status = Status.builder().build();
    IndexStatusFactory.build(status, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertNotNull(index.getSources());
    assertEquals(0, index.getSources().size());
    assertNotNull(index.getChanges());
    assertEquals(0, index.getChanges().size());

    assertTerm(index, STATUS_TERM_TYP_TEXT, null);
  }
}
