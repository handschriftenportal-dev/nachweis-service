package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.sessionmodel;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.HspLazyDataModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.kod.KulturObjektDokumentBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.persistence.KulturObjektDokumentListDTO;
import java.io.Serial;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.primefaces.model.FilterMeta;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 26.03.25.
 * @version 1.0
 */
class KulturObjektDokumentLazyDataModel extends
    HspLazyDataModel<KulturObjektDokumentListDTO> {

  @Serial
  private static final long serialVersionUID = 3080177565246695274L;

  private Map<String, Boolean> sortByBoolean;
  private Map<String, String> filterByString;
  private final KulturObjektDokumentBoundary kulturObjektDokumentBoundary;

  KulturObjektDokumentLazyDataModel(
      KulturObjektDokumentBoundary kulturObjektDokumentBoundary) {
    this.kulturObjektDokumentBoundary = kulturObjektDokumentBoundary;
  }

  @Override
  public List<KulturObjektDokumentListDTO> loadMapped(int firstEntry, int pageSize,
      Map<String, Boolean> sortByAsc,
      Map<String, String> filterBy) {

    if (!filterBy.equals(filterByString) || !sortByAsc.equals(sortByBoolean)) {
      filterByString = filterBy;
      sortByBoolean = sortByAsc;
      int totalResults = kulturObjektDokumentBoundary.countFilteredKulturObjektDokumentListDTOs(
          filterByString);
      this.setRowCount(totalResults);
    }

    return kulturObjektDokumentBoundary.findFilteredKulturObjektDokumentListDTOs(firstEntry,
        pageSize,
        filterByString, sortByBoolean);
  }

  @Override
  public KulturObjektDokumentListDTO getRowData(String rowKey) {
    return kulturObjektDokumentBoundary.findKulturObjektDokumentListDTOById(rowKey);
  }

  @Override
  public String getRowKey(KulturObjektDokumentListDTO object) {
    return Optional.ofNullable(object).map(KulturObjektDokumentListDTO::getHspId).orElse(null);
  }

  @Override
  public int count(Map<String, FilterMeta> map) {

    if (filterByString == null) {
      return Math.toIntExact(kulturObjektDokumentBoundary.count());
    }

    return kulturObjektDokumentBoundary.countFilteredKulturObjektDokumentListDTOs(
        filterByString);
  }
}
