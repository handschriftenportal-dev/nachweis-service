package de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.GNDEntityFact.PERSON_TYPE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions;
import de.staatsbibliothek.berlin.javaee.authentication.entity.Authentication;
import de.staatsbibliothek.berlin.javaee.authentication.exception.AuthenticationException;
import de.staatsbibliothek.berlin.javaee.authentication.exception.IdentityProviderNotAvailableException;
import de.staatsbibliothek.berlin.javaee.authentication.exception.OAUTH2Exception;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import de.staatsbibliothek.berlin.javaee.authentication.service.AuthenticationService;
import de.staatsbibliothek.berlin.javaee.authentication.services.AuthenticationImpl;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;

/**
 * @author Michael.Hintersonnleitner@sbb.spk-berlin.de
 * @since 9.11.2021
 */
@Slf4j
@QuarkusTest
@TestTransaction
@TestInstance(Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
public class BearbeiterServiceTest {

  AuthenticationRepository authenticationRepositoryMock;
  AuthenticationService authenticationServiceMock;

  @Inject
  BearbeiterRepository bearbeiterRepository;

  MockedStatic<FacesContext> contextStatic;
  FacesContext facesContext;
  ExternalContext context;
  HttpSession session;
  Map<String, Object> sessionAttributes;

  NormdatenReferenz institution = new NormdatenReferenz("1", "5036103-X",
      "Staatsbibliothek zu Berlin",
      NormdatenReferenz.KOERPERSCHAFT_TYPE_NAME);

  NormdatenReferenz person = new NormdatenReferenz("2", "118575449", "Luther, Martin",
      PERSON_TYPE_NAME);

  Bearbeiter bearbeiter;

  Set<String> permission = new HashSet<>(Arrays.asList(
      Permissions.B_HSP_SUPER_REDAKTEUR_APP,
      Permissions.B_HSP_REDAKTEUR_APP,
      Permissions.B_HSP_ERSCHLIESSER_APP,
      Permissions.B_HSP_ENDNUTZER_APP));

  AuthenticationImpl authenticationAdmin = new AuthenticationImpl("", true, "b-ml123", permission,
      permission,
      permission, "Martin", "Luther", "martin@luther.de",
      LocalDateTime.now());

  @BeforeEach
  void beforeEach() throws BearbeiterException {
    authenticationRepositoryMock = mock(AuthenticationRepository.class);
    authenticationServiceMock = mock(AuthenticationService.class);
    contextStatic = mockStatic(FacesContext.class);
    facesContext = mock(FacesContext.class);
    context = mock(ExternalContext.class);
    session = mock(HttpSession.class);
    sessionAttributes = new HashMap<>();

    bearbeiter = Bearbeiter.newBuilder()
        .withBearbeitername("b-ml123")
        .withVorname("Martin")
        .withNachname("Luther")
        .withEmail("martin@luther.de")
        .withRolle("Redakteur")
        .build();

    when(session.getAttribute(anyString()))
        .thenAnswer(i -> sessionAttributes.get(i.getArguments()[0]));

    doAnswer((Answer<Void>) invocation -> {
      sessionAttributes.put((String) invocation.getArguments()[0], invocation.getArguments()[1]);
      return null;
    }).when(session).setAttribute(anyString(), any());

    doAnswer((Answer<Void>) invocation -> {
      sessionAttributes.remove((String) invocation.getArguments()[0]);
      return null;
    }).when(session).removeAttribute(anyString());

    contextStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
    when(facesContext.getExternalContext()).thenReturn(context);
    when(context.getSession(false)).thenReturn(session);
  }

  @AfterEach
  public void resetMocks() {
    contextStatic.close();
  }

  @Test
  void testGetUnbekannterBearbeiter() {
    BearbeiterService bearbeiterService = new BearbeiterService(bearbeiterRepository,
        authenticationRepositoryMock, authenticationServiceMock);

    Bearbeiter unbekannterBearbeiter = bearbeiterService.getUnbekannterBearbeiter();

    assertNotNull(unbekannterBearbeiter);
    assertEquals(BearbeiterService.UNBEKANNTER_BEARBEITER_ID, unbekannterBearbeiter.getId());
    assertEquals(BearbeiterService.UNBEKANNTER_BEARBEITER_ID,
        unbekannterBearbeiter.getBearbeitername());
    assertNull(unbekannterBearbeiter.getEmail());
    assertNull(unbekannterBearbeiter.getVorname());
    assertNull(unbekannterBearbeiter.getNachname());
    assertNull(unbekannterBearbeiter.getInstitution());

    Optional<Bearbeiter> bearbeiter = bearbeiterRepository.findByIdOptional(
        BearbeiterService.UNBEKANNTER_BEARBEITER_ID);
    assertTrue(bearbeiter.isPresent());
  }

  @Test
  void testFindBearbeiterWithName() {
    bearbeiterRepository.saveAndFlush(bearbeiter);

    BearbeiterService bearbeiterService = new BearbeiterService(bearbeiterRepository,
        authenticationRepositoryMock, authenticationServiceMock);

    Bearbeiter found = bearbeiterService.findBearbeiterWithName(bearbeiter.getBearbeitername());
    assertNotNull(found);
    assertEquals(bearbeiter, found);
  }

  @Test
  void testfindBearbeiterByFirstnameOrLastname() {

    bearbeiterRepository.deleteAll();

    Bearbeiter testBearbeiter = Bearbeiter.newBuilder()
        .withBearbeitername("b-ke101")
        .withVorname("Martin")
        .withNachname("Luther")
        .withEmail("martin@luther.de")
        .withRolle("Redakteur")
        .build();

    bearbeiterRepository.saveAndFlush(testBearbeiter);
    BearbeiterService bearbeiterService = new BearbeiterService(bearbeiterRepository,
        authenticationRepositoryMock, authenticationServiceMock);

    List<Bearbeiter> resultWithLastName = bearbeiterService.findBearbeiterByFirstnameOrLastname(
        "Lu");

    assertNotNull(resultWithLastName);

    assertEquals(1, resultWithLastName.size());

    assertEquals(testBearbeiter, resultWithLastName.get(0));

    List<Bearbeiter> resultWithFirstName = bearbeiterService.findBearbeiterByFirstnameOrLastname(
        "Ma");

    assertEquals(testBearbeiter, resultWithFirstName.get(0));

  }

  @Test
  void testGetActiveSession() {
    BearbeiterService bearbeiterService = new BearbeiterService(bearbeiterRepository,
        authenticationRepositoryMock, authenticationServiceMock);

    HttpSession session = bearbeiterService.getActiveSession();
    assertNotNull(session);
  }

  @Test
  void testGetAuthentication() {
    BearbeiterService bearbeiterService = new BearbeiterService(
        bearbeiterRepository,
        authenticationRepositoryMock,
        authenticationServiceMock
    );

    assertNull(bearbeiterService.getAuthentication());

    when(authenticationRepositoryMock.findAuthentication()).thenReturn(authenticationAdmin);

    Authentication authentication = bearbeiterService.getAuthentication();

    assertEquals(authenticationAdmin, authentication);
  }

  @Test
  void testGetLoggedBearbeiter() {
    BearbeiterService bearbeiterService = new BearbeiterService(bearbeiterRepository,
        authenticationRepositoryMock, authenticationServiceMock);

    Bearbeiter unbekannterBearbeiter = bearbeiterService.getLoggedBearbeiter();
    assertNotNull(unbekannterBearbeiter);
    assertEquals(BearbeiterService.UNBEKANNTER_BEARBEITER_ID, unbekannterBearbeiter.getId());
    assertEquals(BearbeiterService.UNBEKANNTER_BEARBEITER_ID,
        unbekannterBearbeiter.getBearbeitername());

    when(authenticationRepositoryMock.findAuthentication()).thenReturn(authenticationAdmin);
    Bearbeiter loggedBearbeiter = bearbeiterService.getLoggedBearbeiter();
    assertNotNull(loggedBearbeiter);
    assertEquals(bearbeiter, loggedBearbeiter);
  }

  @Test
  void testGetRole() {
    BearbeiterService bearbeiterService = new BearbeiterService(bearbeiterRepository,
        authenticationRepositoryMock, authenticationServiceMock);

    String role_1 = bearbeiterService.getRole();
    assertEquals("nicht angemeldeter Nutzer", role_1);

    when(authenticationRepositoryMock.findAuthentication()).thenReturn(authenticationAdmin);

    String role_2 = bearbeiterService.getRole();
    assertEquals("Super-Redakteur", role_2);
  }

  @Test
  void testGetRoleWithAuthentication() {
    BearbeiterService bearbeiterService = new BearbeiterService(bearbeiterRepository,
        authenticationRepositoryMock, authenticationServiceMock);

    String role_1 = bearbeiterService.getRole();
    assertEquals("nicht angemeldeter Nutzer", role_1);

    String role_2 = bearbeiterService.getRole(authenticationAdmin);
    assertEquals("Super-Redakteur", role_2);
  }

  @Test
  void testIsBearbeiterNew() {
    BearbeiterService bearbeiterService = new BearbeiterService(bearbeiterRepository,
        authenticationRepositoryMock, authenticationServiceMock);

    assertFalse(bearbeiterService.isBearbeiterNew());

    sessionAttributes.put(BearbeiterService.LOGGED_USER_NEW_BEARBEITER,
        BearbeiterService.LOGGED_USER_NEW_BEARBEITER);
    assertTrue(bearbeiterService.isBearbeiterNew());
  }

  @Test
  void testLogout() throws AuthenticationException, IdentityProviderNotAvailableException {
    BearbeiterService bearbeiterService = new BearbeiterService(bearbeiterRepository,
        authenticationRepositoryMock, authenticationServiceMock);

    bearbeiterService.logout();
    verify(authenticationServiceMock, times(1)).logout();
    verify(context, times(1)).invalidateSession();
  }

  @Test
  void testStartLogin() throws AuthenticationException, IdentityProviderNotAvailableException {
    BearbeiterService bearbeiterService = new BearbeiterService(bearbeiterRepository,
        authenticationRepositoryMock, authenticationServiceMock);

    bearbeiterService.startLogin();
    verify(authenticationServiceMock, times(1)).startLoginProcess();
  }

  @Test
  void testUpdateBearbeiterDaten() {
    BearbeiterService bearbeiterService = new BearbeiterService(bearbeiterRepository,
        authenticationRepositoryMock, authenticationServiceMock);

    Bearbeiter toUpdate = Bearbeiter.newBuilder()
        .withBearbeitername(authenticationAdmin.getUsername())
        .withEmail("to@change.de")
        .withNachname("nachname")
        .withRolle("Redakteur")
        .withVorname("Vorname")
        .build();

    bearbeiterRepository.saveAndFlush(toUpdate);

    Bearbeiter updated = bearbeiterService.updateBearbeiterDaten(toUpdate, authenticationAdmin,
        bearbeiter.getBearbeitername());

    assertNotNull(updated);
    assertEquals(authenticationAdmin.getUsername(), updated.getId());
    assertEquals(authenticationAdmin.getUsername(), updated.getBearbeitername());
    assertEquals(authenticationAdmin.getFirstname(), updated.getVorname());
    assertEquals(authenticationAdmin.getLastname(), updated.getNachname());
    assertEquals(authenticationAdmin.getEmail(), updated.getEmail());
    assertEquals("Super-Redakteur", updated.getRolle());
    assertNull(bearbeiter.getPerson());
    assertNull(bearbeiter.getInstitution());
  }

  @Test
  void testUpdateLoggedBearbeiter() throws BearbeiterException {
    BearbeiterService bearbeiterService = new BearbeiterService(bearbeiterRepository,
        authenticationRepositoryMock, authenticationServiceMock);

    when(authenticationRepositoryMock.findAuthentication()).thenReturn(authenticationAdmin);

    bearbeiterRepository.saveAndFlush(bearbeiter);
    sessionAttributes.put(BearbeiterService.LOGGED_USER_SESSION_KEY, bearbeiter);
    sessionAttributes.put(BearbeiterService.LOGGED_USER_NEW_BEARBEITER,
        BearbeiterService.LOGGED_USER_NEW_BEARBEITER);

    Bearbeiter updated = bearbeiterService.updateLoggedBearbeiter(person, institution);

    assertNotNull(updated);
    assertEquals(bearbeiter.getId(), updated.getId());
    assertEquals(bearbeiter.getBearbeitername(), updated.getBearbeitername());
    assertEquals(bearbeiter.getVorname(), updated.getVorname());
    assertEquals(bearbeiter.getNachname(), updated.getNachname());
    assertEquals(bearbeiter.getEmail(), updated.getEmail());

    assertNotNull(bearbeiter.getPerson());
    assertEquals(person.getId(), bearbeiter.getPerson().getId());
    assertEquals(person.getName(), bearbeiter.getPerson().getName());
    assertEquals(person.getGndID(), bearbeiter.getPerson().getGndID());
    assertEquals(person.getTypeName(), bearbeiter.getPerson().getTypeName());

    assertNotNull(bearbeiter.getInstitution());
    assertEquals(institution.getId(), bearbeiter.getInstitution().getId());
    assertEquals(institution.getName(), bearbeiter.getInstitution().getName());
    assertEquals(institution.getGndID(), bearbeiter.getInstitution().getGndID());
    assertEquals(institution.getTypeName(), bearbeiter.getInstitution().getTypeName());

    assertFalse(bearbeiterService.isBearbeiterNew());
  }

  @Test
  void testGetAllBearbeiter() {

    BearbeiterService bearbeiterService = new BearbeiterService(bearbeiterRepository,
        authenticationRepositoryMock, authenticationServiceMock);

    bearbeiterRepository.deleteAll();
    assertEquals(0, bearbeiterService.getAllBearbeiter().size());

    Bearbeiter bearbeiter1 = Bearbeiter.newBuilder()
        .withBearbeitername("112233")
        .withNachname("Nachname")
        .withVorname("Vorname")
        .build();
    bearbeiterRepository.saveAndFlush(bearbeiter1);

    Bearbeiter bearbeiter2 = Bearbeiter.newBuilder()
        .withBearbeitername("445566")
        .withNachname("nachname1")
        .withVorname("vorname1")
        .build();

    bearbeiterRepository.saveAndFlush(bearbeiter2);

    Set<Bearbeiter> bearbeiterSet = bearbeiterService.getAllBearbeiter();
    assertNotNull(bearbeiterSet);
    assertEquals(2, bearbeiterSet.size());

    Set<Bearbeiter> bearbeiters = new LinkedHashSet<>();
    bearbeiters.add(bearbeiter1);
    bearbeiters.add(bearbeiter2);

    assertEquals(2, bearbeiters.size());
    assertEquals(bearbeiters, bearbeiterSet);

  }

  @Test
  @TestTransaction
  void testGetConfig() throws OAUTH2Exception {

    BearbeiterService bearbeiterService = new BearbeiterService(bearbeiterRepository,
        authenticationRepositoryMock, authenticationServiceMock);
    Bearbeiter bearbeiter1 = Bearbeiter.newBuilder()
        .withBearbeitername("USER1")
        .withNachname("Nachname")
        .withVorname("Vorname")
        .withEditorConfig("""
            { "bookmarks": [1, 5, 7] }""")
        .build();
    bearbeiterRepository.saveAndFlush(bearbeiter1);

    var config = bearbeiterService.getConfig("USER1");

    assertEquals("""
        { "bookmarks": [1, 5, 7] }""", config);
  }

  @Test
  @TestTransaction
  void testSetConfig() throws OAUTH2Exception {
    BearbeiterService bearbeiterService = new BearbeiterService(bearbeiterRepository,
        authenticationRepositoryMock, authenticationServiceMock);
    Bearbeiter bearbeiter1 = Bearbeiter.newBuilder()
        .withBearbeitername("USER2")
        .withNachname("Nachname")
        .withVorname("Vorname")
        .build();
    bearbeiterRepository.saveAndFlush(bearbeiter1);

    bearbeiterService.setConfig("""
        { "bookmarks": [3, 7, 11] }""", "USER2");

    var config = bearbeiterService.getConfig("USER2");

    assertEquals("""
        { "bookmarks": [3, 7, 11] }""", config);
  }

}
