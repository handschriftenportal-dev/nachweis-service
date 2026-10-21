package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission.BearbeiterRechte;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterBoundary;
import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.entity.Authentication;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("loginController")
@ViewScoped
public class LoginController implements Serializable {

  public static final String USER_B_DFG = "b-dfg";
  @Serial
  private static final long serialVersionUID = -2811987782584052650L;
  private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
  BearbeiterBoundary bearbeiterBoundary;

  OAUTH2Config oauth2Config;

  @Setter
  private String title;
  @Setter
  private String name;
  @Setter
  private String role;
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

  public String getName() {
    Authentication auth = bearbeiterBoundary.getAuthentication();
    if (auth == null) {
      name = "Gast";
    } else {
      name = auth.getFirstname() + " " + auth.getLastname();
    }
    return name;
  }

  public String getRole() {
    role = bearbeiterBoundary.getRole();
    return role;
  }

  public boolean getLoggedIn() {
    var auth = bearbeiterBoundary.getAuthentication();
    if (auth != null) {
      logger.debug(
        "Check if {} is logged in: {}",
        auth.getUsername(),
        auth.isAlreadyLoggedIn()
      );
      logger.debug("Auth details: {}", auth);
    }
    return auth != null && auth.isAlreadyLoggedIn();
  }

  public void checkLogin() throws IOException {

    if (oauth2Config.isLoginCheckEnabled()) {

      String requestServletPath = FacesContext.getCurrentInstance().getExternalContext()
          .getRequestServletPath();
      logger.debug("Check Login on {}", requestServletPath);

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
