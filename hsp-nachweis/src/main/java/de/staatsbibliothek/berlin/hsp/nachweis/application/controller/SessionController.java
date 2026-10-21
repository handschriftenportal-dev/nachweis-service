package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpSession;
import java.io.Serial;
import java.io.Serializable;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 09.09.20
 */
@Named
@SessionScoped
public class SessionController implements Serializable {

  @Serial
  private static final long serialVersionUID = 2883393570827924458L;

  private final BearbeiterBoundary bearbeiterBoundary;
  // Calling session.getLastAccessTime() will renew the session. Hence we need to track the
  // access time in a separate variable
  private long initialAccessTime = System.currentTimeMillis();

  @Inject
  public SessionController(BearbeiterBoundary bearbeiterBoundary) {
    this.bearbeiterBoundary = bearbeiterBoundary;
  }

  private boolean isLoggedIn() {
    var auth = bearbeiterBoundary.getAuthentication();
    return auth != null && auth.isAlreadyLoggedIn();
  }

  public boolean isExpired() {
    var auth = bearbeiterBoundary.getAuthentication();
    return isLoggedIn() && (
      !auth.isStillValid() || this.remainingSessionSeconds() <= 0
    );
  }

  public void renewSession() {
    if (!isLoggedIn()) {
      return;
    }
    HttpSession session = (HttpSession) FacesContext
        .getCurrentInstance()
        .getExternalContext()
        .getSession(false);
    initialAccessTime = session.getLastAccessedTime();
  }

  public void invalidateSession() {
    bearbeiterBoundary.logout();
  }

  private int remainingSessionSeconds() {
    if (!isLoggedIn()) {
      return 0;
    }
    HttpSession session = (HttpSession) FacesContext
        .getCurrentInstance()
        .getExternalContext()
        .getSession(false);
    return session.getMaxInactiveInterval()
        - (int) (System.currentTimeMillis() - initialAccessTime) / 1000;
  }
}
