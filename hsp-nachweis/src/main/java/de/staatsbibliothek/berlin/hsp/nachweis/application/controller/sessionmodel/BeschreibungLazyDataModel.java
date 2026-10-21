package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.HspLazyDataModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.views.BeschreibungListDbView;
import jakarta.inject.Inject;
import java.io.Serial;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.primefaces.model.FilterMeta;

public class BeschreibungLazyDataModel extends HspLazyDataModel<BeschreibungListDbView> {

  @Serial
  private static final long serialVersionUID = 3080177565246695274L;

  BeschreibungsBoundary beschreibungsBoundary;

  @Inject
  public BeschreibungLazyDataModel(BeschreibungsBoundary beschreibungsBoundary) {
    this.beschreibungsBoundary = beschreibungsBoundary;
  }

  @Override
  public List<BeschreibungListDbView> loadMapped(int firstEntry, int pageSize,
      Map<String, Boolean> sortByAsc,
      Map<String, String> filterBy) {
    return beschreibungsBoundary.findFilteredBeschreibungListDTOs(
        firstEntry,
        pageSize,
        filterBy,
        sortByAsc
    );
  }

  @Override
  public BeschreibungListDbView getRowData(String rowKey) {
    return beschreibungsBoundary.findBeschreibungListDTOId(rowKey);
  }

  @Override
  public String getRowKey(BeschreibungListDbView object) {
    return Optional.ofNullable(object).map(BeschreibungListDbView::getHspId).orElse(null);
  }

  @Override
  public int count(Map<String, FilterMeta> map) {
    if (map == null || map.isEmpty()) {
      return Math.toIntExact(beschreibungsBoundary.count());
    }
    return beschreibungsBoundary.countFilteredBeschreibungListDTOs(mapFilterBy(map));
  }
}
