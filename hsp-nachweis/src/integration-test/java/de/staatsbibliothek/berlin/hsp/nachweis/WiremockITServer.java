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

package de.staatsbibliothek.berlin.hsp.nachweis;

/**
 * Created by Christoph Marten on 30.09.2020 at 10:41
 */


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.HashMap;
import java.util.Map;

public class WiremockITServer implements QuarkusTestResourceLifecycleManager {

  private WireMockServer wireMockServer;

  @Override
  public Map<String, String> start() {
    wireMockServer = new WireMockServer();
    wireMockServer.start();

    stubFor(get(urlEqualTo("/import/job/"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
                "[{" +

                    "}]"
            )));

    stubFor(
        post(urlEqualTo("/rest/graphql"))
            .withRequestBody(containing("findCorporateBodyWithID")).willReturn(aResponse()
            .withHeader("Content-Type", "application/json").withBody("{\n"
                + "  \"data\": {\n"
                + "    \"findCorporateBodyWithID\": {\n"
                + "      \"preferredName\": \"Domstiftsbibliothek St. Petri\",\n"
                + "      \"gndIdentifier\": \"1027608000\"\n"
                + "    }\n"
                + "  }\n"
                + "}")));

    stubFor(post(urlEqualTo("/rest/graphql")).withRequestBody(containing("findPersonWithName"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json").withBody("{\n"
                + "  \"data\": {\n"
                + "    \"findPersonWithName\": [{\n"
                + "      \"preferredName\": \"Robert\",\n"
                + "      \"gndIdentifier\": \"123X\"\n"
                + "    }]\n"
                + "  }\n"
                + "}")));

    stubFor(post(urlEqualTo("/rest/graphql")).withRequestBody(containing("findLanguageWithID"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json").withBody("{\n"
                + "  \"data\": {\n"
                + "  \"findLanguageWithID\": {\n"
                + "    \"id\": \"6ecc000f-4433-30f0-b47d-e3e369ba743e\",\n"
                + "        \"preferredName\": \"deutsch\",\n"
                + "        \"variantName\": [\n"
                + "    {\n"
                + "      \"name\": \"Hochdeutsch\",\n"
                + "        \"languageCode\": \"de\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"name\": \"Neuhochdeutsch\",\n"
                + "        \"languageCode\": \"de\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"name\": \"Deutsche Sprache\",\n"
                + "        \"languageCode\": \"de\"\n"
                + "    },\n"
                + "   {\n"
                + "      \"name\": \"Deutsch\",\n"
                + "        \"languageCode\": \"de\"\n"
                + "    }\n"
                + "  ],\n"
                + "    \"identifier\": [\n"
                + "    {\n"
                + "      \"text\": \"de\",\n"
                + "        \"url\": \"http://id.loc.gov/vocabulary/iso639-1/de\",\n"
                + "        \"type\": \"ISO_639-1\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"text\": \"ger\",\n"
                + "        \"url\": \"http://id.loc.gov/vocabulary/iso639-2/ger\",\n"
                + "        \"type\": \"ISO_639-2\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"text\": \"deu\",\n"
                + "        \"url\": \"http://id.loc.gov/vocabulary/iso639-2/deu\",\n"
                + "        \"type\": \"ISO_639-2\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"text\": \"de\",\n"
                + "        \"url\": \"\",\n"
                + "        \"type\": \"\"\n"
                + "    }\n"
                + "  ],\n"
                + "    \"gndIdentifier\": \"4113292-0\"\n"
                + "  }\n"
                + "}\n"
                + "}\n")));

    Map<String, String> restMapping = new HashMap<>();
    restMapping.put(
        "de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.importservice.ImportServicePort/mp-rest/url",
        wireMockServer.baseUrl());

    restMapping.put(
        "de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.normdaten.NormdatenGraphQLPort/mp-rest/url",
        wireMockServer.baseUrl() + "/rest/graphql");

    return restMapping;
  }

  @Override
  public void stop() {
    if (null != wireMockServer) {
      wireMockServer.stop();
    }
  }
}
