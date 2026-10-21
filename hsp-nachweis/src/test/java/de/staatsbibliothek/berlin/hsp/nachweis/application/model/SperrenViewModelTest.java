package de.staatsbibliothek.berlin.hsp.nachweis.application.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreDokumentTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.SperreTyp;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.SperrenViewModel.SperrenViewModelBuilder;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 30.08.2021
 */
public class SperrenViewModelTest {

  @Test
  public void testBuilder() {
    LocalDateTime testDate = LocalDateTime.of(2021, 1, 1, 12, 0, 0);
    SperrenViewModel model = new SperrenViewModelBuilder()
        .withId("1")
        .withBearbeitername("Tester")
        .withStartDatum(testDate)
        .withSperreTyp(SperreTyp.MANUAL)
        .withSperreGrund("Testsperre")
        .withTxId("123xyz")
        .withSperreEintraege(List.of(new SperreEintragViewModel("1", SperreDokumentTyp.BESCHREIBUNG, "HS. 1_1")))
        .addSperreEintrag(new SperreEintragViewModel("2", SperreDokumentTyp.BESCHREIBUNG, "HS. 1"))
        .build();

    assertNotNull(model);
    assertEquals("Tester", model.getBearbeitername());
    assertEquals(testDate, model.getStartDatum());
    assertEquals(SperreTyp.MANUAL, model.getSperreTyp());
    assertEquals("Testsperre", model.getSperreGrund());
    assertEquals("123xyz", model.getTxId());
    assertNotNull(model.getSperreEintraege());
    assertEquals(2, model.getSperreEintraege().size());
    assertNotNull(model.getSperreEintraegeSignaturen());
    assertTrue(model.getSperreEintraegeSignaturen().contains("HS. 1_1"));
    assertNotNull(model.getSperreEintraegeIds());
    assertTrue(model.getSperreEintraegeIds().contains("2"));
  }

  @Test
  public void testEquals() {
    LocalDateTime testDate = LocalDateTime.of(2021, 1, 1, 12, 0, 0);
    SperrenViewModel model1 = new SperrenViewModelBuilder()
        .withId("1")
        .withBearbeitername("Tester")
        .withStartDatum(testDate)
        .withSperreTyp(SperreTyp.MANUAL)
        .withSperreGrund("Testsperre")
        .withTxId("123xyz")
        .withSperreEintraege(List.of(new SperreEintragViewModel("1", SperreDokumentTyp.BESCHREIBUNG, "HS. 1_1")))
        .addSperreEintrag(new SperreEintragViewModel("2", SperreDokumentTyp.BESCHREIBUNG, "HS. 1"))
        .build();

    SperrenViewModel model2 = new SperrenViewModelBuilder()
        .withId("1")
        .withBearbeitername("Tester")
        .withStartDatum(testDate)
        .build();

    SperrenViewModel model3 = new SperrenViewModelBuilder()
        .withId("2")
        .withBearbeitername("Tester 2")
        .withStartDatum(testDate)
        .build();

    SperrenViewModel model4 = new SperrenViewModelBuilder()
        .withId("3")
        .withBearbeitername("Tester")
        .withStartDatum(LocalDateTime.now())
        .build();

    assertEquals(model1, model2);
    assertNotEquals(model1, model3);
    assertNotEquals(model1, model4);
  }
}
