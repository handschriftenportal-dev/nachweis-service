package de.staatsbibliothek.berlin.hsp.nachweis.application.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * JSF pages must not be cached by the browser: their view-scoped beans are destroyed when a page is
 * left, so a page restored by browser back would post to an empty bean. Faces resources (scripts,
 * styles) stay cacheable.
 */
@QuarkusTest
class CacheControlTest {

  @Test
  void pagesAreNotCached() {
    given().when().get("/datensicherung.xhtml")
        .then().statusCode(200)
        .header("Cache-Control", containsString("no-store"));
  }

  @Test
  void facesResourcesStayCacheable() {
    given().when().get("/jakarta.faces.resource/components.js.xhtml?ln=primefaces")
        .then().statusCode(200)
        .header("Cache-Control", not(containsString("no-store")));
  }
}
