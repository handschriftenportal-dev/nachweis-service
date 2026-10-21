package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.oauth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @author Piotr.Czarnecki@sbb.spk-berlin.de
 * @since 13.09.22
 */
class OAUTH2ConfigTest {

  @Test
  void removeEndingForwardSlash() {
    String testling = "http://www.tux.pl/ttt";
    assertEquals(testling, OAUTH2Config.removeEndingForwardSlash(testling));
    assertEquals(testling, OAUTH2Config.removeEndingForwardSlash(testling + "/"));
  }
}
