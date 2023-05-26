/*
 * MIT License
 *
 * Copyright (c) 2023 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
