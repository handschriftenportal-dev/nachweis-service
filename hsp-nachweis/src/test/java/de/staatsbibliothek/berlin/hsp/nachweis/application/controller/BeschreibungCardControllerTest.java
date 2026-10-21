package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.ApprovalEvent;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.ApprovalRequest;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Publikation;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Sperre;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ApprovalEventType;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.ApprovalStatus;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.LizenzTyp;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PublikationStatus;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BeschreibungsRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ApprovalEventViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.ApprovalRequestViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.BeschreibungsViewModel.BeschreibungsViewBuilder;
import de.staatsbibliothek.berlin.hsp.nachweis.application.model.PublikationViewModel;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.beschreibung.BeschreibungsException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.PublikationsService;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.DokumentSperreException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.sperre.exception.SperreAlreadyExistException;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ValidationResult;
import de.staatsbibliothek.berlin.javaee.authentication.entity.Authentication;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;

class BeschreibungCardControllerTest {

  private BeschreibungCardController controller;
  private PublikationsService publikationsService;
  private BearbeiterBoundary bearbeiterBoundary;
  private BeschreibungsBoundary beschreibungsBoundary;
  private BeschreibungsRechte beschreibungsRechte;
  private KulturObjektDokumentViewController kulturObjektDokumentViewController;

  private FacesContext facesContext;
  private ExternalContext externalContext;
  private MockedStatic<FacesContext> facesContextStatic;
  private PrimeFaces primeFaces;
  private PrimeFaces.Dialog primeFacesDialog;
  private PrimeFaces.Ajax primeFacesAjax;
  private MockedStatic<PrimeFaces> primeFacesStatic;

  @BeforeEach
  void setUp() {
    publikationsService = mock(PublikationsService.class);
    bearbeiterBoundary = mock(BearbeiterBoundary.class);
    beschreibungsBoundary = mock(BeschreibungsBoundary.class);
    beschreibungsRechte = mock(BeschreibungsRechte.class);
    kulturObjektDokumentViewController = mock(KulturObjektDokumentViewController.class);

    facesContext = mock(FacesContext.class);
    externalContext = mock(ExternalContext.class);
    when(facesContext.getExternalContext()).thenReturn(externalContext);

    facesContextStatic = mockStatic(FacesContext.class);
    facesContextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

    primeFaces = mock(PrimeFaces.class);
    primeFacesDialog = mock(PrimeFaces.Dialog.class);
    primeFacesAjax = mock(PrimeFaces.Ajax.class);
    when(primeFaces.dialog()).thenReturn(primeFacesDialog);
    when(primeFaces.ajax()).thenReturn(primeFacesAjax);

    primeFacesStatic = mockStatic(PrimeFaces.class);
    primeFacesStatic.when(PrimeFaces::current).thenReturn(primeFaces);

    controller = new BeschreibungCardController(publikationsService, bearbeiterBoundary,
        beschreibungsBoundary, beschreibungsRechte,
        kulturObjektDokumentViewController);
  }

  @AfterEach
  void tearDown() {
    primeFacesStatic.close();
    facesContextStatic.close();
  }

  // ==========================================================================
  // init() / PostConstruct
  // ==========================================================================

  @Nested
  @DisplayName("init()")
  class InitTests {

    @Test
    void test_init_sets_beschreibungID_from_request_parameter() {
      when(externalContext.getRequestParameterMap()).thenReturn(
          Map.of("beschreibungID", "B-123"));

      controller.init();

      assertEquals("B-123", controller.getBeschreibungID());
    }

    @Test
    void test_init_with_no_beschreibungID_parameter_sets_null() {
      when(externalContext.getRequestParameterMap()).thenReturn(Map.of());

      controller.init();

      assertNull(controller.getBeschreibungID());
    }

    @Test
    void test_init_with_null_faces_context_does_nothing() {
      facesContextStatic.when(FacesContext::getCurrentInstance).thenReturn(null);

      controller.init();

      assertNull(controller.getBeschreibungID());
    }
  }

  // ==========================================================================
  // Getters / Setters / Simple State
  // ==========================================================================

  @Nested
  @DisplayName("Getters and Setters")
  class GetterSetterTests {

    @Test
    void test_setBeschreibungID_and_getBeschreibungID() {
      controller.setBeschreibungID("test-id");
      assertEquals("test-id", controller.getBeschreibungID());
    }

    @Test
    void test_setComment_and_getComment() {
      controller.setComment("test comment");
      assertEquals("test comment", controller.getComment());
    }

    @Test
    void test_setSelectedLizenzTyp_and_getSelectedLizenzTyp() {
      controller.setSelectedLizenzTyp(LizenzTyp.CC_BY_SA);
      assertEquals(LizenzTyp.CC_BY_SA, controller.getSelectedLizenzTyp());
    }

    @Test
    void test_setSelectedLizenzTyp_with_null() {
      controller.setSelectedLizenzTyp(LizenzTyp.CC_BY_SA);
      controller.setSelectedLizenzTyp(null);
      assertNull(controller.getSelectedLizenzTyp());
    }

    @Test
    void test_getLizenzTypen_returns_all_enum_values() {
      List<LizenzTyp> lizenzTypen = controller.getLizenzTypen();

      assertNotNull(lizenzTypen);
      assertEquals(LizenzTyp.values().length, lizenzTypen.size());
      for (LizenzTyp typ : LizenzTyp.values()) {
        assertTrue(lizenzTypen.contains(typ),
            "LizenzTypen should contain " + typ);
      }
    }

    @Test
    void test_initial_teiXmlValid_is_false() {
      assertFalse(controller.isTeiXmlValid());
    }

    @Test
    void test_initial_publikation_is_null() {
      assertNull(controller.getPublikation());
    }
  }

  // ==========================================================================
  // resetDialogFields()
  // ==========================================================================

  @Nested
  @DisplayName("resetDialogFields()")
  class ResetDialogFieldsTests {

    @Test
    void test_resetDialogFields_clears_comment_and_lizenzTyp() {
      controller.setComment("test comment");
      controller.setSelectedLizenzTyp(LizenzTyp.CC_BY_SA);

      controller.resetDialogFields();

      assertNull(controller.getComment());
      assertNull(controller.getSelectedLizenzTyp());
    }

    @Test
    void test_resetDialogFields_on_already_null_fields() {
      controller.resetDialogFields();

      assertNull(controller.getComment());
      assertNull(controller.getSelectedLizenzTyp());
    }
  }

  // ==========================================================================
  // Dialog Methods
  // ==========================================================================

  @Nested
  @DisplayName("Dialog Methods")
  class DialogTests {

    @Test
    void test_openCreatePublikationDialog_opens_dynamic_dialog() {
      controller.openCreatePublikationDialog("test-beschreibung-id");

      verify(primeFacesDialog, times(1)).openDynamic(anyString(), anyMap(), anyMap());
    }

    @Test
    void test_openWithdrawDialog_opens_dynamic_dialog() {
      controller.openWithdrawDialog("test-beschreibung-id");

      verify(primeFacesDialog, times(1)).openDynamic(anyString(), anyMap(), anyMap());
    }

    @Test
    void test_openHistoryDialog_opens_dynamic_dialog() {
      controller.openHistoryDialog("test-beschreibung-id");

      verify(primeFacesDialog, times(1)).openDynamic(anyString(), anyMap(), anyMap());
    }

    @Test
    void test_dialogSchliessen_closes_dynamic_dialog() {
      controller.dialogSchliessen("result");

      verify(primeFacesDialog, times(1)).closeDynamic("result");
    }

    @Test
    void test_dialogSchliessen_with_null_result() {
      controller.dialogSchliessen(null);

      verify(primeFacesDialog, times(1)).closeDynamic(null);
    }

  }

  // ==========================================================================
  // Card State Check: isEditable
  // ==========================================================================

  @Nested
  @DisplayName("isEditable()")
  class IsEditableTests {

    @Test
    void test_isEditable_returns_true_when_beschreibung_is_editable() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .build();

      assertTrue(controller.isEditable(beschreibung));
    }

    @Test
    void test_isEditable_returns_false_when_beschreibung_is_not_editable() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .withPublikation(
              new PublikationViewModel(PublikationStatus.IN_REVIEW, null, null, true, false,
                  List.of()))
          .build();

      assertFalse(controller.isEditable(beschreibung));
    }
  }

  // ==========================================================================
  // Card State Check: isEditableForCurrentUser
  // ==========================================================================

  @Nested
  @DisplayName("isEditableForCurrentUser()")
  class IsEditableForCurrentUserTests {

    @Test
    void test_editable_and_actionable_returns_true() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .build();
      when(beschreibungsBoundary.isBeschreibungActionableForLoggedInBearbeiter("B1")).thenReturn(
          true);

      assertTrue(controller.isEditableForCurrentUser(beschreibung));
    }

    @Test
    void test_editable_but_not_actionable_returns_false() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .build();
      when(beschreibungsBoundary.isBeschreibungActionableForLoggedInBearbeiter("B1")).thenReturn(
          false);

      assertFalse(controller.isEditableForCurrentUser(beschreibung));
    }

    @Test
    void test_not_editable_and_actionable_returns_false() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .withPublikation(
              new PublikationViewModel(PublikationStatus.IN_REVIEW, null, null, true, false,
                  List.of()))
          .build();

      assertFalse(controller.isEditableForCurrentUser(beschreibung));
      // Short-circuit: actionCache should not be called
    }

    @Test
    void test_not_editable_and_not_actionable_returns_false() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .withPublikation(
              new PublikationViewModel(PublikationStatus.IN_REVIEW, null, null, true, false,
                  List.of()))
          .build();
      when(beschreibungsBoundary.isBeschreibungActionableForLoggedInBearbeiter("B1")).thenReturn(
          false);

      assertFalse(controller.isEditableForCurrentUser(beschreibung));
    }
  }

  // ==========================================================================
  // Card State Check: hasNoAssignedBearbeiter
  // ==========================================================================

  @Nested
  @DisplayName("hasNoAssignedBearbeiter()")
  class HasNoAssignedBearbeiterTests {

    @Test
    void test_empty_bearbeiterNames_returns_true() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .build();

      assertTrue(controller.hasNoAssignedBearbeiter(beschreibung));
    }

    @Test
    void test_non_empty_bearbeiterNames_returns_false() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .withBearbeiter(List.of(new Bearbeiter("b1", "User1")))
          .build();

      assertFalse(controller.hasNoAssignedBearbeiter(beschreibung));
    }

    @Test
    void test_multiple_bearbeiterNames_returns_false() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .withBearbeiter(List.of(new Bearbeiter("b1", "User1"), new Bearbeiter("b2", "User2")))
          .build();

      assertFalse(controller.hasNoAssignedBearbeiter(beschreibung));
    }
  }

  // ==========================================================================
  // Card State Check: canRequestPublication
  // ==========================================================================

  @Nested
  @DisplayName("canRequestPublication()")
  class CanRequestPublicationTests {

    @Test
    void test_all_conditions_met_returns_true() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .withPublikation(
              new PublikationViewModel(PublikationStatus.DRAFT, null, null, false, false,
                  List.of()))
          .build();
      when(beschreibungsBoundary.isBeschreibungActionableForLoggedInBearbeiter("B1")).thenReturn(
          true);

      assertTrue(controller.canRequestPublication(beschreibung));
    }

    @Test
    void test_not_editable_returns_false() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .withPublikation(
              new PublikationViewModel(PublikationStatus.IN_REVIEW, null, null, true, false,
                  List.of()))
          .build();

      assertFalse(controller.canRequestPublication(beschreibung));
    }

    @Test
    void test_cannot_request_approval_returns_false() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .build();

      assertFalse(controller.canRequestPublication(beschreibung));
    }

    @Test
    void test_not_actionable_returns_false() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .withPublikation(
              new PublikationViewModel(PublikationStatus.DRAFT, null, null, false, false,
                  List.of()))
          .build();
      when(beschreibungsBoundary.isBeschreibungActionableForLoggedInBearbeiter("B1")).thenReturn(
          false);

      assertFalse(controller.canRequestPublication(beschreibung));
    }

  }

  // ==========================================================================
  // Card State Check: canWithdrawPublication
  // ==========================================================================

  @Nested
  @DisplayName("canWithdrawPublication()")
  class CanWithdrawPublicationTests {

    @Test
    void test_all_conditions_met_returns_true() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .withPublikation(
              new PublikationViewModel(PublikationStatus.IN_REVIEW, null, null, true, false,
                  List.of()))
          .build();
      when(beschreibungsBoundary.isBeschreibungActionableForLoggedInBearbeiter("B1")).thenReturn(
          true);

      assertTrue(controller.canWithdrawPublication(beschreibung));
    }

    @Test
    void test_no_publikation_returns_false() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .build();

      assertFalse(controller.canWithdrawPublication(beschreibung));
    }

    @Test
    void test_no_active_approval_request_returns_false() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .withPublikation(
              new PublikationViewModel(PublikationStatus.DRAFT, null, null, false, false,
                  List.of()))
          .build();
      when(beschreibungsBoundary.isBeschreibungActionableForLoggedInBearbeiter("B1")).thenReturn(
          true);

      assertFalse(controller.canWithdrawPublication(beschreibung));
    }

    @Test
    void test_not_actionable_returns_false() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .withPublikation(
              new PublikationViewModel(PublikationStatus.IN_REVIEW, null, null, true, false,
                  List.of()))
          .build();
      when(beschreibungsBoundary.isBeschreibungActionableForLoggedInBearbeiter("B1")).thenReturn(
          false);

      assertFalse(controller.canWithdrawPublication(beschreibung));
    }

    @Test
    void test_already_published_returns_false() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .withPublikation(
              new PublikationViewModel(PublikationStatus.PUBLISHED, null, null, false, false,
                  List.of()))
          .build();

      assertFalse(controller.canWithdrawPublication(beschreibung));
    }
  }

  // ==========================================================================
  // Card State Check: canViewPublicationHistory
  // ==========================================================================

  @Nested
  @DisplayName("canViewPublicationHistory()")
  class CanViewPublicationHistoryTests {

    @Test
    void test_all_conditions_met_returns_true() {
      ApprovalRequestViewModel approvalRequest = new ApprovalRequestViewModel(
          ApprovalStatus.REQUESTED, "user1", null, null, null, true, List.of());
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .withPublikation(
              new PublikationViewModel(PublikationStatus.IN_REVIEW, null, null, true, false,
                  List.of(approvalRequest)))
          .build();
      when(beschreibungsBoundary.isBeschreibungAssignedToLoggedInBearbeiter("B1")).thenReturn(
          true);

      assertTrue(controller.canViewPublicationHistory(beschreibung));
    }

    @Test
    void test_no_publikation_returns_false() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .build();

      assertFalse(controller.canViewPublicationHistory(beschreibung));
    }

    @Test
    void test_empty_approval_requests_returns_false() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .withPublikation(
              new PublikationViewModel(PublikationStatus.DRAFT, null, null, false, false,
                  List.of()))
          .build();

      assertFalse(controller.canViewPublicationHistory(beschreibung));
    }

    @Test
    void test_not_assigned_returns_false() {
      ApprovalRequestViewModel approvalRequest = new ApprovalRequestViewModel(
          ApprovalStatus.REQUESTED, "user1", null, null, null, true, List.of());
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .withPublikation(
              new PublikationViewModel(PublikationStatus.IN_REVIEW, null, null, true, false,
                  List.of(approvalRequest)))
          .build();
      when(beschreibungsBoundary.isBeschreibungAssignedToLoggedInBearbeiter("B1")).thenReturn(
          false);

      assertFalse(controller.canViewPublicationHistory(beschreibung));
    }

    @Test
    void test_foreign_sperre_does_not_block_view() {
      ApprovalRequestViewModel approvalRequest = new ApprovalRequestViewModel(
          ApprovalStatus.REQUESTED, "user1", null, null, null, true, List.of());
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .withPublikation(
              new PublikationViewModel(PublikationStatus.IN_REVIEW, null, null, true, false,
                  List.of(approvalRequest)))
          .build();
      // assigned (read access ok) but actionable=false (foreign Sperre) — view must still work
      when(beschreibungsBoundary.isBeschreibungAssignedToLoggedInBearbeiter("B1")).thenReturn(true);
      when(beschreibungsBoundary.isBeschreibungActionableForLoggedInBearbeiter("B1")).thenReturn(
          false);

      assertTrue(controller.canViewPublicationHistory(beschreibung));
    }
  }

  // ==========================================================================
  // Card State Check: isDeletableForCurrentUser
  // ==========================================================================

  @Nested
  @DisplayName("isDeletableForCurrentUser()")
  class IsDeletableForCurrentUserTests {

    @Test
    void test_editable_and_deletable_returns_true() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .build();
      when(beschreibungsBoundary.isBeschreibungDeletableForLoggedInBearbeiter("B1")).thenReturn(
          true);

      assertTrue(controller.isDeletableForCurrentUser(beschreibung));
    }

    @Test
    void test_not_editable_returns_false() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .withPublikation(
              new PublikationViewModel(PublikationStatus.IN_REVIEW, null, null, true, false,
                  List.of()))
          .build();

      assertFalse(controller.isDeletableForCurrentUser(beschreibung));
    }

    @Test
    void test_editable_but_not_deletable_returns_false() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .build();
      when(beschreibungsBoundary.isBeschreibungDeletableForLoggedInBearbeiter("B1")).thenReturn(
          false);

      assertFalse(controller.isDeletableForCurrentUser(beschreibung));
    }

    @Test
    void test_not_editable_and_not_deletable_returns_false() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .withPublikation(
              new PublikationViewModel(PublikationStatus.IN_REVIEW, null, null, true, false,
                  List.of()))
          .build();
      when(beschreibungsBoundary.isBeschreibungDeletableForLoggedInBearbeiter("B1")).thenReturn(
          false);

      assertFalse(controller.isDeletableForCurrentUser(beschreibung));
    }
  }

  // ==========================================================================
  // loadPublikation()
  // ==========================================================================

  @Nested
  @DisplayName("loadPublikation()")
  class LoadPublikationTests {

    @Test
    void test_loads_publikation_from_service() throws Exception {
      Publikation publikation = new Publikation.PublikationBuilder()
          .withErstpublikationsDatum(LocalDateTime.of(2020, 1, 1, 0, 0))
          .withPublikationsDatumHSP(LocalDateTime.of(2024, 6, 15, 10, 30))
          .build();
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(beschreibung.getPublikation()).thenReturn(publikation);
      when(publikationsService.reloadBeschreibung("test-id")).thenReturn(beschreibung);

      controller.setBeschreibungID("test-id");
      controller.loadPublikation();

      assertNotNull(controller.getPublikation());
      assertEquals(PublikationStatus.DRAFT, controller.getPublikation().status());
      assertEquals(LocalDateTime.of(2024, 6, 15, 10, 30),
          controller.getPublikation().publikationsDatumHSP());
    }

    @Test
    void test_with_null_beschreibungID_does_nothing() throws Exception {
      controller.loadPublikation();

      assertNull(controller.getPublikation());
      verify(publikationsService, never()).reloadBeschreibung(anyString());
    }

    @Test
    void test_with_null_publikation_on_beschreibung() throws Exception {
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(beschreibung.getPublikation()).thenReturn(null);
      when(publikationsService.reloadBeschreibung("test-id")).thenReturn(beschreibung);

      controller.setBeschreibungID("test-id");
      controller.loadPublikation();

      assertNull(controller.getPublikation());
    }
  }

  // ==========================================================================
  // getApprovalRequestsHistory()
  // ==========================================================================

  @Nested
  @DisplayName("getApprovalRequestsHistory()")
  class GetApprovalRequestsHistoryTests {

    @Test
    void test_with_null_returns_empty_list() {
      List<ApprovalEventViewModel> result = controller.getApprovalRequestsHistory(null);

      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    void test_with_empty_list_returns_empty_list() {
      List<ApprovalEventViewModel> result = controller.getApprovalRequestsHistory(
          Collections.emptyList());

      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    void test_returns_events_sorted_descending_by_createdAt() {
      LocalDateTime oldest = LocalDateTime.of(2024, 1, 1, 10, 0);
      LocalDateTime middle = LocalDateTime.of(2024, 1, 2, 10, 0);
      LocalDateTime newest = LocalDateTime.of(2024, 1, 3, 10, 0);

      ApprovalEventViewModel event1 = new ApprovalEventViewModel(
          ApprovalEventType.REQUESTED, "User1", oldest, null);
      ApprovalEventViewModel event2 = new ApprovalEventViewModel(
          ApprovalEventType.ASSIGNED, "User2", middle, null);
      ApprovalEventViewModel event3 = new ApprovalEventViewModel(
          ApprovalEventType.APPROVED, "User3", newest, "Approved with comment");

      ApprovalRequestViewModel request = new ApprovalRequestViewModel(
          ApprovalStatus.APPROVED, "User1", oldest, "User3", middle, false,
          List.of(event1, event2, event3));

      List<ApprovalEventViewModel> result = controller.getApprovalRequestsHistory(
          List.of(request));

      assertEquals(3, result.size());
      assertEquals(newest, result.get(0).createdAt());
      assertEquals(middle, result.get(1).createdAt());
      assertEquals(oldest, result.get(2).createdAt());
    }

    @Test
    void test_aggregates_events_from_multiple_requests() {
      LocalDateTime time1 = LocalDateTime.of(2024, 1, 1, 10, 0);
      LocalDateTime time2 = LocalDateTime.of(2024, 1, 2, 10, 0);
      LocalDateTime time3 = LocalDateTime.of(2024, 1, 3, 10, 0);
      LocalDateTime time4 = LocalDateTime.of(2024, 1, 4, 10, 0);

      ApprovalRequestViewModel request1 = new ApprovalRequestViewModel(
          ApprovalStatus.WITHDRAWN, "User1", time1, null, null, false,
          List.of(
              new ApprovalEventViewModel(ApprovalEventType.REQUESTED, "User1", time1, null),
              new ApprovalEventViewModel(ApprovalEventType.WITHDRAWN, "User1", time2, null)));

      ApprovalRequestViewModel request2 = new ApprovalRequestViewModel(
          ApprovalStatus.APPROVED, "User1", time3, "User2", time3, false,
          List.of(
              new ApprovalEventViewModel(ApprovalEventType.REQUESTED, "User1", time3, null),
              new ApprovalEventViewModel(ApprovalEventType.APPROVED, "User2", time4, null)));

      List<ApprovalEventViewModel> result = controller.getApprovalRequestsHistory(
          List.of(request1, request2));

      assertEquals(4, result.size());
      assertEquals(time4, result.get(0).createdAt());
      assertEquals(time3, result.get(1).createdAt());
      assertEquals(time2, result.get(2).createdAt());
      assertEquals(time1, result.get(3).createdAt());
    }

    @Test
    void test_with_empty_events_on_request_returns_empty_list() {
      ApprovalRequestViewModel request = new ApprovalRequestViewModel(
          ApprovalStatus.REQUESTED, "User1", LocalDateTime.now(), null, null, true,
          Collections.emptyList());

      List<ApprovalEventViewModel> result = controller.getApprovalRequestsHistory(
          List.of(request));

      assertTrue(result.isEmpty());
    }

    @Test
    void test_single_event_returns_list_with_one_element() {
      LocalDateTime now = LocalDateTime.of(2024, 6, 15, 10, 0);
      ApprovalEventViewModel event = new ApprovalEventViewModel(
          ApprovalEventType.REQUESTED, "User1", now, null);
      ApprovalRequestViewModel request = new ApprovalRequestViewModel(
          ApprovalStatus.REQUESTED, "User1", now, null, null, true,
          List.of(event));

      List<ApprovalEventViewModel> result = controller.getApprovalRequestsHistory(
          List.of(request));

      assertEquals(1, result.size());
      assertEquals(now, result.getFirst().createdAt());
      assertEquals(ApprovalEventType.REQUESTED, result.getFirst().eventType());
    }
  }

  // ==========================================================================
  // getActiveApprovalRequest() / getApprovalRequestsHistory() ohne Argumente
  // ==========================================================================

  @Nested
  @DisplayName("getActiveApprovalRequest()")
  class GetActiveApprovalRequestTests {

    @Test
    void test_without_loaded_publikation_returns_null_and_empty_history() {
      assertNull(controller.getActiveApprovalRequest());

      assertNotNull(controller.getApprovalRequestsHistory());
      assertTrue(controller.getApprovalRequestsHistory().isEmpty());
    }

    @Test
    void test_returns_active_request_and_history_after_loadPublikation() throws Exception {
      LocalDateTime requestedAt = LocalDateTime.of(2026, 7, 1, 10, 0);

      ApprovalEvent event = mock(ApprovalEvent.class);
      when(event.getEventType()).thenReturn(ApprovalEventType.REQUESTED);
      when(event.getActor()).thenReturn("User1");
      when(event.getCreatedAt()).thenReturn(requestedAt);

      ApprovalRequest activeRequest = mock(ApprovalRequest.class);
      when(activeRequest.getStatus()).thenReturn(ApprovalStatus.REQUESTED);
      when(activeRequest.getRequestedBy()).thenReturn("User1");
      when(activeRequest.getRequestedAt()).thenReturn(requestedAt);
      when(activeRequest.isActive()).thenReturn(true);
      when(activeRequest.getEvents()).thenReturn(List.of(event));

      Publikation publikation = mock(Publikation.class);
      when(publikation.getStatus()).thenReturn(PublikationStatus.IN_REVIEW);
      when(publikation.getApprovalRequests()).thenReturn(Set.of(activeRequest));
      when(publikation.hasActiveApprovalRequest()).thenReturn(true);

      Beschreibung beschreibung = mock(Beschreibung.class);
      when(beschreibung.getPublikation()).thenReturn(publikation);
      when(publikationsService.reloadBeschreibung("B1")).thenReturn(beschreibung);

      controller.setBeschreibungID("B1");
      controller.loadPublikation();

      ApprovalRequestViewModel result = controller.getActiveApprovalRequest();
      assertNotNull(result);
      assertEquals(ApprovalStatus.REQUESTED, result.status());
      assertEquals("User1", result.requestedBy());
      assertEquals(requestedAt, result.requestedAt());

      List<ApprovalEventViewModel> history = controller.getApprovalRequestsHistory();
      assertEquals(1, history.size());
      assertEquals(ApprovalEventType.REQUESTED, history.getFirst().eventType());
    }

    @Test
    void test_with_only_inactive_requests_returns_null() throws Exception {
      ApprovalRequest inactiveRequest = mock(ApprovalRequest.class);
      when(inactiveRequest.getStatus()).thenReturn(ApprovalStatus.WITHDRAWN);
      when(inactiveRequest.isActive()).thenReturn(false);
      when(inactiveRequest.getEvents()).thenReturn(List.of());

      Publikation publikation = mock(Publikation.class);
      when(publikation.getStatus()).thenReturn(PublikationStatus.DRAFT);
      when(publikation.getApprovalRequests()).thenReturn(Set.of(inactiveRequest));

      Beschreibung beschreibung = mock(Beschreibung.class);
      when(beschreibung.getPublikation()).thenReturn(publikation);
      when(publikationsService.reloadBeschreibung("B1")).thenReturn(beschreibung);

      controller.setBeschreibungID("B1");
      controller.loadPublikation();

      assertNull(controller.getActiveApprovalRequest());
    }
  }

  // ==========================================================================
  // createPublicationRequest()
  // ==========================================================================

  @Nested
  @DisplayName("createPublicationRequest()")
  class CreatePublicationRequestTests {

    @Test
    void test_happy_path_with_explicit_id() throws Exception {
      Authentication auth = mock(Authentication.class);
      when(auth.getFirstname()).thenReturn("Max");
      when(auth.getLastname()).thenReturn("Mustermann");
      when(bearbeiterBoundary.getAuthentication()).thenReturn(auth);

      controller.setSelectedLizenzTyp(LizenzTyp.CC_BY_SA);
      controller.setComment("Publication comment");

      controller.createPublicationRequest("B-explicit-id");

      verify(publikationsService).applyForPublication(
          eq("B-explicit-id"), eq(LizenzTyp.CC_BY_SA),
          eq("Max Mustermann"), eq("Publication comment"));
      // Verify dialog was closed (success message is now added by onPublicationRequested listener)
      verify(primeFacesDialog).closeDynamic("B-explicit-id");
      verify(facesContext, never()).addMessage(any(), any());
    }

    @Test
    void test_happy_path_with_null_param_falls_back_to_field() throws Exception {
      Authentication auth = mock(Authentication.class);
      when(auth.getFirstname()).thenReturn("Max");
      when(auth.getLastname()).thenReturn("Mustermann");
      when(bearbeiterBoundary.getAuthentication()).thenReturn(auth);

      controller.setBeschreibungID("B-field-id");
      controller.setSelectedLizenzTyp(LizenzTyp.CC_BY_SA);

      controller.createPublicationRequest(null);

      verify(publikationsService).applyForPublication(
          eq("B-field-id"), eq(LizenzTyp.CC_BY_SA),
          eq("Max Mustermann"), isNull());
    }

    @Test
    void test_no_arg_overload_uses_field_id() throws Exception {
      Authentication auth = mock(Authentication.class);
      when(auth.getFirstname()).thenReturn("Max");
      when(auth.getLastname()).thenReturn("Mustermann");
      when(bearbeiterBoundary.getAuthentication()).thenReturn(auth);

      controller.setBeschreibungID("B-field-id");
      controller.setSelectedLizenzTyp(LizenzTyp.CC_BY_SA);

      controller.createPublicationRequest();

      verify(publikationsService).applyForPublication(
          eq("B-field-id"), eq(LizenzTyp.CC_BY_SA),
          eq("Max Mustermann"), isNull());
    }

    @Test
    void test_exception_from_service_adds_error_message() throws Exception {
      Authentication auth = mock(Authentication.class);
      when(auth.getFirstname()).thenReturn("Max");
      when(auth.getLastname()).thenReturn("Mustermann");
      when(bearbeiterBoundary.getAuthentication()).thenReturn(auth);
      when(publikationsService.applyForPublication(
          anyString(), any(LizenzTyp.class), anyString(), any()))
          .thenThrow(new RuntimeException("Service error"));

      controller.setBeschreibungID("B1");
      controller.setSelectedLizenzTyp(LizenzTyp.CC_BY_SA);

      controller.createPublicationRequest("B1");

      ArgumentCaptor<FacesMessage> messageCaptor = ArgumentCaptor.forClass(FacesMessage.class);
      verify(facesContext).addMessage(isNull(), messageCaptor.capture());
      assertEquals(FacesMessage.SEVERITY_ERROR, messageCaptor.getValue().getSeverity());
      assertTrue(messageCaptor.getValue().getSummary().contains("Service error"));
    }

    @Test
    void test_fields_are_reset_after_success() {
      Authentication auth = mock(Authentication.class);
      when(auth.getFirstname()).thenReturn("Max");
      when(auth.getLastname()).thenReturn("Mustermann");
      when(bearbeiterBoundary.getAuthentication()).thenReturn(auth);

      controller.setComment("comment");
      controller.setSelectedLizenzTyp(LizenzTyp.CC_BY_SA);
      controller.setBeschreibungID("B1");

      controller.createPublicationRequest("B1");

      assertNull(controller.getComment());
      assertNull(controller.getSelectedLizenzTyp());
    }

    @Test
    void test_fields_are_reset_after_exception() throws Exception {
      Authentication auth = mock(Authentication.class);
      when(auth.getFirstname()).thenReturn("Max");
      when(auth.getLastname()).thenReturn("Mustermann");
      when(bearbeiterBoundary.getAuthentication()).thenReturn(auth);
      when(publikationsService.applyForPublication(
          anyString(), any(LizenzTyp.class), anyString(), any()))
          .thenThrow(new RuntimeException("fail"));

      controller.setComment("comment");
      controller.setSelectedLizenzTyp(LizenzTyp.CC_BY_SA);
      controller.setBeschreibungID("B1");

      controller.createPublicationRequest("B1");

      assertNull(controller.getComment());
      assertNull(controller.getSelectedLizenzTyp());
    }

    @Test
    void test_with_null_authentication_uses_gast() throws Exception {
      when(bearbeiterBoundary.getAuthentication()).thenReturn(null);

      controller.setBeschreibungID("B1");
      controller.setSelectedLizenzTyp(LizenzTyp.CC_BY_SA);

      controller.createPublicationRequest("B1");

      verify(publikationsService).applyForPublication(
          eq("B1"), eq(LizenzTyp.CC_BY_SA), eq("Gast"), isNull());
    }
  }

  // ==========================================================================
  // withdraw()
  // ==========================================================================

  @Nested
  @DisplayName("withdraw()")
  class WithdrawTests {

    @Test
    void test_happy_path_with_explicit_id() throws Exception {
      Authentication auth = mock(Authentication.class);
      when(auth.getFirstname()).thenReturn("Max");
      when(auth.getLastname()).thenReturn("Mustermann");
      when(bearbeiterBoundary.getAuthentication()).thenReturn(auth);

      controller.setComment("Withdrawal comment");

      controller.withdraw("B-explicit-id");

      verify(publikationsService).withdraw(
          eq("B-explicit-id"), eq("Max Mustermann"), eq("Withdrawal comment"));
      // Verify dialog was closed (success message is now added by onPublicationWithdrawn listener)
      verify(primeFacesDialog).closeDynamic("B-explicit-id");
      verify(facesContext, never()).addMessage(any(), any());
    }

    @Test
    void test_happy_path_with_null_param_falls_back_to_field() throws Exception {
      Authentication auth = mock(Authentication.class);
      when(auth.getFirstname()).thenReturn("Max");
      when(auth.getLastname()).thenReturn("Mustermann");
      when(bearbeiterBoundary.getAuthentication()).thenReturn(auth);

      controller.setBeschreibungID("B-field-id");
      controller.setComment("comment");

      controller.withdraw(null);

      verify(publikationsService).withdraw(
          eq("B-field-id"), eq("Max Mustermann"), eq("comment"));
    }

    @Test
    void test_no_arg_overload_uses_field_id() throws Exception {
      Authentication auth = mock(Authentication.class);
      when(auth.getFirstname()).thenReturn("Max");
      when(auth.getLastname()).thenReturn("Mustermann");
      when(bearbeiterBoundary.getAuthentication()).thenReturn(auth);

      controller.setBeschreibungID("B-field-id");

      controller.withdraw();

      verify(publikationsService).withdraw(
          eq("B-field-id"), eq("Max Mustermann"), isNull());
    }

    @Test
    void test_exception_from_service_adds_error_message() throws Exception {
      Authentication auth = mock(Authentication.class);
      when(auth.getFirstname()).thenReturn("Max");
      when(auth.getLastname()).thenReturn("Mustermann");
      when(bearbeiterBoundary.getAuthentication()).thenReturn(auth);
      when(publikationsService.withdraw(anyString(), anyString(), any()))
          .thenThrow(new RuntimeException("Withdraw error"));

      controller.setBeschreibungID("B1");

      controller.withdraw("B1");

      ArgumentCaptor<FacesMessage> messageCaptor = ArgumentCaptor.forClass(FacesMessage.class);
      verify(facesContext).addMessage(isNull(), messageCaptor.capture());
      assertEquals(FacesMessage.SEVERITY_ERROR, messageCaptor.getValue().getSeverity());
      assertTrue(messageCaptor.getValue().getSummary().contains("Withdraw error"));
    }

    @Test
    void test_fields_are_reset_after_success() {
      Authentication auth = mock(Authentication.class);
      when(auth.getFirstname()).thenReturn("Max");
      when(auth.getLastname()).thenReturn("Mustermann");
      when(bearbeiterBoundary.getAuthentication()).thenReturn(auth);

      controller.setComment("comment");
      controller.setSelectedLizenzTyp(LizenzTyp.CC_BY_SA);
      controller.setBeschreibungID("B1");

      controller.withdraw("B1");

      assertNull(controller.getComment());
      assertNull(controller.getSelectedLizenzTyp());
    }

    @Test
    void test_fields_are_reset_after_exception() throws Exception {
      Authentication auth = mock(Authentication.class);
      when(auth.getFirstname()).thenReturn("Max");
      when(auth.getLastname()).thenReturn("Mustermann");
      when(bearbeiterBoundary.getAuthentication()).thenReturn(auth);
      when(publikationsService.withdraw(anyString(), anyString(), any()))
          .thenThrow(new RuntimeException("fail"));

      controller.setComment("comment");
      controller.setSelectedLizenzTyp(LizenzTyp.CC_BY_SA);
      controller.setBeschreibungID("B1");

      controller.withdraw("B1");

      assertNull(controller.getComment());
      assertNull(controller.getSelectedLizenzTyp());
    }

    @Test
    void test_with_null_authentication_uses_gast() throws Exception {
      when(bearbeiterBoundary.getAuthentication()).thenReturn(null);

      controller.setBeschreibungID("B1");

      controller.withdraw("B1");

      verify(publikationsService).withdraw(eq("B1"), eq("Gast"), isNull());
    }
  }

  // ==========================================================================
  // validateTEI(String locale)
  // ==========================================================================

  @Nested
  @DisplayName("validateTEI(String locale)")
  class ValidateTEIByLocaleTests {

    @Test
    void test_with_null_beschreibungID_does_nothing() throws Exception {
      controller.validateTEI("de");

      verify(publikationsService, never()).reloadBeschreibung(anyString());
      assertFalse(controller.isTeiXmlValid());
    }

    @Test
    void test_valid_tei_sets_teiXmlValid_true() throws Exception {
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(beschreibung.getTeiXML()).thenReturn("<TEI/>");
      when(publikationsService.reloadBeschreibung("B1")).thenReturn(beschreibung);

      ValidationResult validResult = new ValidationResult(true, null, null, null);
      when(beschreibungsBoundary.validateXML("<TEI/>", "ODD", "de")).thenReturn(validResult);

      controller.setBeschreibungID("B1");
      controller.validateTEI("de");

      assertTrue(controller.isTeiXmlValid());
    }

    @Test
    void test_invalid_tei_sets_teiXmlValid_false() throws Exception {
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(beschreibung.getTeiXML()).thenReturn("<INVALID/>");
      when(publikationsService.reloadBeschreibung("B1")).thenReturn(beschreibung);

      ValidationResult invalidResult = new ValidationResult(false, "10", "5", "Validation error");
      when(beschreibungsBoundary.validateXML("<INVALID/>", "ODD", "de")).thenReturn(invalidResult);

      controller.setBeschreibungID("B1");
      controller.validateTEI("de");

      assertFalse(controller.isTeiXmlValid());
    }
  }

  // ==========================================================================
  // validateTEI(BeschreibungsViewModel, String locale)
  // ==========================================================================

  @Nested
  @DisplayName("validateTEI(BeschreibungsViewModel, String locale)")
  class ValidateTEIByViewModelTests {

    @Test
    void test_valid_tei_returns_true_and_sets_field() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .withTEIXML("<TEI/>")
          .build();

      ValidationResult validResult = new ValidationResult(true, null, null, null);
      when(beschreibungsBoundary.validateXML("<TEI/>", "ODD", "de")).thenReturn(validResult);

      boolean result = controller.validateTEI(beschreibung, "de");

      assertTrue(result);
      assertTrue(controller.isTeiXmlValid());
    }

    @Test
    void test_invalid_tei_returns_false_and_sets_field() {
      BeschreibungsViewModel beschreibung = new BeschreibungsViewBuilder()
          .withID("B1")
          .withTEIXML("<INVALID/>")
          .build();

      ValidationResult invalidResult = new ValidationResult(false, "10", "5", "Error");
      when(beschreibungsBoundary.validateXML("<INVALID/>", "ODD", "en")).thenReturn(invalidResult);

      boolean result = controller.validateTEI(beschreibung, "en");

      assertFalse(result);
      assertFalse(controller.isTeiXmlValid());
    }
  }

  // ==========================================================================
  // Beschreibung action methods (moved from KulturObjektDokumentViewController)
  // ==========================================================================

  @Nested
  @DisplayName("openBearbeiterZuweisenDialog()")
  class OpenBearbeiterZuweisenDialogTests {

    @Test
    void test_opens_dialog() {
      controller.openBearbeiterZuweisenDialog("b_1");

      verify(primeFacesDialog, times(1)).openDynamic(anyString(), anyMap(), anyMap());
    }
  }

  @Nested
  @DisplayName("openBearbeiterEntziehenDialog()")
  class OpenBearbeiterEntziehenDialogTests {

    @Test
    void test_opens_dialog() {
      controller.openBearbeiterEntziehenDialog("b_1");

      verify(primeFacesDialog, times(1)).openDynamic(anyString(), anyMap(), anyMap());
    }
  }

  @Nested
  @DisplayName("isEntziehenDialogVisible()")
  class IsEntziehenDialogVisibleTests {

    @Test
    void test_returns_true_for_redakteur() {
      when(bearbeiterBoundary.getLoggedBearbeiter())
          .thenReturn(Bearbeiter.newBuilder().withRolle("Redakteur").build());

      assertTrue(controller.isEntziehenDialogVisible());
    }

    @Test
    void test_returns_false_for_erschliesser() {
      when(bearbeiterBoundary.getLoggedBearbeiter())
          .thenReturn(Bearbeiter.newBuilder().withRolle("Erschließer").build());

      assertFalse(controller.isEntziehenDialogVisible());
    }

    @Test
    void test_returns_false_for_non_redakteur() {
      when(bearbeiterBoundary.getLoggedBearbeiter())
          .thenReturn(new Bearbeiter("b_1", "bearbeiterName"));

      assertFalse(controller.isEntziehenDialogVisible());
    }
  }

  @Nested
  @DisplayName("isDeleteVisible()")
  class IsDeleteVisibleTests {

    @Test
    void test_returns_false_when_no_permissions() {
      when(beschreibungsRechte.kannAllerDritterhartLoeschen()).thenReturn(false);
      when(beschreibungsRechte.kannLoeschen()).thenReturn(false);

      assertFalse(controller.isDeleteVisible());
    }

    @Test
    void test_returns_true_when_kann_loeschen() {
      when(beschreibungsRechte.kannAllerDritterhartLoeschen()).thenReturn(false);
      when(beschreibungsRechte.kannLoeschen()).thenReturn(true);

      assertTrue(controller.isDeleteVisible());
    }

    @Test
    void test_returns_true_when_kann_aller_dritter_hart_loeschen() {
      when(beschreibungsRechte.kannAllerDritterhartLoeschen()).thenReturn(true);
      when(beschreibungsRechte.kannLoeschen()).thenReturn(false);

      assertTrue(controller.isDeleteVisible());
    }
  }

  @Nested
  @DisplayName("beschreibungLoeschen()")
  class BeschreibungLoeschenTests {

    @Test
    void test_happy_path_calls_delete() throws Exception {
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(beschreibungsBoundary.findById("b_1")).thenReturn(Optional.of(beschreibung));

      Bearbeiter bearbeiter = new Bearbeiter("b_1", "bearbeiterName");
      when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);

      controller.beschreibungLoeschen("b_1");

      verify(beschreibungsBoundary).delete(bearbeiter, beschreibung);
    }

    @Test
    void test_beschreibung_not_found_does_nothing() throws Exception {
      when(beschreibungsBoundary.findById("b_missing")).thenReturn(Optional.empty());

      controller.beschreibungLoeschen("b_missing");

      verify(beschreibungsBoundary, never()).delete(any(), any(Beschreibung.class));
    }

    @Test
    void test_exception_shows_error_dialog() throws Exception {
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(beschreibungsBoundary.findById("b_1")).thenReturn(Optional.of(beschreibung));

      Bearbeiter bearbeiter = new Bearbeiter("b_1", "bearbeiterName");
      when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);
      doThrow(new RuntimeException("DB error"))
          .when(beschreibungsBoundary).delete(any(), any(Beschreibung.class));

      controller.beschreibungLoeschen("b_1");

      ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
      verify(primeFacesDialog).showMessageDynamic(captor.capture());
      assertEquals(FacesMessage.SEVERITY_ERROR, captor.getValue().getSeverity());
    }

    @Test
    void test_dokument_sperre_exception_uses_sperre_specific_message_key() throws Exception {
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(beschreibungsBoundary.findById("b_1")).thenReturn(Optional.of(beschreibung));
      Bearbeiter bearbeiter = new Bearbeiter("b_1", "bearbeiterName");
      when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);
      doThrow(new DokumentSperreException("sperre service down"))
          .when(beschreibungsBoundary).delete(any(), any(Beschreibung.class));

      controller.beschreibungLoeschen("b_1");

      ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
      verify(primeFacesDialog).showMessageDynamic(captor.capture());
      FacesMessage msg = captor.getValue();
      assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
      assertTrue(msg.getDetail().contains("sperre service down"));
    }

    @Test
    void test_beschreibungs_exception_uses_generic_loeschen_message_key() throws Exception {
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(beschreibungsBoundary.findById("b_1")).thenReturn(Optional.of(beschreibung));
      Bearbeiter bearbeiter = new Bearbeiter("b_1", "bearbeiterName");
      when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);
      doThrow(new BeschreibungsException("delete failed mid-tx"))
          .when(beschreibungsBoundary).delete(any(), any(Beschreibung.class));

      controller.beschreibungLoeschen("b_1");

      ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
      verify(primeFacesDialog).showMessageDynamic(captor.capture());
      FacesMessage msg = captor.getValue();
      assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
      assertTrue(msg.getDetail().contains("delete failed mid-tx"));
    }

    @Test
    void test_sperre_already_exists_shows_user_friendly_message() throws Exception {
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(beschreibungsBoundary.findById("b_1")).thenReturn(Optional.of(beschreibung));

      Bearbeiter bearbeiter = new Bearbeiter("b_1", "bearbeiterName");
      when(bearbeiterBoundary.getLoggedBearbeiter()).thenReturn(bearbeiter);

      Bearbeiter sperreBearbeiter = new Bearbeiter("other_user", "Other User");
      Sperre sperre = Sperre.newBuilder()
          .withBearbeiter(sperreBearbeiter)
          .withStartDatum(LocalDateTime.of(2026, 2, 25, 10, 30))
          .withSperreGrund("Publication approval in progress")
          .build();

      doThrow(new SperreAlreadyExistException("locked", List.of(sperre)))
          .when(beschreibungsBoundary).delete(any(), any(Beschreibung.class));

      controller.beschreibungLoeschen("b_1");

      ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
      verify(primeFacesDialog).showMessageDynamic(captor.capture());
      FacesMessage msg = captor.getValue();
      assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
      assertFalse(msg.getDetail().contains("SperreTyp"),
          "Detail should not contain raw technical message");
      assertTrue(msg.getDetail().contains("Other User"),
          "Detail should contain the lock holder's name");
    }
  }

  @Nested
  @DisplayName("updateBearbeiterListe()")
  class UpdateBearbeiterListeTests {

    @Test
    void test_delegates_to_kod_view_controller() {
      controller.reloadViewModel();

      verify(kulturObjektDokumentViewController).reloadViewModel();
    }
  }

  @Nested
  @DisplayName("reloadViewModel()")
  class ReloadViewModelTests {

    @Test
    @SuppressWarnings("unchecked")
    void test_null_id_reloads_view_model() {
      SelectEvent<String> event = mock(SelectEvent.class);
      when(event.getObject()).thenReturn(null);

      controller.reloadViewModel();

      verify(kulturObjektDokumentViewController).reloadViewModel();
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_with_id_reloads_view_model() {
      SelectEvent<String> event = mock(SelectEvent.class);
      when(event.getObject()).thenReturn("b_1");

      controller.reloadViewModel();

      verify(kulturObjektDokumentViewController).reloadViewModel();
    }
  }

  // ==========================================================================
  // onPublicationRequested() / onPublicationWithdrawn()
  // ==========================================================================

  @Nested
  @DisplayName("onPublicationRequested()")
  class OnPublicationRequestedTests {

    @Test
    @SuppressWarnings("unchecked")
    void test_reloads_view_and_adds_info_message() {
      SelectEvent<String> event = mock(SelectEvent.class);
      when(event.getObject()).thenReturn("b_1");

      controller.onPublicationRequested(event);

      verify(kulturObjektDokumentViewController).reloadViewModel();
      ArgumentCaptor<FacesMessage> messageCaptor = ArgumentCaptor.forClass(FacesMessage.class);
      verify(facesContext).addMessage(isNull(), messageCaptor.capture());
      assertEquals(FacesMessage.SEVERITY_INFO, messageCaptor.getValue().getSeverity());
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_with_null_id_reloads_view_and_adds_message() {
      SelectEvent<String> event = mock(SelectEvent.class);
      when(event.getObject()).thenReturn(null);

      controller.onPublicationRequested(event);

      verify(kulturObjektDokumentViewController).reloadViewModel();
      verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
    }
  }

  @Nested
  @DisplayName("onPublicationWithdrawn()")
  class OnPublicationWithdrawnTests {

    @Test
    @SuppressWarnings("unchecked")
    void test_reloads_view_and_adds_info_message() {
      SelectEvent<String> event = mock(SelectEvent.class);
      when(event.getObject()).thenReturn("b_1");

      controller.onPublicationWithdrawn(event);

      verify(kulturObjektDokumentViewController).reloadViewModel();
      ArgumentCaptor<FacesMessage> messageCaptor = ArgumentCaptor.forClass(FacesMessage.class);
      verify(facesContext).addMessage(isNull(), messageCaptor.capture());
      assertEquals(FacesMessage.SEVERITY_INFO, messageCaptor.getValue().getSeverity());
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_with_null_id_reloads_view_and_adds_message() {
      SelectEvent<String> event = mock(SelectEvent.class);
      when(event.getObject()).thenReturn(null);

      controller.onPublicationWithdrawn(event);

      verify(kulturObjektDokumentViewController).reloadViewModel();
      verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
    }
  }
}
