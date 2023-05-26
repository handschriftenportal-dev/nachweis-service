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

package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.eureka;

import com.netflix.appinfo.InstanceInfo;
import de.staatsbibliothek.berlin.javaee.authentication.cloud.CloudService;
import java.io.IOException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author konrad.eichstaedt@sbb.spk-berlin.de on 12.01.2022.
 * @version 1.0
 *
 * This object handles the replacement of a service discovery request url with hostname and port.
 */
public class RestClientRequestFilter implements ClientRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(RestClientRequestFilter.class);

  @Inject
  CloudService cloudService;

  @Override
  public void filter(ClientRequestContext clientRequestContext) throws IOException {

    if (clientRequestContext.getUri() != null) {

      logger.debug("Intercept the Rest Client Request {} ",
          clientRequestContext.getUri().toASCIIString());

      clientRequestContext.setUri(extractServiceURIWithEureka(clientRequestContext.getUri()));

      logger.debug("Adjust the Rest Client Request {} ",
          clientRequestContext.getUri().toASCIIString());
    }
  }

  public URI extractServiceURIWithEureka(URI uri) {

    final String serviceVIPName = extractVipServiceName(uri);
    final boolean isSecure = isSecure(uri);

    if (!serviceVIPName.isBlank()) {
      try {
        final InstanceInfo service = cloudService.getEurekaClient()
            .getNextServerFromEureka(serviceVIPName,
                false);

        return URI.create(uri.toASCIIString().replaceAll(extractVipServiceName(uri),
            service.getHostName() + ":" + (isSecure ? service.getSecurePort()
                : service.getPort())));
      } catch (Exception e) {
        logger.error("Error getting instanceInfo from eureka", e);
        return uri;
      }
    }

    return uri;
  }

  URI createNewURI(URI uri, String serviceVIPName,
      boolean isSecure, InstanceInfo importservice) {
    return URI.create(uri.toASCIIString().replaceAll(serviceVIPName,
        importservice.getHostName() + ":" + (isSecure ? importservice.getSecurePort()
            : importservice.getPort())));
  }

  boolean isSecure(URI uri) {
    return uri.toASCIIString().contains("https");
  }

  String extractVipServiceName(URI uri) {

    Pattern pattern = Pattern.compile("(://)([A-Z,\\-]*)(/)");

    final Matcher vipMatcher = pattern.matcher(uri.toASCIIString());
    if (vipMatcher.find()) {
      return vipMatcher.group(2);
    }

    return "";
  }
}
