package de.staatsbibliothek.berlin.hsp.nachweis.infrastructure.purl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 04.08.22
 */

public class SBBPURLGenResponseTest {

  @Test
  void testBuilder() {
    SBBPURLGenResponse response = createResponse();

    assertEquals("OK", response.getStatus());
    assertEquals("message", response.getMessage());
    assertEquals("HSP", response.getPid());
    assertEquals("00000023", response.getMvid());
    assertEquals("0000", response.getVid());
    assertEquals("0001", response.getPage());
  }

  @Test
  void testGetFullPid() {
    SBBPURLGenResponse response = createResponse();

    assertEquals("HSP0000002300000001", response.getFullPid());
  }

  private SBBPURLGenResponse createResponse() {
    return SBBPURLGenResponse.builder()
        .withStatus("OK")
        .withMessage("message")
        .withPid("HSP")
        .withMvid("00000023")
        .withVid("0000")
        .withPage("0001")
        .build();
  }
}
