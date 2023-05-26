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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BearbeiterRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.entity.Authentication;
import de.staatsbibliothek.berlin.javaee.authentication.interceptor.LoginCheck;
import java.io.IOException;
import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("loginController")
@ViewScoped
@LoginCheck
public class LoginController implements Serializable {

  public static final String USER_B_DFG = "b-dfg";
  private static final long serialVersionUID = -2811987782584052650L;
  private static Logger logger = LoggerFactory.getLogger(LoginController.class);
  BearbeiterBoundary bearbeiterBoundary;

  OAUTH2Config oauth2Config;

  private String title;
  private String name;
  private String role;
  private Boolean loggedIn;
  private BearbeiterRechte bearbeiterRechte;

  @Inject
  public LoginController(BearbeiterBoundary bearbeiterBoundary,
      OAUTH2Config oauth2Config,
      BearbeiterRechte bearbeiterRechte) {
    this.bearbeiterBoundary = bearbeiterBoundary;
    this.oauth2Config = oauth2Config;
    this.bearbeiterRechte = bearbeiterRechte;
  }

  public void logout() {
    bearbeiterBoundary.logout();
  }

  public String getTitle() {
    logger.info("Creating login controller");
    Authentication auth = bearbeiterBoundary.getAuthentication();
    if (auth == null) {
      title = "Hello Gast You Have permissions: none";
    } else {
      title =
          "Hello " + auth.getUsername() + " You Have permissions: " + auth.getPermission()
              .toString();
    }
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getName() {
    Authentication auth = bearbeiterBoundary.getAuthentication();
    if (auth == null) {
      name = "Gast";
    } else {
      name = auth.getFirstname() + " " + auth.getLastname();
    }
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRole() {
    role = bearbeiterBoundary.getRole();
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public Boolean getLoggedIn() {
    Authentication auth = bearbeiterBoundary.getAuthentication();
    if (auth == null) {
      loggedIn = false;
    } else {
      loggedIn = auth.isAlreadyLoggedIn();
    }
    return loggedIn;
  }

  public void checkLogin() throws IOException {

    if (oauth2Config.isLoginCheckEnabled()) {

      String requestServletPath = FacesContext.getCurrentInstance().getExternalContext()
          .getRequestServletPath();
      logger.info("Check Login on {}", requestServletPath);

      FacesContext facesContext = FacesContext.getCurrentInstance();

      Authentication auth = bearbeiterBoundary.getAuthentication();

      if (!"/dashboard.xhtml".equals(requestServletPath)) {
        if (auth == null || !auth.isAlreadyLoggedIn()) {
          Flash flash = facesContext.getExternalContext().getFlash();
          flash.setKeepMessages(true);
          facesContext.addMessage(null,
              new FacesMessage(FacesMessage.SEVERITY_WARN, "Bitte melden Sie sich an!",
                  "Keine Anmeldung gefunden!"));
          facesContext.getExternalContext()
              .redirect("/dashboard.xhtml?faces-redirect=true");
          return;
        }
      }
      if (!"/bearbeiter.xhtml".equals(requestServletPath)
          && bearbeiterRechte.kannAnzeigen()
          && bearbeiterBoundary.isBearbeiterNew()
          && !facesContext.getResponseComplete()) {
        facesContext.getExternalContext().redirect("/bearbeiter.xhtml?faces-redirect=true");
      }
    }
  }

  public void startLogin() {
    bearbeiterBoundary.startLogin();
  }

  public String getProfileImageCssClass() {

    Authentication auth = bearbeiterBoundary.getAuthentication();
    if (auth != null && auth.getUsername() != null && auth.getUsername().startsWith(USER_B_DFG)) {
      return "profileDFG-image";
    }
    return "profile-image";
  }
}
