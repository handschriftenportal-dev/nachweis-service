package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues.HANDSCHRIFTENTYP_TERM_NORM;
import static de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories.IndexFactoryTest.assertTerm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Handschriftentyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsReferenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.AttributsTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.mapper.tei.TEIValues;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.tei_c.ns._1.Index;
import org.tei_c.ns._1.Term;

class IndexFormFactoryTest {

  public static final String REFERENZ_URL = "https://resolver.spk-berlin.de/123456";

  static Handschriftentyp createHandschriftentyp() {
    return Handschriftentyp.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzen(List.of(new NormdatenReferenz("NORM-1234", "normText1", "001-3")))
        .withReferenzId("FO-1")
        .withReferenzURL(REFERENZ_URL)
        .withChangeId("KKK-009")
        .build();
  }

  @Test
  void testBuild() {
    List<Index> ergebnis = new ArrayList<>();

    IndexFormFactory.getInstance().build(null, ergebnis);
    assertTrue(ergebnis.isEmpty());

    Handschriftentyp handschriftentyp = createHandschriftentyp();
    IndexFormFactory.getInstance().build(handschriftentyp, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertEquals(AttributsTyp.HANDSCHRIFTENTYP.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(REFERENZ_URL, index.getSources().getFirst());
    assertEquals(TEIValues.TEI_ANKER_TAG + handschriftentyp.getReferenzId(), index.getCopyOf());
    assertNotNull(index.getChanges());
    assertFalse(index.getChanges().isEmpty());
    assertEquals("#KKK-009", index.getChanges().getFirst());
    assertTerm(index, HANDSCHRIFTENTYP_TERM_NORM, handschriftentyp.getText());
  }

  @Test
  void testBuild_empty() {
    List<Index> ergebnis = new ArrayList<>();

    Handschriftentyp handschriftentyp = Handschriftentyp.builder().build();
    IndexFormFactory.getInstance().build(handschriftentyp, ergebnis);

    assertFalse(ergebnis.isEmpty());

    Index index = ergebnis.getFirst();

    assertNotNull(index);
    assertEquals(AttributsTyp.HANDSCHRIFTENTYP.getIndexName(), index.getIndexName());
    assertNotNull(index.getSources());
    assertEquals(0, index.getSources().size());
    assertNotNull(index.getChanges());
    assertEquals(0, index.getChanges().size());
    assertNotNull(index.getTermsAndIndices());
    assertEquals(1, index.getTermsAndIndices().size());
    assertEquals(List.of(), ((Term) index.getTermsAndIndices().getFirst()).getContent());
    assertNull(((Term) index.getTermsAndIndices().getFirst()).getKey());
  }

  @Test
  void testBuild_withFreitextAndNormdaten() {
    List<Index> ergebnis = new ArrayList<>();

    Handschriftentyp handschriftentyp = Handschriftentyp.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("HSP-1")
        .withReferenzURL("https://resolver.spk.de/123")
        .withChangeId("KKK-009")
        .withReferenzen(List.of(
            new NormdatenReferenz.NormdatenReferenzBuilder()
                .withId("NORM-8edc413b-0b70-3366-b78c-fdc091ab3ab2")
                .withUri("https://normdaten.staatsbibliothek-berlin.de/hsp/vocabulary/FORM-X709")
                .withName("fragment")
                .build(),
            new NormdatenReferenz.NormdatenReferenzBuilder()
                .withId("NORM-fcdc9965-677e-3c69-9a70-0c266893e376")
                .withUri("https://normdaten.staatsbibliothek-berlin.de/hsp/vocabulary/FORM-A890")
                .build()
        ))
        .build();

    IndexFormFactory.getInstance().build(handschriftentyp, ergebnis);

    assertFalse(ergebnis.isEmpty());
    Index index = ergebnis.getFirst();

    assertEquals("norm_form", index.getIndexName());
    assertEquals("https://resolver.spk.de/123", index.getSources().getFirst());
    assertEquals("#HSP-1", index.getCopyOf());

    assertNotNull(index.getChanges());
    assertEquals(1, index.getChanges().size());
    assertEquals("#KKK-009", index.getChanges().getFirst());

    List<Term> terms = index.getTermsAndIndices().stream()
        .filter(Term.class::isInstance)
        .map(Term.class::cast)
        .toList();

    assertEquals(2, terms.size());

    Term first = terms.getFirst();
    assertEquals("form", first.getType());
    assertEquals("NORM-8edc413b-0b70-3366-b78c-fdc091ab3ab2", first.getKey());
    assertEquals("https://normdaten.staatsbibliothek-berlin.de/hsp/vocabulary/FORM-X709",
        first.getReves().getFirst());
    assertEquals("fragment", first.getContent().getFirst());

    Term second = terms.get(1);
    assertEquals("form_keyFeature", second.getType());
    assertEquals("NORM-fcdc9965-677e-3c69-9a70-0c266893e376", second.getKey());
    assertEquals("https://normdaten.staatsbibliothek-berlin.de/hsp/vocabulary/FORM-A890",
        second.getReves().getFirst());
    assertTrue(second.getContent().isEmpty());
  }

  @Test
  void testBuild_onlyNormdaten_noFreitext() {
    List<Index> ergebnis = new ArrayList<>();

    Handschriftentyp handschriftentyp = Handschriftentyp.builder()
        .withReferenzTyp(AttributsReferenzTyp.BESCHREIBUNG)
        .withReferenzId("HSP-1")
        .withReferenzURL("https://resolver.spk.de/123")
        .withChangeId("CHANGE-001")
        .withReferenzen(List.of(
            new NormdatenReferenz.NormdatenReferenzBuilder()
                .withId("NORM-111")
                .withUri("https://normdaten.staatsbibliothek-berlin.de/hsp/vocabulary/FORM-X111")
                .build(),
            new NormdatenReferenz.NormdatenReferenzBuilder()
                .withId("NORM-222")
                .withUri("https://normdaten.staatsbibliothek-berlin.de/hsp/vocabulary/FORM-X222")
                .build()
        ))
        .build();

    IndexFormFactory.getInstance().build(handschriftentyp, ergebnis);

    assertFalse(ergebnis.isEmpty());
    Index index = ergebnis.getFirst();

    List<Term> terms = index.getTermsAndIndices().stream()
        .filter(Term.class::isInstance)
        .map(Term.class::cast)
        .toList();

    assertEquals(2, terms.size());

    Term first = terms.getFirst();
    assertEquals("form", first.getType());
    assertEquals("NORM-111", first.getKey());
    assertEquals("https://normdaten.staatsbibliothek-berlin.de/hsp/vocabulary/FORM-X111",
        first.getReves().getFirst());
    assertTrue(first.getContent().isEmpty(), "Freitext sollte nicht vorhanden sein");

    Term second = terms.get(1);
    assertEquals("NORM-222", second.getKey());
    assertEquals("form_keyFeature", second.getType());
    assertTrue(second.getContent().isEmpty());
  }
}
