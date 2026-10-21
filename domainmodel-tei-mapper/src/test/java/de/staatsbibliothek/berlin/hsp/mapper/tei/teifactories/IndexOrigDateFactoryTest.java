package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_NOT_AFTER;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_NOT_BEFORE;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_TEXT;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.ENTSTEHUNGSZEIT_TERM_TYP_TYPE;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.assertTerm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Entstehungszeit;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.EntstehungszeitWert;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Index;

class IndexOrigDateFactoryTest {

  private static final String REFERENZ_URL = "https://resolver.spk-berlin.de/123456";

  static Entstehungszeit createEntstehungszeit() {
    return Entstehungszeit.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("OD-1")
        .withReferenzURL(REFERENZ_URL)
        .withChangeId("ZZZ-010")
        .addEntstehungszeitWert(EntstehungszeitWert.builder()
            .withText("1446 oder vorher (Fasz. I)")
            .withNichtVor("1426")
            .withNichtNach("1446")
            .withTyp("datable")
            .build())
        .addEntstehungszeitWert(
            EntstehungszeitWert.builder()
                .withText("1446 (Fasz. II)")
                .withNichtVor("1446")
                .withNichtNach("1446")
                .withTyp("dated")
                .build())
        .build();
  }

  @Test
  void testBuild() {
    List<Index> ergebnis = new ArrayList<>();

    IndexOrigDateFactory.build(null, ergebnis);
    assertTrue(ergebnis.isEmpty());

    Entstehungszeit entstehungszeit = createEntstehungszeit();

    IndexOrigDateFactory.build(entstehungszeit, ergebnis);

    assertFalse(ergebnis.isEmpty());
    assertEquals(2, ergebnis.size());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertEquals(AttributsTyp.ENTSTEHUNGSZEIT.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(TEIValues.TEI_ANKER_TAG+entstehungszeit.getReferenzId(),index.getCopyOf());
    assertEquals(REFERENZ_URL,index.getSources().getFirst());
    assertNotNull(index.getChanges());
    assertFalse(index.getChanges().isEmpty());
    assertEquals("#ZZZ-010", index.getChanges().getFirst());

    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_TEXT,
        entstehungszeit.getEntstehungszeitWerte().getFirst().getText());
    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_NOT_BEFORE,
        entstehungszeit.getEntstehungszeitWerte().getFirst().getNichtVor());
    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_NOT_AFTER,
        entstehungszeit.getEntstehungszeitWerte().getFirst().getNichtNach());
    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_TYPE,
        entstehungszeit.getEntstehungszeitWerte().getFirst().getTyp());

    index = ergebnis.get(1);

    assertNotNull(index);
    assertEquals(AttributsTyp.ENTSTEHUNGSZEIT.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());

    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_TEXT,
        entstehungszeit.getEntstehungszeitWerte().get(1).getText());
    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_NOT_BEFORE,
        entstehungszeit.getEntstehungszeitWerte().get(1).getNichtVor());
    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_NOT_AFTER,
        entstehungszeit.getEntstehungszeitWerte().get(1).getNichtNach());
    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_TYPE,
        entstehungszeit.getEntstehungszeitWerte().get(1).getTyp());
  }

  @Test
  void testBuild_empty() {
    Entstehungszeit entstehungszeit = Entstehungszeit.builder().build();

    List<Index> ergebnis = new ArrayList<>();

    IndexOrigDateFactory.build(entstehungszeit, ergebnis);

    assertFalse(ergebnis.isEmpty());
    assertEquals(1, ergebnis.size());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertEquals(AttributsTyp.ENTSTEHUNGSZEIT.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(0, index.getSources().size());
    assertNotNull(index.getChanges());
    assertEquals(0, index.getChanges().size());
    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_TEXT, null);
    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_NOT_BEFORE, null);
    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_NOT_AFTER, null);
    assertTerm(index, ENTSTEHUNGSZEIT_TERM_TYP_TYPE, null);
  }

}
