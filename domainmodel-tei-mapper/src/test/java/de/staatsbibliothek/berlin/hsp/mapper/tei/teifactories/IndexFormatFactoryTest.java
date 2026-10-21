package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.FORMAT_TERM_NORM;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.FORMAT_TERM_TEXT_TYPE;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.assertTerm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Format;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.FormatWert;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Index;

class IndexFormatFactoryTest {

  public static final String REFERENZ_URL = "https://resolver.spk-berlin.de/123456";

  static Format createFormat() {
    return Format.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("FO-1")
        .withReferenzURL(REFERENZ_URL)
        .withChangeId("TTT-005")
        .addFormatWert(FormatWert.builder()
            .withReferenz(
                new NormdatenReferenz(
                    "ND-001", "long and narrow", "GND-001"))
            .withTyp("factual")
            .build())
        .addFormatWert(
            FormatWert.builder()
                .withReferenz(
                    new NormdatenReferenz(
                        "ND-002", "larger than folio", "GND-002"))
                .withTyp("computed")
                .build())
        .build();
  }

  @Test
  void testBuild() {
    List<Index> ergebnis = new ArrayList<>();

    IndexFormatFactory.getInstance().build(null, ergebnis);
    assertTrue(ergebnis.isEmpty());

    Format format = createFormat();

    IndexFormatFactory.getInstance().build(format, ergebnis);

    assertFalse(ergebnis.isEmpty());
    assertEquals(2, ergebnis.size());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertEquals(AttributsTyp.FORMAT.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertNotNull(index.getChanges());
    assertFalse(index.getChanges().isEmpty());
    assertEquals("#TTT-005", index.getChanges().getFirst());

    assertTerm(index, FORMAT_TERM_NORM, "long and narrow");
    assertTerm(index, FORMAT_TERM_TEXT_TYPE, format.getFormatWerte().getFirst().getTyp());

    index = ergebnis.get(1);

    assertNotNull(index);
    assertEquals(AttributsTyp.FORMAT.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(REFERENZ_URL, index.getSources().getFirst());
    assertEquals(TEIValues.TEI_ANKER_TAG + format.getReferenzId(), index.getCopyOf());

    assertTerm(index, FORMAT_TERM_NORM, "larger than folio");
    assertTerm(index, FORMAT_TERM_TEXT_TYPE, format.getFormatWerte().get(1).getTyp());
  }

  @Test
  void testBuild_empty() {
    Format format = Format.builder().build();

    List<Index> ergebnis = new ArrayList<>();

    IndexFormatFactory.getInstance().build(format, ergebnis);

    assertFalse(ergebnis.isEmpty());
    assertEquals(1, ergebnis.size());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertEquals(AttributsTyp.FORMAT.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(0, index.getSources().size());
    assertNotNull(index.getChanges());
    assertEquals(0, index.getChanges().size());
    assertTerm(index, FORMAT_TERM_NORM, null);
    assertTerm(index, FORMAT_TERM_TEXT_TYPE, null);
  }
}
