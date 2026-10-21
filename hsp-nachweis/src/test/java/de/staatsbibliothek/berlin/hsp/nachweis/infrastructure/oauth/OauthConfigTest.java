package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.undertow.servlet.spec.HttpServletRequestImpl;
import io.undertow.servlet.spec.ServletContextImpl;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class OauthConfigTest implements Serializable {

  public static final String CLIENT_ID = "clientId";
  public static final String CLIENT_SECRET = "clientSecret";
  private ServletContextImpl servletContext;
  private HttpServletRequestImpl httpServletRequest;
  private OAUTH2Config oauth2Config;


  public OauthConfigTest() {
    this.servletContext = mock(ServletContextImpl.class);
    this.httpServletRequest = mock(HttpServletRequestImpl.class);
    this.oauth2Config = new OAUTH2Config(servletContext, httpServletRequest, false,"IDENTITYMANAGEMENT","", "OAUTH", false, false, null, CLIENT_ID,
        CLIENT_SECRET, "/dashboard.xhtml", "/dashboard.xhtml", "/error_authentication.xhtml");
  }

  @Test
  void getClientID() {
    assertEquals(CLIENT_ID, oauth2Config.getClient_id());
  }

  @Test
  void getClientSecret() {
    assertEquals(CLIENT_SECRET, oauth2Config.getClient_secret());
  }

  @Test
  void getErrorUrl() {
    when(httpServletRequest.getScheme()).thenReturn("http");
    when(httpServletRequest.getServerName()).thenReturn("localhost");
    when(httpServletRequest.getServerPort()).thenReturn(80);
    when(servletContext.getContextPath()).thenReturn("");

    assertEquals(
        URLEncoder.encode("http://localhost/error_authentication.xhtml", StandardCharsets.UTF_8),
        oauth2Config.getErrorUrl()
    );
  }
}
