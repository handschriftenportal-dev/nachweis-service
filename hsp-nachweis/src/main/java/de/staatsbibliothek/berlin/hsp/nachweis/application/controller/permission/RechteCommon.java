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

package de.staatsbibliothek.berlin.hsp.nachweis.application.controller.permission;

import de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth.OAUTH2Config;
import de.staatsbibliothek.berlin.javaee.authentication.entity.Authentication;
import de.staatsbibliothek.berlin.javaee.authentication.infrastructure.AuthenticationRepository;
import java.io.Serializable;
import org.slf4j.Logger;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 03.06.21
 */
public class RechteCommon implements Serializable {

  public static final String DEFAULT_ACTION = "#";
  public static final String STYLE_ALLOWED = "pointer-events:all;";
  public static final String STYLE_DISALLOWED = "pointer-events:none;opacity:0.6;";
  private transient AuthenticationRepository authenticationRepository;
  private transient OAUTH2Config oauth2Config;

  transient Logger logger;

  RechteCommon() {
  }

  public RechteCommon(AuthenticationRepository authenticationRepository, OAUTH2Config oauth2Config,
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
    if (authentication == null || requiredPermissions == null || requiredPermissions.length == 0) {
      logger.debug("authentication = {} for required permissions = {} not set proper", authentication,
          requiredPermissions);
      return false;
    }

    return authentication.isAlreadyLoggedIn() && checkRequiredPermission(authentication, requiredPermissions);
  }

  private boolean checkRequiredPermission(Authentication authentication, String[] requiredPermissions) {
    boolean result = false;
    for (String permission : requiredPermissions) {
      result |= authentication.getPermission().contains(permission);
    }
    return result;
  }
}
