package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
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
  @Serial
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
  private final String widgetVar;

  private final Integer initHitProPage;
  @Getter
  private final String allPossibleHitProPage;
  @Getter
  private Integer currentHitProPage;
  @Getter
  private Integer currentPage;
  @Setter
  @Getter
  private List<Boolean> visibleColumns;
  private List<String> widthColumns;
  @Setter
  @Getter
  private List<String> positionColumns;
  private List<String> positionColumnIDs;
  @Setter
  @Getter
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
    widgetVar = null;
  }

  public DataTableStoreController(String datatableWidgetVar, String rootClientId,
      List<String> columnsIndex,
      List<SortMeta> initialSorting, List<String> initWidthColumns,
      List<Boolean> initVisibleColumns,
      Integer initHitProPage, String allPossibleHitProPage) {
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
    this.widgetVar = datatableWidgetVar;
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
  }

  private void setupDataTableStoreController() {
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
    visibleColumns = null;
    widthColumns = null;
    positionColumns = null;
    sortMetaColumns.clear();
    currentHitProPage = null;
    currentPage = null;
    setupDataTableStoreController();
    resetDataTableToDefaults();
  }

  public void initSortMetaColumns() {
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
  }

  public void onToggleColumns(ToggleEvent e) {
    final Integer columnId = (Integer) e.getData();
    if (columnId != null && columnId > -1) {
      if (positionColumns.size() > columnId) {
        String clientId = positionColumns.get(columnId);
        int realColumnId = getRealColumnId(clientId);
        if (realColumnId > -1) {
          visibleColumns.set(realColumnId, e.getVisibility() == Visibility.VISIBLE);
          reorderColumns();

          refreshDataTable(true);
        }
      }
    }
  }

  public void onSort(SortEvent e) {
    DataTable dt = (DataTable) e.getSource();
    sortMetaColumns.clear();
    sortMetaColumns.putAll(dt.getSortByAsMap());
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
      if (clientId == null || columnName == null) {
        throw new RuntimeException(
            "Column with ID = '" + clientId + "' doesn't exist in the model " + columnName);
      }
      positionColumns.add(clientId);
      positionColumnIDs.add(columnName);
    }
  }

  public void columnsResized(ColumnResizeEvent event) {
    String clientId = event.getColumn().getClientId();
    int width = event.getWidth();
    int realColumnId = getRealColumnId(clientId);
    if (realColumnId > -1) {
      String widthStr = String.valueOf(width);
      if (!widthColumns.get(realColumnId).equals(widthStr)) {
        widthColumns.set(realColumnId, widthStr);
      }
    }
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

  public void setCurrentHitProPage(Integer newPageSize) {

    if (!Objects.equals(this.currentHitProPage, newPageSize)) {

      this.currentHitProPage = newPageSize;
      this.currentPage = 0;

      onRowsChange();
    }
  }

  protected void onRowsChange() {
    if (widgetVar == null) {
      log.warn("{}: widgetVar is null → paginator reset skipped",
          getClass().getSimpleName());
      return;
    }

    PrimeFaces.current().executeScript(
        "PF('" + widgetVar + "').getPaginator().setPage(0);"
    );

    log.debug("{}: paginator moved to first page",
        getClass().getSimpleName());
  }

  public void setCurrentPage(Integer currentPage) {
    log.debug("Page changed to {}", currentPage);
    this.currentPage = currentPage;
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
    return rootClientId + ':' + columnId;
  }

  protected void refreshDataTable(boolean withUpdate) {
    PrimeFaces context = PrimeFaces.current();
    if (context != null) {
      try {
        context.executeScript(filterDatatablePF);
        if (withUpdate) {
          context.executeScript(
              "PrimeFaces.requestIdleCallback(function() { PrimeFaces.ajax.Request.handle({source: '"
                  + rootClientId + "'}); });");
        }
      } catch (NullPointerException npe) {
        log.warn("Unable to update component", npe);
      }
    }
  }

  public void resetDataTableToDefaults() {
    DataTable dataTable = findDataTableSafely();
    if (dataTable == null) {
      return;
    }

    resetMultiViewState(dataTable);
    resetVisualState(dataTable);

    refreshDataTable(true);
  }

  private DataTable findDataTableSafely() {
    FacesContext context = FacesContext.getCurrentInstance();
    if (context == null) {
      return null;
    }

    UIViewRoot viewRoot = context.getViewRoot();
    if (viewRoot == null) {
      return null;
    }

    UIComponent component = viewRoot.findComponent(rootClientId);
    if (!(component instanceof DataTable)) {
      log.error("DataTable with id {} not found in view.", rootClientId);
      return null;
    }

    return (DataTable) component;
  }

  private void resetMultiViewState(DataTable dataTable) {
    dataTable.resetMultiViewState();
    DataTableState multiViewState = dataTable.getMultiViewState(true);
    if (multiViewState != null) {
      multiViewState.setRows(currentHitProPage);
      multiViewState.setSortBy(sortMetaColumns);
      restoreColumnVisibility(multiViewState);
    }
  }

  private void resetVisualState(DataTable dataTable) {
    dataTable.reset();
    dataTable.clearInitialState();
  }

  protected void restoreColumnVisibility(DataTableState multiViewState) {
    if (visibleColumns == null || initVisibleColumns == null || positionColumnIDs == null) {
      return;
    }

    log.debug(
        "Restoring default visible columns (initVisibleColumns size = {}, positionColumnIDs size = {})",
        initVisibleColumns.size(), positionColumnIDs.size());

    multiViewState.getColumnMeta().forEach((name, columnMeta) -> {
      boolean matched = false;
      for (int i = 0; i < positionColumnIDs.size(); i++) {
        if (name.endsWith(":" + positionColumnIDs.get(i))) {
          Boolean flag = (i < initVisibleColumns.size()) ? initVisibleColumns.get(i) : null;
          columnMeta.setVisible(flag == null || flag);
          if (i >= initVisibleColumns.size()) {
            log.warn(
                "Index {} out of bounds for initVisibleColumns (size = {}) — fallback visible=true",
                i, initVisibleColumns.size());
          }
          matched = true;
          break;
        }
      }
      if (!matched) {
        columnMeta.setVisible(false);
        log.warn("Unknown column clientId: {} — set to invisible", name);
      }
    });

  }

  protected void sendViewMessage(String clientid, FacesMessage message) {

    FacesContext context = FacesContext.getCurrentInstance();

    if (context != null) {
      context.addMessage(clientid, message);
    }
  }
}
