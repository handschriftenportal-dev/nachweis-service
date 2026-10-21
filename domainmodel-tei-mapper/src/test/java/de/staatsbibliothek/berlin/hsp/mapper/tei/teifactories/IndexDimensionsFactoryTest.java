package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ABMESSUNG_TERM_TYP_DEPTH;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ABMESSUNG_TERM_TYP_HEIGHT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ABMESSUNG_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ABMESSUNG_TERM_TYP_TYPE;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ABMESSUNG_TERM_TYP_WIDTH;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.assertTerm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Abmessung;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AbmessungWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Index;

public class IndexDimensionsFactoryTest {

  public static final String REFERENZ_URL = "https://resolver.spk-berlin.de/123456";

  static Abmessung createAbmessung() {
    return Abmessung.builder()
        .withReferenzId("DI-1")
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzURL(REFERENZ_URL)
        .withChangeId("UUU-002")
        .addAbmessungWert(AbmessungWert.builder()
            .withText("23,5 × 16,5 x 2,5")
            .withHoehe("23,5")
            .withBreite("16,5")
            .withTiefe("2,5")
            .withTyp("factual")
            .build())
        .addAbmessungWert(AbmessungWert.builder()
            .withText("25 × 15 x 5")
            .withHoehe("25")
            .withBreite("15")
            .withTiefe("5")
            .withTyp("deduced")
            .build())
        .build();
  }

  @Test
  public void testBuild() {
    List<Index> ergebnis = new ArrayList<>();

    IndexDimensionsFactory.build(null, ergebnis);
    assertTrue(ergebnis.isEmpty());

    Abmessung abmessung = createAbmessung();
    IndexDimensionsFactory.build(abmessung, ergebnis);

    assertEquals(2, ergebnis.size());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertEquals(AttributsTyp.ABMESSUNG.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(REFERENZ_URL, abmessung.getReferenzURL());
    assertNotNull(index.getChanges());
    assertFalse(index.getChanges().isEmpty());
    assertEquals("#UUU-002", index.getChanges().getFirst());

    assertTerm(index, ABMESSUNG_TERM_TYP_TEXT, abmessung.getAbmessungWerte().getFirst().getText());
    assertTerm(index, ABMESSUNG_TERM_TYP_HEIGHT, abmessung.getAbmessungWerte().getFirst().getHoehe());
    assertTerm(index, ABMESSUNG_TERM_TYP_WIDTH, abmessung.getAbmessungWerte().getFirst().getBreite());
    assertTerm(index, ABMESSUNG_TERM_TYP_DEPTH, abmessung.getAbmessungWerte().getFirst().getTiefe());
    assertTerm(index, ABMESSUNG_TERM_TYP_TYPE, abmessung.getAbmessungWerte().getFirst().getTyp());

    index = ergebnis.get(1);

    assertNotNull(index);
    assertEquals(AttributsTyp.ABMESSUNG.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(TEIValues.TEI_ANKER_TAG + abmessung.getReferenzId(), index.getCopyOf());
    assertTerm(index, ABMESSUNG_TERM_TYP_TEXT, abmessung.getAbmessungWerte().get(1).getText());
    assertTerm(index, ABMESSUNG_TERM_TYP_HEIGHT, abmessung.getAbmessungWerte().get(1).getHoehe());
    assertTerm(index, ABMESSUNG_TERM_TYP_WIDTH, abmessung.getAbmessungWerte().get(1).getBreite());
    assertTerm(index, ABMESSUNG_TERM_TYP_DEPTH, abmessung.getAbmessungWerte().get(1).getTiefe());
    assertTerm(index, ABMESSUNG_TERM_TYP_TYPE, abmessung.getAbmessungWerte().get(1).getTyp());
  }

  @Test
  public void testBuild_empty() {
    Abmessung abmessung = Abmessung.builder().build();

    List<Index> ergebnis = new ArrayList<>();

    IndexDimensionsFactory.build(abmessung, ergebnis);

    assertFalse(ergebnis.isEmpty());
    assertEquals(1, ergebnis.size());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertEquals(AttributsTyp.ABMESSUNG.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(0, index.getSources().size());
    assertNotNull(index.getChanges());
    assertEquals(0, index.getChanges().size());
    assertTerm(index, ABMESSUNG_TERM_TYP_TEXT, null);
    assertTerm(index, ABMESSUNG_TERM_TYP_HEIGHT, null);
    assertTerm(index, ABMESSUNG_TERM_TYP_WIDTH, null);
    assertTerm(index, ABMESSUNG_TERM_TYP_DEPTH, null);
    assertTerm(index, ABMESSUNG_TERM_TYP_TYPE, null);
  }

}
