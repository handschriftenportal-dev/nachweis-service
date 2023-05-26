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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURL;
import de.staatsbibliothek.berlin.hsp.domainmodel.valueobjects.PURLTyp;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author michael.hintersonnleitner@sbb.spk-berlin.de
 * @since 04.08.22
 */

public class SBBPURLGeneratorTest {

  SBBPURLGenPort sbbPURLGenPortMock;
  String configuredUrlPrefix = "https://resolver.staatsbibliothek-berlin.de/";
  URI target = URI.create("http://target.url/documentId");

  @BeforeEach
  void init() {
    sbbPURLGenPortMock = Mockito.mock(SBBPURLGenPort.class);
  }

  @Test
  void testCreateNewPURL() throws PURLGeneratorException {

    when(sbbPURLGenPortMock.createPURL(SBBPURLGenPort.PARAM_PID, SBBPURLGenPort.PARAM_MVID))
        .thenReturn(createResponse());

    SBBPURLGenerator generator = new SBBPURLGenerator(configuredUrlPrefix,
        sbbPURLGenPortMock);

    PURL purl = generator.createNewPURL(target);
    assertNotNull(purl);
    assertNotNull(purl.getPurl());
    assertEquals(target, purl.getTarget());
    assertEquals(PURLTyp.INTERNAL, purl.getTyp());
  }

  @Test
  void testCreateNewPURL_GenPortError() {

    when(sbbPURLGenPortMock.createPURL(SBBPURLGenPort.PARAM_PID, SBBPURLGenPort.PARAM_MVID))
        .thenThrow(new RuntimeException("TestException"));

    SBBPURLGenerator generator = new SBBPURLGenerator(configuredUrlPrefix,
        sbbPURLGenPortMock);

    PURLGeneratorException exception = assertThrows(PURLGeneratorException.class, () ->
        generator.createNewPURL(target));
    assertEquals("Error calling sbbPURLGenPort!", exception.getMessage());
  }

  @Test
  void testCreatePID() throws PURLGeneratorException {

    SBBPURLGenerator generator = new SBBPURLGenerator(configuredUrlPrefix,
        sbbPURLGenPortMock);

    NullPointerException npe = assertThrows(NullPointerException.class, generator::createPID);
    assertEquals("Calling sbbPURLGenPort returned no response!", npe.getMessage());

    when(sbbPURLGenPortMock.createPURL(SBBPURLGenPort.PARAM_PID, SBBPURLGenPort.PARAM_MVID))
        .thenReturn(SBBPURLGenResponse.builder().withStatus("ERROR").withMessage("MESSAGE").build());

    PURLGeneratorException exception = assertThrows(PURLGeneratorException.class, generator::createPID);
    assertEquals("Calling sbbPURLGenPort returned status error with message: MESSAGE", exception.getMessage());

    when(sbbPURLGenPortMock.createPURL(SBBPURLGenPort.PARAM_PID, SBBPURLGenPort.PARAM_MVID))
        .thenReturn(SBBPURLGenResponse.builder().withStatus("OK").withMvid("INVALID").build());

    exception = assertThrows(PURLGeneratorException.class, generator::createPID);
    assertEquals("Calling sbbPURLGenPort returned invalid pid: INVALID", exception.getMessage());

    when(sbbPURLGenPortMock.createPURL(SBBPURLGenPort.PARAM_PID, SBBPURLGenPort.PARAM_MVID))
        .thenReturn(createResponse());

    String pid = generator.createPID();
    assertEquals("HSP0000002300000001", pid);
  }

  private SBBPURLGenResponse createResponse() {
    return SBBPURLGenResponse.builder()
        .withStatus("OK")
        .withMessage("")
        .withPid("HSP")
        .withMvid("00000023")
        .withVid("0000")
        .withPage("0001")
        .build();
  }
}
