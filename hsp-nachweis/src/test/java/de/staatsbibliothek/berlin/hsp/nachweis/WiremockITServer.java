package de.staatsbibliothek.berlin.hsp.nachweis;

/**
 * Created by Christoph Marten on 30.09.2020 at 10:41
 */


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.HashMap;
import java.util.Map;

public class WiremockITServer implements QuarkusTestResourceLifecycleManager {

  private WireMockServer wireMockServer;

  @Override
  public Map<String, String> start() {
    wireMockServer = new WireMockServer(options().dynamicPort());
    wireMockServer.start();
    WireMock.configureFor("localhost", wireMockServer.port());

    stubFor(get(urlEqualTo("/rest/import/job/"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
                "[{" +

                    "}]"
            )));

    stubFor(
        post(urlEqualTo("/rest/graphql"))
            .withRequestBody(containing("create"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json").withBody("""
                    {
                        "data": {
                            "create": {
                                "preferredName": "Domstiftsbibliothek St. Petri",
                                "gndIdentifier": "1027608000",
                                "typeName": "CorporateBody"
                            }
                        }
                    }
                    """)));

    stubFor(
        post(urlEqualTo("/rest/graphql"))
            .withRequestBody(containing("findCorporateBodyWithID"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json").withBody("""
                    {
                      "data": {
                        "findCorporateBodyWithID": {
                          "preferredName": "Domstiftsbibliothek St. Petri",
                          "gndIdentifier": "1027608000"
                        }
                      }
                    }""")));

    stubFor(post(urlEqualTo("/rest/graphql")).withRequestBody(
            containing("findPersonWithName"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json").withBody("""
                {
                  "data": {
                    "findPersonWithName": [{
                      "preferredName": "Robert",
                      "gndIdentifier": "123X"
                    }]
                  }
                }""")));

    stubFor(post(urlEqualTo("/rest/graphql")).withRequestBody(
            containing("findLanguageWithID"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json").withBody("""
                {
                  "data": {
                  "findLanguageWithID": {
                    "id": "6ecc000f-4433-30f0-b47d-e3e369ba743e",
                        "preferredName": "deutsch",
                        "variantName": [
                    {
                      "name": "Hochdeutsch",
                        "languageCode": "de"
                    },
                    {
                      "name": "Neuhochdeutsch",
                        "languageCode": "de"
                    },
                    {
                      "name": "Deutsche Sprache",
                        "languageCode": "de"
                    },
                   {
                      "name": "Deutsch",
                        "languageCode": "de"
                    }
                  ],
                    "identifier": [
                    {
                      "text": "de",
                        "url": "http://id.loc.gov/vocabulary/iso639-1/de",
                        "type": "ISO_639-1"
                    },
                    {
                      "text": "ger",
                        "url": "http://id.loc.gov/vocabulary/iso639-2/ger",
                        "type": "ISO_639-2"
                    },
                    {
                      "text": "deu",
                        "url": "http://id.loc.gov/vocabulary/iso639-2/deu",
                        "type": "ISO_639-2"
                    },
                    {
                      "text": "de",
                        "url": "",
                        "type": ""
                    }
                  ],
                    "gndIdentifier": "4113292-0"
                  }
                }
                }
                """)));

    stubFor(
        post(urlEqualTo("/rest/graphql"))
            .withRequestBody(containing("findSkosConceptsWithConceptScheme"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json").withBody("""
                    {
                       "data": {
                         "findSkosConceptsWithConceptScheme": [
                           {
                             "id": "NORM-cc73d86f-2707-3284-bd08-ba61cdacca42",
                             "notation": "reproduction",
                             "uri": "https://normdaten.staatsbibliothek-berlin.de/vocabulary/sourceType/reproduction",
                             "labels": [
                               {
                                 "isoCode": "de",
                                 "text": "Reproduktion"
                               },
                               {
                                 "isoCode": "en",
                                 "text": "reproduction"
                               }
                             ]
                           },
                           {
                             "id": "NORM-919c8b64-3b71-3311-ab02-fc0d9bb7df3f",
                             "notation": "original",
                             "uri": "https://normdaten.staatsbibliothek-berlin.de/vocabulary/sourceType/original",
                             "labels": [
                               {
                                 "isoCode": "de",
                                 "text": "Original"
                               },
                               {
                                 "isoCode": "en",
                                 "text": "Original"
                               }
                             ]
                           }
                         ]
                       }
                     }""")));

    stubFor(
        post(urlEqualTo("/rest/graphql"))
            .withRequestBody(
                containing("findSkosConceptSchemesWithinSchemeConceptScheme"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json").withBody("""
                    {
                      "data": {
                         "findSkosConceptSchemesWithinSchemeConceptScheme": [
                           {
                             "id": "NORM-c3de1202-9e0e-33ce-beec-4d64dcb09e97",
                             "notation": "digitizationType",
                             "uri": "https://normdaten.staatsbibliothek-berlin.de/vocabulary/digitizationType",
                             "labels": [
                               {
                                 "isoCode": "de",
                                 "text": "Art der Digitalisierung"
                               },
                               {
                                 "isoCode": "en",
                                 "text": "Digitization Type"
                               }
                             ]
                           },
                           {
                             "id": "NORM-5ab77627-345a-3347-b34d-a97a041920c9",
                             "notation": "sourceType",
                             "uri": "https://normdaten.staatsbibliothek-berlin.de/vocabulary/sourceType",
                             "labels": [
                               {
                                 "isoCode": "de",
                                 "text": "Art der Quelle"
                               },
                               {
                                 "isoCode": "en",
                                 "text": "Source Type"
                               }
                             ]
                           },
                           {
                             "id": "NORM-cb736d6d-2fdb-3671-8bdb-6c40ab412d3e",
                             "notation": "captureType",
                             "uri": "https://normdaten.staatsbibliothek-berlin.de/vocabulary/captureType",
                             "labels": [
                               {
                                 "isoCode": "de",
                                 "text": "Art der Erfassung"
                               },
                               {
                                 "isoCode": "en",
                                 "text": "Capture Type"
                               }
                             ]
                           },
                           {
                             "id": "NORM-fb54620f-8626-3cc1-b72c-f6753b98685e",
                             "notation": "completeness",
                             "uri": "https://normdaten.staatsbibliothek-berlin.de/vocabulary/completeness",
                             "labels": [
                               {
                                 "isoCode": "de",
                                 "text": "Vollständigkeit"
                               },
                               {
                                 "isoCode": "en",
                                 "text": "Completeness"
                               }
                             ]
                           }
                         ]
                       }
                    }""")));

    stubFor(post("/introspect").willReturn(aResponse()
        .withBody(
            "{\"active\":true,\"scope\":\"Echoer\",\"username\":null,\"iat\":1562315654,\"exp\":1562317454,\"expires_in\":1458,\"client_id\":\"my_client_id\"}")));

    WireMock.stubFor(get("/rest/import/job/123")
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody("""
                {
                  "id": "7637a746-282b-44a7-a85f-79d2d2f6885d",
                  "creationDate": "2024-08-26T16:18:48.022776",
                  "benutzerName": "b-pc101",
                  "importFiles": [
                    {
                      "id": "12a61e11-238f-4746-91b9-7de34175843d",
                      "path": "file:///tmp/hsp-import-/2024.08.26.16.18.48.020/02_Berlin_Kupferstichkabinett_HSK007_EXT-wthout-BOM-translated.xml",
                      "dateiTyp": "02_Berlin_Kupferstichkabinett_HSK007_EXT-wthout-BOM.xml",
                      "dateiName": "02_Berlin_Kupferstichkabinett_HSK007_EXT.xml",
                      "dateiFormat": "TEI_ALL",
                      "error": false,
                      "message": "",
                      "importEntityData": [
                        {
                          "id": "HSP-007539c4-44cb-394c-8bc1-3b5e4a37190d",
                          "label": "Cod. 78 A 9",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-b423da08-3072-3f1e-beeb-330720bbca09?q=%2A"
                        },
                        {
                          "id": "HSP-4600fc88-a9c4-3ca6-aa8a-f511c299e395",
                          "label": "Cod. 78 C 33",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-be612216-08f4-3e10-aa87-fa7db6248966?q=%2A"
                        },
                        {
                          "id": "HSP-1da4931c-357c-35e0-b71e-43f9b6cbbe5c",
                          "label": "Cod. 78 C 22",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-4a885c2c-7e8b-3e50-926c-5e48b8a01a8f?q=%2A"
                        },
                        {
                          "id": "HSP-7e8f1c2b-da30-34ef-a7ae-c09d8878bf38",
                          "label": "Cod. 77 B 19",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-80c51aa6-8574-3f50-87f4-3dafd63342ab?q=%2A"
                        },
                        {
                          "id": "HSP-77fffd79-6d6e-3fa1-8a33-64e624607e0d",
                          "label": "Cod. 78 C 21",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-9d642ba5-0e13-31ad-81d5-b102a9812f20?q=%2A"
                        },
                        {
                          "id": "HSP-e3915df4-7775-31bb-be04-bc4cddbb5a1c",
                          "label": "Cod. 77 B 16",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-350dad13-18af-3809-aac6-a4dacae5c2bb?q=%2A"
                        },
                        {
                          "id": "HSP-60806c78-22e9-34aa-bb19-4046ced7b9c0",
                          "label": "Cod. 78 D 7",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-306d86cd-a0be-3880-a28d-7890c3fdb468?q=%2A"
                        },
                        {
                          "id": "HSP-03b4b6f3-9450-31d7-8290-fb543a592c08",
                          "label": "Cod. 78 C 1",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-6e362d26-2968-33dc-88a8-b28b8cde6cce?q=%2A"
                        },
                        {
                          "id": "HSP-ecfe73f0-6607-3349-a7b2-5dd02539ccb3",
                          "label": "Cod. Botticelli",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-478d2d87-5c00-38b6-80de-0e094661a632?q=%2A"
                        },
                        {
                          "id": "HSP-8c29da6e-b926-3566-a261-dbee42289a43",
                          "label": "Cod. 78 C 16",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-c211d206-7646-36d5-b6ac-bcc7e8adc2b2?q=%2A"
                        },
                        {
                          "id": "HSP-2ac63f99-d55b-301e-9045-e529cf423997",
                          "label": "Cod. 78 B 21",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-4428c1ca-c920-3c30-89ef-34f6b86b04a4?q=%2A"
                        },
                        {
                          "id": "HSP-8262ef30-1c7b-3463-8cc5-0c949b5ce5b9",
                          "label": "Cod. 78 D 8",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-8f029975-8639-3ee0-9b2a-4c37d816dc70?q=%2A"
                        },
                        {
                          "id": "HSP-a5dc19dd-169c-3907-87ab-e5bb4e9db17b",
                          "label": "Cod. 78 B 22",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-18a74a4b-096e-3d8e-9999-946bc4ed4f6c?q=%2A"
                        },
                        {
                          "id": "HSP-b3f6250d-76f2-34ec-9dea-cf35b9634340",
                          "label": "Cod. 77 B 17",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-2d63f0a6-9ab3-31b8-95f4-005bbd082259?q=%2A"
                        },
                        {
                          "id": "HSP-3dc90324-14b5-3afc-a12d-18e94a825f9f",
                          "label": "Cod. 78 C 26",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-4f836a97-6b2b-3d2e-a313-98059ea63121?q=%2A"
                        },
                        {
                          "id": "HSP-dc7e9256-40f7-3a9d-966c-c3810b7c2526",
                          "label": "Cod. 78 D 11",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-575e9e1e-1972-3b46-afa3-db9a2a827bcf?q=%2A"
                        },
                        {
                          "id": "HSP-22fcf764-0471-374d-875f-a29f734bb51b",
                          "label": "Cod. 78 E 4",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-d1d3dc79-5c5b-3e70-8925-d8cb41a41354?q=%2A"
                        },
                        {
                          "id": "HSP-722992d9-9cae-355c-96dd-ed6e057f2fcd",
                          "label": "Cod. 78 D 13",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-da7e26b4-90c2-3543-812b-ac4874533f19?q=%2A"
                        },
                        {
                          "id": "HSP-8ec26b88-c095-3b5d-8e46-1c1bdbeb0208",
                          "label": "Cod. 78 C 11",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-34109950-03f0-3385-824e-4ff1a88f4e80?q=%2A"
                        },
                        {
                          "id": "HSP-5aa43d33-f2fe-3981-b0e0-cf86fa65eb3d",
                          "label": "Cod. 78 C 10",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-5edd46da-2b14-3641-aa9d-8c88955244b3?q=%2A"
                        },
                        {
                          "id": "HSP-fd81b167-476f-3898-b2b5-7b358804f5c4",
                          "label": "Cod. 77 A 1",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-75cf694b-1990-34e5-a435-352a86d1970f?q=%2A"
                        },
                        {
                          "id": "HSP-d1cd15ff-9984-36c0-a4c5-cea88b5e0a04",
                          "label": "Cod. 79 A 17",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-f29b4237-2325-367a-a456-c1837cb2ca82?q=%2A"
                        },
                        {
                          "id": "HSP-d89b85c3-f680-3dd1-9689-f5d1908edb7c",
                          "label": "Cod. 78 C 6",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-0ddd5ed8-b89f-3675-94bd-a9bf88df45f8?q=%2A"
                        },
                        {
                          "id": "HSP-81870647-9e4b-3bae-a805-493c0727b70b",
                          "label": "Cod. 77 B 18",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-249b83d6-d744-3adb-b14c-c3f284b884d5?q=%2A"
                        },
                        {
                          "id": "HSP-ebdfc23c-3ae3-32ef-9e40-942bae3c7d5a",
                          "label": "Cod. 78 B 14",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-9160cce5-235b-3418-8ec9-7505e319e39d?q=%2A"
                        },
                        {
                          "id": "HSP-ce2b50fb-0f87-3a4f-afd6-f696ced8c9f9",
                          "label": "Cod. 78 B 19",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-6e6d1ef4-2073-3b45-8001-1f6c5d0aa041?q=%2A"
                        },
                        {
                          "id": "HSP-c9444898-e752-33d0-a40e-966638076ee7",
                          "label": "Cod. 78 B 13",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-d3d3f234-dd8b-34b8-bb5e-98c372b93ddb?q=%2A"
                        },
                        {
                          "id": "HSP-24f98432-9ca5-397c-9759-9184a69853fb",
                          "label": "Cod. 78 C 32",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-41a6fb13-1577-381a-ba6d-3ae948e52b37?q=%2A"
                        },
                        {
                          "id": "HSP-81142500-5649-3d7a-b6a8-eedc8ba8eaba",
                          "label": "Cod. 78 A 10",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-9bd67a56-74a4-3d0d-9dff-e619bcd60069?q=%2A"
                        },
                        {
                          "id": "HSP-900101de-cf9c-343f-bf00-5f9c3c19ab42",
                          "label": "Cod. 78 B 20",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-d29699a4-446d-3c17-97cc-b509265f08c4?q=%2A"
                        },
                        {
                          "id": "HSP-ae5feaa7-cc47-3530-97f3-820cfebff574",
                          "label": "Cod. 78 C 23",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-d1ea36b3-1105-3e3e-a617-9ec0e746a1da?q=%2A"
                        },
                        {
                          "id": "HSP-90dbfb60-6f88-36ba-9dc0-00cc660094b6",
                          "label": "Cod. 78 D 14",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-e96470ba-851d-3c43-ab9d-3ee46dcc567f?q=%2A"
                        },
                        {
                          "id": "HSP-5db75345-a46d-35e6-833c-d1866a144a2d",
                          "label": "Cod. 78 A 7",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-6c975c91-98ef-3dc0-9290-dbb5392a3092?q=%2A"
                        },
                        {
                          "id": "HSP-2c773e63-9209-3e9f-a496-0eb1eff95e81",
                          "label": "Cod. 78 D 17",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-ddbc587c-e04f-3420-badf-883d0f68d188?q=%2A"
                        },
                        {
                          "id": "HSP-86e1a52b-08d3-34bb-a60e-115d5d990677",
                          "label": "Cod. 78 B 23",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-9bd79481-9174-31d7-a61b-13556d1d9050?q=%2A"
                        },
                        {
                          "id": "HSP-18955a80-01b9-3789-bb6e-e5b842ed6ce0",
                          "label": "Cod. 78 A 19b",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-8555ccfe-52a7-3c84-b069-c7e549b70d01?q=%2A"
                        },
                        {
                          "id": "HSP-42e8ebb4-0f61-30dd-a334-b36fbeb5086d",
                          "label": "Cod. 78 B 12",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-dbd5dc4a-b792-373d-93b6-92bf2fbd5438?q=%2A"
                        },
                        {
                          "id": "HSP-3392e94e-334a-37bb-af9b-744e612ed62a",
                          "label": "Cod. 78 C 24",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-43863e66-3590-30a3-95a0-a073f27c3f55?q=%2A"
                        },
                        {
                          "id": "HSP-0baef02b-e255-3ec6-97a4-658b4c8a695f",
                          "label": "Cod. 78 D 9",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-8dadd2b6-2199-33fd-9192-d34a4ddaa66b?q=%2A"
                        },
                        {
                          "id": "HSP-8e9d4779-e3c4-3455-9908-6b2677ea879c",
                          "label": "Cod. 78 C 27",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-ea4db677-2ca5-31ff-8fcd-e2079d8a695a?q=%2A"
                        },
                        {
                          "id": "HSP-f200577e-364d-38c9-9a60-289e5782aa2d",
                          "label": "Cod. 78 C 12",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-49d82f0f-8a37-3b5a-9708-09be2758f7e5?q=%2A"
                        },
                        {
                          "id": "HSP-768b095a-5bef-38ab-9557-a6548a440711",
                          "label": "Cod. 78 D 6",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-653536b9-f950-3cb7-a920-29680033aacb?q=%2A"
                        },
                        {
                          "id": "HSP-3df015ac-2860-3ddd-98bb-7f060c0aa17a",
                          "label": "Cod. 77 A 3",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-d4d4c29f-fb4c-33fa-96e0-943943345b9e?q=%2A"
                        },
                        {
                          "id": "HSP-44a5b21b-3540-3530-b894-c711ca50cc1b",
                          "label": "Cod. 78 D 12",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-fe4080d7-84c4-3d96-aee5-aac735054a2e?q=%2A"
                        },
                        {
                          "id": "HSP-abb33327-7a71-3a76-af79-d1d663269226",
                          "label": "Cod. 78 F 1",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-dc6d7a38-5341-397e-a683-764a74ddb40a?q=%2A"
                        },
                        {
                          "id": "HSP-c0e13dcf-ddc1-3f04-bb01-9eaacec4ae15",
                          "label": "Cod. 78 B 26",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-c4841155-1e26-3a93-8eeb-eac13b4e2f37?q=%2A"
                        },
                        {
                          "id": "HSP-a746f06c-9d87-3539-b38c-119ebc1d714e",
                          "label": "Cod. 77 A 10",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-8f3c406e-a3ec-3503-aa42-c9ae7e6913c7?q=%2A"
                        },
                        {
                          "id": "HSP-f88360ff-1518-30c5-ae45-7158fe911b62",
                          "label": "Cod. 78 C 2",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-943564be-f128-392e-ab3a-59aa893a5bd6?q=%2A"
                        },
                        {
                          "id": "HSP-f5c4b857-0ec9-305b-a0f9-5dc930acb3f4",
                          "label": "Cod. 78 B 18",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-a29da3e4-3d4e-3505-a839-a64e3785b8fa?q=%2A"
                        },
                        {
                          "id": "HSP-beffa1b9-8c39-3bfd-82e6-39eb53b31af2",
                          "label": "Cod. 78 C 5",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-d7c58bc4-8630-3a96-bb29-41ce898683b0?q=%2A"
                        },
                        {
                          "id": "HSP-16541e71-26d4-3d66-a4d3-6046310bac8f",
                          "label": "Cod. 78 C 7",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-286ca832-fdff-3e72-89cd-8e35c49b0854?q=%2A"
                        },
                        {
                          "id": "HSP-a6e6675a-30eb-38bf-a965-9e9fda740318",
                          "label": "Cod. 77 B 1",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-b409e784-99ea-391d-9572-9f98326eba9f?q=%2A"
                        },
                        {
                          "id": "HSP-7e4cd2ab-6672-3795-8bec-74de2fd9eecf",
                          "label": "Cod. 78 E 3",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-1ae465ea-71d5-3684-aeea-74d525fb05c2?q=%2A"
                        },
                        {
                          "id": "HSP-ee12a31b-cf5e-3bb1-be80-d8ac95269bf8",
                          "label": "Cod. 78 C 18",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-9850c8a9-23bd-3ae8-9922-c0210cd6db4b?q=%2A"
                        },
                        {
                          "id": "HSP-2746cd7d-7770-3477-b6dd-80c634a9eca4",
                          "label": "Cod. 78 D 5",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-22fc4006-ed3d-3e46-821e-8679e60fe51a?q=%2A"
                        },
                        {
                          "id": "HSP-821d46ac-872d-3ea7-8cc1-c2a14fea10f5",
                          "label": "Cod. 78 C 25",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-e599e606-80cd-3b2d-8474-f157f1b054b5?q=%2A"
                        },
                        {
                          "id": "HSP-229e51b1-abc8-3837-b3eb-76c8b768511b",
                          "label": "Cod. 77 A 2",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-bab1000a-56a0-3113-987a-06675ac20245?q=%2A"
                        },
                        {
                          "id": "HSP-d4b03436-6d4e-35ec-b6ea-4bb08a23dcef",
                          "label": "Cod. 78 C 3",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-c4a5d7f3-a802-32c7-9bee-421a6f09be33?q=%2A"
                        },
                        {
                          "id": "HSP-9df9efa1-df7c-3742-937f-75e7e502b826",
                          "label": "Cod. 78 C 28",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-3e0f1d32-2a86-3c81-928f-f73d5c318c53?q=%2A"
                        },
                        {
                          "id": "HSP-a1d54fd3-30fd-3fa4-a4f4-86154498bbae",
                          "label": "Cod. 78 C 8",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-522116f3-c545-35e0-b072-c7d24953a7d3?q=%2A"
                        },
                        {
                          "id": "HSP-a1c1a576-0772-3389-913a-81afa1ae54cc",
                          "label": "Cod. 78 A 5",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-794fe032-fd24-38d0-880c-29f42c21fe63?q=%2A"
                        },
                        {
                          "id": "HSP-7d96c367-4f9b-3604-9a24-6c0e62659fff",
                          "label": "Cod. 78 D 10",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-b28fd187-eae5-37f8-accf-69fb511d97b0?q=%2A"
                        },
                        {
                          "id": "HSP-38175cd2-c339-32c0-8fcf-8a9512a97aa5",
                          "label": "Cod. 78 C 9",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-dd2dc53e-e925-39b8-ba26-847112e94032?q=%2A"
                        },
                        {
                          "id": "HSP-c773b72d-c5a7-33e0-9129-9a5bd5b96707",
                          "label": "Cod. 78 B 17",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-4195991e-95a0-3826-a702-1c510a6b25d1?q=%2A"
                        },
                        {
                          "id": "HSP-ab87de02-a0a4-3f25-9724-16549f544a3e",
                          "label": "Cod. 78 C 31",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-6d168421-314a-3f79-b6a1-c1f2a2cba6d6?q=%2A"
                        },
                        {
                          "id": "HSP-962d78c3-24b2-330d-85df-9f71f1a3c4eb",
                          "label": "Cod. 78 D 19",
                          "url": "http://b-dev1047.pk.de:8080/search?hspobjectid=HSP-6f4a9be0-a273-328f-b808-f40e10082db0?q=%2A"
                        }
                      ]
                    }
                  ],
                  "name": "02_Berlin_Kupferstichkabinett_HSK007_EXT.xml",
                  "importDir": "/tmp/hsp-import-/2024.08.26.16.18.48.020",
                  "result": "SUCCESS",
                  "errorMessage": "",
                  "datatype": "BESCHREIBUNG"
                }
                """)));

    stubFor(
        post(urlEqualTo("/rest/graphql"))
            .withRequestBody(containing("normdatenerkennung"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json").withBody("""
                    {
                      "data": {
                        "normdatenerkennung": {
                          "error": "",
                          "id": "95a1f9c9-c987-4c70-9a10-29d40cacef52",
                          "treffer": [
                            {
                              "id": "NORM-1b81c52c-e395-3a3c-a054-9b667739cf4b",
                              "gndid": "100958427",
                              "name": "Philipp, Eichstätt, Bischof",
                              "quelle": "GRAPH",
                              "otherIDs": [
                                "(DE-588a)100958427",
                                "30006618",
                                "100958427",
                                "NORM-1b81c52c-e395-3a3c-a054-9b667739cf4b"
                              ]
                            },
                            {
                              "id": "NORM-25b32b19-2665-3e25-bad5-75ed40ae3423",
                              "gndid": "130302414",
                              "name": "Heribert, Eichstätt, Bischof",
                              "quelle": "GRAPH",
                              "otherIDs": [
                                "(DE-588a)130302414",
                                "30009284",
                                "102421242",
                                "130302414",
                                "NORM-25b32b19-2665-3e25-bad5-75ed40ae3423"
                              ]
                            },
                            {
                              "id": "NORM-9216e737-5925-3944-bc05-e3c9818855b2",
                              "gndid": "118895656",
                              "name": "Gundecharus <Eichstetensis>",
                              "quelle": "GRAPH",
                              "otherIDs": [
                                "104099240",
                                "30014386",
                                "NORM-9216e737-5925-3944-bc05-e3c9818855b2"
                              ]
                            },
                            {
                              "id": "NORM-e45ee7ce-7e88-349a-b8dd-32b27f9512ce",
                              "gndid": "1065612508",
                              "name": "Eichstädt, Konrad",
                              "quelle": "GRAPH",
                              "otherIDs": [
                                "03",
                                "1065612508",
                                "NORM-e45ee7ce-7e88-349a-b8dd-32b27f9512ce"
                              ]
                            }
                          ],
                          "empfehlung": {
                            "aehnlichkeit": 0.8666667,
                            "treffer": {
                              "id": "NORM-e45ee7ce-7e88-349a-b8dd-32b27f9512ce",
                              "gndid": "1065612508",
                              "name": "Eichstädt, Konrad",
                              "quelle": "GRAPH"
                            }
                          }
                        }
                      }
                    }""")));

    WireMock.stubFor(delete("/rest/import/job")
        .withRequestBody(containing("123"))
        .willReturn(aResponse().withStatus(200)));

    Map<String, String> restMapping = new HashMap<>();
    restMapping.put(
        "hsp.services.importservice.base",
        wireMockServer.baseUrl());

    restMapping.put(
        "hsp.services.normdaten.graphql",
        wireMockServer.baseUrl() + "/rest/graphql");

    restMapping.put("quarkus.oauth2.introspection-url",
        wireMockServer.baseUrl() + "/introspect");

    return restMapping;
  }

  @Override
  public void stop() {
    if (null != wireMockServer) {
      wireMockServer.stop();
    }
  }
}
