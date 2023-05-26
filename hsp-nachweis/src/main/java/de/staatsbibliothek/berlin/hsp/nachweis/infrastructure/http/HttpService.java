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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.http;

import static java.net.HttpURLConnection.HTTP_OK;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 20.07.22
 */

@ApplicationScoped
@Slf4j
public class HttpService implements HttpBoundary, Serializable {

  private static final long serialVersionUID = -4536754074828147054L;

  private static TrustManager[] trustAllCerts = new TrustManager[]{
      new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return null;
        }

        public void checkClientTrusted(
            java.security.cert.X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(
            java.security.cert.X509Certificate[] certs, String authType) {
        }
      }
  };

  private final String proxyHost;
  private final Integer proxyPort;

  public HttpService(
      @ConfigProperty(name = "proxy.http.host") String proxyHost,
      @ConfigProperty(name = "proxy.http.port") Integer proxyPort) {
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
  }

  @Override
  public Optional<String> loadBodyFromURL(URI targetURL)
      throws IOException, InterruptedException, NoSuchAlgorithmException, KeyManagementException {

    HttpClient client = getHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
        .timeout(Duration.ofSeconds(8L))
        .uri(targetURL)
        .build();

    HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
    log.debug("Response Code: {}", response.statusCode());
    if (response.statusCode() == HTTP_OK) {
      return Optional.ofNullable(response.body());
    }
    return Optional.empty();
  }

  private HttpClient getHttpClient() throws NoSuchAlgorithmException, KeyManagementException {
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, trustAllCerts, new SecureRandom());

    Builder builder = HttpClient.newBuilder()
        .version(Version.HTTP_1_1)
        .followRedirects(Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(8L));
    if (proxyPort != null && proxyHost != null) {
      builder.sslContext(sslContext).proxy(ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort)));
    }
    HttpClient client = builder.build();
    return client;
  }
}
