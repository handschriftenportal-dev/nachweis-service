package de.staatsbibliothek.berlin.hsp.nachweis.domain.normdatenerkennung;

import static org.junit.jupiter.api.Assertions.*;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenTyp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NormdatenErkennungEintragTest {

  private static final Bearbeiter REVIEWER = new Bearbeiter("1", "Reviewer");

  private NormdatenErkennungEintrag response;

  @BeforeEach
  void setUp() {
    response = new NormdatenErkennungEintrag("/persName[1]", NormdatenTyp.Person, "Test Person");
  }

  @Test
  void constructor_setsDefaults() {
    assertNotNull(response.getId());
    assertEquals(NormdatenTyp.Person, response.getNormdatenTyp());
    assertEquals("Test Person", response.getDisplayText());
    assertEquals(NormdatenErkennungAuswahlTyp.NONE, response.getSelectionType());
    assertNotNull(response.getCreatedAt());
    assertNull(response.getUpdatedAt());
    assertNull(response.getManualIdentifier());
    assertNull(response.getReviewedBy());
  }

  @Test
  void setSelectionManualIdentifier_setsTypeAndIdentifier() {
    response.setSelectionManualIdentifier("gnd123", REVIEWER);

    assertEquals(NormdatenErkennungAuswahlTyp.MANUAL, response.getSelectionType());
    assertEquals("gnd123", response.getManualIdentifier());
    assertEquals(REVIEWER, response.getReviewedBy());
    assertNotNull(response.getUpdatedAt());
  }

  @Test
  void setSelectionNone_clearsSelection() {
    response.setSelectionManualIdentifier("gnd123", REVIEWER);

    response.setSelectionNone(REVIEWER);

    assertEquals(NormdatenErkennungAuswahlTyp.NONE, response.getSelectionType());
    assertNull(response.getManualIdentifier());
    assertNull(response.getSelectedMatchIndex());
  }

  @Test
  void setSelectionRecommendation_doesNothing_whenNoErgebnis() {
    response.setSelectionRecommendation(REVIEWER);

    assertEquals(NormdatenErkennungAuswahlTyp.NONE, response.getSelectionType());
  }

  @Test
  void setSelectionMatchIndex_doesNothing_whenNoErgebnis() {
    response.setSelectionMatchIndex(0, REVIEWER);

    assertEquals(NormdatenErkennungAuswahlTyp.NONE, response.getSelectionType());
  }

  @Test
  void setErrorMessage_storesMessage() {
    response.setErrorMessage("something failed");

    assertEquals("something failed", response.getErrorMessage());
  }

  @Test
  void getSelectedTreffer_returnsNull_whenSelectionIsNone() {
    assertNull(response.getSelectedTreffer());
  }
}
