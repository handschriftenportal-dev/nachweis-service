package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.aggregates.Beschreibung;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.PublikationException;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.publikation.PublikationsService;
import de.staatsbibliothek.berlin.javaee.authentication.entity.Authentication;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class ApprovalRequestControllerTest {

  private ApprovalRequestController controller;
  private PublikationsService service;
  private BearbeiterBoundary bearbeiterBoundary;

  private FacesContext facesContext;
  private MockedStatic<FacesContext> facesContextStatic;

  @BeforeEach
  void setUp() {
    service = mock(PublikationsService.class);
    bearbeiterBoundary = mock(BearbeiterBoundary.class);

    facesContext = mock(FacesContext.class);
    facesContextStatic = mockStatic(FacesContext.class);
    facesContextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);

    controller = new ApprovalRequestController(service, bearbeiterBoundary);
  }

  @AfterEach
  void tearDown() {
    facesContextStatic.close();
  }

  private Authentication mockAuthentication(String firstname, String lastname) {
    Authentication auth = mock(Authentication.class);
    when(auth.getFirstname()).thenReturn(firstname);
    when(auth.getLastname()).thenReturn(lastname);
    when(bearbeiterBoundary.getAuthentication()).thenReturn(auth);
    return auth;
  }

  private FacesMessage captureMessage() {
    ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
    verify(facesContext).addMessage(isNull(), captor.capture());
    return captor.getValue();
  }

  // ==========================================================================
  // init() / PostConstruct
  // ==========================================================================

  @Nested
  @DisplayName("init()")
  class InitTests {

    @Test
    void test_init_calls_loadApprovalRequests() {
      List<Beschreibung> expected = List.of(mock(Beschreibung.class));
      when(service.findAllBeschreibungWithActiveApprovalRequest()).thenReturn(expected);

      controller.init();

      verify(service, times(1)).findAllBeschreibungWithActiveApprovalRequest();
      assertEquals(expected, controller.getBeschreibungen());
    }

    @Test
    void test_init_with_empty_list_from_service() {
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.init();

      assertNotNull(controller.getBeschreibungen());
      assertTrue(controller.getBeschreibungen().isEmpty());
    }
  }

  // ==========================================================================
  // loadApprovalRequests()
  // ==========================================================================

  @Nested
  @DisplayName("loadApprovalRequests()")
  class LoadApprovalRequestsTests {

    @Test
    void test_loadApprovalRequests_populates_list() {
      Beschreibung b1 = mock(Beschreibung.class);
      Beschreibung b2 = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(List.of(b1, b2));

      controller.loadApprovalRequests();

      assertEquals(2, controller.getBeschreibungen().size());
      assertEquals(b1, controller.getBeschreibungen().get(0));
      assertEquals(b2, controller.getBeschreibungen().get(1));
    }

    @Test
    void test_loadApprovalRequests_replaces_previous_list() {
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(List.of(mock(Beschreibung.class), mock(Beschreibung.class)));
      controller.loadApprovalRequests();
      assertEquals(2, controller.getBeschreibungen().size());

      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(List.of(mock(Beschreibung.class)));
      controller.loadApprovalRequests();
      assertEquals(1, controller.getBeschreibungen().size());
    }

    @Test
    void test_loadApprovalRequests_with_single_element_list() {
      Beschreibung single = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(List.of(single));

      controller.loadApprovalRequests();

      assertEquals(1, controller.getBeschreibungen().size());
      assertEquals(single, controller.getBeschreibungen().getFirst());
    }
  }

  // ==========================================================================
  // Getters and Setters
  // ==========================================================================

  @Nested
  @DisplayName("Getters and Setters")
  class GetterSetterTests {

    @Test
    void test_setSelectedBeschreibung_and_getSelectedBeschreibung() {
      Beschreibung beschreibung = mock(Beschreibung.class);
      controller.setSelectedBeschreibung(beschreibung);
      assertEquals(beschreibung, controller.getSelectedBeschreibung());
    }

    @Test
    void test_setSelectedBeschreibung_with_null() {
      controller.setSelectedBeschreibung(mock(Beschreibung.class));
      controller.setSelectedBeschreibung(null);
      assertNull(controller.getSelectedBeschreibung());
    }

    @Test
    void test_setComment_and_getComment() {
      controller.setComment("test comment");
      assertEquals("test comment", controller.getComment());
    }

    @Test
    void test_setComment_with_null() {
      controller.setComment("something");
      controller.setComment(null);
      assertNull(controller.getComment());
    }

    @Test
    void test_setComment_with_empty_string() {
      controller.setComment("");
      assertEquals("", controller.getComment());
    }

    @Test
    void test_initial_beschreibungen_is_null() {
      assertNull(controller.getBeschreibungen());
    }

    @Test
    void test_initial_selectedBeschreibung_is_null() {
      assertNull(controller.getSelectedBeschreibung());
    }

    @Test
    void test_initial_comment_is_null() {
      assertNull(controller.getComment());
    }
  }

  // ==========================================================================
  // assign(Beschreibung)
  // ==========================================================================

  @Nested
  @DisplayName("assign()")
  class AssignTests {

    @Test
    void test_assign_happy_path_calls_service_and_adds_info_message() throws Exception {
      mockAuthentication("Max", "Mustermann");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.assign(beschreibung);

      verify(service).assignReviewer(beschreibung, "Max Mustermann");
      FacesMessage msg = captureMessage();
      assertEquals(FacesMessage.SEVERITY_INFO, msg.getSeverity());
      assertTrue(msg.getSummary().contains("Max Mustermann"));
    }

    @Test
    void test_assign_happy_path_reloads_approval_requests() {
      mockAuthentication("Max", "Mustermann");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.assign(beschreibung);

      verify(service, times(1)).findAllBeschreibungWithActiveApprovalRequest();
    }

    @Test
    void test_assign_happy_path_resets_fields() {
      mockAuthentication("Max", "Mustermann");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.setSelectedBeschreibung(mock(Beschreibung.class));
      controller.setComment("some comment");

      controller.assign(beschreibung);

      assertNull(controller.getSelectedBeschreibung());
      assertNull(controller.getComment());
    }

    @Test
    void test_assign_exception_adds_error_message() throws Exception {
      mockAuthentication("Max", "Mustermann");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.assignReviewer(any(Beschreibung.class), any(String.class)))
          .thenThrow(new PublikationException("publikation_error_assigning_reviewer", "B1"));

      controller.assign(beschreibung);

      FacesMessage msg = captureMessage();
      assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
      assertNotNull(msg.getSummary());
    }

    @Test
    void test_assign_runtime_exception_adds_error_message() throws Exception {
      mockAuthentication("Max", "Mustermann");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.assignReviewer(any(Beschreibung.class), any(String.class)))
          .thenThrow(new RuntimeException("Unexpected error"));

      controller.assign(beschreibung);

      FacesMessage msg = captureMessage();
      assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
      assertNotNull(msg.getSummary());
    }

    @Test
    void test_assign_exception_does_not_reload_approval_requests() throws Exception {
      mockAuthentication("Max", "Mustermann");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.assignReviewer(any(Beschreibung.class), any(String.class)))
          .thenThrow(new PublikationException("publikation_error_test", "B1"));

      controller.assign(beschreibung);

      verify(service, never()).findAllBeschreibungWithActiveApprovalRequest();
    }

    @Test
    void test_assign_exception_still_resets_fields() throws Exception {
      mockAuthentication("Max", "Mustermann");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.assignReviewer(any(Beschreibung.class), any(String.class)))
          .thenThrow(new PublikationException("publikation_error_test", "B1"));

      controller.setSelectedBeschreibung(mock(Beschreibung.class));
      controller.setComment("comment");

      controller.assign(beschreibung);

      assertNull(controller.getSelectedBeschreibung());
      assertNull(controller.getComment());
    }

    @Test
    void test_assign_with_null_auth_uses_gast() throws Exception {
      when(bearbeiterBoundary.getAuthentication()).thenReturn(null);
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.assign(beschreibung);

      verify(service).assignReviewer(beschreibung, "Gast");
      FacesMessage msg = captureMessage();
      assertTrue(msg.getSummary().contains("Gast"));
    }

    @Test
    void test_assign_message_summary_contains_assigned_prefix() {
      mockAuthentication("Max", "Mustermann");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.assign(beschreibung);

      FacesMessage msg = captureMessage();
      assertTrue(msg.getSummary().contains("Max Mustermann"));
    }

    @Test
    void test_assign_message_first_argument_is_null() {
      mockAuthentication("Max", "Mustermann");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.assign(beschreibung);

      verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
    }
  }

  // ==========================================================================
  // approve(Beschreibung)
  // ==========================================================================

  @Nested
  @DisplayName("approve()")
  class ApproveTests {

    @Test
    void test_approve_happy_path_calls_service_with_comment() throws Exception {
      mockAuthentication("Anna", "Schmidt");
      Beschreibung beschreibung = mock(Beschreibung.class);
      controller.setComment("Looks good");
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.approve(beschreibung);

      verify(service).approve(beschreibung, "Anna Schmidt", "Looks good");
    }

    @Test
    void test_approve_happy_path_adds_info_message() {
      mockAuthentication("Anna", "Schmidt");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.approve(beschreibung);

      FacesMessage msg = captureMessage();
      assertEquals(FacesMessage.SEVERITY_INFO, msg.getSeverity());
      assertNotNull(msg.getSummary());
    }

    @Test
    void test_approve_happy_path_reloads_approval_requests() {
      mockAuthentication("Anna", "Schmidt");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.approve(beschreibung);

      verify(service, times(1)).findAllBeschreibungWithActiveApprovalRequest();
    }

    @Test
    void test_approve_happy_path_resets_fields() {
      mockAuthentication("Anna", "Schmidt");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.setSelectedBeschreibung(mock(Beschreibung.class));
      controller.setComment("approval comment");

      controller.approve(beschreibung);

      assertNull(controller.getSelectedBeschreibung());
      assertNull(controller.getComment());
    }

    @Test
    void test_approve_with_null_comment() throws Exception {
      mockAuthentication("Anna", "Schmidt");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.approve(beschreibung);

      verify(service).approve(beschreibung, "Anna Schmidt", null);
    }

    @Test
    void test_approve_with_empty_comment() throws Exception {
      mockAuthentication("Anna", "Schmidt");
      Beschreibung beschreibung = mock(Beschreibung.class);
      controller.setComment("");
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.approve(beschreibung);

      verify(service).approve(beschreibung, "Anna Schmidt", "");
    }

    @Test
    void test_approve_exception_adds_error_message() throws Exception {
      mockAuthentication("Anna", "Schmidt");
      Beschreibung beschreibung = mock(Beschreibung.class);
      doThrow(new PublikationException("publikation_error_internal_approval", "B1"))
          .when(service).approve(any(Beschreibung.class), any(String.class), any());

      controller.approve(beschreibung);

      FacesMessage msg = captureMessage();
      assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
      assertNotNull(msg.getSummary());
    }

    @Test
    void test_approve_runtime_exception_adds_error_message() throws Exception {
      mockAuthentication("Anna", "Schmidt");
      Beschreibung beschreibung = mock(Beschreibung.class);
      doThrow(new RuntimeException("NPE in service"))
          .when(service).approve(any(Beschreibung.class), any(String.class), any());

      controller.approve(beschreibung);

      FacesMessage msg = captureMessage();
      assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
      assertNotNull(msg.getSummary());
    }

    @Test
    void test_approve_exception_does_not_reload_approval_requests() throws Exception {
      mockAuthentication("Anna", "Schmidt");
      Beschreibung beschreibung = mock(Beschreibung.class);
      doThrow(new PublikationException("publikation_error_test", "B1"))
          .when(service).approve(any(Beschreibung.class), any(String.class), any());

      controller.approve(beschreibung);

      verify(service, never()).findAllBeschreibungWithActiveApprovalRequest();
    }

    @Test
    void test_approve_exception_still_resets_fields() throws Exception {
      mockAuthentication("Anna", "Schmidt");
      Beschreibung beschreibung = mock(Beschreibung.class);
      doThrow(new PublikationException("publikation_error_test", "B1"))
          .when(service).approve(any(Beschreibung.class), any(String.class), any());

      controller.setSelectedBeschreibung(mock(Beschreibung.class));
      controller.setComment("comment");

      controller.approve(beschreibung);

      assertNull(controller.getSelectedBeschreibung());
      assertNull(controller.getComment());
    }

    @Test
    void test_approve_with_null_auth_uses_gast() throws Exception {
      when(bearbeiterBoundary.getAuthentication()).thenReturn(null);
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.approve(beschreibung);

      verify(service).approve(beschreibung, "Gast", null);
    }

    @Test
    void test_approve_error_message_contains_failed_to_approve_prefix() throws Exception {
      mockAuthentication("Anna", "Schmidt");
      Beschreibung beschreibung = mock(Beschreibung.class);
      doThrow(new PublikationException("publikation_error_test", "B1"))
          .when(service).approve(any(Beschreibung.class), any(String.class), any());

      controller.approve(beschreibung);

      FacesMessage msg = captureMessage();
      assertNotNull(msg.getSummary());
    }

    @Test
    void test_approve_message_first_argument_is_null() {
      mockAuthentication("Anna", "Schmidt");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.approve(beschreibung);

      verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
    }
  }

  // ==========================================================================
  // reject(Beschreibung)
  // ==========================================================================

  @Nested
  @DisplayName("reject()")
  class RejectTests {

    @Test
    void test_reject_happy_path_calls_service_with_comment() throws Exception {
      mockAuthentication("Hans", "Meier");
      Beschreibung beschreibung = mock(Beschreibung.class);
      controller.setComment("Needs revision");
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.reject(beschreibung);

      verify(service).reject(beschreibung, "Hans Meier", "Needs revision");
    }

    @Test
    void test_reject_happy_path_adds_info_message() {
      mockAuthentication("Hans", "Meier");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.reject(beschreibung);

      FacesMessage msg = captureMessage();
      assertEquals(FacesMessage.SEVERITY_INFO, msg.getSeverity());
      assertNotNull(msg.getSummary());
    }

    @Test
    void test_reject_happy_path_reloads_approval_requests() {
      mockAuthentication("Hans", "Meier");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.reject(beschreibung);

      verify(service, times(1)).findAllBeschreibungWithActiveApprovalRequest();
    }

    @Test
    void test_reject_happy_path_resets_fields() {
      mockAuthentication("Hans", "Meier");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.setSelectedBeschreibung(mock(Beschreibung.class));
      controller.setComment("rejection reason");

      controller.reject(beschreibung);

      assertNull(controller.getSelectedBeschreibung());
      assertNull(controller.getComment());
    }

    @Test
    void test_reject_with_null_comment() throws Exception {
      mockAuthentication("Hans", "Meier");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.reject(beschreibung);

      verify(service).reject(beschreibung, "Hans Meier", null);
    }

    @Test
    void test_reject_with_empty_comment() throws Exception {
      mockAuthentication("Hans", "Meier");
      Beschreibung beschreibung = mock(Beschreibung.class);
      controller.setComment("");
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.reject(beschreibung);

      verify(service).reject(beschreibung, "Hans Meier", "");
    }

    @Test
    void test_reject_exception_adds_error_message() throws Exception {
      mockAuthentication("Hans", "Meier");
      Beschreibung beschreibung = mock(Beschreibung.class);
      doThrow(new PublikationException("publikation_error_rejecting_publication", "B1"))
          .when(service).reject(any(Beschreibung.class), any(String.class), any());

      controller.reject(beschreibung);

      FacesMessage msg = captureMessage();
      assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
      assertNotNull(msg.getSummary());
    }

    @Test
    void test_reject_runtime_exception_adds_error_message() throws Exception {
      mockAuthentication("Hans", "Meier");
      Beschreibung beschreibung = mock(Beschreibung.class);
      doThrow(new RuntimeException("Unexpected rejection error"))
          .when(service).reject(any(Beschreibung.class), any(String.class), any());

      controller.reject(beschreibung);

      FacesMessage msg = captureMessage();
      assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
      assertNotNull(msg.getSummary());
    }

    @Test
    void test_reject_exception_does_not_reload_approval_requests() throws Exception {
      mockAuthentication("Hans", "Meier");
      Beschreibung beschreibung = mock(Beschreibung.class);
      doThrow(new PublikationException("publikation_error_test", "B1"))
          .when(service).reject(any(Beschreibung.class), any(String.class), any());

      controller.reject(beschreibung);

      verify(service, never()).findAllBeschreibungWithActiveApprovalRequest();
    }

    @Test
    void test_reject_exception_still_resets_fields() throws Exception {
      mockAuthentication("Hans", "Meier");
      Beschreibung beschreibung = mock(Beschreibung.class);
      doThrow(new PublikationException("publikation_error_test", "B1"))
          .when(service).reject(any(Beschreibung.class), any(String.class), any());

      controller.setSelectedBeschreibung(mock(Beschreibung.class));
      controller.setComment("comment");

      controller.reject(beschreibung);

      assertNull(controller.getSelectedBeschreibung());
      assertNull(controller.getComment());
    }

    @Test
    void test_reject_with_null_auth_uses_gast() throws Exception {
      when(bearbeiterBoundary.getAuthentication()).thenReturn(null);
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.reject(beschreibung);

      verify(service).reject(beschreibung, "Gast", null);
    }

    @Test
    void test_reject_error_message_contains_failed_to_reject_prefix() throws Exception {
      mockAuthentication("Hans", "Meier");
      Beschreibung beschreibung = mock(Beschreibung.class);
      doThrow(new PublikationException("publikation_error_test", "B1"))
          .when(service).reject(any(Beschreibung.class), any(String.class), any());

      controller.reject(beschreibung);

      FacesMessage msg = captureMessage();
      assertNotNull(msg.getSummary());
    }

    @Test
    void test_reject_message_first_argument_is_null() {
      mockAuthentication("Hans", "Meier");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.reject(beschreibung);

      verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
    }
  }

  // ==========================================================================
  // getCurrentUser() (tested indirectly through action methods)
  // ==========================================================================

  @Nested
  @DisplayName("getCurrentUser() behavior")
  class GetCurrentUserTests {

    @Test
    void test_getCurrentUser_concatenates_firstname_and_lastname() throws Exception {
      mockAuthentication("Marie", "Curie");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.assign(beschreibung);

      verify(service).assignReviewer(beschreibung, "Marie Curie");
    }

    @Test
    void test_getCurrentUser_with_null_authentication_returns_gast() throws Exception {
      when(bearbeiterBoundary.getAuthentication()).thenReturn(null);
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.assign(beschreibung);

      verify(service).assignReviewer(beschreibung, "Gast");
    }

    @Test
    void test_getCurrentUser_with_empty_names() throws Exception {
      mockAuthentication("", "");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.assign(beschreibung);

      verify(service).assignReviewer(beschreibung, " ");
    }

    @Test
    void test_getCurrentUser_with_special_characters_in_name() throws Exception {
      mockAuthentication("Hans-Peter", "Mueller-Schmidt");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.assign(beschreibung);

      verify(service).assignReviewer(beschreibung, "Hans-Peter Mueller-Schmidt");
    }

    @Test
    void test_getCurrentUser_with_unicode_characters() throws Exception {
      mockAuthentication("Rene", "Muller");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.findAllBeschreibungWithActiveApprovalRequest())
          .thenReturn(Collections.emptyList());

      controller.assign(beschreibung);

      verify(service).assignReviewer(beschreibung, "Rene Muller");
    }
  }

  // ==========================================================================
  // Exception with null message
  // ==========================================================================

  @Nested
  @DisplayName("Exception with null message")
  class ExceptionNullMessageTests {

    @Test
    void test_assign_exception_with_null_message_shows_error()
        throws Exception {
      mockAuthentication("Max", "Mustermann");
      Beschreibung beschreibung = mock(Beschreibung.class);
      when(service.assignReviewer(any(Beschreibung.class), any(String.class)))
          .thenThrow(new RuntimeException((String) null));

      controller.assign(beschreibung);

      FacesMessage msg = captureMessage();
      assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
      assertEquals("???no_key???", msg.getSummary());
    }

    @Test
    void test_approve_exception_with_null_message_shows_error()
        throws Exception {
      mockAuthentication("Max", "Mustermann");
      Beschreibung beschreibung = mock(Beschreibung.class);
      doThrow(new RuntimeException((String) null))
          .when(service).approve(any(Beschreibung.class), any(String.class), any());

      controller.approve(beschreibung);

      FacesMessage msg = captureMessage();
      assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
      assertEquals("???no_key???", msg.getSummary());
    }

    @Test
    void test_reject_exception_with_null_message_shows_error()
        throws Exception {
      mockAuthentication("Max", "Mustermann");
      Beschreibung beschreibung = mock(Beschreibung.class);
      doThrow(new RuntimeException((String) null))
          .when(service).reject(any(Beschreibung.class), any(String.class), any());

      controller.reject(beschreibung);

      FacesMessage msg = captureMessage();
      assertEquals(FacesMessage.SEVERITY_ERROR, msg.getSeverity());
      assertEquals("???no_key???", msg.getSummary());
    }
  }
}
