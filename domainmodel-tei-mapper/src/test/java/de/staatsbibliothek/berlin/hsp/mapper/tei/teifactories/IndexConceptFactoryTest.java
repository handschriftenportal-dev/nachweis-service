package de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories;

import static de.staatsbibliothek.berlin.hsp.mapper.tei.ReflectionTestUtil.findAllSubclasses;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz.NormdatenReferenzBuilder;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.tei_c.ns._1.Term;

class IndexConceptFactoryTest {

  private static final String TEST_TERM_TYPE = "testType";
  private IndexConceptFactory<?> factory;

  @BeforeEach
  void setUp() {
    factory = Mockito.mock(IndexConceptFactory.class, Mockito.CALLS_REAL_METHODS);
  }

  @Test
  void testCreateTerm_withValidNormdatenReferenz() {
    NormdatenReferenz normdatenReferenz = new NormdatenReferenz("NORM1", "Pergament", "001-3");

    Term term = factory.createNormdatenTerm(normdatenReferenz, TEST_TERM_TYPE);

    assertNotNull(term);
    assertEquals(TEST_TERM_TYPE, term.getType());
    assertEquals("NORM1", term.getKey());
    assertEquals("Pergament", term.getContent().getFirst());
    assertTrue(term.getReves().isEmpty());
  }

  @Test
  void testCreateTerm_withNormdatenReferenzWithoutGndID() {
    NormdatenReferenz normdatenReferenz = new NormdatenReferenzBuilder()
        .withId("NORM2")
        .withName("Papyrus")
        .withUri("https://test.uri/Papyrus")
        .build();

    Term term = factory.createNormdatenTerm(normdatenReferenz, TEST_TERM_TYPE);

    assertNotNull(term);
    assertEquals(TEST_TERM_TYPE, term.getType());
    assertEquals("NORM2", term.getKey());
    assertEquals("Papyrus", term.getContent().getFirst());
    assertEquals("https://test.uri/Papyrus", term.getReves().getFirst());
  }

  @Test
  void testCreateTerm_withNullNormdatenReferenz() {
    var term = factory.createNormdatenTerm(null, TEST_TERM_TYPE);
    assertNotNull(term);
    assertEquals(TEST_TERM_TYPE, term.getType());
    assertTrue(term.getContent().isEmpty());
    assertTrue(term.getReves().isEmpty());
    assertNull(term.getKey());
  }

  @Test
  void testCreateTerm_withNormdatenReferenzWithNullId() {
    NormdatenReferenz normdatenReferenz = new NormdatenReferenz(null, "Test", "123");

    var term = factory.createNormdatenTerm(normdatenReferenz, TEST_TERM_TYPE);

    assertNotNull(term);
    assertEquals(TEST_TERM_TYPE, term.getType());
    assertTrue(term.getContent().isEmpty());
    assertTrue(term.getReves().isEmpty());
    assertNull(term.getKey());
  }

  @Test
  void testBuildTermsFromNormdatenReferenzen_withMultipleReferences() {
    List<NormdatenReferenz> normdatenReferenzen = List.of(
        new NormdatenReferenz("NORM1", "Pergament", "001-3"),
        new NormdatenReferenz("NORM2", "Papyrus", null)
    );

    List<Term> terms = factory.createNormdatenTerms(TEST_TERM_TYPE, normdatenReferenzen);

    assertNotNull(terms);
    assertEquals(2, terms.size());

    Term term1 = terms.getFirst();
    assertEquals(TEST_TERM_TYPE, term1.getType());
    assertEquals("NORM1", term1.getKey());
    assertEquals("Pergament", term1.getContent().getFirst());
    assertEquals(0, term1.getReves().size());

    Term term2 = terms.get(1);
    assertEquals(TEST_TERM_TYPE, term2.getType());
    assertEquals("NORM2", term2.getKey());
    assertEquals("Papyrus", term2.getContent().getFirst());
    assertEquals(0, term2.getReves().size());
  }

  @Test
  void testBuildTermsFromNormdatenReferenzen_withEmptyList() {
    List<NormdatenReferenz> normdatenReferenzen = new ArrayList<>();

    List<Term> terms = factory.createNormdatenTerms(TEST_TERM_TYPE, normdatenReferenzen);

    assertNotNull(terms);
    assertEquals(1, terms.size());

    Term term = terms.getFirst();
    assertNotNull(term);
    assertEquals(TEST_TERM_TYPE, term.getType());
    assertTrue(term.getContent().isEmpty());
    assertTrue(term.getReves().isEmpty());
    assertNull(term.getKey());
  }

  @Test
  void testBuildTermsFromNormdatenReferenzen_withNullList() {
    List<Term> terms = factory.createNormdatenTerms(TEST_TERM_TYPE, null);

    assertNotNull(terms);
    assertEquals(1, terms.size());

    Term term = terms.getFirst();
    assertNotNull(term);
    assertEquals(TEST_TERM_TYPE, term.getType());
    assertTrue(term.getContent().isEmpty());
    assertTrue(term.getReves().isEmpty());
    assertNull(term.getKey());

  }

  @Test
  void testCreateTerm_withNormdatenReferenzWithoutName() {
    NormdatenReferenz normdatenReferenz = new NormdatenReferenz("NORM3", null, "003-5");

    Term term = factory.createNormdatenTerm(normdatenReferenz, TEST_TERM_TYPE);

    assertNotNull(term);
    assertEquals(TEST_TERM_TYPE, term.getType());
    assertEquals("NORM3", term.getKey());
    assertTrue(term.getContent().isEmpty());
    assertEquals(0, term.getReves().size());
  }

  @Test
  void testCreateEmptyTerm() {
    Term term = factory.createEmptyTerm(TEST_TERM_TYPE);

    assertNotNull(term);
    assertEquals(TEST_TERM_TYPE, term.getType());
    assertTrue(term.getContent().isEmpty());
    assertTrue(term.getReves().isEmpty());
    assertNull(term.getKey());
  }

  @Test
  void testAllSubclassesHaveStaticGetInstanceMethod() {
    List<Class<?>> subclasses = findAllSubclasses(IndexConceptFactory.class,
        "de.staatsbibliothek.berlin.hsp.mapper.tei.teifactories");

    assertFalse(subclasses.isEmpty(), "Should find at least one subclass of IndexConceptFactory");

    for (Class<?> subclass : subclasses) {
      try {
        Method getInstance = subclass.getDeclaredMethod("getInstance");

        assertTrue(Modifier.isStatic(getInstance.getModifiers()),
            subclass.getSimpleName() + ".getInstance() must be static");

        assertTrue(Modifier.isPublic(getInstance.getModifiers()),
            subclass.getSimpleName() + ".getInstance() must be public");

        assertEquals(subclass, getInstance.getReturnType(),
            subclass.getSimpleName() + ".getInstance() must return " + subclass.getSimpleName());

        assertEquals(0, getInstance.getParameterCount(),
            subclass.getSimpleName() + ".getInstance() must have no parameters");

      } catch (NoSuchMethodException e) {
        throw new AssertionError(
            subclass.getSimpleName() + " must implement public static "
                + subclass.getSimpleName() + " getInstance() method", e);
      }
    }
  }
}
