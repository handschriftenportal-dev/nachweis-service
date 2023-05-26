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
