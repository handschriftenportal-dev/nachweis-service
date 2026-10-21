package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static com.github.dreamhead.moco.Moco.by;
import static com.github.dreamhead.moco.Moco.header;
import static com.github.dreamhead.moco.Moco.httpServer;
import static com.github.dreamhead.moco.Moco.json;
import static com.github.dreamhead.moco.Moco.log;
import static com.github.dreamhead.moco.Moco.uri;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import com.github.dreamhead.moco.HttpServer;
import com.github.dreamhead.moco.Runner;
import de.staatsbibliothek.berlin.hsp.domainmodel.entities.Bearbeiter;
import de.staatsbibliothek.berlin.hsp.nachweis.domain.bearbeiter.BearbeiterRepository;
import de.staatsbibliothek.berlin.javaee.authentication.exception.OAUTH2Exception;
import de.staatsbibliothek.berlin.javaee.authentication.repository.jwt.OAuth2JWTParser;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@QuarkusTest
@Execution(ExecutionMode.SAME_THREAD)
class BearbeiterRestControllerTest {

  private static final String BEARER_TOKEN = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJmaXJzdG5hbWUiOiJLb25yYWQiLCJ1c2VyX25hbWUiOiJiLWtlMTAxIiwicm9sZXMiOlsiQi1IU1BSRUQtQVBQIl0sImlzcyI6Imh0dHBzOi8vaW1kLnN0YWF0c2JpYmxpb3RoZWstYmVybGluLmRlIiwiYXV0aG9yaXRpZXMiOlsiUk9MRV9CLUhTUFJFRC1BUFAiLCJST0xFX1JlZGFrdGV1ciIsIlJPTEVfQi1FREFETS1BUFAiXSwiY2xpZW50X2lkIjoiaHNwIiwibGFzdG5hbWUiOiJFaWNoc3RhZWR0IiwibmJmIjoxNzM0NDE0NTQzMDIyLCJwZXJtaXNzaW9ucyI6WyJCLUhTUEVSU0NIQ01OLUFQUCIsIkItSFNQTFJFRENNTi1BUFAiLCJCLUhTUFJFRC1BUFAiLCJCLUxSRURDTU4tQVBQIl0sInNjb3BlIjpbInN0YWF0c2JpYmxpb3RoZWtCZXJsaW4iLCJoYW5kc2NocmlmdGVucG9ydGFsIl0sImV4cCI6MTczNDQ1Nzc0MywianRpIjoiZ3VSc2Zld2hfZG1hN3FFVERxYlpkams4d2lVIiwiZW1haWwiOiJLb25yYWQuRWljaHN0YWVkdEBzYmIuc3BrLWJlcmxpbi5kZSJ9.pI95nObspafDwDB5B8M6CQkhRsqhQURfgWYerA4o0CmF9rzlQ_iwybHNZ7J8lWlfHeTTxRyH6SpKAdwNiF95grZwLEu5y7O-9hLm2-3wPoCSuoTMnykYo8nBnfXgdQSTZ0-iUyQzvSYKC6_T6VxlXlY2I4A3Kr41aR2dUuw_Ji8-jzu3_24IblqCOqW2fDyXcGkyBaxbwuaT9QI_zCjsgWbJzcbvYKOsC_Un_O3WoDJgVE1ruoNCnwCk6EjKJe9jVMAdApPfYqeF5Q43zXjy4OVH1uSZgTxeRUprHT-AJWMeJPkFTp3Lcd215HwbzYYGzan7uxa7wLKnP7cnE5ltcg";
  private static Runner RUNNER;
  @Inject
  BearbeiterRepository bearbeiterRepository;

  @Inject
  OAuth2JWTParser jwtParser;

  @BeforeAll
  static void start() {
    // Use another port than identitymanagmenet for local tests
    HttpServer server = httpServer(9584, log());
    server.request(by(uri("/oauth/check_token")))
        .response(json(
                "{\"active\":true,\"scope\":\"Echoer\",\"username\":\"b-ke101\",\"iat\":1562315654,\"exp\":1562317454,\"expires_in\":1458,\"client_id\":\"my_client_id\"}"),
            header("content-type", "application/json"));

    RUNNER = Runner.runner(server);
    RUNNER.start();
  }

  @AfterAll
  static void down() {
    RUNNER.stop();
  }

  @BeforeEach
  @Transactional
  void setUp() {
    Bearbeiter bearbeiter = Bearbeiter.newBuilder()
        .withBearbeitername("b-ke101")
        .withNachname("Nachname")
        .withVorname("Vorname")
        .withEditorConfig("""
            { "bookmarks": [1, 5, 7] }""")
        .build();
    bearbeiterRepository.saveAndFlush(bearbeiter);
  }

  @AfterEach
  @Transactional
  void cleanup() {
    bearbeiterRepository.deleteAll();
  }


  @Test
  void getConfigTest() {

    given()
        .when()
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION,
            BEARER_TOKEN)
        .get("/rest/bearbeiter/config")
        .then()
        .statusCode(200)
        .body(is("{ \"bookmarks\": [1, 5, 7] }")).log();
  }

  @Test
  void setConfig() {
    // change config
    given()
        .when().body("{ \"bookmarks\": [1, 5, 9] }")
        .header("Content-Type", MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION,
            BEARER_TOKEN)
        .post("/rest/bearbeiter/config")
        .then()
        .statusCode(201);

    // check if config was changed
    given()
        .when()
        .header("Content-Type", MediaType.APPLICATION_JSON)
        .header(HttpHeaders.AUTHORIZATION,
            BEARER_TOKEN)
        .get("/rest/bearbeiter/config")
        .then()
        .statusCode(200)
        .body(is("{ \"bookmarks\": [1, 5, 9] }"));
  }

  @Test
  void testGetUsername() throws OAUTH2Exception {

    BearbeiterRestController controller = new BearbeiterRestController(null, jwtParser);

    String username = controller.getAuthenticatedUser(
        BEARER_TOKEN);

    Assertions.assertEquals("b-ke101", username);
  }
}
