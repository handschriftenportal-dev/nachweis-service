package de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_CMS_REDAKTEUR_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ENDNUTZER_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ERSCHLIESSER_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_LOKALE_REDAKTEUR_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_NORMDATEN_REDAKTEUR_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_REDAKTEUR_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_SUPER_REDAKTEUR_APP;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.common.I18NController;
import de.staatsbibliothek.berlin.javaee.authentication.OAUTH2;
import de.staatsbibliothek.berlin.javaee.authentication.entity.Authentication;
import de.staatsbibliothek.berlin.javaee.authentication.exception.AuthenticationException;
import de.staatsbibliothek.berlin.javaee.authentication.exception.IdentityProviderNotAvailableException;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import de.staatsbibliothek.berlin.javaee.authentication.service.AuthenticationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Provide access to authenticated user with authorized roles and permissions
 *
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 10.09.21
 */
@ApplicationScoped
@Slf4j
public class BearbeiterService implements BearbeiterBoundary, Serializable {

  public static final String UNBEKANNTER_BEARBEITER_ID = "unbekannterBearbeiter";
  public static final String LOGGED_USER_SESSION_KEY = "LOGGED_USER";
  public static final String LOGGED_USER_AUTH_SESSION_KEY = "LOGGED_USER_AUTH";
  public static final String LOGGED_USER_NEW_BEARBEITER = "LOGGED_USER_NEW_BEARBEITER";

  @Serial
  private static final long serialVersionUID = 8524863140840965054L;

  private BearbeiterRepository bearbeiterRepository;

  private AuthenticationRepository authenticationRepository;
  private AuthenticationService authenticationService;

  BearbeiterService() {
  }

  @Inject
  public BearbeiterService(
      BearbeiterRepository bearbeiterRepository,
      AuthenticationRepository authenticationRepository,
      @OAUTH2 AuthenticationService authenticationService) {
    this.bearbeiterRepository = bearbeiterRepository;
    this.authenticationRepository = authenticationRepository;
    this.authenticationService = authenticationService;
  }

  @Override
  @Transactional(TxType.REQUIRES_NEW)
  public Bearbeiter getLoggedBearbeiter() {
    try {
      HttpSession session = getActiveSession();
      if (session == null) {
        return getUnbekannterBearbeiter();
      }
      Bearbeiter bearbeiter = (Bearbeiter) session.getAttribute(LOGGED_USER_SESSION_KEY);
      if (bearbeiter == null) {
        Authentication auth = authenticationRepository.findAuthentication();
        if (auth == null) {
          return getUnbekannterBearbeiter();
        }
        String username = auth.getUsername();

        bearbeiter = bearbeiterRepository.findById(username);
        if (bearbeiter == null) {
          log.info("Create the local bearbeiter with authentication {}", username);
          String rolle = getRole(auth);

          bearbeiter =
              Bearbeiter.newBuilder()
                  .withBearbeitername(username)
                  .withVorname(auth.getFirstname())
                  .withNachname(auth.getLastname())
                  .withEmail(auth.getEmail())
                  .withRolle(rolle)
                  .build();

          bearbeiterRepository.persistAndFlush(bearbeiter);
          session.setAttribute(LOGGED_USER_NEW_BEARBEITER, LOGGED_USER_NEW_BEARBEITER);
        }
        bearbeiter = updateBearbeiterDaten(bearbeiter, auth, username);
        session.setAttribute(LOGGED_USER_SESSION_KEY, bearbeiter);
        session.setAttribute(LOGGED_USER_AUTH_SESSION_KEY, auth);
      }

      return bearbeiter;
    } catch (Exception ex) {
      log.warn("Unable to obtain logged user id {}", ex.getMessage(), ex);
      return getUnbekannterBearbeiter();
    }
  }

  @Override
  public Set<Bearbeiter> getAllBearbeiter() {
    return bearbeiterRepository.findAll().stream().collect(Collectors.toSet());
  }

  @Override
  public boolean isBearbeiterNew() {
    HttpSession session = getActiveSession();
    if (session == null) {
      return false;
    }
    return session.getAttribute(LOGGED_USER_NEW_BEARBEITER) != null;
  }

  HttpSession getActiveSession() {
    FacesContext currentInstance = FacesContext.getCurrentInstance();
    if (currentInstance == null) {
      return null;
    }
    ExternalContext externalContext = currentInstance.getExternalContext();
    if (externalContext == null) {
      return null;
    }
    return (HttpSession) externalContext.getSession(false);
  }

  @Override
  public Authentication getAuthentication() {
    HttpSession session = getActiveSession();
    if (session == null) {
      return null;
    }
    Authentication authentication = authenticationRepository.findAuthentication();

    if (authentication == null) {
      getLoggedBearbeiter();
      authentication = (Authentication) session.getAttribute(LOGGED_USER_AUTH_SESSION_KEY);
    }
    return authentication;
  }

  @Override
  public void logout() {
    try {
      authenticationService.logout();
    } catch (AuthenticationException e) {
      log.error("Error during logout", e);
    } catch (IdentityProviderNotAvailableException e) {
      log.error("Identity provider is not available", e);
    } finally {
      FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
    }
  }

  @Override
  public void startLogin() {
    try {
      authenticationService.startLoginProcess();
    } catch (Exception e) {
      log.error("Fehler beim Starten des Anmeldevorgangs", e);
      FacesContext facesContext = FacesContext.getCurrentInstance();
      facesContext.getExternalContext().getFlash().setKeepMessages(true);
      facesContext.addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR,
              I18NController.getMessage("login_fehler_summary"),
              I18NController.getMessage("login_fehler_idp_nicht_erreichbar")));
    }
  }

  @Override
  public String getConfig(String username) {
    var currentUser = bearbeiterRepository.findById(username);
    return currentUser.getEditorConfig();
  }

  @Transactional(TxType.REQUIRES_NEW)
  @Override
  public void setConfig(String config, String username) {
    var currentUser = bearbeiterRepository.findById(username);
    currentUser.setEditorConfig(config);
    bearbeiterRepository.saveAndFlush(currentUser);
  }

  @Override
  public String getRole() {
    return getRole(getAuthentication());
  }

  protected Bearbeiter updateBearbeiterDaten(
      Bearbeiter bearbeiter, Authentication auth, String username) {
    if (Objects.isNull(bearbeiter)) {
      throw new NullPointerException("Bearbeiter must not be null!");
    }

    String rolle = getRole(auth);
    if (!(Objects.equals(bearbeiter.getVorname(), auth.getFirstname())
        && Objects.equals(bearbeiter.getNachname(), auth.getLastname())
        && Objects.equals(bearbeiter.getEmail(), auth.getEmail())
        && Objects.equals(bearbeiter.getRolle(), rolle))) {
      log.info("Update local bearbeiter with authentication {}", username);
      bearbeiter.setVorname(auth.getFirstname());
      bearbeiter.setNachname(auth.getLastname());
      bearbeiter.setEmail(auth.getEmail());
      bearbeiter.setRolle(rolle);
      bearbeiter = bearbeiterRepository.save(bearbeiter);
    }
    return bearbeiter;
  }

  @Override
  @Transactional(TxType.REQUIRES_NEW)
  public Bearbeiter getUnbekannterBearbeiter() {
    Bearbeiter unbekannterBearbeiter = bearbeiterRepository.findById(UNBEKANNTER_BEARBEITER_ID);

    if (unbekannterBearbeiter == null) {
      unbekannterBearbeiter = new Bearbeiter(UNBEKANNTER_BEARBEITER_ID, UNBEKANNTER_BEARBEITER_ID);
      bearbeiterRepository.persistAndFlush(unbekannterBearbeiter);
    }
    return unbekannterBearbeiter;
  }

  @Override
  @Transactional(TxType.REQUIRES_NEW)
  public Bearbeiter updateLoggedBearbeiter(NormdatenReferenz person, NormdatenReferenz institution)
      throws BearbeiterException {
    HttpSession session = getActiveSession();
    if (session == null) {
      throw new BearbeiterException("No HttpSession available!");
    }
    Bearbeiter bearbeiter = (Bearbeiter) session.getAttribute(LOGGED_USER_SESSION_KEY);
    if (Objects.isNull(bearbeiter)) {
      throw new BearbeiterException("No Bearbeiter saved in session!");
    }

    bearbeiter.setInstitution(institution);
    bearbeiter.setPerson(person);

    bearbeiter = bearbeiterRepository.saveAndFlush(bearbeiter);
    session.setAttribute(LOGGED_USER_SESSION_KEY, bearbeiter);

    session.removeAttribute(LOGGED_USER_NEW_BEARBEITER);

    log.info("Updated logged bearbeiter {}", bearbeiter.getId());
    return bearbeiter;
  }

  @Override
  public Bearbeiter findBearbeiterWithName(String name) {
    Bearbeiter bearbeiter = bearbeiterRepository.findByBearbeiterName(name);
    if (bearbeiter == null) {
      bearbeiter = getUnbekannterBearbeiter();
    }
    return bearbeiter;
  }

  @Override
  public List<Bearbeiter> findBearbeiterByNames(Collection<String> name) {
    List<Bearbeiter> bearbeiter = bearbeiterRepository.findByBearbeiterName(name);
    while (bearbeiter.size() < name.size()) {
      bearbeiter.add(getUnbekannterBearbeiter());
    }
    return bearbeiter;
  }

  @Override
  public List<Bearbeiter> findBearbeiterByFirstnameOrLastname(String name) {

    if (name == null || name.isEmpty()) {
      return List.of();
    }

    return bearbeiterRepository.findAll().stream()
        .parallel()
        .filter(
            bearbeiter ->
                (bearbeiter.getNachname() != null
                    && bearbeiter.getNachname().toLowerCase().startsWith(name.toLowerCase()))
                    || (bearbeiter.getVorname() != null
                    && bearbeiter.getVorname().toLowerCase().startsWith(name.toLowerCase())))
        .collect(Collectors.toList());
  }

  public void setBearbeiterRepository(BearbeiterRepository bearbeiterRepository) {
    this.bearbeiterRepository = bearbeiterRepository;
  }

  public void setAuthenticationRepository(AuthenticationRepository authenticationRepository) {
    this.authenticationRepository = authenticationRepository;
  }

  public void setAuthenticationService(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  String getRole(Authentication auth) {
    String role;
    if (auth == null) {
      role = "nicht angemeldeter Nutzer";
    } else {
      String roles = auth.getRole().toString();
      if (roles.contains(B_HSP_SUPER_REDAKTEUR_APP)) {
        role = "Super-Redakteur";
      } else if (roles.contains(B_HSP_REDAKTEUR_APP)) {
        role = "Redakteur";
      } else if (roles.contains(B_HSP_NORMDATEN_REDAKTEUR_APP)) {
        role = "Normdaten Redakteur";
      } else if (roles.contains(B_HSP_LOKALE_REDAKTEUR_APP)) {
        role = "Lokale Redakteur";
      } else if (roles.contains(B_HSP_CMS_REDAKTEUR_APP)) {
        role = "CMS Redakteur";
      } else if (roles.contains(B_HSP_ERSCHLIESSER_APP)) {
        role = "Erschließer";
      } else if (roles.contains(B_HSP_ENDNUTZER_APP)) {
        role = "Nutzer";
      } else {
        role = "nicht angemeldeter Nutzer";
      }
    }
    return role;
  }
}
