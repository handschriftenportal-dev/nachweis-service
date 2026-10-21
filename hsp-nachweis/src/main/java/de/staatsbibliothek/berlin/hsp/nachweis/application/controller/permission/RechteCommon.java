package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.entity.Authentication;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.slf4j.Logger;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 03.06.21
 */
public class RechteCommon implements Serializable {

  public static final String DEFAULT_ACTION = "#";
  public static final String STYLE_ALLOWED = "pointer-events:all;";
  public static final String STYLE_DISALLOWED = "pointer-events:none;opacity:0.6;";
  @Serial
  private static final long serialVersionUID = 196451780797590535L;
  transient Logger logger;
  private transient AuthenticationRepository authenticationRepository;
  private transient OAUTH2Config oauth2Config;

  protected RechteCommon() {
  }

  public RechteCommon(AuthenticationRepository authenticationRepository,
      OAUTH2Config oauth2Config,
      Logger logger) {
    this.authenticationRepository = authenticationRepository;
    this.oauth2Config = oauth2Config;
    this.logger = logger;
  }

  private Authentication getAuthentication() {
    return authenticationRepository.findAuthentication();
  }

  public boolean hasAccessWithPermission(String... requiredPermission) {
    if (!oauth2Config.isPermissionCheckEnabled()) {
      return true;
    }

    return hasActionAccess(requiredPermission);
  }

  public String getAllowedAction(String action, String... permission) {
    return getAllowedAction(action, hasAccessWithPermission(permission));
  }

  public String getAllowedAction(String action, boolean allowed) {
    if (allowed) {
      return action;
    }
    return DEFAULT_ACTION;
  }

  public String getAllowedStyle(String... permission) {
    return getAllowedStyle(hasAccessWithPermission(permission));
  }

  public String getAllowedStyle(boolean allowed) {
    if (allowed) {
      return STYLE_ALLOWED;
    }
    return STYLE_DISALLOWED;
  }

  private boolean hasActionAccess(String... requiredPermissions) {
    Authentication authentication = getAuthentication();
    if (authentication == null || requiredPermissions == null
        || requiredPermissions.length == 0) {
      logger.debug(
          "authentication = {} for required permissions = {} not set proper",
          authentication,
          requiredPermissions);
      return false;
    }

    return authentication.isAlreadyLoggedIn() && checkRequiredPermission(
        authentication,
        requiredPermissions);
  }

  private boolean checkRequiredPermission(Authentication authentication,
      String[] requiredPermissions) {
    boolean result = false;
    if (logger.isDebugEnabled()) {
      logger.debug("Check Permission required {} authentication {} ",
          Arrays.stream(requiredPermissions).map(String::valueOf).collect(
              Collectors.joining(",")),
          authentication.getPermission().stream()
              .collect(Collectors.joining(",")));
    }
    for (String permission : requiredPermissions) {
      result |= authentication.getPermission().contains(permission);
    }
    return result;
  }
}
