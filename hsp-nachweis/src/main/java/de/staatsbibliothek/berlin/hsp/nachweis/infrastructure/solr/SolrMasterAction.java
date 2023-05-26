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

import de.staatsbibliothek.berlin.hsp.nachweis.domain.suche.SuchDokument;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 25.02.21
 */
public class SolrMasterAction {

  private final SolrMasterActionType type;
  private List<String> idSToDelete;
  private List<SuchDokument> suchDokumentsToUpdate;

  private SolrMasterAction(SolrMasterActionType type) {
    this.type = type;
  }

  private SolrMasterAction(String... ids) {
    if (ids == null || ids.length == 0) {
      throw new IllegalArgumentException("Please provide at least one ID");
    }
    type = SolrMasterActionType.DELETE;
    idSToDelete = new ArrayList<>(ids.length);
    idSToDelete.addAll(Arrays.asList(ids));
  }

  private SolrMasterAction(boolean deleteBeforeUpdate, SuchDokument... suchDokuments) {
    if (suchDokuments == null || suchDokuments.length == 0) {
      throw new IllegalArgumentException("Please provide at least one SuchDokument");
    }
    if (deleteBeforeUpdate) {
      type = SolrMasterActionType.DELETE_BEFORE_UPDATE;
    } else {
      type = SolrMasterActionType.UPDATE;
    }
    suchDokumentsToUpdate = new ArrayList<>(suchDokuments.length);
    suchDokumentsToUpdate.addAll(Arrays.asList(suchDokuments));
  }

  public static SolrMasterAction newDeleteAll() {
    return new SolrMasterAction(SolrMasterActionType.DELETE_ALL);
  }

  public static SolrMasterAction newDelete(String... ids) {
    return new SolrMasterAction(ids);
  }

  public static SolrMasterAction newUpdate(SuchDokument... suchDokuments) {
    return new SolrMasterAction(true, suchDokuments);
  }

  public static SolrMasterAction newUpdate(boolean deleteBeforeUpdate, SuchDokument... suchDokuments) {
    return new SolrMasterAction(deleteBeforeUpdate, suchDokuments);
  }

  public static SolrMasterAction newCreate(SuchDokument... suchDokuments) {
    return new SolrMasterAction(false, suchDokuments);
  }

  public SolrMasterActionType getType() {
    return type;
  }

  public List<String> getIdSToDelete() {
    return idSToDelete;
  }

  public List<SuchDokument> getSuchDokumentsToUpdate() {
    return suchDokumentsToUpdate;
  }

}
