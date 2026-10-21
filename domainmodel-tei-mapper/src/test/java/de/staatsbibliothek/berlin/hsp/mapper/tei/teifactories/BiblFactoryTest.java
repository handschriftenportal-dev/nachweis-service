package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessource;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ExterneRessourceTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.Forschungsdokumentation;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.tei_c.ns._1.Bibl;
import org.tei_c.ns._1.Ref;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 20.05.2020.
 * @version 1.0
 */
public class BiblFactoryTest {

  private static final String HSP_ID = "HSP-123";
  private static final String RESSOURCE = "Handschriftenforschung und ihr Weg ins Digitale";
  private static final String HSC_URL = "https://handschriftencensus.de/123";
  private static final String TEST_URL = "https://example.org/resource";

  @Test
  void testBuild() {

    Bibl bibl = BiblFactory.build(HSP_ID);

    assertEquals(1, bibl.getContent().size());
    assertEquals(HSP_ID, bibl.getId());
    assertNotEquals("true", bibl.getDefault());
    assertEquals(HSP_ID, ((Ref) bibl.getContent().getFirst()).getTargets().getFirst());
  }

  @Test
  void shouldBuildBiblForExterneRessource() {
    Bibl bibl = BiblFactory.build(new ExterneRessource(ExterneRessourceTyp.HSC, HSC_URL));

    assertEquals("hsc", bibl.getType());
    assertEquals(1, bibl.getContent().size());

    Ref ref = assertInstanceOf(Ref.class, bibl.getContent().getFirst());
    assertEquals(1, ref.getTargets().size());
    assertEquals(HSC_URL, ref.getTargets().getFirst());
    assertEquals(1, ref.getContent().size());
    assertEquals("HSC", ref.getContent().getFirst());
  }

  @ParameterizedTest
  @MethodSource("translatedUiTypes")
  void shouldAlwaysWriteGermanCaptionToTei(
      ExterneRessourceTyp typ,
      String expectedGermanCaption) {
    Bibl bibl = BiblFactory.build(new ExterneRessource(typ, TEST_URL));

    assertEquals(typ.getTeiType(), bibl.getType());
    Ref ref = assertInstanceOf(Ref.class, bibl.getContent().getFirst());
    assertEquals(TEST_URL, ref.getTargets().getFirst());
    assertEquals(expectedGermanCaption, ref.getContent().getFirst());
  }

  @ParameterizedTest
  @EnumSource(ExterneRessourceTyp.class)
  void shouldNotSetDefaultForExterneRessource(ExterneRessourceTyp typ) {
    Bibl bibl = BiblFactory.build(new ExterneRessource(typ, TEST_URL));

    assertNotEquals("true", bibl.getDefault());
  }

  @Test
  void shouldBuildForschungsdokumentationWithLink() {
    Bibl bibl = BiblFactory.build(new Forschungsdokumentation(RESSOURCE, TEST_URL));

    assertEquals(1, bibl.getContent().size());
    Ref ref = assertInstanceOf(Ref.class, bibl.getContent().getFirst());
    assertEquals(TEST_URL, ref.getTargets().getFirst());
    assertEquals(RESSOURCE, ref.getContent().getFirst());
  }

  @Test
  void shouldBuildForschungsdokumentationWithoutLink() {
    Bibl bibl = BiblFactory.build(new Forschungsdokumentation(RESSOURCE, null));

    assertEquals(1, bibl.getContent().size());
    assertEquals(RESSOURCE, bibl.getContent().getFirst());
  }

  @Test
  void shouldRejectNullForschungsdokumentation() {
    assertThrows(
        NullPointerException.class,
        () -> BiblFactory.build((Forschungsdokumentation) null));
  }

  private static Stream<Arguments> translatedUiTypes() {
    return Stream.of(
        Arguments.of(ExterneRessourceTyp.GESCHICHTSQUELLEN, "Geschichtsquellen"),
        Arguments.of(ExterneRessourceTyp.SONSTIGES, "Sonstiges"),
        Arguments.of(ExterneRessourceTyp.BESCHREIBUNG, "Beschreibung"));
  }
}
