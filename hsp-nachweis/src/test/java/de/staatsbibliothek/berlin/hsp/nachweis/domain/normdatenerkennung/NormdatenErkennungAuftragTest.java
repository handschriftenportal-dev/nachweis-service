package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung;

import static org.junit.jupiter.api.Assertions.*;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NormdatenErkennungAuftragTest {
  private NormdatenErkennungAuftrag auftrag;
  private Bearbeiter bearbeiter;

  @BeforeEach
  void setUp() {
    bearbeiter = new Bearbeiter("1", "Test");
    auftrag = new NormdatenErkennungAuftrag("beschreibung-uuid", bearbeiter);
  }

  @Test
  void testContructorSetsInitialState() {
    assertNotNull(auftrag.getId());
    assertEquals("beschreibung-uuid", auftrag.getBeschreibungsdokumentId());
    assertEquals(NormdatenErkennungAuftragStatus.PENDING, auftrag.getStatus());
    assertEquals(bearbeiter, auftrag.getBearbeiter());
    assertNotNull(auftrag.getCreatedAt());
    assertNull(auftrag.getUpdatedAt());
    assertNull(auftrag.getAppliedBy());
  }

  @Test
  void testStartProcessing() {
    auftrag.startProcessing();
    assertEquals(NormdatenErkennungAuftragStatus.PROCESSING, auftrag.getStatus());
  }

  @Test
  void testCompleteProcessing() {
    auftrag.finishProcessing();
    assertEquals(NormdatenErkennungAuftragStatus.AWAITING_REVIEW, auftrag.getStatus());
  }

  @Test
  void testStartApplying() {
    auftrag.startApplying(bearbeiter);
    assertEquals(NormdatenErkennungAuftragStatus.APPLYING, auftrag.getStatus());
    assertEquals(bearbeiter, auftrag.getAppliedBy());
  }

  @Test
  void testCompleteApplying() {
    auftrag.completeApplying();
    assertEquals(NormdatenErkennungAuftragStatus.APPLIED, auftrag.getStatus());
    assertNotNull(auftrag.getUpdatedAt());
  }

  @Test
  void testMarkError() {
    auftrag.markError("something failed");
    assertEquals(NormdatenErkennungAuftragStatus.ERROR, auftrag.getStatus());
    assertEquals("something failed", auftrag.getErrorMessage());
  }

  @Test
  void testCanBeEdited() {
    assertFalse(auftrag.isAwaitingReview());
    auftrag.finishProcessing();
    assertTrue(auftrag.isAwaitingReview());
  }
}
