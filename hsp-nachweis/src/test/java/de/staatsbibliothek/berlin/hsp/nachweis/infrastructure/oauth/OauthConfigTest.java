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
    this.oauth2Config = new OAUTH2Config(servletContext, httpServletRequest, false, false, false, null, CLIENT_ID,
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
