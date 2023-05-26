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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth;

import de.staatsbibliothek.berlin.javaee.authentication.adapter.OAUTH2Configuration;
import de.staatsbibliothek.berlin.javaee.authentication.service.OAuth2Http;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.ReentrantLock;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class OAUTH2Config implements OAUTH2Configuration, Serializable {

  private static final ReentrantLock LOCK = new ReentrantLock();

  private static final long serialVersionUID = -6171495359220450620L;

  private final String clientId;

  private final String clientSecret;

  private final String grantCodeRedirectUri;

  private final String revokeTokenRedirectUri;

  private final String errorUrl;

  private static Logger logger = LoggerFactory.getLogger(OAUTH2Config.class);

  private static String urlPrefix;

  private ServletContext context;

  private HttpServletRequest request;

  private boolean oauth2FilterEnabled;

  private boolean oauth2LoginCheckEnabled;

  private boolean oauth2PermissionCheckEnabled;

  @Inject
  public OAUTH2Config(ServletContext context, HttpServletRequest request,
      @ConfigProperty(name = "oauth2.filter.enabled") boolean oauth2FilterEnabled,
      @ConfigProperty(name = "oauth2.logincheck.enabled") boolean oauth2LoginCheckEnabled,
      @ConfigProperty(name = "oauth2.permissioncheck.enabled") boolean oauth2PermissionCheckEnabled,
      @ConfigProperty(name = "oauth2.own.url.prefix") String configuredUrlPrefix,
      @ConfigProperty(name = "oauth2.client.id") String clientId,
      @ConfigProperty(name = "oauth2.client.secret") String clientSecret,
      @ConfigProperty(name = "oauth2.redirect-uri.grant-code") String grantCodeRedirectUri,
      @ConfigProperty(name = "oauth2.redirect-uri.revoke-token") String revokeTokenRedirectUri,
      @ConfigProperty(name = "oauth2.redirect-uri.error") String errorUrl) {
    this.context = context;
    this.request = request;
    this.oauth2FilterEnabled = oauth2FilterEnabled;
    this.oauth2LoginCheckEnabled = oauth2LoginCheckEnabled;
    this.oauth2PermissionCheckEnabled = oauth2PermissionCheckEnabled;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.grantCodeRedirectUri = grantCodeRedirectUri;
    this.revokeTokenRedirectUri = revokeTokenRedirectUri;
    this.errorUrl = errorUrl;

    if (configuredUrlPrefix != null && configuredUrlPrefix.startsWith("http")) {
      urlPrefix = removeEndingForwardSlash(configuredUrlPrefix);
      ;
    }
  }

  @Override
  public String getClient_id() {
    return clientId;
  }

  @Override
  public String getClient_secret() {
    return clientSecret;
  }

  @Override
  public String getGrant_code_redirect_uri() {
    return getEncodedURL(grantCodeRedirectUri);
  }

  @Override
  public String getRevokeToken_redirect_uri() {
    return getEncodedURL(revokeTokenRedirectUri);
  }

  @Override
  public OAuth2Http.GRANT.type getGrantType() {
    return OAuth2Http.GRANT.type.CODE;
  }

  @Override
  public String getErrorUrl() {
    return getEncodedURL(errorUrl);
  }

  @Override
  public boolean isOAUTHFilterEnabled() {
    return oauth2FilterEnabled;
  }

  @Override
  public boolean isLoginCheckEnabled() {
    return oauth2LoginCheckEnabled;
  }

  @Override
  public boolean isPermissionCheckEnabled() {
    return oauth2PermissionCheckEnabled;
  }

  private String getServerNameAndPort() {
    if (urlPrefix == null) {
      try {
        LOCK.lock();
        if (urlPrefix == null) {
          String requestScheme = request.getScheme();
          StringBuilder sb = new StringBuilder(requestScheme);
          sb.append("://");
          sb.append(request.getServerName());
          int serverPort = request.getServerPort();
          if (("http".equals(requestScheme) && serverPort != 80)
              || ("https".equals(requestScheme) && serverPort != 443)) {
            sb.append(':').append(serverPort);
          }
          sb.append(context.getContextPath());

          urlPrefix = sb.toString();
          logger.info("Generated URL: {}", urlPrefix);
        }
      } finally {
        LOCK.unlock();
      }
    }
    urlPrefix = removeEndingForwardSlash(urlPrefix);
    return urlPrefix;
  }

  private String getEncodedURL(String suffix) {
    String url = new StringBuilder().append(getServerNameAndPort()).append(suffix).toString();
    return URLEncoder.encode(url, StandardCharsets.UTF_8);
  }

  static String removeEndingForwardSlash(String urlPrefix) {
    int end = urlPrefix.length() - 1;
    if (urlPrefix.charAt(end) == '/') {
      urlPrefix = urlPrefix.substring(0, end);
    }
    return urlPrefix;
  }
}
