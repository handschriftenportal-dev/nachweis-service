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

package de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter;

import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ADMIN_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_CMS_REDAKTEUR_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ENDNUTZER_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_ERSCHLIESSER_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_LOKALE_REDAKTEUR_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_NORMDATEN_REDAKTEUR_APP;
import static de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.Permissions.B_HSP_REDAKTEUR_APP;

import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.NormdatenReferenz;
import de.staatsbibliothek.berlin.javaee.authentication.OAUTH2;
import de.staatsbibliothek.berlin.javaee.authentication.entity.Authentication;
import de.staatsbibliothek.berlin.javaee.authentication.exception.AuthenticationException;
import de.staatsbibliothek.berlin.javaee.authentication.exception.IdentityProviderNotAvailableException;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import de.staatsbibliothek.berlin.javaee.authentication.service.AuthenticationService;
import java.io.Serializable;
import java.util.Objects;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
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
  private static final long serialVersionUID = 8524863140840965054L;
  private BearbeiterRepository bearbeiterRepository;

  private AuthenticationRepository authenticationRepository;
  private AuthenticationService authenticationService;

  BearbeiterService() {
  }

  @Inject
  public BearbeiterService(BearbeiterRepository bearbeiterRepository,
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

          bearbeiter = Bearbeiter.newBuilder()
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
    Authentication authentication = (Authentication) session.getAttribute(
        LOGGED_USER_AUTH_SESSION_KEY);
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
      FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
    } catch (AuthenticationException e) {
      log.error("Error during logout", e);
    } catch (IdentityProviderNotAvailableException e) {
      log.error("Identity provider is not available", e);
    }

  }

  @Override
  public void startLogin() {
    try {
      authenticationService.startLoginProcess();
    } catch (Exception e) {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      facesContext.addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler bei der Anmeldung",
              e.getMessage()));
    }
  }

  @Override
  public String getRole() {
    return getRole(getAuthentication());
  }

  protected Bearbeiter updateBearbeiterDaten(Bearbeiter bearbeiter, Authentication auth, String username) {
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

  public void setBearbeiterRepository(
      BearbeiterRepository bearbeiterRepository) {
    this.bearbeiterRepository = bearbeiterRepository;
  }

  public void setAuthenticationRepository(
      AuthenticationRepository authenticationRepository) {
    this.authenticationRepository = authenticationRepository;
  }

  public void setAuthenticationService(
      AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  String getRole(Authentication auth) {
    String role;
    if (auth == null) {
      role = "nicht angemeldeter Nutzer";
    } else {
      String roles = auth.getRole().toString();
      if (roles.contains(B_HSP_ADMIN_APP)) {
        role = "Admin";
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
