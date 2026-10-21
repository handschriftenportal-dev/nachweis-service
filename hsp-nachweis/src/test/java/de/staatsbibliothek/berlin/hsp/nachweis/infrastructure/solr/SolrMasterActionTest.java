package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.solr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokument;
import org.junit.jupiter.api.Test;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 31.05.2022
 */
class SolrMasterActionTest {

  @Test
  void testNewDeleteAll() {
    SolrMasterAction action = SolrMasterAction.newDeleteAll();

    assertNotNull(action);
    assertEquals(SolrMasterActionType.DELETE_ALL, action.getType());
    assertNull(action.getIdSToDelete());
    assertNull(action.getSuchDokumentsToUpdate());
  }

  @Test
  void testNewDelete() {
    SolrMasterAction action = SolrMasterAction.newDelete("1", "2", "3");

    assertNotNull(action);
    assertEquals(SolrMasterActionType.DELETE, action.getType());
    assertNotNull(action.getIdSToDelete());
    assertEquals(3, action.getIdSToDelete().size());
    assertNull(action.getSuchDokumentsToUpdate());
  }

  @Test
  void testNewUpdate() {
    SuchDokument suchDokument = SuchDokument.builder().withId("1").build();
    SolrMasterAction action = SolrMasterAction.newUpdate(suchDokument);

    assertNotNull(action);
    assertEquals(SolrMasterActionType.DELETE_BEFORE_UPDATE, action.getType());
    assertNull(action.getIdSToDelete());
    assertNotNull(action.getSuchDokumentsToUpdate());
    assertEquals(1, action.getSuchDokumentsToUpdate().size());
    assertTrue(action.getSuchDokumentsToUpdate().stream().anyMatch(sd -> "1".equals(sd.getId())));
  }

  @Test
  void testNewUpdateNoDeleteBeforeUpdate() {
    SuchDokument suchDokument = SuchDokument.builder().withId("1").build();
    SolrMasterAction action = SolrMasterAction.newUpdate(false, suchDokument);

    assertNotNull(action);
    assertEquals(SolrMasterActionType.UPDATE, action.getType());
    assertNull(action.getIdSToDelete());
    assertNotNull(action.getSuchDokumentsToUpdate());
    assertEquals(1, action.getSuchDokumentsToUpdate().size());
    assertTrue(action.getSuchDokumentsToUpdate().stream().anyMatch(sd -> "1".equals(sd.getId())));
  }

  @Test
  void testNewCreate() {
    SuchDokument suchDokument = SuchDokument.builder().withId("1").build();
    SolrMasterAction action = SolrMasterAction.newCreate(suchDokument);

    assertNotNull(action);
    assertEquals(SolrMasterActionType.UPDATE, action.getType());
    assertNull(action.getIdSToDelete());
    assertNotNull(action.getSuchDokumentsToUpdate());
    assertEquals(1, action.getSuchDokumentsToUpdate().size());
    assertTrue(action.getSuchDokumentsToUpdate().stream().anyMatch(sd -> "1".equals(sd.getId())));
  }

}
