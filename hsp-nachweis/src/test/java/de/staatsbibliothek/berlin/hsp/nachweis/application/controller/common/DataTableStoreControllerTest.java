package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common;

import static de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.DataTableStoreController.ALL_POSSIBLE_HIT_PRO_PAGE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import de.staatsbibliothek.berlin.hsp.nachweis.TestUtils.FacesContextSetter;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.behavior.Behavior;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.primefaces.component.api.UIColumn;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.datatable.DataTableState;
import org.primefaces.event.ColumnResizeEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.event.data.SortEvent;
import org.primefaces.model.ColumnMeta;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.primefaces.model.Visibility;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 28.08.20
 */
class DataTableStoreControllerTest {

  private static final String DATATABLE_WIDGET_VAR = "koerperschaftTableWidget";
  private static final String ROOT_CLIENT_ID = "koerperschaftform:koerperschaftTable";
  private static final String ID_PREFERRED_NAME = "preferredName";
  private static final String ID_ALTERNATIVE_NAMEN = "alternativeNamen";
  private static final String ID_KOERPERSCHAFT_ID = "koerperschaftId";
  private static final String ID_GND_IDENTIFIER = "gndIdentifier";
  private static final String ID_ISIL_ID = "isilID";
  private static final List<String> COLUMS_INDEX = new ArrayList<>(
      Arrays
          .asList(ID_PREFERRED_NAME, ID_ALTERNATIVE_NAMEN, ID_KOERPERSCHAFT_ID, ID_GND_IDENTIFIER,
              ID_ISIL_ID));
  private static final List<Boolean> COLUMNS_VISABLE = new ArrayList<>(
      Arrays.asList(true, true, true, true, true));
  private static final List<String> COLUMNS_WIDTH = new ArrayList<>(
      Arrays
          .asList("300", "655", "150", "100", "100"));
  private static final List<SortMeta> INITIAL_SORTING = Arrays
      .asList(
              SortMeta.builder().field("#{koerperschaft.preferredName}").order(SortOrder.ASCENDING).build());
  private static final Integer INIT_HIT_PRO_PAGE = 5;
  UIComponent uiComponent = Mockito.mock(UIComponent.class);
  Behavior behavior = Mockito.mock(Behavior.class);
  private DataTableStoreController dtsc;

  @BeforeEach
  void setUp() {
    dtsc = new DataTableStoreController(DATATABLE_WIDGET_VAR, ROOT_CLIENT_ID, COLUMS_INDEX,
        INITIAL_SORTING, COLUMNS_WIDTH, COLUMNS_VISABLE, INIT_HIT_PRO_PAGE, ALL_POSSIBLE_HIT_PRO_PAGE);
  }

  @AfterEach
  void tearDown() {
    FacesContextSetter.set(null);
  }

  @Test
  void testResetTable() {
    assertEquals(1, dtsc.getSortMetaColumns().size());
    dtsc.getSortMetaColumns().clear();
    dtsc.resetTable();
    assertEquals(1, dtsc.getSortMetaColumns().size());
  }

  @Test
  void testInitSortMetaColumns() {
    assertEquals(1, dtsc.getSortMetaColumns().size());
    dtsc.getSortMetaColumns().clear();
    dtsc.initSortMetaColumns();
    assertEquals(1, dtsc.getSortMetaColumns().size());
  }

  @Test
  void testOnToggleColumns() {
    assertTrue(dtsc.isColumnVisible(1));
    ToggleEvent event = new ToggleEvent(uiComponent, behavior, Visibility.HIDDEN, 1);
    dtsc.onToggleColumns(event);
    assertFalse(dtsc.isColumnVisible(1));
  }

  @Test
  void testOnSort() {
    SortEvent sortEvent = Mockito.mock(SortEvent.class);
    DataTable dataTable = Mockito.mock(DataTable.class);
    when(sortEvent.getSource()).thenReturn(dataTable);
    when(dataTable.getSortByAsMap()).thenReturn(new HashMap<>());
    when(dataTable.getSortMetaAsString()).thenReturn("");
    assertFalse(dtsc.getSortMetaColumns().isEmpty());
    dtsc.onSort(sortEvent);
    assertTrue(dtsc.getSortMetaColumns().isEmpty());
  }

  @Test
  void testIsColumnVisible() {
    assertTrue(dtsc.isColumnVisible(1));
  }

  @Test
  void testGetColumnWidth() {
    assertEquals(COLUMNS_WIDTH.get(1), dtsc.getColumnWidth(1));
  }

  @Test
  void testReorderColumns() {
    assertEquals(1, dtsc.getSortMetaColumns().size());
    dtsc.getSortMetaColumns().clear();
    assertEquals(0, dtsc.getSortMetaColumns().size());
    dtsc.reorderColumns();
    assertEquals(1, dtsc.getSortMetaColumns().size());
  }

  @Test
  void testColumnsReordered() {
    AjaxBehaviorEvent ajaxBehaviorEvent = Mockito.mock(AjaxBehaviorEvent.class);
    DataTable dataTable = Mockito.mock(DataTable.class);
    when(ajaxBehaviorEvent.getSource()).thenReturn(dataTable);
    UIColumn uiColumn = Mockito.mock(UIColumn.class);
    String testClientID = ROOT_CLIENT_ID + ":" + ID_ALTERNATIVE_NAMEN;
    String newWidth = "10000";

    when(uiColumn.getClientId()).thenReturn(testClientID);
    when(uiColumn.getWidth()).thenReturn(newWidth);
    when(dataTable.getColumns()).thenReturn(List.of(uiColumn));
    assertEquals(COLUMNS_WIDTH.size(), dtsc.getPositionColumnIDs().size());
    assertEquals(COLUMNS_WIDTH.size(), dtsc.getPositionColumns().size());
    dtsc.columnsReordered(ajaxBehaviorEvent);

    assertEquals(1, dtsc.getPositionColumnIDs().size());
    assertEquals(1, dtsc.getPositionColumns().size());
  }

  @Test
  void testColumnsResized() {
    String testClientID = ROOT_CLIENT_ID + ":" + ID_ALTERNATIVE_NAMEN;
    String newWidth = "42";
    ColumnResizeEvent columnResizeEvent = Mockito.mock(ColumnResizeEvent.class);
    UIColumn uiColumn = Mockito.mock(UIColumn.class);
    when(uiColumn.getClientId()).thenReturn(testClientID);
    when(uiColumn.getWidth()).thenReturn(newWidth);
    when(columnResizeEvent.getColumn()).thenReturn(uiColumn);
    when(columnResizeEvent.getWidth()).thenReturn(42);
    int position = dtsc.getRealColumnId(testClientID);
    assertEquals(COLUMNS_WIDTH.get(position), dtsc.getColumnWidth(position));
    dtsc.columnsResized(columnResizeEvent);
    assertEquals(newWidth, dtsc.getColumnWidth(position));
  }

  @Test
  void testGetVisibleColumns() {
    assertEquals(COLUMNS_VISABLE.size(), dtsc.getVisibleColumns().size());
  }

  @Test
  void testSetVisibleColumns() {
    assertEquals(COLUMNS_VISABLE.size(), dtsc.getVisibleColumns().size());
    dtsc.setVisibleColumns(Collections.emptyList());
    assertEquals(0, dtsc.getVisibleColumns().size());
  }

  @Test
  void testGetPositionColumns() {
    assertEquals(COLUMS_INDEX.size(), dtsc.getPositionColumns().size());
  }

  @Test
  void testSetPositionColumns() {
    assertEquals(COLUMS_INDEX.size(), dtsc.getPositionColumns().size());
    dtsc.setPositionColumns(Collections.emptyList());
    assertEquals(0, dtsc.getPositionColumns().size());
  }

  @Test
  void testGetSortMetaColumns() {
    assertEquals(1, dtsc.getSortMetaColumns().size());
  }

  @Test
  void testSetSortMetaColumns() {
    assertEquals(1, dtsc.getSortMetaColumns().size());
    dtsc.setSortMetaColumns(Collections.emptyMap());
    assertEquals(0, dtsc.getSortMetaColumns().size());
  }

  @Test
  void testGetPositionColumnIDs() {
    assertEquals(COLUMS_INDEX.size(), dtsc.getPositionColumnIDs().size());
  }

  @Test
  void testSetPositionColumnIDs() {
    assertEquals(COLUMS_INDEX.size(), dtsc.getPositionColumnIDs().size());
    dtsc.setPositionColumnIDs(new ArrayList<>());
    assertEquals(0, dtsc.getPositionColumnIDs().size());
  }

  @Test
  void getCurrentHitProPage() {
    assertEquals(INIT_HIT_PRO_PAGE, dtsc.getCurrentHitProPage());
  }

  @Test
  void getAllPossibleHitProPage() {
    assertEquals(ALL_POSSIBLE_HIT_PRO_PAGE, dtsc.getAllPossibleHitProPage());
  }

  @Test
  void testResetDataTableToDefaults() {
    FacesContext context = mock(FacesContext.class);
    UIViewRoot viewRoot = mock(UIViewRoot.class);
    DataTable dataTable = mock(DataTable.class);
    DataTableState state = new DataTableState();

    state.setColumnMeta(new HashMap<>());

    FacesContextSetter.set(context);

    when(context.getViewRoot()).thenReturn(viewRoot);
    when(viewRoot.findComponent(ROOT_CLIENT_ID)).thenReturn(dataTable);
    when(dataTable.getMultiViewState(true)).thenReturn(state);

    dtsc.resetDataTableToDefaults();

    verify(dataTable).resetMultiViewState();
    verify(dataTable).reset();
    verify(dataTable).clearInitialState();
    verify(dataTable).getMultiViewState(true);

    assertEquals(INIT_HIT_PRO_PAGE, state.getRows(), "Rows per page should be reset");
    assertEquals(dtsc.getSortMetaColumns(), state.getSortBy(), "SortMeta should be restored");
  }

  @Test
  void testRestoreColumnVisibility() {
    DataTableState state = new DataTableState();
    Map<String, ColumnMeta> columnMetaMap = new HashMap<>();

    String clientIdPreferredName = ROOT_CLIENT_ID + ":" + ID_PREFERRED_NAME;
    String clientIdAlternativeNames = ROOT_CLIENT_ID + ":" + ID_ALTERNATIVE_NAMEN;
    String clientIdUnknown = ROOT_CLIENT_ID + ":unknownColumn";

    ColumnMeta preferredNameColumn = new ColumnMeta();
    preferredNameColumn.setVisible(false);
    columnMetaMap.put(clientIdPreferredName, preferredNameColumn);

    ColumnMeta alternativeNamesColumn = new ColumnMeta();
    alternativeNamesColumn.setVisible(false);
    columnMetaMap.put(clientIdAlternativeNames, alternativeNamesColumn);

    ColumnMeta unknownColumn = new ColumnMeta();
    unknownColumn.setVisible(true);
    columnMetaMap.put(clientIdUnknown, unknownColumn);

    state.setColumnMeta(columnMetaMap);

    dtsc.restoreColumnVisibility(state);

    assertAll(
        () -> assertTrue(state.getColumnMeta().get(clientIdPreferredName).getVisible(),
            "preferredName column should be visible"),
        () -> assertTrue(state.getColumnMeta().get(clientIdAlternativeNames).getVisible(),
            "alternativeNames column should be visible"),
        () -> assertFalse(state.getColumnMeta().get(clientIdUnknown).getVisible(),
            "unknown column should be invisible")
    );
  }

  @Test
  void setCurrentHitProPage_shouldResetCurrentPageToZero_whenChanged() {
    DataTableStoreController spy = Mockito.spy(dtsc);

    doNothing().when(spy).onRowsChange();

    spy.setCurrentPage(50);
    spy.setCurrentHitProPage(25);

    assertEquals(0, spy.getCurrentPage());
  }

  @Test
  void setCurrentHitProPage_shouldCallOnRowsChange_whenChanged() {
    DataTableStoreController spy = Mockito.spy(dtsc);

    doNothing().when(spy).onRowsChange();

    spy.setCurrentHitProPage(25);

    verify(spy).onRowsChange();
  }

  @Test
  void setCurrentHitProPage_shouldNotCallOnRowsChange_whenSameValue() {
    DataTableStoreController spy = Mockito.spy(dtsc);

    spy.setCurrentHitProPage(INIT_HIT_PRO_PAGE);

    verify(spy, never()).onRowsChange();
  }
}
