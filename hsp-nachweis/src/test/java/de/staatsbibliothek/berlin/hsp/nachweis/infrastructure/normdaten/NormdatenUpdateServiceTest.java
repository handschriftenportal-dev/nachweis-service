package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.smallrye.graphql.client.typesafe.api.ErrorOr;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NormdatenUpdateServiceTest {

  private NormdatenGraphQLBoundary boundary;
  private NormdatenGraphQLMutationClient mutationClient;
  private LobidOrgPort lobidPort;
  private NormdatenUpdateService service;

  @BeforeEach
  void setUp() {
    boundary = mock(NormdatenGraphQLBoundary.class);
    mutationClient = mock(NormdatenGraphQLMutationClient.class);
    lobidPort = mock(LobidOrgPort.class);
    service = new NormdatenUpdateService(boundary, mutationClient, lobidPort);
  }

  @Test
  void ensureInGraph_returnsExisting_whenAlreadyInGraph() {
    var fact = new GNDEntityFact(null, "gnd1", "Test Person", "Person");
    when(boundary.findOneByIdOrNameAndNodeLabel("gnd1", "Person")).thenReturn(Optional.of(fact));

    var result = service.ensureInGraph("gnd1", NormdatenTyp.Person);

    assertTrue(result.isPresent());
    assertSame(fact, result.get());
    verifyNoInteractions(lobidPort, mutationClient);
  }

  @Test
  @SuppressWarnings("unchecked")
  void ensureInGraph_createsEntity_whenNotInGraph() {
    var entityFact = new GNDEntityFact(null, "gnd1", "Test Person", "Person");
    var lobidFact = mock(LobidEntityFact.class);
    var mutationResult = (ErrorOr<GNDEntityFact>) mock(ErrorOr.class);

    when(boundary.findOneByIdOrNameAndNodeLabel(anyString(), anyString())).thenReturn(Optional.empty());
    when(lobidPort.findByGNDID("gnd1.json")).thenReturn(lobidFact);
    when(lobidFact.toGNDEntityFact("Person")).thenReturn(entityFact);
    when(mutationResult.hasErrors()).thenReturn(false);
    when(mutationResult.get()).thenReturn(entityFact);
    when(mutationClient.create(any())).thenReturn(mutationResult);

    var result = service.ensureInGraph("gnd1", NormdatenTyp.Person);

    assertTrue(result.isPresent());
    assertSame(entityFact, result.get());
  }

  @Test
  void ensureInGraph_returnsEmpty_whenLobidReturnsNull() {
    when(boundary.findOneByIdOrNameAndNodeLabel(anyString(), anyString())).thenReturn(Optional.empty());
    when(lobidPort.findByGNDID(anyString())).thenReturn(null);

    var result = service.ensureInGraph("gnd1", NormdatenTyp.Person);

    assertTrue(result.isEmpty());
    verifyNoInteractions(mutationClient);
  }

  @Test
  @SuppressWarnings("unchecked")
  void ensureInGraph_returnsEmpty_whenMutationFails() {
    var entityFact = new GNDEntityFact(null, "gnd1", "Test Person", "Person");
    var lobidFact = mock(LobidEntityFact.class);
    var mutationResult = (ErrorOr<GNDEntityFact>) mock(ErrorOr.class);

    when(boundary.findOneByIdOrNameAndNodeLabel(anyString(), anyString())).thenReturn(Optional.empty());
    when(lobidPort.findByGNDID(anyString())).thenReturn(lobidFact);
    when(lobidFact.toGNDEntityFact(anyString())).thenReturn(entityFact);
    when(mutationResult.hasErrors()).thenReturn(true);
    when(mutationResult.getErrors()).thenReturn(java.util.List.of());
    when(mutationClient.create(any())).thenReturn(mutationResult);

    var result = service.ensureInGraph("gnd1", NormdatenTyp.Person);

    assertTrue(result.isEmpty());
  }
}
