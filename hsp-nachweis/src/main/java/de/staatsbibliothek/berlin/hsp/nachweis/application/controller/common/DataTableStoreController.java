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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import org.primefaces.PrimeFaces;
import org.primefaces.component.api.UIColumn;
import org.primefaces.component.column.Column;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.datatable.DataTableState;
import org.primefaces.event.ColumnResizeEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.event.data.PageEvent;
import org.primefaces.event.data.SortEvent;
import org.primefaces.model.SortMeta;
import org.primefaces.model.Visibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide possibility to store DataTable layout
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 22.06.2017
 */
public class DataTableStoreController implements Serializable {

  public static final String ALL_POSSIBLE_HIT_PRO_PAGE = "5,10,15,25,50,100";
  public static final Integer INIT_HIT_PRO_PAGE = 10;
  public static final Integer FIRST_PAGE = null;
  private static final long serialVersionUID = -7370154446629974912L;
  private static final Logger log = LoggerFactory.getLogger(DataTableStoreController.class);
  private final String filterDatatablePF;
  private final String rootClientId;
  private final List<String> columnsIndex;
  private final List<String> clientIdIndex;
  private final Map<String, String> clientIdsMap;
  private final Map<String, String> columnsMap;
  private final Map<String, Integer> clientIdsPositionMap;
  private final List<SortMeta> initialSorting;
  private final List<Boolean> initVisibleColumns;
  private final List<String> initWidthColumns;

  private final Integer initHitProPage;
  private final String allPossibleHitProPage;
  private Integer currentHitProPage;
  private Integer currentPage;
  private List<Boolean> visibleColumns;
  private List<String> widthColumns;
  private List<String> positionColumns;
  private List<String> positionColumnIDs;
  private Map<String, SortMeta> sortMetaColumns = new LinkedHashMap<>();

  DataTableStoreController() {
    rootClientId = null;
    columnsIndex = null;
    clientIdIndex = null;
    clientIdsMap = null;
    columnsMap = null;
    clientIdsPositionMap = null;
    initialSorting = null;
    filterDatatablePF = null;
    initHitProPage = 0;
    initVisibleColumns = null;
    initWidthColumns = null;
    allPossibleHitProPage = null;
    currentHitProPage = null;
    currentPage = null;
  }

  public DataTableStoreController(String datatableWidgetVar, String rootClientId,
      List<String> columnsIndex,
      List<SortMeta> initialSorting, List<String> initWidthColumns,
      List<Boolean> initVisibleColumns,
      Integer initHitProPage, String allPossibleHitProPage) {
    log.info("C'tor DataTableStoreController Entry");
    clientIdIndex = new ArrayList<>();
    clientIdsMap = new ConcurrentHashMap<>();
    columnsMap = new ConcurrentHashMap<>();
    clientIdsPositionMap = new ConcurrentHashMap<>();

    this.initialSorting = initialSorting;

    this.rootClientId = rootClientId;
    this.columnsIndex = columnsIndex;
    this.initWidthColumns = initWidthColumns;
    this.initVisibleColumns = initVisibleColumns;
    this.filterDatatablePF = "PF('" + datatableWidgetVar + "').filter();";

    this.initHitProPage = initHitProPage;
    this.allPossibleHitProPage = allPossibleHitProPage;
    this.currentPage = null;

    int pos = 0;
    for (String columnId : columnsIndex) {
      String clientIdColumn = getClientIdColumn(rootClientId, columnId);
      clientIdIndex.add(clientIdColumn);
      clientIdsMap.put(clientIdColumn, columnId);
      columnsMap.put(columnId, clientIdColumn);
      clientIdsPositionMap.put(clientIdColumn, pos++);
    }
    setupDataTableStoreController();
    log.debug("C'tor DataTableStoreController Exit");
  }

  private void setupDataTableStoreController() {
    log.info("Post Construct...");
    if (visibleColumns == null) {
      initVisibleColumnsIfRequired();
    }
    if (currentHitProPage == null) {
      currentHitProPage = initHitProPage;
    }
    if (widthColumns == null) {
      initWidthColumnsIfRequired();
    }

    if (positionColumns == null) {
      initPositionColumnsIfRequired();
    }
    initSortMetaColumns();
  }

  public void resetTable() {
    log.debug("ResetTable Start..");
    visibleColumns = null;
    widthColumns = null;
    positionColumns = null;
    sortMetaColumns.clear();
    currentHitProPage = null;
    currentPage = null;
    setupDataTableStoreController();
    clearAllFilters();
    log.debug("ResetTable End.");
  }

  public void initSortMetaColumns() {
    log.debug("initSortMetaColumns Start..");
    if (sortMetaColumns.isEmpty()) {

      FacesContext context = FacesContext.getCurrentInstance();
      if (context == null) {
        for (SortMeta initSort : initialSorting) {
          SortMeta sortMeta = SortMeta.builder().field(initSort.getField())
              .sortBy(initSort.getSortBy()).build();
          sortMetaColumns.put(initSort.getField(), sortMeta);
        }
      } else {
        final UIViewRoot viewRoot = context.getViewRoot();
        if (viewRoot != null) {
          for (SortMeta initSort : initialSorting) {
            log.info("Try to get Sort Field {}", initSort.getField());
            UIColumn columnDate = (UIColumn) viewRoot.findComponent(initSort.getField());
            if (columnDate != null) {
              log.debug("----->>>>> {}",
                  columnDate.getValueExpression(Column.PropertyKeys.sortBy.toString()));
              String columnKey = columnDate.getField();

              SortMeta currentSortMeta = SortMeta.builder()
                  .field(initSort.getField())
                  .order(initSort.getOrder())
                  .build();
              log.info("Column Key = {}", columnKey);
              sortMetaColumns.put(columnKey, currentSortMeta);
            } else {
              SortMeta sortMeta = SortMeta.builder()
                  .field(initSort.getField())
                  .order(initSort.getOrder())
                  .build();
              sortMetaColumns.put(initSort.getField(), sortMeta);
            }
          }
        }
      }
    }
    log.debug("initSortMetaColumns End..");
  }

  public void onToggleColumns(ToggleEvent e) {
    final Integer columnId = (Integer) e.getData();
    log.debug("Got toggle event for the index={}", columnId);
    if (columnId != null && columnId > -1) {
      if (positionColumns.size() > columnId) {
        String clientId = positionColumns.get(columnId);
        log.debug("Looking for {}", clientId);
        int realColumnId = getRealColumnId(clientId);
        if (realColumnId > -1) {
          visibleColumns.set(realColumnId, e.getVisibility() == Visibility.VISIBLE);
          log.debug("Columns toggled {}", visibleColumns);
          reorderColumns();

          refreshDataTable(true);
        }
      }
    }
  }

  public void onSort(SortEvent e) {
    log.debug("onSort {}", e);
    DataTable dt = (DataTable) e.getSource();
    sortMetaColumns.clear();
    sortMetaColumns.putAll(dt.getSortByAsMap());
    log.debug("SortMeta {}", dt.getSortMetaAsString());
  }

  public void onPage(PageEvent e) {
  }

  public boolean isColumnVisible(int columnNumber) {
    boolean result = true;
    if (visibleColumns != null && visibleColumns.size() > columnNumber) {
      result = visibleColumns.get(columnNumber);
    }
    return result;
  }

  public String getColumnWidth(int columnNumber) {
    if (widthColumns == null || widthColumns.size() < columnNumber) {
      return "auto";

    }
    return widthColumns.get(columnNumber);
  }

  public void reorderColumns() {
    initSortMetaColumns();
  }

  public void columnsReordered(AjaxBehaviorEvent event) {
    log.debug("columnsReordered Start..");
    DataTable table = (DataTable) event.getSource();
    positionColumns.clear();
    positionColumnIDs.clear();
    List<UIColumn> columns = table.getColumns();
    log.info("ColumnsCount={}", columns.size());
    for (UIColumn column : columns) {
      final String clientId = column.getClientId();
      String columnName = clientIdsMap.get(clientId);
      log.debug("Process column with the ID={} width={} ({})", clientId, column.getWidth(), columnName);
      if (clientId == null || columnName == null) {
        throw new RuntimeException("Column with ID = '" + clientId + "' doesn't exist in the model " + columnName);
      }
      positionColumns.add(clientId);
      positionColumnIDs.add(columnName);
    }
    log.debug("columnsReordered End..");
  }

  public void columnsResized(ColumnResizeEvent event) {
    log.debug("columnsResized Start..");
    String clientId = event.getColumn().getClientId();
    int width = event.getWidth();
    log.debug("Got resize column event with {} {}", clientId, width);
    int realColumnId = getRealColumnId(clientId);
    if (realColumnId > -1) {
      String widthStr = String.valueOf(width);
      if (!widthColumns.get(realColumnId).equals(widthStr)) {
        widthColumns.set(realColumnId, widthStr);
      }
    }
    log.debug("columnsResized End..");
  }

  public List<Boolean> getVisibleColumns() {
    return visibleColumns;
  }

  public void setVisibleColumns(List<Boolean> visibleColumns) {
    this.visibleColumns = visibleColumns;
  }

  public List<String> getPositionColumns() {
    return positionColumns;
  }

  public void setPositionColumns(List<String> positionColumns) {
    this.positionColumns = positionColumns;
  }

  public Map<String, SortMeta> getSortMetaColumns() {
    return sortMetaColumns;
  }

  public void setSortMetaColumns(Map<String, SortMeta> sortMetaColumns) {
    this.sortMetaColumns = sortMetaColumns;
  }


  public List<String> getPositionColumnIDs() {
    if (positionColumnIDs == null) {
      setupDataTableStoreController();
    }
    return positionColumnIDs;
  }

  public void setPositionColumnIDs(ArrayList<String> positionColumnIDs) {
    this.positionColumnIDs = positionColumnIDs;
  }

  private void initPositionColumnsIfRequired() {
    if (positionColumns == null || visibleColumns.isEmpty()) {
      positionColumns = new ArrayList<>(columnsIndex);
    }
    positionColumnIDs = new ArrayList<>(positionColumns.size());
    positionColumnIDs.addAll(positionColumns);
  }

  public Integer getCurrentHitProPage() {
    return currentHitProPage;
  }

  public void setCurrentHitProPage(Integer currentHitProPage) {
    log.debug("Update current hit pro page to {}", currentHitProPage);
    if (!this.currentHitProPage.equals(currentHitProPage)) {
      currentPage = FIRST_PAGE;
    }
    this.currentHitProPage = currentHitProPage;
  }

  public Integer getCurrentPage() {
    return currentPage;
  }

  public void setCurrentPage(Integer currentPage) {
    log.debug("Page changed to {}", currentPage);
    this.currentPage = currentPage;
  }

  public String getAllPossibleHitProPage() {
    return allPossibleHitProPage;
  }

  private void initWidthColumnsIfRequired() {
    if (widthColumns == null || widthColumns.isEmpty()) {
      widthColumns =
          new ArrayList<>(initWidthColumns);
    }
  }

  private void initVisibleColumnsIfRequired() {
    if (visibleColumns == null || visibleColumns.isEmpty()) {
      visibleColumns =
          new ArrayList<>(initVisibleColumns);
    }
  }

  int getRealColumnId(String clientId) {
    int realColumnId = -1;
    if (clientId != null && clientId.indexOf(':') == -1) {
      clientId = getClientIdColumn(rootClientId, clientId);
    }
    if (clientIdsPositionMap.containsKey(clientId)) {
      realColumnId = clientIdsPositionMap.get(clientId);
    }
    return realColumnId;
  }


  private String getClientIdColumn(String rootClientId, String columnId) {
    return new StringBuilder().append(rootClientId).append(':').append(columnId).toString();
  }

  private void refreshDataTable(boolean withUpdate) {
    PrimeFaces context = PrimeFaces.current();
    if (context != null) {
      try {
        context.executeScript(filterDatatablePF);
        if (withUpdate) {
          context.ajax().update(rootClientId);
        }
      } catch (NullPointerException npe) {
        log.warn("Unable to update component", npe);
      }
    }
  }

  public void clearAllFilters() {
    log.debug("clearAllFilters start");
    FacesContext currentInstance = FacesContext.getCurrentInstance();
    if (currentInstance != null) {
      UIViewRoot viewRoot = currentInstance.getViewRoot();
      if (viewRoot != null) {
        DataTable dataTable = (DataTable) viewRoot.findComponent(rootClientId);
        dataTable.clearLazyCache();
        DataTableState multiViewState = dataTable.getMultiViewState(false);
        if (multiViewState != null) {
          dataTable.resetMultiViewState();
          multiViewState.setRows(currentHitProPage);
          multiViewState.setSortBy(sortMetaColumns);
        }
        dataTable.reset();
        dataTable.clearInitialState();
        log.debug("resetMultiViewState");
        refreshDataTable(true);
      }
    }
    log.debug("clearAllFilters end");
  }

  protected void sendViewMessage(String clientid, FacesMessage message) {

    FacesContext context = FacesContext.getCurrentInstance();

    if (context != null) {
      context.addMessage(clientid, message);
    }

  }
}
