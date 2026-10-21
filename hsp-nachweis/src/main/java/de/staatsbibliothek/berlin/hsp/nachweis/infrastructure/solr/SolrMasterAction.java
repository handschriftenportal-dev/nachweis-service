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
