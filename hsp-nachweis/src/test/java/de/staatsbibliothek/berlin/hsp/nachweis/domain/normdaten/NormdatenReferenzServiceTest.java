package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdaten;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.Concept;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenGraphQLBoundary;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 07.12.20
 */
class NormdatenReferenzServiceTest {

  private final NormdatenGraphQLBoundary normdatenGraphQLBoundaryMock = Mockito.mock(
      NormdatenGraphQLBoundary.class);

  private final NormdatenReferenzRepository normdatenReferenzRepositoryMock = Mockito.mock(
      NormdatenReferenzRepository.class);
  private NormdatenReferenzService service = new NormdatenReferenzService(
      normdatenGraphQLBoundaryMock, normdatenReferenzRepositoryMock);

  @Test
  void testFindOneByIdOrNameAndType() {
    service.findOneByIdOrNameAndType("1", NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);
    Mockito.verify(normdatenGraphQLBoundaryMock, Mockito.times(1))
        .findOneByIdOrNameAndNodeLabel("1", NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);
  }

  @Test
  void findAllByIdOrNameAndType() {
    service.findAllByIdOrNameAndType("2", NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME, true);
    Mockito.verify(normdatenGraphQLBoundaryMock, Mockito.times(1))
        .findAllByIdOrNameAndNodeLabel("2", NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME, true);
  }

  @Test
  void testFindSpracheById() {
    service.findSpracheById("de");
    Mockito.verify(normdatenGraphQLBoundaryMock, Mockito.times(1)).findLanguageById("de");
  }

  @Test
  void testFindKoerperschaftenByOrtId() {
    service.findKoerperschaftenByOrtId("123", false);
    Mockito.verify(normdatenGraphQLBoundaryMock, Mockito.times(1))
        .findCorporateBodiesByPlaceId("123", false);
  }

  @Test
  void testCompareUrisWithoutProtocol() {
    assertTrue(service.compareUrisWithoutProtocol("http://uri", "https://uri"));
  }

  @Test
  void testfindConceptWithSchemeTypeAndURI() {

    when(normdatenGraphQLBoundaryMock.findConceptsWithConceptSchemeType("completeness"))
        .thenReturn(java.util.List.of(new Concept("NORM-123",
            "http://normdaten.staatsbibliothek-berlin.de/vocabulary/completeness/complete",
            "completeness")));

    Optional<NormdatenReferenz> normdatenReferenz = service.findConceptWithSchemeTypeAndURI(
        "completeness",
        "http://normdaten.staatsbibliothek-berlin.de/vocabulary/completeness/complete");

    Mockito.verify(normdatenGraphQLBoundaryMock, Mockito.times(1))
        .findConceptsWithConceptSchemeType("completeness");

    assertTrue(normdatenReferenz.isPresent());
  }

  @Test
  void testfindConceptWithConceptSchemeReferenz() {
    NormdatenReferenz normdatenReferenz = new NormdatenReferenz.NormdatenReferenzBuilder().withId(
            "NORM-123")
        .withTypeName("completeness").withName("completeness").withUri(
            "http://normdaten.staatsbibliothek-berlin.de/vocabulary/completeness")
        .build();

    when(normdatenGraphQLBoundaryMock.findConceptsWithConceptSchemeType("completeness"))
        .thenReturn(java.util.List.of(new Concept("NORM-123",
            "http://normdaten.staatsbibliothek-berlin.de/vocabulary/completeness/complete",
            "completeness")));

    Set<NormdatenReferenz> concept = service.findConceptWithConceptSchemeReferenz(
        normdatenReferenz);

    assertEquals(1, concept.size());
  }

  @Test
  void testmapConceptToNormdatenReferenz() {
    NormdatenReferenz conceptScheme = new NormdatenReferenz.NormdatenReferenzBuilder().withId(
            "NORM-123")
        .withTypeName("completeness").withName("completeness").withUri(
            "http://normdaten.staatsbibliothek-berlin.de/vocabulary/completeness")
        .build();

    Concept concept = new Concept("NORM-123",
        "http://normdaten.staatsbibliothek-berlin.de/vocabulary/complete", "complete");

    Function<Concept, NormdatenReferenz> mapper = service.mapConceptToNormdatenReferenz(
        conceptScheme);

    NormdatenReferenz conceptReferenz = mapper.apply(concept);

    assertNotNull(conceptReferenz);

    assertEquals("NORM-123", conceptReferenz.getId());
  }

  @Test
  void testupdateAllNormdatenReferenzen() {

    NormdatenReferenz berlinPlace = new NormdatenReferenz.NormdatenReferenzBuilder().withId(
            "NORM-123")
        .withTypeName("Place").withName("Berlin")
        .build();

    NormdatenReferenz berlinStaabi = new NormdatenReferenz.NormdatenReferenzBuilder().withId(
            "123")
        .withTypeName("CorporateBody").withName("Staabi Berlin")
        .build();

    when(normdatenReferenzRepositoryMock.listAll(false)).thenReturn(
        List.of(berlinPlace, berlinStaabi));

    service.updateAllNormdatenReferenzen();

    verify(normdatenGraphQLBoundaryMock, times(1)).findOneByIdOrNameAndNodeLabel(
        berlinPlace.getId(), null);
  }

  @Test
  void updateNormdatenReferenzWithIDTest() {
    NormdatenReferenz berlinPlace = new NormdatenReferenz.NormdatenReferenzBuilder().withId(
            "NORM-123")
        .withTypeName("Place").withName("Berlin")
        .build();

    GNDEntityFact gndEntityFact = new GNDEntityFact.GNDEntityFactBuilder().withId(
            berlinPlace.getId()).withTypeName(berlinPlace.getTypeName())
        .withPreferredName(berlinPlace.getName()).build();

    when(normdatenGraphQLBoundaryMock.findOneByIdOrNameAndNodeLabel(berlinPlace.getId(),
        null)).thenReturn(Optional.of(gndEntityFact));

    service.updateNormdatenReferenzWithID(berlinPlace.getId());

    verify(normdatenGraphQLBoundaryMock, times(1)).findOneByIdOrNameAndNodeLabel(
        berlinPlace.getId(), null);

    verify(normdatenReferenzRepositoryMock, times(1)).save(berlinPlace);
  }
}
